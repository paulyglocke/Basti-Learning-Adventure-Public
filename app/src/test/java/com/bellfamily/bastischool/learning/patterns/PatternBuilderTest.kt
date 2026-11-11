package com.bellfamily.bastischool.learning.patterns

import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.session.*
import com.bellfamily.bastischool.learning.progress.*
import com.bellfamily.bastischool.audio.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class PatternBuilderTest {
    @get:Rule val temp=TemporaryFolder()
    private fun disk()=AtomicProgressStorage(temp.newFolder(),JvmAtomicCommit)
    private fun progress()=FileProgressRepository(disk(),ProgressClock{1})
    private fun start(level:PatternLevel=PatternLevel.AAB,round:RoundLength=RoundLength.FIVE)=PatternBuilder.start(SessionId("patterns"),level,round,1,ContentLanguage.ENGLISH)
    private fun fill(s:PatternBuilder.State):PatternBuilder.State {
        var next=s
        next.placed.indices.forEach { i->if(next.placed[i]!=next.task.unit[i]) {next=PatternBuilder.reduce(next,PatternBuilder.Action.Remove(i));next=PatternBuilder.reduce(next,PatternBuilder.Action.Add(next.task.unit[i]))} }
        return next
    }
    @Test fun seededPlansBalanceBothContextsAndNeverRepeatQuestionsOrAnimalCombinations() {
        for(level in PatternLevel.entries)for(round in RoundLength.entries)for(seed in 0L..200) {
            val plan=PatternBuilder.generate(level,round,seed)
            assertEquals(plan,PatternBuilder.generate(level,round,seed))
            assertEquals(round.count,plan.map{it.key}.distinct().size)
            assertTrue(plan.zipWithNext().all{(a,b)->a.context!=b.context})
            val animals=plan.filter{it.context==PatternContext.ANIMALS}
            assertEquals(animals.size,animals.map{it.symbols.toSet()}.distinct().size)
            assertTrue(kotlin.math.abs(animals.size-(plan.size-animals.size))<=1)
            plan.forEach { task ->
                assertEquals(level.rule.map{task.symbols[it]},task.unit)
                assertEquals(task.unit+task.unit,task.example)
                assertEquals(task.symbols.toSet(),task.choices.toSet())
                for(language in ContentLanguage.entries){assertTrue(PatternBuilder.prompt(task).speech[language].isNotBlank());assertTrue(PatternBuilder.hint(task).display[language].isNotBlank())}
            }
            val again=PatternBuilder.generate(level,round,seed,plan)
            assertNotEquals(plan,again);assertNotEquals(plan.last().key,again.first().key)
        }
    }
    @Test fun aabRepeatsTheSameSemanticSymbolAndLegacyUniqueSequenceRulesStayStrict() {
        val task=start().task
        assertEquals(task.unit[0],task.unit[1]);assertNotEquals(task.unit[1],task.unit[2])
        assertThrows(IllegalArgumentException::class.java) {
            com.bellfamily.bastischool.learning.sequencing.SequenceAssembly.start(SessionId("old"),task.unit,1,ContentLanguage.ENGLISH)
        }
        assertEquals(3,PatternShape.entries.map{it.id}.distinct().size)
        assertEquals(listOf("Kreis","Quadrat","Dreieck"),PatternShape.entries.map{PatternBuilder.label(it.id).display.de})
    }
    @Test fun placementRemovalUndoAndListenContentNeverFabricateAnAttemptOrChangeTheExample() {
        val s=start();var next=PatternBuilder.reduce(s,PatternBuilder.Action.Add(s.task.unit[0]))
        next=PatternBuilder.reduce(next,PatternBuilder.Action.Add(s.task.unit[1]))
        assertEquals(s.task,next.task);assertEquals(0,next.current.attempts)
        next=PatternBuilder.reduce(next,PatternBuilder.Action.Remove(0));assertNull(next.placed[0]);assertNotNull(next.placed[1])
        next=PatternBuilder.reduce(next,PatternBuilder.Action.Undo);assertTrue(next.placed.all{it==null})
        assertEquals(s.task,next.task);assertEquals(0,next.current.attempts)
        assertSame(next,PatternBuilder.reduce(next,PatternBuilder.Action.Add(ContentId("shape.unknown"))))
        assertSame(next,PatternBuilder.reduce(next,PatternBuilder.Action.Remove(-1)))
        assertSame(next,PatternBuilder.reduce(next,PatternBuilder.Action.Check))
    }
    @Test fun onlyChangedCompleteChecksRecordAttemptsAndSuccessLocksUntilExplicitNext() {
        var s=start();val incorrect=s.task.unit.map { expected->s.task.choices.first{it!=expected} }
        incorrect.forEach{s=PatternBuilder.reduce(s,PatternBuilder.Action.Add(it))}
        assertEquals(0,s.current.attempts)
        s=PatternBuilder.reduce(s,PatternBuilder.Action.Check)
        assertTrue(s.needsCorrection);assertEquals(AttemptOutcome.INCORRECT,PatternBuilder.attempt(s).outcome)
        assertEquals(PatternBuilder.answerId(incorrect),PatternBuilder.attempt(s).choice)
        assertSame(s,PatternBuilder.reduce(s,PatternBuilder.Action.Check))
        s=PatternBuilder.reduce(s,PatternBuilder.Action.Help);s=PatternBuilder.reduce(s,PatternBuilder.Action.Replay)
        for(i in s.placed.indices)s=PatternBuilder.reduce(s,PatternBuilder.Action.Remove(i))
        s=fill(s);s=PatternBuilder.reduce(s,PatternBuilder.Action.Check)
        assertTrue(s.current.solved);assertEquals(2,s.current.attempts);assertTrue(PatternBuilder.attempt(s).support.hint)
        assertEquals(1,PatternBuilder.attempt(s).support.replays)
        assertSame(s,PatternBuilder.reduce(s,PatternBuilder.Action.Remove(0)))
        assertSame(s,PatternBuilder.reduce(s,PatternBuilder.Action.Check))
        val next=PatternBuilder.reduce(s,PatternBuilder.Action.Next);assertEquals(1,next.index);assertTrue(next.placed.all{it==null})
    }
    @Test fun helpAndLanguageRetainTheExactTaskChoicesPlacementsAndAnswerLock() {
        var s=start();s=PatternBuilder.reduce(s,PatternBuilder.Action.Add(s.task.unit[0]))
        val before=s;s=PatternBuilder.reduce(s,PatternBuilder.Action.Help)
        assertEquals(before.placed,s.placed);assertTrue(s.current.support.hint);assertEquals(0,s.current.attempts)
        s=PatternBuilder.reduce(s,PatternBuilder.Action.Language(ContentLanguage.GERMAN))
        assertEquals(before.tasks,s.tasks);assertEquals(before.placed,s.placed);assertEquals(before.task.choices,s.task.choices)
        s=PatternBuilder.reduce(fill(s),PatternBuilder.Action.Check)
        val solved=PatternBuilder.reduce(s,PatternBuilder.Action.Language(ContentLanguage.ENGLISH));assertTrue(solved.current.solved);assertEquals(s.placed,solved.placed)
    }
    @Test fun allLevelsAndRoundLengthsRestoreFrozenPlansAndDeduplicateSharedCompletion() {
        for(level in PatternLevel.entries)for(round in RoundLength.entries) {
            val disk=disk();val progress=progress();var host=PatternHost(disk,progress,level)
            assertTrue(host.open(SessionId("${level.key}-${round.name}"),round,77,ContentLanguage.GERMAN))
            host.dispatch(PatternBuilder.Action.Add(host.state!!.task.unit[0]));host.dispatch(PatternBuilder.Action.Help)
            val partial=host.state!!;assertTrue((progress.read()as ProgressReadResult.Events).records.isEmpty())
            host=PatternHost(disk,progress,level);assertFalse(host.open(SessionId("ignored"),RoundLength.FIVE,99,ContentLanguage.ENGLISH))
            assertEquals(partial,host.state)
            repeat(round.count) {
                val s=host.state!!
                for(i in s.placed.indices)if(s.placed[i]==null)host.dispatch(PatternBuilder.Action.Add(s.task.unit[i]))
                host.dispatch(PatternBuilder.Action.Check);host.dispatch(PatternBuilder.Action.Next)
            }
            assertTrue(host.state!!.completed&&host.state!!.acknowledged)
            val complete=host.state!!
            host=PatternHost(disk,progress,level);host.open(SessionId("ignored-two"),round,3,ContentLanguage.ENGLISH)
            assertEquals(complete,host.state)
            val records=(progress.read()as ProgressReadResult.Events).records
            assertEquals(round.count,records.count{it.event is AttemptEvent});assertEquals(1,records.count{it.event is CompletionEvent})
            val event=records.map{it.event}.filterIsInstance<CompletionEvent>().single()
            assertTrue(event.tasks.first().support.hint);assertEquals(2,event.tasks.map{it.evidence.context}.distinct().size)
            assertTrue(event.tasks.all{it.evidence.skill==PatternBuilder.skill(level)})
            assertEquals(records,ProgressCodec.decode(ProgressCodec.encode(records)))
            host.again(SessionId("${level.key}-${round.name}-again"),round,77,ContentLanguage.GERMAN);assertNotEquals(complete.tasks,host.state!!.tasks)
        }
    }
    @Test fun partialWrongSolvedAndCompletedBoardsHaveExactBoundedJournalRoundTrips() {
        for(level in PatternLevel.entries) {
            val initial=start(level);val partial=PatternBuilder.reduce(initial,PatternBuilder.Action.Add(initial.task.unit[0]))
            var wrong=initial;initial.task.unit.forEach{expected->wrong=PatternBuilder.reduce(wrong,PatternBuilder.Action.Add(initial.task.choices.first{it!=expected}))}
            wrong=PatternBuilder.reduce(wrong,PatternBuilder.Action.Check)
            val correct=PatternBuilder.reduce(fill(initial),PatternBuilder.Action.Check)
            for(s in listOf(initial,partial,wrong,correct)) {
                val bytes=PatternHost.encode(s,emptyList());assertTrue(bytes.size<32000);assertEquals(s,PatternHost.decode(bytes).first)
                val broken=bytes.copyOf();broken[20]=(broken[20].toInt() xor 1).toByte()
                assertTrue(runCatching{PatternHost.decode(broken)}.isFailure)
                assertTrue(runCatching{PatternHost.decode(bytes+byteArrayOf(1))}.isFailure)
                assertTrue(runCatching{PatternHost.decode(bytes.copyOf(bytes.size-1))}.isFailure)
            }
        }
    }
    @Test fun wrongLevelAndCorruptJournalCannotOverwriteSavedWork() {
        val disk=disk();val progress=progress();val host=PatternHost(disk,progress,PatternLevel.AB)
        host.open(SessionId("keep"),RoundLength.TEN,5,ContentLanguage.ENGLISH)
        val bytes=disk.access{it.read()}!!
        assertTrue(runCatching{PatternHost(disk,progress,PatternLevel.ABC).open(SessionId("bad-level"),RoundLength.FIVE,1,ContentLanguage.ENGLISH)}.isFailure)
        assertArrayEquals(bytes,disk.access{it.read()})
        val broken=bytes.copyOf();broken[20]=(broken[20].toInt() xor 1).toByte();disk.access{it.replace(broken)}
        assertTrue(runCatching{PatternHost(disk,progress,PatternLevel.AB).open(SessionId("bad-bytes"),RoundLength.FIVE,1,ContentLanguage.ENGLISH)}.isFailure)
        assertArrayEquals(broken,disk.access{it.read()})
    }
    @Test fun acceptedProgressWithLostAcknowledgementRecoversWithoutAnotherAttempt() {
        val disk=disk();val actual=progress();var fail=true
        val flaky=object:ProgressRepository {
            override fun read(query:ProgressQuery)=actual.read(query)
            override fun append(event:ProgressEvent):ProgressWriteResult {val accepted=actual.append(event);return if(fail)ProgressWriteResult.Failed(ProgressFailure.IO)else accepted}
        }
        var host=PatternHost(disk,flaky,PatternLevel.AB);host.open(SessionId("pending"),RoundLength.FIVE,5,ContentLanguage.ENGLISH)
        host.state!!.task.unit.forEach{host.dispatch(PatternBuilder.Action.Add(it))};host.dispatch(PatternBuilder.Action.Check)
        assertTrue(host.failure&&host.hasPending);val before=host.state
        host.dispatch(PatternBuilder.Action.Next);assertEquals(before,host.state)
        fail=false;host=PatternHost(disk,flaky,PatternLevel.AB);host.open(SessionId("ignored"),RoundLength.TEN,3,ContentLanguage.GERMAN)
        assertEquals(before,host.state);assertFalse(host.failure||host.hasPending);assertEquals(1,(actual.read()as ProgressReadResult.Events).records.size)
    }
    @Test fun uncertainJournalCommitRestoresTheDurablePlacementWithoutProgress() {
        val actual=disk();var fail=false
        val uncertain=object:ProgressStorage {
            override fun <T> access(block:(ProgressTransaction)->T):T=actual.access{tx->block(object:ProgressTransaction{
                override fun read()=tx.read()
                override fun replace(bytes:ByteArray){tx.replace(bytes);if(fail)throw java.io.IOException("Accepted then interrupted")}
            })}
        }
        val progress=progress();val host=PatternHost(uncertain,progress,PatternLevel.AAB);host.open(SessionId("commit"),RoundLength.FIVE,5,ContentLanguage.ENGLISH)
        val symbol=host.state!!.task.unit[0];fail=true;assertTrue(runCatching{host.dispatch(PatternBuilder.Action.Add(symbol))}.isFailure)
        fail=false;host.retryWrites();assertEquals(symbol,host.state!!.placed[0]);assertEquals(0,host.state!!.current.attempts)
        assertTrue((progress.read()as ProgressReadResult.Events).records.isEmpty())
    }
    @Test fun acceptedCompletionRecoversExactlyOnceAndBlocksPrematureNewRound() {
        val disk=disk();val actual=progress()
        val flaky=object:ProgressRepository {
            override fun read(query:ProgressQuery)=actual.read(query)
            override fun append(event:ProgressEvent):ProgressWriteResult {val saved=actual.append(event);return if(event is CompletionEvent)ProgressWriteResult.Failed(ProgressFailure.IO)else saved}
        }
        var host=PatternHost(disk,flaky,PatternLevel.ABC);host.open(SessionId("complete-pending"),RoundLength.FIVE,5,ContentLanguage.ENGLISH)
        repeat(5){host.state!!.task.unit.forEach{host.dispatch(PatternBuilder.Action.Add(it))};host.dispatch(PatternBuilder.Action.Check);host.dispatch(PatternBuilder.Action.Next)}
        assertTrue(host.state!!.completed);assertFalse(host.state!!.acknowledged)
        assertTrue(runCatching{host.again(SessionId("early"),RoundLength.FIVE,5,ContentLanguage.ENGLISH)}.isFailure)
        host=PatternHost(disk,actual,PatternLevel.ABC);host.open(SessionId("ignored"),RoundLength.FIVE,6,ContentLanguage.GERMAN)
        assertTrue(host.state!!.acknowledged);assertEquals(1,(actual.read()as ProgressReadResult.Events).records.count{it.event is CompletionEvent})
    }
    @Test fun audioUsesBothLanguagesSharedPolicyCancellationAndCurrentCompletionReplay() {
        for(mode in AudioMode.entries)for(lang in ContentLanguage.entries) {
            val engine=FakeSpeechEngine();val audio=PatternAudio(DefaultAudioController(engine,mode));audio.visible(true)
            val s=start().copy(language=lang)
            audio.speak(s,PatternBuilder.prompt(s.task),SpeechKind.QUESTION)
            audio.speak(s,PatternBuilder.retry,SpeechKind.FEEDBACK)
            audio.speak(s,PatternBuilder.hint(s.task),SpeechKind.EXPLANATION,true)
            audio.speak(s,PatternBuilder.complete,SpeechKind.COMPLETION)
            audio.speak(s,PatternBuilder.complete,SpeechKind.COMPLETION,true)
            assertEquals(when(mode){AudioMode.ALL->5;AudioMode.QUESTIONS->3;AudioMode.OFF->0},engine.spoken.size)
            if(engine.spoken.isNotEmpty()){assertEquals(lang,engine.spoken.last().context.language);assertEquals(PatternBuilder.complete.speech[lang],engine.spoken.last().text)}
            val count=engine.spoken.size;audio.visible(false);audio.speak(s,PatternBuilder.prompt(s.task),SpeechKind.QUESTION,true);assertEquals(count,engine.spoken.size)
            audio.close();assertEquals(0,s.current.attempts)
        }
    }
}
