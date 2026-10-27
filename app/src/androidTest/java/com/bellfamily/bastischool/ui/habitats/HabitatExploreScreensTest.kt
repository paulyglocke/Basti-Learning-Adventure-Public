package com.bellfamily.bastischool.ui.habitats

import android.graphics.Bitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.bellfamily.bastischool.learning.models.ContentLanguage
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class HabitatExploreScreensTest {
    @get:Rule val compose = createComposeRule()
    private val art = Bitmap.createBitmap(144, 108, Bitmap.Config.ARGB_8888).asImageBitmap()

    @Test fun oceanStartsSilentAndDolphinHotspotSelectsAndHighlightsTheSemanticAnimal() {
        var selected by mutableStateOf(false)
        var replays = 0
        compose.setContent {
            OceanExploreScreen(ContentLanguage.ENGLISH, art, false, selected, false,
                onDolphin = { selected = true }, onReplay = { replays++ }, onHabitats = {}, onHome = {})
        }
        compose.onNodeWithTag("ocean-prompt").assertIsDisplayed()
        compose.onNodeWithTag("ocean-dolphin-fact").assertDoesNotExist()
        compose.onNodeWithTag("ocean-dolphin-hotspot").assertIsNotSelected().performClick()
        compose.runOnIdle { assertEquals(true, selected) }
        compose.onNodeWithTag("ocean-dolphin-fact").assertIsDisplayed()
        compose.onNodeWithText("Dolphins live in the sea.").assertIsDisplayed()
        compose.onNodeWithTag("ocean-dolphin-choice").assertIsDisplayed().performClick()
        compose.onNodeWithTag("ocean-dolphin-hotspot").assertIsSelected()
        compose.onNodeWithTag("ocean-replay").assertIsDisplayed().performClick()
        compose.runOnIdle { assertEquals(1, replays) }
    }

    @Test fun germanOceanNamesAndFactRemainAvailableThroughLabeledControl() {
        var selected by mutableStateOf(false)
        compose.setContent {
            OceanExploreScreen(ContentLanguage.GERMAN, art, false, selected, false,
                onDolphin = { selected = true }, onReplay = {}, onHabitats = {}, onHome = {})
        }
        compose.onNodeWithTag("ocean-title").assertIsDisplayed()
        compose.onNodeWithText("Delfin", useUnmergedTree = true).assertIsDisplayed().performClick()
        compose.onNodeWithText("Delfine leben im Meer.").assertIsDisplayed()
        compose.onNodeWithTag("ocean-habitats-back").assertIsDisplayed()
        compose.onNodeWithTag("ocean-home").assertIsDisplayed()
    }

    @Test fun habitatChooserOffersOnlyWorkingOceanAndProvidesParentAndHomeControls() {
        var opened by mutableStateOf(false)
        compose.setContent {
            HabitatExploreChooserScreen(ContentLanguage.ENGLISH, art, false, onOcean = { opened = true },
                onAnimals = {}, onHome = {})
        }
        compose.onNodeWithTag("habitat-ocean-choice").assertIsDisplayed().performClick()
        compose.runOnIdle { assertEquals(true, opened) }
        compose.onNodeWithTag("habitats-animals-back").assertIsDisplayed()
        compose.onNodeWithTag("habitats-home").assertIsDisplayed()
        compose.onNodeWithText("Woodland").assertDoesNotExist()
        compose.onNodeWithText("Freshwater").assertDoesNotExist()
        compose.onNodeWithText("Underground").assertDoesNotExist()
    }
}
