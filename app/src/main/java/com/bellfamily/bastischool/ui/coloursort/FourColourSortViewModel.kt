package com.bellfamily.bastischool.ui.coloursort

import android.app.Application
import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.*
import androidx.lifecycle.AndroidViewModel
import com.bellfamily.bastischool.audio.*
import com.bellfamily.bastischool.audio.android.AndroidSystemSpeechEngine
import com.bellfamily.bastischool.learning.coloursort.*
import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.progress.AtomicProgressStorage
import com.bellfamily.bastischool.learning.progress.android.*
import com.bellfamily.bastischool.learning.session.*
import com.bellfamily.bastischool.learning.sorting.*
import java.io.File
import java.util.UUID
import java.util.concurrent.Executors

class FourColourSortViewModel @JvmOverloads constructor(application: Application,
    engineFactory: () -> SpeechEngine = { AndroidSystemSpeechEngine(application) }) : AndroidViewModel(application) {
    var state by mutableStateOf<SortState?>(null); private set
    var busy by mutableStateOf(false); private set
    var saveFailed by mutableStateOf(false); private set
    var audioFailed by mutableStateOf(false); private set
    private val host = FourColourSortHost(AtomicProgressStorage(File(application.noBackupFilesDir, "four-colour-sort-session"), AndroidAtomicCommit), AndroidProgressRepository.create(application))
    private val worker = Executors.newSingleThreadExecutor(); private val main = Handler(Looper.getMainLooper())
    private val audio = DefaultAudioController(engineFactory, AudioMode.OFF); private val owner = SpeechOwner("native-four-colour-sort")
    private var visible = false; private var closed = false; private var language = ContentLanguage.ENGLISH; private var mode = AudioMode.OFF; private var epoch = 0L
    fun configure(lang: String, audioMode: String) {
        val next = if (lang == "de") ContentLanguage.GERMAN else ContentLanguage.ENGLISH
        val nextMode = when (audioMode) { "all" -> AudioMode.ALL; "questions" -> AudioMode.QUESTIONS; else -> AudioMode.OFF }
        if (language != next || mode != nextMode) { language = next; mode = nextMode; epoch++; audio.cancelOwner(owner); audio.setMode(mode); syncLanguage() }
    }
    fun setVisible(value: Boolean) { if (visible == value) return; visible = value; epoch++; if (!value) audio.cancelOwner(owner); if (value && state == null && !busy) work { host.open(SessionId(UUID.randomUUID().toString()), System.nanoTime(), language) } }
    fun action(action: SortAction) { if (visible && !busy && !saveFailed && state?.language == language) { epoch++; audio.cancelOwner(owner); work { host.dispatch(action) } }
    }
    fun again() { if (state?.completed == true) { epoch++; work { host.again(SessionId(UUID.randomUUID().toString()), System.nanoTime(), language) } } }
    fun retry() { if (!busy) { epoch++; work { host.retryWrites(); null } } }
    private fun syncLanguage() { if (!busy && host.state != null && host.state?.language != language) work { host.dispatch(SortAction.Language(language)) } }
    private fun work(operation: () -> SessionEffect.Narrate?) {
        if (busy || closed) return
        busy = true; val ticket = epoch
        worker.execute { var failed = false; val effect = try { operation() } catch (_: Exception) { failed = true; null }; val current = host.state; val pending = host.failure
            main.post { if (!closed) { busy = false; saveFailed = failed || pending; if (!failed) state = current
                if (!failed && visible && ticket == epoch && current?.language == language && effect != null) {
                    val kind = when (effect.kind) { NarrationKind.FEEDBACK -> SpeechKind.FEEDBACK; NarrationKind.COMPLETION -> SpeechKind.COMPLETION; else -> SpeechKind.INSTRUCTION }
                    val context = audio.openContext(owner, SpeechSessionId(current.id.value), language)
                    audio.speak(SpeechRequest.fromContent(context, effect.text, kind, effect.trigger)) { if (!closed && visible && ticket == epoch) audioFailed = it.result is SpeechResult.Failed }
                }
                if (!failed && visible && current == null) work { host.open(SessionId(UUID.randomUUID().toString()), System.nanoTime(), language) }
            } }
        }
    }
    override fun onCleared() { closed = true; audio.cancelOwner(owner); audio.close(); worker.shutdown(); super.onCleared() }
}
