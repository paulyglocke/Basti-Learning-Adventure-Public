package com.bellfamily.bastischool.ui.months

import com.bellfamily.bastischool.openHomeActivity

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
                compose.openHomeActivity("Days & Seasons")
                compose.onNodeWithTag("open-months").performScrollTo().performClick()
                compose.onNodeWithTag("months-open-explore").performScrollTo().performClick();settled()
                compose.onNodeWithTag(MonthIds.SEPTEMBER.value).performScrollTo().performClick()
                scenario.recreate();settled()
                compose.onNodeWithTag("months-selected").performScrollTo().assertTextEquals("September")
                compose.onNodeWithText("⚙ Options").performClick()
                compose.onNodeWithText("🇩🇪 Deutsch").performClick()
                scenario.onActivity{it.onBackPressedDispatcher.onBackPressed()};settled()
                compose.onNodeWithTag("months-selected").performScrollTo().assertTextEquals("September")
                compose.onNodeWithTag(MonthIds.MARCH.value).performScrollTo().assertTextEquals("März")
                scenario.onActivity{it.onBackPressedDispatcher.onBackPressed()}
                compose.onNodeWithTag("months-open-explore").performScrollTo().performClick();settled()
                compose.onNodeWithTag("months-selected").performScrollTo().assertTextEquals("September")
                compose.onNodeWithTag("months-home").performScrollTo().performClick()
                compose.onNodeWithTag("home-category-TIME").performScrollTo().performClick()
                compose.onNodeWithText("Tage & Jahreszeiten").performScrollTo().assertIsDisplayed()
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
                    scenario.onActivity {
                        val owner = ViewModelProvider(it)[MonthsViewModel::class.java]
                        loaded = owner.buildYear != null && !owner.buildBusy && !owner.buildYearFailed &&
                            owner.buildYear?.language == ContentLanguage.ENGLISH
                    }
                    loaded
                }
                compose.openHomeActivity("Days & Seasons")
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
                    scenario.onActivity { val vm=ViewModelProvider(it)[MonthsViewModel::class.java]; loaded=vm.buildYearFour!=null && !vm.buildYearFourBusy && !vm.buildYearFourFailed }
                    loaded
                }
                compose.openHomeActivity("Days & Seasons")
                compose.onNodeWithTag("open-months").performScrollTo().performClick()
                compose.onNodeWithTag("months-open-build-year").performScrollTo().performClick()
                compose.onNodeWithTag("months-build-year-level-1").assertTextEquals("Level 1 — Three Months")
                compose.onNodeWithTag("months-build-year-level-2").assertTextEquals("Level 2 — Four Months").performClick()
                fourReady()
                fun state(): MonthsBuildYearFourState {
                    var value: MonthsBuildYearFourState? = null
                    scenario.onActivity { value = ViewModelProvider(it)[MonthsViewModel::class.java].buildYearFour }
                    return value!!
                }
                // Preserve any existing partial/completed round. Only a genuinely empty
                // sequence needs a placement to exercise restoration of child work.
                if (!state().completed && state().placed.isEmpty() && state().nextAttempt != null) {
                    val firstMonth = state().order.first().value
                    compose.onNodeWithTag("months-build-year-four-choice-$firstMonth").performScrollTo().performClick()
                    compose.onNodeWithTag("months-build-year-four-slot-1").performScrollTo().performClick()
                    compose.waitUntil(10_000) { state().placed.size == 1 }
                    fourReady()
                }
                val original = state()
                val originalId = original.id.value
                fun assertRestored(expected: MonthsBuildYearFourState) = assertArrayEquals(
                    MonthsBuildYearFourHost.encode(expected, emptyList()),
                    MonthsBuildYearFourHost.encode(state(), emptyList()))
                scenario.recreate(); fourReady()
                assertRestored(original)
                compose.onNodeWithText("⚙ Options").performClick()
                compose.onNodeWithText("🇩🇪 Deutsch").performClick()
                scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
                fourReady()
                val german = original.changed(language = ContentLanguage.GERMAN)
                assertRestored(german)
                if (german.completed) compose.onNodeWithTag("months-build-year-four-again").assertIsDisplayed()
                else compose.onNodeWithTag("months-build-year-four-progress").performScrollTo()
                    .assertTextEquals("Folge ${german.questionIndex + 1} von ${german.round.count}")
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
                assertRestored(german)
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
                compose.openHomeActivity("Days & Seasons")
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
