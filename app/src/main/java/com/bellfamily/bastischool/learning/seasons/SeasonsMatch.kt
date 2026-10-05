package com.bellfamily.bastischool.learning.seasons

import com.bellfamily.bastischool.learning.content.SeasonIds
import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.progress.*
import com.bellfamily.bastischool.learning.sorting.*
import com.bellfamily.bastischool.learning.session.*
import com.bellfamily.bastischool.audio.SpeechTrigger

/** Seasons-to-clue membership uses the small sorting primitive; it is not a sequence. */
object SeasonsMatch : SortingContent {
    override val revision = 1
    override val journalMagic = 0x534D5431
    override val speakOnStart = true
    val activity = ActivityId("activity.seasons.match")
    override val version = SeasonsContent.repository.version
    // The first authored alternative is the bounded v1 selection for each season.
    // The seeded start still determines the visible season/clue order and exact restore.
    val selectedClues = SeasonIds.canonicalOrder.map { season -> SeasonsClues.catalogue.first { it.correct == season } }
    val clueIds = selectedClues.map { ContentId("clue.${it.definition.value.substringAfter("task.seasons.clues.")}") }
    val clueById = selectedClues.zip(clueIds).associate { it.second to it.first }
    val seasonByClue = selectedClues.zip(clueIds).associate { it.second to it.first.correct }
    override val rule = SortRule(ContentId("rule.seasons.match"), clueIds,
        SeasonIds.canonicalOrder.map { season -> SortItem(season, clueIds.first { seasonByClue[it] == season }) })
    private fun seasonFor(clue: ContentId) = seasonByClue[clue] ?: error("Unknown clue")
    val title = ContentText.plain("Match season to clue", "Jahreszeit zu Hinweis")
    val instruction = ContentText.plain("Tap a season, then its matching clue.", "Tippe eine Jahreszeit und dann den passenden Hinweis an.")
    val selectedInstruction = ContentText.plain("Now tap its matching clue.", "Tippe jetzt den passenden Hinweis an.")
    val wrong = ContentText.plain("That clue belongs to another season. Try again.", "Dieser Hinweis gehört zu einer anderen Jahreszeit. Versuche es noch einmal.")
    val correct = ContentText.plain("That season and clue match!", "Die Jahreszeit und der Hinweis passen zusammen!")
    val completion = ContentText.plain("You matched all four seasons!", "Du hast alle vier Jahreszeiten zugeordnet!")
    fun clue(id: ContentId) = requireNotNull(clueById[id]) { "Unknown clue" }
    fun categoryName(id: ContentId) = clue(id).instruction.display.let { ContentText.plain(it.en.substringBefore(" Which season"), it.de.substringBefore(" Welche Jahreszeit")) }
    fun hint(s: SortState) = s.selected?.let { item ->
        val season = SeasonsContent.season(item).text.display
        ContentText.plain("Find the clue about ${season.en.lowercase()}.", "Finde den Hinweis zum ${season.de.lowercase()}.")
    } ?: instruction
    override fun start(id: SessionId, seed: Long, language: ContentLanguage) = Sorting.start(id, rule, seed, language)
    override fun prompt(s: SortState) = if (s.completed) completion else instruction
    override fun reduce(s: SortState, action: SortAction): SortTransition {
        val n = Sorting.reduce(s, action)
        if (n === s && action != SortAction.Replay) return SortTransition(s)
        val events = if (action is SortAction.Place && n !== s) listOf(attempt(n, n.index(action.item))) +
            if (n.completed) listOf(completionEvent(n)) else emptyList() else emptyList()
        val speech: SessionEffect.Narrate? = when (action) {
            is SortAction.Language -> null
            is SortAction.Select -> SessionEffect.Narrate(selectedInstruction, NarrationKind.INSTRUCTION, SpeechTrigger.MANUAL)
            SortAction.Replay -> SessionEffect.Narrate(prompt(n), if (n.completed) NarrationKind.COMPLETION else NarrationKind.INSTRUCTION, SpeechTrigger.REPLAY)
            SortAction.Hint -> SessionEffect.Narrate(hint(n), NarrationKind.INSTRUCTION, SpeechTrigger.MANUAL)
            is SortAction.Place -> SessionEffect.Narrate(if (n.completed) completion else if (n.placement(action.item).placed) correct else wrong,
                if (n.completed) NarrationKind.COMPLETION else NarrationKind.FEEDBACK)
        }
        return SortTransition(n, events, speech)
    }
    private fun origin(s: SortState) = ProgressOrigin(s.id, activity, revision, version)
    private fun evidence(s: SortState, i: Int) = TaskEvidence(TaskInstanceId(s.id, i + 1),
        TaskDefinitionId("task.seasons.match.${SeasonIds.canonicalOrder[i].value.substringAfter('.') }"),
        SkillId("skill.seasons.match"), LearningContextId("context.seasons.lakeside_tree"), 1)
    override fun attempt(s: SortState, i: Int) = AttemptEvent(origin(s), evidence(s, i),
        AttemptId(TaskInstanceId(s.id, i + 1), s.placements[i].attempts), s.language, s.placements[i].lastCategory!!,
        if (s.placements[i].placed) AttemptOutcome.CORRECT else AttemptOutcome.INCORRECT, s.placements[i].support)
    override fun completionEvent(s: SortState) = CompletionEvent(origin(s), s.placements.mapIndexed { i, p ->
        CompletedTask(evidence(s, i), p.lastCategory!!, AttemptOutcome.CORRECT, p.attempts, p.attempts - 1, p.support)
    })
}
