package com.bellfamily.bastischool.learning.colours

import com.bellfamily.bastischool.audio.SpeechTrigger
import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.progress.*
import com.bellfamily.bastischool.learning.sequencing.OrderedPlacement
import com.bellfamily.bastischool.learning.session.*
import java.util.Collections

enum class RainbowOrderLevel { STARTER, FULL }

/** Exact durable ordering snapshot. Starter's visible model is explicitly recorded as hint support. */
class RainbowOrderState internal constructor(
    val id: SessionId,
    val level: RainbowOrderLevel,
    val language: ContentLanguage,
    val seed: Long,
    choices: List<ContentId>,
    steps: List<TaskProgress>,
    val acknowledged: Boolean = false
) {
    val target = Collections.unmodifiableList(RainbowOrderContent.target(level).toList())
    val choices = Collections.unmodifiableList(choices.toList())
    val steps = Collections.unmodifiableList(steps.toList())
    private val placement = OrderedPlacement(target, this.choices, this.steps)
    val index get() = placement.index
    val completed get() = placement.completed
    val placed get() = placement.placed
    val task get() = TaskInstanceId(id, (index + 1).coerceAtMost(target.size))
    val current get() = steps[index.coerceAtMost(target.lastIndex)]
    val nextAttempt get() = if (!completed && current.answer == AnswerState.UNANSWERED)
        AttemptId(task, current.attempts + 1) else null

    init {
        require(choices == OrderedPlacement.scramble(target, seed))
        require(steps.size == target.size)
        require(!acknowledged || completed)
    }

    internal fun changed(language: ContentLanguage = this.language, steps: List<TaskProgress> = this.steps,
                         acknowledged: Boolean = this.acknowledged) =
        RainbowOrderState(id, level, language, seed, choices, steps, acknowledged)
}

sealed interface RainbowOrderAction {
    data class Place(val attempt: AttemptId, val colour: ContentId) : RainbowOrderAction
    data class Retry(val attempt: AttemptId) : RainbowOrderAction
    data class Replay(val task: TaskInstanceId) : RainbowOrderAction
    data class Help(val task: TaskInstanceId) : RainbowOrderAction
    data class Language(val language: ContentLanguage) : RainbowOrderAction
}

data class RainbowOrderTransition(val state: RainbowOrderState, val events: List<ProgressEvent> = emptyList(),
                                  val speech: SessionEffect.Narrate? = null)

object RainbowOrderContent {
    const val REVISION = 1
    val version = ContentVersion(1, 1)
    val activity = ActivityId("activity.colours.rainbow_order")
    val starterOrder = listOf(0, 2, 4, 6).map(RainbowContent.order::get)
    val fullOrder = RainbowContent.order
    fun target(level: RainbowOrderLevel) = if (level == RainbowOrderLevel.STARTER) starterOrder else fullOrder
    fun journalName(level: RainbowOrderLevel) = if (level == RainbowOrderLevel.STARTER)
        "rainbow-order-starter-session" else "rainbow-order-full-session"
    val instruction = ContentText.plain("Place the colours from the rainbow's outside edge toward the inside.",
        "Ordne die Farben vom äußeren Rand des Regenbogens nach innen.")
    val starterInstruction = ContentText.plain("Use the complete rainbow as your example. Start outside and move inward.",
        "Nutze den ganzen Regenbogen als Beispiel. Beginne außen und ordne nach innen.")
    val completion = ContentText.plain("You put the rainbow colours in order!", "Du hast die Regenbogenfarben geordnet!")

    fun start(id: SessionId, level: RainbowOrderLevel, seed: Long, language: ContentLanguage): RainbowOrderState {
        val target = target(level)
        val choices = OrderedPlacement.scramble(target, seed)
        return RainbowOrderState(id, level, language, seed, choices, List(target.size) { TaskProgress() })
    }

    fun prompt(s: RainbowOrderState): ContentText = when {
        s.completed -> completion
        s.index == 0 -> if (s.level == RainbowOrderLevel.STARTER) starterInstruction else instruction
        else -> RainbowContent.definition(s.placed.last()).text.speech.let {
            ContentText.plain("${it.en} is in place. Which colour comes next?", "${it.de.replaceFirstChar(Char::uppercase)} ist an seinem Platz. Welche Farbe kommt als Nächstes?")
        }
    }

    fun help(s: RainbowOrderState): ContentText = if (s.completed) completion else
        RainbowContent.definition(s.target[s.index]).text.speech.let {
            ContentText.plain("The next colour is ${it.en}.", "Als Nächstes kommt ${it.de.replaceFirstChar(Char::uppercase)}.")
        }

