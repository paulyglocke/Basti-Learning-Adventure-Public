package com.bellfamily.bastischool.ui.common

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Bitmap
import android.view.Surface
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.io.File

@RunWith(Parameterized::class)
class NativeTextChoiceTest(private val german: Boolean, private val orientation: Int) {
    companion object {
        @JvmStatic @Parameterized.Parameters(name = "German={0},orientation={1}")
        fun cases() = listOf(
            arrayOf<Any>(false, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT),
            arrayOf<Any>(true, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT),
            arrayOf<Any>(true, ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE),
            arrayOf<Any>(true, ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE),
        )
    }
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

    @OptIn(ExperimentalTestApi::class)
    @Test fun choicesKeepLabelsTargetsFocusLockingAndSeparateListenAtLargeFont() {
        val landscape = orientation != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        compose.activity.requestedOrientation = orientation
        compose.waitUntil(10_000) { compose.activity.resources.configuration.orientation ==
            if (landscape) Configuration.ORIENTATION_LANDSCAPE else Configuration.ORIENTATION_PORTRAIT }
        val rotation = when (orientation) {
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE -> Surface.ROTATION_90
            ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE -> Surface.ROTATION_270
            else -> Surface.ROTATION_0
        }
        compose.waitUntil(10_000) { compose.activity.window.decorView.display?.rotation == rotation }
        val labels = if (german) listOf("zwischen", "Frühling", "zwischen den Gegenständen")
            else listOf("between", "Spring", "between the objects")
        var enabled by mutableStateOf(true)
        var answers = 0
        var listens = 0
        var normalSize: TextUnit = TextUnit.Unspecified
        compose.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, 1.5f), LocalLargerSupportText provides true) {
                MaterialTheme {
                    normalSize = MaterialTheme.typography.bodyLarge.fontSize
                    Column(Modifier.size(if (landscape) 700.dp else 320.dp, if (landscape) 240.dp else 480.dp)
                        .testTag("review").verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        labels.forEachIndexed { index, label ->
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                NativeTextChoice(label, { answers++ }, Modifier.weight(1f).testTag("answer-$index"), enabled)
                                OutlinedButton({ listens++ }, Modifier.heightIn(min = 64.dp).testTag("speaker-$index")) {
                                    Text(if (german) "Hören" else "Listen")
                                }
                            }
                        }
                        NativeActionButton(if (german) "Weiter" else "Next", NativeActionRole.PRIMARY, {}, Modifier.testTag("next"))
                    }
                }
            }
        }
        labels.forEachIndexed { index, label ->
            val node = compose.onNodeWithTag("answer-$index").performScrollTo().assertIsDisplayed()
                .assertTextEquals(label).assertIsEnabled().assertHeightIsAtLeast(64.dp).assertWidthIsAtLeast(48.dp)
                .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button))
                .assert(SemanticsMatcher.keyNotDefined(SemanticsProperties.ContentDescription))
            val layouts = mutableListOf<TextLayoutResult>()
            node.performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(layouts) }
            assertFalse(layouts.single().hasVisualOverflow)
            assertEquals(normalSize, layouts.single().layoutInput.style.fontSize)
        }
        compose.onNodeWithTag("next").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("answer-0").performScrollTo()
        val image = compose.onNodeWithTag("review").captureToImage().asAndroidBitmap()
        File(compose.activity.cacheDir, "text-choice-$german-$orientation.png").outputStream().use {
            image.compress(Bitmap.CompressFormat.PNG, 100, it)
        }
        compose.onNodeWithTag("speaker-0").performScrollTo().performClick()
        compose.runOnIdle { assertEquals(0, answers); assertEquals(1, listens) }
        compose.onNodeWithTag("answer-0").performScrollTo().performClick()
        compose.onNodeWithTag("answer-1").performScrollTo()
            .performSemanticsAction(SemanticsActions.RequestFocus) { it() }
        compose.onNodeWithTag("answer-1").assertIsFocused().performKeyInput { pressKey(Key.Enter) }
        compose.runOnIdle { assertEquals(2, answers); assertEquals(1, listens); enabled = false }
        compose.onNodeWithTag("answer-0").performScrollTo().assertIsNotEnabled().performTouchInput { click() }
        compose.onNodeWithTag("speaker-0").performScrollTo().assertIsEnabled().performClick()
        compose.runOnIdle { assertEquals(2, answers); assertEquals(2, listens) }
    }
}
