package com.bellfamily.bastischool.ui.vocabulary

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.lifecycle.ViewModelProvider
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import com.bellfamily.bastischool.MainActivity
import com.bellfamily.bastischool.openHomeActivity
import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.session.*
import com.bellfamily.bastischool.learning.vocabulary.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class FoodVocabularyRouteTest {
    @get:Rule val compose=createEmptyComposeRule()

    @Test fun foodExploreFindNameLanguageAndCategorySwitchPreserveSavedPractice() {
        val prefs=InstrumentationRegistry.getInstrumentation().targetContext.getSharedPreferences("basti_shell",0)
        val language=prefs.getString("lang",null);val audio=prefs.getString("audioMode",null);val round=prefs.getInt("round",5)
        try {
            prefs.edit().putString("lang","en").putString("audioMode","off").putInt("round",5).commit()
            ActivityScenario.launch(MainActivity::class.java).use { scenario ->
                fun settled() {compose.waitUntil(20_000) {
                    var ready=false
                    scenario.onActivity {val vm=ViewModelProvider(it)[VocabularyViewModel::class.java];ready=vm.selection!=null && !vm.busy && !vm.saveFailed}
                    ready
                }}
                fun state():SessionState {var value:SessionState?=null;scenario.onActivity {value=ViewModelProvider(it)[VocabularyViewModel::class.java].quiz};return value!!}
                fun click(tag:String) {compose.onNodeWithTag(tag).performScrollTo().performClick();settled()}
                compose.openHomeActivity("Vocabulary Booster");settled()
                click("vocabulary-category-food");click("vocabulary-open-EXPLORE")
                compose.waitUntil(20_000) {
                    var loaded=false
                    scenario.onActivity {loaded=VocabularyContent.foodPack.items.all {item->item.id in ViewModelProvider(it)[VocabularyViewModel::class.java].images}}
                    loaded
                }
                for(item in VocabularyContent.foodPack.items) {
                    click("word-${item.id.value}")
                    compose.onNodeWithTag("vocabulary-word").assertTextEquals(item.text.display.en)
                    compose.onNodeWithTag("vocabulary-art-${item.id.value}",useUnmergedTree=true).performScrollTo().assertIsDisplayed()
                }
                compose.onNodeWithText("Explore food and drink").performScrollTo().assertIsDisplayed()
                compose.onNodeWithText("⚙ Options").performClick()
                compose.onNodeWithText("🇩🇪 Deutsch").performClick()
                scenario.onActivity {it.onBackPressedDispatcher.onBackPressed()};settled()
                compose.onNodeWithTag("vocabulary-word").assertTextEquals("Wasser")
                compose.onNodeWithText("Essen und Trinken entdecken").performScrollTo().assertIsDisplayed()
                click("vocabulary-choose-activity");click("vocabulary-open-FIND")
                while(state().phase!=SessionPhase.COMPLETED) {
                    val current=state()
                    if(!current.current.locked) {
                        if(current.current.answer==AnswerState.RETRY_AVAILABLE)click("vocabulary-retry")
                        click("answer-${state().task.question.correct.value}")
                    }
                    click("vocabulary-next")
                }
                compose.onNodeWithTag("completion-celebration").assertExists()
                scenario.onActivity {it.onBackPressedDispatcher.onBackPressed()};settled()
                click("vocabulary-open-NAME")
                if(state().phase==SessionPhase.COMPLETED)click("vocabulary-again")
                val saved=SessionCheckpoint.encode(state())
                scenario.recreate();settled()
                assertArrayEquals(saved,SessionCheckpoint.encode(state()))
                click("vocabulary-choose-activity");click("vocabulary-category-classroom")
                click("vocabulary-category-food");click("vocabulary-open-NAME")
                assertArrayEquals(saved,SessionCheckpoint.encode(state()))
                assertEquals(ContentLanguage.GERMAN,state().language)
            }
        } finally {
            prefs.edit().putString("lang",language).putString("audioMode",audio).putInt("round",round).commit()
        }
    }
}
