package com.bellfamily.bastischool.learning.months

import com.bellfamily.bastischool.audio.SpeechTrigger
import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.progress.*
import com.bellfamily.bastischool.learning.session.*
import com.bellfamily.bastischool.learning.sequencing.OrderedPlacement
import java.util.Collections
import java.util.Random

/** One introductory, cyclic three-month sequence; it is intentionally not a quiz round. */
class MonthsBuildYearState internal constructor(
    val id: SessionId,
    val start: ContentId,
    val language: ContentLanguage,
    choices: List<ContentId>,
    steps: List<TaskProgress>,
    val acknowledged: Boolean = false
) {
    val order = Collections.unmodifiableList(List(3) { offset -> MonthIds.canonicalOrder[Math.floorMod(MonthIds.canonicalOrder.indexOf(start) + offset, 12)] })
    val choices = Collections.unmodifiableList(choices.toList())
    val steps = Collections.unmodifiableList(steps.toList())
    private val placement = OrderedPlacement(order, this.choices, this.steps)
    val index get() = placement.index
    val completed get() = placement.completed
    val placed get() = placement.placed
    val task get() = TaskInstanceId(id, (index + 1).coerceAtMost(3))
    val current get() = steps[index.coerceAtMost(2)]
    val nextAttempt get() = if (!completed && current.answer == AnswerState.UNANSWERED) AttemptId(task, current.attempts + 1) else null

    init {
        MonthContent.month(start)
        require(!acknowledged || completed)
    }

    internal fun changed(language: ContentLanguage = this.language, steps: List<TaskProgress> = this.steps,
        acknowledged: Boolean = this.acknowledged) = MonthsBuildYearState(id, start, language, choices, steps, acknowledged)
}

sealed interface MonthsBuildYearAction {
    data class Place(val attempt: AttemptId, val month: ContentId): MonthsBuildYearAction
    data class Retry(val attempt: AttemptId): MonthsBuildYearAction
    data class Replay(val task: TaskInstanceId): MonthsBuildYearAction
    data class Help(val task: TaskInstanceId): MonthsBuildYearAction
    data class Language(val language: ContentLanguage): MonthsBuildYearAction
}

data class MonthsBuildYearTransition(val state: MonthsBuildYearState, val events: List<ProgressEvent> = emptyList(),
    val speech: SessionEffect.Narrate? = null)

object MonthsBuildYear {
    const val REVISION = 1
    val activity = ActivityId("activity.months.build_year.three")
    val skill = SkillId("skill.months.sequence")
    val context = LearningContextId("context.months.year_order")
    val introduction = ContentText.plain(
        "Watch this example: December, January, February. The months start again after December. Now choose the three month tiles in order.",
        "Sieh dir dieses Beispiel an: Dezember, Januar, Februar. Nach Dezember beginnen die Monate wieder von vorn. Wähle nun die drei Monatskarten der Reihe nach aus.")
    val completion = ContentText.plain("You built this part of the year!", "Du hast diesen Teil des Jahres geordnet!")

    fun sequence(start: ContentId) = List(3) { offset ->
        MonthIds.canonicalOrder[Math.floorMod(MonthIds.canonicalOrder.indexOf(start).also { require(it >= 0) } + offset, 12)]
    }

    fun start(id: SessionId, seed: Long, language: ContentLanguage): MonthsBuildYearState {
        val first = MonthIds.canonicalOrder[Random(seed).nextInt(MonthIds.canonicalOrder.size)]
        val order = sequence(first)
        return MonthsBuildYearState(id, first, language, OrderedPlacement.scramble(order, seed xor 0x4D4F4E544853L), List(3) { TaskProgress() })
    }

    fun prompt(state: MonthsBuildYearState): ContentText = when {
        state.completed -> completion
        state.index == 0 -> introduction
        else -> {
            val previous = MonthContent.month(state.placed.last()).text.speech
            ContentText.plain("${previous.en} is in place. Choose the next month.", "${previous.de} ist an seinem Platz. Wähle den nächsten Monat.")
        }
    }

