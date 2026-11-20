package com.bellfamily.bastischool.learning.numberstories

import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.session.*
import com.bellfamily.bastischool.learning.progress.*
import com.bellfamily.bastischool.audio.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class NumberStoriesTest {
    @get:Rule val temp=TemporaryFolder()
    private fun disk()=AtomicProgressStorage(temp.newFolder(),JvmAtomicCommit)
    private fun progress()=FileProgressRepository(disk(),ProgressClock{1})
    private fun start(c:StoryConfig=StoryConfig.all.first(),r:RoundLength=RoundLength.FIVE)=NumberStories.start(SessionId("number-stories"),c,r,1,ContentLanguage.ENGLISH)
    private fun fill(s:NumberStories.State):NumberStories.State {
        var next=s;val desired=(1 shl s.answer)-1
        for(slot in 0 until s.config.range.limit)if((next.mask xor desired)and(1 shl slot)!=0)next=NumberStories.reduce(next,NumberStories.Action.Toggle(slot))
        return next
    }
    private fun solve(host:NumberStoriesHost) {
        val desired=(1 shl host.state!!.answer)-1
        for(slot in 0 until host.state!!.config.range.limit)if((host.state!!.mask xor desired)and(1 shl slot)!=0)host.dispatch(NumberStories.Action.Toggle(slot))
        host.dispatch(NumberStories.Action.Check)
    }
    @Test fun seededRoundsUseUniqueFactsAcrossBothSettingsAndAvoidBoundaryRepetition() {
        for(c in StoryConfig.all)for(r in RoundLength.entries)for(seed in 0L..200) {
            val plan=NumberStories.generate(c,r,seed)
            assertEquals(plan,NumberStories.generate(c,r,seed));assertEquals(r.count,plan.map{it.start to it.amount}.distinct().size)
            assertEquals(StoryScene.entries.toSet(),plan.map{it.scene}.toSet())
            assertTrue(plan.zipWithNext().all{(a,b)->NumberStories.target(a,c)!=NumberStories.target(b,c)&&a.scene!=b.scene})
            assertTrue(plan.count{it.amount>1}>=r.count/2)
            assertTrue(plan.all{NumberStories.valid(it,c)})
            assertTrue(plan.any{NumberStories.target(it,c)==if(c.mode==StoryMode.ADD)c.range.limit else 0})
            val again=NumberStories.generate(c,r,seed,plan)
            assertNotEquals(plan,again);assertNotEquals(plan.last().scene,again.first().scene)
            assertNotEquals(plan.last().let{it.start to it.amount},again.first().let{it.start to it.amount})
            assertNotEquals(NumberStories.target(plan.last(),c),NumberStories.target(again.first(),c))
        }
    }
    @Test fun bilingualStoriesDescribeEachSceneAndChangeIncludingZeroWithoutAnnouncingTheAnswer() {
        for(c in StoryConfig.all)for(scene in StoryScene.entries)for(n in 1..c.range.limit)for(amount in 1..5) {
            val t=StoryTask(n,amount,scene);if(!NumberStories.valid(t,c))continue
            val text=NumberStories.prompt(t,c)
            for(lang in ContentLanguage.entries) {
                assertTrue(text.speech[lang].contains("$n"));assertTrue(text.speech[lang].contains("$amount"))
                assertTrue(text.display[lang].isNotBlank());assertFalse(text.speech[lang].contains("="))
            }
            assertTrue(text.speech.en.contains(if(scene==StoryScene.POND)"pond"else"blanket"))
            assertTrue(text.speech.de.lowercase().contains(if(scene==StoryScene.POND)"teich"else"decke"))
        }
        val c=StoryConfig(StoryMode.TAKE_AWAY,StoryRange.TEN)
        var s=NumberStories.start(SessionId("zero"),c,RoundLength.TEN,4,ContentLanguage.GERMAN)
        while(s.answer!=0) {s=NumberStories.reduce(fill(s),NumberStories.Action.Check);s=NumberStories.reduce(s,NumberStories.Action.Next)}
        val solved=NumberStories.reduce(fill(s),NumberStories.Action.Check)
        assertEquals(0,solved.count);assertTrue(solved.current.solved)
        assertEquals(ContentId("number.zero"),NumberStories.attempt(solved).choice)
        assertTrue(NumberStories.feedback(solved).speech.de.contains("null"))
    }
    @Test fun editsAndMultiStepUndoPreserveInitialReferenceAndCreateNoAttempts() {
        val initial=start();var s=initial
        repeat(102){s=NumberStories.reduce(s,NumberStories.Action.Toggle(it%5))}
        assertEquals(100,s.undoMasks.size);assertEquals(initial.tasks,s.tasks);assertEquals(initial.task,s.task)
        assertEquals(0,s.current.attempts)
        val last=s.undoMasks.last();s=NumberStories.reduce(s,NumberStories.Action.Undo);assertEquals(last,s.mask)
        assertEquals(99,s.undoMasks.size)
        assertSame(s,NumberStories.reduce(s,NumberStories.Action.Toggle(-1)))
        assertSame(s,NumberStories.reduce(s,NumberStories.Action.Toggle(5)))
        assertEquals(initial.tasks,s.tasks)
    }
    @Test fun changedCheckedCountsRecordEvidenceAndSolvedWorkLocksUntilNext() {
        var s=start();s=NumberStories.reduce(s,NumberStories.Action.Check)
        assertTrue(s.needsCorrection);assertEquals(1,s.current.attempts)
        assertEquals(AttemptOutcome.INCORRECT,NumberStories.attempt(s).outcome)
        assertSame(s,NumberStories.reduce(s,NumberStories.Action.Check))
        val before=s.mask
        s=NumberStories.reduce(s,NumberStories.Action.Help);s=NumberStories.reduce(s,NumberStories.Action.Replay)
        assertEquals(before,s.mask);assertEquals(1,s.current.attempts)
        s=NumberStories.reduce(fill(s),NumberStories.Action.Check)
        assertTrue(s.current.solved);assertEquals(2,s.current.attempts);assertTrue(s.current.support.hint);assertEquals(1,s.current.support.replays)
        assertEquals(AttemptOutcome.CORRECT,NumberStories.attempt(s).outcome)
        for(a in listOf(NumberStories.Action.Toggle(0),NumberStories.Action.Undo,NumberStories.Action.Check))assertSame(s,NumberStories.reduce(s,a))
        val next=NumberStories.reduce(s,NumberStories.Action.Next)
        assertEquals(1,next.index);assertEquals(NumberStories.initialMask(next.task),next.mask);assertTrue(next.undoMasks.isEmpty())
    }
    @Test fun movingAnAnimalToAnotherSpaceCannotResubmitTheSameWrongCount() {
        var s=(0L..100).map{NumberStories.start(SessionId("move"),StoryConfig.all.first(),RoundLength.FIVE,it,ContentLanguage.ENGLISH)}.first{it.task.start==2}
        s=NumberStories.reduce(s,NumberStories.Action.Check)
        s=NumberStories.reduce(s,NumberStories.Action.Toggle(0));s=NumberStories.reduce(s,NumberStories.Action.Toggle(4))
        assertEquals(2,s.count);assertTrue(s.needsCorrection);assertSame(s,NumberStories.reduce(s,NumberStories.Action.Check))
        assertEquals(1,s.current.attempts)
    }
    @Test fun helpAndLanguageKeepTheExactTasksEditsUndoAndLock() {
        var s=start();s=NumberStories.reduce(s,NumberStories.Action.Toggle(0));val before=s
        s=NumberStories.reduce(s,NumberStories.Action.Help);s=NumberStories.reduce(s,NumberStories.Action.Language(ContentLanguage.GERMAN))
        assertEquals(before.tasks,s.tasks);assertEquals(before.mask,s.mask);assertEquals(before.undoMasks,s.undoMasks)
        assertEquals(0,s.current.attempts);assertTrue(s.current.support.hint)
        s=NumberStories.reduce(fill(s),NumberStories.Action.Check)
        val translated=NumberStories.reduce(s,NumberStories.Action.Language(ContentLanguage.ENGLISH))
        assertTrue(translated.current.solved);assertEquals(s.mask,translated.mask)
    }
    @Test fun allConfigurationsAndRoundLengthsRestoreAndDeduplicateSharedProgressAndCompletion() {
        for(c in StoryConfig.all)for(r in RoundLength.entries) {
            val disk=disk();val progress=progress();var host=NumberStoriesHost(disk,progress,c)
            assertTrue(host.open(SessionId("${c.key}-${r.name}"),r,9,ContentLanguage.GERMAN))
            host.dispatch(NumberStories.Action.Toggle(0));host.dispatch(NumberStories.Action.Help)
            val edited=host.state!!;assertTrue((progress.read()as ProgressReadResult.Events).records.isEmpty())
            host=NumberStoriesHost(disk,progress,c);assertFalse(host.open(SessionId("ignored"),RoundLength.FIVE,1,ContentLanguage.ENGLISH));assertEquals(edited,host.state)
            repeat(r.count){solve(host);host.dispatch(NumberStories.Action.Next)}
            val complete=host.state!!;assertTrue(complete.completed&&complete.acknowledged)
            host=NumberStoriesHost(disk,progress,c);host.open(SessionId("ignored-again"),r,99,ContentLanguage.ENGLISH);assertEquals(complete,host.state)
            val records=(progress.read()as ProgressReadResult.Events).records
            assertEquals(r.count,records.count{it.event is AttemptEvent});assertEquals(1,records.count{it.event is CompletionEvent})
            val event=records.map{it.event}.filterIsInstance<CompletionEvent>().single()
            assertTrue(event.tasks.first().support.hint);assertTrue(event.tasks.all{it.evidence.skill==NumberStories.skill(c)})
            assertEquals(records,ProgressCodec.decode(ProgressCodec.encode(records)))
            host.again(SessionId("again-${c.key}-${r.name}"),r,9,ContentLanguage.GERMAN);assertNotEquals(complete.tasks,host.state!!.tasks)
        }
    }
    @Test fun editedWrongSolvedAndUndoHistoryRoundTripAndCorruptBytesAreRejected() {
        for(c in StoryConfig.all) {
            val initial=start(c);val partial=NumberStories.reduce(initial,NumberStories.Action.Toggle(0))
            val wrong=NumberStories.reduce(initial,NumberStories.Action.Check);val solved=NumberStories.reduce(fill(initial),NumberStories.Action.Check)
            for(s in listOf(initial,partial,wrong,solved)) {
                val bytes=NumberStoriesHost.encode(s,emptyList());assertTrue(bytes.size<32000);assertEquals(s,NumberStoriesHost.decode(bytes).first)
                val broken=bytes.copyOf();broken[20]=(broken[20].toInt() xor 1).toByte()
                for(bad in listOf(broken,bytes+byteArrayOf(1),bytes.copyOf(bytes.size-1)))assertTrue(runCatching{NumberStoriesHost.decode(bad)}.isFailure)
            }
        }
    }
    @Test fun wrongConfigurationAndCorruptCheckpointNeverOverwriteSavedWork() {
        val disk=disk();val progress=progress();val host=NumberStoriesHost(disk,progress,StoryConfig.all.first())
        host.open(SessionId("keep"),RoundLength.TEN,5,ContentLanguage.ENGLISH);val bytes=disk.access{it.read()}!!
        assertTrue(runCatching{NumberStoriesHost(disk,progress,StoryConfig.all.last()).open(SessionId("wrong"),RoundLength.FIVE,1,ContentLanguage.ENGLISH)}.isFailure)
        assertArrayEquals(bytes,disk.access{it.read()})
        val broken=bytes.copyOf();broken[20]=(broken[20].toInt() xor 1).toByte();disk.access{it.replace(broken)}
        assertTrue(runCatching{NumberStoriesHost(disk,progress,StoryConfig.all.first()).open(SessionId("bad"),RoundLength.FIVE,1,ContentLanguage.ENGLISH)}.isFailure)
        assertArrayEquals(broken,disk.access{it.read()})
    }
    @Test fun uncertainProgressAcknowledgementRetainsOneCheckedAttempt() {
        val disk=disk();val actual=progress();var fail=true
        val flaky=object:ProgressRepository {
            override fun read(query:ProgressQuery)=actual.read(query)
            override fun append(event:ProgressEvent):ProgressWriteResult {val result=actual.append(event);return if(fail)ProgressWriteResult.Failed(ProgressFailure.IO)else result}
        }
        var host=NumberStoriesHost(disk,flaky,StoryConfig.all.first());host.open(SessionId("pending"),RoundLength.FIVE,5,ContentLanguage.ENGLISH)
        solve(host);assertTrue(host.failure&&host.hasPending);val saved=host.state
        host.dispatch(NumberStories.Action.Next);assertEquals(saved,host.state)
        fail=false;host=NumberStoriesHost(disk,flaky,StoryConfig.all.first());host.open(SessionId("ignored"),RoundLength.TEN,1,ContentLanguage.GERMAN)
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
        val progress=progress();val host=NumberStoriesHost(uncertain,progress,StoryConfig.all.first());host.open(SessionId("checkpoint"),RoundLength.FIVE,5,ContentLanguage.ENGLISH)
        val original=host.state!!.mask;fail=true;assertTrue(runCatching{host.dispatch(NumberStories.Action.Toggle(0))}.isFailure)
        fail=false;host.retryWrites();assertEquals(original xor 1,host.state!!.mask);assertEquals(listOf(original),host.state!!.undoMasks)
        assertEquals(0,host.state!!.current.attempts);assertTrue((progress.read()as ProgressReadResult.Events).records.isEmpty())
    }
    @Test fun acceptedCompletionRecoversExactlyOnceBeforeAnotherRound() {
        val disk=disk();val actual=progress();val config=StoryConfig.all.last()
        val flaky=object:ProgressRepository {
            override fun read(query:ProgressQuery)=actual.read(query)
            override fun append(event:ProgressEvent):ProgressWriteResult {val result=actual.append(event);return if(event is CompletionEvent)ProgressWriteResult.Failed(ProgressFailure.IO)else result}
        }
        var host=NumberStoriesHost(disk,flaky,config);host.open(SessionId("completion"),RoundLength.FIVE,5,ContentLanguage.ENGLISH)
        repeat(5){solve(host);host.dispatch(NumberStories.Action.Next)}
        assertTrue(host.state!!.completed);assertFalse(host.state!!.acknowledged)
        assertTrue(runCatching{host.again(SessionId("early"),RoundLength.FIVE,5,ContentLanguage.ENGLISH)}.isFailure)
        host=NumberStoriesHost(disk,actual,config);host.open(SessionId("ignored"),RoundLength.FIVE,6,ContentLanguage.GERMAN)
        assertTrue(host.state!!.acknowledged);assertEquals(1,(actual.read()as ProgressReadResult.Events).records.count{it.event is CompletionEvent})
    }
    @Test fun bilingualAudioUsesSharedModesManualSupportAndCurrentPhaseReplay() {
        for(mode in AudioMode.entries)for(lang in ContentLanguage.entries) {
            val engine=FakeSpeechEngine();val audio=NumberStoriesAudio(DefaultAudioController(engine,mode));audio.visible(true)
            val s=start().copy(language=lang)
            audio.speak(s,NumberStories.prompt(s.task,s.config),SpeechKind.QUESTION)
            audio.speak(s,NumberStories.feedback(s),SpeechKind.FEEDBACK)
            audio.speak(s,NumberStories.hint(s.task,s.config),SpeechKind.EXPLANATION,true)
            audio.speak(s,NumberStories.complete,SpeechKind.COMPLETION)
            audio.speak(s,NumberStories.complete,SpeechKind.COMPLETION,true)
            assertEquals(when(mode){AudioMode.ALL->5;AudioMode.QUESTIONS->3;AudioMode.OFF->0},engine.spoken.size)
            if(engine.spoken.isNotEmpty()){assertEquals(lang,engine.spoken.last().context.language);assertEquals(NumberStories.complete.speech[lang],engine.spoken.last().text)}
            val count=engine.spoken.size;audio.visible(false);audio.speak(s,NumberStories.prompt(s.task,s.config),SpeechKind.QUESTION,true);assertEquals(count,engine.spoken.size)
            val solved=NumberStories.reduce(fill(s),NumberStories.Action.Check)
            assertEquals(NumberStories.feedback(solved),NumberStoriesAudio.current(solved))
            audio.close()
        }
    }
}
