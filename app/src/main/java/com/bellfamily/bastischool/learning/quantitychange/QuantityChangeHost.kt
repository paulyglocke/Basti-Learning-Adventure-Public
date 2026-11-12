package com.bellfamily.bastischool.learning.quantitychange

import com.bellfamily.bastischool.learning.models.ContentLanguage
import com.bellfamily.bastischool.learning.progress.*
import com.bellfamily.bastischool.learning.session.*
import java.io.*
import java.security.MessageDigest

/** Write-ahead journal for quantity changes and pending shared-progress events. */
class QuantityChangeHost(private val disk: ProgressStorage, private val progress: ProgressRepository, private val config: ChangeConfig) {
    var state: QuantityChange.State? = null; private set
    var failure = false; private set
    private var pending = emptyList<ProgressEvent>()
    private var last: ByteArray? = null
    val hasPending get() = pending.isNotEmpty()

    /** Returns true only when a new round was created; restored rounds open silently. */
    fun open(id: SessionId, round: RoundLength, seed: Long, language: ContentLanguage): Boolean {
        val bytes = disk.access { it.read() }; last = bytes
        if (bytes != null) { load(bytes); require(state?.config == config); flush(); return false }
        val next = QuantityChange.start(id, config, round, seed, language)
        save(next, emptyList()); state = next; pending = emptyList(); failure = false
        return true
    }

    fun again(id: SessionId, round: RoundLength, seed: Long, language: ContentLanguage) {
        check(state?.completed == true && state?.acknowledged == true && !failure && !hasPending && id != state?.id)
        val next = QuantityChange.start(id, config, round, seed, language, state?.tasks ?: emptyList())
        save(next, emptyList()); state = next; pending = emptyList(); failure = false
    }

    fun dispatch(action: QuantityChange.Action) {
        if (failure || hasPending) return
        val before = state ?: return
        val next = QuantityChange.reduce(before, action)
        if (next == before) return
        val events = when {
            action == QuantityChange.Action.Check -> listOf(QuantityChange.attempt(next))
            next.completed && !before.completed -> listOf(QuantityChange.completion(next))
            else -> emptyList()
        }
        save(next, events); state = next; pending = events
        if (events.isNotEmpty()) flush()
    }

    fun retryWrites() {
        val bytes = disk.access { it.read() } ?: throw IOException("Missing quantity change journal")
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

    private fun save(value: QuantityChange.State, events: List<ProgressEvent>) {
        val bytes = encode(value, events)
        try {
            disk.access { tx ->
                val actual = tx.read()
                if (!(actual?.contentEquals(last ?: byteArrayOf()) ?: (last == null))) throw IOException("Quantity change checkpoint changed")
                tx.replace(bytes)
            }
            last = bytes
        } catch (error: IOException) { failure = true; throw error }
        catch (error: SecurityException) { failure = true; throw error }
    }

    private fun load(bytes: ByteArray) { val decoded = decode(bytes); state = decoded.first; pending = decoded.second }

    companion object {
        private const val MAGIC = 0x51434831
        private const val MAX_CHECKPOINT_BYTES = 32_000

        internal fun encode(state: QuantityChange.State, events: List<ProgressEvent>): ByteArray {
            require(events.size <= 1)
            val buffer = ByteArrayOutputStream()
            DataOutputStream(buffer).use { out ->
                out.writeInt(MAGIC); out.writeInt(QuantityChange.REVISION)
                out.writeInt(QuantityChange.version.schema); out.writeInt(QuantityChange.version.revision)
                out.writeUTF(state.config.mode.name); out.writeUTF(state.config.range.name); out.writeUTF(state.id.value); out.writeUTF(state.round.name); out.writeUTF(state.language.name)
                out.writeInt(state.index); out.writeInt(state.mask); out.writeBoolean(state.completed); out.writeBoolean(state.acknowledged)
                state.tasks.forEach { task -> out.writeInt(task.start); out.writeUTF(task.animal.value) }
                out.writeInt(state.undoMasks.size); state.undoMasks.forEach(out::writeInt)
                state.results.forEach { result ->
                    out.writeInt(result.attempts); out.writeInt(result.wrong); out.writeBoolean(result.solved)
                    out.writeInt(result.checkedCount ?: -1)
                    out.writeInt(result.support.replays); out.writeBoolean(result.support.hint); out.writeBoolean(result.support.parentHelp)
                }
                val bytes = ProgressCodec.encode(events.mapIndexed { index, event -> StoredProgressEvent(index + 1L, 0, event) })
                out.writeInt(bytes.size); out.write(bytes)
            }
            val payload = buffer.toByteArray()
            return payload + digest(payload)
        }

        internal fun decode(bytes: ByteArray): Pair<QuantityChange.State, List<ProgressEvent>> {
            require(bytes.size in 100..MAX_CHECKPOINT_BYTES)
            val payload = bytes.copyOfRange(0, bytes.size - 32)
            require(MessageDigest.isEqual(bytes.takeLast(32).toByteArray(), digest(payload)))
            return DataInputStream(ByteArrayInputStream(payload)).use { input ->
                require(input.readInt() == MAGIC && input.readInt() == QuantityChange.REVISION)
                require(input.readInt() == QuantityChange.version.schema && input.readInt() == QuantityChange.version.revision)
                val config = ChangeConfig(ChangeMode.valueOf(input.readUTF()),ChangeRange.valueOf(input.readUTF()))
                val id = SessionId(input.readUTF()); val round = RoundLength.valueOf(input.readUTF())
                val language = ContentLanguage.valueOf(input.readUTF()); val index = input.readInt(); val mask = input.readInt()
                val completed = input.bool(); val acknowledged = input.bool()
                val tasks = List(round.count) { ChangeTask(input.readInt(),com.bellfamily.bastischool.learning.models.ContentId(input.readUTF())) }
                val undo = List(input.readInt().also { require(it in 0..100) }) { input.readInt() }
                val results = List(round.count) {
                    val attempts = input.readInt(); val wrong = input.readInt(); val solved = input.bool()
                    val checked = input.readInt().also { require(it in -1..config.range.limit) }.takeIf { it>=0 }
                    QuantityChange.Result(attempts,wrong,solved,checked,SupportUse(input.readInt(),input.bool(),input.bool()))
                }
                val state = QuantityChange.State(id,config,tasks,round,language,index,mask,undo,results,completed,acknowledged)
                val length = input.readInt().also { require(it in 1..20_000) }
                val events = ProgressCodec.decode(ByteArray(length).also { input.readFully(it) }).map { it.event }
                require(input.available() == 0 && events.size <= 1)
                events.forEach { event -> when (event) {
                    is AttemptEvent -> require(!state.completed && event == QuantityChange.attempt(state))
                    is CompletionEvent -> require(state.completed && !state.acknowledged && event == QuantityChange.completion(state))
                } }
                require((state.completed && !state.acknowledged) == events.any { it is CompletionEvent })
                state to events
            }
        }

        private fun digest(bytes: ByteArray) = MessageDigest.getInstance("SHA-256").digest(bytes)
        private fun DataInputStream.bool() = readUnsignedByte().also { require(it in 0..1) } == 1
    }
}
