package com.bellfamily.bastischool.ui.length

import android.app.Application
import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.*
import androidx.lifecycle.AndroidViewModel
import com.bellfamily.bastischool.audio.*
import com.bellfamily.bastischool.audio.android.AndroidSystemSpeechEngine
import com.bellfamily.bastischool.learning.length.*
import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.progress.AtomicProgressStorage
import com.bellfamily.bastischool.learning.progress.android.AndroidAtomicCommit
import com.bellfamily.bastischool.learning.progress.android.AndroidProgressRepository
import com.bellfamily.bastischool.learning.session.SessionId
import com.bellfamily.bastischool.learning.session.RoundLength
import java.io.File
import java.util.UUID
import java.util.concurrent.Executors

class LengthOrderingViewModel @JvmOverloads constructor(application: Application,
    engineFactory: () -> SpeechEngine = { AndroidSystemSpeechEngine(application) }) : AndroidViewModel(application) {
    var state by mutableStateOf<LengthOrdering.State?>(null); private set
    var busy by mutableStateOf(false); private set
    var saveFailed by mutableStateOf(false); private set
    var audioFailed by mutableStateOf(false); private set
    private val main=Handler(Looper.getMainLooper())
    private val worker=Executors.newSingleThreadExecutor()
    private var host: LengthOrderingHost?=null // Worker confined.
    private val audio=LengthOrderingAudio(DefaultAudioController(engineFactory,AudioMode.OFF)) { audioFailed=it }
    private var language=ContentLanguage.ENGLISH
    private var mode=AudioMode.OFF
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
    fun setVisible(value: Boolean) {
        if(value!=visible){visible=value;speechEpoch++;audio.visible(value)}
        if(value)reconcile()
    }
    private fun reconcile() {
        if(closed||busy)return
        if(state==null&&visible) {
            val lang=language;val length=round
            work(speech={opened,_->if(opened)LengthOrdering.prompt else null}) { it.open(newId(),length,System.nanoTime(),lang) }
        } else if(state?.language!=language&&state!=null) {
            val lang=language;work { it.dispatch(LengthOrdering.Action.Language(lang)) }
        }
    }
    fun place(strip:ContentId) {
        if(!ready()||state?.solved==true)return
        interrupt();work { it.dispatch(LengthOrdering.Action.Place(strip)) }
    }
    fun remove(strip:ContentId) {
        if(!ready()||state?.solved==true)return
        interrupt();work { it.dispatch(LengthOrdering.Action.Remove(strip)) }
    }
    fun undo() {
        if(!ready()||state?.solved==true)return
        interrupt();work { it.dispatch(LengthOrdering.Action.Undo) }
    }
    fun check() {
        val s=state?:return
        if(!ready()||!s.canCheck)return
        interrupt()
        work(speech={_,after->LengthOrdering.feedback(after)},kind=SpeechKind.FEEDBACK) { it.dispatch(LengthOrdering.Action.Check) }
    }
    fun next() {
        if(!ready()||state?.solved!=true)return
        interrupt()
        work(speech={_,after->if(after.completed)LengthOrdering.complete else LengthOrdering.prompt}) { it.dispatch(LengthOrdering.Action.Next) }
    }
    fun replay() {
        if(!ready())return
        interrupt()
        work(speech={_,after->LengthOrderingAudio.current(after)},manual=true) { it.dispatch(LengthOrdering.Action.Replay) }
    }
    fun help() {
        if(!ready()||state?.solved==true)return
        interrupt()
        work(speech={_,after->LengthOrdering.hint},kind=SpeechKind.EXPLANATION,manual=true) { it.dispatch(LengthOrdering.Action.Help) }
    }
    fun again() {
        if(!ready()||state?.completed!=true)return
        interrupt();val lang=language;val length=round
        work(speech={_,after->LengthOrdering.prompt}) { it.again(newId(),length,System.nanoTime(),lang) }
    }
    fun retrySave() {
        if(busy)return
        interrupt();val lang=language;val length=round
        work { if(it.state==null)it.open(newId(),length,System.nanoTime(),lang)else it.retryWrites() }
    }
    private fun ready()=visible&&!busy&&!saveFailed&&state!=null&&state?.language==language
    private fun interrupt(){speechEpoch++;audio.cancel()}
    private fun newId()=SessionId(UUID.randomUUID().toString())
    private fun <T> work(speech: ((Boolean,LengthOrdering.State)->ContentText?)?=null,manual: Boolean=false,kind:SpeechKind=SpeechKind.QUESTION,operation:(LengthOrderingHost)->T) {
        if(closed||busy)return
        busy=true;val audioToken=speechEpoch
        worker.execute {
            val owner=host ?: LengthOrderingHost(
                AtomicProgressStorage(File(getApplication<Application>().noBackupFilesDir,"length-order-session"),AndroidAtomicCommit),
                AndroidProgressRepository.create(getApplication())).also { host=it }
            val result=runCatching { operation(owner) };val next=owner.state;val failed=result.isFailure||owner.failure
            main.post {
                if(!closed) {
                    busy=false
                    state=next;saveFailed=failed
                        if(!failed&&next!=null&&next.language==language&&visible&&audioToken==speechEpoch) {
                            speech?.invoke(result.getOrNull() as? Boolean?:false,next)?.let {
                                audio.speak(next,it,if(next.completed)SpeechKind.COMPLETION else kind,manual)
                            }
                        }
                    if(!saveFailed)reconcile()
                }
            }
        }
    }
    override fun onCleared(){closed=true;audio.close();worker.shutdown();super.onCleared()}
}
