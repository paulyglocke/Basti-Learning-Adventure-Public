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

class MatchColoursViewModel @JvmOverloads constructor(
    application: Application,
    engineFactory: () -> SpeechEngine = { AndroidSystemSpeechEngine(application) }
) : AndroidViewModel(application) {
    var state by mutableStateOf<SessionState?>(null); private set
    var busy by mutableStateOf(false); private set
    var saveFailed by mutableStateOf(false); private set
    var audioFailed by mutableStateOf(false); private set
    private val host = MatchColoursHost(AtomicProgressStorage(File(application.noBackupFilesDir, "colours-match-session"), AndroidAtomicCommit), AndroidProgressRepository.create(application))
    private val worker = Executors.newSingleThreadExecutor()
    private val main = Handler(Looper.getMainLooper())
    private val audio = DefaultAudioController(engineFactory, AudioMode.OFF)
    private val owner = SpeechOwner("native-colours-match")
    private var visible = false
    private var closed = false
    private var language = ContentLanguage.ENGLISH
    private var mode = AudioMode.OFF
    private var round = RoundLength.FIVE
    private var epoch = 0L
    fun configure(lang: String, audioMode: String, count: Int) {
        val nextLanguage = if (lang == "de") ContentLanguage.GERMAN else ContentLanguage.ENGLISH
        val nextMode = when (audioMode) { "all" -> AudioMode.ALL; "questions" -> AudioMode.QUESTIONS; else -> AudioMode.OFF }
        val changed = language != nextLanguage || mode != nextMode
        language = nextLanguage; mode = nextMode; round = if (count == 10) RoundLength.TEN else RoundLength.FIVE
        if (changed) { epoch++; audio.cancelOwner(owner); audio.setMode(mode); syncLanguage() }
    }
    fun setVisible(value: Boolean) { if (visible == value) return; visible = value; epoch++; if (!value) audio.cancelOwner(owner); if (value && state == null && !busy) work { host.open(SessionId(UUID.randomUUID().toString()), round, System.nanoTime(), language) } }
    fun action(action: SessionAction) { if (visible && !busy && !saveFailed && state?.language == language) { epoch++; audio.cancelOwner(owner); work { host.dispatch(action) } } }
    fun replay() = state?.let { action(SessionAction.Replay(it.task.id)) }
    fun help() = state?.let { action(SessionAction.Hint(it.task.id)) }
    fun again() { if (state?.phase == SessionPhase.COMPLETED) { epoch++; work { host.newRound(SessionId(UUID.randomUUID().toString()), round, System.nanoTime(), language) } } }
    fun retry() { if (!busy) { epoch++; work { host.retryWrites(); emptyList() } } }
    private fun syncLanguage() { if (!busy && host.state != null && host.state?.language != language) work { host.dispatch(SessionAction.Language(language)) } }
    private fun work(operation: () -> List<SessionEffect>) {
        if (busy || closed) return
        busy = true; val ticket = epoch
        worker.execute {
            var failed = false
            val effects = try { operation() } catch (_: Exception) { failed = true; emptyList() }
            val current = host.state; val pending = host.failure
            main.post { if (!closed) {
                busy = false; saveFailed = failed || pending
                if (!failed) state = current
                if (!failed && visible && ticket == epoch && current?.language == language) effects.forEach { effect ->
                    val text = when (effect) { is SessionEffect.Narrate -> effect.text; else -> null }
                    text?.let { value ->
                        val kind = when ((effect as SessionEffect.Narrate).kind) { NarrationKind.FEEDBACK -> SpeechKind.FEEDBACK; NarrationKind.COMPLETION -> SpeechKind.COMPLETION; else -> SpeechKind.INSTRUCTION }
                        val context = audio.openContext(owner, SpeechSessionId(current.plan.id.value), language)
                        audio.speak(SpeechRequest.fromContent(context, value, kind, effect.trigger)) { if (!closed && visible && ticket == epoch) audioFailed = it.result is SpeechResult.Failed }
                    }
                }
                if (!failed && visible && current == null) work { host.open(SessionId(UUID.randomUUID().toString()), round, System.nanoTime(), language) }
            } }
        }
    }
    override fun onCleared() { closed = true; audio.cancelOwner(owner); audio.close(); worker.shutdown(); super.onCleared() }
}
