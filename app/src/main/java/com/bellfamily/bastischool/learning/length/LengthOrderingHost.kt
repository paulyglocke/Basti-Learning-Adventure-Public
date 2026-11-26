package com.bellfamily.bastischool.learning.length

import com.bellfamily.bastischool.learning.models.ContentLanguage
import com.bellfamily.bastischool.learning.progress.*
import com.bellfamily.bastischool.learning.session.*
import java.io.*
import java.security.MessageDigest

/** Write-ahead journal for length ordering work and pending shared-progress events. */
class LengthOrderingHost(private val disk: ProgressStorage, private val progress: ProgressRepository) {
    var state: LengthOrdering.State? = null; private set
    var failure = false; private set
    private var pending = emptyList<ProgressEvent>()
    private var last: ByteArray? = null
    val hasPending get() = pending.isNotEmpty()

    /** Returns true only when a new round was created; restored rounds open silently. */
    fun open(id: SessionId, round: RoundLength, seed: Long, language: ContentLanguage): Boolean {
        val bytes = disk.access { it.read() }; last = bytes
        if (bytes != null) { load(bytes); flush(); return false }
        val next = LengthOrdering.start(id, round, seed, language)
        save(next, emptyList()); state = next; pending = emptyList(); failure = false
        return true
    }

    fun again(id: SessionId, round: RoundLength, seed: Long, language: ContentLanguage) {
        check(state?.completed == true && state?.acknowledged == true && !failure && !hasPending && id != state?.id)
        val next = LengthOrdering.start(id, round, seed, language, state?.tasks ?: emptyList())
        save(next, emptyList()); state = next; pending = emptyList(); failure = false
    }

    fun dispatch(action: LengthOrdering.Action) {
        if (failure || hasPending) return
        val before = state ?: return
        val next = LengthOrdering.reduce(before, action)
        if (next == before) return
        val events = when {
            action == LengthOrdering.Action.Check -> listOf(LengthOrdering.attempt(next))
            next.completed && !before.completed -> listOf(LengthOrdering.completion(next))
            else -> emptyList()
        }
        save(next, events); state = next; pending = events
        if (events.isNotEmpty()) flush()
    }

    fun retryWrites() {
        val bytes = disk.access { it.read() } ?: throw IOException("Missing length ordering journal")
        load(bytes); last = bytes; flush()
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

    private fun save(value: LengthOrdering.State, events: List<ProgressEvent>) {
        val bytes = encode(value, events)
        try {
            disk.access { tx ->
                val actual = tx.read()
                if (!(actual?.contentEquals(last ?: byteArrayOf()) ?: (last == null))) throw IOException("LengthOrdering checkpoint changed")
                tx.replace(bytes)
            }
            last = bytes
        } catch (error: IOException) { failure = true; throw error }
        catch (error: SecurityException) { failure = true; throw error }
    }

    private fun load(bytes: ByteArray) { val decoded = decode(bytes); state = decoded.first; pending = decoded.second }

    companion object {
        private const val MAGIC = 0x4c4e4f31
        private const val MAX_CHECKPOINT_BYTES = 32_000

        internal fun encode(state: LengthOrdering.State, events: List<ProgressEvent>): ByteArray {
            require(events.size <= 1)
            val buffer = ByteArrayOutputStream()
            DataOutputStream(buffer).use { out ->
                out.writeInt(MAGIC); out.writeInt(LengthOrdering.REVISION)
                out.writeInt(LengthOrdering.version.schema); out.writeInt(LengthOrdering.version.revision)
                out.writeUTF(state.id.value); out.writeUTF(state.round.name); out.writeUTF(state.language.name)
                out.writeInt(state.index); out.writeBoolean(state.completed); out.writeBoolean(state.acknowledged)
                fun ids(values: List<com.bellfamily.bastischool.learning.models.ContentId>) {
                    out.writeInt(values.size); values.forEach { out.writeInt(LengthOrdering.strips.indexOf(it)) }
                }
                state.tasks.forEach { task -> task.lengths.forEach(out::writeInt); ids(task.presentation); out.writeInt(task.palette) }
                out.writeInt(state.undo.size); state.undo.forEach(::ids)
                state.work.forEach { work ->
                    ids(work.order); out.writeInt(work.revision); out.writeInt(work.attempts); ids(work.checked)
                    out.writeInt(work.support.replays); out.writeBoolean(work.support.hint); out.writeBoolean(work.support.parentHelp)
                    out.writeInt(work.hintPosition ?: -1)
                }
                val bytes = ProgressCodec.encode(events.mapIndexed { index, event -> StoredProgressEvent(index + 1L, 0, event) })
                out.writeInt(bytes.size); out.write(bytes)
            }
            val payload = buffer.toByteArray()
            return payload + digest(payload)
        }

        internal fun decode(bytes: ByteArray): Pair<LengthOrdering.State, List<ProgressEvent>> {
            require(bytes.size in 100..MAX_CHECKPOINT_BYTES)
            val payload = bytes.copyOfRange(0, bytes.size - 32)
            require(MessageDigest.isEqual(bytes.takeLast(32).toByteArray(), digest(payload)))
            return DataInputStream(ByteArrayInputStream(payload)).use { input ->
                require(input.readInt() == MAGIC && input.readInt() == LengthOrdering.REVISION)
                require(input.readInt() == LengthOrdering.version.schema && input.readInt() == LengthOrdering.version.revision)
                val id = SessionId(input.readUTF()); val round = RoundLength.valueOf(input.readUTF())
                val language = ContentLanguage.valueOf(input.readUTF()); val index = input.readInt()
                val completed = input.bool(); val acknowledged = input.bool()
                fun ids() = List(input.readInt().also { require(it in 0..3) }) {
                    LengthOrdering.strips[input.readInt().also { require(it in 0..2) }]
                }
                val tasks = List(round.count) { LengthOrderTask(List(3) { input.readInt() },ids(),input.readInt()) }
                val undo = List(input.readInt().also { require(it in 0..100) }) { ids() }
                val work = List(round.count) {
                    val order=ids(); val revision=input.readInt(); val attempts=input.readInt(); val checked=ids()
                    val support=SupportUse(input.readInt(),input.bool(),input.bool())
                    val hint=input.readInt().also { require(it in -1..2) }.takeIf { it >= 0 }
                    LengthOrderWork(order,revision,attempts,checked,support,hint)
                }
                val state = LengthOrdering.State(id,tasks,round,language,index,work,undo,completed,acknowledged)
                val length = input.readInt().also { require(it in 1..20_000) }
                val events = ProgressCodec.decode(ByteArray(length).also { input.readFully(it) }).map { it.event }
                require(input.available() == 0 && events.size <= 1)
                events.forEach { event -> when (event) {
                    is AttemptEvent -> require(!state.completed && event == LengthOrdering.attempt(state))
                    is CompletionEvent -> require(state.completed && !state.acknowledged && event == LengthOrdering.completion(state))
                } }
                require((state.completed && !state.acknowledged) == events.any { it is CompletionEvent })
                state to events
            }
        }

        private fun digest(bytes: ByteArray) = MessageDigest.getInstance("SHA-256").digest(bytes)
        private fun DataInputStream.bool() = readUnsignedByte().also { require(it in 0..1) } == 1
    }
}
