package com.bellfamily.bastischool.ui.months

import android.app.Application
import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.*
import androidx.lifecycle.AndroidViewModel
import com.bellfamily.bastischool.audio.*
import com.bellfamily.bastischool.audio.android.AndroidSystemSpeechEngine
import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.months.*
import com.bellfamily.bastischool.learning.progress.AtomicProgressStorage
import com.bellfamily.bastischool.learning.progress.android.AndroidAtomicCommit
import com.bellfamily.bastischool.learning.progress.android.AndroidProgressRepository
import com.bellfamily.bastischool.learning.session.*
import java.util.UUID
import java.io.File
import java.util.concurrent.Executors

class MonthsViewModel @JvmOverloads constructor(application: Application,
    engineFactory: () -> SpeechEngine = { AndroidSystemSpeechEngine(application) }
) : AndroidViewModel(application) {
    var selection by mutableStateOf<MonthsSelection?>(null); private set
    var saveFailed by mutableStateOf(false); private set
    var audioFailed by mutableStateOf(false); private set
    private val main = Handler(Looper.getMainLooper())
    private val worker = Executors.newSingleThreadExecutor()
    private val store = MonthsSelectionStore(AtomicProgressStorage(File(application.noBackupFilesDir, "months-selection"), AndroidAtomicCommit))
    private val audio = MonthsAudio(DefaultAudioController(engineFactory, AudioMode.OFF)) { audioFailed = it }
    private var language = ContentLanguage.ENGLISH
    private var mode = AudioMode.OFF
    private var visible = false
    private var loading = false
    private var closed = false
    private var revision = 0L
    private var speechEpoch = 0L
    private var round = RoundLength.FIVE
    var phase by mutableStateOf(MonthsMode.EXPLORE); private set
    var quizBusy by mutableStateOf(false); private set
    var buildBusy by mutableStateOf(false); private set
    var buildYear by mutableStateOf<MonthsBuildYearState?>(null); private set
    private var buildFailure = false
    val buildYearFailed get() = buildFailure
    private val quizzes = mutableStateMapOf<MonthsMode, SessionState>()
    private val failures = mutableStateMapOf<MonthsMode, Boolean>()
    val quiz get() = quizzes[phase]
    val quizFailed get() = failures[phase] == true
    private val hosts = listOf(MonthsMode.FIND, MonthsMode.RELATIONS, MonthsMode.MISSING).associateWith {
        MonthsQuestions.host(it, AtomicProgressStorage(File(application.noBackupFilesDir,
            "months-${it.name.lowercase()}-session"), AndroidAtomicCommit), AndroidProgressRepository.create(application))
    }
    private val buildHost = MonthsBuildYearHost(
        AtomicProgressStorage(File(application.noBackupFilesDir, "months-build-year-three-session"), AndroidAtomicCommit),
        AndroidProgressRepository.create(application))

    fun configure(lang: String, sound: String, count: Int = 5) {
        val nextLanguage = if (lang == "de") ContentLanguage.GERMAN else ContentLanguage.ENGLISH
        val nextMode = when (sound) { "all" -> AudioMode.ALL; "questions" -> AudioMode.QUESTIONS; else -> AudioMode.OFF }
        round = if (count == 10) RoundLength.TEN else RoundLength.FIVE
        if (nextLanguage != language) { language = nextLanguage; speechEpoch++; audio.cancel() }
        if (nextMode != mode) { mode = nextMode; speechEpoch++; audio.mode(mode) }
        ensureQuiz()
        ensureBuildYear()
    }
    fun setVisible(value: Boolean, next: MonthsMode = MonthsMode.EXPLORE) {
        if (visible == value && phase == next) return
        visible = value; phase = next; speechEpoch++; audio.visible(value)
        if (value && next == MonthsMode.EXPLORE && selection == null) load()
        ensureQuiz()
        ensureBuildYear()
    }
    private fun load() {
        if (loading || closed) return
        loading = true
        worker.execute {
            val result = runCatching { store.read() }
            main.post {
                if (!closed) {
                    loading = false; saveFailed = result.isFailure
                    result.getOrNull()?.let { selection = it } // Never narrate restoration.
                }
            }
        }
    }
    fun select(id: ContentId) {
        if (!visible || phase != MonthsMode.EXPLORE || selection == null || saveFailed || closed) return
        val next = MonthsSelection(id)
        if (next != selection) { selection = next; persist(next) }
        audio.month(id, language) // Re-tapping the selected month explicitly listens again.
    }
    fun listen() { if (visible && phase == MonthsMode.EXPLORE) selection?.let { audio.month(it.selected, language) } }
    fun retry() { if (selection == null) load() else persist(selection!!) }
    private fun persist(snapshot: MonthsSelection) {
        if (closed) return
        val ticket = ++revision
        worker.execute {
            val result = runCatching { store.write(snapshot) }
            main.post { if (!closed && revision == ticket) saveFailed = result.isFailure }
        }
    }
    private fun ensureQuiz() {
        if (!visible || phase == MonthsMode.EXPLORE || phase == MonthsMode.BUILD_YEAR || quizBusy || closed || quizFailed) return
        val current = quiz
        val lang = language; val count = round
        if (current == null) quizWork { it.open(SessionId(UUID.randomUUID().toString()), count, System.nanoTime(), lang) }
        else if (current.language != lang) quizWork(speak = false) { it.dispatch(SessionAction.Language(lang)) }
    }
    private fun ensureBuildYear() {
        if (!visible || phase != MonthsMode.BUILD_YEAR || buildBusy || closed || buildFailure) return
        val current = buildYear
        val lang = language
        if (current == null) buildWork { it.open(SessionId(UUID.randomUUID().toString()), System.nanoTime(), lang) }
        else if (current.language != lang) buildWork(speak = false) { it.dispatch(MonthsBuildYearAction.Language(lang)) }
    }
    fun buildAction(action: MonthsBuildYearAction) {
        if (!buildReady()) return
        audio.cancel()
        buildWork { it.dispatch(action) }
    }
    fun buildReplay() { buildYear?.let { buildAction(MonthsBuildYearAction.Replay(it.task)) } }
    fun buildOption(id: ContentId) {
        val state = buildYear ?: return
        if (!buildReady() || id !in state.choices || state.completed || state.current.answer != AnswerState.UNANSWERED) return
        speechEpoch++; audio.month(id, language)
    }
    fun buildAgain() {
        val state = buildYear ?: return
        if (!buildReady() || !state.completed) return
        val lang = language
        audio.cancel()
        buildWork { it.again(SessionId(UUID.randomUUID().toString()), System.nanoTime(), lang) }
    }
    fun retryBuildYear() {
        if (buildBusy || phase != MonthsMode.BUILD_YEAR) return
        val lang = language
        audio.cancel()
        buildWork(speak = false) {
            if (it.state == null) it.open(SessionId(UUID.randomUUID().toString()), System.nanoTime(), lang)
            else it.retryWrites()
            null
        }
    }
    private fun buildReady() = visible && phase == MonthsMode.BUILD_YEAR && !buildBusy && !buildFailure && buildYear?.language == language
    private fun buildWork(speak: Boolean = true, operation: (MonthsBuildYearHost) -> SessionEffect.Narrate?) {
        if (buildBusy || closed || phase != MonthsMode.BUILD_YEAR) return
        buildBusy = true
        val ticket = ++speechEpoch
        worker.execute {
            val result = runCatching { operation(buildHost) }
            val state = buildHost.state
            val failed = result.isFailure || buildHost.failure
            main.post {
                if (!closed) {
                    buildBusy = false; buildFailure = failed
                    if (state != null) buildYear = state
                    if (!failed && state != null && visible && phase == MonthsMode.BUILD_YEAR && ticket == speechEpoch && state.language == language && speak) {
                        audio.effects(state.id, language, listOfNotNull(result.getOrNull()))
                    }
                    ensureBuildYear()
                }
            }
        }
    }
    private fun ready() = visible && phase != MonthsMode.EXPLORE && !quizBusy && !quizFailed && quiz?.language == language
    fun action(action: SessionAction) {
        if (!ready()) return
        audio.cancel()
        quizWork { it.dispatch(action) }
    }
    fun replay() { quiz?.let { action(SessionAction.Replay(it.task.id)) } }
    fun option(id: ContentId) {
        if (!ready() || id !in quiz!!.task.question.choices || quiz!!.phase != SessionPhase.ACTIVE) return
        speechEpoch++; audio.month(id, language)
    }
    /** Saving assistance precedes exposing the labelled Explore wheel. No attempt is created. */
    fun learn(onExplore: () -> Unit) {
        if (!ready()) return
        val current = quiz ?: return
        if (current.current.locked || current.phase == SessionPhase.COMPLETED) { onExplore(); return }
        audio.cancel()
        quizWork(speak = false, after = onExplore) { it.dispatch(SessionAction.Hint(current.task.id)) }
    }
    fun again() {
        if (!ready() || quiz?.phase != SessionPhase.COMPLETED) return
        val lang = language; val count = round
        audio.cancel()
        quizWork { it.newRound(SessionId(UUID.randomUUID().toString()), count, System.nanoTime(), lang) }
    }
    fun retryQuiz() {
        if (quizBusy || phase == MonthsMode.EXPLORE) return
        val lang = language; val count = round
        audio.cancel()
        quizWork(speak = false) {
            if (it.state == null) it.open(SessionId(UUID.randomUUID().toString()), count, System.nanoTime(), lang)
            else it.retryWrites()
            emptyList()
        }
    }
    private fun quizWork(speak: Boolean = true, after: (() -> Unit)? = null,
        operation: (DurableSessionHost) -> List<SessionEffect>) {
        if (quizBusy || closed || phase == MonthsMode.EXPLORE) return
        val destination = phase; val host = hosts.getValue(destination)
        quizBusy = true; val ticket = ++speechEpoch
        worker.execute {
            val result = runCatching { operation(host) }
            val state = host.state
            val failed = result.isFailure || host.failure
            main.post {
                if (!closed) {
                    // Accepted state/busy publication is independent of cancelled speech/navigation.
                    quizBusy = false; failures[destination] = failed
                    if (state != null) quizzes[destination] = state
                    if (!failed && state != null && visible && phase == destination && ticket == speechEpoch && state.language == language) {
                        if (speak) audio.effects(state.plan.id, language, result.getOrDefault(emptyList()))
                        after?.invoke()
                    }
                    ensureQuiz()
                }
            }
        }
    }
    override fun onCleared() {
        closed = true; audio.close()
        worker.shutdown() // Already queued selections finish in order; callbacks cannot publish.
        super.onCleared()
    }
}
