package com.bellfamily.bastischool.learning.memory

import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.progress.*
import com.bellfamily.bastischool.learning.session.*
import com.bellfamily.bastischool.audio.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class MemoryPairsTest {
    @get:Rule val temp=TemporaryFolder()
    private fun disk()=AtomicProgressStorage(temp.newFolder(),JvmAtomicCommit)
    private fun progress()=FileProgressRepository(disk(),ProgressClock{1})
    private fun start(mode:MemoryMode=MemoryMode.ANIMALS,pairs:Int=4)=MemoryPairs.start(SessionId("memory"),MemoryConfig(mode,pairs),24,ContentLanguage.ENGLISH)
    private fun partner(s:MemoryPairs.State,slot:Int)=s.deck.indices.single { it!=slot&&s.deck[it].pair==s.deck[slot].pair }
    private fun reveal(s:MemoryPairs.State,slot:Int)=MemoryPairs.reduce(s,MemoryPairs.Action.Reveal(slot))
    private fun solve(s:MemoryPairs.State):MemoryPairs.State {
        var next=s
        if(next.phase!=MemoryPairs.Phase.PLAYING)next=MemoryPairs.reduce(next,MemoryPairs.Action.Continue)
        while(!next.completed) {
            val slot=next.revealed.firstOrNull()?:next.deck.indices.first { !next.visible(it) }
            if(next.revealed.isEmpty())next=reveal(next,slot)
            next=reveal(next,partner(next,slot))
            next=MemoryPairs.reduce(next,MemoryPairs.Action.Continue)
        }
        return next
    }
    @Test fun allSixBoardsAreDeterministicUniqueSemanticRelationshipsAndDoNotRepeatTheLastSet() {
        for(mode in MemoryMode.entries)for(pairs in 2..4)for(seed in 0L..100) {
            val config=MemoryConfig(mode,pairs)
            val s=MemoryPairs.start(SessionId("seed"),config,seed,ContentLanguage.ENGLISH)
            assertEquals(s,MemoryPairs.start(s.id,config,seed,s.language))
            assertEquals(pairs,s.pairIds.size)
            assertEquals(pairs*2,s.deck.map { it.key }.distinct().size)
            s.pairIds.forEach { id->assertEquals(2,s.deck.count { it.pair==id }) }
            assertTrue(s.deck.all { MemoryPairs.label(it).display.en.isNotBlank()&&MemoryPairs.label(it).display.de.isNotBlank() })
            val again=MemoryPairs.start(SessionId("again"),config,seed,ContentLanguage.GERMAN,s)
            assertNotEquals(s.pairIds,again.pairIds);assertNotEquals(s.deck,again.deck)
            if(mode==MemoryMode.NUMBERS)s.pairIds.forEach { id->assertEquals(setOf(MemoryFace.NUMERAL,MemoryFace.QUANTITY),s.deck.filter{it.pair==id}.map{it.face}.toSet()) }
        }
    }
    @Test fun rapidTapsCannotRevealThreeCardsAndFirstExposureIsNotAnAttempt() {
        val s=start();assertSame(s,reveal(s,-1));assertSame(s,reveal(s,8))
        val first=reveal(s,0);assertEquals(1,first.exposures.sum());assertTrue(first.results.all{it.attempts==0})
        assertSame(first,reveal(first,0));assertSame(first,MemoryPairs.reduce(first,MemoryPairs.Action.Continue))
        val second=reveal(first,partner(first,0));assertEquals(MemoryPairs.Phase.MATCH,second.phase)
        assertEquals(1,second.results.sumOf{it.attempts});assertEquals(1,second.solvedPairs)
        assertSame(second,reveal(second,1));assertSame(second,MemoryPairs.reduce(second,MemoryPairs.Action.Help))
        val next=MemoryPairs.reduce(second,MemoryPairs.Action.Continue)
        assertTrue(next.revealed.isEmpty());assertTrue(next.visible(0));assertSame(next,reveal(next,0))
    }
    @Test fun mismatchIsChildControlledAndSurvivesLanguageChangesWithoutReshuffling() {
        var s=start();val other=s.deck.indices.first{ s.deck[it].pair!=s.deck[0].pair }
        s=reveal(reveal(s,0),other)
        assertEquals(MemoryPairs.Phase.DIFFERENT,s.phase);assertEquals(0,s.solvedPairs)
        val event=MemoryPairs.attempt(s);assertEquals(AttemptOutcome.INCORRECT,event.outcome)
        assertEquals(s.deck[other].pair,event.choice)
        val translated=MemoryPairs.reduce(s,MemoryPairs.Action.Language(ContentLanguage.GERMAN))
        assertEquals(s.deck,translated.deck);assertEquals(s.revealed,translated.revealed);assertEquals(s.results,translated.results)
        s=MemoryPairs.reduce(translated,MemoryPairs.Action.Continue);assertFalse(s.visible(0));assertFalse(s.visible(other))
        s=reveal(reveal(s,0),partner(s,0));assertEquals(2,MemoryPairs.attempt(s).attempt.number)
    }
    @Test fun modelAndReplayAreSupportedExposureWithoutFabricatedSuccess() {
        var s=reveal(start(),0);s=MemoryPairs.reduce(s,MemoryPairs.Action.Help)
        assertEquals(MemoryPairs.Phase.MODEL,s.phase);assertEquals(2,s.exposures.sum());assertEquals(0,s.solvedPairs)
        assertTrue(s.results.all{it.attempts==0});assertTrue(s.results[s.ordinal(0)].support.hint)
        s=MemoryPairs.reduce(s,MemoryPairs.Action.Listen(partner(s,0)));assertEquals(1,s.results[s.ordinal(0)].support.replays)
        s=MemoryPairs.reduce(s,MemoryPairs.Action.Continue);s=MemoryPairs.reduce(s,MemoryPairs.Action.Replay)
        s=reveal(reveal(s,0),partner(s,0));val e=MemoryPairs.attempt(s)
        assertTrue(e.support.hint);assertEquals(2,e.support.replays);assertEquals(1,e.attempt.number)
    }
    @Test fun allBoardPhasesRoundTripExactlyAndDamagedOrWrongConfigJournalsArePreserved() {
        for(mode in MemoryMode.entries)for(pairs in 2..4) {
            val s=start(mode,pairs);val first=reveal(s,0);val correct=reveal(first,partner(first,0))
            val wrong=reveal(first,s.deck.indices.first{ s.deck[it].pair!=s.deck[0].pair })
            val model=MemoryPairs.reduce(first,MemoryPairs.Action.Help)
            for(value in listOf(s,first,correct,wrong,model,solve(s).copy(acknowledged=true))) {
                val bytes=MemoryPairsHost.encode(value,emptyList());assertEquals(value,MemoryPairsHost.decode(bytes).first)
                assertTrue(runCatching{MemoryPairsHost.decode(bytes.copyOf(bytes.size-1))}.isFailure)
                assertTrue(runCatching{MemoryPairsHost.decode(bytes+byteArrayOf(1))}.isFailure)
                val bad=bytes.copyOf();bad[20]=(bad[20].toInt() xor 1).toByte();assertTrue(runCatching{MemoryPairsHost.decode(bad)}.isFailure)
            }
            val disk=disk();val progress=progress();val host=MemoryPairsHost(disk,progress,s.config)
            host.open(s.id,1,s.language);host.dispatch(MemoryPairs.Action.Reveal(0));val saved=disk.access{it.read()}!!
            assertTrue(runCatching{MemoryPairsHost(disk,progress,MemoryConfig(mode,if(pairs==2)3 else 2)).open(SessionId("wrong"),1,s.language)}.isFailure)
            assertArrayEquals(saved,disk.access{it.read()})
        }
    }
    @Test fun sharedProgressRecordsPairsNotFlipsAndCompletionIsDeduplicatedForTwoThreeAndFourPairs() {
        for(mode in MemoryMode.entries)for(pairs in 2..4) {
            val config=MemoryConfig(mode,pairs);val disk=disk();val progress=progress()
            var host=MemoryPairsHost(disk,progress,config)
            assertTrue(host.open(SessionId("${config.key}-one"),42,ContentLanguage.GERMAN))
            host.dispatch(MemoryPairs.Action.Reveal(0));val first=host.state!!
            assertTrue((progress.read()as ProgressReadResult.Events).records.isEmpty())
            host=MemoryPairsHost(disk,progress,config);assertFalse(host.open(SessionId("ignored"),55,ContentLanguage.ENGLISH));assertEquals(first,host.state)
            host.dispatch(MemoryPairs.Action.Help);host.dispatch(MemoryPairs.Action.Continue)
            while(!host.state!!.completed) {
                val s=host.state!!;val slot=s.deck.indices.first{!s.visible(it)}
                host.dispatch(MemoryPairs.Action.Reveal(slot));host.dispatch(MemoryPairs.Action.Reveal(partner(s,slot)))
                host.dispatch(MemoryPairs.Action.Continue);host.dispatch(MemoryPairs.Action.Continue)
            }
            val finished=host.state!!;assertTrue(finished.acknowledged)
            host=MemoryPairsHost(disk,progress,config);host.open(SessionId("ignored-again"),3,ContentLanguage.ENGLISH)
            assertEquals(finished,host.state)
            val records=(progress.read()as ProgressReadResult.Events).records
            assertEquals(pairs,records.count{it.event is AttemptEvent});assertEquals(1,records.count{it.event is CompletionEvent})
            val completion=records.map{it.event}.filterIsInstance<CompletionEvent>().single()
            assertEquals(pairs,completion.tasks.size);assertTrue(completion.tasks.any{it.support.hint})
            assertTrue(completion.tasks.all{it.evidence.skill==MemoryPairs.skill(config)&&it.attempts==1})
            assertEquals(records,ProgressCodec.decode(ProgressCodec.encode(records)))
            host.again(SessionId("${config.key}-two"),42,ContentLanguage.GERMAN)
            assertNotEquals(finished.pairIds,host.state!!.pairIds)
        }
    }
    @Test fun uncertainProgressAcceptanceAndFailedAcknowledgementRecoverWithoutDuplicateEvidence() {
        val disk=disk();val real=progress();val config=MemoryConfig(MemoryMode.NUMBERS,2);var fail=true
        val flaky=object:ProgressRepository {
            override fun read(query:ProgressQuery)=real.read(query)
            override fun append(event:ProgressEvent):ProgressWriteResult {val result=real.append(event);return if(fail)ProgressWriteResult.Failed(ProgressFailure.IO)else result}
        }
        var host=MemoryPairsHost(disk,flaky,config);host.open(SessionId("pending"),4,ContentLanguage.ENGLISH)
        host.dispatch(MemoryPairs.Action.Reveal(0));host.dispatch(MemoryPairs.Action.Reveal(partner(host.state!!,0)))
        assertTrue(host.failure&&host.hasPending);val pending=host.state!!
        host.dispatch(MemoryPairs.Action.Continue);assertEquals(pending,host.state)
        fail=false;host=MemoryPairsHost(disk,flaky,config);host.open(SessionId("ignored"),2,ContentLanguage.ENGLISH)
        assertEquals(pending,host.state);assertFalse(host.failure||host.hasPending)
        assertEquals(1,(real.read()as ProgressReadResult.Events).records.size)
    }
    @Test fun completionAcceptedBeforeErrorIsRecoveredExactlyOnceAndAgainWaitsForAcknowledgement() {
        val disk=disk();val real=progress();val config=MemoryConfig(MemoryMode.ANIMALS,2)
        val flaky=object:ProgressRepository {
            override fun read(query:ProgressQuery)=real.read(query)
            override fun append(event:ProgressEvent):ProgressWriteResult {
                val result=real.append(event)
                return if(event is CompletionEvent)ProgressWriteResult.Failed(ProgressFailure.IO)else result
            }
        }
        var host=MemoryPairsHost(disk,flaky,config);host.open(SessionId("completion-pending"),2,ContentLanguage.ENGLISH)
        repeat(config.pairs) {
            val s=host.state!!;val slot=s.deck.indices.first{!s.visible(it)}
            host.dispatch(MemoryPairs.Action.Reveal(slot));host.dispatch(MemoryPairs.Action.Reveal(partner(s,slot)));host.dispatch(MemoryPairs.Action.Continue)
        }
        assertTrue(host.state!!.completed);assertFalse(host.state!!.acknowledged);assertTrue(host.failure)
        assertTrue(runCatching{host.again(SessionId("too-early"),1,ContentLanguage.ENGLISH)}.isFailure)
        host=MemoryPairsHost(disk,real,config);host.open(SessionId("ignored"),99,ContentLanguage.ENGLISH)
        assertTrue(host.state!!.acknowledged);assertFalse(host.failure)
        assertEquals(1,(real.read()as ProgressReadResult.Events).records.count{it.event is CompletionEvent})
    }
    @Test fun uncertainJournalCommitRecoversTheDurableFirstRevealWithoutInventingAnAttempt() {
        val actual=disk();var fail=false
        val uncertain=object:ProgressStorage {
            override fun <T> access(block:(ProgressTransaction)->T):T=actual.access { tx->
                block(object:ProgressTransaction {
                    override fun read()=tx.read()
                    override fun replace(bytes:ByteArray){tx.replace(bytes);if(fail)throw java.io.IOException("Accepted then interrupted")}
                })
            }
        }
        val progress=progress();val config=MemoryConfig(MemoryMode.ANIMALS,3)
        val host=MemoryPairsHost(uncertain,progress,config);host.open(SessionId("journal"),4,ContentLanguage.ENGLISH)
        fail=true;assertTrue(runCatching{host.dispatch(MemoryPairs.Action.Reveal(0))}.isFailure)
        assertTrue(host.failure);assertTrue(host.state!!.revealed.isEmpty())
        fail=false;host.retryWrites();assertEquals(listOf(0),host.state!!.revealed)
        assertEquals(1,host.state!!.exposures.sum());assertFalse(host.failure)
        assertTrue((progress.read()as ProgressReadResult.Events).records.isEmpty())
    }
    @Test fun audioRespectsPolicyLanguageLifecycleAndCompletionReplayWithoutChangingEvidence() {
        for(mode in AudioMode.entries)for(lang in ContentLanguage.entries) {
            val engine=FakeSpeechEngine();val audio=MemoryPairsAudio(DefaultAudioController(engine,mode));audio.visible(true)
            val s=start().copy(language=lang)
            audio.speak(s,MemoryPairs.instruction,SpeechKind.EXPLANATION)
            audio.speak(s,MemoryPairs.label(s.deck[0]),SpeechKind.EXPLANATION,true)
            val done=solve(s);audio.speak(done,MemoryPairs.complete,SpeechKind.COMPLETION)
            audio.speak(done,MemoryPairsAudio.current(done),SpeechKind.COMPLETION,true)
            assertEquals(when(mode){AudioMode.OFF->0;AudioMode.ALL->4;AudioMode.QUESTIONS->3},engine.spoken.size)
            if(engine.spoken.isNotEmpty()){assertEquals(lang,engine.spoken.last().context.language);assertEquals(MemoryPairs.complete.speech[lang],engine.spoken.last().text)}
            val count=engine.spoken.size;audio.visible(false);audio.speak(s,MemoryPairs.instruction,SpeechKind.EXPLANATION,true);assertEquals(count,engine.spoken.size)
            assertEquals(0,s.results.sumOf{it.attempts});audio.close()
        }
    }
}
