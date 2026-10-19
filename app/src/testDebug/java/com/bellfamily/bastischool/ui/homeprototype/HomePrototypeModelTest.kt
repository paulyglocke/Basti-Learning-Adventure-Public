package com.bellfamily.bastischool.ui.homeprototype

import org.junit.Assert.*
import org.junit.Test
import java.io.File

class HomePrototypeModelTest {
    @Test fun refinedArtworkKeepsCanonicalCharactersAndApprovedScenes() {
        assertEquals(DRAGON_ART, prototypeAssetPaths[PrototypeCategory.WORDS])
        assertEquals("Animals/canonical/dinosaur.png", prototypeAssetPaths[PrototypeCategory.MATHS])
        assertEquals("Animals/canonical/whale.png", prototypeAssetPaths[PrototypeCategory.ANIMALS])
        assertTrue(prototypeAssetPaths.getValue(PrototypeCategory.COLOURS).endsWith("classroom_006_art_sorting.png"))
        assertTrue(prototypeAssetPaths.getValue(PrototypeCategory.EVERYDAY).endsWith("classroom_007_school_routines.png"))
    }

    @Test fun sixCategoriesCoverNineteenDistinctExistingIdentities() {
        val ids = PrototypeCategory.entries.flatMap { it.activityIds }
        assertEquals(listOf(7, 5, 2, 2, 1, 2), PrototypeCategory.entries.map { it.activityIds.size })
        assertEquals(19, ids.size)
        assertEquals(19, ids.toSet().size)
        assertEquals(prototypeActivities().map { it.prototypeId() }.toSet() - "mixed", ids.toSet())
    }

    @Test fun allActivitiesPreservesTwentyOriginalCardsWithoutFutureGames() {
        val cards = prototypeActivities()
        assertEquals(20, cards.size)
        assertEquals(20, cards.map { it.prototypeId() }.toSet().size)
        assertFalse(cards.any { it.play })
        assertEquals("Mixed Adventure", prototypeActivity("mixed").titleEn)
        assertNull(prototypeCategory("mixed"))
        assertTrue(prototypeActivity("verbExplorer").verbExplorer)
        assertEquals("verbs", prototypeActivity("verbs").mode)
    }

    @Test fun categoryLabelsAreBilingualAndDoNotPromiseShapes() {
        assertEquals(listOf("Numbers & Maths", "Words & Talking", "Time & Seasons", "Animals & Nature", "Colours", "Everyday Skills"),
            PrototypeCategory.entries.map { it.label(false) })
        assertEquals(listOf("Zahlen & Rechnen", "Wörter & Erzählen", "Zeit & Jahreszeiten", "Tiere & Natur", "Farben", "Alltag"),
            PrototypeCategory.entries.map { it.label(true) })
        prototypeActivities().forEach { assertTrue(it.titleEn.isNotBlank()); assertTrue(it.titleDe.isNotBlank()) }
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
        PrototypeCategory.entries.forEach { c ->
            val category = PrototypeNavigation().open("category:${c.name}")
            c.activityIds.forEach { id ->
                val detail = category.open("activity:$id")
                assertEquals(detail, PrototypeNavigation.restore(detail.stack))
                assertEquals(category, detail.back())
            }
        }
    }

    @Test fun invalidRestorationReturnsToPrototypeHome() {
        listOf(emptyList(), listOf("activity:count"), listOf("home", "activity:unknown"),
            List(9) { "home" }).forEach { assertEquals(PrototypeNavigation(), PrototypeNavigation.restore(it)) }
        assertThrows(IllegalArgumentException::class.java) { PrototypeNavigation().open("production:count") }
    }

    @Test fun columnsAdaptToAvailableWidthAndTextWithoutUnboundedTabletCards() {
        assertEquals(2, prototypeColumns(363f, 1f))
        assertEquals(1, prototypeColumns(363f, 1.5f))
        assertEquals(1, prototypeColumns(312f, 2f))
        assertEquals(3, prototypeColumns(1040f, 1f))
        assertEquals(3, prototypeColumns(2000f, 2f))
        assertEquals(2, prototypeColumns(752f, 2f))
    }

    @Test fun artReferencesExistAndPrototypeHasNoProductionSessionDependencies() {
        (prototypeAssetPaths.values + DRAGON_ART).forEach { assertTrue(it, File("src/main/assets", it).isFile) }
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
