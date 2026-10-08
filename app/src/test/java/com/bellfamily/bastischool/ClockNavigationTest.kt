package com.bellfamily.bastischool

import org.junit.Assert.*
import org.junit.Test

class ClockNavigationTest {
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
