package com.bellfamily.bastischool.ui.months

import com.bellfamily.bastischool.openHomeActivity

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModelProvider
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import com.bellfamily.bastischool.MainActivity
import com.bellfamily.bastischool.learning.months.MonthsSeasonWheelState
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class MonthsSeasonWheelRouteTest {
    @get:Rule val compose = createEmptyComposeRule()

    @Test fun optionsLanguageRecreationAndReturnPreserveTheWheel() {
        val prefs = InstrumentationRegistry.getInstrumentation().targetContext.getSharedPreferences("basti_shell", 0)
        val oldLang = prefs.getString("lang", null)
        val oldAudio = prefs.getString("audioMode", null)
        prefs.edit().putString("lang", "en").putString("audioMode", "off").commit()
        try {
            ActivityScenario.launch(MainActivity::class.java).use { scenario ->
                fun settled() = compose.waitUntil(15_000) {
                    var ready = false
                    scenario.onActivity { ready = ViewModelProvider(it)[MonthsViewModel::class.java].wheel != null }
                    ready
                }
                compose.openHomeActivity("Days & Seasons")
                compose.onNodeWithTag("open-months").performScrollTo().performClick()
                compose.onNodeWithTag("months-open-seasons-wheel").performScrollTo().performClick()
                settled()
                // Previous runs may have left Explore selected. Choose Match through
                // the UI, preserving the saved rotation, round and support counters.
                compose.onNodeWithTag("wheel-match-mode").performScrollTo().performClick()
                var priorReplays = 0
                scenario.onActivity { priorReplays = ViewModelProvider(it)[MonthsViewModel::class.java].wheel!!.replayCount }
                compose.onNodeWithTag("wheel-next").performScrollTo().performClick()
                var beforeDrag = 0
                scenario.onActivity { beforeDrag = ViewModelProvider(it)[MonthsViewModel::class.java].wheel!!.rotation }
                compose.onNodeWithTag("wheel-ring").performScrollTo().performTouchInput {
                    val r = width * .38f
                    down(center + Offset(r, 0f))
                    moveTo(center + Offset(r * .85f, r * .5f))
                    moveTo(center + Offset(r * .5f, r * .85f))
                    moveTo(center + Offset(0f, r))
                    up()
                }
                compose.waitUntil(5_000) {
                    var changed = false
                    scenario.onActivity { changed = ViewModelProvider(it)[MonthsViewModel::class.java].wheel!!.rotation != beforeDrag }
                    changed
                }
                scenario.onActivity {
                    assertEquals(Math.floorMod(beforeDrag + 3, 12), ViewModelProvider(it)[MonthsViewModel::class.java].wheel!!.rotation)
                }
                // A second quarter-turn must use the restored/current rotation, not
                // the state captured by the original pointer-input coroutine.
                compose.onNodeWithTag("wheel-ring").performTouchInput {
                    val r = width * .38f
                    down(center + Offset(r, 0f))
                    moveTo(center + Offset(r * .85f, r * .5f))
                    moveTo(center + Offset(r * .5f, r * .85f))
                    moveTo(center + Offset(0f, r))
                    up()
                }
                compose.waitUntil(5_000) {
                    var correct = false
                    scenario.onActivity { correct = ViewModelProvider(it)[MonthsViewModel::class.java].wheel!!.rotation == Math.floorMod(beforeDrag + 6, 12) }
                    correct
                }
                compose.onNodeWithTag("wheel-hint").performScrollTo().performClick()
                compose.onNodeWithTag("wheel-listen").performScrollTo().performClick()
                compose.onNodeWithTag("wheel-explore-mode").performScrollTo().performClick()
                var weatherEnabled = false
                scenario.onActivity { weatherEnabled = ViewModelProvider(it)[MonthsViewModel::class.java].wheel!!.weatherEnabled }
                if (!weatherEnabled) compose.onNodeWithTag("wheel-weather-toggle").performScrollTo().performClick()
                compose.onNodeWithTag("wheel-weather-change").performScrollTo().performClick()
                var snapshot: MonthsSeasonWheelState? = null
                scenario.onActivity { snapshot = ViewModelProvider(it)[MonthsViewModel::class.java].wheel }
                assertTrue(snapshot!!.hintUsed)
                assertEquals(priorReplays + 1, snapshot!!.replayCount)
                compose.onNodeWithText("⚙ Options").performClick()
                compose.onNodeWithText("🇩🇪 Deutsch").performClick()
                scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
                compose.onNodeWithTag("wheel-weather-change").performScrollTo().assertTextEquals("Wetter ändern")
                scenario.recreate(); settled()
                scenario.onActivity { assertEquals(snapshot, ViewModelProvider(it)[MonthsViewModel::class.java].wheel) }
                scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
                compose.onNodeWithTag("months-open-seasons-wheel").performScrollTo().performClick()
                settled()
                scenario.onActivity { assertEquals(snapshot, ViewModelProvider(it)[MonthsViewModel::class.java].wheel) }
            }
        } finally { prefs.edit().putString("lang", oldLang).putString("audioMode", oldAudio).commit() }
    }
}
