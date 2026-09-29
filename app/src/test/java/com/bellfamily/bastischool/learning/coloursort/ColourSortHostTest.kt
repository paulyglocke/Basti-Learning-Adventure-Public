package com.bellfamily.bastischool.learning.coloursort

import com.bellfamily.bastischool.learning.sorting.*
import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.session.*
import com.bellfamily.bastischool.learning.progress.*
import org.junit.Assert.*
import org.junit.Test
import java.io.IOException

class ColourSortHostTest {
    private class Disk:ProgressStorage {
        var bytes:ByteArray?=null;var fail=false
        override fun <T> access(block:(ProgressTransaction)->T):T=block(object:ProgressTransaction {
            override fun read()=bytes?.clone()
            override fun replace(bytes:ByteArray){if(fail)throw IOException("disk");this@Disk.bytes=bytes.clone()}
        })
    }
    private val records=mutableListOf<ProgressEvent>();private var failProgress=false
    private val progress=object:ProgressRepository {
        override fun append(event:ProgressEvent):ProgressWriteResult {
            if(failProgress)return ProgressWriteResult.Failed(ProgressFailure.IO)
            val fresh=event !in records;if(fresh)records+=event
            return ProgressWriteResult.Saved(StoredProgressEvent(records.indexOf(event)+1L,0,event),fresh)
        }
        override fun read(query:ProgressQuery)=ProgressReadResult.Events(emptyList())
    }
    private fun open(d:Disk)=ColourSortHost(d,progress).also {it.open(SessionId("sort"),10,ContentLanguage.ENGLISH)}
    private fun place(h:ColourSortHost,item:ColourSortObject,category:ContentId=item.category) {
        h.dispatch(SortAction.Select(item.id));val s=h.state!!
        h.dispatch(SortAction.Place(s.id,item.id,category,s.placement(item.id).attempts+1))
    }
    @Test fun partialStateSelectionOrderSupportAndLanguageRestoreExactlyAndSilently() {
        val d=Disk();val h=open(d);place(h,ColourSort.objects[0]);h.dispatch(SortAction.Select(ColourSort.objects[1].id))
        h.dispatch(SortAction.Hint);h.dispatch(SortAction.Replay);h.dispatch(SortAction.Language(ContentLanguage.GERMAN))
        val before=ColourSortHost.encode(h.state!!,emptyList())
        val restored=ColourSortHost(d,progress)
        assertNull(restored.open(SessionId("ignored"),999,ContentLanguage.ENGLISH))
        assertArrayEquals(before,ColourSortHost.encode(restored.state!!,emptyList()))
        assertEquals(1,records.size)
    }
    @Test fun pendingPlacementSurvivesFailureAndRetriesWithoutDuplicateEvidence() {
        val d=Disk();val h=open(d);failProgress=true;place(h,ColourSort.objects[0])
        assertTrue(h.failure);assertTrue(h.state!!.placements[0].placed)
        val restored=open(d);assertTrue(restored.failure);failProgress=false;restored.retryWrites();restored.retryWrites()
        assertFalse(restored.failure);assertEquals(1,records.size)
    }
    @Test fun completionOnlyAfterAllFourPersistsAndAgainCreatesNewIdentity() {
        val d=Disk();val h=open(d)
        ColourSort.objects.forEach {place(h,it)}
        assertTrue(h.state!!.completed);assertTrue(h.state!!.acknowledged)
        assertEquals(4,records.filterIsInstance<AttemptEvent>().size);assertEquals(1,records.filterIsInstance<CompletionEvent>().size)
        val restored=open(d);assertTrue(restored.state!!.completed);assertEquals(5,records.size)
        restored.again(SessionId("again"),11,ContentLanguage.GERMAN)
        assertFalse(restored.state!!.completed);assertTrue(restored.state!!.placements.all {it.attempts==0})
    }
    @Test fun corruptJournalAndFailedWritesPreserveEvidence() {
        val d=Disk();val h=open(d);val before=d.bytes!!.clone();d.fail=true
        try{h.dispatch(SortAction.Select(ColourSort.objects[0].id));fail()}catch(_:IOException){}
        assertArrayEquals(before,d.bytes);assertNull(h.state!!.selected)
        d.fail=false;d.bytes=before.clone().also {it[20]=(it[20].toInt() xor 1).toByte()};val corrupt=d.bytes!!.clone()
        try{open(d);fail()}catch(_:IllegalArgumentException){}
        assertArrayEquals(corrupt,d.bytes)
    }
    @Test fun staleOwnerCannotOverwriteAcceptedPlacement() {
        val d=Disk();val h=open(d);val stale=open(d);place(h,ColourSort.objects[0]);val bytes=d.bytes!!.clone()
        try{stale.dispatch(SortAction.Select(ColourSort.objects[1].id));fail()}catch(_:IOException){}
        assertArrayEquals(bytes,d.bytes)
    }
}
