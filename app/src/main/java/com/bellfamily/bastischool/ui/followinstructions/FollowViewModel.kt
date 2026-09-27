package com.bellfamily.bastischool.ui.followinstructions

import android.app.Application
import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.AndroidViewModel
import com.bellfamily.bastischool.audio.*
import com.bellfamily.bastischool.audio.android.AndroidSystemSpeechEngine
import com.bellfamily.bastischool.learning.follow.*
import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.progress.AtomicProgressStorage
import com.bellfamily.bastischool.learning.progress.android.*
import com.bellfamily.bastischool.learning.session.*
import java.io.File
import java.util.UUID
import java.util.concurrent.Executors

class FollowViewModel @JvmOverloads constructor(
    application: Application,
    engineFactory: () -> SpeechEngine = { AndroidSystemSpeechEngine(application) }
) : AndroidViewModel(application) {
    var state by mutableStateOf<SessionState?>(null); private set
    var images by mutableStateOf<Map<ContentId, ImageBitmap>>(emptyMap()); private set
    var busy by mutableStateOf(false); private set
    var saveFailed by mutableStateOf(false); private set
    var audioFailed by mutableStateOf(false); private set
    private val worker = Executors.newSingleThreadExecutor()
    private val main = Handler(Looper.getMainLooper())
    private val progress = AndroidProgressRepository.create(application)
    private val host = FollowContent.host(AtomicProgressStorage(File(application.noBackupFilesDir, "follow-instructions-session"), AndroidAtomicCommit), progress)
    private val loader = FollowArtworkLoader { application.assets.open(it) }
    private val audio = FollowAudio(DefaultAudioController(engineFactory, AudioMode.OFF)) { audioFailed = it is SpeechResult.Failed }
    private var visible = false
    private var loaded = false
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
        if (value && !loaded) { loaded = true; worker.execute { val result = loader.load(); main.post { if (!closed) images = result } } }
        if (value && state == null && !busy) work { if (host.state == null) host.open(SessionId(UUID.randomUUID().toString()), round, System.nanoTime(), language) else emptyList() }
    }
    private fun syncLanguage() { val current = host.state ?: return; if (current.language != language && !busy) work { host.dispatch(SessionAction.Language(language)) } }
    fun replay() { val current = state; if (ready() && current != null) { epoch++; audio.replay(current, language) } }
    fun action(action: SessionAction) { if (!ready()) return; epoch++; audio.cancel(); work { host.dispatch(action) } }
    fun retrySave() { if (!busy) work { host.retryWrites(); emptyList() } }
    fun again() { if (!ready() || state?.phase != SessionPhase.COMPLETED) return; epoch++; audio.cancel(); work { host.newRound(SessionId(UUID.randomUUID().toString()), round, System.nanoTime(), language) } }
    private fun ready() = visible && !busy && !saveFailed && state != null
    private fun work(operation: () -> List<SessionEffect>) {
        if (busy || closed) return
        busy = true; val token = ++epoch
        worker.execute {
            var failed = false; var effects = emptyList<SessionEffect>()
            try { effects = operation() } catch (_: Exception) { failed = true }
            val next = host.state; val pending = host.failure
            main.post { if (!closed && token == epoch) { busy = false; saveFailed = failed || pending; if (!failed) state = next; if (!failed && next != null && visible && next.language == language) audio.effects(next.plan.id, language, effects) } }
        }
    }
    override fun onCleared() { closed = true; audio.close(); worker.shutdown(); super.onCleared() }
}
