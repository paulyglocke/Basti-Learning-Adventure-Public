package com.bellfamily.bastischool.ui.home

import com.bellfamily.bastischool.HomeCard
import com.bellfamily.bastischool.ShellNavigation
import com.bellfamily.bastischool.homeCardGroups
import com.bellfamily.bastischool.ui.design.BastiCategoryTreatment

/** Shared Home presentation. Existing catalogue entries remain the source of labels and identity. */
internal enum class HomeCategory(
    val en: String, val de: String, val treatment: BastiCategoryTreatment, val activityIds: List<String>,
) {
    MATHS("Numbers & Maths", "Zahlen & Rechnen", BastiCategoryTreatment.MATHS,
        listOf("count", "subitising", "quantityMatch", "compareQuantity", "numberOrder", "addTogether", "math")),
    WORDS("Words & Talking", "Wörter & Erzählen", BastiCategoryTreatment.WORDS,
        listOf("tellme", "vocabulary", "verbExplorer", "letters", "positions")),
    TIME("Time & Seasons", "Zeit & Jahreszeiten", BastiCategoryTreatment.TIME, listOf("time", "clock")),
    ANIMALS("Animals & Nature", "Tiere & Natur", BastiCategoryTreatment.ANIMALS,
        listOf("verbs", "exploreHabitats", "animalGroups")),
    COLOURS("Colours", "Farben", BastiCategoryTreatment.COLOURS, listOf("colourSort")),
    EVERYDAY("Everyday Skills", "Alltag", BastiCategoryTreatment.EVERYDAY, listOf("washHands", "follow"));

    fun label(german: Boolean) = if (german) de else en
}

internal fun HomeCard.homeId(): String = if (verbExplorer) "verbExplorer" else requireNotNull(mode)
internal fun homeActivities(): List<HomeCard> = homeCardGroups().flatten()
internal fun homeActivity(id: String): HomeCard = homeActivities().single { it.homeId() == id }
internal fun homeCategory(id: String) = HomeCategory.entries.firstOrNull { id in it.activityIds }

/** Width is the already padded content width; neither device name nor orientation chooses columns. */
internal fun homeColumns(width: Float, fontScale: Float): Int =
    ((width.coerceAtMost(1040f) + 16f) / (160f * fontScale.coerceAtLeast(1f) + 16f)).toInt().coerceIn(1, 3)


/** Home browsing only: no new learning destination or session identity. */
internal fun validHomePage(page: String?) = page == "home" || page == "all" ||
    HomeCategory.entries.any { page == "category:${it.name}" }

/** Exact former MainActivity Home dispatch, shared by category and All Activities entry. */
internal fun HomeCard.homeDestination(): ShellNavigation {
    require(!play && this in homeActivities())
    val shell = ShellNavigation()
    return when (homeId()) {
        "clock" -> shell.openClock()
        "subitising" -> shell.openSubitising()
        "addTogether" -> shell.openAddTogether()
        "numberOrder" -> shell.openNumberOrder()
        "compareQuantity" -> shell.openCompareQuantity()
        "quantityMatch" -> shell.openQuantityMatch()
        "tellme" -> shell.openTellMe()
        "washHands" -> shell.openWashHands()
        "animalGroups" -> shell.openAnimalGroups()
        "exploreHabitats" -> shell.openHabitatsChooser()
        "colourSort" -> shell.openColoursChooser()
        "follow" -> shell.openFollowInstructions()
        "vocabulary" -> shell.openVocabulary()
        "positions" -> shell.openPrepositions()
        "time" -> shell.openDaysSeasons()
        else -> shell.openActivity(homeId())
    }
}
