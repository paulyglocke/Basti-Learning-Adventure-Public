package com.bellfamily.bastischool.learning.animals

import com.bellfamily.bastischool.learning.models.ContentId
import com.bellfamily.bastischool.learning.models.ContentLanguage
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test

class HabitatExploreContentTest {
    @Test fun oceanAndDolphinKeepStableCanonicalSemanticIdentities() {
        assertEquals("habitat.ocean", HabitatExploreContent.ocean.value)
        assertEquals("animal.dolphin", HabitatExploreContent.dolphin.value)
        assertEquals("SceneDescriptions/ocean_underwater/canonical/ocean_001_reef_actions.png",
            HabitatExploreContent.oceanScene)
    }

    @Test fun oceanNameAndReviewedFactAreBilingualAndIndependentOfSelectionState() {
        assertEquals("Ocean", HabitatExploreContent.oceanTitle.display[ContentLanguage.ENGLISH])
        assertEquals("Meer", HabitatExploreContent.oceanTitle.display[ContentLanguage.GERMAN])
        assertEquals("Dolphin", HabitatExploreContent.dolphinName.display[ContentLanguage.ENGLISH])
        assertEquals("Delfin", HabitatExploreContent.dolphinName.display[ContentLanguage.GERMAN])
        assertEquals("Dolphins live in the sea.", HabitatExploreContent.dolphinFact.display[ContentLanguage.ENGLISH])
        assertEquals("Delfine leben im Meer.", HabitatExploreContent.dolphinFact.display[ContentLanguage.GERMAN])
    }

    @Test fun semanticSelectionIsLanguageIndependentAndHasNoAssessedAnswerState() {
        val initial = HabitatExploreState()
        assertNull(initial.selectedAnimal)
        val selected = HabitatExploreReducer.reduce(initial, HabitatExploreAction.Select(HabitatExploreContent.dolphin))
        assertEquals(HabitatExploreContent.dolphin, selected.selectedAnimal)
        assertThrows(IllegalArgumentException::class.java) {
            HabitatExploreReducer.reduce(initial, HabitatExploreAction.Select(ContentId("animal.whale")))
        }
    }

    @Test fun woodlandUsesApprovedSceneAndStableRabbitAndFoxIdentities() {
        assertEquals("habitat.woodland", HabitatExploreContent.woodland.value)
        assertEquals("animal.rabbit", HabitatExploreContent.rabbit.value)
        assertEquals("animal.fox", HabitatExploreContent.fox.value)
        assertEquals("SceneDescriptions/woodland_forest/canonical/woodland_008_homes_choices.png",
            HabitatExploreContent.woodlandScene)
        assertEquals(listOf(HabitatExploreContent.rabbit, HabitatExploreContent.fox),
            HabitatExploreContent.woodlandAnimals.map { it.id })
        assertEquals("Woodland", HabitatExploreContent.woodlandTitle.display[ContentLanguage.ENGLISH])
        assertEquals("Wald", HabitatExploreContent.woodlandTitle.display[ContentLanguage.GERMAN])
    }

    @Test fun woodlandAnimalNamesFactsAndUtterancesMatchTheReviewedBilingualCatalogue() {
        assertEquals("Rabbit", HabitatExploreContent.rabbitName.display[ContentLanguage.ENGLISH])
        assertEquals("Kaninchen", HabitatExploreContent.rabbitName.display[ContentLanguage.GERMAN])
        assertEquals("Wild rabbits can use underground warrens for shelter.",
            HabitatExploreContent.rabbitFact.display[ContentLanguage.ENGLISH])
        assertEquals("Wildkaninchen können unterirdische Baue als Schutz nutzen.",
            HabitatExploreContent.rabbitFact.display[ContentLanguage.GERMAN])
        assertEquals("Fox", HabitatExploreContent.foxName.display[ContentLanguage.ENGLISH])
        assertEquals("Fuchs", HabitatExploreContent.foxName.display[ContentLanguage.GERMAN])
        assertEquals("Foxes live in many habitats. A fox may use an underground den to raise its cubs.",
            HabitatExploreContent.foxFact.display[ContentLanguage.ENGLISH])
        assertEquals("Füchse leben in vielen Lebensräumen. Ein Fuchs kann einen unterirdischen Bau nutzen, um seine Jungen aufzuziehen.",
            HabitatExploreContent.foxFact.display[ContentLanguage.GERMAN])
        assertEquals("Rabbit. Wild rabbits can use underground warrens for shelter.",
            HabitatExploreContent.woodlandAnimal(HabitatExploreContent.rabbit)?.utterance?.display?.get(ContentLanguage.ENGLISH))
        assertEquals("Fuchs. Füchse leben in vielen Lebensräumen. Ein Fuchs kann einen unterirdischen Bau nutzen, um seine Jungen aufzuziehen.",
            HabitatExploreContent.woodlandAnimal(HabitatExploreContent.fox)?.utterance?.display?.get(ContentLanguage.GERMAN))
    }

    @Test fun woodlandNormalizedImageTargetsAreValidSeparatedAndScaleFromSourceCoordinates() {
        val rabbit = HabitatExploreContent.woodlandAnimal(HabitatExploreContent.rabbit)!!.target
        val fox = HabitatExploreContent.woodlandAnimal(HabitatExploreContent.fox)!!.target
        assertEquals(0.39f, rabbit.left)
        assertEquals(0.46f, rabbit.top)
        assertEquals(0.55f, rabbit.right)
        assertEquals(0.69f, rabbit.bottom)
        assertEquals(0.73f, fox.left)
        assertEquals(0.48f, fox.top)
        assertEquals(0.91f, fox.right)
        assertEquals(0.75f, fox.bottom)
        assertTrue("Rabbit and Fox touch regions must not overlap", rabbit.right < fox.left)
        assertFalse(HabitatExploreContent.woodlandScene == HabitatExploreContent.oceanScene)
    }

    @Test fun woodlandReducerChangesAnimalWithoutLanguageOrScoringStateAndRejectsUnknownTargets() {
        val empty = WoodlandExploreState()
        assertNull(empty.selectedAnimal)
        val rabbit = WoodlandExploreReducer.reduce(empty, WoodlandExploreAction.Select(HabitatExploreContent.rabbit))
        val fox = WoodlandExploreReducer.reduce(rabbit, WoodlandExploreAction.Select(HabitatExploreContent.fox))
        assertEquals(HabitatExploreContent.rabbit, rabbit.selectedAnimal)
        assertEquals(HabitatExploreContent.fox, fox.selectedAnimal)
        assertThrows(IllegalArgumentException::class.java) {
            WoodlandExploreReducer.reduce(fox, WoodlandExploreAction.Select(ContentId("animal.owl")))
        }
        assertThrows(IllegalArgumentException::class.java) {
            WoodlandExploreReducer.reduce(fox, WoodlandExploreAction.Select(HabitatExploreContent.dolphin))
        }
    }
}
