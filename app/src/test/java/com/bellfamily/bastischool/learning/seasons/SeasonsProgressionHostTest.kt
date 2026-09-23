package com.bellfamily.bastischool.learning.seasons

import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.content.SeasonIds
import com.bellfamily.bastischool.learning.session.*
import com.bellfamily.bastischool.learning.progress.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.IOException

class SeasonsProgressionHostTest {
    @get:Rule val temp=TemporaryFolder()
    private fun storage()=AtomicProgressStorage(temp.newFolder(),JvmAtomicCommit)
    @Test fun findAndRelationAttemptsSupportAndCompletionPersistAcrossSilentRestore() {
        for(phase in listOf(SeasonsPhase.NEXT,SeasonsPhase.BEFORE)) for(round in RoundLength.entries) {
            val disk=storage();val folder=temp.newFolder();val repository=ProgressFixtures.repository(folder)
            fun open()=SeasonsCycle.host(phase,disk,repository).also {it.open(SessionId("${phase.name}-${round.name}"),round,42,ContentLanguage.ENGLISH)}
            var h=open();val original=SessionCheckpoint.encode(h.state!!)
            val silent=SeasonsCycle.host(phase,disk,repository)
            assertTrue(silent.open(SessionId("other"),RoundLength.TEN,99,ContentLanguage.GERMAN).isEmpty())
            assertArrayEquals(original,SessionCheckpoint.encode(silent.state!!))
            h.dispatch(SessionAction.Replay(h.state!!.task.id));h.dispatch(SessionAction.Hint(h.state!!.task.id))
            assertEquals(0,h.state!!.current.attempts)
            var s=h.state!!
            h.dispatch(SessionAction.Answer(s.nextAttempt!!,s.task.question.choices.first {it!=s.task.question.correct}))
            h=open();s=h.state!!
            h.dispatch(SessionAction.Retry(AttemptId(s.task.id,s.current.attempts)))
            h.dispatch(SessionAction.Language(ContentLanguage.GERMAN))
            repeat(round.count) {
                s=h.state!!;val answer=SessionAction.Answer(s.nextAttempt!!,s.task.question.correct)
                h.dispatch(answer);h.dispatch(answer)
                val answered=SessionCheckpoint.encode(h.state!!);h=open();assertArrayEquals(answered,SessionCheckpoint.encode(h.state!!))
                assertEquals(it+1,h.state!!.score)
                h.dispatch(SessionAction.Next(h.state!!.task.id));h=open()
            }
            assertEquals(CompletionState.ACKNOWLEDGED,h.state!!.completion)
            h=open();assertEquals(SessionPhase.COMPLETED,h.state!!.phase)
            val records=ProgressFixtures.records(ProgressFixtures.repository(folder)).map {it.event}
            assertEquals(round.count+1,records.filterIsInstance<AttemptEvent>().size)
            assertEquals(1,records.filterIsInstance<CompletionEvent>().size)
            val attempts=records.filterIsInstance<AttemptEvent>()
            assertTrue(attempts.all { it.evidence.skill.value.startsWith(if(phase==SeasonsPhase.NEXT) "skill.seasons.next" else "skill.seasons.before") })
            assertTrue(attempts.all { it.origin.activity==SeasonsCycle.activity(phase) })
            assertTrue((records.first() as AttemptEvent).support.hint)
            assertEquals(AttemptOutcome.INCORRECT,(records.first() as AttemptEvent).outcome)
            assertEquals(ContentLanguage.GERMAN,(records[1] as AttemptEvent).language)
        }
    }
    @Test fun failedQuizEffectRemainsRetryableWithItsOriginalIdentity() {
        for(phase in listOf(SeasonsPhase.NEXT,SeasonsPhase.BEFORE)) {
            val disk=storage();val real=ProgressFixtures.repository(temp.newFolder());var fail=true
            val failing=object:ProgressRepository {
                override fun append(event:ProgressEvent)=if(fail)ProgressWriteResult.Failed(ProgressFailure.IO) else real.append(event)
                override fun read(query:ProgressQuery)=real.read(query)
            }
            val h=SeasonsCycle.host(phase,disk,failing);h.open(SessionId(phase.name),RoundLength.FIVE,1,ContentLanguage.ENGLISH)
            val s=h.state!!;h.dispatch(SessionAction.Answer(s.nextAttempt!!,s.task.question.correct))
            assertTrue(h.failure && h.hasPending)
            fail=false
            val restored=SeasonsCycle.host(phase,disk,failing)
            assertTrue(restored.open(SessionId("unused"),RoundLength.TEN,2,ContentLanguage.GERMAN).isEmpty())
            restored.retryWrites()
            val events=ProgressFixtures.records(real);assertEquals(1,events.size)
            assertEquals(ProgressEventId.attempt(s.nextAttempt!!),events.single().event.id)
        }
    }
    @Test fun orderingPendingFinalPlacementAndCompletionSurviveRecreation() {
        val disk=storage();val folder=temp.newFolder();val real=ProgressFixtures.repository(folder);var fail=false
        val failing=object:ProgressRepository {
            override fun append(event:ProgressEvent)=if(fail)ProgressWriteResult.Failed(ProgressFailure.IO) else real.append(event)
            override fun read(query:ProgressQuery)=real.read(query)
        }
        fun open()=SeasonsOrderHost(disk,failing).also {it.open(SessionId("order"),42,ContentLanguage.GERMAN)}
        var h=open();h.dispatch(SeasonsOrderAction.Help(h.state!!.task));h.dispatch(SeasonsOrderAction.Replay(h.state!!.task))
        h.dispatch(SeasonsOrderAction.Place(h.state!!.nextAttempt!!,SeasonIds.canonicalOrder[3]))
        h=open();h.dispatch(SeasonsOrderAction.Retry(AttemptId(h.state!!.task,1)))
        repeat(4){i ->
            if(i==3)fail=true
            val action=SeasonsOrderAction.Place(h.state!!.nextAttempt!!,SeasonIds.canonicalOrder[i])
            h.dispatch(action);h.dispatch(action)
            if(i<3){val before=h.state!!;h=open();assertEquals(before.choices,h.state!!.choices);assertEquals(before.steps,h.state!!.steps)}
        }
        assertTrue(h.state!!.completed && !h.state!!.acknowledged && h.hasPending && h.failure)
        fail=false;h=open();assertTrue(h.state!!.acknowledged);h.retryWrites();open()
        val silent=SeasonsOrderHost(disk,failing)
        assertNull(silent.open(SessionId("different"),999,ContentLanguage.ENGLISH))
        assertEquals(h.state!!.choices,silent.state!!.choices)
        assertEquals(ContentLanguage.GERMAN,silent.state!!.language)
        val records=ProgressFixtures.records(ProgressFixtures.repository(folder)).map {it.event}
        assertEquals(5,records.filterIsInstance<AttemptEvent>().size)
        val completion=records.filterIsInstance<CompletionEvent>().single()
        assertEquals(4,completion.tasks.size);assertEquals(2,completion.tasks.first().attempts)
        assertTrue(completion.tasks.first().support.hint);assertEquals(1,completion.tasks.first().support.replays)
    }
    @Test fun uncertainOrderingJournalWriteReloadsBeforeRetryWithoutLosingEvent() {
        val disk=storage();var fail=false;val real=ProgressFixtures.repository(temp.newFolder())
        val uncertain=object:ProgressStorage {
            override fun <T> access(block:(ProgressTransaction)->T):T=disk.access {tx->block(object:ProgressTransaction {
                override fun read()=tx.read()
                override fun replace(bytes:ByteArray){tx.replace(bytes);if(fail)throw IOException("after commit")}
            })}
        }
        val h=SeasonsOrderHost(uncertain,real);h.open(SessionId("order"),42,ContentLanguage.ENGLISH)
        fail=true;assertThrows(IOException::class.java){h.dispatch(SeasonsOrderAction.Place(h.state!!.nextAttempt!!,SeasonIds.canonicalOrder[0]))}
        assertEquals(0,h.state!!.index);assertTrue(h.failure)
        fail=false;h.retryWrites();assertEquals(1,h.state!!.index);assertEquals(1,ProgressFixtures.records(real).size)
    }
    @Test fun staleOrderingHostCannotOverwriteAndCorruptionIsPreserved() {
        val disk=storage();val real=ProgressFixtures.repository(temp.newFolder())
        fun open()=SeasonsOrderHost(disk,real).also {it.open(SessionId("order"),42,ContentLanguage.ENGLISH)}
        val a=open();val stale=open()
        a.dispatch(SeasonsOrderAction.Place(a.state!!.nextAttempt!!,SeasonIds.canonicalOrder[0]))
        assertThrows(IOException::class.java){stale.dispatch(SeasonsOrderAction.Place(stale.state!!.nextAttempt!!,SeasonIds.canonicalOrder[0]))}
        stale.retryWrites();assertEquals(1,stale.state!!.index)
        disk.access {it.replace(byteArrayOf(1,2,3))}
        assertThrows(IllegalArgumentException::class.java){open()}
        assertArrayEquals(byteArrayOf(1,2,3),disk.access {it.read()})
    }
}
