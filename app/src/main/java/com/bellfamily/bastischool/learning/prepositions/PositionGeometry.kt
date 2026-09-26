package com.bellfamily.bastischool.learning.prepositions

/** Original six-relation procedural geometry, retained for regression evidence; live scenes use artwork. */
data class SceneRect(val x: Float, val y: Float, val width: Float, val height: Float) {
    val right get() = x + width
    val bottom get() = y + height
}
data class PositionGeometry(val animal: SceneRect, val objects: List<SceneRect>, val animalBehind: Boolean = false,
                            val boxFront: SceneRect? = null) {
    companion object {
        fun forRelation(relation: PositionRelation): PositionGeometry = when (relation) {
            PositionRelation.ON -> PositionGeometry(SceneRect(135f, 55f, 50f, 50f), listOf(SceneRect(110f, 105f, 100f, 65f)))
            PositionRelation.UNDER -> PositionGeometry(SceneRect(135f, 112f, 50f, 50f), listOf(SceneRect(85f, 85f, 150f, 18f)))
            PositionRelation.BEHIND -> PositionGeometry(SceneRect(135f, 78f, 50f, 60f), listOf(SceneRect(110f, 108f, 100f, 65f)), true)
            PositionRelation.NEXT_TO -> PositionGeometry(SceneRect(75f, 120f, 60f, 50f), listOf(SceneRect(175f, 105f, 100f, 65f)))
            PositionRelation.IN -> PositionGeometry(SceneRect(135f, 105f, 50f, 50f), listOf(SceneRect(110f, 95f, 100f, 80f)), false, SceneRect(110f, 140f, 100f, 35f))
            PositionRelation.BETWEEN -> PositionGeometry(SceneRect(135f, 120f, 50f, 50f), listOf(SceneRect(30f, 105f, 85f, 65f), SceneRect(205f, 105f, 85f, 65f)))
            else -> throw IllegalArgumentException("No procedural geometry for ${relation.key}; use authored artwork")
        }
    }
}
