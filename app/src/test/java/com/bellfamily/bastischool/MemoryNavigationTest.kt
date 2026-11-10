package com.bellfamily.bastischool

import com.bellfamily.bastischool.learning.memory.*
import com.bellfamily.bastischool.ui.home.*
import org.junit.Assert.*
import org.junit.Test

class MemoryNavigationTest {
    @Test fun eachBoardAndItsOptionsOverlayRestoreTheExactConfigAndReturnThroughTheChooser() {
        val chooser=homeActivity("memoryPairs").homeDestination()
        assertEquals(ShellScreen.MEMORY_CHOOSER,chooser.screen);assertEquals(BackAction.NATIVE_HOME,chooser.backAction)
        for(mode in MemoryMode.entries)for(pairs in 2..4) {
            val config=MemoryConfig(mode,pairs);val game=chooser.openMemory(config)
            assertEquals(config,game.memoryConfig);assertEquals(BackAction.MEMORY_CHOOSER,game.backAction)
            for(route in listOf(game,game.openOptions())) {
                val restored=ShellNavigation.restore(route.screen.name,route.optionsOrigin.name,route.mode,null,route.homePage,route.verbId,route.memoryMode,route.memoryPairs)
                assertEquals(route,restored)
            }
            assertEquals(game,game.openOptions().openOptions().closeOptions())
            assertEquals(chooser,game.openMemoryChooser())
        }
    }
    @Test fun malformedBoardRoutesFallBackSafelyAndOldRoutesNeedNoMemoryArguments() {
        for(mode in listOf(null,"bad","animals"))for(size in listOf(0,1,5,8)) {
            assertTrue(ShellNavigation.restore("MEMORY_GAME",null,null,null,memoryMode=mode,memoryPairs=size).recoveryFailed)
            assertTrue(ShellNavigation.restore("OPTIONS","MEMORY_GAME",null,null,memoryMode=mode,memoryPairs=size).recoveryFailed)
        }
        assertEquals(ShellNavigation(),ShellNavigation.restore("HOME",null,null,null))
        assertEquals(ShellScreen.BUILD_FIVE,ShellNavigation.restore("BUILD_FIVE",null,null,null).screen)
    }
}
