package com.bellfamily.bastischool.audio.android

import android.media.AudioManager
import android.media.ToneGenerator
import com.bellfamily.bastischool.audio.PopSoundEngine

/** One lazy, quiet 45ms native tone; no downloads, audio focus requests or TTS calls. */
class AndroidPopSound : PopSoundEngine {
    private var tone: ToneGenerator? = null
    private var closed = false
    override fun play(): Boolean {
        if(closed) return false
        return try {
            val player = tone ?: ToneGenerator(AudioManager.STREAM_MUSIC, 8).also {tone = it}
            player.startTone(ToneGenerator.TONE_PROP_BEEP, 45)
        } catch (_: RuntimeException) { false } // Optional sound failure cannot block completion.
    }
    override fun stop() { tone?.stopTone() }
    override fun close() { if(!closed) {stop(); tone?.release(); tone = null; closed = true} }
}
