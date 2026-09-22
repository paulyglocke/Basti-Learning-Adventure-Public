package com.bellfamily.bastischool.audio

import com.bellfamily.bastischool.learning.models.ContentLanguage
import org.junit.Assert.*
import org.junit.Test

class SystemSpeechEngineTest {
    private class Port : SystemTtsPort {
        lateinit var ready: (Boolean) -> Unit
        lateinit var result: (String, EngineResult) -> Unit
        var installed = listOf(OfflineVoice("en-gb", "en", "GB"), OfflineVoice("de-de", "de", "DE"))
        val selected = mutableListOf<String>()
        val played = mutableListOf<Pair<String, String>>()
        var enumerations = 0
        var stops = 0
        var closes = 0
        var acceptVoice = true
        var acceptSpeech = true
        var throwPlayback = false
        var throwEnumeration = false
        var throwStop = false
        override fun initialise(ready: (Boolean) -> Unit, result: (String, EngineResult) -> Unit) {
            this.ready = ready; this.result = result
        }
        override fun voices(): List<OfflineVoice> {
            enumerations++
            if (throwEnumeration) error("Unavailable engine metadata")
            return installed
        }
        override fun selectVoice(id: String): Boolean { selected.add(id); return acceptVoice }
        override fun speak(text: String, utteranceId: String): Boolean {
            if (throwPlayback) error("Engine disconnected")
            if (acceptSpeech) played.add(utteranceId to text)
            return acceptSpeech
        }
        override fun stop() { stops++; if (throwStop) error("Disconnected") }
        override fun close() { closes++ }
    }

    private val port = Port()
    private val engine = SystemSpeechEngine(port)
    private val audio = DefaultAudioController(engine, AudioMode.ALL)
    private fun speak(language: ContentLanguage = ContentLanguage.ENGLISH): SpeechTicket {
        val context = audio.openContext(SpeechOwner("quiz"), SpeechSessionId("round"), language)
        return audio.speak(SpeechRequest(context, if (language == ContentLanguage.ENGLISH) "Hello" else "Hallo", SpeechKind.QUESTION))
    }

    @Test fun initializationDispatchesLatestOwnedPendingRequestOnce() {
        val old = speak()
        val current = speak(ContentLanguage.GERMAN)
        assertEquals(EngineReadiness.INITIALISING, engine.readiness)
        assertTrue(port.played.isEmpty())
        port.ready(true)
        port.ready(true)
        assertEquals(1, port.enumerations)
        assertEquals(listOf("de-de"), port.selected)
        assertEquals(listOf("Hallo"), port.played.map { it.second })
        assertEquals(SpeechResult.Cancelled(SpeechCancellation.CONTEXT_CHANGED), old.result)
        port.result(port.played.single().first, EngineResult.Completed)
        assertEquals(SpeechResult.Completed, current.result)
    }

    @Test fun initFailureAndEnumerationFailureNeverDispatchAndExposeFailure() {
        val pending = speak()
        port.ready(false)
        assertEquals(EngineReadiness.FAILED, engine.readiness)
        assertEquals(SpeechResult.Failed(SpeechFailure.ENGINE_UNAVAILABLE), pending.result)
        assertEquals(pending.result, speak().result)
        assertEquals(0, port.enumerations)
        val brokenPort = Port().apply { throwEnumeration = true }
        val broken = SystemSpeechEngine(brokenPort)
        brokenPort.ready(true)
        assertEquals(EngineReadiness.FAILED, broken.readiness)
    }

    @Test fun preferredOfflineLocalesAreDeterministicAndCachedAcrossLanguageSwitches() {
        port.installed = listOf(
            OfflineVoice("z-en-gb", "en", "GB"), OfflineVoice("b-en-gb", "en", "GB"),
            OfflineVoice("a-en-us", "en", "US"), OfflineVoice("a-de-at", "de", "AT"),
            OfflineVoice("z-de-de", "de", "DE"), OfflineVoice("a-network", "en", "GB", networkRequired = true),
            OfflineVoice("a-absent", "de", "DE", installed = false)
        ).reversed()
        port.ready(true)
        speak(); speak(ContentLanguage.GERMAN); speak()
        assertEquals(listOf("b-en-gb", "z-de-de", "b-en-gb"), port.selected)
        assertEquals(1, port.enumerations)
    }

