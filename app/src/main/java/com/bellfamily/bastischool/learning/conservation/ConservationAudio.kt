package com.bellfamily.bastischool.learning.conservation

import com.bellfamily.bastischool.audio.*
import com.bellfamily.bastischool.learning.models.ContentText

class ConservationAudio(private val controller: AudioController, private val failed: (Boolean) -> Unit = {}) {
    private val owner=SpeechOwner("native-conservation")
    private var visible=false
    private var epoch=0L
    fun visible(value: Boolean) { visible=value; cancel() }
    fun mode(value: AudioMode) { controller.setMode(value); cancel() }
    fun cancel() { epoch++; controller.cancelOwner(owner); failed(false) }
    fun speak(state: Conservation.State, content: ContentText, kind: SpeechKind, manual: Boolean=false) {
        if(!visible)return
        cancel();val token=epoch
        val context=controller.openContext(owner,SpeechSessionId(state.id.value),state.language)
        controller.speak(SpeechRequest.fromContent(context,content,kind,if(manual)SpeechTrigger.REPLAY else SpeechTrigger.AUTOMATIC)) {
            if(visible&&token==epoch)failed(it.result is SpeechResult.Failed)
        }
    }
    fun close() { visible=false; cancel(); controller.close() }
    companion object {
        fun current(s: Conservation.State)=when {
            s.completed -> Conservation.complete
            s.current.solved -> Conservation.feedback(s)
            else -> Conservation.prompt(s.task,s.config)
        }
    }
}
