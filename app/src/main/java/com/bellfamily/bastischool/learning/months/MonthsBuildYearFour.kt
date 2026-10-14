package com.bellfamily.bastischool.learning.months

import com.bellfamily.bastischool.audio.SpeechTrigger
import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.progress.*
import com.bellfamily.bastischool.learning.session.*
import com.bellfamily.bastischool.learning.sequencing.OrderedPlacement
import java.util.Collections
import java.util.Random

/** A five- or ten-sequence round of four consecutive month placements. */
class MonthsBuildYearFourState internal constructor(
    val id: SessionId,
    val round: RoundLength,
    val language: ContentLanguage,
    starts: List<ContentId>,
    choices: List<List<ContentId>>,
    steps: List<List<TaskProgress>>,
    val questionIndex: Int = 0,
    val acknowledged: Boolean = false
) {
    val starts = Collections.unmodifiableList(starts.toList())
    val choices = Collections.unmodifiableList(choices.map { Collections.unmodifiableList(it.toList()) })
    val steps = Collections.unmodifiableList(steps.map { Collections.unmodifiableList(it.toList()) })
    val completed get() = questionIndex == round.count
    private val currentQuestion get() = (questionIndex.coerceAtMost(round.count - 1))
    val order get() = MonthsBuildYearFour.sequence(starts[currentQuestion])
    private val placement get() = OrderedPlacement(order, choices[currentQuestion], steps[currentQuestion])
    val index get() = if (completed) 4 else placement.index
    val placed get() = if (completed) order else placement.placed
    val sequenceReady get() = !completed && placement.completed
    val task get() = TaskInstanceId(id, currentQuestion + 1)
    val placementTask get() = task
    val currentAttemptNumber get() = steps[currentQuestion].take(index.coerceAtMost(3) + 1).sumOf { it.attempts }
    val current get() = steps[currentQuestion][index.coerceAtMost(3)]
    val nextAttempt get() = if (!completed && !sequenceReady && current.answer == AnswerState.UNANSWERED)
        AttemptId(task, currentAttemptNumber + 1) else null

    init {
        require(starts.size == round.count && starts.distinct().size == starts.size)
        require(choices.size == round.count && steps.size == round.count)
        require(questionIndex in 0..round.count && (!acknowledged || completed))
        starts.forEachIndexed { question, start ->
            MonthContent.month(start)
            val expected = MonthsBuildYearFour.sequence(start)
            require(choices[question].size == 4 && choices[question].toSet() == expected.toSet())
            require(choices[question] != expected)
            require(steps[question].size == 4)
            OrderedPlacement(expected, choices[question], steps[question])
            if (question < questionIndex) require(steps[question].all { it.answer == AnswerState.CORRECT })
            if (question > questionIndex) require(steps[question].all { it == TaskProgress() })
        }
        if (completed) require(starts.indices.all { q -> steps[q].all { it.answer == AnswerState.CORRECT } })
    }

    internal fun changed(
        language: ContentLanguage = this.language,
        steps: List<List<TaskProgress>> = this.steps,
        questionIndex: Int = this.questionIndex,
        acknowledged: Boolean = this.acknowledged
    ) = MonthsBuildYearFourState(id, round, language, starts, choices, steps, questionIndex, acknowledged)
}

sealed interface MonthsBuildYearFourAction {
    data class Place(val attempt: AttemptId, val month: ContentId) : MonthsBuildYearFourAction
    data class Retry(val attempt: AttemptId) : MonthsBuildYearFourAction
    data class Replay(val task: TaskInstanceId) : MonthsBuildYearFourAction
    data class Help(val task: TaskInstanceId) : MonthsBuildYearFourAction
    data class Continue(val task: TaskInstanceId) : MonthsBuildYearFourAction
    data class Language(val language: ContentLanguage) : MonthsBuildYearFourAction
}

data class MonthsBuildYearFourTransition(
    val state: MonthsBuildYearFourState,
    val events: List<ProgressEvent> = emptyList(),
    val speech: SessionEffect.Narrate? = null
)

