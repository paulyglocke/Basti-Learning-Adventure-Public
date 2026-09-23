package com.bellfamily.bastischool.ui.prepositions

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
import com.bellfamily.bastischool.learning.session.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class PrepositionsRouteTest {
    @get:Rule val compose = createEmptyComposeRule()
    @Test fun realRouteOptionsRecreationAndHomePreserveNativeSessionWithoutWebView() {
        val context=InstrumentationRegistry.getInstrumentation().targetContext
        val prefs=context.getSharedPreferences("basti_shell",0)
        prefs.edit().putString("lang","en").putString("audioMode","off").putInt("round",5).commit()
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            fun current():SessionState? {var result:SessionState?=null;scenario.onActivity {result=ViewModelProvider(it)[PrepositionsViewModel::class.java].state};return result}
            fun settled() {compose.waitUntil(10_000) { var ready=false;scenario.onActivity {val vm=ViewModelProvider(it)[PrepositionsViewModel::class.java];ready=vm.state!=null && !vm.busy};ready }}
            compose.onNodeWithText("Prepositions").performScrollTo().performClick();settled()
            if(current()!!.phase==SessionPhase.COMPLETED) {compose.onNodeWithTag("again").assertIsDisplayed().performClick();settled()}
            if(!current()!!.current.locked) {
                if(current()!!.current.answer==AnswerState.RETRY_AVAILABLE) {compose.onNodeWithTag("retry").performScrollTo().performClick();settled()}
                compose.onNodeWithTag("answer-${current()!!.task.question.correct.value}").performScrollTo().performClick();settled()
            }
            val saved=SessionCheckpoint.encode(current()!!)
            compose.onNodeWithText("⚙ Options").performClick()
            compose.onNodeWithText("←").performClick();settled()
            assertArrayEquals(saved,SessionCheckpoint.encode(current()!!))
            compose.onNodeWithText("⚙ Options").performClick()
            scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() };settled()
            assertArrayEquals(saved,SessionCheckpoint.encode(current()!!))
            scenario.recreate();settled()
            for (orientation in listOf(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE, ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)) {
                scenario.onActivity { it.requestedOrientation=orientation }
                settled()
                compose.onNodeWithTag("replay").performScrollTo().assertIsDisplayed()
                assertArrayEquals(saved,SessionCheckpoint.encode(current()!!))
            }
            assertArrayEquals(saved,SessionCheckpoint.encode(current()!!))
            scenario.onActivity { activity ->
                fun web(view:View):Boolean = view is WebView || (view is ViewGroup && (0 until view.childCount).any {web(view.getChildAt(it))})
                assertFalse(web(activity.window.decorView))
            }
            compose.onNodeWithTag("home").performScrollTo().performClick()
            compose.onNodeWithText("Prepositions").performScrollTo().performClick();settled()
            assertArrayEquals(saved,SessionCheckpoint.encode(current()!!))
            val task=current()!!.task;val score=current()!!.score
            compose.onNodeWithText("⚙ Options").performClick()
            compose.onNodeWithText("🇩🇪 Deutsch").performClick()
            scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() };settled()
            assertEquals(task.id,current()!!.task.id)
            assertEquals(task.question.choices,current()!!.task.question.choices)
            assertEquals(score,current()!!.score)
            assertEquals(com.bellfamily.bastischool.learning.models.ContentLanguage.GERMAN,current()!!.language)
        }
    }
}
