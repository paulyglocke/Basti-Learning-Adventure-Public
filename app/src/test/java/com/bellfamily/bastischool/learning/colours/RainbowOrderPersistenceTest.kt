package com.bellfamily.bastischool.learning.colours

import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.progress.*
import com.bellfamily.bastischool.learning.session.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.IOException

class RainbowOrderPersistenceTest {
    @get:Rule val temp = TemporaryFolder()
    private fun disk() = AtomicProgressStorage(temp.newFolder(), JvmAtomicCommit)
    private fun host(level: RainbowOrderLevel, disk: ProgressStorage, repository: ProgressRepository) =
        RainbowOrderHost(disk, repository, level)
    private fun records(repository: ProgressRepository) = ProgressFixtures.records(repository).map { it.event }

    @Test fun levelsHaveSeparateDeterministicTargetsAndStableEvidence() {
        val starter = RainbowOrderContent.start(SessionId("starter"), RainbowOrderLevel.STARTER, 81L, ContentLanguage.ENGLISH)
        val same = RainbowOrderContent.start(SessionId("starter"), RainbowOrderLevel.STARTER, 81L, ContentLanguage.GERMAN)
        val full = RainbowOrderContent.start(SessionId("full"), RainbowOrderLevel.FULL, 81L, ContentLanguage.ENGLISH)
        assertEquals(listOf("colour.red", "colour.yellow", "colour.blue", "colour.violet"), starter.target.map { it.value })
        assertEquals(RainbowContent.order, full.target)
        assertEquals(starter.choices, same.choices)
        assertEquals("activity.colours.rainbow_order", RainbowOrderContent.activity.value)
        assertNotEquals(RainbowOrderContent.journalName(RainbowOrderLevel.STARTER), RainbowOrderContent.journalName(RainbowOrderLevel.FULL))
    }

    @Test fun partialAndCompletedStarterRestoreWithSupportedEvidenceOnlyOnce() {
        val disk = disk(); val repo = ProgressFixtures.repository(temp.newFolder())
        var h = host(RainbowOrderLevel.STARTER, disk, repo)
        h.open(SessionId("starter-session"), 10L, ContentLanguage.GERMAN)
        val first = h.state!!
        h.dispatch(RainbowOrderAction.Help(first.task))
        val helped = h.state!!
        h.dispatch(RainbowOrderAction.Place(helped.nextAttempt!!, helped.target.first()))
        val placed = h.state!!
        assertEquals(1, placed.index)
        h = host(RainbowOrderLevel.STARTER, disk, repo)
        h.open(SessionId("ignored"), 999L, ContentLanguage.ENGLISH)
        assertEquals(placed.id, h.state!!.id)
        assertEquals(placed.choices, h.state!!.choices)
        assertEquals(placed.steps, h.state!!.steps)
        assertEquals(ContentLanguage.GERMAN, h.state!!.language)
        assertTrue(h.state!!.steps.first().support.hint)

        while (!h.state!!.completed) {
            val s = h.state!!
            val choice = s.target[s.index]
            h.dispatch(RainbowOrderAction.Place(s.nextAttempt!!, choice))
            h = host(RainbowOrderLevel.STARTER, disk, repo).also { it.open(SessionId("ignored"), 1L, ContentLanguage.ENGLISH) }
        }
        assertTrue(h.state!!.acknowledged)
        val before = records(repo)
        assertEquals(4, before.filterIsInstance<AttemptEvent>().size)
        assertTrue(before.filterIsInstance<AttemptEvent>().all { it.support.hint })
        assertEquals("context.colours.rainbow_order.starter", before.filterIsInstance<AttemptEvent>().first().evidence.context.value)
        assertEquals(1, before.filterIsInstance<CompletionEvent>().size)
        host(RainbowOrderLevel.STARTER, disk, repo).open(SessionId("new"), 5L, ContentLanguage.ENGLISH)
        assertEquals(before, records(repo))
    }

