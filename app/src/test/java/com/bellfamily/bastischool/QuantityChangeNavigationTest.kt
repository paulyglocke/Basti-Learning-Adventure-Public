package com.bellfamily.bastischool

import com.bellfamily.bastischool.learning.quantitychange.*
import com.bellfamily.bastischool.ui.home.*
import org.junit.Assert.*
import org.junit.Test

class QuantityChangeNavigationTest {
    @Test fun allFourConfigurationsAndOptionsRestoreExactlyAndReturnToChooser() {
        val chooser=homeActivity("quantityChange").homeDestination()
        assertEquals(ShellScreen.QUANTITY_CHANGE_CHOOSER,chooser.screen);assertEquals(BackAction.NATIVE_HOME,chooser.backAction)
        for(c in ChangeConfig.all) {
            val game=chooser.openQuantityChange(c)
            assertEquals(c,game.quantityConfig);assertEquals(BackAction.QUANTITY_CHANGE_CHOOSER,game.backAction)
            assertEquals(game,game.openOptions().closeOptions())
            for(route in listOf(game,game.openOptions()))assertEquals(route,ShellNavigation.restore(route.screen.name,route.optionsOrigin.name,route.mode,null,route.homePage,
                route.verbId,route.memoryMode,route.memoryPairs,route.patternLevel,route.quantityMode,route.quantityLimit))
            assertEquals(chooser,game.openQuantityChangeChooser())
        }
    }
    @Test fun invalidArgumentsReturnSafelyAndExistingRoutesNeedNoNewArguments() {
        for(mode in listOf(null,"unknown","MORE","", "x".repeat(1000)))for(limit in listOf(5,10)) {
            assertTrue(ShellNavigation.restore("QUANTITY_CHANGE_GAME",null,null,null,quantityMode=mode,quantityLimit=limit).recoveryFailed)
            assertTrue(ShellNavigation.restore("OPTIONS","QUANTITY_CHANGE_GAME",null,null,quantityMode=mode,quantityLimit=limit).recoveryFailed)
        }
        for(limit in listOf(-1,0,6,Int.MAX_VALUE))assertTrue(ShellNavigation.restore("QUANTITY_CHANGE_GAME",null,null,null,quantityMode="more",quantityLimit=limit).recoveryFailed)
        assertEquals(ShellNavigation(),ShellNavigation.restore("HOME",null,null,null))
        assertEquals(ShellScreen.PATTERN_CHOOSER,ShellNavigation.restore("PATTERN_CHOOSER",null,null,null).screen)
        assertEquals(ShellScreen.COUNTING_TRAY,ShellNavigation.restore("COUNTING_TRAY",null,null,null).screen)
    }
}
