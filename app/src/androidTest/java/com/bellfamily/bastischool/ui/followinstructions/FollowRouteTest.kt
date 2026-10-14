package com.bellfamily.bastischool.ui.followinstructions

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.lifecycle.ViewModelProvider
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import com.bellfamily.bastischool.MainActivity
import com.bellfamily.bastischool.learning.follow.FollowTwo
import com.bellfamily.bastischool.learning.models.ContentLanguage
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class FollowRouteTest {
    @get:Rule val compose = createEmptyComposeRule()

    @Test fun levelsOptionsLanguageAndRecreationKeepBothSessionsIndependent() {
        val prefs = InstrumentationRegistry.getInstrumentation().targetContext.getSharedPreferences("basti_shell", 0)
        val oldLanguage = prefs.getString("lang", null)
        val oldAudio = prefs.getString("audioMode", null)
        val oldRound = prefs.getInt("round", 5)
        prefs.edit().putString("lang", "en").putString("audioMode", "off").putInt("round", 5).commit()
        try {
            ActivityScenario.launch(MainActivity::class.java).use { scenario ->
                fun ready(two: Boolean) = compose.waitUntil(10_000) {
                    var loaded = false
                    scenario.onActivity {
                        val vm = ViewModelProvider(it)[FollowViewModel::class.java]
                        loaded = (if (two) vm.twoState != null else vm.state != null) && vm.images.size == 6 && !vm.busy
                    }
                    loaded
                }
                compose.onNodeWithText("Follow the Instructions").performScrollTo().performClick()
                compose.onNodeWithTag("follow-level-one").assertTextEquals("Level 1 — One instruction").performClick()
                ready(false)
                var oneId = ""
                scenario.onActivity { oneId = ViewModelProvider(it)[FollowViewModel::class.java].state!!.plan.id.value }
                compose.onNodeWithTag("follow-hint").performScrollTo().performClick()
                compose.waitUntil(5_000) {
                    var hinted = false
                    scenario.onActivity { hinted = ViewModelProvider(it)[FollowViewModel::class.java].state?.current?.support?.hint == true }
                    hinted
                }
                compose.onNodeWithTag("follow-hint-message").performScrollTo().assertIsDisplayed()
                compose.onNodeWithTag("follow-levels").performScrollTo().performClick()
                compose.onNodeWithTag("follow-level-two").assertTextEquals("Level 2 — Two in order").performClick()
                ready(true)
                var twoId = ""; var first = ""
                scenario.onActivity {
                    val state = ViewModelProvider(it)[FollowViewModel::class.java].twoState!!
                    twoId = state.id.value; first = state.task.first.value
                }
                compose.onNodeWithTag("follow-two-tap-$first").performScrollTo().performClick()
                compose.waitUntil(5_000) {
                    var progressed = false
                    scenario.onActivity { progressed = ViewModelProvider(it)[FollowViewModel::class.java].twoState?.step == 1 }
                    progressed
                }
                compose.onNodeWithText("⚙ Options").performClick()
                compose.onNodeWithText("🇩🇪 Deutsch").performClick()
                scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
                compose.waitUntil(5_000) {
                    var german = false
                    scenario.onActivity { german = ViewModelProvider(it)[FollowViewModel::class.java].twoState?.language == ContentLanguage.GERMAN }
                    german
                }
                var expectedGerman = ""
                scenario.onActivity {
                    expectedGerman = FollowTwo.instruction(ViewModelProvider(it)[FollowViewModel::class.java].twoState!!.task).display.de
                }
                compose.onNodeWithTag("follow-two-instruction").performScrollTo().assertTextEquals(expectedGerman)
                scenario.recreate(); ready(true)
                scenario.onActivity {
                    val state = ViewModelProvider(it)[FollowViewModel::class.java].twoState!!
                    assertEquals(twoId, state.id.value)
                    assertEquals(1, state.step)
                }
                scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
                compose.onNodeWithTag("follow-level-one").assertIsDisplayed().performClick()
                ready(false)
                scenario.onActivity { assertEquals(oneId, ViewModelProvider(it)[FollowViewModel::class.java].state!!.plan.id.value) }
                compose.onNodeWithTag("follow-home").performScrollTo().performClick()
                compose.onNodeWithText("Anweisungen folgen").assertExists()
            }
        } finally {
            prefs.edit().putString("lang", oldLanguage).putString("audioMode", oldAudio).putInt("round", oldRound).commit()
        }
    }
}
