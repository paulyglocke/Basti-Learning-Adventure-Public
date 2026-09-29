package com.bellfamily.bastischool.learning.sorting

import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.session.*
import org.junit.Assert.*
import org.junit.Test

class SortingTest {
    private val a=ContentId("group.first");private val b=ContentId("group.second")
    private val one=ContentId("item.first");private val two=ContentId("item.second")
    private val rule=SortRule(ContentId("rule.example.membership"),listOf(a,b),listOf(SortItem(one,a),SortItem(two,b)))
    private fun start(seed:Long=4)=Sorting.start(SessionId("sort"),rule,seed,ContentLanguage.ENGLISH)
    private fun place(s:SortState,id:ContentId,group:ContentId)=Sorting.reduce(Sorting.reduce(s,SortAction.Select(id)),SortAction.Place(s.id,id,group,s.placement(id).attempts+1))
    @Test fun semanticMembershipWorksWithoutColoursOrUi() {
        val s=place(start(),one,a)
        assertTrue(s.placement(one).placed);assertFalse(s.completed);assertNull(s.selected)
        assertTrue(place(s,two,b).completed)
    }
    @Test fun incorrectPlacementCanBeCorrectedImmediatelyWithoutAutoSolving() {
        val wrong=place(start(),one,b)
        assertFalse(wrong.placement(one).placed);assertEquals(one,wrong.selected);assertEquals(1,wrong.placement(one).attempts)
        val correct=place(wrong,one,a)
        assertTrue(correct.placement(one).placed);assertEquals(2,correct.placement(one).attempts)
    }
    @Test fun noSelectionEmptySpaceUnknownCategoryStaleSessionAndDoubleActionAreIgnored() {
        val s=start();assertSame(s,Sorting.reduce(s,SortAction.Place(s.id,one,a,1)))
        val selected=Sorting.reduce(s,SortAction.Select(one))
        assertSame(selected,Sorting.reduce(selected,SortAction.Place(s.id,one,ContentId("group.unknown"),1)))
        assertSame(selected,Sorting.reduce(selected,SortAction.Place(SessionId("old"),one,a,1)))
        val action=SortAction.Place(s.id,one,a,1);val placed=Sorting.reduce(selected,action)
        assertSame(placed,Sorting.reduce(placed,action))
        assertSame(placed,Sorting.reduce(placed,SortAction.Select(one)))
    }
    @Test fun deterministicOrderAndLanguagePreserveSemanticState() {
        for(seed in 0L..50L)assertEquals(start(seed).order,start(seed).order)
        val s=place(start(),one,a);val de=Sorting.reduce(s,SortAction.Language(ContentLanguage.GERMAN))
        assertEquals(s.order,de.order);assertEquals(s.placements,de.placements);assertEquals(s.id,de.id)
    }
    @Test fun supportNeverPlacesOrIncrementsAttempts() {
        val initial=start();val replay=Sorting.reduce(initial,SortAction.Replay)
        assertTrue(replay.placements.all {it.support.replays==1 && it.attempts==0 && !it.placed})
        val selected=Sorting.reduce(replay,SortAction.Select(one));val hint=Sorting.reduce(selected,SortAction.Hint)
        assertTrue(hint.placement(one).support.hint);assertFalse(hint.placement(two).support.hint)
        assertEquals(0,hint.placement(one).attempts)
        assertTrue(place(hint,one,a).placement(one).support.hint)
    }
    @Test fun duplicateMissingAndInvalidMembershipAreRejected() {
        fun rejects(block:()->Unit){try{block();fail("must reject")}catch(_:IllegalArgumentException){}}
        rejects {SortRule(rule.id,listOf(a,a),rule.items)}
        rejects {SortRule(rule.id,listOf(a,b),listOf(SortItem(one,a),SortItem(one,b)))}
        rejects {SortRule(rule.id,listOf(a,b),listOf(SortItem(one,ContentId("group.unknown"))))}
        rejects {SortState(SessionId("bad"),rule,ContentLanguage.ENGLISH,listOf(one,one),List(2){SortPlacement()})}
        rejects {SortState(SessionId("bad"),rule,ContentLanguage.ENGLISH,listOf(one),List(2){SortPlacement()})}
        rejects {SortState(SessionId("bad"),rule,ContentLanguage.ENGLISH,listOf(one,two),listOf(SortPlacement(true,1,b),SortPlacement()))}
    }
}
