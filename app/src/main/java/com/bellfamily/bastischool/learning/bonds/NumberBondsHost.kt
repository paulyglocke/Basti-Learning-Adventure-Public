package com.bellfamily.bastischool.learning.bonds

import com.bellfamily.bastischool.learning.models.ContentLanguage
import com.bellfamily.bastischool.learning.progress.*
import com.bellfamily.bastischool.learning.session.*
import java.io.*
import java.security.MessageDigest

/** Write-ahead journal for built quantities and pending shared-progress events. */
class NumberBondsHost(private val disk: ProgressStorage, private val progress: ProgressRepository, private val level: BondLevel) {
    var state: NumberBonds.State? = null; private set
    var failure = false; private set
    private var pending = emptyList<ProgressEvent>()
    private var last: ByteArray? = null
    val hasPending get() = pending.isNotEmpty()

    /** Returns true only when a new round was created; restored rounds open silently. */
    fun open(id: SessionId, round: RoundLength, seed: Long, language: ContentLanguage): Boolean {
        val bytes = disk.access { it.read() }; last = bytes
        if (bytes != null) { load(bytes); require(state?.level == level); flush(); return false }
        val next = NumberBonds.start(id, level, round, seed, language)
        save(next, emptyList()); state = next; pending = emptyList(); failure = false
        return true
    }

    fun again(id: SessionId, round: RoundLength, seed: Long, language: ContentLanguage) {
        check(state?.completed == true && state?.acknowledged == true && !failure && !hasPending && id != state?.id)
        val next = NumberBonds.start(id, level, round, seed, language)
        save(next, emptyList()); state = next; pending = emptyList(); failure = false
    }

    fun dispatch(action: NumberBonds.Action) {
        if (failure || hasPending) return
        val before = state ?: return
        val next = NumberBonds.reduce(before, action)
        if (next == before) return
        val events = when {
            action == NumberBonds.Action.Check -> listOf(NumberBonds.attempt(next))
            next.completed && !before.completed -> listOf(NumberBonds.completion(next))
            else -> emptyList()
        }
        save(next, events); state = next; pending = events
        if (events.isNotEmpty()) flush()
    }

    fun retryWrites() {
        val bytes = disk.access { it.read() } ?: throw IOException("Missing number bonds journal")
        load(bytes); require(state?.level == level); last = bytes; flush()
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

    private fun save(value: NumberBonds.State, events: List<ProgressEvent>) {
        val bytes = encode(value, events)
        try {
            disk.access { tx ->
                val actual = tx.read()
                if (!(actual?.contentEquals(last ?: byteArrayOf()) ?: (last == null))) throw IOException("Counting tray checkpoint changed")
                tx.replace(bytes)
            }
            last = bytes
        } catch (error: IOException) { failure = true; throw error }
        catch (error: SecurityException) { failure = true; throw error }
    }

    private fun load(bytes: ByteArray) { val decoded = decode(bytes); state = decoded.first; pending = decoded.second }

    companion object {
        private const val MAGIC = 0x4E424431
        private const val MAX_CHECKPOINT_BYTES = 32_000

        internal fun encode(state: NumberBonds.State, events: List<ProgressEvent>): ByteArray {
            require(events.size <= 1)
            val buffer = ByteArrayOutputStream()
            DataOutputStream(buffer).use { out ->
                out.writeInt(MAGIC); out.writeInt(NumberBonds.REVISION)
                out.writeInt(NumberBonds.version.schema); out.writeInt(NumberBonds.version.revision)
                out.writeUTF(state.level.name); out.writeUTF(state.id.value); out.writeUTF(state.round.name); out.writeUTF(state.language.name)
                out.writeInt(state.index); out.writeInt(state.placedMask); out.writeBoolean(state.completed); out.writeBoolean(state.acknowledged)
                state.targets.forEach(out::writeInt)
                state.results.forEach { result ->
                    out.writeInt(result.attempts); out.writeInt(result.wrong); out.writeBoolean(result.solved)
                    out.writeInt(result.checkedCount ?: -1); out.writeInt(result.support.replays)
                    out.writeBoolean(result.support.hint); out.writeBoolean(result.support.parentHelp)
                }
                val bytes = ProgressCodec.encode(events.mapIndexed { index, event -> StoredProgressEvent(index + 1L, 0, event) })
                out.writeInt(bytes.size); out.write(bytes)
            }
            val payload = buffer.toByteArray()
            return payload + digest(payload)
        }

        internal fun decode(bytes: ByteArray): Pair<NumberBonds.State, List<ProgressEvent>> {
            require(bytes.size in 100..MAX_CHECKPOINT_BYTES)
            val payload = bytes.copyOfRange(0, bytes.size - 32)
            require(MessageDigest.isEqual(bytes.takeLast(32).toByteArray(), digest(payload)))
            return DataInputStream(ByteArrayInputStream(payload)).use { input ->
                require(input.readInt() == MAGIC && input.readInt() == NumberBonds.REVISION)
                require(input.readInt() == NumberBonds.version.schema && input.readInt() == NumberBonds.version.revision)
                val level = BondLevel.valueOf(input.readUTF()); val id = SessionId(input.readUTF()); val round = RoundLength.valueOf(input.readUTF())
                val language = ContentLanguage.valueOf(input.readUTF()); val index = input.readInt(); val mask = input.readInt()
                val completed = input.bool(); val acknowledged = input.bool()
                val targets = List(round.count) { input.readInt() }
                val results = List(round.count) {
                    val attempts = input.readInt(); val wrong = input.readInt(); val solved = input.bool()
                    val checked = input.readInt().also { require(it in -1..10) }.takeIf { it >= 0 }
                    val support = SupportUse(input.readInt(), input.bool(), input.bool())
                    NumberBonds.Result(attempts, wrong, solved, checked, support)
                }
                val state = NumberBonds.State(id, level, targets, round, language, index, mask, results, completed, acknowledged)
                val length = input.readInt().also { require(it in 1..20_000) }
                val events = ProgressCodec.decode(ByteArray(length).also { input.readFully(it) }).map { it.event }
                require(input.available() == 0 && events.size <= 1)
                events.forEach { event -> when (event) {
                    is AttemptEvent -> require(!state.completed && event == NumberBonds.attempt(state))
                    is CompletionEvent -> require(state.completed && !state.acknowledged && event == NumberBonds.completion(state))
                } }
                require((state.completed && !state.acknowledged) == events.any { it is CompletionEvent })
                state to events
            }
        }

        private fun digest(bytes: ByteArray) = MessageDigest.getInstance("SHA-256").digest(bytes)
        private fun DataInputStream.bool() = readUnsignedByte().also { require(it in 0..1) } == 1
    }
}
