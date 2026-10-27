package com.bellfamily.bastischool.learning.animals

import com.bellfamily.bastischool.learning.models.ContentId
import com.bellfamily.bastischool.learning.models.ContentText

/** Small reviewed Explore slice; this content does not define Animal Groups sorting or progress. */
object HabitatExploreContent {
    val ocean = ContentId("habitat.ocean")
    val dolphin = ContentId("animal.dolphin")
    const val oceanScene = "SceneDescriptions/ocean_underwater/canonical/ocean_001_reef_actions.png"

    val oceanTitle = ContentText.plain("Ocean", "Meer")
    val choosePrompt = ContentText.plain("Choose a place to explore.", "Wähle einen Ort zum Entdecken.")
    val scenePrompt = ContentText.plain("Tap the dolphin or its name to learn about it.",
        "Tippe auf den Delfin oder seinen Namen, um mehr über ihn zu erfahren.")
    val dolphinName = ContentText.plain("Dolphin", "Delfin")
    // Reviewed from HABITAT_FACTS.md; the composite scene gives this dolphin a marine context.
    val dolphinFact = ContentText.plain("Dolphins live in the sea.", "Delfine leben im Meer.")
    val dolphinUtterance = ContentText.plain(
        "Dolphin. Dolphins live in the sea.",
        "Delfin. Delfine leben im Meer.")
}

data class HabitatExploreState(val selectedAnimal: ContentId? = null)

sealed interface HabitatExploreAction {
    data class Select(val animal: ContentId) : HabitatExploreAction
}

/** Language-neutral browsing selection; deliberately has no scoring/progress action. */
object HabitatExploreReducer {
    fun reduce(state: HabitatExploreState, action: HabitatExploreAction): HabitatExploreState = when (action) {
        is HabitatExploreAction.Select -> {
            require(action.animal == HabitatExploreContent.dolphin) { "Unknown Ocean Explore animal" }
            state.copy(selectedAnimal = action.animal)
        }
    }
}
