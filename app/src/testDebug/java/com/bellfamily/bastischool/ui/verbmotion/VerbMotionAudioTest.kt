package com.bellfamily.bastischool.ui.verbmotion

import com.bellfamily.bastischool.audio.*
import com.bellfamily.bastischool.learning.models.ContentLanguage
import org.junit.Assert.*
import org.junit.Test

class VerbMotionAudioTest {
    @Test fun previewIsSilentUntilRequestedAndOffNeverCreatesAnEngine() {
        var creates=0
        val audio=VerbMotionAudio(DefaultAudioController({creates++;FakeSpeechEngine()},AudioMode.OFF)) {}
        audio.visible(true);assertEquals(0,creates)
        audio.listen(VerbMotionSample.JUMP,ContentLanguage.ENGLISH);assertEquals(0,creates)
        audio.mode(AudioMode.QUESTIONS);assertEquals(0,creates)
        audio.listen(VerbMotionSample.JUMP,ContentLanguage.ENGLISH);assertEquals(1,creates)
        audio.close()
    }
    @Test fun languageReplacementBackgroundAndLateFailuresCannotLeakAcrossSamples() {
        val engine=FakeSpeechEngine();var failed=false
        val audio=VerbMotionAudio(DefaultAudioController(engine,AudioMode.QUESTIONS)){failed=it}
        audio.visible(true);audio.listen(VerbMotionSample.JUMP,ContentLanguage.ENGLISH)
        val old=engine.spoken.single()
        audio.listen(VerbMotionSample.SNAP,ContentLanguage.GERMAN)
        val current=engine.spoken.last()
        assertTrue(engine.cancelled.contains(old.id));assertEquals(ContentLanguage.GERMAN,current.context.language)
        assertTrue(current.text.contains("Krokodil"));assertEquals(SpeechTrigger.MANUAL,current.trigger)
        engine.emit(old.id,EngineResult.Failed(SpeechFailure.PLAYBACK));assertFalse(failed)
        audio.visible(false)
        assertTrue(engine.cancelled.contains(current.id))
        audio.listen(VerbMotionSample.FLY,ContentLanguage.ENGLISH);assertEquals(2,engine.spoken.size)
        engine.emit(current.id,EngineResult.Failed(SpeechFailure.PLAYBACK));assertFalse(failed)
        audio.visible(true);assertEquals(2,engine.spoken.size)
        audio.close();assertEquals(1,engine.closeCount)
    }
}
