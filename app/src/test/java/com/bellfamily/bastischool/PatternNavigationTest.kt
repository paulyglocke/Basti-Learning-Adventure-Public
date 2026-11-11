package com.bellfamily.bastischool

import com.bellfamily.bastischool.learning.patterns.*
import com.bellfamily.bastischool.ui.home.*
import org.junit.Assert.*
import org.junit.Test

class PatternNavigationTest {
    @Test fun everyLevelAndItsOptionsOverlayRestoreExactlyAndReturnToTheChooser() {
        val chooser=homeActivity("patternBuilder").homeDestination()
        assertEquals(ShellScreen.PATTERN_CHOOSER,chooser.screen);assertEquals(BackAction.NATIVE_HOME,chooser.backAction)
        for(level in PatternLevel.entries) {
            val game=chooser.openPatternBuilder(level)
            assertEquals(level,game.patternConfig);assertEquals(BackAction.PATTERN_CHOOSER,game.backAction)
            assertEquals(game,game.openOptions().closeOptions())
            for(route in listOf(game,game.openOptions())) {
                assertEquals(route,ShellNavigation.restore(route.screen.name,route.optionsOrigin.name,route.mode,null,route.homePage,
                    route.verbId,route.memoryMode,route.memoryPairs,route.patternLevel))
            }
            assertEquals(chooser,game.openPatternChooser())
        }
    }
    @Test fun invalidLevelRoutesFallBackSafelyAndOlderDestinationsNeedNoPatternArguments() {
        for(level in listOf(null,"unknown","AB","", "x".repeat(1000))) {
            assertTrue(ShellNavigation.restore("PATTERN_GAME",null,null,null,patternLevel=level).recoveryFailed)
            assertTrue(ShellNavigation.restore("OPTIONS","PATTERN_GAME",null,null,patternLevel=level).recoveryFailed)
        }
        assertEquals(ShellNavigation(),ShellNavigation.restore("HOME",null,null,null))
        assertEquals(ShellScreen.MEMORY_CHOOSER,ShellNavigation.restore("MEMORY_CHOOSER",null,null,null).screen)
        assertEquals(ShellScreen.BUILD_TEN,ShellNavigation.restore("BUILD_TEN",null,null,null).screen)
    }
}
