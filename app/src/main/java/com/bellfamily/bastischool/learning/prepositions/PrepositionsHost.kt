package com.bellfamily.bastischool.learning.prepositions

import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.progress.*
import com.bellfamily.bastischool.learning.session.*
import java.io.*
import java.security.MessageDigest

/** Activity-specific write-ahead checkpoint. Worker-thread confined; never retains a UI/platform object. */
class PrepositionsHost(private val journal: ProgressStorage, private val progress: ProgressRepository) {
    var state: SessionState? = null
        private set
    private var pending = emptyList<ProgressEvent>()
    private var lastJournal: ByteArray? = null
    var failure: Boolean = false
        private set
    val hasPending get() = pending.isNotEmpty()

    fun open(id: SessionId, round: RoundLength, seed: Long, language: ContentLanguage): List<SessionEffect> {
        val saved = journal.access { it.read() }
        lastJournal = saved
        if (saved != null) {
            val restored = decode(saved)
            state = restored.first; pending = restored.second
            flush()
            return emptyList() // Recovery is always silent.
        }
        return start(id, round, seed, language)
    }
    fun newRound(id: SessionId, round: RoundLength, seed: Long, language: ContentLanguage): List<SessionEffect> {
        check(state?.phase == SessionPhase.COMPLETED && pending.isEmpty() && !failure && id != state?.plan?.id)
        return start(id, round, seed, language)
    }
    private fun start(id: SessionId, round: RoundLength, seed: Long, language: ContentLanguage): List<SessionEffect> {
        val generated = PrepositionsContent.generate(id, round, seed) as? GenerationResult.Generated ?: throw IOException("Invalid content")
        val transition = SessionReducer.start(generated.plan, language, PrepositionsContent.repository)
        save(transition.state, emptyList())
        state = transition.state; pending = emptyList(); failure = false
        return transition.effects
    }
    fun dispatch(action: SessionAction): List<SessionEffect> {
        val before = state ?: return emptyList()
        if (pending.isNotEmpty() || failure) return emptyList()
        val next = SessionReducer.reduce(before, action)
        if (next.state === before && next.effects.isEmpty()) return emptyList()
        val events = mutableListOf<ProgressEvent>()
        val capture = object : ProgressRepository {
            override fun append(event: ProgressEvent): ProgressWriteResult {
                events += event
                return ProgressWriteResult.Saved(StoredProgressEvent(1, 0, event), true)
            }
            override fun read(query: ProgressQuery) = ProgressReadResult.Events(emptyList())
        }
        val recorder = SessionProgressRecorder(before.plan, capture)
        next.effects.forEach { effect ->
            val result = recorder.record(effect)
            check(result !is ProgressEffectResult.Handled || result.write is ProgressWriteResult.Saved)
        }
        // Do not publish an accepted answer/score until its checkpoint AND effect are durable.
        save(next.state, events)
        state = next.state; pending = events
        flush()
        return next.effects.filter { it is SessionEffect.Narrate || it == SessionEffect.CancelNarration }
    }
    fun retryWrites() {
        // Re-read first: a failed commit may already have replaced the journal.
        val saved = journal.access { it.read() } ?: throw IOException("Missing checkpoint")
        val restored = decode(saved)
        lastJournal = saved
        state = restored.first; pending = restored.second
        flush()
    }

