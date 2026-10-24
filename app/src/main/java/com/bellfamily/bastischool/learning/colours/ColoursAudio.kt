package com.bellfamily.bastischool.learning.colours

import com.bellfamily.bastischool.audio.*
import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.session.*

class ColoursAudio(private val controller: AudioController, ownerName: String = "native-colours", private val status: (SpeechResult?) -> Unit = {}) {
    private val owner = SpeechOwner(ownerName)
    private var active = false
    private var revision = 0L

    fun visible(value: Boolean) { active = value; cancel() }
    fun mode(value: AudioMode) { controller.setMode(value); cancel() }
    fun cancel() { revision++; controller.cancelOwner(owner); status(null) }

    fun word(id: ContentId, language: ContentLanguage, replay: Boolean = false) = speak(
        "colours-explore", language, ColoursContent.definition(id).text, SpeechKind.OPTION,
        if (replay) SpeechTrigger.REPLAY else SpeechTrigger.MANUAL
    )

    fun say(id: String, language: ContentLanguage, text: ContentText, replay: Boolean = false) = speak(
        id, language, text, SpeechKind.OPTION, if (replay) SpeechTrigger.REPLAY else SpeechTrigger.MANUAL
    )

    fun effects(id: SessionId, language: ContentLanguage, effects: List<SessionEffect>) {
        effects.forEach { effect -> when (effect) {
            SessionEffect.CancelNarration -> cancel()
            is SessionEffect.Narrate -> speak(id.value, language, effect.text, when (effect.kind) {
                NarrationKind.INSTRUCTION -> SpeechKind.INSTRUCTION
                NarrationKind.FEEDBACK -> SpeechKind.FEEDBACK
                NarrationKind.COMPLETION -> SpeechKind.COMPLETION
            }, effect.trigger)
            else -> Unit
        } }
    }

    private fun speak(id: String, language: ContentLanguage, text: ContentText, kind: SpeechKind, trigger: SpeechTrigger) {
        if (!active) return
        cancel()
        val ticket = revision
        val context = controller.openContext(owner, SpeechSessionId(id), language)
        controller.speak(SpeechRequest.fromContent(context, text, kind, trigger)) {
            if (active && ticket == revision) status(it.result)
        }
    }

    fun close() { active = false; cancel(); controller.close() }
}
