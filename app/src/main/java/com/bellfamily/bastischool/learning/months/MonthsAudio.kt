package com.bellfamily.bastischool.learning.months

import com.bellfamily.bastischool.audio.*
import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.session.*

/** Activity adapter only: policy, engine and speech ownership use the shared controller. */
class MonthsAudio(private val controller: AudioController, private val failed: (Boolean) -> Unit = {}) {
    private val owner = SpeechOwner("native-months")
    private var visible = false
    private var epoch = 0L
    fun visible(value: Boolean) { visible = value; cancel() }
    fun mode(value: AudioMode) { controller.setMode(value); cancel() }
    fun cancel() { epoch++; controller.cancelOwner(owner); failed(false) }
    fun month(id: ContentId, language: ContentLanguage) {
        speak(MonthContent.ACTIVITY, MonthContent.month(id).text, language, SpeechKind.OPTION, SpeechTrigger.MANUAL)
    }
    fun effects(id: SessionId, language: ContentLanguage, effects: List<SessionEffect>) {
        effects.forEach { effect -> when(effect) {
            SessionEffect.CancelNarration -> cancel()
            is SessionEffect.Narrate -> speak(id.value, effect.text, language, when(effect.kind) {
                NarrationKind.INSTRUCTION -> SpeechKind.INSTRUCTION
                NarrationKind.FEEDBACK -> SpeechKind.FEEDBACK
                NarrationKind.COMPLETION -> SpeechKind.COMPLETION
            }, effect.trigger)
            else -> Unit
        } }
    }
    private fun speak(session: String, text: ContentText, language: ContentLanguage, kind: SpeechKind, trigger: SpeechTrigger) {
        if (!visible) return
        cancel()
        val ticket = epoch
        val context = controller.openContext(owner, SpeechSessionId(session), language)
        controller.speak(SpeechRequest.fromContent(context, text, kind, trigger)) {
            if (visible && ticket == epoch) failed(it.result is SpeechResult.Failed)
        }
    }
    fun close() { visible = false; cancel(); controller.close() }
}
