package com.bellfamily.bastischool.ui.months

import com.bellfamily.bastischool.openHomeActivity

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.lifecycle.ViewModelProvider
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import com.bellfamily.bastischool.MainActivity
import com.bellfamily.bastischool.learning.session.SessionPhase
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class MonthsSeasonsMatchRouteTest {
    @get:Rule val compose = createEmptyComposeRule()

    @Test fun routeOptionsAndActivityRecreationRestoreTheSameMonthAssignment() {
        val prefs = InstrumentationRegistry.getInstrumentation().targetContext.getSharedPreferences("basti_shell", 0)
        val oldLang = prefs.getString("lang", null)
        val oldAudio = prefs.getString("audioMode", null)
        prefs.edit().putString("lang", "en").putString("audioMode", "off").commit()
        try {
            ActivityScenario.launch(MainActivity::class.java).use { scenario ->
                fun settled() = compose.waitUntil(15_000) {
                    var ready = false
                    scenario.onActivity {
                        val vm = ViewModelProvider(it)[MonthsViewModel::class.java]
                        ready = vm.quiz != null && !vm.quizBusy && vm.seasonArtwork.size == 4
                    }
                    ready
                }
                compose.openHomeActivity("Days & Seasons")
                compose.onNodeWithTag("open-months").performScrollTo().performClick()
                compose.onNodeWithTag("months-open-seasons-match").performScrollTo().performClick()
                settled()

                var session = ""; var target = ""; var month = ""
                scenario.onActivity {
                    val vm = ViewModelProvider(it)[MonthsViewModel::class.java]
                    if (vm.quiz!!.phase == SessionPhase.COMPLETED) vm.again()
                }
                compose.waitUntil(15_000) {
                    var active = false
                    scenario.onActivity { active = ViewModelProvider(it)[MonthsViewModel::class.java].quiz?.phase == SessionPhase.ACTIVE }
                    active
                }
                scenario.onActivity {
                    val state = ViewModelProvider(it)[MonthsViewModel::class.java].quiz!!
                    session = state.plan.id.value; target = state.task.question.correct.value
                    month = com.bellfamily.bastischool.learning.months.MonthContent.month(
                        com.bellfamily.bastischool.learning.months.MonthsSeasonsMatch.month(state.task.question)).text.display.en
                }
                compose.onNodeWithTag("months-seasons-month-tile").performScrollTo().assertTextEquals(month)
                compose.onNodeWithTag("months-season-target-$target").performScrollTo().assertIsEnabled()

                compose.onNodeWithText("⚙ Options").performClick()
                compose.onNodeWithText("🇩🇪 Deutsch").performClick()
                scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
                settled()
                compose.onNodeWithTag("months-seasons-month-tile").performScrollTo().assertTextContains(
                    com.bellfamily.bastischool.learning.months.MonthContent.definitions.first { it.text.display.en == month }.text.display.de)

                scenario.recreate(); settled()
                var restored = ""
                scenario.onActivity { restored = ViewModelProvider(it)[MonthsViewModel::class.java].quiz!!.plan.id.value }
                assertEquals(session, restored)
                compose.onNodeWithTag("months-seasons-month-tile").performScrollTo().assertIsDisplayed()
                scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
                compose.waitUntil(10_000) {
                    compose.onAllNodesWithTag("months-open-seasons-match").fetchSemanticsNodes().isNotEmpty()
                }
                compose.onNodeWithTag("months-open-seasons-match").performScrollTo().assertIsDisplayed()
            }
        } finally { prefs.edit().putString("lang", oldLang).putString("audioMode", oldAudio).commit() }
    }
}
