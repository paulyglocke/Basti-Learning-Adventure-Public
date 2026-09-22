package com.bellfamily.bastischool.learning.progress

import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.session.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.io.IOException
import java.util.concurrent.Executors
import com.bellfamily.bastischool.learning.progress.ProgressFixtures as F

class ProgressRepositoryTest {
    @get:Rule val temporary = TemporaryFolder()
    private fun directory() = File(temporary.root, "progress")

    @Test fun emptyStoreAndRepositoryRecreationReadDurableEventExactly() {
        val repo = F.repository(directory())
        assertTrue(F.records(repo).isEmpty())
        val event = F.event()
        val saved = repo.append(event) as ProgressWriteResult.Saved
        assertTrue(saved.inserted)
        assertEquals(event, saved.record.event)
        assertEquals(1234L, saved.record.recordedAtMillis)
        assertEquals(listOf(saved.record), F.records(F.repository(directory())))
    }
    @Test fun multipleEventsKeepAcceptanceOrderDespiteClockRollbackAndTies() {
        val times = ArrayDeque(listOf(200L, 100L, 100L))
        val repo = F.repository(directory(), ProgressClock { times.removeFirst() })
        repeat(3) { assertTrue(repo.append(F.event(number = it + 1)) is ProgressWriteResult.Saved) }
        val records = F.records(repo)
        assertEquals(listOf(1L, 2L, 3L), records.map { it.sequence })
        assertEquals(listOf(200L, 100L, 100L), records.map { it.recordedAtMillis })
        assertEquals(records.reversed().take(2), F.records(repo, ProgressQuery(newestFirst = true, limit = 2)))
    }
    @Test fun duplicateDeliveryAcrossRestartKeepsFirstTimestampAndSequence() {
        val event = F.event()
        val first = F.repository(directory(), ProgressClock { 10 }).append(event) as ProgressWriteResult.Saved
        val duplicate = F.repository(directory(), ProgressClock { error("Duplicate must not read clock") }).append(event) as ProgressWriteResult.Saved
        assertFalse(duplicate.inserted)
        assertEquals(first.record, duplicate.record)
        assertEquals(1, F.records(F.repository(directory())).size)
    }
    @Test fun sameIdentityWithDifferentOutcomeIsConflictAndPreservesOriginal() {
        val repo = F.repository(directory())
        val event = F.event()
        repo.append(event)
        val bytes = File(directory(), "events.bin").readBytes()
        assertEquals(ProgressWriteResult.Failed(ProgressFailure.CONFLICT), repo.append(event.copy(outcome = AttemptOutcome.INCORRECT)))
        assertArrayEquals(bytes, File(directory(), "events.bin").readBytes())
        assertEquals(event, F.records(repo).single().event)
    }
    @Test fun reusedSessionIdentityCannotChangeActivityOrContentVersion() {
        val repo = F.repository(directory())
        val event = F.event()
        repo.append(event)
        val next = F.event(number = 2)
        assertEquals(ProgressWriteResult.Failed(ProgressFailure.CONFLICT), repo.append(next.copy(origin =
            next.origin.copy(activity = ActivityId("activity.other")))))
        assertEquals(ProgressWriteResult.Failed(ProgressFailure.CONFLICT), repo.append(next.copy(origin =
            next.origin.copy(contentVersion = ContentVersion(1, 2)))))
        assertEquals(1, F.records(repo).size)
    }
    @Test fun filtersBySkillSessionActivityAndKindCanBeCombined() {
        val repo = F.repository(directory())
        val first = F.event()
        val second = F.event(session = "round-two", skill = "skill.test.other", activity = "activity.other")
        repo.append(first); repo.append(second)
        assertEquals(listOf(first), F.records(repo, ProgressQuery(skill = first.evidence.skill)).map { it.event })
        assertEquals(listOf(second), F.records(repo, ProgressQuery(session = second.origin.session, activity = second.origin.activity)).map { it.event })
        assertTrue(F.records(repo, ProgressQuery(skill = first.evidence.skill, activity = second.origin.activity)).isEmpty())
        assertTrue(F.records(repo, ProgressQuery(kind = ProgressEventKind.SESSION_COMPLETED)).isEmpty())
    }
    @Test fun supportAttemptLanguageOutcomeAndVersionRemainSeparateAfterPersistence() {
        val event = F.event(number = 3).copy(outcome = AttemptOutcome.INCORRECT)
        val repo = F.repository(directory())
        repo.append(event)
        val read = F.records(F.repository(directory())).single().event as AttemptEvent
        assertEquals(event, read)
        assertEquals(3, read.attempt.number)
        assertEquals(2, read.retriesBeforeAttempt)
        assertEquals(SupportUse(2, hint = true, parentHelp = true), read.support)
        assertEquals(ContentLanguage.GERMAN, read.language)
        assertEquals(AttemptOutcome.INCORRECT, read.outcome)
        assertEquals(ContentVersion(1, 1), read.origin.contentVersion)
    }
    @Test fun failureBeforeRenameKeepsOldStoreAndRetrySucceeds() {
        val repo = F.repository(directory())
        repo.append(F.event())
        val old = File(directory(), "events.bin").readBytes()
        val commit = object : AtomicProgressCommit by JvmAtomicCommit {
            override fun replace(source: File, target: File) { throw IOException("simulated write failure") }
        }
        val event = F.event(number = 2)
        assertEquals(ProgressWriteResult.Failed(ProgressFailure.IO), F.repository(directory(), commit = commit).append(event))
        assertArrayEquals(old, File(directory(), "events.bin").readBytes())
        assertTrue(File(directory(), "events.pending").exists())
        assertEquals(1, F.records(F.repository(directory())).size)
        assertTrue(F.repository(directory()).append(event) is ProgressWriteResult.Saved)
        assertEquals(2, F.records(F.repository(directory())).size)
    }
    @Test fun uncertainFailureAfterRenameRetriesWithSameIdWithoutDuplicating() {
        val commit = object : AtomicProgressCommit by JvmAtomicCommit {
            override fun replace(source: File, target: File) {
                JvmAtomicCommit.replace(source, target)
                throw IOException("Simulated interruption after atomic replacement")
            }
        }
        val event = F.event()
        assertEquals(ProgressWriteResult.Failed(ProgressFailure.IO), F.repository(directory(), commit = commit).append(event))
        val retry = F.repository(directory()).append(event) as ProgressWriteResult.Saved
        assertFalse(retry.inserted)
        assertEquals(1, F.records(F.repository(directory())).size)
    }
    @Test fun directorySyncFailureAfterRenameIsExplicitAndSafelyRetryable() {
        val dir = directory()
        val commit = object : AtomicProgressCommit by JvmAtomicCommit {
            override fun syncDirectory(directory: File) {
                if (directory == dir) throw IOException("simulated fsync failure")
                JvmAtomicCommit.syncDirectory(directory)
            }
        }
        assertEquals(ProgressWriteResult.Failed(ProgressFailure.IO), F.repository(dir, commit = commit).append(F.event()))
        assertFalse((F.repository(dir).append(F.event()) as ProgressWriteResult.Saved).inserted)
    }
    @Test fun abandonedTemporaryFileIsNotTreatedAsCommittedProgress() {
        val dir = directory().also { assertTrue(it.mkdirs()) }
        File(dir, "events.pending").writeBytes(byteArrayOf(1, 2, 3))
        assertTrue(F.records(F.repository(dir)).isEmpty())
        assertTrue(F.repository(dir).append(F.event()) is ProgressWriteResult.Saved)
        assertEquals(1, F.records(F.repository(dir)).size)
    }
    @Test fun malformedExistingStoreIsNotSilentlyResetOrOverwritten() {
        val dir = directory().also { assertTrue(it.mkdirs()) }
        val bad = byteArrayOf(1, 2, 3)
        File(dir, "events.bin").writeBytes(bad)
        val repo = F.repository(dir)
        assertEquals(ProgressReadResult.Failed(ProgressFailure.CORRUPT), repo.read())
        assertEquals(ProgressWriteResult.Failed(ProgressFailure.CORRUPT), repo.append(F.event()))
        assertArrayEquals(bad, File(dir, "events.bin").readBytes())
    }
    @Test fun invalidStorageLocationAndClockReportFailures() {
        val dir = directory().also { it.writeText("not a directory") }
        assertEquals(ProgressReadResult.Failed(ProgressFailure.IO), F.repository(dir).read())
        assertEquals(ProgressWriteResult.Failed(ProgressFailure.IO), F.repository(dir).append(F.event()))
        val other = File(temporary.root, "other")
        assertEquals(ProgressWriteResult.Failed(ProgressFailure.CLOCK), F.repository(other, ProgressClock { -1 }).append(F.event()))
        assertEquals(ProgressWriteResult.Failed(ProgressFailure.CLOCK), F.repository(other, ProgressClock { error("clock") }).append(F.event()))
        assertTrue(F.records(F.repository(other)).isEmpty())
    }
    @Test fun twoRepositoryInstancesSerializeConcurrentWritesAndDuplicateDeliveries() {
        val executor = Executors.newFixedThreadPool(2)
        try {
            val futures = (1..12).map { number -> executor.submit<ProgressWriteResult> { F.repository(directory()).append(F.event(number = number)) } }
            futures.forEach { assertTrue(it.get() is ProgressWriteResult.Saved) }
            val duplicates = (1..2).map { executor.submit<ProgressWriteResult> { F.repository(directory()).append(F.event(number = 20)) } }
                .map { it.get() as ProgressWriteResult.Saved }
            assertEquals(1, duplicates.count { it.inserted })
            val all = F.records(F.repository(directory()))
            assertEquals((1L..13L).toList(), all.map { it.sequence })
            assertEquals(13, all.map { it.event.id }.toSet().size)
        } finally { executor.shutdownNow() }
    }
    @Test fun queryResultsCannotBeMutatedByCallers() {
        val repo = F.repository(directory())
        repo.append(F.event())
        assertThrows(UnsupportedOperationException::class.java) { (F.records(repo) as MutableList).clear() }
        assertEquals(1, F.records(repo).size)
    }
}
