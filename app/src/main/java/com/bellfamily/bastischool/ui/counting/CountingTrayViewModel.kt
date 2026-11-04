package com.bellfamily.bastischool.ui.counting

import android.app.Application
import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.bellfamily.bastischool.audio.*
import com.bellfamily.bastischool.audio.android.AndroidSystemSpeechEngine
import com.bellfamily.bastischool.learning.counting.*
import com.bellfamily.bastischool.learning.models.ContentLanguage
import com.bellfamily.bastischool.learning.progress.AtomicProgressStorage
import com.bellfamily.bastischool.learning.progress.android.AndroidAtomicCommit
import com.bellfamily.bastischool.learning.progress.android.AndroidProgressRepository
import com.bellfamily.bastischool.learning.session.RoundLength
import com.bellfamily.bastischool.learning.session.SessionId
import java.io.File
import java.util.UUID
import java.util.concurrent.Executors

class CountingTrayViewModel @JvmOverloads constructor(
    application: Application,
    engineFactory: () -> SpeechEngine = { AndroidSystemSpeechEngine(application) }
) : AndroidViewModel(application) {
    var state by mutableStateOf<CountingTray.State?>(null); private set
    var busy by mutableStateOf(false); private set
    var saveFailed by mutableStateOf(false); private set
    var audioFailed by mutableStateOf(false); private set

    private val main = Handler(Looper.getMainLooper())
    private val worker = Executors.newSingleThreadExecutor()
    private val host = CountingTrayHost(
        AtomicProgressStorage(File(application.noBackupFilesDir, "counting-tray-session"), AndroidAtomicCommit),
        AndroidProgressRepository.create(application)
    )
    private val audio = CountingTrayAudio(DefaultAudioController(engineFactory, AudioMode.OFF)) { audioFailed = it }
    private var language = ContentLanguage.ENGLISH
    private var mode = AudioMode.OFF
    private var round = RoundLength.FIVE
    private var visible = false
    private var closed = false
    private var loading = false
    private var serial = 0L
    private var speechEpoch = 0L

    fun configure(lang: String, audioMode: String, count: Int) {
        val nextLanguage = if (lang == "de") ContentLanguage.GERMAN else ContentLanguage.ENGLISH
        val nextMode = when (audioMode) { "all" -> AudioMode.ALL; "questions" -> AudioMode.QUESTIONS; else -> AudioMode.OFF }
        round = if (count == 10) RoundLength.TEN else RoundLength.FIVE
        if (nextMode != mode) { mode = nextMode; speechEpoch++; audio.mode(mode) }
        if (nextLanguage != language) {
            language = nextLanguage; speechEpoch++; audio.cancel()
            if (state != null) work { host.dispatch(CountingTray.Action.Language(nextLanguage)) }
        }
    }

    fun setVisible(value: Boolean) {
        if (visible == value) return
        visible = value; speechEpoch++; audio.visible(value)
        if (value && state == null && !loading) open()
    }

    private fun open() {
        loading = true
        val lang = language; val length = round
        work(speech = { opened, _ -> if (opened) SpeechKind.QUESTION else null }) {
            host.open(SessionId(UUID.randomUUID().toString()), length, System.nanoTime(), lang)
        }
    }

    fun toggle(slot: Int) {
        if (!ready()) return
        work { host.dispatch(CountingTray.Action.Toggle(slot)) }
    }

    fun check() {
        if (!ready() || state?.current?.solved == true) return
        interrupt()
        work(speech = { _, _ -> SpeechKind.FEEDBACK }) { host.dispatch(CountingTray.Action.Check) }
    }

    fun next() {
        if (!ready() || state?.current?.solved != true) return
        interrupt()
        work(speech = { _, after -> if (after.completed) SpeechKind.COMPLETION else SpeechKind.QUESTION }) {
            host.dispatch(CountingTray.Action.Next)
        }
    }

    fun replay() {
        if (!ready()) return
        interrupt()
        work(speech = { _, after -> if (after.completed) SpeechKind.COMPLETION else SpeechKind.QUESTION }, manual = true) {
            host.dispatch(CountingTray.Action.Replay)
        }
    }

    fun help() {
        if (!ready() || state?.current?.solved == true) return
        interrupt()
        work(speech = { _, _ -> SpeechKind.EXPLANATION }, manual = true) { host.dispatch(CountingTray.Action.Help) }
    }

    fun again() {
        if (!ready() || state?.completed != true) return
        interrupt(); val lang = language; val length = round
        work(speech = { _, _ -> SpeechKind.QUESTION }) {
            host.again(SessionId(UUID.randomUUID().toString()), length, System.nanoTime(), lang)
        }
    }

    fun retrySave() {
        if (busy) return
        interrupt()
        if (host.state == null) open() else work { host.retryWrites() }
    }

    fun home() { interrupt() }
    private fun interrupt() { speechEpoch++; audio.cancel() }
    private fun ready() = visible && !busy && !saveFailed && state?.language == language

    private fun <T> work(speech: ((Boolean, CountingTray.State) -> SpeechKind?)? = null, manual: Boolean = false,
                         operation: () -> T) {
        if (closed || busy) return
        busy = true; loading = true
        val token = ++serial; val audioToken = speechEpoch
        worker.execute {
            val result = runCatching(operation)
            val next = host.state
            val failed = result.isFailure || host.failure
            val opened = result.getOrNull() as? Boolean ?: false
            main.post {
                if (!closed && token == serial) {
                    busy = false; loading = false; saveFailed = failed
                    if (next != null) state = next
                    if (!failed && next != null) {
                        if (next.language != language) {
                            val currentLanguage = language
                            work { host.dispatch(CountingTray.Action.Language(currentLanguage)) }
                        } else if (visible && audioToken == speechEpoch) {
                            speech?.invoke(opened, next)?.let { audio.speak(next, it, manual) }
                        }
                    }
                }
            }
        }
    }

    override fun onCleared() {
        closed = true; audio.close(); worker.shutdown(); super.onCleared()
    }
}