    @Test fun fullRoundPreservesWrongAttemptHelpAndExactRecovery() {
        val disk = disk(); val repo = ProgressFixtures.repository(temp.newFolder())
        var h = host(RainbowOrderLevel.FULL, disk, repo)
        h.open(SessionId("full-session"), 22L, ContentLanguage.ENGLISH)
        var s = h.state!!
        h.dispatch(RainbowOrderAction.Help(s.task))
        h.dispatch(RainbowOrderAction.Replay(h.state!!.task))
        assertEquals(1, h.state!!.current.support.replays)
        assertTrue(records(repo).isEmpty())
        s = h.state!!
        h.dispatch(RainbowOrderAction.Place(s.nextAttempt!!, s.target.last()))
        assertEquals(AnswerState.RETRY_AVAILABLE, h.state!!.current.answer)
        assertEquals(AttemptOutcome.INCORRECT, records(repo).filterIsInstance<AttemptEvent>().single().outcome)
        s = h.state!!
        h.dispatch(RainbowOrderAction.Retry(AttemptId(s.task, s.current.attempts)))
        h.dispatch(RainbowOrderAction.Language(ContentLanguage.GERMAN))
        s = h.state!!
        h.dispatch(RainbowOrderAction.Place(s.nextAttempt!!, s.target.first()))
        val accepted = h.state!!
        h = host(RainbowOrderLevel.FULL, disk, repo)
        h.open(SessionId("different"), 400L, ContentLanguage.ENGLISH)
        assertEquals(accepted.id, h.state!!.id)
        assertEquals(accepted.choices, h.state!!.choices)
        assertEquals(accepted.steps, h.state!!.steps)
        assertEquals(ContentLanguage.GERMAN, h.state!!.language)
        assertEquals(1, h.state!!.index)

        while (!h.state!!.completed) {
            s = h.state!!
            h.dispatch(RainbowOrderAction.Place(s.nextAttempt!!, s.target[s.index]))
        }
        assertTrue(h.state!!.acknowledged)
        val events = records(repo)
        assertEquals(8, events.filterIsInstance<AttemptEvent>().size)
        assertEquals(1, events.filterIsInstance<CompletionEvent>().size)
        assertTrue(events.filterIsInstance<AttemptEvent>().all { it.evidence.context.value == "context.colours.rainbow_order.full" })
        assertTrue(events.filterIsInstance<AttemptEvent>().any { it.support.hint })
        assertEquals(7, events.filterIsInstance<CompletionEvent>().single().tasks.size)
        val firstTask = events.filterIsInstance<CompletionEvent>().single().tasks.first()
        assertEquals(2, firstTask.attempts)
        assertEquals(1, firstTask.retries)
        assertEquals(1, firstTask.support.replays)
        val completed = h.state!!
        h = host(RainbowOrderLevel.FULL, disk, repo)
        h.open(SessionId("ignored-completed"), 999L, ContentLanguage.ENGLISH)
        assertEquals(completed.id, h.state!!.id)
        assertEquals(completed.choices, h.state!!.choices)
        assertEquals(completed.steps, h.state!!.steps)
        assertTrue(h.state!!.completed && h.state!!.acknowledged)
        assertEquals(events, records(repo))
    }

    @Test fun acceptedPlacementAndProgressEventSurviveDeliveryFailureAndRetryWithoutDuplicates() {
        val disk = disk(); val real = ProgressFixtures.repository(temp.newFolder()); var fail = true
        val flaky = object : ProgressRepository {
            override fun append(event: ProgressEvent) = if (fail) ProgressWriteResult.Failed(ProgressFailure.IO) else real.append(event)
            override fun read(query: ProgressQuery) = real.read(query)
        }
        var h = host(RainbowOrderLevel.FULL, disk, flaky)
        h.open(SessionId("delivery"), 31L, ContentLanguage.ENGLISH)
        val before = h.state!!
        h.dispatch(RainbowOrderAction.Place(before.nextAttempt!!, before.target.first()))
        assertTrue(h.failure)
        assertTrue(h.hasPending)
        val accepted = h.state!!
        assertEquals(1, accepted.index)
        fail = false
        h = host(RainbowOrderLevel.FULL, disk, flaky)
        h.open(SessionId("unused"), 99L, ContentLanguage.GERMAN)
        assertEquals(accepted.steps, h.state!!.steps)
        h.retryWrites()
        h.retryWrites()
        assertEquals(1, records(real).filterIsInstance<AttemptEvent>().size)
        assertFalse(h.hasPending)
        assertFalse(h.failure)
    }

