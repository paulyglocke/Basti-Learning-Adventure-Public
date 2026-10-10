package com.bellfamily.bastischool.learning.months

import com.bellfamily.bastischool.audio.*
import com.bellfamily.bastischool.learning.models.*

/** Activity adapter only: policy, engine and speech ownership use the shared controller. */
class MonthsAudio(private val controller: AudioController, private val failed: (Boolean) -> Unit = {}) {
    private val owner = SpeechOwner("native-months")
    private var visible = false
    private var epoch = 0L
    fun visible(value: Boolean) { visible = value; cancel() }
    fun mode(value: AudioMode) { controller.setMode(value); cancel() }
    fun cancel() { epoch++; controller.cancelOwner(owner); failed(false) }
    fun month(id: ContentId, language: ContentLanguage) {
        if (!visible) return
        cancel()
        val ticket = epoch
        val context = controller.openContext(owner, SpeechSessionId(MonthContent.ACTIVITY), language)
        controller.speak(SpeechRequest.fromContent(context, MonthContent.month(id).text, SpeechKind.OPTION, SpeechTrigger.MANUAL)) {
            if (visible && ticket == epoch) failed(it.result is SpeechResult.Failed)
        }
    }
    fun close() { visible = false; cancel(); controller.close() }
}
