package com.bellfamily.bastischool.learning.colours

import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.progress.*
import com.bellfamily.bastischool.learning.session.*
import com.bellfamily.bastischool.learning.sequencing.OrderedPlacement
import java.io.*
import java.security.MessageDigest

/** Worker-confined WAL host. Each level gets an independent file; events remain pending until acknowledged. */
class RainbowOrderHost(private val disk: ProgressStorage, private val progress: ProgressRepository,
                       private val level: RainbowOrderLevel) {
    var state: RainbowOrderState? = null; private set
    var failure = false; private set
    private var pending = emptyList<ProgressEvent>()
    private var last: ByteArray? = null
    val hasPending get() = pending.isNotEmpty()

    fun open(id: SessionId, seed: Long, language: ContentLanguage) {
        val bytes = disk.access { it.read() }; last = bytes
        if (bytes != null) { load(bytes); require(state?.level == level) { "Rainbow Order level mismatch" }; flush() } else start(id, seed, language)
    }
    fun again(id: SessionId, seed: Long, language: ContentLanguage) {
        check(state?.completed == true && state?.acknowledged == true && !failure && !hasPending && id != state?.id)
        start(id, seed, language)
    }
    private fun start(id: SessionId, seed: Long, language: ContentLanguage) {
        val next = RainbowOrderContent.start(id, level, seed, language)
        save(next, emptyList()); state = next; pending = emptyList(); failure = false
    }
    fun dispatch(action: RainbowOrderAction): SessionEffect.Narrate? {
        val before = state ?: return null
        if (failure || hasPending) return null
        val transition = RainbowOrderContent.reduce(before, action)
        if (transition.state === before && transition.speech == null) return null
        save(transition.state, transition.events)
        state = transition.state; pending = transition.events; flush()
        return transition.speech
    }
    fun retryWrites() {
        val bytes = disk.access { it.read() } ?: throw IOException("Missing Rainbow Order checkpoint")
        load(bytes); last = bytes; flush()
    }

    private fun flush() {
        failure = false
        for (event in pending) if (progress.append(event) !is ProgressWriteResult.Saved) { failure = true; return }
        val current = state ?: return
        val acknowledged = if (current.completed && !current.acknowledged) current.changed(acknowledged = true) else current
        try {
            save(acknowledged, emptyList()); state = acknowledged; pending = emptyList()
        } catch (_: IOException) { failure = true } catch (_: SecurityException) { failure = true }
    }

    private fun save(value: RainbowOrderState, events: List<ProgressEvent>) {
        val encoded = encode(value, events)
        try {
            disk.access { tx ->
                val actual = tx.read()
                if (!(actual?.contentEquals(last ?: byteArrayOf()) ?: (last == null))) throw IOException("Rainbow checkpoint changed")
                tx.replace(encoded)
            }
            last = encoded
        } catch (e: IOException) { failure = true; throw e }
        catch (e: SecurityException) { failure = true; throw e }
    }

    private fun load(bytes: ByteArray) { val decoded = decode(bytes); require(decoded.first.level == level) { "Rainbow Order level mismatch" }; state = decoded.first; pending = decoded.second }

    companion object {
        private const val MAGIC = 0x52424F31 // RBO1

        internal fun encode(s: RainbowOrderState, events: List<ProgressEvent>): ByteArray {
            require(events.size <= 2)
            val buffer = ByteArrayOutputStream()
            DataOutputStream(buffer).use { out ->
                out.writeInt(MAGIC); out.writeInt(RainbowOrderContent.REVISION)
                out.writeInt(RainbowOrderContent.version.schema); out.writeInt(RainbowOrderContent.version.revision)
                out.writeUTF(s.id.value); out.writeUTF(s.level.name); out.writeUTF(s.language.name)
                out.writeLong(s.seed); out.writeBoolean(s.acknowledged)
                out.writeInt(s.choices.size); s.choices.forEach { out.writeUTF(it.value) }
                out.writeInt(s.steps.size)
                s.steps.forEach { p ->
                    out.writeUTF(p.answer.name); out.writeInt(p.attempts); out.writeInt(p.retries)
                    out.writeInt(p.support.replays); out.writeBoolean(p.support.hint); out.writeBoolean(p.support.parentHelp)
                    out.writeUTF(p.lastChoice?.value ?: "")
                }
                val stored = ProgressCodec.encode(events.mapIndexed { index, event -> StoredProgressEvent(index + 1L, 0, event) })
                out.writeInt(stored.size); out.write(stored)
            }
            val payload = buffer.toByteArray()
            return payload + MessageDigest.getInstance("SHA-256").digest(payload)
        }

        internal fun decode(bytes: ByteArray): Pair<RainbowOrderState, List<ProgressEvent>> {
            require(bytes.size in 100..32_000)
            val payload = bytes.copyOfRange(0, bytes.size - 32)
            require(MessageDigest.isEqual(MessageDigest.getInstance("SHA-256").digest(payload), bytes.copyOfRange(payload.size, bytes.size)))
            return DataInputStream(ByteArrayInputStream(payload)).use { input ->
                require(input.readInt() == MAGIC && input.readInt() == RainbowOrderContent.REVISION &&
                    input.readInt() == RainbowOrderContent.version.schema && input.readInt() == RainbowOrderContent.version.revision)
                val id = SessionId(input.readUTF()); val level = RainbowOrderLevel.valueOf(input.readUTF())
                val language = ContentLanguage.valueOf(input.readUTF()); val seed = input.readLong()
                val acknowledged = input.boolean()
                val targetSize = RainbowOrderContent.target(level).size
                val choices = List(input.readInt().also { require(it == targetSize) }) { ContentId(input.readUTF()) }
                val steps = List(input.readInt().also { require(it == targetSize) }) {
                    TaskProgress(AnswerState.valueOf(input.readUTF()), input.readInt(), input.readInt(),
                        SupportUse(input.readInt(), input.boolean(), input.boolean()),
                        input.readUTF().takeIf { it.isNotEmpty() }?.let(::ContentId))
                }
                val state = RainbowOrderState(id, level, language, seed, choices, steps, acknowledged)
                val length = input.readInt().also { require(it in 1..20_000) }
                val events = ProgressCodec.decode(ByteArray(length).also(input::readFully)).map { it.event }
                require(input.available() == 0 && events.size <= 2)
                events.forEach { event -> when (event) {
                    is AttemptEvent -> {
                        val index = event.evidence.task.ordinal - 1
                        require(index in state.target.indices && (index == state.index || index == state.index - 1))
                        require(event == RainbowOrderContent.attempt(state, index))
                    }
                    is CompletionEvent -> require(state.completed && !state.acknowledged && event == RainbowOrderContent.completion(state))
                } }
                require(events.filterIsInstance<AttemptEvent>().size <= 1)
                require((state.completed && !state.acknowledged) == events.any { it is CompletionEvent })
                state to events
            }
        }
        private fun DataInputStream.boolean() = readUnsignedByte().also { require(it in 0..1) } == 1
    }
}

