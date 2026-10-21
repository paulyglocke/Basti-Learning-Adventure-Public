package com.bellfamily.bastischool

import org.junit.Assert.*
import org.junit.Test

class VocabularyNavigationTest {
    @Test fun nativeVocabularyReturnsFromOptionsWithoutOwningWebViewOrChangingOtherRoutes() {
        val route=ShellNavigation().openVocabulary()
        assertEquals(ShellScreen.VOCABULARY_CHOOSER,route.screen);assertEquals(BackAction.NATIVE_HOME,route.backAction)
        repeat(5){assertEquals(route,route.openOptions().openOptions().closeOptions())}
        val activity=route.openVocabularyActivity()
        assertEquals(ShellScreen.VOCABULARY,activity.screen);assertEquals(BackAction.VOCABULARY_CHOOSER,activity.backAction)
        assertEquals(activity,activity.openOptions().closeOptions())
        assertEquals(activity,ShellNavigation.restore("OPTIONS","VOCABULARY","verbs",null).closeOptions())
        assertEquals(BackAction.NATIVE_HOME,ShellNavigation.restore("VOCABULARY_CHOOSER",null,"verbs",null).backAction)
        assertFalse(route.ownsWebSession);assertFalse(route.openOptions().ownsWebSession)
        assertEquals(ShellScreen.SEASONS_CHOOSER,route.openSeasons().screen)
        assertEquals(ShellScreen.WILMA_CHOOSER,route.openWilma().screen)
        assertEquals(ShellScreen.PREPOSITIONS,route.openPrepositions().screen)
        assertEquals(BackAction.LEGACY,route.openActivity("verbs").backAction)
    }
}
