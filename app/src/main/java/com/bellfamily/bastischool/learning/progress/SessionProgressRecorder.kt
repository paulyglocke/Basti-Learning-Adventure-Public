package com.bellfamily.bastischool.learning.progress

import com.bellfamily.bastischool.learning.session.*

sealed interface ProgressEffectResult {
    data object Ignored : ProgressEffectResult
    data class Handled(val write: ProgressWriteResult, val acknowledgement: SessionAction.CompletionResult? = null) : ProgressEffectResult
}

/**
 * Worker-thread effect adapter. Keep/retry failed effects with their original identity.
 * Host delivers the returned completion acknowledgement to its serialized session owner.
 * The reducer and the completed content/audio/session foundations remain unchanged.
 */
class SessionProgressRecorder(private val plan: SessionPlan, private val repository: ProgressRepository) {
    private val origin = ProgressOrigin(plan.id, plan.activity, plan.activityRevision, plan.contentVersion)

    fun record(effect: SessionEffect): ProgressEffectResult {
        if (effect !is SessionEffect.RecordAttempt && effect !is SessionEffect.Complete) return ProgressEffectResult.Ignored
        val write = try {
            repository.append(when (effect) {
                is SessionEffect.RecordAttempt -> attempt(effect.result)
                is SessionEffect.Complete -> completion(effect.request)
                else -> error("Not a progress effect")
            })
        } catch (_: IllegalArgumentException) { ProgressWriteResult.Failed(ProgressFailure.INVALID_EVENT) }
        val acknowledgement = (effect as? SessionEffect.Complete)?.request?.id?.let {
            SessionAction.CompletionResult(it, write is ProgressWriteResult.Saved)
        }
        return ProgressEffectResult.Handled(write, acknowledgement)
    }

    private fun attempt(result: AttemptResult): AttemptEvent {
        require(result.id.task.session == plan.id)
        val task = plan.tasks.getOrNull(result.id.task.ordinal - 1) ?: throw IllegalArgumentException("Unknown task")
        val q = task.question
        require(result.skill == q.skill && result.context == q.context && result.difficulty == q.difficulty)
        require(result.choice in q.choices && result.correct == (result.choice == q.correct))
        require(!result.support.hint || q.hint != null)
        require(plan.policy.wrongAnswer == WrongAnswerPolicy.RETRY || result.id.number == 1)
        return AttemptEvent(origin, evidence(task), result.id, result.language, result.choice,
            if (result.correct) AttemptOutcome.CORRECT else AttemptOutcome.INCORRECT, result.support)
    }

    private fun completion(request: CompletionRequest): CompletionEvent {
        require(request.id.session == plan.id && request.result.session == plan.id && request.result.activity == plan.activity)
        require(request.result.tasks.map { it.first.id } == plan.tasks.map { it.id })
        val results = request.result.tasks.mapIndexed { index, (task, progress) ->
            val expected = plan.tasks[index]
            require(evidence(task) == evidence(expected))
            require(progress.locked && progress.lastChoice in expected.question.choices)
            require((progress.answer == AnswerState.CORRECT) == (progress.lastChoice == expected.question.correct))
            require(!progress.support.hint || expected.question.hint != null)
            require(plan.policy.wrongAnswer == WrongAnswerPolicy.RETRY || progress.attempts == 1)
            require(progress.answer != AnswerState.INCORRECT || plan.policy.wrongAnswer == WrongAnswerPolicy.LOCK)
            CompletedTask(evidence(expected), progress.lastChoice!!,
                if (progress.answer == AnswerState.CORRECT) AttemptOutcome.CORRECT else AttemptOutcome.INCORRECT,
                progress.attempts, progress.retries, progress.support)
        }
        require(request.result.score == results.count { it.outcome == AttemptOutcome.CORRECT })
        return CompletionEvent(origin, results)
    }

    private fun evidence(task: ChoiceTask) = TaskEvidence(task.id, task.question.definition,
        task.question.skill, task.question.context, task.question.difficulty)
}
