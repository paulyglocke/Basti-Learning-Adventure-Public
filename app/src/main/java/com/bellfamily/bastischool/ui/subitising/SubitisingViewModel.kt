package com.bellfamily.bastischool.ui.subitising

import android.app.Application
import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.*
import androidx.lifecycle.AndroidViewModel
import com.bellfamily.bastischool.audio.*
import com.bellfamily.bastischool.audio.android.AndroidSystemSpeechEngine
import com.bellfamily.bastischool.learning.subitising.*
import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.progress.AtomicProgressStorage
import com.bellfamily.bastischool.learning.progress.android.*
import com.bellfamily.bastischool.learning.session.*
import java.io.File
import java.util.UUID
import java.util.concurrent.Executors

class SubitisingViewModel @JvmOverloads constructor(
    application: Application,
    engineFactory: () -> SpeechEngine = { AndroidSystemSpeechEngine(application) }
) : AndroidViewModel(application) {
    var state by mutableStateOf<SessionState?>(null); private set
    var busy by mutableStateOf(false); private set
    var saveFailed by mutableStateOf(false); private set
    var audioFailed by mutableStateOf(false); private set
    private val worker = Executors.newSingleThreadExecutor()
    private val main = Handler(Looper.getMainLooper())
    private val progress = AndroidProgressRepository.create(application)
    private val host = SubitisingContent.host(AtomicProgressStorage(File(application.noBackupFilesDir, "subitising-session"), AndroidAtomicCommit), progress)
    private val audio = SubitisingAudio(DefaultAudioController(engineFactory, AudioMode.OFF)) { audioFailed = it is SpeechResult.Failed }
    private var visible = false
    private var closed = false
    private var language = ContentLanguage.ENGLISH
    private var mode = AudioMode.OFF
    private var round = RoundLength.FIVE
    private var epoch = 0L
    fun configure(lang: String, audioMode: String, count: Int) {
        val nextLanguage = if (lang == "de") ContentLanguage.GERMAN else ContentLanguage.ENGLISH
        val nextMode = when (audioMode) { "all" -> AudioMode.ALL; "questions" -> AudioMode.QUESTIONS; else -> AudioMode.OFF }
        round = if (count == 10) RoundLength.TEN else RoundLength.FIVE
        if (nextLanguage != language || nextMode != mode) { language = nextLanguage; mode = nextMode; epoch++; audio.mode(nextMode); syncLanguage() }
    }
    fun setVisible(value: Boolean) {
        if (visible == value) return
        visible = value; epoch++; audio.visible(value)
        if (value && state == null && !busy) {
            val count=round; val lang=language
            work { if (host.state == null) host.open(SessionId(UUID.randomUUID().toString()), count, System.nanoTime(), lang); emptyList() }
        }
    }
    private fun syncLanguage() {
        val current=state ?: return
        val lang=language
        if(current.language!=lang && !busy && !saveFailed) work {host.dispatch(SessionAction.Language(lang))}
    }
    fun replay() {
        val current=state ?: return
        if(!ready())return
        if(current.phase==SessionPhase.ACTIVE) action(SessionAction.Replay(current.task.id))
        else {epoch++;audio.replay(current,language)}
    }
    fun action(action: SessionAction) { if (!ready()) return; epoch++; audio.cancel(); work { host.dispatch(action) } }
    fun retrySave() {
        if(busy)return
        epoch++;audio.cancel();val count=round;val lang=language
        work {if(host.state==null)host.open(SessionId(UUID.randomUUID().toString()),count,System.nanoTime(),lang) else host.retryWrites();emptyList()}
    }
    fun again() {
        if(!ready() || state?.phase!=SessionPhase.COMPLETED)return
        epoch++;audio.cancel();val count=round;val lang=language
        work {host.newRound(SessionId(UUID.randomUUID().toString()),count,System.nanoTime(),lang)}
    }
    private fun ready() = visible && !busy && !saveFailed && state != null && state?.language == language
    private fun work(operation: () -> List<SessionEffect>) {
        if (busy || closed) return
        busy = true; val token = ++epoch
        worker.execute {
            var failed = false; var effects = emptyList<SessionEffect>()
            try { effects = operation() } catch (_: Exception) { failed = true }
            val next = host.state; val pending = host.failure
            main.post {
                if (!closed) {
                    busy = false
                    saveFailed = failed || pending
                    if (!failed) state = next
                    if (!failed && !saveFailed && token == epoch && next != null && visible && next.language == language) {
                        audio.effects(next.plan.id, language, effects)
                    }
                    if (!saveFailed) syncLanguage()
                }
            }
        }
    }
    override fun onCleared() { closed = true; audio.close(); worker.shutdown(); super.onCleared() }
}
