package com.bellfamily.bastischool

import org.junit.Assert.*
import org.junit.Test

class VocabularyNavigationTest {
    @Test fun nativeVocabularyReturnsFromOptionsWithoutOwningWebViewOrChangingOtherRoutes() {
        val route=ShellNavigation().openVocabulary()
        assertEquals(ShellScreen.VOCABULARY,route.screen);assertEquals(BackAction.NATIVE_HOME,route.backAction)
        repeat(5){assertEquals(route,route.openOptions().openOptions().closeOptions())}
        assertEquals(route,ShellNavigation.restore("OPTIONS","VOCABULARY","verbs",null).closeOptions())
        assertFalse(route.ownsWebSession);assertFalse(route.openOptions().ownsWebSession)
        assertEquals(ShellScreen.SEASONS,route.openSeasons().screen)
        assertEquals(ShellScreen.WILMA,route.openWilma().screen)
        assertEquals(ShellScreen.PREPOSITIONS,route.openPrepositions().screen)
        assertEquals(BackAction.LEGACY,route.openActivity("verbs").backAction)
    }
}
