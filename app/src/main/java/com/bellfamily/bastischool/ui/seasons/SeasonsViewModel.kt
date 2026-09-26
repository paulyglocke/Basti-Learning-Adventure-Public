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
import com.bellfamily.bastischool.learning.content.SeasonIds
import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.progress.AtomicProgressStorage
import com.bellfamily.bastischool.learning.progress.android.*
import com.bellfamily.bastischool.learning.seasons.*
import com.bellfamily.bastischool.learning.session.*
import java.io.File
import java.util.UUID
import java.util.concurrent.Executors

class SeasonsViewModel @JvmOverloads constructor(
    application: Application,
    engineFactory: () -> SpeechEngine = { AndroidSystemSpeechEngine(application) }
) : AndroidViewModel(application) {
    var selection by mutableStateOf<SeasonsSelection?>(null); private set
    var state by mutableStateOf<SessionState?>(null); private set
    var ordering by mutableStateOf<SeasonsOrderState?>(null); private set
    var artwork by mutableStateOf<ImageBitmap?>(null); private set
    var orderArtwork by mutableStateOf<Map<ContentId,ImageBitmap>>(emptyMap()); private set
    var busy by mutableStateOf(false); private set
    var saveFailed by mutableStateOf(false); private set
    var imageFailed by mutableStateOf(false); private set
    var audioFailed by mutableStateOf(false); private set
    private fun storage(name: String) = AtomicProgressStorage(File(getApplication<Application>().noBackupFilesDir, name), AndroidAtomicCommit)
    private val progress = AndroidProgressRepository.create(application)
    private val selectionStore = SeasonsSelectionStore(storage("seasons-selection"))
    private val hosts = mapOf(
        SeasonsPhase.PRACTICE to SeasonsContent.host(storage("seasons-session"),progress),
        SeasonsPhase.NEXT to SeasonsCycle.host(SeasonsPhase.NEXT,storage("seasons-next-session"),progress),
        SeasonsPhase.BEFORE to SeasonsCycle.host(SeasonsPhase.BEFORE,storage("seasons-before-session"),progress))
    private val orderHost = SeasonsOrderHost(storage("seasons-order-session"),progress)
    private val worker = Executors.newSingleThreadExecutor()
    private val main = Handler(Looper.getMainLooper())
    private val audio = SeasonsAudio(DefaultAudioController(engineFactory, AudioMode.OFF)) { audioFailed = it is SpeechResult.Failed }
    private var visible = false
    private var closed = false
    private var epoch = 0L
    private var language = ContentLanguage.ENGLISH
    private var round = RoundLength.FIVE
    private var mode = AudioMode.OFF
    // Worker-confined browsing state and bounded four-image cache; never stored as progress.
    private var browsing = SeasonsSelection()
    private val images = linkedMapOf<ContentId, ImageBitmap>()
    private fun id() = SessionId(UUID.randomUUID().toString())

    fun configure(lang: String, audioMode: String, count: Int) {
        val nextLanguage = if(lang == "de") ContentLanguage.GERMAN else ContentLanguage.ENGLISH
        val nextMode = when(audioMode) {"all" -> AudioMode.ALL; "questions" -> AudioMode.QUESTIONS; else -> AudioMode.OFF}
        round = if(count == 10) RoundLength.TEN else RoundLength.FIVE
        if(language != nextLanguage || mode != nextMode) {epoch++; audio.mode(nextMode)}
        language = nextLanguage; mode = nextMode
        syncLanguage()
    }
    fun setVisible(value: Boolean) {
        if(visible == value) return
        visible = value; epoch++; audio.visible(value)
        if(value && selection == null && !busy) retry()
    }
    private fun syncLanguage() {
        if(busy || saveFailed) return
        val desired = language
        val phase = selection?.phase ?: return
        if(phase == SeasonsPhase.ORDER && ordering != null && ordering!!.language != desired)
            work {listOfNotNull(orderHost.dispatch(SeasonsOrderAction.Language(desired)))}
        else if(phase.isQuiz && state != null && state!!.language != desired)
            work {hosts.getValue(phase).dispatch(SessionAction.Language(desired))}
    }
    fun select(id: ContentId) {
        if(!ready() || selection?.phase != SeasonsPhase.EXPLORE) return
        epoch++; audio.cancel()
        work(exploreSpeech = true) {
            val next = browsing.select(id); selectionStore.write(next); browsing = next; emptyList()
        }
    }
    private fun openPhase(phase: SeasonsPhase,count: RoundLength,lang: ContentLanguage): List<SessionEffect> = when {
        phase.isQuiz -> hosts.getValue(phase).let {if(it.state == null) it.open(id(),count,System.nanoTime(),lang) else emptyList()}
        phase == SeasonsPhase.ORDER -> if(orderHost.state == null) listOfNotNull(orderHost.open(id(),System.nanoTime(),lang)) else emptyList()
        else -> emptyList()
    }
    fun phase(phase: SeasonsPhase) {
        if(!ready() || selection?.phase == phase) return
        epoch++; audio.cancel(); val count = round; val lang = language
        work {
            val effects = openPhase(phase,count,lang)
            if(phase == SeasonsPhase.EXPLORE) {
                val previous = hosts[browsing.phase]
                if(previous?.state?.hintAvailable == true) previous.dispatch(SessionAction.Hint(previous.state!!.task.id))
                if(browsing.phase == SeasonsPhase.ORDER && orderHost.state?.completed == false)
                    orderHost.dispatch(SeasonsOrderAction.Help(orderHost.state!!.task))
            }
            val next = browsing.copy(phase = phase); selectionStore.write(next); browsing = next
            effects
        }
    }
    fun replay() {
        if(!ready()) return
        when(selection?.phase) {
            SeasonsPhase.EXPLORE -> {epoch++; audio.explore(selection!!,language,replay = true)}
            SeasonsPhase.ORDER -> ordering?.let {orderAction(SeasonsOrderAction.Replay(it.task))}
            else -> state?.let {action(SessionAction.Replay(it.task.id))}
        }
    }
    fun option(id: ContentId) {
        if(!ready()) return
        epoch++
        if(selection?.phase == SeasonsPhase.ORDER) ordering?.let {audio.orderOption(it,id)}
        else if(selection?.phase?.isQuiz == true) state?.let {audio.option(it,id)}
    }
    fun action(action: SessionAction) {
        val phase = selection?.phase ?: return
        if(!ready() || !phase.isQuiz || imageFailed && action is SessionAction.Answer) return
        epoch++; audio.cancel(); work {hosts.getValue(phase).dispatch(action)}
    }
    fun orderAction(action: SeasonsOrderAction) {
        if(!ready() || selection?.phase != SeasonsPhase.ORDER || imageFailed && action is SeasonsOrderAction.Place) return
        epoch++; audio.cancel(); work {listOfNotNull(orderHost.dispatch(action))}
    }
    fun again() {
        if(!ready()) return
        val phase = selection!!.phase
        if(phase == SeasonsPhase.ORDER && ordering?.completed != true || phase.isQuiz && state?.phase != SessionPhase.COMPLETED || phase == SeasonsPhase.EXPLORE) return
        epoch++; audio.cancel(); val count = round; val lang = language
        work {
            if(phase == SeasonsPhase.ORDER) listOfNotNull(orderHost.again(id(),System.nanoTime(),lang))
            else hosts.getValue(phase).newRound(id(),count,System.nanoTime(),lang)
        }
    }
    fun retry() {
        if(busy) return
        epoch++; audio.cancel(); val count = round; val lang = language
        work {
            browsing = selectionStore.read()
            hosts.values.filter {it.state != null}.forEach {it.retryWrites()}
            if(orderHost.state != null) orderHost.retryWrites()
            openPhase(browsing.phase,count,lang)
            emptyList() // Reload/restoration/delivery retry never narrate.
        }
    }
    private fun ready() = visible && !busy && !saveFailed && selection != null
    private fun image(id: ContentId): ImageBitmap = images.getOrPut(id) {
        getApplication<Application>().assets.open(SeasonsContent.image(id).path).use {
            requireNotNull(BitmapFactory.decodeStream(it,null,BitmapFactory.Options().apply {inSampleSize = 2})).asImageBitmap()
        }
    }
    private fun work(exploreSpeech: Boolean = false, operation: () -> List<SessionEffect>) {
        if(busy || closed) return
        busy = true; val token = epoch
        worker.execute {
            var failed = false
            val effects = try {operation()} catch(_: Exception) {failed = true; emptyList()}
            val selected = browsing; val snapshot = hosts[selected.phase]?.state; val ordered = orderHost.state
            val pendingFailure = hosts.values.any {it.failure} || orderHost.failure
            var picture: ImageBitmap? = null
            var orderPictures = emptyMap<ContentId,ImageBitmap>()
            val imageError = try {
                if(selected.phase == SeasonsPhase.ORDER) orderPictures = SeasonIds.canonicalOrder.associateWith(::image)
                else {
                    val target = if(snapshot == null) selected.selected else if(selected.phase == SeasonsPhase.PRACTICE) snapshot.task.question.correct
                        else SeasonsCycle.anchor(selected.phase,snapshot.task.question)
                    picture = image(target)
                }
                false
            } catch(_: Exception) {true}
            main.post {
                if(!closed) {
                    busy = false; saveFailed = failed || pendingFailure; imageFailed = imageError
                    if(!failed) {selection = selected; state = snapshot; ordering = ordered; artwork = picture; orderArtwork = orderPictures}
                    if(!failed && !saveFailed && visible && token == epoch) {
                        if(exploreSpeech && selected.phase == SeasonsPhase.EXPLORE) audio.explore(selected,language)
                        else if(selected.phase.isQuiz && snapshot != null && snapshot.language == language) audio.effects(snapshot,effects)
                        else if(selected.phase == SeasonsPhase.ORDER && ordered != null && ordered.language == language)
                            effects.filterIsInstance<SessionEffect.Narrate>().forEach {audio.order(ordered,it)}
                    }
                    syncLanguage()
                }
            }
        }
    }
    override fun onCleared() {closed = true; audio.close(); worker.shutdown(); super.onCleared()}
}
