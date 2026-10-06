package com.bellfamily.bastischool

import org.junit.Assert.*
import org.junit.Test

class QuantityMatchNavigationTest {
    @Test fun quantityMatchIsSeparateNativeRoute() {
        val route = ShellNavigation().openQuantityMatch()
        assertEquals(ShellScreen.QUANTITY_MATCH, route.screen)
        assertEquals(BackAction.NATIVE_HOME, route.backAction)
        assertFalse(route.ownsWebSession)
        assertEquals(route, route.openOptions().closeOptions())
        assertEquals(route, ShellNavigation.restore("QUANTITY_MATCH", "HOME", "verbs", null))
    }
}
