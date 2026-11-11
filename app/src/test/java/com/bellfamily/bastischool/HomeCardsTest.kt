package com.bellfamily.bastischool

import org.junit.Assert.*
import org.junit.Test

class HomeCardsTest {
    @Test fun liveEntriesKeepTheirHistoricalGroupsAndAddSeparateBondBuilders() {
        val expected = listOf(
            listOf("tellme", "vocabulary", "verbExplorer", "letters", "count"),
            listOf("time", "verbs", "subitising", "quantityMatch"),
            listOf("compareQuantity", "numberOrder", "clock", "addTogether", "addChallenge", "subtraction", "mixedAddition", "buildFive", "buildTen", "makeFive", "countingTray", "math", "positions",
                "mixed", "memoryPairs", "washHands", "exploreHabitats", "matchHabitats", "animalGroups", "colourSort", "follow", "patternBuilder")
        )
        val groups = homeCardGroups()
        assertEquals(expected, groups.map { group -> group.map { if (it.verbExplorer) "verbExplorer" else it.mode } })
        assertEquals(31, groups.flatten().size)
        assertEquals(31, expected.flatten().distinct().size)
        assertEquals(1, groups.flatten().count { it.verbExplorer })
        assertTrue(groups.flatten().filterNot { it.verbExplorer }.all { it.mode != null })
    }

    @Test fun unavailableGamesAreAbsentWithoutDroppingTheFollowingLiveEntries() {
        val cards = homeCardGroups().flatten()
        assertTrue(cards.none { it.play })
        listOf("Dragon Treasure Hunt", "Dinosaur Rescue", "Crocodile Snap").forEach { title ->
            assertFalse(cards.any { it.titleEn == title })
        }
        assertEquals(listOf("washHands", "exploreHabitats", "matchHabitats", "animalGroups", "colourSort", "follow", "patternBuilder"), cards.takeLast(7).map { it.mode })
    }

    @Test fun allExistingEnglishAndGermanLabelsArePreserved() {
        val cards = homeCardGroups().flatten()
        assertEquals(listOf("Tell Me!", "Vocabulary Booster", "Learn Verbs", "Animal First Letters", "Numbers",
            "Days & Seasons", "Animal Actions", "How many?", "Numbers & Groups", "More or Fewer",
            "Number Order", "Clock & Time", "Add Together", "Add Bigger Numbers", "Take Away", "Mixed Addition", "Build 5", "Build 10", "Make 5", "Build a Dinosaur Group", "Easy Maths", "Prepositions", "Mixed Adventure",
            "Memory Pairs", "Wash Hands", "Explore Habitats", "Match Animals to Habitats", "Animal Groups", "Colour Sort", "Follow the Instructions", "Pattern Builder"), cards.map { it.titleEn })
        assertEquals(listOf("Erzähl mal!", "Wortschatz", "Verben lernen", "Tier-Anfangsbuchstaben", "Zahlen",
            "Tage & Jahreszeiten", "Tier-Aktionen", "Wie viele?", "Zahlen & Mengen", "Mehr oder weniger",
            "Zahlenfolge", "Uhr & Zeit", "Zusammenzählen", "Größere Zahlen addieren", "Wegnehmen", "Gemischte Plusaufgaben", "Fünf legen", "Zehn legen", "Fünf ergänzen", "Dinosauriergruppe legen", "Einfache Mathe", "Präpositionen", "Gemischtes Abenteuer",
            "Paare merken", "Hände waschen", "Lebensräume entdecken", "Tiere und Lebensräume zuordnen", "Tiere zuordnen", "Farben sortieren", "Anweisungen folgen", "Muster bauen"), cards.map { it.titleDe })
        assertTrue(cards.all { it.descriptionEn.isNotBlank() && it.descriptionDe.isNotBlank() })
    }
}
