package com.bellfamily.bastischool.learning.progress

import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.session.*
import java.io.*
import java.security.MessageDigest

internal class ProgressProblem(val reason: ProgressFailure) : RuntimeException()

/** Fixed schema: unknown fields/record kinds fail closed; never skip data and rewrite a partial store. */
internal object ProgressCodec {
    const val VERSION = 1
    const val MAX_EVENTS = 10_000
    const val MAX_BYTES = 16 * 1024 * 1024
    private const val MAGIC = 0x42505247
    private const val DIGEST_BYTES = 32

    fun encode(records: List<StoredProgressEvent>): ByteArray {
        if (records.size > MAX_EVENTS) throw ProgressProblem(ProgressFailure.CAPACITY)
        require(records.map { it.event.id }.toSet().size == records.size)
        val buffer = ByteArrayOutputStream()
        DataOutputStream(buffer).use { out ->
            out.writeInt(MAGIC); out.writeInt(VERSION); out.writeInt(records.size)
            records.forEachIndexed { index, record ->
                require(record.sequence == index + 1L)
                out.writeLong(record.sequence); out.writeLong(record.recordedAtMillis)
                val event = record.event
                out.writeUTF(event.kind.name)
                val origin = event.origin
                out.writeUTF(origin.session.value); out.writeUTF(origin.activity.value)
                out.writeInt(origin.activityRevision); out.writeInt(origin.contentVersion.schema); out.writeInt(origin.contentVersion.revision)
                when (event) {
                    is AttemptEvent -> {
                        out.evidence(event.evidence); out.writeInt(event.attempt.number)
                        out.writeUTF(event.language.name); out.writeUTF(event.choice.value); out.writeUTF(event.outcome.name)
                        out.support(event.support)
                    }
                    is CompletionEvent -> {
                        out.writeInt(event.tasks.size)
                        event.tasks.forEach {
                            out.evidence(it.evidence); out.writeUTF(it.lastChoice.value); out.writeUTF(it.outcome.name)
                            out.writeInt(it.attempts); out.writeInt(it.retries); out.support(it.support)
                        }
                    }
                }
                if (buffer.size() + DIGEST_BYTES > MAX_BYTES) throw ProgressProblem(ProgressFailure.CAPACITY)
            }
        }
        val payload = buffer.toByteArray()
        return payload + digest(payload)
    }

    fun decode(bytes: ByteArray): List<StoredProgressEvent> {
        if (bytes.size > MAX_BYTES) throw ProgressProblem(ProgressFailure.CAPACITY)
        try {
            require(bytes.size >= 12 + DIGEST_BYTES)
            val payload = bytes.copyOfRange(0, bytes.size - DIGEST_BYTES)
            require(MessageDigest.isEqual(digest(payload), bytes.copyOfRange(payload.size, bytes.size)))
            return DataInputStream(ByteArrayInputStream(payload)).use { input ->
                require(input.readInt() == MAGIC)
                if (input.readInt() != VERSION) throw ProgressProblem(ProgressFailure.INCOMPATIBLE_VERSION)
                val count = input.readInt().also { require(it in 0..MAX_EVENTS) }
                val records = List(count) { index ->
                    val sequence = input.readLong().also { require(it == index + 1L) }
                    val timestamp = input.readLong()
                    val kind = enumValueOf<ProgressEventKind>(input.string())
                    val origin = ProgressOrigin(SessionId(input.string()), ActivityId(input.string()), input.readInt(),
                        ContentVersion(input.readInt(), input.readInt()))
                    val event = when (kind) {
                        ProgressEventKind.ATTEMPT -> {
                            val evidence = input.evidence(origin.session)
                            AttemptEvent(origin, evidence, AttemptId(evidence.task, input.readInt()),
                                enumValueOf(input.string()), ContentId(input.string()), enumValueOf(input.string()), input.support())
                        }
                        ProgressEventKind.SESSION_COMPLETED -> {
                            val size = input.readInt().also { require(it == 5 || it == 10) }
                            CompletionEvent(origin, List(size) {
                                CompletedTask(input.evidence(origin.session), ContentId(input.string()),
                                    enumValueOf(input.string()), input.readInt(), input.readInt(), input.support())
                            })
                        }
                    }
                    StoredProgressEvent(sequence, timestamp, event)
                }
                require(input.available() == 0 && records.map { it.event.id }.toSet().size == records.size)
                require(records.groupBy { it.event.origin.session }.values.all { sameSession ->
                    sameSession.map { it.event.origin }.toSet().size == 1
                })
                records
            }
        } catch (_: IOException) { throw ProgressProblem(ProgressFailure.CORRUPT) }
        catch (_: IllegalArgumentException) { throw ProgressProblem(ProgressFailure.CORRUPT) }
    }

    private fun digest(bytes: ByteArray) = MessageDigest.getInstance("SHA-256").digest(bytes)
    private fun DataOutputStream.evidence(evidence: TaskEvidence) {
        writeInt(evidence.task.ordinal); writeUTF(evidence.definition.value); writeUTF(evidence.skill.value)
        writeUTF(evidence.context.value); writeInt(evidence.difficulty)
    }
    private fun DataInputStream.evidence(session: SessionId) = TaskEvidence(TaskInstanceId(session, readInt()),
        TaskDefinitionId(string()), SkillId(string()), LearningContextId(string()), readInt())
    private fun DataOutputStream.support(support: SupportUse) {
        writeInt(support.replays); writeBoolean(support.hint); writeBoolean(support.parentHelp)
    }
    private fun DataInputStream.support() = SupportUse(readInt(), boolean(), boolean())
    private fun DataInputStream.boolean() = readUnsignedByte().also { require(it in 0..1) } == 1
    private fun DataInputStream.string() = readUTF().also { require(it.length <= 96) }
}
