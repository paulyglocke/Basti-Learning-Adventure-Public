package com.bellfamily.bastischool

import com.bellfamily.bastischool.ui.home.*
import org.junit.Assert.*
import org.junit.Test

class HomeIntegrationTest {
    @Test fun allTwentyCardsDispatchToTheirHistoricalDestinations() {
        val expected = listOf("TELL_ME", "VOCABULARY", "WEB", "WEB", "WEB", "DAYS_SEASONS",
            "WEB", "SUBITISING", "QUANTITY_MATCH", "COMPARE_QUANTITY", "NUMBER_ORDER", "CLOCK",
            "ADD_TOGETHER", "WEB", "PREPOSITIONS", "WEB", "WASH_HANDS", "ANIMAL_GROUPS", "COLOUR_SORT", "FOLLOW_LEVELS")
        val cards = homeActivities()
        assertEquals(20, cards.size)
        assertEquals(expected, cards.map { it.homeDestination().screen.name })
        cards.filter { it.homeDestination().screen == ShellScreen.WEB }.forEach {
            assertEquals(it.homeId(), it.homeDestination().mode)
        }
        assertEquals("positions", homeActivity("positions").homeDestination().mode)
        assertTrue(cards.none { it.play })
    }

    @Test fun sixBilingualCategoriesCoverNineteenAndMixedRemainsTwentieth() {
        assertEquals(listOf(7,5,2,2,1,2), HomeCategory.entries.map { it.activityIds.size })
        val ids = HomeCategory.entries.flatMap { it.activityIds }
        assertEquals(19, ids.distinct().size)
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
        assertEquals(BackAction.LEGACY, homeActivity("count").homeDestination().backAction)
        val months = ShellNavigation().openMonths()
        assertEquals(BackAction.DAYS_HUB,months.backAction)
        assertEquals(BackAction.MONTHS_AREA,months.openMonthsBetweenWheel().backAction)
        assertEquals(BackAction.MONTHS_AREA,months.openMonthsBuildYearFour().backAction)
        assertEquals(BackAction.FOLLOW_LEVELS,ShellNavigation().openFollowTwo().backAction)
    }
}
