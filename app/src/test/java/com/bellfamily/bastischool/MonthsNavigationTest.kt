package com.bellfamily.bastischool

import org.junit.Assert.*
import org.junit.Test

class MonthsNavigationTest {
    @Test fun monthsChooserAndExploreHaveSeparateNativeRoutes() {
        val chooser=ShellNavigation().openDaysSeasons().openMonthsChooser()
        assertEquals(ShellScreen.MONTHS_CHOOSER,chooser.screen)
        assertEquals(BackAction.DAYS_HUB,chooser.backAction)
        assertFalse(chooser.ownsWebSession)
        assertEquals(chooser,chooser.openOptions().closeOptions())
        assertEquals(chooser,ShellNavigation.restore("MONTHS_CHOOSER","HOME","verbs",null))
        assertEquals(chooser,ShellNavigation.restore("OPTIONS","MONTHS_CHOOSER","verbs",null).closeOptions())

        val explore=chooser.openMonths()
        assertEquals(ShellScreen.MONTHS,explore.screen)
        assertEquals(BackAction.MONTHS_AREA,explore.backAction)
        assertEquals(explore,ShellNavigation.restore("MONTHS","HOME","verbs",null))
        assertEquals(explore,ShellNavigation.restore("OPTIONS","MONTHS","verbs",null).closeOptions())
    }
    @Test fun quizRoutesRestoreOptionsAndReturnToChooser() {
        for(route in listOf(ShellNavigation().openMonthsFind(),ShellNavigation().openMonthsRelations(),ShellNavigation().openMonthsMissing(),
            ShellNavigation().openMonthsSeasons(), ShellNavigation().openMonthsSeasonsWheel())) {
            assertEquals(BackAction.MONTHS_AREA,route.backAction)
            assertFalse(route.ownsWebSession)
            assertEquals(route,route.openOptions().closeOptions())
            assertEquals(route,ShellNavigation.restore(route.screen.name,"HOME","verbs",null))
            assertEquals(route,ShellNavigation.restore("OPTIONS",route.screen.name,"verbs",null).closeOptions())
        }
    }
}
