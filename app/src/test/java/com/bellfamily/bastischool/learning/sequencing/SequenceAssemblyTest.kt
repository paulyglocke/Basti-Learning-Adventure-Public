package com.bellfamily.bastischool.learning.sequencing

import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.session.*
import org.junit.Assert.*
import org.junit.Test

class SequenceAssemblyTest {
    private val order=(1..4).map {ContentId("example.item_$it")}
    private fun start(seed:Long=1)=SequenceAssembly.start(SessionId("sequence"),order,seed,ContentLanguage.ENGLISH)
    private fun apply(s:SequenceState,op:SequenceOperation)=SequenceAssembly.reduce(s,SequenceAction(s.id,s.revision,op))
    @Test fun deterministicScrambleReusesExistingOrderingContract() {
        for(seed in 0L..100L){val s=start(seed);assertEquals(start(seed).presentation,s.presentation)
            assertEquals(OrderedPlacement.scramble(order,seed),s.presentation);assertNotEquals(order,s.presentation);assertEquals(order.toSet(),s.available.toSet())}
    }
    @Test fun freePlacementRemovalAndIncompleteCheck() {
        var s=start();s=apply(s,SequenceOperation.Append(order.last()))
        assertEquals(listOf(order.last()),s.constructed);assertEquals(0,s.attempts)
        assertSame(s,apply(s,SequenceOperation.Check));assertFalse(s.completed)
        s=apply(s,SequenceOperation.Remove(order.last()));assertTrue(s.constructed.isEmpty());assertEquals(s.presentation,s.available)
    }
    @Test fun staleDuplicateUnknownAndInvalidRemoveAreRejected() {
        val s=start();val a=SequenceAction(s.id,s.revision,SequenceOperation.Append(order[0]));val n=SequenceAssembly.reduce(s,a)
        assertSame(n,SequenceAssembly.reduce(n,a));assertSame(n,apply(n,SequenceOperation.Append(order[0])))
        assertSame(n,apply(n,SequenceOperation.Append(ContentId("unknown.item"))))
        assertSame(n,apply(n,SequenceOperation.Remove(order[1])))
        assertSame(n,SequenceAssembly.reduce(n,SequenceAction(SessionId("old"),n.revision,SequenceOperation.Check)))
    }
    @Test fun wrongCheckRemainsEditableHintDoesNotSolveAndSupportSurvivesCorrection() {
        var s=start();order.reversed().forEach {s=apply(s,SequenceOperation.Append(it))}
        s=apply(s,SequenceOperation.Check);assertFalse(s.completed);assertTrue(s.needsCorrection);assertEquals(1,s.attempts)
        s=apply(s,SequenceOperation.Hint);assertEquals(0,s.hintPosition);assertEquals(order.reversed(),s.constructed)
        s=apply(s,SequenceOperation.Replay);assertEquals(1,s.support.replays)
        order.forEach {s=apply(s,SequenceOperation.Remove(it))};order.forEach {s=apply(s,SequenceOperation.Append(it))}
        assertFalse(s.completed);s=apply(s,SequenceOperation.Check);assertTrue(s.completed);assertEquals(2,s.attempts);assertFalse(s.support.independent)
        assertSame(s,apply(s,SequenceOperation.Remove(order[0])))
    }
    @Test fun hintIdentifiesFirstMismatchBeyondCorrectPrefix() {
        var s=start();s=apply(s,SequenceOperation.Append(order[0]));s=apply(s,SequenceOperation.Append(order[3]))
        s=apply(s,SequenceOperation.Hint);assertEquals(1,s.hintPosition);assertEquals(listOf(order[0],order[3]),s.constructed)
    }
    @Test fun corruptionAndDuplicateMissingPresentationAreRejected() {
        val s=start()
        assertThrows(IllegalArgumentException::class.java){SequenceState(s.id,s.language,order,List(4){order[0]})}
        assertThrows(IllegalArgumentException::class.java){SequenceState(s.id,s.language,order,order, listOf(order[0],order[0]))}
        assertThrows(IllegalArgumentException::class.java){SequenceState(s.id,s.language,order,order,attempts=1)}
        assertThrows(IllegalArgumentException::class.java){SequenceState(s.id,s.language,order,order,acknowledged=true)}
    }
}
