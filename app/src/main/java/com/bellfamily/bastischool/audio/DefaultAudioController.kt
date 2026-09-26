package com.bellfamily.bastischool.audio

import com.bellfamily.bastischool.learning.models.ContentLanguage

/**
 * Single-thread confined (main thread with Android). At most one pending/active utterance.
 * No timers or lifecycle/platform references. Caller revokes the context on leaving/backgrounding.
 * Return/restoration opens a silent fresh context, then explicit Replay may request speech.
 */
class DefaultAudioController private constructor(
    initialMode: AudioMode,
    private var engine: SpeechEngine?,
    private var engineFactory: (() -> SpeechEngine)?
) : AudioController {
    constructor(engine: SpeechEngine, initialMode: AudioMode) : this(initialMode, engine, null)
    constructor(engineFactory: () -> SpeechEngine, initialMode: AudioMode) : this(initialMode, null, engineFactory)
    override var mode = initialMode
        private set
    override val readiness get() = when {
        closed -> EngineReadiness.CLOSED
        engine != null -> engine!!.readiness
        engineFactory != null -> EngineReadiness.INITIALISING
        else -> EngineReadiness.FAILED
    }
    private var context: SpeechContext? = null
    private var priority = SpeechPriority.INSTRUCTION
    private var sequence = 0L
    private var closed = false
    private data class Active(
        val request: SpeechRequest, val ticket: SpeechTicket,
        val callback: (SpeechOutcome) -> Unit, var dispatched: Boolean = false
    )
    private var active: Active? = null
    private var mutationDepth = 0
    private var notifying = false
    private val notifications = ArrayDeque<() -> Unit>()

    init { engine?.onReadinessChanged = { mutate { dispatch() } } }

    override fun openContext(owner: SpeechOwner, session: SpeechSessionId, language: ContentLanguage): SpeechContext = mutate {
        check(!closed) { "Audio controller is closed" }
        val next = SpeechContext(owner, session, language)
        context = next
        priority = SpeechPriority.INSTRUCTION
        cancelActive(SpeechCancellation.CONTEXT_CHANGED)
        next
    }

    override fun speak(request: SpeechRequest, onResult: (SpeechOutcome) -> Unit): SpeechTicket = mutate {
        val item = Active(request, SpeechTicket(SpeechRequestId(++sequence)), onResult)
        val suppressed = when {
            closed -> SpeechSuppression.CLOSED
            request.context !== context -> SpeechSuppression.STALE_CONTEXT
            request.priority < priority -> SpeechSuppression.LOWER_PRIORITY
            else -> null
        }
        if (suppressed != null) {
            finish(item, SpeechResult.Suppressed(suppressed))
        } else {
            priority = request.priority
            // Even muted feedback supersedes a pending question in Questions Only.
            cancelActive(SpeechCancellation.REPLACED)
            if (!mode.allows(request.kind, request.trigger)) finish(item, SpeechResult.Suppressed(SpeechSuppression.POLICY))
            else {
                active = item
                dispatch()
            }
        }
        item.ticket
    }

    override fun setMode(mode: AudioMode): Unit = mutate {
        this.mode = mode
        active?.let { if (!mode.allows(it.request.kind, it.request.trigger)) cancelActive(SpeechCancellation.POLICY_CHANGED) }
    }

    override fun cancelRequest(id: SpeechRequestId) = mutate {
        if (active?.ticket?.id == id) cancelActive(SpeechCancellation.REQUESTED)
    }

    override fun cancelOwner(owner: SpeechOwner) = mutate {
        if (context?.owner == owner) revoke(SpeechCancellation.OWNER_LEFT)
    }

    override fun cancelSession(session: SpeechSessionId) = mutate {
        if (context?.session == session) revoke(SpeechCancellation.SESSION_ENDED)
    }

    override fun close() = mutate {
        if (!closed) {
            closed = true
            revoke(SpeechCancellation.CLOSED)
            engineFactory = null
            engine?.onReadinessChanged = null
            engine?.close()
        }
    }

    private fun revoke(reason: SpeechCancellation) {
        context = null
        cancelActive(reason)
    }

    private fun dispatch() {
        val item = active ?: return
        if (engine == null) {
            // Policy/context already accepted this request. Empty speech must not allocate TTS.
            if (sanitizeSpeech(item.request.speechText).isBlank()) {
                complete(item, SpeechResult.Failed(SpeechFailure.EMPTY_TEXT))
                return
            }
            val factory = engineFactory
            engineFactory = null // One attempt per owner, including construction failure.
            try {
                engine = factory?.invoke()
                engine?.onReadinessChanged = { mutate { dispatch() } }
            } catch (_: RuntimeException) {
                complete(item, SpeechResult.Failed(SpeechFailure.ENGINE_UNAVAILABLE))
                return
            }
        }
        val engine = engine
        if (engine == null) {
            complete(item, SpeechResult.Failed(SpeechFailure.ENGINE_UNAVAILABLE))
            return
        }
        if (active !== item) return
        if (engine.readiness == EngineReadiness.INITIALISING) return
        when (engine.readiness) {
            EngineReadiness.FAILED -> complete(item, SpeechResult.Failed(SpeechFailure.ENGINE_UNAVAILABLE))
            EngineReadiness.CLOSED -> complete(item, SpeechResult.Failed(SpeechFailure.ENGINE_UNAVAILABLE))
            EngineReadiness.READY -> {
                if (item.dispatched) return
                val text = sanitizeSpeech(item.request.speechText)
                if (text.isBlank()) {
                    complete(item, SpeechResult.Failed(SpeechFailure.EMPTY_TEXT))
                    return
                }
                item.dispatched = true
                val request = item.request
                try {
                    engine.speak(EngineSpeechRequest(item.ticket.id, request.context, text, request.kind, request.trigger)) { result ->
                        mutate {
                            complete(item, when (result) {
                                EngineResult.Completed -> SpeechResult.Completed
                                EngineResult.Cancelled -> SpeechResult.Cancelled(SpeechCancellation.ENGINE_STOPPED)
                                is EngineResult.Failed -> SpeechResult.Failed(result.reason)
                            })
                        }
                    }
                } catch (_: RuntimeException) {
                    complete(item, SpeechResult.Failed(SpeechFailure.PLAYBACK))
                }
            }
            EngineReadiness.INITIALISING -> Unit
        }
    }

    private fun complete(item: Active, result: SpeechResult) {
        if (active !== item) return // Late, duplicate or previous context callback.
        active = null
        finish(item, result)
    }

    private fun cancelActive(reason: SpeechCancellation) {
        val previous = active ?: return
        active = null // Revoke before engine.stop, which may synchronously call back.
        if (previous.dispatched) engine?.stop()
        finish(previous, SpeechResult.Cancelled(reason))
    }

    private fun finish(item: Active, result: SpeechResult) {
        if (item.ticket.result != null) return
        item.ticket.result = result
        notifications.addLast { item.callback(SpeechOutcome(item.ticket.id, result)) }
    }

    /** Deliver observers only after transitions settle; callbacks may safely submit/cancel speech. */
    private fun <T> mutate(action: () -> T): T {
        mutationDepth++
        try { return action() } finally {
            mutationDepth--
            if (mutationDepth == 0 && !notifying) {
                notifying = true
                try { while (notifications.isNotEmpty()) notifications.removeFirst().invoke() }
                finally { notifying = false }
            }
        }
    }
}
