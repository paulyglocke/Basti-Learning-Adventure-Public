package com.bellfamily.bastischool.ui.quantitychange

import android.app.Application
import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.*
import androidx.lifecycle.AndroidViewModel
import com.bellfamily.bastischool.audio.*
import com.bellfamily.bastischool.audio.android.AndroidSystemSpeechEngine
import com.bellfamily.bastischool.learning.quantitychange.*
import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.progress.AtomicProgressStorage
import com.bellfamily.bastischool.learning.progress.android.AndroidAtomicCommit
import com.bellfamily.bastischool.learning.progress.android.AndroidProgressRepository
import com.bellfamily.bastischool.learning.session.SessionId
import com.bellfamily.bastischool.learning.session.RoundLength
import java.io.File
import java.util.UUID
import java.util.concurrent.Executors

class QuantityChangeViewModel @JvmOverloads constructor(application: Application,
    engineFactory: () -> SpeechEngine = { AndroidSystemSpeechEngine(application) }) : AndroidViewModel(application) {
    var state by mutableStateOf<QuantityChange.State?>(null); private set
    var busy by mutableStateOf(false); private set
    var saveFailed by mutableStateOf(false); private set
    var audioFailed by mutableStateOf(false); private set
    private val main=Handler(Looper.getMainLooper())
    private val worker=Executors.newSingleThreadExecutor()
    private val hosts=mutableMapOf<ChangeConfig,QuantityChangeHost>() // Worker confined.
    private val audio=QuantityChangeAudio(DefaultAudioController(engineFactory,AudioMode.OFF)) { audioFailed=it }
    private var language=ContentLanguage.ENGLISH
    private var mode=AudioMode.OFF
    private var config: ChangeConfig?=null
    private var round=RoundLength.FIVE
    private var visible=false
    private var closed=false
    private var speechEpoch=0L

    fun configure(lang: String, audioMode: String, count:Int) {
        round=if(count==10)RoundLength.TEN else RoundLength.FIVE
        val nextLanguage=if(lang=="de")ContentLanguage.GERMAN else ContentLanguage.ENGLISH
        val nextMode=when(audioMode){"all"->AudioMode.ALL;"questions"->AudioMode.QUESTIONS;else->AudioMode.OFF}
        if(nextMode!=mode){mode=nextMode; speechEpoch++; audio.mode(mode)}
        if(nextLanguage!=language){language=nextLanguage; interrupt(); reconcile()}
    }
    fun setVisible(value: Boolean, selected: ChangeConfig?=null) {
        if(selected!=null&&selected!=config){config=selected;state=null;saveFailed=false;interrupt()}
        if(value!=visible){visible=value;speechEpoch++;audio.visible(value)}
        if(value)reconcile()
    }
    private fun reconcile() {
        if(closed||busy)return
        if(config==null)return
        if(state==null&&visible) {
            val lang=language;val length=round
            work(speech={opened,after->if(opened)QuantityChange.prompt(after.task,after.config)else null}) { it.open(newId(),length,System.nanoTime(),lang) }
        } else if(state?.language!=language&&state!=null) {
            val lang=language;work { it.dispatch(QuantityChange.Action.Language(lang)) }
        }
    }
    fun toggle(slot:Int) {
        if(!ready()||state?.current?.solved==true)return
        interrupt();work { it.dispatch(QuantityChange.Action.Toggle(slot)) }
    }
    fun undo() {
        if(!ready()||state?.current?.solved==true)return
        interrupt();work { it.dispatch(QuantityChange.Action.Undo) }
    }
    fun check() {
        val s=state?:return
        if(!ready()||s.current.solved||s.needsCorrection)return
        interrupt()
        work(speech={_,after->QuantityChange.feedback(after)},kind=SpeechKind.FEEDBACK) { it.dispatch(QuantityChange.Action.Check) }
    }
    fun next() {
        if(!ready()||state?.current?.solved!=true)return
        interrupt()
        work(speech={_,after->if(after.completed)QuantityChange.complete else QuantityChange.prompt(after.task,after.config)}) { it.dispatch(QuantityChange.Action.Next) }
    }
    fun replay() {
        if(!ready())return
        interrupt()
        work(speech={_,after->QuantityChangeAudio.current(after)},manual=true) { it.dispatch(QuantityChange.Action.Replay) }
    }
    fun help() {
        if(!ready()||state?.current?.solved==true)return
        interrupt()
        work(speech={_,after->QuantityChange.hint(after.config)},kind=SpeechKind.EXPLANATION,manual=true) { it.dispatch(QuantityChange.Action.Help) }
    }
    fun again() {
        if(!ready()||state?.completed!=true)return
        interrupt();val lang=language;val length=round
        work(speech={_,after->QuantityChange.prompt(after.task,after.config)}) { it.again(newId(),length,System.nanoTime(),lang) }
    }
    fun retrySave() {
        if(busy)return
        interrupt();val lang=language;val length=round
        work { if(it.state==null)it.open(newId(),length,System.nanoTime(),lang)else it.retryWrites() }
    }
    private fun ready()=visible&&!busy&&!saveFailed&&state?.config==config&&state?.language==language
    private fun interrupt(){speechEpoch++;audio.cancel()}
    private fun newId()=SessionId(UUID.randomUUID().toString())
    private fun <T> work(speech: ((Boolean,QuantityChange.State)->ContentText?)?=null,manual: Boolean=false,kind:SpeechKind=SpeechKind.QUESTION,operation:(QuantityChangeHost)->T) {
        if(closed||busy)return
        val target=config?:return
        busy=true;val audioToken=speechEpoch
        worker.execute {
            val host=hosts.getOrPut(target) { QuantityChangeHost(
                AtomicProgressStorage(File(getApplication<Application>().noBackupFilesDir,"quantity-change-${target.key}-session"),AndroidAtomicCommit),
                AndroidProgressRepository.create(getApplication()),target) }
            val result=runCatching { operation(host) };val next=host.state;val failed=result.isFailure||host.failure
            main.post {
                if(!closed) {
                    busy=false
                    if(target==config){state=next;saveFailed=failed
                        if(!failed&&next!=null&&next.language==language&&visible&&audioToken==speechEpoch) {
                            speech?.invoke(result.getOrNull() as? Boolean?:false,next)?.let {
                                audio.speak(next,it,if(next.completed)SpeechKind.COMPLETION else kind,manual)
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
