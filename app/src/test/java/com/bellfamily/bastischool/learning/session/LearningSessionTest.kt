package com.bellfamily.bastischool.learning.session

import com.bellfamily.bastischool.audio.*
import com.bellfamily.bastischool.learning.models.ContentLanguage
import org.junit.Assert.*
import org.junit.Test
import com.bellfamily.bastischool.learning.session.SessionFixtures as F

class LearningSessionTest {
    private val engine = FakeSpeechEngine()
    private val audio = DefaultAudioController(engine, AudioMode.ALL)
    private val owner = SpeechOwner("test-screen")
    private fun start(language: ContentLanguage = ContentLanguage.ENGLISH) =
        LearningSession.start(F.plan(), language, F.content, audio, owner)
    private fun answer(session: LearningSession, correct: Boolean = true): List<SessionEffect> {
        val state = session.state
        return session.dispatch(SessionAction.Answer(state.nextAttempt!!,
            if (correct) state.task.question.correct else state.task.question.choices.first { it != state.task.question.correct }))
    }
    private fun restore(session: LearningSession): LearningSession {
        val bytes = SessionCheckpoint.encode(session.state)
        session.close()
        val result = SessionCheckpoint.restore(bytes, F.activity, 1, F.content) as SessionRestoreResult.Restored
        return LearningSession.restore(result, audio, owner)
    }
    @Test fun startUsesAuthoredSpeechAndDoesNotMakeSpeechCompletionAnAnswer() {
        val session = start()
        assertEquals(session.state.task.question.instruction.speech.en, engine.spoken.single().text)
        assertEquals(SpeechTrigger.AUTOMATIC, engine.spoken.single().trigger)
        val before = session.state
        engine.emit(engine.spoken.single().id)
        assertSame(before, session.state)
        assertEquals(0, session.state.score)
        assertEquals(SpeechResult.Completed, session.lastSpeech!!.result)
    }
    @Test fun replayReplacesAudioWithoutSubmittingAnAttemptAndStaleCallbackIsIgnored() {
        val session = start()
        val old = engine.spoken.last().id
        repeat(2) { session.dispatch(SessionAction.Replay(session.state.task.id)) }
        assertEquals(0, session.state.current.attempts)
        assertEquals(2, session.state.current.support.replays)
        assertEquals(2, engine.cancelled.size)
        engine.emit(old)
        assertNull(session.lastSpeech)
        engine.emit(engine.spoken.last().id)
        assertEquals(SpeechResult.Completed, session.lastSpeech!!.result)
    }
    @Test fun nativeAudioModePolicyStillControlsQuestionsFeedbackAndReplay() {
        for (mode in AudioMode.entries) {
            audio.setMode(mode)
            val before = engine.spoken.size
            val session = start()
            session.dispatch(SessionAction.Replay(session.state.task.id))
            answer(session)
            val spoken = engine.spoken.drop(before)
            when (mode) {
                AudioMode.ALL -> assertEquals(listOf(SpeechKind.INSTRUCTION, SpeechKind.INSTRUCTION, SpeechKind.FEEDBACK), spoken.map { it.kind })
                AudioMode.QUESTIONS -> assertEquals(listOf(SpeechKind.INSTRUCTION, SpeechKind.INSTRUCTION), spoken.map { it.kind })
                AudioMode.OFF -> assertTrue(spoken.isEmpty())
            }
            assertEquals(1, session.state.score) // Audio availability does not gate learning.
            session.close()
        }
    }
    @Test fun languageSwitchIsSilentAndGermanReplayKeepsSameTaskAndScore() {
        val session = start()
        answer(session)
        val task = session.state.task
        val count = engine.spoken.size
        session.dispatch(SessionAction.Language(ContentLanguage.GERMAN))
        assertEquals(count, engine.spoken.size)
        assertSame(task, session.state.task)
        assertEquals(1, session.state.score)
        session.dispatch(SessionAction.Replay(task.id))
        assertEquals(task.question.instruction.speech.de, engine.spoken.last().text)
        assertEquals(ContentLanguage.GERMAN, engine.spoken.last().context.language)
    }
    @Test fun restoredAnsweredAndUnansweredSessionsStaySilentUntilReplayInBothLanguages() {
        for (language in ContentLanguage.entries) for (answered in listOf(false, true)) {
            val old = start(language)
            if (answered) answer(old)
            val before = engine.spoken.size
            val restored = restore(old)
            assertEquals(before, engine.spoken.size)
            assertEquals(old.state.task.id, restored.state.task.id)
            assertEquals(old.state.progress, restored.state.progress)
            assertNull(restored.lastSpeech)
            restored.dispatch(SessionAction.Replay(restored.state.task.id))
            assertEquals(before + 1, engine.spoken.size)
            assertEquals(language, engine.spoken.last().context.language)
            restored.close()
        }
    }
    @Test fun restoreWhileInitializingDropsOldPendingNarration() {
        engine.become(EngineReadiness.INITIALISING)
        val original = start()
        val restored = restore(original)
        engine.become(EngineReadiness.READY)
        assertTrue(engine.spoken.isEmpty())
        restored.dispatch(SessionAction.Replay(restored.state.task.id))
        assertEquals(1, engine.spoken.size)
    }
    @Test fun oldEngineCallbacksCannotMutateRestoredSession() {
        val original = start()
        val oldId = engine.spoken.last().id
        val restored = restore(original)
        val snapshot = restored.state
        engine.emit(oldId)
        assertSame(snapshot, restored.state)
        assertNull(restored.lastSpeech)
        assertEquals(0, snapshot.score)
    }
    @Test fun backgroundResumeAndCloseCancelSpeechAndNeverReplayOrAward() {
        val session = start()
        val snapshot = session.state
        val oldId = engine.spoken.last().id
        session.suspend()
        assertTrue(answer(session).isEmpty())
        engine.emit(oldId)
        session.resume()
        assertSame(snapshot, session.state)
        assertEquals(1, engine.spoken.size)
        session.close()
        session.resume()
        assertTrue(session.dispatch(SessionAction.Replay(snapshot.task.id)).isEmpty())
    }
    @Test fun completionAcknowledgementDoesNotInterruptSummaryAndCompletedRestoreDoesNotAward() {
        val session = start()
        var effects = emptyList<SessionEffect>()
        repeat(5) {
            answer(session)
            effects = session.dispatch(SessionAction.Next(session.state.task.id))
        }
        val request = effects.filterIsInstance<SessionEffect.Complete>().single().request
        val summary = engine.spoken.last().id
        session.dispatch(SessionAction.CompletionResult(request.id, true))
        assertFalse(summary in engine.cancelled)
        val spoken = engine.spoken.size
        val restored = restore(session)
        assertEquals(spoken, engine.spoken.size)
        assertEquals(CompletionState.ACKNOWLEDGED, restored.state.completion)
        assertTrue(restored.dispatch(SessionAction.Next(restored.state.task.id)).isEmpty())
        assertTrue(restored.dispatch(SessionAction.Replay(restored.state.task.id)).isEmpty())
        assertEquals(F.summary.speech.en, engine.spoken.last().text)
    }
    @Test fun hintIsManualInstructionRatherThanReplayAndDoesNotCountAnAttempt() {
        val session = start()
        session.dispatch(SessionAction.Hint(session.state.task.id))
        assertEquals(SpeechTrigger.MANUAL, engine.spoken.last().trigger)
        assertEquals(SpeechKind.INSTRUCTION, engine.spoken.last().kind)
        assertEquals(session.state.task.question.hint!!.speech.en, engine.spoken.last().text)
        assertEquals(0, session.state.current.attempts)
        assertEquals(0, session.state.current.support.replays)
        assertTrue(session.state.current.support.hint)
    }
    @Test fun speechFailureLeavesTaskUsableAndDoesNotChangeLearningResult() {
        val session = start()
        val before = session.state
        engine.emit(engine.spoken.last().id, EngineResult.Failed(SpeechFailure.MISSING_OFFLINE_VOICE))
        assertSame(before, session.state)
        assertEquals(SpeechResult.Failed(SpeechFailure.MISSING_OFFLINE_VOICE), session.lastSpeech!!.result)
        assertEquals(1, answer(session).filterIsInstance<SessionEffect.RecordAttempt>().size)
        assertEquals(1, session.state.score)
    }
}
