package com.bellfamily.bastischool

import org.junit.Assert.*
import org.junit.Test

class HomeCardsTest {
    @Test fun twentyLiveEntriesKeepTheirHistoricalGroupsOrderAndDispatchIdentity() {
        val expected = listOf(
            listOf("tellme", "vocabulary", "verbExplorer", "letters", "count"),
            listOf("time", "verbs", "subitising", "quantityMatch"),
            listOf("compareQuantity", "numberOrder", "clock", "addTogether", "math", "positions",
                "mixed", "washHands", "animalGroups", "colourSort", "follow")
        )
        val groups = homeCardGroups()
        assertEquals(expected, groups.map { group -> group.map { if (it.verbExplorer) "verbExplorer" else it.mode } })
        assertEquals(20, groups.flatten().size)
        assertEquals(20, expected.flatten().distinct().size)
        assertEquals(1, groups.flatten().count { it.verbExplorer })
        assertTrue(groups.flatten().filterNot { it.verbExplorer }.all { it.mode != null })
    }

    @Test fun unavailableGamesAreAbsentWithoutDroppingTheFollowingLiveEntries() {
        val cards = homeCardGroups().flatten()
        assertTrue(cards.none { it.play })
        listOf("Dragon Treasure Hunt", "Dinosaur Rescue", "Memory Pairs", "Crocodile Snap").forEach { title ->
            assertFalse(cards.any { it.titleEn == title })
        }
        assertEquals(listOf("washHands", "animalGroups", "colourSort", "follow"), cards.takeLast(4).map { it.mode })
    }

    @Test fun allExistingEnglishAndGermanLabelsArePreserved() {
        val cards = homeCardGroups().flatten()
        assertEquals(listOf("Tell Me!", "Vocabulary Booster", "Learn Verbs", "Letters", "Numbers",
            "Days & Seasons", "Animal Actions", "How many?", "Numbers & Groups", "More or Fewer",
            "Number Order", "Clock & Time", "Add Together", "Easy Maths", "Prepositions", "Mixed Adventure",
            "Wash Hands", "Animal Groups", "Colour Sort", "Follow the Instructions"), cards.map { it.titleEn })
        assertEquals(listOf("Erzähl mal!", "Wortschatz", "Verben lernen", "Buchstaben", "Zahlen",
            "Tage & Jahreszeiten", "Tier-Aktionen", "Wie viele?", "Zahlen & Mengen", "Mehr oder weniger",
            "Zahlenfolge", "Uhr & Zeit", "Zusammenzählen", "Einfache Mathe", "Präpositionen", "Gemischtes Abenteuer",
            "Hände waschen", "Tiere zuordnen", "Farben sortieren", "Anweisungen folgen"), cards.map { it.titleDe })
        assertTrue(cards.all { it.descriptionEn.isNotBlank() && it.descriptionDe.isNotBlank() })
    }
}
