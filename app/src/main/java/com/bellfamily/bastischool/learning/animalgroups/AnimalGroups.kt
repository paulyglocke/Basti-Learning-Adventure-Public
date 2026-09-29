package com.bellfamily.bastischool.learning.animalgroups

import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.vocabulary.VocabularyContent
import com.bellfamily.bastischool.learning.sorting.*
import com.bellfamily.bastischool.learning.session.*
import com.bellfamily.bastischool.learning.progress.*
import com.bellfamily.bastischool.audio.SpeechTrigger

data class AnimalGroupObject(val id: ContentId, val category: ContentId, val text: ContentText, val help: ContentText, val image: String)
object AnimalGroups : SortingContent {
    override val revision = 1
    override val journalMagic = 0x41475231
    override val speakOnStart = false
    const val REVISION = 1
    override val version = ContentVersion(1, 1)
    val activity = ActivityId("activity.discover.animal_groups")
    val water = ContentId("habitat.water")
    val land = ContentId("habitat.land")
    val categories = listOf(water, land)
    val objects = listOf(
        AnimalGroupObject(ContentId("animal.whale"), water, VocabularyContent.item(ContentId("animal.whale")).text,
            ContentText.plain("The whale lives in water.", "Der Wal lebt im Wasser."), "Animals/canonical/whale.png"),
        AnimalGroupObject(ContentId("animal.dolphin"), water, ContentText.plain("Dolphin", "Delfin"),
            ContentText.plain("The dolphin lives in water.", "Der Delfin lebt im Wasser."), "Animals/canonical/dolphin.png"),
        AnimalGroupObject(ContentId("animal.horse"), land, VocabularyContent.item(ContentId("animal.horse")).text,
            ContentText.plain("The horse lives on land.", "Das Pferd lebt an Land."), "Animals/canonical/horse.png"),
        AnimalGroupObject(ContentId("animal.rabbit"), land, ContentText.plain("Rabbit", "Kaninchen"),
            ContentText.plain("The rabbit lives on land.", "Das Kaninchen lebt an Land."), "Animals/canonical/rabbit.png"))
    override val rule = SortRule(ContentId("rule.sort.animal_habitat"), categories, objects.map { SortItem(it.id, it.category) })
    fun category(id: ContentId) = when(id) {
        water -> ContentText.plain("Water", "Wasser")
        land -> ContentText.plain("Land", "Land")
        else -> error("Unknown animal group")
    }
    fun item(id: ContentId) = objects.single { it.id == id }
    val title = ContentText.plain("Animal Groups", "Tiere zuordnen")
    val instruction = ContentText.plain("Put each animal in the right group.", "Ordne jedes Tier der richtigen Gruppe zu.")
    val interaction = ContentText.plain("Tap an animal, then Water or Land.", "Tippe auf ein Tier und dann auf Wasser oder Land.")
    val selectedInstruction = ContentText.plain("Now tap Water or Land.", "Tippe jetzt auf Wasser oder Land.")
    val wrong = ContentText.plain("Where does this animal live? Try another group.", "Wo lebt dieses Tier? Probiere die andere Gruppe.")
    val correct = ContentText.plain("That is where it lives!", "Dort lebt es!")
    val completion = ContentText.plain("You grouped all the animals!", "Du hast alle Tiere zugeordnet!")
    fun hint(s: SortState) = s.selected?.let { item(it).help } ?: instruction
    override fun prompt(s: SortState) = if (s.completed) completion else instruction
    override fun start(id: SessionId, seed: Long, language: ContentLanguage) = Sorting.start(id, rule, seed, language)
    override fun reduce(s: SortState, action: SortAction): SortTransition {
        val n = Sorting.reduce(s, action)
        if (n === s && action != SortAction.Replay) return SortTransition(s)
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
        return SortTransition(n, events, speech)
    }
    private fun origin(s: SortState) = ProgressOrigin(s.id, activity, REVISION, version)
    private fun evidence(s: SortState, i: Int) = TaskEvidence(TaskInstanceId(s.id, i+1),
        TaskDefinitionId("task.discover.animal_groups.${objects[i].id.value.substringAfterLast('.')}"),
        SkillId("skill.discover.animal_habitat"), LearningContextId("context.discover.water_land"), 1)
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
