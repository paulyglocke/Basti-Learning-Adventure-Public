package com.bellfamily.bastischool.audio

import com.bellfamily.bastischool.learning.models.ContentLanguage
import com.bellfamily.bastischool.learning.models.ContentText
import com.bellfamily.bastischool.learning.models.LocalizedText
import org.junit.Assert.*
import org.junit.Test

class AudioControllerTest {
    private val engine = FakeSpeechEngine()
    private val audio = DefaultAudioController(engine, AudioMode.ALL)
    private val owner = SpeechOwner("quiz")
    private val session = SpeechSessionId("round-1")
    private fun context(language: ContentLanguage = ContentLanguage.ENGLISH) = audio.openContext(owner, session, language)
    private fun request(context: SpeechContext = context(), kind: SpeechKind = SpeechKind.QUESTION,
                        trigger: SpeechTrigger = SpeechTrigger.AUTOMATIC, text: String = "Touch the snake.") =
        SpeechRequest(context, text, kind, trigger)

    @Test fun allAllowsEveryAuthoredSpeechKindAndTrigger() {
        for (kind in SpeechKind.entries) for (trigger in SpeechTrigger.entries) {
            val ticket = audio.speak(request(kind = kind, trigger = trigger))
            assertNull(ticket.result)
            assertEquals(kind, engine.spoken.last().kind)
            assertEquals(trigger, engine.spoken.last().trigger)
            engine.emit(ticket.id)
            assertEquals(SpeechResult.Completed, ticket.result)
        }
    }

    @Test fun questionsAllowsInstructionsTutorialsExplanationsAndAllManualControls() {
        audio.setMode(AudioMode.QUESTIONS)
        for (kind in listOf(SpeechKind.QUESTION, SpeechKind.INSTRUCTION, SpeechKind.TUTORIAL, SpeechKind.EXPLANATION)) {
            assertNull(audio.speak(request(kind = kind)).result)
        }
        for (kind in SpeechKind.entries) for (trigger in listOf(SpeechTrigger.REPLAY, SpeechTrigger.MANUAL)) {
            assertNull(audio.speak(request(kind = kind, trigger = trigger)).result)
        }
    }

    @Test fun questionsSuppressesAutomaticFeedbackPraiseCompletionAndNonInstructionalContent() {
        audio.setMode(AudioMode.QUESTIONS)
        for (kind in listOf(SpeechKind.FEEDBACK, SpeechKind.COMPLETION, SpeechKind.OPTION,
            SpeechKind.CHARACTER_LINE, SpeechKind.DISCOVERY_FACT)) {
            assertEquals(SpeechResult.Suppressed(SpeechSuppression.POLICY), audio.speak(request(kind = kind)).result)
        }
        assertTrue(engine.spoken.isEmpty())
    }

    @Test fun offBlocksAutomaticReplayOptionAndListenWithoutDispatch() {
        audio.setMode(AudioMode.OFF)
        for (kind in SpeechKind.entries) for (trigger in SpeechTrigger.entries) {
            assertEquals(SpeechResult.Suppressed(SpeechSuppression.POLICY),
                audio.speak(request(kind = kind, trigger = trigger)).result)
        }
        assertTrue(engine.spoken.isEmpty())
    }

    @Test fun repeatedReplayRestartsSameAuthoredInstructionWithDistinctIdsAndNoQueue() {
        val prompt = request(trigger = SpeechTrigger.REPLAY)
        val tickets = (1..3).map { audio.speak(prompt) }
        assertEquals(3, tickets.map { it.id }.toSet().size)
        assertEquals(listOf(tickets[0].id, tickets[1].id), engine.cancelled)
        assertEquals(List(3) { prompt.speechText }, engine.spoken.map { it.text })
        tickets.take(2).forEach { assertEquals(SpeechResult.Cancelled(SpeechCancellation.REPLACED), it.result) }
        assertNull(tickets.last().result)
    }

    @Test fun repeatedOptionSpeakerReplacesWithoutChangingItsSemanticKind() {
        val option = request(kind = SpeechKind.OPTION, trigger = SpeechTrigger.MANUAL, text = "Krokodil")
        val first = audio.speak(option)
        val second = audio.speak(option)
        assertEquals(listOf(first.id), engine.cancelled)
        assertNull(second.result)
        assertTrue(engine.spoken.all { it.kind == SpeechKind.OPTION && it.trigger == SpeechTrigger.MANUAL })
    }

    @Test fun staleAndDuplicateCompletionCannotCompleteNewRequestOrDoubleCompleteOldId() {
        val results = mutableListOf<SpeechOutcome>()
        val prompt = request()
        val old = audio.speak(prompt, results::add)
        val current = audio.speak(prompt, results::add)
        engine.emit(old.id)
        engine.emit(old.id, EngineResult.Failed(SpeechFailure.PLAYBACK))
        assertNull(current.result)
        repeat(3) { engine.emit(current.id) }
        assertEquals(listOf(SpeechOutcome(old.id, SpeechResult.Cancelled(SpeechCancellation.REPLACED)),
            SpeechOutcome(current.id, SpeechResult.Completed)), results)
    }

