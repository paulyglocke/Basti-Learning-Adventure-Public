package com.bellfamily.bastischool.learning.quantitymatch

import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.progress.*
import com.bellfamily.bastischool.learning.session.*
import com.bellfamily.bastischool.learning.subitising.SubitisingContent
import java.util.Collections
import java.util.Random

object QuantityMatchContent {
    enum class Direction { QUANTITY_TO_NUMERAL, NUMERAL_TO_QUANTITY }

    val activity = ActivityId("activity.math.quantity_match")
    val context = LearningContextId("context.math.quantity")
    val completion = SubitisingContent.completion
    const val REVISION = 1

    fun question(n: Int, direction: Direction, choices: List<ContentId>): ChoiceQuestion {
        require(n in 1..5)
        val number = SubitisingContent.numbers[n - 1]
        val quantityToNumeral = direction == Direction.QUANTITY_TO_NUMERAL
        val prompt = if (quantityToNumeral) {
            ContentText.plain("Which number matches this group?", "Welche Zahl passt zu dieser Gruppe?")
        } else {
            ContentText.plain("Which group shows $n?", "Welche Gruppe zeigt $n?")
        }
        val hint = if (quantityToNumeral) {
            ContentText.plain("Look at the whole group, then choose its number.", "Schau dir die ganze Gruppe an und wähle dann die passende Zahl.")
        } else {
            ContentText.plain("Look for the group with $n dots.", "Suche die Gruppe mit $n Punkten.")
        }
        return ChoiceQuestion(
            TaskDefinitionId("task.math.quantity_match.${direction.name.lowercase()}.${number.value.substringAfter('.') }"),
            SkillId(if (quantityToNumeral) "skill.math.quantity_to_numeral" else "skill.math.numeral_to_quantity"),
            context, 1, prompt, choices, number,
            ContentText.plain("Yes, $n!", "Ja, $n!"),
            ContentText.plain("Try again.", "Versuch es noch einmal."), hint
        )
    }

    fun definition(question: ChoiceQuestion): Pair<Int, Direction> {
        val parts = question.definition.value.split('.')
        require(parts.size == 5 && parts.take(3) == listOf("task", "math", "quantity_match"))
        return SubitisingContent.quantity(ContentId("number.${parts[4]}")) to Direction.valueOf(parts[3].uppercase())
    }

    fun generate(id: SessionId, round: RoundLength, seed: Long): GenerationResult {
        val random = Random(seed)
        fun <T> shuffled(values: List<T>) = values.toMutableList().also { Collections.shuffle(it, random) }
        val quantities = shuffled((1..5).toList())
        val startingDirection = random.nextInt(2)
        val firstDirections = List(5) { Direction.entries[(startingDirection + it) % 2] }
        val pairs = if (round == RoundLength.FIVE) {
            quantities.mapIndexed { index, n -> n to firstDirections[index] }
        } else {
            quantities.mapIndexed { index, n -> n to firstDirections[index] } +
                quantities.mapIndexed { index, n -> n to if (firstDirections[index] == Direction.QUANTITY_TO_NUMERAL) Direction.NUMERAL_TO_QUANTITY else Direction.QUANTITY_TO_NUMERAL }
        }
        val tasks = pairs.mapIndexed { index, (n, direction) ->
            val correct = SubitisingContent.numbers[n - 1]
            val choices = shuffled(listOf(correct) + shuffled(SubitisingContent.numbers.filter { it != correct }).take(3))
            ChoiceTask(TaskInstanceId(id, index + 1), question(n, direction, choices))
        }
        val plan = SessionPlan(id, activity, REVISION, SubitisingContent.repository.version,
            SessionPolicy(round), tasks, completion)
        plan.validate(SubitisingContent.repository)
        return GenerationResult.Generated(plan)
    }

    fun validate(state: SessionState) {
        val plan = state.plan
        require(plan.activity == activity && plan.activityRevision == REVISION)
        require(plan.contentVersion == SubitisingContent.repository.version)
        require(plan.policy.wrongAnswer == WrongAnswerPolicy.RETRY && plan.completionText == completion)
        val pairs = plan.tasks.map { task ->
            val q = task.question
            val (n, direction) = definition(q)
            require(q.choices.size == 4 && q.choices.toSet().size == 4 && q.choices.all { it in SubitisingContent.numbers })
            val expected = question(n, direction, q.choices)
            require(q.difficulty == expected.difficulty && q.skill == expected.skill && q.context == context && q.instruction == expected.instruction &&
                q.hint == expected.hint && q.correct == expected.correct && q.correctFeedback == expected.correctFeedback &&
                q.wrongFeedback == expected.wrongFeedback)
            n to direction
        }
        require(pairs.map { it.first }.toSet() == (1..5).toSet())
        require(pairs.size == plan.policy.round.count && pairs.groupingBy { it.first }.eachCount().values.all { it == plan.policy.round.count / 5 })
        require(pairs.toSet().size == pairs.size)
        require(pairs.zipWithNext().all { (a, b) -> a.first != b.first })
        val counts = pairs.groupingBy { it.second }.eachCount().values.sorted()
        require(counts == if (plan.policy.round == RoundLength.FIVE) listOf(2, 3) else listOf(5, 5))
    }

    fun host(journal: ProgressStorage, progress: ProgressRepository) = DurableSessionHost(
        journal, progress, activity, REVISION, SubitisingContent.repository, ::generate, ::validate
    )
}
