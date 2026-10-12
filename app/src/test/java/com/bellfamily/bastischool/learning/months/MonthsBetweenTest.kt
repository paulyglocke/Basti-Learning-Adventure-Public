package com.bellfamily.bastischool.learning.months

import com.bellfamily.bastischool.audio.*
import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.progress.*
import com.bellfamily.bastischool.learning.session.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class MonthsBetweenTest {
    @get:Rule val temp = TemporaryFolder()
    private fun state(round: RoundLength = RoundLength.FIVE, seed: Long = 17) = SessionReducer.start(
        (MonthsBetween.generate(SessionId("between"), round, seed) as GenerationResult.Generated).plan,
        ContentLanguage.ENGLISH, MonthContent.repository).state

    @Test fun catalogueCoversEveryMiddleMonthAndBothYearBoundaries() {
        assertThrows(IllegalArgumentException::class.java) { MonthsQuestions.catalogue(MonthsMode.BETWEEN) }
        assertEquals(MonthIds.canonicalOrder, MonthsBetween.catalogue.map { it.middle })
        MonthsBetween.catalogue.forEach { d ->
            assertEquals(MonthIds.next(d.before), d.middle)
            assertEquals(MonthIds.next(d.middle), d.after)
            assertEquals(4, d.choices.distinct().size)
            assertTrue(d.middle in d.choices)
        }
        val november = MonthsBetween.Definition(MonthIds.DECEMBER)
        assertEquals(MonthIds.NOVEMBER, november.before)
        assertEquals(MonthIds.JANUARY, november.after)
        val december = MonthsBetween.Definition(MonthIds.JANUARY)
        assertEquals(MonthIds.DECEMBER, december.before)
        assertEquals(MonthIds.FEBRUARY, december.after)
    }

    @Test fun fiveAndTenRoundsAreReproducibleUniqueAndUseFourPlausibleChoices() {
        val chosen = mutableSetOf<ContentId>()
        for (round in RoundLength.entries) for (seed in 0L..80L) {
            val current = state(round, seed)
            MonthsBetween.validate(current)
            assertArrayEquals(SessionCheckpoint.encode(current), SessionCheckpoint.encode(state(round, seed)))
            val defs = current.plan.tasks.map { MonthsBetween.definition(it.question) }
            assertEquals(round.count, defs.map { it.middle }.distinct().size)
            current.plan.tasks.forEach { task ->
                val q = task.question
                assertEquals(4, q.choices.size)
                assertEquals(4, q.choices.toSet().size)
                assertEquals(1, q.choices.count { it == q.correct })
                assertTrue(q.choices.all { it in MonthIds.canonicalOrder })
                assertTrue(MonthIds.previous(MonthIds.previous(q.correct)) in q.choices)
                assertTrue(MonthIds.previous(q.correct) in q.choices)
                assertTrue(MonthIds.next(q.correct) in q.choices)
                chosen += q.correct
            }
        }
        assertEquals(MonthIds.canonicalOrder.toSet(), chosen)
    }

    @Test fun bilingualPromptHintAndFeedbackNameTheRelationshipWithoutLeakingAnswer() {
        MonthsBetween.catalogue.forEach { d ->
            val q = MonthsBetween.question(d)
            val before = MonthContent.month(d.before).text.speech
            val after = MonthContent.month(d.after).text.speech
            val middle = MonthContent.month(d.middle).text.speech
            assertEquals("Which month comes between ${before.en} and ${after.en}?", q.instruction.display.en)
            assertEquals("Welcher Monat kommt zwischen ${before.de} und ${after.de}?", q.instruction.display.de)
            assertEquals(q.instruction.display, q.instruction.speech)
            assertTrue(q.hint!!.speech.en.contains(before.en))
            assertTrue(q.hint!!.speech.de.contains(before.de))
            assertFalse(q.hint!!.speech.en.contains(middle.en))
            assertFalse(q.hint!!.speech.de.contains(middle.de))
            assertTrue(q.correctFeedback.speech.en.contains(middle.en))
            assertTrue(q.correctFeedback.speech.de.contains(middle.de))
            assertNotEquals(MonthsMissing.question(MonthsMissing.Definition(d.before, 1)).instruction.display,
                q.instruction.display)
            assertEquals("skill.months.between", q.skill.value)
        }
    }

    @Test fun retryReplayHintLanguageAndProgressUseIsolatedBetweenIdentity() {
        val disk = AtomicProgressStorage(temp.newFolder(), JvmAtomicCommit)
        val progress = ProgressFixtures.repository(temp.newFolder())
        val host = MonthsBetween.host(disk, progress)
        host.open(SessionId("between-round"), RoundLength.FIVE, 9, ContentLanguage.ENGLISH)
        val task = host.state!!.task
        host.dispatch(SessionAction.Replay(task.id))
        host.dispatch(SessionAction.Hint(task.id))
        val wrong = task.question.choices.first { it != task.question.correct }
        host.dispatch(SessionAction.Answer(host.state!!.nextAttempt!!, wrong))
        host.dispatch(SessionAction.Retry(AttemptId(task.id, 1)))
        host.dispatch(SessionAction.Language(ContentLanguage.GERMAN))
        host.dispatch(SessionAction.Answer(host.state!!.nextAttempt!!, task.question.correct))
        val attempts = ProgressFixtures.records(progress).map { it.event }.filterIsInstance<AttemptEvent>()
        assertEquals(2, attempts.size)
        assertEquals(MonthsBetween.activity, attempts.last().origin.activity)
        assertEquals(MonthsBetween.skill, attempts.last().evidence.skill)
        assertTrue(attempts.last().support.hint)
        assertEquals(1, attempts.last().support.replays)
        val saved = SessionCheckpoint.encode(host.state!!)
        val restored = MonthsBetween.host(disk, progress)
        restored.open(SessionId("unused"), RoundLength.TEN, 999, ContentLanguage.ENGLISH)
        assertArrayEquals(saved, SessionCheckpoint.encode(restored.state!!))
        assertEquals(task.question.choices, restored.state!!.task.question.choices)
        assertNotEquals(MonthsQuestions.activity(MonthsMode.MISSING), restored.state!!.plan.activity)
        while (restored.state!!.phase == SessionPhase.ACTIVE) {
            val current = restored.state!!
            if (current.current.answer == AnswerState.UNANSWERED)
                restored.dispatch(SessionAction.Answer(current.nextAttempt!!, current.task.question.correct))
            restored.dispatch(SessionAction.Next(restored.state!!.task.id))
        }
        val completion = ProgressFixtures.records(progress).map { it.event }.filterIsInstance<CompletionEvent>().single()
        assertEquals(MonthsBetween.activity, completion.origin.activity)
        assertEquals(RoundLength.FIVE.count, completion.tasks.size)
    }

    @Test fun replayUsesExistingMonthsAudioLanguageAndPolicy() {
        for (language in ContentLanguage.entries) for (mode in AudioMode.entries) {
            val engine = FakeSpeechEngine()
            val audio = MonthsAudio(DefaultAudioController(engine, mode))
            audio.visible(true)
            val current = SessionReducer.reduce(state(), SessionAction.Language(language)).state
            val replay = SessionReducer.reduce(current, SessionAction.Replay(current.task.id))
            audio.effects(current.plan.id, language, replay.effects)
            assertEquals(0, replay.state.current.attempts)
            if (mode == AudioMode.OFF) assertTrue(engine.spoken.isEmpty()) else {
                assertEquals(current.task.question.instruction.speech[language], engine.spoken.last().text)
                assertEquals(language, engine.spoken.last().context.language)
            }
            audio.close()
        }
    }
}