    fun hint(state: MonthsBuildYearState): ContentText {
        val next = MonthContent.month(state.order[state.index.coerceAtMost(2)]).text.speech
        return ContentText.plain("The next month is ${next.en}.", "Der nächste Monat ist ${next.de}.")
    }

    fun reduce(s: MonthsBuildYearState, action: MonthsBuildYearAction): MonthsBuildYearTransition {
        fun unchanged() = MonthsBuildYearTransition(s)
        fun update(progress: TaskProgress) = s.changed(steps = s.steps.toMutableList().also { it[s.index] = progress })
        return when (action) {
            is MonthsBuildYearAction.Language -> MonthsBuildYearTransition(s.changed(language = action.language))
            is MonthsBuildYearAction.Place -> {
                if (s.nextAttempt != action.attempt || action.month !in s.choices || action.month in s.placed || s.current.attempts >= 10000) return unchanged()
                val progress = OrderedPlacement(s.order, s.choices, s.steps).place(action.month) ?: return unchanged()
                val next = s.changed(steps = progress)
                val events = listOf(attempt(next, s.index)) + if (next.completed) listOf(completion(next)) else emptyList()
                val text = if (action.month == s.order[s.index]) prompt(next) else ContentText.plain(
                    "Not quite. The months already placed stay here. Try another month.",
                    "Noch nicht. Die eingeordneten Monate bleiben hier. Versuche es mit einem anderen Monat.")
                MonthsBuildYearTransition(next, events, SessionEffect.Narrate(text,
                    if (next.completed) NarrationKind.COMPLETION else if (action.month == s.order[s.index]) NarrationKind.INSTRUCTION else NarrationKind.FEEDBACK,
                    SpeechTrigger.AUTOMATIC))
            }
            is MonthsBuildYearAction.Retry -> if (s.completed || s.current.answer != AnswerState.RETRY_AVAILABLE || action.attempt != AttemptId(s.task, s.current.attempts)) unchanged()
                else MonthsBuildYearTransition(update(s.current.copy(answer = AnswerState.UNANSWERED, retries = s.current.retries + 1)))
            is MonthsBuildYearAction.Replay -> if (action.task != s.task) unchanged() else {
                val next = if (s.completed) s else update(s.current.copy(support = s.current.support.copy(replays = (s.current.support.replays + 1).coerceAtMost(10000))))
                MonthsBuildYearTransition(next, speech = SessionEffect.Narrate(prompt(next),
                    if (next.completed) NarrationKind.COMPLETION else NarrationKind.INSTRUCTION, SpeechTrigger.REPLAY))
            }
            is MonthsBuildYearAction.Help -> if (s.completed || action.task != s.task) unchanged() else {
                val next = update(s.current.copy(support = s.current.support.copy(hint = true)))
                MonthsBuildYearTransition(next, speech = SessionEffect.Narrate(hint(next), NarrationKind.INSTRUCTION, SpeechTrigger.MANUAL))
            }
        }
    }

    private fun evidence(s: MonthsBuildYearState, index: Int) = TaskEvidence(TaskInstanceId(s.id, index + 1),
        TaskDefinitionId("task.months.build_year.three.${s.start.value.substringAfter('.')}.${listOf("first", "second", "third")[index]}"), skill, context, 1)
    private fun origin(s: MonthsBuildYearState) = ProgressOrigin(s.id, activity, REVISION, MonthContent.repository.version)
    fun attempt(s: MonthsBuildYearState, index: Int): AttemptEvent {
        val step = s.steps[index]
        return AttemptEvent(origin(s), evidence(s, index), AttemptId(TaskInstanceId(s.id, index + 1), step.attempts), s.language,
            step.lastChoice!!, if (step.answer == AnswerState.CORRECT) AttemptOutcome.CORRECT else AttemptOutcome.INCORRECT, step.support)
    }
    fun completion(s: MonthsBuildYearState): CompletionEvent {
        require(s.completed)
        return CompletionEvent(origin(s), s.steps.mapIndexed { index, step ->
            CompletedTask(evidence(s, index), step.lastChoice!!, AttemptOutcome.CORRECT, step.attempts, step.retries, step.support)
        })
    }
}
