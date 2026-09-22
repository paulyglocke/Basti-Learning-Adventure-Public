package com.bellfamily.bastischool.learning.session

import com.bellfamily.bastischool.learning.content.ContentRepository
import com.bellfamily.bastischool.learning.models.*
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException

sealed interface SessionRestoreResult {
    data class Restored(val state: SessionState) : SessionRestoreResult
    data class Rejected(val reason: CheckpointRejection) : SessionRestoreResult
}
enum class CheckpointRejection { MALFORMED, INCOMPATIBLE, TOO_LARGE }

/** Platform-free binary snapshot. No Java object serialization, seed regeneration or side effects. */
object SessionCheckpoint {
    const val VERSION = 1
    const val MAX_BYTES = 100_000
    private const val MAGIC = 0x42415354

    fun encode(state: SessionState): ByteArray {
        validateState(state)
        val bytes = ByteArrayOutputStream()
        DataOutputStream(bytes).use { out ->
            val plan = state.plan
            out.writeInt(MAGIC); out.writeInt(VERSION)
            out.writeUTF(plan.activity.value); out.writeInt(plan.activityRevision)
            out.writeInt(plan.contentVersion.schema); out.writeInt(plan.contentVersion.revision)
            out.writeUTF(plan.id.value)
            out.writeUTF(plan.policy.round.name); out.writeUTF(plan.policy.wrongAnswer.name)
            out.text(plan.completionText)
            out.writeInt(plan.tasks.size)
            plan.tasks.forEach { task ->
                out.writeInt(task.id.ordinal)
                val q = task.question
                out.writeUTF(q.definition.value); out.writeUTF(q.skill.value); out.writeUTF(q.context.value)
                out.writeInt(q.difficulty); out.text(q.instruction)
                out.writeInt(q.choices.size); q.choices.forEach { out.writeUTF(it.value) }
                out.writeUTF(q.correct.value); out.text(q.correctFeedback); out.text(q.wrongFeedback)
                out.writeBoolean(q.hint != null); q.hint?.let { out.text(it) }
            }
            out.writeUTF(state.language.name); out.writeInt(state.index); out.writeInt(state.score)
            out.writeUTF(state.phase.name); out.writeUTF(state.completion.name); out.writeInt(state.completionDelivery)
            state.progress.forEach {
                out.writeUTF(it.answer.name); out.writeInt(it.attempts); out.writeInt(it.retries)
                out.writeInt(it.support.replays); out.writeBoolean(it.support.hint); out.writeBoolean(it.support.parentHelp)
                out.writeBoolean(it.lastChoice != null); it.lastChoice?.let { choice -> out.writeUTF(choice.value) }
            }
        }
        return bytes.toByteArray().also { require(it.size <= MAX_BYTES) { "Checkpoint exceeds byte bound" } }
    }

