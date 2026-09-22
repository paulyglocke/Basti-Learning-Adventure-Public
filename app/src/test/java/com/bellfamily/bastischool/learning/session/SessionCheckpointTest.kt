package com.bellfamily.bastischool.learning.session

import com.bellfamily.bastischool.learning.content.ContentRepository
import com.bellfamily.bastischool.learning.models.*
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.nio.ByteBuffer
import org.junit.Assert.*
import org.junit.Test
import com.bellfamily.bastischool.learning.session.SessionFixtures as F

class SessionCheckpointTest {
    private fun restore(bytes: ByteArray) = SessionCheckpoint.restore(bytes, F.activity, 1, F.content)
    private fun roundTrip(state: SessionState): SessionState {
        val bytes = SessionCheckpoint.encode(state)
        val restored = (restore(bytes) as SessionRestoreResult.Restored).state
        assertArrayEquals(bytes, SessionCheckpoint.encode(restored))
        assertEquals(state.plan.id, restored.plan.id)
        assertEquals(state.task.id, restored.task.id)
        assertEquals(state.task.question.choices, restored.task.question.choices)
        assertEquals(state.progress, restored.progress)
        assertEquals(state.score, restored.score)
        return restored
    }
    @Test fun unansweredTaskRestoresExactlyInBothLanguages() {
        ContentLanguage.entries.forEach {
            val original = F.start(language = it)
            val restored = roundTrip(original)
            assertEquals(it, restored.language)
            assertEquals(AnswerState.UNANSWERED, restored.current.answer)
            assertEquals(original.task.question.instruction, restored.task.question.instruction)
        }
    }
    @Test fun answeredTaskRestoresAnswerLockAndRejectsDuplicateScoring() {
        val restored = roundTrip(F.answer(F.start()).state)
        assertEquals(AnswerState.CORRECT, restored.current.answer)
        assertEquals(1, restored.score)
        assertSame(restored, F.answer(restored).state)
    }
    @Test fun hintsReplayParentHelpAndPendingRetrySurviveRestore() {
        var state = F.start()
        state = SessionReducer.reduce(state, SessionAction.Hint(state.task.id)).state
        state = SessionReducer.reduce(state, SessionAction.ParentHelp(state.task.id)).state
        state = SessionReducer.reduce(state, SessionAction.Replay(state.task.id)).state
        state = F.answer(state, false).state
        val waiting = roundTrip(state)
        assertEquals(AnswerState.RETRY_AVAILABLE, waiting.current.answer)
        val retried = roundTrip(F.retry(waiting).state)
        assertEquals(1, retried.current.retries)
        assertEquals(SupportUse(1, hint = true, parentHelp = true), retried.current.support)
        val answered = roundTrip(F.answer(retried).state)
        assertEquals(2, answered.current.attempts)
        assertEquals(1, answered.score)
    }
    @Test fun currentIndexScoreAndAllEarlierResultsSurviveRestore() {
        var state = F.start(RoundLength.TEN, WrongAnswerPolicy.LOCK)
        repeat(4) { state = F.next(F.answer(state, correct = it % 2 == 0).state).state }
        val restored = roundTrip(state)
        assertEquals(4, restored.index)
        assertEquals(2, restored.score)
        assertEquals(TaskProgress(), restored.current)
        assertEquals(AnswerState.INCORRECT, restored.progress[1].answer)
    }
    @Test fun completedPendingAcknowledgedAndFailedSessionsDoNotCompleteAgainOnRestore() {
        val pending = F.completed().state
        val acknowledged = SessionReducer.reduce(pending, SessionAction.CompletionResult(pending.completionRequestId!!, true)).state
        val failed = SessionReducer.reduce(pending, SessionAction.CompletionResult(pending.completionRequestId!!, false)).state
        for (state in listOf(pending, acknowledged, failed)) {
            val restored = roundTrip(state)
            assertEquals(SessionPhase.COMPLETED, restored.phase)
            assertEquals(state.completion, restored.completion)
            assertTrue(F.next(restored).effects.isEmpty())
            assertTrue(F.answer(restored).effects.isEmpty())
            assertTrue(SessionReducer.reduce(restored, SessionAction.Replay(restored.task.id)).effects.none { it is SessionEffect.Complete })
        }
    }
    @Test fun restoredPlanIsSnapshotNotRegenerationFromChangedSeedOrCandidates() {
        val original = F.start()
        val newPlan = F.plan(seed = 999)
        assertFalse(SessionCheckpoint.encode(original).contentEquals(SessionCheckpoint.encode(
            SessionReducer.start(newPlan, original.language, F.content).state)))
        val restored = roundTrip(original)
        for (index in original.plan.tasks.indices) {
            val before = original.plan.tasks[index]
            val after = restored.plan.tasks[index]
            assertEquals(before.id, after.id)
            assertEquals(before.question.definition, after.question.definition)
            assertEquals(before.question.choices, after.question.choices)
            assertEquals(before.question.correct, after.question.correct)
            assertEquals(before.question.instruction, after.question.instruction)
            assertEquals(before.question.hint, after.question.hint)
        }
    }
    @Test fun schemaActivityAndContentVersionMismatchAreExplicitlyIncompatible() {
        val bytes = SessionCheckpoint.encode(F.start())
        val badVersion = bytes.copyOf().apply { ByteBuffer.wrap(this).putInt(4, SessionCheckpoint.VERSION + 1) }
        val rejected = SessionRestoreResult.Rejected(CheckpointRejection.INCOMPATIBLE)
        assertEquals(rejected, restore(badVersion))
        assertEquals(rejected, SessionCheckpoint.restore(bytes, ActivityId("activity.other"), 1, F.content))
        assertEquals(rejected, SessionCheckpoint.restore(bytes, F.activity, 2, F.content))
        val newer = object : ContentRepository by F.content { override val version = ContentVersion(1, 2) }
        assertEquals(rejected, SessionCheckpoint.restore(bytes, F.activity, 1, newer))
    }
    @Test fun truncatedRandomOversizedAndTrailingDataRejectSafely() {
        val bytes = SessionCheckpoint.encode(F.start())
        for (length in 0 until bytes.size step 13) {
            assertTrue("truncated at $length", restore(bytes.copyOf(length)) is SessionRestoreResult.Rejected)
        }
        val random = java.util.Random(18)
        repeat(100) {
            val junk = ByteArray(random.nextInt(400)).also(random::nextBytes)
            assertTrue(restore(junk) is SessionRestoreResult.Rejected)
        }
        assertEquals(SessionRestoreResult.Rejected(CheckpointRejection.TOO_LARGE), restore(ByteArray(SessionCheckpoint.MAX_BYTES + 1)))
        assertTrue(restore(bytes + byteArrayOf(0)) is SessionRestoreResult.Rejected)
    }
    @Test fun invalidEnumsReferencesAndBlankBilingualTextAreRejected() {
        val bytes = SessionCheckpoint.encode(F.start())
        assertMalformed(replaceUtf(bytes, "ENGLISH", "UNKNOWN"))
        assertMalformed(replaceUtf(bytes, F.summary.display.en, ""))
        val unknown = object : ContentRepository by F.content { override fun find(id: ContentId): ContentDefinition? = null }
        assertTrue(SessionCheckpoint.restore(bytes, F.activity, 1, unknown) is SessionRestoreResult.Rejected)
    }
    @Test fun invalidIndexAndContradictoryScoreAreRejected() {
        val bytes = SessionCheckpoint.encode(F.start())
        val offset = find(bytes, utf("ENGLISH")) + utf("ENGLISH").size
        assertMalformed(bytes.copyOf().apply { ByteBuffer.wrap(this).putInt(offset, -1) })
        assertMalformed(bytes.copyOf().apply { ByteBuffer.wrap(this).putInt(offset, 10) })
        assertMalformed(bytes.copyOf().apply { ByteBuffer.wrap(this).putInt(offset + 4, 5) })
    }
    @Test fun unboundedCountsAndImpossibleAttemptCountersAreRejected() {
        val bytes = SessionCheckpoint.encode(F.start())
        val summaryEnd = find(bytes, utf(F.summary.speech.de), last = true) + utf(F.summary.speech.de).size
        assertMalformed(bytes.copyOf().apply { ByteBuffer.wrap(this).putInt(summaryEnd, Int.MAX_VALUE) })
        val progress = find(bytes, utf("UNANSWERED")) + utf("UNANSWERED").size
        assertMalformed(bytes.copyOf().apply { ByteBuffer.wrap(this).putInt(progress, -1) })
        assertMalformed(bytes.copyOf().apply { ByteBuffer.wrap(this).putInt(progress + 4, 2) })
        assertMalformed(bytes.copyOf().apply { this[progress + 12] = 2 }) // Invalid boolean encoding.
    }
    @Test fun encoderRejectsImpossiblePhaseProgressAndCompletionCombinations() {
        val state = F.start()
        val impossible = listOf(
            state.changed(index = 3),
            state.changed(phase = SessionPhase.COMPLETED),
            state.changed(completion = CompletionState.ACKNOWLEDGED, delivery = 1),
            state.changed(progress = state.progress.toMutableList().apply { this[2] = TaskProgress(support = SupportUse(1)) }),
            state.changed(progress = state.progress.toMutableList().apply { this[0] = TaskProgress(AnswerState.CORRECT) })
        )
        impossible.forEach { invalid -> assertThrows(IllegalArgumentException::class.java) { SessionCheckpoint.encode(invalid) } }
    }
    private fun assertMalformed(bytes: ByteArray) =
        assertEquals(SessionRestoreResult.Rejected(CheckpointRejection.MALFORMED), restore(bytes))
    private fun utf(value: String): ByteArray = ByteArrayOutputStream().also { bytes ->
        DataOutputStream(bytes).use { it.writeUTF(value) }
    }.toByteArray()
    private fun find(bytes: ByteArray, pattern: ByteArray, last: Boolean = false): Int {
        val matches = (0..bytes.size - pattern.size).filter { index -> pattern.indices.all { bytes[index + it] == pattern[it] } }
        return if (last) matches.last() else matches.first()
    }
    private fun replaceUtf(bytes: ByteArray, before: String, after: String): ByteArray {
        val pattern = utf(before)
        val offset = find(bytes, pattern)
        return bytes.copyOfRange(0, offset) + utf(after) + bytes.copyOfRange(offset + pattern.size, bytes.size)
    }
}
