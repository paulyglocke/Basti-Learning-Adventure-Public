package com.bellfamily.bastischool.learning.vocabulary

import com.bellfamily.bastischool.audio.*
import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.session.*
import org.junit.Assert.*
import org.junit.Test

class VocabularyAudioTest {
    @Test fun everyExploreWordReplayAndSentenceUseAuthoredLanguageAndRespectOff() {
        for(mode in AudioMode.entries)for(lang in ContentLanguage.entries) {
            val engine=FakeSpeechEngine();val audio=VocabularyAudio(DefaultAudioController(engine,mode));audio.visible(true)
            assertTrue(engine.spoken.isEmpty())
            VocabularyContent.items.forEach {item ->
                audio.word(item.id,lang);audio.word(item.id,lang,true)
                if(mode!=AudioMode.OFF){assertEquals(item.text.speech[lang],engine.spoken.last().text);assertEquals(SpeechTrigger.REPLAY,engine.spoken.last().trigger)}
                audio.example(item.id,lang)
                if(mode!=AudioMode.OFF){assertEquals(item.example.speech[lang],engine.spoken.last().text);assertEquals(lang,engine.spoken.last().context.language)}
            }
            assertEquals(if(mode==AudioMode.OFF)0 else 18,engine.spoken.size)
        }
    }
    @Test fun questionsFeedbackAndManualReplayUseExistingPolicyInBothModes() {
        for(phase in listOf(VocabularyPhase.FIND,VocabularyPhase.NAME))for(mode in AudioMode.entries) {
            val engine=FakeSpeechEngine();val audio=VocabularyAudio(DefaultAudioController(engine,mode));audio.visible(true)
            val plan=(VocabularyContent.generate(phase,SessionId("audio"),RoundLength.FIVE,1) as GenerationResult.Generated).plan
            val start=SessionReducer.start(plan,ContentLanguage.ENGLISH,VocabularyContent.repository)
            fun emit(t:SessionTransition)=audio.effects(t.state.plan.id,t.state.language,t.effects)
            emit(start);assertEquals(if(mode==AudioMode.OFF)0 else 1,engine.spoken.size)
            val answer=SessionReducer.reduce(start.state,SessionAction.Answer(start.state.nextAttempt!!,start.state.task.question.correct))
            emit(answer);assertEquals(if(mode==AudioMode.ALL)2 else if(mode==AudioMode.QUESTIONS)1 else 0,engine.spoken.size)
            val replay=SessionReducer.reduce(answer.state,SessionAction.Replay(answer.state.task.id));emit(replay);emit(replay)
            if(mode!=AudioMode.OFF){assertEquals(SpeechTrigger.REPLAY,engine.spoken.last().trigger);assertTrue(engine.cancelled.isNotEmpty())}
            else assertTrue(engine.spoken.isEmpty())
        }
    }
    @Test fun backgroundRestoreAndLanguageBoundariesAreSilentUntilExplicitSpeech() {
        val engine=FakeSpeechEngine();val audio=VocabularyAudio(DefaultAudioController(engine,AudioMode.ALL));audio.visible(true)
        val id=ContentId("animal.horse")
        audio.word(id,ContentLanguage.ENGLISH);audio.visible(false);audio.word(id,ContentLanguage.GERMAN)
        audio.visible(true);audio.effects(SessionId("restore"),ContentLanguage.GERMAN,emptyList())
        assertEquals(1,engine.spoken.size)
        audio.word(id,ContentLanguage.GERMAN,true)
        assertEquals("Pferd",engine.spoken.last().text);assertEquals(ContentLanguage.GERMAN,engine.spoken.last().context.language)
        audio.close();assertEquals(1,engine.closeCount)
    }
    @Test fun staleCallbacksCannotReplaceMissingVoiceFailureOrFallbackLanguage() {
        val engine=FakeSpeechEngine();var status:SpeechResult?=null
        val audio=VocabularyAudio(DefaultAudioController(engine,AudioMode.ALL)){status=it};audio.visible(true)
        audio.word(ContentId("animal.fish"),ContentLanguage.ENGLISH);val old=engine.spoken.last().id
        audio.word(ContentId("animal.fish"),ContentLanguage.GERMAN)
        engine.emit(engine.spoken.last().id,EngineResult.Failed(SpeechFailure.MISSING_OFFLINE_VOICE));engine.emit(old)
        assertEquals(SpeechResult.Failed(SpeechFailure.MISSING_OFFLINE_VOICE),status);assertEquals(2,engine.spoken.size)
        audio.visible(false);engine.emit(old);assertNull(status)
    }
}
