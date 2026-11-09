package com.bellfamily.bastischool.learning.bonds

import com.bellfamily.bastischool.audio.*
import com.bellfamily.bastischool.learning.models.ContentLanguage

class NumberBondsAudio(private val controller: AudioController, private val failed: (Boolean) -> Unit = {}) {
    private val owner = SpeechOwner("native-number-bonds")
    private var visible = false
    private var epoch = 0L

    fun visible(value: Boolean) { visible = value; cancel() }
    fun mode(value: AudioMode) { controller.setMode(value); cancel() }
    fun cancel() { epoch++; controller.cancelOwner(owner); failed(false) }

    fun speak(state: NumberBonds.State, kind: SpeechKind, manual: Boolean = false) {
        val content = when (kind) {
            SpeechKind.COMPLETION -> NumberBonds.complete
            SpeechKind.FEEDBACK -> NumberBonds.feedback(state)
            SpeechKind.EXPLANATION -> NumberBonds.help
            else -> NumberBonds.prompt(state.shown,state.level)
        }
        val language: ContentLanguage = state.language
        if (!visible) return
        cancel()
        val token = epoch
        val context = controller.openContext(owner, SpeechSessionId(state.id.value), language)
        val trigger = if (manual) SpeechTrigger.REPLAY else SpeechTrigger.AUTOMATIC
        controller.speak(SpeechRequest.fromContent(context, content, kind, trigger)) {
            if (visible && token == epoch) failed(it.result is SpeechResult.Failed)
        }
    }

    fun close() { visible = false; cancel(); controller.close() }
}