/** Durable pointer to the last selected challenge, separate from both level-owned session journals. */
class RainbowOrderLevelStore(private val disk: ProgressStorage) {
    fun read(): RainbowOrderLevel? = disk.access { it.read()?.let(::decode) }
    fun write(level: RainbowOrderLevel) = disk.access { it.replace(encode(level)) }

    private fun encode(level: RainbowOrderLevel): ByteArray {
        val payload = ByteArrayOutputStream().also { bytes -> DataOutputStream(bytes).use { out ->
            out.writeInt(MAGIC); out.writeInt(RainbowOrderContent.REVISION); out.writeUTF(level.name)
        } }.toByteArray()
        return payload + MessageDigest.getInstance("SHA-256").digest(payload)
    }

    private fun decode(bytes: ByteArray): RainbowOrderLevel {
        require(bytes.size in 40..128)
        val payload = bytes.copyOfRange(0, bytes.size - 32)
        require(MessageDigest.isEqual(MessageDigest.getInstance("SHA-256").digest(payload), bytes.copyOfRange(payload.size, bytes.size)))
        return DataInputStream(ByteArrayInputStream(payload)).use { input ->
            require(input.readInt() == MAGIC && input.readInt() == RainbowOrderContent.REVISION)
            val level = RainbowOrderLevel.valueOf(input.readUTF())
            require(input.available() == 0)
            level
        }
    }

    private companion object {
        const val MAGIC = 0x52424C31 // RBL1
    }
}
