package com.bellfamily.bastischool.ui.prepositions

import android.app.Application
import android.os.Handler
import android.os.Looper
import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.AndroidViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.bellfamily.bastischool.audio.*
import com.bellfamily.bastischool.audio.android.AndroidSystemSpeechEngine
import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.prepositions.*
import com.bellfamily.bastischool.learning.progress.AtomicProgressStorage
import com.bellfamily.bastischool.learning.progress.android.*
import com.bellfamily.bastischool.learning.session.*
import java.io.File
import java.util.UUID
import java.util.concurrent.Executors

/** One retained screen owner. Disk work is serialized off main; audio and UI stay on main. */
class PrepositionsViewModel @JvmOverloads constructor(
    application: Application,
    engineFactory: () -> SpeechEngine = { AndroidSystemSpeechEngine(application) }
) : AndroidViewModel(application) {
    var state by mutableStateOf<SessionState?>(null); private set
    var artwork by mutableStateOf<ImageBitmap?>(null); private set
    private val images = PrepositionsArtworkLoader { application.assets.open(it) }
    var busy by mutableStateOf(false); private set
    var saveFailed by mutableStateOf(false); private set
    var recoveryFailed by mutableStateOf(false); private set
    var audioFailed by mutableStateOf(false); private set
    private val prefs = application.getSharedPreferences("basti_shell", 0)
    private val host = PrepositionsHost(AtomicProgressStorage(File(application.noBackupFilesDir, "prepositions-session"), AndroidAtomicCommit),
        AndroidProgressRepository.create(application))
    private val worker = Executors.newSingleThreadExecutor()
    private val main = Handler(Looper.getMainLooper())
    private val audio = PrepositionsAudio(DefaultAudioController(engineFactory, AudioMode.OFF)) {
        audioFailed = it is SpeechResult.Failed
    }
    private var visible = false
    private var epoch = 0L
    private var closed = false
    private var opened = false
    private var language = ContentLanguage.ENGLISH
    private var round = RoundLength.FIVE
    private var mode = AudioMode.OFF

    fun configure(lang: String, audioMode: String, count: Int) {
        language = if (lang == "de") ContentLanguage.GERMAN else ContentLanguage.ENGLISH
        round = if (count == 10) RoundLength.TEN else RoundLength.FIVE
        val nextMode = when (audioMode) { "all" -> AudioMode.ALL; "questions" -> AudioMode.QUESTIONS; else -> AudioMode.OFF }
        if (nextMode != mode) { mode = nextMode; epoch++; audio.mode(mode) }
        if (state != null && state!!.language != language) { epoch++; audio.cancel(); syncLanguage() }
    }
    fun setVisible(value: Boolean) {
        if (visible == value) return
        visible = value; epoch++; audio.visible(value)
        if (value && !opened && !busy) open()
    }
    private fun open() {
        val id = SessionId(UUID.randomUUID().toString())
        val selectedRound = round; val selectedLanguage = language
        work { host.open(id, selectedRound, System.nanoTime(), selectedLanguage) }
    }
    private fun syncLanguage() {
        if (!busy && !saveFailed && state?.language != language) {
            val desired = language
            work { host.dispatch(SessionAction.Language(desired)) }
        }
    }
    fun action(action: SessionAction) {
        if (!visible || busy || saveFailed || recoveryFailed) return
        // Silence obsolete narration immediately, before the worker accepts a transition.
        epoch++; audio.cancel()
        work { host.dispatch(action) }
    }
    fun option(id: ContentId) { if (!busy && visible && !saveFailed) state?.let { epoch++; audio.option(it, id) } }
    fun introduction() { if (!busy && visible) state?.let { epoch++; playIntroduction(it, false) } }
    fun resetTutorials() { epoch++; audio.cancel() } // Shared epoch in prefs invalidates prior success callbacks.
    fun retrySave() {
        if (busy) return
        if (!opened) open() else work { host.retryWrites(); emptyList() }
    }
    fun again() {
        if (busy || saveFailed || state?.phase != SessionPhase.COMPLETED) return
        epoch++; audio.cancel()
        val id = SessionId(UUID.randomUUID().toString()); val count = round; val lang = language
        work { host.newRound(id, count, System.nanoTime(), lang) }
    }
    private fun playIntroduction(snapshot: SessionState, automatic: Boolean) {
        val reset = prefs.getLong("tutorialResetEpoch", 0)
        val key = "native_positions_v1_${snapshot.language}"
        audio.introduction(snapshot, automatic) { result ->
            if (result == SpeechResult.Completed && reset == prefs.getLong("tutorialResetEpoch", 0))
                prefs.edit().putLong(key, reset).apply()
        }
    }
    private fun work(operation: () -> List<SessionEffect>) {
        if (busy || closed) return
        busy = true
        val token = epoch
        worker.execute {
            var failed = false
            val effects = try { operation() } catch (_: Exception) { failed = true; emptyList() }
            val snapshot = host.state
            val pendingFailure = host.failure
            val picture = snapshot?.takeIf { it.phase == SessionPhase.ACTIVE }?.let {
                images.load(PrepositionsContent.scene(it.task).id)
            }
            main.post {
                if (!closed) {
                    busy = false
                    if (!failed) { opened = true; recoveryFailed = false; state = snapshot; artwork = picture }
                    saveFailed = failed || pendingFailure
                    recoveryFailed = failed && state == null
                    if (!failed && visible && token == epoch && snapshot != null && snapshot.language == language) {
                        val reset = prefs.getLong("tutorialResetEpoch", 0)
                        val heard = prefs.getLong("native_positions_v1_${snapshot.language}", -1) == reset
                        if (!heard && effects.any { it is SessionEffect.Narrate && it.kind == NarrationKind.INSTRUCTION && it.trigger == SpeechTrigger.AUTOMATIC })
                            playIntroduction(snapshot, true)
                        else audio.effects(snapshot, effects)
                    }
                    syncLanguage()
                }
            }
        }
    }
    override fun onCleared() { closed = true; audio.close(); worker.shutdown(); super.onCleared() }
}
