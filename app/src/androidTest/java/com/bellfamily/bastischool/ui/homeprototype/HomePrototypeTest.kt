package com.bellfamily.bastischool.ui.homeprototype

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.InputMode
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInputModeManager
import androidx.compose.ui.semantics.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.*
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class HomePrototypeLayoutTest(private val german: Boolean, private val scale: Float, private val window: String) {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
    companion object {
        @JvmStatic @Parameterized.Parameters(name="de={0}-scale={1}-{2}")
        fun cases() = listOf(false, true).flatMap { de -> listOf(1f, 1.5f, 2f).flatMap { scale ->
            listOf("phone", "short", "tabletPortrait", "tabletLandscape").map { arrayOf<Any>(de, scale, it) }
        } }
    }
    private fun readable(label: String, scroll: Boolean = true) {
        val node = compose.onNodeWithText(label, useUnmergedTree = true)
        if (scroll) node.performScrollTo()
        node.assertIsDisplayed()
        val results = mutableListOf<TextLayoutResult>()
        node.performSemanticsAction(SemanticsActions.GetTextLayoutResult) { assertTrue(it(results)) }
        val layout = results.single()
        assertFalse("$label $window $scale: ${layout.size}; paragraph=${layout.multiParagraph.width}x${layout.multiParagraph.height}; " +
            "constraints=${layout.layoutInput.constraints}; widthOverflow=${layout.didOverflowWidth}; heightOverflow=${layout.didOverflowHeight}", layout.hasVisualOverflow)
        for (line in 0 until layout.lineCount) {
            assertFalse(layout.isLineEllipsized(line))
            assertTrue(layout.getLineRight(line) <= layout.size.width + 1)
            assertTrue(layout.getLineLeft(line) >= -1)
        }
        val bounds = node.fetchSemanticsNode().boundsInRoot
        assertTrue(bounds.width + 1 >= layout.size.width)
        assertTrue(bounds.height + 1 >= layout.size.height)
    }

    @OptIn(ExperimentalTestApi::class, androidx.compose.ui.ExperimentalComposeUiApi::class)
    @Test fun allCategoryLabelsAndPreviewNavigationWorkAcrossWindowAndFontMatrix() {
        val landscape = window in listOf("short", "tabletLandscape")
        compose.activity.requestedOrientation = if (landscape) ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE else ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        compose.waitUntil(10_000) { compose.activity.resources.configuration.orientation ==
            if (landscape) Configuration.ORIENTATION_LANDSCAPE else Configuration.ORIENTATION_PORTRAIT }
        val (width, height) = when(window) {
            "phone" -> 360 to 800; "short" -> 640 to 320; "tabletPortrait" -> 800 to 1100; else -> 1100 to 800
        }
        lateinit var keyboard: () -> Boolean
        compose.setContent {
            val input = LocalInputModeManager.current
            keyboard = { input.requestInputMode(InputMode.Keyboard) }
            // Controlled Compose viewport, not a claim of physical tablet testing.
            CompositionLocalProvider(LocalDensity provides Density(1f, scale)) {
                Box(Modifier.size(width.dp, height.dp)) { HomePrototype(initialGerman = german) }
            }
        }
        if (window == "short" && scale == 1f) {
            // The first category label must be visible before scrolling past any welcome content.
            compose.onNodeWithText(PrototypeCategory.MATHS.label(german), useUnmergedTree = true).assertIsDisplayed()
        }
        val settings = compose.onNodeWithTag("prototype-settings").performScrollTo()
            .assertIsDisplayed().assertHasClickAction().assertHeightIsAtLeast(56.dp).assertWidthIsAtLeast(56.dp)
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button))
        readable(if (german) "Erwachsene" else "Grown-ups")
        compose.runOnIdle { assertTrue(keyboard()) }
        settings.performSemanticsAction(SemanticsActions.RequestFocus) { assertTrue(it()) }
        settings.assertIsFocused().performKeyInput { pressKey(Key.Enter) }
        compose.onNodeWithTag("prototype-language-${if (german) "de" else "en"}").assertIsSelected()
        readable(if (german) "Zurück" else "Back", scroll = false)
        readable(if (german) "Startseite" else "Home", scroll = false)
        compose.onNodeWithTag("prototype-back").assertHeightIsAtLeast(56.dp).performClick()
        readable(if (german) "Was wollen wir entdecken?" else "What shall we explore?")
        PrototypeCategory.entries.forEach { category ->
            val card = compose.onNodeWithTag("prototype-category-${category.name}").performScrollTo()
                .assertIsDisplayed().assertHasClickAction().assertHeightIsAtLeast(56.dp).assertWidthIsAtLeast(56.dp)
                .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button))
            readable(category.label(german))
            if (category == PrototypeCategory.MATHS) {
                compose.runOnIdle { assertTrue(keyboard()) }
                card.performSemanticsAction(SemanticsActions.RequestFocus) { assertTrue(it()) }
                card.assertIsFocused().performKeyInput { pressKey(Key.Enter) }
            } else card.performTouchInput { click() }
            readable(category.label(german))
            category.activityIds.forEach { id ->
                val activity = prototypeActivity(id)
                readable(if (german) activity.titleDe else activity.titleEn)
                readable(if (german) activity.descriptionDe else activity.descriptionEn)
            }
            compose.onNodeWithTag("prototype-activity-${category.activityIds.first()}").performScrollTo().performClick()
            compose.onNodeWithTag("prototype-detail-notice").performScrollTo().assertIsDisplayed()
            compose.onNodeWithTag("prototype-back").assertIsDisplayed().assertHeightIsAtLeast(56.dp).performClick()
            compose.onNodeWithTag("prototype-home").assertIsDisplayed().assertHeightIsAtLeast(56.dp).performClick()
        }
        compose.onNodeWithTag("prototype-all").performScrollTo().performClick()
        prototypeActivities().forEach { readable(if (german) it.titleDe else it.titleEn) }
        compose.onNodeWithTag("prototype-activity-mixed").performScrollTo().performClick()
        compose.onNodeWithTag("prototype-detail-notice").performScrollTo().assertIsDisplayed()
    }
}

