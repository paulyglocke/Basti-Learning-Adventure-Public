package com.bellfamily.bastischool

import java.util.concurrent.atomic.AtomicReference
import com.bellfamily.bastischool.ui.home.validHomePage

internal enum class ShellScreen { HOME, OPTIONS, WEB, PREPOSITIONS, DAYS_SEASONS, SEASONS_CHOOSER, SEASONS, WILMA, VOCABULARY, TELL_ME, FOLLOW_LEVELS, FOLLOW_INSTRUCTIONS, FOLLOW_TWO, COLOUR_SORT, ANIMAL_GROUPS, WASH_HANDS, SUBITISING, QUANTITY_MATCH, COMPARE_QUANTITY, NUMBER_ORDER, ADD_TOGETHER, CLOCK, CLOCK_MAKE, MONTHS_CHOOSER, MONTHS, MONTHS_FIND, MONTHS_RELATIONS, MONTHS_MISSING, MONTHS_BETWEEN, MONTHS_BETWEEN_WHEEL, MONTHS_BUILD_YEAR_LEVELS, MONTHS_BUILD_YEAR, MONTHS_BUILD_YEAR_FOUR, MONTHS_SEASONS, MONTHS_SEASONS_WHEEL }
internal enum class BackAction { EXIT, HOME_ROOT, CLOSE_OPTIONS, LEGACY, NATIVE_HOME, DAYS_HUB, SEASONS_CHOOSER, CLOCK_AREA, MONTHS_AREA, FOLLOW_LEVELS }

