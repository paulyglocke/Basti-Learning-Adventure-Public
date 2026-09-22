package com.bellfamily.bastischool.learning.session

import com.bellfamily.bastischool.learning.content.ContentRepository
import com.bellfamily.bastischool.learning.models.*
import java.util.Collections

internal fun <T> frozen(values: List<T>): List<T> = Collections.unmodifiableList(values.toList())
private fun identifier(value: String) = require(value.length in 1..96 && value.matches(Regex("[a-zA-Z0-9_.:-]+")))
private fun semantic(value: String, prefix: String) {
    identifier(value)
    require(value.startsWith(prefix) && value.matches(Regex("[a-z][a-z0-9_]*(\\.[a-z][a-z0-9_]*)+")))
}
data class SessionId(val value: String) { init { identifier(value) } }
data class ActivityId(val value: String) { init { semantic(value, "activity.") } }
data class TaskDefinitionId(val value: String) { init { semantic(value, "task.") } }
data class SkillId(val value: String) { init { semantic(value, "skill.") } }
data class LearningContextId(val value: String) { init { semantic(value, "context.") } }
data class TaskInstanceId(val session: SessionId, val ordinal: Int) { init { require(ordinal in 1..10) } }
data class AttemptId(val task: TaskInstanceId, val number: Int) { init { require(number > 0) } }
data class CompletionRequestId(val session: SessionId, val delivery: Int) { init { require(delivery > 0) } }

enum class RoundLength(val count: Int) { FIVE(5), TEN(10) }
enum class WrongAnswerPolicy { RETRY, LOCK }
data class SessionPolicy(val round: RoundLength, val wrongAnswer: WrongAnswerPolicy = WrongAnswerPolicy.RETRY)
enum class SessionPhase { ACTIVE, COMPLETED }
enum class AnswerState { UNANSWERED, RETRY_AVAILABLE, CORRECT, INCORRECT }
enum class CompletionState { NOT_REQUESTED, PENDING, ACKNOWLEDGED, FAILED }

/** A bounded choice question, not a universal game schema. All choices are canonical content IDs. */
class ChoiceQuestion(
    val definition: TaskDefinitionId,
    val skill: SkillId,
    val context: LearningContextId,
    val difficulty: Int,
    val instruction: ContentText,
    choices: List<ContentId>,
    val correct: ContentId,
    val correctFeedback: ContentText,
    val wrongFeedback: ContentText,
    val hint: ContentText? = null
) {
    val choices = frozen(choices)
    init {
        require(difficulty in 1..100)
        require(this.choices.size in 2..8 && this.choices.toSet().size == this.choices.size)
        require(correct in this.choices)
        require(this.choices.all { it.value.length <= 96 })
        texts().forEach { text -> text.strings().forEach { require(it.length <= 1000) } }
    }
    internal fun texts() = listOfNotNull(instruction, correctFeedback, wrongFeedback, hint)
    internal fun reordered(choices: List<ContentId>) = ChoiceQuestion(definition, skill, context, difficulty,
        instruction, choices, correct, correctFeedback, wrongFeedback, hint)
}
internal fun ContentText.strings() = listOf(display.en, display.de, speech.en, speech.de)
data class ChoiceTask(val id: TaskInstanceId, val question: ChoiceQuestion)

/** Complete frozen round. Changing future settings cannot change this plan. */
class SessionPlan(
    val id: SessionId,
    val activity: ActivityId,
    val activityRevision: Int,
    val contentVersion: ContentVersion,
    val policy: SessionPolicy,
    tasks: List<ChoiceTask>,
    val completionText: ContentText
) {
    val tasks = frozen(tasks)
    init {
        require(activityRevision > 0)
        require(this.tasks.size == policy.round.count)
        require(this.tasks.map { it.id } == (1..policy.round.count).map { TaskInstanceId(id, it) })
        require(completionText.strings().all { it.length <= 1000 })
        // Bounds the entire authored snapshot, not just the number of questions.
        require((this.tasks.flatMap { it.question.texts() } + completionText)
            .sumOf { it.strings().sumOf(String::length) } <= 16000)
    }
    fun validate(content: ContentRepository) {
        require(contentVersion == content.version) { "Content version mismatch" }
        tasks.forEach { task ->
            task.question.choices.forEach { require(content.find(it) != null) { "Missing content: ${it.value}" } }
        }
    }
}

data class SupportUse(val replays: Int = 0, val hint: Boolean = false, val parentHelp: Boolean = false) {
    init { require(replays >= 0) }
    val independent get() = replays == 0 && !hint && !parentHelp
}
data class TaskProgress(
    val answer: AnswerState = AnswerState.UNANSWERED,
    val attempts: Int = 0,
    val retries: Int = 0,
    val support: SupportUse = SupportUse(),
    val lastChoice: ContentId? = null
) {
    val locked get() = answer == AnswerState.CORRECT || answer == AnswerState.INCORRECT
}

/** Constructed only through start/reduce/validated restore; list snapshots cannot be mutated. */
class SessionState internal constructor(
    val plan: SessionPlan,
    val language: ContentLanguage,
    val index: Int,
    progress: List<TaskProgress>,
    val phase: SessionPhase,
    val completion: CompletionState = CompletionState.NOT_REQUESTED,
    val completionDelivery: Int = 0
) {
    val progress = frozen(progress)
    val task get() = plan.tasks[index]
    val current get() = progress[index]
    val score get() = progress.count { it.answer == AnswerState.CORRECT }
    val replayAvailable get() = true // Completed sessions replay their authored summary.
    val hintAvailable get() = phase == SessionPhase.ACTIVE && !current.locked && task.question.hint != null
    val nextAttempt get() = if (current.attempts < Int.MAX_VALUE) AttemptId(task.id, current.attempts + 1) else null
    val completionRequestId get() = if (completionDelivery > 0) CompletionRequestId(plan.id, completionDelivery) else null
    internal fun changed(language: ContentLanguage = this.language, index: Int = this.index,
                         progress: List<TaskProgress> = this.progress, phase: SessionPhase = this.phase,
                         completion: CompletionState = this.completion, delivery: Int = completionDelivery) =
        SessionState(plan, language, index, progress, phase, completion, delivery)
}

/** Future shared progress writes use these identities/support snapshots, not score as mastery. */
data class AttemptResult(val id: AttemptId, val skill: SkillId, val context: LearningContextId,
                         val difficulty: Int, val language: ContentLanguage, val choice: ContentId,
                         val correct: Boolean, val support: SupportUse)
class SessionResult internal constructor(val session: SessionId, val activity: ActivityId,
                                        val score: Int, tasks: List<Pair<ChoiceTask, TaskProgress>>) {
    val tasks = frozen(tasks)
}
data class CompletionRequest(val id: CompletionRequestId, val result: SessionResult)
