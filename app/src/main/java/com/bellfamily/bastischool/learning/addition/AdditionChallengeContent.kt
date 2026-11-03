package com.bellfamily.bastischool.learning.addition

import com.bellfamily.bastischool.learning.content.BundledContentRepository
import com.bellfamily.bastischool.learning.content.ContentRepository
import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.session.*
import java.util.Collections
import java.util.Random

/** A separate challenge track: addends 3–10 and totals 6–20. */
object AdditionChallengeContent : AdditionCurriculum {
    private val words = listOf("one", "two", "three", "four", "five", "six", "seven", "eight", "nine", "ten",
        "eleven", "twelve", "thirteen", "fourteen", "fifteen", "sixteen", "seventeen", "eighteen", "nineteen", "twenty")
    private val germanWords = listOf("eins", "zwei", "drei", "vier", "fünf", "sechs", "sieben", "acht", "neun", "zehn",
        "elf", "zwölf", "dreizehn", "vierzehn", "fünfzehn", "sechzehn", "siebzehn", "achtzehn", "neunzehn", "zwanzig")

    override val activity = ActivityId("activity.math.add_within_20")
    override val skill = SkillId("skill.math.addition.within_20")
    override val context = LearningContextId("context.math.addition.challenge")
    override val revision = 1
    override val repository: ContentRepository = BundledContentRepository(ContentVersion(1, 1),
        (1..20).map { SemanticObjectDefinition(numberId(it), ContentText.plain("$it", "$it")) }, emptyList())
    override val title = ContentText.plain("Add Bigger Numbers", "Größere Zahlen addieren")
    override val prompt = ContentText.plain("How many altogether?", "Wie viele sind es zusammen?")
    override val help = ContentText.plain(
        "Keep one group in mind while you count the other, then put both groups together.",
        "Merk dir eine Gruppe, während du die andere zählst. Zähle dann beide Gruppen zusammen.")
    override val completion = AdditionContent.completion
    override val useTenFrame = true

    override fun catalogue(): List<AdditionFact> = (3..10).flatMap { left ->
        (left..10).map { right -> AdditionFact(left, right) }
    }

    override fun numberId(number: Int): ContentId {
        require(number in 1..20)
        return ContentId("number.${words[number - 1]}")
    }

    override fun numberValue(id: ContentId): Int {
        val value = words.indexOf(id.value.substringAfter("number.", "")) + 1
        require(value in 1..20 && numberId(value) == id)
        return value
    }

    override fun groupDescription(number: Int, first: Boolean, language: ContentLanguage): String {
        require(number in 1..10)
        val side = if (language == ContentLanguage.GERMAN) {
            if (first) "Erste Gruppe" else "Zweite Gruppe"
        } else if (first) "First group" else "Second group"
        val name = if (language == ContentLanguage.GERMAN) germanWords[number - 1] else words[number - 1]
        val unit = if (language == ContentLanguage.GERMAN) "Punkte" else if (number == 1) "dot" else "dots"
        return "$side, $name $unit"
    }

    private fun name(number: Int) = words[number - 1]

    override fun question(fact: AdditionFact, choices: List<ContentId>): ChoiceQuestion {
        require(fact.left in 3..10 && fact.right in fact.left..10 && fact.total in 6..20)
        require(choices.size == 4 && choices.toSet().size == 4 && numberId(fact.total) in choices)
        require(choices.all { numberValue(it) in 6..20 })
        return ChoiceQuestion(
        TaskDefinitionId("task.math.add_within_20.${name(fact.left)}.${name(fact.right)}.${name(fact.total)}"),
        skill, context, 2, prompt, choices, numberId(fact.total),
        ContentText.plain("Yes, ${fact.total}!", "Ja, ${fact.total}!"),
        ContentText.plain("Try again.", "Versuch es noch einmal."), help)
    }

    override fun definition(question: ChoiceQuestion): AdditionFact {
        val parts = question.definition.value.split('.')
        require(parts.size == 6 && parts.take(3) == listOf("task", "math", "add_within_20"))
        val fact = AdditionFact(numberValue(ContentId("number.${parts[3]}")), numberValue(ContentId("number.${parts[4]}")))
        require(fact.left in 3..10 && fact.right in fact.left..10 && fact.total in 6..20)
        require(numberValue(ContentId("number.${parts[5]}")) == fact.total)
        return fact
    }

    override fun generate(id: SessionId, round: RoundLength, seed: Long): GenerationResult {
        val random = Random(seed)
        fun <T> shuffled(items: List<T>) = items.toMutableList().also { Collections.shuffle(it, random) }
        val anchors = listOf(AdditionFact(4, 7), AdditionFact(5, 9))
        val remainingTotals = shuffled((6..20).filterNot { total -> anchors.any { it.total == total } })
        val facts = anchors + remainingTotals.take(round.count - anchors.size).map { total ->
            shuffled(catalogue().filter { it.total == total }).first()
        }
        val ordered = shuffled(facts)
        val tasks = ordered.mapIndexed { index, fact ->
            val possible = shuffled((6..20).toList()).sortedBy { kotlin.math.abs(it - fact.total) }.take(4)
            val choices = shuffled(possible.map(::numberId))
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
            val q = task.question
            val fact = definition(q)
            val expected = question(fact, q.choices)
            require(q.choices.size == 4 && q.choices.toSet().size == 4)
            require(q.choices.all { numberValue(it) in 6..20 })
            require(q.correct == expected.correct && q.skill == skill && q.context == context &&
                q.difficulty == 2 && q.instruction == prompt && q.hint == help &&
                q.correctFeedback == expected.correctFeedback && q.wrongFeedback == expected.wrongFeedback)
            require(q.choices.count { it == q.correct } == 1)
            fact
        }
        require(facts.size == plan.policy.round.count && facts.map { it.unordered }.toSet().size == facts.size)
        require(facts.map { it.total }.toSet().size == facts.size)
        require(facts.any { it == AdditionFact(4, 7) } && facts.any { it == AdditionFact(5, 9) })
    }
}
