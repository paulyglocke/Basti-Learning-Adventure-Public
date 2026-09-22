package com.bellfamily.bastischool.learning.prepositions

import com.bellfamily.bastischool.audio.*
import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.session.*

/** Main-thread effect boundary. Leaving invalidates callbacks, including in-flight disk operations. */
class PrepositionsAudio(private val controller: AudioController, private val status: (SpeechResult?) -> Unit = {}) {
    private val owner = SpeechOwner("native-prepositions")
    private var revision = 0L
    private var active = false
    fun visible(value: Boolean) { active = value; cancel() }
    fun cancel() { revision++; controller.cancelOwner(owner); status(null) }
    fun mode(value: AudioMode) { controller.setMode(value); cancel() }
    fun effects(state: SessionState, effects: List<SessionEffect>) {
        if (!active) return
        for (effect in effects) when (effect) {
            SessionEffect.CancelNarration -> cancel()
            is SessionEffect.Narrate -> speak(state, effect.text, when(effect.kind) {
                NarrationKind.INSTRUCTION -> SpeechKind.INSTRUCTION
                NarrationKind.FEEDBACK -> SpeechKind.FEEDBACK
                NarrationKind.COMPLETION -> SpeechKind.COMPLETION
            }, effect.trigger)
            else -> Unit
        }
    }
    fun option(state: SessionState, id: ContentId) {
        if (id in state.task.question.choices) speak(state, PrepositionsContent.repository.find(id)!!.text, SpeechKind.OPTION, SpeechTrigger.MANUAL)
    }
    fun introduction(state: SessionState, automatic: Boolean = false, result: (SpeechResult) -> Unit) {
        val tutorial = PrepositionsContent.tutorial
        val question = state.task.question.instruction
        val combined = ContentText.plain("${tutorial.speech.en} ${question.speech.en}", "${tutorial.speech.de} ${question.speech.de}")
        speak(state, combined, SpeechKind.TUTORIAL, if (automatic) SpeechTrigger.AUTOMATIC else SpeechTrigger.MANUAL, result)
    }
    private fun speak(state: SessionState, text: ContentText, kind: SpeechKind, trigger: SpeechTrigger,
                      result: (SpeechResult) -> Unit = {}) {
        if (!active) return
        cancel()
        val token = revision
        val context = controller.openContext(owner, SpeechSessionId(state.plan.id.value), state.language)
        controller.speak(SpeechRequest.fromContent(context, text, kind, trigger)) {
            if (active && token == revision) { status(it.result); result(it.result) }
        }
    }
    fun close() { active = false; cancel(); controller.close() }
}
