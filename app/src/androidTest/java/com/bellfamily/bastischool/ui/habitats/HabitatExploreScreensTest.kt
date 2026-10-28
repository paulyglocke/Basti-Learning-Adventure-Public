package com.bellfamily.bastischool.ui.habitats

import android.graphics.Bitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import com.bellfamily.bastischool.learning.models.ContentLanguage
import com.bellfamily.bastischool.learning.models.ContentId
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

    @Test fun habitatChooserOffersWorkingOceanAndWoodlandAndProvidesParentAndHomeControls() {
        var opened by mutableStateOf(false)
        var woodlandOpened by mutableStateOf(false)
        compose.setContent {
            HabitatExploreChooserScreen(ContentLanguage.ENGLISH, art, false, art, false,
                onOcean = { opened = true }, onWoodland = { woodlandOpened = true },
                onAnimals = {}, onHome = {})
        }
        compose.onNodeWithTag("habitat-ocean-choice").assertIsDisplayed().performClick()
        compose.runOnIdle { assertEquals(true, opened) }
        compose.onNodeWithTag("habitats-animals-back").assertIsDisplayed()
        compose.onNodeWithTag("habitats-home").assertIsDisplayed()
        compose.onNodeWithTag("habitat-woodland-choice").assertIsDisplayed().performClick()
        compose.runOnIdle { assertEquals(true, woodlandOpened) }
        compose.onNodeWithText("Freshwater").assertDoesNotExist()
        compose.onNodeWithText("Underground").assertDoesNotExist()
    }

    @Test fun woodlandImageRelativeTargetsSelectRabbitAndFoxAndReplaceCurrentFact() {
        var selected by mutableStateOf<ContentId?>(null)
        var replays = 0
        compose.setContent {
            WoodlandExploreScreen(ContentLanguage.ENGLISH, art, false, selected, false,
                onAnimal = { selected = it }, onReplay = { replays++ }, onHabitats = {}, onHome = {})
        }
        compose.onNodeWithTag("woodland-title").assertIsDisplayed()
        compose.onNodeWithTag("woodland-prompt").assertIsDisplayed()
        compose.onNodeWithTag("woodland-fact-text").assertDoesNotExist()
        compose.onNodeWithTag("woodland-scene").performTouchInput {
            click(Offset(width * .45f, height * .67f))
        }
        compose.onNodeWithText("Wild rabbits can use underground warrens for shelter.").assertIsDisplayed()
        compose.onNodeWithTag("woodland-hotspot-rabbit").assertIsSelected()
        compose.onNodeWithTag("woodland-hotspot-fox").assertIsNotSelected()

        compose.onNodeWithTag("woodland-scene").performTouchInput {
            click(Offset(width * .80f, height * .67f))
        }
        compose.onNodeWithText("Foxes live in many habitats. A fox may use an underground den to raise its cubs.")
            .assertIsDisplayed()
        compose.onNodeWithText("Wild rabbits can use underground warrens for shelter.").assertDoesNotExist()
        compose.onNodeWithTag("woodland-hotspot-rabbit").assertIsNotSelected()
        compose.onNodeWithTag("woodland-hotspot-fox").assertIsSelected()
        compose.onNodeWithTag("woodland-replay").performClick()
        compose.runOnIdle { assertEquals(1, replays) }
        compose.onNodeWithTag("woodland-choice-rabbit").performClick()
        compose.onNodeWithText("Wild rabbits can use underground warrens for shelter.").assertIsDisplayed()
        compose.onNodeWithTag("woodland-choice-rabbit").assertIsSelected()
    }

    @Test fun woodlandGermanFactsAndLabeledAnimalAlternativesUpdateTogether() {
        var selected by mutableStateOf<ContentId?>(null)
        compose.setContent {
            WoodlandExploreScreen(ContentLanguage.GERMAN, art, false, selected, false,
                onAnimal = { selected = it }, onReplay = {}, onHabitats = {}, onHome = {})
        }
        compose.onNodeWithTag("woodland-choice-fox").assertIsDisplayed().performClick()
        compose.onNodeWithText("Füchse leben in vielen Lebensräumen. Ein Fuchs kann einen unterirdischen Bau nutzen, um seine Jungen aufzuziehen.")
            .assertIsDisplayed()
        compose.onNodeWithTag("woodland-choice-fox").assertIsSelected()
        compose.onNodeWithTag("woodland-choice-rabbit").performClick()
        compose.onNodeWithText("Wildkaninchen können unterirdische Baue als Schutz nutzen.").assertIsDisplayed()
        compose.onNodeWithTag("woodland-hotspot-rabbit").assertIsSelected()
        compose.onNodeWithTag("woodland-habitats-back").assertIsDisplayed()
        compose.onNodeWithTag("woodland-home").assertIsDisplayed()
    }
}
