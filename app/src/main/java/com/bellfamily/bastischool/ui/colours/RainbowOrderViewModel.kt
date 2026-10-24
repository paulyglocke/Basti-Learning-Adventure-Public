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
import com.bellfamily.bastischool.learning.progress.*
import com.bellfamily.bastischool.learning.progress.android.*
import com.bellfamily.bastischool.learning.session.*
import java.io.File
import java.util.UUID
import java.util.concurrent.Executors

class RainbowOrderViewModel(application: Application) : AndroidViewModel(application) {
    var state by mutableStateOf<RainbowOrderState?>(null); private set
    var level by mutableStateOf(RainbowOrderLevel.STARTER); private set
    var busy by mutableStateOf(false); private set
    var saveFailed by mutableStateOf(false); private set
    private val worker = Executors.newSingleThreadExecutor()
    private val main = Handler(Looper.getMainLooper())
    private val progress = AndroidProgressRepository.create(application)
    private val audioController = DefaultAudioController({ AndroidSystemSpeechEngine(application) }, AudioMode.OFF)
    private val audio = ColoursAudio(audioController, ownerName = "native-rainbow-order")
    private var audioMode = AudioMode.OFF
    private fun host(level: RainbowOrderLevel) = hosts.getValue(level)
    private val hosts by lazy {
        RainbowOrderLevel.entries.associateWith { level ->
            RainbowOrderHost(AtomicProgressStorage(File(application.noBackupFilesDir, RainbowOrderContent.journalName(level)), AndroidAtomicCommit), progress, level)
        }
    }
    private val levelStore by lazy {
        RainbowOrderLevelStore(AtomicProgressStorage(File(application.noBackupFilesDir, "rainbow-order-active-level"), AndroidAtomicCommit))
    }
    private var visible = false
    private var closed = false
    private var language = ContentLanguage.ENGLISH

    fun configure(lang: String) {
        val nextLanguage = if (lang == "de") ContentLanguage.GERMAN else ContentLanguage.ENGLISH
        if (language != nextLanguage) {
            language = nextLanguage
            if (visible && !busy && !saveFailed && state?.language != language) {
                val selectedLevel = level
                work(selectedLevel) { host(selectedLevel).dispatch(RainbowOrderAction.Language(language)) }
            }
        }
    }

    fun configureAudio(mode: String) {
        val next = when (mode) { "all" -> AudioMode.ALL; "questions" -> AudioMode.QUESTIONS; else -> AudioMode.OFF }
        if (audioMode != next) { audioMode = next; audio.mode(next) }
    }

    fun setVisible(value: Boolean) {
        if (visible == value) return
        visible = value
        audio.visible(value)
        if (value && state == null && !busy && !closed) restoreSession()
        else if (value && state?.language != language && !busy && !saveFailed && !closed) {
            val selectedLevel = level
            val desired = language
            work(selectedLevel) { host(selectedLevel).dispatch(RainbowOrderAction.Language(desired)) }
        }
    }

    fun activate(next: RainbowOrderLevel) {
        if (!visible || closed || busy) return
        if (level == next && state?.language == language && !saveFailed) return
        level = next
        work(next) {
            levelStore.write(next)
            val selected = host(next)
            if (selected.failure) selected.retryWrites()
            selected.open(SessionId(UUID.randomUUID().toString()), System.nanoTime(), language)
            null
        }
    }

    fun dispatch(action: RainbowOrderAction) {
        if (!visible || busy || saveFailed || state?.language != language || state?.level != level) return
        val selectedLevel = level
        work(selectedLevel) { host(selectedLevel).dispatch(action) }
    }

    fun again() {
        if (!visible || busy || saveFailed || state?.completed != true || !state!!.acknowledged) return
        val selectedLevel = level
        work(selectedLevel) { host(selectedLevel).again(SessionId(UUID.randomUUID().toString()), System.nanoTime(), language); null }
    }

    fun retry() {
        if (busy || closed) return
        if (state == null) { restoreSession(); return }
        val selectedLevel = level
        work(selectedLevel) { host(selectedLevel).retryWrites(); null }
    }

    private fun restoreSession() {
        if (busy || closed) return
        busy = true
        worker.execute {
            var restoredLevel = RainbowOrderLevel.STARTER
            var failed = false
            try {
                restoredLevel = levelStore.read() ?: RainbowOrderLevel.STARTER
                val selected = host(restoredLevel)
                if (selected.failure) selected.retryWrites()
                selected.open(SessionId(UUID.randomUUID().toString()), System.nanoTime(), language)
            } catch (_: Exception) { failed = true }
            val selected = hosts[restoredLevel]
            val restored = selected?.state
            val pendingFailure = selected?.failure == true
            main.post {
                if (!closed) {
                    level = restoredLevel
                    state = restored
                    busy = false
                    saveFailed = failed || pendingFailure
                    if (!saveFailed && !failed && visible && restored != null && restored.language != language) {
                        val desired = language
                        work(restoredLevel) { host(restoredLevel).dispatch(RainbowOrderAction.Language(desired)) }
                    }
                }
            }
        }
    }

    private fun work(selectedLevel: RainbowOrderLevel, operation: () -> SessionEffect.Narrate?) {
        if (busy || closed) return
        busy = true
        worker.execute {
            var failed = false
            val effect = try { operation() } catch (_: Exception) { failed = true; null }
            val selectedHost = hosts[selectedLevel]
            val restored = selectedHost?.state
            val pendingFailure = selectedHost?.failure == true
            main.post {
                if (!closed) {
                    busy = false
                    saveFailed = failed || pendingFailure
                    if (!failed && level == selectedLevel) state = restored
                    if (!saveFailed && !failed && visible && level == selectedLevel && restored?.language == language)
                        effect?.let { audio.effects(restored.id, restored.language, listOf(it)) }
                    if (!saveFailed && !failed && visible && level == selectedLevel && restored != null && restored.language != language) {
                        val desired = language
                        work(selectedLevel) { host(selectedLevel).dispatch(RainbowOrderAction.Language(desired)) }
                    }
                }
            }
        }
    }

    override fun onCleared() { closed = true; audio.close(); worker.shutdown(); super.onCleared() }
}