    fun restore(bytes: ByteArray, activity: ActivityId, activityRevision: Int,
                content: ContentRepository): SessionRestoreResult {
        if (bytes.size > MAX_BYTES) return SessionRestoreResult.Rejected(CheckpointRejection.TOO_LARGE)
        return try {
            DataInputStream(ByteArrayInputStream(bytes)).use { input ->
                require(input.readInt() == MAGIC)
                if (input.readInt() != VERSION) return SessionRestoreResult.Rejected(CheckpointRejection.INCOMPATIBLE)
                val savedActivity = ActivityId(input.string())
                val savedRevision = input.readInt()
                val version = ContentVersion(input.readInt(), input.readInt())
                if (savedActivity != activity || savedRevision != activityRevision || version != content.version)
                    return SessionRestoreResult.Rejected(CheckpointRejection.INCOMPATIBLE)
                val id = SessionId(input.string())
                val policy = SessionPolicy(enumValueOf(input.string()), enumValueOf(input.string()))
                val summary = input.text()
                val count = input.readInt()
                require(count == policy.round.count)
                val tasks = List(count) {
                    val taskId = TaskInstanceId(id, input.readInt())
                    val definition = TaskDefinitionId(input.string())
                    val skill = SkillId(input.string())
                    val context = LearningContextId(input.string())
                    val difficulty = input.readInt()
                    val instruction = input.text()
                    val optionCount = input.readInt().also { require(it in 2..8) }
                    val choices = List(optionCount) { ContentId(input.string()) }
                    val correct = ContentId(input.string())
                    val rightFeedback = input.text()
                    val wrongFeedback = input.text()
                    val hint = if (input.boolean()) input.text() else null
                    ChoiceTask(taskId, ChoiceQuestion(definition, skill, context, difficulty, instruction,
                        choices, correct, rightFeedback, wrongFeedback, hint))
                }
                val plan = SessionPlan(id, savedActivity, savedRevision, version, policy, tasks, summary)
                plan.validate(content)
                val language = enumValueOf<ContentLanguage>(input.string())
                val index = input.readInt()
                val score = input.readInt()
                val phase = enumValueOf<SessionPhase>(input.string())
                val completion = enumValueOf<CompletionState>(input.string())
                val delivery = input.readInt()
                val progress = List(count) {
                    val answer = enumValueOf<AnswerState>(input.string())
                    val attempts = input.readInt(); val retries = input.readInt()
                    val support = SupportUse(input.readInt(), input.boolean(), input.boolean())
                    val choice = if (input.boolean()) ContentId(input.string()) else null
                    TaskProgress(answer, attempts, retries, support, choice)
                }
                require(input.available() == 0)
                val state = SessionState(plan, language, index, progress, phase, completion, delivery)
                validateState(state)
                require(state.score == score)
                SessionRestoreResult.Restored(state)
            }
        } catch (_: IOException) {
            SessionRestoreResult.Rejected(CheckpointRejection.MALFORMED)
        } catch (_: IllegalArgumentException) {
            SessionRestoreResult.Rejected(CheckpointRejection.MALFORMED)
        }
    }

    private fun validateState(state: SessionState) {
        require(state.index in state.plan.tasks.indices && state.progress.size == state.plan.tasks.size)
        if (state.phase == SessionPhase.COMPLETED) {
            require(state.index == state.plan.tasks.lastIndex && state.progress.all { it.locked })
            require(state.completion != CompletionState.NOT_REQUESTED && state.completionDelivery > 0)
        } else require(state.completion == CompletionState.NOT_REQUESTED && state.completionDelivery == 0)
        state.progress.forEachIndexed { index, p ->
            val q = state.plan.tasks[index].question
            require(p.attempts >= 0 && p.retries >= 0)
            require(!p.support.hint || q.hint != null)
            if (index < state.index) require(p.locked)
            if (index > state.index) require(p == TaskProgress())
            if (p.attempts == 0) require(p.lastChoice == null && p.retries == 0 && p.answer == AnswerState.UNANSWERED)
            else {
                require(p.lastChoice in q.choices)
                when (p.answer) {
                    AnswerState.UNANSWERED -> require(state.plan.policy.wrongAnswer == WrongAnswerPolicy.RETRY &&
                        p.retries == p.attempts && p.lastChoice != q.correct)
                    AnswerState.RETRY_AVAILABLE -> require(state.plan.policy.wrongAnswer == WrongAnswerPolicy.RETRY &&
                        p.retries == p.attempts - 1 && p.lastChoice != q.correct)
                    AnswerState.CORRECT -> require(p.retries == p.attempts - 1 && p.lastChoice == q.correct)
                    AnswerState.INCORRECT -> require(state.plan.policy.wrongAnswer == WrongAnswerPolicy.LOCK &&
                        p.attempts == 1 && p.retries == 0 && p.lastChoice != q.correct)
                }
                if (state.plan.policy.wrongAnswer == WrongAnswerPolicy.LOCK) require(p.attempts == 1)
            }
        }
    }

    private fun DataOutputStream.text(text: ContentText) { text.strings().forEach { writeUTF(it) } }
    private fun DataInputStream.string(): String = readUTF().also { require(it.length <= 1000) }
    private fun DataInputStream.text() = ContentText(LocalizedText(string(), string()), LocalizedText(string(), string()))
    private fun DataInputStream.boolean(): Boolean = readUnsignedByte().also { require(it in 0..1) } == 1
}
