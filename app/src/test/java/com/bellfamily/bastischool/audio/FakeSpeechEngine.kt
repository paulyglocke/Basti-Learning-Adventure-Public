package com.bellfamily.bastischool.audio

/** Shared JVM test double. Intentionally permits late/duplicate callbacks to exercise ownership guards. */
class FakeSpeechEngine(initialReadiness: EngineReadiness = EngineReadiness.READY) : SpeechEngine {
    override var readiness = initialReadiness
        private set
    override var onReadinessChanged: ((EngineReadiness) -> Unit)? = null
    val spoken = mutableListOf<EngineSpeechRequest>()
    val cancelled = mutableListOf<SpeechRequestId>()
    var throwOnSpeak = false
    var closeCount = 0
        private set
    private val callbacks = mutableMapOf<SpeechRequestId, (EngineResult) -> Unit>()
    private var current: SpeechRequestId? = null

    fun become(state: EngineReadiness) {
        readiness = state
        onReadinessChanged?.invoke(state)
    }

    override fun speak(request: EngineSpeechRequest, result: (EngineResult) -> Unit) {
        if (throwOnSpeak) throw IllegalStateException("Test engine failure")
        spoken.add(request)
        callbacks[request.id] = result
        current = request.id
    }

    fun emit(id: SpeechRequestId, result: EngineResult = EngineResult.Completed) {
        if (id == current) current = null
        callbacks.getValue(id).invoke(result)
    }

    override fun stop() {
        current?.let {
            current = null
            cancelled.add(it)
            callbacks.getValue(it).invoke(EngineResult.Cancelled)
        }
    }

    override fun close() {
        closeCount++
        stop()
        become(EngineReadiness.CLOSED)
    }
}
