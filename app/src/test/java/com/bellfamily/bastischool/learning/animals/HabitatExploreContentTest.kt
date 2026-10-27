package com.bellfamily.bastischool.learning.animals

import com.bellfamily.bastischool.learning.models.ContentId
import com.bellfamily.bastischool.learning.models.ContentLanguage
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
}
