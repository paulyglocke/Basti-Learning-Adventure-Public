package com.bellfamily.bastischool.learning.seasons

import com.bellfamily.bastischool.learning.content.*
import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.session.*
import com.bellfamily.bastischool.learning.progress.*

/** Activity content references the existing canonical pack; no copied season descriptions or paths. */
object SeasonsContent {
    val repository = CoreContent.repository()
    val activity = ActivityId("activity.seasons")
    const val REVISION = 1
    val completion = ContentText.plain("Adventure complete! Well done!", "Abenteuer geschafft! Sehr gut!")
    fun season(id: ContentId): SeasonDefinition = repository.find(id) as? SeasonDefinition
        ?: throw IllegalArgumentException("Unknown season")
    fun image(id: ContentId): LocalImageAsset = repository.image(season(id).illustration)!!
    fun narration(id: ContentId): ContentText = season(id).spokenDescription.let { ContentText(it, it) }
    fun generate(id: SessionId, round: RoundLength, seed: Long): GenerationResult {
        val candidates = repository.seasons().map { question(it.id, SeasonIds.canonicalOrder) }
        val generated = CandidateTaskGenerator(candidates).generate(
            GenerationRequest(id, activity, REVISION, SessionPolicy(round), seed, completion), repository)
        if (generated !is GenerationResult.Generated) return generated
        val p = generated.plan
        // Author the spoken choice enumeration AFTER the shared generator fixes its order.
        return GenerationResult.Generated(SessionPlan(p.id, p.activity, p.activityRevision, p.contentVersion,
            p.policy, p.tasks.map { ChoiceTask(it.id, question(it.question.correct, it.question.choices)) }, completion))
    }
    fun question(correct: ContentId, choices: List<ContentId>): ChoiceQuestion {
        val target = season(correct)
        require(choices.size == 4 && choices.toSet() == SeasonIds.canonicalOrder.toSet())
        val instruction = ContentText.plain(
            "Which season is this? Look at the picture. Choose: ${choices.joinToString(", ") { season(it).text.speech.en }}.",
            "Welche Jahreszeit ist das? Schau dir das Bild an. Wähle: ${choices.joinToString(", ") { season(it).text.speech.de }}.")
        return ChoiceQuestion(TaskDefinitionId("task.seasons.${correct.value.substringAfter('.')}.recognise"),
            SkillId("skill.seasons.${correct.value.substringAfter('.')}.recognise"),
            LearningContextId("context.seasons.lakeside_tree"), 1, instruction, choices, correct,
            ContentText.plain("Great! ${target.text.speech.en}.", "Super! ${target.text.speech.de}."),
            ContentText.plain("Try again!", "Nochmal versuchen!"), narration(correct))
    }
    fun validate(state: SessionState) {
        require(state.plan.activity == activity && state.plan.activityRevision == REVISION && state.plan.contentVersion == repository.version)
        require(state.plan.completionText == completion && state.plan.policy.wrongAnswer == WrongAnswerPolicy.RETRY)
        state.plan.tasks.forEach {
            val actual = it.question; val expected = question(actual.correct, actual.choices)
            require(actual.definition == expected.definition && actual.skill == expected.skill && actual.context == expected.context &&
                actual.difficulty == expected.difficulty && actual.instruction == expected.instruction &&
                actual.correctFeedback == expected.correctFeedback && actual.wrongFeedback == expected.wrongFeedback && actual.hint == expected.hint)
        }
    }
    fun host(journal: ProgressStorage, progress: ProgressRepository) = DurableSessionHost(journal, progress,
        activity, REVISION, repository, ::generate, ::validate)
}

enum class SeasonsPhase { EXPLORE, PRACTICE }
/** Browsing is not a learning attempt. Language belongs to settings, never season identity. */
data class SeasonsSelection(val selected: ContentId = SeasonIds.SPRING, val phase: SeasonsPhase = SeasonsPhase.EXPLORE) {
    init { SeasonsContent.season(selected) }
    val season get() = SeasonsContent.season(selected)
    val image get() = SeasonsContent.image(selected)
    fun select(id: ContentId) = copy(selected = id)
}