    fun reduce(s: RainbowOrderState, action: RainbowOrderAction): RainbowOrderTransition {
        fun unchanged() = RainbowOrderTransition(s)
        fun current(p: TaskProgress) = s.changed(steps = s.steps.toMutableList().also { it[s.index] = p })
        return when (action) {
            is RainbowOrderAction.Language -> RainbowOrderTransition(s.changed(language = action.language))
            is RainbowOrderAction.Place -> {
                if (s.nextAttempt != action.attempt || action.colour !in s.choices || action.colour in s.placed ||
                    s.current.attempts >= 10000) return unchanged()
                val correct = action.colour == s.target[s.index]
                val nextSteps = OrderedPlacement(s.target, s.choices, s.steps).place(action.colour) ?: return unchanged()
                val assessedSteps = if (s.level == RainbowOrderLevel.STARTER) nextSteps.mapIndexed { index, progress ->
                    if (index == s.index) progress.copy(support = progress.support.copy(hint = true)) else progress
                } else nextSteps
                val next = s.changed(steps = assessedSteps)
                val events = listOf(attempt(next, s.index)) + if (next.completed) listOf(completion(next)) else emptyList()
                val feedback = if (correct) prompt(next) else ContentText.plain(
                    "Have another look at the example. Try a different colour.",
                    "Schau noch einmal auf das Beispiel. Versuche eine andere Farbe.")
                RainbowOrderTransition(next, events, SessionEffect.Narrate(feedback,
                    if (next.completed) NarrationKind.COMPLETION else if (correct) NarrationKind.INSTRUCTION else NarrationKind.FEEDBACK,
                    SpeechTrigger.AUTOMATIC))
            }
            is RainbowOrderAction.Retry -> {
                if (s.completed || s.current.answer != AnswerState.RETRY_AVAILABLE ||
                    action.attempt != AttemptId(s.task, s.current.attempts)) unchanged()
                else RainbowOrderTransition(current(s.current.copy(answer = AnswerState.UNANSWERED,
                    retries = s.current.retries + 1)))
            }
            is RainbowOrderAction.Replay -> {
                if (action.task != s.task) unchanged() else {
                    val next = if (s.completed) s else current(s.current.copy(support = s.current.support.copy(
                        replays = (s.current.support.replays + 1).coerceAtMost(10000))))
                    RainbowOrderTransition(next, speech = SessionEffect.Narrate(prompt(next),
                        if (next.completed) NarrationKind.COMPLETION else NarrationKind.INSTRUCTION, SpeechTrigger.REPLAY))
                }
            }
            is RainbowOrderAction.Help -> {
                if (s.completed || action.task != s.task) unchanged() else {
                    val next = current(s.current.copy(support = s.current.support.copy(hint = true)))
                    RainbowOrderTransition(next, speech = SessionEffect.Narrate(help(next), NarrationKind.INSTRUCTION, SpeechTrigger.MANUAL))
                }
            }
        }
    }

    private fun origin(s: RainbowOrderState) = ProgressOrigin(s.id, activity, REVISION, version)
    private fun evidence(s: RainbowOrderState, i: Int) = TaskEvidence(TaskInstanceId(s.id, i + 1),
        TaskDefinitionId("task.colours.rainbow_order.${s.level.name.lowercase()}.position_${i + 1}"),
        SkillId("skill.colours.rainbow_sequence"),
        LearningContextId("context.colours.rainbow_order.${s.level.name.lowercase()}"),
        if (s.level == RainbowOrderLevel.STARTER) 1 else 2)

    fun attempt(s: RainbowOrderState, i: Int): AttemptEvent {
        val step = s.steps[i]
        return AttemptEvent(origin(s), evidence(s, i), AttemptId(TaskInstanceId(s.id, i + 1), step.attempts),
            s.language, requireNotNull(step.lastChoice),
            if (step.answer == AnswerState.CORRECT) AttemptOutcome.CORRECT else AttemptOutcome.INCORRECT, step.support)
    }

    fun completion(s: RainbowOrderState): CompletionEvent {
        require(s.completed)
        return CompletionEvent(origin(s), s.steps.mapIndexed { i, p ->
            CompletedTask(evidence(s, i), requireNotNull(p.lastChoice), AttemptOutcome.CORRECT, p.attempts, p.retries, p.support)
        })
    }
}
