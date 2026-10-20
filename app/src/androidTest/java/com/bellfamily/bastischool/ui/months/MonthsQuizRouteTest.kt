package com.bellfamily.bastischool.ui.months

import com.bellfamily.bastischool.openHomeActivity

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.lifecycle.ViewModelProvider
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import com.bellfamily.bastischool.MainActivity
import com.bellfamily.bastischool.learning.months.*
import com.bellfamily.bastischool.learning.session.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class MonthsQuizRouteTest {
    @get:Rule val compose=createEmptyComposeRule()
    @Test fun optionsRecreationAssistedWheelAndIndependentModeJournals() {
        val prefs=InstrumentationRegistry.getInstrumentation().targetContext.getSharedPreferences("basti_shell",0)
        val oldLang=prefs.getString("lang",null);val oldSound=prefs.getString("audioMode",null)
        prefs.edit().putString("lang","en").putString("audioMode","off").commit()
        try {ActivityScenario.launch(MainActivity::class.java).use {scenario ->
            fun settled(){compose.waitUntil(10_000){var done=false;scenario.onActivity{val vm=ViewModelProvider(it)[MonthsViewModel::class.java];done=vm.quiz!=null && !vm.quizBusy};done}}
            fun state():SessionState {var s:SessionState?=null;scenario.onActivity{s=ViewModelProvider(it)[MonthsViewModel::class.java].quiz};return s!!}
            fun click(tag:String)=compose.onNodeWithTag(tag).performScrollTo().performClick()
            compose.openHomeActivity("Days & Seasons");click("open-months")
            click("months-open-find");settled()
            if(state().phase==SessionPhase.COMPLETED){compose.onNodeWithTag("months-quiz-again").performClick();settled()}
            if(state().current.locked){click("months-quiz-next");settled()}
            if(state().phase==SessionPhase.COMPLETED){compose.onNodeWithTag("months-quiz-again").performClick();settled()}
            click("months-quiz-help");settled()
            val before=SessionCheckpoint.encode(state())
            scenario.recreate();settled();assertArrayEquals(before,SessionCheckpoint.encode(state()))
            compose.onNodeWithText("⚙ Options").performClick();compose.onNodeWithText("🇩🇪 Deutsch").performClick()
            scenario.onActivity{it.onBackPressedDispatcher.onBackPressed()};settled()
            val find=state();assertEquals("GERMAN",find.language.name)
            click("months-quiz-explore")
            compose.waitUntil(10_000){compose.onAllNodesWithTag("months-wheel").fetchSemanticsNodes().isNotEmpty()}
            scenario.onActivity{it.onBackPressedDispatcher.onBackPressed()}
            compose.waitUntil(10_000){compose.onAllNodesWithTag("months-open-relations").fetchSemanticsNodes().isNotEmpty()}
            click("months-open-relations");settled()
            assertEquals(MonthsQuestions.activity(MonthsMode.RELATIONS),state().plan.activity)
            scenario.onActivity{it.onBackPressedDispatcher.onBackPressed()}
            compose.waitUntil(10_000){compose.onAllNodesWithTag("months-open-find").fetchSemanticsNodes().isNotEmpty()}
            click("months-open-missing");settled()
            assertEquals(MonthsQuestions.activity(MonthsMode.MISSING),state().plan.activity)
            val missing=SessionCheckpoint.encode(state())
            scenario.recreate();settled();assertArrayEquals(missing,SessionCheckpoint.encode(state()))
            scenario.onActivity{it.onBackPressedDispatcher.onBackPressed()}
            compose.waitUntil(10_000){compose.onAllNodesWithTag("months-open-find").fetchSemanticsNodes().isNotEmpty()}
            click("months-open-find");settled()
            assertArrayEquals(SessionCheckpoint.encode(find),SessionCheckpoint.encode(state()))
            compose.onNodeWithTag("months-wheel").assertDoesNotExist()
            click("months-quiz-home")
        }} finally {prefs.edit().putString("lang",oldLang).putString("audioMode",oldSound).commit()}
    }
}
