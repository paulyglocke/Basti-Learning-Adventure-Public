package com.bellfamily.bastischool.ui.seasons

import android.content.pm.ActivityInfo
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.lifecycle.ViewModelProvider
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import com.bellfamily.bastischool.MainActivity
import com.bellfamily.bastischool.learning.content.SeasonIds
import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.seasons.*
import com.bellfamily.bastischool.learning.session.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class SeasonsProgressionRouteTest {
    @get:Rule val compose=createEmptyComposeRule()
    @Test fun newModesKeepExactStateAcrossOptionsLanguageRecreationAndCompleteOrdering() {
        val context=InstrumentationRegistry.getInstrumentation().targetContext
        context.getSharedPreferences("basti_shell",0).edit().putString("lang","en").putString("audioMode","off").putInt("round",5).commit()
        ActivityScenario.launch(MainActivity::class.java).use {scenario ->
            fun vm():SeasonsViewModel {lateinit var result:SeasonsViewModel;scenario.onActivity {result=ViewModelProvider(it)[SeasonsViewModel::class.java]};return result}
            fun ready() {compose.waitUntil(10_000){val v=vm();v.selection!=null && !v.busy && !v.saveFailed && !v.imageFailed}}
            compose.onNodeWithText("Days & Seasons").performScrollTo().performClick()
            compose.onNodeWithTag("open-seasons").performScrollTo().performClick();ready()
            for(mode in listOf(SeasonsPhase.NEXT,SeasonsPhase.BEFORE)) {
                compose.onNodeWithTag(mode.tag).performScrollTo().performClick();ready()
                if(vm().state!!.phase==SessionPhase.COMPLETED){compose.onNodeWithTag("seasons-again").performClick();ready()}
                val unanswered=SessionCheckpoint.encode(vm().state!!)
                scenario.recreate();ready();assertArrayEquals(unanswered,SessionCheckpoint.encode(vm().state!!))
                if(vm().state!!.current.answer==AnswerState.RETRY_AVAILABLE){compose.onNodeWithTag("seasons-retry").performScrollTo().performClick();ready()}
                if(!vm().state!!.current.locked){compose.onNodeWithTag("answer-${vm().state!!.task.question.correct.value}").performScrollTo().performClick();ready()}
                val answered=SessionCheckpoint.encode(vm().state!!)
                compose.onNodeWithText("⚙ Options").performClick()
                scenario.onActivity {it.onBackPressedDispatcher.onBackPressed()};ready()
                assertArrayEquals(answered,SessionCheckpoint.encode(vm().state!!))
            }
            compose.onNodeWithTag(SeasonsPhase.ORDER.tag).performScrollTo().performClick();ready()
            if(vm().ordering!!.completed){compose.onNodeWithTag("seasons-again").performClick();ready()}
            if(vm().ordering!!.current.answer==AnswerState.RETRY_AVAILABLE){compose.onNodeWithTag("seasons-retry").performScrollTo().performClick();ready()}
            while(vm().ordering!!.index<2) {
                compose.onNodeWithTag("season-order-${SeasonIds.canonicalOrder[vm().ordering!!.index].value}").performScrollTo().performClick();ready()
            }
            val before=vm().ordering!!
            scenario.recreate();ready()
            assertEquals(before.id,vm().ordering!!.id);assertEquals(before.choices,vm().ordering!!.choices);assertEquals(before.steps,vm().ordering!!.steps)
            compose.onNodeWithText("⚙ Options").performClick();compose.onNodeWithText("🇩🇪 Deutsch").performClick()
            scenario.onActivity {it.onBackPressedDispatcher.onBackPressed()};ready()
            assertEquals(ContentLanguage.GERMAN,vm().ordering!!.language)
            assertEquals(before.id,vm().ordering!!.id);assertEquals(before.steps,vm().ordering!!.steps)
            while(!vm().ordering!!.completed) {
                compose.onNodeWithTag("season-order-${SeasonIds.canonicalOrder[vm().ordering!!.index].value}").performScrollTo().performClick();ready()
            }
            assertTrue(vm().ordering!!.acknowledged)
            val done=vm().ordering!!
            compose.onNodeWithTag("balloon-0").performClick()
            scenario.recreate();ready()
            assertEquals(done.id,vm().ordering!!.id);assertEquals(done.steps,vm().ordering!!.steps);assertTrue(vm().ordering!!.acknowledged)
            for(orientation in listOf(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE,ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE,ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)) {
                scenario.onActivity {it.requestedOrientation=orientation};ready()
                compose.onNodeWithTag("seasons-home").assertIsDisplayed()
                compose.onNodeWithTag("seasons-again").assertIsDisplayed()
                compose.onNodeWithTag("seasons-replay").assertIsDisplayed()
                compose.onNodeWithTag("completion-celebration").assertExists()
            }
            compose.onNodeWithTag("learn").performScrollTo().performClick();ready()
            compose.onNodeWithTag("completion-celebration").assertDoesNotExist()
        }
    }
}
