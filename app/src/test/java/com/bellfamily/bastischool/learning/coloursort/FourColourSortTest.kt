package com.bellfamily.bastischool.learning.coloursort

import com.bellfamily.bastischool.learning.colours.ColoursContent
import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.sorting.*
import com.bellfamily.bastischool.learning.session.*
import com.bellfamily.bastischool.learning.progress.*
import org.junit.Assert.*
import org.junit.Test
import java.io.IOException

class FourColourSortTest {
    private class Disk : ProgressStorage { var bytes: ByteArray? = null
        override fun <T> access(block: (ProgressTransaction) -> T): T = block(object : ProgressTransaction {
            override fun read() = bytes?.clone()
            override fun replace(bytes: ByteArray) { this@Disk.bytes = bytes.clone() }
        })
    }
    private fun repo(events: MutableList<ProgressEvent>) = object : ProgressRepository {
        override fun append(event: ProgressEvent): ProgressWriteResult {
            val old = events.indexOfFirst { it.id == event.id }
            if (old >= 0 && events[old] != event) return ProgressWriteResult.Failed(ProgressFailure.CONFLICT)
            if (old < 0) events += event
            return ProgressWriteResult.Saved(StoredProgressEvent(events.indexOf(event) + 1L, 0, event), old < 0)
        }
        override fun read(query: ProgressQuery) = ProgressReadResult.Events(emptyList())
    }
    private fun open(disk: Disk, events: MutableList<ProgressEvent>) = FourColourSortHost(disk, repo(events)).also {
        it.open(SessionId("four-colour-sort"), 67L, ContentLanguage.ENGLISH)
    }
    private fun place(host: FourColourSortHost, item: ColourSortObject) {
        host.dispatch(SortAction.Select(item.id))
        host.dispatch(SortAction.Place(host.state!!.id, item.id, item.category, host.state!!.placement(item.id).attempts + 1))
    }

    @Test fun separateEightObjectRuleAndRestorationProduceIndependentSingleCompletion() {
        assertNotEquals(ColourSort.activity, FourColourSort.activity)
        assertNotEquals(ColourSort.journalMagic, FourColourSort.journalMagic)
        assertEquals(ColoursContent.order, FourColourSort.categories)
        assertEquals(8, FourColourSort.rule.items.size)
        assertEquals(listOf(2, 2, 2, 2), FourColourSort.categories.map { category -> FourColourSort.rule.items.count { it.category == category } })
        val disk = Disk(); val events = mutableListOf<ProgressEvent>(); var host = open(disk, events)
        place(host, FourColourSort.objects.first())
        val partial = disk.bytes!!.clone()
        host = open(disk, events)
        assertArrayEquals(partial, disk.bytes)
        assertEquals(1, host.state!!.placements.count { it.placed })
        assertEquals(7, host.state!!.placements.count { !it.placed })
        FourColourSort.objects.drop(1).forEach { place(host, it) }
        assertTrue(host.state!!.completed)
        host = open(disk, events)
        assertTrue(host.state!!.acknowledged)
        assertEquals(8, events.filterIsInstance<AttemptEvent>().size)
        assertEquals(1, events.filterIsInstance<CompletionEvent>().size)
        assertTrue(events.all { it.origin.activity == FourColourSort.activity })
        assertTrue(events.none { it.origin.activity == ColourSort.activity })
    }

    @Test fun wrongPlacementRetainsSelectionForCorrectionAndRecordsBothAttempts() {
        val disk = Disk(); val events = mutableListOf<ProgressEvent>(); val host = open(disk, events)
        val item = FourColourSort.objects.first()
        host.dispatch(SortAction.Select(item.id))
        val wrong = FourColourSort.categories.first { it != item.category }
        host.dispatch(SortAction.Place(host.state!!.id, item.id, wrong, 1))
        assertEquals(item.id, host.state!!.selected)
        assertFalse(host.state!!.placement(item.id).placed)
        host.dispatch(SortAction.Place(host.state!!.id, item.id, item.category, 2))
        assertTrue(host.state!!.placement(item.id).placed)
        assertEquals(2, events.filterIsInstance<AttemptEvent>().size)
    }
}
