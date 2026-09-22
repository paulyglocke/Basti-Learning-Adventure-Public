package com.bellfamily.bastischool.learning.session

import com.bellfamily.bastischool.audio.SpeechTrigger
import com.bellfamily.bastischool.learning.content.ContentRepository
import com.bellfamily.bastischool.learning.models.*

sealed interface SessionAction {
    data class Answer(val attempt: AttemptId, val choice: ContentId) : SessionAction
    data class Retry(val attempt: AttemptId) : SessionAction
    data class Hint(val task: TaskInstanceId) : SessionAction
    data class ParentHelp(val task: TaskInstanceId) : SessionAction
    data class Replay(val task: TaskInstanceId) : SessionAction
    data class Next(val task: TaskInstanceId) : SessionAction
    data class Language(val language: ContentLanguage) : SessionAction
    data class CompletionResult(val id: CompletionRequestId, val success: Boolean) : SessionAction
    data class RetryCompletion(val previous: CompletionRequestId) : SessionAction
}
enum class NarrationKind { INSTRUCTION, FEEDBACK, COMPLETION }
sealed interface SessionEffect {
    data class Narrate(val text: ContentText, val kind: NarrationKind, val trigger: SpeechTrigger = SpeechTrigger.AUTOMATIC) : SessionEffect
    data object CancelNarration : SessionEffect
    data class RecordAttempt(val result: AttemptResult) : SessionEffect
    data class Complete(val request: CompletionRequest) : SessionEffect
}
class SessionTransition(val state: SessionState, effects: List<SessionEffect> = emptyList()) {
    val effects = frozen(effects)
}

/** Pure explicit transitions: no engine, navigation, time, randomness or persistence calls. */
object SessionReducer {
    fun start(plan: SessionPlan, language: ContentLanguage, content: ContentRepository): SessionTransition {
        plan.validate(content)
        val state = SessionState(plan, language, 0, List(plan.tasks.size) { TaskProgress() }, SessionPhase.ACTIVE)
        return SessionTransition(state, listOf(instruction(state)))
    }

    fun reduce(state: SessionState, action: SessionAction): SessionTransition {
        val task = state.task
        val current = state.current
        fun unchanged() = SessionTransition(state)
        fun update(progress: TaskProgress, effects: List<SessionEffect> = emptyList()): SessionTransition {
            val all = state.progress.toMutableList().apply { set(state.index, progress) }
            return SessionTransition(state.changed(progress = all), effects)
        }
        when (action) {
            is SessionAction.Language -> return if (state.language == action.language) unchanged()
                else SessionTransition(state.changed(language = action.language), listOf(SessionEffect.CancelNarration))
            is SessionAction.CompletionResult -> return if (state.completion == CompletionState.PENDING &&
                state.completionRequestId == action.id) SessionTransition(state.changed(completion =
                if (action.success) CompletionState.ACKNOWLEDGED else CompletionState.FAILED)) else unchanged()
            is SessionAction.RetryCompletion -> {
                if (action.previous != state.completionRequestId || state.phase != SessionPhase.COMPLETED || state.completion == CompletionState.ACKNOWLEDGED ||
                    state.completionDelivery == Int.MAX_VALUE) return unchanged()
                val next = state.changed(completion = CompletionState.PENDING, delivery = state.completionDelivery + 1)
                return SessionTransition(next, listOf(completion(next)))
            }
            is SessionAction.Replay -> {
                if (action.task != task.id) return unchanged()
                val effect = if (state.phase == SessionPhase.COMPLETED)
                    SessionEffect.Narrate(state.plan.completionText, NarrationKind.COMPLETION, trigger = SpeechTrigger.REPLAY)
                else instruction(state, trigger = SpeechTrigger.REPLAY)
                // Replay after a locked answer cannot retrospectively change independence evidence.
                return if (state.phase == SessionPhase.ACTIVE && !current.locked && current.support.replays < Int.MAX_VALUE)
                    update(current.copy(support = current.support.copy(replays = current.support.replays + 1)), listOf(effect))
                else SessionTransition(state, listOf(effect))
            }
            else -> Unit
        }
        if (state.phase != SessionPhase.ACTIVE) return unchanged()
        return when (action) {
            is SessionAction.Answer -> {
                if (current.answer != AnswerState.UNANSWERED || action.attempt != state.nextAttempt ||
                    action.choice !in task.question.choices) return unchanged()
                val correct = action.choice == task.question.correct
                val answer = if (correct) AnswerState.CORRECT else if (state.plan.policy.wrongAnswer == WrongAnswerPolicy.RETRY)
                    AnswerState.RETRY_AVAILABLE else AnswerState.INCORRECT
                val result = AttemptResult(action.attempt, task.question.skill, task.question.context,
                    task.question.difficulty, state.language, action.choice, correct, current.support)
                update(current.copy(answer = answer, attempts = current.attempts + 1, lastChoice = action.choice),
                    listOf(SessionEffect.RecordAttempt(result), SessionEffect.Narrate(
                        if (correct) task.question.correctFeedback else task.question.wrongFeedback, NarrationKind.FEEDBACK)))
            }
            is SessionAction.Retry -> if (action.attempt.task == task.id && action.attempt.number == current.attempts &&
                current.answer == AnswerState.RETRY_AVAILABLE)
                update(current.copy(answer = AnswerState.UNANSWERED, retries = current.retries + 1), listOf(instruction(state)))
                else unchanged()
            is SessionAction.Hint -> if (action.task == task.id && state.hintAvailable)
                update(current.copy(support = current.support.copy(hint = true)),
                    listOf(SessionEffect.Narrate(task.question.hint!!, NarrationKind.INSTRUCTION, trigger = SpeechTrigger.MANUAL)))
                else unchanged()
            is SessionAction.ParentHelp -> if (action.task == task.id && !current.locked)
                update(current.copy(support = current.support.copy(parentHelp = true))) else unchanged()
            is SessionAction.Next -> {
                if (action.task != task.id || !current.locked) return unchanged()
                if (state.index < state.plan.tasks.lastIndex) {
                    val next = state.changed(index = state.index + 1)
                    SessionTransition(next, listOf(instruction(next)))
                } else {
                    val next = state.changed(phase = SessionPhase.COMPLETED, completion = CompletionState.PENDING, delivery = 1)
                    SessionTransition(next, listOf(completion(next),
                        SessionEffect.Narrate(state.plan.completionText, NarrationKind.COMPLETION)))
                }
            }
            else -> unchanged()
        }
    }

    private fun instruction(state: SessionState, trigger: SpeechTrigger = SpeechTrigger.AUTOMATIC) =
        SessionEffect.Narrate(state.task.question.instruction, NarrationKind.INSTRUCTION, trigger)
    private fun completion(state: SessionState) = SessionEffect.Complete(CompletionRequest(
        state.completionRequestId!!, SessionResult(state.plan.id, state.plan.activity, state.score,
            state.plan.tasks.zip(state.progress))))
}
