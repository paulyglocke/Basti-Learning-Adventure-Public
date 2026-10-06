package com.bellfamily.bastischool.learning.comparequantity

import com.bellfamily.bastischool.audio.*
import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.session.*

class CompareQuantityAudio(private val controller: AudioController, private val status: (SpeechResult?) -> Unit = {}) {
    private val owner = SpeechOwner("native-compare-quantity")
    private var active = false
    private var revision = 0L
    fun visible(value: Boolean) { active = value; cancel() }
    fun mode(value: AudioMode) { controller.setMode(value); cancel() }
    fun cancel() { revision++; controller.cancelOwner(owner); status(null) }
    fun effects(id: SessionId, language: ContentLanguage, effects: List<SessionEffect>) = effects.forEach { effect ->
        when (effect) {
            SessionEffect.CancelNarration -> cancel()
            is SessionEffect.Narrate -> speak(id.value, language, effect.text,
                if (effect.kind == NarrationKind.FEEDBACK) SpeechKind.FEEDBACK else if (effect.kind == NarrationKind.COMPLETION) SpeechKind.COMPLETION else SpeechKind.INSTRUCTION,
                effect.trigger)
            else -> Unit
        }
    }
    fun replay(state: SessionState, language: ContentLanguage) = speak(
        state.task.id.toString(), language, state.task.question.instruction, SpeechKind.INSTRUCTION, SpeechTrigger.REPLAY
    )
    private fun speak(id: String, language: ContentLanguage, text: ContentText, kind: SpeechKind, trigger: SpeechTrigger) {
        if (!active) return
        cancel(); val ticket = revision; val context = controller.openContext(owner, SpeechSessionId(id), language)
        controller.speak(SpeechRequest.fromContent(context, text, kind, trigger)) { if (active && ticket == revision) status(it.result) }
    }
    fun close() { active = false; cancel(); controller.close() }
}

