package com.bellfamily.bastischool.learning.length

import com.bellfamily.bastischool.audio.*
import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.progress.*
import com.bellfamily.bastischool.learning.session.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class LengthOrderingTest {
    @get:Rule val temp = TemporaryFolder()
    private fun disk() = AtomicProgressStorage(temp.newFolder(),JvmAtomicCommit)
    private fun progress() = FileProgressRepository(disk(),ProgressClock { 1 })
    private fun start(r: RoundLength = RoundLength.FIVE) = LengthOrdering.start(SessionId("length-order"),r,7,ContentLanguage.ENGLISH)
    private fun act(s: LengthOrdering.State, a: LengthOrdering.Action) = LengthOrdering.reduce(s,a)
    private fun arrange(s: LengthOrdering.State, ids: List<ContentId>): LengthOrdering.State {
        var n=s
        n.current.order.toList().forEach { n=act(n,LengthOrdering.Action.Remove(it)) }
        ids.forEach { n=act(n,LengthOrdering.Action.Place(it)) }
        return n
    }
    private fun solve(h: LengthOrderingHost) {
        h.state!!.current.order.toList().forEach { h.dispatch(LengthOrdering.Action.Remove(it)) }
        h.state!!.task.target.forEach { h.dispatch(LengthOrdering.Action.Place(it)) }
        h.dispatch(LengthOrdering.Action.Check)
    }
    @Test fun seededPlansVaryLengthsPalettesAndSourceOrderWithoutTiesOrRepeatedSets() {
        val palettes=mutableSetOf<Int>(); val sources=mutableSetOf<List<ContentId>>()
        for(r in RoundLength.entries)for(seed in 0L..300) {
            val plan=LengthOrdering.generate(r,seed)
            assertEquals(plan,LengthOrdering.generate(r,seed))
            assertEquals(r.count,plan.size);assertEquals(r.count,plan.map { it.lengths.sorted() }.distinct().size)
            plan.forEach { t ->
                assertTrue(t.lengths.sorted().zipWithNext().all { (a,b) -> b-a >= 2 })
                assertNotEquals(t.target,t.presentation)
                assertEquals(t.lengths.sorted(),t.target.map(t::length))
                palettes+=t.palette;sources+=t.presentation
            }
            val again=LengthOrdering.generate(r,seed,plan)
            assertNotEquals(plan.first().lengths,again.first().lengths)
            assertNotEquals(plan.last().lengths,again.first().lengths)
        }
        assertEquals(4,palettes.size);assertEquals(6,sources.size)
    }
    @Test fun freeConstructionRemovalUndoAndChangedWrongChecksReuseSequencingWithoutScoringEdits() {
        var s=start();val task=s.task
        assertSame(s,act(s,LengthOrdering.Action.Check))
        s=act(s,LengthOrdering.Action.Place(task.presentation.first()))
        assertEquals(0,s.current.attempts)
        assertSame(s,act(s,LengthOrdering.Action.Place(task.presentation.first())))
        s=act(s,LengthOrdering.Action.Undo);assertTrue(s.current.order.isEmpty())
        s=arrange(s,task.target.reversed());s=act(s,LengthOrdering.Action.Check)
        assertTrue(s.needsCorrection);assertEquals(1,s.current.attempts)
        assertSame(s,act(s,LengthOrdering.Action.Check))
        val last=s.current.order.last();s=act(s,LengthOrdering.Action.Remove(last));s=act(s,LengthOrdering.Action.Place(last))
        assertSame(s,act(s,LengthOrdering.Action.Check))
        s=act(s,LengthOrdering.Action.Help);s=act(s,LengthOrdering.Action.Replay)
        s=arrange(s,task.target);s=act(s,LengthOrdering.Action.Check)
        assertTrue(s.solved);assertEquals(2,s.current.attempts);assertTrue(s.current.support.hint)
        for(a in listOf(LengthOrdering.Action.Undo,LengthOrdering.Action.Remove(last),LengthOrdering.Action.Place(last),LengthOrdering.Action.Check))assertSame(s,act(s,a))
        assertEquals(task,s.task)
        val next=act(s,LengthOrdering.Action.Next);assertEquals(1,next.index);assertTrue(next.current.order.isEmpty()&&next.undo.isEmpty())
    }
    @Test fun undoIsBoundedAndDoesNotEraseAssessmentOrSupport() {
        var s=start();val id=s.task.presentation.first()
        repeat(110) { s=act(s,LengthOrdering.Action.Place(id));s=act(s,LengthOrdering.Action.Remove(id)) }
        assertEquals(100,s.undo.size);assertEquals(0,s.current.attempts)
        s=arrange(s,s.task.target.reversed());s=act(s,LengthOrdering.Action.Check)
        s=act(s,LengthOrdering.Action.Help);val checked=s.current.checked
        s=act(s,LengthOrdering.Action.Remove(s.current.order.first()));s=act(s,LengthOrdering.Action.Undo)
        assertEquals(checked,s.current.order);assertTrue(s.needsCorrection&&s.current.support.hint)
        assertEquals(1,s.current.attempts)
    }
    @Test fun fiveAndTenRoundsRestoreEverySolvedPhaseAndDeliverOnlySharedCheckedEvidence() {
        for(r in RoundLength.entries) {
            val d=disk();val p=progress();var h=LengthOrderingHost(d,p)
            assertTrue(h.open(SessionId("round-${r.name}"),r,8,ContentLanguage.GERMAN))
            h.dispatch(LengthOrdering.Action.Place(h.state!!.task.presentation.first()));h.dispatch(LengthOrdering.Action.Help)
            val partial=h.state!!;assertTrue((p.read() as ProgressReadResult.Events).records.isEmpty())
            h=LengthOrderingHost(d,p);assertFalse(h.open(SessionId("ignored"),RoundLength.FIVE,999,ContentLanguage.ENGLISH));assertEquals(partial,h.state)
            repeat(r.count) {
                solve(h);val solved=h.state!!;h=LengthOrderingHost(d,p);h.open(SessionId("ignored"),r,1,ContentLanguage.ENGLISH);assertEquals(solved,h.state)
                h.dispatch(LengthOrdering.Action.Next)
            }
            val complete=h.state!!;assertTrue(complete.completed&&complete.acknowledged)
            h=LengthOrderingHost(d,p);h.open(SessionId("ignored"),r,5,ContentLanguage.ENGLISH);assertEquals(complete,h.state)
            val records=(p.read() as ProgressReadResult.Events).records
            assertEquals(r.count,records.count { it.event is AttemptEvent });assertEquals(1,records.count { it.event is CompletionEvent })
            val completed=records.last().event as CompletionEvent
            assertEquals(setOf("skill.math.length.order_three"),completed.tasks.map { it.evidence.skill.value }.toSet())
            assertEquals(records,ProgressCodec.decode(ProgressCodec.encode(records)))
            h.again(SessionId("again-${r.name}"),r,8,ContentLanguage.ENGLISH);assertNotEquals(complete.tasks,h.state!!.tasks)
        }
    }
    @Test fun allWorkHistoryAndLanguageRoundTripAndMalformedStateCannotLoad() {
        val initial=start();val partial=act(initial,LengthOrdering.Action.Place(initial.task.presentation.first()))
        val wrong=act(arrange(partial,partial.task.target.reversed()),LengthOrdering.Action.Check)
        val solved=act(arrange(wrong,wrong.task.target),LengthOrdering.Action.Check)
        for(s in listOf(initial,partial,wrong,solved)) {
            val de=act(s,LengthOrdering.Action.Language(ContentLanguage.GERMAN))
            assertEquals(s.tasks,de.tasks);assertEquals(s.work,de.work);assertEquals(s.undo,de.undo)
            val bytes=LengthOrderingHost.encode(de,emptyList());assertEquals(de,LengthOrderingHost.decode(bytes).first)
            val bad=bytes.copyOf();bad[20]=(bad[20].toInt() xor 1).toByte()
            for(b in listOf(bad,bytes+byteArrayOf(1),bytes.copyOf(bytes.size-1),ByteArray(32001)))assertTrue(runCatching { LengthOrderingHost.decode(b) }.isFailure)
        }
        assertTrue(runCatching { initial.copy(undo=listOf(emptyList())) }.isFailure)
        assertTrue(runCatching { LengthOrderTask(listOf(2,3,6),initial.task.presentation,0) }.isFailure)
        assertTrue(runCatching { LengthOrderingHost.encode(wrong,listOf(LengthOrdering.attempt(solved))).let(LengthOrderingHost::decode) }.isFailure)
    }
    @Test fun acceptedButUncertainProgressRetriesOriginalAttemptAndCompletionIdentities() {
        val d=disk();val actual=progress();var fail=true
        val flaky=object: ProgressRepository {
            override fun read(query: ProgressQuery)=actual.read(query)
            override fun append(event: ProgressEvent): ProgressWriteResult { val result=actual.append(event);return if(fail)ProgressWriteResult.Failed(ProgressFailure.IO)else result }
        }
        var h=LengthOrderingHost(d,flaky);h.open(SessionId("pending"),RoundLength.FIVE,5,ContentLanguage.ENGLISH);solve(h)
        assertTrue(h.failure&&h.hasPending);val saved=h.state;h.dispatch(LengthOrdering.Action.Next);assertEquals(saved,h.state)
        fail=false;h=LengthOrderingHost(d,flaky);h.open(SessionId("ignored"),RoundLength.TEN,8,ContentLanguage.GERMAN)
        assertEquals(saved,h.state);assertFalse(h.failure||h.hasPending);assertEquals(1,(actual.read() as ProgressReadResult.Events).records.size)
        h.dispatch(LengthOrdering.Action.Next);repeat(4) { solve(h);if(it==3)fail=true;h.dispatch(LengthOrdering.Action.Next) }
        assertTrue(h.state!!.completed&&!h.state!!.acknowledged)
        assertTrue(runCatching { h.again(SessionId("early"),RoundLength.FIVE,1,ContentLanguage.ENGLISH) }.isFailure)
        h=LengthOrderingHost(d,actual);h.open(SessionId("ignored"),RoundLength.FIVE,1,ContentLanguage.ENGLISH)
        assertTrue(h.state!!.acknowledged);assertEquals(1,(actual.read() as ProgressReadResult.Events).records.count { it.event is CompletionEvent })
    }
    @Test fun uncertainPlacementAndConcurrentOwnersPreserveAcceptedWork() {
        val actual=disk();var fail=false
        val uncertain=object: ProgressStorage {
            override fun <T> access(block:(ProgressTransaction)->T):T=actual.access { tx -> block(object: ProgressTransaction {
                override fun read()=tx.read()
                override fun replace(bytes: ByteArray) { tx.replace(bytes);if(fail)throw java.io.IOException("accepted then interrupted") }
            }) }
        }
        val p=progress();val h=LengthOrderingHost(uncertain,p);h.open(SessionId("checkpoint"),RoundLength.FIVE,7,ContentLanguage.ENGLISH)
        val stale=LengthOrderingHost(actual,p);stale.open(SessionId("ignored"),RoundLength.FIVE,7,ContentLanguage.ENGLISH)
        fail=true;assertTrue(runCatching { h.dispatch(LengthOrdering.Action.Place(h.state!!.task.presentation.first())) }.isFailure)
        val bytes=actual.access { it.read() }!!
        assertTrue(runCatching { stale.dispatch(LengthOrdering.Action.Help) }.isFailure);assertArrayEquals(bytes,actual.access { it.read() })
        fail=false;h.retryWrites();assertEquals(1,h.state!!.current.order.size);assertEquals(1,h.state!!.undo.size)
        assertTrue((p.read() as ProgressReadResult.Events).records.isEmpty())
        val bad=bytes.copyOf();bad[20]=(bad[20].toInt() xor 1).toByte();actual.access { it.replace(bad) }
        assertTrue(runCatching { h.retryWrites() }.isFailure);assertArrayEquals(bad,actual.access { it.read() })
    }
    @Test fun helpOnCorrectUncheckedWorkIsStillSupportAndAudioUsesSharedModes() {
        val arranged=arrange(start(),start().task.target)
        val supported=act(arranged,LengthOrdering.Action.Help);assertTrue(supported.current.support.hint);assertFalse(supported.solved)
        for(mode in AudioMode.entries)for(lang in ContentLanguage.entries) {
            val engine=FakeSpeechEngine();val audio=LengthOrderingAudio(DefaultAudioController(engine,mode));audio.visible(true)
            val s=supported.copy(language=lang)
            audio.speak(s,LengthOrdering.prompt,SpeechKind.QUESTION)
            audio.speak(s,LengthOrdering.feedback(s),SpeechKind.FEEDBACK)
            audio.speak(s,LengthOrdering.hint,SpeechKind.EXPLANATION,true)
            audio.speak(s,LengthOrdering.complete,SpeechKind.COMPLETION)
            audio.speak(s,LengthOrdering.complete,SpeechKind.COMPLETION,true)
            assertEquals(when(mode) { AudioMode.ALL->5;AudioMode.QUESTIONS->3;AudioMode.OFF->0 },engine.spoken.size)
            val count=engine.spoken.size;audio.visible(false);audio.speak(s,LengthOrdering.complete,SpeechKind.COMPLETION,true);assertEquals(count,engine.spoken.size)
            val solved=act(s,LengthOrdering.Action.Check);assertEquals(LengthOrdering.feedback(solved),LengthOrderingAudio.current(solved));audio.close()
        }
    }
}
