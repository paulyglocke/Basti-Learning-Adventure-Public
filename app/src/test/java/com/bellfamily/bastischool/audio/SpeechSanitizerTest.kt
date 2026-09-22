package com.bellfamily.bastischool.audio

import org.junit.Assert.assertEquals
import org.junit.Test

class SpeechSanitizerTest {
    @Test fun allEnglishAndGermanPraiseIsSpeechSafe() {
        listOf("Great! 🌟" to "Great!", "Yes! 🎉" to "Yes!", "Correct! ⭐" to "Correct!",
            "Well done! 🐉" to "Well done!", "Super! 🌟" to "Super!", "Ja! 🎉" to "Ja!",
            "Richtig! ⭐" to "Richtig!", "Sehr gut! 🐉" to "Sehr gut!").forEach { (input, expected) ->
            assertEquals(expected, sanitizeSpeech(input))
        }
    }

    @Test fun compoundEmojiKeycapsFlagsAndUiSymbolsNeverReachEngine() {
        assertEquals("", sanitizeSpeech("👩🏽‍🏫 🏳️‍🌈 🇩🇪 1️⃣ #️⃣ *️⃣ ➡️ ★ 🔊 👁️ ©️ ™️"))
        assertEquals("Hello world!", sanitizeSpeech("Hello🐉world!"))
    }

    @Test fun germanPunctuationWhitespaceAndAuthoredNumbersArePreserved() {
        assertEquals("ÄÖÜ äöü ß – Grüße! ‘Ja’, 10? 3 + 2 = 5.",
            sanitizeSpeech("\tÄÖÜ äöü ß – Grüße!\n ‘Ja’,\u00a0 10? 3 + 2 = 5.  "))
    }
}
