package com.bellfamily.bastischool.ui.common

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class SupportTextSettingTest(private val language: String, private val orientation: Int) {
    companion object {
        @JvmStatic @Parameterized.Parameters(name = "{0}-{1}")
        fun cases() = listOf(
            arrayOf<Any>("en", ActivityInfo.SCREEN_ORIENTATION_PORTRAIT),
            arrayOf<Any>("de", ActivityInfo.SCREEN_ORIENTATION_PORTRAIT),
            arrayOf<Any>("de", ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE),
            arrayOf<Any>("de", ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE),
        )
    }
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
    private fun layout(tag: String): TextLayoutResult {
        val results = mutableListOf<TextLayoutResult>()
        compose.onNodeWithTag(tag).performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(results) }
        return results.single()
    }
    @OptIn(ExperimentalTestApi::class)
    @Test fun settingScalesOnlySupportAndKeepsLongTextActionsAndSwitchAccessible() {
        val landscape = orientation != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        compose.activity.requestedOrientation = orientation
        compose.waitUntil(10_000) { compose.activity.resources.configuration.orientation ==
            if (landscape) Configuration.ORIENTATION_LANDSCAPE else Configuration.ORIENTATION_PORTRAIT }
        var changes = 0
        val text = if (language == "de") "Versuche es noch einmal. Deine eingeordneten Tage bleiben hier. Du kannst dir helfen lassen."
            else "Try again. Your placed days stay here. You can ask for help."
        compose.setContent {
            var larger by remember { mutableStateOf(false) }
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, 1.5f)) {
                MaterialTheme {
                    Column(Modifier.size(if (landscape) 700.dp else 320.dp, if (landscape) 240.dp else 480.dp)
                        .verticalScroll(rememberScrollState())) {
                        SupportTextSetting(language, larger) { larger = it; changes++ }
                        NativeSupportMessage(text, Modifier.testTag("default-support"))
                        CompositionLocalProvider(LocalLargerSupportText provides larger) {
                            Text("Prompt", Modifier.testTag("prompt"), style = MaterialTheme.typography.bodyLarge)
                            NativeSupportMessage(text, Modifier.testTag("support"))
                            NativeActionButton("Home", NativeActionRole.NAVIGATION, {}, Modifier.testTag("home"))
                        }
                    }
                }
            }
        }
        val base = layout("default-support").layoutInput.style
        val ordinary = layout("prompt").layoutInput.style
        assertEquals(base, layout("support").layoutInput.style)
        val toggle = compose.onNodeWithTag("larger-support-text")
        toggle.performScrollTo().assertIsOff().assertHeightIsAtLeast(56.dp)
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Switch)).performClick()
        toggle.assertIsOn()
        val support = compose.onNodeWithTag("support")
        support.performScrollTo().assertIsDisplayed().assertTextEquals(text).assert(hasNoClickAction())
            .assert(SemanticsMatcher.keyNotDefined(SemanticsProperties.Focused))
            .assert(SemanticsMatcher.keyNotDefined(SemanticsProperties.ContentDescription))
            .assert(SemanticsMatcher.keyNotDefined(SemanticsProperties.LiveRegion))
        val enlarged = layout("support")
        assertEquals(base.fontSize * 1.25f, enlarged.layoutInput.style.fontSize)
        assertEquals(base.lineHeight * 1.25f, enlarged.layoutInput.style.lineHeight)
        assertFalse(enlarged.hasVisualOverflow)
        assertEquals(ordinary, layout("prompt").layoutInput.style)
        assertEquals(base, layout("default-support").layoutInput.style)
        compose.onNodeWithTag("home").performScrollTo().assertIsDisplayed()
        toggle.performScrollTo().performSemanticsAction(SemanticsActions.RequestFocus) { it() }
        toggle.assertIsFocused().performKeyInput { pressKey(Key.Enter) }
        toggle.assertIsOff()
        assertEquals(base, layout("support").layoutInput.style)
        compose.runOnIdle { assertEquals(2, changes) }
    }
}
