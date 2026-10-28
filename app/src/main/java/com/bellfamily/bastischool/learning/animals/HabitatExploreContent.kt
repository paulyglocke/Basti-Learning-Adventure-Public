package com.bellfamily.bastischool.learning.animals

import com.bellfamily.bastischool.learning.models.ContentId
import com.bellfamily.bastischool.learning.models.ContentText

/** Small reviewed Explore slice; this content does not define Animal Groups sorting or progress. */
object HabitatExploreContent {
    val ocean = ContentId("habitat.ocean")
    val woodland = ContentId("habitat.woodland")
    val dolphin = ContentId("animal.dolphin")
    val rabbit = ContentId("animal.rabbit")
    /** Stable scene identity; the app has no shared canonical animal registry yet. */
    val fox = ContentId("animal.fox")
    const val oceanScene = "SceneDescriptions/ocean_underwater/canonical/ocean_001_reef_actions.png"
    const val woodlandScene = "SceneDescriptions/woodland_forest/canonical/woodland_008_homes_choices.png"

    val oceanTitle = ContentText.plain("Ocean", "Meer")
    val woodlandTitle = ContentText.plain("Woodland", "Wald")
    val choosePrompt = ContentText.plain("Choose a place to explore.", "Wähle einen Ort zum Entdecken.")
    val scenePrompt = ContentText.plain("Tap the dolphin or its name to learn about it.",
        "Tippe auf den Delfin oder seinen Namen, um mehr über ihn zu erfahren.")
    val dolphinName = ContentText.plain("Dolphin", "Delfin")
    // Reviewed from HABITAT_FACTS.md; the composite scene gives this dolphin a marine context.
    val dolphinFact = ContentText.plain("Dolphins live in the sea.", "Delfine leben im Meer.")
    val dolphinUtterance = ContentText.plain(
        "Dolphin. Dolphins live in the sea.",
        "Delfin. Delfine leben im Meer.")

    val foxName = ContentText.plain("Fox", "Fuchs")
    val foxFact = ContentText.plain(
        "Foxes live in many habitats. A fox may use an underground den to raise its cubs.",
        "Füchse leben in vielen Lebensräumen. Ein Fuchs kann einen unterirdischen Bau nutzen, um seine Jungen aufzuziehen.")
    val foxUtterance = ContentText.plain(
        "Fox. Foxes live in many habitats. A fox may use an underground den to raise its cubs.",
        "Fuchs. Füchse leben in vielen Lebensräumen. Ein Fuchs kann einen unterirdischen Bau nutzen, um seine Jungen aufzuziehen.")

    val rabbitName = ContentText.plain("Rabbit", "Kaninchen")
    val rabbitFact = ContentText.plain(
        "Wild rabbits can use underground warrens for shelter.",
        "Wildkaninchen können unterirdische Baue als Schutz nutzen.")
    val rabbitUtterance = ContentText.plain(
        "Rabbit. Wild rabbits can use underground warrens for shelter.",
        "Kaninchen. Wildkaninchen können unterirdische Baue als Schutz nutzen.")

    val woodlandAnimals = listOf(
        WoodlandAnimalContent(
            id = rabbit,
            name = rabbitName,
            fact = rabbitFact,
            utterance = rabbitUtterance,
            target = NormalizedSceneTarget(left = 0.39f, top = 0.46f, right = 0.55f, bottom = 0.69f)
        ),
        WoodlandAnimalContent(
            id = fox,
            name = foxName,
            fact = foxFact,
            utterance = foxUtterance,
            target = NormalizedSceneTarget(left = 0.73f, top = 0.48f, right = 0.91f, bottom = 0.75f)
        )
    )

    fun woodlandAnimal(id: ContentId) = woodlandAnimals.firstOrNull { it.id == id }
}

data class NormalizedSceneTarget(val left: Float, val top: Float, val right: Float, val bottom: Float) {
    init { require(left >= 0f && top >= 0f && right <= 1f && bottom <= 1f && left < right && top < bottom) }
}

data class WoodlandAnimalContent(
    val id: ContentId,
    val name: ContentText,
    val fact: ContentText,
    val utterance: ContentText,
    /** Fractional coordinates on the 4:3 source image, not screen coordinates. */
    val target: NormalizedSceneTarget
)

data class WoodlandExploreState(val selectedAnimal: ContentId? = null)

sealed interface WoodlandExploreAction {
    data class Select(val animal: ContentId) : WoodlandExploreAction
}

object WoodlandExploreReducer {
    private val availableAnimals = HabitatExploreContent.woodlandAnimals.map { it.id }.toSet()

    fun reduce(state: WoodlandExploreState, action: WoodlandExploreAction): WoodlandExploreState = when (action) {
        is WoodlandExploreAction.Select -> {
            require(action.animal in availableAnimals) { "Unknown Woodland Explore animal" }
            state.copy(selectedAnimal = action.animal)
        }
    }
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
