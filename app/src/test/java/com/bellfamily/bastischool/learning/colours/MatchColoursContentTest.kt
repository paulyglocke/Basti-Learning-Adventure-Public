package com.bellfamily.bastischool.learning.colours

import com.bellfamily.bastischool.learning.models.ContentLanguage
import com.bellfamily.bastischool.learning.session.*
import org.junit.Assert.*
import org.junit.Test

class MatchColoursContentTest {
    @Test fun fiveAndTenRoundsAreDeterministicBalancedAndShapeIndependent() {
        for (round in RoundLength.entries) {
            val id = SessionId("match-${round.count}")
            val a = MatchColoursContent.generate(id, round, 423L) as GenerationResult.Generated
            val b = MatchColoursContent.generate(id, round, 423L) as GenerationResult.Generated
            assertEquals(a.plan.tasks.map { it.question.choices }, b.plan.tasks.map { it.question.choices })
            assertEquals(a.plan.tasks.map { it.question.definition }, b.plan.tasks.map { it.question.definition })
            assertEquals(MatchColoursContent.activity, a.plan.activity)
            MatchColoursContent.validate(SessionReducer.start(a.plan, ContentLanguage.ENGLISH, MatchColoursContent.repository).state)
            a.plan.tasks.forEach { task ->
                val target = MatchColoursContent.targetColour(task.question)
                val targetShape = MatchColoursContent.targetShape(task.question)
                assertEquals(4, task.question.choices.map { MatchColoursContent.sample(it).colour }.toSet().size)
                assertNotEquals(targetShape, MatchColoursContent.sample(task.question.correct).shape)
                assertTrue(task.question.choices.all { MatchColoursContent.repository.find(it) != null })
                assertTrue(task.question.choices.any { MatchColoursContent.sample(it).colour == target })
            }
            assertEquals(ColoursContent.order.toSet(), a.plan.tasks.map { MatchColoursContent.targetColour(it.question) }.toSet())
        }
    }

    @Test fun helpRetryAndCorrectAnswerUseDedicatedSkillIdentity() {
        val plan = (MatchColoursContent.generate(SessionId("match-retry"), RoundLength.FIVE, 2L) as GenerationResult.Generated).plan
        var state = SessionReducer.start(plan, ContentLanguage.GERMAN, MatchColoursContent.repository).state
        val target = state.task.question.correct
        val wrong = state.task.question.choices.first { it != target }
        state = SessionReducer.reduce(state, SessionAction.Hint(state.task.id)).state
        state = SessionReducer.reduce(state, SessionAction.Answer(state.nextAttempt!!, wrong)).state
        assertEquals(AnswerState.RETRY_AVAILABLE, state.current.answer)
        assertTrue(state.current.support.hint)
        state = SessionReducer.reduce(state, SessionAction.Retry(AttemptId(state.task.id, state.current.attempts))).state
        state = SessionReducer.reduce(state, SessionAction.Answer(state.nextAttempt!!, target)).state
        assertEquals(AnswerState.CORRECT, state.current.answer)
        assertEquals("skill.colours.matching", state.task.question.skill.value)
    }
}
