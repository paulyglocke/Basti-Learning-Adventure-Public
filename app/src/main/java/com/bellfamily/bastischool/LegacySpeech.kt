package com.bellfamily.bastischool

/** Main-thread confined lifecycle for the legacy bridge. One owner, never a queue. */
internal class LegacySpeech<V>(
    private val stopEngine: () -> Unit,
    private val play: (V, String, String) -> Boolean,
    private val statusChanged: (String) -> Unit = {}
) {
    enum class Readiness { INITIALISING, READY, FAILED }
    data class OfflineVoice<V>(val value: V, val name: String, val language: String, val country: String, val network: Boolean, val installed: Boolean = true)
    private data class Request(val text: String, val language: String, val id: String, val result: (Boolean) -> Unit)
    var readiness = Readiness.INITIALISING
        private set
    private var voices = emptyMap<String, V>()
    private var current: Request? = null

    fun initialise(success: Boolean, installed: List<OfflineVoice<V>>) {
        voices = listOf("en" to "GB", "de" to "DE").mapNotNull { (language, country) ->
            installed.filter { !it.network && it.installed && it.language == language }
                .sortedWith(compareBy<OfflineVoice<V>> { it.country != country }.thenBy { it.name })
                .firstOrNull()?.let { language to it.value }
        }.toMap()
        readiness = if (success) Readiness.READY else Readiness.FAILED
        statusChanged(if (!success) "failed" else if (voices.size < 2) "missing" else "ready")
        current?.let { dispatch(it) }
    }

    fun request(text: String, language: String, id: String, result: (Boolean) -> Unit) {
        stop()
        current = Request(text, language, id, result)
        if (readiness != Readiness.INITIALISING) dispatch(current!!)
    }

    private fun dispatch(request: Request) {
        val voice = voices[request.language]
        if (readiness != Readiness.READY || voice == null) {
            statusChanged(if (readiness == Readiness.FAILED) "failed" else "missing")
            completed(request.id, false)
            return
        }
        val accepted = try { play(voice, request.text, request.id) } catch (_: RuntimeException) { false }
        if (!accepted) { statusChanged("failed"); completed(request.id, false) }
    }

    fun completed(id: String, success: Boolean) {
        val request = current?.takeIf { it.id == id } ?: return
        current = null
        request.result(success)
    }

    fun stop() {
        val previous = current
        current = null
        stopEngine()
        previous?.result?.invoke(false)
    }
}
