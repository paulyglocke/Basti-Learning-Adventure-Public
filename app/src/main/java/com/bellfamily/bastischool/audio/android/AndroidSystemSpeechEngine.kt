package com.bellfamily.bastischool.audio.android

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import com.bellfamily.bastischool.audio.EngineResult
import com.bellfamily.bastischool.audio.OfflineVoice
import com.bellfamily.bastischool.audio.SpeechEngine
import com.bellfamily.bastischool.audio.SpeechFailure
import com.bellfamily.bastischool.audio.SystemSpeechEngine
import com.bellfamily.bastischool.audio.SystemTtsPort

/**
 * Construct/use/close on main. Holds application context only. Not wired to legacy or a screen yet.
 * The owner must close the controller/engine on disposal and cancel its context on backgrounding.
 */
class AndroidSystemSpeechEngine(context: Context) : SpeechEngine by
    SystemSpeechEngine(AndroidSystemTtsPort(context.applicationContext))

private class AndroidSystemTtsPort(private val context: Context) : SystemTtsPort {
    private val handler = Handler(Looper.getMainLooper())
    private var tts: TextToSpeech? = null
    private var cachedVoices = emptyMap<String, Voice>()
    private var closed = false

    private fun checkMain() = check(Looper.myLooper() == Looper.getMainLooper()) { "Speech requires main thread" }

    override fun initialise(ready: (Boolean) -> Unit, result: (String, EngineResult) -> Unit) {
        checkMain()
        tts = TextToSpeech(context) { status ->
            // Post even if init is synchronous: tts must be assigned before accessing it.
            handler.post {
                if (!closed) {
                    val engine = tts
                    val success = try {
                        status == TextToSpeech.SUCCESS && engine != null &&
                            engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                                override fun onStart(utteranceId: String) = Unit
                                override fun onDone(utteranceId: String) = deliver(utteranceId, EngineResult.Completed)
                                @Deprecated("Android legacy callback")
                                override fun onError(utteranceId: String) = deliver(utteranceId, EngineResult.Failed(SpeechFailure.PLAYBACK))
                                override fun onError(utteranceId: String, errorCode: Int) = deliver(utteranceId, EngineResult.Failed(SpeechFailure.PLAYBACK))
                                override fun onStop(utteranceId: String, interrupted: Boolean) = deliver(utteranceId, EngineResult.Cancelled)
                                private fun deliver(id: String, outcome: EngineResult) {
                                    handler.post { if (!closed) result(id, outcome) }
                                }
                            }) == TextToSpeech.SUCCESS && engine.setSpeechRate(0.84f) == TextToSpeech.SUCCESS
                    } catch (_: RuntimeException) { false }
                    ready(success)
                }
            }
        }
    }

    override fun voices(): List<OfflineVoice> {
        checkMain()
        cachedVoices = tts?.voices.orEmpty().associateBy { it.name }
        return cachedVoices.values.map {
            OfflineVoice(it.name, it.locale.language, it.locale.country, it.isNetworkConnectionRequired,
                !it.features.orEmpty().contains(TextToSpeech.Engine.KEY_FEATURE_NOT_INSTALLED))
        }
    }

    override fun selectVoice(id: String): Boolean {
        checkMain()
        val voice = cachedVoices[id] ?: return false
        val engine = tts ?: return false
        if (engine.setVoice(voice) != TextToSpeech.SUCCESS) return false
        // Do not trust a success code that actually left the previous language/default voice selected.
        val selected = engine.voice ?: return false
        return selected.name == voice.name && selected.locale.language == voice.locale.language &&
            !selected.isNetworkConnectionRequired &&
            !selected.features.orEmpty().contains(TextToSpeech.Engine.KEY_FEATURE_NOT_INSTALLED)
    }

    override fun speak(text: String, utteranceId: String): Boolean {
        checkMain()
        if (text.length > TextToSpeech.getMaxSpeechInputLength()) return false
        return tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId) == TextToSpeech.SUCCESS
    }

    override fun stop() {
        checkMain()
        check(tts?.stop() == TextToSpeech.SUCCESS) { "System TTS stop failed" }
    }

    override fun close() {
        checkMain()
        if (closed) return
        closed = true
        handler.removeCallbacksAndMessages(null)
        val engine = tts
        tts = null
        cachedVoices = emptyMap()
        try { engine?.stop() } finally { engine?.shutdown() }
    }
}
