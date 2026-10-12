package com.bellfamily.bastischool.learning.months

import com.bellfamily.bastischool.audio.*
import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.session.*
import org.junit.Assert.*
import org.junit.Test

class MonthsMissingTest {
    private fun start(round: RoundLength = RoundLength.FIVE, seed: Long = 7) = SessionReducer.start(
        (MonthsQuestions.generate(MonthsMode.MISSING, SessionId("missing"), round, seed) as GenerationResult.Generated).plan,
        ContentLanguage.ENGLISH, MonthContent.repository).state

    @Test fun catalogueHasEveryMonthAndGapAndUnambiguousCyclicSequences() {
        assertEquals(36, MonthsMissing.catalogue.size)
        assertEquals(36, MonthsMissing.catalogue.map { it.id }.distinct().size)
        MonthIds.canonicalOrder.forEach { month -> assertEquals(3, MonthsMissing.catalogue.count { it.correct == month }) }
        MonthsMissing.catalogue.forEach { d ->
            assertEquals(3, d.months.distinct().size)
            assertEquals(d.correct, d.months[d.gap])
            d.months.zipWithNext().forEach { (a,b) -> assertEquals(MonthIds.next(a), b) }
            ContentLanguage.entries.forEach { language ->
                assertEquals(1, Regex("___").findAll(d.sequence(language)).count())
                assertFalse(d.sequence(language, true).contains(MonthContent.month(d.correct).text.speech[language]))
            }
        }
        assertEquals(listOf(MonthIds.DECEMBER, MonthIds.JANUARY, MonthIds.FEBRUARY),
            MonthsMissing.Definition(MonthIds.DECEMBER, 1).months)
        assertEquals(MonthIds.DECEMBER, MonthsMissing.Definition(MonthIds.NOVEMBER, 1).correct)
        assertEquals(MonthIds.JANUARY, MonthsMissing.Definition(MonthIds.DECEMBER, 1).correct)
        assertThrows(IllegalArgumentException::class.java) { MonthsMissing.Definition(MonthIds.JANUARY, 3) }
    }

    @Test fun roundsAreDeterministicUniqueBalancedAndRestoreExactSequence() {
        val positions = mutableSetOf<Int>()
        for (round in RoundLength.entries) for (seed in 0L..39L) {
            val s = start(round, seed)
            MonthsQuestions.validate(MonthsMode.MISSING, s)
            assertArrayEquals(SessionCheckpoint.encode(s), SessionCheckpoint.encode(start(round, seed)))
            assertEquals(round.count, s.plan.tasks.map { it.question.correct }.distinct().size)
            assertEquals(round.count, s.plan.tasks.map { it.question.definition }.distinct().size)
            val definitions = s.plan.tasks.map { MonthsMissing.definition(it.question) }
            assertEquals(1, definitions.first().gap)
            assertEquals(setOf(0,1,2), definitions.map { it.gap }.toSet())
            val counts = (0..2).map { gap -> definitions.count { it.gap == gap } }
            assertTrue(counts.max() - counts.min() <= 1)
            s.plan.tasks.forEach { task ->
                val q = task.question
                assertEquals(4, q.choices.size); assertEquals(4, q.choices.distinct().size)
                assertEquals(1, q.choices.count { it == q.correct })
                assertTrue(q.choices.all { it in MonthIds.canonicalOrder })
                assertTrue(MonthIds.previous(q.correct) in q.choices && MonthIds.next(q.correct) in q.choices)
                positions += q.choices.indexOf(q.correct)
            }
        }
        assertEquals(setOf(0,1,2,3), positions)
    }

    @Test fun bilingualPromptSequenceAndHintDoNotLeakMissingMonth() {
        MonthsMissing.catalogue.forEach { d ->
            val q = MonthsMissing.question(d)
            assertEquals("Which month is missing?", q.instruction.display.en)
            assertEquals("Welcher Monat fehlt?", q.instruction.display.de)
            assertEquals("Which month is missing? ${d.sequence(ContentLanguage.ENGLISH, true)}.", q.instruction.speech.en)
            assertEquals("Welcher Monat fehlt? ${d.sequence(ContentLanguage.GERMAN, true)}.", q.instruction.speech.de)
            for (language in ContentLanguage.entries) {
                val missing = MonthContent.month(d.correct).text.speech[language]
                assertFalse(q.instruction.speech[language].contains(missing))
                assertFalse(q.hint!!.speech[language].contains(missing))
                assertTrue(q.correctFeedback.speech[language].contains(missing))
            }
        }
        val d = MonthsMissing.Definition(MonthIds.DECEMBER, 1)
        assertEquals("December → ___ → February", d.sequence(ContentLanguage.ENGLISH))
        assertEquals("Monatsfolge: Dezember, Lücke, Februar", d.description(ContentLanguage.GERMAN))
    }

    @Test fun alteredSequenceWordsIdentityOrCorrectAnswerAreRejected() {
        val s = start(); val q = s.task.question; val p = s.plan
        fun rejects(changed: ChoiceQuestion) {
            val plan = SessionPlan(p.id,p.activity,p.activityRevision,p.contentVersion,p.policy,
                listOf(ChoiceTask(s.task.id, changed)) + p.tasks.drop(1),p.completionText)
            assertTrue(runCatching { MonthsMissing.validate(SessionReducer.start(plan,s.language,MonthContent.repository).state) }.isFailure)
        }
        rejects(ChoiceQuestion(q.definition,q.skill,q.context,q.difficulty,ContentText.plain("Other sequence", "Andere Folge"),
            q.choices,q.correct,q.correctFeedback,q.wrongFeedback,q.hint))
        rejects(ChoiceQuestion(q.definition,q.skill,q.context,q.difficulty,q.instruction,
            q.choices,q.choices.first { it != q.correct },q.correctFeedback,q.wrongFeedback,q.hint))
        rejects(ChoiceQuestion(TaskDefinitionId("task.months.missing.invalid.three.first"),q.skill,q.context,q.difficulty,q.instruction,
            q.choices,q.correct,q.correctFeedback,q.wrongFeedback,q.hint))
    }

    @Test fun replayReadsSequenceInCorrectLanguageWithPolicyAndNoAttempts() {
        for (language in ContentLanguage.entries) for (mode in AudioMode.entries) {
            val engine = FakeSpeechEngine()
            val audio = MonthsAudio(DefaultAudioController(engine, mode))
            audio.visible(true)
            assertTrue(engine.spoken.isEmpty())
            var s = start()
            s = SessionReducer.reduce(s, SessionAction.Language(language)).state
            val result = SessionReducer.reduce(s, SessionAction.Replay(s.task.id))
            audio.effects(s.plan.id, language, result.effects)
            assertEquals(0, result.state.current.attempts)
            if (mode == AudioMode.OFF) assertTrue(engine.spoken.isEmpty()) else {
                assertEquals(s.task.question.instruction.speech[language], engine.spoken.last().text)
                assertEquals(language, engine.spoken.last().context.language)
            }
            val count = engine.spoken.size
            audio.visible(false); audio.effects(s.plan.id,language,result.effects); audio.visible(true)
            assertEquals(count, engine.spoken.size)
            audio.close()
        }
    }
}
