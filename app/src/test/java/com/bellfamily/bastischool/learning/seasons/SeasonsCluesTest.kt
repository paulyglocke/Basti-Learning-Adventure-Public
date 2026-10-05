package com.bellfamily.bastischool.learning.seasons

import com.bellfamily.bastischool.audio.*
import com.bellfamily.bastischool.learning.content.SeasonIds
import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.progress.*
import com.bellfamily.bastischool.learning.session.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class SeasonsCluesTest {
    @get:Rule val temp = TemporaryFolder()
    private fun plan(round: RoundLength = RoundLength.FIVE, seed: Long = 42) =
        (SeasonsClues.generate(SessionId("clues"), round, seed) as GenerationResult.Generated).plan

    @Test fun catalogueHasTwoDistinctBilingualCluesPerCanonicalSeasonWithoutNamingTheAnswer() {
        val clues = SeasonsClues.catalogue
        assertEquals(8, clues.map { it.definition }.distinct().size)
        assertEquals(SeasonIds.canonicalOrder.associateWith { 2 }, clues.groupingBy { it.correct }.eachCount())
        for (language in ContentLanguage.entries) {
            assertEquals(8, clues.map { it.instruction.display[language] }.distinct().size)
            for (clue in clues) {
                assertEquals(SeasonIds.canonicalOrder.toSet(), clue.choices.toSet())
                assertEquals(clue.instruction.display[language], clue.instruction.speech[language])
                assertFalse(clue.instruction.speech[language].contains(
                    SeasonsContent.season(clue.correct).text.speech[language], ignoreCase = true))
                assertNotNull(clue.hint)
            }
        }
    }

    @Test fun seededRoundsFreezeClueIdentityWordingAndChoicesAndVisitAllEightBeforeRepeating() {
        for (round in RoundLength.entries) for (seed in 0L..20L) {
            val a = SessionReducer.start(plan(round, seed), ContentLanguage.ENGLISH, SeasonsContent.repository).state
            val b = SessionReducer.start(plan(round, seed), ContentLanguage.ENGLISH, SeasonsContent.repository).state
            assertArrayEquals(SessionCheckpoint.encode(a), SessionCheckpoint.encode(b))
            val firstCycle = a.plan.tasks.take(8).map { it.question.definition }
            assertEquals(minOf(8, round.count), firstCycle.distinct().size)
            assertEquals(SeasonIds.canonicalOrder.toSet(), a.plan.tasks.map { it.question.correct }.toSet())
            assertTrue(a.plan.tasks.all { it.question.choices.size == 4 &&
                it.question.choices.toSet() == SeasonIds.canonicalOrder.toSet() })
            if (round == RoundLength.FIVE) {
                assertEquals(listOf(1, 1, 1, 2), a.plan.tasks.groupingBy { it.question.correct }.eachCount().values.sorted())
            } else {
                // The bounded five-question selection must not alter the original ten-question algorithm.
                val original = (CandidateTaskGenerator(SeasonsClues.catalogue).generate(
                    GenerationRequest(a.plan.id, SeasonsClues.activity, SeasonsClues.REVISION,
                        SessionPolicy(round), seed, SeasonsContent.completion), SeasonsContent.repository) as GenerationResult.Generated).plan
                val originalState = SessionReducer.start(original, ContentLanguage.ENGLISH, SeasonsContent.repository).state
                assertArrayEquals(SessionCheckpoint.encode(originalState), SessionCheckpoint.encode(a))
            }
            if (round == RoundLength.TEN) assertEquals(SeasonsClues.catalogue.map { it.definition }.toSet(), firstCycle.toSet())
            SeasonsClues.validate(a)
            val restored = SessionCheckpoint.restore(SessionCheckpoint.encode(a), SeasonsClues.activity,
                SeasonsClues.REVISION, SeasonsContent.repository) as SessionRestoreResult.Restored
            SeasonsClues.validate(restored.state)
            assertArrayEquals(SessionCheckpoint.encode(a), SessionCheckpoint.encode(restored.state))
        }
    }

    @Test fun validatorRejectsWrongAnswerOrChangedClueUnderAnExistingDefinition() {
        val original = plan()
        val q = original.tasks.first().question
        for (changeAnswer in listOf(true, false)) {
            val changed = ChoiceQuestion(q.definition, q.skill, q.context, q.difficulty,
                if (changeAnswer) q.instruction else ContentText.plain("Another clue.", "Ein anderer Hinweis."),
                q.choices, if (changeAnswer) q.choices.first { it != q.correct } else q.correct,
                q.correctFeedback, q.wrongFeedback, q.hint)
            val badPlan = SessionPlan(original.id, original.activity, original.activityRevision, original.contentVersion,
                original.policy, original.tasks.mapIndexed { i, task -> if (i == 0) ChoiceTask(task.id, changed) else task },
                original.completionText)
            val state = SessionReducer.start(badPlan, ContentLanguage.ENGLISH, SeasonsContent.repository).state
            assertThrows(IllegalArgumentException::class.java) { SeasonsClues.validate(state) }
        }
    }

    @Test fun journalRestoresRetryHelpLanguageAndCompletionWithoutRegenerationOrDuplicateProgress() {
        val disk = AtomicProgressStorage(temp.newFolder(), JvmAtomicCommit)
        val progress = ProgressFixtures.repository(temp.newFolder())
        fun host() = SeasonsClues.host(disk, progress)
        var h = host()
        h.open(SessionId("clue-round"), RoundLength.FIVE, 42, ContentLanguage.ENGLISH)
        val first = h.state!!
        h.dispatch(SessionAction.Replay(first.task.id))
        h.dispatch(SessionAction.Hint(first.task.id))
        h.dispatch(SessionAction.Answer(first.nextAttempt!!, first.task.question.choices.first { it != first.task.question.correct }))
        val saved = SessionCheckpoint.encode(h.state!!)
        h = host()
        assertTrue(h.open(SessionId("unused"), RoundLength.TEN, 999, ContentLanguage.GERMAN).isEmpty())
        assertArrayEquals(saved, SessionCheckpoint.encode(h.state!!))
        assertEquals(AnswerState.RETRY_AVAILABLE, h.state!!.current.answer)
        h.dispatch(SessionAction.Retry(AttemptId(first.task.id, 1)))
        h.dispatch(SessionAction.Language(ContentLanguage.GERMAN))
        assertEquals(first.task.question.definition, h.state!!.task.question.definition)
        assertEquals(first.task.question.choices, h.state!!.task.question.choices)
        assertTrue(h.state!!.current.support.hint)
        repeat(5) {
            val s = h.state!!
            val answer = SessionAction.Answer(s.nextAttempt!!, s.task.question.correct)
            h.dispatch(answer); h.dispatch(answer)
            h.dispatch(SessionAction.Next(s.task.id))
        }
        assertEquals(CompletionState.ACKNOWLEDGED, h.state!!.completion)
        val done = SessionCheckpoint.encode(h.state!!)
        val records = ProgressFixtures.records(progress)
        h = host()
        assertTrue(h.open(SessionId("unused-again"), RoundLength.TEN, 0, ContentLanguage.ENGLISH).isEmpty())
        assertArrayEquals(done, SessionCheckpoint.encode(h.state!!))
        assertEquals(records, ProgressFixtures.records(progress))
        assertEquals(6, records.count { it.event is AttemptEvent })
        assertEquals(1, records.count { it.event is CompletionEvent })
        val selection = SeasonsSelection(SeasonIds.AUTUMN, SeasonsPhase.CLUES)
        val store = SeasonsSelectionStore(AtomicProgressStorage(temp.newFolder(), JvmAtomicCommit))
        store.write(selection)
        assertEquals(selection, store.read())
    }

    @Test fun clueAndReplayUseExistingAudioPolicyInBothLanguages() {
        for (mode in AudioMode.entries) for (language in ContentLanguage.entries) {
            val engine = FakeSpeechEngine()
            val audio = SeasonsAudio(DefaultAudioController(engine, mode))
            audio.visible(true)
            val start = SessionReducer.start(plan(), language, SeasonsContent.repository)
            audio.effects(start.state, start.effects)
            val replay = SessionReducer.reduce(start.state, SessionAction.Replay(start.state.task.id))
            audio.effects(replay.state, replay.effects)
            if (mode == AudioMode.OFF) assertTrue(engine.spoken.isEmpty()) else {
                assertEquals(2, engine.spoken.size)
                assertTrue(engine.spoken.all { it.text == start.state.task.question.instruction.speech[language] })
                assertTrue(engine.spoken.all { it.context.language == language })
                assertEquals(SpeechTrigger.REPLAY, engine.spoken.last().trigger)
            }
            audio.close()
        }
    }
}
