package com.bellfamily.bastischool.learning.length

import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.session.*
import com.bellfamily.bastischool.learning.progress.*
import com.bellfamily.bastischool.audio.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class LengthComparisonTest {
    @get:Rule val temp=TemporaryFolder()
    private fun disk()=AtomicProgressStorage(temp.newFolder(),JvmAtomicCommit)
    private fun progress()=FileProgressRepository(disk(),ProgressClock{1})
    private fun start(r:RoundLength=RoundLength.FIVE)=LengthComparison.start(SessionId("length"),r,7,ContentLanguage.ENGLISH)
    private fun act(s:LengthComparison.State,a:LengthComparison.Action)=LengthComparison.reduce(s,a)
    private fun align(s:LengthComparison.State):LengthComparison.State {
        var next=s
        for(strip in Strip.entries) {
            if(next.selected!=strip)next=act(next,LengthComparison.Action.Select(strip))
            next=act(next,LengthComparison.Action.Move(0))
        }
        return next
    }
    private fun solve(h:LengthComparisonHost) {
        for(strip in Strip.entries) {
            if(h.state!!.selected!=strip)h.dispatch(LengthComparison.Action.Select(strip))
            h.dispatch(LengthComparison.Action.Move(0))
        }
        h.dispatch(LengthComparison.Action.Choose(h.state!!.task.relation));h.dispatch(LengthComparison.Action.Check)
    }
    @Test fun seededRoundsCoverAllRelationsAndKeepLengthSeparateFromPaletteAndPosition() {
        val palettes=mutableMapOf<LengthRelation,MutableSet<Int>>();val offsets=mutableMapOf<LengthRelation,MutableSet<StripPositions>>()
        for(r in RoundLength.entries)for(seed in 0L..500) {
            val plan=LengthComparison.generate(r,seed)
            assertEquals(plan,LengthComparison.generate(r,seed));assertEquals(r.count,plan.map{it.a to it.b}.distinct().size)
            val counts=LengthRelation.entries.map{relation->plan.count{it.relation==relation}}
            assertTrue(counts.min()>=1&&counts.max()-counts.min()<=1)
            for(t in plan) {
                assertTrue(t.a==t.b||kotlin.math.abs(t.a-t.b)>=2)
                assertFalse(t.initial.aligned);assertNotEquals(t.initial.a,t.initial.b)
                assertTrue(t.a+t.initial.a<=9&&t.b+t.initial.b<=9)
                palettes.getOrPut(t.relation){mutableSetOf()}.add(t.palette);offsets.getOrPut(t.relation){mutableSetOf()}.add(t.initial)
            }
            val again=LengthComparison.generate(r,seed,plan)
            assertNotEquals(plan,again);assertNotEquals(plan.last().a to plan.last().b,again.first().a to again.first().b)
        }
        assertTrue(palettes.values.all{it.size==4});assertTrue(offsets.values.all{it.size==6})
    }
    @Test fun selectTranslateCancelAndUndoPreserveBothLengthsAndBoundedHistory() {
        var s=start();val task=s.task
        assertSame(s,act(s,LengthComparison.Action.Move(0)))
        val original=s;s=act(s,LengthComparison.Action.Select(Strip.A));assertEquals(original,act(s,LengthComparison.Action.Select(Strip.A)))
        repeat(110) {
            s=act(s,LengthComparison.Action.Move(if(s.positions.a==0)3 else 0))
            assertEquals(task,s.task);assertEquals(0,s.current.attempts);assertEquals(original.positions.b,s.positions.b)
        }
        assertEquals(100,s.undo.size);val old=s.undo.last();s=act(s,LengthComparison.Action.Undo);assertEquals(old,s.positions);assertNull(s.selected)
        for(offset in listOf(-1,4,Int.MAX_VALUE))assertSame(s,act(s,LengthComparison.Action.Move(offset)))
    }
    @Test fun unalignedWorkCannotScoreAndOnlyChangedCheckedRelationsRecordAttempts() {
        var s=start();s=act(s,LengthComparison.Action.Choose(s.task.relation));assertSame(s,act(s,LengthComparison.Action.Check))
        s=align(s);val wrong=LengthRelation.entries.first{it!=s.task.relation}
        s=act(s,LengthComparison.Action.Choose(wrong));assertEquals(0,s.current.attempts)
        s=act(s,LengthComparison.Action.Check);assertTrue(s.needsCorrection);assertEquals(1,s.current.attempts)
        s=act(s,LengthComparison.Action.Move(2));s=act(s,LengthComparison.Action.Move(0))
        assertSame(s,act(s,LengthComparison.Action.Check))
        s=act(s,LengthComparison.Action.Help);s=act(s,LengthComparison.Action.Replay)
        s=act(s,LengthComparison.Action.Choose(s.task.relation));s=act(s,LengthComparison.Action.Check)
        assertTrue(s.current.solved);assertEquals(2,s.current.attempts);assertTrue(s.current.support.hint);assertEquals(1,s.current.support.replays)
        assertNull(s.selected);assertEquals(s.task.relation.id,LengthComparison.attempt(s).choice)
        for(a in listOf(LengthComparison.Action.Select(Strip.A),LengthComparison.Action.Move(3),LengthComparison.Action.Undo,LengthComparison.Action.Check,LengthComparison.Action.Choose(wrong)))assertSame(s,act(s,a))
        val next=act(s,LengthComparison.Action.Next);assertEquals(1,next.index);assertEquals(next.task.initial,next.positions);assertNull(next.selected);assertNull(next.answer);assertTrue(next.undo.isEmpty())
    }
    @Test fun durableRoundsRestoreEveryPhaseAndDeduplicateSharedEvidence() {
        for(r in RoundLength.entries) {
            val d=disk();val p=progress();var h=LengthComparisonHost(d,p)
            assertTrue(h.open(SessionId(r.name),r,5,ContentLanguage.GERMAN))
            h.dispatch(LengthComparison.Action.Select(Strip.A));h.dispatch(LengthComparison.Action.Move(0));h.dispatch(LengthComparison.Action.Help)
            val partial=h.state!!;assertTrue((p.read()as ProgressReadResult.Events).records.isEmpty())
            h=LengthComparisonHost(d,p);assertFalse(h.open(SessionId("ignored"),RoundLength.FIVE,1,ContentLanguage.ENGLISH));assertEquals(partial,h.state)
            repeat(r.count) {
                solve(h);val solved=h.state!!;h=LengthComparisonHost(d,p);h.open(SessionId("ignored"),r,99,ContentLanguage.ENGLISH);assertEquals(solved,h.state)
                h.dispatch(LengthComparison.Action.Next)
            }
            val complete=h.state!!;assertTrue(complete.completed&&complete.acknowledged)
            h=LengthComparisonHost(d,p);h.open(SessionId("ignored"),r,1,ContentLanguage.ENGLISH);assertEquals(complete,h.state)
            val records=(p.read()as ProgressReadResult.Events).records
            assertEquals(r.count,records.count{it.event is AttemptEvent});assertEquals(1,records.count{it.event is CompletionEvent})
            assertEquals(records,ProgressCodec.decode(ProgressCodec.encode(records)))
            assertEquals(setOf("skill.math.length.direct_comparison"),(records.last().event as CompletionEvent).tasks.map{it.evidence.skill.value}.toSet())
            h.again(SessionId("again-${r.name}"),r,5,ContentLanguage.GERMAN);assertNotEquals(complete.tasks,h.state!!.tasks)
        }
    }
    @Test fun selectionMovementWrongAnswerAndTranslationRoundTripAndCorruptionIsRejected() {
        val initial=start();val selected=act(initial,LengthComparison.Action.Select(Strip.B));val moved=act(selected,LengthComparison.Action.Move(0))
        val aligned=align(moved);val wrong=act(act(aligned,LengthComparison.Action.Choose(LengthRelation.entries.first{it!=aligned.task.relation})),LengthComparison.Action.Check)
        val solved=act(act(wrong,LengthComparison.Action.Choose(wrong.task.relation)),LengthComparison.Action.Check)
        for(s in listOf(initial,selected,moved,aligned,wrong,solved)) {
            val de=act(s,LengthComparison.Action.Language(ContentLanguage.GERMAN));assertEquals(s.positions,de.positions);assertEquals(s.tasks,de.tasks)
            val bytes=LengthComparisonHost.encode(de,emptyList());assertEquals(de,LengthComparisonHost.decode(bytes).first)
            val bad=bytes.copyOf();bad[20]=(bad[20].toInt()xor 1).toByte()
            for(b in listOf(bad,bytes+byteArrayOf(1),bytes.copyOf(bytes.size-1),ByteArray(32001)))assertTrue(runCatching{LengthComparisonHost.decode(b)}.isFailure)
        }
        assertTrue(runCatching{StripPositions(4,0)}.isFailure);assertTrue(runCatching{initial.copy(undo=listOf(initial.positions))}.isFailure)
        assertTrue(runCatching{LengthTask(3,4,StripPositions(1,2),0)}.isFailure)
    }
    @Test fun uncertainProgressAndCompletionRecoverWithOriginalEventIdentity() {
        val d=disk();val actual=progress();var fail=true
        val flaky=object:ProgressRepository {
            override fun read(query:ProgressQuery)=actual.read(query)
            override fun append(event:ProgressEvent):ProgressWriteResult {val result=actual.append(event);return if(fail)ProgressWriteResult.Failed(ProgressFailure.IO)else result}
        }
        var h=LengthComparisonHost(d,flaky);h.open(SessionId("pending"),RoundLength.FIVE,5,ContentLanguage.ENGLISH);solve(h)
        assertTrue(h.failure&&h.hasPending);val saved=h.state;h.dispatch(LengthComparison.Action.Next);assertEquals(saved,h.state)
        fail=false;h=LengthComparisonHost(d,flaky);h.open(SessionId("ignored"),RoundLength.TEN,1,ContentLanguage.GERMAN)
        assertEquals(saved,h.state);assertFalse(h.failure||h.hasPending);assertEquals(1,(actual.read()as ProgressReadResult.Events).records.size)
        h.dispatch(LengthComparison.Action.Next);repeat(4){solve(h);if(it==3)fail=true;h.dispatch(LengthComparison.Action.Next)}
        assertTrue(h.state!!.completed&&!h.state!!.acknowledged)
        assertTrue(runCatching{h.again(SessionId("early"),RoundLength.FIVE,1,ContentLanguage.ENGLISH)}.isFailure)
        h=LengthComparisonHost(d,actual);h.open(SessionId("ignored"),RoundLength.FIVE,1,ContentLanguage.ENGLISH)
        assertTrue(h.state!!.acknowledged);assertEquals(1,(actual.read()as ProgressReadResult.Events).records.count{it.event is CompletionEvent})
    }
    @Test fun uncertainCheckpointCanRecoverMovesWithoutFabricatingAssessment() {
        val actual=disk();var fail=false
        val uncertain=object:ProgressStorage {
            override fun <T> access(block:(ProgressTransaction)->T):T=actual.access{tx->block(object:ProgressTransaction {
                override fun read()=tx.read()
                override fun replace(bytes:ByteArray){tx.replace(bytes);if(fail)throw java.io.IOException("accepted then interrupted")}
            })}
        }
        val p=progress();val h=LengthComparisonHost(uncertain,p);h.open(SessionId("checkpoint"),RoundLength.FIVE,7,ContentLanguage.ENGLISH)
        h.dispatch(LengthComparison.Action.Select(Strip.A));fail=true
        assertTrue(runCatching{h.dispatch(LengthComparison.Action.Move(0))}.isFailure)
        fail=false;h.retryWrites();assertEquals(0,h.state!!.positions.a);assertEquals(1,h.state!!.undo.size)
        assertTrue((p.read()as ProgressReadResult.Events).records.isEmpty())
        val bytes=actual.access{it.read()}!!;val bad=bytes.copyOf();bad[20]=(bad[20].toInt()xor 1).toByte();actual.access{it.replace(bad)}
        assertTrue(runCatching{h.retryWrites()}.isFailure);assertArrayEquals(bad,actual.access{it.read()})
    }
    @Test fun staleConcurrentOwnerCannotOverwriteAcceptedWork() {
        val d=disk();val p=progress();val first=LengthComparisonHost(d,p);first.open(SessionId("shared"),RoundLength.FIVE,7,ContentLanguage.ENGLISH)
        val stale=LengthComparisonHost(d,p);stale.open(SessionId("ignored"),RoundLength.FIVE,7,ContentLanguage.ENGLISH)
        first.dispatch(LengthComparison.Action.Select(Strip.B));val bytes=d.access{it.read()}!!
        assertTrue(runCatching{stale.dispatch(LengthComparison.Action.Help)}.isFailure);assertArrayEquals(bytes,d.access{it.read()})
        stale.retryWrites();assertEquals(first.state,stale.state)
    }
    @Test fun bilingualPromptsAndSharedAudioModesRespectHiddenOwnersAndPhase() {
        for(mode in AudioMode.entries)for(lang in ContentLanguage.entries) {
            val engine=FakeSpeechEngine();val audio=LengthComparisonAudio(DefaultAudioController(engine,mode));audio.visible(true);val s=start().copy(language=lang)
            audio.speak(s,LengthComparison.prompt,SpeechKind.QUESTION)
            audio.speak(s,LengthComparison.feedback(s),SpeechKind.FEEDBACK)
            audio.speak(s,LengthComparison.hint,SpeechKind.EXPLANATION,true)
            audio.speak(s,LengthComparison.complete,SpeechKind.COMPLETION)
            audio.speak(s,LengthComparison.complete,SpeechKind.COMPLETION,true)
            assertEquals(when(mode){AudioMode.ALL->5;AudioMode.QUESTIONS->3;AudioMode.OFF->0},engine.spoken.size)
            val count=engine.spoken.size;audio.visible(false);audio.speak(s,LengthComparison.complete,SpeechKind.COMPLETION,true);assertEquals(count,engine.spoken.size)
            val solved=act(act(align(s),LengthComparison.Action.Choose(s.task.relation)),LengthComparison.Action.Check)
            assertEquals(LengthComparison.feedback(solved),LengthComparisonAudio.current(solved));audio.close()
        }
    }
}
