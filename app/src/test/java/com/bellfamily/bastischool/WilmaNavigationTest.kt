package com.bellfamily.bastischool

import org.junit.Assert.*
import org.junit.Test

class WilmaNavigationTest {
    @Test fun chooserOptionsRestorationAndBackHaveStableNativeOrigins() {
        val hub=ShellNavigation().openDaysSeasons();val chooser=hub.openWilma()
        assertEquals(ShellScreen.WILMA_CHOOSER,chooser.screen);assertEquals(BackAction.DAYS_HUB,chooser.backAction)
        val wilma=chooser.openWilmaActivity()
        assertEquals(ShellScreen.WILMA,wilma.screen);assertEquals(BackAction.WILMA_CHOOSER,wilma.backAction)
        assertFalse(wilma.ownsWebSession)
        repeat(5){assertEquals(chooser,chooser.openOptions().openOptions().closeOptions())}
        assertEquals(chooser,ShellNavigation.restore("OPTIONS","WILMA_CHOOSER","verbs",null).closeOptions())
        assertEquals(wilma,ShellNavigation.restore("OPTIONS","WILMA","verbs",null).closeOptions())
        assertEquals(ShellScreen.WILMA,wilma.screen) // Older saved activity routes still restore in place.
        assertEquals(hub,chooser.openDaysSeasons());assertEquals(BackAction.NATIVE_HOME,hub.backAction)
        assertEquals(ShellScreen.SEASONS_CHOOSER,hub.openSeasons().screen)
        assertEquals(BackAction.LEGACY,hub.openActivity("time").backAction)
    }
}
