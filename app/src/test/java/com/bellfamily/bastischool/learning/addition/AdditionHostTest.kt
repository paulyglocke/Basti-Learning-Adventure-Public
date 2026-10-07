package com.bellfamily.bastischool.learning.addition

import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.progress.*
import com.bellfamily.bastischool.learning.session.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class AdditionHostTest {
    @get:Rule val temp = TemporaryFolder()

    @Test fun partialAndCompletedRestoreRetainAdditionEvidence() {
        for (round in RoundLength.entries) {
            val disk = AtomicProgressStorage(temp.newFolder(), JvmAtomicCommit)
            val progress = ProgressFixtures.repository(temp.newFolder())
            fun open() = AdditionContent.host(disk, progress).also {
                it.open(SessionId("match"), round, 7, ContentLanguage.ENGLISH)
            }
            var host = open()
            host.dispatch(SessionAction.Hint(host.state!!.task.id))
            host.dispatch(SessionAction.Replay(host.state!!.task.id))
            val initial = host.state!!
            host.dispatch(SessionAction.Answer(initial.nextAttempt!!, initial.task.question.choices.first { it != initial.task.question.correct }))
            host.dispatch(SessionAction.Language(ContentLanguage.GERMAN))
            val partial = SessionCheckpoint.encode(host.state!!)
            val restored = AdditionContent.host(disk, progress)
            assertTrue(restored.open(SessionId("unused"), RoundLength.TEN, 99, ContentLanguage.ENGLISH).isEmpty())
            assertArrayEquals(partial, SessionCheckpoint.encode(restored.state!!))
            host = restored
            host.dispatch(SessionAction.Retry(AttemptId(host.state!!.task.id, 1)))
            assertEquals(1, host.state!!.current.attempts)
            assertTrue(host.state!!.current.support.hint)
            assertEquals(1, host.state!!.current.support.replays)
            repeat(round.count) {
                val s = host.state!!
                val action = SessionAction.Answer(s.nextAttempt!!, s.task.question.correct)
                host.dispatch(action); host.dispatch(action)
                val answered = SessionCheckpoint.encode(host.state!!)
                host = open()
                assertArrayEquals(answered, SessionCheckpoint.encode(host.state!!))
                host.dispatch(SessionAction.Next(host.state!!.task.id))
            }
            val completed = SessionCheckpoint.encode(host.state!!)
            host = open(); host.retryWrites()
            assertArrayEquals(completed, SessionCheckpoint.encode(host.state!!))
            assertEquals(CompletionState.ACKNOWLEDGED, host.state!!.completion)
            val events = ProgressFixtures.records(progress).map { it.event }
            val attempts = events.filterIsInstance<AttemptEvent>()
            assertEquals(round.count + 1, attempts.size)
            assertEquals(setOf("skill.math.addition.within_5"), attempts.map { it.evidence.skill.value }.toSet())
            assertEquals(AttemptOutcome.INCORRECT, attempts.first().outcome)
            assertTrue(attempts.first().support.hint)
            assertTrue(attempts.drop(1).all { it.language == ContentLanguage.GERMAN })
            assertTrue(events.all { it.origin.activity == AdditionContent.activity })
            assertEquals(round.count, events.filterIsInstance<CompletionEvent>().single().tasks.size)
            host.newRound(SessionId("fresh"), round, 8, ContentLanguage.ENGLISH)
            assertEquals(SessionPhase.ACTIVE, host.state!!.phase)
            assertEquals(0, host.state!!.current.attempts)
        }
    }

    @Test fun uncertainAttemptAndCompletionDeliveryDeduplicatesAfterRestore() {
        val disk = AtomicProgressStorage(temp.newFolder(), JvmAtomicCommit)
        val real = ProgressFixtures.repository(temp.newFolder())
        var fail = false
        val uncertain = object : ProgressRepository {
            override fun append(event: ProgressEvent): ProgressWriteResult {
                val result = real.append(event)
                return if (fail) ProgressWriteResult.Failed(ProgressFailure.IO) else result
            }
            override fun read(query: ProgressQuery) = real.read(query)
        }
        fun open() = AdditionContent.host(disk, uncertain).also {
            it.open(SessionId("match"), RoundLength.FIVE, 1, ContentLanguage.ENGLISH)
        }
        var host = open()
        repeat(5) { index ->
            val s = host.state!!
            fail = index == 0
            host.dispatch(SessionAction.Answer(s.nextAttempt!!, s.task.question.correct))
            if (fail) {
                assertTrue(host.hasPending)
                fail = false; host = open(); host.retryWrites()
            }
            fail = index == 4
            host.dispatch(SessionAction.Next(host.state!!.task.id))
        }
        assertTrue(host.hasPending)
        fail = false; host = open(); host.retryWrites(); open()
        val events = ProgressFixtures.records(real).map { it.event }
        assertEquals(5, events.filterIsInstance<AttemptEvent>().size)
        assertEquals(1, events.filterIsInstance<CompletionEvent>().size)
    }
}
