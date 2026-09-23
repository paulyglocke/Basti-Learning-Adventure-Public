package com.bellfamily.bastischool.ui.vocabulary

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
import com.bellfamily.bastischool.learning.models.ContentId
import com.bellfamily.bastischool.learning.models.ContentLanguage
import com.bellfamily.bastischool.learning.session.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class VocabularyRouteTest {
    @get:Rule val compose = createEmptyComposeRule()
    @Test fun nativeExploreAndQuizSurviveOptionsLanguageRecreationAndHomeReturn() {
        val context=InstrumentationRegistry.getInstrumentation().targetContext
        context.getSharedPreferences("basti_shell",0).edit().putString("lang","en").putString("audioMode","off").putInt("round",5).commit()
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            fun current():SessionState? {var result:SessionState?=null;scenario.onActivity {result=ViewModelProvider(it)[VocabularyViewModel::class.java].quiz};return result}
            fun settled() {compose.waitUntil(10_000) {var ready=false;scenario.onActivity {val vm=ViewModelProvider(it)[VocabularyViewModel::class.java];ready=vm.selection!=null && !vm.busy && !vm.saveFailed};ready}}
            compose.onNodeWithText("Vocabulary Booster").performScrollTo().performClick()
            settled()
            compose.onNodeWithTag("vocabulary-explore").performScrollTo().performClick();settled()
            compose.onNodeWithTag("word-animal.horse").performScrollTo().performClick();settled()
            compose.onNodeWithTag("vocabulary-word").assertTextEquals("Horse")
            scenario.recreate();settled()
            compose.onNodeWithTag("vocabulary-word").assertTextEquals("Horse")
            compose.onNodeWithText("⚙ Options").performClick()
            compose.onNodeWithText("🇩🇪 Deutsch").performClick()
            scenario.onActivity {it.onBackPressedDispatcher.onBackPressed()};settled()
            compose.onNodeWithTag("vocabulary-word").assertTextEquals("Pferd")
            scenario.onActivity {assertEquals(ContentId("animal.horse"),ViewModelProvider(it)[VocabularyViewModel::class.java].selection!!.selected)}
            compose.onNodeWithTag("vocabulary-find").performScrollTo().performClick();settled()
            if(current()!!.phase==SessionPhase.COMPLETED) {compose.onNodeWithTag("vocabulary-again").performScrollTo().performClick();settled()}
            val unanswered=SessionCheckpoint.encode(current()!!)
            scenario.recreate();settled();assertArrayEquals(unanswered,SessionCheckpoint.encode(current()!!))
            if(!current()!!.current.locked) {
                if(current()!!.current.answer==AnswerState.RETRY_AVAILABLE) {compose.onNodeWithTag("vocabulary-retry").performScrollTo().performClick();settled()}
                compose.onNodeWithTag("answer-${current()!!.task.question.correct.value}").performScrollTo().performClick();settled()
            }
            val saved=SessionCheckpoint.encode(current()!!)
            compose.onNodeWithTag("vocabulary-name").performScrollTo().performClick();settled()
            compose.onNodeWithTag("vocabulary-find").performScrollTo().performClick();settled()
            assertArrayEquals(saved,SessionCheckpoint.encode(current()!!))
            scenario.moveToState(androidx.lifecycle.Lifecycle.State.CREATED)
            scenario.moveToState(androidx.lifecycle.Lifecycle.State.RESUMED);settled()
            compose.onNodeWithText("⚙ Optionen").performClick()
            compose.onNodeWithText("←").performClick();settled()
            assertArrayEquals(saved,SessionCheckpoint.encode(current()!!))
            scenario.recreate();settled()
            for(orientation in listOf(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE,ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE,ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)) {
                scenario.onActivity {it.requestedOrientation=orientation};settled()
                compose.onNodeWithTag("vocabulary-replay").performScrollTo().assertIsDisplayed()
                assertArrayEquals(saved,SessionCheckpoint.encode(current()!!))
            }
            scenario.onActivity {activity ->
                fun web(view:View):Boolean=view is WebView || (view is ViewGroup && (0 until view.childCount).any {web(view.getChildAt(it))})
                assertFalse(web(activity.window.decorView))
                activity.onBackPressedDispatcher.onBackPressed()
            }
            compose.onNodeWithText("Wortschatz").performScrollTo().performClick();settled()
            assertArrayEquals(saved,SessionCheckpoint.encode(current()!!))
            assertEquals(ContentLanguage.GERMAN,current()!!.language)
        }
    }
}
