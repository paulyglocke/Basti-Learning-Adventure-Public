package com.bellfamily.bastischool.learning.coloursort

import com.bellfamily.bastischool.learning.colours.ColoursContent
import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.sorting.*
import com.bellfamily.bastischool.learning.session.*
import com.bellfamily.bastischool.learning.progress.*
import com.bellfamily.bastischool.audio.SpeechTrigger

/** Separate eight-object v1 pack. ColourSort/CSR1 remains the historical two-colour contract. */
object FourColourSort : SortingContent {
    override val revision = 1
    override val journalMagic = 0x43534631 // CSF1
    override val speakOnStart = true
    val activity = ActivityId("activity.colours.sort.four")
    override val version = ContentVersion(1, 1)
    private val shapes = listOf(ColourSortShape.BALL, ColourSortShape.BLOCK)
    val categories = ColoursContent.order
    val objects: List<ColourSortObject> = categories.flatMap { colour -> shapes.map { shape ->
        val suffix = shape.name.lowercase()
        val name = ColoursContent.definition(colour).text.display
        val display = if (shape == ColourSortShape.BALL) "${name.en} ball" to "${name.de} Ball"
            else "${name.en} block" to "${name.de} Baustein"
        ColourSortObject(ContentId("object.colours.four.${colour.value.substringAfterLast('.')}_$suffix"), colour, shape,
            ContentText.plain(display.first, display.second))
    } }
    override val rule = SortRule(ContentId("rule.sort.colour.four"), categories,
        objects.map { SortItem(it.id, it.category) })
    private val core = com.bellfamily.bastischool.learning.content.CoreContent.repository()
    fun category(id: ContentId) = requireNotNull(core.find(id)).text
    fun item(id: ContentId) = objects.single { it.id == id }
    val title = ContentText.plain("Four-Colour Sort", "Vier Farben sortieren")
    val instruction = ContentText.plain("Tap an object, then its colour group.", "Tippe auf ein Ding und dann auf die passende Farbgruppe.")
    val completion = ContentText.plain("You sorted all eight objects!", "Du hast alle acht Dinge sortiert!")
    override fun prompt(s: SortState) = if (s.completed) completion else instruction
    override fun start(id: SessionId, seed: Long, language: ContentLanguage) = Sorting.start(id, rule, seed, language)
    override fun reduce(s: SortState, action: SortAction): SortTransition {
        val next = Sorting.reduce(s, action)
        if (next === s && action != SortAction.Replay) return SortTransition(s)
        val events = if (action is SortAction.Place && next !== s) listOf(attempt(next, next.index(action.item))) +
            if (next.completed) listOf(completionEvent(next)) else emptyList() else emptyList()
        val speech = when (action) {
            is SortAction.Language -> null
            is SortAction.Select -> SessionEffect.Narrate(instruction, NarrationKind.INSTRUCTION, SpeechTrigger.MANUAL)
            SortAction.Replay -> SessionEffect.Narrate(prompt(next), if (next.completed) NarrationKind.COMPLETION else NarrationKind.INSTRUCTION, SpeechTrigger.REPLAY)
            SortAction.Hint -> SessionEffect.Narrate(hint(next), NarrationKind.INSTRUCTION, SpeechTrigger.MANUAL)
            is SortAction.Place -> SessionEffect.Narrate(if (next.completed) completion else if (next.placement(action.item).placed)
                ContentText.plain("The colours match!", "Die Farben passen zusammen!") else wrong,
                if (next.completed) NarrationKind.COMPLETION else NarrationKind.FEEDBACK)
        }
        return SortTransition(next, events, speech)
    }
    private val wrong = ContentText.plain("Look at its colour. Try another group.", "Schau dir die Farbe an. Probiere eine andere Gruppe.")
    private fun hint(s: SortState): ContentText = s.selected?.let(::item)?.category?.let { colour ->
        val label = category(colour).display
        ContentText.plain("Look for the ${label.en} group.", "Suche die Farbgruppe ${label.de}.")
    } ?: instruction
    private fun origin(s: SortState) = ProgressOrigin(s.id, activity, revision, version)
    private fun evidence(s: SortState, index: Int) = TaskEvidence(TaskInstanceId(s.id, index + 1),
        TaskDefinitionId("task.colours.sort.four.${objects[index].id.value.substringAfterLast('.')}"),
        SkillId("skill.colours.sort.four"), LearningContextId("context.colours.four_categories"), 1)
    override fun attempt(s: SortState, i: Int): AttemptEvent {
        val p = s.placements[i]
        return AttemptEvent(origin(s), evidence(s, i), AttemptId(TaskInstanceId(s.id, i + 1), p.attempts), s.language,
            p.lastCategory!!, if (p.placed) AttemptOutcome.CORRECT else AttemptOutcome.INCORRECT, p.support)
    }
    override fun completionEvent(s: SortState): CompletionEvent {
        require(s.completed)
        return CompletionEvent(origin(s), s.placements.mapIndexed { i, p -> CompletedTask(evidence(s, i), p.lastCategory!!,
            AttemptOutcome.CORRECT, p.attempts, p.attempts - 1, p.support) })
    }
}
