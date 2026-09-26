package com.bellfamily.bastischool

import org.junit.Assert.*
import org.junit.Test

class TellMeNavigationTest {
    @Test fun nativeRouteOptionsAndConfigurationRestoreNeedNoLegacyCheckpoint() {
        val route = ShellNavigation().openTellMe()
        assertEquals(ShellScreen.TELL_ME, route.screen)
        assertEquals(BackAction.NATIVE_HOME, route.backAction)
        assertFalse(route.ownsWebSession)
        assertEquals(route, route.openOptions().closeOptions())
        assertEquals(route, ShellNavigation.restore("TELL_ME", "HOME", "verbs", null))
        assertEquals(ShellNavigation(), route.home())
    }
}
