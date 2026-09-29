package com.bellfamily.bastischool.learning.coloursort

import com.bellfamily.bastischool.learning.content.CoreContent
import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.sorting.*
import com.bellfamily.bastischool.learning.session.*
import com.bellfamily.bastischool.learning.progress.*
import com.bellfamily.bastischool.audio.SpeechTrigger

enum class ColourSortShape { BALL, BLOCK }
data class ColourSortObject(val id: ContentId, val category: ContentId, val shape: ColourSortShape, val text: ContentText)
typealias ColourSortTransition = SortTransition
object ColourSort : SortingContent {
    override val revision = 1
    override val journalMagic = 0x43535231
    override val speakOnStart = true
    const val REVISION = 1
    override val version = ContentVersion(1, 1)
    val activity = ActivityId("activity.colours.sort")
    val red = ContentId("colour.red")
    val blue = ContentId("colour.blue")
    val categories = listOf(red, blue)
    val objects = listOf(
        ColourSortObject(ContentId("object.colours.red_ball"), red, ColourSortShape.BALL, ContentText.plain("red ball", "roter Ball")),
        ColourSortObject(ContentId("object.colours.blue_ball"), blue, ColourSortShape.BALL, ContentText.plain("blue ball", "blauer Ball")),
        ColourSortObject(ContentId("object.colours.red_block"), red, ColourSortShape.BLOCK, ContentText.plain("red block", "roter Baustein")),
        ColourSortObject(ContentId("object.colours.blue_block"), blue, ColourSortShape.BLOCK, ContentText.plain("blue block", "blauer Baustein")))
    override val rule = SortRule(ContentId("rule.sort.colour"), categories, objects.map { SortItem(it.id, it.category) })
    private val core = CoreContent.repository()
    fun category(id: ContentId) = requireNotNull(core.find(id)).text
    fun item(id: ContentId) = objects.single { it.id == id }
    val title = ContentText.plain("Colour Sort", "Farben sortieren")
    val instruction = ContentText.plain("Sort by colour. Tap an object, then its colour group.", "Sortiere nach Farben. Tippe auf ein Ding und dann auf die passende Farbgruppe.")
    val selectedInstruction = ContentText.plain("Now tap a colour group.", "Tippe jetzt auf eine Farbgruppe.")
    val wrong = ContentText.plain("Look at its colour. Try another group.", "Schau dir die Farbe an. Probiere eine andere Gruppe.")
    val correct = ContentText.plain("The colours match!", "Die Farben passen zusammen!")
    val completion = ContentText.plain("You sorted all the objects!", "Du hast alle Dinge sortiert!")
    fun hint(s: SortState): ContentText = when (s.selected?.let(::item)?.category) {
        red -> ContentText.plain("Put it with the red ones.", "Lege es zu den roten Dingen.")
        blue -> ContentText.plain("Put it with the blue ones.", "Lege es zu den blauen Dingen.")
        else -> instruction
    }
    override fun prompt(s: SortState) = if (s.completed) completion else instruction
    override fun start(id: SessionId, seed: Long, language: ContentLanguage) = Sorting.start(id, rule, seed, language)
    override fun reduce(s: SortState, action: SortAction): ColourSortTransition {
        val n = Sorting.reduce(s, action)
        if (n === s && action != SortAction.Replay) return ColourSortTransition(s)
        val events = if (action is SortAction.Place && n !== s) listOf(attempt(n, n.index(action.item))) +
            if (n.completed) listOf(completionEvent(n)) else emptyList() else emptyList()
        val speech = when (action) {
            is SortAction.Language -> null
            is SortAction.Select -> SessionEffect.Narrate(selectedInstruction, NarrationKind.INSTRUCTION, SpeechTrigger.MANUAL)
            SortAction.Replay -> SessionEffect.Narrate(prompt(n), if(n.completed) NarrationKind.COMPLETION else NarrationKind.INSTRUCTION, SpeechTrigger.REPLAY)
            SortAction.Hint -> SessionEffect.Narrate(hint(n), NarrationKind.INSTRUCTION, SpeechTrigger.MANUAL)
            is SortAction.Place -> SessionEffect.Narrate(if(n.completed) completion else if(n.placement(action.item).placed) correct else wrong,
                if(n.completed) NarrationKind.COMPLETION else NarrationKind.FEEDBACK)
        }
        return ColourSortTransition(n, events, speech)
    }
    private fun origin(s: SortState) = ProgressOrigin(s.id, activity, REVISION, version)
    private fun evidence(s: SortState, i: Int) = TaskEvidence(TaskInstanceId(s.id, i+1),
        TaskDefinitionId("task.colours.sort.${objects[i].id.value.substringAfterLast('.')}"),
        SkillId("skill.colours.sort"), LearningContextId("context.colours.balls_blocks"), 1)
    override fun attempt(s: SortState, i: Int): AttemptEvent {
        val p = s.placements[i]
        return AttemptEvent(origin(s), evidence(s,i), AttemptId(TaskInstanceId(s.id,i+1),p.attempts), s.language,
            p.lastCategory!!, if(p.placed) AttemptOutcome.CORRECT else AttemptOutcome.INCORRECT, p.support)
    }
    override fun completionEvent(s: SortState): CompletionEvent {
        require(s.completed)
        return CompletionEvent(origin(s),s.placements.mapIndexed { i,p -> CompletedTask(evidence(s,i), p.lastCategory!!,
            AttemptOutcome.CORRECT,p.attempts,p.attempts-1,p.support) })
    }
}
