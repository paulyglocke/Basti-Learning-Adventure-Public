package com.bellfamily.bastischool.learning.wilma

import com.bellfamily.bastischool.audio.*
import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.session.*
import org.junit.Assert.*
import org.junit.Test

class WilmaAudioTest {
    @Test fun exploreAndReplayResolveEveryAuthoredDayInBothLanguagesWithOffSilence() {
        for(mode in AudioMode.entries)for(lang in ContentLanguage.entries) {
            val engine=FakeSpeechEngine();val audio=WilmaAudio(DefaultAudioController(engine,mode));audio.visible(true)
            assertTrue(engine.spoken.isEmpty())
            WilmaContent.days.forEach {id ->
                audio.day(id,lang);audio.day(id,lang,true)
                if(mode!=AudioMode.OFF) {
                    assertEquals(WilmaContent.day(id).text.speech[lang],engine.spoken.last().text)
                    assertEquals(lang,engine.spoken.last().context.language)
                    assertEquals(SpeechTrigger.REPLAY,engine.spoken.last().trigger)
                }
            }
            assertEquals(if(mode==AudioMode.OFF)0 else 14,engine.spoken.size)
        }
    }
    @Test fun policyGatesAutomaticInstructionsAndFeedbackForQuizAndOrdering() {
        for(mode in AudioMode.entries) {
            val engine=FakeSpeechEngine();val audio=WilmaAudio(DefaultAudioController(engine,mode));audio.visible(true)
            val plan=(WilmaContent.generate(WilmaPhase.FIND,SessionId("quiz"),RoundLength.FIVE,42) as GenerationResult.Generated).plan
            val start=SessionReducer.start(plan,ContentLanguage.ENGLISH,WilmaContent.repository)
            audio.effects(plan.id,start.state.language,start.effects)
            assertEquals(if(mode==AudioMode.OFF)0 else 1,engine.spoken.size)
            val answer=SessionReducer.reduce(start.state,SessionAction.Answer(start.state.nextAttempt!!,start.state.task.question.correct))
            audio.effects(plan.id,answer.state.language,answer.effects)
            assertEquals(if(mode==AudioMode.ALL)2 else if(mode==AudioMode.QUESTIONS)1 else 0,engine.spoken.size)
            val order=WilmaOrder.start(SessionId("order"),42,ContentLanguage.GERMAN)
            val before=engine.spoken.size
            val replay=WilmaOrder.reduce(order,WilmaOrderAction.Replay(order.task))
            audio.effects(order.id,order.language,listOfNotNull(replay.speech))
            assertEquals(before+if(mode==AudioMode.OFF)0 else 1,engine.spoken.size)
        }
    }
    @Test fun navigationRestoreLanguageAndStaleCallbacksCannotRestartOldSpeech() {
        val engine=FakeSpeechEngine();var status:SpeechResult?=null
        val audio=WilmaAudio(DefaultAudioController(engine,AudioMode.ALL)){status=it};audio.visible(true)
        audio.day(WilmaContent.days[0],ContentLanguage.ENGLISH);val old=engine.spoken.last().id
        audio.visible(false);audio.day(WilmaContent.days[1],ContentLanguage.ENGLISH);audio.visible(true)
        assertEquals(1,engine.spoken.size)
        audio.day(WilmaContent.days[2],ContentLanguage.GERMAN,true)
        engine.emit(engine.spoken.last().id,EngineResult.Failed(SpeechFailure.MISSING_OFFLINE_VOICE));engine.emit(old)
        assertEquals(SpeechResult.Failed(SpeechFailure.MISSING_OFFLINE_VOICE),status)
        assertEquals(2,engine.spoken.size);assertEquals(ContentLanguage.GERMAN,engine.spoken.last().context.language)
        audio.close();assertEquals(1,engine.closeCount)
    }
}
