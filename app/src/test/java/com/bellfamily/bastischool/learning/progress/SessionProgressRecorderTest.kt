package com.bellfamily.bastischool.learning.progress

import com.bellfamily.bastischool.learning.models.ContentLanguage
import com.bellfamily.bastischool.learning.session.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.io.IOException
import com.bellfamily.bastischool.learning.progress.ProgressFixtures as F
import com.bellfamily.bastischool.learning.session.SessionFixtures as S

class SessionProgressRecorderTest {
    @get:Rule val temporary = TemporaryFolder()
    private fun attempt(state: SessionState, correct: Boolean = true) = S.answer(state, correct)
        .effects.filterIsInstance<SessionEffect.RecordAttempt>().single()

    @Test fun sessionAttemptEffectsMapWithoutUiPersistenceFieldsAndDeduplicateOnRetry() {
        val state = S.start(language = ContentLanguage.GERMAN)
        val repo = F.repository(temporary.newFolder())
        val recorder = SessionProgressRecorder(state.plan, repo)
        val effect = attempt(state)
        repeat(2) { assertTrue((recorder.record(effect) as ProgressEffectResult.Handled).write is ProgressWriteResult.Saved) }
        val event = F.records(repo).single().event as AttemptEvent
        assertEquals(effect.result.id, event.attempt)
        assertEquals(state.plan.id, event.origin.session)
        assertEquals(state.task.question.definition, event.evidence.definition)
        assertEquals(ContentLanguage.GERMAN, event.language)
        assertEquals(state.plan.contentVersion, event.origin.contentVersion)
    }
    @Test fun wrongThenSupportedCorrectPreservesTwoDistinctOutcomesAndAttempts() {
        var state = S.start()
        val repo = F.repository(temporary.newFolder())
        val recorder = SessionProgressRecorder(state.plan, repo)
        val wrong = S.answer(state, false)
        recorder.record(wrong.effects.filterIsInstance<SessionEffect.RecordAttempt>().single())
        state = S.retry(wrong.state).state
        state = SessionReducer.reduce(state, SessionAction.Hint(state.task.id)).state
        state = SessionReducer.reduce(state, SessionAction.Replay(state.task.id)).state
        state = SessionReducer.reduce(state, SessionAction.ParentHelp(state.task.id)).state
        recorder.record(attempt(state))
        val events = F.records(repo).map { it.event as AttemptEvent }
        assertEquals(listOf(AttemptOutcome.INCORRECT, AttemptOutcome.CORRECT), events.map { it.outcome })
        assertEquals(listOf(1, 2), events.map { it.attempt.number })
        assertEquals(SupportUse(1, hint = true, parentHelp = true), events.last().support)
        assertFalse(events.first().id == events.last().id)
    }
    @Test fun completedCheckpointRetryAfterRepositoryRecreationStoresOneCompletion() {
        val transition = S.completed()
        val effect = transition.effects.filterIsInstance<SessionEffect.Complete>().single()
        val dir = temporary.newFolder()
        val first = SessionProgressRecorder(transition.state.plan, F.repository(dir)).record(effect) as ProgressEffectResult.Handled
        assertTrue((first.write as ProgressWriteResult.Saved).inserted)
        val restored = S.restore(transition.state) // Original pending checkpoint, before acknowledgement arrived.
        val retry = SessionReducer.reduce(restored, SessionAction.RetryCompletion(restored.completionRequestId!!))
        val second = SessionProgressRecorder(retry.state.plan, F.repository(dir)).record(
            retry.effects.filterIsInstance<SessionEffect.Complete>().single()) as ProgressEffectResult.Handled
        assertFalse((second.write as ProgressWriteResult.Saved).inserted)
        assertEquals(retry.state.completionRequestId, second.acknowledgement!!.id)
        assertTrue(second.acknowledgement.success)
        val acknowledged = SessionReducer.reduce(retry.state, second.acknowledgement).state
        assertEquals(CompletionState.ACKNOWLEDGED, acknowledged.completion)
        assertEquals(1, F.records(F.repository(dir)).size)
        assertTrue(SessionReducer.reduce(S.restore(acknowledged), SessionAction.Next(acknowledged.task.id)).effects.isEmpty())
    }
    @Test fun fiveAndTenTaskCompletionStoreTaskEvidenceNotFakeAttemptHistory() {
        for (round in RoundLength.entries) {
            val completed = S.completed(round)
            val repo = F.repository(temporary.newFolder())
            val result = SessionProgressRecorder(completed.state.plan, repo).record(
                completed.effects.filterIsInstance<SessionEffect.Complete>().single()) as ProgressEffectResult.Handled
            assertTrue(result.acknowledgement!!.success)
            val event = F.records(repo).single().event as CompletionEvent
            assertEquals(round.count, event.tasks.size)
            assertTrue(event.tasks.all { it.outcome == AttemptOutcome.CORRECT && it.attempts == 1 })
            assertEquals(1, F.records(repo, ProgressQuery(skill = event.tasks.first().evidence.skill)).size)
            assertTrue(F.records(repo, ProgressQuery(kind = ProgressEventKind.ATTEMPT)).isEmpty())
            assertThrows(UnsupportedOperationException::class.java) { (event.tasks as MutableList).clear() }
        }
    }
    @Test fun failedCompletionCanRetrySameLogicalKeyAndOnlyAcknowledgeAfterSuccess() {
        val completed = S.completed()
        val dir = temporary.newFolder()
        var fail = true
        val commit = object : AtomicProgressCommit by JvmAtomicCommit {
            override fun replace(source: File, target: File) {
                if (fail) throw IOException("simulated failure")
                JvmAtomicCommit.replace(source, target)
            }
        }
        val repo = F.repository(dir, commit = commit)
        val recorder = SessionProgressRecorder(completed.state.plan, repo)
        val result = recorder.record(completed.effects.filterIsInstance<SessionEffect.Complete>().single()) as ProgressEffectResult.Handled
        assertEquals(ProgressWriteResult.Failed(ProgressFailure.IO), result.write)
        assertFalse(result.acknowledgement!!.success)
        assertTrue(F.records(repo).isEmpty())
        val failed = SessionReducer.reduce(completed.state, result.acknowledgement).state
        val retry = SessionReducer.reduce(failed, SessionAction.RetryCompletion(failed.completionRequestId!!))
        fail = false
        val saved = recorder.record(retry.effects.filterIsInstance<SessionEffect.Complete>().single()) as ProgressEffectResult.Handled
        assertTrue(saved.acknowledgement!!.success)
        assertEquals(ProgressEventId.completion(completed.state.plan.id), (saved.write as ProgressWriteResult.Saved).record.event.id)
        assertEquals(1, F.records(repo).size)
    }
    @Test fun retryAfterLanguageChangeDoesNotRelabelPreviouslyRecordedAttemptLanguage() {
        val initial = S.start()
        val dir = temporary.newFolder()
        val recorder = SessionProgressRecorder(initial.plan, F.repository(dir))
        val effect = attempt(initial)
        recorder.record(effect)
        val translated = SessionReducer.reduce(S.answer(initial).state, SessionAction.Language(ContentLanguage.GERMAN)).state
        val restored = S.restore(translated)
        SessionProgressRecorder(restored.plan, F.repository(dir)).record(effect)
        assertEquals(ContentLanguage.ENGLISH, (F.records(F.repository(dir)).single().event as AttemptEvent).language)
    }
    @Test fun invalidEffectsFailExplicitlyWithoutWritingAndNarrationIsIgnored() {
        val state = S.start()
        val dir = File(temporary.root, "progress")
        val repo = F.repository(dir)
        val recorder = SessionProgressRecorder(state.plan, repo)
        assertEquals(ProgressEffectResult.Ignored, recorder.record(SessionEffect.CancelNarration))
        assertFalse(dir.exists())
        val valid = attempt(state)
        val invalid = valid.copy(result = valid.result.copy(skill = SkillId("skill.unrelated")))
        assertEquals(ProgressWriteResult.Failed(ProgressFailure.INVALID_EVENT), (recorder.record(invalid) as ProgressEffectResult.Handled).write)
        assertTrue(F.records(repo).isEmpty())
    }
    @Test fun completionPreservesWrongResultsAndSupportInsteadOfTreatingCompletionAsCorrect() {
        var state = S.start(policy = WrongAnswerPolicy.LOCK)
        state = SessionReducer.reduce(state, SessionAction.Hint(state.task.id)).state
        var final = SessionTransition(state)
        repeat(5) { final = S.next(S.answer(final.state, correct = it != 0).state) }
        val repo = F.repository(temporary.newFolder())
        SessionProgressRecorder(final.state.plan, repo).record(final.effects.filterIsInstance<SessionEffect.Complete>().single())
        val stored = F.records(repo).single().event as CompletionEvent
        assertEquals(AttemptOutcome.INCORRECT, stored.tasks.first().outcome)
        assertTrue(stored.tasks.first().support.hint)
        assertEquals(4, stored.tasks.count { it.outcome == AttemptOutcome.CORRECT })
    }
}
