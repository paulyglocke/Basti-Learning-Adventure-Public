package com.bellfamily.bastischool.learning.seasons

import com.bellfamily.bastischool.learning.content.SeasonIds
import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.progress.*
import com.bellfamily.bastischool.learning.session.*

/** One gap in the canonical year; separate identity/journal preserves existing quiz contracts. */
object SeasonsMissing {
    val activity = ActivityId("activity.seasons.missing")
    const val REVISION = 1
    fun question(missing: ContentId, choices: List<ContentId>): ChoiceQuestion {
        require(missing in SeasonIds.canonicalOrder)
        require(choices.size == 4 && choices.toSet() == SeasonIds.canonicalOrder.toSet())
        val name = SeasonsContent.season(missing).text.speech
        val names = SeasonIds.canonicalOrder.map { SeasonsContent.season(it).text.speech }
        return ChoiceQuestion(TaskDefinitionId("task.seasons.missing.${missing.value.substringAfter('.')}"),
            SkillId("skill.seasons.missing"), LearningContextId("context.seasons.year_order"), 1,
            ContentText.plain("Which season is missing?", "Welche Jahreszeit fehlt?"), choices, missing,
            ContentText.plain("${name.en} is missing.", "Der ${name.de} fehlt."),
            ContentText.plain("Try again. You can listen or ask for help.",
                "Versuche es noch einmal. Du kannst noch einmal hören oder dir helfen lassen."),
            ContentText.plain("Say the seasons in order: ${names.joinToString(", ") { it.en }}.",
                "Sag die Jahreszeiten der Reihe nach: ${names.joinToString(", ") { it.de }}."))
    }
    fun generate(id: SessionId, round: RoundLength, seed: Long) = CandidateTaskGenerator(
        SeasonIds.canonicalOrder.map { question(it, SeasonIds.canonicalOrder) }).generate(
        GenerationRequest(id, activity, REVISION, SessionPolicy(round), seed, SeasonsContent.completion),
        SeasonsContent.repository)

    fun validate(state: SessionState) {
        require(state.plan.activity == activity && state.plan.activityRevision == REVISION &&
            state.plan.contentVersion == SeasonsContent.repository.version)
        require(state.plan.policy == SessionPolicy(state.plan.policy.round) && state.plan.completionText == SeasonsContent.completion)
        state.plan.tasks.forEach { task ->
            val q = task.question
            val expected = question(q.correct, q.choices)
            require(q.definition == expected.definition && q.skill == expected.skill && q.context == expected.context &&
                q.difficulty == expected.difficulty && q.instruction == expected.instruction && q.hint == expected.hint &&
                q.correctFeedback == expected.correctFeedback && q.wrongFeedback == expected.wrongFeedback)
        }
    }
    fun host(journal: ProgressStorage, progress: ProgressRepository) = DurableSessionHost(journal, progress,
        activity, REVISION, SeasonsContent.repository, ::generate, ::validate)
}
