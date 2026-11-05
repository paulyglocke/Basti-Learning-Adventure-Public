package com.bellfamily.bastischool.learning.vocabulary

import com.bellfamily.bastischool.learning.models.ContentLanguage
import com.bellfamily.bastischool.learning.progress.*
import com.bellfamily.bastischool.learning.session.*
import java.io.IOException
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class VocabularyJournalRecoveryTest {
    @get:Rule val temp=TemporaryFolder()
    private fun disk()=AtomicProgressStorage(temp.newFolder(),JvmAtomicCommit)

    @Test fun legacyAnsweredRoundsRecoverSilentlyWithoutChangingOriginalOrDuplicatingProgress() {
        for(phase in listOf(VocabularyPhase.FIND,VocabularyPhase.NAME)) {
            val legacy=disk();val current=disk();val progress=ProgressFixtures.repository(temp.newFolder())
            val original=VocabularyContent.host(phase,legacy,progress)
            original.open(SessionId("legacy-${phase.name.lowercase()}"),RoundLength.TEN,73,ContentLanguage.GERMAN)
            original.dispatch(SessionAction.Hint(original.state!!.task.id))
            original.dispatch(SessionAction.Answer(original.state!!.nextAttempt!!,original.state!!.task.question.correct))
            val oldBytes=legacy.access {it.read()!!};val snapshot=SessionCheckpoint.encode(original.state!!)
            val recovered=VocabularyContent.host(phase,VocabularyJournalRecovery(current,legacy),progress)
            assertTrue(recovered.open(SessionId("unused"),RoundLength.FIVE,0,ContentLanguage.ENGLISH).isEmpty())
            assertArrayEquals(snapshot,SessionCheckpoint.encode(recovered.state!!))
            assertArrayEquals(oldBytes,legacy.access {it.read()})
            assertNotNull(current.access {it.read()})
            recovered.retryWrites()
            assertEquals(1,ProgressFixtures.records(progress).size)
        }
    }

    @Test fun currentJournalWinsEvenWhenLegacyHasDifferentSavedWork() {
        val current=disk();val legacy=disk();val progress=ProgressFixtures.repository(temp.newFolder())
        val phase=VocabularyPhase.FIND
        VocabularyContent.host(phase,legacy,progress).open(SessionId("old"),RoundLength.FIVE,1,ContentLanguage.ENGLISH)
        val expected=VocabularyContent.host(phase,current,progress)
        expected.open(SessionId("newer"),RoundLength.TEN,2,ContentLanguage.GERMAN)
        val bytes=current.access {it.read()!!}
        val recovered=VocabularyContent.host(phase,VocabularyJournalRecovery(current,legacy),progress)
        recovered.open(SessionId("unused"),RoundLength.FIVE,3,ContentLanguage.ENGLISH)
        assertArrayEquals(SessionCheckpoint.encode(expected.state!!),SessionCheckpoint.encode(recovered.state!!))
        assertArrayEquals(bytes,current.access {it.read()})
    }

    @Test fun corruptRecordsArePreservedRatherThanCopiedOrReplaced() {
        val legacy=disk();val current=disk();val progress=ProgressFixtures.repository(temp.newFolder())
        val bad=byteArrayOf(1,2,3);legacy.access {it.replace(bad)}
        val host=VocabularyContent.host(VocabularyPhase.FIND,VocabularyJournalRecovery(current,legacy),progress)
        assertThrows(IllegalArgumentException::class.java) {host.open(SessionId("unused"),RoundLength.FIVE,1,ContentLanguage.ENGLISH)}
        assertNull(current.access {it.read()});assertArrayEquals(bad,legacy.access {it.read()})
        // A corrupt current record also wins: fallback must not silently discard it.
        current.access {it.replace(bad)}
        assertThrows(IllegalArgumentException::class.java) {host.open(SessionId("unused"),RoundLength.FIVE,1,ContentLanguage.ENGLISH)}
        assertArrayEquals(bad,current.access {it.read()})
    }

    @Test fun uncertainRecoveryCommitCanBeRetriedWithoutTouchingLegacy() {
        val legacy=disk();val current=disk();val progress=ProgressFixtures.repository(temp.newFolder())
        VocabularyContent.host(VocabularyPhase.NAME,legacy,progress).open(SessionId("legacy"),RoundLength.TEN,9,ContentLanguage.ENGLISH)
        val original=legacy.access {it.read()!!};var fail=true
        val uncertain=object:ProgressStorage {
            override fun <T> access(block:(ProgressTransaction)->T):T=current.access { tx ->
                block(object:ProgressTransaction {
                    override fun read()=tx.read()
                    override fun replace(bytes:ByteArray) {tx.replace(bytes);if(fail)throw IOException("after commit")}
                })
            }
        }
        val host=VocabularyContent.host(VocabularyPhase.NAME,VocabularyJournalRecovery(uncertain,legacy),progress)
        host.open(SessionId("unused"),RoundLength.FIVE,1,ContentLanguage.GERMAN)
        assertTrue(host.failure);fail=false;host.retryWrites();assertFalse(host.failure)
        assertEquals("legacy",host.state!!.plan.id.value)
        assertArrayEquals(original,legacy.access {it.read()})
    }
}
