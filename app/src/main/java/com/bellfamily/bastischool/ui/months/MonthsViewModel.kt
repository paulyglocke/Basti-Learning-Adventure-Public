package com.bellfamily.bastischool.ui.months

import android.app.Application
import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.*
import androidx.lifecycle.AndroidViewModel
import com.bellfamily.bastischool.audio.*
import com.bellfamily.bastischool.audio.android.AndroidSystemSpeechEngine
import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.months.*
import com.bellfamily.bastischool.learning.progress.AtomicProgressStorage
import com.bellfamily.bastischool.learning.progress.android.AndroidAtomicCommit
import java.io.File
import java.util.concurrent.Executors

class MonthsViewModel @JvmOverloads constructor(application: Application,
    engineFactory: () -> SpeechEngine = { AndroidSystemSpeechEngine(application) }
) : AndroidViewModel(application) {
    var selection by mutableStateOf<MonthsSelection?>(null); private set
    var saveFailed by mutableStateOf(false); private set
    var audioFailed by mutableStateOf(false); private set
    private val main = Handler(Looper.getMainLooper())
    private val worker = Executors.newSingleThreadExecutor()
    private val store = MonthsSelectionStore(AtomicProgressStorage(File(application.noBackupFilesDir, "months-selection"), AndroidAtomicCommit))
    private val audio = MonthsAudio(DefaultAudioController(engineFactory, AudioMode.OFF)) { audioFailed = it }
    private var language = ContentLanguage.ENGLISH
    private var mode = AudioMode.OFF
    private var visible = false
    private var loading = false
    private var closed = false
    private var revision = 0L

    fun configure(lang: String, sound: String) {
        val nextLanguage = if (lang == "de") ContentLanguage.GERMAN else ContentLanguage.ENGLISH
        val nextMode = when (sound) { "all" -> AudioMode.ALL; "questions" -> AudioMode.QUESTIONS; else -> AudioMode.OFF }
        if (nextLanguage != language) { language = nextLanguage; audio.cancel() }
        if (nextMode != mode) { mode = nextMode; audio.mode(mode) }
    }
    fun setVisible(value: Boolean) {
        if (visible == value) return
        visible = value; audio.visible(value)
        if (value && selection == null) load()
    }
    private fun load() {
        if (loading || closed) return
        loading = true
        worker.execute {
            val result = runCatching { store.read() }
            main.post {
                if (!closed) {
                    loading = false; saveFailed = result.isFailure
                    result.getOrNull()?.let { selection = it } // Never narrate restoration.
                }
            }
        }
    }
    fun select(id: ContentId) {
        if (!visible || selection == null || saveFailed || closed) return
        val next = MonthsSelection(id)
        if (next != selection) { selection = next; persist(next) }
        audio.month(id, language) // Re-tapping the selected month explicitly listens again.
    }
    fun listen() { if (visible) selection?.let { audio.month(it.selected, language) } }
    fun retry() { if (selection == null) load() else persist(selection!!) }
    private fun persist(snapshot: MonthsSelection) {
        if (closed) return
        val ticket = ++revision
        worker.execute {
            val result = runCatching { store.write(snapshot) }
            main.post { if (!closed && revision == ticket) saveFailed = result.isFailure }
        }
    }
    override fun onCleared() {
        closed = true; audio.close()
        worker.shutdown() // Already queued selections finish in order; callbacks cannot publish.
        super.onCleared()
    }
}
