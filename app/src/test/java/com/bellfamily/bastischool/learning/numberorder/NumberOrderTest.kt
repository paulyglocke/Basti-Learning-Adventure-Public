package com.bellfamily.bastischool.learning.numberorder

import com.bellfamily.bastischool.audio.*
import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.session.*
import com.bellfamily.bastischool.learning.subitising.SubitisingContent
import org.junit.Assert.*
import org.junit.Test

class NumberOrderTest {
    private fun plan(round: RoundLength, seed: Long) =
        (NumberOrderContent.generate(SessionId("order"), round, seed) as GenerationResult.Generated).plan
    private fun question(item: NumberOrderContent.Item): ChoiceQuestion {
        val correct = SubitisingContent.numbers[item.answer - 1]
        return NumberOrderContent.question(item, listOf(correct) + SubitisingContent.numbers.filter { it != correct }.take(3))
    }
    @Test fun neighboursNeverWrapAndAllMissingSequencesAreAscendingWithOneGap() {
        for (n in 2..5) assertEquals(n - 1, NumberOrderContent.Neighbour(NumberOrderContent.Type.BEFORE, n).answer)
        for (n in 1..4) assertEquals(n + 1, NumberOrderContent.Neighbour(NumberOrderContent.Type.AFTER, n).answer)
        assertThrows(IllegalArgumentException::class.java) { NumberOrderContent.Neighbour(NumberOrderContent.Type.BEFORE, 1) }
        assertThrows(IllegalArgumentException::class.java) { NumberOrderContent.Neighbour(NumberOrderContent.Type.AFTER, 5) }
        assertThrows(IllegalArgumentException::class.java) { NumberOrderContent.Neighbour(NumberOrderContent.Type.MISSING, 3) }
        assertThrows(IllegalArgumentException::class.java) { NumberOrderContent.Missing(4, 3, 0) }
        assertThrows(IllegalArgumentException::class.java) { NumberOrderContent.Missing(1, 5, 2) }
        assertThrows(IllegalArgumentException::class.java) { NumberOrderContent.Missing(1, 3, 3) }
        val items = NumberOrderContent.catalogue()
        assertEquals(25, items.size) // Eight neighbours, nine length-3 and eight length-4 sequences.
        items.forEach { item ->
            assertTrue(item.answer in 1..5)
            assertEquals(item, NumberOrderContent.definition(question(item)))
            if (item is NumberOrderContent.Missing) {
                assertTrue(item.tiles.size in 3..4)
                assertEquals(1, item.tiles.count { it == null })
                val completed = item.tiles.map { it ?: item.answer }
                assertTrue(completed.all { it in 1..5 })
                assertTrue(completed.zipWithNext().all { (a, b) -> b == a + 1 })
            }
        }
        assertEquals(listOf(1, 2, null, 4), NumberOrderContent.Missing(1, 4, 2).tiles)
        assertEquals(listOf(null, 4, 5), NumberOrderContent.Missing(3, 3, 0).tiles)
    }

    @Test fun seededRoundsHaveAllTypesFourChoicesAndVariedCorrectPositions() {
        val positions = mutableSetOf<Int>()
        for (seed in 0L..99L) for (round in RoundLength.entries) {
            val p = plan(round, seed)
            fun start(plan: SessionPlan) = SessionReducer.start(plan, ContentLanguage.ENGLISH, NumberOrderContent.repository).state
            val state = start(p)
            assertArrayEquals(SessionCheckpoint.encode(state), SessionCheckpoint.encode(start(plan(round, seed))))
            NumberOrderContent.validate(state)
            val items = p.tasks.map { NumberOrderContent.definition(it.question) }
            assertEquals(round.count, items.toSet().size)
            val counts = items.groupingBy { it.type }.eachCount()
            assertEquals(if (round == RoundLength.FIVE) 2 else 3, counts[NumberOrderContent.Type.BEFORE])
            assertEquals(if (round == RoundLength.FIVE) 2 else 3, counts[NumberOrderContent.Type.AFTER])
            assertEquals(if (round == RoundLength.FIVE) 1 else 4, counts[NumberOrderContent.Type.MISSING])
            p.tasks.forEach {
                assertEquals(4, it.question.choices.toSet().size)
                assertEquals(1, it.question.choices.count { choice -> choice == it.question.correct })
                assertTrue(it.question.choices.all { choice -> choice in SubitisingContent.numbers })
                positions += it.question.choices.indexOf(it.question.correct)
            }
            // A repeated answer is only permitted when no different remaining answer is available.
            items.zipWithNext().forEachIndexed { i, (a, b) ->
                if (a.answer == b.answer) assertTrue(items.drop(i + 1).all { it.answer == a.answer })
            }
        }
        assertEquals(setOf(0, 1, 2, 3), positions)
    }

