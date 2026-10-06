package com.bellfamily.bastischool

import org.junit.Assert.*
import org.junit.Test

class NumberOrderNavigationTest {
    @Test fun separateNativeRouteRestoresThroughOptions() {
        val route = ShellNavigation().openNumberOrder()
        assertEquals(ShellScreen.NUMBER_ORDER, route.screen)
        assertEquals(BackAction.NATIVE_HOME, route.backAction)
        assertFalse(route.ownsWebSession)
        assertEquals(route, route.openOptions().closeOptions())
        assertEquals(route, ShellNavigation.restore("NUMBER_ORDER", "HOME", "verbs", null))
        assertNotEquals(route, ShellNavigation().openCompareQuantity())
        assertNotEquals(route, ShellNavigation().openQuantityMatch())
        assertNotEquals(route, ShellNavigation().openSubitising())
    }
}
