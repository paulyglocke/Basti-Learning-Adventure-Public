package com.bellfamily.bastischool.ui.verbmotion

import com.bellfamily.bastischool.audio.*
import com.bellfamily.bastischool.learning.models.ContentLanguage

/** Uses the shipped speech controller/policy; review actions never write learning progress. */
internal class VerbMotionAudio(private val controller: AudioController, private val failed: (Boolean) -> Unit) {
    private val owner = SpeechOwner("debug-verb-motion")
    private var visible = false
    private var epoch = 0L
    fun visible(value: Boolean) { visible = value; cancel() }
    fun mode(mode: AudioMode) { cancel(); controller.setMode(mode) }
    fun cancel() { epoch++; controller.cancelOwner(owner); failed(false) }
    fun listen(verb: VerbMotionSample, language: ContentLanguage) {
        if (!visible) return
        cancel()
        val token = epoch
        val context = controller.openContext(owner, SpeechSessionId(verb.id.value), language)
        controller.speak(SpeechRequest(context, verb.speech(language), SpeechKind.EXPLANATION, SpeechTrigger.MANUAL)) {
            if (visible && epoch == token) failed(it.result is SpeechResult.Failed)
        }
    }
    fun close() { visible = false; cancel(); controller.close() }
}
