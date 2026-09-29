package com.bellfamily.bastischool.learning.coloursort

import com.bellfamily.bastischool.learning.sorting.*
import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.session.*
import com.bellfamily.bastischool.learning.progress.*
import org.junit.Assert.*
import org.junit.Test

class ColourSortTest {
    private fun start()=ColourSort.start(SessionId("colours"),42,ContentLanguage.ENGLISH)
    @Test fun fourUnambiguousItemsTwoColoursAndIndependentBilingualCopy() {
        assertEquals(4,ColourSort.objects.size);assertEquals(4,ColourSort.objects.map {it.id}.toSet().size)
        assertEquals(listOf("red ball","blue ball","red block","blue block"),ColourSort.objects.map {it.text.display.en})
        assertEquals(listOf("roter Ball","blauer Ball","roter Baustein","blauer Baustein"),ColourSort.objects.map {it.text.display.de})
        assertEquals(listOf("rot","blau"),ColourSort.categories.map {ColourSort.category(it).display.de})
        assertTrue(ColourSort.instruction.display.en.startsWith("Sort by colour."))
        assertTrue(ColourSort.instruction.display.de.startsWith("Sortiere nach Farben."))
        assertEquals(listOf(2,2),ColourSort.categories.map {c->ColourSort.objects.count {it.category==c}})
    }
    @Test fun onlyPlacementsEmitAttemptsAndAllFourEmitCompletion() {
        var s=start()
        assertTrue(ColourSort.reduce(s,SortAction.Replay).events.isEmpty())
        for((index,item) in ColourSort.objects.withIndex()) {
            val selection=ColourSort.reduce(s,SortAction.Select(item.id));assertTrue(selection.events.isEmpty());s=selection.state
            val placed=ColourSort.reduce(s,SortAction.Place(s.id,item.id,item.category,1));s=placed.state
            assertEquals(1,placed.events.filterIsInstance<AttemptEvent>().size)
            val e=placed.events.first() as AttemptEvent
            assertTrue(e.support.independent);assertEquals(AttemptOutcome.CORRECT,e.outcome)
            assertEquals(SkillId("skill.colours.sort"),e.evidence.skill)
            assertEquals(if(index==3)1 else 0,placed.events.filterIsInstance<CompletionEvent>().size)
        }
        assertTrue(s.completed);assertEquals(4,ColourSort.completionEvent(s).tasks.size)
    }
    @Test fun wrongThenHintThenCorrectPreservesSupportWithoutPenalty() {
        var s=Sorting.reduce(start(),SortAction.Select(ColourSort.objects.first().id));val id=s.selected!!
        val wrong=ColourSort.reduce(s,SortAction.Place(s.id,id,ColourSort.blue,1));s=wrong.state
        assertEquals(AttemptOutcome.INCORRECT,(wrong.events.single() as AttemptEvent).outcome)
        val hint=ColourSort.reduce(s,SortAction.Hint);s=hint.state
        assertEquals("Put it with the red ones.",hint.speech!!.text.display.en)
        val correct=ColourSort.reduce(s,SortAction.Place(s.id,id,ColourSort.red,2))
        val e=correct.events.single() as AttemptEvent
        assertTrue(e.support.hint);assertEquals(1,e.retriesBeforeAttempt);assertEquals(AttemptOutcome.CORRECT,e.outcome)
    }
}
