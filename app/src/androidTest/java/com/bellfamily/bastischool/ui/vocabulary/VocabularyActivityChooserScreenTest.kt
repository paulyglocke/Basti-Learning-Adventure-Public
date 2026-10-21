package com.bellfamily.bastischool.ui.vocabulary

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.bellfamily.bastischool.learning.models.ContentLanguage
import com.bellfamily.bastischool.learning.vocabulary.VocabularyPhase
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class VocabularyActivityChooserScreenTest {
    @get:Rule val compose = createComposeRule()

    @Test fun englishChooserShowsAllOriginalModesAndEachOpensItsActualPhase() {
        val opened = mutableStateListOf<VocabularyPhase>()
        compose.setContent {
            MaterialTheme {
                VocabularyActivityChooserScreen(ContentLanguage.ENGLISH, emptyMap(), true, false, false,
                    onChoose = opened::add, onRetry = {}, onHome = {}, modifier = Modifier.testTag("chooser"))
            }
        }
        compose.onNodeWithText("Vocabulary Booster").assertIsDisplayed()
        compose.onNodeWithText("Explore words").assertIsDisplayed()
        compose.onNodeWithText("Find the word").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("What is it?").performScrollTo().assertIsDisplayed()
        listOf(VocabularyPhase.EXPLORE, VocabularyPhase.FIND, VocabularyPhase.NAME).forEach { phase ->
            compose.onNodeWithTag("vocabulary-open-${phase.name}").performScrollTo().assertIsEnabled().performClick()
            compose.runOnIdle { assertEquals(phase, opened.last()) }
        }
    }

    @Test fun germanChooserWrapsAllThreeLabelsAndHomeStaysAvailable() {
        val opened = mutableStateListOf<VocabularyPhase>()
        compose.setContent {
            MaterialTheme {
                VocabularyActivityChooserScreen(ContentLanguage.GERMAN, emptyMap(), true, false, false,
                    onChoose = opened::add, onRetry = {}, onHome = {}, modifier = Modifier)
            }
        }
        compose.onNodeWithText("Wortschatz").assertIsDisplayed()
        compose.onNodeWithText("Wörter entdecken").assertIsDisplayed()
        compose.onNodeWithText("Finde das Wort").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Was ist das?").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("vocabulary-chooser-home").performScrollTo().assertIsDisplayed()
    }
}