    @Test fun otherCountryFallbackStaysWithinRequestedLanguage() {
        port.installed = listOf(OfflineVoice("us", "en", "US"), OfflineVoice("at", "de", "AT"))
        port.ready(true)
        speak(); speak(ContentLanguage.GERMAN)
        assertEquals(listOf("us", "at"), port.selected)
    }

    @Test fun missingGermanAfterEnglishCannotReusePreviousVoiceOrNetworkVoice() {
        port.installed = listOf(OfflineVoice("english", "en", "GB"),
            OfflineVoice("network-de", "de", "DE", networkRequired = true),
            OfflineVoice("uninstalled-de", "de", "DE", installed = false))
        port.ready(true)
        speak()
        val de = speak(ContentLanguage.GERMAN)
        assertEquals(SpeechResult.Failed(SpeechFailure.MISSING_OFFLINE_VOICE), de.result)
        assertEquals(listOf("english"), port.selected)
        assertEquals(1, port.played.size)
        assertEquals(1, port.stops)
    }

    @Test fun missingEnglishAfterGermanCannotReusePreviousVoice() {
        port.installed = listOf(OfflineVoice("de", "de", "DE"))
        port.ready(true)
        speak(ContentLanguage.GERMAN)
        assertEquals(SpeechResult.Failed(SpeechFailure.MISSING_OFFLINE_VOICE), speak().result)
        assertEquals(listOf("de"), port.selected)
        assertEquals(1, port.played.size)
    }

    @Test fun emptyOfflineVoiceInventoryIsReadyButLanguageRequestExplicitlyFails() {
        port.installed = emptyList()
        port.ready(true)
        assertEquals(EngineReadiness.READY, engine.readiness)
        assertEquals(SpeechResult.Failed(SpeechFailure.MISSING_OFFLINE_VOICE), speak().result)
        assertTrue(port.played.isEmpty())
    }

    @Test fun voiceSelectionFailureNeverCallsSpeakWithPreviousVoice() {
        port.ready(true)
        speak()
        port.acceptVoice = false
        assertEquals(SpeechResult.Failed(SpeechFailure.VOICE_SELECTION), speak(ContentLanguage.GERMAN).result)
        assertEquals(1, port.played.size)
    }

    @Test fun rejectedThrownAndAsynchronousPlaybackFailuresAreExplicit() {
        port.ready(true)
        port.acceptSpeech = false
        assertEquals(SpeechResult.Failed(SpeechFailure.PLAYBACK), speak().result)
        port.acceptSpeech = true
        port.throwPlayback = true
        assertEquals(SpeechResult.Failed(SpeechFailure.PLAYBACK), speak().result)
        port.throwPlayback = false
        val async = speak()
        port.result(port.played.last().first, EngineResult.Failed(SpeechFailure.PLAYBACK))
        assertEquals(SpeechResult.Failed(SpeechFailure.PLAYBACK), async.result)
    }

    @Test fun platformStaleCallbacksAndDuplicateCallbacksAreIgnored() {
        port.ready(true)
        val old = speak()
        val oldId = port.played.last().first
        val current = speak()
        val newId = port.played.last().first
        assertNotEquals(oldId, newId)
        port.result(oldId, EngineResult.Completed)
        port.result(oldId, EngineResult.Cancelled)
        assertNull(current.result)
        port.result(newId, EngineResult.Completed)
        port.result(newId, EngineResult.Failed(SpeechFailure.PLAYBACK))
        assertEquals(SpeechResult.Completed, current.result)
        assertEquals(SpeechResult.Cancelled(SpeechCancellation.CONTEXT_CHANGED), old.result)
    }

    @Test fun failedStopDisablesEngineInsteadOfStartingOverlappingSpeech() {
        port.ready(true)
        speak()
        port.throwStop = true
        assertEquals(SpeechResult.Failed(SpeechFailure.ENGINE_UNAVAILABLE), speak().result)
        assertEquals(EngineReadiness.FAILED, engine.readiness)
        assertEquals(1, port.played.size)
    }

    @Test fun closeDuringInitializationNeverResurrectsEngineOrPlaysPendingSpeech() {
        val pending = speak()
        audio.close()
        port.ready(true)
        engine.close()
        assertEquals(EngineReadiness.CLOSED, engine.readiness)
        assertEquals(SpeechResult.Cancelled(SpeechCancellation.CLOSED), pending.result)
        assertTrue(port.played.isEmpty())
        assertEquals(0, port.enumerations)
        assertEquals(1, port.closes)
    }
}
