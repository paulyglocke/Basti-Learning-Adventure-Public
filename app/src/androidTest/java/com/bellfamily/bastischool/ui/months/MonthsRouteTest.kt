package com.bellfamily.bastischool.ui.months

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import com.bellfamily.bastischool.MainActivity
import com.bellfamily.bastischool.learning.models.ContentId
import com.bellfamily.bastischool.learning.models.ContentLanguage
import com.bellfamily.bastischool.learning.months.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class MonthsRouteTest {
    @get:Rule val compose=createEmptyComposeRule()
    @Test fun hubOptionsBackAndRecreationKeepMonth() {
        val prefs=InstrumentationRegistry.getInstrumentation().targetContext.getSharedPreferences("basti_shell",0)
        val oldLang=prefs.getString("lang",null);val oldAudio=prefs.getString("audioMode",null)
        prefs.edit().putString("lang","en").putString("audioMode","off").commit()
        try {
            ActivityScenario.launch(MainActivity::class.java).use {scenario ->
                fun settled(){compose.waitUntil(10_000){var ready=false;scenario.onActivity{ready=ViewModelProvider(it)[MonthsViewModel::class.java].selection!=null};ready}}
                compose.onNodeWithText("Days & Seasons").performScrollTo().performClick()
                compose.onNodeWithTag("open-months").performScrollTo().performClick();settled()
                compose.onNodeWithTag(MonthIds.SEPTEMBER.value).performScrollTo().performClick()
                scenario.recreate();settled()
                compose.onNodeWithTag("months-selected").performScrollTo().assertTextEquals("September")
                compose.onNodeWithText("⚙ Options").performClick()
                compose.onNodeWithText("🇩🇪 Deutsch").performClick()
                scenario.onActivity{it.onBackPressedDispatcher.onBackPressed()};settled()
                compose.onNodeWithTag("months-selected").performScrollTo().assertTextEquals("September")
                compose.onNodeWithTag(MonthIds.MARCH.value).performScrollTo().assertTextEquals("März")
                scenario.onActivity{it.onBackPressedDispatcher.onBackPressed()}
                compose.onNodeWithTag("open-months").performScrollTo().performClick();settled()
                compose.onNodeWithTag("months-selected").performScrollTo().assertTextEquals("September")
                compose.onNodeWithTag("months-home").performScrollTo().performClick()
                compose.onNodeWithText("Tage & Jahreszeiten").assertExists()
            }
        } finally {prefs.edit().putString("lang",oldLang).putString("audioMode",oldAudio).commit()}
    }

    @Test fun buildYearRouteSurvivesOptionsRecreationAndBack() {
        val prefs=InstrumentationRegistry.getInstrumentation().targetContext.getSharedPreferences("basti_shell",0)
        val oldLang=prefs.getString("lang",null);val oldAudio=prefs.getString("audioMode",null)
        prefs.edit().putString("lang","en").putString("audioMode","off").commit()
        try {
            ActivityScenario.launch(MainActivity::class.java).use { scenario ->
                fun ready() = compose.waitUntil(10_000) {
                    var loaded=false
                    scenario.onActivity { loaded=ViewModelProvider(it)[MonthsViewModel::class.java].buildYear!=null }
                    loaded
                }
                compose.onNodeWithText("Days & Seasons").performScrollTo().performClick()
                compose.onNodeWithTag("open-months").performScrollTo().performClick()
                compose.onNodeWithTag("months-open-build-year").performScrollTo().performClick()
                compose.onNodeWithTag("months-build-year-level-1").performScrollTo().assertIsDisplayed().performClick()
                ready()
                compose.onNodeWithTag("months-build-year-prompt").performScrollTo()
                    .assertTextEquals(MonthsBuildYear.introduction.display[com.bellfamily.bastischool.learning.models.ContentLanguage.ENGLISH])
                var originalId=""
                var selectedMonth=""
                scenario.onActivity {
                    val state=ViewModelProvider(it)[MonthsViewModel::class.java].buildYear!!
                    originalId=state.id.value;selectedMonth=state.choices.first().value
                }
                compose.onNodeWithTag("months-build-year-choice-$selectedMonth").performScrollTo()
                    .assertIsEnabled().performClick()
                compose.waitUntil(5_000) {
                    compose.onAllNodesWithTag("months-build-year-selected").fetchSemanticsNodes().isNotEmpty()
                }
                compose.onNodeWithTag("months-build-year-selected").performScrollTo().assertIsDisplayed()
                scenario.moveToState(Lifecycle.State.CREATED)
                scenario.moveToState(Lifecycle.State.RESUMED);ready()
                var returnedId=""
                scenario.onActivity { returnedId=ViewModelProvider(it)[MonthsViewModel::class.java].buildYear!!.id.value }
                assertEquals(originalId,returnedId)
                scenario.recreate();ready()
                compose.onNodeWithTag("months-build-year-choice-$selectedMonth").performScrollTo().assertIsSelected()
                var restoredId=""
                scenario.onActivity { restoredId=ViewModelProvider(it)[MonthsViewModel::class.java].buildYear!!.id.value }
                assertEquals(originalId,restoredId)
                compose.onNodeWithText("⚙ Options").performClick()
                compose.onNodeWithText("🇩🇪 Deutsch").performClick()
                scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
                compose.onNodeWithTag("months-build-year-prompt").performScrollTo()
                    .assertTextEquals(MonthsBuildYear.introduction.display[com.bellfamily.bastischool.learning.models.ContentLanguage.GERMAN])
                scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
                compose.onNodeWithTag("months-build-year-level-1").assertIsDisplayed()
                scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
                compose.onNodeWithTag("months-open-build-year").performScrollTo().assertIsDisplayed()
            }
        } finally { prefs.edit().putString("lang",oldLang).putString("audioMode",oldAudio).commit() }
    }

    @Test fun buildYearLevelsKeepIndependentSessionsAcrossOptionsAndRecreation() {
        val prefs=InstrumentationRegistry.getInstrumentation().targetContext.getSharedPreferences("basti_shell",0)
        val oldLang=prefs.getString("lang",null);val oldAudio=prefs.getString("audioMode",null);val oldRound=prefs.getInt("round",5)
        prefs.edit().putString("lang","en").putString("audioMode","off").putInt("round",5).commit()
        try {
            ActivityScenario.launch(MainActivity::class.java).use { scenario ->
                fun fourReady() = compose.waitUntil(10_000) {
                    var loaded=false
                    scenario.onActivity { loaded=ViewModelProvider(it)[MonthsViewModel::class.java].buildYearFour!=null }
                    loaded
                }
                compose.onNodeWithText("Days & Seasons").performScrollTo().performClick()
                compose.onNodeWithTag("open-months").performScrollTo().performClick()
                compose.onNodeWithTag("months-open-build-year").performScrollTo().performClick()
                compose.onNodeWithTag("months-build-year-level-1").assertTextEquals("Level 1 — Three Months")
                compose.onNodeWithTag("months-build-year-level-2").assertTextEquals("Level 2 — Four Months").performClick()
                fourReady()
                var originalId="";var firstMonth="";var roundCount=0
                scenario.onActivity {
                    val state=ViewModelProvider(it)[MonthsViewModel::class.java].buildYearFour!!
                    originalId=state.id.value;firstMonth=state.order.first().value;roundCount=state.round.count
                }
                assertEquals(5,roundCount)
                compose.onNodeWithTag("months-build-year-four-choice-$firstMonth").performScrollTo().performClick()
                compose.onNodeWithTag("months-build-year-four-slot-1").performScrollTo().performClick()
                compose.waitUntil(10_000) {
                    var placed=false
                    scenario.onActivity { placed=ViewModelProvider(it)[MonthsViewModel::class.java].buildYearFour?.placed?.size==1 }
                    placed
                }
                scenario.recreate();fourReady()
                scenario.onActivity {
                    val state=ViewModelProvider(it)[MonthsViewModel::class.java].buildYearFour!!
                    assertEquals(originalId,state.id.value);assertEquals(listOf(ContentId(firstMonth)),state.placed)
                }
                compose.onNodeWithText("⚙ Options").performClick()
                compose.onNodeWithText("🇩🇪 Deutsch").performClick()
                scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
                compose.onNodeWithTag("months-build-year-four-progress").performScrollTo().assertTextEquals("Folge 1 von 5")
                scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
                compose.onNodeWithTag("months-build-year-level-1").performScrollTo().performClick()
                compose.waitUntil(10_000) {
                    var loaded=false
                    scenario.onActivity { loaded=ViewModelProvider(it)[MonthsViewModel::class.java].buildYear!=null }
                    loaded
                }
                scenario.onActivity {
                    val three=ViewModelProvider(it)[MonthsViewModel::class.java].buildYear!!
                    assertEquals(3,three.order.size);assertEquals("activity.months.build_year.three",MonthsBuildYear.activity.value)
                    assertNotEquals(originalId,three.id.value)
                }
                scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
                compose.onNodeWithTag("months-build-year-level-2").performScrollTo().performClick()
                fourReady()
                scenario.onActivity {
                    val four=ViewModelProvider(it)[MonthsViewModel::class.java].buildYearFour!!
                    assertEquals(originalId,four.id.value);assertEquals(listOf(ContentId(firstMonth)),four.placed)
                }
            }
        } finally { prefs.edit().putString("lang",oldLang).putString("audioMode",oldAudio).putInt("round",oldRound).commit() }
    }

    @Test fun betweenKeepsEndpointsAndReturnsFromYearWheelWithSameQuestionAndAssistance() {
        val prefs=InstrumentationRegistry.getInstrumentation().targetContext.getSharedPreferences("basti_shell",0)
        val oldLang=prefs.getString("lang",null);val oldAudio=prefs.getString("audioMode",null)
        prefs.edit().putString("lang","en").putString("audioMode","off").commit()
        try {
            ActivityScenario.launch(MainActivity::class.java).use { scenario ->
                fun ready() = compose.waitUntil(10_000) {
                    var loaded=false
                    scenario.onActivity { loaded=ViewModelProvider(it)[MonthsViewModel::class.java].quiz!=null }
                    loaded
                }
                compose.onNodeWithText("Days & Seasons").performScrollTo().performClick()
                compose.onNodeWithTag("open-months").performScrollTo().performClick()
                compose.onNodeWithTag("months-open-between").performScrollTo().performClick()
                ready()
                var originalSession="";var originalTask="";var firstEndpoint=""
                scenario.onActivity {
                    val state=ViewModelProvider(it)[MonthsViewModel::class.java].quiz!!
                    originalSession=state.plan.id.value;originalTask=state.task.question.definition.value
                    firstEndpoint=MonthsBetween.definition(state.task.question).before.value
                }
                compose.onNodeWithTag("months-between-before").performScrollTo()
                    .assertTextEquals(MonthContent.month(ContentId(firstEndpoint)).text.display[ContentLanguage.ENGLISH])
                compose.onNodeWithTag("months-quiz-explore").performScrollTo().performClick()
                compose.waitUntil(10_000) {
                    compose.onAllNodesWithTag("months-between-return").fetchSemanticsNodes().isNotEmpty()
                }
                compose.onNodeWithTag("months-between-return").performScrollTo().assertIsDisplayed()
                compose.onNodeWithTag("months-between-return").performClick()
                ready()
                scenario.onActivity {
                    val state=ViewModelProvider(it)[MonthsViewModel::class.java].quiz!!
                    assertEquals(originalSession,state.plan.id.value)
                    assertEquals(originalTask,state.task.question.definition.value)
                    assertTrue(state.current.support.hint)
                }
                scenario.recreate();ready()
                compose.onNodeWithTag("months-between-sequence").performScrollTo().assertIsDisplayed()
                scenario.onActivity {
                    val state=ViewModelProvider(it)[MonthsViewModel::class.java].quiz!!
                    assertEquals(originalSession,state.plan.id.value)
                    assertEquals(originalTask,state.task.question.definition.value)
                    assertTrue(state.current.support.hint)
                }
            }
        } finally {prefs.edit().putString("lang",oldLang).putString("audioMode",oldAudio).commit()}
    }
}