    @Test fun exactEnglishGermanQuestionHelpFeedbackAndSequenceSemantics() {
        NumberOrderContent.catalogue().forEach { item ->
            val q = question(item)
            when (item) {
                is NumberOrderContent.Neighbour -> {
                    val before = item.type == NumberOrderContent.Type.BEFORE
                    assertEquals("What comes ${if (before) "before" else "after"} ${item.anchor}?", q.instruction.display.en)
                    assertEquals("Was kommt ${if (before) "vor" else "nach"} der ${item.anchor}?", q.instruction.display.de)
                    assertEquals("Think about the number just ${if (before) "before" else "after"} ${item.anchor}.", q.hint!!.display.en)
                    assertEquals("Denk an die Zahl direkt ${if (before) "vor" else "nach"} der ${item.anchor}.", q.hint!!.display.de)
                }
                is NumberOrderContent.Missing -> {
                    assertEquals("Which number is missing?", q.instruction.display.en)
                    assertEquals("Welche Zahl fehlt?", q.instruction.display.de)
                    assertEquals("Read the numbers in order and find the gap.", q.hint!!.display.en)
                    assertEquals("Lies die Zahlen der Reihe nach und finde die Lücke.", q.hint!!.display.de)
                }
            }
            assertEquals("Yes, ${item.answer}!", q.correctFeedback.display.en)
            assertEquals("Ja, ${item.answer}!", q.correctFeedback.display.de)
            assertEquals("Try again.", q.wrongFeedback.display.en)
            assertEquals("Versuch es noch einmal.", q.wrongFeedback.display.de)
        }
        val sequence = NumberOrderContent.Missing(1, 4, 2)
        assertEquals("Sequence: 1, 2, blank, 4", NumberOrderContent.sequenceDescription(sequence, ContentLanguage.ENGLISH))
        assertEquals("Zahlenfolge: 1, 2, Lücke, 4", NumberOrderContent.sequenceDescription(sequence, ContentLanguage.GERMAN))
    }

    @Test fun audioReplaysCurrentQuestionInBothLanguagesWithoutAnswerOrSequenceLeakage() {
        for (language in ContentLanguage.entries) for (mode in AudioMode.entries) {
            val engine = FakeSpeechEngine()
            val audio = NumberOrderAudio(DefaultAudioController(engine, mode))
            audio.visible(true); assertTrue(engine.spoken.isEmpty())
            var state = SessionReducer.start(plan(RoundLength.TEN, 3), language, NumberOrderContent.repository).state
            repeat(10) {
                val q = state.task.question
                val replay = SessionReducer.reduce(state, SessionAction.Replay(state.task.id))
                audio.effects(state.plan.id, language, replay.effects)
                if (mode != AudioMode.OFF) {
                    assertEquals(q.instruction.speech[language], engine.spoken.last().text)
                    assertEquals(language, engine.spoken.last().context.language)
                    assertFalse(engine.spoken.last().text.contains(SubitisingContent.quantity(q.correct).toString()))
                } else assertTrue(engine.spoken.isEmpty())
                val before = engine.spoken.size
                val answered = SessionReducer.reduce(replay.state, SessionAction.Answer(replay.state.nextAttempt!!, q.correct))
                audio.effects(state.plan.id, language, answered.effects)
                assertEquals(before + if (mode == AudioMode.ALL) 1 else 0, engine.spoken.size)
                state = SessionReducer.reduce(answered.state, SessionAction.Next(answered.state.task.id)).state
            }
            val before = engine.spoken.size
            audio.visible(false); audio.replay(state, language); audio.visible(true)
            assertEquals(before, engine.spoken.size)
            audio.close()
        }
    }
}
