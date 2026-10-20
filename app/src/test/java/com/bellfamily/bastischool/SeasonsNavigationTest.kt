package com.bellfamily.bastischool

import org.junit.Assert.*
import org.junit.Test

class SeasonsNavigationTest {
    @Test fun hubAndSeasonsRestoreOptionsOriginWithoutWebViewOrLoops() {
        val hub=ShellNavigation().openDaysSeasons()
        val chooser=hub.openSeasons()
        assertEquals(BackAction.NATIVE_HOME,hub.backAction)
        assertEquals(ShellScreen.SEASONS_CHOOSER,chooser.screen)
        assertEquals(BackAction.DAYS_HUB,chooser.backAction)
        val seasons=chooser.openSeasonsActivity()
        assertEquals(ShellScreen.SEASONS,seasons.screen)
        assertEquals(BackAction.SEASONS_CHOOSER,seasons.backAction)
        for(route in listOf(hub,chooser,seasons)) {
            assertFalse(route.ownsWebSession)
            repeat(5) {assertEquals(route,route.openOptions().openOptions().closeOptions())}
            val restored=ShellNavigation.restore("OPTIONS",route.screen.name,route.mode,null)
            assertEquals(route,restored.closeOptions())
            assertFalse(restored.ownsWebSession)
        }
        assertEquals(hub,chooser.openDaysSeasons())
        assertEquals(chooser,seasons.openSeasons())
        assertEquals(ShellScreen.HOME,hub.home().screen)
    }
    @Test fun legacyDaysAndSeasonsRemainsAvailable() {
        val legacy=ShellNavigation().openDaysSeasons().openActivity("time")
        assertTrue(legacy.ownsWebSession)
        assertEquals("time",legacy.mode)
        assertEquals(BackAction.LEGACY,legacy.backAction)
    }
}
