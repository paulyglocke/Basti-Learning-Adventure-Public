package com.bellfamily.bastischool.learning.months

import com.bellfamily.bastischool.audio.*
import com.bellfamily.bastischool.learning.content.SeasonIds
import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.progress.*
import com.bellfamily.bastischool.learning.session.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class MonthsSeasonsMatchTest {
    @get:Rule val temp = TemporaryFolder()

    @Test fun allTwelveMonthsUseTheMeteorologicalGermanyMappingWithBilingualLabels() {
        val expected = mapOf(
            MonthIds.JANUARY to SeasonIds.WINTER, MonthIds.FEBRUARY to SeasonIds.WINTER,
            MonthIds.MARCH to SeasonIds.SPRING, MonthIds.APRIL to SeasonIds.SPRING, MonthIds.MAY to SeasonIds.SPRING,
            MonthIds.JUNE to SeasonIds.SUMMER, MonthIds.JULY to SeasonIds.SUMMER, MonthIds.AUGUST to SeasonIds.SUMMER,
            MonthIds.SEPTEMBER to SeasonIds.AUTUMN, MonthIds.OCTOBER to SeasonIds.AUTUMN, MonthIds.NOVEMBER to SeasonIds.AUTUMN,
            MonthIds.DECEMBER to SeasonIds.WINTER)
        assertEquals(expected, MonthContent.definitions.associate { it.id to it.season })
        assertEquals(setOf(MonthIds.DECEMBER, MonthIds.JANUARY, MonthIds.FEBRUARY),
            MonthContent.definitions.filter { it.season == SeasonIds.WINTER }.map { it.id }.toSet())
        MonthIds.canonicalOrder.forEach { monthId ->
            val month = MonthContent.month(monthId)
            val season = MonthContent.season(expected.getValue(monthId))
            val question = MonthsSeasonsMatch.question(monthId)
            assertEquals("Place ${month.text.speech.en} with its season. Choose from Spring, Summer, Autumn or Winter.", question.instruction.display.en)
            assertEquals("Ordne ${month.text.speech.de} der passenden Jahreszeit zu. Wähle: Frühling, Sommer, Herbst oder Winter.", question.instruction.display.de)
            assertEquals(SeasonIds.canonicalOrder, question.choices)
            assertEquals(expected.getValue(monthId), question.correct)
            assertTrue(question.correctFeedback.display.en.contains(season.text.display.en))
            assertTrue(question.correctFeedback.display.de.contains(season.text.display.de))
            assertEquals("skill.months.season", question.skill.value)
            assertEquals("context.months.season_mapping", question.context.value)
        }
    }

    @Test fun fiveAndTenRoundPlansAreDeterministicUniqueBalancedAndReachEveryMonth() {
        val reached = mutableSetOf<ContentId>()
        for (round in RoundLength.entries) for (seed in 0L..120L) {
            val id = SessionId("season-$seed-${round.count}")
            val state = started(id, round, seed)
            MonthsSeasonsMatch.validate(state)
            val repeated = started(id, round, seed)
            assertArrayEquals(SessionCheckpoint.encode(state), SessionCheckpoint.encode(repeated))
            val targets = state.plan.tasks.map { MonthsSeasonsMatch.month(it.question) }
            assertEquals(round.count, targets.distinct().size)
            assertTrue(targets.all { it in MonthIds.canonicalOrder })
            assertTrue(state.plan.tasks.all {
                it.question.choices.toSet() == SeasonIds.canonicalOrder.toSet() &&
                    it.question.choices.size == 4 && it.question.correct in it.question.choices
            })
            val counts = targets.groupingBy { MonthContent.month(it).season }.eachCount().values
            assertTrue(counts.maxOrNull()!! - counts.minOrNull()!! <= 1)
            reached += targets
        }
        assertEquals(MonthIds.canonicalOrder.toSet(), reached)
    }

    @Test fun retryReplayHintLanguageRestoreAndProgressUseOnlyTheNewIdentity() {
        val journal = AtomicProgressStorage(temp.newFolder("months-seasons"), JvmAtomicCommit)
        val progress = ProgressFixtures.repository(temp.newFolder("progress"))
        val host = MonthsSeasonsMatch.host(journal, progress)
        host.open(SessionId("months-seasons-session"), RoundLength.FIVE, 54, ContentLanguage.ENGLISH)
        val before = host.state!!
        val task = before.task
        host.dispatch(SessionAction.Replay(task.id))
        host.dispatch(SessionAction.Hint(task.id))
        val wrong = task.question.choices.first { it != task.question.correct }
        host.dispatch(SessionAction.Answer(host.state!!.nextAttempt!!, wrong))
        assertEquals(AnswerState.RETRY_AVAILABLE, host.state!!.current.answer)
        host.dispatch(SessionAction.Retry(AttemptId(task.id, 1)))
        host.dispatch(SessionAction.Language(ContentLanguage.GERMAN))
        host.dispatch(SessionAction.Answer(host.state!!.nextAttempt!!, task.question.correct))

        val attemptEvents = ProgressFixtures.records(progress).map { it.event }.filterIsInstance<AttemptEvent>()
        assertEquals(2, attemptEvents.size)
        assertEquals(MonthsSeasonsMatch.activity, attemptEvents.last().origin.activity)
        assertEquals(MonthsSeasonsMatch.skill, attemptEvents.last().evidence.skill)
        assertEquals(MonthsSeasonsMatch.context, attemptEvents.last().evidence.context)
        assertTrue(attemptEvents.last().support.hint)
        assertEquals(1, attemptEvents.last().support.replays)
        assertEquals(AttemptOutcome.CORRECT, attemptEvents.last().outcome)

        val snapshot = SessionCheckpoint.encode(host.state!!)
        val restored = MonthsSeasonsMatch.host(journal, progress)
        restored.open(SessionId("ignored"), RoundLength.TEN, 999, ContentLanguage.ENGLISH)
        assertArrayEquals(snapshot, SessionCheckpoint.encode(restored.state!!))
        assertEquals(2, ProgressFixtures.records(progress).map { it.event }.filterIsInstance<AttemptEvent>().size)

        while (restored.state!!.phase == SessionPhase.ACTIVE) {
            val current = restored.state!!
            if (current.current.answer == AnswerState.UNANSWERED)
                restored.dispatch(SessionAction.Answer(current.nextAttempt!!, current.task.question.correct))
            restored.dispatch(SessionAction.Next(restored.state!!.task.id))
        }
        val events = ProgressFixtures.records(progress).map { it.event }
        val completion = events.filterIsInstance<CompletionEvent>().single()
        assertEquals(MonthsSeasonsMatch.activity, completion.origin.activity)
        assertEquals(MonthsSeasonsMatch.skill, completion.tasks.first().evidence.skill)
        assertEquals(5, completion.tasks.size)
        assertEquals(6, events.filterIsInstance<AttemptEvent>().size)
        assertEquals(MonthsQuestions.activity(MonthsMode.BETWEEN), MonthsBetween.activity)
        assertNotEquals(MonthsBetween.activity, MonthsSeasonsMatch.activity)
    }

    @Test fun existingMonthsAudioPolicySpeaksBilingualReplayAndHintWithoutAnswerAttempts() {
        for (language in ContentLanguage.entries) for (mode in AudioMode.entries) {
            val engine = FakeSpeechEngine()
            val audio = MonthsAudio(DefaultAudioController(engine, mode))
            audio.visible(true)
            val state = started(SessionId("audio-${language.name}-${mode.name}"), RoundLength.FIVE, 2)
            audio.effects(state.plan.id, language, SessionReducer.reduce(state,
                SessionAction.Replay(state.task.id)).effects)
            assertEquals(0, state.current.attempts)
            if (mode == AudioMode.OFF) assertTrue(engine.spoken.isEmpty()) else {
                assertEquals(state.task.question.instruction.speech[language], engine.spoken.last().text)
                assertEquals(language, engine.spoken.last().context.language)
            }
            val hinted = SessionReducer.reduce(state, SessionAction.Hint(state.task.id))
            audio.effects(state.plan.id, language, hinted.effects)
            assertEquals(0, hinted.state.current.attempts)
            if (mode == AudioMode.OFF) assertTrue(engine.spoken.isEmpty()) else
                assertEquals(state.task.question.hint!!.speech[language], engine.spoken.last().text)
            audio.close()
        }
    }

    private fun started(id: SessionId, round: RoundLength, seed: Long) = SessionReducer.start(
        (MonthsSeasonsMatch.generate(id, round, seed) as GenerationResult.Generated).plan,
        ContentLanguage.ENGLISH, com.bellfamily.bastischool.learning.content.CoreContent.repository()).state
}
