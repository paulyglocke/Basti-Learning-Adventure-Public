package com.bellfamily.bastischool.learning.subitising

import com.bellfamily.bastischool.audio.*
import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.progress.*
import com.bellfamily.bastischool.learning.session.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class SubitisingTest {
    @get:Rule val temp=TemporaryFolder()
    private fun plan(round:RoundLength,seed:Long)=(SubitisingContent.generate(SessionId("dots"),round,seed) as GenerationResult.Generated).plan
    @Test fun patternsAreStableDistinctDiePositionsAndBilingualDescriptions() {
        val expected=listOf(listOf(.5f to .5f),listOf(.25f to .25f,.75f to .75f),
            listOf(.25f to .25f,.5f to .5f,.75f to .75f),
            listOf(.25f to .25f,.75f to .25f,.25f to .75f,.75f to .75f))
        for(n in 1..5) {
            val dots=SubitisingContent.pattern(n)
            assertEquals(n,dots.toSet().size)
            assertEquals(if(n<5)expected[n-1] else expected[3]+(.5f to .5f),dots.map {it.x to it.y})
        }
        assertEquals("Three dots",SubitisingContent.description(3).display.en)
        assertEquals("Drei Punkte",SubitisingContent.description(3).display.de)
    }
    @Test fun deterministicRoundsCoverEachQuantityAndFourUniqueChoicesWithoutAdjacentRepeats() {
        for(round in RoundLength.entries)for(seed in 0L..99L) {
            fun start()=SessionReducer.start(plan(round,seed),ContentLanguage.ENGLISH,SubitisingContent.repository).state
            val s=start()
            assertArrayEquals(SessionCheckpoint.encode(s),SessionCheckpoint.encode(start()))
            SubitisingContent.validate(s)
            val quantities=s.plan.tasks.map {it.question.correct}
            assertEquals(SubitisingContent.numbers.toSet(),quantities.toSet())
            assertTrue(quantities.groupingBy {it}.eachCount().values.all {it==round.count/5})
            assertTrue(quantities.zipWithNext().all {it.first!=it.second})
            s.plan.tasks.forEach {
                val q=it.question;val n=SubitisingContent.quantity(q.correct)
                assertEquals(4,q.choices.toSet().size);assertEquals(1,q.choices.count {it==q.correct})
                assertTrue(q.choices.all {it in SubitisingContent.numbers})
                assertEquals("How many?",q.instruction.display.en);assertEquals("Wie viele?",q.instruction.display.de)
                assertEquals("Yes, $n!",q.correctFeedback.display.en);assertEquals("Ja, $n!",q.correctFeedback.display.de)
                assertEquals("Try again.",q.wrongFeedback.display.en);assertEquals("Versuch es noch einmal.",q.wrongFeedback.display.de)
                assertEquals(SubitisingContent.help,q.hint)
            }
        }
    }
    @Test fun exactRestoreRetryHelpLanguageCompletionAndNewRoundUseSharedProgress() {
        for(round in RoundLength.entries) {
            val disk=AtomicProgressStorage(temp.newFolder(),JvmAtomicCommit)
            val repository=ProgressFixtures.repository(temp.newFolder())
            fun open()=SubitisingContent.host(disk,repository).also {it.open(SessionId("dots"),round,7,ContentLanguage.ENGLISH)}
            var host=open()
            host.dispatch(SessionAction.Hint(host.state!!.task.id))
            val s=host.state!!
            host.dispatch(SessionAction.Answer(s.nextAttempt!!,s.task.question.choices.first {it!=s.task.question.correct}))
            host.dispatch(SessionAction.Language(ContentLanguage.GERMAN))
            val partial=SessionCheckpoint.encode(host.state!!)
            val restored=SubitisingContent.host(disk,repository)
            assertTrue(restored.open(SessionId("unused"),RoundLength.TEN,99,ContentLanguage.ENGLISH).isEmpty())
            assertArrayEquals(partial,SessionCheckpoint.encode(restored.state!!))
            host=restored
            host.dispatch(SessionAction.Retry(AttemptId(host.state!!.task.id,1)))
            assertEquals(1,host.state!!.current.attempts);assertTrue(host.state!!.current.support.hint)
            repeat(round.count) {
                val current=host.state!!
                val action=SessionAction.Answer(current.nextAttempt!!,current.task.question.correct)
                host.dispatch(action);host.dispatch(action)
                val answered=SessionCheckpoint.encode(host.state!!)
                host=open();assertArrayEquals(answered,SessionCheckpoint.encode(host.state!!))
                host.dispatch(SessionAction.Next(host.state!!.task.id))
            }
            host=open();host.retryWrites()
            assertEquals(SessionPhase.COMPLETED,host.state!!.phase)
            assertEquals(CompletionState.ACKNOWLEDGED,host.state!!.completion)
            val events=ProgressFixtures.records(repository).map {it.event}
            assertEquals(round.count+1,events.filterIsInstance<AttemptEvent>().size)
            val completion=events.filterIsInstance<CompletionEvent>().single()
            assertEquals(round.count,completion.tasks.size)
            assertTrue(events.all {it.origin.activity==SubitisingContent.activity})
            host.newRound(SessionId("fresh"),round,8,ContentLanguage.ENGLISH)
            assertEquals(SessionPhase.ACTIVE,host.state!!.phase);assertEquals(0,host.state!!.current.attempts)
        }
    }
    @Test fun pendingAttemptAndCompletionRedeliveryDeduplicateAfterRestore() {
        val disk=AtomicProgressStorage(temp.newFolder(),JvmAtomicCommit)
        val real=ProgressFixtures.repository(temp.newFolder());var fail=false
        val uncertain=object:ProgressRepository {
            override fun append(event:ProgressEvent):ProgressWriteResult {
                val saved=real.append(event)
                return if(fail)ProgressWriteResult.Failed(ProgressFailure.IO) else saved
            }
            override fun read(query:ProgressQuery)=real.read(query)
        }
        fun open()=SubitisingContent.host(disk,uncertain).also {it.open(SessionId("dots"),RoundLength.FIVE,1,ContentLanguage.ENGLISH)}
        var h=open()
        repeat(5) {index ->
            val s=h.state!!;fail=index==0
            h.dispatch(SessionAction.Answer(s.nextAttempt!!,s.task.question.correct))
            if(fail) {assertTrue(h.hasPending);fail=false;h=open();h.retryWrites()}
            fail=index==4
            h.dispatch(SessionAction.Next(h.state!!.task.id))
        }
        assertTrue(h.hasPending);fail=false;h=open();h.retryWrites();open()
        val events=ProgressFixtures.records(real).map {it.event}
        assertEquals(5,events.filterIsInstance<AttemptEvent>().size)
        assertEquals(1,events.filterIsInstance<CompletionEvent>().size)
    }
    @Test fun audioReplayOnlyPromptBothLanguagesPolicyAndNavigationSilence() {
        for(lang in ContentLanguage.entries)for(mode in AudioMode.entries) {
            val engine=FakeSpeechEngine();val audio=SubitisingAudio(DefaultAudioController(engine,mode))
            audio.visible(true);assertTrue(engine.spoken.isEmpty())
            val start=SessionReducer.start(plan(RoundLength.FIVE,1),lang,SubitisingContent.repository)
            audio.replay(start.state,lang)
            if(mode==AudioMode.OFF)assertTrue(engine.spoken.isEmpty()) else {
                assertEquals(SubitisingContent.prompt.speech[lang],engine.spoken.single().text)
                assertEquals(lang,engine.spoken.single().context.language)
            }
            val answered=SessionReducer.reduce(start.state,SessionAction.Answer(start.state.nextAttempt!!,start.state.task.question.correct))
            audio.effects(start.state.plan.id,lang,answered.effects)
            assertEquals(if(mode==AudioMode.ALL)2 else if(mode==AudioMode.QUESTIONS)1 else 0,engine.spoken.size)
            val count=engine.spoken.size
            audio.visible(false);audio.replay(start.state,lang);audio.visible(true)
            assertEquals(count,engine.spoken.size);audio.close()
        }
    }
}