class HomePrototypeNavigationTest {
    @get:Rule val compose = createAndroidComposeRule<HomePrototypeActivity>()

    @OptIn(ExperimentalTestApi::class)
    @Test fun headerKeyboardOrderIsBackThenHomeAndActivationKeepsHierarchy() {
        compose.onNodeWithTag("prototype-category-WORDS").performScrollTo().performClick()
        // Enter keyboard mode through the Android host, as a real keyboard does after touch.
        InstrumentationRegistry.getInstrumentation().sendKeyDownUpSync(android.view.KeyEvent.KEYCODE_TAB)
        val back = compose.onNodeWithTag("prototype-back")
        back.performSemanticsAction(SemanticsActions.RequestFocus) { assertTrue(it()) }
        back.assertIsFocused().performKeyInput { pressKey(Key.Tab) }
        val home = compose.onNodeWithTag("prototype-home").assertIsFocused()
        home.performKeyInput { pressKey(Key.Enter) }
        compose.onNodeWithTag("prototype-settings").assertExists()
        compose.onNodeWithTag("prototype-back").assertDoesNotExist()
    }

    @Test fun everyActivityPreviewsAndBackReturnsToAllWithoutProductionLaunch() {
        compose.onNodeWithTag("prototype-all").performScrollTo().performClick()
        prototypeActivities().forEach { card ->
            compose.onNodeWithTag("prototype-activity-${card.prototypeId()}").performScrollTo().performClick()
            compose.onNodeWithTag("prototype-detail-notice").performScrollTo().assertIsDisplayed()
            compose.onNodeWithTag("prototype-back").performClick()
            compose.onNodeWithTag("prototype-activity-${card.prototypeId()}").assertIsDisplayed()
        }
    }

    @Test fun languageSelectionAndNestedNavigationSurviveRecreationAndSystemBack() {
        compose.onNodeWithTag("prototype-settings").performScrollTo().performClick()
        compose.onNodeWithTag("prototype-language-de").performScrollTo().performClick().assertIsSelected()
        compose.onNodeWithTag("prototype-home").performClick()
        compose.onNodeWithTag("prototype-category-TIME").performScrollTo().performClick()
        compose.onNodeWithTag("prototype-activity-time").performScrollTo().performClick()
        compose.activityRule.scenario.recreate()
        compose.onNodeWithText("Tage & Jahreszeiten").assertExists()
        compose.onNodeWithTag("prototype-detail-notice").performScrollTo().assertIsDisplayed()
        compose.runOnIdle { compose.activity.onBackPressedDispatcher.onBackPressed() }
        compose.onNodeWithText("Zeit & Jahreszeiten").assertExists()
        compose.runOnIdle { compose.activity.onBackPressedDispatcher.onBackPressed() }
        compose.onNodeWithTag("prototype-category-TIME").assertIsDisplayed()
    }
}
