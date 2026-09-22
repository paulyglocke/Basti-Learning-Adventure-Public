package com.bellfamily.bastischool.ui.wilma

import android.content.pm.ActivityInfo
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import com.bellfamily.bastischool.MainActivity
import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.session.*
import com.bellfamily.bastischool.learning.wilma.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class WilmaRouteTest {
    @get:Rule val compose=createEmptyComposeRule()
    @Test fun chooserExploreQuizOrderingAndSeasonsSurviveNativeNavigationAndRecreation() {
        val context=InstrumentationRegistry.getInstrumentation().targetContext
        context.getSharedPreferences("basti_shell",0).edit().putString("lang","en").putString("audioMode","off").putInt("round",5).commit()
        ActivityScenario.launch(MainActivity::class.java).use {scenario ->
            fun settled(){compose.waitUntil(10_000){var ready=false;scenario.onActivity {val vm=ViewModelProvider(it)[WilmaViewModel::class.java];ready=vm.selection!=null && !vm.busy && !vm.saveFailed && !vm.imageFailed};ready}}
            fun quiz():SessionState {var s:SessionState?=null;scenario.onActivity {s=ViewModelProvider(it)[WilmaViewModel::class.java].quiz};return s!!}
            fun order():WilmaOrderState {var s:WilmaOrderState?=null;scenario.onActivity {s=ViewModelProvider(it)[WilmaViewModel::class.java].ordering};return s!!}
            compose.onNodeWithText("Days & Seasons").performScrollTo().performClick()
            compose.onNodeWithTag("open-wilma").performClick();settled()
            compose.onNodeWithTag("wilma-phase-EXPLORE").performScrollTo().performClick();settled()
            compose.onNodeWithTag("day-day.sunday").performScrollTo().performClick();settled()
            scenario.recreate();settled();compose.onNodeWithTag("wilma-selected").assertTextEquals("Sunday")
            compose.onNodeWithText("⚙ Options").performClick();compose.onNodeWithText("🇩🇪 Deutsch").performClick()
            scenario.onActivity {it.onBackPressedDispatcher.onBackPressed()};settled()
            compose.onNodeWithTag("wilma-selected").assertTextEquals("Sonntag")
            compose.onNodeWithTag("wilma-phase-FIND").performScrollTo().performClick();settled()
            if(quiz().current.locked && quiz().phase!=SessionPhase.COMPLETED){compose.onNodeWithTag("wilma-next").performScrollTo().performClick();settled()}
            if(quiz().phase==SessionPhase.COMPLETED){compose.onNodeWithTag("wilma-again").performScrollTo().performClick();settled()}
            if(quiz().current.answer==AnswerState.RETRY_AVAILABLE){compose.onNodeWithTag("wilma-retry").performScrollTo().performClick();settled()}
            val unanswered=SessionCheckpoint.encode(quiz());scenario.recreate();settled();assertArrayEquals(unanswered,SessionCheckpoint.encode(quiz()))
            compose.onNodeWithTag("day-${quiz().task.question.correct.value}").performScrollTo().performClick();settled()
            val answered=SessionCheckpoint.encode(quiz())
            compose.onNodeWithText("⚙ Optionen").performClick();compose.onNodeWithText("←").performClick();settled()
            assertArrayEquals(answered,SessionCheckpoint.encode(quiz()))
            compose.onNodeWithTag("wilma-phase-ORDER").performScrollTo().performClick();settled()
            if(order().completed){compose.onNodeWithTag("wilma-again").performScrollTo().performClick();settled()}
            if(order().current.answer==AnswerState.RETRY_AVAILABLE){compose.onNodeWithTag("wilma-retry").performScrollTo().performClick();settled()}
            while(order().index<3){compose.onNodeWithTag("order-${WilmaContent.days[order().index].value}").performScrollTo().performClick();settled()}
            val saved=order();scenario.recreate();settled()
            scenario.moveToState(Lifecycle.State.CREATED);scenario.moveToState(Lifecycle.State.RESUMED);settled()
            for(orientation in listOf(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE,ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE,ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)) {
                scenario.onActivity {it.requestedOrientation=orientation};settled()
                compose.onNodeWithTag("wilma-replay").performScrollTo().assertIsDisplayed()
                assertEquals(saved.choices,order().choices);assertEquals(saved.steps,order().steps);assertEquals(saved.id,order().id)
            }
            scenario.onActivity {activity ->
                fun web(view:View):Boolean=view is WebView || (view is ViewGroup && (0 until view.childCount).any {web(view.getChildAt(it))})
                assertFalse(web(activity.window.decorView));activity.onBackPressedDispatcher.onBackPressed()
            }
            compose.onNodeWithTag("open-wilma").performClick();settled();assertEquals(saved.steps,order().steps)
            scenario.onActivity {it.onBackPressedDispatcher.onBackPressed()}
            compose.onNodeWithTag("legacy-calendar").assertExists()
            compose.onNodeWithTag("open-seasons").performClick()
            compose.onNodeWithTag("learn").assertExists()
        }
    }
}
