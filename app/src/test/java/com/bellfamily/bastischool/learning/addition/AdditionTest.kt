package com.bellfamily.bastischool.learning.addition

import com.bellfamily.bastischool.audio.*
import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.session.*
import com.bellfamily.bastischool.learning.subitising.SubitisingContent
import org.junit.Assert.*
import org.junit.Test

class AdditionTest {
    private fun plan(round: RoundLength, seed: Long) =
        (AdditionContent.generate(SessionId("addition"), round, seed) as GenerationResult.Generated).plan
    private fun question(fact: AdditionContent.Fact): ChoiceQuestion {
        val correct = SubitisingContent.numbers[fact.total - 1]
        return AdditionContent.question(fact, listOf(correct) + SubitisingContent.numbers.filter { it != correct }.take(3))
    }

    @Test fun finiteCatalogueContainsExactlyTheTenValidFactsAndNoZeroOrLargeTotal() {
        val expected = setOf(1 to 1, 1 to 2, 2 to 1, 1 to 3, 2 to 2, 3 to 1, 1 to 4, 2 to 3, 3 to 2, 4 to 1)
        assertEquals(expected, AdditionContent.catalogue().map { it.left to it.right }.toSet())
        assertEquals(10, AdditionContent.catalogue().size)
        AdditionContent.catalogue().forEach { fact ->
            assertTrue(fact.left >= 1 && fact.right >= 1 && fact.total in 2..5)
            assertEquals(fact.left + fact.right, fact.total)
            val q = question(fact)
            assertEquals(fact, AdditionContent.definition(q))
            assertEquals(SubitisingContent.numbers[fact.total - 1], q.correct)
        }
        for ((a, b) in listOf(0 to 1, 1 to 0, -1 to 2, 1 to 5, 3 to 3, 5 to 1)) {
            assertThrows(IllegalArgumentException::class.java) { AdditionContent.Fact(a, b) }
        }
    }

    @Test fun deterministicRoundsCoverTotalsAndChoicesWithoutAdjacentTotalsOrRepeatedFacts() {
        val positions = mutableSetOf<Int>()
        for (seed in 0L..99L) for (round in RoundLength.entries) {
            val p = plan(round, seed)
            fun start(plan: SessionPlan) = SessionReducer.start(plan, ContentLanguage.ENGLISH, AdditionContent.repository).state
            val state = start(p)
            assertArrayEquals(SessionCheckpoint.encode(state), SessionCheckpoint.encode(start(plan(round, seed))))
            AdditionContent.validate(state)
            val facts = p.tasks.map { AdditionContent.definition(it.question) }
            assertEquals(round.count, facts.toSet().size)
            assertEquals(round.count, p.tasks.map { it.question.definition }.toSet().size)
            assertEquals(setOf(2, 3, 4, 5), facts.map { it.total }.toSet())
            assertTrue(facts.zipWithNext().all { (a, b) -> a.total != b.total })
            if (round == RoundLength.FIVE) assertEquals(5, facts.map { it.unordered }.toSet().size)
            else {
                assertEquals(AdditionContent.catalogue().toSet(), facts.toSet())
                assertEquals(mapOf(2 to 1, 3 to 2, 4 to 3, 5 to 4), facts.groupingBy { it.total }.eachCount())
            }
            p.tasks.forEach { task ->
                assertEquals(4, task.question.choices.toSet().size)
                assertEquals(1, task.question.choices.count { it == task.question.correct })
                assertTrue(task.question.choices.all { it in SubitisingContent.numbers })
                positions += task.question.choices.indexOf(task.question.correct)
            }
        }
        assertEquals(setOf(0, 1, 2, 3), positions)
    }

    @Test fun exactBilingualPromptFeedbackAndHelpAndNoFalseTotalInTaskIdentity() {
        AdditionContent.catalogue().forEach { fact ->
            val q = question(fact)
            assertEquals("How many altogether?", q.instruction.display.en)
            assertEquals("Wie viele sind es zusammen?", q.instruction.display.de)
            assertEquals("Yes, ${fact.total}!", q.correctFeedback.display.en)
            assertEquals("Ja, ${fact.total}!", q.correctFeedback.display.de)
            assertEquals("Try again.", q.wrongFeedback.display.en)
            assertEquals("Versuch es noch einmal.", q.wrongFeedback.display.de)
            assertEquals("Put both groups together in your head and count how many there are altogether.", q.hint!!.display.en)
            assertEquals("Stell dir beide Gruppen zusammen vor und zähle, wie viele es insgesamt sind.", q.hint!!.display.de)
        }
        val q = question(AdditionContent.Fact(1, 1))
        val corrupt = ChoiceQuestion(TaskDefinitionId("task.math.add_within_5.one.one.three"),
            q.skill, q.context, q.difficulty, q.instruction, q.choices, q.correct, q.correctFeedback, q.wrongFeedback, q.hint)
        assertThrows(IllegalArgumentException::class.java) { AdditionContent.definition(corrupt) }
        assertEquals("First group, Two dots", AdditionContent.groupDescription(2, true, ContentLanguage.ENGLISH))
        assertEquals("Zweite Gruppe, Ein Punkt", AdditionContent.groupDescription(1, false, ContentLanguage.GERMAN))
    }

    @Test fun audioReplayOnlyPromptInBothLanguagesWithExistingPolicyAndSilentNavigation() {
        for (language in ContentLanguage.entries) for (mode in AudioMode.entries) {
            val engine = FakeSpeechEngine()
            val audio = AdditionAudio(DefaultAudioController(engine, mode))
            audio.visible(true); assertTrue(engine.spoken.isEmpty())
            var state = SessionReducer.start(plan(RoundLength.TEN, 3), language, AdditionContent.repository).state
            repeat(10) {
                val replay = SessionReducer.reduce(state, SessionAction.Replay(state.task.id))
                audio.effects(state.plan.id, language, replay.effects)
                if (mode == AudioMode.OFF) assertTrue(engine.spoken.isEmpty()) else {
                    // Exact prompt equality rules out spoken addends, equations and the correct total.
                    assertEquals(AdditionContent.prompt.speech[language], engine.spoken.last().text)
                    assertEquals(language, engine.spoken.last().context.language)
                }
                val before = engine.spoken.size
                val answer = SessionReducer.reduce(replay.state, SessionAction.Answer(replay.state.nextAttempt!!, replay.state.task.question.correct))
                audio.effects(state.plan.id, language, answer.effects)
                assertEquals(before + if (mode == AudioMode.ALL) 1 else 0, engine.spoken.size)
                state = SessionReducer.reduce(answer.state, SessionAction.Next(answer.state.task.id)).state
            }
            val before = engine.spoken.size
            audio.visible(false); audio.replay(state, language); audio.visible(true)
            assertEquals(before, engine.spoken.size)
            audio.close()
        }
    }
}
