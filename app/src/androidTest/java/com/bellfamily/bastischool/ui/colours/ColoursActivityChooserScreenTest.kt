package com.bellfamily.bastischool.ui.colours

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.bellfamily.bastischool.learning.models.ContentLanguage
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class ColoursActivityChooserScreenTest {
    @get:Rule val compose = createComposeRule()

    @Test fun englishChooserPrioritizesExploreAndEveryRealActivityOpensItsDestination() {
        val opened = mutableStateListOf<String>()
        compose.setContent {
            MaterialTheme {
                ColoursActivityChooserScreen(ContentLanguage.ENGLISH, true, false, false,
                    { opened += "explore" }, { opened += "find" }, { opened += "sort" }, {}, {}, Modifier.testTag("chooser"))
            }
        }
        compose.onNodeWithText("Colours").assertIsDisplayed()
        compose.onNodeWithTag("colours-open-explore").assertIsDisplayed().assertHasClickAction().performClick()
        compose.onNodeWithTag("colours-open-find").performScrollTo().assertIsDisplayed().performClick()
        compose.onNodeWithTag("colours-open-sort").performScrollTo().assertIsDisplayed().performClick()
        compose.runOnIdle { assertEquals(listOf("explore", "find", "sort"), opened) }
    }

    @Test fun germanChooserKeepsHomeAndPracticeControlsAvailable() {
        compose.setContent {
            MaterialTheme {
                ColoursActivityChooserScreen(ContentLanguage.GERMAN, true, false, false, {}, {}, {}, {}, {})
            }
        }
        compose.onNodeWithText("Farben").assertIsDisplayed()
        compose.onNodeWithText("Farben entdecken").assertIsDisplayed()
        compose.onNodeWithText("Finde die Farbe").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Farben sortieren").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("colours-chooser-home").performScrollTo().assertIsDisplayed()
    }
}