/** Options is an overlay on one destination, never another entry in a growing stack. */
internal data class ShellNavigation(
    val screen: ShellScreen = ShellScreen.HOME,
    val optionsOrigin: ShellScreen = ShellScreen.HOME,
    val mode: String = "verbs",
    val recoveryFailed: Boolean = false,
    val homePage: String = "home"
) {
    val backAction: BackAction get() = when (screen) {
        ShellScreen.HOME -> if (homePage == "home") BackAction.EXIT else BackAction.HOME_ROOT
        ShellScreen.OPTIONS -> BackAction.CLOSE_OPTIONS
        ShellScreen.WEB -> BackAction.LEGACY
        ShellScreen.MONTHS, ShellScreen.MONTHS_FIND, ShellScreen.MONTHS_RELATIONS, ShellScreen.MONTHS_MISSING, ShellScreen.MONTHS_BETWEEN, ShellScreen.MONTHS_BETWEEN_WHEEL, ShellScreen.MONTHS_BUILD_YEAR_LEVELS, ShellScreen.MONTHS_BUILD_YEAR, ShellScreen.MONTHS_BUILD_YEAR_FOUR, ShellScreen.MONTHS_SEASONS, ShellScreen.MONTHS_SEASONS_WHEEL -> BackAction.MONTHS_AREA
        ShellScreen.CLOCK_MAKE -> BackAction.CLOCK_AREA
        ShellScreen.FOLLOW_INSTRUCTIONS, ShellScreen.FOLLOW_TWO -> BackAction.FOLLOW_LEVELS
        ShellScreen.NUMBER_ORDER -> BackAction.NATIVE_HOME
        ShellScreen.ADD_TOGETHER, ShellScreen.CLOCK -> BackAction.NATIVE_HOME
        ShellScreen.COMPARE_QUANTITY -> BackAction.NATIVE_HOME
        ShellScreen.PREPOSITIONS, ShellScreen.DAYS_SEASONS, ShellScreen.VOCABULARY, ShellScreen.TELL_ME, ShellScreen.FOLLOW_LEVELS, ShellScreen.COLOUR_SORT, ShellScreen.ANIMAL_GROUPS, ShellScreen.WASH_HANDS, ShellScreen.SUBITISING, ShellScreen.QUANTITY_MATCH -> BackAction.NATIVE_HOME
        ShellScreen.SEASONS -> BackAction.SEASONS_CHOOSER
        ShellScreen.SEASONS_CHOOSER, ShellScreen.WILMA, ShellScreen.MONTHS_CHOOSER -> BackAction.DAYS_HUB
    }
    val ownsWebSession: Boolean get() = screen == ShellScreen.WEB ||
        (screen == ShellScreen.OPTIONS && optionsOrigin == ShellScreen.WEB)
    fun openOptions() = if (screen == ShellScreen.OPTIONS) this else copy(screen = ShellScreen.OPTIONS, optionsOrigin = screen)
    fun closeOptions() = if (screen == ShellScreen.OPTIONS) copy(screen = optionsOrigin, optionsOrigin = ShellScreen.HOME) else this
    fun openDaysSeasons() = ShellNavigation(ShellScreen.DAYS_SEASONS)
    fun openTellMe() = ShellNavigation(ShellScreen.TELL_ME)
    fun openWashHands() = ShellNavigation(ShellScreen.WASH_HANDS)
    fun openAnimalGroups() = ShellNavigation(ShellScreen.ANIMAL_GROUPS)
    fun openColourSort() = ShellNavigation(ShellScreen.COLOUR_SORT)
    fun openSubitising() = ShellNavigation(ShellScreen.SUBITISING)
    fun openQuantityMatch() = ShellNavigation(ShellScreen.QUANTITY_MATCH)
    fun openCompareQuantity() = ShellNavigation(ShellScreen.COMPARE_QUANTITY)
    fun openNumberOrder() = ShellNavigation(ShellScreen.NUMBER_ORDER)
    fun openClockPractice() = ShellNavigation(ShellScreen.CLOCK_MAKE)
    fun openClock() = ShellNavigation(ShellScreen.CLOCK)
    fun openAddTogether() = ShellNavigation(ShellScreen.ADD_TOGETHER)
    fun openFollowInstructions() = ShellNavigation(ShellScreen.FOLLOW_LEVELS)
    fun openFollowOne() = ShellNavigation(ShellScreen.FOLLOW_INSTRUCTIONS)
    fun openFollowTwo() = ShellNavigation(ShellScreen.FOLLOW_TWO)
    fun openVocabulary() = ShellNavigation(ShellScreen.VOCABULARY)
    fun openMonthsFind() = ShellNavigation(ShellScreen.MONTHS_FIND)
    fun openMonthsMissing() = ShellNavigation(ShellScreen.MONTHS_MISSING)
    fun openMonthsBetween() = ShellNavigation(ShellScreen.MONTHS_BETWEEN)
    fun openMonthsBetweenWheel() = ShellNavigation(ShellScreen.MONTHS_BETWEEN_WHEEL)
    fun openMonthsRelations() = ShellNavigation(ShellScreen.MONTHS_RELATIONS)
    fun openMonthsBuildYear() = ShellNavigation(ShellScreen.MONTHS_BUILD_YEAR_LEVELS)
    fun openMonthsBuildYearThree() = ShellNavigation(ShellScreen.MONTHS_BUILD_YEAR)
    fun openMonthsBuildYearFour() = ShellNavigation(ShellScreen.MONTHS_BUILD_YEAR_FOUR)
    fun openMonthsSeasons() = ShellNavigation(ShellScreen.MONTHS_SEASONS)
    fun openMonthsSeasonsWheel() = ShellNavigation(ShellScreen.MONTHS_SEASONS_WHEEL)
    fun openMonths() = ShellNavigation(ShellScreen.MONTHS)
    fun openMonthsChooser() = ShellNavigation(ShellScreen.MONTHS_CHOOSER)
    fun openWilma() = ShellNavigation(ShellScreen.WILMA)
    fun openSeasons() = ShellNavigation(ShellScreen.SEASONS_CHOOSER)
    fun openSeasonsActivity() = ShellNavigation(ShellScreen.SEASONS)
    fun openPrepositions() = ShellNavigation(ShellScreen.PREPOSITIONS, mode = "positions")
    fun openActivity(mode: String) = ShellNavigation(ShellScreen.WEB, mode = mode)
    fun home(failed: Boolean = false) = ShellNavigation(recoveryFailed = failed)
    fun browseHome(page: String): ShellNavigation {
        require(validHomePage(page))
        return ShellNavigation(homePage = page, recoveryFailed = recoveryFailed)
    }

    companion object {
        val modes = setOf("verbs", "count", "math", "positions", "letters", "time", "mixed", "verbExplorer")
        fun restore(screen: String?, origin: String?, mode: String?, session: String?, homePage: String? = null): ShellNavigation {
            if (screen == null) return ShellNavigation()
            val destination = ShellScreen.entries.firstOrNull { it.name == screen }
                ?: return ShellNavigation(recoveryFailed = true)
            val parent = ShellScreen.entries.firstOrNull { it.name == origin && it != ShellScreen.OPTIONS }
                ?: ShellScreen.HOME
            val browsing = destination == ShellScreen.HOME || destination == ShellScreen.OPTIONS && parent == ShellScreen.HOME
            val route = ShellNavigation(destination, parent, mode ?: "verbs",
                homePage = homePage?.takeIf { browsing && validHomePage(it) } ?: "home")
            if (route.ownsWebSession && (mode !in modes || !LegacyCheckpoint.accepts(session))) return ShellNavigation(recoveryFailed = true)
            return route
        }
    }
}

/** The JS bridge publishes synchronously. Android can save the latest complete
 * checkpoint without waiting for evaluateJavascript during onSaveInstanceState.
 * Each WebView owns a separate mailbox; a disposed view cannot overwrite its successor. */
internal class LegacyCheckpoint(initial: String? = null) {
    private val latest = AtomicReference(initial?.takeIf { accepts(it) })
    fun publish(value: String) { latest.set(value.takeIf { accepts(it) }) }
    fun read(): String? = latest.get()
    companion object {
        const val MAX_CHARS = 100_000
        fun accepts(value: String?) = value != null && value.isNotBlank() && value.length <= MAX_CHARS
    }
}

/** Coalesce Back while JavaScript responds; navigation invalidates its callback. */
internal class LegacyBackGate {
    private var revision = 0L
    private var pending = false
    val isPending: Boolean get() = pending
    fun begin(): Long? { if (pending) return null; pending = true; return revision }
    fun finish(ticket: Long): Boolean {
        if (ticket != revision || !pending) return false
        pending = false; return true
    }
    fun invalidate() { revision++; pending = false }
}
