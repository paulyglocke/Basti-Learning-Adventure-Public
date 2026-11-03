package com.bellfamily.bastischool.learning.addition

import com.bellfamily.bastischool.learning.content.BundledContentRepository
import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.session.*
import java.util.Collections
import java.util.Random

data class SubtractionFact(val start: Int, val takeAway: Int) {
    init { require(start in 1..5 && takeAway in 1..start) }
    val answer get() = start - takeAway
}

/** Concrete take-away facts within five, including zero as a possible remainder. */
object SubtractionContent : ChoiceSessionCurriculum {
    private val words = listOf("zero", "one", "two", "three", "four", "five")
    override val activity = ActivityId("activity.math.subtract_within_5")
    val skill = SkillId("skill.math.subtraction.within_5")
    val context = LearningContextId("context.math.subtraction.within_5")
    override val revision = 1
    val title = ContentText.plain("Take Away", "Wegnehmen")
    val prompt = ContentText.plain("How many are left?", "Wie viele sind noch da?")
    val help = ContentText.plain("Start with the whole group. Take away the second group and count what is left.",
        "Beginne mit der ganzen Gruppe. Nimm die zweite Gruppe weg und zähle, wie viele übrig bleiben.")
    override val completion = AdditionContent.completion
    val facts = (1..5).flatMap { start -> (1..start).map { SubtractionFact(start, it) } }
    override val repository = BundledContentRepository(ContentVersion(1, 1), words.mapIndexed { index, word ->
        SemanticObjectDefinition(ContentId("number.$word"), ContentText.plain("$index", "$index"))
    }, emptyList())
    fun numberId(number: Int): ContentId { require(number in 0..5); return ContentId("number.${words[number]}") }
    fun numberValue(id: ContentId): Int = words.indexOf(id.value.substringAfter("number.", "")).also { require(it >= 0) }

    fun question(fact: SubtractionFact, choices: List<ContentId>): ChoiceQuestion {
        require(fact in facts && choices.size == 4 && choices.toSet().size == 4 && numberId(fact.answer) in choices)
        require(choices.all { numberValue(it) in 0..5 })
        val key = "${words[fact.start]}.${words[fact.takeAway]}.${words[fact.answer]}"
        return ChoiceQuestion(TaskDefinitionId("task.math.subtract_within_5.$key"), skill, context, 1, prompt,
            choices, numberId(fact.answer), ContentText.plain("Yes, ${fact.answer} left!", "Ja, ${fact.answer} sind übrig!"),
            ContentText.plain("Try again.", "Versuch es noch einmal."), help)
    }

    fun definition(question: ChoiceQuestion): SubtractionFact {
        val parts = question.definition.value.split('.')
        require(parts.size == 6 && parts.take(3) == listOf("task", "math", "subtract_within_5"))
        val fact = SubtractionFact(words.indexOf(parts[3]), words.indexOf(parts[4]))
        require(fact in facts && words[fact.answer] == parts[5])
        return fact
    }

    override fun generate(id: SessionId, round: RoundLength, seed: Long): GenerationResult {
        val random = Random(seed)
        fun <T> shuffle(values: List<T>) = values.toMutableList().also { Collections.shuffle(it, random) }
        val onePerAnswer = (0..4).map { answer -> shuffle(facts.filter { it.answer == answer }).first() }
        val selected = if (round == RoundLength.FIVE) onePerAnswer else
            onePerAnswer + shuffle(facts - onePerAnswer.toSet()).take(round.count - onePerAnswer.size)
        val tasks = selected.mapIndexed { index, fact ->
            val choices = shuffle((0..5).filter { it != fact.answer }.take(3) + fact.answer).map(::numberId)
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
        val questions = plan.tasks.map { task ->
            val fact = definition(task.question)
            val expected = question(fact, task.question.choices)
            require(task.question.correct == expected.correct && task.question.skill == skill && task.question.context == context &&
                task.question.difficulty == 1 && task.question.instruction == prompt && task.question.hint == help)
            require(task.question.choices.size == 4 && task.question.choices.toSet().size == 4)
            fact
        }
        require(questions.size == plan.policy.round.count && questions.toSet().size == questions.size)
        require(questions.map { it.answer }.toSet().size == 5)
    }
}
