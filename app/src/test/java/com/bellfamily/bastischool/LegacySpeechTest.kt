package com.bellfamily.bastischool

import org.junit.Assert.*
import org.junit.Test

class LegacySpeechTest {
    private val en = LegacySpeech.OfflineVoice("english", "en", "en", "GB", false)
    private val de = LegacySpeech.OfflineVoice("german", "de", "de", "DE", false)

    @Test fun initialisationRetainsOnlyLatestAndCompletionBelongsToOwner() {
        val played = mutableListOf<String>()
        val results = mutableListOf<Pair<String, Boolean>>()
        val speech = LegacySpeech<String>({}, { voice, text, _ -> played.add("$voice:$text"); true })
        assertEquals(LegacySpeech.Readiness.INITIALISING, speech.readiness)
        speech.request("old", "en", "1") { results.add("1" to it) }
        speech.request("new", "de", "2") { results.add("2" to it) }
        assertTrue(played.isEmpty())
        speech.initialise(true, listOf(en, de))
        assertEquals(listOf("german:new"), played)
        speech.completed("1", true)
        assertEquals(listOf("1" to false), results)
        speech.completed("2", true)
        assertEquals(listOf("1" to false, "2" to true), results)
    }

    @Test fun navigationDropsPendingAndInterruptedCompletion() {
        val played = mutableListOf<String>()
        val results = mutableListOf<Boolean>()
        val speech = LegacySpeech<String>({}, { _, text, _ -> played.add(text); true })
        speech.request("pending", "en", "1") { results.add(it) }
        speech.stop()
        speech.initialise(true, listOf(en))
        assertTrue(played.isEmpty())
        speech.request("active", "en", "2") { results.add(it) }
        speech.stop()
        speech.completed("2", true)
        assertEquals(listOf(false, false), results)
    }

    @Test fun languagesSelectOfflineVoicesAndMissingNeverReusesPreviousVoice() {
        val played = mutableListOf<String>()
        val results = mutableListOf<Boolean>()
        val speech = LegacySpeech<String>({}, { voice, _, _ -> played.add(voice); true })
        speech.initialise(true, listOf(en, de.copy(network = true)))
        speech.request("hello", "en", "1") {}
        speech.request("hallo", "de", "2") { results.add(it) }
        assertEquals(listOf("english"), played)
        assertEquals(listOf(false), results)
        speech.initialise(true, listOf(en, de))
        speech.request("hallo", "de", "3") {}
        speech.request("hello", "en", "4") {}
        assertEquals(listOf("english", "german", "english"), played)
    }

    @Test fun failureRejectsPendingAndFutureRequestsWithoutPlaying() {
        val results = mutableListOf<Boolean>()
        val speech = LegacySpeech<String>({}, { _, _, _ -> fail("must not play"); true })
        speech.request("pending", "en", "1") { results.add(it) }
        speech.initialise(false, listOf(en))
        speech.request("later", "en", "2") { results.add(it) }
        assertEquals(LegacySpeech.Readiness.FAILED, speech.readiness)
        assertEquals(listOf(false, false), results)
    }

    @Test fun rejectedPlaybackNeverReportsSuccessfulPresentation() {
        val results = mutableListOf<Boolean>()
        val speech = LegacySpeech<String>({}, { _, _, _ -> false })
        speech.initialise(true, listOf(en))
        speech.request("hello", "en", "1") { results.add(it) }
        speech.completed("1", true)
        assertEquals(listOf(false), results)
    }

    @Test fun preferredLocaleAndSameLanguageFallbackExcludeUninstalledVoices() {
        val played = mutableListOf<String>()
        val speech = LegacySpeech<String>({}, { voice, _, _ -> played.add(voice); true })
        val us = en.copy(value = "american", name = "a", country = "US")
        speech.initialise(true, listOf(us, en, de.copy(installed = false)))
        speech.request("hello", "en", "1") {}
        speech.request("hallo", "de", "2") {}
        assertEquals(listOf("english"), played)
        speech.initialise(true, listOf(us))
        speech.request("hello", "en", "3") {}
        assertEquals(listOf("english", "american"), played)
    }

    @Test fun repeatedRequestsStopPreviousPlaybackAndIgnoreLateCompletion() {
        var stops = 0
        val successful = mutableListOf<String>()
        val speech = LegacySpeech<String>({ stops++ }, { _, _, _ -> true })
        speech.initialise(true, listOf(en))
        repeat(3) { n -> speech.request("replay", "en", "$n") { if (it) successful.add("$n") } }
        speech.completed("0", true)
        speech.completed("1", true)
        speech.completed("2", true)
        assertEquals(3, stops)
        assertEquals(listOf("2"), successful)
    }
}
