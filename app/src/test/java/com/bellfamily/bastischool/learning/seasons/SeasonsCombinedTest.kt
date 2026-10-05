package com.bellfamily.bastischool.learning.seasons

import com.bellfamily.bastischool.learning.content.SeasonIds
import com.bellfamily.bastischool.learning.models.ContentLanguage
import com.bellfamily.bastischool.learning.session.RoundLength
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class SeasonsCombinedTest {
    @Test fun everyAnchorUsesCanonicalPreviousAndNextIncludingWrap() {
        SeasonIds.canonicalOrder.forEach { anchor ->
            val task = SeasonsCombined.task(anchor)
            assertEquals(SeasonIds.previous(anchor), task.before)
            assertEquals(SeasonIds.next(anchor), task.after)
        }
        assertEquals(SeasonIds.WINTER, SeasonsCombined.task(SeasonIds.SPRING).before)
        assertEquals(SeasonIds.SPRING, SeasonsCombined.task(SeasonIds.WINTER).after)
    }

    @Test fun generationIsDeterministicAndFiveQuestionsCoverAllAnchors() {
        val first = SeasonsCombined.generate(RoundLength.FIVE, 42)
        assertEquals(first, SeasonsCombined.generate(RoundLength.FIVE, 42))
        assertEquals(5, first.size)
        assertEquals(SeasonIds.canonicalOrder.toSet(), first.take(4).map { it.anchor }.toSet())
        assertNotEquals(first, SeasonsCombined.generate(RoundLength.FIVE, 43))
    }

    @Test fun wordingIsExactInBothLanguagesAndChoicesStayCanonical() {
        val summer = SeasonsCombined.task(SeasonIds.SUMMER)
        assertEquals("Which season comes before and after Summer?", SeasonsCombined.question(summer, ContentLanguage.ENGLISH))
        assertEquals("Welche Jahreszeit kommt vor und nach dem Sommer?", SeasonsCombined.question(summer, ContentLanguage.GERMAN))
        assertEquals(SeasonIds.canonicalOrder.toSet(), summer.choices.toSet())
    }
    @Test fun slotsRemainOrderedAndCorrectWorkSurvivesWrongOtherSlot() {
        val task = SeasonsCombined.task(SeasonIds.SUMMER)
        var state = SeasonsCombined.State("test", listOf(task))
        state = SeasonsCombined.reduce(state, SeasonsCombined.Action.Before(task.before))
        state = SeasonsCombined.reduce(state, SeasonsCombined.Action.After(SeasonIds.WINTER))
        state = SeasonsCombined.reduce(state, SeasonsCombined.Action.Check)
        assertEquals(task.before, state.before)
        assertEquals(SeasonIds.WINTER, state.after)
        assertEquals(false, state.solved)
        state = SeasonsCombined.reduce(state, SeasonsCombined.Action.After(task.after))
        assertEquals(true, state.solved)
    }

    @Test fun helpDoesNotFillEitherSlotAndNextCompletesRound() {
        val task = SeasonsCombined.task(SeasonIds.WINTER)
        var state = SeasonsCombined.State("test", listOf(task))
        state = SeasonsCombined.reduce(state, SeasonsCombined.Action.Help)
        assertEquals(null, state.before); assertEquals(null, state.after); assertEquals(true, state.help)
        state = SeasonsCombined.reduce(state, SeasonsCombined.Action.Before(task.before))
        state = SeasonsCombined.reduce(state, SeasonsCombined.Action.After(task.after))
        state = SeasonsCombined.reduce(state, SeasonsCombined.Action.Next)
        assertEquals(true, state.completed)
    }
}
