package com.bellfamily.bastischool.learning.follow

import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.progress.*
import com.bellfamily.bastischool.learning.session.*
import java.io.*
import java.security.MessageDigest

/** Independent write-ahead journal; existing one-step checkpoints are untouched. */
class FollowTwoHost(private val disk: ProgressStorage, private val progress: ProgressRepository) {
    var state: FollowTwoState? = null; private set
    var failure = false; private set
    private var pending = emptyList<ProgressEvent>()
    private var last: ByteArray? = null
    val hasPending get() = pending.isNotEmpty()

    fun open(id: SessionId, round: RoundLength, seed: Long, language: ContentLanguage): SessionEffect.Narrate? {
        val bytes = disk.access { it.read() }; last = bytes
        if (bytes != null) { load(bytes); flush(); return null }
        return start(id, round, seed, language)
    }
    fun again(id: SessionId, round: RoundLength, seed: Long, language: ContentLanguage): SessionEffect.Narrate? {
        check(state?.completed == true && state?.acknowledged == true && !failure && !hasPending && id != state?.id)
        return start(id, round, seed, language)
    }
    private fun start(id: SessionId, round: RoundLength, seed: Long, language: ContentLanguage): SessionEffect.Narrate {
        val next = FollowTwo.generate(id, round, seed, language)
        save(next, emptyList()); state = next; pending = emptyList(); failure = false
        return SessionEffect.Narrate(FollowTwo.instruction(next.task), NarrationKind.INSTRUCTION)
    }
    fun dispatch(action: FollowTwoAction): SessionEffect.Narrate? {
        val before = state ?: return null
        if (failure || hasPending) return null
        val transition = FollowTwo.reduce(before, action)
        if (transition.state === before && transition.speech == null) return null
        save(transition.state, transition.events)
        state = transition.state; pending = transition.events; flush()
        return transition.speech
    }
    fun retryWrites() {
        val bytes = disk.access { it.read() } ?: throw IOException("Missing two-step checkpoint")
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
    private fun save(snapshot: FollowTwoState, events: List<ProgressEvent>) {
        val bytes = encode(snapshot, events)
        try {
            disk.access { tx ->
                val actual = tx.read()
                if (!(actual?.contentEquals(last ?: byteArrayOf()) ?: (last == null))) throw IOException("Two-step checkpoint changed")
                tx.replace(bytes)
            }
            last = bytes
        } catch (e: IOException) { failure = true; throw e }
        catch (e: SecurityException) { failure = true; throw e }
    }
    private fun load(bytes: ByteArray) { val decoded = decode(bytes); state = decoded.first; pending = decoded.second }

    companion object {
        private const val MAGIC = 0x46545732 // FTW2
        internal fun encode(state: FollowTwoState, events: List<ProgressEvent>): ByteArray {
            val buffer = ByteArrayOutputStream()
            DataOutputStream(buffer).use { out ->
                out.writeInt(MAGIC); out.writeInt(FollowTwo.REVISION)
                out.writeInt(FollowContent.repository.version.schema); out.writeInt(FollowContent.repository.version.revision)
                out.writeUTF(state.id.value); out.writeUTF(state.round.name); out.writeUTF(state.language.name)
                out.writeInt(state.questionIndex); out.writeBoolean(state.acknowledged)
                state.tasks.forEach { task ->
                    out.writeUTF(task.first.value); out.writeUTF(task.second.value)
                    task.choices.forEach { out.writeUTF(it.value) }
                }
                state.steps.flatten().forEach { step ->
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
        internal fun decode(bytes: ByteArray): Pair<FollowTwoState, List<ProgressEvent>> {
            require(bytes.size in 100..64_000)
            val payload = bytes.copyOfRange(0, bytes.size - 32)
            require(MessageDigest.isEqual(bytes.takeLast(32).toByteArray(), MessageDigest.getInstance("SHA-256").digest(payload)))
            return DataInputStream(ByteArrayInputStream(payload)).use { input ->
                require(input.readInt() == MAGIC && input.readInt() == FollowTwo.REVISION &&
                    input.readInt() == FollowContent.repository.version.schema && input.readInt() == FollowContent.repository.version.revision)
                val id = SessionId(input.readUTF()); val round = RoundLength.valueOf(input.readUTF())
                val language = ContentLanguage.valueOf(input.readUTF()); val questionIndex = input.readInt()
                val acknowledged = input.bool()
                val tasks = List(round.count) {
                    FollowTwoTask(ContentId(input.readUTF()), ContentId(input.readUTF()), List(4) { ContentId(input.readUTF()) })
                }
                val steps = List(round.count) { List(2) {
                    TaskProgress(AnswerState.valueOf(input.readUTF()), input.readInt(), input.readInt(),
                        SupportUse(input.readInt(), input.bool(), input.bool()), input.readUTF().takeIf { it.isNotEmpty() }?.let(::ContentId))
                } }
                val state = FollowTwoState(id, round, language, tasks, steps, questionIndex, acknowledged)
                val length = input.readInt().also { require(it in 1..60_000) }
                val events = ProgressCodec.decode(ByteArray(length).also(input::readFully)).map { it.event }
                require(input.available() == 0 && events.size <= 1)
                events.forEach { event -> when (event) {
                    is AttemptEvent -> {
                        require(!state.completed && event.evidence == FollowTwo.evidence(state, state.questionIndex))
                        require(event.origin.activity == FollowTwo.activity && event.origin.session == id && event.language == language)
                        require(event == FollowTwo.attempt(state, event.choice, event.outcome, event.attempt.number))
                        require(event.attempt.number == state.steps[state.questionIndex].sumOf { it.retries } + 1)
                        if (event.outcome == AttemptOutcome.CORRECT) require(state.readyToContinue && event.choice == state.task.second)
                        else require(state.current.answer == AnswerState.RETRY_AVAILABLE && state.current.lastChoice == event.choice)
                    }
                    is CompletionEvent -> require(state.completed && !state.acknowledged && event == FollowTwo.completion(state))
                } }
                require((state.completed && !state.acknowledged) == events.any { it is CompletionEvent })
                state to events
            }
        }
        private fun DataInputStream.bool() = readUnsignedByte().also { require(it in 0..1) } == 1
    }
}
