package com.bellfamily.bastischool.learning.months

import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.session.*
import java.util.Collections
import java.util.Random

/** Bounded three-month sequence. Identity fixes start, length and gap; no second saved sequence state. */
object MonthsMissing {
    data class Definition(val start: ContentId, val gap: Int) {
        init { require(start in MonthIds.canonicalOrder && gap in 0..2) }
        val months get() = listOf(start, MonthIds.next(start), MonthIds.next(MonthIds.next(start)))
        val correct get() = months[gap]
        val id get() = TaskDefinitionId("task.months.missing.${start.value.substringAfter('.')}.three.${listOf("first", "middle", "last")[gap]}")
        val choices get() = listOf(MonthIds.previous(correct), correct, MonthIds.next(correct), MonthIds.next(MonthIds.next(correct)))
        fun sequence(language: ContentLanguage, spoken: Boolean = false): String = months.mapIndexed { index, month ->
            if (index == gap) {
                if (!spoken) "___" else if (language == ContentLanguage.GERMAN) "Lücke" else "blank"
            } else MonthContent.month(month).text.display[language]
        }.joinToString(if (spoken) ", " else " → ")
        fun description(language: ContentLanguage) =
            (if (language == ContentLanguage.GERMAN) "Monatsfolge: " else "Month sequence: ") + sequence(language, true)
    }
    val catalogue = MonthIds.canonicalOrder.flatMap { start -> (0..2).map { Definition(start, it) } }
    fun definition(question: ChoiceQuestion) = catalogue.single { it.id == question.definition }

    fun question(task: Definition, choices: List<ContentId> = task.choices): ChoiceQuestion {
        require(choices.size == 4 && choices.toSet() == task.choices.toSet())
        val answer = MonthContent.month(task.correct).text.speech
        return ChoiceQuestion(task.id, SkillId("skill.months.missing"), LearningContextId("context.months.year_order"), 1,
            ContentText(LocalizedText("Which month is missing?", "Welcher Monat fehlt?"),
                LocalizedText("Which month is missing? ${task.sequence(ContentLanguage.ENGLISH, true)}.",
                    "Welcher Monat fehlt? ${task.sequence(ContentLanguage.GERMAN, true)}.")),
            choices, task.correct, ContentText.plain("Yes, ${answer.en}!", "Ja, ${answer.de}!"),
            ContentText.plain("Try again. You can listen or ask for help.",
                "Versuche es noch einmal. Du kannst noch einmal hören oder dir helfen lassen."),
            ContentText.plain("Say the months in order and find the gap.",
                "Sag die Monate der Reihe nach und finde die Lücke."))
    }

    fun generate(id: SessionId, round: RoundLength, seed: Long): GenerationResult {
        val random = Random(seed)
        fun <T> shuffled(items: List<T>) = items.toMutableList().also { Collections.shuffle(it, random) }
        val targets = shuffled(MonthIds.canonicalOrder).take(round.count)
        // Start with an interior gap, then mix all three positions evenly. No adaptive state or rejection loop.
        val gaps = listOf(1) + shuffled(List(round.count - 1) { listOf(0, 2, 1)[it % 3] })
        val tasks = targets.mapIndexed { index, target ->
            val gap = gaps[index]
            var start = target
            repeat(gap) { start = MonthIds.previous(start) }
            val task = Definition(start, gap)
            ChoiceTask(TaskInstanceId(id, index + 1), question(task, shuffled(task.choices)))
        }
        val plan = SessionPlan(id, MonthsQuestions.activity(MonthsMode.MISSING), MonthsQuestions.REVISION,
            MonthContent.repository.version, SessionPolicy(round), tasks, MonthsQuestions.completion)
        plan.validate(MonthContent.repository)
        return GenerationResult.Generated(plan)
    }

    fun validate(state: SessionState) {
        val plan = state.plan
        require(plan.activity == MonthsQuestions.activity(MonthsMode.MISSING) && plan.activityRevision == MonthsQuestions.REVISION &&
            plan.contentVersion == MonthContent.repository.version)
        require(plan.policy == SessionPolicy(plan.policy.round) && plan.completionText == MonthsQuestions.completion)
        val definitions = plan.tasks.map { definition(it.question) }
        require(definitions.map { it.correct }.distinct().size == plan.tasks.size)
        require(definitions.first().gap == 1 && definitions.map { it.gap }.toSet() == setOf(0, 1, 2))
        require((0..2).map { gap -> definitions.count { it.gap == gap } }.let { it.max() - it.min() <= 1 })
        plan.tasks.forEach { task ->
            val q = task.question
            val expected = question(definition(q), q.choices)
            require(q.correct == expected.correct && q.skill == expected.skill && q.context == expected.context &&
                q.difficulty == expected.difficulty && q.instruction == expected.instruction && q.hint == expected.hint &&
                q.correctFeedback == expected.correctFeedback && q.wrongFeedback == expected.wrongFeedback)
        }
    }
}
