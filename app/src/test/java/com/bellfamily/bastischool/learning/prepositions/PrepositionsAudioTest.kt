package com.bellfamily.bastischool.learning.prepositions

import com.bellfamily.bastischool.audio.*
import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.session.*
import org.junit.Assert.*
import org.junit.Test

class PrepositionsAudioTest {
    private fun start(lang: ContentLanguage = ContentLanguage.ENGLISH) = SessionReducer.start(PrepositionsContentTest.plan(),lang,PrepositionsContent.repository)
    @Test fun allQuestionsOffApplyToInstructionsFeedbackReplayAndOptions() {
        for(mode in AudioMode.entries) {
            val engine=FakeSpeechEngine();val audio=PrepositionsAudio(DefaultAudioController(engine,mode));audio.visible(true)
            val start=start();audio.effects(start.state,start.effects)
            assertEquals(if(mode==AudioMode.OFF)0 else 1,engine.spoken.size)
            val feedback=SessionReducer.reduce(start.state,SessionAction.Answer(start.state.nextAttempt!!,start.state.task.question.correct))
            audio.effects(feedback.state,feedback.effects)
            assertEquals(if(mode==AudioMode.ALL)2 else if(mode==AudioMode.QUESTIONS)1 else 0,engine.spoken.size)
            val replay=SessionReducer.reduce(feedback.state,SessionAction.Replay(feedback.state.task.id))
            audio.effects(replay.state,replay.effects);audio.effects(replay.state,replay.effects)
            audio.option(replay.state,replay.state.task.question.correct)
            if(mode!=AudioMode.OFF) {
                assertEquals(SpeechTrigger.MANUAL,engine.spoken.last().trigger)
                assertEquals(SpeechKind.OPTION,engine.spoken.last().kind)
                assertTrue(engine.cancelled.isNotEmpty())
            } else assertTrue(engine.spoken.isEmpty())
        }
    }
    @Test fun languageAndLifecycleBoundariesCancelAndRestoreRemainsSilent() {
        val engine=FakeSpeechEngine();val audio=PrepositionsAudio(DefaultAudioController(engine,AudioMode.ALL));audio.visible(true)
        val en=start();audio.effects(en.state,en.effects)
        assertEquals(ContentLanguage.ENGLISH,engine.spoken.last().context.language)
        audio.visible(false);audio.effects(en.state,en.effects);val size=engine.spoken.size
        audio.visible(true);assertEquals(size,engine.spoken.size)
        val de=SessionReducer.reduce(en.state,SessionAction.Language(ContentLanguage.GERMAN))
        audio.effects(de.state,de.effects);assertEquals(size,engine.spoken.size)
        val replay=SessionReducer.reduce(de.state,SessionAction.Replay(de.state.task.id));audio.effects(replay.state,replay.effects)
        assertEquals(ContentLanguage.GERMAN,engine.spoken.last().context.language)
        assertTrue(engine.spoken.last().text.startsWith("Wo ist"))
        audio.close();assertEquals(1,engine.closeCount)
    }
    @Test fun staleTutorialCompletionAndOffNeverMarkHeard() {
        val engine=FakeSpeechEngine();val controller=DefaultAudioController(engine,AudioMode.ALL)
        val audio=PrepositionsAudio(controller);audio.visible(true);var heard=0
        audio.introduction(start().state,true) {if(it==SpeechResult.Completed)heard++}
        val old=engine.spoken.last();audio.visible(false);engine.emit(old.id)
        assertEquals(0,heard)
        audio.visible(true);controller.setMode(AudioMode.OFF)
        audio.introduction(start().state,true) {if(it==SpeechResult.Completed)heard++}
        assertEquals(0,heard)
        controller.setMode(AudioMode.ALL);audio.introduction(start().state,true) {if(it==SpeechResult.Completed)heard++}
        engine.emit(engine.spoken.last().id);assertEquals(1,heard)
    }
    @Test fun missingVoiceIsExplicitWithoutChangingLearningState() {
        val engine=FakeSpeechEngine();var failure:SpeechResult?=null
        val audio=PrepositionsAudio(DefaultAudioController(engine,AudioMode.ALL)){failure=it};audio.visible(true)
        val state=start(ContentLanguage.GERMAN);audio.effects(state.state,state.effects)
        engine.emit(engine.spoken.last().id,EngineResult.Failed(SpeechFailure.MISSING_OFFLINE_VOICE))
        assertEquals(SpeechResult.Failed(SpeechFailure.MISSING_OFFLINE_VOICE),failure)
        assertEquals(0,state.state.score);assertEquals(0,state.state.current.attempts)
        assertEquals(1,engine.spoken.size)
    }
}
