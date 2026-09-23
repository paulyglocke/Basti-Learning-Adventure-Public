package com.bellfamily.bastischool.learning.seasons

import com.bellfamily.bastischool.learning.content.SeasonIds
import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.session.*
import com.bellfamily.bastischool.learning.progress.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class SeasonsCycleTest {
    @get:Rule val temp=TemporaryFolder()
    private val modes=listOf(SeasonsPhase.NEXT,SeasonsPhase.BEFORE)
    @Test fun allCyclicRelationshipsAndWrapBoundariesAreSemantic() {
        val order=SeasonIds.canonicalOrder
        order.forEachIndexed {i,id ->
            assertEquals(order[(i+1)%4],SeasonIds.next(id));assertEquals(order[(i+3)%4],SeasonIds.previous(id))
            assertEquals(id,SeasonIds.previous(SeasonIds.next(id)))
        }
        assertEquals(SeasonIds.SPRING,SeasonIds.next(SeasonIds.WINTER))
        assertEquals(SeasonIds.WINTER,SeasonIds.previous(SeasonIds.SPRING))
        assertThrows(IllegalArgumentException::class.java){SeasonIds.next(ContentId("day.monday"))}
    }
    @Test fun bilingualQuestionsUseCycleAndNeverWeatherStereotypes() {
        for(mode in modes) for(anchor in SeasonIds.canonicalOrder) {
            val q=SeasonsCycle.question(mode,anchor,SeasonIds.canonicalOrder)
            assertEquals(if(mode==SeasonsPhase.NEXT)SeasonIds.next(anchor) else SeasonIds.previous(anchor),q.correct)
            assertEquals(anchor,SeasonsCycle.anchor(mode,q))
            val word=SeasonsContent.season(anchor).text.display
            assertTrue(q.instruction.speech.en.contains(word.en));assertTrue(q.instruction.speech.de.contains(word.de))
            assertTrue(q.hint!!.speech.de.contains("im Kreis"))
        }
        assertEquals("Welche Jahreszeit kommt vor dem Frühling?",SeasonsCycle.question(SeasonsPhase.BEFORE,SeasonIds.SPRING,SeasonIds.canonicalOrder).instruction.speech.de)
    }
    @Test fun fiveAndTenRoundsAreStableAndCoverEveryBoundaryBeforeRepeating() {
        for(mode in modes) for(round in RoundLength.entries) for(seed in 0L..20L) {
            fun plan()=(SeasonsCycle.generate(mode,SessionId("cycle"),round,seed) as GenerationResult.Generated).plan
            val a=plan();val b=plan()
            assertEquals(round.count,a.tasks.size)
            assertEquals(SeasonIds.canonicalOrder.toSet(),a.tasks.take(4).map {SeasonsCycle.anchor(mode,it.question)}.toSet())
            a.tasks.zip(b.tasks).forEach {(x,y)-> assertEquals(x.id,y.id);assertEquals(x.question.choices,y.question.choices)
                assertEquals(1,x.question.choices.count {it==x.question.correct}) }
            val s=SessionReducer.start(a,ContentLanguage.ENGLISH,SeasonsContent.repository).state
            SeasonsCycle.validate(mode,s)
            assertEquals(s.task,SessionReducer.reduce(s,SessionAction.Language(ContentLanguage.GERMAN)).state.task)
        }
    }
    @Test fun realJournalsRestoreAnsweredSupportScoreAndCompletedRoundsSilently() {
        for(mode in modes) for(round in RoundLength.entries) {
            val disk=AtomicProgressStorage(temp.newFolder(),JvmAtomicCommit);val dir=temp.newFolder()
            fun host()=SeasonsCycle.host(mode,disk,ProgressFixtures.repository(dir))
            var h=host();h.open(SessionId("cycle-${mode.name}-${round.count}"),round,42,ContentLanguage.ENGLISH)
            val untouched=SessionCheckpoint.encode(h.state!!)
            h=host();assertTrue(h.open(SessionId("unused"),RoundLength.TEN,999,ContentLanguage.GERMAN).isEmpty())
            assertArrayEquals(untouched,SessionCheckpoint.encode(h.state!!))
            h.dispatch(SessionAction.Replay(h.state!!.task.id));h.dispatch(SessionAction.Hint(h.state!!.task.id))
            assertEquals(0,h.state!!.current.attempts)
            val s=h.state!!;h.dispatch(SessionAction.Answer(s.nextAttempt!!,s.task.question.choices.first {it!=s.task.question.correct}))
            h.dispatch(SessionAction.Retry(AttemptId(s.task.id,1)))
            h.dispatch(SessionAction.Language(ContentLanguage.GERMAN))
            repeat(round.count) {
                val before=h.state!!;val action=SessionAction.Answer(before.nextAttempt!!,before.task.question.correct)
                h.dispatch(action);h.dispatch(action)
                val encoded=SessionCheckpoint.encode(h.state!!)
                h=host();assertTrue(h.open(SessionId("unused"),round,0,ContentLanguage.ENGLISH).isEmpty())
                assertArrayEquals(encoded,SessionCheckpoint.encode(h.state!!))
                assertEquals(it+1,h.state!!.score)
                h.dispatch(SessionAction.Next(h.state!!.task.id))
            }
            assertEquals(CompletionState.ACKNOWLEDGED,h.state!!.completion)
            val final=SessionCheckpoint.encode(h.state!!);h=host();h.open(SessionId("unused"),round,0,ContentLanguage.ENGLISH)
            assertArrayEquals(final,SessionCheckpoint.encode(h.state!!))
            val events=ProgressFixtures.records(ProgressFixtures.repository(dir)).map {it.event}
            assertEquals(1,events.filterIsInstance<CompletionEvent>().size)
            assertTrue(events.filterIsInstance<AttemptEvent>().first().support.hint)
            assertEquals(round.count+2,events.size)
        }
    }
    @Test fun distractorsRepresentSameSeasonReversedDirectionAndSkippingASeason() {
        for(mode in modes) for(anchor in SeasonIds.canonicalOrder) {
            val q=SeasonsCycle.question(mode,anchor,SeasonIds.canonicalOrder)
            val reverse=if(mode==SeasonsPhase.NEXT)SeasonIds.previous(anchor) else SeasonIds.next(anchor)
            val skip=SeasonIds.next(SeasonIds.next(anchor))
            assertEquals(setOf(anchor,reverse,skip),q.choices.filter {it!=q.correct}.toSet())
        }
    }
    @Test fun rejectsWrongModeAndInvalidCandidateReferences() {
        assertThrows(IllegalArgumentException::class.java){SeasonsCycle.question(SeasonsPhase.EXPLORE,SeasonIds.SPRING,SeasonIds.canonicalOrder)}
        assertThrows(IllegalArgumentException::class.java){SeasonsCycle.question(SeasonsPhase.NEXT,SeasonIds.SPRING,List(4){SeasonIds.SPRING})}
        val s=SessionReducer.start((SeasonsCycle.generate(SeasonsPhase.NEXT,SessionId("x"),RoundLength.FIVE,1) as GenerationResult.Generated).plan,ContentLanguage.ENGLISH,SeasonsContent.repository).state
        assertThrows(IllegalArgumentException::class.java){SeasonsCycle.validate(SeasonsPhase.BEFORE,s)}
    }
    @Test fun everyNewPhaseSelectionRestoresWithoutChangingSelectedExploreSeason() {
        val store=SeasonsSelectionStore(AtomicProgressStorage(temp.newFolder(),JvmAtomicCommit))
        for(phase in SeasonsPhase.entries) {val s=SeasonsSelection(SeasonIds.AUTUMN,phase);store.write(s);assertEquals(s,store.read())}
    }
}
