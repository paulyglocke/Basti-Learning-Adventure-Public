package com.bellfamily.bastischool.ui.seasons

import android.app.Application
import android.graphics.BitmapFactory
import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.AndroidViewModel
import com.bellfamily.bastischool.audio.*
import com.bellfamily.bastischool.audio.android.AndroidSystemSpeechEngine
import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.progress.AtomicProgressStorage
import com.bellfamily.bastischool.learning.progress.android.*
import com.bellfamily.bastischool.learning.seasons.*
import com.bellfamily.bastischool.learning.session.*
import java.io.File
import java.util.UUID
import java.util.concurrent.Executors

class SeasonsViewModel(application: Application) : AndroidViewModel(application) {
    var selection by mutableStateOf<SeasonsSelection?>(null); private set
    var state by mutableStateOf<SessionState?>(null); private set
    var artwork by mutableStateOf<ImageBitmap?>(null); private set
    var busy by mutableStateOf(false); private set
    var saveFailed by mutableStateOf(false); private set
    var imageFailed by mutableStateOf(false); private set
    var audioFailed by mutableStateOf(false); private set
    private fun storage(name: String) = AtomicProgressStorage(File(getApplication<Application>().noBackupFilesDir, name), AndroidAtomicCommit)
    private val selectionStore = SeasonsSelectionStore(storage("seasons-selection"))
    private val host = SeasonsContent.host(storage("seasons-session"), AndroidProgressRepository.create(application))
    private val worker = Executors.newSingleThreadExecutor()
    private val main = Handler(Looper.getMainLooper())
    private val audio = SeasonsAudio(DefaultAudioController(AndroidSystemSpeechEngine(application), AudioMode.OFF)) { audioFailed = it is SpeechResult.Failed }
    private var visible = false
    private var closed = false
    private var epoch = 0L
    private var language = ContentLanguage.ENGLISH
    private var round = RoundLength.FIVE
    private var mode = AudioMode.OFF
    // Worker-confined browsing state/cache; never stored as progress.
    private var browsing = SeasonsSelection()
    private var quizOpened = false
    private val images = linkedMapOf<ContentId, ImageBitmap>()

    fun configure(lang: String, audioMode: String, count: Int) {
        val nextLanguage = if (lang == "de") ContentLanguage.GERMAN else ContentLanguage.ENGLISH
        val nextMode = when(audioMode) { "all" -> AudioMode.ALL; "questions" -> AudioMode.QUESTIONS; else -> AudioMode.OFF }
        round = if(count == 10) RoundLength.TEN else RoundLength.FIVE
        if(language != nextLanguage || mode != nextMode) { epoch++; audio.mode(nextMode) }
        language = nextLanguage; mode = nextMode
        syncLanguage()
    }
    fun setVisible(value: Boolean) {
        if(visible == value) return
        visible = value; epoch++; audio.visible(value)
        if(value && selection == null && !busy) retry()
    }
    private fun syncLanguage() {
        if(!busy && !saveFailed && state != null && state!!.language != language) {
            val desired = language
            work { host.dispatch(SessionAction.Language(desired)) }
        }
    }
    fun select(id: ContentId) {
        if(!ready() || selection?.phase != SeasonsPhase.EXPLORE) return
        epoch++; audio.cancel()
        work(exploreSpeech = true) {
            val next = browsing.select(id); selectionStore.write(next); browsing = next; emptyList()
        }
    }
    fun phase(phase: SeasonsPhase) {
        if(!ready() || selection?.phase == phase) return
        epoch++; audio.cancel()
        val count = round; val lang = language
        work {
            val effects = if(phase == SeasonsPhase.PRACTICE && !quizOpened) {
                host.open(SessionId(UUID.randomUUID().toString()), count, System.nanoTime(), lang).also { quizOpened = true }
            } else emptyList()
            // Returning to Learn during an unanswered quiz is explicit support, not a new attempt.
            if(phase == SeasonsPhase.EXPLORE && host.state?.hintAvailable == true)
                host.dispatch(SessionAction.Hint(host.state!!.task.id))
            val next = browsing.copy(phase = phase); selectionStore.write(next); browsing = next
            effects
        }
    }
    fun replay() {
        if(!ready()) return
        if(selection?.phase == SeasonsPhase.EXPLORE) { epoch++; audio.explore(selection!!, language, replay = true) }
        else state?.let { action(SessionAction.Replay(it.task.id)) }
    }
    fun option(id: ContentId) { if(ready() && selection?.phase == SeasonsPhase.PRACTICE) state?.let { epoch++; audio.option(it,id) } }
    fun action(action: SessionAction) {
        if(!ready() || selection?.phase != SeasonsPhase.PRACTICE || imageFailed && action is SessionAction.Answer) return
        epoch++; audio.cancel(); work { host.dispatch(action) }
    }
    fun again() {
        if(!ready() || state?.phase != SessionPhase.COMPLETED) return
        epoch++; audio.cancel(); val count = round; val lang = language
        work { host.newRound(SessionId(UUID.randomUUID().toString()),count,System.nanoTime(),lang) }
    }
    fun retry() {
        if(busy) return
        epoch++; audio.cancel(); val count = round; val lang = language
        work {
            browsing = selectionStore.read()
            if(browsing.phase == SeasonsPhase.PRACTICE || quizOpened) {
                if(host.state != null) host.retryWrites()
                else host.open(SessionId(UUID.randomUUID().toString()),count,System.nanoTime(),lang)
                quizOpened = true
            }
            emptyList() // No replay on reload, lifecycle restoration or failed-delivery retry.
        }
    }
    private fun ready() = visible && !busy && !saveFailed && selection != null
    private fun image(id: ContentId): ImageBitmap {
        images[id]?.let { return it }
        val bitmap = getApplication<Application>().assets.open(SeasonsContent.image(id).path).use {
            requireNotNull(BitmapFactory.decodeStream(it)) { "Cannot decode season artwork" }.asImageBitmap()
        }
        if(images.size >= 2) images.remove(images.keys.first())
        images[id] = bitmap
        return bitmap
    }
    private fun work(exploreSpeech: Boolean = false, operation: () -> List<SessionEffect>) {
        if(busy || closed) return
        busy = true; val token = epoch
        worker.execute {
            var failed = false
            val effects = try { operation() } catch (_: Exception) { failed = true; emptyList() }
            val snapshot = host.state; val selected = browsing; val pendingFailure = host.failure
            val picture = try { image(if(selected.phase == SeasonsPhase.PRACTICE && snapshot != null) snapshot.task.question.correct else selected.selected) }
                catch (_: Exception) { null }
            main.post {
                if(!closed) {
                    busy = false; saveFailed = failed || pendingFailure; imageFailed = picture == null
                    if(!failed) { selection = selected; state = snapshot; artwork = picture }
                    if(!failed && visible && token == epoch) {
                        if(exploreSpeech && selected.phase == SeasonsPhase.EXPLORE) audio.explore(selected,language)
                        else if(selected.phase == SeasonsPhase.PRACTICE && snapshot != null && snapshot.language == language) audio.effects(snapshot,effects)
                    }
                    syncLanguage()
                }
            }
        }
    }
    override fun onCleared() { closed = true; audio.close(); worker.shutdown(); super.onCleared() }
}
