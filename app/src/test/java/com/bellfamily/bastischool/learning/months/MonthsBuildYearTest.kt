package com.bellfamily.bastischool.learning.months

import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.progress.*
import com.bellfamily.bastischool.learning.session.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class MonthsBuildYearTest {
    @get:Rule val temporary = TemporaryFolder()
    private fun start(seed: Long = 42) = MonthsBuildYear.start(SessionId("build-$seed"), seed, ContentLanguage.ENGLISH)

    @Test fun everyThreeMonthWindowIsConsecutiveAndIncludesYearWrap() {
        MonthIds.canonicalOrder.forEachIndexed { index, month ->
            val expected = listOf(month, MonthIds.next(month), MonthIds.next(MonthIds.next(month)))
            assertEquals(expected, MonthsBuildYear.sequence(month))
            assertEquals(3, expected.toSet().size)
            assertEquals(MonthIds.canonicalOrder[(index + 1) % 12], expected[1])
        }
        assertEquals(listOf(MonthIds.DECEMBER, MonthIds.JANUARY, MonthIds.FEBRUARY), MonthsBuildYear.sequence(MonthIds.DECEMBER))
    }

    @Test fun sessionGenerationIsDeterministicUniqueAndBilingual() {
        val starts = mutableSetOf<ContentId>()
        for (seed in 0L..100L) {
            val english = MonthsBuildYear.start(SessionId("same"), seed, ContentLanguage.ENGLISH)
            val german = MonthsBuildYear.start(SessionId("same"), seed, ContentLanguage.GERMAN)
            starts += english.start
            assertEquals(english.start, german.start)
            assertEquals(english.order, german.order)
            assertEquals(english.choices, german.choices)
            assertEquals(3, english.choices.size)
            assertEquals(english.order.toSet(), english.choices.toSet())
            assertNotEquals(english.order, english.choices)
        }
        assertEquals(MonthIds.canonicalOrder.toSet(), starts)
        assertNotEquals(MonthsBuildYear.introduction.display[ContentLanguage.ENGLISH],
            MonthsBuildYear.introduction.display[ContentLanguage.GERMAN])
    }

    @Test fun wrongPlacementRetryReplayHintAndProgressRemainDistinct() {
        var state = MonthsBuildYear.start(SessionId("december"), MonthIds.canonicalOrder.indexOf(MonthIds.DECEMBER).toLong(), ContentLanguage.ENGLISH)
        // A fixed seed does not define the start month; explicitly restore a December fixture.
        state = MonthsBuildYearState(state.id, MonthIds.DECEMBER, state.language,
            com.bellfamily.bastischool.learning.sequencing.OrderedPlacement.scramble(MonthsBuildYear.sequence(MonthIds.DECEMBER), 9),
            List(3) { com.bellfamily.bastischool.learning.session.TaskProgress() })
        val wrong = state.choices.first { it != MonthIds.DECEMBER }
        val first = MonthsBuildYear.reduce(state, MonthsBuildYearAction.Place(state.nextAttempt!!, wrong))
        state = first.state
        assertEquals(AnswerState.RETRY_AVAILABLE, state.current.answer)
        assertEquals(AttemptOutcome.INCORRECT, (first.events.single() as AttemptEvent).outcome)
        assertTrue(state.placed.isEmpty())
        state = MonthsBuildYear.reduce(state, MonthsBuildYearAction.Retry(AttemptId(state.task, 1))).state
        val task = state.task
        state = MonthsBuildYear.reduce(state, MonthsBuildYearAction.Replay(task)).state
        state = MonthsBuildYear.reduce(state, MonthsBuildYearAction.Help(task)).state
        assertEquals(1, state.current.attempts)
        assertEquals(1, state.current.support.replays)
        assertTrue(state.current.support.hint)
        val correct = MonthsBuildYear.reduce(state, MonthsBuildYearAction.Place(state.nextAttempt!!, MonthIds.DECEMBER))
        state = correct.state
        assertEquals(AttemptOutcome.CORRECT, (correct.events.single() as AttemptEvent).outcome)
        assertEquals(listOf(MonthIds.DECEMBER), state.placed)
        val restored = MonthsBuildYearHost.decode(MonthsBuildYearHost.encode(state, emptyList())).first
        assertEquals(state.choices, restored.choices)
        assertEquals(state.steps, restored.steps)
        assertEquals(state.order, restored.order)
    }

    @Test fun completionContainsThreeOrderedCanonicalProgressRecords() {
        var state = MonthsBuildYearState(SessionId("complete"), MonthIds.NOVEMBER, ContentLanguage.GERMAN,
            MonthsBuildYear.sequence(MonthIds.NOVEMBER), List(3) { com.bellfamily.bastischool.learning.session.TaskProgress() })
        val emitted = mutableListOf<ProgressEvent>()
        state.order.forEach { month ->
            val transition = MonthsBuildYear.reduce(state, MonthsBuildYearAction.Place(state.nextAttempt!!, month))
            emitted += transition.events
            state = transition.state
        }
        assertTrue(state.completed)
        assertEquals(3, emitted.filterIsInstance<AttemptEvent>().size)
        val completion = emitted.filterIsInstance<CompletionEvent>().single()
        assertEquals(3, completion.tasks.size)
        assertEquals("activity.months.build_year.three", completion.origin.activity.value)
        assertTrue(completion.tasks.all { it.evidence.skill == MonthsBuildYear.skill && it.evidence.context == MonthsBuildYear.context })
        assertEquals(state.order, completion.tasks.map { it.lastChoice })
        val records = listOf(StoredProgressEvent(1, 123, completion))
        assertEquals(records, ProgressCodec.decode(ProgressCodec.encode(records)))
    }

    @Test fun checkpointRejectsTamperingAndPreservesPendingCompletion() {
        var state = MonthsBuildYearState(SessionId("pending"), MonthIds.DECEMBER, ContentLanguage.ENGLISH,
            MonthsBuildYear.sequence(MonthIds.DECEMBER), List(3) { com.bellfamily.bastischool.learning.session.TaskProgress() })
        repeat(2) { index -> state = MonthsBuildYear.reduce(state,
            MonthsBuildYearAction.Place(state.nextAttempt!!, state.order[index])).state }
        val final = MonthsBuildYear.reduce(state, MonthsBuildYearAction.Place(state.nextAttempt!!, state.order[2]))
        val bytes = MonthsBuildYearHost.encode(final.state, final.events)
        val restored = MonthsBuildYearHost.decode(bytes)
        assertEquals(final.state.order, restored.first.order)
        assertEquals(final.events, restored.second)
        assertThrows(IllegalArgumentException::class.java) {
            MonthsBuildYearHost.decode(bytes.copyOf().also { it[8] = (it[8].toInt() xor 1).toByte() })
        }
    }

    @Test fun durableHostRestoresExactOrderAndRecordsAttemptsAndCompletion() {
        val disk = AtomicProgressStorage(temporary.newFolder(), JvmAtomicCommit)
        val repository = ProgressFixtures.repository(temporary.newFolder())
        fun open() = MonthsBuildYearHost(disk, repository).also { it.open(SessionId("ignored"), 999, ContentLanguage.GERMAN) }
        var host = MonthsBuildYearHost(disk, repository)
        host.open(SessionId("host"), 73, ContentLanguage.ENGLISH)
        val start = host.state!!
        host.dispatch(MonthsBuildYearAction.Help(start.task))
        host.dispatch(MonthsBuildYearAction.Replay(start.task))
        val wrong = start.choices.first { it != start.order.first() }
        host.dispatch(MonthsBuildYearAction.Place(host.state!!.nextAttempt!!, wrong))
        val saved = host.state!!
        host = open()
        assertEquals(saved.id, host.state!!.id)
        assertEquals(saved.choices, host.state!!.choices)
        assertEquals(saved.steps, host.state!!.steps)
        assertEquals(ContentLanguage.ENGLISH, host.state!!.language)
        host.dispatch(MonthsBuildYearAction.Retry(AttemptId(host.state!!.task, 1)))
        host.state!!.order.forEach { month -> host.dispatch(MonthsBuildYearAction.Place(host.state!!.nextAttempt!!, month)) }
        assertTrue(host.state!!.completed && host.state!!.acknowledged)
        val events = ProgressFixtures.records(repository).map { it.event }
        assertEquals(4, events.filterIsInstance<AttemptEvent>().size)
        val completion = events.filterIsInstance<CompletionEvent>().single()
        assertEquals(1, completion.tasks.first().retries)
        assertEquals(1, completion.tasks.first().support.replays)
        assertTrue(completion.tasks.first().support.hint)
        val again = MonthsBuildYearHost(disk, repository)
        assertNull(again.open(SessionId("new"), 17, ContentLanguage.GERMAN))
        assertTrue(again.state!!.completed && again.state!!.acknowledged)
    }

    @Test fun failedProgressDeliveryStaysPendingAndRetriesWithSameIdentity() {
        val disk = AtomicProgressStorage(temporary.newFolder(), JvmAtomicCommit)
        val repository = ProgressFixtures.repository(temporary.newFolder())
        var fail = true
        val failing = object : ProgressRepository {
            override fun append(event: ProgressEvent) = if (fail) ProgressWriteResult.Failed(ProgressFailure.IO) else repository.append(event)
            override fun read(query: ProgressQuery) = repository.read(query)
        }
        val host = MonthsBuildYearHost(disk, failing)
        host.open(SessionId("delivery"), 42, ContentLanguage.ENGLISH)
        val state = host.state!!
        host.dispatch(MonthsBuildYearAction.Place(state.nextAttempt!!, state.order.first()))
        assertTrue(host.failure && host.hasPending)
        val event = host.dispatch(MonthsBuildYearAction.Replay(state.task))
        assertNull(event)
        val restored = MonthsBuildYearHost(disk, failing)
        restored.open(SessionId("unused"), 8, ContentLanguage.GERMAN)
        assertTrue(restored.hasPending)
        fail = false
        restored.retryWrites()
        assertFalse(restored.failure || restored.hasPending)
        val attempts = ProgressFixtures.records(repository).map { it.event }.filterIsInstance<AttemptEvent>()
        assertEquals(1, attempts.size)
        assertEquals(ProgressEventId.attempt(state.nextAttempt!!), attempts.single().id)
    }
}
