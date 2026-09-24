package com.bellfamily.bastischool.ui.common

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class NativeSupportMessageTest {
    @get:Rule val compose = createComposeRule()

    @Test fun retryAndHintRetainSingleNonInteractiveTextSemantics() {
        val messages = listOf("Try again. The seasons already placed stay here.", "Look at the picture. Find spring.")
        compose.setContent { MaterialTheme { Column { messages.forEach { NativeSupportMessage(it) } } } }
        messages.forEach { text ->
            compose.onAllNodesWithText(text, useUnmergedTree = true).assertCountEquals(1)
            compose.onNodeWithText(text).assertIsDisplayed().assert(hasNoClickAction())
                .assert(SemanticsMatcher.keyNotDefined(SemanticsProperties.ContentDescription))
                .assert(SemanticsMatcher.keyNotDefined(SemanticsProperties.Focused))
                .assert(SemanticsMatcher.keyNotDefined(SemanticsProperties.LiveRegion))
        }
    }

    @Test fun longGermanTextGrowsAndRemainsScrollableAtLargeFont() {
        val text = "Versuche es noch einmal. Die eingeordneten Jahreszeiten bleiben hier. Schau dir die Bilder an. Du kannst dir helfen lassen."
        compose.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, 1.5f)) {
                MaterialTheme {
                    Column(Modifier.size(280.dp, 240.dp).verticalScroll(rememberScrollState())) {
                        NativeSupportMessage(text, Modifier.testTag("support"))
                        NativeActionButton("Noch einmal hören", NativeActionRole.SECONDARY, {}, Modifier.testTag("replay"))
                    }
                }
            }
        }
        val layouts = mutableListOf<TextLayoutResult>()
        compose.onNodeWithTag("support").assertTextEquals(text)
            .performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(layouts) }
        assertEquals(1, layouts.size)
        assertTrue(layouts.single().lineCount > 2)
        assertFalse(layouts.single().hasVisualOverflow)
        compose.onNodeWithTag("replay").performScrollTo().assertIsDisplayed().assertHeightIsAtLeast(56.dp)
    }
}
