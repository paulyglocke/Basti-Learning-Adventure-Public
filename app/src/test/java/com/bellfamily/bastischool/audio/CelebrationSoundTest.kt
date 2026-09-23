package com.bellfamily.bastischool.audio

import com.bellfamily.bastischool.learning.models.*
import org.junit.Assert.*
import org.junit.Test

class CelebrationSoundTest {
    private class FakePop:PopSoundEngine {
        var plays=0;var stops=0;var closes=0;var available=true
        override fun play():Boolean {plays++;return available}
        override fun stop(){stops++}
        override fun close(){closes++}
    }
    @Test fun allAndQuestionsAllowSoundButOffBlocksEvenExplicitPop() {
        for(mode in AudioMode.entries) {
            val engine=FakePop();val sound=CelebrationSound(engine);sound.activate(mode,"round")
            assertEquals(mode!=AudioMode.OFF,sound.pop("round"));assertEquals(if(mode==AudioMode.OFF)0 else 1,engine.plays)
            sound.activate(AudioMode.OFF,"round");assertFalse(sound.pop("round"))
        }
    }
    @Test fun navigationOldOwnersBackgroundAndCloseRejectStaleRequests() {
        val engine=FakePop();val sound=CelebrationSound(engine)
        sound.activate(AudioMode.ALL,"old");sound.pop("old")
        sound.activate(AudioMode.ALL,"new");assertFalse(sound.pop("old"));assertTrue(sound.pop("new"))
        sound.cancel();assertFalse(sound.pop("new"));sound.close();sound.close()
        sound.activate(AudioMode.ALL,"new");assertFalse(sound.pop("new"))
        assertEquals(2,engine.plays);assertEquals(1,engine.closes)
    }
    @Test fun eachToneReplacesRatherThanQueuesAndFailureIsOptional() {
        val engine=FakePop();val sound=CelebrationSound(engine);sound.activate(AudioMode.ALL,"round")
        val initial=engine.stops
        repeat(5){sound.pop("round")}
        assertEquals(initial+5,engine.stops);assertEquals(5,engine.plays)
        engine.available=false;assertFalse(sound.pop("round"))
    }
    @Test fun popDoesNotCancelOrCompleteSharedSpeech() {
        val speech=FakeSpeechEngine();val controller=DefaultAudioController(speech,AudioMode.ALL)
        val context=controller.openContext(SpeechOwner("learning"),SpeechSessionId("round"),ContentLanguage.ENGLISH)
        val ticket=controller.speak(SpeechRequest.fromContent(context,ContentText.plain("Well done!","Gut gemacht!"),SpeechKind.COMPLETION))
        val sound=CelebrationSound(FakePop());sound.activate(AudioMode.ALL,"round");sound.pop("round");sound.cancel()
        assertEquals(1,speech.spoken.size);assertTrue(speech.cancelled.isEmpty());assertNull(ticket.result)
    }
}
