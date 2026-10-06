package com.bellfamily.bastischool.learning.numberorder

import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.progress.*
import com.bellfamily.bastischool.learning.session.*
import com.bellfamily.bastischool.learning.subitising.SubitisingContent
import java.util.Collections
import java.util.Random

object NumberOrderContent {
    enum class Type { BEFORE, AFTER, MISSING }
    sealed interface Item {
        val type: Type
        val answer: Int
    }
    data class Neighbour(override val type: Type, val anchor: Int) : Item {
        init {
            require(type == Type.BEFORE && anchor in 2..5 || type == Type.AFTER && anchor in 1..4)
        }
        override val answer get() = anchor + if (type == Type.BEFORE) -1 else 1
    }
    data class Missing(val start: Int, val length: Int, val gap: Int) : Item {
        init { require(length in 3..4 && start >= 1 && start + length - 1 <= 5 && gap in 0 until length) }
        override val type = Type.MISSING
        override val answer get() = start + gap
        val tiles: List<Int?> get() = List(length) { if (it == gap) null else start + it }
    }
    val activity = ActivityId("activity.math.number_order")
    val context = LearningContextId("context.math.number_sequence")
    val title = ContentText.plain("Number Order", "Zahlenfolge")
    val completion = SubitisingContent.completion
    val repository = SubitisingContent.repository
    const val REVISION = 1
    private fun name(n: Int) = SubitisingContent.numbers[n - 1].value.substringAfter('.')
    private fun number(name: String) = SubitisingContent.quantity(ContentId("number.$name"))

    fun prompt(item: Item): ContentText = when (item) {
        is Neighbour -> if (item.type == Type.BEFORE)
            ContentText.plain("What comes before ${item.anchor}?", "Was kommt vor der ${item.anchor}?")
        else ContentText.plain("What comes after ${item.anchor}?", "Was kommt nach der ${item.anchor}?")
        is Missing -> ContentText.plain("Which number is missing?", "Welche Zahl fehlt?")
    }
    fun help(item: Item): ContentText = when (item) {
        is Neighbour -> if (item.type == Type.BEFORE)
            ContentText.plain("Think about the number just before ${item.anchor}.", "Denk an die Zahl direkt vor der ${item.anchor}.")
        else ContentText.plain("Think about the number just after ${item.anchor}.", "Denk an die Zahl direkt nach der ${item.anchor}.")
        is Missing -> ContentText.plain("Read the numbers in order and find the gap.", "Lies die Zahlen der Reihe nach und finde die Lücke.")
    }
    fun sequenceDescription(item: Missing, language: ContentLanguage): String {
        val de = language == ContentLanguage.GERMAN
        return (if (de) "Zahlenfolge: " else "Sequence: ") + item.tiles.joinToString(", ") { it?.toString() ?: if (de) "Lücke" else "blank" }
    }
    fun question(item: Item, choices: List<ContentId>): ChoiceQuestion {
        val suffix = when (item) {
            is Neighbour -> "${item.type.name.lowercase()}.${name(item.anchor)}"
            is Missing -> "missing.${name(item.start)}.${name(item.start + item.length - 1)}.${name(item.answer)}"
        }
        return ChoiceQuestion(TaskDefinitionId("task.math.number_order.$suffix"),
            SkillId("skill.math.number_order.${item.type.name.lowercase()}"), context, 1, prompt(item), choices,
            SubitisingContent.numbers[item.answer - 1], ContentText.plain("Yes, ${item.answer}!", "Ja, ${item.answer}!"),
            ContentText.plain("Try again.", "Versuch es noch einmal."), help(item))
    }
    fun definition(q: ChoiceQuestion): Item {
        val p = q.definition.value.split('.')
        require(p.size in listOf(5, 7) && p.take(3) == listOf("task", "math", "number_order"))
        val type = Type.valueOf(p[3].uppercase())
        return if (type == Type.MISSING) {
            require(p.size == 7)
            val start = number(p[4])
            Missing(start, number(p[5]) - start + 1, number(p[6]) - start)
        } else {
            require(p.size == 5)
            Neighbour(type, number(p[4]))
        }
    }
    fun catalogue(): List<Item> = (2..5).map { Neighbour(Type.BEFORE, it) } +
        (1..4).map { Neighbour(Type.AFTER, it) } +
        (3..4).flatMap { length -> (1..(6 - length)).flatMap { start -> (0 until length).map { Missing(start, length, it) } } }

    private fun counts(round: RoundLength) = if (round == RoundLength.FIVE)
        mapOf(Type.BEFORE to 2, Type.AFTER to 2, Type.MISSING to 1)
    else mapOf(Type.BEFORE to 3, Type.AFTER to 3, Type.MISSING to 4)

    fun generate(id: SessionId, round: RoundLength, seed: Long): GenerationResult {
        val random = Random(seed)
        fun <T> shuffle(values: List<T>) = values.toMutableList().also { Collections.shuffle(it, random) }
        val selected = Type.entries.flatMap { type -> shuffle(catalogue().filter { it.type == type }).take(counts(round).getValue(type)) }
        val remaining = shuffle(selected)
        val ordered = mutableListOf<Item>()
        // Finite scan: prefer a different answer on the next question when available.
        repeat(selected.size) {
            val index = remaining.indexOfFirst { it.answer != ordered.lastOrNull()?.answer }.coerceAtLeast(0)
            ordered += remaining.removeAt(index)
        }
        val tasks = ordered.mapIndexed { i, item ->
            val correct = SubitisingContent.numbers[item.answer - 1]
            val choices = shuffle(listOf(correct) + shuffle(SubitisingContent.numbers.filter { it != correct }).take(3))
            ChoiceTask(TaskInstanceId(id, i + 1), question(item, choices))
        }
        val plan = SessionPlan(id, activity, REVISION, repository.version, SessionPolicy(round), tasks, completion)
        plan.validate(repository)
        return GenerationResult.Generated(plan)
    }
    fun validate(state: SessionState) {
        val p = state.plan
        require(p.activity == activity && p.activityRevision == REVISION && p.contentVersion == repository.version)
        require(p.policy.wrongAnswer == WrongAnswerPolicy.RETRY && p.completionText == completion)
        val items = p.tasks.map { task ->
            val q = task.question
            val item = definition(q)
            val expected = question(item, q.choices)
            require(q.choices.size == 4 && q.choices.toSet().size == 4 && q.choices.all { it in SubitisingContent.numbers })
            require(q.correct == expected.correct && q.skill == expected.skill && q.context == context && q.difficulty == 1 &&
                q.instruction == expected.instruction && q.hint == expected.hint && q.correctFeedback == expected.correctFeedback && q.wrongFeedback == expected.wrongFeedback)
            item
        }
        require(items.size == p.policy.round.count && items.toSet().size == items.size)
        require(items.groupingBy { it.type }.eachCount() == counts(p.policy.round))
    }
    fun host(journal: ProgressStorage, progress: ProgressRepository) = DurableSessionHost(
        journal, progress, activity, REVISION, repository, ::generate, ::validate)
}
