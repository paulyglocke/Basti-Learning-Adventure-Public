package com.bellfamily.bastischool.learning.washhands

import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.session.*
import com.bellfamily.bastischool.learning.sequencing.*
import com.bellfamily.bastischool.learning.progress.*
import org.junit.Assert.*
import org.junit.Test

class WashHandsTest {
    @Test fun exactFourBilingualStepsAndIndependentAudio() {
        assertEquals(listOf("Turn on the water","Wet your hands","Wash with soap","Rinse your hands"),WashHands.steps.map {it.text.display[ContentLanguage.ENGLISH]})
        assertEquals(listOf("Wasser anmachen","Hände nass machen","Mit Seife waschen","Hände abspülen"),WashHands.steps.map {it.text.display[ContentLanguage.GERMAN]})
        assertEquals(4,WashHands.order.toSet().size)
        WashHands.steps.forEach {assertEquals(it.text,WashHands.listen(it.id).text)}
    }
    @Test fun onlyCheckProducesEvidenceAndCompletionAndLanguageRetainsIdentity() {
        var s=WashHands.start(SessionId("wash"),6,ContentLanguage.ENGLISH)
        WashHands.order.forEach {val t=WashHands.reduce(s,SequenceAction(s.id,s.revision,SequenceOperation.Append(it)));assertTrue(t.events.isEmpty());s=t.state}
        val de=WashHands.reduce(s,SequenceAction(s.id,s.revision,SequenceOperation.Language(ContentLanguage.GERMAN)))
        assertEquals(s.presentation,de.state.presentation);assertEquals(s.constructed,de.state.constructed);s=de.state
        val t=WashHands.reduce(s,SequenceAction(s.id,s.revision,SequenceOperation.Check))
        assertTrue(t.state.completed);assertEquals(4,t.events.filterIsInstance<AttemptEvent>().size)
        assertEquals(1,t.events.filterIsInstance<CompletionEvent>().size)
        assertTrue(t.events.filterIsInstance<AttemptEvent>().all {it.support.independent && it.outcome==AttemptOutcome.CORRECT})
    }
    @Test fun replayAndHintNeverRecordAttemptsAndWrongCheckRecordsActualPositions() {
        var s=WashHands.start(SessionId("wash"),8,ContentLanguage.ENGLISH)
        for(op in listOf(SequenceOperation.Replay,SequenceOperation.Hint)){val t=WashHands.reduce(s,SequenceAction(s.id,s.revision,op));assertTrue(t.events.isEmpty());s=t.state}
        WashHands.order.reversed().forEach {s=WashHands.reduce(s,SequenceAction(s.id,s.revision,SequenceOperation.Append(it))).state}
        val t=WashHands.reduce(s,SequenceAction(s.id,s.revision,SequenceOperation.Check))
        assertEquals(WashHands.order.reversed(),t.events.filterIsInstance<AttemptEvent>().map {it.choice})
        assertTrue(t.events.all {it is AttemptEvent && !it.support.independent && it.outcome==AttemptOutcome.INCORRECT})
        assertFalse(t.state.completed)
    }
}
