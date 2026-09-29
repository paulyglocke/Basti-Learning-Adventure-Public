package com.bellfamily.bastischool.ui.animalgroups

import android.app.Application
import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.*
import androidx.lifecycle.AndroidViewModel
import com.bellfamily.bastischool.audio.*
import com.bellfamily.bastischool.audio.android.AndroidSystemSpeechEngine
import com.bellfamily.bastischool.learning.animalgroups.*
import com.bellfamily.bastischool.learning.sorting.*
import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.progress.*
import com.bellfamily.bastischool.learning.progress.android.*
import com.bellfamily.bastischool.learning.session.*
import java.io.File
import java.util.UUID
import java.util.concurrent.Executors

class AnimalGroupsViewModel @JvmOverloads constructor(application: Application,
    engineFactory: () -> SpeechEngine = { AndroidSystemSpeechEngine(application) }
) : AndroidViewModel(application) {
    var state by mutableStateOf<SortState?>(null); private set
    var busy by mutableStateOf(false); private set
    var saveFailed by mutableStateOf(false); private set
    var audioFailed by mutableStateOf(false); private set
    var images by mutableStateOf<Map<ContentId, androidx.compose.ui.graphics.ImageBitmap>>(emptyMap()); private set
    private val artwork = AnimalGroupsArtworkLoader { application.assets.open(it) }
    private val worker = Executors.newSingleThreadExecutor()
    private val main = Handler(Looper.getMainLooper())
    private val host = AnimalGroupsHost(AtomicProgressStorage(File(application.noBackupFilesDir,"animal-groups-session"),AndroidAtomicCommit),AndroidProgressRepository.create(application))
    private val audio = DefaultAudioController(engineFactory,AudioMode.OFF)
    private val owner = SpeechOwner("native-animal-groups")
    private var visible = false
    private var closed = false
    private var language = ContentLanguage.ENGLISH
    private var mode = AudioMode.OFF
    private var epoch = 0L
    fun configure(lang: String, audioMode: String) {
        val next = if(lang=="de") ContentLanguage.GERMAN else ContentLanguage.ENGLISH
        val nextMode = when(audioMode){"all"->AudioMode.ALL;"questions"->AudioMode.QUESTIONS;else->AudioMode.OFF}
        if(language != next || mode != nextMode) {
            language=next;mode=nextMode;cancel();audio.setMode(mode)
            syncLanguage()
        }
    }
    fun setVisible(value: Boolean) {
        if(visible==value)return
        visible=value;cancel()
        if(value && state==null && !busy) work {host.open(SessionId(UUID.randomUUID().toString()),System.nanoTime(),language)}
    }
    private fun cancel(){epoch++;audio.cancelOwner(owner);audioFailed=false}
    private fun syncLanguage(){if(!busy && state!=null && state!!.language!=language)work {host.dispatch(SortAction.Language(language))}}
    fun action(action: SortAction){if(visible && !busy && !saveFailed && state?.language==language){cancel();work {host.dispatch(action)}}}
    fun again(){if(visible && !busy && !saveFailed && state?.completed==true){cancel();work {host.again(SessionId(UUID.randomUUID().toString()),System.nanoTime(),language)}}}
    fun retry(){if(!busy){cancel();work {host.retryWrites();null}}}
    private fun work(operation:()->SessionEffect.Narrate?) {
        if(busy || closed)return
        busy=true;val ticket=epoch
        worker.execute {
            var failed=false;var speech:SessionEffect.Narrate?=null
            try {speech=operation()}catch(_:Exception){failed=true}
            val next=host.state;val pending=host.failure
            val loaded = try { artwork.load() } catch(_:Exception) { emptyMap() }
            main.post {
                if(!closed){
                    busy=false;saveFailed=failed || pending
                    images=loaded
                    if(!failed)state=next
                    if(!saveFailed && ticket==epoch && visible && next?.language==language) speech?.let {speak(it,next.id)}
                    if(!saveFailed)syncLanguage()
                }
            }
        }
    }
    private fun speak(effect:SessionEffect.Narrate,id:SessionId) {
        val ticket=epoch
        val context=audio.openContext(owner,SpeechSessionId(id.value),language)
        val kind=when(effect.kind){NarrationKind.FEEDBACK->SpeechKind.FEEDBACK;NarrationKind.COMPLETION->SpeechKind.COMPLETION;else->SpeechKind.INSTRUCTION}
        audio.speak(SpeechRequest.fromContent(context,effect.text,kind,effect.trigger)){if(!closed && visible && ticket==epoch)audioFailed=it.result is SpeechResult.Failed}
    }
    override fun onCleared(){closed=true;cancel();audio.close();worker.shutdown();super.onCleared()}
}