object MonthsBuildYearFour {
    const val REVISION = 1
    val activity = ActivityId("activity.months.build_year.four")
    val skill = SkillId("skill.months.sequence.four")
    val context = LearningContextId("context.months.year_order")
    val introduction = ContentText.plain(
        "Put these four months in the right order.",
        "Bringe diese vier Monate in die richtige Reihenfolge.")
    val demonstration = MonthIds.canonicalOrder.take(4)
    val continuePrompt = ContentText.plain("Lovely ordering. Continue when you are ready.", "Gut geordnet. Mach weiter, wenn du bereit bist.")
    val completion = ContentText.plain("You built four months in order!", "Du hast vier Monate geordnet!")

    fun sequence(start: ContentId): List<ContentId> {
        val index = MonthIds.canonicalOrder.indexOf(start)
        require(index >= 0) { "Unknown starting month: ${start.value}" }
        return List(4) { offset -> MonthIds.canonicalOrder[(index + offset) % 12] }
    }

    fun generate(id: SessionId, round: RoundLength, seed: Long, language: ContentLanguage): MonthsBuildYearFourState {
        val random = Random(seed)
        val starts = MonthIds.canonicalOrder.shuffled(random).take(round.count)
        val choices = starts.mapIndexed { index, start ->
            OrderedPlacement.scramble(sequence(start), seed xor (0x4D4F4E544846L + index * 0x9E3779B9L))
        }
        return MonthsBuildYearFourState(id, round, language, starts, choices,
            List(round.count) { List(4) { TaskProgress() } })
    }

    fun prompt(state: MonthsBuildYearFourState): ContentText = when {
        state.completed -> completion
        state.sequenceReady -> continuePrompt
        else -> introduction
    }

    fun hint(state: MonthsBuildYearFourState): ContentText {
        val next = MonthContent.month(state.order[state.index.coerceAtMost(3)]).text.speech
        return ContentText.plain("The next month is ${next.en}.", "Der nächste Monat ist ${next.de}.")
    }

