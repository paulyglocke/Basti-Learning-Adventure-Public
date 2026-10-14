package com.bellfamily.bastischool.learning.follow

import com.bellfamily.bastischool.audio.SpeechTrigger
import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.progress.*
import com.bellfamily.bastischool.learning.session.*
import java.util.Collections
import java.util.Random

data class FollowTwoTask(val first: ContentId, val second: ContentId, val choices: List<ContentId>) {
    init {
        require(first != second && choices.size == 4 && choices.toSet().size == 4)
        require(first in choices && second in choices)
        choices.forEach(FollowContent::objectFor)
    }
    val definition get() = TaskDefinitionId("task.follow.order.${first.value.substringAfterLast('.')}.${second.value.substringAfterLast('.')}")
}

class FollowTwoState internal constructor(
    val id: SessionId, val round: RoundLength, val language: ContentLanguage,
    tasks: List<FollowTwoTask>, steps: List<List<TaskProgress>>,
    val questionIndex: Int = 0, val acknowledged: Boolean = false
) {
    val tasks = Collections.unmodifiableList(tasks.map { it.copy(choices = Collections.unmodifiableList(it.choices.toList())) })
    val steps = Collections.unmodifiableList(steps.map { Collections.unmodifiableList(it.toList()) })
    val completed get() = questionIndex == round.count
    private val activeQuestion get() = questionIndex.coerceAtMost(round.count - 1)
    val task get() = tasks[activeQuestion]
    val taskId get() = TaskInstanceId(id, activeQuestion + 1)
    val step get() = if (steps[activeQuestion][0].answer == AnswerState.CORRECT) 1 else 0
    val current get() = steps[activeQuestion][step]
    val readyToContinue get() = !completed && steps[activeQuestion][1].answer == AnswerState.CORRECT
    val nextAttempt get() = if (!completed && !readyToContinue && current.answer == AnswerState.UNANSWERED)
        AttemptId(taskId, steps[activeQuestion].sumOf { it.retries } + 1) else null

    init {
        require(tasks.size == round.count && tasks.map { it.definition }.distinct().size == round.count)
        require(steps.size == round.count && questionIndex in 0..round.count && (!acknowledged || completed))
        tasks.forEachIndexed { q, task ->
            require(steps[q].size == 2)
            steps[q].forEachIndexed { s, progress ->
                val expected = if (s == 0) task.first else task.second
                require(progress.answer in setOf(AnswerState.UNANSWERED, AnswerState.RETRY_AVAILABLE, AnswerState.CORRECT))
                require(progress.attempts in 0..10_000 && progress.retries in 0..10_000)
                require(progress.attempts == progress.retries + (if (progress.answer == AnswerState.UNANSWERED) 0 else 1))
                if (progress.answer == AnswerState.CORRECT) require(progress.lastChoice == expected)
                if (progress.answer == AnswerState.RETRY_AVAILABLE) require(progress.lastChoice in task.choices && progress.lastChoice != expected)
            }
            if (q < questionIndex) require(steps[q].all { it.answer == AnswerState.CORRECT })
            if (q > questionIndex) require(steps[q].all { it == TaskProgress() })
            if (steps[q][0].answer != AnswerState.CORRECT) require(steps[q][1] == TaskProgress())
            if (steps[q][1].answer == AnswerState.CORRECT) require(steps[q][0].answer == AnswerState.CORRECT)
        }
        if (completed) require(steps.all { pair -> pair.all { it.answer == AnswerState.CORRECT } })
    }
    internal fun changed(language: ContentLanguage = this.language, steps: List<List<TaskProgress>> = this.steps,
        questionIndex: Int = this.questionIndex, acknowledged: Boolean = this.acknowledged) =
        FollowTwoState(id, round, language, tasks, steps, questionIndex, acknowledged)
}

sealed interface FollowTwoAction {
    data class Choose(val attempt: AttemptId, val objectId: ContentId) : FollowTwoAction
    data class Retry(val attempt: AttemptId) : FollowTwoAction
    data class Replay(val task: TaskInstanceId) : FollowTwoAction
    data class Hint(val task: TaskInstanceId) : FollowTwoAction
    data class Next(val task: TaskInstanceId) : FollowTwoAction
    data class Language(val language: ContentLanguage) : FollowTwoAction
}

