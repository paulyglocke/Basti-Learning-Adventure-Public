package com.bellfamily.bastischool.learning.follow

import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.progress.*
import com.bellfamily.bastischool.learning.session.*
import org.junit.Assert.*
import org.junit.Test

class FollowContentTest {
    @Test fun exactObjectsInstructionsAndAssetsAreBilingual() {
        assertEquals(4, FollowContent.objects.size)
        assertEquals(4, FollowContent.objects.map { it.id }.toSet().size)
        assertEquals(setOf("object.follow.crocodile", "object.follow.dinosaur", "object.follow.snake", "object.follow.fish"), FollowContent.objects.map { it.id.value }.toSet())
        FollowContent.objects.forEach { assertTrue(it.assetPath.startsWith("Animals/canonical/")); assertTrue(FollowContent.instruction(it).display.en.startsWith("Touch ")); assertTrue(FollowContent.instruction(it).display.de.startsWith("Tippe auf ")) }
        assertEquals("skill.listening.one_step", FollowContent.skill.value)
    }
    @Test fun generationIsDeterministicAndTargetsPresentedObjects() {
        for (round in RoundLength.entries) {
            val ar = FollowContent.generate(SessionId("a"), round, 42); assertTrue(ar.toString(), ar is GenerationResult.Generated)
            val br = FollowContent.generate(SessionId("a"), round, 42); assertTrue(br.toString(), br is GenerationResult.Generated)
            val a = (ar as GenerationResult.Generated).plan
            val b = (br as GenerationResult.Generated).plan
            assertEquals(a.tasks.map { it.question.correct }, b.tasks.map { it.question.correct })
            assertEquals(round.count, a.tasks.size)
            a.tasks.forEach { assertEquals(4, it.question.choices.size); assertTrue(it.question.correct in it.question.choices) }
            assertTrue(a.tasks.zipWithNext().all { it.first.question.correct != it.second.question.correct })
        }
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
