package com.bellfamily.bastischool.learning.models

/** Explicit migration visuals, not production illustration assets or semantic identity. */
enum class TemporaryAnimalVisual(val animal: ContentId, val glyph: String) {
    DINOSAUR(ContentId("animal.dinosaur"), "🦖"), SNAKE(ContentId("animal.snake"), "🐍"),
    WHALE(ContentId("animal.whale"), "🐋"), HORSE(ContentId("animal.horse"), "🐎"),
    CROCODILE(ContentId("animal.crocodile"), "🐊"), FISH(ContentId("animal.fish"), "🐟")
}

data class VocabularyCategory(override val id: ContentId, override val text: ContentText): ContentDefinition {
    init { require(id.value.startsWith("category.")) }
}

data class VocabularyDefinition(override val id: ContentId, override val text: ContentText,
    val category: ContentId, val visual: TemporaryAnimalVisual, val example: ContentText,
    val findPrompt: ContentText): ContentDefinition {
    init { require(id == visual.animal); require(category.value.startsWith("category.")) }
}
