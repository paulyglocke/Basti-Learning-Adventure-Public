package com.bellfamily.bastischool.ui.common

import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.text.TextLayoutResult
import androidx.lifecycle.ViewModelProvider
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import com.bellfamily.bastischool.MainActivity
import com.bellfamily.bastischool.learning.session.*
import com.bellfamily.bastischool.ui.prepositions.PrepositionsViewModel
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class SupportTextSettingRouteTest {
    @get:Rule val compose = createEmptyComposeRule()
    @Test fun preferenceSurvivesRecreationAndRelaunchWithoutChangingSession() {
        val prefs = InstrumentationRegistry.getInstrumentation().targetContext.getSharedPreferences("basti_shell", 0)
        val existed = prefs.contains("largerSupportText")
        val original = prefs.getBoolean("largerSupportText", false)
        prefs.edit().remove("largerSupportText").putString("lang", "en").putString("audioMode", "off").commit()
        try {
            ActivityScenario.launch(MainActivity::class.java).use { scenario ->
                fun current(): SessionState {
                    var state: SessionState? = null
                    scenario.onActivity { state = ViewModelProvider(it)[PrepositionsViewModel::class.java].state }
                    return state!!
                }
                fun settled() { compose.waitUntil(10_000) {
                    var ready = false
                    scenario.onActivity { val vm = ViewModelProvider(it)[PrepositionsViewModel::class.java]; ready = vm.state != null && !vm.busy && !vm.saveFailed }
                    ready
                } }
                fun hintSize(): Float {
                    val layouts = mutableListOf<TextLayoutResult>()
                    compose.onNodeWithTag("hint-text").performScrollTo()
                        .performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(layouts) }
                    return layouts.single().layoutInput.style.fontSize.value
                }
                compose.onNodeWithText("Prepositions").performScrollTo().performClick(); settled()
                if (current().phase == SessionPhase.COMPLETED) {
                    compose.onNodeWithTag("again").performClick(); settled()
                }
                if (current().current.locked) {
                    compose.onNodeWithTag("next").performScrollTo().performClick(); settled()
                    if (current().phase == SessionPhase.COMPLETED) { compose.onNodeWithTag("again").performClick(); settled() }
                }
                compose.onNodeWithTag("hint").performScrollTo().performClick(); settled()
                val before = SessionCheckpoint.encode(current())
                val normal = hintSize()
                compose.onNodeWithText("⚙ Options").performClick()
                compose.onNodeWithTag("larger-support-text").performScrollTo().assertIsOff().performClick().assertIsOn()
                scenario.recreate()
                compose.onNodeWithTag("larger-support-text").performScrollTo().assertIsOn()
                scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }; settled()
                assertArrayEquals(before, SessionCheckpoint.encode(current()))
                assertEquals(normal * 1.25f, hintSize(), 0.001f)
            }
            ActivityScenario.launch(MainActivity::class.java).use {
                compose.onNodeWithText("⚙ Options").performClick()
                compose.onNodeWithTag("larger-support-text").performScrollTo().assertIsOn().performClick().assertIsOff()
                assertFalse(prefs.getBoolean("largerSupportText", true))
            }
        } finally {
            val editor = prefs.edit()
            if (existed) editor.putBoolean("largerSupportText", original) else editor.remove("largerSupportText")
            editor.commit()
        }
    }
}
