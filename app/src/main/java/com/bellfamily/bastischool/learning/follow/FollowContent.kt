package com.bellfamily.bastischool.learning.follow

import com.bellfamily.bastischool.learning.content.*
import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.progress.*
import com.bellfamily.bastischool.learning.session.*

data class FollowObject(val id: ContentId, val text: ContentText, val assetPath: String) {
    val definition get() = SemanticObjectDefinition(id, text)
}

object FollowContent {
    const val REVISION = 1
    val objects = listOf(
        FollowObject(ContentId("object.follow.crocodile"), ContentText.plain("the crocodile", "das Krokodil"), "Animals/canonical/crocodile.png"),
        FollowObject(ContentId("object.follow.dinosaur"), ContentText.plain("the dinosaur", "der Dinosaurier"), "Animals/canonical/dinosaur.png"),
        FollowObject(ContentId("object.follow.snake"), ContentText.plain("the snake", "die Schlange"), "Animals/canonical/snake.png"),
        FollowObject(ContentId("object.follow.fish"), ContentText.plain("the fish", "der Fisch"), "Animals/canonical/fish.png"),
    )
    private val byId = objects.associateBy { it.id }
    val repository: ContentRepository = BundledContentRepository(ContentVersion(1, 1), objects.map { it.definition }, emptyList())
    val skill = SkillId("skill.listening.one_step")
    val context = LearningContextId("context.follow.four_animals")
    val completion = ContentText.plain("Great listening!", "Gut zugehört!")
    fun objectFor(id: ContentId) = byId[id] ?: error("Unknown follow object: ${id.value}")
    fun instruction(target: FollowObject) = ContentText(
        LocalizedText("Touch ${target.text.display.en}.", "Tippe auf ${target.text.display.de}.") ,
        LocalizedText("Touch ${target.text.speech.en}.", "Tippe auf ${target.text.speech.de}.")
    )
    val correctFeedback = ContentText.plain("Great listening!", "Gut zugehört!")
    val wrongFeedback = ContentText.plain("Try again. Listen carefully.", "Versuch es noch einmal. Hör gut zu.")
    val hint = ContentText.plain("Listen for the animal name.", "Hör auf den Namen des Tieres.")
    fun question(target: FollowObject): ChoiceQuestion = ChoiceQuestion(
        // ChoiceQuestion's established task identity grammar is task.*; the authored
        // instruction meaning remains the one-step follow.touch target in this ID.
        TaskDefinitionId("task.follow.touch.${target.id.value.substringAfterLast('.') }"), skill, context, 1,
        instruction(target), objects.map { it.id }, target.id, correctFeedback, wrongFeedback, hint
    )
    fun generate(id: SessionId, round: RoundLength, seed: Long): GenerationResult {
        val random = java.util.Random(seed)
        // CandidateTaskGenerator shuffles a unique four-item cycle and repeats it for 10.
        // This gives deterministic order without immediate target repetition.
        val candidates = objects.shuffled(random).map(::question)
        return CandidateTaskGenerator(candidates).generate(
            GenerationRequest(id, ActivityId("activity.follow.instructions"), REVISION, SessionPolicy(round), seed, completion), repository)
    }
    fun validate(state: SessionState) {
        require(state.plan.activity == ActivityId("activity.follow.instructions"))
        require(state.plan.activityRevision == REVISION && state.plan.contentVersion == repository.version)
        state.plan.tasks.forEach { task ->
            // CandidateTaskGenerator deterministically shuffles the presented objects;
            // the checkpoint owns that order, so validate the semantic set rather than
            // requiring the pre-generation catalogue order.
            require(task.question.choices.toSet() == objects.map { it.id }.toSet())
            val target = objectFor(task.question.correct)
            val expected = question(target)
            require(task.question.definition == expected.definition && task.question.instruction == expected.instruction &&
                task.question.choices.toSet() == expected.choices.toSet() && task.question.correct == expected.correct &&
                task.question.skill == expected.skill && task.question.context == expected.context &&
                task.question.correctFeedback == expected.correctFeedback && task.question.wrongFeedback == expected.wrongFeedback &&
                task.question.hint == expected.hint)
        }
    }
    fun host(journal: ProgressStorage, progress: ProgressRepository) = DurableSessionHost(
        journal, progress, ActivityId("activity.follow.instructions"), REVISION, repository,
        { id, round, seed -> generate(id, round, seed) }, ::validate
    )
}
