package com.bellfamily.bastischool.learning.addition

import com.bellfamily.bastischool.learning.models.ContentLanguage
import com.bellfamily.bastischool.learning.session.*
import org.junit.Assert.*
import org.junit.Test

class MixedArithmeticTest {
    @Test fun makeFiveRoundsCoverEveryMissingAmountWithNoAdjacentRepeat() {
        assertNotEquals(AdditionContent.activity, MakeFiveContent.activity)
        assertNotEquals(AdditionContent.skill, MakeFiveContent.skill)
        for (round in RoundLength.entries) for (seed in 0L..100L) {
            val id = SessionId("make-five-$seed-${round.count}")
            val first = (MakeFiveContent.generate(id, round, seed) as GenerationResult.Generated).plan
            val second = (MakeFiveContent.generate(id, round, seed) as GenerationResult.Generated).plan
            val taskFacts = first.tasks.map { MakeFiveContent.fact(it.question) }
            assertEquals(taskFacts, second.tasks.map { MakeFiveContent.fact(it.question) })
            val state = SessionReducer.start(first, ContentLanguage.ENGLISH, MakeFiveContent.repository).state
            MakeFiveContent.validate(state)
            assertEquals((1..4).toSet(), taskFacts.map { it.shown }.toSet())
            assertTrue(taskFacts.zipWithNext().none { (a, b) -> a.shown == b.shown })
            taskFacts.forEach { assertEquals(5, it.shown + it.missing) }
            first.tasks.forEach { task -> assertEquals(4, task.question.choices.toSet().size) }
        }
    }

    @Test fun subtractionCoversEveryRemainderAndUsesItsOwnIdentity() {
        assertEquals(15, SubtractionContent.facts.size)
        assertEquals((0..4).toSet(), SubtractionContent.facts.map { it.answer }.toSet())
        assertNotEquals(AdditionContent.activity, SubtractionContent.activity)
        assertNotEquals(AdditionContent.skill, SubtractionContent.skill)
        assertNotEquals(AdditionContent.context, SubtractionContent.context)
        assertEquals("How many are left?", SubtractionContent.prompt.display.en)
    }

    @Test fun subtractionRoundsAreDeterministicAndCoverAllFiveRemainders() {
        for (round in RoundLength.entries) for (seed in 0L..100L) {
            val id = SessionId("sub-$seed-${round.count}")
            val a = SubtractionContent.generate(id, round, seed) as GenerationResult.Generated
            val b = SubtractionContent.generate(id, round, seed) as GenerationResult.Generated
            assertEquals(a.plan.tasks.map { Triple(it.question.definition, it.question.choices, it.question.correct) },
                b.plan.tasks.map { Triple(it.question.definition, it.question.choices, it.question.correct) })
            val state = SessionReducer.start(a.plan, ContentLanguage.ENGLISH, SubtractionContent.repository).state
            SubtractionContent.validate(state)
            val facts = a.plan.tasks.map { SubtractionContent.definition(it.question) }
            assertEquals(round.count, facts.toSet().size)
            assertEquals((0..4).toSet(), facts.map { it.answer }.toSet())
            facts.forEach { fact -> assertEquals(fact.answer, fact.start - fact.takeAway) }
        }
    }

    @Test fun mixedAdditionBalancesBothRangesAndIncludesTheRequestedExamples() {
        assertNotEquals(AdditionContent.activity, MixedAdditionContent.activity)
        assertNotEquals(AdditionChallengeContent.activity, MixedAdditionContent.activity)
        assertNotEquals(AdditionContent.skill, MixedAdditionContent.skill)
        for (round in RoundLength.entries) for (seed in 0L..100L) {
            val plan = (MixedAdditionContent.generate(SessionId("mix-$seed-${round.count}"), round, seed) as GenerationResult.Generated).plan
            val state = SessionReducer.start(plan, ContentLanguage.ENGLISH, MixedAdditionContent.repository).state
            MixedAdditionContent.validate(state)
            val facts = plan.tasks.map { MixedAdditionContent.definition(it.question) }
            assertEquals(round.count / 2, facts.count { it in AdditionContent.catalogue() })
            assertTrue(facts.contains(AdditionFact(4, 7)))
            assertTrue(facts.contains(AdditionFact(5, 9)))
            assertTrue(facts.all { it.total == it.left + it.right })
        }
    }
}
