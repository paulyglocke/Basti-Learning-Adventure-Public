package com.bellfamily.bastischool.audio

import com.bellfamily.bastischool.learning.models.ContentLanguage

internal data class OfflineVoice(
    val id: String, val language: String, val country: String,
    val networkRequired: Boolean = false, val installed: Boolean = true
)

/** Small platform seam: initialization, installed metadata, explicit voice selection and flush playback. */
internal interface SystemTtsPort {
    fun initialise(ready: (Boolean) -> Unit, result: (String, EngineResult) -> Unit)
    fun voices(): List<OfflineVoice>
    fun selectVoice(id: String): Boolean
    fun speak(text: String, utteranceId: String): Boolean
    fun stop()
    fun close()
}

/** Tested without Android; platform callbacks must be serialized with calls. No engine-side queue. */
internal class SystemSpeechEngine(private val port: SystemTtsPort) : SpeechEngine {
    override var readiness = EngineReadiness.INITIALISING
        private set
    override var onReadinessChanged: ((EngineReadiness) -> Unit)? = null
    private var voices = emptyMap<ContentLanguage, OfflineVoice>()
    private var sequence = 0L
    private data class Playback(val id: String, val callback: (EngineResult) -> Unit)
    private var playback: Playback? = null

    init {
        try { port.initialise(::initialised, ::completed) }
        catch (_: RuntimeException) { initialised(false) }
    }

    private fun initialised(success: Boolean) {
        if (readiness != EngineReadiness.INITIALISING) return
        var usable = success
        if (success) {
            try {
                val installed = port.voices().filter { it.installed && !it.networkRequired }
                voices = ContentLanguage.entries.mapNotNull { language ->
                    val (code, country) = when (language) {
                        ContentLanguage.ENGLISH -> "en" to "GB"
                        ContentLanguage.GERMAN -> "de" to "DE"
                    }
                    installed.filter { it.language.equals(code, ignoreCase = true) }
                        .sortedWith(compareBy<OfflineVoice> { !it.country.equals(country, ignoreCase = true) }.thenBy { it.id })
                        .firstOrNull()?.let { language to it }
                }.toMap()
            } catch (_: RuntimeException) { usable = false }
        }
        readiness = if (usable) EngineReadiness.READY else EngineReadiness.FAILED
        onReadinessChanged?.invoke(readiness)
    }

    override fun speak(request: EngineSpeechRequest, result: (EngineResult) -> Unit) {
        stop()
        if (readiness != EngineReadiness.READY) {
            result(EngineResult.Failed(SpeechFailure.ENGINE_UNAVAILABLE))
            return
        }
        val voice = voices[request.context.language]
        if (voice == null) {
            result(EngineResult.Failed(SpeechFailure.MISSING_OFFLINE_VOICE))
            return
        }
        val selected = try { port.selectVoice(voice.id) } catch (_: RuntimeException) { false }
        if (!selected) {
            result(EngineResult.Failed(SpeechFailure.VOICE_SELECTION))
            return
        }
        val current = Playback("native-${++sequence}", result)
        playback = current
        val accepted = try { port.speak(request.text, current.id) } catch (_: RuntimeException) { false }
        if (!accepted) completed(current.id, EngineResult.Failed(SpeechFailure.PLAYBACK))
    }

    private fun completed(id: String, result: EngineResult) {
        val current = playback?.takeIf { it.id == id } ?: return
        playback = null
        current.callback(result)
    }

    override fun stop() {
        val current = playback
        playback = null
        if (current != null) {
            try { port.stop() } catch (_: RuntimeException) {
                readiness = EngineReadiness.FAILED
                onReadinessChanged?.invoke(readiness)
            }
            current.callback(EngineResult.Cancelled)
        }
    }

    override fun close() {
        if (readiness == EngineReadiness.CLOSED) return
        readiness = EngineReadiness.CLOSED
        val current = playback
        playback = null
        try { port.close() } catch (_: RuntimeException) { /* Already terminal; never resume playback. */ }
        current?.callback?.invoke(EngineResult.Cancelled)
        onReadinessChanged?.invoke(readiness)
        onReadinessChanged = null
        voices = emptyMap()
    }
}
