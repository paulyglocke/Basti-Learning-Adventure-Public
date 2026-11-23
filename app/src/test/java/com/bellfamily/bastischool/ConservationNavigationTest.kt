package com.bellfamily.bastischool

import com.bellfamily.bastischool.learning.conservation.*
import com.bellfamily.bastischool.ui.home.*
import org.junit.Assert.*
import org.junit.Test

class ConservationNavigationTest {
    @Test fun bothRangesAndOptionsRestoreAndReturnToChooser() {
        val chooser=homeActivity("conservation").homeDestination()
        assertEquals(ShellScreen.CONSERVATION_CHOOSER,chooser.screen);assertEquals(BackAction.NATIVE_HOME,chooser.backAction)
        for(c in ConservationRange.entries) {
            val game=chooser.openConservation(c)
            assertEquals(c,game.conservationConfig);assertEquals(BackAction.CONSERVATION_CHOOSER,game.backAction)
            assertEquals(game,game.openOptions().closeOptions())
            for(route in listOf(game,game.openOptions()))assertEquals(route,ShellNavigation.restore(route.screen.name,route.optionsOrigin.name,route.mode,null,conservationLimit=route.conservationLimit))
            assertEquals(chooser,game.openConservationChooser())
        }
    }
    @Test fun invalidRangesRecoverSafelyAndOldRoutesNeedNoNewArguments() {
        for(limit in listOf(-1,0,6,Int.MAX_VALUE))for(screen in listOf("CONSERVATION_GAME","OPTIONS"))
            assertTrue(ShellNavigation.restore(screen,"CONSERVATION_GAME",null,null,conservationLimit=limit).recoveryFailed)
        assertEquals(ShellNavigation(),ShellNavigation.restore("HOME",null,null,null))
        assertEquals(ShellScreen.NUMBER_STORIES_CHOOSER,ShellNavigation.restore("NUMBER_STORIES_CHOOSER",null,null,null).screen)
    }
}
