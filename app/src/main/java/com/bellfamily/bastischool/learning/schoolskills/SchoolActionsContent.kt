package com.bellfamily.bastischool.learning.schoolskills

import com.bellfamily.bastischool.learning.content.BundledContentRepository
import com.bellfamily.bastischool.learning.content.ContentRepository
import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.progress.*
import com.bellfamily.bastischool.learning.session.*
import java.util.Collections
import java.util.Random

/** Listening comprehension through matching classroom instructions to action pictures. */
object SchoolActionsContent {
    const val REVISION = 1
    val activity = ActivityId("activity.school.classroom_actions")
    val skill = SkillId("skill.school.classroom_action_comprehension")
    val context = LearningContextId("context.school.routines_and_objects")
    val completion = ContentText.plain("You followed the classroom instructions!", "Du hast die Anweisungen im Klassenzimmer befolgt!")

    data class Action(val id: ContentId, val label: ContentText, val instruction: ContentText, val illustrationAsset: String)

    val actions = listOf(
        Action(ContentId("school_action.book_desk"), ContentText.plain("Book on the desk", "Buch auf dem Tisch"),
            ContentText.plain("Put the book on the desk.", "Leg das Buch auf den Tisch."), "school_actions/book_desk.png"),
        Action(ContentId("school_action.pencil_cup"), ContentText.plain("Pencil in the cup", "Bleistift in den Becher"),
            ContentText.plain("Put the pencil in the cup.", "Steck den Bleistift in den Becher."), "school_actions/pencil_cup.png"),
        Action(ContentId("school_action.paper_teacher"), ContentText.plain("Give the paper to the teacher", "Gib das Blatt der Lehrkraft"),
            ContentText.plain("Give the paper to the teacher.", "Gib das Blatt der Lehrkraft."), "school_actions/paper_teacher.png"),
        Action(ContentId("school_action.bag_under_table"), ContentText.plain("Bag under the table", "Tasche unter den Tisch"),
            ContentText.plain("Put the bag under the table.", "Leg die Tasche unter den Tisch."), "school_actions/bag_under_table.png"),
        Action(ContentId("school_action.book_shelf"), ContentText.plain("Book on the shelf", "Buch ins Regal"),
            ContentText.plain("Put the book on the shelf.", "Stell das Buch ins Regal."), "school_actions/book_shelf.png")
    )
    private val byId = actions.associateBy { it.id }
    val repository: ContentRepository = BundledContentRepository(ContentVersion(1, 0), actions.map {
        SemanticObjectDefinition(it.id, it.label)
    }, emptyList())

    private val correct = ContentText.plain("You found the picture that matches the instruction.", "Du hast das Bild zur Anweisung gefunden.")
    private val wrong = ContentText.plain("Listen for the object and where it should go, then try again.", "Hör auf den Gegenstand und wohin er soll, und versuch es noch einmal.")
    private val hint = ContentText.plain("Which object did you hear? Where should it go?", "Welchen Gegenstand hast du gehört? Wohin soll er?")

    fun action(id: ContentId): Action = byId[id] ?: error("Unknown classroom action: ${id.value}")

    fun question(target: ContentId, choices: List<ContentId>): ChoiceQuestion {
        require(choices.size == 4 && choices.toSet().size == 4 && target in choices)
        choices.forEach(::action)
        val key = target.value.substringAfterLast('.')
        val taskChoices = choices.map { it.value.substringAfterLast('.') }.sorted().joinToString("_")
        return ChoiceQuestion(
            TaskDefinitionId("task.school.action.$key.$taskChoices"), skill, context, 1,
            action(target).instruction, choices, target, correct, wrong, hint
        )
    }

    fun generate(id: SessionId, round: RoundLength, seed: Long): GenerationResult {
        val random = Random(seed)
        val targets = actions.flatMap { action -> List(round.count / actions.size) { action.id } }
        fun arrange(left: List<ContentId>, previous: ContentId?): List<ContentId>? {
            if (left.isEmpty()) return emptyList()
            val options = left.distinct().filter { it != previous }.toMutableList()
            Collections.shuffle(options, random)
            for (option in options) {
                val rest = left.toMutableList().also { it.remove(option) }
                val suffix = arrange(rest, option) ?: continue
                return listOf(option) + suffix
            }
            return null
        }
        val arranged = requireNotNull(arrange(targets, null))
        val choiceSets = actions.associate { target ->
            target.id to combinations(actions.map { it.id }.filter { it != target.id }, 3)
                .map { listOf(target.id) + it }.toMutableList()
        }
        val tasks = arranged.mapIndexed { index, target ->
            val options = choiceSets.getValue(target).also { Collections.shuffle(it, random) }
            check(options.isNotEmpty())
            val choices = options.removeAt(0).toMutableList().also { Collections.shuffle(it, random) }
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
        require(actions.all { action -> targets.count { it == action.id } == state.plan.tasks.size / actions.size })
        require(targets.zipWithNext().none { (a, b) -> a == b })
        state.plan.tasks.forEach { task ->
            val actual = task.question
            val expected = question(actual.correct, actual.choices)
            require(actual.definition == expected.definition && actual.skill == expected.skill && actual.context == expected.context &&
                actual.difficulty == expected.difficulty && actual.instruction == expected.instruction &&
                actual.correctFeedback == expected.correctFeedback && actual.wrongFeedback == expected.wrongFeedback && actual.hint == expected.hint)
        }
    }

    fun host(journal: ProgressStorage, progress: ProgressRepository) = DurableSessionHost(
        journal, progress, activity, REVISION, repository,
        { id, round, seed -> generate(id, round, seed) }, ::validate
    )

    private fun <T> combinations(values: List<T>, size: Int): List<List<T>> {
        if (size == 0) return listOf(emptyList())
        if (size > values.size) return emptyList()
        return values.indices.flatMap { index ->
            combinations(values.drop(index + 1), size - 1).map { listOf(values[index]) + it }
        }
    }
}
