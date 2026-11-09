package com.bellfamily.bastischool.learning.bonds

import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.progress.*
import com.bellfamily.bastischool.learning.session.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class NumberBondsTest {
    @get:Rule val temp=TemporaryFolder()
    private fun disk()=AtomicProgressStorage(temp.newFolder(),JvmAtomicCommit)
    private fun progress()=FileProgressRepository(disk(),ProgressClock{1})
    @Test fun audioPolicyAndBackgroundCancellationAreSharedAndNeverChangeTheBoard() {
        for(mode in com.bellfamily.bastischool.audio.AudioMode.entries)for(language in ContentLanguage.entries){
            val engine=com.bellfamily.bastischool.audio.FakeSpeechEngine()
            val audio=NumberBondsAudio(com.bellfamily.bastischool.audio.DefaultAudioController(engine,mode))
            audio.visible(true)
            val s=NumberBonds.start(SessionId("audio"),BondLevel.TEN,RoundLength.FIVE,1,language)
            audio.speak(s,com.bellfamily.bastischool.audio.SpeechKind.QUESTION)
            audio.speak(s,com.bellfamily.bastischool.audio.SpeechKind.EXPLANATION,true)
            assertEquals(if(mode==com.bellfamily.bastischool.audio.AudioMode.OFF)0 else 2,engine.spoken.size)
            if(engine.spoken.isNotEmpty())assertEquals(language,engine.spoken.last().context.language)
            audio.visible(false)
            audio.speak(s,com.bellfamily.bastischool.audio.SpeechKind.QUESTION,true)
            assertEquals(if(mode==com.bellfamily.bastischool.audio.AudioMode.OFF)0 else 2,engine.spoken.size)
            assertEquals(0,s.current.attempts)
            audio.close()
        }
    }
    @Test fun seededTargetsUseCompleteCyclesWithBalancedCountsAndNoAdjacentRepeats() {
        for(level in BondLevel.entries)for(round in RoundLength.entries)for(seed in 0L..250){
            val s=NumberBonds.start(SessionId("bonds"),level,round,seed,ContentLanguage.ENGLISH)
            assertEquals(s.targets,NumberBonds.targets(level,round,seed))
            assertTrue(s.targets.zipWithNext().none{(a,b)->a==b})
            assertEquals(minOf(level.total-1,round.count),s.targets.take(level.total-1).distinct().size)
            assertEquals(0,s.addedCount);assertEquals(s.shown,s.totalCount)
        }
    }
    @Test fun fixedStartingGroupCannotChangeAndExplorationIsUnscored() {
        for(level in BondLevel.entries){
            var s=NumberBonds.start(SessionId("build"),level,RoundLength.FIVE,1,ContentLanguage.ENGLISH)
            repeat(s.shown){assertSame(s,NumberBonds.reduce(s,NumberBonds.Action.Toggle(it)))}
            assertSame(s,NumberBonds.reduce(s,NumberBonds.Action.Toggle(-1)))
            assertSame(s,NumberBonds.reduce(s,NumberBonds.Action.Toggle(level.total)))
            s=NumberBonds.reduce(s,NumberBonds.Action.Toggle(s.shown));assertEquals(1,s.addedCount)
            s=NumberBonds.reduce(s,NumberBonds.Action.Toggle(s.shown));assertEquals(0,s.addedCount)
            assertEquals(0,s.current.attempts)
        }
    }
    @Test fun onlyCheckRecordsEvidenceAndSolvedBoardLocksBeforeExplicitNext() {
        for(level in BondLevel.entries){
            var s=NumberBonds.start(SessionId("check"),level,RoundLength.FIVE,2,ContentLanguage.ENGLISH)
            s=NumberBonds.reduce(s,NumberBonds.Action.Check);assertFalse(s.current.solved)
            assertEquals(AttemptOutcome.INCORRECT,NumberBonds.attempt(s).outcome)
            s=NumberBonds.reduce(s,NumberBonds.Action.Help);s=NumberBonds.reduce(s,NumberBonds.Action.Replay)
            for(slot in s.shown until level.total)s=NumberBonds.reduce(s,NumberBonds.Action.Toggle(slot))
            assertEquals(1,s.current.attempts)
            s=NumberBonds.reduce(s,NumberBonds.Action.Check);assertTrue(s.current.solved)
            assertTrue(NumberBonds.attempt(s).support.hint);assertEquals(1,NumberBonds.attempt(s).support.replays)
            assertSame(s,NumberBonds.reduce(s,NumberBonds.Action.Check))
            assertSame(s,NumberBonds.reduce(s,NumberBonds.Action.Toggle(s.shown)))
            val next=NumberBonds.reduce(s,NumberBonds.Action.Next);assertEquals(1,next.index);assertEquals(0,next.placedMask)
        }
    }
    @Test fun partialBoardAndSupportedChecksRestoreAndCompletionIsDeduplicated() {
        for(level in BondLevel.entries){
            val disk=disk();val progress=progress();var host=NumberBondsHost(disk,progress,level)
            host.open(SessionId("${level.key}-first"),RoundLength.TEN,42,ContentLanguage.GERMAN)
            host.dispatch(NumberBonds.Action.Toggle(host.state!!.shown));host.dispatch(NumberBonds.Action.Help)
            val partial=host.state
            host=NumberBondsHost(disk,progress,level);assertFalse(host.open(SessionId("ignored"),RoundLength.FIVE,99,ContentLanguage.ENGLISH))
            assertEquals(partial,host.state);assertTrue((progress.read() as ProgressReadResult.Events).records.isEmpty())
            repeat(10){
                val current=host.state!!
                for(slot in current.shown until level.total)if(current.placedMask and (1 shl slot)==0)host.dispatch(NumberBonds.Action.Toggle(slot))
                host.dispatch(NumberBonds.Action.Check);host.dispatch(NumberBonds.Action.Next)
            }
            assertTrue(host.state!!.completed&&host.state!!.acknowledged)
            host=NumberBondsHost(disk,progress,level);host.open(SessionId("another-ignored"),RoundLength.FIVE,77,ContentLanguage.ENGLISH)
            val records=(progress.read() as ProgressReadResult.Events).records.map{it.event}
            assertEquals(10,records.filterIsInstance<AttemptEvent>().size);assertEquals(1,records.filterIsInstance<CompletionEvent>().size)
            assertEquals(NumberBonds.activity(level),records.first().origin.activity)
            assertTrue(records.filterIsInstance<CompletionEvent>().single().tasks.first().support.hint)
            assertTrue(runCatching {NumberBondsHost(disk,progress,if(level==BondLevel.FIVE)BondLevel.TEN else BondLevel.FIVE).open(SessionId("wrong-level"),RoundLength.FIVE,1,ContentLanguage.ENGLISH)}.isFailure)
        }
    }
    @Test fun uncertainProgressWriteRecoversIdempotentlyAndDamagedJournalIsPreserved() {
        val disk=disk();val progress=progress();var fail=true
        val flaky=object:ProgressRepository {
            override fun read(query:ProgressQuery)=progress.read(query)
            override fun append(event:ProgressEvent):ProgressWriteResult {val accepted=progress.append(event);return if(fail)ProgressWriteResult.Failed(ProgressFailure.IO)else accepted}
        }
        var host=NumberBondsHost(disk,flaky,BondLevel.TEN)
        host.open(SessionId("pending"),RoundLength.FIVE,1,ContentLanguage.ENGLISH);host.dispatch(NumberBonds.Action.Check)
        assertTrue(host.failure&&host.hasPending);val pending=host.state
        host.dispatch(NumberBonds.Action.Check);assertEquals(pending,host.state)
        fail=false;host=NumberBondsHost(disk,flaky,BondLevel.TEN);host.open(SessionId("ignored"),RoundLength.FIVE,1,ContentLanguage.ENGLISH)
        assertFalse(host.failure);assertEquals(1,(progress.read() as ProgressReadResult.Events).records.size)
        val bytes=NumberBondsHost.encode(host.state!!,emptyList());assertEquals(host.state,NumberBondsHost.decode(bytes).first)
        val broken=bytes.copyOf();broken[20]=(broken[20].toInt() xor 1).toByte()
        assertTrue(runCatching{NumberBondsHost.decode(broken)}.isFailure)
        assertTrue(runCatching{NumberBondsHost.decode(bytes+byteArrayOf(1))}.isFailure)
    }
}
