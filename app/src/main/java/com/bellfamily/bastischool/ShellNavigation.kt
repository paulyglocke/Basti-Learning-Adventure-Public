package com.bellfamily.bastischool

import java.util.concurrent.atomic.AtomicReference

internal enum class ShellScreen { HOME, OPTIONS, WEB, PREPOSITIONS, DAYS_SEASONS, SEASONS, WILMA, VOCABULARY }
internal enum class BackAction { EXIT, CLOSE_OPTIONS, LEGACY, NATIVE_HOME, DAYS_HUB }

/** Options is an overlay on one destination, never another entry in a growing stack. */
internal data class ShellNavigation(
    val screen: ShellScreen = ShellScreen.HOME,
    val optionsOrigin: ShellScreen = ShellScreen.HOME,
    val mode: String = "verbs",
    val recoveryFailed: Boolean = false
) {
    val backAction: BackAction get() = when (screen) {
        ShellScreen.HOME -> BackAction.EXIT
        ShellScreen.OPTIONS -> BackAction.CLOSE_OPTIONS
        ShellScreen.WEB -> BackAction.LEGACY
        ShellScreen.PREPOSITIONS, ShellScreen.DAYS_SEASONS, ShellScreen.VOCABULARY -> BackAction.NATIVE_HOME
        ShellScreen.SEASONS, ShellScreen.WILMA -> BackAction.DAYS_HUB
    }
    val ownsWebSession: Boolean get() = screen == ShellScreen.WEB ||
        (screen == ShellScreen.OPTIONS && optionsOrigin == ShellScreen.WEB)
    fun openOptions() = if (screen == ShellScreen.OPTIONS) this else copy(screen = ShellScreen.OPTIONS, optionsOrigin = screen)
    fun closeOptions() = if (screen == ShellScreen.OPTIONS) copy(screen = optionsOrigin, optionsOrigin = ShellScreen.HOME) else this
    fun openDaysSeasons() = ShellNavigation(ShellScreen.DAYS_SEASONS)
    fun openVocabulary() = ShellNavigation(ShellScreen.VOCABULARY)
    fun openWilma() = ShellNavigation(ShellScreen.WILMA)
    fun openSeasons() = ShellNavigation(ShellScreen.SEASONS)
    fun openPrepositions() = ShellNavigation(ShellScreen.PREPOSITIONS, mode = "positions")
    fun openActivity(mode: String) = ShellNavigation(ShellScreen.WEB, mode = mode)
    fun home(failed: Boolean = false) = ShellNavigation(recoveryFailed = failed)

    companion object {
        val modes = setOf("verbs", "count", "math", "positions", "letters", "time", "mixed", "verbExplorer")
        fun restore(screen: String?, origin: String?, mode: String?, session: String?): ShellNavigation {
            if (screen == null) return ShellNavigation()
            val destination = ShellScreen.entries.firstOrNull { it.name == screen }
                ?: return ShellNavigation(recoveryFailed = true)
            val parent = ShellScreen.entries.firstOrNull { it.name == origin && it != ShellScreen.OPTIONS }
                ?: ShellScreen.HOME
            val route = ShellNavigation(destination, parent, mode ?: "verbs")
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
