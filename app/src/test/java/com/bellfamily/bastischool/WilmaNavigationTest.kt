package com.bellfamily.bastischool

import org.junit.Assert.*
import org.junit.Test

class WilmaNavigationTest {
    @Test fun chooserOptionsRestorationAndBackHaveStableNativeOrigins() {
        val hub=ShellNavigation().openDaysSeasons();val wilma=hub.openWilma()
        assertEquals(ShellScreen.WILMA,wilma.screen);assertEquals(BackAction.DAYS_HUB,wilma.backAction)
        assertFalse(wilma.ownsWebSession)
        repeat(5){assertEquals(wilma,wilma.openOptions().openOptions().closeOptions())}
        assertEquals(wilma,ShellNavigation.restore("OPTIONS","WILMA","verbs",null).closeOptions())
        assertEquals(hub,wilma.openDaysSeasons());assertEquals(BackAction.NATIVE_HOME,hub.backAction)
        assertEquals(ShellScreen.SEASONS,hub.openSeasons().screen)
        assertEquals(BackAction.LEGACY,hub.openActivity("time").backAction)
    }
}
