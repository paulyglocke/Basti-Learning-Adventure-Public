package com.bellfamily.bastischool.ui.common

import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.vocabulary.*
import com.bellfamily.bastischool.learning.session.*
import com.bellfamily.bastischool.learning.progress.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.Rule
import org.junit.rules.TemporaryFolder

class CelebrationStateTest {
    @get:Rule val temp=TemporaryFolder()
    @Test fun fiveStableIdsAndSemanticAssignmentsAreDeterministicAndBounded() {
        repeat(200) {n ->
            val a=CelebrationState.create("round-$n");val b=CelebrationState.create("round-$n")
            assertEquals((0..4).toList(),a.slots.map {it.id});assertEquals(a.slots,b.slots)
            assertEquals(5,a.slots.map {it.animal}.distinct().size)
            a.slots.forEach {assertTrue(VocabularyContent.item(it.animal).text.display.de.isNotBlank())}
        }
    }
    @Test fun artworkReferencesCoverExactlyTheSemanticPool() {
        assertEquals(CelebrationState.animals.toSet(),CelebrationState.artwork.keys)
        assertEquals(listOf("dinosaur","snake","crocodile","whale","fish").map {"Animals/canonical/$it.png"}.toSet(),CelebrationState.artwork.values.toSet())
    }
    @Test fun popOnlyOnceAndSettledAnimalRemainsWhileOtherSlotsStayInteractive() {
        val start=CelebrationState.create("round")
        assertSame(start,start.pop(-1));assertSame(start,start.settle(0))
        val popped=start.pop(0);assertSame(popped,popped.pop(0))
        val settled=popped.settle(0);assertSame(settled,settled.pop(0));assertSame(settled,settled.settle(0))
        assertEquals(BalloonPhase.SETTLED,settled.slots[0].phase)
        assertEquals(start.slots[0].animal,settled.slots[0].animal)
        assertEquals(BalloonPhase.REVEALING,settled.pop(1).slots[1].phase)
        assertEquals(BalloonPhase.FLOATING,start.slots[0].phase)
    }
    @Test fun optionalResetHasNoGlobalStateAndKeepsSemanticAssignment() {
        val initial=CelebrationState.create("completed")
        val used=initial.pop(0).settle(0)
        assertEquals(initial.slots,CelebrationState.create("completed").slots)
        assertNotEquals(used.slots,CelebrationState.create("completed").slots)
        assertTrue(CelebrationState.create("new-round").slots.all {it.phase==BalloonPhase.FLOATING})
    }
    @Test fun completionPersistsBeforeAnyPopsAndRewardResetDoesNotDuplicateProgress() {
        val disk=AtomicProgressStorage(temp.newFolder(),JvmAtomicCommit)
        val progressDir=temp.newFolder()
        fun open()=VocabularyContent.host(VocabularyPhase.FIND,disk,ProgressFixtures.repository(progressDir)).also {
            it.open(SessionId("learning"),RoundLength.FIVE,42,ContentLanguage.ENGLISH)
        }
        val host=open()
        repeat(5) {val s=host.state!!;host.dispatch(SessionAction.Answer(s.nextAttempt!!,s.task.question.correct));host.dispatch(SessionAction.Next(s.task.id))}
        assertEquals(CompletionState.ACKNOWLEDGED,host.state!!.completion)
        val before=SessionCheckpoint.encode(host.state!!)
        assertEquals(6,ProgressFixtures.records(ProgressFixtures.repository(progressDir)).size)
        var reward=CelebrationState.create(host.state!!.plan.id.value)
        repeat(5){reward=reward.pop(it).settle(it)}
        assertArrayEquals(before,SessionCheckpoint.encode(host.state!!))
        val restored=open()
        CelebrationState.create(restored.state!!.plan.id.value)
        assertArrayEquals(before,SessionCheckpoint.encode(restored.state!!))
        assertEquals(6,ProgressFixtures.records(ProgressFixtures.repository(progressDir)).size)
    }
}
