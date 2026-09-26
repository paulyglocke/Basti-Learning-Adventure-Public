package com.bellfamily.bastischool.learning.prepositions

import com.bellfamily.bastischool.learning.models.*

/** Explicit reviewed art-revision-3 catalogue; not an animal × relation product. */
object PrepositionsArtwork {
    private val entries = listOf(
        PositionScene(PositionAnimal.SNAKE, PositionRelation.ON) to "Prepositions/scenes/scene_prepositions_snake_on.png",
        PositionScene(PositionAnimal.DINOSAUR, PositionRelation.ON) to "Prepositions/scenes/scene_prepositions_dinosaur_on.png",
        PositionScene(PositionAnimal.DRAGON, PositionRelation.ON) to "Prepositions/scenes/scene_prepositions_dragon_on.png",
        PositionScene(PositionAnimal.CROCODILE, PositionRelation.ON) to "Prepositions/scenes/scene_prepositions_crocodile_on.png",
        PositionScene(PositionAnimal.SNAKE, PositionRelation.UNDER) to "Prepositions/scenes/scene_prepositions_snake_under.png",
        PositionScene(PositionAnimal.DINOSAUR, PositionRelation.UNDER) to "Prepositions/scenes/scene_prepositions_dinosaur_under.png",
        PositionScene(PositionAnimal.DRAGON, PositionRelation.UNDER) to "Prepositions/scenes/scene_prepositions_dragon_under.png",
        PositionScene(PositionAnimal.CROCODILE, PositionRelation.UNDER) to "Prepositions/scenes/scene_prepositions_crocodile_under.png",
        PositionScene(PositionAnimal.SNAKE, PositionRelation.BEHIND) to "Prepositions/scenes/scene_prepositions_snake_behind.png",
        PositionScene(PositionAnimal.DINOSAUR, PositionRelation.BEHIND) to "Prepositions/scenes/scene_prepositions_dinosaur_behind.png",
        PositionScene(PositionAnimal.DRAGON, PositionRelation.BEHIND) to "Prepositions/scenes/scene_prepositions_dragon_behind.png",
        PositionScene(PositionAnimal.CROCODILE, PositionRelation.BEHIND) to "Prepositions/scenes/scene_prepositions_crocodile_behind.png",
        PositionScene(PositionAnimal.SNAKE, PositionRelation.NEXT_TO) to "Prepositions/scenes/scene_prepositions_snake_next_to.png",
        PositionScene(PositionAnimal.DINOSAUR, PositionRelation.NEXT_TO) to "Prepositions/scenes/scene_prepositions_dinosaur_next_to.png",
        PositionScene(PositionAnimal.DRAGON, PositionRelation.NEXT_TO) to "Prepositions/scenes/scene_prepositions_dragon_next_to.png",
        PositionScene(PositionAnimal.CROCODILE, PositionRelation.NEXT_TO) to "Prepositions/scenes/scene_prepositions_crocodile_next_to.png",
        PositionScene(PositionAnimal.SNAKE, PositionRelation.IN) to "Prepositions/scenes/scene_prepositions_snake_in.png",
        PositionScene(PositionAnimal.DINOSAUR, PositionRelation.IN) to "Prepositions/scenes/scene_prepositions_dinosaur_in.png",
        PositionScene(PositionAnimal.DRAGON, PositionRelation.IN) to "Prepositions/scenes/scene_prepositions_dragon_in.png",
        PositionScene(PositionAnimal.CROCODILE, PositionRelation.IN) to "Prepositions/scenes/scene_prepositions_crocodile_in.png",
        PositionScene(PositionAnimal.SNAKE, PositionRelation.BETWEEN) to "Prepositions/scenes/scene_prepositions_snake_between.png",
        PositionScene(PositionAnimal.DINOSAUR, PositionRelation.BETWEEN) to "Prepositions/scenes/scene_prepositions_dinosaur_between.png",
        PositionScene(PositionAnimal.DRAGON, PositionRelation.BETWEEN) to "Prepositions/scenes/scene_prepositions_dragon_between.png",
        PositionScene(PositionAnimal.CROCODILE, PositionRelation.BETWEEN) to "Prepositions/scenes/scene_prepositions_crocodile_between.png",
        PositionScene(PositionAnimal.BIRD, PositionRelation.ABOVE) to "Prepositions/scenes/scene_prepositions_bird_above.png",
        PositionScene(PositionAnimal.BEE, PositionRelation.ABOVE) to "Prepositions/scenes/scene_prepositions_bee_above.png",
        PositionScene(PositionAnimal.BUTTERFLY, PositionRelation.ABOVE) to "Prepositions/scenes/scene_prepositions_butterfly_above.png",
        PositionScene(PositionAnimal.DRAGON, PositionRelation.ABOVE) to "Prepositions/scenes/scene_prepositions_dragon_above.png",
        PositionScene(PositionAnimal.BIRD, PositionRelation.BELOW) to "Prepositions/scenes/scene_prepositions_bird_below.png",
        PositionScene(PositionAnimal.BEE, PositionRelation.BELOW) to "Prepositions/scenes/scene_prepositions_bee_below.png",
        PositionScene(PositionAnimal.BUTTERFLY, PositionRelation.BELOW) to "Prepositions/scenes/scene_prepositions_butterfly_below.png",
        PositionScene(PositionAnimal.DRAGON, PositionRelation.BELOW) to "Prepositions/scenes/scene_prepositions_dragon_below.png",
        PositionScene(PositionAnimal.FISH, PositionRelation.INSIDE) to "Prepositions/scenes/scene_prepositions_fish_inside.png",
        PositionScene(PositionAnimal.TURTLE, PositionRelation.INSIDE) to "Prepositions/scenes/scene_prepositions_turtle_inside.png",
        PositionScene(PositionAnimal.OCTOPUS, PositionRelation.INSIDE) to "Prepositions/scenes/scene_prepositions_octopus_inside.png",
        PositionScene(PositionAnimal.SEAHORSE, PositionRelation.INSIDE) to "Prepositions/scenes/scene_prepositions_seahorse_inside.png",
        PositionScene(PositionAnimal.FISH, PositionRelation.OUTSIDE) to "Prepositions/scenes/scene_prepositions_fish_outside.png",
        PositionScene(PositionAnimal.TURTLE, PositionRelation.OUTSIDE) to "Prepositions/scenes/scene_prepositions_turtle_outside.png",
        PositionScene(PositionAnimal.OCTOPUS, PositionRelation.OUTSIDE) to "Prepositions/scenes/scene_prepositions_octopus_outside.png",
        PositionScene(PositionAnimal.SEAHORSE, PositionRelation.OUTSIDE) to "Prepositions/scenes/scene_prepositions_seahorse_outside.png",
        PositionScene(PositionAnimal.SNAKE, PositionRelation.IN_FRONT_OF) to "Prepositions/scenes/scene_prepositions_snake_in_front_of.png",
        PositionScene(PositionAnimal.DINOSAUR, PositionRelation.IN_FRONT_OF) to "Prepositions/scenes/scene_prepositions_dinosaur_in_front_of.png",
        PositionScene(PositionAnimal.DRAGON, PositionRelation.IN_FRONT_OF) to "Prepositions/scenes/scene_prepositions_dragon_in_front_of.png",
        PositionScene(PositionAnimal.CROCODILE, PositionRelation.IN_FRONT_OF) to "Prepositions/scenes/scene_prepositions_crocodile_in_front_of.png",
        PositionScene(PositionAnimal.SNAKE, PositionRelation.NEAR) to "Prepositions/scenes/scene_prepositions_snake_near.png",
        PositionScene(PositionAnimal.DINOSAUR, PositionRelation.NEAR) to "Prepositions/scenes/scene_prepositions_dinosaur_near.png",
        PositionScene(PositionAnimal.DRAGON, PositionRelation.NEAR) to "Prepositions/scenes/scene_prepositions_dragon_near.png",
        PositionScene(PositionAnimal.CROCODILE, PositionRelation.NEAR) to "Prepositions/scenes/scene_prepositions_crocodile_near.png",
        PositionScene(PositionAnimal.SNAKE, PositionRelation.FAR_FROM) to "Prepositions/scenes/scene_prepositions_snake_far_from.png",
        PositionScene(PositionAnimal.DINOSAUR, PositionRelation.FAR_FROM) to "Prepositions/scenes/scene_prepositions_dinosaur_far_from.png",
        PositionScene(PositionAnimal.DRAGON, PositionRelation.FAR_FROM) to "Prepositions/scenes/scene_prepositions_dragon_far_from.png",
        PositionScene(PositionAnimal.CROCODILE, PositionRelation.FAR_FROM) to "Prepositions/scenes/scene_prepositions_crocodile_far_from.png"
    )
    val scenes: List<PositionScene> = java.util.Collections.unmodifiableList(entries.map { it.first })
    private val images = entries.associate { (scene, path) ->
        scene.id to LocalImageAsset(AssetId("asset.prepositions.${scene.animal.key}.${scene.relation.key}"), path)
    }
    init { require(entries.size == 52 && images.size == 52 && images.values.map { it.path }.toSet().size == 52) }
    fun image(scene: ContentId): LocalImageAsset = requireNotNull(images[scene]) { "Unknown Prepositions scene: $scene" }
}
