package com.bellfamily.bastischool.learning.months

import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.progress.*
import com.bellfamily.bastischool.learning.session.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class MonthsBuildYearFourTest {
    @get:Rule val temporary = TemporaryFolder()

    @Test fun everyStartingMonthBuildsFourConsecutiveMonthsAcrossYearBoundaries() {
        MonthIds.canonicalOrder.forEachIndexed { index, start ->
            val expected = List(4) { offset -> MonthIds.canonicalOrder[(index + offset) % 12] }
            assertEquals(expected, MonthsBuildYearFour.sequence(start))
            assertEquals(4, expected.toSet().size)
        }
        assertEquals(listOf(MonthIds.OCTOBER, MonthIds.NOVEMBER, MonthIds.DECEMBER, MonthIds.JANUARY),
            MonthsBuildYearFour.sequence(MonthIds.OCTOBER))
        assertEquals(listOf(MonthIds.NOVEMBER, MonthIds.DECEMBER, MonthIds.JANUARY, MonthIds.FEBRUARY),
            MonthsBuildYearFour.sequence(MonthIds.NOVEMBER))
        assertEquals(listOf(MonthIds.DECEMBER, MonthIds.JANUARY, MonthIds.FEBRUARY, MonthIds.MARCH),
            MonthsBuildYearFour.sequence(MonthIds.DECEMBER))
    }

    @Test fun fiveAndTenSequenceRoundsAreDeterministicUniqueAndNeverInitiallySolved() {
        val reachable = mutableSetOf<ContentId>()
        for (round in RoundLength.entries) for (seed in 0L..300L) {
            val first = MonthsBuildYearFour.generate(SessionId("same"), round, seed, ContentLanguage.ENGLISH)
            val restoredLanguage = MonthsBuildYearFour.generate(SessionId("same"), round, seed, ContentLanguage.GERMAN)
            val repeated = MonthsBuildYearFour.generate(SessionId("same"), round, seed, ContentLanguage.ENGLISH)
            assertEquals(round.count, first.starts.size)
            assertEquals(round.count, first.starts.distinct().size)
            assertEquals(first.starts, repeated.starts)
            assertEquals(first.choices, repeated.choices)
            assertEquals(first.starts, restoredLanguage.starts)
            assertEquals(first.choices, restoredLanguage.choices)
            first.starts.forEachIndexed { question, start ->
                reachable += start
                val order = MonthsBuildYearFour.sequence(start)
                assertEquals(order.toSet(), first.choices[question].toSet())
                assertNotEquals(order, first.choices[question])
            }
        }
        assertEquals(MonthIds.canonicalOrder.toSet(), reachable)
    }

    @Test fun wrongRetryHintReplayAndContinuePreserveTheFourMonthTask() {
        var state = MonthsBuildYearFour.generate(SessionId("support"), RoundLength.FIVE, 19, ContentLanguage.ENGLISH)
        val firstTask = state.task
        val wrong = state.choices.first().first { it != state.order.first() }
        val wrongResult = MonthsBuildYearFour.reduce(state, MonthsBuildYearFourAction.Place(state.nextAttempt!!, wrong))
        state = wrongResult.state
        assertEquals(AttemptOutcome.INCORRECT, (wrongResult.events.single() as AttemptEvent).outcome)
        assertEquals(AnswerState.RETRY_AVAILABLE, state.current.answer)
        assertTrue(state.placed.isEmpty())
        assertFalse(state.sequenceReady)

        state = MonthsBuildYearFour.reduce(state, MonthsBuildYearFourAction.Retry(AttemptId(state.placementTask, state.current.attempts))).state
        state = MonthsBuildYearFour.reduce(state, MonthsBuildYearFourAction.Replay(state.task)).state
        state = MonthsBuildYearFour.reduce(state, MonthsBuildYearFourAction.Help(state.task)).state
        assertEquals(firstTask, state.task)
        assertEquals(1, state.current.support.replays)
        assertTrue(state.current.support.hint)
        assertEquals("The next month is ${MonthContent.month(state.order.first()).text.speech.en}.",
            MonthsBuildYearFour.hint(state).display[ContentLanguage.ENGLISH])

        state.order.forEachIndexed { position, month ->
            state = MonthsBuildYearFour.reduce(state, MonthsBuildYearFourAction.Place(state.nextAttempt!!, month)).state
            assertEquals(state.order.take(position + 1), state.placed)
        }
        assertTrue(state.sequenceReady)
        assertNull(state.nextAttempt)
        val advanced = MonthsBuildYearFour.reduce(state, MonthsBuildYearFourAction.Continue(state.task))
        state = advanced.state
        assertEquals(1, state.questionIndex)
        assertEquals(firstTask.ordinal + 1, state.task.ordinal)
    }

    @Test fun completedFiveQuestionRoundRecordsIndependentProgressAndPreservesThreeMonthIdentity() {
        var state = MonthsBuildYearFour.generate(SessionId("four-round"), RoundLength.FIVE, 71, ContentLanguage.GERMAN)
        val events = mutableListOf<ProgressEvent>()
        while (!state.completed) {
            if (!state.sequenceReady) {
                if (state.questionIndex == 0 && state.index == 0 && state.current.attempts == 0) {
                    val wrong = state.choices[state.questionIndex].first { it != state.order.first() }
                    val incorrect = MonthsBuildYearFour.reduce(state,
                        MonthsBuildYearFourAction.Place(state.nextAttempt!!, wrong))
                    events += incorrect.events
                    state = incorrect.state
                    state = MonthsBuildYearFour.reduce(state,
                        MonthsBuildYearFourAction.Retry(AttemptId(state.placementTask, state.current.attempts))).state
                    state = MonthsBuildYearFour.reduce(state, MonthsBuildYearFourAction.Replay(state.task)).state
                    state = MonthsBuildYearFour.reduce(state, MonthsBuildYearFourAction.Help(state.task)).state
                }
                val order = state.order
                val placed = MonthsBuildYearFour.reduce(state,
                    MonthsBuildYearFourAction.Place(state.nextAttempt!!, order[state.index]))
                events += placed.events
                state = placed.state
            } else {
                val continued = MonthsBuildYearFour.reduce(state, MonthsBuildYearFourAction.Continue(state.task))
                events += continued.events
                state = continued.state
            }
        }
        assertEquals(6, events.filterIsInstance<AttemptEvent>().size)
        val completion = events.filterIsInstance<CompletionEvent>().single()
        assertEquals(5, completion.tasks.size)
        assertEquals(MonthsBuildYearFour.activity, completion.origin.activity)
        assertEquals(MonthsBuildYearFour.skill, completion.tasks.first().evidence.skill)
        assertEquals("activity.months.build_year.four", completion.origin.activity.value)
        assertEquals("skill.months.sequence.four", MonthsBuildYearFour.skill.value)
        assertTrue(completion.tasks.all { it.evidence.difficulty == 4 })
        assertEquals(1, completion.tasks.first().retries)
        assertEquals(1, completion.tasks.first().support.replays)
        assertTrue(completion.tasks.first().support.hint)

        val threeMonth = MonthsBuildYear.start(SessionId("three-regression"), 12, ContentLanguage.ENGLISH)
        assertEquals(3, threeMonth.order.size)
        assertEquals("activity.months.build_year.three", MonthsBuildYear.activity.value)
        assertEquals("skill.months.sequence", MonthsBuildYear.skill.value)
        assertNotEquals(MonthsBuildYear.activity, MonthsBuildYearFour.activity)
        assertNotEquals(MonthsBuildYear.skill, MonthsBuildYearFour.skill)
    }

    @Test fun tenQuestionRoundCompletesAndCheckpointRestoresExactRoundAndSupport() {
        val disk = AtomicProgressStorage(temporary.newFolder(), JvmAtomicCommit)
        val progress = ProgressFixtures.repository(temporary.newFolder())
        var host = MonthsBuildYearFourHost(disk, progress)
        host.open(SessionId("four-host"), RoundLength.TEN, 91, ContentLanguage.ENGLISH)
        var state = host.state!!
        val first = state.choices.first().first { it != state.order.first() }
        host.dispatch(MonthsBuildYearFourAction.Help(state.task))
        host.dispatch(MonthsBuildYearFourAction.Replay(state.task))
        host.dispatch(MonthsBuildYearFourAction.Place(host.state!!.nextAttempt!!, first))
        val saved = host.state!!
        host = MonthsBuildYearFourHost(disk, progress)
        assertNull(host.open(SessionId("ignored"), RoundLength.FIVE, 1, ContentLanguage.GERMAN))
        assertEquals(saved.round, host.state!!.round)
        assertEquals(saved.starts, host.state!!.starts)
        assertEquals(saved.choices, host.state!!.choices)
        assertEquals(saved.steps, host.state!!.steps)
        assertEquals(saved.language, host.state!!.language)
        assertEquals(saved.id, host.state!!.id)
        host.dispatch(MonthsBuildYearFourAction.Retry(AttemptId(host.state!!.placementTask, host.state!!.current.attempts)))
        while (host.state!!.questionIndex == 0) {
            val current = host.state!!
            if (current.sequenceReady) host.dispatch(MonthsBuildYearFourAction.Continue(current.task))
            else host.dispatch(MonthsBuildYearFourAction.Place(current.nextAttempt!!, current.order[current.index]))
        }
        val beforeNextQuestion = host.state!!
        val decoded = MonthsBuildYearFourHost.decode(MonthsBuildYearFourHost.encode(beforeNextQuestion, emptyList())).first
        assertEquals(beforeNextQuestion.questionIndex, decoded.questionIndex)
        assertEquals(beforeNextQuestion.starts, decoded.starts)
        assertEquals(beforeNextQuestion.choices, decoded.choices)
        assertEquals(beforeNextQuestion.steps, decoded.steps)

        while (!host.state!!.completed) {
            val current = host.state!!
            if (current.sequenceReady) host.dispatch(MonthsBuildYearFourAction.Continue(current.task))
            else host.dispatch(MonthsBuildYearFourAction.Place(current.nextAttempt!!, current.order[current.index]))
        }
        assertTrue(host.state!!.acknowledged)
        val completed = ProgressFixtures.records(progress).map { it.event }.filterIsInstance<CompletionEvent>().single()
        assertEquals(10, completed.tasks.size)
        assertEquals(11, ProgressFixtures.records(progress).map { it.event }.filterIsInstance<AttemptEvent>().size)
    }
}
