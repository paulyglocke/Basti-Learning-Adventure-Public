package com.bellfamily.bastischool.learning.progress

import com.bellfamily.bastischool.learning.session.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.*
import java.nio.ByteBuffer
import java.security.MessageDigest
import com.bellfamily.bastischool.learning.progress.ProgressFixtures as F

class ProgressCodecTest {
    @get:Rule val temporary = TemporaryFolder()
    private fun records() = (1..3).map { StoredProgressEvent(it.toLong(), 900, F.event(number = it)) }
    private fun failure(bytes: ByteArray) = assertThrows(ProgressProblem::class.java) { ProgressCodec.decode(bytes) }.reason
    private fun signed(payload: ByteArray) = payload + MessageDigest.getInstance("SHA-256").digest(payload)

    @Test fun serializationIsDeterministicAndRoundTripsTypedRecords() {
        val records = records()
        val bytes = ProgressCodec.encode(records)
        assertArrayEquals(bytes, ProgressCodec.encode(records.toList()))
        assertEquals(records, ProgressCodec.decode(bytes))
        assertArrayEquals(bytes, ProgressCodec.encode(ProgressCodec.decode(bytes)))
    }
    @Test fun checksumDetectsChangedBytesAndTruncationCannotReturnPartialRecords() {
        val bytes = ProgressCodec.encode(records())
        assertEquals(ProgressFailure.CORRUPT, failure(bytes.copyOf().apply { this[25] = (this[25].toInt() xor 1).toByte() }))
        for (length in 0 until bytes.size step 11) assertEquals(ProgressFailure.CORRUPT, failure(bytes.copyOf(length)))
        assertEquals(ProgressFailure.CORRUPT, failure(bytes + byteArrayOf(0)))
    }
    @Test fun incompatibleSchemaRemainsReadOnlyAndPreservesPersistedBytes() {
        val bytes = ProgressCodec.encode(records())
        val future = signed(bytes.copyOfRange(0, bytes.size - 32).apply { ByteBuffer.wrap(this).putInt(4, 99) })
        assertEquals(ProgressFailure.INCOMPATIBLE_VERSION, failure(future))
        val dir = temporary.newFolder()
        File(dir, "events.bin").writeBytes(future)
        val repo = F.repository(dir)
        assertEquals(ProgressReadResult.Failed(ProgressFailure.INCOMPATIBLE_VERSION), repo.read())
        assertEquals(ProgressWriteResult.Failed(ProgressFailure.INCOMPATIBLE_VERSION), repo.append(F.event()))
        assertArrayEquals(future, File(dir, "events.bin").readBytes())
    }
    @Test fun unknownRecordKindAndExtraFieldsFailClosedEvenWithValidChecksum() {
        val bytes = ProgressCodec.encode(records())
        val payload = bytes.copyOfRange(0, bytes.size - 32)
        val pattern = "ATTEMPT".toByteArray(Charsets.UTF_8)
        val offset = (0..payload.size - pattern.size).first { i -> pattern.indices.all { payload[i + it] == pattern[it] } }
        val unknown = payload.copyOf().apply { "UNKNOWN".toByteArray().copyInto(this, offset) }
        assertEquals(ProgressFailure.CORRUPT, failure(signed(unknown)))
        assertEquals(ProgressFailure.CORRUPT, failure(signed(payload + byteArrayOf(1, 2))))
    }
    @Test fun invalidCountsOrderAndTimestampsAreRejected() {
        val encoded = ProgressCodec.encode(records())
        val payload = encoded.copyOfRange(0, encoded.size - 32)
        assertEquals(ProgressFailure.CORRUPT, failure(signed(payload.copyOf().apply { ByteBuffer.wrap(this).putInt(8, Int.MAX_VALUE) })))
        assertEquals(ProgressFailure.CORRUPT, failure(signed(payload.copyOf().apply { ByteBuffer.wrap(this).putLong(12, 2) })))
        assertEquals(ProgressFailure.CORRUPT, failure(signed(payload.copyOf().apply { ByteBuffer.wrap(this).putLong(20, -1) })))
    }
    @Test fun capacityIsExplicitAndExistingHistoryIsNotPruned() {
        val dir = temporary.newFolder()
        val records = (1..ProgressCodec.MAX_EVENTS).map { StoredProgressEvent(it.toLong(), 10, F.event(number = it)) }
        val bytes = ProgressCodec.encode(records)
        File(dir, "events.bin").writeBytes(bytes)
        val repo = F.repository(dir)
        assertEquals(ProgressWriteResult.Failed(ProgressFailure.CAPACITY), repo.append(F.event(number = ProgressCodec.MAX_EVENTS + 1)))
        assertArrayEquals(bytes, File(dir, "events.bin").readBytes())
        assertFalse((repo.append(F.event()) as ProgressWriteResult.Saved).inserted)
        assertEquals(ProgressCodec.MAX_EVENTS, F.records(repo).size)
        assertEquals(ProgressFailure.CAPACITY, failure(ByteArray(ProgressCodec.MAX_BYTES + 1)))
    }
    @Test fun malformedRandomInputAlwaysProducesExplicitFailure() {
        val random = java.util.Random(9)
        repeat(100) {
            val bytes = ByteArray(random.nextInt(1000)).also(random::nextBytes)
            assertEquals(ProgressFailure.CORRUPT, failure(bytes))
        }
    }
    @Test fun eventIdentitiesAndInvalidModelCombinationsAreValidated() {
        val event = F.event()
        assertEquals("attempt/round-one/1/1", event.id.value)
        assertEquals("completion/round-one", ProgressEventId.completion(event.origin.session).value)
        assertNotEquals(event.id, ProgressEventId.completion(event.origin.session))
        assertThrows(IllegalArgumentException::class.java) { event.copy(attempt = AttemptId(TaskInstanceId(SessionId("other"), 1), 1)) }
        assertThrows(IllegalArgumentException::class.java) { CompletedTask(event.evidence, event.choice, event.outcome, 2, 0, event.support) }
        assertThrows(IllegalArgumentException::class.java) { CompletionEvent(event.origin, emptyList()) }
        assertThrows(IllegalArgumentException::class.java) { ProgressCodec.encode(listOf(records()[0], records()[0])) }
    }
}
