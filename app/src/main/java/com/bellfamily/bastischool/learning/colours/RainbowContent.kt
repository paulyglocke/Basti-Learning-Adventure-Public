package com.bellfamily.bastischool.learning.colours

import com.bellfamily.bastischool.learning.content.CoreContent
import com.bellfamily.bastischool.learning.models.*

/** Separate seven-band convention; the spectrum itself is continuous. */
object RainbowContent {
    const val REVISION = 1
    val version = ContentVersion(1, 1)
    val order = listOf("red", "orange", "yellow", "green", "blue", "indigo", "violet")
        .map { ContentId("colour.$it") }
    private val existing = CoreContent.repository()
    private val additions = mapOf(
        ContentId("colour.indigo") to ColourDefinition(ContentId("colour.indigo"), ContentText.plain("indigo", "indigo")),
        ContentId("colour.violet") to ColourDefinition(ContentId("colour.violet"), ContentText.plain("violet", "violett"))
    )
    fun definition(id: ContentId): ColourDefinition =
        (existing.find(id) as? ColourDefinition ?: additions[id])
            ?: error("Unknown rainbow colour ${id.value}")
    val title = ContentText.plain("The Rainbow", "Der Regenbogen")
    val orderTitle = ContentText.plain("Rainbow Order", "Regenbogen ordnen")
    val orderInstruction = ContentText.plain("Arrange the colours in the traditional rainbow order.",
        "Ordne die Farben in der traditionellen Regenbogen-Reihenfolge.")
    val orderHelp = ContentText.plain("Start with red on the outside. Violet is on the inside.",
        "Rot liegt außen. Violett liegt innen.")
    val conventionalNote = ContentText.plain(
        "This is the traditional seven-colour order. A rainbow is a continuous range of light.",
        "Das ist die traditionelle Reihenfolge mit sieben Farben. Ein Regenbogen ist ein fließender Übergang des Lichts."
    )
    val swatches = listOf(0xFFC62828, 0xFFEF6C00, 0xFFFFEB3B, 0xFF2E7D32, 0xFF1565C0, 0xFF3949AB, 0xFF7B1FA2)
        .map { it.toInt() }
    init { require(order.map(::definition).map { it.id } == order) }
}
