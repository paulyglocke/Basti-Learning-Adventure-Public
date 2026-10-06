package com.bellfamily.bastischool.learning.prepositions

import com.bellfamily.bastischool.audio.*
import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.session.*
import org.junit.Assert.*
import org.junit.Test

class PrepositionsGuidedAudioTest {
    private fun state(language:ContentLanguage=ContentLanguage.ENGLISH) = SessionReducer.start(
        PrepositionsContentTest.plan(),language,PrepositionsContent.repository).state
    private fun replay(audio:PrepositionsAudio,s:SessionState) {
        val transition=SessionReducer.reduce(s,SessionAction.Replay(s.task.id))
        audio.effects(transition.state,transition.effects)
    }

    @Test fun guidedReplayUsesSixDistinctUtterancesInVisibleOrderAndHighlightsOnlyAnswers() {
        for(language in ContentLanguage.entries) for(mode in listOf(AudioMode.ALL,AudioMode.QUESTIONS)) {
            val engine=FakeSpeechEngine()
            var highlighted:ContentId?=null
            val audio=PrepositionsAudio(DefaultAudioController(engine,mode),spokenOption={highlighted=it})
            val s=state(language);val saved=SessionCheckpoint.encode(s)
            val scene=PrepositionsContent.scene(s.task)
            audio.visible(true);assertTrue(engine.spoken.isEmpty())
            replay(audio,s)
            assertEquals(1,engine.spoken.size);assertNull(highlighted)
            assertEquals(positionQuestion(scene).speech[language],engine.spoken.last().text)
            engine.emit(engine.spoken.last().id)
            assertEquals(sanitizeSpeech(positionAnswerStem(scene).speech[language]),engine.spoken.last().text)
            assertNull(highlighted)
            engine.emit(engine.spoken.last().id)
            s.task.question.choices.forEachIndexed { index,id ->
                assertEquals(index+3,engine.spoken.size)
                assertEquals(id,highlighted)
                assertEquals(answerPhrase(scene,id,language)+".",engine.spoken.last().text)
                engine.emit(engine.spoken.last().id)
            }
            assertEquals(6,engine.spoken.size);assertNull(highlighted)
            assertTrue(engine.spoken.all {it.context.language==language && it.trigger==SpeechTrigger.REPLAY})
            assertArrayEquals(saved,SessionCheckpoint.encode(s))
        }
    }

    @Test fun cancellationNavigationLanguageModeAndCloseRevokeSequenceAndLateCallbacks() {
        for(boundary in listOf("cancel","navigation","language","off","close")) {
            val engine=FakeSpeechEngine();var highlighted:ContentId?=null
            val audio=PrepositionsAudio(DefaultAudioController(engine,AudioMode.ALL),spokenOption={highlighted=it})
            val s=state();audio.visible(true);replay(audio,s)
            repeat(2){engine.emit(engine.spoken.last().id)}
            assertNotNull(highlighted)
            val old=engine.spoken.last().id;val count=engine.spoken.size
            when(boundary) {
                "cancel" -> audio.cancel()
                "navigation" -> audio.visible(false)
                "language" -> {
                    val de=SessionReducer.reduce(s,SessionAction.Language(ContentLanguage.GERMAN))
                    audio.effects(de.state,de.effects)
                }
                "off" -> audio.mode(AudioMode.OFF)
                "close" -> audio.close()
            }
            assertNull(highlighted)
            engine.emit(old);assertEquals(count,engine.spoken.size);assertNull(highlighted)
        }
    }

    @Test fun offAndPendingEngineNeverHighlightAndFailuresDoNotAdvance() {
        val engine=FakeSpeechEngine(EngineReadiness.INITIALISING)
        var highlighted:ContentId?=null
        val seen=mutableListOf<ContentId?>()
        val audio=PrepositionsAudio(DefaultAudioController(engine,AudioMode.OFF),spokenOption={highlighted=it;seen+=it})
        val s=state();audio.visible(true)
        replay(audio,s);audio.option(s,s.task.question.choices[2])
        assertTrue(engine.spoken.isEmpty());assertTrue(seen.all {it==null})
        audio.mode(AudioMode.ALL);replay(audio,s)
        assertNull(highlighted);assertTrue(engine.spoken.isEmpty())
        engine.become(EngineReadiness.READY)
        repeat(2){engine.emit(engine.spoken.last().id)}
        assertNotNull(highlighted)
        engine.emit(engine.spoken.last().id,EngineResult.Failed(SpeechFailure.PLAYBACK))
        assertNull(highlighted);assertEquals(3,engine.spoken.size)
    }

