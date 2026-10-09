package com.bellfamily.bastischool.ui.clock

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.lifecycle.ViewModelProvider
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import com.bellfamily.bastischool.MainActivity
import com.bellfamily.bastischool.learning.clock.*
import com.bellfamily.bastischool.learning.models.ContentLanguage
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class ClockRouteTest {
    @get:Rule val compose=createEmptyComposeRule()
    @Test fun homeExplorePracticeOptionsAndRecreationKeepTargetAndClock() {
        val context=InstrumentationRegistry.getInstrumentation().targetContext
        context.getSharedPreferences("basti_shell",0).edit().putString("lang","en").putString("audioMode","off").commit()
        ActivityScenario.launch(MainActivity::class.java).use {scenario ->
            fun settled() {compose.waitUntil(10_000){var ready=false;scenario.onActivity {
                val vm=ViewModelProvider(it)[ClockViewModel::class.java]
                ready=vm.practice!=null && !vm.practiceBusy && !vm.practiceFailed
            };ready}}
            fun state():ClockPractice.State {var s:ClockPractice.State?=null;scenario.onActivity {s=ViewModelProvider(it)[ClockViewModel::class.java].practice};return s!!}
            compose.onNodeWithText("Clock & Time").performScrollTo().performClick()
            compose.onNodeWithTag("clock-open-make").performScrollTo().performClick();settled()
            if(state().completed) {compose.onNodeWithTag("clock-make-again").performClick();settled()}
            if(state().current.solved) {compose.onNodeWithTag("clock-make-next").performScrollTo().performClick();settled()}
            if(state().completed) {compose.onNodeWithTag("clock-make-again").performClick();settled()}
            val title=compose.onNodeWithText("Make This Time").performScrollTo().fetchSemanticsNode().boundsInRoot
            val options=compose.onNodeWithText("⚙ Options").fetchSemanticsNode().boundsInRoot
            assertTrue(title.top >= options.bottom)
            scenario.onActivity {val vm=ViewModelProvider(it)[ClockViewModel::class.java];vm.practiceMove(vm.practice!!.target.advance(30))}
            compose.onNodeWithTag("clock-make-check").performScrollTo().performClick();settled()
            val saved=state();scenario.recreate();settled();assertEquals(saved,state())
            compose.onNodeWithText("⚙ Options").performClick()
            compose.onNodeWithText("🇩🇪 Deutsch").performClick()
            scenario.onActivity {it.onBackPressedDispatcher.onBackPressed()};settled()
            assertEquals(saved.copy(language=ContentLanguage.GERMAN),state())
            compose.onNodeWithTag("clock-target").performScrollTo().assertTextEquals(ClockPractice.prompt(saved.target).display.de)
            scenario.onActivity {it.onBackPressedDispatcher.onBackPressed()}
            compose.onNodeWithTag("clock-open-make").performScrollTo().performClick();settled()
            assertEquals(saved.copy(language=ContentLanguage.GERMAN),state())
        }
    }
}
