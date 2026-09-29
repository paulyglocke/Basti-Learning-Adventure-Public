package com.bellfamily.bastischool.learning.sorting

import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.progress.*
import com.bellfamily.bastischool.learning.session.*

/** Only the content/progress hooks required by the two durable sorting consumers. */
interface SortingContent {
    val revision: Int
    val version: ContentVersion
    val journalMagic: Int
    val speakOnStart: Boolean
    val rule: SortRule
    fun start(id: SessionId, seed: Long, language: ContentLanguage): SortState
    fun prompt(s: SortState): ContentText
    fun reduce(s: SortState, action: SortAction): SortTransition
    fun attempt(s: SortState, i: Int): AttemptEvent
    fun completionEvent(s: SortState): CompletionEvent
}
data class SortTransition(val state: SortState, val events: List<ProgressEvent> = emptyList(),
    val speech: SessionEffect.Narrate? = null)
