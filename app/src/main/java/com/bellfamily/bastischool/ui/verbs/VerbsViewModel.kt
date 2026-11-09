package com.bellfamily.bastischool.ui.verbs

import android.app.Application
import androidx.compose.runtime.*
import androidx.lifecycle.AndroidViewModel
import com.bellfamily.bastischool.audio.*
import com.bellfamily.bastischool.audio.android.AndroidSystemSpeechEngine
import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.verbs.*
import com.bellfamily.bastischool.learning.session.*
import com.bellfamily.bastischool.ui.addition.AdditionViewModel

class VerbsQuizViewModel(application:Application):AdditionViewModel(application,VerbContent,"native-animal-actions-session",{AndroidSystemSpeechEngine(application)}) {
    override val narrateNewRound = true
    private val prefs=application.getSharedPreferences("basti_shell",0)
    private val intros=VerbIntroductions({prefs.getLong(it,-1)},{key,value->prefs.edit().putLong(key,value).apply()},{prefs.getLong("tutorialResetEpoch",0)})
    private var pending:VerbIntroductions.Prepared?=null
    override fun narration(state:SessionState,effects:List<SessionEffect>):List<SessionEffect> = effects.map { effect ->
        if(effect is SessionEffect.Narrate && effect.kind==NarrationKind.INSTRUCTION && effect.trigger==SpeechTrigger.AUTOMATIC) {
            val prepared=intros.prepare("quiz",state.language,effect.text);pending=prepared;effect.copy(text=prepared.text)
        } else effect
    }
    override fun narrated(text:ContentText,result:SpeechResult) {pending?.takeIf{it.text==text}?.result?.invoke(result)}
    fun instructions()=option(VerbContent.instructionsId)
}

class VerbExplorerViewModel @JvmOverloads constructor(application:Application,
    engineFactory:()->SpeechEngine={AndroidSystemSpeechEngine(application)}):AndroidViewModel(application) {
    var audioFailed by mutableStateOf(false);private set
    private val audio=DefaultAudioController(engineFactory,AudioMode.OFF)
    private val owner=SpeechOwner("native-verb-explorer")
    private var visible=false
    private var language=ContentLanguage.ENGLISH
    private var mode=AudioMode.OFF
    private var epoch=0L
    private var pendingLesson:String?=null
    private val prefs=application.getSharedPreferences("basti_shell",0)
    private val intros=VerbIntroductions({prefs.getLong(it,-1)},{key,value->prefs.edit().putLong(key,value).apply()},{prefs.getLong("tutorialResetEpoch",0)})
    fun prepareLesson(id:String){pendingLesson=id}
    fun entered(lesson:VerbLesson) {
        if(visible&&pendingLesson==lesson.id.value){pendingLesson=null;listen(lesson,manual=false)}
    }
    fun configure(lang:String,sound:String) {
        val next=if(lang=="de")ContentLanguage.GERMAN else ContentLanguage.ENGLISH
        val nextMode=when(sound){"all"->AudioMode.ALL;"questions"->AudioMode.QUESTIONS;else->AudioMode.OFF}
        if(next!=language||nextMode!=mode){interrupt();language=next;mode=nextMode;audio.setMode(mode)}
    }
    fun setVisible(value:Boolean){if(visible!=value){visible=value;if(!value)pendingLesson=null;interrupt()}}
    fun interrupt(){epoch++;audio.cancelOwner(owner);audioFailed=false}
    fun listen(lesson:VerbLesson,guide:Boolean=false,manual:Boolean=true) {
        if(!visible)return
        val content=if(guide)ContentText.plain("Grown-up, make the animal's sound for Basti. Listen carefully, then try making that sound together.",
            "Mach Basti als erwachsene Begleitperson das Tiergeräusch vor. Hör gut zu und versucht dann gemeinsam, das Geräusch nachzumachen.")
        else ContentText.plain(lesson.speech(ContentLanguage.ENGLISH),lesson.speech(ContentLanguage.GERMAN))
        val prepared=if(!manual&&!guide)intros.prepare("explorer",language,content)else null
        interrupt();val ticket=epoch
        val context=audio.openContext(owner,SpeechSessionId(lesson.id.value),language)
        audio.speak(SpeechRequest.fromContent(context,prepared?.text?:content,SpeechKind.EXPLANATION,if(manual)SpeechTrigger.MANUAL else SpeechTrigger.AUTOMATIC)){
            if(visible&&ticket==epoch){audioFailed=it.result is SpeechResult.Failed;prepared?.result?.invoke(it.result)}
        }
    }
    override fun onCleared(){visible=false;interrupt();audio.close();super.onCleared()}
}
