package com.bellfamily.bastischool.learning.wilma

import com.bellfamily.bastischool.audio.*
import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.session.*

class WilmaAudio(private val controller:AudioController,private val status:(SpeechResult?)->Unit = {}) {
    private val owner=SpeechOwner("native-wilma")
    private var active=false
    private var revision=0L
    fun visible(value:Boolean) {active=value;cancel()}
    fun mode(value:AudioMode) {controller.setMode(value);cancel()}
    fun cancel() {revision++;controller.cancelOwner(owner);status(null)}
    fun day(id:ContentId,language:ContentLanguage,replay:Boolean=false)=speak("wilma-explore",language,WilmaContent.day(id).text,
        SpeechKind.OPTION,if(replay)SpeechTrigger.REPLAY else SpeechTrigger.MANUAL)
    fun effects(id:SessionId,language:ContentLanguage,effects:List<SessionEffect>) {
        effects.forEach {effect -> when(effect) {
            SessionEffect.CancelNarration -> cancel()
            is SessionEffect.Narrate -> speak(id.value,language,effect.text,when(effect.kind) {
                NarrationKind.INSTRUCTION -> SpeechKind.INSTRUCTION
                NarrationKind.FEEDBACK -> SpeechKind.FEEDBACK
                NarrationKind.COMPLETION -> SpeechKind.COMPLETION
            },effect.trigger)
            else -> Unit
        } }
    }
    private fun speak(id:String,language:ContentLanguage,text:ContentText,kind:SpeechKind,trigger:SpeechTrigger) {
        if(!active)return
        cancel();val ticket=revision
        val context=controller.openContext(owner,SpeechSessionId(id),language)
        controller.speak(SpeechRequest.fromContent(context,text,kind,trigger)) {if(active && ticket==revision)status(it.result)}
    }
    fun close(){active=false;cancel();controller.close()}
}