    @Test fun initializationKeepsOnlyLatestPendingRequestAndDispatchesOnce() {
        engine.become(EngineReadiness.INITIALISING)
        val prompt = request()
        val old = audio.speak(prompt)
        val current = audio.speak(prompt.copy(speechText = "Current"))
        assertTrue(engine.spoken.isEmpty())
        assertEquals(SpeechResult.Cancelled(SpeechCancellation.REPLACED), old.result)
        repeat(2) { engine.become(EngineReadiness.READY) }
        assertEquals(listOf(current.id), engine.spoken.map { it.id })
    }

    @Test fun cancellationDuringInitializationPreventsPlaybackWhenReady() {
        engine.become(EngineReadiness.INITIALISING)
        val pending = audio.speak(request())
        audio.cancelOwner(owner)
        engine.become(EngineReadiness.READY)
        assertEquals(SpeechResult.Cancelled(SpeechCancellation.OWNER_LEFT), pending.result)
        assertTrue(engine.spoken.isEmpty())
    }

    @Test fun failedInitializationRejectsPendingAndFutureSpeechExplicitly() {
        engine.become(EngineReadiness.INITIALISING)
        val prompt = request()
        val pending = audio.speak(prompt)
        engine.become(EngineReadiness.FAILED)
        assertEquals(SpeechResult.Failed(SpeechFailure.ENGINE_UNAVAILABLE), pending.result)
        assertEquals(pending.result, audio.speak(prompt).result)
        assertTrue(engine.spoken.isEmpty())
    }

    @Test fun languageChangeCancelsOldLanguageAndUsesAuthoredSpeechNotDisplay() {
        val text = ContentText(LocalizedText("WRONG 🌟", "FALSCH 🎉"), LocalizedText("Hello!", "Grüße!"))
        val en = context()
        val old = audio.speak(SpeechRequest.fromContent(en, text, SpeechKind.QUESTION))
        val de = context(ContentLanguage.GERMAN)
        audio.speak(SpeechRequest.fromContent(de, text, SpeechKind.QUESTION))
        assertEquals(SpeechResult.Cancelled(SpeechCancellation.CONTEXT_CHANGED), old.result)
        assertEquals(listOf("Hello!", "Grüße!"), engine.spoken.map { it.text })
        assertEquals(listOf(ContentLanguage.ENGLISH, ContentLanguage.GERMAN), engine.spoken.map { it.context.language })
        assertEquals(SpeechResult.Suppressed(SpeechSuppression.STALE_CONTEXT), audio.speak(request(en)).result)
    }

    @Test fun leavingAndReturningSameOwnerSessionCannotReviveAnOldContext() {
        val old = context()
        audio.speak(request(old))
        audio.cancelOwner(owner)
        val next = context()
        assertEquals(1, engine.spoken.size) // Return itself is silent.
        assertEquals(SpeechResult.Suppressed(SpeechSuppression.STALE_CONTEXT), audio.speak(request(old)).result)
        assertNull(audio.speak(request(next, trigger = SpeechTrigger.REPLAY)).result)
    }

    @Test fun otherOwnerSessionAndOldRequestCancellationCannotStopCurrentSpeech() {
        val first = audio.speak(request())
        val current = audio.speak(request())
        audio.cancelOwner(SpeechOwner("lesson"))
        audio.cancelSession(SpeechSessionId("round-0"))
        audio.cancelRequest(first.id)
        assertNull(current.result)
        audio.cancelSession(session)
        assertEquals(SpeechResult.Cancelled(SpeechCancellation.SESSION_ENDED), current.result)
    }

    @Test fun requestCancellationDoesNotRevokeCurrentContext() {
        val prompt = request()
        val ticket = audio.speak(prompt)
        audio.cancelRequest(ticket.id)
        assertEquals(SpeechResult.Cancelled(SpeechCancellation.REQUESTED), ticket.result)
        assertNull(audio.speak(prompt.copy(trigger = SpeechTrigger.REPLAY)).result)
    }

    @Test fun requestIdFromAnotherControllerCannotCancelThisControllerRequest() {
        val other = DefaultAudioController(FakeSpeechEngine(), AudioMode.ALL)
        val otherContext = other.openContext(owner, session, ContentLanguage.ENGLISH)
        val otherTicket = other.speak(request(otherContext))
        val current = audio.speak(request())
        audio.cancelRequest(otherTicket.id)
        assertNull(current.result)
        assertNotEquals(otherTicket.id, current.id)
    }

    @Test fun engineReadinessFailureTerminatesAlreadySubmittedRequest() {
        val current = audio.speak(request())
        engine.become(EngineReadiness.FAILED)
        engine.emit(current.id)
        assertEquals(SpeechResult.Failed(SpeechFailure.ENGINE_UNAVAILABLE), current.result)
    }

