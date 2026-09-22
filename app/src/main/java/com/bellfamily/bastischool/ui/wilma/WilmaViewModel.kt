package com.bellfamily.bastischool.ui.wilma

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
import com.bellfamily.bastischool.learning.session.*
import com.bellfamily.bastischool.learning.wilma.*
import java.io.File
import java.util.UUID
import java.util.concurrent.Executors

/** Activity-specific owner. Pure reducers do not load assets, write files or speak. */
class WilmaViewModel(application:Application):AndroidViewModel(application) {
    var selection by mutableStateOf<WilmaSelection?>(null);private set
    var quiz by mutableStateOf<SessionState?>(null);private set
    var ordering by mutableStateOf<WilmaOrderState?>(null);private set
    var images by mutableStateOf<Map<String,ImageBitmap>>(emptyMap());private set
    var busy by mutableStateOf(false);private set
    var saveFailed by mutableStateOf(false);private set
    var imageFailed by mutableStateOf(false);private set
    var audioFailed by mutableStateOf(false);private set
    private fun storage(name:String)=AtomicProgressStorage(File(getApplication<Application>().noBackupFilesDir,name),AndroidAtomicCommit)
    private val progress=AndroidProgressRepository.create(application)
    private val browse=WilmaSelectionStore(storage("wilma-selection"))
    private val quizzes=listOf(WilmaPhase.FIND,WilmaPhase.RELATIONS).associateWith {WilmaContent.host(it,storage("wilma-${it.name.lowercase()}"),progress)}
    private val order=WilmaOrderHost(storage("wilma-order"),progress)
    private val worker=Executors.newSingleThreadExecutor()
    private val main=Handler(Looper.getMainLooper())
    private val audio=WilmaAudio(DefaultAudioController(AndroidSystemSpeechEngine(application),AudioMode.OFF)){audioFailed=it is SpeechResult.Failed}
    private var visible=false
    private var closed=false
    private var epoch=0L
    private var language=ContentLanguage.ENGLISH
    private var round=RoundLength.FIVE
    private var mode=AudioMode.OFF
    // Worker-confined state and decoded-asset cache.
    private var selected=WilmaSelection()
    private var pictures=emptyMap<String,ImageBitmap>()
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
        if(value && selection==null && !busy)retry()
    }
    private fun syncLanguage() {
        val phase=selection?.phase ?: return
        val differs=if(phase==WilmaPhase.ORDER)ordering?.language?.let {it!=language}==true else quiz?.language?.let {it!=language}==true
        if(!busy && !saveFailed && differs) {
            val lang=language
            work {if(phase==WilmaPhase.ORDER)order.dispatch(WilmaOrderAction.Language(lang)) else quizzes[phase]?.dispatch(SessionAction.Language(lang));emptyList()}
        }
    }
    private fun ready()=visible && !busy && !saveFailed && selection!=null
    private fun boundary(){epoch++;audio.cancel()}
    private fun openPhase(phase:WilmaPhase,count:RoundLength,lang:ContentLanguage):List<SessionEffect> {
        val effects=when(phase) {
            WilmaPhase.EXPLORE -> emptyList()
            WilmaPhase.ORDER -> if(order.state==null)listOfNotNull(order.open(id(),System.nanoTime(),lang)) else emptyList()
            else -> quizzes.getValue(phase).let {if(it.state==null)it.open(id(),count,System.nanoTime(),lang) else emptyList()}
        }
        // A returned phase may have been left in the other language; change identity-neutral text silently.
        if(phase==WilmaPhase.ORDER && order.state?.language!=lang)order.dispatch(WilmaOrderAction.Language(lang))
        quizzes[phase]?.let {if(it.state?.language!=lang)it.dispatch(SessionAction.Language(lang))}
        return effects
    }
    fun phase(phase:WilmaPhase) {
        if(!ready() || phase==selection?.phase)return
        boundary();val count=round;val lang=language
        work {
            if(phase==WilmaPhase.EXPLORE) {
                quizzes[selected.phase]?.state?.takeIf {it.hintAvailable}?.let {quizzes.getValue(selected.phase).dispatch(SessionAction.Hint(it.task.id))}
                if(selected.phase==WilmaPhase.ORDER)order.state?.takeIf {!it.completed}?.let {order.dispatch(WilmaOrderAction.Help(it.task))}
            }
            val effects=openPhase(phase,count,lang)
            val next=selected.copy(phase=phase);browse.write(next);selected=next;effects
        }
    }
    fun select(day:ContentId) {
        if(!ready() || selection?.phase!=WilmaPhase.EXPLORE)return
        boundary();work(exploreSpeech=true){val next=selected.copy(selected=day);browse.write(next);selected=next;emptyList()}
    }
    fun replay() {
        if(!ready())return
        when(selection!!.phase) {
            WilmaPhase.EXPLORE -> {boundary();audio.day(selection!!.selected,language,true)}
            WilmaPhase.ORDER -> ordering?.let {orderAction(WilmaOrderAction.Replay(it.task))}
            else -> quiz?.let {action(SessionAction.Replay(it.task.id))}
        }
    }
    fun option(day:ContentId) {if(ready()){boundary();audio.day(day,language)}}
    fun action(action:SessionAction) {
        if(!ready() || imageFailed && action is SessionAction.Answer)return
        val host=quizzes[selection!!.phase] ?: return
        boundary();work {host.dispatch(action)}
    }
    fun orderAction(action:WilmaOrderAction) {
        if(!ready() || selection?.phase!=WilmaPhase.ORDER || imageFailed && action is WilmaOrderAction.Place)return
        boundary();work {listOfNotNull(order.dispatch(action))}
    }
    fun again() {
        if(!ready())return
        val phase=selection!!.phase;val count=round;val lang=language
        if(phase==WilmaPhase.ORDER && ordering?.completed!=true || phase!=WilmaPhase.ORDER && quiz?.phase!=SessionPhase.COMPLETED)return
        boundary();work {if(phase==WilmaPhase.ORDER)listOfNotNull(order.again(id(),System.nanoTime(),lang)) else quizzes.getValue(phase).newRound(id(),count,System.nanoTime(),lang)}
    }
    fun retry() {
        if(busy)return
        boundary();val count=round;val lang=language
        work {
            selected=browse.read()
            // Retry all loaded journals, including an uncertain write immediately before a phase switch.
            quizzes.values.filter {it.state!=null}.forEach {it.retryWrites()}
            if(order.state!=null)order.retryWrites()
            openPhase(selected.phase,count,lang)
            emptyList() // Restore/retry never narrates old instructions.
        }
    }
    private fun loadImages():Map<String,ImageBitmap> {
        if(pictures.isNotEmpty())return pictures
        val paths=WilmaContent.days.map(WilmaContent::image)+listOf(WilmaContent.HEAD,WilmaContent.TAIL,WilmaContent.REFERENCE)
        val loaded=paths.associateWith {path -> getApplication<Application>().assets.open(path).use {
            requireNotNull(BitmapFactory.decodeStream(it,null,BitmapFactory.Options().apply {inSampleSize=2})).asImageBitmap()
        } }
        pictures=loaded;return loaded
    }
    private fun work(exploreSpeech:Boolean=false,operation:()->List<SessionEffect>) {
        if(busy || closed)return
        busy=true;val token=epoch
        worker.execute {
            var failed=false
            val effects=try{operation()}catch(_:Exception){failed=true;emptyList()}
            val selectedSnapshot=selected;val q=quizzes[selected.phase]?.state;val o=order.state
            val pendingFailure=quizzes.values.any {it.failure} || order.failure
            val art=try{loadImages()}catch(_:Exception){emptyMap()}
            main.post {
                if(!closed) {
                    busy=false;saveFailed=failed || pendingFailure;imageFailed=art.isEmpty();images=art
                    if(!failed){selection=selectedSnapshot;quiz=q;ordering=o}
                    if(!failed && visible && token==epoch) {
                        if(exploreSpeech)audio.day(selectedSnapshot.selected,language)
                        else if(selectedSnapshot.phase==WilmaPhase.ORDER && o?.language==language)audio.effects(o.id,language,effects)
                        else if(q?.language==language)audio.effects(q.plan.id,language,effects)
                    }
                    syncLanguage()
                }
            }
        }
    }
    override fun onCleared(){closed=true;audio.close();worker.shutdown();super.onCleared()}
}
