package com.bellfamily.bastischool.learning.schoolskills

import com.bellfamily.bastischool.learning.content.BundledContentRepository
import com.bellfamily.bastischool.learning.content.ContentRepository
import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.progress.*
import com.bellfamily.bastischool.learning.session.*
import java.util.Collections
import java.util.Random

/** Accessible two-tap item placement; only the destination answer is a scored attempt. */
object ClassroomHelperContent {
    const val REVISION = 1
    val activity = ActivityId("activity.school.classroom_helper")
    val skill = SkillId("skill.school.classroom_item_placement")
    val context = LearningContextId("context.school.classroom_objects_and_places")
    val completion = ContentText.plain("You put the classroom items in the right places!", "Du hast die Klassenzimmer-Sachen an die richtigen Plätze gelegt!")

    data class Item(val id: ContentId, val label: ContentText, val asset: String)
    data class Place(val id: ContentId, val label: ContentText, val asset: String)
    data class Prompt(val definition: TaskDefinitionId, val item: Item, val place: Place, val instruction: ContentText)

    val items = listOf(
        Item(ContentId("school_item.book"), ContentText.plain("Book", "Buch"), "school_actions/board/object_book.png"),
        Item(ContentId("school_item.pencil"), ContentText.plain("Pencil", "Bleistift"), "school_actions/board/object_pencil.png"),
        Item(ContentId("school_item.paper"), ContentText.plain("Paper", "Blatt"), "school_actions/board/object_paper.png"),
        Item(ContentId("school_item.bag"), ContentText.plain("Backpack", "Rucksack"), "school_actions/board/object_bag.png")
    )
    val places = listOf(
        Place(ContentId("school_place.desk"), ContentText.plain("On the desk", "Auf dem Tisch"), "school_actions/board/place_desk.png"),
        Place(ContentId("school_place.cup"), ContentText.plain("In the pencil cup", "In den Stiftebecher"), "school_actions/board/place_cup.png"),
        Place(ContentId("school_place.teacher"), ContentText.plain("To the teacher", "Zur Lehrkraft"), "school_actions/board/place_teacher.png"),
        Place(ContentId("school_place.shelf"), ContentText.plain("On the bookshelf", "Ins Bücherregal"), "school_actions/board/place_shelf.png"),
        Place(ContentId("school_place.under_table"), ContentText.plain("Under the table", "Unter den Tisch"), "school_actions/board/place_under_table.png"),
        Place(ContentId("school_place.inside_bag"), ContentText.plain("Inside the backpack", "In den Rucksack"), "school_actions/board/object_bag.png"),
        Place(ContentId("school_place.beside_shelf"), ContentText.plain("Next to the bookshelf", "Neben das Bücherregal"), "school_actions/board/place_beside_shelf.png")
    )
    private val itemByKey = items.associateBy { it.id.value.substringAfterLast('.') }
    private val placeByKey = places.associateBy { it.id.value.substringAfterLast('.') }
    private val placeById = places.associateBy { it.id }

    private fun prompt(item: String, place: String, en: String, de: String): Prompt = Prompt(
        TaskDefinitionId("task.school.place.$item.$place"), itemByKey.getValue(item), placeByKey.getValue(place), ContentText.plain(en, de)
    )
    val prompts = listOf(
        prompt("book", "desk", "Put the book on the desk.", "Leg das Buch auf den Tisch."),
        prompt("book", "shelf", "Put the book on the bookshelf.", "Stell das Buch ins Regal."),
        prompt("book", "inside_bag", "Put the book in the backpack.", "Leg das Buch in den Rucksack."),
        prompt("pencil", "cup", "Put the pencil in the pencil cup.", "Steck den Bleistift in den Stiftebecher."),
        prompt("pencil", "desk", "Put the pencil on the desk.", "Leg den Bleistift auf den Tisch."),
        prompt("pencil", "teacher", "Give the pencil to the teacher.", "Gib den Bleistift der Lehrkraft."),
        prompt("paper", "teacher", "Give the paper to the teacher.", "Gib das Blatt der Lehrkraft."),
        prompt("paper", "desk", "Put the paper on the desk.", "Leg das Blatt auf den Tisch."),
        prompt("paper", "shelf", "Put the paper on the bookshelf.", "Leg das Blatt ins Regal."),
        prompt("bag", "under_table", "Put the backpack under the table.", "Leg den Rucksack unter den Tisch."),
        prompt("bag", "beside_shelf", "Put the backpack next to the bookshelf.", "Stell den Rucksack neben das Regal."),
        prompt("bag", "desk", "Put the backpack on the desk.", "Leg den Rucksack auf den Tisch.")
    )
    private val promptByDefinition = prompts.associateBy { it.definition }
    val repository: ContentRepository = BundledContentRepository(ContentVersion(1, 0), places.map {
        SemanticObjectDefinition(it.id, it.label)
    }, emptyList())

