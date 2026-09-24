package com.bellfamily.bastischool.ui.common

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Bitmap
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.input.InputMode
import androidx.compose.ui.input.InputModeManager
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInputModeManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.bellfamily.bastischool.learning.models.ContentLanguage
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import java.io.File

class NativeActionButtonTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

    @Test fun allRolesExposeButtonSemanticsTargetsAndRespectDisabledState() {
        var enabled by mutableStateOf(true)
        val calls = IntArray(3)
        compose.setContent { MaterialTheme { Column {
            NativeActionRole.entries.forEachIndexed { index, role ->
                NativeActionButton(role.name, role, { calls[index]++ }, Modifier.testTag(role.name), enabled)
            }
        } } }
        NativeActionRole.entries.forEach { role ->
            compose.onNodeWithTag(role.name).assertTextEquals(role.name).assertIsEnabled()
                .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button))
                .assertHeightIsAtLeast(56.dp).assertWidthIsAtLeast(48.dp).performClick()
        }
        compose.runOnIdle { assertArrayEquals(intArrayOf(1,1,1), calls); enabled = false }
        NativeActionRole.entries.forEach { role ->
            compose.onNodeWithTag(role.name).assertIsNotEnabled().performTouchInput { click() }
        }
        compose.runOnIdle { assertArrayEquals(intArrayOf(1,1,1), calls) }
    }

    @OptIn(ExperimentalTestApi::class, androidx.compose.ui.ExperimentalComposeUiApi::class)
    @Test fun eachRoleSupportsKeyboardFocusAndActivation() {
        lateinit var input: InputModeManager
        val calls = IntArray(3)
        compose.setContent { input = LocalInputModeManager.current; MaterialTheme { Column {
            NativeActionRole.entries.forEachIndexed { i, role ->
                NativeActionButton(role.name, role, { calls[i]++ }, Modifier.testTag(role.name))
            }
        } } }
        compose.runOnIdle { assertTrue(input.requestInputMode(InputMode.Keyboard)) }
        NativeActionRole.entries.forEach { role ->
            compose.onNodeWithTag(role.name).performSemanticsAction(SemanticsActions.RequestFocus) { it() }
            compose.onNodeWithTag(role.name).assertIsFocused().performKeyInput { pressKey(Key.Enter) }
        }
        compose.runOnIdle { assertArrayEquals(intArrayOf(1,1,1), calls) }
    }

    private fun completion(landscape: Boolean) {
        if (landscape) {
            compose.activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            compose.waitUntil(10_000) { compose.activity.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE }
        }
        var ready by mutableStateOf(true)
        var home = 0
        compose.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, 1.5f)) {
                MaterialTheme(colorScheme = lightColorScheme(primary = Color(0xFF2E7D5B), secondary = Color(0xFFE59D28))) {
                    Box(Modifier.size(if (landscape) 700.dp else 320.dp, if (landscape) 240.dp else 480.dp).testTag("review")) {
                        NativeCompletionScreen("roles", ContentLanguage.GERMAN, "Geschafft!", ready, "review-", {}, {}, {home++}, {})
                    }
                }
            }
        }
        listOf("again", "home", "replay").forEach { tag ->
            compose.onNodeWithTag("review-$tag").assertIsDisplayed().assertHeightIsAtLeast(56.dp)
                .assertWidthIsAtLeast(48.dp)
        }
        // Check actual text layout, not just a partially visible semantic bounding box.
        listOf("Nochmal spielen", "Zurück zum Start", "Noch einmal hören").forEach { label ->
            val results = mutableListOf<androidx.compose.ui.text.TextLayoutResult>()
            compose.onNodeWithText(label).performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(results) }
            assertTrue(results.isNotEmpty())
            assertTrue("$label: ${results.map { "size=${it.size}, lines=${it.lineCount}, width=${it.didOverflowWidth}, height=${it.didOverflowHeight}, paragraph=${it.multiParagraph.width}, right=${it.getLineRight(0)}" }}", results.none { it.hasVisualOverflow })
        }
        val image = compose.onNodeWithTag("review").captureToImage().asAndroidBitmap()
        File(compose.activity.cacheDir, "action-${if(landscape) "landscape" else "portrait"}.png").outputStream().use {
            image.compress(Bitmap.CompressFormat.PNG, 100, it)
        }
        compose.runOnIdle { ready = false }
        compose.onNodeWithTag("review-again").assertIsNotEnabled()
        compose.onNodeWithTag("review-replay").assertIsNotEnabled()
        compose.onNodeWithTag("review-home").assertIsEnabled().performClick()
        compose.runOnIdle { assertEquals(1, home) }
    }
    @Test fun germanLargeFontPortraitKeepsLabelsAndActions() = completion(false)
    @Test fun germanLargeFontShortLandscapeKeepsLabelsAndActions() = completion(true)
}
