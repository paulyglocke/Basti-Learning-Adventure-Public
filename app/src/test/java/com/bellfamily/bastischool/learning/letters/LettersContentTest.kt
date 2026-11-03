package com.bellfamily.bastischool.learning.letters

import com.bellfamily.bastischool.learning.models.ContentLanguage
import com.bellfamily.bastischool.learning.session.*
import org.junit.Assert.*
import org.junit.Test

class LettersContentTest {
    @Test fun bothRoundSizesHaveNoRepeatedQuestionOrTargetAnimal() {
        for (round in RoundLength.entries) for (language in ContentLanguage.entries) for (seed in 0L..100L) {
            val result = LettersContent.generate(SessionId("letters-$round-$language-$seed"), round, seed)
            assertTrue(result.toString(), result is GenerationResult.Generated)
            val plan = (result as GenerationResult.Generated).plan
            val targets = plan.tasks.map { it.question.correct }
            assertEquals(round.count, targets.distinct().size)
            assertEquals(round.count, plan.tasks.map { it.question.definition }.distinct().size)
            plan.tasks.forEach { task ->
                val question = task.question
                val targetInitial = LettersContent.letter(question.correct, language)
                assertEquals(4, question.choices.distinct().size)
                assertEquals(1, question.choices.count { LettersContent.letter(it, language) == targetInitial })
            }
            val state = SessionReducer.start(plan, language, LettersContent.repository).state
            LettersContent.validate(state)
        }
    }

    @Test fun writtenInitialsAreExplicitlyLocalizedAndCanonicalArtIsReferenced() {
        assertEquals("H", LettersContent.letter(com.bellfamily.bastischool.learning.models.ContentId("animal.horse"), ContentLanguage.ENGLISH))
        assertEquals("P", LettersContent.letter(com.bellfamily.bastischool.learning.models.ContentId("animal.horse"), ContentLanguage.GERMAN))
        assertEquals("C", LettersContent.letter(com.bellfamily.bastischool.learning.models.ContentId("animal.crocodile"), ContentLanguage.ENGLISH))
        assertEquals("K", LettersContent.letter(com.bellfamily.bastischool.learning.models.ContentId("animal.crocodile"), ContentLanguage.GERMAN))
        assertTrue(LettersContent.animals.all { it.image.startsWith("Animals/canonical/") })
    }
}
