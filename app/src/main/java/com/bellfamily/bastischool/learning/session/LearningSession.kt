package com.bellfamily.bastischool.learning.session

import com.bellfamily.bastischool.audio.*
import com.bellfamily.bastischool.learning.content.ContentRepository
import com.bellfamily.bastischool.learning.models.ContentLanguage

/**
 * Serialized state holder/effect boundary for a future screen owner. No Android/UI dependency.
 * The host handles returned RecordAttempt/Complete effects, with persistent session/event deduplication.
 * The host owns controller disposal; this session cancels only its speech owner.
 */
class LearningSession private constructor(initial: SessionState, private val audio: AudioController,
                                         private val owner: SpeechOwner) {
    var state: SessionState = initial
        private set
    var lastSpeech: SpeechOutcome? = null
        private set
    private var generation = 0L
    private var suspended = false
    private var closed = false
    private var speechContext: SpeechContext? = null

    init { silence() }

    fun dispatch(action: SessionAction): List<SessionEffect> {
        if (closed || suspended) return emptyList()
        val transition = SessionReducer.reduce(state, action)
        if (transition.state !== state || transition.effects.isNotEmpty()) {
            state = transition.state
            if (transition.effects.any { it is SessionEffect.Narrate || it == SessionEffect.CancelNarration })
                applyAudio(transition.effects)
        }
        return transition.effects.filter { it is SessionEffect.RecordAttempt || it is SessionEffect.Complete }
    }

    /** Call on navigation away/background; returning is deliberately silent. */
    fun suspend() { suspended = true; silence() }
    fun resume() { if (!closed) { suspended = false; silence() } }
    fun close() { closed = true; silence() }

    private fun silence() {
        generation++
        speechContext = null
        lastSpeech = null
        audio.cancelOwner(owner)
    }

    private fun applyAudio(effects: List<SessionEffect>) {
        silence()
        val snapshot = state
        val requestGeneration = generation
        for (effect in effects) if (effect is SessionEffect.Narrate) {
            val context = speechContext ?: audio.openContext(owner, SpeechSessionId(state.plan.id.value), state.language)
                .also { speechContext = it }
            val kind = when (effect.kind) {
                NarrationKind.INSTRUCTION -> SpeechKind.INSTRUCTION
                NarrationKind.FEEDBACK -> SpeechKind.FEEDBACK
                NarrationKind.COMPLETION -> SpeechKind.COMPLETION
            }
            audio.speak(SpeechRequest.fromContent(context, effect.text, kind,
                effect.trigger)) { outcome ->
                if (!closed && !suspended && generation == requestGeneration && state === snapshot) lastSpeech = outcome
                // Speech success/failure never answers, advances, scores or completes a learning task.
            }
        }
    }

    companion object {
        fun start(plan: SessionPlan, language: ContentLanguage, content: ContentRepository,
                  audio: AudioController, owner: SpeechOwner): LearningSession {
            val transition = SessionReducer.start(plan, language, content)
            return LearningSession(transition.state, audio, owner).also { it.applyAudio(transition.effects) }
        }
        /** Only a validated restore result is accepted; no old speech/completion effects are drained. */
        fun restore(restored: SessionRestoreResult.Restored, audio: AudioController, owner: SpeechOwner) =
            LearningSession(restored.state, audio, owner)
    }
}
