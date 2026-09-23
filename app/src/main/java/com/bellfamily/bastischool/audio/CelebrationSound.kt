package com.bellfamily.bastischool.audio

/** Same ALL/QUESTIONS/OFF SFX policy as the legacy celebration; no speech relabelling. */
fun AudioMode.allowsCelebrationSound() = this != AudioMode.OFF
interface PopSoundEngine {
    fun play(): Boolean
    fun stop()
    fun close()
}

/** Main-thread host owner, independent of TTS queues/focus and with no asynchronous callbacks. */
class CelebrationSound(private val engine: PopSoundEngine) {
    private var mode = AudioMode.OFF
    private var owner: String? = null
    private var closed = false
    fun activate(mode: AudioMode, completion: String?) {
        if(closed) return
        if(this.mode != mode || owner != completion) engine.stop()
        this.mode = mode; owner = completion
    }
    fun pop(completion: String): Boolean {
        if(closed || owner != completion || !mode.allowsCelebrationSound()) return false
        engine.stop() // replace; never accumulate tones
        return engine.play()
    }
    fun cancel() { owner = null; engine.stop() }
    fun close() { if(!closed) {cancel(); closed = true; engine.close()} }
}
