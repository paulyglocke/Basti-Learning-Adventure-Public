package com.bellfamily.bastischool.ui.months

import android.app.Application
import android.graphics.BitmapFactory
import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.AndroidViewModel
import com.bellfamily.bastischool.audio.*
import com.bellfamily.bastischool.audio.android.AndroidSystemSpeechEngine
import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.months.*
import com.bellfamily.bastischool.learning.content.SeasonIds
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
    var seasonArtwork by mutableStateOf<Map<ContentId, ImageBitmap>>(emptyMap()); private set
    var artworkFailed by mutableStateOf(false); private set
    private val main = Handler(Looper.getMainLooper())
    private val worker = Executors.newSingleThreadExecutor()
    private val store = MonthsSelectionStore(AtomicProgressStorage(File(application.noBackupFilesDir, "months-selection"), AndroidAtomicCommit))
    private val wheelStore = MonthsSeasonWheelStore(AtomicProgressStorage(File(application.noBackupFilesDir,
        "months-seasons-wheel-v1"), AndroidAtomicCommit))
    var wheel by mutableStateOf<MonthsSeasonWheelState?>(null); private set
    var wheelFailed by mutableStateOf(false); private set
    private var wheelLoading = false
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
    var buildYearFour by mutableStateOf<MonthsBuildYearFourState?>(null); private set
    var buildYearFourBusy by mutableStateOf(false); private set
    private var buildYearFourFailure = false
    val buildYearFourFailed get() = buildYearFourFailure
    private val quizzes = mutableStateMapOf<MonthsMode, SessionState>()
    private val failures = mutableStateMapOf<MonthsMode, Boolean>()
    val quiz get() = quizzes[phase]
    val quizFailed get() = failures[phase] == true
    private val hosts = listOf(MonthsMode.FIND, MonthsMode.RELATIONS, MonthsMode.MISSING, MonthsMode.BETWEEN, MonthsMode.SEASONS_MATCH).associateWith {
        MonthsQuestions.host(it, AtomicProgressStorage(File(application.noBackupFilesDir,
            when (it) {
                MonthsMode.BETWEEN -> "months-between-session"
                MonthsMode.SEASONS_MATCH -> "months-seasons-session"
                else -> "months-${it.name.lowercase()}-session"
            }),
            AndroidAtomicCommit), AndroidProgressRepository.create(application))
    }
    private val buildHost = MonthsBuildYearHost(
        AtomicProgressStorage(File(application.noBackupFilesDir, "months-build-year-three-session"), AndroidAtomicCommit),
        AndroidProgressRepository.create(application))
    private val buildFourHost = MonthsBuildYearFourHost(
        AtomicProgressStorage(File(application.noBackupFilesDir, "months-build-year-four-session"), AndroidAtomicCommit),
        AndroidProgressRepository.create(application))

    fun configure(lang: String, sound: String, count: Int = 5) {
        val nextLanguage = if (lang == "de") ContentLanguage.GERMAN else ContentLanguage.ENGLISH
        val nextMode = when (sound) { "all" -> AudioMode.ALL; "questions" -> AudioMode.QUESTIONS; else -> AudioMode.OFF }
        round = if (count == 10) RoundLength.TEN else RoundLength.FIVE
        if (nextLanguage != language) { language = nextLanguage; speechEpoch++; audio.cancel() }
        if (nextMode != mode) { mode = nextMode; speechEpoch++; audio.mode(mode) }
        ensureQuiz()
        ensureBuildYear()
        ensureBuildYearFour()
    }
    fun setVisible(value: Boolean, next: MonthsMode = MonthsMode.EXPLORE) {
        if (visible == value && phase == next) return
        visible = value; phase = next; speechEpoch++; audio.visible(value)
        if (value && next == MonthsMode.EXPLORE && selection == null) load()
        if (value && next == MonthsMode.SEASONS_WHEEL && wheel == null) loadWheel()
        ensureQuiz()
        ensureBuildYear()
        ensureBuildYearFour()
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
    private fun loadWheel() {
        if (wheelLoading || closed) return
        wheelLoading = true
        worker.execute {
            val result = runCatching {
                val restored = wheelStore.read()
                (restored ?: MonthsSeasonWheelState.new(System.nanoTime()).also(wheelStore::write)) to (restored == null)
            }
            main.post {
                if (!closed) {
                    wheelLoading = false; wheelFailed = result.isFailure
                    result.getOrNull()?.let { (state, fresh) ->
                        wheel = state
                        if (fresh && visible && phase == MonthsMode.SEASONS_WHEEL)
                            audio.effects(SessionId("months-seasons-wheel"), language, listOf(SessionEffect.Narrate(
                                ContentText.plain("Turn the months so each group of three matches its season. Then choose Check.",
                                    "Drehe die Monate, bis jede Dreiergruppe zu ihrer Jahreszeit passt. Tippe dann auf Prüfen."),
                                NarrationKind.INSTRUCTION)))
                    } // Restoration is silent.
                }
            }
        }
    }
    private fun updateWheel(change: (MonthsSeasonWheelState) -> MonthsSeasonWheelState) {
        if (!visible || phase != MonthsMode.SEASONS_WHEEL || wheelFailed || closed) return
        val before = wheel ?: return
        val next = change(before)
        if (next == before) return
        wheel = next
        worker.execute {
            val result = runCatching { wheelStore.write(next) }
            main.post { if (!closed && wheel == next) wheelFailed = result.isFailure }
        }
    }
    fun wheelRotate(steps: Int) {
        audio.cancel()
        updateWheel { it.rotate(steps) }
    }
    fun wheelChangeWeather() {
        audio.cancel()
        updateWheel { it.changeWeather() }
    }
    fun wheelToggleWeather() {
        audio.cancel()
        updateWheel { if (it.mode == MonthsWheelMode.EXPLORE) it.copy(weatherEnabled = !it.weatherEnabled) else it }
    }
    fun wheelWeatherListen() {
        if (!visible || phase != MonthsMode.SEASONS_WHEEL || closed) return
        val state = wheel?.takeIf { it.showsWeather } ?: return
        audio.effects(SessionId("months-seasons-wheel"), language, listOf(SessionEffect.Narrate(
            MonthsWeatherContent.narration(state.selectedMonth, state.weather), NarrationKind.INSTRUCTION, SpeechTrigger.MANUAL)))
    }
    fun wheelMode(mode: MonthsWheelMode) {
        audio.cancel()
        updateWheel { it.copy(mode = mode, feedback = MonthsWheelFeedback.READY) }
    }
    fun wheelCheck() {
        val before = wheel ?: return
        updateWheel { it.check() }
        val after = wheel ?: return
        if (after == before) return
        val message = if (after.feedback == MonthsWheelFeedback.COMPLETE)
            ContentText.plain("Yes! Every month is with its season. Well done!", "Ja! Jeder Monat gehört zu seiner Jahreszeit. Sehr gut!")
        else ContentText.plain("Take another look. You can turn the wheel and try again.",
            "Schau noch einmal hin. Du kannst das Rad drehen und es erneut versuchen.")
        audio.effects(SessionId("months-seasons-wheel"), language,
            listOf(SessionEffect.Narrate(message, if (after.feedback == MonthsWheelFeedback.COMPLETE)
                NarrationKind.COMPLETION else NarrationKind.FEEDBACK)))
    }
    fun wheelReplay() {
        updateWheel { it.copy(replayCount = it.replayCount + 1) }
        val text = if (wheel?.mode == MonthsWheelMode.EXPLORE)
            ContentText.plain("Turn the months. Touch a month to hear its name.", "Drehe die Monate. Tippe auf einen Monat, um seinen Namen zu hören.")
        else ContentText.plain("Turn the months so each group of three matches its season. Then choose Check.",
            "Drehe die Monate, bis jede Dreiergruppe zu ihrer Jahreszeit passt. Tippe dann auf Prüfen.")
        audio.effects(SessionId("months-seasons-wheel"), language,
            listOf(SessionEffect.Narrate(text, NarrationKind.INSTRUCTION, SpeechTrigger.REPLAY)))
    }
    fun wheelHint() {
        updateWheel { it.copy(hintUsed = true) }
        audio.effects(SessionId("months-seasons-wheel"), language,
            listOf(SessionEffect.Narrate(ContentText.plain("Look for December, January and February together in winter.",
                "Suche Dezember, Januar und Februar zusammen im Winter."), NarrationKind.INSTRUCTION, SpeechTrigger.MANUAL)))
    }
    fun wheelMonth(id: ContentId) {
        if (visible && phase == MonthsMode.SEASONS_WHEEL) audio.month(id, language)
    }
    fun wheelShowAlignment() {
        audio.cancel()
        updateWheel { it.showAlignment() }
    }
    fun wheelAgain() {
        audio.cancel()
        updateWheel { MonthsSeasonWheelState.new(System.nanoTime(), it.round + 1) }
    }
    fun wheelRetry() {
        if (wheel == null) loadWheel() else {
            val snapshot = wheel ?: return
            worker.execute {
                val result = runCatching { wheelStore.write(snapshot) }
                main.post { if (!closed) wheelFailed = result.isFailure }
            }
        }
    }
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
        if (!visible || phase in setOf(MonthsMode.EXPLORE, MonthsMode.SEASONS_WHEEL, MonthsMode.BUILD_YEAR, MonthsMode.BUILD_YEAR_LEVELS, MonthsMode.BUILD_YEAR_FOUR) || quizBusy || closed || quizFailed) return
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
    private fun ensureBuildYearFour() {
        if (!visible || phase != MonthsMode.BUILD_YEAR_FOUR || buildYearFourBusy || closed || buildYearFourFailure) return
        val current = buildYearFour
        val lang = language; val count = round
        if (current == null) buildYearFourWork { it.open(SessionId(UUID.randomUUID().toString()), count, System.nanoTime(), lang) }
        else if (current.language != lang) buildYearFourWork(speak = false) { it.dispatch(MonthsBuildYearFourAction.Language(lang)) }
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
    fun buildYearFourAction(action: MonthsBuildYearFourAction) {
        if (!buildYearFourReady()) return
        audio.cancel()
        buildYearFourWork { it.dispatch(action) }
    }
    fun buildYearFourReplay() { buildYearFour?.let { buildYearFourAction(MonthsBuildYearFourAction.Replay(it.task)) } }
    fun buildYearFourOption(id: ContentId) {
        val state = buildYearFour ?: return
        if (!buildYearFourReady() || id !in state.choices[state.questionIndex] || state.sequenceReady || state.completed ||
            state.current.answer != AnswerState.UNANSWERED) return
        speechEpoch++; audio.month(id, language)
    }
    fun buildYearFourAgain() {
        val state = buildYearFour ?: return
        if (!buildYearFourReady() || !state.completed) return
        val lang = language; val count = round
        audio.cancel()
        buildYearFourWork { it.again(SessionId(UUID.randomUUID().toString()), count, System.nanoTime(), lang) }
    }
    fun retryBuildYearFour() {
        if (buildYearFourBusy || phase != MonthsMode.BUILD_YEAR_FOUR) return
        val lang = language; val count = round
        audio.cancel()
        buildYearFourWork(speak = false) {
            if (it.state == null) it.open(SessionId(UUID.randomUUID().toString()), count, System.nanoTime(), lang)
            else it.retryWrites()
            null
        }
    }
    private fun buildYearFourReady() = visible && phase == MonthsMode.BUILD_YEAR_FOUR && !buildYearFourBusy &&
        !buildYearFourFailure && buildYearFour?.language == language
    private fun buildYearFourWork(speak: Boolean = true, operation: (MonthsBuildYearFourHost) -> SessionEffect.Narrate?) {
        if (buildYearFourBusy || closed || phase != MonthsMode.BUILD_YEAR_FOUR) return
        buildYearFourBusy = true
        val ticket = ++speechEpoch
        worker.execute {
            val result = runCatching { operation(buildFourHost) }
            val state = buildFourHost.state
            val failed = result.isFailure || buildFourHost.failure
            main.post {
                if (!closed) {
                    buildYearFourBusy = false; buildYearFourFailure = failed
                    if (state != null) buildYearFour = state
                    if (!failed && state != null && visible && phase == MonthsMode.BUILD_YEAR_FOUR && ticket == speechEpoch &&
                        state.language == language && speak) {
                        audio.effects(state.id, language, listOfNotNull(result.getOrNull()))
                    }
                    ensureBuildYearFour()
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
            val artResult = if (destination == MonthsMode.SEASONS_MATCH && seasonArtwork.isEmpty()) runCatching {
                val repository = com.bellfamily.bastischool.learning.content.CoreContent.repository()
                SeasonIds.canonicalOrder.associateWith { seasonId ->
                    val season = repository.find(seasonId) as SeasonDefinition
                    getApplication<Application>().assets.open(requireNotNull(repository.image(season.illustration)).path).use { stream ->
                        requireNotNull(BitmapFactory.decodeStream(stream, null, BitmapFactory.Options().apply { inSampleSize = 2 })).asImageBitmap()
                    }
                }
            } else null
            main.post {
                if (!closed) {
                    // Accepted state/busy publication is independent of cancelled speech/navigation.
                    quizBusy = false; failures[destination] = failed
                    if (state != null) quizzes[destination] = state
                    if (artResult != null) {
                        seasonArtwork = artResult.getOrNull() ?: emptyMap()
                        artworkFailed = artResult.isFailure
                    }
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