    private fun flush() {
        failure = false
        val current = state ?: return
        for (event in pending) if (progress.append(event) !is ProgressWriteResult.Saved) { failure = true; return }
        val acknowledged = if (current.phase == SessionPhase.COMPLETED && current.completion == CompletionState.PENDING)
            SessionReducer.reduce(current, SessionAction.CompletionResult(current.completionRequestId!!, true)).state else current
        // Failure here leaves the durable pending record available for idempotent redelivery.
        try { save(acknowledged, emptyList()); state = acknowledged; pending = emptyList() }
        catch (_: IOException) { failure = true }
        catch (_: SecurityException) { failure = true }
    }
    private fun save(state: SessionState, pending: List<ProgressEvent>) {
        val checkpoint = SessionCheckpoint.encode(state)
        val events = ProgressCodec.encode(pending.mapIndexed { i, e -> StoredProgressEvent(i + 1L, 0, e) })
        val output = ByteArrayOutputStream()
        DataOutputStream(output).use { it.writeInt(1); it.writeInt(checkpoint.size); it.write(checkpoint); it.writeInt(events.size); it.write(events) }
        val bytes = output.toByteArray()
        val encoded = bytes + MessageDigest.getInstance("SHA-256").digest(bytes)
        try {
            journal.access {
                val actual = it.read()
                if (actual != null && lastJournal == null || actual == null && lastJournal != null ||
                    actual != null && lastJournal != null && !actual.contentEquals(lastJournal!!))
                    throw IOException("Checkpoint changed; reload before retry")
                it.replace(encoded)
            }
            lastJournal = encoded
        }
        catch (error: IOException) { failure = true; throw error }
        catch (error: SecurityException) { failure = true; throw error }
    }
    private fun decode(bytes: ByteArray): Pair<SessionState, List<ProgressEvent>> {
        require(bytes.size in 44..150_000)
        val payload = bytes.copyOfRange(0, bytes.size - 32)
        require(MessageDigest.isEqual(bytes.takeLast(32).toByteArray(), MessageDigest.getInstance("SHA-256").digest(payload)))
        return DataInputStream(ByteArrayInputStream(payload)).use { input ->
            require(input.readInt() == 1)
            val checkpointSize = input.readInt().also { require(it in 1..SessionCheckpoint.MAX_BYTES) }
            val checkpoint = ByteArray(checkpointSize).also { input.readFully(it) }
            val restored = SessionCheckpoint.restore(checkpoint, PrepositionsContent.activity, PrepositionsContent.REVISION,
                PrepositionsContent.repository) as? SessionRestoreResult.Restored ?: throw IOException("Incompatible checkpoint")
            PrepositionsContent.validate(restored.state)
            val eventSize = input.readInt().also { require(it in 1..40_000) }
            val events = ProgressCodec.decode(ByteArray(eventSize).also { input.readFully(it) }).map { it.event }
            require(events.size <= 1 && input.available() == 0)
            require(events.all { it.origin.session == restored.state.plan.id && it.origin.activity == PrepositionsContent.activity })
            val state = restored.state
            if (events.isNotEmpty()) {
                val actual = events.single()
                val effect: SessionEffect = when (actual) {
                    is AttemptEvent -> {
                        require(state.phase == SessionPhase.ACTIVE && state.current.attempts > 0 && state.current.lastChoice != null)
                        require(state.current.answer == AnswerState.CORRECT || state.current.answer == AnswerState.RETRY_AVAILABLE)
                        SessionEffect.RecordAttempt(AttemptResult(AttemptId(state.task.id, state.current.attempts),
                            state.task.question.skill, state.task.question.context, state.task.question.difficulty,
                            state.language, state.current.lastChoice!!, state.current.answer == AnswerState.CORRECT, state.current.support))
                    }
                    is CompletionEvent -> {
                        require(state.phase == SessionPhase.COMPLETED && state.completion == CompletionState.PENDING)
                        SessionEffect.Complete(CompletionRequest(state.completionRequestId!!,
                            SessionResult(state.plan.id, state.plan.activity, state.score, state.plan.tasks.zip(state.progress))))
                    }
                }
                var expected: ProgressEvent? = null
                val capture = object : ProgressRepository {
                    override fun append(event: ProgressEvent): ProgressWriteResult {
                        expected = event
                        return ProgressWriteResult.Saved(StoredProgressEvent(1, 0, event), true)
                    }
                    override fun read(query: ProgressQuery) = ProgressReadResult.Events(emptyList())
                }
                SessionProgressRecorder(state.plan, capture).record(effect)
                require(actual == expected)
            } else require(state.completion != CompletionState.PENDING)
            restored.state to events
        }
    }
}
