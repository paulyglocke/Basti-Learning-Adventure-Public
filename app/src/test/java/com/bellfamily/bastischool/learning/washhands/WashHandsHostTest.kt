package com.bellfamily.bastischool.learning.washhands

import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.session.*
import com.bellfamily.bastischool.learning.sequencing.*
import com.bellfamily.bastischool.learning.progress.*
import org.junit.Assert.*
import org.junit.Test
import java.io.IOException

class WashHandsHostTest {
    private class Disk:ProgressStorage {
        var bytes:ByteArray?=null;var fail=false
        override fun <T> access(block:(ProgressTransaction)->T)=block(object:ProgressTransaction {
            override fun read()=bytes?.clone()
            override fun replace(bytes:ByteArray){if(fail)throw IOException("disk");this@Disk.bytes=bytes.clone()}
        })
    }
    private val events=mutableListOf<ProgressEvent>();private var failAt=Int.MAX_VALUE
    private val progress=object:ProgressRepository {
        override fun append(event:ProgressEvent):ProgressWriteResult {
            if(events.size>=failAt)return ProgressWriteResult.Failed(ProgressFailure.IO)
            val old=events.indexOfFirst {it.id==event.id};if(old<0)events+=event
            val i=if(old<0)events.lastIndex else old
            return ProgressWriteResult.Saved(StoredProgressEvent(i+1L,0,events[i]),old<0)
        }
        override fun read(query:ProgressQuery)=ProgressReadResult.Events(emptyList())
    }
    private fun open(d:Disk)=WashHandsHost(d,progress).also {assertNull(it.open(SessionId("wash"),7,ContentLanguage.ENGLISH))}
    private fun act(h:WashHandsHost,op:SequenceOperation)=h.state!!.let {h.dispatch(SequenceAction(it.id,it.revision,op))}
    @Test fun exactPartialRestoreRetainsScrambleRemovalHelpAndLanguage() {
        val d=Disk();val h=open(d);act(h,SequenceOperation.Append(WashHands.order.last()));act(h,SequenceOperation.Hint);act(h,SequenceOperation.Replay)
        act(h,SequenceOperation.Language(ContentLanguage.GERMAN));val before=d.bytes!!.clone()
        val restored=open(d);assertArrayEquals(before,d.bytes);assertEquals(h.state!!.constructed,restored.state!!.constructed)
        assertEquals(h.state!!.support,restored.state!!.support);assertEquals(h.state!!.presentation,restored.state!!.presentation)
        assertTrue(events.isEmpty());restored.listen(WashHands.order[0]);assertArrayEquals(before,d.bytes)
    }
    @Test fun wrongCompleteSequenceRestoresEditableWithAttemptEvidence() {
        val d=Disk();val h=open(d);WashHands.order.reversed().forEach {act(h,SequenceOperation.Append(it))};act(h,SequenceOperation.Check)
        val restored=open(d);assertFalse(restored.state!!.completed);assertEquals(4,events.size)
        act(restored,SequenceOperation.Remove(WashHands.order[0]));assertEquals(3,restored.state!!.constructed.size)
    }
    @Test fun pendingPartialDeliveryRestoresDeduplicatesAndAgainStartsFresh() {
        val d=Disk();val h=open(d);WashHands.order.forEach {act(h,SequenceOperation.Append(it))};failAt=2;act(h,SequenceOperation.Check)
        assertTrue(h.failure);assertTrue(h.state!!.completed);assertEquals(2,events.size)
        val restored=open(d);failAt=Int.MAX_VALUE;restored.retryWrites();restored.retryWrites()
        assertEquals(5,events.size);assertTrue(restored.state!!.acknowledged);assertEquals(1,events.filterIsInstance<CompletionEvent>().size)
        restored.again(SessionId("new-wash"),8,ContentLanguage.GERMAN);assertFalse(restored.state!!.completed);assertEquals(0,restored.state!!.attempts)
    }
    @Test fun corruptAndFailedWritesNeverReplaceAcceptedEvidence() {
        val d=Disk();val h=open(d);val original=d.bytes!!.clone();d.fail=true
        assertThrows(IOException::class.java){act(h,SequenceOperation.Append(WashHands.order[0]))}
        assertArrayEquals(original,d.bytes);assertTrue(h.state!!.constructed.isEmpty());d.fail=false
        d.bytes=original.clone().also {it[30]=(it[30].toInt() xor 1).toByte()};val corrupted=d.bytes!!.clone()
        assertThrows(IllegalArgumentException::class.java){open(d)};assertArrayEquals(corrupted,d.bytes)
    }
    @Test fun staleWriterCannotOverwriteNewerPuzzle() {
        val d=Disk();val a=open(d);val b=open(d);act(a,SequenceOperation.Append(WashHands.order[0]));val saved=d.bytes!!.clone()
        assertThrows(IOException::class.java){act(b,SequenceOperation.Append(WashHands.order[1]))};assertArrayEquals(saved,d.bytes)
    }
}
