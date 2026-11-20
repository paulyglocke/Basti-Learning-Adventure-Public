package com.bellfamily.bastischool

import com.bellfamily.bastischool.learning.numberstories.*
import com.bellfamily.bastischool.ui.home.*
import org.junit.Assert.*
import org.junit.Test

class NumberStoriesNavigationTest {
    @Test fun allFourConfigurationsAndOptionsRestoreExactlyAndReturnToChooser() {
        val chooser=homeActivity("numberStories").homeDestination()
        assertEquals(ShellScreen.NUMBER_STORIES_CHOOSER,chooser.screen);assertEquals(BackAction.NATIVE_HOME,chooser.backAction)
        for(c in StoryConfig.all) {
            val game=chooser.openNumberStories(c)
            assertEquals(c,game.storyConfig);assertEquals(BackAction.NUMBER_STORIES_CHOOSER,game.backAction)
            assertEquals(game,game.openOptions().closeOptions())
            for(route in listOf(game,game.openOptions()))assertEquals(route,ShellNavigation.restore(route.screen.name,route.optionsOrigin.name,route.mode,null,route.homePage,
                route.verbId,route.memoryMode,route.memoryPairs,route.patternLevel,storyMode=route.storyMode,storyLimit=route.storyLimit))
            assertEquals(chooser,game.openNumberStoriesChooser())
        }
    }
    @Test fun invalidArgumentsReturnSafelyAndExistingRoutesNeedNoNewArguments() {
        for(mode in listOf(null,"unknown","MORE","", "x".repeat(1000)))for(limit in listOf(5,10)) {
            assertTrue(ShellNavigation.restore("NUMBER_STORIES_GAME",null,null,null,storyMode=mode,storyLimit=limit).recoveryFailed)
            assertTrue(ShellNavigation.restore("OPTIONS","NUMBER_STORIES_GAME",null,null,storyMode=mode,storyLimit=limit).recoveryFailed)
        }
        for(limit in listOf(-1,0,6,Int.MAX_VALUE))assertTrue(ShellNavigation.restore("NUMBER_STORIES_GAME",null,null,null,storyMode="add",storyLimit=limit).recoveryFailed)
        assertEquals(ShellNavigation(),ShellNavigation.restore("HOME",null,null,null))
        assertEquals(ShellScreen.PATTERN_CHOOSER,ShellNavigation.restore("PATTERN_CHOOSER",null,null,null).screen)
        assertEquals(ShellScreen.COUNTING_TRAY,ShellNavigation.restore("COUNTING_TRAY",null,null,null).screen)
    }
}