    @Test fun anAcknowledgementWriteFailureCanBeRecoveredWithoutDuplicateCompletion() {
        val base = disk(); var failAfterReplace = false; val real = ProgressFixtures.repository(temp.newFolder())
        val uncertain = object : ProgressStorage {
            override fun <T> access(block: (ProgressTransaction) -> T): T = base.access { tx ->
                block(object : ProgressTransaction {
                    override fun read() = tx.read()
                    override fun replace(bytes: ByteArray) {
                        val acknowledgement = runCatching { RainbowOrderHost.decode(bytes).first.acknowledged }.getOrDefault(false)
                        tx.replace(bytes)
                        if (failAfterReplace && acknowledgement) throw IOException("acknowledgement committed but response interrupted")
                    }
                })
            }
        }
        var h = host(RainbowOrderLevel.STARTER, uncertain, real)
        h.open(SessionId("ack"), 17L, ContentLanguage.ENGLISH)
        while (!h.state!!.completed) {
            val s = h.state!!
            if (s.index == s.target.lastIndex) failAfterReplace = true
            h.dispatch(RainbowOrderAction.Place(s.nextAttempt!!, s.target[s.index]))
            if (h.failure) {
                assertEquals(1, records(real).filterIsInstance<CompletionEvent>().size)
                val restored = host(RainbowOrderLevel.STARTER, uncertain, real)
                restored.open(SessionId("ignored"), 1L, ContentLanguage.GERMAN)
                h = restored
                failAfterReplace = false
            }
        }
        assertTrue(h.state!!.acknowledged)
        host(RainbowOrderLevel.STARTER, uncertain, real).open(SessionId("ignored"), 1L, ContentLanguage.GERMAN)
        assertEquals(1, records(real).filterIsInstance<CompletionEvent>().size)
    }

    @Test fun corruptAndWrongLevelJournalsAreRejectedWithoutReplacement() {
        val disk = disk(); val repo = ProgressFixtures.repository(temp.newFolder())
        val starter = host(RainbowOrderLevel.STARTER, disk, repo)
        starter.open(SessionId("level"), 1L, ContentLanguage.ENGLISH)
        val bytes = disk.access { it.read() }!!
        try { host(RainbowOrderLevel.FULL, disk, repo).open(SessionId("ignored"), 2L, ContentLanguage.ENGLISH); fail("wrong level accepted") }
        catch (_: IllegalArgumentException) { }
        assertArrayEquals(bytes, disk.access { it.read() })
        disk.access { it.replace(byteArrayOf(1, 2, 3)) }
        try { host(RainbowOrderLevel.STARTER, disk, repo).open(SessionId("ignored"), 2L, ContentLanguage.ENGLISH); fail("corrupt journal accepted") }
        catch (_: IllegalArgumentException) { }
        assertArrayEquals(byteArrayOf(1, 2, 3), disk.access { it.read() })
    }

    @Test fun selectedLevelPointerSurvivesRecreationAndRejectsCorruption() {
        val disk = disk()
        assertNull(RainbowOrderLevelStore(disk).read())
        RainbowOrderLevelStore(disk).write(RainbowOrderLevel.FULL)
        assertEquals(RainbowOrderLevel.FULL, RainbowOrderLevelStore(disk).read())
        disk.access { it.replace(byteArrayOf(1, 2, 3)) }
        try { RainbowOrderLevelStore(disk).read(); fail("corrupt level pointer accepted") }
        catch (_: IllegalArgumentException) { }
        assertArrayEquals(byteArrayOf(1, 2, 3), disk.access { it.read() })
    }

    @Test fun failedCheckpointWriteDoesNotExposeAnUncommittedPlacementAsCurrent() {
        val base = disk(); var fail = false
        val flaky = object : ProgressStorage {
            override fun <T> access(block: (ProgressTransaction) -> T): T = base.access { tx ->
                block(object : ProgressTransaction {
                    override fun read() = tx.read()
                    override fun replace(bytes: ByteArray) { if (fail) throw IOException("before replace"); tx.replace(bytes) }
                })
            }
        }
        val repository = ProgressFixtures.repository(temp.newFolder())
        val h = host(RainbowOrderLevel.FULL, flaky, repository)
        h.open(SessionId("write-failure"), 8L, ContentLanguage.ENGLISH)
        val before = h.state!!
        fail = true
        try { h.dispatch(RainbowOrderAction.Place(before.nextAttempt!!, before.target.first())); fail("write should fail") }
        catch (_: IOException) { }
        assertEquals(before.steps, h.state!!.steps)
        assertTrue(h.failure)
        assertTrue(records(repository).isEmpty())
        fail = false
        h.retryWrites()
        assertEquals(before.steps, h.state!!.steps)
        assertFalse(h.failure)
    }
}
