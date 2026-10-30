package com.bellfamily.bastischool.ui.homeprototype

import com.bellfamily.bastischool.ui.home.*

import org.junit.Assert.*
import org.junit.Test
import java.io.File

class HomePrototypeModelTest {
    @Test fun refinedArtworkKeepsCanonicalCharactersAndApprovedScenes() {
        assertEquals(DRAGON_ART, homeAssetPaths[HomeCategory.WORDS])
        assertEquals("Animals/canonical/dinosaur.png", homeAssetPaths[HomeCategory.MATHS])
        assertEquals("Animals/canonical/whale.png", homeAssetPaths[HomeCategory.ANIMALS])
        assertTrue(homeAssetPaths.getValue(HomeCategory.COLOURS).endsWith("classroom_006_art_sorting.png"))
        assertTrue(homeAssetPaths.getValue(HomeCategory.EVERYDAY).endsWith("classroom_007_school_routines.png"))
    }

    @Test fun sixCategoriesCoverTwentyOneDistinctExistingIdentities() {
        val ids = HomeCategory.entries.flatMap { it.activityIds }
        assertEquals(listOf(7, 5, 2, 4, 1, 2), HomeCategory.entries.map { it.activityIds.size })
        assertEquals(21, ids.size)
        assertEquals(21, ids.toSet().size)
        assertEquals(homeActivities().map { it.homeId() }.toSet() - "mixed", ids.toSet())
    }

    @Test fun allActivitiesPreservesTwentyTwoCurrentCardsWithoutFutureGames() {
        val cards = homeActivities()
        assertEquals(22, cards.size)
        assertEquals(22, cards.map { it.homeId() }.toSet().size)
        assertFalse(cards.any { it.play })
        assertEquals("Mixed Adventure", homeActivity("mixed").titleEn)
        assertNull(homeCategory("mixed"))
        assertTrue(homeActivity("verbExplorer").verbExplorer)
        assertEquals("verbs", homeActivity("verbs").mode)
    }

    @Test fun categoryLabelsAreBilingualAndDoNotPromiseShapes() {
        assertEquals(listOf("Numbers & Maths", "Words & Talking", "Time & Seasons", "Animals & Nature", "Colours", "Everyday Skills"),
            HomeCategory.entries.map { it.label(false) })
        assertEquals(listOf("Zahlen & Rechnen", "Wörter & Erzählen", "Zeit & Jahreszeiten", "Tiere & Natur", "Farben", "Alltag"),
            HomeCategory.entries.map { it.label(true) })
        homeActivities().forEach { assertTrue(it.titleEn.isNotBlank()); assertTrue(it.titleDe.isNotBlank()) }
    }

    @Test fun localBackAndHomeKeepOriginWithoutLaunchingAnything() {
        val category = PrototypeNavigation().open("category:WORDS")
        val detail = category.open("activity:verbExplorer")
        assertEquals(category, detail.back())
        assertEquals(detail, detail.open(detail.current))
        assertEquals(detail, detail.open("settings").back())
        assertEquals(PrototypeNavigation(), detail.open("home"))
        assertEquals(PrototypeNavigation(), PrototypeNavigation().back())
        val all = PrototypeNavigation().open("all")
        assertEquals(all, all.open("activity:mixed").back())
    }

    @Test fun everyCategoryAndActivityRouteRoundTrips() {
        HomeCategory.entries.forEach { c ->
            val category = PrototypeNavigation().open("category:${c.name}")
            c.activityIds.forEach { id ->
                val detail = category.open("activity:$id")
                assertEquals(detail, PrototypeNavigation.restore(detail.stack))
                assertEquals(category, detail.back())
            }
        }
    }

    @Test fun invalidRestorationReturnsToHomeWelcomeCards() {
        listOf(emptyList(), listOf("activity:count"), listOf("home", "activity:unknown"),
            List(9) { "home" }).forEach { assertEquals(PrototypeNavigation(), PrototypeNavigation.restore(it)) }
        assertThrows(IllegalArgumentException::class.java) { PrototypeNavigation().open("production:count") }
    }

    @Test fun columnsAdaptToAvailableWidthAndTextWithoutUnboundedTabletCards() {
        assertEquals(2, homeColumns(363f, 1f))
        assertEquals(1, homeColumns(363f, 1.5f))
        assertEquals(1, homeColumns(312f, 2f))
        assertEquals(3, homeColumns(1040f, 1f))
        assertEquals(3, homeColumns(2000f, 2f))
        assertEquals(2, homeColumns(752f, 2f))
    }

    @Test fun artReferencesExistAndPrototypeHasNoProductionSessionDependencies() {
        (homeAssetPaths.values + DRAGON_ART).forEach { assertTrue(it, File("src/main/assets", it).isFile) }
        assertTrue(File("src/main/res/drawable-nodpi/seasons_center.png").isFile)
        val dir = File("src/debug/java/com/bellfamily/bastischool/ui/homeprototype")
        dir.listFiles()!!.filter { it.extension == "kt" }.forEach { file ->
            val source = file.readText()
            listOf("ViewModelProvider", "SharedPreferences", "TextToSpeech", "WebView(", "ShellNavigation", "startActivity(")
                .filterNot { it == "ShellNavigation" && file.name == "HomePrototypeModel.kt" }
                .forEach { assertFalse("${file.name}: $it", source.contains(it)) }
        }
    }
}
