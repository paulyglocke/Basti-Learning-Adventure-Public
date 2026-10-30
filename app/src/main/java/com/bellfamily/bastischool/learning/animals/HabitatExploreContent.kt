package com.bellfamily.bastischool.learning.animals

import com.bellfamily.bastischool.learning.models.ContentId
import com.bellfamily.bastischool.learning.models.ContentText

/** Small reviewed Explore slice; this content does not define Animal Groups sorting or progress. */
object HabitatExploreContent {
    val ocean = ContentId("habitat.ocean")
    val woodland = ContentId("habitat.woodland")
    val underground = ContentId("habitat.underground")
    val freshwater = ContentId("habitat.freshwater")
    val dolphin = ContentId("animal.dolphin")
    val rabbit = ContentId("animal.rabbit")
    val mole = ContentId("animal.mole")
    val earthworm = ContentId("animal.earthworm")
    val frog = ContentId("animal.frog")
    val duck = ContentId("animal.duck")
    /** Stable scene identity; the app has no shared canonical animal registry yet. */
    val fox = ContentId("animal.fox")
    const val oceanScene = "SceneDescriptions/ocean_underwater/canonical/ocean_001_reef_actions.png"
    const val woodlandScene = "SceneDescriptions/woodland_forest/canonical/woodland_008_homes_choices.png"
    const val undergroundScene = "SceneDescriptions/underground/canonical/underground_001_cutaway.png"
    const val freshwaterScene = "SceneDescriptions/freshwater_pond/canonical/freshwater_001_pond.png"

    val oceanTitle = ContentText.plain("Ocean", "Meer")
    val woodlandTitle = ContentText.plain("Woodland", "Wald")
    val undergroundTitle = ContentText.plain("Underground", "Unter der Erde")
    val freshwaterTitle = ContentText.plain("Freshwater", "Süßwasser")
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

    val moleName = ContentText.plain("Mole", "Maulwurf")
    val moleFact = ContentText.plain(
        "A mole lives mostly underground and digs tunnels.",
        "Ein Maulwurf lebt überwiegend unter der Erde und gräbt Gänge.")
    val moleUtterance = ContentText.plain(
        "Mole. A mole lives mostly underground and digs tunnels.",
        "Maulwurf. Ein Maulwurf lebt überwiegend unter der Erde und gräbt Gänge.")

    val wildRabbitName = ContentText.plain("Wild Rabbit", "Wildkaninchen")
    val wildRabbitFact = ContentText.plain(
        "Wild rabbits can use underground warrens for shelter.",
        "Wildkaninchen können unterirdische Baue als Schutz nutzen.")
    val wildRabbitUtterance = ContentText.plain(
        "Wild rabbit. Wild rabbits can use underground warrens for shelter.",
        "Wildkaninchen. Wildkaninchen können unterirdische Baue als Schutz nutzen.")

    val earthwormName = ContentText.plain("Earthworm", "Regenwurm")
    val earthwormFact = ContentText.plain(
        "Earthworms move through soil.",
        "Regenwürmer bewegen sich durch den Boden.")
    val earthwormUtterance = ContentText.plain(
        "Earthworm. Earthworms move through soil.",
        "Regenwurm. Regenwürmer bewegen sich durch den Boden.")

    val undergroundAnimals = listOf(
        UndergroundAnimalContent(mole, moleName, moleFact, moleUtterance,
            NormalizedSceneTarget(0.10f, 0.58f, 0.39f, 0.96f), "Animals/canonical/mole.png"),
        UndergroundAnimalContent(rabbit, wildRabbitName, wildRabbitFact, wildRabbitUtterance,
            NormalizedSceneTarget(0.70f, 0.54f, 0.93f, 0.84f), "Animals/canonical/rabbit.png"),
        UndergroundAnimalContent(earthworm, earthwormName, earthwormFact, earthwormUtterance,
            // Taller than the worm artwork itself for practical direct tapping;
            // it remains isolated horizontally from the Mole and Rabbit regions.
            NormalizedSceneTarget(0.41f, 0.35f, 0.68f, 0.60f), "Animals/canonical/earthworm.png")
    )

