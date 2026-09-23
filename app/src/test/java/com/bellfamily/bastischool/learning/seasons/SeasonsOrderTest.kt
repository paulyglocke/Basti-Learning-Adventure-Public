package com.bellfamily.bastischool.learning.seasons

import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.content.SeasonIds
import com.bellfamily.bastischool.learning.session.*
import com.bellfamily.bastischool.learning.progress.*
import org.junit.Assert.*
import org.junit.Test

class SeasonsOrderTest {
    private fun start()=SeasonsOrder.start(SessionId("week"),42,ContentLanguage.ENGLISH)
    @Test fun fourUniqueSeasonsScrambleDeterministically() {
        for(seed in 0L..50L) {
            val a=SeasonsOrder.start(SessionId("week"),seed,ContentLanguage.ENGLISH)
            val b=SeasonsOrder.start(SessionId("week"),seed,ContentLanguage.GERMAN)
            assertEquals(a.choices,b.choices);assertEquals(4,a.choices.size);assertEquals(SeasonIds.canonicalOrder.toSet(),a.choices.toSet())
            assertNotEquals(SeasonIds.canonicalOrder,a.choices)
        }
    }
    @Test fun placementLocksEachStepAndCompletesOnceWithFourEvidenceRecords() {
        var s=start();val events=mutableListOf<ProgressEvent>()
        SeasonIds.canonicalOrder.forEach {day ->
            val action=SeasonsOrderAction.Place(s.nextAttempt!!,day)
            val next=SeasonsOrder.reduce(s,action);s=next.state;events+=next.events
            val repeated=SeasonsOrder.reduce(s,action);assertSame(s,repeated.state);assertTrue(repeated.events.isEmpty())
        }
        assertTrue(s.completed);assertEquals(SeasonIds.canonicalOrder,s.placed)
        assertEquals(4,events.filterIsInstance<AttemptEvent>().size)
        assertEquals(4,events.filterIsInstance<CompletionEvent>().single().tasks.size)
    }
    @Test fun wrongPlacementPreservesSequenceAndReplayHelpAreNotAttempts() {
        var s=SeasonsOrder.reduce(start(),SeasonsOrderAction.Place(start().nextAttempt!!,SeasonIds.canonicalOrder[0])).state
        val task=s.task
        s=SeasonsOrder.reduce(s,SeasonsOrderAction.Replay(task)).state
        s=SeasonsOrder.reduce(s,SeasonsOrderAction.Help(task)).state
        assertEquals(0,s.current.attempts);assertTrue(s.current.support.hint);assertEquals(1,s.current.support.replays)
        val wrong=SeasonsOrder.reduce(s,SeasonsOrderAction.Place(s.nextAttempt!!,SeasonIds.canonicalOrder[3]));s=wrong.state
        assertEquals(1,s.index);assertEquals(AnswerState.RETRY_AVAILABLE,s.current.answer)
        assertEquals(AttemptOutcome.INCORRECT,(wrong.events.single() as AttemptEvent).outcome)
        s=SeasonsOrder.reduce(s,SeasonsOrderAction.Retry(AttemptId(task,1))).state
        s=SeasonsOrder.reduce(s,SeasonsOrderAction.Language(ContentLanguage.GERMAN)).state
        s=SeasonsOrder.reduce(s,SeasonsOrderAction.Place(s.nextAttempt!!,SeasonIds.canonicalOrder[1])).state
        assertEquals(2,s.index);assertEquals(2,s.steps[1].attempts);assertEquals(1,s.steps[1].retries)
        assertTrue(s.steps[1].support.hint);assertEquals(ContentLanguage.GERMAN,s.language)
    }
    @Test fun partialOrderingCheckpointPreservesActualScrambleAndRejectsCorruption() {
        var s=start()
        repeat(3){s=SeasonsOrder.reduce(s,SeasonsOrderAction.Place(s.nextAttempt!!,SeasonIds.canonicalOrder[it])).state}
        val bytes=SeasonsOrderHost.encode(s,emptyList());val restored=SeasonsOrderHost.decode(bytes).first
        assertEquals(s.choices,restored.choices);assertEquals(s.steps,restored.steps);assertEquals(3,restored.index)
        assertArrayEquals(bytes,SeasonsOrderHost.encode(restored,emptyList()))
        assertThrows(IllegalArgumentException::class.java){SeasonsOrderHost.decode(bytes.copyOf().also {it[10]=(it[10].toInt() xor 1).toByte()})}
        assertThrows(IllegalArgumentException::class.java){SeasonsOrderState(s.id,s.language,List(4){SeasonIds.canonicalOrder[0]},s.steps)}
        val incompatible=bytes.copyOfRange(0,bytes.size-32).also {it[3]=2}
        assertThrows(IllegalArgumentException::class.java){SeasonsOrderHost.decode(incompatible+java.security.MessageDigest.getInstance("SHA-256").digest(incompatible))}
    }
}
