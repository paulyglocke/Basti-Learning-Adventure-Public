package com.bellfamily.bastischool

import org.junit.Assert.*
import org.junit.Test

class AdditionNavigationTest {
    @Test fun newNativeRouteRestoresThroughOptionsAndKeepsLegacyMathsSeparate() {
        val route = ShellNavigation().openAddTogether()
        assertEquals(ShellScreen.ADD_TOGETHER, route.screen)
        assertEquals(BackAction.NATIVE_HOME, route.backAction)
        assertFalse(route.ownsWebSession)
        assertEquals(route, route.openOptions().closeOptions())
        assertEquals(route, ShellNavigation.restore("ADD_TOGETHER", "HOME", "verbs", null))
        assertEquals(ShellScreen.WEB, ShellNavigation().openActivity("math").screen)
        assertNotEquals(route, ShellNavigation().openSubitising())
    }

    @Test fun challengeHasItsOwnRestorableRouteAndReturnsHome() {
        val route = ShellNavigation().openAdditionChallenge()
        assertEquals(ShellScreen.ADD_CHALLENGE, route.screen)
        assertEquals(BackAction.NATIVE_HOME, route.backAction)
        assertEquals(route, route.openOptions().closeOptions())
        assertEquals(route, ShellNavigation.restore("ADD_CHALLENGE", "HOME", "verbs", null))
        assertNotEquals(route, ShellNavigation().openAddTogether())
    }
}
