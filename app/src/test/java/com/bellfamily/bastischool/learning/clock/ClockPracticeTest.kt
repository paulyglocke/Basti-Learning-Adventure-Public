package com.bellfamily.bastischool.learning.clock

import com.bellfamily.bastischool.audio.*
import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.progress.*
import com.bellfamily.bastischool.learning.session.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.IOException

class ClockPracticeTest {
    @get:Rule val temp = TemporaryFolder()
    private fun state(seed: Long = 1) = ClockPractice.State(SessionId("clock-practice"), ClockPractice.generate(seed), ContentLanguage.ENGLISH)
    private fun disk() = AtomicProgressStorage(temp.newFolder(), JvmAtomicCommit)
    private fun repository() = FileProgressRepository(disk(), ProgressClock { 1 })
    private fun reduce(s: ClockPractice.State, a: ClockPractice.Action) = ClockPractice.reduce(s, a)
    @Test fun catalogueAndBalancedDeterministicGeneration() {
        assertEquals(24, ClockPractice.targets.toSet().size)
        for (seed in 0L..100L) {
            val tasks = ClockPractice.generate(seed)
            assertEquals(tasks, ClockPractice.generate(seed))
            assertEquals(8, tasks.toSet().size)
            assertEquals(4, tasks.count { it.minute == 0 }); assertEquals(4, tasks.count { it.minute == 30 })
            tasks.chunked(2).forEach { assertEquals(setOf(0,30), it.map { t -> t.minute }.toSet()) }
        }
        ClockPractice.targets.forEach {
            val start = ClockPractice.startTime(it)
            assertNotEquals(it, start); assertTrue(start in ClockPractice.targets)
            assertEquals(it, start.advance(90))
        }
    }
    @Test fun allTwentyFourPromptsAndFeedbackUseNaturalAuthoredTime() {
        val wholeEn = listOf("one","two","three","four","five","six","seven","eight","nine","ten","eleven","twelve")
        val wholeDe = listOf("ein","zwei","drei","vier","fünf","sechs","sieben","acht","neun","zehn","elf","zwölf")
        val halfDe = listOf("zwei","drei","vier","fünf","sechs","sieben","acht","neun","zehn","elf","zwölf","eins")
        for (h in 1..12) for (minute in listOf(0,30)) {
            val t = ClockTime(h,minute)
            val en = if (minute == 0) "${wholeEn[h-1]} o'clock" else "half past ${wholeEn[h-1]}"
            val de = if (minute == 0) "${wholeDe[h-1]} Uhr" else "halb ${halfDe[h-1]}"
            assertEquals("Make $en.", ClockPractice.prompt(t).display.en)
            assertEquals("Stelle $de ein.", ClockPractice.prompt(t).display.de)
            assertEquals(ClockPractice.prompt(t).display, ClockPractice.prompt(t).speech)
            assertEquals("Yes! That's $en.", ClockPractice.correct(t).speech.en)
            assertEquals("Ja! Das ist $de.", ClockPractice.correct(t).speech.de)
            val mark = if (minute == 0) 12 else 6
            assertEquals("Put the long hand on $mark.", ClockPractice.hint(t).display.en)
            assertEquals("Stelle den langen Zeiger auf die $mark.", ClockPractice.hint(t).display.de)
        }
    }
    @Test fun onlyChecksCountWrongHourAndMinuteHintThenSuccessLocksAndNext() {
        var s = state(); val target = s.target
        assertFalse(s.current.support.hint)
        s = reduce(s, ClockPractice.Action.Move(target.advance(60)))
        assertEquals(0,s.current.attempts)
        s = reduce(s,ClockPractice.Action.Check)
        assertEquals(1,s.current.wrong); assertFalse(s.current.support.hint)
        assertEquals(target,s.target); assertFalse(s.current.solved)
        s = reduce(s,ClockPractice.Action.Move(target.advance(30)))
        s = reduce(s,ClockPractice.Action.Check)
        assertEquals(2,s.current.wrong); assertTrue(s.current.support.hint)
        assertFalse(ClockPractice.attempt(s).support.hint) // hint appears AFTER second wrong check
        s = reduce(s,ClockPractice.Action.Move(target))
        assertFalse(s.current.solved)
        s = reduce(s,ClockPractice.Action.Check)
        assertTrue(s.current.solved); assertEquals(3,s.current.attempts)
        assertTrue(ClockPractice.attempt(s).support.hint)
        assertEquals(s,reduce(s,ClockPractice.Action.Check))
        assertEquals(s,reduce(s,ClockPractice.Action.Move(target.advance(30))))
        s = reduce(s,ClockPractice.Action.Next)
        assertEquals(1,s.index); assertEquals(0,s.current.attempts); assertEquals(0,s.firstTry)
    }
    @Test fun thirtyMinuteMovementWrapAndExploreStillFive() {
        assertEquals(ClockTime(12),ClockTime(11,30).advance(30))
        assertEquals(ClockTime(11,30),ClockTime(12).advance(-30))
        assertEquals(ClockTime(1),ClockTime(12,30).advance(30))
        assertEquals(ClockTime(12,30),ClockTime(1).advance(-30))
        assertEquals(ClockTime(3,30),ClockTime(3,23).snap(30))
        assertEquals(ClockTime(3,25),ClockTime(3,23).snap())
        var s=state()
        ClockPractice.targets.forEach { target ->
            s = ClockPractice.State(s.id,listOf(target)+ClockPractice.generate(2).filter {it!=target}.let { rest ->
                // Use a validated balanced round containing this target.
                val opposite=rest.filter {it.minute!=target.minute}.take(4)
                val same=ClockPractice.targets.filter {it.minute==target.minute && it!=target}.take(3)
                opposite+same
            },s.language)
            s=reduce(reduce(s,ClockPractice.Action.Move(target)),ClockPractice.Action.Check)
            assertTrue(s.current.solved)
        }
    }
    @Test fun exactPartialAndCompletedRestoreWithFirstTryAndFreshAgain() {
        val journal=disk(); val progress=repository()
        var host=ClockPracticeHost(journal,progress)
        host.open(SessionId("round"),5,ContentLanguage.GERMAN)
        host.dispatch(ClockPractice.Action.Check); host.dispatch(ClockPractice.Action.Check)
        host.dispatch(ClockPractice.Action.Replay)
        host.dispatch(ClockPractice.Action.Move(host.state!!.target.advance(30)))
        val partial=host.state!!
        host=ClockPracticeHost(journal,progress)
        host.open(SessionId("ignored"),999,ContentLanguage.ENGLISH)
        assertEquals(partial,host.state)
        repeat(8) {
            host.dispatch(ClockPractice.Action.Move(host.state!!.target)); host.dispatch(ClockPractice.Action.Check)
            host.dispatch(ClockPractice.Action.Next)
        }
        val completed=host.state!!
        assertTrue(completed.completed && completed.acknowledged);assertEquals(7,completed.firstTry)
        val restored=ClockPracticeHost(journal,progress)
        restored.open(SessionId("ignored-again"),0,ContentLanguage.ENGLISH)
        assertEquals(completed,restored.state)
        val events=(progress.read() as ProgressReadResult.Events).records.map {it.event}
        assertEquals(10,events.filterIsInstance<AttemptEvent>().size)
        val completion=events.filterIsInstance<CompletionEvent>().single()
        assertEquals(8,completion.tasks.size);assertEquals(3,completion.tasks.first().attempts)
        assertEquals(2,completion.tasks.first().retries);assertTrue(completion.tasks.first().support.hint)
        assertEquals(completion,ProgressCodec.decode(ProgressCodec.encode(listOf(StoredProgressEvent(1,0,completion)))).single().event)
        restored.again(SessionId("fresh"),6,ContentLanguage.ENGLISH)
        assertFalse(restored.state!!.completed);assertEquals(0,restored.state!!.index)
        assertTrue(restored.state!!.results.all {it.attempts==0}); assertNotEquals(completed.id,restored.state!!.id)
    }
    private class FailingProgress(val delegate: ProgressRepository):ProgressRepository {
        var fail=true
        override fun read(query:ProgressQuery)=delegate.read(query)
        override fun append(event:ProgressEvent):ProgressWriteResult {
            val accepted=delegate.append(event) // uncertain acknowledgement, already durable
            return if(fail)ProgressWriteResult.Failed(ProgressFailure.IO) else accepted
        }
    }
    @Test fun failedAttemptAndCompletionRedeliverExactlyOnceAfterRestore() {
        val journal=disk();val real=repository();val delivery=FailingProgress(real)
        var host=ClockPracticeHost(journal,delivery)
        host.open(SessionId("delivery"),3,ContentLanguage.ENGLISH)
        host.dispatch(ClockPractice.Action.Check)
        assertTrue(host.failure && host.hasPending)
        val stuck=host.state
        host.dispatch(ClockPractice.Action.Check);assertEquals(stuck,host.state)
        delivery.fail=false
        host=ClockPracticeHost(journal,delivery);host.open(SessionId("unused"),0,ContentLanguage.GERMAN)
        assertFalse(host.failure);assertFalse(host.hasPending)
        repeat(8) {
            host.dispatch(ClockPractice.Action.Move(host.state!!.target));host.dispatch(ClockPractice.Action.Check)
            if(it==7)delivery.fail=true
            host.dispatch(ClockPractice.Action.Next)
        }
        assertTrue(host.state!!.completed);assertTrue(host.hasPending)
        delivery.fail=false
        host=ClockPracticeHost(journal,delivery);host.open(SessionId("unused2"),0,ContentLanguage.ENGLISH)
        host.retryWrites()
        val events=(real.read() as ProgressReadResult.Events).records.map {it.event}
        assertEquals(9,events.filterIsInstance<AttemptEvent>().size)
        assertEquals(1,events.filterIsInstance<CompletionEvent>().size)
        assertEquals(events.size,events.map {it.id}.toSet().size)
    }
    @Test fun journalCorruptionAndForgedPendingEvidenceAreRejected() {
        val s=reduce(state(),ClockPractice.Action.Check)
        val bytes=ClockPracticeHost.encode(s,listOf(ClockPractice.attempt(s)))
        assertEquals(s,ClockPracticeHost.decode(bytes).first)
        bytes[20]=(bytes[20].toInt() xor 1).toByte()
        assertTrue(runCatching {ClockPracticeHost.decode(bytes)}.isFailure)
        val wrong=ClockPractice.attempt(s).copy(choice=ClockPractice.timeId(s.target))
        assertTrue(runCatching {ClockPracticeHost.decode(ClockPracticeHost.encode(s,listOf(wrong)))}.isFailure)
    }
    @Test fun uncertainJournalCommitRecoversWithoutLosingOrDuplicatingAttempt() {
        val backing=disk();val progress=repository()
        var fail=false
        val disk=object:ProgressStorage {
            override fun <T> access(block:(ProgressTransaction)->T):T=backing.access {tx ->
                block(object:ProgressTransaction {
                    override fun read()=tx.read()
                    override fun replace(bytes:ByteArray) {tx.replace(bytes);if(fail)throw IOException("uncertain commit")}
                })
            }
        }
        val host=ClockPracticeHost(disk,progress);host.open(SessionId("uncertain"),4,ContentLanguage.ENGLISH)
        fail=true
        assertTrue(runCatching {host.dispatch(ClockPractice.Action.Check)}.isFailure)
        fail=false;host.retryWrites()
        assertEquals(1,host.state!!.current.attempts)
        assertEquals(1,(progress.read() as ProgressReadResult.Events).records.size)
    }
    @Test fun practiceAudioPolicyCorrectLanguageAndSilentManipulation() {
        for(mode in AudioMode.entries) for(lang in ContentLanguage.entries) {
            val engine=FakeSpeechEngine();val audio=ClockAudio(DefaultAudioController(engine,mode));audio.visible(true)
            var s=state().copy(language=lang)
            assertTrue(engine.spoken.isEmpty())
            audio.practice(s,SpeechKind.QUESTION,true)
            assertEquals(if(mode==AudioMode.OFF)0 else 1,engine.spoken.size)
            if(mode!=AudioMode.OFF)assertEquals(ClockPractice.prompt(s.target).speech[lang],engine.spoken.last().text)
            audio.cancel(); s=reduce(s,ClockPractice.Action.Move(s.target.advance(30)))
            val count=engine.spoken.size
            s=reduce(s,ClockPractice.Action.Check);audio.practice(s,SpeechKind.FEEDBACK)
            assertEquals(count+if(mode==AudioMode.ALL)1 else 0,engine.spoken.size)
            s=reduce(s,ClockPractice.Action.Check);audio.practice(s,SpeechKind.EXPLANATION)
            if(mode!=AudioMode.OFF)assertEquals(ClockPractice.hint(s.target).speech[lang],engine.spoken.last().text)
            s=reduce(reduce(s,ClockPractice.Action.Move(s.target)),ClockPractice.Action.Check)
            audio.practice(s,SpeechKind.FEEDBACK)
            if(mode==AudioMode.ALL)assertEquals(ClockPractice.correct(s.target).speech[lang],engine.spoken.last().text)
            if(mode!=AudioMode.OFF)assertEquals(lang,engine.spoken.last().context.language)
            val after=engine.spoken.size;audio.visible(false);audio.visible(true)
            assertEquals(after,engine.spoken.size);audio.close()
        }
    }
}