    @Test fun feedbackPreventsDelayedInstructionEvenAfterCompletionUntilNextContext() {
        val ctx = context()
        val question = audio.speak(request(ctx))
        val feedback = audio.speak(request(ctx, SpeechKind.FEEDBACK))
        assertEquals(SpeechResult.Cancelled(SpeechCancellation.REPLACED), question.result)
        assertEquals(SpeechResult.Suppressed(SpeechSuppression.LOWER_PRIORITY), audio.speak(request(ctx)).result)
        engine.emit(feedback.id)
        assertEquals(SpeechResult.Suppressed(SpeechSuppression.LOWER_PRIORITY), audio.speak(request(ctx)).result)
        assertNull(audio.speak(request(ctx, trigger = SpeechTrigger.REPLAY)).result)
        assertNull(audio.speak(request()).result) // New question opens a fresh context.
    }

    @Test fun mutedFeedbackAlsoCancelsPendingQuestionAndBlocksDelayedInstruction() {
        audio.setMode(AudioMode.QUESTIONS)
        engine.become(EngineReadiness.INITIALISING)
        val ctx = context()
        val question = audio.speak(request(ctx))
        val feedback = audio.speak(request(ctx, SpeechKind.FEEDBACK))
        assertEquals(SpeechResult.Suppressed(SpeechSuppression.POLICY), feedback.result)
        assertEquals(SpeechResult.Cancelled(SpeechCancellation.REPLACED), question.result)
        engine.become(EngineReadiness.READY)
        assertEquals(SpeechResult.Suppressed(SpeechSuppression.LOWER_PRIORITY), audio.speak(request(ctx)).result)
        assertTrue(engine.spoken.isEmpty())
    }

    @Test fun changingPolicyCancelsForbiddenPlaybackAndDoesNotReplayOnEnable() {
        val feedback = audio.speak(request(kind = SpeechKind.FEEDBACK))
        audio.setMode(AudioMode.QUESTIONS)
        assertEquals(SpeechResult.Cancelled(SpeechCancellation.POLICY_CHANGED), feedback.result)
        val manual = audio.speak(request(trigger = SpeechTrigger.REPLAY))
        audio.setMode(AudioMode.OFF)
        assertEquals(SpeechResult.Cancelled(SpeechCancellation.POLICY_CHANGED), manual.result)
        audio.setMode(AudioMode.ALL)
        assertEquals(2, engine.spoken.size)
    }

    @Test fun engineMissingVoiceFailureAndUnexpectedStopAreExplicitTerminalResults() {
        for (reason in SpeechFailure.entries) {
            val ticket = audio.speak(request())
            engine.emit(ticket.id, EngineResult.Failed(reason))
            assertEquals(SpeechResult.Failed(reason), ticket.result)
        }
        val stopped = audio.speak(request())
        engine.emit(stopped.id, EngineResult.Cancelled)
        assertEquals(SpeechResult.Cancelled(SpeechCancellation.ENGINE_STOPPED), stopped.result)
        engine.throwOnSpeak = true
        assertEquals(SpeechResult.Failed(SpeechFailure.PLAYBACK), audio.speak(request()).result)
    }

    @Test fun closeIsTerminalIdempotentAndRejectsLateCallbacks() {
        val prompt = request()
        val ticket = audio.speak(prompt)
        repeat(2) { audio.close() }
        engine.emit(ticket.id)
        assertEquals(SpeechResult.Cancelled(SpeechCancellation.CLOSED), ticket.result)
        assertEquals(SpeechResult.Suppressed(SpeechSuppression.CLOSED), audio.speak(prompt).result)
        assertEquals(1, engine.closeCount)
        assertThrows(IllegalStateException::class.java) { context() }
    }

    @Test fun resultObserverMaySubmitReplayWithoutCorruptingReplacement() {
        val prompt = request()
        var replay: SpeechTicket? = null
        audio.speak(prompt) { replay = audio.speak(prompt.copy(trigger = SpeechTrigger.REPLAY)) }
        val replaced = audio.speak(prompt)
        assertEquals(SpeechResult.Cancelled(SpeechCancellation.REPLACED), replaced.result)
        assertNull(replay!!.result)
        engine.emit(replay!!.id)
        assertEquals(SpeechResult.Completed, replay!!.result)
    }

    @Test fun safetyBoundaryStripsDecorationAndRejectsEmptyUtterance() {
        audio.speak(request(text = "  Grüße! 🐉  10, groß.\n ÄÖÜäöüß! "))
        assertEquals("Grüße! 10, groß. ÄÖÜäöüß!", engine.spoken.single().text)
        assertEquals(SpeechResult.Failed(SpeechFailure.EMPTY_TEXT), audio.speak(request(text = "🌟🎉🐉")).result)
        assertEquals(1, engine.spoken.size)
    }
}
