package com.bellfamily.bastischool

import org.junit.Assert.*
import org.junit.Test

class PrepositionsNavigationTest {
    @Test fun nativeOptionsBackAndRestorationDoNotRequireOrOwnWebView() {
        val native=ShellNavigation().openPrepositions()
        assertEquals(ShellScreen.PREPOSITIONS,native.screen)
        assertEquals(BackAction.NATIVE_HOME,native.backAction)
        assertFalse(native.ownsWebSession)
        repeat(5) {assertEquals(native,native.openOptions().openOptions().closeOptions())}
        val restored=ShellNavigation.restore("OPTIONS","PREPOSITIONS","positions",null)
        assertEquals(native,restored.closeOptions());assertFalse(restored.ownsWebSession)
        assertEquals(ShellScreen.HOME,native.home().screen)
        val fallback=native.openActivity("positions")
        assertTrue(fallback.ownsWebSession);assertEquals(BackAction.LEGACY,fallback.backAction)
    }
}