    fun reduce(state: MonthsBuildYearFourState, action: MonthsBuildYearFourAction): MonthsBuildYearFourTransition {
        fun unchanged() = MonthsBuildYearFourTransition(state)
        fun update(progress: TaskProgress): MonthsBuildYearFourState {
            val next = state.steps.map { it.toList() }.toMutableList()
            next[state.questionIndex] = state.steps[state.questionIndex].toMutableList().also { it[state.index] = progress }
            return state.changed(steps = next)
        }
        return when (action) {
            is MonthsBuildYearFourAction.Language -> MonthsBuildYearFourTransition(state.changed(language = action.language))
            is MonthsBuildYearFourAction.Place -> {
                val attempt = state.nextAttempt
                if (attempt == null || attempt != action.attempt || action.month !in state.order.drop(state.index) || state.current.attempts >= 10_000) return unchanged()
                val q = state.questionIndex
                val p = state.index
                val next = update(OrderedPlacement(state.order, state.choices[q], state.steps[q]).place(action.month)?.get(p)
                    ?: return unchanged())
                val events = buildList {
                    if (action.month != state.order[p]) {
                        add(attempt(next, q, action.month, AttemptOutcome.INCORRECT,
                            next.steps[q].sumOf { it.retries } + 1, next.current.support))
                    } else if (next.sequenceReady) {
                        add(attempt(next, q, action.month, AttemptOutcome.CORRECT,
                            next.steps[q].sumOf { it.retries } + 1, next.current.support))
                    }
                }
                val response = when {
                    next.completed -> completion
                    next.sequenceReady -> continuePrompt
                    action.month == state.order[p] -> {
                        val placedMonth = MonthContent.month(action.month).text.speech
                        ContentText.plain("Yes, ${placedMonth.en}. Choose the next month.", "Ja, ${placedMonth.de}. Wähle den nächsten Monat.")
                    }
                    else -> ContentText.plain(
                        "Not quite. Months already placed stay here. Try another month.",
                        "Noch nicht. Die eingeordneten Monate bleiben hier. Versuche es mit einem anderen Monat.")
                }
                MonthsBuildYearFourTransition(next, events, SessionEffect.Narrate(response,
                    if (next.completed) NarrationKind.COMPLETION else if (action.month == state.order[p]) NarrationKind.INSTRUCTION else NarrationKind.FEEDBACK,
                    SpeechTrigger.AUTOMATIC))
            }
            is MonthsBuildYearFourAction.Retry -> if (state.completed || state.sequenceReady ||
                state.current.answer != AnswerState.RETRY_AVAILABLE || action.attempt != AttemptId(state.task, state.currentAttemptNumber)) unchanged()
                else MonthsBuildYearFourTransition(update(state.current.copy(answer = AnswerState.UNANSWERED, retries = state.current.retries + 1)))
            is MonthsBuildYearFourAction.Replay -> if (state.completed || action.task != state.task) unchanged() else {
                val next = if (state.sequenceReady) state else update(state.current.copy(
                    support = state.current.support.copy(replays = (state.current.support.replays + 1).coerceAtMost(10_000))))
                MonthsBuildYearFourTransition(next, speech = SessionEffect.Narrate(prompt(next),
                    if (state.sequenceReady) NarrationKind.FEEDBACK else NarrationKind.INSTRUCTION, SpeechTrigger.REPLAY))
            }
            is MonthsBuildYearFourAction.Help -> if (state.completed || state.sequenceReady || action.task != state.task) unchanged() else {
                val next = update(state.current.copy(support = state.current.support.copy(hint = true)))
                MonthsBuildYearFourTransition(next, speech = SessionEffect.Narrate(hint(next), NarrationKind.INSTRUCTION, SpeechTrigger.MANUAL))
            }
            is MonthsBuildYearFourAction.Continue -> if (!state.sequenceReady || action.task != state.task) unchanged() else {
                val next = state.changed(questionIndex = state.questionIndex + 1)
                MonthsBuildYearFourTransition(next, if (next.completed) listOf(completion(next)) else emptyList(), speech = SessionEffect.Narrate(prompt(next),
                    if (next.completed) NarrationKind.COMPLETION else NarrationKind.INSTRUCTION, SpeechTrigger.AUTOMATIC))
            }
        }
    }

    internal fun evidenceFor(state: MonthsBuildYearFourState, question: Int) = TaskEvidence(TaskInstanceId(state.id, question + 1),
        TaskDefinitionId("task.months.build_year.four.window.${state.starts[question].value.substringAfter('.')}") , skill, context, 4)

    private fun origin(state: MonthsBuildYearFourState) = ProgressOrigin(state.id, activity, REVISION, MonthContent.repository.version)

    fun attempt(state: MonthsBuildYearFourState, question: Int, choice: ContentId,
        outcome: AttemptOutcome, attemptNumber: Int, support: SupportUse): AttemptEvent {
        return AttemptEvent(origin(state), evidenceFor(state, question),
            AttemptId(TaskInstanceId(state.id, question + 1), attemptNumber), state.language, choice, outcome, support)
    }

    fun completion(state: MonthsBuildYearFourState): CompletionEvent {
        require(state.completed)
        val tasks = state.starts.mapIndexed { question, start ->
            val sequenceSteps = state.steps[question]
            val support = SupportUse(
                replays = sequenceSteps.sumOf { it.support.replays }.coerceAtMost(10_000),
                hint = sequenceSteps.any { it.support.hint },
                parentHelp = sequenceSteps.any { it.support.parentHelp })
            val order = sequence(start)
            val retries = sequenceSteps.sumOf { it.retries }
            CompletedTask(evidenceFor(state, question),
                order.last(), AttemptOutcome.CORRECT, retries + 1, retries, support)
        }
        return CompletionEvent(origin(state), tasks)
    }
}
