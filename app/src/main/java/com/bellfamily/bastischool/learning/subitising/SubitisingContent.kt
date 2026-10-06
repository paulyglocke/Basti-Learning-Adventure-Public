package com.bellfamily.bastischool.learning.subitising

import com.bellfamily.bastischool.learning.content.BundledContentRepository
import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.progress.*
import com.bellfamily.bastischool.learning.session.*
import java.util.Collections
import java.util.Random

/** Bounded 1–5 content; coordinates are stable die patterns, never scattered or timed. */
object SubitisingContent {
    val activity = ActivityId("activity.math.subitising")
    val skill = SkillId("skill.math.subitising.range_1_5")
    const val REVISION = 1
    val prompt = ContentText.plain("How many?", "Wie viele?")
    val help = ContentText.plain("Look at the whole group. How many dots can you see?", "Schau dir die ganze Gruppe an. Wie viele Punkte siehst du?")
    val completion = ContentText.plain("Adventure complete! Well done!", "Abenteuer geschafft! Gut gemacht!")
    private val words = listOf("one", "two", "three", "four", "five")
    val numbers = words.map { ContentId("number.$it") }
    val repository = BundledContentRepository(ContentVersion(1, 1), numbers.mapIndexed { i, id ->
        SemanticObjectDefinition(id, ContentText.plain("${i+1}", "${i+1}"))
    }, emptyList())
    fun quantity(id: ContentId): Int = numbers.indexOf(id).also {require(it >= 0)} + 1
    fun description(n: Int): ContentText {
        require(n in 1..5)
        return ContentText.plain(listOf("One dot", "Two dots", "Three dots", "Four dots", "Five dots")[n-1],
            listOf("Ein Punkt", "Zwei Punkte", "Drei Punkte", "Vier Punkte", "Fünf Punkte")[n-1])
    }
    data class Dot(val x: Float, val y: Float)
    fun pattern(n: Int): List<Dot> {
        require(n in 1..5)
        val corners = listOf(Dot(.25f,.25f), Dot(.75f,.25f), Dot(.25f,.75f), Dot(.75f,.75f))
        return when(n) {
            1 -> listOf(Dot(.5f,.5f))
            2 -> listOf(corners[0], corners[3])
            3 -> listOf(corners[0], Dot(.5f,.5f), corners[3])
            4 -> corners
            else -> corners + Dot(.5f,.5f)
        }
    }
    fun question(n: Int, choices: List<ContentId>) = ChoiceQuestion(
        TaskDefinitionId("task.math.subitising.${words[n-1]}"), skill, LearningContextId("context.math.quantity"), 1,
        prompt, choices, numbers[n-1], ContentText.plain("Yes, $n!", "Ja, $n!"),
        ContentText.plain("Try again.", "Versuch es noch einmal."), help)

    fun generate(id: SessionId, round: RoundLength, seed: Long): GenerationResult {
        val random = Random(seed)
        fun <T> shuffled(values: List<T>) = values.toMutableList().also { Collections.shuffle(it, random) }
        val quantities = shuffled((1..5).toList())
        if(round == RoundLength.TEN) {
            val second = shuffled((1..5).toList())
            if(second.first() == quantities.last()) Collections.swap(second, 0, 1)
            quantities.addAll(second)
        }
        val tasks = quantities.mapIndexed { index, n ->
            val correct = numbers[n-1]
            val choices = shuffled(listOf(correct) + shuffled(numbers.filter {it != correct}).take(3))
            ChoiceTask(TaskInstanceId(id,index+1), question(n,choices))
        }
        val plan = SessionPlan(id,activity,REVISION,repository.version,SessionPolicy(round),tasks,completion)
        plan.validate(repository)
        return GenerationResult.Generated(plan)
    }
    fun validate(state: SessionState) {
        val p = state.plan
        require(p.activity == activity && p.activityRevision == REVISION && p.contentVersion == repository.version)
        require(p.policy.wrongAnswer == WrongAnswerPolicy.RETRY && p.completionText == completion)
        p.tasks.forEach {
            val q=it.question
            require(q.choices.size == 4 && q.choices.all {id -> id in numbers})
            val expected=question(quantity(q.correct),q.choices)
            require(q.definition==expected.definition && q.skill==skill && q.context==expected.context && q.difficulty==1 &&
                q.instruction==prompt && q.hint==help && q.correctFeedback==expected.correctFeedback && q.wrongFeedback==expected.wrongFeedback)
        }
        val counts=p.tasks.groupingBy {it.question.correct}.eachCount()
        require(counts.keys==numbers.toSet() && counts.values.all {it==p.policy.round.count/5})
    }
    fun host(journal: ProgressStorage, progress: ProgressRepository) = DurableSessionHost(
        journal,progress,activity,REVISION,repository,::generate,::validate)
}
