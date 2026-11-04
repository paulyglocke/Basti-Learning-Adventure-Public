package com.bellfamily.bastischool.learning.schoolskills

import com.bellfamily.bastischool.learning.content.BundledContentRepository
import com.bellfamily.bastischool.learning.content.ContentRepository
import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.progress.*
import com.bellfamily.bastischool.learning.session.*
import java.util.Collections
import java.util.Random

/** Three-action listening comprehension using the same classroom action pictures. */
object SchoolActionTripleContent {
    const val REVISION = 1
    val activity = ActivityId("activity.school.classroom_actions.three_step")
    val skill = SkillId("skill.school.classroom_action_sequence_three")
    val context = LearningContextId("context.school.routines_and_objects.three_order")
    val completion = ContentText.plain("You matched three actions in order!", "Du hast drei Aktionen der Reihe nach richtig zugeordnet!")

    data class Sequence(val id: ContentId, val first: SchoolActionsContent.Action,
        val second: SchoolActionsContent.Action, val third: SchoolActionsContent.Action,
        val label: ContentText, val instruction: ContentText)

    val sequences: List<Sequence> = SchoolActionsContent.actions.flatMap { first ->
        SchoolActionsContent.actions.filter { it.id != first.id }.flatMap { second ->
            SchoolActionsContent.actions.filter { it.id != first.id && it.id != second.id }.map { third ->
                val a = first.id.value.substringAfterLast('.')
                val b = second.id.value.substringAfterLast('.')
                val c = third.id.value.substringAfterLast('.')
                Sequence(
                    ContentId("school_action_triple.${a}_then_${b}_then_${c}"), first, second, third,
                    ContentText.plain("${first.label.display.en}, then ${second.label.display.en}, then ${third.label.display.en}",
                        "${first.label.display.de}, dann ${second.label.display.de}, dann ${third.label.display.de}"),
                    ContentText.plain("First ${lower(first.instruction.display.en).removeSuffix(".")}, then ${lower(second.instruction.display.en).removeSuffix(".")}, and finally ${lower(third.instruction.display.en)}",
                        "Zuerst ${lower(first.instruction.display.de).removeSuffix(".")}, dann ${lower(second.instruction.display.de).removeSuffix(".")}, und zum Schluss ${lower(third.instruction.display.de)}")
                )
            }
        }
    }
    private val byId = sequences.associateBy { it.id }
    private val indexById = sequences.mapIndexed { index, sequence -> sequence.id to index }.toMap()
    val repository: ContentRepository = BundledContentRepository(ContentVersion(1, 0), sequences.map {
        SemanticObjectDefinition(it.id, it.label)
    }, emptyList())

    private val correct = ContentText.plain("That is the right order. Great listening!", "Das ist die richtige Reihenfolge. Toll zugehört!")
    private val wrong = ContentText.plain("Listen for what comes first, next, and last.", "Hör genau hin: Was kommt zuerst, danach und zum Schluss?")
    private val hint = ContentText.plain("Listen for “first”, “then”, and “finally”.", "Achte auf „zuerst“, „dann“ und „zum Schluss“." )

    fun sequence(id: ContentId): Sequence = byId[id] ?: error("Unknown three-action sequence: ${id.value}")

    fun question(target: ContentId, choices: List<ContentId>): ChoiceQuestion {
        require(choices.size == 4 && choices.toSet().size == 4 && target in choices)
        choices.forEach(::sequence)
        val targetIndex = indexById.getValue(target)
        val choiceKey = choices.map(indexById::getValue).sorted().joinToString("_")
        return ChoiceQuestion(TaskDefinitionId("task.school.action_order_three.$targetIndex.$choiceKey"),
            skill, context, 3, sequence(target).instruction, choices, target, correct, wrong, hint)
    }

    fun generate(id: SessionId, round: RoundLength, seed: Long): GenerationResult {
        val random = Random(seed)
        val targets = sequences.map { it.id }.toMutableList().also { Collections.shuffle(it, random) }.take(round.count)
        val tasks = targets.mapIndexed { index, target ->
            val targetSequence = sequence(target)
            val reverse = sequences.first { it.first.id == targetSequence.third.id && it.second.id == targetSequence.second.id && it.third.id == targetSequence.first.id }.id
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
            val expectedReverse = sequence(actual.correct).let { target ->
                sequences.first { it.first.id == target.third.id && it.second.id == target.second.id && it.third.id == target.first.id }.id
            }
            require(expectedReverse in actual.choices)
        }
    }

    fun host(journal: ProgressStorage, progress: ProgressRepository) = DurableSessionHost(
        journal, progress, activity, REVISION, repository,
        { id, round, seed -> generate(id, round, seed) }, ::validate
    )

    private fun lower(value: String) = value.replaceFirstChar { it.lowercase() }
}
