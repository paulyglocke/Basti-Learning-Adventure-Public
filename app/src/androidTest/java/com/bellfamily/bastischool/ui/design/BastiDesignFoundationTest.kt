package com.bellfamily.bastischool.ui.design

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Bitmap
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.input.InputMode
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInputModeManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.io.File

@RunWith(Parameterized::class)
class BastiDesignFoundationTest(private val german: Boolean, private val scale: Float, private val landscape: Boolean) {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
    companion object {
        @JvmStatic @Parameterized.Parameters(name="de={0}-scale={1}-landscape={2}")
        fun cases() = listOf(false, true).flatMap { de -> listOf(1f, 1.5f, 2f).flatMap { scale ->
            listOf(false, true).map { arrayOf<Any>(de, scale, it) }
        } }
    }

    private fun readable(node: SemanticsNodeInteraction) {
        node.performScrollTo().assertIsDisplayed()
        val results = mutableListOf<TextLayoutResult>()
        node.performSemanticsAction(SemanticsActions.GetTextLayoutResult) { assertTrue(it(results)) }
        val result = results.single()
        if (result.hasVisualOverflow) save("overflow")
        assertFalse("Text '${result.layoutInput.text}': size=${result.size}, lines=${result.lineCount}, " +
            "widthOverflow=${result.didOverflowWidth}, heightOverflow=${result.didOverflowHeight}, " +
            "paragraphWidth=${result.multiParagraph.width}, right=${result.getLineRight(0)}, " +
            "constraints=${result.layoutInput.constraints}", result.hasVisualOverflow)
        val bounds = node.fetchSemanticsNode().boundsInRoot
        assertTrue(bounds.width + 1 >= result.size.width)
        assertTrue(bounds.height + 1 >= result.size.height)
        for (line in 0 until result.lineCount) {
            assertFalse(result.isLineEllipsized(line))
            assertTrue(result.getLineLeft(line) >= -1)
            assertTrue(result.getLineRight(line) <= result.size.width + 1)
        }
    }

    @OptIn(ExperimentalTestApi::class, androidx.compose.ui.ExperimentalComposeUiApi::class)
    @Test fun samplesWrapAndRetainAccessibleSelectionAndActionSemantics() {
        compose.activity.requestedOrientation = if (landscape) ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE else ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        compose.waitUntil(10_000) { compose.activity.resources.configuration.orientation ==
            if (landscape) Configuration.ORIENTATION_LANDSCAPE else Configuration.ORIENTATION_PORTRAIT }
        var page by mutableStateOf("categories")
        lateinit var keyboard: () -> Boolean
        compose.setContent {
            val input = LocalInputModeManager.current
            keyboard = { input.requestInputMode(InputMode.Keyboard) }
            CompositionLocalProvider(LocalDensity provides Density(LocalDensity.current.density, scale)) {
                BastiDesignTheme {
                    Surface(color = BastiPalette.background,
                        modifier = Modifier.size(if (landscape) 640.dp else 360.dp, if (landscape) 320.dp else 800.dp)) {
                        key(page) { BastiDesignShowcase(german, Modifier.fillMaxSize(), page) }
                    }
                }
            }
        }
        compose.onNodeWithTag("category-MATHS").assertIsSelected()
        BastiCategoryTreatment.entries.forEach { treatment ->
            val tag = "category-${treatment.name}"
            val card = compose.onNodeWithTag(tag).performScrollTo().assertIsDisplayed()
                .assertHeightIsAtLeast(56.dp).assertWidthIsAtLeast(48.dp)
                .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button))
            readable(compose.onNodeWithText(demoCategoryLabel(treatment, german), useUnmergedTree = true))
            if (treatment == BastiCategoryTreatment.WORDS) {
                card.performTouchInput { click() }
                card.assertIsSelected()
                compose.onNodeWithTag("category-MATHS").assertIsNotSelected()
            }
            if (treatment == BastiCategoryTreatment.ANIMALS) {
                compose.runOnIdle { assertTrue(keyboard()) }
                card.performSemanticsAction(SemanticsActions.RequestFocus) { assertTrue(it()) }
                card.assertIsFocused().performKeyInput { pressKey(Key.Enter) }
                card.assertIsSelected()
            }
            if (treatment in listOf(BastiCategoryTreatment.MATHS, BastiCategoryTreatment.TIME, BastiCategoryTreatment.EVERYDAY)) save(treatment.name)
        }
        compose.runOnIdle { page = "type" }
        for (tag in listOf("type-0", "type-1", "type-2", "type-3", "type-4", "type-support", "type-parent")) {
            readable(compose.onNodeWithTag(tag))
        }
        compose.onNodeWithTag("type-0").performScrollTo()
        save("type")
        compose.runOnIdle { page = "actions" }
        listOf("primary", "secondary", "navigation").forEachIndexed { index, role ->
            val button = compose.onNodeWithTag("demo-$role").performScrollTo().assertIsEnabled()
                .assertHeightIsAtLeast(56.dp).assertHasClickAction()
            val label = button.fetchSemanticsNode().config[SemanticsProperties.Text].single().text
            readable(compose.onNodeWithText(label, useUnmergedTree = true))
            if (index == 0) button.performTouchInput { click() } else {
                compose.runOnIdle { assertTrue(keyboard()) }
                button.performSemanticsAction(SemanticsActions.RequestFocus) { assertTrue(it()) }
                button.assertIsFocused().performKeyInput { pressKey(Key.Enter) }
            }
        }
        compose.onNodeWithTag("demo-disabled").performScrollTo().assertIsNotEnabled().performTouchInput { click() }
        compose.onNodeWithTag("demo-count").assertTextEquals(if (german) "Beispielbetätigungen: 3" else "Demo activations: 3")
        compose.onNodeWithTag("demo-primary").performScrollTo()
        save("actions")
    }

    private fun save(part: String) {
        val bitmap = compose.onRoot().captureToImage().asAndroidBitmap()
        File(compose.activity.getExternalFilesDir(null), "foundation-${if (german) "de" else "en"}-$scale-$landscape-$part.png")
            .outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }
}

class BastiDesignIsolationTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

    @Test fun optInThemeDoesNotLeakAndDisabledCategoryRetainsLabelWithoutActivation() {
        var calls = 0
        compose.setContent {
            MaterialTheme {
                val original = MaterialTheme.colorScheme
                BastiDesignTheme {
                    assertEquals(BastiPalette.background, MaterialTheme.colorScheme.background)
                    BastiCategoryCard("Disabled sample", ColorPainter(BastiPalette.surface), BastiCategoryTreatment.MATHS,
                        { calls++ }, Modifier.testTag("disabled"), enabled = false)
                }
                assertSame(original, MaterialTheme.colorScheme)
            }
        }
        compose.onNodeWithTag("disabled").assertTextEquals("Disabled sample").assertIsNotEnabled().performTouchInput { click() }
        compose.runOnIdle { assertEquals(0, calls) }
    }
}
