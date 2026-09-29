package com.bellfamily.bastischool.learning.sorting

import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.session.*
import java.util.Collections

private fun <T> snapshot(values: List<T>): List<T> = Collections.unmodifiableList(values.toList())
data class SortItem(val id: ContentId, val category: ContentId)
/** Membership only: no colours, shapes, screen coordinates, speech or storage. */
class SortRule(val id: ContentId, categories: List<ContentId>, items: List<SortItem>) {
    val categories = snapshot(categories)
    val items = snapshot(items)
    init {
        require(categories.size in 2..8 && categories.toSet().size == categories.size)
        require(items.size in 1..10 && items.map { it.id }.toSet().size == items.size)
        require(items.all { it.category in categories } && items.none { it.id in categories })
    }
}
data class SortPlacement(val placed: Boolean = false, val attempts: Int = 0,
    val lastCategory: ContentId? = null, val support: SupportUse = SupportUse())
class SortState(val id: SessionId, val rule: SortRule, val language: ContentLanguage,
    order: List<ContentId>, placements: List<SortPlacement>, val selected: ContentId? = null,
    val acknowledged: Boolean = false) {
    val order = snapshot(order)
    val placements = snapshot(placements)
    val completed get() = placements.all { it.placed }
    fun index(item: ContentId) = rule.items.indexOfFirst { it.id == item }
    fun placement(item: ContentId) = placements[index(item)]
    val selectedPlacement get() = selected?.let(::placement)
    init {
        require(order.size == rule.items.size && order.toSet() == rule.items.map { it.id }.toSet())
        require(placements.size == rule.items.size)
        placements.forEachIndexed { i, p ->
            require(p.attempts in 0..10000 && p.support.replays in 0..10000)
            require((p.attempts == 0) == (p.lastCategory == null))
            require(p.lastCategory == null || p.lastCategory in rule.categories)
            require(p.placed == (p.lastCategory == rule.items[i].category))
        }
        require(selected == null || selected in order && !placement(selected).placed)
        require(!acknowledged || completed)
    }
    fun changed(language: ContentLanguage = this.language, placements: List<SortPlacement> = this.placements,
        selected: ContentId? = this.selected, acknowledged: Boolean = this.acknowledged) =
        SortState(id, rule, language, order, placements, selected, acknowledged)
}
sealed interface SortAction {
    data class Select(val item: ContentId): SortAction
    data class Place(val session: SessionId, val item: ContentId, val category: ContentId, val attempt: Int): SortAction
    data object Replay: SortAction
    data object Hint: SortAction
    data class Language(val language: ContentLanguage): SortAction
}
object Sorting {
    fun start(id: SessionId, rule: SortRule, seed: Long, language: ContentLanguage): SortState {
        val order = rule.items.map { it.id }.toMutableList()
        Collections.shuffle(order, java.util.Random(seed))
        return SortState(id, rule, language, order, List(rule.items.size) { SortPlacement() })
    }
    fun reduce(s: SortState, action: SortAction): SortState = when (action) {
        is SortAction.Language -> if (s.language == action.language) s else s.changed(language = action.language)
        is SortAction.Select -> if (action.item !in s.order || s.placement(action.item).placed || s.selected == action.item) s else s.changed(selected = action.item)
        is SortAction.Place -> {
            val i = s.index(action.item)
            if (action.session != s.id || i < 0 || s.selected != action.item || action.category !in s.rule.categories ||
                s.placements[i].placed || action.attempt != s.placements[i].attempts + 1 || action.attempt > 10000) s
            else {
                val correct = s.rule.items[i].category == action.category
                val next = s.placements.toMutableList().also { it[i] = it[i].copy(placed = correct, attempts = action.attempt, lastCategory = action.category) }
                s.changed(placements = next, selected = if (correct) null else action.item)
            }
        }
        SortAction.Hint -> if (s.selected == null || s.selectedPlacement!!.support.hint) s else {
            val next = s.placements.mapIndexed { i, p -> if (s.rule.items[i].id == s.selected) p.copy(support = p.support.copy(hint = true)) else p }
            s.changed(placements = next)
        }
        SortAction.Replay -> if (s.completed) s else s.changed(placements = s.placements.mapIndexed { i, p ->
            if (!p.placed && (s.selected == null || s.rule.items[i].id == s.selected))
                p.copy(support = p.support.copy(replays = (p.support.replays + 1).coerceAtMost(10000))) else p
        })
    }
}
