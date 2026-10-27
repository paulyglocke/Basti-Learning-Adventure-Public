package com.bellfamily.bastischool

import com.bellfamily.bastischool.ui.home.*
import com.bellfamily.bastischool.learning.animalgroups.AnimalGroups
import org.junit.Assert.*
import org.junit.Test

class HomeIntegrationTest {
    @Test fun liveCardsDispatchToTheirExistingOrNewDestinations() {
        val expected = listOf("TELL_ME", "VOCABULARY_CHOOSER", "WEB", "WEB", "WEB", "DAYS_SEASONS",
            "WEB", "SUBITISING", "QUANTITY_MATCH", "COMPARE_QUANTITY", "NUMBER_ORDER", "CLOCK",
            "ADD_TOGETHER", "WEB", "PREPOSITIONS", "WEB", "WASH_HANDS", "HABITATS_CHOOSER", "ANIMAL_GROUPS", "COLOURS_CHOOSER", "FOLLOW_LEVELS")
        val cards = homeActivities()
        assertEquals(21, cards.size)
        assertEquals(expected, cards.map { it.homeDestination().screen.name })
        cards.filter { it.homeDestination().screen == ShellScreen.WEB }.forEach {
            assertEquals(it.homeId(), it.homeDestination().mode)
        }
        assertEquals("positions", homeActivity("positions").homeDestination().mode)
        assertTrue(cards.none { it.play })
    }

    @Test fun sixBilingualCategoriesCoverTwentyAndMixedRemainsTwentyFirst() {
        assertEquals(listOf(7,5,2,3,1,2), HomeCategory.entries.map { it.activityIds.size })
        val ids = HomeCategory.entries.flatMap { it.activityIds }
        assertEquals(20, ids.distinct().size)
        assertEquals(homeActivities().map { it.homeId() }.toSet(), ids.toSet() + "mixed")
        assertEquals(listOf("Numbers & Maths","Words & Talking","Time & Seasons","Animals & Nature","Colours","Everyday Skills"), HomeCategory.entries.map { it.en })
        assertEquals(listOf("Zahlen & Rechnen","Wörter & Erzählen","Zeit & Jahreszeiten","Tiere & Natur","Farben","Alltag"), HomeCategory.entries.map { it.de })
        assertEquals(listOf("colourSort"), HomeCategory.entries.single { it.activityIds.size == 1 }.activityIds)
    }

    @Test fun homePagesAndOptionsRestoreWithoutNewLearningState() {
        val pages = listOf("home", "all") + HomeCategory.entries.map { "category:${it.name}" }
        pages.forEach { page ->
            val home = ShellNavigation().browseHome(page)
            assertEquals(if (page == "home") BackAction.EXIT else BackAction.HOME_ROOT, home.backAction)
            assertEquals(home, home.openOptions().closeOptions())
            for (route in listOf(home,home.openOptions())) {
                assertFalse(route.ownsWebSession)
                assertEquals(route,ShellNavigation.restore(route.screen.name,route.optionsOrigin.name,route.mode,null,page))
            }
            assertEquals(ShellNavigation(),home.home())
        }
    }

    @Test fun olderSavedRoutesAndMalformedHomePagesKeepSafeDefaults() {
        assertEquals(ShellNavigation(),ShellNavigation.restore("HOME",null,null,null))
        for (invalid in listOf("category:SHAPES","activity:mixed","settings","", "x".repeat(1000))) {
            assertEquals("home",ShellNavigation.restore("OPTIONS","HOME","verbs",null,invalid).homePage)
        }
        val legacy = ShellNavigation.restore("OPTIONS","WEB","count","{}","category:MATHS")
        assertEquals(ShellScreen.WEB,legacy.closeOptions().screen)
        assertEquals("home",legacy.homePage)
        assertTrue(legacy.ownsWebSession)
        assertEquals(ShellNavigation(recoveryFailed=true),ShellNavigation.restore("WEB","HOME","count",null,"all"))
    }

    @Test fun activityRootAndNestedBackContractsAreNotReinterpretedAsCategoryBack() {
        assertEquals(BackAction.NATIVE_HOME, homeActivity("tellme").homeDestination().backAction)
        assertEquals(BackAction.NATIVE_HOME, homeActivity("colourSort").homeDestination().backAction)
        assertEquals(ShellScreen.COLOURS_CHOOSER, homeActivity("colourSort").homeDestination().screen)
        assertEquals(BackAction.LEGACY, homeActivity("count").homeDestination().backAction)
        val months = ShellNavigation().openMonthsChooser()
        assertEquals(BackAction.DAYS_HUB,months.backAction)
        assertEquals(BackAction.MONTHS_AREA,months.openMonths().backAction)
        assertEquals(BackAction.MONTHS_AREA,months.openMonthsBetweenWheel().backAction)
        assertEquals(BackAction.MONTHS_AREA,months.openMonthsBuildYearFour().backAction)
        assertEquals(BackAction.FOLLOW_LEVELS,ShellNavigation().openFollowTwo().backAction)
    }

    @Test fun habitatExploreUsesItsOwnChooserAndKeepsTheAnimalsHomePageAsItsParent() {
        val chooser = homeActivity("exploreHabitats").homeDestination()
        assertEquals(ShellScreen.HABITATS_CHOOSER, chooser.screen)
        assertEquals(BackAction.ANIMALS_CATEGORY, chooser.backAction)
        val ocean = chooser.openOceanExplore()
        assertEquals(ShellScreen.OCEAN_EXPLORE, ocean.screen)
        assertEquals(BackAction.HABITATS_CHOOSER, ocean.backAction)
        assertEquals(chooser, ocean.openHabitatsChooser())
        assertEquals(ocean, ocean.openOptions().closeOptions())
        for (route in listOf(chooser, ocean, ocean.openOptions())) {
            val restored = ShellNavigation.restore(route.screen.name, route.optionsOrigin.name, route.mode, null, route.homePage)
            assertEquals(route.screen, restored.screen)
            assertEquals("category:ANIMALS", restored.homePage)
            assertFalse(restored.ownsWebSession)
        }
        assertEquals(BackAction.NATIVE_HOME, homeActivity("animalGroups").homeDestination().backAction)
    }

    @Test fun originalAnimalGroupsIdentityPoolAndWaterLandContractRemainUntouched() {
        assertEquals("activity.discover.animal_groups", AnimalGroups.activity.value)
        assertEquals(1, AnimalGroups.revision)
        assertEquals(1, AnimalGroups.version.schema)
        assertEquals(1, AnimalGroups.version.revision)
        assertEquals(0x41475231, AnimalGroups.journalMagic)
        assertEquals(listOf("animal.whale", "animal.dolphin", "animal.horse", "animal.rabbit"),
            AnimalGroups.objects.map { it.id.value })
        assertEquals(listOf("habitat.water", "habitat.water", "habitat.land", "habitat.land"),
            AnimalGroups.objects.map { it.category.value })
        assertEquals("rule.sort.animal_habitat", AnimalGroups.rule.id.value)
    }
}
