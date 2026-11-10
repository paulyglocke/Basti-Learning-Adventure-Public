package com.bellfamily.bastischool.ui.memory

import android.app.Application
import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.*
import androidx.lifecycle.AndroidViewModel
import com.bellfamily.bastischool.audio.*
import com.bellfamily.bastischool.audio.android.AndroidSystemSpeechEngine
import com.bellfamily.bastischool.learning.memory.*
import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.progress.AtomicProgressStorage
import com.bellfamily.bastischool.learning.progress.android.AndroidAtomicCommit
import com.bellfamily.bastischool.learning.progress.android.AndroidProgressRepository
import com.bellfamily.bastischool.learning.session.SessionId
import java.io.File
import java.util.UUID
import java.util.concurrent.Executors

class MemoryPairsViewModel @JvmOverloads constructor(application: Application,
    engineFactory: () -> SpeechEngine = { AndroidSystemSpeechEngine(application) }) : AndroidViewModel(application) {
    var state by mutableStateOf<MemoryPairs.State?>(null); private set
    var busy by mutableStateOf(false); private set
    var saveFailed by mutableStateOf(false); private set
    var audioFailed by mutableStateOf(false); private set
    private val main=Handler(Looper.getMainLooper())
    private val worker=Executors.newSingleThreadExecutor()
    private val hosts=mutableMapOf<MemoryConfig,MemoryPairsHost>() // Worker confined.
    private val audio=MemoryPairsAudio(DefaultAudioController(engineFactory,AudioMode.OFF)) { audioFailed=it }
    private var language=ContentLanguage.ENGLISH
    private var mode=AudioMode.OFF
    private var config: MemoryConfig?=null
    private var visible=false
    private var closed=false
    private var speechEpoch=0L

    fun configure(lang: String, audioMode: String) {
        val nextLanguage=if(lang=="de")ContentLanguage.GERMAN else ContentLanguage.ENGLISH
        val nextMode=when(audioMode){"all"->AudioMode.ALL;"questions"->AudioMode.QUESTIONS;else->AudioMode.OFF}
        if(nextMode!=mode){mode=nextMode; speechEpoch++; audio.mode(mode)}
        if(nextLanguage!=language){language=nextLanguage; interrupt(); reconcile()}
    }
    fun setVisible(value: Boolean, selected: MemoryConfig?=null) {
        if(selected!=null&&selected!=config){config=selected;state=null;saveFailed=false;interrupt()}
        if(value!=visible){visible=value;speechEpoch++;audio.visible(value)}
        if(value)reconcile()
    }
    private fun reconcile() {
        if(closed||busy)return
        if(config==null)return
        if(state==null&&visible) {
            val lang=language
            work(speech={opened,_->if(opened)MemoryPairs.instruction else null}) { it.open(newId(),System.nanoTime(),lang) }
        } else if(state?.language!=language&&state!=null) {
            val lang=language;work { it.dispatch(MemoryPairs.Action.Language(lang)) }
        }
    }
    fun reveal(slot: Int) {
        val s=state?:return
        if(!ready()||s.phase!=MemoryPairs.Phase.PLAYING||slot !in s.deck.indices||s.visible(slot))return
        interrupt()
        work(speech={_,after->MemoryPairs.label(after.deck[slot])}) { it.dispatch(MemoryPairs.Action.Reveal(slot)) }
    }
    fun next() {
        if(!ready()||state?.phase==MemoryPairs.Phase.PLAYING)return
        interrupt()
        work(speech={_,after->if(after.completed)MemoryPairs.complete else null}) { it.dispatch(MemoryPairs.Action.Continue) }
    }
    fun replay() {
        if(!ready())return
        interrupt()
        work(speech={_,after->MemoryPairsAudio.current(after)},manual=true) { it.dispatch(MemoryPairs.Action.Replay) }
    }
    fun listen(slot: Int) {
        val s=state?:return
        if(!ready()||slot !in s.deck.indices||!s.visible(slot))return
        interrupt()
        work(speech={_,after->MemoryPairs.label(after.deck[slot])},manual=true) { it.dispatch(MemoryPairs.Action.Listen(slot)) }
    }
    fun help() {
        if(!ready()||state?.phase!=MemoryPairs.Phase.PLAYING)return
        interrupt()
        work(speech={_,_->MemoryPairs.model},manual=true) { it.dispatch(MemoryPairs.Action.Help) }
    }
    fun again() {
        if(!ready()||state?.completed!=true)return
        interrupt();val lang=language
        work(speech={_,_->MemoryPairs.instruction}) { it.again(newId(),System.nanoTime(),lang) }
    }
    fun retrySave() {
        if(busy)return
        interrupt()
        work { if(it.state==null)it.open(newId(),System.nanoTime(),language)else it.retryWrites() }
    }
    private fun ready()=visible&&!busy&&!saveFailed&&state?.config==config&&state?.language==language
    private fun interrupt(){speechEpoch++;audio.cancel()}
    private fun newId()=SessionId(UUID.randomUUID().toString())
    private fun <T> work(speech: ((Boolean,MemoryPairs.State)->ContentText?)?=null,manual: Boolean=false,operation:(MemoryPairsHost)->T) {
        if(closed||busy)return
        val target=config?:return
        busy=true;val audioToken=speechEpoch
        worker.execute {
            val host=hosts.getOrPut(target) { MemoryPairsHost(
                AtomicProgressStorage(File(getApplication<Application>().noBackupFilesDir,"memory-pairs-${target.key}-session"),AndroidAtomicCommit),
                AndroidProgressRepository.create(getApplication()),target) }
            val result=runCatching { operation(host) };val next=host.state;val failed=result.isFailure||host.failure
            main.post {
                if(!closed) {
                    busy=false
                    if(target==config){state=next;saveFailed=failed
                        if(!failed&&next!=null&&next.language==language&&visible&&audioToken==speechEpoch) {
                            speech?.invoke(result.getOrNull() as? Boolean?:false,next)?.let {
                                audio.speak(next,it,if(next.completed)SpeechKind.COMPLETION else SpeechKind.EXPLANATION,manual)
                            }
                        }
                    }
                    if(!saveFailed)reconcile()
                }
            }
        }
    }
    override fun onCleared(){closed=true;audio.close();worker.shutdown();super.onCleared()}
}