    private val correct = ContentText.plain("That is the right place. Nice helping!", "Das ist der richtige Platz. Gut geholfen!")
    private val wrong = ContentText.plain("That is not the place in the instruction. Listen and try again.", "Das ist nicht der Platz aus der Anweisung. Hör noch einmal hin und versuch es erneut.")
    private val hint = ContentText.plain("Listen for where the item should go.", "Hör genau hin, wohin der Gegenstand soll.")

    fun promptFor(definition: TaskDefinitionId): Prompt = promptByDefinition[definition] ?: error("Unknown classroom placement: ${definition.value}")

    fun question(prompt: Prompt, choices: List<ContentId> = places.map { it.id }): ChoiceQuestion {
        require(choices.size == places.size && choices.toSet() == placeById.keys && prompt.place.id in choices)
        choices.forEach { require(it in placeById) }
        return ChoiceQuestion(prompt.definition, skill, context, 1, prompt.instruction, choices, prompt.place.id, correct, wrong, hint)
    }

    fun generate(id: SessionId, round: RoundLength, seed: Long): GenerationResult {
        val random = Random(seed)
        val groups = prompts.groupBy { it.item.id }.mapValues { (_, group) -> group.toMutableList().also { Collections.shuffle(it, random) } }
        val each = round.count / items.size
        val extra = (0 until items.size).toMutableList().also { Collections.shuffle(it, random) }.take(round.count % items.size).toSet()
        val selected = items.flatMapIndexed { index, item -> groups.getValue(item.id).take(each + if (index in extra) 1 else 0) }
        fun arrange(left: List<Prompt>, previous: ContentId?): List<Prompt>? {
            if (left.isEmpty()) return emptyList()
            val options = left.filter { it.item.id != previous }.toMutableList()
            Collections.shuffle(options, random)
            for (option in options) {
                val suffix = arrange(left.toMutableList().also { it.remove(option) }, option.item.id) ?: continue
                return listOf(option) + suffix
            }
            return null
        }
        val ordered = requireNotNull(arrange(selected, null))
        val tasks = ordered.mapIndexed { index, prompt ->
            val choices = places.map { it.id }.toMutableList().also { Collections.shuffle(it, random) }
            ChoiceTask(TaskInstanceId(id, index + 1), question(prompt, choices))
        }
        val plan = SessionPlan(id, activity, REVISION, repository.version, SessionPolicy(round), tasks, completion)
        plan.validate(repository)
        return GenerationResult.Generated(plan)
    }

    fun validate(state: SessionState) {
        require(state.plan.activity == activity && state.plan.activityRevision == REVISION && state.plan.contentVersion == repository.version)
        require(state.plan.completionText == completion && state.plan.policy.wrongAnswer == WrongAnswerPolicy.RETRY)
        val promptsInRound = state.plan.tasks.map { promptFor(it.question.definition) }
        require(promptsInRound.distinctBy { it.definition }.size == promptsInRound.size)
        require(promptsInRound.zipWithNext().none { (a, b) -> a.item.id == b.item.id })
        state.plan.tasks.forEach { task ->
            val expected = question(promptFor(task.question.definition), task.question.choices)
            val actual = task.question
            require(actual == expected)
        }
    }

    fun host(journal: ProgressStorage, progress: ProgressRepository) = DurableSessionHost(
        journal, progress, activity, REVISION, repository,
        { id, round, seed -> generate(id, round, seed) }, ::validate
    )
}
