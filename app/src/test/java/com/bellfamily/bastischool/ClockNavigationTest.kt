package com.bellfamily.bastischool

import org.junit.Assert.*
import org.junit.Test

class ClockNavigationTest {
    @Test fun practiceBackAndOptionsPreserveClockArea() {
        val route = ShellNavigation().openClockPractice()
        assertEquals(ShellScreen.CLOCK_MAKE, route.screen)
        assertEquals(BackAction.CLOCK_AREA, route.backAction)
        assertFalse(route.ownsWebSession)
        assertEquals(route, ShellNavigation.restore("OPTIONS", "CLOCK_MAKE", "verbs", null).closeOptions())
        assertEquals(route, route.openOptions().closeOptions())
    }
    @Test fun independentNativeExploreAndOptionsRestore() {
        val route=ShellNavigation().openClock()
        assertEquals(ShellScreen.CLOCK,route.screen)
        assertEquals(BackAction.NATIVE_HOME,route.backAction)
        assertFalse(route.ownsWebSession)
        assertEquals(route,route.openOptions().closeOptions())
        assertEquals(route,ShellNavigation.restore("CLOCK","HOME","verbs",null))
        assertEquals(ShellScreen.WEB,ShellNavigation().openActivity("time").screen)
        assertEquals(ShellScreen.DAYS_SEASONS,ShellNavigation().openDaysSeasons().screen)
    }
}
