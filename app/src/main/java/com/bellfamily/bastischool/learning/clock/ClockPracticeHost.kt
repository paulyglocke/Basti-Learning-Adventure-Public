package com.bellfamily.bastischool.learning.clock

import com.bellfamily.bastischool.learning.models.ContentLanguage
import com.bellfamily.bastischool.learning.progress.*
import com.bellfamily.bastischool.learning.session.*
import java.io.*
import java.security.MessageDigest

/** Worker-confined write-ahead journal, following DurableSessionHost/SortingHost delivery.
 * No schema changes to Explore or existing activity journals. */
class ClockPracticeHost(private val disk: ProgressStorage, private val progress: ProgressRepository) {
    var state: ClockPractice.State? = null; private set
    var failure = false; private set
    private var pending = emptyList<ProgressEvent>()
    private var last: ByteArray? = null
    val hasPending get() = pending.isNotEmpty()
    fun open(id: SessionId, seed: Long, language: ContentLanguage) {
        val bytes = disk.access { it.read() }; last = bytes
        if (bytes == null) start(id, seed, language) else { load(bytes); flush() }
    }
    fun again(id: SessionId, seed: Long, language: ContentLanguage) {
        check(state?.completed == true && state?.acknowledged == true && !failure && !hasPending && id != state?.id)
        start(id, seed, language)
    }
    private fun start(id: SessionId, seed: Long, language: ContentLanguage) {
        val next = ClockPractice.State(id, ClockPractice.generate(seed), language)
        save(next, emptyList()); state = next; pending = emptyList(); failure = false
    }
    fun dispatch(action: ClockPractice.Action) {
        if (failure || hasPending) return
        val before = state ?: return
        val next = ClockPractice.reduce(before, action)
        if (next == before) return
        val events = when {
            action == ClockPractice.Action.Check -> listOf(ClockPractice.attempt(next))
            next.completed && !before.completed -> listOf(ClockPractice.completion(next))
            else -> emptyList()
        }
        save(next, events); state = next; pending = events
        if (events.isNotEmpty()) flush()
    }
    fun retryWrites() {
        val bytes = disk.access { it.read() } ?: throw IOException("Missing Clock practice journal")
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
    private fun save(s: ClockPractice.State, events: List<ProgressEvent>) {
        val bytes = encode(s, events)
        try {
            disk.access { tx ->
                val actual = tx.read()
                if (!(actual?.contentEquals(last ?: byteArrayOf()) ?: (last == null))) throw IOException("Clock practice journal changed")
                tx.replace(bytes)
            }
            last = bytes
        } catch (e: IOException) { failure = true; throw e }
        catch (e: SecurityException) { failure = true; throw e }
    }
    private fun load(bytes: ByteArray) { val pair = decode(bytes); state = pair.first; pending = pair.second }
    companion object {
        internal fun encode(s: ClockPractice.State, pending: List<ProgressEvent>): ByteArray {
            val buffer = ByteArrayOutputStream()
            DataOutputStream(buffer).use { o ->
                o.writeInt(0x434C4B50); o.writeInt(ClockPractice.REVISION)
                o.writeInt(ClockPractice.version.schema); o.writeInt(ClockPractice.version.revision)
                o.writeUTF(s.id.value); o.writeUTF(s.language.name); o.writeInt(s.index)
                o.time(s.position); o.writeBoolean(s.completed); o.writeBoolean(s.acknowledged)
                s.targets.forEach { o.time(it) }
                s.results.forEach { r ->
                    o.writeInt(r.attempts); o.writeInt(r.wrong); o.writeBoolean(r.solved)
                    o.writeInt(r.support.replays); o.writeBoolean(r.support.hint); o.writeBoolean(r.support.parentHelp)
                    o.writeBoolean(r.checked != null); r.checked?.let { o.time(it) }
                }
                val events = ProgressCodec.encode(pending.mapIndexed { i, e -> StoredProgressEvent(i + 1L, 0, e) })
                o.writeInt(events.size); o.write(events)
            }
            val payload = buffer.toByteArray()
            return payload + digest(payload)
        }
        internal fun decode(bytes: ByteArray): Pair<ClockPractice.State, List<ProgressEvent>> {
            require(bytes.size in 100..32000)
            val payload = bytes.copyOfRange(0, bytes.size - 32)
            require(MessageDigest.isEqual(bytes.takeLast(32).toByteArray(), digest(payload)))
            return DataInputStream(ByteArrayInputStream(payload)).use { i ->
                require(i.readInt() == 0x434C4B50 && i.readInt() == ClockPractice.REVISION)
                require(i.readInt() == ClockPractice.version.schema && i.readInt() == ClockPractice.version.revision)
                val id = SessionId(i.readUTF()); val language = ContentLanguage.valueOf(i.readUTF()); val index = i.readInt()
                val position = i.time(); val completed = i.bool(); val acknowledged = i.bool()
                val targets = List(8) { i.time() }
                val results = List(8) {
                    ClockPractice.Result(i.readInt(), i.readInt(), i.bool(), SupportUse(i.readInt(), i.bool(), i.bool()), if (i.bool()) i.time() else null)
                }
                val state = ClockPractice.State(id, targets, language, index, position, results, completed, acknowledged)
                val length = i.readInt().also { require(it in 1..20000) }
                val events = ProgressCodec.decode(ByteArray(length).also { i.readFully(it) }).map { it.event }
                require(i.available() == 0 && events.size <= 1)
                events.forEach { e -> when (e) {
                    is AttemptEvent -> require(!state.completed && e == ClockPractice.attempt(state))
                    is CompletionEvent -> require(state.completed && !state.acknowledged && e == ClockPractice.completion(state))
                } }
                require((state.completed && !state.acknowledged) == events.any { it is CompletionEvent })
                state to events
            }
        }
        private fun digest(bytes: ByteArray) = MessageDigest.getInstance("SHA-256").digest(bytes)
        private fun DataOutputStream.time(t: ClockTime) { writeInt(t.hour); writeInt(t.minute) }
        private fun DataInputStream.time() = ClockTime(readInt(), readInt())
        private fun DataInputStream.bool() = readUnsignedByte().also { require(it in 0..1) } == 1
    }
}