    @Test fun individualListenInterruptsGuidedSpeechAndDoesNotAnswer() {
        for(language in ContentLanguage.entries) {
            val engine=FakeSpeechEngine();var highlighted:ContentId?=null
            val audio=PrepositionsAudio(DefaultAudioController(engine,AudioMode.QUESTIONS),spokenOption={highlighted=it})
            val s=state(language);val saved=SessionCheckpoint.encode(s)
            audio.visible(true);replay(audio,s)
            repeat(2){engine.emit(engine.spoken.last().id)}
            val old=engine.spoken.last().id
            val third=s.task.question.choices[2]
            audio.option(s,third)
            assertEquals(third,highlighted)
            assertEquals(answerPhrase(PrepositionsContent.scene(s.task),third,language)+".",engine.spoken.last().text)
            assertEquals(SpeechTrigger.MANUAL,engine.spoken.last().trigger)
            assertEquals(language,engine.spoken.last().context.language)
            engine.emit(old);assertEquals(third,highlighted)
            engine.emit(engine.spoken.last().id)
            assertNull(highlighted);assertEquals(4,engine.spoken.size)
            assertArrayEquals(saved,SessionCheckpoint.encode(s))
        }
    }

    @Test fun synchronousCompletionAndEngineStopCannotLeaveAnAnswerHighlighted() {
        var highlighted:ContentId?=null
        val immediate=object:SpeechEngine {
            override val readiness=EngineReadiness.READY
            override var onReadinessChanged:((EngineReadiness)->Unit)?=null
            var calls=0
            override fun speak(request:EngineSpeechRequest,result:(EngineResult)->Unit) {calls++;result(EngineResult.Completed)}
            override fun stop()=Unit
            override fun close()=Unit
        }
        val audio=PrepositionsAudio(DefaultAudioController(immediate,AudioMode.ALL),spokenOption={highlighted=it})
        audio.visible(true);replay(audio,state())
        assertEquals(6,immediate.calls);assertNull(highlighted)

        val engine=FakeSpeechEngine()
        val stopped=PrepositionsAudio(DefaultAudioController(engine,AudioMode.ALL),spokenOption={highlighted=it})
        stopped.visible(true);replay(stopped,state())
        repeat(2){engine.emit(engine.spoken.last().id)}
        assertNotNull(highlighted)
        val old=engine.spoken.last().id
        engine.emit(old,EngineResult.Cancelled);assertNull(highlighted)
        engine.emit(old);assertEquals(3,engine.spoken.size);assertNull(highlighted)
    }

    @Test fun answeringAndNextCancelHighlightsWhileExistingRetryHelpAndProgressStayIntact() {
        val engine=FakeSpeechEngine();var highlighted:ContentId?=null
        val audio=PrepositionsAudio(DefaultAudioController(engine,AudioMode.ALL),spokenOption={highlighted=it})
        var s=state();audio.visible(true)
        fun dispatch(action:SessionAction):List<SessionEffect> {
            audio.cancel() // The retained owner cancels before accepting every learning action.
            val t=SessionReducer.reduce(s,action);s=t.state;audio.effects(s,t.effects);return t.effects
        }
        replay(audio,s);repeat(2){engine.emit(engine.spoken.last().id)}
        assertNotNull(highlighted)
        val wrong=s.task.question.choices.first {it!=s.task.question.correct}
        val effects=dispatch(SessionAction.Answer(s.nextAttempt!!,wrong))
        assertNull(highlighted);assertEquals(1,s.current.attempts)
        assertEquals(1,effects.filterIsInstance<SessionEffect.RecordAttempt>().size)
        dispatch(SessionAction.Retry(AttemptId(s.task.id,s.current.attempts)))
        assertEquals(1,s.current.attempts)
        dispatch(SessionAction.Hint(s.task.id));assertTrue(s.current.support.hint)
        dispatch(SessionAction.Answer(s.nextAttempt!!,s.task.question.correct))
        assertEquals(2,s.current.attempts);assertEquals(1,s.score)
        dispatch(SessionAction.Next(s.task.id));assertEquals(1,s.index);assertNull(highlighted)
    }
}
