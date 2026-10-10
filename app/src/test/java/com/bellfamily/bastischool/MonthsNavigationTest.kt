package com.bellfamily.bastischool

import org.junit.Assert.*
import org.junit.Test

class MonthsNavigationTest {
    @Test fun monthsIsNativeAndReturnsToDaysHub() {
        val route=ShellNavigation().openDaysSeasons().openMonths()
        assertEquals(ShellScreen.MONTHS,route.screen)
        assertEquals(BackAction.DAYS_HUB,route.backAction)
        assertFalse(route.ownsWebSession)
        assertEquals(route,route.openOptions().closeOptions())
        assertEquals(route,ShellNavigation.restore("MONTHS","HOME","verbs",null))
        assertEquals(route,ShellNavigation.restore("OPTIONS","MONTHS","verbs",null).closeOptions())
    }
}
