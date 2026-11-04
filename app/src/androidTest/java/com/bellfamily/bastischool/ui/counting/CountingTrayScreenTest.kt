package com.bellfamily.bastischool.ui.counting

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.bellfamily.bastischool.learning.counting.CountingTray
import com.bellfamily.bastischool.learning.models.ContentLanguage
import com.bellfamily.bastischool.learning.session.RoundLength
import com.bellfamily.bastischool.learning.session.SessionId
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class CountingTrayScreenTest {
    @get:Rule val compose = createComposeRule()
    private lateinit var state: MutableState<CountingTray.State>

    private fun show(language: ContentLanguage, width: Int = 360, height: Int = 760, fontScale: Float = 1f) {
        state = mutableStateOf(CountingTray.start(SessionId("tray-screen"), RoundLength.FIVE, 22, language))
        compose.setContent {
            CompositionLocalProvider(LocalDensity provides Density(LocalDensity.current.density, fontScale)) {
                MaterialTheme {
                    CountingTrayScreen(state.value, language, false, false, false,
                        { reduce(CountingTray.Action.Toggle(it)) }, { reduce(CountingTray.Action.Check) },
                        { reduce(CountingTray.Action.Next) }, {}, { reduce(CountingTray.Action.Help) }, {}, {}, {},
                        Modifier.width(width.dp).height(height.dp))
                }
            }
        }
    }

    private fun reduce(action: CountingTray.Action) {
        state.value = CountingTray.reduce(state.value, action)
    }

    @Test fun englishBuildCheckAdjustAndSuccessUseTheTapTray() {
        show(ContentLanguage.ENGLISH)
        repeat(5) {
            val target = state.value.target
            compose.onNodeWithTag("counting-tray-target").performScrollTo().assertTextEquals(target.toString())
            compose.onNodeWithTag("counting-tray-prompt").performScrollTo()
                .assertTextEquals(CountingTray.prompt(target).display.en)
            compose.onNodeWithTag("counting-tray-how-to").performScrollTo().assertIsDisplayed()
            compose.onNodeWithTag("counting-tray-slot-1").assertContentDescriptionEquals(
                "Empty space 1. Tap to add a dinosaur.")
            repeat(target - 1) { slot ->
                compose.onNodeWithTag("counting-tray-slot-${slot + 1}").performScrollTo().performClick()
            }
            compose.onNodeWithTag("counting-tray-check").performScrollTo().performClick()
            if (target > 1) {
                compose.onNodeWithTag("counting-tray-feedback").performScrollTo().assertIsDisplayed()
                compose.onNodeWithTag("counting-tray-slot-$target").performScrollTo().performClick()
                compose.onNodeWithTag("counting-tray-check").performScrollTo().performClick()
            } else {
                compose.onNodeWithTag("counting-tray-slot-5").performScrollTo().performClick()
                compose.onNodeWithTag("counting-tray-check").performScrollTo().performClick()
            }
            compose.onNodeWithTag("counting-tray-feedback").performScrollTo()
                .assertTextEquals(CountingTray.success(target).display.en)
            compose.onNodeWithTag("counting-tray-next").performScrollTo().performClick()
        }
        compose.runOnIdle { assertTrue(state.value.completed) }
        compose.onNodeWithTag("counting-tray-complete").assertExists()
    }

    @Test fun germanLargeTextAndShortLandscapeKeepFiveTapTargetsReachable() {
        show(ContentLanguage.GERMAN, width = 640, height = 300, fontScale = 2f)
        val target = state.value.target
        compose.onNodeWithTag("counting-tray-prompt").performScrollTo()
            .assertTextEquals(CountingTray.prompt(target).display.de)
        (1..5).forEach { slot ->
            val node = compose.onNodeWithTag("counting-tray-slot-$slot").performScrollTo().assertIsDisplayed()
            node.assertWidthIsAtLeast(44.dp).assertHeightIsAtLeast(44.dp)
        }
        compose.onNodeWithTag("counting-tray-check").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("counting-tray-help-button").performScrollTo().performClick()
        compose.onNodeWithTag("counting-tray-help").performScrollTo().assertIsDisplayed()
    }
}
