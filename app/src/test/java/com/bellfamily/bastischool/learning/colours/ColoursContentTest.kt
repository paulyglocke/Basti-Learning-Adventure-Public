package com.bellfamily.bastischool.learning.colours

import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.session.*
import org.junit.Assert.*
import org.junit.Test

class ColoursContentTest {
    @Test fun fourCanonicalSharedDefinitionsHaveCorrectBilingualNames() {
        assertEquals(listOf("colour.red", "colour.blue", "colour.yellow", "colour.green"),
            ColoursContent.order.map { it.value })
        assertEquals(listOf("red/rot", "blue/blau", "yellow/gelb", "green/grün"),
            ColoursContent.order.map { id ->
                val text = ColoursContent.definition(id).text.display
                "${text.en}/${text.de}"
            })
        assertEquals("activity.colours.find_colour", ColoursContent.activity.value)
        assertEquals("activity.colours.sort", com.bellfamily.bastischool.learning.coloursort.ColourSort.activity.value)
    }

    @Test fun questionMapsEveryTargetToItsOwnStableTaskAndBilingualFeedback() {
        ColoursContent.order.forEach { target ->
            val question = ColoursContent.question(target, ColoursContent.order.reversed())
            assertEquals(target, question.correct)
            assertEquals(ColoursContent.order.toSet(), question.choices.toSet())
            assertEquals("task.colours.find.${target.value.substringAfterLast('.')}", question.definition.value)
            assertEquals("skill.colours.recognition", question.skill.value)
            assertEquals("context.colours.named_swatches", question.context.value)
            val color = ColoursContent.definition(target).text.display
            assertEquals("Find ${color.en}.", question.instruction.display.en)
            assertEquals("Finde ${color.de.replaceFirstChar { it.uppercase() }}.", question.instruction.display.de)
            assertEquals("That's ${color.en}!", question.correctFeedback.display.en)
            assertEquals("Das ist ${color.de.replaceFirstChar { it.uppercase() }}!", question.correctFeedback.display.de)
            assertNotNull(question.hint)
        }
    }

    @Test fun fiveAndTenQuestionRoundsAreDeterministicBalancedAndDoNotRepeatAdjacentTargets() {
        for (round in RoundLength.entries) {
            val id = SessionId("colour-round-${round.count}")
            val first = ColoursContent.generate(id, round, 89123L) as GenerationResult.Generated
            val second = ColoursContent.generate(id, round, 89123L) as GenerationResult.Generated
            assertEquals(ColoursContent.REVISION, first.plan.activityRevision)
            assertEquals(ColoursContent.version, first.plan.contentVersion)
            assertEquals(first.plan.tasks.map { it.question.correct }, second.plan.tasks.map { it.question.correct })
            assertEquals(first.plan.tasks.map { it.question.choices }, second.plan.tasks.map { it.question.choices })
            val targets = first.plan.tasks.map { it.question.correct }
            assertEquals(round.count, targets.size)
            assertEquals(ColoursContent.order.toSet(), targets.toSet())
            assertTrue(targets.zipWithNext().none { (a, b) -> a == b })
            assertTrue(targets.groupingBy { it }.eachCount().values.let { it.max() - it.min() <= 1 })
            assertTrue(first.plan.tasks.all { it.question.choices.toSet() == ColoursContent.order.toSet() })
            val state = SessionReducer.start(first.plan, ContentLanguage.ENGLISH, ColoursContent.repository).state
            ColoursContent.validate(state)
        }
    }

    @Test fun incorrectAnswerCanRetryAndHintIsRecordedAsSupportWithoutScoringExploration() {
        val plan = (ColoursContent.generate(SessionId("retry-colour"), RoundLength.FIVE, 4L) as GenerationResult.Generated).plan
        var state = SessionReducer.start(plan, ContentLanguage.GERMAN, ColoursContent.repository).state
        val wrong = state.task.question.choices.first { it != state.task.question.correct }
        state = SessionReducer.reduce(state, SessionAction.Hint(state.task.id)).state
        state = SessionReducer.reduce(state, SessionAction.Answer(state.nextAttempt!!, wrong)).state
        assertEquals(AnswerState.RETRY_AVAILABLE, state.current.answer)
        assertTrue(state.current.support.hint)
        state = SessionReducer.reduce(state, SessionAction.Retry(AttemptId(state.task.id, state.current.attempts))).state
        state = SessionReducer.reduce(state, SessionAction.Answer(state.nextAttempt!!, state.task.question.correct)).state
        assertEquals(AnswerState.CORRECT, state.current.answer)
        assertEquals(1, state.current.retries)
        assertEquals(1, state.score)
    }
}
