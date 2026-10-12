package com.bellfamily.bastischool.learning.months

import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.progress.*
import com.bellfamily.bastischool.learning.session.*
import java.util.Collections
import java.util.Random

/** Identifies a middle month by the two visible, consecutive-sequence endpoints. */
object MonthsBetween {
    const val REVISION = 1
    val activity = ActivityId("activity.months.between")
    val skill = SkillId("skill.months.between")
    val context = LearningContextId("context.months.year_order")
    val completion = ContentText.plain("You practised which month comes between. Well done!",
        "Du hast geübt, welcher Monat dazwischenkommt. Sehr gut!")

    data class Definition(val middle: ContentId) {
        init { require(middle in MonthIds.canonicalOrder) }
        val before get() = MonthIds.previous(middle)
        val after get() = MonthIds.next(middle)
        val id get() = TaskDefinitionId("task.months.between.${middle.value.substringAfter('.')}")
        /** Nearby calendar months are plausible alternatives, while endpoints stay visible above. */
        val choices get() = listOf(MonthIds.previous(before), before, middle, after)
    }

    val catalogue = MonthIds.canonicalOrder.map(::Definition)
    fun definition(question: ChoiceQuestion) = catalogue.single { it.id == question.definition }

    fun question(task: Definition, choices: List<ContentId> = task.choices): ChoiceQuestion {
        require(choices.size == 4 && choices.toSet() == task.choices.toSet())
        val before = MonthContent.month(task.before).text.speech
        val after = MonthContent.month(task.after).text.speech
        val middle = MonthContent.month(task.middle).text.speech
        val prompt = ContentText(
            LocalizedText("Which month comes between ${before.en} and ${after.en}?",
                "Welcher Monat kommt zwischen ${before.de} und ${after.de}?"),
            LocalizedText("Which month comes between ${before.en} and ${after.en}?",
                "Welcher Monat kommt zwischen ${before.de} und ${after.de}?"))
        return ChoiceQuestion(task.id, skill, context, 1, prompt, choices, task.middle,
            ContentText.plain("Yes, ${middle.en} comes between them!", "Ja, ${middle.de} kommt dazwischen!"),
            ContentText.plain("Not quite. Listen again or look at the Year Wheel for help.",
                "Noch nicht. Höre noch einmal oder sieh dir das Jahresrad als Hilfe an."),
            ContentText.plain("Say the months in order. What comes after ${before.en} and before ${after.en}?",
                "Sag die Monate der Reihe nach. Was kommt nach ${before.de} und vor ${after.de}?"))
    }

    fun generate(id: SessionId, round: RoundLength, seed: Long): GenerationResult {
        val random = Random(seed)
        fun <T> shuffled(values: List<T>) = values.toMutableList().also { Collections.shuffle(it, random) }
        val targets = shuffled(MonthIds.canonicalOrder).take(round.count)
        val tasks = targets.mapIndexed { index, target ->
            val definition = Definition(target)
            ChoiceTask(TaskInstanceId(id, index + 1), question(definition, shuffled(definition.choices)))
        }
        val plan = SessionPlan(id, activity, REVISION, MonthContent.repository.version,
            SessionPolicy(round), tasks, completion)
        plan.validate(MonthContent.repository)
        return GenerationResult.Generated(plan)
    }

    fun validate(state: SessionState) {
        val plan = state.plan
        require(plan.activity == activity && plan.activityRevision == REVISION &&
            plan.contentVersion == MonthContent.repository.version)
        require(plan.policy == SessionPolicy(plan.policy.round) && plan.completionText == completion)
        val definitions = plan.tasks.map { definition(it.question) }
        require(definitions.map { it.middle }.distinct().size == plan.tasks.size)
        plan.tasks.forEach { task ->
            val q = task.question
            val expected = question(definition(q), q.choices)
            require(q.correct == expected.correct && q.skill == skill && q.context == context &&
                q.difficulty == 1 && q.instruction == expected.instruction && q.hint == expected.hint &&
                q.correctFeedback == expected.correctFeedback && q.wrongFeedback == expected.wrongFeedback)
        }
    }

    fun host(journal: ProgressStorage, progress: ProgressRepository) = DurableSessionHost(
        journal, progress, activity, REVISION, MonthContent.repository,
        { id, round, seed -> generate(id, round, seed) }, ::validate)
}
