package com.bellfamily.bastischool.learning.counting

import com.bellfamily.bastischool.audio.*
import com.bellfamily.bastischool.learning.models.ContentLanguage
import com.bellfamily.bastischool.learning.progress.*
import com.bellfamily.bastischool.learning.session.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.IOException

class CountingTrayTest {
    @get:Rule val temp = TemporaryFolder()
    private fun disk() = AtomicProgressStorage(temp.newFolder(), JvmAtomicCommit)
    private fun repository() = FileProgressRepository(disk(), ProgressClock { 123 })
    private fun state(seed: Long = 1) = CountingTray.start(SessionId("counting-tray"), RoundLength.FIVE, seed, ContentLanguage.ENGLISH)

    @Test fun generationIsSeededBalancedAndNeverRepeatsAdjacentTarget() {
        for (seed in 0L..500L) {
            val five = CountingTray.generateTargets(RoundLength.FIVE, seed)
            assertEquals(five, CountingTray.generateTargets(RoundLength.FIVE, seed))
            assertEquals(setOf(1, 2, 3, 4, 5), five.toSet())
            assertTrue(five.zipWithNext().none { (a, b) -> a == b })
            val ten = CountingTray.generateTargets(RoundLength.TEN, seed)
            assertEquals(ten, CountingTray.generateTargets(RoundLength.TEN, seed))
            assertEquals((1..5).flatMap { listOf(it, it) }.sorted(), ten.sorted())
            assertTrue(ten.zipWithNext().none { (a, b) -> a == b })
        }
    }

    @Test fun tappingBuildsAndRemovesObjectsWithoutEvidenceUntilCheck() {
        var s = state()
        assertEquals(0, s.placedCount)
        s = CountingTray.reduce(s, CountingTray.Action.Toggle(0))
        s = CountingTray.reduce(s, CountingTray.Action.Toggle(4))
        assertEquals(2, s.placedCount)
        assertEquals(0, s.current.attempts)
        s = CountingTray.reduce(s, CountingTray.Action.Toggle(0))
        assertEquals(1, s.placedCount)
        assertSame(s, CountingTray.reduce(s, CountingTray.Action.Toggle(-1)))
        assertSame(s, CountingTray.reduce(s, CountingTray.Action.Toggle(5)))
    }

    @Test fun checksRecordWrongAndSupportedSuccessOnceAndThenLocksTheTask() {
        var s = state()
        val target = s.target
        s = CountingTray.reduce(s, CountingTray.Action.Help)
        s = CountingTray.reduce(s, CountingTray.Action.Replay)
        repeat((target - 1).coerceAtLeast(0)) { s = CountingTray.reduce(s, CountingTray.Action.Toggle(it)) }
        s = CountingTray.reduce(s, CountingTray.Action.Check)
        assertFalse(s.current.solved)
        assertEquals(target - 1, s.current.checkedCount)
        assertEquals(target - 1, CountingTray.count(CountingTray.attempt(s).choice))
        assertEquals(AttemptOutcome.INCORRECT, CountingTray.attempt(s).outcome)
        assertTrue(CountingTray.attempt(s).support.hint)
        if (target > 1) s = CountingTray.reduce(s, CountingTray.Action.Toggle(target - 1))
        else s = CountingTray.reduce(s, CountingTray.Action.Toggle(4))
        // Ensure the tray has exactly the requested number after the first check.
        while (s.placedCount > target) s = CountingTray.reduce(s, CountingTray.Action.Toggle(4))
        while (s.placedCount < target) s = CountingTray.reduce(s, CountingTray.Action.Toggle(s.placedMask.countOneBits()))
        s = CountingTray.reduce(s, CountingTray.Action.Check)
        assertTrue(s.current.solved)
        assertEquals(AttemptOutcome.CORRECT, CountingTray.attempt(s).outcome)
        assertEquals(target, CountingTray.count(CountingTray.attempt(s).choice))
        assertEquals(s, CountingTray.reduce(s, CountingTray.Action.Check))
        assertEquals(s, CountingTray.reduce(s, CountingTray.Action.Toggle(4)))
        val next = CountingTray.reduce(s, CountingTray.Action.Next)
        assertEquals(1, next.index)
        assertEquals(0, next.placedCount)
        assertEquals(0, next.current.attempts)
    }