data class FollowTwoTransition(val state: FollowTwoState, val events: List<ProgressEvent> = emptyList(),
    val speech: SessionEffect.Narrate? = null)

object FollowTwo {
    const val REVISION = 1
    val activity = ActivityId("activity.follow.instructions.two")
    val skill = SkillId("skill.listening.two_step")
    val context = LearningContextId("context.follow.animal_order")
    val completion = ContentText.plain("You followed both steps!", "Du hast beide Schritte befolgt!")
    val example = ContentText.plain("Listen to the whole instruction. Touch the first animal, then the second.",
        "Höre dir die ganze Anweisung an. Tippe zuerst auf das erste Tier und dann auf das zweite.")
    val demonstration = ContentText.plain("For example: first touch the dinosaur, then touch the horse.",
        "Zum Beispiel: Tippe zuerst auf den Dinosaurier und dann auf das Pferd.")
    private val wrong = ContentText.plain("Not quite. Listen again and try this step.",
        "Noch nicht ganz. Hör noch einmal zu und versuche diesen Schritt erneut.")
    private val firstDone = ContentText.plain("Good listening. Now do the second step.",
        "Gut zugehört. Jetzt kommt der zweite Schritt.")
    private val ready = ContentText.plain("Both steps done. Continue when you are ready.",
        "Beide Schritte geschafft. Mach weiter, wenn du bereit bist.")
    private val hintFirst = ContentText.plain("Listen for the animal named first.", "Hör auf das zuerst genannte Tier.")
    private val hintSecond = ContentText.plain("Listen again for the second animal.", "Hör noch einmal auf das zweite Tier.")

    fun instruction(task: FollowTwoTask): ContentText {
        val first = FollowContent.objectFor(task.first)
        val second = FollowContent.objectFor(task.second)
        val choose = first.id.value.substringAfterLast('.') in setOf("snake", "fish", "whale")
        return if (choose) ContentText.plain("First choose ${first.text.display.en}, then choose ${second.text.display.en}.",
            "Wähle zuerst ${first.germanObject} und dann ${second.germanObject} aus.")
        else ContentText.plain("First touch ${first.text.display.en}, then touch ${second.text.display.en}.",
            "Tippe zuerst auf ${first.germanObject} und dann auf ${second.germanObject}.")
    }

    fun generate(id: SessionId, round: RoundLength, seed: Long, language: ContentLanguage): FollowTwoState {
        val random = Random(seed)
        val objects = FollowContent.objects.map { it.id }
        val firsts = mutableListOf<ContentId>()
        while (firsts.size < round.count) {
            val cycle = objects.shuffled(random).toMutableList()
            if (firsts.isNotEmpty() && cycle.first() == firsts.last()) Collections.swap(cycle, 0, 1)
            firsts += cycle.take(round.count - firsts.size)
        }
        val used = mutableSetOf<Pair<ContentId, ContentId>>()
        val tasks = firsts.map { first ->
            val second = objects.filter { it != first && (first to it) !in used &&
                !(first.value.endsWith("dinosaur") && it.value.endsWith("horse")) }.shuffled(random).first()
            used += first to second
            val choices = (listOf(first, second) + objects.filter { it != first && it != second }.shuffled(random).take(2)).shuffled(random)
            FollowTwoTask(first, second, choices)
        }
        return FollowTwoState(id, round, language, tasks, List(round.count) { List(2) { TaskProgress() } })
    }

