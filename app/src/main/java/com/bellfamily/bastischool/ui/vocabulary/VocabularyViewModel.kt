package com.bellfamily.bastischool.ui.vocabulary

import android.app.Application
import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.AndroidViewModel
import com.bellfamily.bastischool.audio.*
import com.bellfamily.bastischool.audio.android.AndroidSystemSpeechEngine
import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.progress.AtomicProgressStorage
import com.bellfamily.bastischool.learning.progress.android.*
import com.bellfamily.bastischool.learning.session.*
import com.bellfamily.bastischool.learning.vocabulary.*
import java.io.File
import java.util.UUID
import java.util.concurrent.Executors

/** Activity-specific owner. Pure reducers do not load assets, write files or speak. */
class VocabularyViewModel @JvmOverloads constructor(
    application: Application,
    engineFactory: () -> SpeechEngine = { AndroidSystemSpeechEngine(application) }
) : AndroidViewModel(application) {
    var selection by mutableStateOf<VocabularySelection?>(null);private set
    var quiz by mutableStateOf<SessionState?>(null);private set
    var busy by mutableStateOf(false);private set
    var saveFailed by mutableStateOf(false);private set
    var audioFailed by mutableStateOf(false);private set
    var images by mutableStateOf<Map<ContentId, ImageBitmap>>(emptyMap());private set
    private val artworkLoader = VocabularyArtworkLoader { application.assets.open(it) }
    private var artworkRequested = false
    private fun storage(name:String)=AtomicProgressStorage(File(getApplication<Application>().noBackupFilesDir,name),AndroidAtomicCommit)
    private val progress=AndroidProgressRepository.create(application)
    private val browse=VocabularySelectionStore(storage("vocabulary-selection"))
    private val categories=listOf(ContentId("category.animals"),VocabularyContent.classroom)
    private val quizzes=categories.flatMap { category -> listOf(VocabularyPhase.FIND,VocabularyPhase.NAME).map { phase ->
        (category to phase) to VocabularyContent.host(phase,category,storage("vocabulary-${category.value.substringAfter('.')}-${phase.name.lowercase()}"),progress)
    }}.toMap()
    private fun host(category:ContentId,phase:VocabularyPhase)=quizzes[category to phase]
    private val worker=Executors.newSingleThreadExecutor()
    private val main=Handler(Looper.getMainLooper())
    private val audio=VocabularyAudio(DefaultAudioController(engineFactory,AudioMode.OFF)){audioFailed=it is SpeechResult.Failed}
    private var visible=false
    private var closed=false
    private var epoch=0L
    private var language=ContentLanguage.ENGLISH
    private var round=RoundLength.FIVE
    private var mode=AudioMode.OFF
    // Worker-confined browsing state; no progress for exploration.
    private var selected=VocabularySelection()
    private fun id()=SessionId(UUID.randomUUID().toString())
    fun configure(lang:String,audioMode:String,count:Int) {
        val nextLanguage=if(lang=="de")ContentLanguage.GERMAN else ContentLanguage.ENGLISH
        val nextMode=when(audioMode){"all"->AudioMode.ALL;"questions"->AudioMode.QUESTIONS;else->AudioMode.OFF}
        round=if(count==10)RoundLength.TEN else RoundLength.FIVE
        if(language!=nextLanguage || mode!=nextMode){epoch++;audio.mode(nextMode)}
        language=nextLanguage;mode=nextMode;syncLanguage()
    }
    fun setVisible(value:Boolean) {
        if(visible==value)return
        visible=value;epoch++;audio.visible(value)
        if(value && !closed && !artworkRequested) {
            artworkRequested = true
            worker.execute {
                val loaded = artworkLoader.load()
                main.post { if(!closed) images = loaded }
            }
        }
        if(value && selection==null && !busy)retry()
    }
    private fun syncLanguage() {
        val phase=selection?.phase ?: return
        val differs=quiz?.language?.let {it!=language}==true
        if(!busy && !saveFailed && differs) {
            val lang=language
            work {host(selection?.category ?: selected.category,phase)?.dispatch(SessionAction.Language(lang));emptyList()}
        }
    }
    private fun ready()=visible && !busy && !saveFailed && selection!=null
    private fun boundary(){epoch++;audio.cancel()}
    private fun openPhase(phase:VocabularyPhase,category:ContentId,count:RoundLength,lang:ContentLanguage):List<SessionEffect> {
        val host=host(category,phase)
        val effects=when(phase) {
            VocabularyPhase.EXPLORE -> emptyList()
            else -> host!!.let {if(it.state==null)it.open(id(),count,System.nanoTime(),lang) else emptyList()}
        }
        // A returned phase may have been left in the other language; change identity-neutral text silently.
        host?.let {if(it.state?.language!=lang)it.dispatch(SessionAction.Language(lang))}
        return effects
    }
    fun phase(phase:VocabularyPhase) {
        if(!ready() || phase==selection?.phase)return
        boundary();val count=round;val lang=language
        work {
            if(phase==VocabularyPhase.EXPLORE) {
                host(selected.category,selected.phase)?.state?.takeIf {it.hintAvailable}?.let {host(selected.category,selected.phase)?.dispatch(SessionAction.Hint(it.task.id))}
            }
            val effects=openPhase(phase,selected.category,count,lang)
            val next=selected.copy(phase=phase);browse.write(next);selected=next;effects
        }
    }
    fun category(category:ContentId) {
        if(!ready() || category !in categories || category==selected.category)return
        boundary();work(exploreSpeech=true){
            val next=VocabularySelection(VocabularyContent.items(category).first().id,VocabularyPhase.EXPLORE,category)
            browse.write(next);selected=next;emptyList()
        }
    }
    fun select(item:ContentId) {
        if(!ready() || selection?.phase!=VocabularyPhase.EXPLORE)return
        boundary();work(exploreSpeech=true){val next=selected.copy(selected=item);browse.write(next);selected=next;emptyList()}
    }
    fun replay() {
        if(!ready())return
        when(selection!!.phase) {
            VocabularyPhase.EXPLORE -> {boundary();audio.word(selection!!.selected,language,true)}
            else -> quiz?.let {action(SessionAction.Replay(it.task.id))}
        }
    }
    fun option(item:ContentId) {if(ready()){boundary();audio.word(item,language)}}
    fun example() {if(ready() && selection?.phase==VocabularyPhase.EXPLORE){boundary();audio.example(selection!!.selected,language)}}
    fun action(action:SessionAction) {
        if(!ready())return
        val host=host(selection!!.category,selection!!.phase) ?: return
        boundary();work {host.dispatch(action)}
    }
    fun again() {
        if(!ready() || quiz?.phase!=SessionPhase.COMPLETED)return
        val phase=selection!!.phase;val category=selection!!.category;val count=round;val lang=language
        boundary();work {host(category,phase)!!.newRound(id(),count,System.nanoTime(),lang)}
    }
    fun retry() {
        if(busy)return
        boundary();val count=round;val lang=language
        work {
            selected=browse.read()
            // Retry all loaded journals, including an uncertain write immediately before a phase switch.
            quizzes.values.filter {it.state!=null}.forEach {it.retryWrites()}
            openPhase(selected.phase,selected.category,count,lang)
            emptyList() // Restore/retry never narrates old instructions.
        }
    }
    private fun work(exploreSpeech:Boolean=false,operation:()->List<SessionEffect>) {
        if(busy || closed)return
        busy=true;val token=epoch
        worker.execute {
            var failed=false
            val effects=try{operation()}catch(_:Exception){failed=true;emptyList()}
            val selectedSnapshot=selected;val q=host(selected.category,selected.phase)?.state
            val pendingFailure=quizzes.values.any {it.failure}
            main.post {
                if(!closed) {
                    busy=false;saveFailed=failed || pendingFailure
                    if(!failed){selection=selectedSnapshot;quiz=q}
                    if(!failed && visible && token==epoch) {
                        if(exploreSpeech)audio.word(selectedSnapshot.selected,language)
                        else if(q?.language==language)audio.effects(q.plan.id,language,effects)
                    }
                    syncLanguage()
                }
            }
        }
    }
    override fun onCleared(){closed=true;audio.close();worker.shutdown();super.onCleared()}
}
