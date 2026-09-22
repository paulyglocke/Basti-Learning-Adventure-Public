package com.bellfamily.bastischool.learning.seasons

import com.bellfamily.bastischool.audio.*
import com.bellfamily.bastischool.learning.content.SeasonIds
import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.session.*
import org.junit.Assert.*
import org.junit.Test

class SeasonsAudioTest {
    private fun start() = SessionReducer.start((SeasonsContent.generate(SessionId("audio"),RoundLength.FIVE,42) as GenerationResult.Generated).plan,
        ContentLanguage.ENGLISH,SeasonsContent.repository)
    @Test fun exploreUsesCanonicalNarrationAndReplayForEveryLanguageAndMode() {
        for(mode in AudioMode.entries) for(lang in ContentLanguage.entries) {
            val engine=FakeSpeechEngine();val audio=SeasonsAudio(DefaultAudioController(engine,mode));audio.visible(true)
            assertTrue(engine.spoken.isEmpty())
            for(id in SeasonIds.canonicalOrder) {
                audio.explore(SeasonsSelection(id),lang)
                audio.explore(SeasonsSelection(id),lang,true)
                if(mode!=AudioMode.OFF) {
                    assertEquals(SeasonsContent.narration(id).speech[lang],engine.spoken.last().text)
                    assertEquals(lang,engine.spoken.last().context.language)
                    assertEquals(SpeechTrigger.REPLAY,engine.spoken.last().trigger)
                    assertEquals(SpeechTrigger.MANUAL,engine.spoken[engine.spoken.lastIndex-1].trigger)
                }
            }
            assertEquals(if(mode==AudioMode.OFF)0 else 8,engine.spoken.size)
        }
    }
    @Test fun quizPolicySeparatesInstructionsFeedbackReplayAndOptionSpeech() {
        for(mode in AudioMode.entries) {
            val engine=FakeSpeechEngine();val audio=SeasonsAudio(DefaultAudioController(engine,mode));audio.visible(true)
            val start=start();audio.effects(start.state,start.effects)
            assertEquals(if(mode==AudioMode.OFF)0 else 1,engine.spoken.size)
            val feedback=SessionReducer.reduce(start.state,SessionAction.Answer(start.state.nextAttempt!!,start.state.task.question.correct))
            audio.effects(feedback.state,feedback.effects)
            assertEquals(if(mode==AudioMode.ALL)2 else if(mode==AudioMode.QUESTIONS)1 else 0,engine.spoken.size)
            val replay=SessionReducer.reduce(feedback.state,SessionAction.Replay(feedback.state.task.id))
            audio.effects(replay.state,replay.effects);audio.effects(replay.state,replay.effects)
            audio.option(replay.state,replay.state.task.question.correct)
            if(mode==AudioMode.OFF) assertTrue(engine.spoken.isEmpty()) else {
                assertEquals(SpeechKind.OPTION,engine.spoken.last().kind)
                assertTrue(engine.cancelled.isNotEmpty())
            }
        }
    }
    @Test fun restorationLanguageAndNavigationBoundariesRemainSilentUntilReplay() {
        val engine=FakeSpeechEngine();val audio=SeasonsAudio(DefaultAudioController(engine,AudioMode.ALL));audio.visible(true)
        val start=start();audio.effects(start.state,start.effects)
        audio.visible(false);audio.effects(start.state,start.effects);audio.visible(true)
        assertEquals(1,engine.spoken.size)
        val de=SessionReducer.reduce(start.state,SessionAction.Language(ContentLanguage.GERMAN))
        audio.effects(de.state,de.effects);assertEquals(1,engine.spoken.size)
        val replay=SessionReducer.reduce(de.state,SessionAction.Replay(de.state.task.id));audio.effects(replay.state,replay.effects)
        assertEquals(ContentLanguage.GERMAN,engine.spoken.last().context.language)
        assertTrue(engine.spoken.last().text.startsWith("Welche Jahreszeit"))
        audio.close();assertEquals(1,engine.closeCount)
    }
    @Test fun staleCallbacksCannotHideCurrentFailureOrFallbackLanguage() {
        val engine=FakeSpeechEngine();var status:SpeechResult?=null
        val audio=SeasonsAudio(DefaultAudioController(engine,AudioMode.ALL)){status=it};audio.visible(true)
        audio.explore(SeasonsSelection(),ContentLanguage.ENGLISH);val old=engine.spoken.last().id
        audio.explore(SeasonsSelection(SeasonIds.WINTER),ContentLanguage.GERMAN)
        engine.emit(engine.spoken.last().id,EngineResult.Failed(SpeechFailure.MISSING_OFFLINE_VOICE))
        engine.emit(old)
        assertEquals(SpeechResult.Failed(SpeechFailure.MISSING_OFFLINE_VOICE),status)
        assertEquals(2,engine.spoken.size)
        audio.visible(false);engine.emit(old);assertNull(status)
    }
}
