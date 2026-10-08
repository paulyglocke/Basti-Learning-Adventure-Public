package com.bellfamily.bastischool.ui.clock

import android.app.Application
import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.*
import androidx.lifecycle.AndroidViewModel
import com.bellfamily.bastischool.audio.*
import com.bellfamily.bastischool.audio.android.AndroidSystemSpeechEngine
import com.bellfamily.bastischool.learning.clock.*
import com.bellfamily.bastischool.learning.models.ContentLanguage
import com.bellfamily.bastischool.learning.progress.AtomicProgressStorage
import com.bellfamily.bastischool.learning.progress.android.AndroidAtomicCommit
import java.io.File
import java.util.concurrent.Executors

class ClockViewModel @JvmOverloads constructor(
    application: Application,
    engineFactory: () -> SpeechEngine = { AndroidSystemSpeechEngine(application) }
) : AndroidViewModel(application) {
    var state by mutableStateOf<ClockExploreState?>(null); private set
    var saveFailed by mutableStateOf(false); private set
    var audioFailed by mutableStateOf(false); private set
    private val main = Handler(Looper.getMainLooper())
    private val worker = Executors.newSingleThreadExecutor()
    private val store = ClockStore(AtomicProgressStorage(File(application.noBackupFilesDir, "clock-explore"), AndroidAtomicCommit))
    private val audio = ClockAudio(DefaultAudioController(engineFactory, AudioMode.OFF)) { audioFailed = it }
    private var language = ContentLanguage.ENGLISH
    private var mode = AudioMode.OFF
    private var visible = false
    private var loading = false
    private var closed = false
    private var revision = 0L
    private val pendingWrite = Runnable { persist() }

    fun configure(lang: String, sound: String) {
        val next = if (lang == "de") ContentLanguage.GERMAN else ContentLanguage.ENGLISH
        val nextMode = when (sound) { "all" -> AudioMode.ALL; "questions" -> AudioMode.QUESTIONS; else -> AudioMode.OFF }
        if (nextMode != mode) { mode = nextMode; audio.mode(mode) }
        if (next != language) {
            language = next; audio.cancel()
            state?.let { state = it.copy(language = next); scheduleSave() }
        }
    }
    fun setVisible(value: Boolean) {
        if (visible == value) return
        visible = value; audio.visible(value)
        if (value && state == null) load()
        if (!value) { main.removeCallbacks(pendingWrite); persist() }
    }
    private fun load() {
        if (loading || closed) return
        loading = true
        worker.execute {
            val result = runCatching { store.read() }
            main.post {
                if (!closed) {
                    loading = false; saveFailed = result.isFailure
                    result.getOrNull()?.let {
                        // Global Options remains the language authority, as in other native owners.
                        state = it.copy(language = language)
                        if (it.language != language) scheduleSave()
                    }
                }
            }
        }
    }
    fun move(time: ClockTime) {
        if (!visible || saveFailed) return
        val current = state ?: return
        audio.cancel()
        state = current.copy(time = time)
        scheduleSave()
    }
    fun adjust(delta: Int) { state?.let { move(it.time.advance(delta)) } }
    fun settle() { state?.let { move(it.time.snap()) }; main.removeCallbacks(pendingWrite); persist() }
    fun interrupt() = audio.cancel()
    fun listen() { if (visible) state?.let(audio::listen) }
    fun retrySave() { if (state == null) load() else persist() }
    private fun scheduleSave() {
        revision++
        main.removeCallbacks(pendingWrite)
        main.postDelayed(pendingWrite, 120)
    }
    private fun persist() {
        val snapshot = state ?: return
        if (closed) return
        val token = revision
        worker.execute {
            val result = runCatching { store.write(snapshot) }
            main.post { if (!closed && token == revision) saveFailed = result.isFailure }
        }
    }
    override fun onCleared() {
        main.removeCallbacks(pendingWrite); persist()
        closed = true; audio.close(); worker.shutdown(); super.onCleared()
    }
}
