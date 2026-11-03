package com.bellfamily.bastischool.learning.addition

import com.bellfamily.bastischool.learning.content.BundledContentRepository
import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.session.*
import java.util.Collections
import java.util.Random

data class MakeFiveFact(val shown: Int) {
    init { require(shown in 1..4) }
    val missing get() = 5 - shown
}

/** Number-bond practice: see a part of five, then identify the spaces still to fill. */
object MakeFiveContent : ChoiceSessionCurriculum {
    private val words = listOf("one", "two", "three", "four")
    override val activity = ActivityId("activity.math.make_5")
    val skill = SkillId("skill.math.number_bonds.make_5")
    val context = LearningContextId("context.math.number_bonds.make_5")
    override val revision = 1
    val title = ContentText.plain("Make 5", "Fünf ergänzen")
    val prompt = ContentText.plain("How many more make five?", "Wie viele fehlen bis fünf?")
    val help = ContentText.plain("Look at the empty spaces in the five-frame. How many dots would fill them?",
        "Schau auf die leeren Plätze im Fünferfeld. Wie viele Punkte fehlen, um es zu füllen?")
    override val completion = AdditionContent.completion
    val numbers = (1..4).map(::numberId)
    override val repository = BundledContentRepository(ContentVersion(1, 1), numbers.mapIndexed { i, id ->
        SemanticObjectDefinition(id, ContentText.plain("${i + 1}", "${i + 1}"))
    }, emptyList())

    fun numberId(value: Int): ContentId { require(value in 1..4); return ContentId("number.${words[value - 1]}") }
    fun numberValue(id: ContentId) = words.indexOf(id.value.substringAfter("number.", "")).also { require(it >= 0) } + 1
    fun fact(question: ChoiceQuestion): MakeFiveFact {
        val parts = question.definition.value.split('.')
        require(parts.size == 5 && parts.take(3) == listOf("task", "math", "make_5"))
        val shown = words.indexOf(parts[3]) + 1
        val fact = MakeFiveFact(shown)
        require(words[fact.missing - 1] == parts[4])
        return fact
    }
    private fun instruction(fact: MakeFiveFact) = ContentText.plain(
        "You have ${fact.shown}. How many more make five?",
        "Du hast ${fact.shown}. Wie viele fehlen bis fünf?")

    fun question(fact: MakeFiveFact, choices: List<ContentId>): ChoiceQuestion {
        require(choices.size == 4 && choices.toSet() == numbers.toSet())
        return ChoiceQuestion(
        TaskDefinitionId("task.math.make_5.${words[fact.shown - 1]}.${words[fact.missing - 1]}"),
        skill, context, 1, instruction(fact), choices, numberId(fact.missing),
        ContentText.plain("Yes, ${fact.shown} and ${fact.missing} make five!", "Ja, ${fact.shown} und ${fact.missing} sind fünf!"),
        ContentText.plain("Try again.", "Versuch es noch einmal."), help)
    }

    override fun generate(id: SessionId, round: RoundLength, seed: Long): GenerationResult {
        val random = Random(seed)
        fun <T> shuffled(values: List<T>) = values.toMutableList().also { Collections.shuffle(it, random) }
        val base = shuffled((1..4).toList())
        val shown = if (round == RoundLength.FIVE) {
            listOf(base[0], base[1], base[0], base[2], base[3])
        } else {
            listOf(base[0], base[1], base[0], base[2], base[3], base[1], base[2], base[1], base[3], base[0])
        }
        val tasks = shown.mapIndexed { index, quantity ->
            val fact = MakeFiveFact(quantity)
            val choices = shuffled((1..4).toList()).map(::numberId)
            ChoiceTask(TaskInstanceId(id, index + 1), question(fact, choices))
        }
        val plan = SessionPlan(id, activity, revision, repository.version, SessionPolicy(round), tasks, completion)
        plan.validate(repository)
        return GenerationResult.Generated(plan)
    }

    override fun validate(state: SessionState) {
        val plan = state.plan
        require(plan.activity == activity && plan.activityRevision == revision && plan.contentVersion == repository.version)
        require(plan.policy.wrongAnswer == WrongAnswerPolicy.RETRY && plan.completionText == completion)
        val facts = plan.tasks.map { task ->
            val fact = fact(task.question)
            val expected = question(fact, task.question.choices)
            require(task.question.correct == expected.correct && task.question.skill == skill && task.question.context == context &&
                task.question.difficulty == 1 && task.question.instruction == instruction(fact) && task.question.hint == help &&
                task.question.correctFeedback == expected.correctFeedback && task.question.wrongFeedback == expected.wrongFeedback)
            require(task.question.choices.size == 4 && task.question.choices.toSet().size == 4 && task.question.choices.toSet() == numbers.toSet())
            fact
        }
        require(facts.size == plan.policy.round.count && facts.map { it.shown }.toSet() == (1..4).toSet())
        val expectedCount = if (plan.policy.round == RoundLength.FIVE) listOf(1, 1, 1, 2) else listOf(2, 2, 3, 3)
        require(facts.groupingBy { it.shown }.eachCount().values.sorted() == expectedCount)
        require(facts.zipWithNext().none { (a, b) -> a.shown == b.shown })
    }
}
