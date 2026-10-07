package com.bellfamily.bastischool.learning.addition

import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.progress.*
import com.bellfamily.bastischool.learning.session.*
import com.bellfamily.bastischool.learning.subitising.SubitisingContent
import java.util.Collections
import java.util.Random

/** Concrete ordered pairs only: both groups are nonempty and the combined quantity is at most five. */
object AdditionContent {
    data class Fact(val left: Int, val right: Int) {
        init { require(left in 1..4 && right in 1..4 && left + right <= 5) }
        val total get() = left + right
        val unordered get() = minOf(left, right) to maxOf(left, right)
    }
    val activity = ActivityId("activity.math.add_within_5")
    val skill = SkillId("skill.math.addition.within_5")
    val context = LearningContextId("context.math.addition")
    const val REVISION = 1
    val repository = SubitisingContent.repository
    val title = ContentText.plain("Add Together", "Zusammenzählen")
    val prompt = ContentText.plain("How many altogether?", "Wie viele sind es zusammen?")
    val help = ContentText.plain("Put both groups together in your head and count how many there are altogether.",
        "Stell dir beide Gruppen zusammen vor und zähle, wie viele es insgesamt sind.")
    val completion = SubitisingContent.completion
    fun catalogue(): List<Fact> = (1..4).flatMap { left -> (1..(5 - left)).map { right -> Fact(left, right) } }
    private fun name(n: Int) = SubitisingContent.numbers[n - 1].value.substringAfter('.')
    private fun number(word: String) = SubitisingContent.quantity(ContentId("number.$word"))
    fun groupDescription(n: Int, first: Boolean, language: ContentLanguage): String {
        val side = if (language == ContentLanguage.GERMAN) {
            if (first) "Erste Gruppe" else "Zweite Gruppe"
        } else if (first) "First group" else "Second group"
        return "$side, ${SubitisingContent.description(n).display[language]}"
    }
    fun question(fact: Fact, choices: List<ContentId>) = ChoiceQuestion(
        TaskDefinitionId("task.math.add_within_5.${name(fact.left)}.${name(fact.right)}.${name(fact.total)}"),
        skill, context, 1, prompt, choices, SubitisingContent.numbers[fact.total - 1],
        ContentText.plain("Yes, ${fact.total}!", "Ja, ${fact.total}!"),
        ContentText.plain("Try again.", "Versuch es noch einmal."), help)

    fun definition(q: ChoiceQuestion): Fact {
        val p = q.definition.value.split('.')
        require(p.size == 6 && p.take(3) == listOf("task", "math", "add_within_5"))
        val fact = Fact(number(p[3]), number(p[4]))
        require(number(p[5]) == fact.total)
        return fact
    }
    fun generate(id: SessionId, round: RoundLength, seed: Long): GenerationResult {
        val random = Random(seed)
        fun <T> shuffle(items: List<T>) = items.toMutableList().also { Collections.shuffle(it, random) }
        val all = catalogue()
        val selected = if (round == RoundLength.TEN) all else {
            val coverage = (2..5).map { total -> shuffle(all.filter { it.total == total }).first() }
            // A second partition of four or five is available: no mirrored repetition in a short round.
            coverage + shuffle(all.filter { f -> coverage.none { it.unordered == f.unordered } }).first()
        }
        val remaining = shuffle(selected)
        val ordered = mutableListOf<Fact>()
        repeat(selected.size) {
            val counts = remaining.groupingBy { it.total }.eachCount()
            val candidates = remaining.filter { it.total != ordered.lastOrNull()?.total }
            // Prefer the largest remaining total bucket; seeded order breaks ties. Finite, no rejection loop.
            val next = (candidates.ifEmpty { remaining }).maxBy { counts.getValue(it.total) }
            ordered += next
            remaining.remove(next)
        }
        val tasks = ordered.mapIndexed { i, fact ->
            val correct = SubitisingContent.numbers[fact.total - 1]
            val choices = shuffle(listOf(correct) + shuffle(SubitisingContent.numbers.filter { it != correct }).take(3))
            ChoiceTask(TaskInstanceId(id, i + 1), question(fact, choices))
        }
        val plan = SessionPlan(id, activity, REVISION, repository.version, SessionPolicy(round), tasks, completion)
        plan.validate(repository)
        return GenerationResult.Generated(plan)
    }
    fun validate(state: SessionState) {
        val p = state.plan
        require(p.activity == activity && p.activityRevision == REVISION && p.contentVersion == repository.version)
        require(p.policy.wrongAnswer == WrongAnswerPolicy.RETRY && p.completionText == completion)
        val facts = p.tasks.map { task ->
            val q = task.question
            val fact = definition(q)
            val expected = question(fact, q.choices)
            require(q.choices.size == 4 && q.choices.toSet().size == 4 && q.choices.all { it in SubitisingContent.numbers })
            require(q.correct == expected.correct && q.skill == skill && q.context == context && q.difficulty == 1 &&
                q.instruction == prompt && q.hint == help && q.correctFeedback == expected.correctFeedback && q.wrongFeedback == expected.wrongFeedback)
            fact
        }
        require(facts.size == p.policy.round.count && facts.toSet().size == facts.size)
        require(facts.map { it.total }.toSet() == (2..5).toSet())
        require(facts.zipWithNext().all { (a, b) -> a.total != b.total })
        if (p.policy.round == RoundLength.TEN) require(facts.toSet() == catalogue().toSet())
        else require(facts.map { it.unordered }.toSet().size == facts.size)
    }
    fun host(journal: ProgressStorage, progress: ProgressRepository) = DurableSessionHost(
        journal, progress, activity, REVISION, repository, ::generate, ::validate)
}
