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
import com.bellfamily.bastischool.learning.animals.HabitatExploreContent
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
        var undergroundOpened by mutableStateOf(false)
        compose.setContent {
            HabitatExploreChooserScreen(ContentLanguage.ENGLISH, art, false, art, false, art, false,
                onOcean = { opened = true }, onWoodland = { woodlandOpened = true },
                onUnderground = { undergroundOpened = true },
                onAnimals = {}, onHome = {})
        }
        compose.onNodeWithTag("habitat-ocean-choice").assertIsDisplayed().performClick()
        compose.runOnIdle { assertEquals(true, opened) }
        compose.onNodeWithTag("habitats-animals-back").assertIsDisplayed()
        compose.onNodeWithTag("habitats-home").assertIsDisplayed()
        compose.onNodeWithTag("habitat-woodland-choice").assertIsDisplayed().performClick()
        compose.runOnIdle { assertEquals(true, woodlandOpened) }
        compose.onNodeWithTag("habitat-underground-choice").assertIsDisplayed().performClick()
        compose.runOnIdle { assertEquals(true, undergroundOpened) }
        compose.onNodeWithText("Freshwater").assertDoesNotExist()
        compose.onNodeWithText("Underground").assertIsDisplayed()
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

    @Test fun undergroundImageTargetsAndLabeledAlternativesSelectThreeAnimalsWithoutColourOnlyFeedback() {
        var selected by mutableStateOf<ContentId?>(null)
        var replays = 0
        val animals = HabitatExploreContent.undergroundAnimals.associate { it.id to art }
        compose.setContent {
            UndergroundExploreScreen(ContentLanguage.ENGLISH, art, animals, false, selected, false,
                onAnimal = { selected = it }, onReplay = { replays++ }, onHabitats = {}, onHome = {})
        }
        compose.onNodeWithTag("underground-prompt").assertIsDisplayed()
        compose.onNodeWithTag("underground-fact-text").assertDoesNotExist()
        compose.onNodeWithTag("underground-scene").performTouchInput {
            click(Offset(width * .24f, height * .76f))
        }
        compose.onNodeWithTag("underground-hotspot-mole").assertIsSelected()
        compose.onNodeWithText("A mole lives mostly underground and digs tunnels.").assertIsDisplayed()
        compose.onNodeWithTag("underground-scene").performTouchInput {
            click(Offset(width * .54f, height * .47f))
        }
        compose.onNodeWithTag("underground-hotspot-earthworm").assertIsSelected()
        compose.onNodeWithTag("underground-hotspot-mole").assertIsNotSelected()
        compose.onNodeWithText("Earthworms move through soil.").assertIsDisplayed()
        compose.onNodeWithTag("underground-choice-rabbit").assertIsDisplayed().performClick()
        compose.onNodeWithTag("underground-hotspot-rabbit").assertIsSelected()
        compose.onNodeWithText("Wild rabbits can use underground warrens for shelter.").assertIsDisplayed()
        compose.onNodeWithTag("underground-replay").performClick()
        compose.runOnIdle { assertEquals(1, replays) }
        compose.onNodeWithTag("underground-habitats-back").assertIsDisplayed()
        compose.onNodeWithTag("underground-home").assertIsDisplayed()
    }

    @Test fun undergroundGermanLabelsAndFactsStayReachableAndSelectionIsLocalized() {
        var selected by mutableStateOf<ContentId?>(null)
        val animals = HabitatExploreContent.undergroundAnimals.associate { it.id to art }
        compose.setContent {
            UndergroundExploreScreen(ContentLanguage.GERMAN, art, animals, false, selected, false,
                onAnimal = { selected = it }, onReplay = {}, onHabitats = {}, onHome = {})
        }
        compose.onNodeWithTag("underground-choice-mole").performClick()
        compose.onNodeWithText("Ein Maulwurf lebt überwiegend unter der Erde und gräbt Gänge.").assertIsDisplayed()
        compose.onNodeWithTag("underground-choice-earthworm").performClick()
        compose.onNodeWithText("Regenwürmer bewegen sich durch den Boden.").assertIsDisplayed()
        compose.onNodeWithTag("underground-choice-rabbit").assertIsNotSelected()
        compose.onNodeWithTag("underground-choice-rabbit").performClick()
        compose.onNodeWithText("Wildkaninchen können unterirdische Baue als Schutz nutzen.").assertIsDisplayed()
        compose.onNodeWithTag("underground-choice-rabbit").assertIsSelected()
        compose.onNodeWithTag("underground-hotspot-rabbit").assertIsSelected()
    }
}
