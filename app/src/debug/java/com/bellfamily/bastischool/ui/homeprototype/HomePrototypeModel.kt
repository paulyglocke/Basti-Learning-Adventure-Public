package com.bellfamily.bastischool.ui.homeprototype

import com.bellfamily.bastischool.ui.home.*

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
            HomeCategory.entries.any { route == "category:${it.name}" } ||
            homeActivities().any { route == "activity:${it.homeId()}" }
        fun restore(saved: List<String>) = if (saved.size in 1..8 && saved.first() == "home" && saved.all(::validRoute))
            PrototypeNavigation(saved.toList()) else PrototypeNavigation()
    }
}
