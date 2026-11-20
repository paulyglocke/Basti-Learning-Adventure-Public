package com.bellfamily.bastischool.learning.numberstories

import com.bellfamily.bastischool.audio.*
import com.bellfamily.bastischool.learning.models.ContentText

class NumberStoriesAudio(private val controller: AudioController, private val failed: (Boolean) -> Unit = {}) {
    private val owner=SpeechOwner("native-number-stories")
    private var visible=false
    private var epoch=0L
    fun visible(value: Boolean) { visible=value; cancel() }
    fun mode(value: AudioMode) { controller.setMode(value); cancel() }
    fun cancel() { epoch++; controller.cancelOwner(owner); failed(false) }
    fun speak(state: NumberStories.State, content: ContentText, kind: SpeechKind, manual: Boolean=false) {
        if(!visible)return
        cancel();val token=epoch
        val context=controller.openContext(owner,SpeechSessionId(state.id.value),state.language)
        controller.speak(SpeechRequest.fromContent(context,content,kind,if(manual)SpeechTrigger.REPLAY else SpeechTrigger.AUTOMATIC)) {
            if(visible&&token==epoch)failed(it.result is SpeechResult.Failed)
        }
    }
    fun close() { visible=false; cancel(); controller.close() }
    companion object {
        fun current(s: NumberStories.State)=when {
            s.completed -> NumberStories.complete
            s.current.solved -> NumberStories.feedback(s)
            else -> NumberStories.prompt(s.task,s.config)
        }
    }
}
