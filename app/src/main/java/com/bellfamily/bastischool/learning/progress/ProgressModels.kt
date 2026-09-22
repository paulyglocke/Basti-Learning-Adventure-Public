package com.bellfamily.bastischool.learning.progress

import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.session.*
import java.util.Collections

/** Derived solely from existing session/attempt identity. Delivery retries never change this key. */
class ProgressEventId private constructor(val value: String) {
    override fun equals(other: Any?) = other is ProgressEventId && value == other.value
    override fun hashCode() = value.hashCode()
    override fun toString() = value
    companion object {
        fun attempt(id: AttemptId) = ProgressEventId("attempt/${id.task.session.value}/${id.task.ordinal}/${id.number}")
        fun completion(id: SessionId) = ProgressEventId("completion/${id.value}")
    }
}

data class ProgressOrigin(val session: SessionId, val activity: ActivityId,
                          val activityRevision: Int, val contentVersion: ContentVersion) {
    init { require(activityRevision > 0) }
}
data class TaskEvidence(val task: TaskInstanceId, val definition: TaskDefinitionId,
                        val skill: SkillId, val context: LearningContextId, val difficulty: Int) {
    init { require(difficulty in 1..100) }
}
enum class AttemptOutcome { CORRECT, INCORRECT }
enum class ProgressEventKind { ATTEMPT, SESSION_COMPLETED }

sealed interface ProgressEvent {
    val id: ProgressEventId
    val origin: ProgressOrigin
    val kind: ProgressEventKind
    fun involves(skill: SkillId): Boolean
}

data class AttemptEvent(
    override val origin: ProgressOrigin,
    val evidence: TaskEvidence,
    val attempt: AttemptId,
    val language: ContentLanguage,
    val choice: ContentId,
    val outcome: AttemptOutcome,
    val support: SupportUse
) : ProgressEvent {
    init {
        require(attempt.task == evidence.task && evidence.task.session == origin.session)
        require(choice.value.length <= 96)
    }
    override val id get() = ProgressEventId.attempt(attempt)
    override val kind get() = ProgressEventKind.ATTEMPT
    val retriesBeforeAttempt get() = attempt.number - 1
    override fun involves(skill: SkillId) = evidence.skill == skill
}

data class CompletedTask(val evidence: TaskEvidence, val lastChoice: ContentId,
                         val outcome: AttemptOutcome, val attempts: Int, val retries: Int,
                         val support: SupportUse) {
    init {
        require(attempts > 0 && retries == attempts - 1)
        require(lastChoice.value.length <= 96)
    }
}

/** Final task evidence is not a fabricated attempt history or a mastery score. */
class CompletionEvent(override val origin: ProgressOrigin, tasks: List<CompletedTask>) : ProgressEvent {
    val tasks: List<CompletedTask> = Collections.unmodifiableList(tasks.toList())
    init {
        require(this.tasks.size in setOf(5, 7, 10))
        require(this.tasks.map { it.evidence.task } == (1..this.tasks.size).map { TaskInstanceId(origin.session, it) })
    }
    override val id get() = ProgressEventId.completion(origin.session)
    override val kind get() = ProgressEventKind.SESSION_COMPLETED
    override fun involves(skill: SkillId) = tasks.any { it.evidence.skill == skill }
    override fun equals(other: Any?) = other is CompletionEvent && origin == other.origin && tasks == other.tasks
    override fun hashCode() = 31 * origin.hashCode() + tasks.hashCode()
}

/** First durable acceptance time, not reaction time; sequence orders even if the clock moves backwards. */
data class StoredProgressEvent(val sequence: Long, val recordedAtMillis: Long, val event: ProgressEvent) {
    init { require(sequence > 0 && recordedAtMillis >= 0) }
}
fun interface ProgressClock { fun nowMillis(): Long }

enum class ProgressFailure { IO, CORRUPT, INCOMPATIBLE_VERSION, CAPACITY, CONFLICT, INVALID_EVENT, CLOCK }
sealed interface ProgressWriteResult {
    data class Saved(val record: StoredProgressEvent, val inserted: Boolean) : ProgressWriteResult
    data class Failed(val reason: ProgressFailure) : ProgressWriteResult
}
sealed interface ProgressReadResult {
    class Events(records: List<StoredProgressEvent>) : ProgressReadResult {
        val records: List<StoredProgressEvent> = Collections.unmodifiableList(records.toList())
    }
    data class Failed(val reason: ProgressFailure) : ProgressReadResult
}

data class ProgressQuery(val skill: SkillId? = null, val session: SessionId? = null,
                         val activity: ActivityId? = null, val kind: ProgressEventKind? = null,
                         val newestFirst: Boolean = false, val limit: Int = 10_000) {
    init { require(limit in 1..10_000) }
}

/** Blocking worker-thread boundary; append success is returned only after durable commit. */
interface ProgressRepository {
    fun append(event: ProgressEvent): ProgressWriteResult
    fun read(query: ProgressQuery = ProgressQuery()): ProgressReadResult
}
