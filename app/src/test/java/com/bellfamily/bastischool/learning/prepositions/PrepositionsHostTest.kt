package com.bellfamily.bastischool.learning.prepositions

import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.progress.*
import com.bellfamily.bastischool.learning.session.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.IOException

class PrepositionsHostTest {
    @get:Rule val temp = TemporaryFolder()
    private val events = mutableListOf<ProgressEvent>()
    private var failProgress = false
    private val repository = object : ProgressRepository {
        override fun append(event: ProgressEvent): ProgressWriteResult {
            if (failProgress) return ProgressWriteResult.Failed(ProgressFailure.IO)
            val old = events.find { it.id == event.id }
            if (old != null && old != event) return ProgressWriteResult.Failed(ProgressFailure.CONFLICT)
            if (old == null) events += event
            return ProgressWriteResult.Saved(StoredProgressEvent(events.indexOf(event) + 1L, 0, event), old == null)
        }
        override fun read(query: ProgressQuery) = ProgressReadResult.Events(emptyList())
    }
    private fun storage() = AtomicProgressStorage(temp.newFolder(), JvmAtomicCommit)
    private fun open(storage: ProgressStorage, round: RoundLength = RoundLength.FIVE) = PrepositionsHost(storage,repository).also {
        it.open(SessionId("round"),round,42,ContentLanguage.ENGLISH)
    }
    private fun answer(host: PrepositionsHost, correct: Boolean = true) {
        val s=host.state!!;host.dispatch(SessionAction.Answer(s.nextAttempt!!,if(correct)s.task.question.correct else s.task.question.choices.first { it!=s.task.question.correct }))
    }
    @Test fun correctScoresOnceAndRestoresExactAnsweredQuestion() {
        val disk=storage();val host=open(disk);val original=host.state!!
        val action=SessionAction.Answer(original.nextAttempt!!,original.task.question.correct)
        host.dispatch(action);host.dispatch(action)
        val restored=open(disk).state!!
        assertEquals(1,restored.score);assertEquals(1,events.size)
        assertEquals(original.task.id,restored.task.id);assertEquals(original.task.question.choices,restored.task.question.choices)
        assertEquals(AnswerState.CORRECT,restored.current.answer)
    }
    @Test fun wrongReplayHintRetryAndLanguageMetadataSurvive() {
        val disk=storage();val host=open(disk)
        host.dispatch(SessionAction.Replay(host.state!!.task.id));host.dispatch(SessionAction.Hint(host.state!!.task.id))
        assertEquals(0,host.state!!.current.attempts)
        answer(host,false)
        assertEquals(AnswerState.RETRY_AVAILABLE,host.state!!.current.answer)
        host.dispatch(SessionAction.Retry(AttemptId(host.state!!.task.id,1)))
        val original=host.state!!.task
        host.dispatch(SessionAction.Language(ContentLanguage.GERMAN));answer(host)
        val restored=open(disk).state!!
        assertEquals(original.id,restored.task.id);assertEquals(original.question.choices,restored.task.question.choices)
        assertEquals(2,restored.current.attempts);assertEquals(1,restored.current.retries)
        assertTrue(restored.current.support.hint);assertEquals(1,restored.current.support.replays)
        val attempts=events.filterIsInstance<AttemptEvent>()
        assertEquals(listOf(AttemptOutcome.INCORRECT,AttemptOutcome.CORRECT),attempts.map { it.outcome })
        assertEquals(listOf(ContentLanguage.ENGLISH,ContentLanguage.GERMAN),attempts.map { it.language })
    }
    @Test fun unansweredRestoreIsSilentAndDoesNotRegenerate() {
        val disk=storage();val host=open(disk)
        val other=PrepositionsHost(disk,repository)
        assertTrue(other.open(SessionId("different"),RoundLength.TEN,999,ContentLanguage.GERMAN).isEmpty())
        assertArrayEquals(SessionCheckpoint.encode(host.state!!),SessionCheckpoint.encode(other.state!!))
    }
    @Test fun fiveAndTenCompleteOnceAndResumeAtSameIndexScore() {
        for(round in RoundLength.entries) {
            events.clear();val disk=storage();var host=open(disk,round)
            repeat(round.count) { index ->
                assertEquals(index,host.state!!.index);assertEquals(index,host.state!!.score)
                answer(host);val action=SessionAction.Next(host.state!!.task.id)
                host.dispatch(action);host.dispatch(action);host=open(disk,round)
            }
            assertEquals(SessionPhase.COMPLETED,host.state!!.phase)
            assertEquals(CompletionState.ACKNOWLEDGED,host.state!!.completion)
            assertEquals(round.count,host.state!!.score)
            assertEquals(1,events.filterIsInstance<CompletionEvent>().size)
        }
    }
    @Test fun failedAttemptDeliveryIsDurableBeforePublicationAndRetriedAfterRestart() {
        val disk=storage();val host=open(disk);failProgress=true;answer(host)
        assertTrue(host.failure);assertTrue(host.hasPending);assertEquals(1,host.state!!.score)
        host.dispatch(SessionAction.Next(host.state!!.task.id));assertEquals(0,host.state!!.index)
        failProgress=false;val restored=open(disk)
        assertFalse(restored.failure);assertEquals(1,events.size);assertEquals(1,restored.state!!.score)
        restored.retryWrites();assertEquals(1,events.size)
    }
    @Test fun failedCompletionSurvivesRestartWithSameDedupeKey() {
        val disk=storage();val host=open(disk)
        repeat(5) { answer(host);if(it==4)failProgress=true;host.dispatch(SessionAction.Next(host.state!!.task.id)) }
        assertEquals(CompletionState.PENDING,host.state!!.completion)
        failProgress=false;val restored=open(disk)
        assertEquals(CompletionState.ACKNOWLEDGED,restored.state!!.completion)
        open(disk);assertEquals(1,events.filterIsInstance<CompletionEvent>().size)
    }
    @Test fun uncertainJournalWriteRequiresReloadAndCannotLoseAcceptedEffect() {
        val disk=storage();var fail=false
        val uncertain=object:ProgressStorage {
            override fun <T> access(block:(ProgressTransaction)->T):T = disk.access { tx -> block(object:ProgressTransaction {
                override fun read()=tx.read()
                override fun replace(bytes:ByteArray) {tx.replace(bytes);if(fail)throw IOException("after rename")}
            }) }
        }
        val host=open(uncertain);fail=true
        assertThrows(IOException::class.java) {answer(host)}
        assertTrue(host.failure);assertEquals(0,host.state!!.score)
        fail=false;host.retryWrites()
        assertEquals(1,host.state!!.score);assertEquals(1,events.size)
    }
    @Test fun realProgressRepositoryAndJournalSurviveRecreationTogether() {
        val journal=storage();val progressDirectory=temp.newFolder()
        fun host()=PrepositionsHost(journal,ProgressFixtures.repository(progressDirectory)).also {
            it.open(SessionId("durable"),RoundLength.FIVE,42,ContentLanguage.GERMAN)
        }
        var h=host()
        h.dispatch(SessionAction.Hint(h.state!!.task.id));answer(h,false)
        h=host();h.dispatch(SessionAction.Retry(AttemptId(h.state!!.task.id,1)));answer(h)
        h=host()
        val records=ProgressFixtures.records(ProgressFixtures.repository(progressDirectory)).map {it.event as AttemptEvent}
        assertEquals(2,records.size);assertEquals(listOf(1,2),records.map {it.attempt.number})
        assertTrue(records.all {it.support.hint && it.language==ContentLanguage.GERMAN})
        assertEquals(1,h.state!!.score)
    }
    @Test fun failureBeforeJournalCommitDoesNotAcceptOrPersistAnswer() {
        val disk=storage();var fail=false
        val broken=object:ProgressStorage {
            override fun <T> access(block:(ProgressTransaction)->T):T=disk.access {tx->block(object:ProgressTransaction {
                override fun read()=tx.read()
                override fun replace(bytes:ByteArray) {if(fail)throw IOException("before commit");tx.replace(bytes)}
            })}
        }
        val host=open(broken);fail=true
        assertThrows(IOException::class.java) {answer(host)}
        assertEquals(0,host.state!!.score);assertTrue(events.isEmpty())
        fail=false;host.retryWrites();assertEquals(AnswerState.UNANSWERED,host.state!!.current.answer)
        answer(host);assertEquals(1,events.size)
    }
    @Test fun staleHostCannotOverwriteANewerDurableQuestion() {
        val disk=storage();val first=open(disk);val stale=open(disk)
        answer(first);first.dispatch(SessionAction.Next(first.state!!.task.id))
        assertThrows(IOException::class.java) {answer(stale)}
        stale.retryWrites()
        assertEquals(1,stale.state!!.index);assertEquals(1,stale.state!!.score)
        assertEquals(1,events.size)
    }
    @Test fun corruptJournalIsNotOverwrittenAndNewRoundHasNewIdentity() {
        val disk=storage();disk.access {it.replace(byteArrayOf(1,2,3))}
        assertThrows(IllegalArgumentException::class.java) {open(disk)}
        assertArrayEquals(byteArrayOf(1,2,3),disk.access {it.read()})
        val host=open(storage());repeat(5) {answer(host);host.dispatch(SessionAction.Next(host.state!!.task.id))}
        host.newRound(SessionId("next-round"),RoundLength.TEN,1,ContentLanguage.GERMAN)
        assertEquals("next-round",host.state!!.plan.id.value);assertEquals(10,host.state!!.plan.tasks.size)
    }
}
