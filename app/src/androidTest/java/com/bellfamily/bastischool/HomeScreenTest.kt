package com.bellfamily.bastischool

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.input.InputMode
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.LocalInputModeManager
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class HomeScreenTest(private val language: String) {
    @get:Rule val compose = createComposeRule()
    companion object {
        @JvmStatic @Parameterized.Parameters(name="{0}") fun cases() = listOf("en", "de")
    }

    @OptIn(ExperimentalTestApi::class, androidx.compose.ui.ExperimentalComposeUiApi::class)
    @Test fun everyLiveCardRetainsGroupingLabelsAndTouchAndKeyboardCallbacks() {
        val clicks = mutableListOf<HomeCard>()
        lateinit var requestKeyboard: () -> Boolean
        compose.setContent {
            val input = LocalInputModeManager.current
            requestKeyboard = { input.requestInputMode(InputMode.Keyboard) }
            MaterialTheme { NativeHome(language, PaddingValues(0.dp), false, clicks::add) }
        }
        val groups = homeCardGroups()
        val cards = groups.flatten()
        val titles = cards.map { if (language == "de") it.titleDe else it.titleEn }
        compose.onAllNodes(hasClickAction()).assertCountEquals(20)
        listOf("Dragon Treasure Hunt", "Dinosaur Rescue", "Memory Pairs", "Crocodile Snap",
            "Drachenschatz", "Dinosaurier-Rettung", "Paare merken", "Krokodil-Schnapp").forEach {
            compose.onNodeWithText(it).assertDoesNotExist()
        }
        val headings = if (language == "de") listOf("LERNEN", "ÜBEN", "SPIELEN") else listOf("LEARN", "PRACTICE", "PLAY")
        val expectedOrder = groups.flatMapIndexed { index, group ->
            listOf(headings[index]) + group.map { if (language == "de") it.titleDe else it.titleEn }
        }
        val orderMatcher = expectedOrder.map { hasText(it) }.reduce { a, b -> a or b }
        val actualOrder = compose.onAllNodes(orderMatcher, useUnmergedTree = true)
            .fetchSemanticsNodes().flatMap { it.config[SemanticsProperties.Text].map { text -> text.text } }
        assertEquals(expectedOrder, actualOrder)
        cards.forEachIndexed { index, card ->
            val node = compose.onNodeWithText(titles[index]).performScrollTo().assertIsDisplayed().assertHasClickAction()
            node.assertWidthIsAtLeast(48.dp).assertHeightIsAtLeast(48.dp)
            node.performTouchInput { click() }
            compose.runOnIdle { assertEquals(listOf(card), clicks); clicks.clear(); assertTrue(requestKeyboard()) }
            node.performSemanticsAction(SemanticsActions.RequestFocus) { assertTrue(it()) }
            node.assertIsFocused().performKeyInput { pressKey(Key.Enter) }
            compose.runOnIdle { assertEquals(listOf(card), clicks); clicks.clear() }
        }
    }
}
