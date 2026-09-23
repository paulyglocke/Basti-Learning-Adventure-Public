package com.bellfamily.bastischool.learning.seasons

import com.bellfamily.bastischool.audio.*
import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.session.*

/** One owned main-thread speech boundary for Explore and Practice; no state mutations from callbacks. */
class SeasonsAudio(private val controller: AudioController, private val status: (SpeechResult?) -> Unit = {}) {
    private val owner = SpeechOwner("native-seasons")
    private var revision = 0L
    private var active = false
    fun visible(value: Boolean) { active = value; cancel() }
    fun cancel() { revision++; controller.cancelOwner(owner); status(null) }
    fun mode(value: AudioMode) { controller.setMode(value); cancel() }
    fun explore(selection: SeasonsSelection, language: ContentLanguage, replay: Boolean = false) =
        speak("seasons-explore", language, SeasonsContent.narration(selection.selected), SpeechKind.EXPLANATION,
            if (replay) SpeechTrigger.REPLAY else SpeechTrigger.MANUAL)
    fun option(state: SessionState, id: ContentId) {
        if (id in state.task.question.choices) speak(state.plan.id.value, state.language, SeasonsContent.season(id).text,
            SpeechKind.OPTION, SpeechTrigger.MANUAL)
    }
    fun order(state: SeasonsOrderState, effect: SessionEffect.Narrate?) {
        if(effect != null) speak(state.id.value,state.language,effect.text,when(effect.kind) {
            NarrationKind.INSTRUCTION -> SpeechKind.INSTRUCTION
            NarrationKind.FEEDBACK -> SpeechKind.FEEDBACK
            NarrationKind.COMPLETION -> SpeechKind.COMPLETION
        },effect.trigger)
    }
    fun orderOption(state: SeasonsOrderState,id: ContentId) {
        if(id in state.choices) speak(state.id.value,state.language,SeasonsContent.season(id).text,SpeechKind.OPTION,SpeechTrigger.MANUAL)
    }
    fun effects(state: SessionState, effects: List<SessionEffect>) {
        if (!active) return
        effects.forEach { effect -> when(effect) {
            SessionEffect.CancelNarration -> cancel()
            is SessionEffect.Narrate -> speak(state.plan.id.value, state.language, effect.text, when(effect.kind) {
                NarrationKind.INSTRUCTION -> SpeechKind.INSTRUCTION
                NarrationKind.FEEDBACK -> SpeechKind.FEEDBACK
                NarrationKind.COMPLETION -> SpeechKind.COMPLETION
            }, effect.trigger)
            else -> Unit
        } }
    }
    private fun speak(session: String, language: ContentLanguage, text: ContentText, kind: SpeechKind, trigger: SpeechTrigger) {
        if (!active) return
        cancel(); val ticket = revision
        val context = controller.openContext(owner, SpeechSessionId(session), language)
        controller.speak(SpeechRequest.fromContent(context, text, kind, trigger)) {
            if (active && ticket == revision) status(it.result)
        }
    }
    fun close() { active = false; cancel(); controller.close() }
}
