package com.bellfamily.bastischool.learning.conservation

import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.session.*
import com.bellfamily.bastischool.learning.progress.*
import com.bellfamily.bastischool.audio.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class ConservationTest {
    @get:Rule val temp=TemporaryFolder()
    private fun disk()=AtomicProgressStorage(temp.newFolder(),JvmAtomicCommit)
    private fun progress()=FileProgressRepository(disk(),ProgressClock{1})
    private fun start(c:ConservationRange=ConservationRange.FIVE,r:RoundLength=RoundLength.FIVE)=Conservation.start(SessionId("conservation"),c,r,7,ContentLanguage.ENGLISH)
    private fun move(s:Conservation.State):Conservation.State {
        if(s.count==0)return s
        val from=(0 until s.config.slots).first{s.mask and(1 shl it)!=0}
        val to=(0 until s.config.slots).first{s.mask and(1 shl it)==0}
        return Conservation.reduce(Conservation.reduce(s,Conservation.Action.Tap(from)),Conservation.Action.Tap(to))
    }
    private fun solve(h:ConservationHost){h.dispatch(Conservation.Action.Choose(h.state!!.task.relation));h.dispatch(Conservation.Action.Check)}
    @Test fun deterministicRoundsBalanceRealChangesAndVariedUnchangedGroupsWithoutRepeats() {
        for(c in ConservationRange.entries)for(r in RoundLength.entries)for(seed in 0L..300) {
            val plan=Conservation.generate(c,r,seed)
            assertEquals(plan,Conservation.generate(c,r,seed));assertEquals(r.count,plan.map{Triple(it.before,it.relation,it.item)}.distinct().size)
            assertEquals(r.count*3/5,plan.count{it.relation==QuantityRelation.SAME})
            assertEquals(r.count/5,plan.count{it.relation==QuantityRelation.FEWER});assertEquals(r.count/5,plan.count{it.relation==QuantityRelation.MORE})
            assertTrue(plan.all{Conservation.valid(it,c)});assertTrue(plan.zipWithNext().all{(a,b)->a.item!=b.item})
            assertTrue(plan.filter{it.count>0}.all{it.initialMask!=(1 shl it.count)-1})
            val again=Conservation.generate(c,r,seed,plan);assertNotEquals(plan,again);assertNotEquals(plan.last().item,again.first().item)
            assertFalse(plan.last().before==again.first().before&&plan.last().relation==again.first().relation)
        }
    }
    @Test fun movingAndUndoNeverChangeCountReferenceAnswerOrAssessment() {
        for(c in ConservationRange.entries)for(seed in 0L..30) {
            var s=Conservation.start(SessionId("moves"),c,RoundLength.TEN,seed,ContentLanguage.ENGLISH)
            val initial=s
            repeat(110){s=move(s);assertEquals(initial.count,s.count);assertEquals(initial.task,s.task);assertEquals(0,s.current.attempts)}
            if(s.count>0) {
                assertEquals(100,s.undoMasks.size);val previous=s.undoMasks.last()
                val undo=Conservation.reduce(s,Conservation.Action.Undo);assertEquals(previous,undo.mask);assertNull(undo.selected)
            }
            assertSame(s,Conservation.reduce(s,Conservation.Action.Tap(-1)))
            assertSame(s,Conservation.reduce(s,Conservation.Action.Tap(c.slots)))
        }
    }
    @Test fun selectionCanCancelOrChangeAndEmptyTapNeverCreatesAnObject() {
        var s=(0L..50).map{Conservation.start(SessionId("select"),ConservationRange.FIVE,RoundLength.FIVE,it,ContentLanguage.ENGLISH)}.first{it.count>1}
        val occupied=(0 until s.config.slots).filter{s.mask and(1 shl it)!=0};val empty=(0 until s.config.slots).first{it !in occupied}
        assertSame(s,Conservation.reduce(s,Conservation.Action.Tap(empty)))
        val original=s;s=Conservation.reduce(s,Conservation.Action.Tap(occupied[0]));assertEquals(original.mask,s.mask)
        assertEquals(original,Conservation.reduce(s,Conservation.Action.Tap(occupied[0])))
        s=Conservation.reduce(s,Conservation.Action.Tap(occupied[1]));assertEquals(occupied[1],s.selected)
        val moved=Conservation.reduce(s,Conservation.Action.Tap(empty));assertEquals(original.count,moved.count);assertNull(moved.selected)
        assertEquals(listOf(original.mask),moved.undoMasks)
    }
    @Test fun onlyChangedCheckedRelationsScoreAndRearrangementCannotResubmitAWrongAnswer() {
        var s=start();assertSame(s,Conservation.reduce(s,Conservation.Action.Check))
        val wrong=QuantityRelation.entries.first{it!=s.task.relation}
        s=Conservation.reduce(s,Conservation.Action.Choose(wrong));assertEquals(0,s.current.attempts)
        s=Conservation.reduce(s,Conservation.Action.Check);assertTrue(s.needsCorrection);assertEquals(1,s.current.attempts)
        s=move(s);assertTrue(s.needsCorrection);assertSame(s,Conservation.reduce(s,Conservation.Action.Check))
        s=Conservation.reduce(s,Conservation.Action.Help);s=Conservation.reduce(s,Conservation.Action.Replay)
        s=Conservation.reduce(s,Conservation.Action.Choose(s.task.relation));s=Conservation.reduce(s,Conservation.Action.Check)
        assertTrue(s.current.solved);assertEquals(2,s.current.attempts);assertTrue(s.current.support.hint);assertEquals(1,s.current.support.replays)
        assertEquals(s.task.relation.id,Conservation.attempt(s).choice)
        for(a in listOf(Conservation.Action.Tap(0),Conservation.Action.Undo,Conservation.Action.Check,Conservation.Action.Choose(wrong)))assertSame(s,Conservation.reduce(s,a))
        val next=Conservation.reduce(s,Conservation.Action.Next);assertEquals(1,next.index);assertEquals(next.task.initialMask,next.mask);assertNull(next.answer);assertTrue(next.undoMasks.isEmpty())
    }
    @Test fun allRangesRoundLengthsAndPhasesRestoreWithSeparateSkillEvidenceAndNoDuplicateCompletion() {
        for(c in ConservationRange.entries)for(r in RoundLength.entries) {
            val d=disk();val p=progress();var h=ConservationHost(d,p,c)
            assertTrue(h.open(SessionId("${c.key}-${r.name}"),r,5,ContentLanguage.GERMAN))
            val s=h.state!!
            if(s.count>0){h.dispatch(Conservation.Action.Tap((0 until c.slots).first{s.mask and(1 shl it)!=0}));assertNotNull(h.state!!.selected)}
            h.dispatch(Conservation.Action.Help);val partial=h.state!!
            assertTrue((p.read()as ProgressReadResult.Events).records.isEmpty())
            h=ConservationHost(d,p,c);assertFalse(h.open(SessionId("ignored"),RoundLength.FIVE,1,ContentLanguage.ENGLISH));assertEquals(partial,h.state)
            repeat(r.count){solve(h);val solved=h.state!!;h=ConservationHost(d,p,c);h.open(SessionId("ignored"),r,99,ContentLanguage.ENGLISH);assertEquals(solved,h.state);h.dispatch(Conservation.Action.Next)}
            val complete=h.state!!;assertTrue(complete.completed&&complete.acknowledged)
            h=ConservationHost(d,p,c);h.open(SessionId("ignored"),r,9,ContentLanguage.GERMAN);assertEquals(complete,h.state)
            val records=(p.read()as ProgressReadResult.Events).records;assertEquals(r.count,records.count{it.event is AttemptEvent});assertEquals(1,records.count{it.event is CompletionEvent})
            assertEquals(records,ProgressCodec.decode(ProgressCodec.encode(records)))
            val tasks=(records.last().event as CompletionEvent).tasks
            assertEquals(r.count*3/5,tasks.count{it.evidence.skill.value.contains("quantity_conservation")});assertTrue(tasks.first().support.hint)
            h.again(SessionId("again-${c.key}-${r.name}"),r,5,ContentLanguage.GERMAN);assertNotEquals(complete.tasks,h.state!!.tasks)
        }
    }
    @Test fun zeroAndBoundaryGroupsAreValidAndUseVisibleChangeFacts() {
        for(c in ConservationRange.entries) {
            val zero=ConservationTask(1,QuantityRelation.FEWER,QuantityObject.APPLE,0)
            assertTrue(Conservation.valid(zero,c));assertEquals(0,zero.count)
            assertTrue(Conservation.valid(ConservationTask(c.limit-1,QuantityRelation.MORE,QuantityObject.DINOSAUR,(1 shl c.limit)-1),c))
            assertFalse(Conservation.valid(zero.copy(initialMask=1),c))
            for(t in listOf(zero,zero.copy(relation=QuantityRelation.SAME,initialMask=2),zero.copy(relation=QuantityRelation.MORE,initialMask=3))) {
                for(lang in ContentLanguage.entries)assertTrue(Conservation.prompt(t,c).speech[lang].isNotBlank())
            }
        }
        assertEquals("apple",QuantityObject.APPLE.noun(1,ContentLanguage.ENGLISH));assertEquals("Äpfel",QuantityObject.APPLE.noun(2,ContentLanguage.GERMAN))
    }
    @Test fun selectedMovedWrongSolvedAndTranslatedSnapshotsRoundTripAndRejectCorruption() {
        for(c in ConservationRange.entries) {
            val initial=start(c);val moved=move(initial);val wrong=Conservation.reduce(Conservation.reduce(moved,Conservation.Action.Choose(QuantityRelation.entries.first{it!=moved.task.relation})),Conservation.Action.Check)
            val solved=Conservation.reduce(Conservation.reduce(wrong,Conservation.Action.Choose(wrong.task.relation)),Conservation.Action.Check)
            for(s in listOf(initial,moved,wrong,solved)) {
                val translated=Conservation.reduce(s,Conservation.Action.Language(ContentLanguage.GERMAN));assertEquals(s.mask,translated.mask);assertEquals(s.tasks,translated.tasks)
                val bytes=ConservationHost.encode(translated,emptyList());assertEquals(translated,ConservationHost.decode(bytes).first)
                val bad=bytes.copyOf();bad[20]=(bad[20].toInt()xor 1).toByte()
                for(b in listOf(bad,bytes+byteArrayOf(1),bytes.copyOf(bytes.size-1),ByteArray(32001)))assertTrue(runCatching{ConservationHost.decode(b)}.isFailure)
            }
            assertTrue(runCatching{initial.copy(mask=1 shl c.slots)}.isFailure)
            assertTrue(runCatching{initial.copy(mask=initial.mask xor 1)}.isFailure)
        }
    }
    @Test fun uncertainProgressAndCompletionRecoverThroughDeduplication() {
        val d=disk();val actual=progress();var fail=true
        val flaky=object:ProgressRepository {
            override fun read(query:ProgressQuery)=actual.read(query)
            override fun append(event:ProgressEvent):ProgressWriteResult {val result=actual.append(event);return if(fail)ProgressWriteResult.Failed(ProgressFailure.IO)else result}
        }
        var h=ConservationHost(d,flaky,ConservationRange.FIVE);h.open(SessionId("pending"),RoundLength.FIVE,5,ContentLanguage.ENGLISH);solve(h)
        assertTrue(h.failure&&h.hasPending);val saved=h.state;h.dispatch(Conservation.Action.Next);assertEquals(saved,h.state)
        fail=false;h=ConservationHost(d,flaky,ConservationRange.FIVE);h.open(SessionId("ignored"),RoundLength.TEN,1,ContentLanguage.GERMAN)
        assertEquals(saved,h.state);assertFalse(h.failure||h.hasPending);assertEquals(1,(actual.read()as ProgressReadResult.Events).records.size)
        h.dispatch(Conservation.Action.Next);repeat(4){solve(h);if(it==3)fail=true;h.dispatch(Conservation.Action.Next)}
        assertTrue(h.state!!.completed&&!h.state!!.acknowledged);assertTrue(runCatching{h.again(SessionId("early"),RoundLength.FIVE,1,ContentLanguage.ENGLISH)}.isFailure)
        h=ConservationHost(d,actual,ConservationRange.FIVE);h.open(SessionId("ignored"),RoundLength.FIVE,1,ContentLanguage.ENGLISH)
        assertTrue(h.state!!.acknowledged);assertEquals(1,(actual.read()as ProgressReadResult.Events).records.count{it.event is CompletionEvent})
    }
    @Test fun corruptOrWrongRangeJournalIsNeverOverwritten() {
        val d=disk();val p=progress();val h=ConservationHost(d,p,ConservationRange.FIVE);h.open(SessionId("keep"),RoundLength.FIVE,5,ContentLanguage.ENGLISH)
        val bytes=d.access{it.read()}!!
        assertTrue(runCatching{ConservationHost(d,p,ConservationRange.TEN).open(SessionId("wrong"),RoundLength.TEN,1,ContentLanguage.ENGLISH)}.isFailure)
        assertArrayEquals(bytes,d.access{it.read()})
        val bad=bytes.copyOf();bad[20]=(bad[20].toInt()xor 1).toByte();d.access{it.replace(bad)}
        assertTrue(runCatching{h.retryWrites()}.isFailure);assertArrayEquals(bad,d.access{it.read()})
    }
    @Test fun uncertainCheckpointRecoversSelectionAndMoveHistoryWithoutProgress() {
        val actual=disk();var fail=false
        val uncertain=object:ProgressStorage {
            override fun <T> access(block:(ProgressTransaction)->T):T=actual.access{tx->block(object:ProgressTransaction {
                override fun read()=tx.read()
                override fun replace(bytes:ByteArray){tx.replace(bytes);if(fail)throw java.io.IOException("accepted then interrupted")}
            })}
        }
        val p=progress();val h=ConservationHost(uncertain,p,ConservationRange.FIVE);h.open(SessionId("checkpoint"),RoundLength.FIVE,7,ContentLanguage.ENGLISH)
        val from=(0 until 10).first{h.state!!.mask and(1 shl it)!=0};fail=true
        assertTrue(runCatching{h.dispatch(Conservation.Action.Tap(from))}.isFailure)
        fail=false;h.retryWrites();assertEquals(from,h.state!!.selected);assertEquals(0,h.state!!.current.attempts);assertTrue((p.read()as ProgressReadResult.Events).records.isEmpty())
    }
    @Test fun sharedAudioModesAndHiddenOwnersRespectCurrentPhase() {
        for(mode in AudioMode.entries)for(lang in ContentLanguage.entries) {
            val engine=FakeSpeechEngine();val audio=ConservationAudio(DefaultAudioController(engine,mode));audio.visible(true);val s=start().copy(language=lang)
            audio.speak(s,Conservation.prompt(s.task,s.config),SpeechKind.QUESTION)
            audio.speak(s,Conservation.feedback(s),SpeechKind.FEEDBACK)
            audio.speak(s,Conservation.hint,SpeechKind.EXPLANATION,true)
            audio.speak(s,Conservation.complete,SpeechKind.COMPLETION)
            audio.speak(s,Conservation.complete,SpeechKind.COMPLETION,true)
            assertEquals(when(mode){AudioMode.ALL->5;AudioMode.QUESTIONS->3;AudioMode.OFF->0},engine.spoken.size)
            val count=engine.spoken.size;audio.visible(false);audio.speak(s,Conservation.complete,SpeechKind.COMPLETION,true);assertEquals(count,engine.spoken.size)
            val solved=Conservation.reduce(Conservation.reduce(s,Conservation.Action.Choose(s.task.relation)),Conservation.Action.Check)
            assertEquals(Conservation.feedback(solved),ConservationAudio.current(solved));audio.close()
        }
    }
}
