package com.bellfamily.bastischool.learning.wilma

import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.session.*
import com.bellfamily.bastischool.learning.progress.*
import org.junit.Assert.*
import org.junit.Test

class WilmaOrderTest {
    private fun start()=WilmaOrder.start(SessionId("week"),42,ContentLanguage.ENGLISH)
    @Test fun sevenUniqueDaysScrambleDeterministicallyWithoutHeadOrTail() {
        for(seed in 0L..50L) {
            val a=WilmaOrder.start(SessionId("week"),seed,ContentLanguage.ENGLISH)
            val b=WilmaOrder.start(SessionId("week"),seed,ContentLanguage.GERMAN)
            assertEquals(a.choices,b.choices);assertEquals(7,a.choices.size);assertEquals(WilmaContent.days.toSet(),a.choices.toSet())
            assertNotEquals(WilmaContent.days,a.choices)
        }
    }
    @Test fun placementLocksEachStepAndCompletesOnceWithSevenEvidenceRecords() {
        var s=start();val events=mutableListOf<ProgressEvent>()
        WilmaContent.days.forEach {day ->
            val action=WilmaOrderAction.Place(s.nextAttempt!!,day)
            val next=WilmaOrder.reduce(s,action);s=next.state;events+=next.events
            val repeated=WilmaOrder.reduce(s,action);assertSame(s,repeated.state);assertTrue(repeated.events.isEmpty())
        }
        assertTrue(s.completed);assertEquals(WilmaContent.days,s.placed)
        assertEquals(7,events.filterIsInstance<AttemptEvent>().size)
        assertEquals(7,events.filterIsInstance<CompletionEvent>().single().tasks.size)
    }
    @Test fun wrongPlacementPreservesSequenceAndReplayHelpAreNotAttempts() {
        var s=WilmaOrder.reduce(start(),WilmaOrderAction.Place(start().nextAttempt!!,WilmaContent.days[0])).state
        val task=s.task
        s=WilmaOrder.reduce(s,WilmaOrderAction.Replay(task)).state
        s=WilmaOrder.reduce(s,WilmaOrderAction.Help(task)).state
        assertEquals(0,s.current.attempts);assertTrue(s.current.support.hint);assertEquals(1,s.current.support.replays)
        val wrong=WilmaOrder.reduce(s,WilmaOrderAction.Place(s.nextAttempt!!,WilmaContent.days[3]));s=wrong.state
        assertEquals(1,s.index);assertEquals(AnswerState.RETRY_AVAILABLE,s.current.answer)
        assertEquals(AttemptOutcome.INCORRECT,(wrong.events.single() as AttemptEvent).outcome)
        s=WilmaOrder.reduce(s,WilmaOrderAction.Retry(AttemptId(task,1))).state
        s=WilmaOrder.reduce(s,WilmaOrderAction.Language(ContentLanguage.GERMAN)).state
        s=WilmaOrder.reduce(s,WilmaOrderAction.Place(s.nextAttempt!!,WilmaContent.days[1])).state
        assertEquals(2,s.index);assertEquals(2,s.steps[1].attempts);assertEquals(1,s.steps[1].retries)
        assertTrue(s.steps[1].support.hint);assertEquals(ContentLanguage.GERMAN,s.language)
    }
    @Test fun partialOrderingCheckpointPreservesActualScrambleAndRejectsCorruption() {
        var s=start()
        repeat(3){s=WilmaOrder.reduce(s,WilmaOrderAction.Place(s.nextAttempt!!,WilmaContent.days[it])).state}
        val bytes=WilmaOrderHost.encode(s,emptyList());val restored=WilmaOrderHost.decode(bytes).first
        assertEquals(s.choices,restored.choices);assertEquals(s.steps,restored.steps);assertEquals(3,restored.index)
        assertArrayEquals(bytes,WilmaOrderHost.encode(restored,emptyList()))
        assertThrows(IllegalArgumentException::class.java){WilmaOrderHost.decode(bytes.copyOf().also {it[10]=(it[10].toInt() xor 1).toByte()})}
        assertThrows(IllegalArgumentException::class.java){WilmaOrderState(s.id,s.language,List(7){WilmaContent.days[0]},s.steps)}
        val incompatible=bytes.copyOfRange(0,bytes.size-32).also {it[3]=2}
        assertThrows(IllegalArgumentException::class.java){WilmaOrderHost.decode(incompatible+java.security.MessageDigest.getInstance("SHA-256").digest(incompatible))}
    }
}
