package com.bellfamily.bastischool.learning.prepositions

import com.bellfamily.bastischool.audio.*
import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.session.*

/** Main-thread effect boundary. Leaving invalidates callbacks, including in-flight disk operations. */
class PrepositionsAudio(private val controller: AudioController,
    private val spokenOption: (ContentId?) -> Unit = {},
    private val status: (SpeechResult?) -> Unit = {}) {
    private val owner = SpeechOwner("native-prepositions")
    private var revision = 0L
    private var active = false
    fun visible(value: Boolean) { active = value; cancel() }
    fun cancel() { revision++; spokenOption(null); controller.cancelOwner(owner); status(null) }
    fun mode(value: AudioMode) { cancel(); controller.setMode(value) }
    fun effects(state: SessionState, effects: List<SessionEffect>) {
        if (!active) return
        for (effect in effects) when (effect) {
            SessionEffect.CancelNarration -> cancel()
            is SessionEffect.Narrate -> if (state.phase == SessionPhase.ACTIVE &&
                effect.kind == NarrationKind.INSTRUCTION && effect.trigger == SpeechTrigger.REPLAY) {
                guided(state)
            } else speak(state, if (state.phase == SessionPhase.ACTIVE &&
                effect.kind == NarrationKind.INSTRUCTION && effect.text == state.task.question.instruction)
                    positionQuestion(PrepositionsContent.scene(state.task)) else effect.text, when(effect.kind) {
                NarrationKind.INSTRUCTION -> SpeechKind.INSTRUCTION
                NarrationKind.FEEDBACK -> SpeechKind.FEEDBACK
                NarrationKind.COMPLETION -> SpeechKind.COMPLETION
            }, effect.trigger)
            else -> Unit
        }
    }
    fun option(state: SessionState, id: ContentId) {
        if (state.phase == SessionPhase.ACTIVE && id in state.task.question.choices)
            sequence(state, listOf(answerStep(state, id)), SpeechTrigger.MANUAL)
    }
    private data class Step(val text: ContentText, val kind: SpeechKind, val choice: ContentId? = null)
    private fun answerStep(state: SessionState, id: ContentId): Step {
        val scene = PrepositionsContent.scene(state.task)
        return Step(ContentText.plain(answerPhrase(scene,id,ContentLanguage.ENGLISH)+".",
            answerPhrase(scene,id,ContentLanguage.GERMAN)+"."), SpeechKind.OPTION, id)
    }
    private fun guided(state: SessionState) {
        val scene = PrepositionsContent.scene(state.task)
        sequence(state, listOf(Step(positionQuestion(scene),SpeechKind.QUESTION),
            Step(positionAnswerStem(scene),SpeechKind.INSTRUCTION)) + state.task.question.choices.map { answerStep(state,it) },
            SpeechTrigger.REPLAY)
    }
    /** Each utterance owns a natural speech boundary. Only actual completion advances the sequence. */
    private fun sequence(state: SessionState, steps: List<Step>, trigger: SpeechTrigger) {
        if (!active) return
        cancel()
        val token = revision
        val context = controller.openContext(owner, SpeechSessionId(state.plan.id.value), state.language)
        fun next(index: Int) {
            if (!active || token != revision) return
            if (index == steps.size) { spokenOption(null); status(SpeechResult.Completed); return }
            val step = steps[index]
            // No start event is exposed by the controller. Highlight only dispatch to a ready
            // engine, never a muted or initialising request. A pending individual Listen may
            // therefore speak without highlighting; guided answers follow a completed question.
            spokenOption(step.choice.takeIf { controller.readiness == EngineReadiness.READY &&
                controller.mode.allows(step.kind,trigger) })
            controller.speak(SpeechRequest.fromContent(context,step.text,step.kind,trigger)) { outcome ->
                if (active && token == revision) {
                    spokenOption(null)
                    if (outcome.result == SpeechResult.Completed) next(index+1)
                    else { revision++; status(outcome.result) }
                }
            }
        }
        next(0)
    }
    fun introduction(state: SessionState, automatic: Boolean = false, result: (SpeechResult) -> Unit) {
        val tutorial = PrepositionsContent.tutorial
        val question = positionQuestion(PrepositionsContent.scene(state.task))
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
