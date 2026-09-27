package com.bellfamily.bastischool.learning.follow

import com.bellfamily.bastischool.learning.content.*
import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.progress.*
import com.bellfamily.bastischool.learning.session.*

data class FollowObject(val id: ContentId, val text: ContentText, val assetPath: String) {
    val definition get() = SemanticObjectDefinition(id, text)
}

object FollowContent {
    const val REVISION = 2
    private const val LEGACY_REVISION = 1
    private val activity = ActivityId("activity.follow.instructions")
    private val legacyObjects = listOf(
        FollowObject(ContentId("object.follow.crocodile"), ContentText.plain("the crocodile", "das Krokodil"), "Animals/canonical/crocodile.png"),
        FollowObject(ContentId("object.follow.dinosaur"), ContentText.plain("the dinosaur", "der Dinosaurier"), "Animals/canonical/dinosaur.png"),
        FollowObject(ContentId("object.follow.snake"), ContentText.plain("the snake", "die Schlange"), "Animals/canonical/snake.png"),
        FollowObject(ContentId("object.follow.fish"), ContentText.plain("the fish", "der Fisch"), "Animals/canonical/fish.png"),
    )
    val objects = legacyObjects + listOf(
        FollowObject(ContentId("object.follow.horse"), ContentText.plain("the horse", "das Pferd"), "Animals/canonical/horse.png"),
        FollowObject(ContentId("object.follow.whale"), ContentText.plain("the whale", "der Wal"), "Animals/canonical/whale.png"),
    )
    private val byId = objects.associateBy { it.id }
    private val legacyById = legacyObjects.associateBy { it.id }
    val repository: ContentRepository = BundledContentRepository(ContentVersion(1, 2), objects.map { it.definition }, emptyList())
    private val legacyRepository: ContentRepository = BundledContentRepository(ContentVersion(1, 1), legacyObjects.map { it.definition }, emptyList())
    val skill = SkillId("skill.listening.one_step")
    val context = LearningContextId("context.follow.animal_pool")
    private val legacyContext = LearningContextId("context.follow.four_animals")
    val completion = ContentText.plain("Great listening!", "Gut zugehört!")
    fun objectFor(id: ContentId) = byId[id] ?: error("Unknown follow object: ${id.value}")
    fun instruction(target: FollowObject) = ContentText(
        LocalizedText("Touch ${target.text.display.en}.", "Tippe auf ${target.text.display.de}."),
        LocalizedText("Touch ${target.text.speech.en}.", "Tippe auf ${target.text.speech.de}.")
    )
    val correctFeedback = ContentText.plain("Great listening!", "Gut zugehört!")
    val wrongFeedback = ContentText.plain("Try again. Listen carefully.", "Versuch es noch einmal. Hör gut zu.")
    val hint = ContentText.plain("Listen for the animal name.", "Hör auf den Namen des Tieres.")
    fun question(target: FollowObject, presented: List<FollowObject>): ChoiceQuestion = ChoiceQuestion(
        TaskDefinitionId("task.follow.touch.${target.id.value.substringAfterLast('.')}.${presented.map { it.id.value.substringAfterLast('.') }.sorted().joinToString("_")}"),
        skill, context, 1, instruction(target), presented.map { it.id }, target.id, correctFeedback, wrongFeedback, hint
    )
    private fun legacyQuestion(target: FollowObject) = ChoiceQuestion(
        TaskDefinitionId("task.follow.touch.${target.id.value.substringAfterLast('.') }"), skill, legacyContext, 1,
        instruction(target), legacyObjects.map { it.id }, target.id, correctFeedback, wrongFeedback, hint
    )
    fun generate(id: SessionId, round: RoundLength, seed: Long): GenerationResult {
        val candidates = objects.flatMap { target ->
            objects.filter { it != target }.combinations(3).map { distractors -> question(target, listOf(target) + distractors) }
        }
        return CandidateTaskGenerator(candidates).generate(
            GenerationRequest(id, activity, REVISION, SessionPolicy(round), seed, completion), repository)
    }
    fun validate(state: SessionState) {
        require(state.plan.activity == activity)
        if (state.plan.activityRevision == LEGACY_REVISION && state.plan.contentVersion == legacyRepository.version) { validateLegacy(state); return }
        require(state.plan.activityRevision == REVISION && state.plan.contentVersion == repository.version)
        state.plan.tasks.forEach { task ->
            require(task.question.choices.size == 4 && task.question.choices.toSet().size == 4 && task.question.correct in task.question.choices)
            val expected = question(objectFor(task.question.correct), task.question.choices.map(::objectFor))
            require(task.question.definition == expected.definition && task.question.instruction == expected.instruction &&
                task.question.choices.toSet() == expected.choices.toSet() && task.question.correct == expected.correct &&
                task.question.skill == expected.skill && task.question.context == expected.context &&
                task.question.correctFeedback == expected.correctFeedback && task.question.wrongFeedback == expected.wrongFeedback && task.question.hint == expected.hint)
        }
    }
    private fun validateLegacy(state: SessionState) {
        state.plan.tasks.forEach { task ->
            require(task.question.choices.size == 4 && task.question.choices.toSet() == legacyObjects.map { it.id }.toSet())
            val expected = legacyQuestion(legacyById[task.question.correct] ?: error("Unknown legacy target"))
            require(task.question.definition == expected.definition && task.question.instruction == expected.instruction &&
                task.question.choices.toSet() == expected.choices.toSet() && task.question.correct == expected.correct &&
                task.question.skill == expected.skill && task.question.context == expected.context &&
                task.question.correctFeedback == expected.correctFeedback && task.question.wrongFeedback == expected.wrongFeedback && task.question.hint == expected.hint)
        }
    }
    fun host(journal: ProgressStorage, progress: ProgressRepository) = DurableSessionHost(
        journal, progress, activity, REVISION, repository, { id, round, seed -> generate(id, round, seed) }, ::validate,
        { bytes ->
            val current = SessionCheckpoint.restore(bytes, activity, REVISION, repository)
            if (current is SessionRestoreResult.Restored) current else SessionCheckpoint.restore(bytes, activity, LEGACY_REVISION, legacyRepository)
        }
    )
    private fun <T> List<T>.combinations(size: Int): List<List<T>> {
        if (size == 0) return listOf(emptyList())
        if (size > this.size) return emptyList()
        return indices.flatMap { index -> drop(index + 1).combinations(size - 1).map { listOf(get(index)) + it } }
    }
}
