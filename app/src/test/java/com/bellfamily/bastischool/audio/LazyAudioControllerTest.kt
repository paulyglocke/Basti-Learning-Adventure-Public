package com.bellfamily.bastischool.audio

import com.bellfamily.bastischool.learning.models.ContentLanguage
import org.junit.Assert.*
import org.junit.Test

class LazyAudioControllerTest {
    private val engine = FakeSpeechEngine(EngineReadiness.INITIALISING)
    private var creations = 0
    private val audio = DefaultAudioController({ creations++; engine }, AudioMode.ALL)
    private val owner = SpeechOwner("lazy-owner")
    private val session = SpeechSessionId("round")
    private fun context(language: ContentLanguage = ContentLanguage.ENGLISH) = audio.openContext(owner, session, language)
    private fun request(context: SpeechContext = context(), text: String = "Listen", kind: SpeechKind = SpeechKind.QUESTION,
                        trigger: SpeechTrigger = SpeechTrigger.REPLAY) = SpeechRequest(context, text, kind, trigger)

    @Test fun constructionReadinessSettingsCancellationAndUnusedCloseDoNotAllocate() {
        assertEquals(EngineReadiness.INITIALISING, audio.readiness)
        context(); audio.setMode(AudioMode.OFF); audio.setMode(AudioMode.ALL)
        audio.cancelOwner(owner); audio.cancelSession(session)
        audio.close(); audio.close()
        assertEquals(EngineReadiness.CLOSED, audio.readiness)
        assertEquals(0, creations); assertEquals(0, engine.closeCount)
    }

    @Test fun offBlocksEveryKindAndTriggerWithoutCreatingEngine() {
        audio.setMode(AudioMode.OFF)
        for (kind in SpeechKind.entries) for (trigger in SpeechTrigger.entries) {
            assertEquals(SpeechResult.Suppressed(SpeechSuppression.POLICY), audio.speak(request(kind = kind, trigger = trigger)).result)
        }
        audio.setMode(AudioMode.ALL)
        assertEquals(0, creations)
    }

    @Test fun suppressedStaleLowerPriorityAndEmptyRequestsDoNotAllocate() {
        val stale = context(); context()
        assertEquals(SpeechResult.Suppressed(SpeechSuppression.STALE_CONTEXT), audio.speak(request(stale)).result)
        audio.setMode(AudioMode.QUESTIONS)
        val current = context()
        assertEquals(SpeechResult.Suppressed(SpeechSuppression.POLICY),
            audio.speak(request(current, kind = SpeechKind.FEEDBACK, trigger = SpeechTrigger.AUTOMATIC)).result)
        assertEquals(SpeechResult.Suppressed(SpeechSuppression.LOWER_PRIORITY),
            audio.speak(request(current, trigger = SpeechTrigger.AUTOMATIC)).result)
        assertEquals(SpeechResult.Failed(SpeechFailure.EMPTY_TEXT), audio.speak(request(text = "🎈")).result)
        assertEquals(0, creations)
    }

    @Test fun firstRequestWaitsThenSpeaksWithoutSecondActionAndLaterSpeechReusesEngine() {
        val first = audio.speak(request(text = "First"))
        assertEquals(1, creations); assertNull(first.result); assertTrue(engine.spoken.isEmpty())
        engine.become(EngineReadiness.READY)
        assertEquals(first.id, engine.spoken.single().id)
        assertEquals("First", engine.spoken.single().text)
        engine.emit(first.id)
        val next = audio.speak(request(text = "Next"))
        assertEquals(1, creations); assertEquals(next.id, engine.spoken.last().id)
    }

    @Test fun alreadyReadyFactorySpeaksFirstRequestImmediately() {
        engine.become(EngineReadiness.READY)
        val ticket = audio.speak(request())
        assertEquals(1, creations); assertEquals(ticket.id, engine.spoken.single().id)
    }

