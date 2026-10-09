package com.bellfamily.bastischool.learning.clock

import com.bellfamily.bastischool.audio.*
import com.bellfamily.bastischool.learning.models.*

class ClockAudio(private val controller: AudioController, private val failed: (Boolean) -> Unit = {}) {
    private val owner = SpeechOwner("native-clock")
    private var visible = false
    private var epoch = 0L
    fun visible(value: Boolean) { visible = value; cancel() }
    fun mode(value: AudioMode) { controller.setMode(value); cancel() }
    fun cancel() { epoch++; controller.cancelOwner(owner); failed(false) }
    fun listen(state: ClockExploreState) = speak(ClockWording.phrase(state.time, state.language), state.language, SpeechKind.EXPLANATION, SpeechTrigger.MANUAL)
    fun practice(state: ClockPractice.State, kind: SpeechKind, manual: Boolean = false) {
        val text = when (kind) {
            SpeechKind.COMPLETION -> ClockPractice.complete
            SpeechKind.FEEDBACK -> if (state.current.solved) ClockPractice.correct(state.target) else ClockPractice.retry
            SpeechKind.EXPLANATION -> ClockPractice.hint(state.target)
            else -> ClockPractice.prompt(state.target)
        }
        speak(text.speech[state.language], state.language, kind, if (manual) SpeechTrigger.REPLAY else SpeechTrigger.AUTOMATIC)
    }
    private fun speak(text: String, language: ContentLanguage, kind: SpeechKind, trigger: SpeechTrigger) {
        if (!visible) return
        cancel()
        val token = epoch
        val context = controller.openContext(owner, SpeechSessionId(ClockWording.ACTIVITY), language)
        controller.speak(SpeechRequest(context, text, kind, trigger)) {
            if (visible && token == epoch) failed(it.result is SpeechResult.Failed)
        }
    }
    fun close() { visible = false; cancel(); controller.close() }
}