    fun undergroundAnimal(id: ContentId) = undergroundAnimals.firstOrNull { it.id == id }

    val freshwaterPrompt = ContentText.plain(
        "Tap the frog or duck, or choose an animal's name below.",
        "Tippe auf den Frosch oder die Ente oder wähle unten den Namen eines Tieres.")
    val frogName = ContentText.plain("Frog", "Frosch")
    val frogFact = ContentText.plain(
        "Many frogs lay eggs in water and also use damp places on land.",
        "Viele Frösche legen ihre Eier im Wasser ab und nutzen auch feuchte Plätze an Land.")
    val frogUtterance = ContentText.plain(
        "Frog. Many frogs lay eggs in water and also use damp places on land.",
        "Frosch. Viele Frösche legen ihre Eier im Wasser ab und nutzen auch feuchte Plätze an Land.")
    val duckName = ContentText.plain("Duck", "Ente")
    val duckFact = ContentText.plain(
        "Many ducks swim on ponds and lakes.",
        "Viele Enten schwimmen auf Teichen und Seen.")
    val duckUtterance = ContentText.plain(
        "Duck. Many ducks swim on ponds and lakes.",
        "Ente. Viele Enten schwimmen auf Teichen und Seen.")
    val freshwaterAnimals = listOf(
        FreshwaterAnimalContent(frog, frogName, frogFact, frogUtterance,
            NormalizedSceneTarget(0.07f, 0.43f, 0.33f, 0.75f), "Animals/canonical/frog.png"),
        FreshwaterAnimalContent(duck, duckName, duckFact, duckUtterance,
            NormalizedSceneTarget(0.60f, 0.34f, 0.93f, 0.74f), "Animals/canonical/duck.png")
    )

    fun freshwaterAnimal(id: ContentId) = freshwaterAnimals.firstOrNull { it.id == id }
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

data class UndergroundAnimalContent(
    val id: ContentId,
    val name: ContentText,
    val fact: ContentText,
    val utterance: ContentText,
    /** Normalized visible-image frame on the 4:3 source scene. */
    val target: NormalizedSceneTarget,
    val artworkPath: String
)

data class FreshwaterAnimalContent(
    val id: ContentId,
    val name: ContentText,
    val fact: ContentText,
    val utterance: ContentText,
    /** Normalized target and layer box on the fitted 4:3 background. */
    val target: NormalizedSceneTarget,
    val artworkPath: String
)

data class UndergroundExploreState(val selectedAnimal: ContentId? = null)

sealed interface UndergroundExploreAction {
    data class Select(val animal: ContentId) : UndergroundExploreAction
}

/** Unscored, language-independent exploration selection. */
object UndergroundExploreReducer {
    private val availableAnimals = HabitatExploreContent.undergroundAnimals.map { it.id }.toSet()

    fun reduce(state: UndergroundExploreState, action: UndergroundExploreAction): UndergroundExploreState = when (action) {
        is UndergroundExploreAction.Select -> {
            require(action.animal in availableAnimals) { "Unknown Underground Explore animal" }
            state.copy(selectedAnimal = action.animal)
        }
    }
}

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

data class FreshwaterExploreState(val selectedAnimal: ContentId? = null)

sealed interface FreshwaterExploreAction {
    data class Select(val animal: ContentId) : FreshwaterExploreAction
}

/** Language-independent unscored selection with no assessment/progress actions. */
object FreshwaterExploreReducer {
    private val availableAnimals = HabitatExploreContent.freshwaterAnimals.map { it.id }.toSet()

    fun reduce(state: FreshwaterExploreState, action: FreshwaterExploreAction): FreshwaterExploreState = when (action) {
        is FreshwaterExploreAction.Select -> {
            require(action.animal in availableAnimals) { "Unknown Freshwater Explore animal" }
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
