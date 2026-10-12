package com.bellfamily.bastischool.learning.months

import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.progress.*
import com.bellfamily.bastischool.learning.session.*
import java.io.*
import java.security.MessageDigest

/** Worker-confined exact checkpoint and write-ahead delivery for the three placements. */
class MonthsBuildYearHost(private val disk: ProgressStorage, private val progress: ProgressRepository) {
    var state: MonthsBuildYearState? = null; private set
    var failure = false; private set
    private var pending = emptyList<ProgressEvent>()
    private var last: ByteArray? = null
    val hasPending get() = pending.isNotEmpty()

    fun open(id: SessionId, seed: Long, language: ContentLanguage): SessionEffect.Narrate? {
        val bytes = disk.access { it.read() }; last = bytes
        if (bytes != null) { load(bytes); flush(); return null }
        return start(id, seed, language)
    }
    fun again(id: SessionId, seed: Long, language: ContentLanguage): SessionEffect.Narrate? {
        check(state?.completed == true && state?.acknowledged == true && !failure && !hasPending && id != state?.id)
        return start(id, seed, language)
    }
    private fun start(id: SessionId, seed: Long, language: ContentLanguage): SessionEffect.Narrate {
        val next = MonthsBuildYear.start(id, seed, language)
        save(next, emptyList()); state = next; pending = emptyList(); failure = false
        return SessionEffect.Narrate(MonthsBuildYear.prompt(next), NarrationKind.INSTRUCTION)
    }
    fun dispatch(action: MonthsBuildYearAction): SessionEffect.Narrate? {
        val before = state ?: return null
        if (failure || hasPending) return null
        val transition = MonthsBuildYear.reduce(before, action)
        if (transition.state === before && transition.speech == null) return null
        save(transition.state, transition.events); state = transition.state; pending = transition.events; flush()
        return transition.speech
    }
    fun retryWrites() {
        val bytes = disk.access { it.read() } ?: throw IOException("Missing Months ordering checkpoint")
        load(bytes); last = bytes; flush()
    }
    private fun flush() {
        failure = false
        for (event in pending) if (progress.append(event) !is ProgressWriteResult.Saved) { failure = true; return }
        val current = state ?: return
        val acknowledged = if (current.completed) current.changed(acknowledged = true) else current
        try { save(acknowledged, emptyList()); state = acknowledged; pending = emptyList() }
        catch (_: IOException) { failure = true }
        catch (_: SecurityException) { failure = true }
    }
    private fun save(snapshot: MonthsBuildYearState, events: List<ProgressEvent>) {
        val bytes = encode(snapshot, events)
        try {
            disk.access { tx ->
                val actual = tx.read()
                if (!(actual?.contentEquals(last ?: byteArrayOf()) ?: (last == null))) throw IOException("Months ordering checkpoint changed")
                tx.replace(bytes)
            }
            last = bytes
        } catch (e: IOException) { failure = true; throw e }
        catch (e: SecurityException) { failure = true; throw e }
    }
    private fun load(bytes: ByteArray) { val decoded = decode(bytes); state = decoded.first; pending = decoded.second }

    companion object {
        private const val MAGIC = 0x4D425931 // MBY1
        internal fun encode(state: MonthsBuildYearState, events: List<ProgressEvent>): ByteArray {
            val buffer = ByteArrayOutputStream()
            DataOutputStream(buffer).use { out ->
                out.writeInt(MAGIC); out.writeInt(MonthsBuildYear.REVISION)
                out.writeInt(MonthContent.repository.version.schema); out.writeInt(MonthContent.repository.version.revision)
                out.writeUTF(state.id.value); out.writeUTF(state.start.value); out.writeUTF(state.language.name); out.writeBoolean(state.acknowledged)
                state.choices.forEach { out.writeUTF(it.value) }
                state.steps.forEach { step ->
                    out.writeUTF(step.answer.name); out.writeInt(step.attempts); out.writeInt(step.retries)
                    out.writeInt(step.support.replays); out.writeBoolean(step.support.hint); out.writeBoolean(step.support.parentHelp)
                    out.writeUTF(step.lastChoice?.value ?: "")
                }
                val stored = ProgressCodec.encode(events.mapIndexed { index, event -> StoredProgressEvent(index + 1L, 0, event) })
                out.writeInt(stored.size); out.write(stored)
            }
            val payload = buffer.toByteArray()
            return payload + MessageDigest.getInstance("SHA-256").digest(payload)
        }
        internal fun decode(bytes: ByteArray): Pair<MonthsBuildYearState, List<ProgressEvent>> {
            require(bytes.size in 100..32_000)
            val payload = bytes.copyOfRange(0, bytes.size - 32)
            require(MessageDigest.isEqual(bytes.takeLast(32).toByteArray(), MessageDigest.getInstance("SHA-256").digest(payload)))
            return DataInputStream(ByteArrayInputStream(payload)).use { input ->
                require(input.readInt() == MAGIC && input.readInt() == MonthsBuildYear.REVISION &&
                    input.readInt() == MonthContent.repository.version.schema && input.readInt() == MonthContent.repository.version.revision)
                val id = SessionId(input.readUTF()); val start = ContentId(input.readUTF())
                val language = ContentLanguage.valueOf(input.readUTF()); val acknowledged = input.bool()
                val choices = List(3) { ContentId(input.readUTF()) }
                val steps = List(3) { TaskProgress(AnswerState.valueOf(input.readUTF()), input.readInt(), input.readInt(),
                    SupportUse(input.readInt(), input.bool(), input.bool()), input.readUTF().takeIf { it.isNotEmpty() }?.let(::ContentId)) }
                val state = MonthsBuildYearState(id, start, language, choices, steps, acknowledged)
                val length = input.readInt().also { require(it in 1..20_000) }
                val events = ProgressCodec.decode(ByteArray(length).also(input::readFully)).map { it.event }
                require(input.available() == 0 && events.size <= 2)
                events.forEach { event -> when (event) {
                    is AttemptEvent -> {
                        val index = event.evidence.task.ordinal - 1
                        require(index in 0..2 && (index == state.index || index == state.index - 1) && state.steps[index].answer != AnswerState.UNANSWERED)
                        require(event == MonthsBuildYear.attempt(state, index))
                    }
                    is CompletionEvent -> require(state.completed && !state.acknowledged && event == MonthsBuildYear.completion(state))
                } }
                require(events.filterIsInstance<AttemptEvent>().size <= 1)
                require((state.completed && !state.acknowledged) == events.any { it is CompletionEvent })
                state to events
            }
        }
        private fun DataInputStream.bool() = readUnsignedByte().also { require(it in 0..1) } == 1
    }
}
