package com.bellfamily.bastischool.learning.counting

import com.bellfamily.bastischool.audio.*
import com.bellfamily.bastischool.learning.models.ContentLanguage

class CountingTrayAudio(private val controller: AudioController, private val failed: (Boolean) -> Unit = {}) {
    private val owner = SpeechOwner("native-counting-tray")
    private var visible = false
    private var epoch = 0L

    fun visible(value: Boolean) { visible = value; cancel() }
    fun mode(value: AudioMode) { controller.setMode(value); cancel() }
    fun cancel() { epoch++; controller.cancelOwner(owner); failed(false) }

    fun speak(state: CountingTray.State, kind: SpeechKind, manual: Boolean = false) {
        val content = when (kind) {
            SpeechKind.COMPLETION -> CountingTray.complete
            SpeechKind.FEEDBACK -> if (state.current.solved) CountingTray.success(state.target) else CountingTray.checkAgain
            SpeechKind.EXPLANATION -> CountingTray.help
            else -> CountingTray.prompt(state.target)
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
