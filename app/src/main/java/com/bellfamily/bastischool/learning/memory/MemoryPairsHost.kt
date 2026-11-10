package com.bellfamily.bastischool.learning.memory

import com.bellfamily.bastischool.learning.models.ContentLanguage
import com.bellfamily.bastischool.learning.progress.*
import com.bellfamily.bastischool.learning.session.*
import java.io.*
import java.security.MessageDigest

/** Write-ahead journal for memory boards and pending shared-progress events. */
class MemoryPairsHost(private val disk: ProgressStorage, private val progress: ProgressRepository, private val config: MemoryConfig) {
    var state: MemoryPairs.State? = null; private set
    var failure = false; private set
    private var pending = emptyList<ProgressEvent>()
    private var last: ByteArray? = null
    val hasPending get() = pending.isNotEmpty()

    /** Returns true only when a new round was created; restored rounds open silently. */
    fun open(id: SessionId, seed: Long, language: ContentLanguage): Boolean {
        val bytes = disk.access { it.read() }; last = bytes
        if (bytes != null) { load(bytes); require(state?.config == config); flush(); return false }
        val next = MemoryPairs.start(id, config, seed, language)
        save(next, emptyList()); state = next; pending = emptyList(); failure = false
        return true
    }

    fun again(id: SessionId, seed: Long, language: ContentLanguage) {
        check(state?.completed == true && state?.acknowledged == true && !failure && !hasPending && id != state?.id)
        val next = MemoryPairs.start(id, config, seed, language, state)
        save(next, emptyList()); state = next; pending = emptyList(); failure = false
    }

    fun dispatch(action: MemoryPairs.Action) {
        if (failure || hasPending) return
        val before = state ?: return
        val next = MemoryPairs.reduce(before, action)
        if (next == before) return
        val events = when {
            action is MemoryPairs.Action.Reveal && next.revealed.size == 2 -> listOf(MemoryPairs.attempt(next))
            next.completed && !before.completed -> listOf(MemoryPairs.completion(next))
            else -> emptyList()
        }
        save(next, events); state = next; pending = events
        if (events.isNotEmpty()) flush()
    }

    fun retryWrites() {
        val bytes = disk.access { it.read() } ?: throw IOException("Missing memory board journal")
        load(bytes); require(state?.config == config); last = bytes; flush()
    }

    private fun flush() {
        failure = false
        for (event in pending) if (progress.append(event) !is ProgressWriteResult.Saved) { failure = true; return }
        val current = state ?: return
        val next = if (current.completed) current.copy(acknowledged = true) else current
        try { save(next, emptyList()); state = next; pending = emptyList() }
        catch (_: IOException) { failure = true }
        catch (_: SecurityException) { failure = true }
    }

    private fun save(value: MemoryPairs.State, events: List<ProgressEvent>) {
        val bytes = encode(value, events)
        try {
            disk.access { tx ->
                val actual = tx.read()
                if (!(actual?.contentEquals(last ?: byteArrayOf()) ?: (last == null))) throw IOException("Memory board checkpoint changed")
                tx.replace(bytes)
            }
            last = bytes
        } catch (error: IOException) { failure = true; throw error }
        catch (error: SecurityException) { failure = true; throw error }
    }

    private fun load(bytes: ByteArray) { val decoded = decode(bytes); state = decoded.first; pending = decoded.second }

    companion object {
        private const val MAGIC = 0x4D505231
        private const val MAX_CHECKPOINT_BYTES = 32_000

        internal fun encode(state: MemoryPairs.State, events: List<ProgressEvent>): ByteArray {
            require(events.size <= 1)
            val buffer = ByteArrayOutputStream()
            DataOutputStream(buffer).use { out ->
                out.writeInt(MAGIC); out.writeInt(MemoryPairs.REVISION)
                out.writeInt(MemoryPairs.version.schema); out.writeInt(MemoryPairs.version.revision)
                out.writeUTF(state.config.mode.name); out.writeInt(state.config.pairs)
                out.writeUTF(state.id.value); out.writeUTF(state.language.name)
                state.deck.forEach { out.writeUTF(it.pair.value); out.writeUTF(it.face.name); out.writeInt(it.copy) }
                out.writeInt(state.revealed.size); state.revealed.forEach(out::writeInt)
                out.writeInt(state.matchedMask); out.writeUTF(state.phase.name); out.writeBoolean(state.acknowledged)
                state.exposures.forEach(out::writeInt)
                state.results.forEach { result ->
                    out.writeInt(result.attempts); out.writeInt(result.wrong); out.writeBoolean(result.solved)
                    out.writeInt(result.support.replays); out.writeBoolean(result.support.hint); out.writeBoolean(result.support.parentHelp)
                }
                val bytes = ProgressCodec.encode(events.mapIndexed { index, event -> StoredProgressEvent(index + 1L, 0, event) })
                out.writeInt(bytes.size); out.write(bytes)
            }
            val payload = buffer.toByteArray()
            return payload + digest(payload)
        }

        internal fun decode(bytes: ByteArray): Pair<MemoryPairs.State, List<ProgressEvent>> {
            require(bytes.size in 100..MAX_CHECKPOINT_BYTES)
            val payload = bytes.copyOfRange(0, bytes.size - 32)
            require(MessageDigest.isEqual(bytes.takeLast(32).toByteArray(), digest(payload)))
            return DataInputStream(ByteArrayInputStream(payload)).use { input ->
                require(input.readInt() == MAGIC && input.readInt() == MemoryPairs.REVISION)
                require(input.readInt() == MemoryPairs.version.schema && input.readInt() == MemoryPairs.version.revision)
                val config = MemoryConfig(MemoryMode.valueOf(input.readUTF()), input.readInt())
                val id = SessionId(input.readUTF()); val language = ContentLanguage.valueOf(input.readUTF())
                val deck = List(config.pairs*2) { MemoryCard(com.bellfamily.bastischool.learning.models.ContentId(input.readUTF()),MemoryFace.valueOf(input.readUTF()),input.readInt()) }
                val revealed = List(input.readInt().also { require(it in 0..2) }) { input.readInt() }
                val mask = input.readInt(); val phase = MemoryPairs.Phase.valueOf(input.readUTF()); val acknowledged = input.bool()
                val exposures = List(deck.size) { input.readInt() }
                val results = List(config.pairs) {
                    MemoryPairs.Result(input.readInt(), input.readInt(), input.bool(), SupportUse(input.readInt(), input.bool(), input.bool()))
                }
                val state = MemoryPairs.State(id,config,deck,language,revealed,mask,phase,results,exposures,acknowledged)
                val length = input.readInt().also { require(it in 1..20_000) }
                val events = ProgressCodec.decode(ByteArray(length).also { input.readFully(it) }).map { it.event }
                require(input.available() == 0 && events.size <= 1)
                events.forEach { event -> when (event) {
                    is AttemptEvent -> require(!state.completed && event == MemoryPairs.attempt(state))
                    is CompletionEvent -> require(state.completed && !state.acknowledged && event == MemoryPairs.completion(state))
                } }
                require((state.completed && !state.acknowledged) == events.any { it is CompletionEvent })
                state to events
            }
        }

        private fun digest(bytes: ByteArray) = MessageDigest.getInstance("SHA-256").digest(bytes)
        private fun DataInputStream.bool() = readUnsignedByte().also { require(it in 0..1) } == 1
    }
}
