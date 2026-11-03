package com.bellfamily.bastischool.learning.addition

import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.session.*
import org.junit.Assert.*
import org.junit.Test

class AdditionChallengeTest {
    private fun plan(round: RoundLength, seed: Long) =
        (AdditionChallengeContent.generate(SessionId("addition-challenge"), round, seed) as GenerationResult.Generated).plan

    @Test fun challengeBankContainsTheRequestedFactsAndStopsAtTwenty() {
        val facts = AdditionChallengeContent.catalogue()
        assertEquals(36, facts.size)
        assertEquals(36, facts.map { it.unordered }.toSet().size)
        assertTrue(AdditionFact(4, 7) in facts)
        assertTrue(AdditionFact(5, 9) in facts)
        facts.forEach { fact ->
            assertTrue(fact.left in 3..10 && fact.right in fact.left..10)
            assertTrue(fact.total in 6..20)
        }
        assertEquals("activity.math.add_within_20", AdditionChallengeContent.activity.value)
        assertEquals("skill.math.addition.within_20", AdditionChallengeContent.skill.value)
        assertNotEquals(AdditionContent.activity, AdditionChallengeContent.activity)
    }

    @Test fun seededFiveAndTenQuestionRoundsIncludeTheExamplesAndDistinctTotals() {
        for (round in RoundLength.entries) for (seed in 0L..100L) {
            val first = plan(round, seed)
            val second = plan(round, seed)
            assertArrayEquals(SessionCheckpoint.encode(SessionReducer.start(first, ContentLanguage.ENGLISH,
                AdditionChallengeContent.repository).state), SessionCheckpoint.encode(SessionReducer.start(second,
                ContentLanguage.ENGLISH, AdditionChallengeContent.repository).state))
            val state = SessionReducer.start(first, ContentLanguage.ENGLISH, AdditionChallengeContent.repository).state
            AdditionChallengeContent.validate(state)
            val facts = first.tasks.map { AdditionChallengeContent.definition(it.question) }
            assertEquals(round.count, facts.size)
            assertTrue(AdditionFact(4, 7) in facts)
            assertTrue(AdditionFact(5, 9) in facts)
            assertEquals(facts.size, facts.map { it.unordered }.toSet().size)
            assertEquals(facts.size, facts.map { it.total }.toSet().size)
            first.tasks.forEach { task ->
                assertEquals(2, task.question.difficulty)
                assertEquals(4, task.question.choices.toSet().size)
                assertEquals(1, task.question.choices.count { it == task.question.correct })
                assertTrue(task.question.choices.all { AdditionChallengeContent.numberValue(it) in 6..20 })
                assertEquals(AdditionChallengeContent.definition(task.question).total,
                    AdditionChallengeContent.numberValue(task.question.correct))
            }
        }
    }

    @Test fun challengeUsesNumberWordsForAccessibleGroupsAndSharesNoProgressIdentity() {
        val q = AdditionChallengeContent.question(AdditionFact(4, 7), listOf(11, 10, 12, 9).map(AdditionChallengeContent::numberId))
        assertEquals(AdditionFact(4, 7), AdditionChallengeContent.definition(q))
        assertEquals("First group, four dots", AdditionChallengeContent.groupDescription(4, true, ContentLanguage.ENGLISH))
        assertEquals("Zweite Gruppe, neun Punkte", AdditionChallengeContent.groupDescription(9, false, ContentLanguage.GERMAN))
        assertEquals("Add Bigger Numbers", AdditionChallengeContent.title.display.en)
        assertEquals("Größere Zahlen addieren", AdditionChallengeContent.title.display.de)
        assertTrue(AdditionChallengeContent.repository.find(ContentId("number.twenty")) != null)
        assertEquals(5, AdditionChallengeContent.numberValue(ContentId("number.five")))
    }
}