    fun reduce(state: FollowTwoState, action: FollowTwoAction): FollowTwoTransition {
        fun unchanged() = FollowTwoTransition(state)
        fun update(progress: TaskProgress): FollowTwoState {
            val all = state.steps.map { it.toMutableList() }.toMutableList()
            all[state.questionIndex][state.step] = progress
            return state.changed(steps = all)
        }
        return when (action) {
            is FollowTwoAction.Language -> FollowTwoTransition(state.changed(language = action.language))
            is FollowTwoAction.Choose -> {
                val attempt = state.nextAttempt ?: return unchanged()
                if (action.attempt != attempt || action.objectId !in state.task.choices) return unchanged()
                val correct = action.objectId == (if (state.step == 0) state.task.first else state.task.second)
                val next = update(state.current.copy(answer = if (correct) AnswerState.CORRECT else AnswerState.RETRY_AVAILABLE,
                    attempts = state.current.attempts + 1, lastChoice = action.objectId))
                val events = if (!correct || next.readyToContinue) listOf(attempt(next, action.objectId,
                    if (correct) AttemptOutcome.CORRECT else AttemptOutcome.INCORRECT, attempt.number)) else emptyList()
                FollowTwoTransition(next, events, SessionEffect.Narrate(if (!correct) wrong else if (next.readyToContinue) ready else firstDone,
                    NarrationKind.FEEDBACK, SpeechTrigger.AUTOMATIC))
            }
            is FollowTwoAction.Retry -> if (state.completed || state.readyToContinue ||
                state.current.answer != AnswerState.RETRY_AVAILABLE ||
                action.attempt != AttemptId(state.taskId, state.steps[state.questionIndex].sumOf { it.retries } + 1)) unchanged()
                else FollowTwoTransition(update(state.current.copy(answer = AnswerState.UNANSWERED,
                    retries = state.current.retries + 1)), speech = SessionEffect.Narrate(instruction(state.task), NarrationKind.INSTRUCTION))
            is FollowTwoAction.Replay -> if (action.task != state.taskId) unchanged() else if (state.completed)
                FollowTwoTransition(state, speech = SessionEffect.Narrate(completion, NarrationKind.COMPLETION, SpeechTrigger.REPLAY))
                else {
                val next = if (state.readyToContinue) state else update(state.current.copy(support = state.current.support.copy(
                    replays = (state.current.support.replays + 1).coerceAtMost(10_000))))
                FollowTwoTransition(next, speech = SessionEffect.Narrate(instruction(state.task), NarrationKind.INSTRUCTION, SpeechTrigger.REPLAY))
            }
            is FollowTwoAction.Hint -> if (state.completed || state.readyToContinue || action.task != state.taskId) unchanged() else {
                val next = update(state.current.copy(support = state.current.support.copy(hint = true)))
                FollowTwoTransition(next, speech = SessionEffect.Narrate(if (state.step == 0) hintFirst else hintSecond,
                    NarrationKind.INSTRUCTION, SpeechTrigger.MANUAL))
            }
            is FollowTwoAction.Next -> if (!state.readyToContinue || action.task != state.taskId) unchanged() else {
                val next = state.changed(questionIndex = state.questionIndex + 1)
                FollowTwoTransition(next, if (next.completed) listOf(completion(next)) else emptyList(),
                    SessionEffect.Narrate(if (next.completed) completion else instruction(next.task),
                        if (next.completed) NarrationKind.COMPLETION else NarrationKind.INSTRUCTION))
            }
        }
    }

    internal fun evidence(state: FollowTwoState, q: Int) = TaskEvidence(TaskInstanceId(state.id, q + 1),
        state.tasks[q].definition, skill, context, 2)
    private fun origin(state: FollowTwoState) = ProgressOrigin(state.id, activity, REVISION, FollowContent.repository.version)
    private fun support(state: FollowTwoState, q: Int) = SupportUse(
        state.steps[q].sumOf { it.support.replays }.coerceAtMost(10_000),
        state.steps[q].any { it.support.hint }, state.steps[q].any { it.support.parentHelp })
    fun attempt(state: FollowTwoState, choice: ContentId, outcome: AttemptOutcome, number: Int): AttemptEvent =
        AttemptEvent(origin(state), evidence(state, state.questionIndex), AttemptId(state.taskId, number),
            state.language, choice, outcome, support(state, state.questionIndex))
    fun completion(state: FollowTwoState): CompletionEvent {
        require(state.completed)
        return CompletionEvent(origin(state), state.tasks.indices.map { q ->
            val retries = state.steps[q].sumOf { it.retries }
            CompletedTask(evidence(state, q), state.tasks[q].second, AttemptOutcome.CORRECT,
                retries + 1, retries, support(state, q))
        })
    }
}
