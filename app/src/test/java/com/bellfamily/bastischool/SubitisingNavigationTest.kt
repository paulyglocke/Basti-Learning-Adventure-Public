package com.bellfamily.bastischool

import org.junit.Assert.*
import org.junit.Test

class SubitisingNavigationTest {
    @Test fun nativeRouteRestoresAndOptionsReturnWithoutOwningWebSession() {
        val route=ShellNavigation().openSubitising()
        assertEquals(ShellScreen.SUBITISING,route.screen)
        assertEquals(BackAction.NATIVE_HOME,route.backAction)
        assertFalse(route.ownsWebSession)
        assertEquals(route,route.openOptions().closeOptions())
        assertEquals(route,ShellNavigation.restore("SUBITISING","HOME","verbs",null))
    }
}
