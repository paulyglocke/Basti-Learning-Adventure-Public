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

enum class FollowLevel { ONE, TWO }

class FollowViewModel @JvmOverloads constructor(
    application: Application,
    engineFactory: () -> SpeechEngine = { AndroidSystemSpeechEngine(application) }
) : AndroidViewModel(application) {
    var state by mutableStateOf<SessionState?>(null); private set
    var twoState by mutableStateOf<FollowTwoState?>(null); private set
    var images by mutableStateOf<Map<ContentId, ImageBitmap>>(emptyMap()); private set
    var busy by mutableStateOf(false); private set
    var saveFailed by mutableStateOf(false); private set
    var audioFailed by mutableStateOf(false); private set
    private val worker = Executors.newSingleThreadExecutor()
    private val main = Handler(Looper.getMainLooper())
    private val progress = AndroidProgressRepository.create(application)
    private val host = FollowContent.host(AtomicProgressStorage(File(application.noBackupFilesDir, "follow-instructions-session"), AndroidAtomicCommit), progress)
    private val twoHost = FollowTwoHost(AtomicProgressStorage(File(application.noBackupFilesDir, "follow-instructions-two-session"), AndroidAtomicCommit), progress)
    private val loader = FollowArtworkLoader { application.assets.open(it) }
    private val audio = FollowAudio(DefaultAudioController(engineFactory, AudioMode.OFF)) { audioFailed = it is SpeechResult.Failed }
    private var visible = false
    private var level: FollowLevel? = null
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
    fun setVisible(value: Boolean, nextLevel: FollowLevel? = FollowLevel.ONE) {
        if (visible == value && level == nextLevel) return
        visible = value; level = nextLevel; epoch++; audio.visible(value && nextLevel != null)
        if (value && !loaded) { loaded = true; worker.execute { val result = loader.load(); main.post { if (!closed) images = result } } }
        if (value && nextLevel == FollowLevel.ONE && !busy) ensureOne()
        if (value && nextLevel == FollowLevel.TWO && !busy) ensureTwo()
    }
    private fun ensureOne() {
        if (state == null) work { host.open(SessionId(UUID.randomUUID().toString()), round, System.nanoTime(), language) }
        else syncLanguage()
    }
    private fun ensureTwo() {
        if (twoState == null) twoWork { twoHost.open(SessionId(UUID.randomUUID().toString()), round, System.nanoTime(), language) }
        else syncLanguage()
    }
    private fun syncLanguage() {
        if (!busy && level == FollowLevel.ONE && host.state?.language != language && host.state != null)
            work { host.dispatch(SessionAction.Language(language)) }
        if (!busy && level == FollowLevel.TWO && twoHost.state?.language != language && twoHost.state != null)
            twoWork { twoHost.dispatch(FollowTwoAction.Language(language)) }
    }
    fun replay() { state?.let { action(SessionAction.Replay(it.task.id)) } }
    fun action(action: SessionAction) { if (!ready(FollowLevel.ONE)) return; epoch++; audio.cancel(); work { host.dispatch(action) } }
    fun twoAction(action: FollowTwoAction) { if (!ready(FollowLevel.TWO)) return; epoch++; audio.cancel(); twoWork { twoHost.dispatch(action) } }
    fun twoReplay() { twoState?.let { twoAction(FollowTwoAction.Replay(it.taskId)) } }
    fun twoExample() { val current = twoState ?: return; if (ready(FollowLevel.TWO)) {
        epoch++; audio.effects(current.id, language, listOf(SessionEffect.Narrate(FollowTwo.demonstration,
            NarrationKind.INSTRUCTION, SpeechTrigger.MANUAL)))
    } }
    fun retrySave() { if (!busy && level == FollowLevel.ONE) work { host.retryWrites(); emptyList() }
        else if (!busy && level == FollowLevel.TWO) twoWork { twoHost.retryWrites(); null } }
    fun again() { if (!ready(FollowLevel.ONE) || state?.phase != SessionPhase.COMPLETED) return; epoch++; audio.cancel(); work { host.newRound(SessionId(UUID.randomUUID().toString()), round, System.nanoTime(), language) } }
    fun twoAgain() { if (!ready(FollowLevel.TWO) || twoState?.completed != true) return; epoch++; audio.cancel()
        twoWork { twoHost.again(SessionId(UUID.randomUUID().toString()), round, System.nanoTime(), language) } }
    private fun ready(expected: FollowLevel) = visible && level == expected && !busy && !saveFailed &&
        (if (expected == FollowLevel.ONE) state?.language == language else twoState?.language == language)
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
                    if (!failed && !saveFailed && token == epoch && next != null && visible && level == FollowLevel.ONE && next.language == language) {
                        audio.effects(next.plan.id, language, effects)
                    }
                    if (!saveFailed) ensureCurrent()
                }
            }
        }
    }
    private fun twoWork(operation: () -> SessionEffect.Narrate?) {
        if (busy || closed) return
        busy = true; val token = ++epoch
        worker.execute {
            val result = runCatching(operation)
            val next = twoHost.state; val pending = twoHost.failure
            main.post {
                if (!closed) {
                    busy = false; saveFailed = result.isFailure || pending
                    if (next != null) twoState = next
                    if (!saveFailed && token == epoch && next != null && visible && level == FollowLevel.TWO && next.language == language)
                        audio.effects(next.id, language, listOfNotNull(result.getOrNull()))
                    if (!saveFailed) ensureCurrent()
                }
            }
        }
    }
    private fun ensureCurrent() {
        if (!visible || busy) return
        when (level) {
            FollowLevel.ONE -> ensureOne()
            FollowLevel.TWO -> ensureTwo()
            null -> Unit
        }
    }
    override fun onCleared() { closed = true; audio.close(); worker.shutdown(); super.onCleared() }
}
