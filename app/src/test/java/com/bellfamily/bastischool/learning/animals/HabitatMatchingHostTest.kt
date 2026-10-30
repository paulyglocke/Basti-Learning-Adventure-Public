package com.bellfamily.bastischool.learning.animals

import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.progress.*
import com.bellfamily.bastischool.learning.session.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class HabitatMatchingHostTest {
    @get:Rule val temp = TemporaryFolder()
    @Test fun wrongAnswerRetryRestoreLanguageAndAcceptedAlternativeAreDurable() {
        val journal = AtomicProgressStorage(temp.newFolder("journal"), JvmAtomicCommit)
        val progress = ProgressFixtures.repository(temp.newFolder("progress"))
        val first = HabitatMatchingContent.host(journal, progress)
        first.open(SessionId("habitat-match-one"), RoundLength.FIVE, 10L, ContentLanguage.ENGLISH)
        val state = first.state!!
        first.dispatch(SessionAction.Language(ContentLanguage.GERMAN))
        val wrong = state.task.question.choices.firstOrNull { it !in state.task.question.acceptedAnswers }
        if (wrong != null) {
            first.dispatch(SessionAction.Answer(first.state!!.nextAttempt!!, wrong))
            assertEquals(AnswerState.RETRY_AVAILABLE, first.state!!.current.answer)
            first.dispatch(SessionAction.Retry(AttemptId(first.state!!.task.id, 1)))
            assertEquals(AnswerState.UNANSWERED, first.state!!.current.answer)
            assertEquals(1, first.state!!.current.retries)
        }
        val acceptedQuestion = first.state!!.plan.tasks.single { it.question.definition.value.endsWith("rabbit_scenes") }.question
        while (!first.state!!.task.question.definition.value.endsWith("rabbit_scenes")) {
            val current = first.state!!
            first.dispatch(SessionAction.Answer(current.nextAttempt!!, current.task.question.acceptedAnswers.first()))
            first.dispatch(SessionAction.Next(first.state!!.task.id))
        }
        val restored = HabitatMatchingContent.host(journal, progress)
        val sideEffects = restored.open(SessionId("ignored"), RoundLength.TEN, 999L, ContentLanguage.ENGLISH)
        assertTrue("Restoration must be silent", sideEffects.isEmpty())
        assertEquals("rabbit_scenes", restored.state!!.task.question.definition.value.substringAfterLast('.'))
        assertEquals(ContentLanguage.GERMAN, restored.state!!.language)
        val alternate = acceptedQuestion.acceptedAnswers.first { it != acceptedQuestion.correct }
        val attempt = restored.state!!.nextAttempt!!
        val taskId = restored.state!!.task.id
        restored.dispatch(SessionAction.Answer(attempt, alternate))
        assertEquals(AnswerState.CORRECT, restored.state!!.current.answer)
        assertEquals(AttemptOutcome.CORRECT, ProgressFixtures.records(progress).map { it.event }.filterIsInstance<AttemptEvent>().last().outcome)
        restored.dispatch(SessionAction.Next(taskId))
        restored.dispatch(SessionAction.Next(taskId))
        assertEquals(1, ProgressFixtures.records(progress).map { it.event }.filterIsInstance<AttemptEvent>()
            .count { it.attempt.task.ordinal == attempt.task.ordinal && it.outcome == AttemptOutcome.CORRECT })
    }

    @Test fun sevenTaskCompletionIsUniqueAndNewSessionPreservesPriorResults() {
        val journal = AtomicProgressStorage(temp.newFolder("journal"), JvmAtomicCommit)
        val progress = ProgressFixtures.repository(temp.newFolder("progress"))
        val host = HabitatMatchingContent.host(journal, progress)
        host.open(SessionId("habitat-match-complete"), RoundLength.TEN, 18L, ContentLanguage.ENGLISH)
        assertEquals(7, host.state!!.plan.tasks.size)
        while (host.state!!.phase == SessionPhase.ACTIVE) {
            val current = host.state!!
            if (current.current.answer == AnswerState.UNANSWERED)
                host.dispatch(SessionAction.Answer(current.nextAttempt!!, current.task.question.acceptedAnswers.first()))
            host.dispatch(SessionAction.Next(host.state!!.task.id))
        }
        host.dispatch(SessionAction.Next(host.state!!.task.id))
        val before = ProgressFixtures.records(progress).map { it.event }
        assertEquals(7, before.filterIsInstance<AttemptEvent>().size)
        assertEquals(1, before.filterIsInstance<CompletionEvent>().size)
        assertEquals(7, before.filterIsInstance<CompletionEvent>().single().tasks.size)
        assertTrue(before.all { it.origin.activity == HabitatMatchingContent.activity })

        host.newRound(SessionId("habitat-match-next"), RoundLength.FIVE, 22L, ContentLanguage.GERMAN)
        assertEquals(0, host.state!!.index)
        assertEquals(1, ProgressFixtures.records(progress).map { it.event }.filterIsInstance<CompletionEvent>().size)
        val restored = HabitatMatchingContent.host(journal, progress)
        restored.open(SessionId("ignored"), RoundLength.FIVE, 0L, ContentLanguage.ENGLISH)
        assertEquals("habitat-match-next", restored.state!!.plan.id.value)
        assertEquals(1, ProgressFixtures.records(progress).map { it.event }.filterIsInstance<CompletionEvent>().size)
    }
}
