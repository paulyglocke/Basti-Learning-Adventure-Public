package com.bellfamily.bastischool.learning.seasons

import com.bellfamily.bastischool.learning.content.SeasonIds
import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.session.*
import com.bellfamily.bastischool.learning.progress.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class SeasonsMissingTest {
    @get:Rule val temp = TemporaryFolder()

    @Test fun everyCanonicalPositionHasOneGapAndBilingualPrompt() {
        for (id in SeasonIds.canonicalOrder) {
            val q = SeasonsMissing.question(id, SeasonIds.canonicalOrder)
            assertEquals(id, q.correct)
            assertEquals(4, q.choices.distinct().size)
            assertEquals("Which season is missing?", q.instruction.display.en)
            assertEquals("Welche Jahreszeit fehlt?", q.instruction.display.de)
            assertEquals(1, SeasonIds.canonicalOrder.count { it == q.correct })
            assertEquals("skill.seasons.missing", q.skill.value)
        }
        assertTrue(SeasonsPhase.MISSING.isQuiz)
    }

    @Test fun seededFiveAndTenRoundsRestoreWithoutRegeneration() {
        for (round in RoundLength.entries) {
            fun plan() = (SeasonsMissing.generate(SessionId("missing"), round, 42) as GenerationResult.Generated).plan
            val a = SessionReducer.start(plan(), ContentLanguage.ENGLISH, SeasonsContent.repository).state
            val b = SessionReducer.start(plan(), ContentLanguage.ENGLISH, SeasonsContent.repository).state
            assertEquals(round.count, a.plan.tasks.size)
            assertArrayEquals(SessionCheckpoint.encode(a), SessionCheckpoint.encode(b))
            assertEquals(SeasonIds.canonicalOrder.toSet(), a.plan.tasks.take(4).map { it.question.correct }.toSet())
            SeasonsMissing.validate(a)
            val restored = SessionCheckpoint.restore(SessionCheckpoint.encode(a), SeasonsMissing.activity,
                SeasonsMissing.REVISION, SeasonsContent.repository) as SessionRestoreResult.Restored
            SeasonsMissing.validate(restored.state)
            assertArrayEquals(SessionCheckpoint.encode(a), SessionCheckpoint.encode(restored.state))
        }
    }

    @Test fun durableRetryAndHelpRestoreExactQuestionAndChoiceOrder() {
        val disk = AtomicProgressStorage(temp.newFolder(), JvmAtomicCommit)
        val dir = temp.newFolder()
        fun host() = SeasonsMissing.host(disk, ProgressFixtures.repository(dir))
        val h = host()
        h.open(SessionId("missing-retry"), RoundLength.FIVE, 42, ContentLanguage.GERMAN)
        val s = h.state!!
        h.dispatch(SessionAction.Hint(s.task.id))
        h.dispatch(SessionAction.Answer(s.nextAttempt!!, s.task.question.choices.first { it != s.task.question.correct }))
        assertEquals(AnswerState.RETRY_AVAILABLE, h.state!!.current.answer)
        val saved = SessionCheckpoint.encode(h.state!!)
        val restored = host()
        assertTrue(restored.open(SessionId("unused"), RoundLength.TEN, 99, ContentLanguage.ENGLISH).isEmpty())
        assertArrayEquals(saved, SessionCheckpoint.encode(restored.state!!))
        restored.dispatch(SessionAction.Retry(AttemptId(s.task.id, 1)))
        assertTrue(restored.state!!.current.support.hint)
        assertEquals(s.task.question.choices, restored.state!!.task.question.choices)
        restored.dispatch(SessionAction.Answer(restored.state!!.nextAttempt!!, s.task.question.correct))
        assertEquals(AnswerState.CORRECT, restored.state!!.current.answer)
    }
}
