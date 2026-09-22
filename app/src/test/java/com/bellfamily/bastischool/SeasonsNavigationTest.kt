package com.bellfamily.bastischool

import org.junit.Assert.*
import org.junit.Test

class SeasonsNavigationTest {
    @Test fun hubAndSeasonsRestoreOptionsOriginWithoutWebViewOrLoops() {
        val hub=ShellNavigation().openDaysSeasons()
        val seasons=hub.openSeasons()
        assertEquals(BackAction.NATIVE_HOME,hub.backAction)
        assertEquals(BackAction.DAYS_HUB,seasons.backAction)
        for(route in listOf(hub,seasons)) {
            assertFalse(route.ownsWebSession)
            repeat(5) {assertEquals(route,route.openOptions().openOptions().closeOptions())}
            val restored=ShellNavigation.restore("OPTIONS",route.screen.name,route.mode,null)
            assertEquals(route,restored.closeOptions())
            assertFalse(restored.ownsWebSession)
        }
        assertEquals(hub,seasons.openDaysSeasons())
        assertEquals(ShellScreen.HOME,hub.home().screen)
    }
    @Test fun legacyDaysAndSeasonsRemainsAvailable() {
        val legacy=ShellNavigation().openDaysSeasons().openActivity("time")
        assertTrue(legacy.ownsWebSession)
        assertEquals("time",legacy.mode)
        assertEquals(BackAction.LEGACY,legacy.backAction)
    }
}
