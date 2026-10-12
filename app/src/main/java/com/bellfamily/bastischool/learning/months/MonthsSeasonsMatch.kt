package com.bellfamily.bastischool.learning.months

import com.bellfamily.bastischool.learning.content.SeasonIds
import com.bellfamily.bastischool.learning.content.CoreContent
import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.progress.*
import com.bellfamily.bastischool.learning.session.*
import java.util.Collections
import java.util.Random

/** Introductory sorting-by-membership activity; it does not revise either source activity. */
object MonthsSeasonsMatch {
    const val REVISION = 1
    val activity = ActivityId("activity.months.seasons")
    val skill = SkillId("skill.months.season")
    val context = LearningContextId("context.months.season_mapping")
    val completion = ContentText.plain("You matched the months to their seasons! Well done!",
        "Du hast die Monate den Jahreszeiten zugeordnet! Sehr gut!")

    fun question(month: ContentId): ChoiceQuestion {
        val definition = MonthContent.month(month)
        val season = MonthContent.season(definition.season)
        return ChoiceQuestion(
            TaskDefinitionId("task.months.seasons.${month.value.substringAfter('.')}"), skill, context, 1,
            ContentText.plain("Place ${definition.text.speech.en} with its season. Choose from Spring, Summer, Autumn or Winter.",
                "Ordne ${definition.text.speech.de} der passenden Jahreszeit zu. Wähle: Frühling, Sommer, Herbst oder Winter."),
            SeasonIds.canonicalOrder, definition.season,
            ContentText.plain("${definition.text.speech.en} belongs to ${season.text.speech.en}.",
                "${definition.text.speech.de} gehört zum ${season.text.speech.de}."),
            ContentText.plain("That month belongs in another season group. Try again.",
                "Dieser Monat gehört zu einer anderen Jahreszeit. Versuche es noch einmal."),
            ContentText.plain("Think about the months that belong together.",
                "Denk daran, welche Monate zusammengehören.")
        )
    }

    fun month(question: ChoiceQuestion): ContentId = MonthContent.definitions.single {
        question.definition == TaskDefinitionId("task.months.seasons.${it.id.value.substringAfter('.')}")
    }.id

    /** Balanced season sampling: five tasks distribute 2/1/1/1; ten distribute 3/3/2/2. */
    fun generate(id: SessionId, round: RoundLength, seed: Long): GenerationResult {
        val random = Random(seed)
        fun <T> shuffled(values: List<T>) = values.toMutableList().also { Collections.shuffle(it, random) }
        val seasons = shuffled(SeasonIds.canonicalOrder)
        val targets = seasons.flatMapIndexed { index, season ->
            val count = round.count / 4 + if (index < round.count % 4) 1 else 0
            shuffled(MonthContent.definitions.filter { it.season == season }.map { it.id }).take(count)
        }
        val tasks = shuffled(targets).mapIndexed { index, month ->
            ChoiceTask(TaskInstanceId(id, index + 1), question(month))
        }
        val content = CoreContent.repository()
        val plan = SessionPlan(id, activity, REVISION, content.version,
            SessionPolicy(round), tasks, completion)
        plan.validate(content)
        return GenerationResult.Generated(plan)
    }

    fun validate(state: SessionState) {
        val plan = state.plan
        require(plan.activity == activity && plan.activityRevision == REVISION &&
            plan.contentVersion == CoreContent.version && plan.completionText == completion &&
            plan.policy == SessionPolicy(plan.policy.round))
        require(plan.tasks.size == plan.policy.round.count)
        val months = plan.tasks.map { task ->
            val q = task.question
            val month = MonthContent.definitions.single { q.definition == question(it.id).definition }.id
            val expected = question(month)
            require(q.skill == skill && q.context == context && q.difficulty == 1 &&
                q.choices == expected.choices && q.correct == expected.correct &&
                q.instruction == expected.instruction && q.correctFeedback == expected.correctFeedback &&
                q.wrongFeedback == expected.wrongFeedback && q.hint == expected.hint)
            month
        }
        require(months.distinct().size == months.size)
        val counts = months.groupingBy { MonthContent.month(it).season }.eachCount()
        require(counts.keys == SeasonIds.canonicalOrder.toSet())
        val base = plan.policy.round.count / 4
        val extra = plan.policy.round.count % 4
        require(counts.values.sorted() == List(4 - extra) { base } + List(extra) { base + 1 })
    }

    fun host(journal: ProgressStorage, progress: ProgressRepository) = DurableSessionHost(
        journal, progress, activity, REVISION, CoreContent.repository(),
        { id, round, seed -> generate(id, round, seed) }, ::validate)
}
