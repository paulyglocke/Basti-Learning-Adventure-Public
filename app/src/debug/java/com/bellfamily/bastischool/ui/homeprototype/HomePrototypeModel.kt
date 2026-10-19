package com.bellfamily.bastischool.ui.homeprototype

import com.bellfamily.bastischool.HomeCard
import com.bellfamily.bastischool.homeCardGroups
import com.bellfamily.bastischool.ui.design.BastiCategoryTreatment

/** Debug presentation only. Existing catalogue entries remain the source of labels and identity. */
internal enum class PrototypeCategory(
    val en: String, val de: String, val treatment: BastiCategoryTreatment, val activityIds: List<String>,
) {
    MATHS("Numbers & Maths", "Zahlen & Rechnen", BastiCategoryTreatment.MATHS,
        listOf("count", "subitising", "quantityMatch", "compareQuantity", "numberOrder", "addTogether", "math")),
    WORDS("Words & Talking", "Wörter & Erzählen", BastiCategoryTreatment.WORDS,
        listOf("tellme", "vocabulary", "verbExplorer", "letters", "positions")),
    TIME("Time & Seasons", "Zeit & Jahreszeiten", BastiCategoryTreatment.TIME, listOf("time", "clock")),
    ANIMALS("Animals & Nature", "Tiere & Natur", BastiCategoryTreatment.ANIMALS, listOf("verbs", "animalGroups")),
    COLOURS("Colours", "Farben", BastiCategoryTreatment.COLOURS, listOf("colourSort")),
    EVERYDAY("Everyday Skills", "Alltag", BastiCategoryTreatment.EVERYDAY, listOf("washHands", "follow"));

    fun label(german: Boolean) = if (german) de else en
}

internal fun HomeCard.prototypeId(): String = if (verbExplorer) "verbExplorer" else requireNotNull(mode)
internal fun prototypeActivities(): List<HomeCard> = homeCardGroups().flatten()
internal fun prototypeActivity(id: String): HomeCard = prototypeActivities().single { it.prototypeId() == id }
internal fun prototypeCategory(id: String) = PrototypeCategory.entries.firstOrNull { id in it.activityIds }

/** Width is the already padded content width; neither device name nor orientation chooses columns. */
internal fun prototypeColumns(width: Float, fontScale: Float): Int =
    ((width.coerceAtMost(1040f) + 16f) / (160f * fontScale.coerceAtLeast(1f) + 16f)).toInt().coerceIn(1, 3)

/** Small local review stack, deliberately unrelated to ShellNavigation or any journal. */
internal data class PrototypeNavigation(val stack: List<String> = listOf("home")) {
    val current get() = stack.last()
    fun open(route: String): PrototypeNavigation {
        require(validRoute(route))
        return if (route == current) this else if (route == "home") PrototypeNavigation()
        else copy(stack = stack + route)
    }
    fun back() = if (stack.size > 1) copy(stack = stack.dropLast(1)) else this

    companion object {
        fun validRoute(route: String): Boolean = route in listOf("home", "all", "settings") ||
            PrototypeCategory.entries.any { route == "category:${it.name}" } ||
            prototypeActivities().any { route == "activity:${it.prototypeId()}" }
        fun restore(saved: List<String>) = if (saved.size in 1..8 && saved.first() == "home" && saved.all(::validRoute))
            PrototypeNavigation(saved.toList()) else PrototypeNavigation()
    }
}