    @Test fun replacementDuringInitializationRetainsOnlyLatestRequest() {
        val current = context()
        val first = audio.speak(request(current, "First"))
        val second = audio.speak(request(current, "Second"))
        assertEquals(SpeechResult.Cancelled(SpeechCancellation.REPLACED), first.result)
        engine.become(EngineReadiness.READY)
        assertEquals(1, creations); assertEquals(second.id, engine.spoken.single().id)
        assertEquals("Second", engine.spoken.single().text)
    }

    @Test fun navigationDuringInitializationPreventsStalePlaybackAndReturnIsSilent() {
        val first = audio.speak(request())
        audio.cancelOwner(owner)
        engine.become(EngineReadiness.READY); context()
        assertEquals(SpeechResult.Cancelled(SpeechCancellation.OWNER_LEFT), first.result)
        assertTrue(engine.spoken.isEmpty())
        audio.speak(request()); assertEquals(1, creations); assertEquals(1, engine.spoken.size)
    }

    @Test fun offDuringInitializationCancelsAndOnDoesNotReplay() {
        val first = audio.speak(request())
        audio.setMode(AudioMode.OFF); engine.become(EngineReadiness.READY); audio.setMode(AudioMode.ALL)
        assertEquals(SpeechResult.Cancelled(SpeechCancellation.POLICY_CHANGED), first.result)
        assertEquals(1, creations); assertTrue(engine.spoken.isEmpty())
    }

    @Test fun languageChangeDuringInitializationDropsEnglishAndSpeaksOnlyNewGerman() {
        val en = audio.speak(request(text = "English"))
        val de = context(ContentLanguage.GERMAN)
        assertEquals(SpeechResult.Cancelled(SpeechCancellation.CONTEXT_CHANGED), en.result)
        val latest = audio.speak(request(de, "Deutsch"))
        engine.become(EngineReadiness.READY)
        assertEquals(latest.id, engine.spoken.single().id)
        assertEquals(ContentLanguage.GERMAN, engine.spoken.single().context.language)
        assertEquals("Deutsch", engine.spoken.single().text); assertEquals(1, creations)
    }

    @Test fun closeDuringInitializationClosesOnceAndLateReadyCannotPlay() {
        val ticket = audio.speak(request())
        audio.close(); audio.close(); engine.become(EngineReadiness.READY)
        assertEquals(SpeechResult.Cancelled(SpeechCancellation.CLOSED), ticket.result)
        assertEquals(1, engine.closeCount); assertEquals(1, creations); assertTrue(engine.spoken.isEmpty())
        assertEquals(EngineReadiness.CLOSED, audio.readiness)
    }

    @Test fun closeDuringPlaybackClosesOnceAndRejectsLateCompletion() {
        engine.become(EngineReadiness.READY)
        var results = 0
        val ticket = audio.speak(request()) { results++ }
        audio.close(); audio.close(); engine.emit(ticket.id)
        assertEquals(1, results); assertEquals(1, engine.closeCount)
        assertEquals(SpeechResult.Cancelled(SpeechCancellation.CLOSED), ticket.result)
    }

    @Test fun failedInitializationIsExplicitAndDoesNotCreateAnotherEngine() {
        val ticket = audio.speak(request())
        engine.become(EngineReadiness.FAILED)
        assertEquals(SpeechResult.Failed(SpeechFailure.ENGINE_UNAVAILABLE), ticket.result)
        assertEquals(ticket.result, audio.speak(request()).result)
        assertEquals(1, creations); audio.close(); assertEquals(1, engine.closeCount)
    }

    @Test fun constructionFailureIsTerminalWithoutRepeatedAllocationAttempts() {
        var calls = 0
        val failed = DefaultAudioController({ calls++; error("Construction failed") }, AudioMode.ALL)
        val ctx = failed.openContext(owner, session, ContentLanguage.ENGLISH)
        repeat(2) { assertEquals(SpeechResult.Failed(SpeechFailure.ENGINE_UNAVAILABLE), failed.speak(request(ctx)).result) }
        assertEquals(EngineReadiness.FAILED, failed.readiness)
        failed.close(); failed.close(); assertEquals(1, calls)
    }
}
