package com.bellfamily.bastischool.learning.wilma

import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.session.*
import java.util.Collections
import java.util.Random

/** Hypothetical today, never the device date. Uses the ordinary single-answer session. */
object WilmaToday {
    enum class Relation { YESTERDAY, TOMORROW }

    fun answer(anchor: ContentId, relation: Relation) = WilmaContent.answer(anchor,
        if (relation == Relation.YESTERDAY) DayRelation.BEFORE else DayRelation.AFTER)

    fun question(anchor: ContentId, relation: Relation, choices: List<ContentId>): ChoiceQuestion {
        val name = WilmaContent.day(anchor).text.speech
        val correct = answer(anchor, relation)
        val target = WilmaContent.day(correct).text.speech
        val yesterday = relation == Relation.YESTERDAY
        return ChoiceQuestion(
            TaskDefinitionId("task.wilma.today.${relation.name.lowercase()}.${anchor.value.substringAfter('.')}"),
            SkillId("skill.weekdays.${relation.name.lowercase()}"), LearningContextId("context.weekdays.wilma"), 1,
            if (yesterday) ContentText.plain("Today is ${name.en}. What day was yesterday?", "Heute ist ${name.de}. Welcher Tag war gestern?")
            else ContentText.plain("Today is ${name.en}. What day is tomorrow?", "Heute ist ${name.de}. Welcher Tag ist morgen?"),
            choices, correct, ContentText.plain("Yes, ${target.en}.", "Ja, ${target.de}."),
            ContentText.plain("Try again. You can ask for help.", "Versuche es noch einmal. Du kannst dir helfen lassen."),
            if (yesterday) ContentText.plain("Yesterday is the day before ${name.en}. Go back one day in the week.", "Gestern ist der Tag vor ${name.de}. Gehe in der Woche einen Tag zurück.")
            else ContentText.plain("Tomorrow is the day after ${name.en}. Go forward one day in the week.", "Morgen ist der Tag nach ${name.de}. Gehe in der Woche einen Tag weiter."))
    }

    fun definition(question: ChoiceQuestion): Pair<ContentId, Relation> {
        val parts = question.definition.value.split('.')
        require(parts.size == 5 && parts.take(3) == listOf("task", "wilma", "today"))
        return ContentId("day.${parts[4]}") to Relation.valueOf(parts[3].uppercase())
    }

    fun generate(id: SessionId, round: RoundLength, seed: Long): GenerationResult {
        val random = Random(seed)
        fun <T> shuffle(items: List<T>) = items.toMutableList().also { Collections.shuffle(it, random) }
        val anchors = shuffle(WilmaContent.days)
        val first = random.nextInt(2)
        // Repeating the seven-anchor cycle flips relation on its next visit: no repeated pair.
        val tasks = (0 until round.count).map { index ->
            val anchor = anchors[index % anchors.size]
            val relation = Relation.entries[(first + index) % 2]
            val correct = answer(anchor, relation)
            val choices = shuffle(listOf(correct) + shuffle(WilmaContent.days.filter { it != correct }).take(3))
            ChoiceTask(TaskInstanceId(id, index + 1), question(anchor, relation, choices))
        }
        val plan = SessionPlan(id, WilmaContent.activity(WilmaPhase.TODAY), WilmaContent.REVISION,
            WilmaContent.repository.version, SessionPolicy(round), tasks, WilmaContent.roundCompletion)
        plan.validate(WilmaContent.repository)
        return GenerationResult.Generated(plan)
    }

    fun validate(state: SessionState) {
        state.plan.tasks.forEach { task ->
            val q = task.question
            val (anchor, relation) = definition(q)
            require(q.choices.size == 4 && q.choices.all { it in WilmaContent.days })
            val expected = question(anchor, relation, q.choices)
            require(q.correct == expected.correct && q.skill == expected.skill && q.context == expected.context &&
                q.difficulty == expected.difficulty && q.instruction == expected.instruction && q.hint == expected.hint &&
                q.correctFeedback == expected.correctFeedback && q.wrongFeedback == expected.wrongFeedback)
        }
    }
}
