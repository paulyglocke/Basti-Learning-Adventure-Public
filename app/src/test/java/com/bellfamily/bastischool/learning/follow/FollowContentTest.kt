package com.bellfamily.bastischool.learning.follow

import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.progress.*
import com.bellfamily.bastischool.learning.session.*
import org.junit.Assert.*
import org.junit.Test

class FollowContentTest {
    @Test fun exactObjectsInstructionsAndAssetsAreBilingual() {
        assertEquals(6, FollowContent.objects.size)
        assertEquals(6, FollowContent.objects.map { it.id }.toSet().size)
        assertTrue(setOf("object.follow.crocodile", "object.follow.dinosaur", "object.follow.snake", "object.follow.fish")
            .all { it in FollowContent.objects.map { objectDef -> objectDef.id.value } })
        assertEquals("the horse", FollowContent.objectFor(ContentId("object.follow.horse")).text.display.en)
        assertEquals("das Pferd", FollowContent.objectFor(ContentId("object.follow.horse")).text.display.de)
        assertEquals("the whale", FollowContent.objectFor(ContentId("object.follow.whale")).text.display.en)
        assertEquals("der Wal", FollowContent.objectFor(ContentId("object.follow.whale")).text.display.de)
        FollowContent.objects.forEach { assertTrue(it.assetPath.startsWith("Animals/canonical/")); assertTrue(FollowContent.instruction(it).display.en.startsWith("Touch ")); assertTrue(FollowContent.instruction(it).display.de.startsWith("Tippe auf ")) }
        assertEquals("skill.listening.one_step", FollowContent.skill.value)
    }
    @Test fun generationIsDeterministicAndTargetsPresentedObjects() {
        for (round in RoundLength.entries) {
            val ar = FollowContent.generate(SessionId("a"), round, 42); assertTrue(ar.toString(), ar is GenerationResult.Generated)
            val br = FollowContent.generate(SessionId("a"), round, 42); assertTrue(br.toString(), br is GenerationResult.Generated)
            val a = (ar as GenerationResult.Generated).plan
            val b = (br as GenerationResult.Generated).plan
            assertArrayEquals(SessionCheckpoint.encode(SessionReducer.start(a, ContentLanguage.ENGLISH, FollowContent.repository).state),
                SessionCheckpoint.encode(SessionReducer.start(b, ContentLanguage.ENGLISH, FollowContent.repository).state))
            assertEquals(round.count, a.tasks.size)
            a.tasks.forEach { assertEquals(4, it.question.choices.size); assertEquals(4, it.question.choices.toSet().size); assertTrue(it.question.correct in it.question.choices) }
            assertTrue(a.tasks.zipWithNext().all { it.first.question.correct != it.second.question.correct })
        }
    }
    @Test fun allApprovedAnimalsReachTargetsAndVaryDistractorContexts() {
        val targetSeeds = (0L..100L).map { FollowContent.generate(SessionId("sample"), RoundLength.TEN, it) as GenerationResult.Generated }
        val targetIds = targetSeeds.flatMap { it.plan.tasks.map { task -> task.question.correct } }.toSet()
        assertEquals(FollowContent.objects.map { it.id }.toSet(), targetIds)
        val horseContexts = targetSeeds.flatMap { it.plan.tasks }.filter { it.question.correct.value.endsWith("horse") }
            .map { it.question.choices.toSet() }.toSet()
        assertTrue(horseContexts.size > 1)
        assertEquals(3, FollowContent.REVISION)
        assertEquals(ContentVersion(1, 3), FollowContent.repository.version)
    }
    @Test fun noConsecutiveTargetsAcrossDeterministicSeedSample() {
        val reached = mutableSetOf<ContentId>()
        for (seed in -500L..500L) for (round in RoundLength.entries) {
            val plan = (FollowContent.generate(SessionId("sample"), round, seed) as GenerationResult.Generated).plan
            assertTrue("seed=$seed round=$round", plan.tasks.zipWithNext().all { it.first.question.correct != it.second.question.correct })
            assertEquals(plan.tasks.size, plan.tasks.map { it.question.definition }.toSet().size)
            plan.tasks.forEach { reached += it.question.correct }
        }
        assertEquals(FollowContent.objects.map { it.id }.toSet(), reached)
    }

    @Test fun reducerSupportsWrongRetryCorrectReplayAndLanguageWithoutChangingTask() {
        val plan = (FollowContent.generate(SessionId("flow"), RoundLength.FIVE, 1) as GenerationResult.Generated).plan
        var state = SessionReducer.start(plan, ContentLanguage.ENGLISH, FollowContent.repository).state
        val task = state.task; val wrong = task.question.choices.first { it != task.question.correct }
        val replay = SessionReducer.reduce(state, SessionAction.Replay(task.id)).state
        assertEquals(state.task.id, replay.task.id); assertEquals(state.current.attempts, replay.current.attempts); assertEquals(1, replay.current.support.replays)
        state = SessionReducer.reduce(state, SessionAction.Answer(state.nextAttempt!!, wrong)).state
        assertEquals(AnswerState.RETRY_AVAILABLE, state.current.answer)
        state = SessionReducer.reduce(state, SessionAction.Retry(AttemptId(task.id, 1))).state
        assertEquals(task.id, state.task.id)
        state = SessionReducer.reduce(state, SessionAction.Answer(state.nextAttempt!!, task.question.correct)).state
        assertEquals(AnswerState.CORRECT, state.current.answer)
        val locked = SessionReducer.reduce(state, SessionAction.Answer(AttemptId(task.id, 3), task.question.correct)).state
        assertEquals(state.score, locked.score)
        state = SessionReducer.reduce(state, SessionAction.Language(ContentLanguage.GERMAN)).state
        assertEquals(task.id, state.task.id); assertEquals(ContentLanguage.GERMAN, state.language)
    }
}
