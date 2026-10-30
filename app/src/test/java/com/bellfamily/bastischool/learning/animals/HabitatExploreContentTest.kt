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

    @Test fun freshwaterUsesStableFrogAndDuckIdentitiesApprovedFactsAndOfflineAssetMapping() {
        assertEquals("habitat.freshwater", HabitatExploreContent.freshwater.value)
        assertEquals("animal.frog", HabitatExploreContent.frog.value)
        assertEquals("animal.duck", HabitatExploreContent.duck.value)
        assertEquals("Freshwater", HabitatExploreContent.freshwaterTitle.display[ContentLanguage.ENGLISH])
        assertEquals("Süßwasser", HabitatExploreContent.freshwaterTitle.display[ContentLanguage.GERMAN])
        assertEquals("Frog", HabitatExploreContent.frogName.display[ContentLanguage.ENGLISH])
        assertEquals("Frosch", HabitatExploreContent.frogName.display[ContentLanguage.GERMAN])
        assertEquals("Many frogs lay eggs in water and also use damp places on land.",
            HabitatExploreContent.frogFact.display[ContentLanguage.ENGLISH])
        assertEquals("Viele Frösche legen ihre Eier im Wasser ab und nutzen auch feuchte Plätze an Land.",
            HabitatExploreContent.frogFact.display[ContentLanguage.GERMAN])
        assertEquals("Duck", HabitatExploreContent.duckName.display[ContentLanguage.ENGLISH])
        assertEquals("Ente", HabitatExploreContent.duckName.display[ContentLanguage.GERMAN])
        assertEquals("Many ducks swim on ponds and lakes.",
            HabitatExploreContent.duckFact.display[ContentLanguage.ENGLISH])
        assertEquals("Viele Enten schwimmen auf Teichen und Seen.",
            HabitatExploreContent.duckFact.display[ContentLanguage.GERMAN])
        assertEquals("Animals/canonical/frog.png", HabitatExploreContent.freshwaterAnimal(HabitatExploreContent.frog)?.artworkPath)
        assertEquals("Animals/canonical/duck.png", HabitatExploreContent.freshwaterAnimal(HabitatExploreContent.duck)?.artworkPath)
        assertEquals("SceneDescriptions/freshwater_pond/canonical/freshwater_001_pond.png",
            HabitatExploreContent.freshwaterScene)
    }

    @Test fun freshwaterSelectionIsLanguageIndependentUnscoredAndReplayUsesCurrentAnimal() {
        val initial = FreshwaterExploreState()
        assertNull(initial.selectedAnimal)
        val frog = FreshwaterExploreReducer.reduce(initial, FreshwaterExploreAction.Select(HabitatExploreContent.frog))
        val duck = FreshwaterExploreReducer.reduce(frog, FreshwaterExploreAction.Select(HabitatExploreContent.duck))
        assertEquals(HabitatExploreContent.frog, frog.selectedAnimal)
        assertEquals(HabitatExploreContent.duck, duck.selectedAnimal)
        assertEquals("Frog. Many frogs lay eggs in water and also use damp places on land.",
            HabitatExploreContent.freshwaterAnimal(frog.selectedAnimal!!)?.utterance?.display?.get(ContentLanguage.ENGLISH))
        assertEquals("Ente. Viele Enten schwimmen auf Teichen und Seen.",
            HabitatExploreContent.freshwaterAnimal(duck.selectedAnimal!!)?.utterance?.display?.get(ContentLanguage.GERMAN))
        assertThrows(IllegalArgumentException::class.java) {
            FreshwaterExploreReducer.reduce(duck, FreshwaterExploreAction.Select(HabitatExploreContent.dolphin))
        }
        assertThrows(IllegalArgumentException::class.java) {
            FreshwaterExploreReducer.reduce(duck, FreshwaterExploreAction.Select(ContentId("animal.fish")))
        }
    }

    @Test fun freshwaterAnimalTargetsAreDistinctSourceRelativeAndMatchTheLayerAspectRatio() {
        val frog = HabitatExploreContent.freshwaterAnimals.first { it.id == HabitatExploreContent.frog }.target
        val duck = HabitatExploreContent.freshwaterAnimals.first { it.id == HabitatExploreContent.duck }.target
        assertTrue(frog.right < duck.left)
        listOf(frog, duck).forEach { target ->
            assertTrue(target.left >= 0f && target.top >= 0f && target.right <= 1f && target.bottom <= 1f)
            val targetAspect = (4f / 3f) * (target.right - target.left) / (target.bottom - target.top)
            assertEquals("Hotspot box should preserve 1312x1199 art ratio", 1312f / 1199f, targetAspect, .02f)
        }
    }

    @Test fun undergroundHasStableHabitatAndAnimalIdsWithBilingualReviewedContent() {
        assertEquals("habitat.underground", HabitatExploreContent.underground.value)
        assertEquals("animal.mole", HabitatExploreContent.mole.value)
        assertEquals("animal.rabbit", HabitatExploreContent.rabbit.value)
        assertEquals("animal.earthworm", HabitatExploreContent.earthworm.value)
        assertEquals("Underground", HabitatExploreContent.undergroundTitle.display[ContentLanguage.ENGLISH])
        assertEquals("Unter der Erde", HabitatExploreContent.undergroundTitle.display[ContentLanguage.GERMAN])
        assertEquals("A mole lives mostly underground and digs tunnels.",
            HabitatExploreContent.moleFact.display[ContentLanguage.ENGLISH])
        assertEquals("Ein Maulwurf lebt überwiegend unter der Erde und gräbt Gänge.",
            HabitatExploreContent.moleFact.display[ContentLanguage.GERMAN])
        assertEquals("Wild rabbits can use underground warrens for shelter.",
            HabitatExploreContent.wildRabbitFact.display[ContentLanguage.ENGLISH])
        assertEquals("Wildkaninchen können unterirdische Baue als Schutz nutzen.",
            HabitatExploreContent.wildRabbitFact.display[ContentLanguage.GERMAN])
        assertEquals("Earthworms move through soil.",
            HabitatExploreContent.earthwormFact.display[ContentLanguage.ENGLISH])
        assertEquals("Regenwürmer bewegen sich durch den Boden.",
            HabitatExploreContent.earthwormFact.display[ContentLanguage.GERMAN])
    }

    @Test fun undergroundAnimalContentUsesOfflineAssetsAndDisjointImageRelativeFrames() {
        assertEquals("SceneDescriptions/underground/canonical/underground_001_cutaway.png",
            HabitatExploreContent.undergroundScene)
        assertEquals(listOf(HabitatExploreContent.mole, HabitatExploreContent.rabbit, HabitatExploreContent.earthworm),
            HabitatExploreContent.undergroundAnimals.map { it.id })
        assertEquals("Animals/canonical/mole.png", HabitatExploreContent.undergroundAnimal(HabitatExploreContent.mole)?.artworkPath)
        assertEquals("Animals/canonical/rabbit.png", HabitatExploreContent.undergroundAnimal(HabitatExploreContent.rabbit)?.artworkPath)
        assertEquals("Animals/canonical/earthworm.png", HabitatExploreContent.undergroundAnimal(HabitatExploreContent.earthworm)?.artworkPath)
        val animals = HabitatExploreContent.undergroundAnimals
        animals.forEach { animal ->
            assertTrue(animal.target.left >= 0f && animal.target.top >= 0f)
            assertTrue(animal.target.right <= 1f && animal.target.bottom <= 1f)
        }
        for (i in animals.indices) for (j in i + 1 until animals.size) {
            val a = animals[i].target
            val b = animals[j].target
            assertFalse("Underground image-relative targets must not overlap",
                a.left < b.right && b.left < a.right && a.top < b.bottom && b.top < a.bottom)
        }
        assertEquals("Mole. A mole lives mostly underground and digs tunnels.",
            HabitatExploreContent.undergroundAnimal(HabitatExploreContent.mole)?.utterance?.display?.get(ContentLanguage.ENGLISH))
        assertEquals("Regenwurm. Regenwürmer bewegen sich durch den Boden.",
            HabitatExploreContent.undergroundAnimal(HabitatExploreContent.earthworm)?.utterance?.display?.get(ContentLanguage.GERMAN))
    }

    @Test fun undergroundSelectionIsLanguageIndependentUnscoredAndRejectsUnknownAnimals() {
        val initial = UndergroundExploreState()
        assertNull(initial.selectedAnimal)
        val mole = UndergroundExploreReducer.reduce(initial, UndergroundExploreAction.Select(HabitatExploreContent.mole))
        val worm = UndergroundExploreReducer.reduce(mole, UndergroundExploreAction.Select(HabitatExploreContent.earthworm))
        assertEquals(HabitatExploreContent.mole, mole.selectedAnimal)
        assertEquals(HabitatExploreContent.earthworm, worm.selectedAnimal)
        assertEquals(HabitatExploreContent.earthworm, worm.selectedAnimal)
        assertThrows(IllegalArgumentException::class.java) {
            UndergroundExploreReducer.reduce(worm, UndergroundExploreAction.Select(ContentId("animal.badger")))
        }
        assertThrows(IllegalArgumentException::class.java) {
            UndergroundExploreReducer.reduce(worm, UndergroundExploreAction.Select(HabitatExploreContent.dolphin))
        }
    }
}
