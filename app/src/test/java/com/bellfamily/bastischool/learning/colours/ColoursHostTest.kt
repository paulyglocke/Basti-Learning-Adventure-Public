package com.bellfamily.bastischool.learning.colours

import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.progress.*
import com.bellfamily.bastischool.learning.session.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class ColoursHostTest {
    @get:Rule val temp = TemporaryFolder()
    private val events = mutableListOf<ProgressEvent>()
    private val progress = object : ProgressRepository {
        override fun append(event: ProgressEvent): ProgressWriteResult {
            val old = events.indexOfFirst { it.id == event.id }
            if (old >= 0 && events[old] != event) return ProgressWriteResult.Failed(ProgressFailure.CONFLICT)
            if (old < 0) events += event
            return ProgressWriteResult.Saved(StoredProgressEvent(events.indexOf(event) + 1L, 0, event), old < 0)
        }
        override fun read(query: ProgressQuery) = ProgressReadResult.Events(emptyList())
    }
    private fun storage() = AtomicProgressStorage(temp.newFolder(), JvmAtomicCommit)
    private fun open(disk: ProgressStorage, round: RoundLength = RoundLength.FIVE) = ColoursHost(disk, progress).also {
        it.open(SessionId("colour-find"), round, 829L, ContentLanguage.ENGLISH)
    }

    @Test fun incorrectAttemptHintAndRestorationPreserveExactTargetAndChoices() {
        val disk = storage()
        val host = open(disk)
        val before = SessionCheckpoint.encode(host.state!!)
        host.dispatch(SessionAction.Hint(host.state!!.task.id))
        val state = host.state!!
        val wrong = state.task.question.choices.first { it != state.task.question.correct }
        host.dispatch(SessionAction.Answer(state.nextAttempt!!, wrong))
        val saved = SessionCheckpoint.encode(host.state!!)
        val restored = open(disk)
        assertArrayEquals(saved, SessionCheckpoint.encode(restored.state!!))
        assertNotEquals(before.toList(), saved.toList())
        assertEquals(AnswerState.RETRY_AVAILABLE, restored.state!!.current.answer)
        assertTrue(restored.state!!.current.support.hint)
        assertEquals(1, events.filterIsInstance<AttemptEvent>().size)
        assertEquals(AttemptOutcome.INCORRECT, events.filterIsInstance<AttemptEvent>().single().outcome)
        assertEquals(ColoursContent.activity, events.single().origin.activity)
    }

    @Test fun correctAnswerAndCompletionUseDedicatedFindIdentityAndSurviveRestart() {
        val disk = storage()
        var host = open(disk, RoundLength.FIVE)
        repeat(5) { index ->
            val state = host.state!!
            host.dispatch(SessionAction.Answer(state.nextAttempt!!, state.task.question.correct))
            host.dispatch(SessionAction.Next(host.state!!.task.id))
            host = open(disk, RoundLength.TEN)
            if (index < 4) assertEquals(index + 1, host.state!!.index)
        }
        assertEquals(SessionPhase.COMPLETED, host.state!!.phase)
        assertEquals(CompletionState.ACKNOWLEDGED, host.state!!.completion)
        assertEquals(1, events.filterIsInstance<CompletionEvent>().size)
        assertTrue(events.all { it.origin.activity == ColoursContent.activity })
        assertTrue(events.all { it.origin.activity != com.bellfamily.bastischool.learning.coloursort.ColourSort.activity })
    }
}
