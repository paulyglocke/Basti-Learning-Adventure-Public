package com.bellfamily.bastischool.learning.session

import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.audio.SpeechTrigger
import org.junit.Assert.*
import org.junit.Test
import com.bellfamily.bastischool.learning.session.SessionFixtures as F

class SessionReducerTest {
    @Test fun startKeepsFrozenTaskIdentityAndOrderAcrossReadsAndSupportActions() {
        val state = F.start()
        val task = state.task
        val choices = task.question.choices.toList()
        var next = SessionReducer.reduce(state, SessionAction.Replay(task.id)).state
        next = SessionReducer.reduce(next, SessionAction.Hint(task.id)).state
        assertSame(task, next.task)
        assertEquals(choices, next.task.question.choices)
        assertEquals(0, next.score)
        assertEquals(AnswerState.UNANSWERED, next.current.answer)
    }
    @Test fun correctAnswerEmitsOneAttemptAndScoresOnlyOnce() {
        val state = F.start()
        val action = SessionAction.Answer(state.nextAttempt!!, state.task.question.correct)
        val answered = SessionReducer.reduce(state, action)
        assertEquals(1, answered.state.score)
        assertEquals(1, answered.state.current.attempts)
        assertEquals(AnswerState.CORRECT, answered.state.current.answer)
        val attempt = answered.effects.filterIsInstance<SessionEffect.RecordAttempt>().single().result
        assertEquals(action.attempt, attempt.id)
        assertTrue(attempt.correct && attempt.support.independent)
        repeat(5) {
            val duplicate = SessionReducer.reduce(answered.state, action)
            assertSame(answered.state, duplicate.state)
            assertTrue(duplicate.effects.isEmpty())
        }
    }
    @Test fun wrongAnswerWaitsForExplicitRetryAndRetainsTask() {
        val initial = F.start()
        val wrong = F.answer(initial, false).state
        assertEquals(AnswerState.RETRY_AVAILABLE, wrong.current.answer)
        assertEquals(0, wrong.score)
        assertSame(wrong, F.answer(wrong).state)
        assertSame(wrong, F.next(wrong).state)
        val retried = F.retry(wrong).state
        assertSame(initial.task, retried.task)
        assertEquals(1, retried.current.attempts)
        assertEquals(1, retried.current.retries)
        val correct = F.answer(retried).state
        assertEquals(2, correct.current.attempts)
        assertEquals(1, correct.score)
    }
    @Test fun staleAnswerAndRetryActionsCannotCountAnotherAttempt() {
        val initial = F.start()
        val action = SessionAction.Answer(initial.nextAttempt!!, initial.task.question.choices.first { it != initial.task.question.correct })
        val wrong = SessionReducer.reduce(initial, action).state
        val retry = SessionAction.Retry(action.attempt)
        val ready = SessionReducer.reduce(wrong, retry).state
        assertSame(ready, SessionReducer.reduce(ready, action).state)
        val wrongAgain = F.answer(ready, false).state
        assertSame(wrongAgain, SessionReducer.reduce(wrongAgain, retry).state)
        assertEquals(2, wrongAgain.current.attempts)
    }
    @Test fun lockPolicyFinishesWrongTaskWithoutNegativeScoreOrRetry() {
        val wrong = F.answer(F.start(policy = WrongAnswerPolicy.LOCK), false).state
        assertEquals(AnswerState.INCORRECT, wrong.current.answer)
        assertEquals(0, wrong.score)
        assertSame(wrong, F.retry(wrong).state)
        assertSame(wrong, F.answer(wrong).state)
        assertEquals(1, F.next(wrong).state.index)
    }
    @Test fun hintsReplayAndParentHelpAreSupportNotAnswerAttempts() {
        var state = F.start()
        state = SessionReducer.reduce(state, SessionAction.Hint(state.task.id)).state
        repeat(2) { state = SessionReducer.reduce(state, SessionAction.Replay(state.task.id)).state }
        state = SessionReducer.reduce(state, SessionAction.ParentHelp(state.task.id)).state
        assertEquals(SupportUse(2, hint = true, parentHelp = true), state.current.support)
        assertEquals(0, state.current.attempts)
        val result = F.answer(state).effects.filterIsInstance<SessionEffect.RecordAttempt>().single().result
        assertEquals(state.current.support, result.support)
        assertFalse(result.support.independent)
    }
    @Test fun replayAfterCorrectAnswerDoesNotRewriteIndependenceOrScore() {
        val answered = F.answer(F.start()).state
        val replay = SessionReducer.reduce(answered, SessionAction.Replay(answered.task.id))
        assertSame(answered, replay.state)
        assertTrue(replay.state.current.support.independent)
        assertEquals(SpeechTrigger.REPLAY, replay.effects.filterIsInstance<SessionEffect.Narrate>().single().trigger)
    }
    @Test fun languageSwitchPreservesAnsweredIdentityOrderAttemptsAndScore() {
        val answered = F.answer(F.start()).state
        val translated = SessionReducer.reduce(answered, SessionAction.Language(ContentLanguage.GERMAN))
        assertSame(answered.task, translated.state.task)
        assertSame(answered.plan, translated.state.plan)
        assertEquals(answered.progress, translated.state.progress)
        assertEquals(1, translated.state.score)
        assertEquals(listOf(SessionEffect.CancelNarration), translated.effects)
    }
    @Test fun nextIsGuardedAndStartsPristineTaskExactlyOnce() {
        val initial = F.start()
        assertSame(initial, F.next(initial).state)
        val answered = F.answer(initial).state
        val action = SessionAction.Next(answered.task.id)
        val next = SessionReducer.reduce(answered, action).state
        assertEquals(1, next.index)
        assertEquals(1, next.score)
        assertEquals(TaskProgress(), next.current)
        assertSame(next, SessionReducer.reduce(next, action).state)
        assertSame(next, SessionReducer.reduce(next, SessionAction.Hint(initial.task.id)).state)
    }
    @Test fun fiveQuestionsCompleteExactlyOnce() = assertCompletion(RoundLength.FIVE)
    @Test fun tenQuestionsCompleteExactlyOnce() = assertCompletion(RoundLength.TEN)
    private fun assertCompletion(round: RoundLength) {
        val transition = F.completed(round)
        val state = transition.state
        assertEquals(round.count, state.score)
        assertEquals(SessionPhase.COMPLETED, state.phase)
        val request = transition.effects.filterIsInstance<SessionEffect.Complete>().single().request
        assertEquals(state.plan.id, request.id.session)
        assertEquals(round.count, request.result.tasks.size)
        repeat(3) { assertTrue(F.next(state).effects.isEmpty()) }
        assertTrue(F.answer(state).effects.isEmpty())
        val replay = SessionReducer.reduce(state, SessionAction.Replay(state.task.id))
        assertTrue(replay.effects.none { it is SessionEffect.Complete })
        assertEquals(F.summary, replay.effects.filterIsInstance<SessionEffect.Narrate>().single().text)
    }
    @Test fun completionAcknowledgementAndExplicitRetryUseStableLogicalSessionId() {
        val complete = F.completed().state
        val first = complete.completionRequestId!!
        val failed = SessionReducer.reduce(complete, SessionAction.CompletionResult(first, false)).state
        assertEquals(CompletionState.FAILED, failed.completion)
        val retryAction = SessionAction.RetryCompletion(first)
        val retry = SessionReducer.reduce(failed, retryAction)
        val second = retry.state.completionRequestId!!
        assertEquals(first.session, second.session)
        assertEquals(first.delivery + 1, second.delivery)
        assertEquals(1, retry.effects.filterIsInstance<SessionEffect.Complete>().size)
        assertTrue(SessionReducer.reduce(retry.state, retryAction).effects.isEmpty())
        assertSame(retry.state, SessionReducer.reduce(retry.state, SessionAction.CompletionResult(first, true)).state)
        val acknowledged = SessionReducer.reduce(retry.state, SessionAction.CompletionResult(second, true)).state
        assertEquals(CompletionState.ACKNOWLEDGED, acknowledged.completion)
        assertTrue(SessionReducer.reduce(acknowledged, SessionAction.RetryCompletion(second)).effects.isEmpty())
    }
    @Test fun activeRoundIsUnaffectedByFutureSettingsAndNewPlans() {
        val active = F.start()
        val future = F.plan(RoundLength.TEN, WrongAnswerPolicy.LOCK)
        assertEquals(RoundLength.TEN, future.policy.round)
        assertEquals(RoundLength.FIVE, active.plan.policy.round)
        assertEquals(WrongAnswerPolicy.RETRY, active.plan.policy.wrongAnswer)
        assertEquals(5, active.plan.tasks.size)
    }
    @Test fun unrelatedChoicesSessionsAndPrematureCompletionAreIgnored() {
        val state = F.start()
        assertSame(state, SessionReducer.reduce(state, SessionAction.Answer(state.nextAttempt!!, ContentId("day.absent"))).state)
        val other = AttemptId(TaskInstanceId(SessionId("other-session"), 1), 1)
        assertSame(state, SessionReducer.reduce(state, SessionAction.Answer(other, state.task.question.correct)).state)
        assertSame(state, SessionReducer.reduce(state, SessionAction.CompletionResult(CompletionRequestId(state.plan.id, 1), true)).state)
    }
}
