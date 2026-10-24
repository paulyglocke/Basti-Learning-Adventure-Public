package com.bellfamily.bastischool.ui.colours

import android.app.Application
import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.*
import androidx.lifecycle.AndroidViewModel
import com.bellfamily.bastischool.audio.*
import com.bellfamily.bastischool.audio.android.AndroidSystemSpeechEngine
import com.bellfamily.bastischool.learning.colours.*
import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.progress.AtomicProgressStorage
import com.bellfamily.bastischool.learning.progress.android.*
import com.bellfamily.bastischool.learning.session.*
import java.io.File
import java.util.UUID
import java.util.concurrent.Executors

class ColoursViewModel @JvmOverloads constructor(
    application: Application,
    engineFactory: () -> SpeechEngine = { AndroidSystemSpeechEngine(application) }
) : AndroidViewModel(application) {
    var selected by mutableStateOf(ColoursContent.red); private set
    var quiz by mutableStateOf<SessionState?>(null); private set
    var busy by mutableStateOf(false); private set
    var saveFailed by mutableStateOf(false); private set
    var audioFailed by mutableStateOf(false); private set

    private fun storage(name: String) = AtomicProgressStorage(File(getApplication<Application>().noBackupFilesDir, name), AndroidAtomicCommit)
    private val browse = ColoursSelectionStore(storage("colours-explore-selection"))
    private val progress = AndroidProgressRepository.create(application)
    private val host = ColoursHost(storage("colours-find-session"), progress)
    private val worker = Executors.newSingleThreadExecutor()
    private val main = Handler(Looper.getMainLooper())
    private val audio = ColoursAudio(DefaultAudioController(engineFactory, AudioMode.OFF)) {
        audioFailed = it is SpeechResult.Failed
    }
    private var visible = false
    private var findActive = false
    private var selectionLoaded = false
    private var closed = false
    private var epoch = 0L
    private var language = ContentLanguage.ENGLISH
    private var round = RoundLength.FIVE
    private var mode = AudioMode.OFF

    private fun sessionId() = SessionId(UUID.randomUUID().toString())

    fun configure(lang: String, audioMode: String, count: Int) {
        val nextLanguage = if (lang == "de") ContentLanguage.GERMAN else ContentLanguage.ENGLISH
        val nextMode = when (audioMode) { "all" -> AudioMode.ALL; "questions" -> AudioMode.QUESTIONS; else -> AudioMode.OFF }
        val languageChanged = language != nextLanguage
        if (languageChanged || mode != nextMode) { epoch++; audio.mode(nextMode) }
        language = nextLanguage
        mode = nextMode
        round = if (count == 10) RoundLength.TEN else RoundLength.FIVE
        if (languageChanged && host.state != null && !busy && !saveFailed) {
            work { host.dispatch(SessionAction.Language(language)); emptyList() }
        }
    }

    fun setVisible(value: Boolean, find: Boolean) {
        if (visible == value && findActive == find) return
        visible = value
        findActive = find
        epoch++
        audio.visible(value)
        if (!value || closed || busy) return
        val lang = language
        work {
            if (!selectionLoaded) { selected = browse.read(); selectionLoaded = true }
            val effects = if (findActive && host.state == null) host.open(sessionId(), round, System.nanoTime(), lang)
                else if (findActive && host.state?.language != lang) host.dispatch(SessionAction.Language(lang))
                else emptyList()
            effects
        }
    }

    fun select(id: ContentId) {
        if (!ready() || !visible || findActive) return
        ColoursContent.definition(id)
        epoch++
        val token = epoch
        work(operation = {
            browse.write(id)
            selected = id
            emptyList()
        }, after = { if (visible && !findActive && token == epoch) audio.word(id, language) })
    }

    fun replay() {
        if (!ready()) return
        if (findActive) quiz?.let { action(SessionAction.Replay(it.task.id)) }
        else { epoch++; audio.word(selected, language, replay = true) }
    }

    fun speakRainbow(index: Int) {
        if (!ready() || !visible || index !in RainbowContent.order.indices) return
        val id = RainbowContent.order[index]
        audio.say("rainbow-explore", language, RainbowContent.definition(id).text)
    }

    fun speakRainbowOrder(help: Boolean = false) {
        if (!ready() || !visible) return
        audio.say("rainbow-order", language, if (help) RainbowContent.orderHelp else RainbowContent.orderInstruction)
    }

    fun help() { quiz?.let { if (ready() && findActive) action(SessionAction.Hint(it.task.id)) } }

    fun action(action: SessionAction) {
        if (!ready() || !findActive || host.state?.language != language) return
        epoch++
        work { host.dispatch(action) }
    }

    fun again() {
        if (!ready() || !findActive || quiz?.phase != SessionPhase.COMPLETED) return
        epoch++
        val count = round
        val lang = language
        work { host.newRound(sessionId(), count, System.nanoTime(), lang) }
    }

    fun retry() {
        if (busy) return
        epoch++
        val lang = language
        work {
            if (host.failure) host.retryWrites()
            if (!selectionLoaded) { selected = browse.read(); selectionLoaded = true }
            if (findActive && host.state == null) host.open(sessionId(), round, System.nanoTime(), lang)
            else if (findActive && host.state?.language != lang) host.dispatch(SessionAction.Language(lang))
            else emptyList()
        }
    }

    private fun ready() = !busy && !saveFailed

    private fun work(after: ((List<SessionEffect>) -> Unit)? = null, operation: () -> List<SessionEffect>) {
        if (busy || closed) return
        busy = true
        val token = epoch
        worker.execute {
            var failed = false
            val effects = try { operation() } catch (_: Exception) { failed = true; emptyList() }
            val current = host.state
            val pendingFailure = host.failure
            main.post {
                if (!closed) {
                    busy = false
                    saveFailed = failed || pendingFailure
                    if (!failed) quiz = current
                    if (!failed && visible && token == epoch) {
                        if (findActive && current?.language == language)
                            audio.effects(current.plan.id, language, effects)
                        after?.invoke(effects)
                    }
                    if (!failed && visible && findActive && current == null) {
                        work { if (host.state == null) host.open(sessionId(), round, System.nanoTime(), language) else emptyList() }
                    }
                }
            }
        }
    }

    override fun onCleared() { closed = true; audio.close(); worker.shutdown(); super.onCleared() }
}
