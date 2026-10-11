package com.bellfamily.bastischool.learning.months

import com.bellfamily.bastischool.audio.*
import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.progress.*
import com.bellfamily.bastischool.learning.session.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.IOException

class MonthsQuestionsTest {
    @get:Rule val temp=TemporaryFolder()
    private val modes=listOf(MonthsMode.FIND,MonthsMode.RELATIONS)
    private fun plan(mode:MonthsMode, round:RoundLength=RoundLength.FIVE,seed:Long=3)=
        (MonthsQuestions.generate(mode,SessionId("months-${mode.name}"),round,seed) as GenerationResult.Generated).plan
    private fun start(mode:MonthsMode,round:RoundLength=RoundLength.FIVE,seed:Long=3)=
        SessionReducer.start(plan(mode,round,seed),ContentLanguage.ENGLISH,MonthContent.repository).state
    private fun disk()=AtomicProgressStorage(temp.newFolder(),JvmAtomicCommit)
    private fun open(mode:MonthsMode,disk:ProgressStorage,progress:ProgressRepository,round:RoundLength=RoundLength.FIVE)=
        MonthsQuestions.host(mode,disk,progress).also {it.open(SessionId("months-${mode.name}"),round,3,ContentLanguage.ENGLISH)}
    private fun answer(h:DurableSessionHost,correct:Boolean=true) {
        val s=h.state!!
        h.dispatch(SessionAction.Answer(s.nextAttempt!!,if(correct)s.task.question.correct else s.task.question.choices.first{it!=s.task.question.correct}))
    }
    @Test fun allMonthsAllDirectionsAndYearBoundaries() {
        assertEquals(MonthIds.canonicalOrder,MonthsQuestions.catalogue(MonthsMode.FIND).map{it.anchor})
        assertEquals(24,MonthsQuestions.catalogue(MonthsMode.RELATIONS).size)
        MonthsQuestions.catalogue(MonthsMode.RELATIONS).forEach {d ->
            assertEquals(if(d.kind==MonthsQuestions.Kind.BEFORE)MonthIds.previous(d.anchor) else MonthIds.next(d.anchor),d.correct)
        }
        assertEquals(MonthIds.DECEMBER,MonthsQuestions.Definition(MonthIds.JANUARY,MonthsQuestions.Kind.BEFORE).correct)
        assertEquals(MonthIds.JANUARY,MonthsQuestions.Definition(MonthIds.DECEMBER,MonthsQuestions.Kind.AFTER).correct)
    }
    @Test fun reviewedBilingualWordingForEveryDefinition() {
        for(mode in modes)for(d in MonthsQuestions.catalogue(mode)) {
            val q=MonthsQuestions.question(d);val n=MonthContent.month(d.anchor).text.speech
            when(d.kind) {
                MonthsQuestions.Kind.FIND -> {assertEquals("Find ${n.en}.",q.instruction.speech.en);assertEquals("Finde ${n.de}.",q.instruction.speech.de)}
                MonthsQuestions.Kind.BEFORE -> {assertEquals("Which month comes immediately before ${n.en}?",q.instruction.speech.en);assertEquals("Welcher Monat kommt direkt vor ${n.de}?",q.instruction.speech.de)}
                MonthsQuestions.Kind.AFTER -> {assertEquals("Which month comes immediately after ${n.en}?",q.instruction.speech.en);assertEquals("Welcher Monat kommt direkt nach ${n.de}?",q.instruction.speech.de)}
            }
            assertEquals(q.instruction.display,q.instruction.speech)
            if(d.kind!=MonthsQuestions.Kind.FIND) {
                assertFalse(q.hint!!.speech.en.contains(MonthContent.month(d.correct).text.speech.en))
                assertFalse(q.hint!!.speech.de.contains(MonthContent.month(d.correct).text.speech.de))
            }
        }
    }
    @Test fun seededRoundsAreBalancedUniqueAndHaveNeighbourDistractors() {
        val positions=mutableSetOf<Int>()
        for(mode in modes)for(round in RoundLength.entries)for(seed in 0L..39L) {
            val s=start(mode,round,seed);MonthsQuestions.validate(mode,s)
            assertArrayEquals(SessionCheckpoint.encode(s),SessionCheckpoint.encode(start(mode,round,seed)))
            assertEquals(round.count,s.plan.tasks.size)
            assertEquals(round.count,s.plan.tasks.map{it.question.definition}.distinct().size)
            val defs=s.plan.tasks.map{MonthsQuestions.definition(mode,it.question)}
            if(mode==MonthsMode.RELATIONS)assertTrue(defs.count{it.kind==MonthsQuestions.Kind.BEFORE} in round.count/2..(round.count+1)/2)
            s.plan.tasks.forEach {task ->
                val q=task.question;val d=MonthsQuestions.definition(mode,q)
                assertEquals(4,q.choices.toSet().size);assertEquals(1,q.choices.count{it==q.correct})
                assertTrue(q.choices.all{it in MonthIds.canonicalOrder})
                assertTrue(d.anchor in q.choices)
                assertTrue(MonthIds.previous(d.anchor) in q.choices && MonthIds.next(d.anchor) in q.choices)
                positions+=q.choices.indexOf(q.correct)
            }
        }
        assertEquals(setOf(0,1,2,3),positions)
    }
    @Test fun wrongRetryReplayHintLanguageAndExactPartialRestore() {
        for(mode in modes) {
            val disk=disk();val progress=ProgressFixtures.repository(temp.newFolder());val h=open(mode,disk,progress)
            val task=h.state!!.task
            h.dispatch(SessionAction.Replay(task.id));h.dispatch(SessionAction.Hint(task.id))
            assertEquals(0,h.state!!.current.attempts)
            answer(h,false);h.dispatch(SessionAction.Retry(AttemptId(task.id,1)))
            assertEquals(1,h.state!!.current.attempts);assertEquals(1,h.state!!.current.retries)
            h.dispatch(SessionAction.Language(ContentLanguage.GERMAN))
            val expected=SessionCheckpoint.encode(h.state!!)
            val restored=MonthsQuestions.host(mode,disk,progress)
            assertTrue(restored.open(SessionId("unused"),RoundLength.TEN,999,ContentLanguage.ENGLISH).isEmpty())
            assertArrayEquals(expected,SessionCheckpoint.encode(restored.state!!))
            assertEquals(task.question.choices,restored.state!!.task.question.choices)
            val correct=SessionAction.Answer(restored.state!!.nextAttempt!!,restored.state!!.task.question.correct)
            restored.dispatch(correct);restored.dispatch(correct)
            assertEquals(1,restored.state!!.score)
            val attempts=ProgressFixtures.records(progress).map{it.event}.filterIsInstance<AttemptEvent>()
            assertEquals(2,attempts.size);assertEquals(ContentLanguage.GERMAN,attempts.last().language)
            assertTrue(attempts.last().support.hint);assertEquals(1,attempts.last().support.replays)
            assertEquals(if(mode==MonthsMode.FIND)"skill.months.recognise" else "skill.months.${MonthsQuestions.definition(mode,task.question).kind.name.lowercase()}",attempts.last().evidence.skill.value)
        }
    }
    @Test fun completedRoundsRestoreOnceAndAgainStartsFreshFiveOrTen() {
        for(mode in modes)for(round in RoundLength.entries) {
            val disk=disk();val progress=ProgressFixtures.repository(temp.newFolder());var h=open(mode,disk,progress,round)
            repeat(round.count){answer(h);val action=SessionAction.Next(h.state!!.task.id);h.dispatch(action);h.dispatch(action)}
            val completed=SessionCheckpoint.encode(h.state!!)
            h=open(mode,disk,progress)
            assertArrayEquals(completed,SessionCheckpoint.encode(h.state!!))
            assertEquals(CompletionState.ACKNOWLEDGED,h.state!!.completion)
            val events=ProgressFixtures.records(progress).map{it.event}
            assertEquals(round.count,events.filterIsInstance<AttemptEvent>().size)
            assertEquals(1,events.filterIsInstance<CompletionEvent>().size)
            h.newRound(SessionId("fresh"),round,16,ContentLanguage.GERMAN)
            assertEquals(SessionPhase.ACTIVE,h.state!!.phase);assertEquals(0,h.state!!.index)
            assertTrue(h.state!!.progress.all{it==TaskProgress()})
            assertEquals(SessionId("fresh"),h.state!!.plan.id)
        }
    }
    @Test fun failedAttemptAndCompletionWritesRedeliverWithStableKeys() {
        for(mode in modes) {
            val disk=disk();val real=ProgressFixtures.repository(temp.newFolder());var fail=false
            val uncertain=object:ProgressRepository {
                override fun append(event:ProgressEvent):ProgressWriteResult {
                    val result=real.append(event)
                    return if(fail)ProgressWriteResult.Failed(ProgressFailure.IO) else result
                }
                override fun read(query:ProgressQuery)=real.read(query)
            }
            var h=open(mode,disk,uncertain)
            repeat(5){i ->
                fail=i==0;answer(h)
                if(fail){assertTrue(h.hasPending);fail=false;h=open(mode,disk,uncertain);h.retryWrites()}
                fail=i==4;h.dispatch(SessionAction.Next(h.state!!.task.id))
            }
            assertTrue(h.hasPending);fail=false;h=open(mode,disk,uncertain);h.retryWrites();open(mode,disk,uncertain)
            val events=ProgressFixtures.records(real).map{it.event}
            assertEquals(5,events.filterIsInstance<AttemptEvent>().size);assertEquals(1,events.filterIsInstance<CompletionEvent>().size)
        }
    }
    @Test fun staleWritersAndCorruptJournalsCannotOverwriteAcceptedWork() {
        for(mode in modes) {
            val disk=disk();val progress=ProgressFixtures.repository(temp.newFolder());val old=open(mode,disk,progress)
            val fresh=open(mode,disk,progress);answer(fresh)
            val saved=disk.access{it.read()!!}
            assertThrows(IOException::class.java){answer(old,false)}
            assertArrayEquals(saved,disk.access{it.read()})
            old.retryWrites();assertEquals(1,old.state!!.score)
            val bad=saved.clone().also{it[10]=(it[10].toInt() xor 1).toByte()};disk.access{it.replace(bad)}
            assertTrue(runCatching{open(mode,disk,progress)}.isFailure)
            assertArrayEquals(bad,disk.access{it.read()})
        }
    }
    @Test fun uncertainAtomicCommitRestoresAcceptedAttempt() {
        val disk=disk();var fail=false
        val uncertain=object:ProgressStorage {
            override fun <T> access(block:(ProgressTransaction)->T):T=disk.access{tx -> block(object:ProgressTransaction {
                override fun read()=tx.read()
                override fun replace(bytes:ByteArray){tx.replace(bytes);if(fail)throw IOException("after commit")}
            })}
        }
        val progress=ProgressFixtures.repository(temp.newFolder());val h=open(MonthsMode.RELATIONS,uncertain,progress)
        fail=true;assertThrows(IOException::class.java){answer(h)};fail=false;h.retryWrites()
        assertEquals(1,h.state!!.score);assertEquals(1,ProgressFixtures.records(progress).size)
    }
    @Test fun authoredValidationRejectsChangedWordsOrCorrectIdentity() {
        val original=start(MonthsMode.FIND);val q=original.task.question
        val bad=ChoiceQuestion(q.definition,q.skill,q.context,q.difficulty,ContentText.plain("Changed","Geändert"),q.choices,q.correct,q.correctFeedback,q.wrongFeedback,q.hint)
        val p=original.plan
        val plan=SessionPlan(p.id,p.activity,p.activityRevision,p.contentVersion,p.policy,listOf(ChoiceTask(original.task.id,bad))+p.tasks.drop(1),p.completionText)
        assertTrue(runCatching{MonthsQuestions.validate(MonthsMode.FIND,SessionReducer.start(plan,original.language,MonthContent.repository).state)}.isFailure)
    }
    @Test fun quizAudioLanguagePolicySupportCompletionAndMissingVoice() {
        for(language in ContentLanguage.entries)for(mode in AudioMode.entries) {
            val engine=FakeSpeechEngine();var failure=false
            val audio=MonthsAudio(DefaultAudioController(engine,mode)){failure=it};audio.visible(true)
            assertTrue(engine.spoken.isEmpty())
            val s=start(MonthsMode.RELATIONS)
            val instruction=SessionEffect.Narrate(s.task.question.instruction,NarrationKind.INSTRUCTION)
            audio.effects(s.plan.id,language,listOf(instruction))
            if(mode==AudioMode.OFF)assertTrue(engine.spoken.isEmpty()) else {
                assertEquals(s.task.question.instruction.speech[language],engine.spoken.last().text)
                assertEquals(language,engine.spoken.last().context.language)
            }
            val count=engine.spoken.size
            audio.effects(s.plan.id,language,listOf(SessionEffect.Narrate(s.task.question.correctFeedback,NarrationKind.FEEDBACK)))
            assertEquals(count+if(mode==AudioMode.ALL)1 else 0,engine.spoken.size)
            audio.effects(s.plan.id,language,listOf(SessionEffect.Narrate(s.task.question.hint!!,NarrationKind.INSTRUCTION,SpeechTrigger.MANUAL)))
            if(mode!=AudioMode.OFF) {
                engine.emit(engine.spoken.last().id,EngineResult.Failed(SpeechFailure.MISSING_OFFLINE_VOICE));assertTrue(failure)
            }
            audio.cancel();assertFalse(failure)
            val before=engine.spoken.size
            audio.visible(false);audio.effects(s.plan.id,language,listOf(instruction));audio.visible(true)
            assertEquals(before,engine.spoken.size);audio.close()
        }
    }
}
