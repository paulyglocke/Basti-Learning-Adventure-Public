package com.bellfamily.bastischool.learning.comparequantity

import com.bellfamily.bastischool.learning.content.BundledContentRepository
import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.progress.*
import com.bellfamily.bastischool.learning.session.*
import com.bellfamily.bastischool.learning.subitising.SubitisingContent
import java.util.Collections
import java.util.Random

/** One comparison skill: the prompt always asks more, with equality as a valid answer. */
object CompareQuantityContent {
    enum class Relation(val answer: ContentId) {
        LEFT_MORE(ContentId("comparison.left")), RIGHT_MORE(ContentId("comparison.right")), SAME(ContentId("comparison.same"))
    }
    data class PairOfGroups(val left: Int, val right: Int) {
        init { require(left in 1..5 && right in 1..5) }
        val relation get() = when {
            left > right -> Relation.LEFT_MORE
            right > left -> Relation.RIGHT_MORE
            else -> Relation.SAME
        }
    }
    val activity = ActivityId("activity.math.compare_quantity")
    val skill = SkillId("skill.math.compare.quantity")
    val context = LearningContextId("context.math.quantity")
    const val REVISION = 1
    val title = ContentText.plain("More or Fewer", "Mehr oder weniger")
    val prompt = ContentText.plain("Which side has more?", "Welche Seite hat mehr?")
    val help = ContentText.plain("Look at both groups. Compare how many dots each one has.",
        "Schau dir beide Gruppen an. Vergleiche, wie viele Punkte jede Gruppe hat.")
    val completion = SubitisingContent.completion
    val answers = Relation.entries.map { it.answer }
    private val labels = listOf(ContentText.plain("Left", "Links"), ContentText.plain("Right", "Rechts"), ContentText.plain("Same", "Gleich"))
    val repository = BundledContentRepository(ContentVersion(1, 1),
        answers.mapIndexed { i, id -> SemanticObjectDefinition(id, labels[i]) }, emptyList())
    fun label(id: ContentId) = requireNotNull(repository.find(id)).text
    fun groupDescription(n: Int, left: Boolean, language: ContentLanguage): String {
        val side = if (language == ContentLanguage.GERMAN) {
            if (left) "Linke Gruppe" else "Rechte Gruppe"
        } else if (left) "Left group" else "Right group"
        return "$side, ${SubitisingContent.description(n).display[language]}"
    }
    private fun name(n: Int) = SubitisingContent.numbers[n - 1].value.substringAfter('.')
    fun question(pair: PairOfGroups, choices: List<ContentId> = answers) = ChoiceQuestion(
        TaskDefinitionId("task.math.compare_quantity.${name(pair.left)}.${name(pair.right)}.${pair.relation.name.lowercase()}"),
        skill, context, 1, prompt, choices, pair.relation.answer,
        ContentText.plain("Yes!", "Ja!"), ContentText.plain("Try again.", "Versuch es noch einmal."), help)

    fun definition(q: ChoiceQuestion): PairOfGroups {
        val parts = q.definition.value.split('.')
        require(parts.size == 6 && parts.take(3) == listOf("task", "math", "compare_quantity"))
        val pair = PairOfGroups(SubitisingContent.quantity(ContentId("number.${parts[3]}")),
            SubitisingContent.quantity(ContentId("number.${parts[4]}")))
        require(parts[5] == pair.relation.name.lowercase())
        return pair
    }

    fun generate(id: SessionId, round: RoundLength, seed: Long): GenerationResult {
        val random = Random(seed)
        fun <T> shuffle(items: List<T>) = items.toMutableList().also { Collections.shuffle(it, random) }
        val catalogue = (1..5).flatMap { left -> (1..5).map { right -> PairOfGroups(left, right) } }
        val factor = round.count / 5
        val selected = Relation.entries.flatMap { relation ->
            shuffle(catalogue.filter { it.relation == relation }).take(if (relation == Relation.SAME) factor else 2 * factor)
        }
        val tasks = shuffle(selected).mapIndexed { i, pair -> ChoiceTask(TaskInstanceId(id, i + 1), question(pair)) }
        // Left/Right/Same retain their natural reading order; correct sides vary with shuffled tasks.
        val plan = SessionPlan(id, activity, REVISION, repository.version, SessionPolicy(round), tasks, completion)
        plan.validate(repository)
        return GenerationResult.Generated(plan)
    }

    fun validate(state: SessionState) {
        val p = state.plan
        require(p.activity == activity && p.activityRevision == REVISION && p.contentVersion == repository.version)
        require(p.policy.wrongAnswer == WrongAnswerPolicy.RETRY && p.completionText == completion)
        val pairs = p.tasks.map { task ->
            val q = task.question
            val pair = definition(q)
            val expected = question(pair, q.choices)
            require(q.choices.size == 3 && q.choices.toSet() == answers.toSet())
            require(q.correct == expected.correct && q.skill == skill && q.context == context && q.difficulty == 1 &&
                q.instruction == prompt && q.hint == help && q.correctFeedback == expected.correctFeedback && q.wrongFeedback == expected.wrongFeedback)
            pair
        }
        require(pairs.size == p.policy.round.count && pairs.toSet().size == pairs.size)
        val counts = pairs.groupingBy { it.relation }.eachCount()
        val factor = p.policy.round.count / 5
        require(counts == mapOf(Relation.LEFT_MORE to 2 * factor, Relation.RIGHT_MORE to 2 * factor, Relation.SAME to factor))
    }
    fun host(journal: ProgressStorage, progress: ProgressRepository) = DurableSessionHost(
        journal, progress, activity, REVISION, repository, ::generate, ::validate)
}