    @Test fun languageAndProgressCopyAreAuthoredAndBilingual() {
        assertEquals("Put one dinosaur in the tray.", CountingTray.prompt(1).display.en)
        assertEquals("Lege einen Dinosaurier ins Feld.", CountingTray.prompt(1).display.de)
        assertEquals("Put five dinosaurs in the tray.", CountingTray.prompt(5).display.en)
        assertEquals("Lege fünf Dinosaurier ins Feld.", CountingTray.prompt(5).display.de)
        assertEquals("You made a group of one dinosaur!", CountingTray.success(1).display.en)
        assertEquals("Du hast eine Gruppe mit einem Dinosaurier gelegt!", CountingTray.success(1).display.de)
        assertEquals("You made a group of five dinosaurs!", CountingTray.success(5).display.en)
        assertEquals("Du hast eine Gruppe mit fünf Dinosauriern gelegt!", CountingTray.success(5).display.de)
    }

    @Test fun hostRestoresPartialBuildDeliversAttemptsAndCompletesSharedProgress() {
        val storage = disk(); val progress = repository()
        var host = CountingTrayHost(storage, progress)
        assertTrue(host.open(SessionId("first"), RoundLength.FIVE, 42, ContentLanguage.GERMAN))
        val initial = host.state!!
        repeat((initial.target - 1).coerceAtLeast(0)) { host.dispatch(CountingTray.Action.Toggle(it)) }
        host.dispatch(CountingTray.Action.Check)
        host.dispatch(CountingTray.Action.Replay)
        host.dispatch(CountingTray.Action.Help)
        val partial = host.state!!
        host = CountingTrayHost(storage, progress)
        assertFalse(host.open(SessionId("ignored"), RoundLength.TEN, 99, ContentLanguage.ENGLISH))
        assertEquals(partial, host.state)

        repeat(5) {
            val current = host.state!!
            val target = current.target
            for (slot in 0 until 5) {
                val occupied = current.placedMask and (1 shl slot) != 0
                if (occupied != (slot < target)) host.dispatch(CountingTray.Action.Toggle(slot))
            }
            host.dispatch(CountingTray.Action.Check)
            host.dispatch(CountingTray.Action.Next)
        }
        assertTrue(host.state!!.completed)
        assertTrue(host.state!!.acknowledged)
        val events = (progress.read() as ProgressReadResult.Events).records.map { it.event }
        assertEquals(6, events.filterIsInstance<AttemptEvent>().size)
        assertEquals(1, events.filterIsInstance<CompletionEvent>().size)
        assertEquals(AttemptOutcome.INCORRECT, events.filterIsInstance<AttemptEvent>().first().outcome)
        val completion = events.filterIsInstance<CompletionEvent>().single()
        assertEquals(5, completion.tasks.size)
        assertEquals(2, completion.tasks.first().attempts)
        assertEquals(1, completion.tasks.first().retries)
        assertTrue(completion.tasks.first().support.hint)
        val restored = CountingTrayHost(storage, progress)
        restored.open(SessionId("ignored-again"), RoundLength.TEN, 100, ContentLanguage.ENGLISH)
        assertEquals(host.state, restored.state)
        restored.again(SessionId("new-round"), RoundLength.TEN, 101, ContentLanguage.ENGLISH)
        assertEquals(RoundLength.TEN, restored.state!!.round)
        assertFalse(restored.state!!.completed)
    }

    @Test fun pendingEvidenceRetriesIdempotentlyAndCorruptCheckpointFailsClosed() {
        val storage = disk(); val real = repository()
        val flaky = object : ProgressRepository {
            var fail = true
            override fun read(query: ProgressQuery) = real.read(query)
            override fun append(event: ProgressEvent): ProgressWriteResult {
                val accepted = real.append(event)
                return if (fail) ProgressWriteResult.Failed(ProgressFailure.IO) else accepted
            }
        }
        var host = CountingTrayHost(storage, flaky)
        host.open(SessionId("retry"), RoundLength.FIVE, 12, ContentLanguage.ENGLISH)
        host.dispatch(CountingTray.Action.Check)
        assertTrue(host.failure && host.hasPending)
        val pendingState = host.state
        host.dispatch(CountingTray.Action.Check)
        assertEquals(pendingState, host.state)
        flaky.fail = false
        host = CountingTrayHost(storage, flaky)
        host.open(SessionId("ignored"), RoundLength.TEN, 13, ContentLanguage.ENGLISH)
        assertFalse(host.failure)
        assertEquals(1, (real.read() as ProgressReadResult.Events).records.size)

        val bytes = CountingTrayHost.encode(host.state!!, emptyList())
        assertEquals(host.state, CountingTrayHost.decode(bytes).first)
        bytes[20] = (bytes[20].toInt() xor 1).toByte()
        assertTrue(runCatching { CountingTrayHost.decode(bytes) }.isFailure)
    }
}
