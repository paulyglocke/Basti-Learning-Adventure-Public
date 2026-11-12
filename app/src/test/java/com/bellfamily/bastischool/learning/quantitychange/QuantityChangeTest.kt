package com.bellfamily.bastischool.learning.quantitychange

import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.session.*
import com.bellfamily.bastischool.learning.progress.*
import com.bellfamily.bastischool.audio.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class QuantityChangeTest {
    @get:Rule val temp=TemporaryFolder()
    private fun disk()=AtomicProgressStorage(temp.newFolder(),JvmAtomicCommit)
    private fun progress()=FileProgressRepository(disk(),ProgressClock{1})
    private fun start(c:ChangeConfig=ChangeConfig.all.first(),r:RoundLength=RoundLength.FIVE)=QuantityChange.start(SessionId("quantity-change"),c,r,1,ContentLanguage.ENGLISH)
    private fun fill(s:QuantityChange.State):QuantityChange.State {
        var next=s;val desired=(1 shl s.answer)-1
        for(slot in 0 until s.config.range.limit)if((next.mask xor desired)and(1 shl slot)!=0)next=QuantityChange.reduce(next,QuantityChange.Action.Toggle(slot))
        return next
    }
    private fun solve(host:QuantityChangeHost) {
        val desired=(1 shl host.state!!.answer)-1
        for(slot in 0 until host.state!!.config.range.limit)if((host.state!!.mask xor desired)and(1 shl slot)!=0)host.dispatch(QuantityChange.Action.Toggle(slot))
        host.dispatch(QuantityChange.Action.Check)
    }
    @Test fun seededRoundsBalanceStartingAmountsAndVaryCanonicalAnimalsWithoutIdenticalQuestions() {
        for(c in ChangeConfig.all)for(r in RoundLength.entries)for(seed in 0L..200) {
            val plan=QuantityChange.generate(c,r,seed)
            assertEquals(plan,QuantityChange.generate(c,r,seed));assertEquals(r.count,plan.distinct().size)
            assertTrue(plan.zipWithNext().all{(a,b)->a.start!=b.start&&a.animal!=b.animal})
            val counts=c.starts.map{n->plan.count{it.start==n}};assertTrue(counts.max()-counts.min()<=1)
            assertTrue(plan.all{it.animal in QuantityChange.animals&&QuantityChange.target(it,c) in 0..c.range.limit})
            val again=QuantityChange.generate(c,r,seed,plan)
            assertNotEquals(plan,again);assertNotEquals(plan.last().start,again.first().start);assertNotEquals(plan.last().animal,again.first().animal)
        }
    }
    @Test fun fullCyclesIncludeZeroToOneAndOneToZeroAndBothTenBoundaries() {
        for(c in ChangeConfig.all) {
            val s=start(c,RoundLength.TEN)
            assertEquals(c.starts.toSet(),s.tasks.map{it.start}.toSet())
            assertEquals(if(c.mode==ChangeMode.MORE)1..c.range.limit else 0 until c.range.limit,s.tasks.map{QuantityChange.target(it,c)}.toSet().let{it.min()..it.max()})
            s.tasks.forEach { task->
                for(lang in ContentLanguage.entries){assertTrue(QuantityChange.prompt(task,c).speech[lang].isNotBlank());assertTrue(QuantityChange.label(task.animal).display[lang].isNotBlank())}
            }
        }
        val c=ChangeConfig(ChangeMode.LESS,ChangeRange.FIVE)
        val s=(0L..100).map{QuantityChange.start(SessionId("zero"),c,RoundLength.FIVE,it,ContentLanguage.GERMAN)}.first{it.task.start==1}
        val solved=QuantityChange.reduce(QuantityChange.reduce(s,QuantityChange.Action.Toggle(0)),QuantityChange.Action.Check)
        assertEquals(0,solved.count);assertTrue(solved.current.solved)
        assertEquals(ContentId("number.zero"),QuantityChange.attempt(solved).choice)
        assertTrue(QuantityChange.feedback(solved).speech.de.contains("null"))
    }
    @Test fun editsAndMultiStepUndoPreserveInitialReferenceAndCreateNoAttempts() {
        val initial=start();var s=initial
        repeat(102){s=QuantityChange.reduce(s,QuantityChange.Action.Toggle(it%5))}
        assertEquals(100,s.undoMasks.size);assertEquals(initial.tasks,s.tasks);assertEquals(initial.task,s.task)
        assertEquals(0,s.current.attempts)
        val last=s.undoMasks.last();s=QuantityChange.reduce(s,QuantityChange.Action.Undo);assertEquals(last,s.mask)
        assertEquals(99,s.undoMasks.size)
        assertSame(s,QuantityChange.reduce(s,QuantityChange.Action.Toggle(-1)))
        assertSame(s,QuantityChange.reduce(s,QuantityChange.Action.Toggle(5)))
        assertEquals(initial.tasks,s.tasks)
    }
    @Test fun changedCheckedCountsRecordEvidenceAndSolvedWorkLocksUntilNext() {
        var s=start();s=QuantityChange.reduce(s,QuantityChange.Action.Check)
        assertTrue(s.needsCorrection);assertEquals(1,s.current.attempts)
        assertEquals(AttemptOutcome.INCORRECT,QuantityChange.attempt(s).outcome)
        assertSame(s,QuantityChange.reduce(s,QuantityChange.Action.Check))
        val before=s.mask
        s=QuantityChange.reduce(s,QuantityChange.Action.Help);s=QuantityChange.reduce(s,QuantityChange.Action.Replay)
        assertEquals(before,s.mask);assertEquals(1,s.current.attempts)
        s=QuantityChange.reduce(fill(s),QuantityChange.Action.Check)
        assertTrue(s.current.solved);assertEquals(2,s.current.attempts);assertTrue(s.current.support.hint);assertEquals(1,s.current.support.replays)
        assertEquals(AttemptOutcome.CORRECT,QuantityChange.attempt(s).outcome)
        for(a in listOf(QuantityChange.Action.Toggle(0),QuantityChange.Action.Undo,QuantityChange.Action.Check))assertSame(s,QuantityChange.reduce(s,a))
        val next=QuantityChange.reduce(s,QuantityChange.Action.Next)
        assertEquals(1,next.index);assertEquals(QuantityChange.initialMask(next.task),next.mask);assertTrue(next.undoMasks.isEmpty())
    }
    @Test fun movingAnAnimalToAnotherSpaceCannotResubmitTheSameWrongCount() {
        var s=(0L..100).map{QuantityChange.start(SessionId("move"),ChangeConfig.all.first(),RoundLength.FIVE,it,ContentLanguage.ENGLISH)}.first{it.task.start==2}
        s=QuantityChange.reduce(s,QuantityChange.Action.Check)
        s=QuantityChange.reduce(s,QuantityChange.Action.Toggle(0));s=QuantityChange.reduce(s,QuantityChange.Action.Toggle(4))
        assertEquals(2,s.count);assertTrue(s.needsCorrection);assertSame(s,QuantityChange.reduce(s,QuantityChange.Action.Check))
        assertEquals(1,s.current.attempts)
    }
    @Test fun helpAndLanguageKeepTheExactTasksEditsUndoAndLock() {
        var s=start();s=QuantityChange.reduce(s,QuantityChange.Action.Toggle(0));val before=s
        s=QuantityChange.reduce(s,QuantityChange.Action.Help);s=QuantityChange.reduce(s,QuantityChange.Action.Language(ContentLanguage.GERMAN))
        assertEquals(before.tasks,s.tasks);assertEquals(before.mask,s.mask);assertEquals(before.undoMasks,s.undoMasks)
        assertEquals(0,s.current.attempts);assertTrue(s.current.support.hint)
        s=QuantityChange.reduce(fill(s),QuantityChange.Action.Check)
        val translated=QuantityChange.reduce(s,QuantityChange.Action.Language(ContentLanguage.ENGLISH))
        assertTrue(translated.current.solved);assertEquals(s.mask,translated.mask)
    }
    @Test fun allConfigurationsAndRoundLengthsRestoreAndDeduplicateSharedProgressAndCompletion() {
        for(c in ChangeConfig.all)for(r in RoundLength.entries) {
            val disk=disk();val progress=progress();var host=QuantityChangeHost(disk,progress,c)
            assertTrue(host.open(SessionId("${c.key}-${r.name}"),r,9,ContentLanguage.GERMAN))
            host.dispatch(QuantityChange.Action.Toggle(0));host.dispatch(QuantityChange.Action.Help)
            val edited=host.state!!;assertTrue((progress.read()as ProgressReadResult.Events).records.isEmpty())
            host=QuantityChangeHost(disk,progress,c);assertFalse(host.open(SessionId("ignored"),RoundLength.FIVE,1,ContentLanguage.ENGLISH));assertEquals(edited,host.state)
            repeat(r.count){solve(host);host.dispatch(QuantityChange.Action.Next)}
            val complete=host.state!!;assertTrue(complete.completed&&complete.acknowledged)
            host=QuantityChangeHost(disk,progress,c);host.open(SessionId("ignored-again"),r,99,ContentLanguage.ENGLISH);assertEquals(complete,host.state)
            val records=(progress.read()as ProgressReadResult.Events).records
            assertEquals(r.count,records.count{it.event is AttemptEvent});assertEquals(1,records.count{it.event is CompletionEvent})
            val event=records.map{it.event}.filterIsInstance<CompletionEvent>().single()
            assertTrue(event.tasks.first().support.hint);assertTrue(event.tasks.all{it.evidence.skill==QuantityChange.skill(c)})
            assertEquals(records,ProgressCodec.decode(ProgressCodec.encode(records)))
            host.again(SessionId("again-${c.key}-${r.name}"),r,9,ContentLanguage.GERMAN);assertNotEquals(complete.tasks,host.state!!.tasks)
        }
    }
    @Test fun editedWrongSolvedAndUndoHistoryRoundTripAndCorruptBytesAreRejected() {
        for(c in ChangeConfig.all) {
            val initial=start(c);val partial=QuantityChange.reduce(initial,QuantityChange.Action.Toggle(0))
            val wrong=QuantityChange.reduce(initial,QuantityChange.Action.Check);val solved=QuantityChange.reduce(fill(initial),QuantityChange.Action.Check)
            for(s in listOf(initial,partial,wrong,solved)) {
                val bytes=QuantityChangeHost.encode(s,emptyList());assertTrue(bytes.size<32000);assertEquals(s,QuantityChangeHost.decode(bytes).first)
                val broken=bytes.copyOf();broken[20]=(broken[20].toInt() xor 1).toByte()
                for(bad in listOf(broken,bytes+byteArrayOf(1),bytes.copyOf(bytes.size-1)))assertTrue(runCatching{QuantityChangeHost.decode(bad)}.isFailure)
            }
        }
    }
    @Test fun wrongConfigurationAndCorruptCheckpointNeverOverwriteSavedWork() {
        val disk=disk();val progress=progress();val host=QuantityChangeHost(disk,progress,ChangeConfig.all.first())
        host.open(SessionId("keep"),RoundLength.TEN,5,ContentLanguage.ENGLISH);val bytes=disk.access{it.read()}!!
        assertTrue(runCatching{QuantityChangeHost(disk,progress,ChangeConfig.all.last()).open(SessionId("wrong"),RoundLength.FIVE,1,ContentLanguage.ENGLISH)}.isFailure)
        assertArrayEquals(bytes,disk.access{it.read()})
        val broken=bytes.copyOf();broken[20]=(broken[20].toInt() xor 1).toByte();disk.access{it.replace(broken)}
        assertTrue(runCatching{QuantityChangeHost(disk,progress,ChangeConfig.all.first()).open(SessionId("bad"),RoundLength.FIVE,1,ContentLanguage.ENGLISH)}.isFailure)
        assertArrayEquals(broken,disk.access{it.read()})
    }
    @Test fun uncertainProgressAcknowledgementRetainsOneCheckedAttempt() {
        val disk=disk();val actual=progress();var fail=true
        val flaky=object:ProgressRepository {
            override fun read(query:ProgressQuery)=actual.read(query)
            override fun append(event:ProgressEvent):ProgressWriteResult {val result=actual.append(event);return if(fail)ProgressWriteResult.Failed(ProgressFailure.IO)else result}
        }
        var host=QuantityChangeHost(disk,flaky,ChangeConfig.all.first());host.open(SessionId("pending"),RoundLength.FIVE,5,ContentLanguage.ENGLISH)
        solve(host);assertTrue(host.failure&&host.hasPending);val saved=host.state
        host.dispatch(QuantityChange.Action.Next);assertEquals(saved,host.state)
        fail=false;host=QuantityChangeHost(disk,flaky,ChangeConfig.all.first());host.open(SessionId("ignored"),RoundLength.TEN,1,ContentLanguage.GERMAN)
        assertEquals(saved,host.state);assertFalse(host.failure||host.hasPending);assertEquals(1,(actual.read()as ProgressReadResult.Events).records.size)
    }
    @Test fun uncertainCheckpointCommitRecoversEditsAndUndoWithoutProgress() {
        val actual=disk();var fail=false
        val uncertain=object:ProgressStorage {
            override fun <T> access(block:(ProgressTransaction)->T):T=actual.access{tx->block(object:ProgressTransaction {
                override fun read()=tx.read()
                override fun replace(bytes:ByteArray){tx.replace(bytes);if(fail)throw java.io.IOException("accepted then interrupted")}
            })}
        }
        val progress=progress();val host=QuantityChangeHost(uncertain,progress,ChangeConfig.all.first());host.open(SessionId("checkpoint"),RoundLength.FIVE,5,ContentLanguage.ENGLISH)
        val original=host.state!!.mask;fail=true;assertTrue(runCatching{host.dispatch(QuantityChange.Action.Toggle(0))}.isFailure)
        fail=false;host.retryWrites();assertEquals(original xor 1,host.state!!.mask);assertEquals(listOf(original),host.state!!.undoMasks)
        assertEquals(0,host.state!!.current.attempts);assertTrue((progress.read()as ProgressReadResult.Events).records.isEmpty())
    }
    @Test fun acceptedCompletionRecoversExactlyOnceBeforeAnotherRound() {
        val disk=disk();val actual=progress();val config=ChangeConfig.all.last()
        val flaky=object:ProgressRepository {
            override fun read(query:ProgressQuery)=actual.read(query)
            override fun append(event:ProgressEvent):ProgressWriteResult {val result=actual.append(event);return if(event is CompletionEvent)ProgressWriteResult.Failed(ProgressFailure.IO)else result}
        }
        var host=QuantityChangeHost(disk,flaky,config);host.open(SessionId("completion"),RoundLength.FIVE,5,ContentLanguage.ENGLISH)
        repeat(5){solve(host);host.dispatch(QuantityChange.Action.Next)}
        assertTrue(host.state!!.completed);assertFalse(host.state!!.acknowledged)
        assertTrue(runCatching{host.again(SessionId("early"),RoundLength.FIVE,5,ContentLanguage.ENGLISH)}.isFailure)
        host=QuantityChangeHost(disk,actual,config);host.open(SessionId("ignored"),RoundLength.FIVE,6,ContentLanguage.GERMAN)
        assertTrue(host.state!!.acknowledged);assertEquals(1,(actual.read()as ProgressReadResult.Events).records.count{it.event is CompletionEvent})
    }
    @Test fun bilingualAudioUsesSharedModesManualSupportAndCurrentPhaseReplay() {
        for(mode in AudioMode.entries)for(lang in ContentLanguage.entries) {
            val engine=FakeSpeechEngine();val audio=QuantityChangeAudio(DefaultAudioController(engine,mode));audio.visible(true)
            val s=start().copy(language=lang)
            audio.speak(s,QuantityChange.prompt(s.task,s.config),SpeechKind.QUESTION)
            audio.speak(s,QuantityChange.feedback(s),SpeechKind.FEEDBACK)
            audio.speak(s,QuantityChange.hint(s.config),SpeechKind.EXPLANATION,true)
            audio.speak(s,QuantityChange.complete,SpeechKind.COMPLETION)
            audio.speak(s,QuantityChange.complete,SpeechKind.COMPLETION,true)
            assertEquals(when(mode){AudioMode.ALL->5;AudioMode.QUESTIONS->3;AudioMode.OFF->0},engine.spoken.size)
            if(engine.spoken.isNotEmpty()){assertEquals(lang,engine.spoken.last().context.language);assertEquals(QuantityChange.complete.speech[lang],engine.spoken.last().text)}
            val count=engine.spoken.size;audio.visible(false);audio.speak(s,QuantityChange.prompt(s.task,s.config),SpeechKind.QUESTION,true);assertEquals(count,engine.spoken.size)
            val solved=QuantityChange.reduce(fill(s),QuantityChange.Action.Check)
            assertEquals(QuantityChange.feedback(solved),QuantityChangeAudio.current(solved))
            audio.close()
        }
    }
}
