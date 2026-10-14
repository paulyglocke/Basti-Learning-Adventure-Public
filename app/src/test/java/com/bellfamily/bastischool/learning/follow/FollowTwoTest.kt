package com.bellfamily.bastischool.learning.follow

import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.progress.*
import com.bellfamily.bastischool.learning.session.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class FollowTwoTest {
    @get:Rule val temporary = TemporaryFolder()

    @Test fun curatedAnimalsHaveUniqueArtworkAndNaturalGermanObjectForms() {
        assertEquals(6, FollowContent.objects.size)
        assertEquals(6, FollowContent.objects.map { it.assetPath }.toSet().size)
        FollowContent.objects.forEach { objectDef ->
            assertTrue(objectDef.assetPath.startsWith("Animals/canonical/"))
            assertTrue(objectDef.assetPath.endsWith(".png"))
            assertTrue(objectDef.text.display.en.isNotBlank())
            assertTrue(objectDef.text.display.de.isNotBlank())
            assertTrue(FollowContent.instruction(objectDef).speech.de.contains(objectDef.germanObject))
        }
        val dino = FollowContent.objects.first { it.id.value.endsWith("dinosaur") }
        val horse = FollowContent.objects.first { it.id.value.endsWith("horse") }
        val task = FollowTwoTask(dino.id, horse.id, listOf(dino.id, horse.id) +
            FollowContent.objects.filter { it != dino && it != horse }.take(2).map { it.id })
        assertEquals("First touch the dinosaur, then touch the horse.", FollowTwo.instruction(task).speech.en)
        assertEquals("Tippe zuerst auf den Dinosaurier und dann auf das Pferd.", FollowTwo.instruction(task).speech.de)
        val fish = FollowContent.objects.first { it.id.value.endsWith("fish") }
        val choiceTask = FollowTwoTask(fish.id, horse.id, task.choices.filter { it != dino.id } + fish.id)
        assertEquals("First choose the fish, then choose the horse.", FollowTwo.instruction(choiceTask).speech.en)
        assertEquals("Wähle zuerst den Fisch und dann das Pferd aus.", FollowTwo.instruction(choiceTask).speech.de)
    }

    @Test fun fiveAndTenRoundsAreDeterministicBalancedAndNeverRepeatAnOrderedTask() {
        val reached = mutableSetOf<ContentId>()
        for (seed in -100L..100L) for (round in RoundLength.entries) {
            val a = FollowTwo.generate(SessionId("deterministic"), round, seed, ContentLanguage.ENGLISH)
            val b = FollowTwo.generate(SessionId("deterministic"), round, seed, ContentLanguage.GERMAN)
            assertEquals(a.tasks, b.tasks)
            assertEquals(round.count, a.tasks.size)
            assertEquals(round.count, a.tasks.map { it.definition }.toSet().size)
            assertTrue(a.tasks.zipWithNext().all { it.first.first != it.second.first })
            assertTrue(a.tasks.groupingBy { it.first }.eachCount().values.max() <= 2)
            a.tasks.forEach { task ->
                reached += task.first; reached += task.second
                assertNotEquals(task.first, task.second)
                assertEquals(4, task.choices.toSet().size)
                assertTrue(task.first in task.choices && task.second in task.choices)
                assertFalse(task.first.value.endsWith("dinosaur") && task.second.value.endsWith("horse"))
            }
        }
        assertEquals(FollowContent.objects.map { it.id }.toSet(), reached)
    }

    @Test fun secondTargetBeforeFirstIsWrongAndFirstSuccessDoesNotFinishQuestion() {
        var state = FollowTwo.generate(SessionId("order"), RoundLength.FIVE, 7, ContentLanguage.ENGLISH)
        val taskId = state.taskId
        val second = state.task.second
        val wrong = FollowTwo.reduce(state, FollowTwoAction.Choose(state.nextAttempt!!, second))
        state = wrong.state
        assertEquals(AttemptOutcome.INCORRECT, (wrong.events.single() as AttemptEvent).outcome)
        assertEquals(AnswerState.RETRY_AVAILABLE, state.current.answer)
        assertEquals(0, state.step)
        assertFalse(state.readyToContinue)
        state = FollowTwo.reduce(state, FollowTwoAction.Retry(AttemptId(taskId, 1))).state
        val firstResult = FollowTwo.reduce(state, FollowTwoAction.Choose(state.nextAttempt!!, state.task.first))
        state = firstResult.state
        assertTrue(firstResult.events.isEmpty())
        assertEquals(1, state.step)
        assertEquals(taskId, state.taskId)
        assertFalse(state.readyToContinue)
        val earlyNext = FollowTwo.reduce(state, FollowTwoAction.Next(taskId))
        assertSame(state, earlyNext.state)
        val correct = FollowTwo.reduce(state, FollowTwoAction.Choose(state.nextAttempt!!, second))
        state = correct.state
        assertTrue(state.readyToContinue)
        assertEquals(AttemptOutcome.CORRECT, (correct.events.single() as AttemptEvent).outcome)
        assertEquals(2, (correct.events.single() as AttemptEvent).attempt.number)
        assertEquals(1, FollowTwo.reduce(state, FollowTwoAction.Next(taskId)).state.questionIndex)
    }

    @Test fun retryHintReplayAndLanguageKeepBothTargetsAndSupportEvidence() {
        var state = FollowTwo.generate(SessionId("support"), RoundLength.FIVE, 23, ContentLanguage.ENGLISH)
        val task = state.task
        val replay = FollowTwo.reduce(state, FollowTwoAction.Replay(state.taskId))
        state = replay.state
        assertEquals(FollowTwo.instruction(task), replay.speech?.text)
        assertEquals(1, state.current.support.replays)
        val hint = FollowTwo.reduce(state, FollowTwoAction.Hint(state.taskId))
        state = hint.state
        assertTrue(state.current.support.hint)
        assertFalse(hint.speech!!.text.display.en.contains(FollowContent.objectFor(task.first).text.display.en))
        state = FollowTwo.reduce(state, FollowTwoAction.Choose(state.nextAttempt!!, task.first)).state
        val secondReplay = FollowTwo.reduce(state, FollowTwoAction.Replay(state.taskId))
        state = secondReplay.state
        assertEquals(FollowTwo.instruction(task), secondReplay.speech?.text)
        assertEquals(1, state.steps[0][1].support.replays)
        state = FollowTwo.reduce(state, FollowTwoAction.Language(ContentLanguage.GERMAN)).state
        assertEquals(task, state.task)
        assertEquals(ContentLanguage.GERMAN, state.language)
        val final = FollowTwo.reduce(state, FollowTwoAction.Choose(state.nextAttempt!!, task.second))
        val success = final.events.single() as AttemptEvent
        assertEquals(ContentLanguage.GERMAN, success.language)
        assertEquals(2, success.support.replays)
        assertTrue(success.support.hint)
    }

    @Test fun wrongSecondStepRetriesWithoutErasingFirstStep() {
        var state = FollowTwo.generate(SessionId("second-retry"), RoundLength.FIVE, 19, ContentLanguage.ENGLISH)
        val first = state.task.first
        val second = state.task.second
        state = FollowTwo.reduce(state, FollowTwoAction.Choose(state.nextAttempt!!, first)).state
        val wrong = state.task.choices.first { it != second }
        val failed = FollowTwo.reduce(state, FollowTwoAction.Choose(state.nextAttempt!!, wrong))
        state = failed.state
        assertEquals(AttemptOutcome.INCORRECT, (failed.events.single() as AttemptEvent).outcome)
        assertEquals(AnswerState.CORRECT, state.steps[0][0].answer)
        assertEquals(AnswerState.RETRY_AVAILABLE, state.steps[0][1].answer)
        state = FollowTwo.reduce(state, FollowTwoAction.Retry(AttemptId(state.taskId, 1))).state
        assertEquals(1, state.step)
        assertEquals(AnswerState.CORRECT, state.steps[0][0].answer)
        val corrected = FollowTwo.reduce(state, FollowTwoAction.Choose(state.nextAttempt!!, second))
        assertTrue(corrected.state.readyToContinue)
        assertEquals(2, (corrected.events.single() as AttemptEvent).attempt.number)
    }

    @Test fun restoredPartialSecondStepAndCompletedRoundKeepSeparateProgress() {
        val twoDisk = AtomicProgressStorage(temporary.newFolder(), JvmAtomicCommit)
        val oneDisk = AtomicProgressStorage(temporary.newFolder(), JvmAtomicCommit)
        val progress = ProgressFixtures.repository(temporary.newFolder())
        val oneHost = FollowContent.host(oneDisk, progress)
        oneHost.open(SessionId("one"), RoundLength.FIVE, 2, ContentLanguage.ENGLISH)
        val originalOne = SessionCheckpoint.encode(oneHost.state!!)
        var host = FollowTwoHost(twoDisk, progress)
        host.open(SessionId("two"), RoundLength.FIVE, 9, ContentLanguage.ENGLISH)
        var state = host.state!!
        host.dispatch(FollowTwoAction.Replay(state.taskId))
        host.dispatch(FollowTwoAction.Choose(host.state!!.nextAttempt!!, state.task.first))
        state = host.state!!
        assertEquals(1, state.step)
        val saved = FollowTwoHost.encode(state, emptyList())
        host = FollowTwoHost(twoDisk, progress)
        assertNull(host.open(SessionId("ignored"), RoundLength.TEN, 999, ContentLanguage.GERMAN))
        assertArrayEquals(saved, FollowTwoHost.encode(host.state!!, emptyList()))
        assertEquals(state.task, host.state!!.task)
        assertEquals(state.steps, host.state!!.steps)
        while (!host.state!!.completed) {
            val current = host.state!!
            if (current.readyToContinue) host.dispatch(FollowTwoAction.Next(current.taskId))
            else host.dispatch(FollowTwoAction.Choose(current.nextAttempt!!,
                if (current.step == 0) current.task.first else current.task.second))
        }
        assertTrue(host.state!!.acknowledged)
        val records = ProgressFixtures.records(progress).map { it.event }
        assertEquals(5, records.filterIsInstance<AttemptEvent>().count { it.origin.activity == FollowTwo.activity })
        val completed = records.filterIsInstance<CompletionEvent>().single()
        assertEquals(5, completed.tasks.size)
        assertTrue(completed.tasks.all { it.evidence.skill == FollowTwo.skill && it.evidence.difficulty == 2 })
        assertArrayEquals(originalOne, SessionCheckpoint.encode(FollowContent.host(oneDisk, progress).also {
            it.open(SessionId("ignored-one"), RoundLength.TEN, 99, ContentLanguage.GERMAN)
        }.state!!))
        host.again(SessionId("two-again"), RoundLength.TEN, 12, ContentLanguage.GERMAN)
        assertEquals(10, host.state!!.tasks.size)
    }

    @Test fun staleAttemptsAndOtherSessionActionsCannotAdvance() {
        val state = FollowTwo.generate(SessionId("stale"), RoundLength.FIVE, 3, ContentLanguage.ENGLISH)
        val other = FollowTwoAction.Choose(AttemptId(TaskInstanceId(SessionId("other"), 1), 1), state.task.first)
        assertSame(state, FollowTwo.reduce(state, other).state)
        assertSame(state, FollowTwo.reduce(state, FollowTwoAction.Next(state.taskId)).state)
        assertSame(state, FollowTwo.reduce(state, FollowTwoAction.Hint(TaskInstanceId(SessionId("other"), 1))).state)
    }

    @Test fun failedAttemptDeliveryRestoresPendingEventWithoutDuplicatingProgress() {
        val disk = AtomicProgressStorage(temporary.newFolder(), JvmAtomicCommit)
        val repository = ProgressFixtures.repository(temporary.newFolder())
        var fail = true
        val failing = object : ProgressRepository {
            override fun append(event: ProgressEvent) = if (fail) ProgressWriteResult.Failed(ProgressFailure.IO)
                else repository.append(event)
            override fun read(query: ProgressQuery) = repository.read(query)
        }
        val host = FollowTwoHost(disk, failing)
        host.open(SessionId("delivery"), RoundLength.FIVE, 8, ContentLanguage.ENGLISH)
        val initial = host.state!!
        val wrong = initial.task.choices.first { it != initial.task.first }
        host.dispatch(FollowTwoAction.Choose(initial.nextAttempt!!, wrong))
        assertTrue(host.failure && host.hasPending)
        assertNull(host.dispatch(FollowTwoAction.Replay(initial.taskId)))
        val restored = FollowTwoHost(disk, failing)
        restored.open(SessionId("unused"), RoundLength.TEN, 100, ContentLanguage.GERMAN)
        assertTrue(restored.hasPending)
        assertEquals(AnswerState.RETRY_AVAILABLE, restored.state!!.current.answer)
        fail = false
        restored.retryWrites()
        assertFalse(restored.failure || restored.hasPending)
        restored.retryWrites()
        val attempts = ProgressFixtures.records(repository).map { it.event }.filterIsInstance<AttemptEvent>()
        assertEquals(1, attempts.size)
        assertEquals(ProgressEventId.attempt(initial.nextAttempt!!), attempts.single().id)
    }
}
