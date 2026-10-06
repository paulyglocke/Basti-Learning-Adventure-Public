package com.bellfamily.bastischool

import org.junit.Assert.*
import org.junit.Test

class CompareQuantityNavigationTest {
    @Test fun separateNativeRouteRestoresThroughOptions() {
        val route = ShellNavigation().openCompareQuantity()
        assertEquals(ShellScreen.COMPARE_QUANTITY, route.screen)
        assertEquals(BackAction.NATIVE_HOME, route.backAction)
        assertFalse(route.ownsWebSession)
        assertEquals(route, route.openOptions().closeOptions())
        assertEquals(route, ShellNavigation.restore("COMPARE_QUANTITY", "HOME", "verbs", null))
        assertNotEquals(route, ShellNavigation().openSubitising())
        assertNotEquals(route, ShellNavigation().openQuantityMatch())
    }
}
