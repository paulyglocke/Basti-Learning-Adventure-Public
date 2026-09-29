package com.bellfamily.bastischool.learning.follow

import com.bellfamily.bastischool.learning.content.BundledContentRepository
import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.progress.*
import com.bellfamily.bastischool.learning.session.*
import org.junit.Assert.*
import org.junit.Test

class FollowRecoveryTest {
    private class Memory : ProgressStorage {
        var bytes: ByteArray? = null
        override fun <T> access(block: (ProgressTransaction) -> T): T = block(object : ProgressTransaction {
            override fun read() = bytes?.clone()
            override fun replace(bytes: ByteArray) { this@Memory.bytes = bytes.clone() }
        })
    }
    private val progress = object : ProgressRepository {
        override fun append(event: ProgressEvent) = ProgressWriteResult.Saved(StoredProgressEvent(1, 0, event), true)
        override fun read(query: ProgressQuery) = ProgressReadResult.Events(emptyList())
    }
    @Test fun oldRepeatedTargetsAndCurrentPlansRestoreExactlyAndAgainUsesNewRevision() {
        for (revision in 1..3) {
            val disk = Memory()
            val pool = if (revision == 1) FollowContent.objects.take(4) else FollowContent.objects
            val repo = BundledContentRepository(ContentVersion(1, revision), pool.map { it.definition }, emptyList())
            val activity = ActivityId("activity.follow.instructions")
            // Explicit old accepted repeated target fixture; never regenerate it on restore.
            val owner = if (revision == 3) FollowContent.host(disk, progress) else DurableSessionHost(disk, progress, activity, revision, repo,
                { id, round, _ ->
                    val target = pool.first()
                    val q = if (revision == 2) FollowContent.question(target, pool.take(4)) else ChoiceQuestion(
                        TaskDefinitionId("task.follow.touch.crocodile"), FollowContent.skill,
                        LearningContextId("context.follow.four_animals"), 1, FollowContent.instruction(target),
                        pool.map { it.id }, target.id, FollowContent.correctFeedback, FollowContent.wrongFeedback, FollowContent.hint)
                    GenerationResult.Generated(SessionPlan(id, activity, revision, repo.version, SessionPolicy(round),
                        (1..round.count).map { ChoiceTask(TaskInstanceId(id, it), q) }, FollowContent.completion))
                }, {})
            owner.open(SessionId("saved"), RoundLength.FIVE, 9, ContentLanguage.ENGLISH)
            val first = owner.state!!
            owner.dispatch(SessionAction.Answer(first.nextAttempt!!, first.task.question.choices.first { it != first.task.question.correct }))
            owner.dispatch(SessionAction.Hint(first.task.id))
            val before = SessionCheckpoint.encode(owner.state!!)
            val restored = FollowContent.host(disk, progress)
            assertTrue(restored.open(SessionId("ignored"), RoundLength.TEN, 999, ContentLanguage.GERMAN).isEmpty())
            assertArrayEquals(before, SessionCheckpoint.encode(restored.state!!))
            assertEquals(AnswerState.RETRY_AVAILABLE, restored.state!!.current.answer)
            restored.dispatch(SessionAction.Retry(AttemptId(first.task.id, 1)))
            repeat(5) {
                val current = restored.state!!
                restored.dispatch(SessionAction.Answer(current.nextAttempt!!, current.task.question.correct))
                restored.dispatch(SessionAction.Next(current.task.id))
            }
            restored.newRound(SessionId("again"), RoundLength.TEN, 9, ContentLanguage.ENGLISH)
            assertEquals(3, restored.state!!.plan.activityRevision)
            assertTrue(restored.state!!.plan.tasks.zipWithNext().all { it.first.question.correct != it.second.question.correct })
        }
    }
    @Test fun priorGeneratorCanRepeatTargetsDespiteDistinctDefinitions() {
        val pool = FollowContent.objects
        val candidates = pool.flatMap { target ->
            val others = pool.filter { it != target }
            others.indices.flatMap { a -> (a+1 until others.size).flatMap { b -> (b+1 until others.size).map { c ->
                FollowContent.question(target, listOf(target, others[a], others[b], others[c]))
            } } }
        }
        val old = CandidateTaskGenerator(candidates)
        val repeated = (0L..100L).any { seed ->
            val plan = (old.generate(GenerationRequest(SessionId("old"), ActivityId("activity.follow.instructions"), 2,
                SessionPolicy(RoundLength.TEN), seed, FollowContent.completion), FollowContent.repository) as GenerationResult.Generated).plan
            plan.tasks.zipWithNext().any { it.first.question.correct == it.second.question.correct }
        }
        assertTrue("Physical repeat is reproducible with the previous selection algorithm", repeated)
    }
}
