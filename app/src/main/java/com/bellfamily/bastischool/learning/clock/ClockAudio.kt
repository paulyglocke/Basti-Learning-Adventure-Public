package com.bellfamily.bastischool.learning.clock

import com.bellfamily.bastischool.audio.*

class ClockAudio(private val controller: AudioController, private val failed: (Boolean) -> Unit = {}) {
    private val owner = SpeechOwner("native-clock")
    private var visible = false
    private var epoch = 0L
    fun visible(value: Boolean) { visible = value; cancel() }
    fun mode(value: AudioMode) { controller.setMode(value); cancel() }
    fun cancel() { epoch++; controller.cancelOwner(owner); failed(false) }
    fun listen(state: ClockExploreState) {
        if (!visible) return
        cancel()
        val token = epoch
        val context = controller.openContext(owner, SpeechSessionId(ClockWording.ACTIVITY), state.language)
        controller.speak(SpeechRequest(context, ClockWording.phrase(state.time, state.language), SpeechKind.EXPLANATION, SpeechTrigger.MANUAL)) {
            if (visible && token == epoch) failed(it.result is SpeechResult.Failed)
        }
    }
    fun close() { visible = false; cancel(); controller.close() }
}
