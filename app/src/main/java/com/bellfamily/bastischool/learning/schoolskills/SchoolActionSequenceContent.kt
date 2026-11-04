package com.bellfamily.bastischool.learning.schoolskills

import com.bellfamily.bastischool.learning.content.BundledContentRepository
import com.bellfamily.bastischool.learning.content.ContentRepository
import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.progress.*
import com.bellfamily.bastischool.learning.session.*
import java.util.Collections
import java.util.Random

/** Ordered two-action comprehension, with the same actions as the one-step round. */
object SchoolActionSequenceContent {
    const val REVISION = 1
    val activity = ActivityId("activity.school.classroom_actions.two_step")
    val skill = SkillId("skill.school.classroom_action_order")
    val context = LearningContextId("context.school.routines_and_objects.order")
    val completion = ContentText.plain("You matched two actions in order!", "Du hast zwei Aktionen der Reihe nach richtig zugeordnet!")

    data class Sequence(val id: ContentId, val first: SchoolActionsContent.Action, val second: SchoolActionsContent.Action,
        val label: ContentText, val instruction: ContentText)

    val sequences: List<Sequence> = SchoolActionsContent.actions.flatMap { first ->
        SchoolActionsContent.actions.filter { it.id != first.id }.map { second ->
            val firstKey = first.id.value.substringAfterLast('.')
            val secondKey = second.id.value.substringAfterLast('.')
            Sequence(
                ContentId("school_action_pair.${firstKey}_then_${secondKey}"), first, second,
                ContentText.plain("${first.label.display.en}, then ${second.label.display.en}",
                    "${first.label.display.de}, dann ${second.label.display.de}"),
                ContentText.plain("First ${first.instruction.display.en.replaceFirstChar { it.lowercase() }.removeSuffix(".")}, then ${second.instruction.display.en.replaceFirstChar { it.lowercase() }}",
                    "Zuerst ${first.instruction.display.de.replaceFirstChar { it.lowercase() }.removeSuffix(".")}, dann ${second.instruction.display.de.replaceFirstChar { it.lowercase() }}")
            )
        }
    }
    private val byId = sequences.associateBy { it.id }
    val repository: ContentRepository = BundledContentRepository(ContentVersion(1, 0), sequences.map {
        SemanticObjectDefinition(it.id, it.label)
    }, emptyList())

    private val correct = ContentText.plain("That is the right order. Good listening!", "Das ist die richtige Reihenfolge. Gut zugehört!")
    private val wrong = ContentText.plain("Listen for what comes first and what comes next.", "Hör genau hin: Was kommt zuerst und was danach?")
    private val hint = ContentText.plain("Look at the word “first”, then listen for “then”.", "Achte auf „zuerst“ und dann auf „danach“." )

    fun sequence(id: ContentId): Sequence = byId[id] ?: error("Unknown action sequence: ${id.value}")

    fun question(target: ContentId, choices: List<ContentId>): ChoiceQuestion {
        require(choices.size == 4 && choices.toSet().size == 4 && target in choices)
        choices.forEach(::sequence)
        val taskChoices = choices.map { it.value.substringAfter(".") }.sorted().joinToString("_")
        return ChoiceQuestion(
            TaskDefinitionId("task.school.action_order.${target.value.substringAfter(".")}.$taskChoices"),
            skill, context, 2, sequence(target).instruction, choices, target, correct, wrong, hint
        )
    }

    fun generate(id: SessionId, round: RoundLength, seed: Long): GenerationResult {
        val random = Random(seed)
        val targets = sequences.map { it.id }.toMutableList().also { Collections.shuffle(it, random) }.take(round.count)
        val tasks = targets.mapIndexed { index, target ->
            val correctSequence = sequence(target)
            val reverse = sequences.first { it.first.id == correctSequence.second.id && it.second.id == correctSequence.first.id }.id
            val distractors = sequences.map { it.id }.filter { it != target && it != reverse }
                .toMutableList().also { Collections.shuffle(it, random) }.take(2)
            val choices = (listOf(target, reverse) + distractors).toMutableList().also { Collections.shuffle(it, random) }
            ChoiceTask(TaskInstanceId(id, index + 1), question(target, choices))
        }
        val plan = SessionPlan(id, activity, REVISION, repository.version, SessionPolicy(round), tasks, completion)
        plan.validate(repository)
        return GenerationResult.Generated(plan)
    }

    fun validate(state: SessionState) {
        require(state.plan.activity == activity && state.plan.activityRevision == REVISION && state.plan.contentVersion == repository.version)
        require(state.plan.completionText == completion && state.plan.policy.wrongAnswer == WrongAnswerPolicy.RETRY)
        val targets = state.plan.tasks.map { it.question.correct }
        require(targets.distinct().size == targets.size)
        require(targets.zipWithNext().none { (a, b) -> a == b })
        state.plan.tasks.forEach { task ->
            val actual = task.question
            val expected = question(actual.correct, actual.choices)
            require(actual.definition == expected.definition && actual.skill == expected.skill && actual.context == expected.context &&
                actual.difficulty == expected.difficulty && actual.instruction == expected.instruction &&
                actual.correctFeedback == expected.correctFeedback && actual.wrongFeedback == expected.wrongFeedback && actual.hint == expected.hint)
            val expectedReverse = sequence(actual.correct).let { pair ->
                sequences.first { it.first.id == pair.second.id && it.second.id == pair.first.id }.id
            }
            require(expectedReverse in actual.choices)
        }
    }

    fun host(journal: ProgressStorage, progress: ProgressRepository) = DurableSessionHost(
        journal, progress, activity, REVISION, repository,
        { id, round, seed -> generate(id, round, seed) }, ::validate
    )
}
