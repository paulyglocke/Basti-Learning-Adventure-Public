package com.bellfamily.bastischool.learning.schoolskills

import com.bellfamily.bastischool.learning.content.BundledContentRepository
import com.bellfamily.bastischool.learning.content.ContentRepository
import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.progress.*
import com.bellfamily.bastischool.learning.session.*
import java.util.Collections
import java.util.Random

/** First bounded School Skills pack: recognise useful, child-authored help-seeking phrases. */
object SchoolSkillsContent {
    const val REVISION = 1
    val activity = ActivityId("activity.school.helpful_phrases")
    val skill = SkillId("skill.school.functional_communication")
    val context = LearningContextId("context.school.self_advocacy_phrases")
    val completion = ContentText.plain("You practised helpful words!", "Du hast hilfreiche Worte geübt!")
    val sceneAsset = "SceneDescriptions/classroom_school/canonical/classroom_002_routines.png"

    data class Phrase(val id: ContentId, val text: ContentText)

    val phrases = listOf(
        Phrase(ContentId("school_phrase.help"), ContentText.plain("Can you help me?", "Kannst du mir helfen?")),
        Phrase(ContentId("school_phrase.repeat"), ContentText.plain("Can you say that again?", "Kannst du das noch einmal sagen?")),
        Phrase(ContentId("school_phrase.understand"), ContentText.plain("I don't understand.", "Ich verstehe das nicht.")),
        Phrase(ContentId("school_phrase.break"), ContentText.plain("Can I have a break?", "Kann ich eine Pause machen?")),
        Phrase(ContentId("school_phrase.where"), ContentText.plain("Where should I put it?", "Wohin soll ich das legen?"))
    )
    private val byId = phrases.associateBy { it.id }
    val repository: ContentRepository = BundledContentRepository(ContentVersion(1, 1), phrases.map { SemanticObjectDefinition(it.id, it.text) }, emptyList())

    private val situations = mapOf(
        "help" to ContentText.plain("If you need help with your work, one thing you could say is:", "Wenn du Hilfe bei deiner Aufgabe brauchst, könntest du sagen:"),
        "repeat" to ContentText.plain("If you missed the instruction, one thing you could ask is:", "Wenn du die Anweisung nicht gehört hast, könntest du fragen:"),
        "understand" to ContentText.plain("If the task is confusing, you could tell someone:", "Wenn die Aufgabe unklar ist, könntest du sagen:"),
        "break" to ContentText.plain("If you would like some quiet time, one thing you could ask is:", "Wenn du etwas Ruhe möchtest, könntest du fragen:"),
        "where" to ContentText.plain("If you are unsure where something belongs, one thing you could ask is:", "Wenn du nicht weißt, wohin etwas gehört, könntest du fragen:")
    )
    private val correct = ContentText.plain("That is one helpful thing you could say.", "Das ist etwas Hilfreiches, das du sagen könntest.")
    private val retry = ContentText.plain("All of these are useful words. Listen again for a phrase that fits this situation.",
        "Alle Sätze sind hilfreich. Hör noch einmal hin: Welcher Satz passt zu dieser Situation?")
    private val hint = ContentText.plain("Think about what you want the other person to know.", "Überlege, was die andere Person wissen soll.")

    fun phrase(id: ContentId): Phrase = byId[id] ?: error("Unknown school phrase: ${id.value}")

    fun question(target: ContentId, choices: List<ContentId>): ChoiceQuestion {
        val phrase = phrase(target)
        require(choices.size == 4 && choices.toSet().size == 4 && target in choices)
        choices.forEach(::phrase)
        val key = target.value.substringAfterLast('.')
        return ChoiceQuestion(
            TaskDefinitionId("task.school.phrase.$key.${choices.map { it.value.substringAfterLast('.') }.sorted().joinToString("_")}"),
            skill, context, 1, situations.getValue(key), choices, target, correct, retry, hint
        )
    }

    fun generate(id: SessionId, round: RoundLength, seed: Long): GenerationResult {
        val random = Random(seed)
        val targets = (phrases.flatMap { phrase -> List(round.count / phrases.size) { phrase.id } }).toMutableList()
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
        val remainingChoices = phrases.associate { target ->
            target.id to combinations(phrases.map { it.id }.filter { it != target.id }, 3).map { listOf(target.id) + it }.toMutableList()
        }.toMutableMap()
        val tasks = arranged.mapIndexed { index, target ->
            val sets = remainingChoices.getValue(target)
            check(sets.isNotEmpty())
            Collections.shuffle(sets, random)
            val selected = sets.removeAt(0).toMutableList().also { Collections.shuffle(it, random) }
            ChoiceTask(TaskInstanceId(id, index + 1), question(target, selected))
        }
        val plan = SessionPlan(id, activity, REVISION, repository.version, SessionPolicy(round), tasks, completion)
        plan.validate(repository)
        return GenerationResult.Generated(plan)
    }

    fun validate(state: SessionState) {
        require(state.plan.activity == activity && state.plan.activityRevision == REVISION && state.plan.contentVersion == repository.version)
        require(state.plan.completionText == completion && state.plan.policy.wrongAnswer == WrongAnswerPolicy.RETRY)
        val targets = state.plan.tasks.map { it.question.correct }
        require(phrases.all { phrase -> targets.count { it == phrase.id } == state.plan.tasks.size / phrases.size })
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
