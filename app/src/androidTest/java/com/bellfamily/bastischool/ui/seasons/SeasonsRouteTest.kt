package com.bellfamily.bastischool.ui.seasons

import android.content.pm.ActivityInfo
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.lifecycle.ViewModelProvider
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import com.bellfamily.bastischool.MainActivity
import com.bellfamily.bastischool.learning.content.SeasonIds
import com.bellfamily.bastischool.learning.models.ContentLanguage
import com.bellfamily.bastischool.learning.session.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class SeasonsRouteTest {
    @get:Rule val compose = createEmptyComposeRule()
    @Test fun nativeExploreAndQuizSurviveOptionsLanguageRecreationAndHubReturn() {
        val context=InstrumentationRegistry.getInstrumentation().targetContext
        context.getSharedPreferences("basti_shell",0).edit().putString("lang","en").putString("audioMode","off").putInt("round",5).commit()
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            fun current():SessionState? {var result:SessionState?=null;scenario.onActivity {result=ViewModelProvider(it)[SeasonsViewModel::class.java].state};return result}
            fun settled() {compose.waitUntil(10_000) {var ready=false;scenario.onActivity {val vm=ViewModelProvider(it)[SeasonsViewModel::class.java];ready=vm.selection!=null && !vm.busy && !vm.saveFailed && vm.artwork!=null};ready}}
            compose.onNodeWithText("Days & Seasons").performScrollTo().performClick()
            compose.onNodeWithTag("open-seasons").performScrollTo().performClick();settled()
            compose.onNodeWithTag("learn").performScrollTo().performClick();settled()
            compose.onNodeWithTag("season-season.autumn").performScrollTo().performClick();settled()
            compose.onNodeWithTag("season-name").assertTextEquals("Autumn")
            scenario.recreate();settled()
            compose.onNodeWithTag("season-name").assertTextEquals("Autumn")
            compose.onNodeWithText("⚙ Options").performClick()
            compose.onNodeWithText("🇩🇪 Deutsch").performClick()
            scenario.onActivity {it.onBackPressedDispatcher.onBackPressed()};settled()
            compose.onNodeWithTag("season-name").assertTextEquals("Herbst")
            scenario.onActivity {assertEquals(SeasonIds.AUTUMN,ViewModelProvider(it)[SeasonsViewModel::class.java].selection!!.selected)}
            compose.onNodeWithTag("practice").performScrollTo().performClick();settled()
            if(current()!!.phase==SessionPhase.COMPLETED) {compose.onNodeWithTag("seasons-again").performScrollTo().performClick();settled()}
            val unanswered=SessionCheckpoint.encode(current()!!)
            scenario.recreate();settled();assertArrayEquals(unanswered,SessionCheckpoint.encode(current()!!))
            if(!current()!!.current.locked) {
                if(current()!!.current.answer==AnswerState.RETRY_AVAILABLE) {compose.onNodeWithTag("seasons-retry").performScrollTo().performClick();settled()}
                compose.onNodeWithTag("answer-${current()!!.task.question.correct.value}").performScrollTo().performClick();settled()
            }
            val saved=SessionCheckpoint.encode(current()!!)
            compose.onNodeWithText("⚙ Optionen").performClick()
            compose.onNodeWithText("←").performClick();settled()
            assertArrayEquals(saved,SessionCheckpoint.encode(current()!!))
            scenario.recreate();settled()
            for(orientation in listOf(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE,ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE,ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)) {
                scenario.onActivity {it.requestedOrientation=orientation};settled()
                compose.onNodeWithTag("seasons-replay").performScrollTo().assertIsDisplayed()
                assertArrayEquals(saved,SessionCheckpoint.encode(current()!!))
            }
            scenario.onActivity {activity ->
                fun web(view:View):Boolean=view is WebView || (view is ViewGroup && (0 until view.childCount).any {web(view.getChildAt(it))})
                assertFalse(web(activity.window.decorView))
                activity.onBackPressedDispatcher.onBackPressed()
            }
            compose.onNodeWithTag("legacy-calendar").assertExists()
            compose.onNodeWithTag("open-seasons").performClick();settled()
            assertArrayEquals(saved,SessionCheckpoint.encode(current()!!))
            assertEquals(ContentLanguage.GERMAN,current()!!.language)
        }
    }
}
