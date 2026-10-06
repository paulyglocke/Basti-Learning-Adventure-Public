package com.bellfamily.bastischool.learning.quantitymatch

import com.bellfamily.bastischool.audio.*
import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.session.*
import com.bellfamily.bastischool.learning.subitising.SubitisingContent
import org.junit.Assert.*
import org.junit.Test

class QuantityMatchTest {
    private fun plan(round: RoundLength, seed: Long) =
        (QuantityMatchContent.generate(SessionId("match"), round, seed) as GenerationResult.Generated).plan

    @Test fun directionsHaveExactBilingualWordingAndSharedPatterns() {
        for (n in 1..5) {
            assertEquals(n, SubitisingContent.pattern(n).size)
            val correct = SubitisingContent.numbers[n - 1]
            val choices = listOf(correct) + SubitisingContent.numbers.filter { it != correct }.take(3)
            val toNumber = QuantityMatchContent.question(n, QuantityMatchContent.Direction.QUANTITY_TO_NUMERAL, choices)
            val toGroup = QuantityMatchContent.question(n, QuantityMatchContent.Direction.NUMERAL_TO_QUANTITY, choices)
            assertEquals("Which number matches this group?", toNumber.instruction.display.en)
            assertEquals("Welche Zahl passt zu dieser Gruppe?", toNumber.instruction.display.de)
            assertEquals("Which group shows $n?", toGroup.instruction.display.en)
            assertEquals("Welche Gruppe zeigt $n?", toGroup.instruction.display.de)
            assertEquals("Look at the whole group, then choose its number.", toNumber.hint!!.display.en)
            assertEquals("Schau dir die ganze Gruppe an und wähle dann die passende Zahl.", toNumber.hint!!.display.de)
            assertEquals("Look for the group with $n dots.", toGroup.hint!!.display.en)
            assertEquals("Suche die Gruppe mit $n Punkten.", toGroup.hint!!.display.de)
            for (q in listOf(toNumber, toGroup)) {
                assertEquals("Yes, $n!", q.correctFeedback.display.en)
                assertEquals("Ja, $n!", q.correctFeedback.display.de)
                assertEquals("Try again.", q.wrongFeedback.display.en)
                assertEquals("Versuch es noch einmal.", q.wrongFeedback.display.de)
            }
        }
    }

    @Test fun fiveAndTenRoundsAreDeterministicBalancedAndUnique() {
        for (round in RoundLength.entries) for (seed in 0L..50L) {
            val first = plan(round, seed)
            val second = plan(round, seed)
            val a = SessionReducer.start(first, ContentLanguage.ENGLISH, SubitisingContent.repository).state
            val b = SessionReducer.start(second, ContentLanguage.ENGLISH, SubitisingContent.repository).state
            assertArrayEquals(SessionCheckpoint.encode(a), SessionCheckpoint.encode(b))
            QuantityMatchContent.validate(a)
            val pairs = first.tasks.map { QuantityMatchContent.definition(it.question) }
            assertEquals(round.count, pairs.size)
            assertEquals(round.count, pairs.toSet().size)
            assertEquals((1..5).toSet(), pairs.map { it.first }.toSet())
            assertTrue(pairs.groupingBy { it.first }.eachCount().values.all { it == round.count / 5 })
            assertEquals(if (round == RoundLength.FIVE) listOf(2, 3) else listOf(5, 5), pairs.groupingBy { it.second }.eachCount().values.sorted())
            assertTrue(pairs.zipWithNext().all { (a, b) -> a.first != b.first })
            first.tasks.forEach { task ->
                assertEquals(4, task.question.choices.toSet().size)
                assertEquals(1, task.question.choices.count { it == task.question.correct })
            }
        }
    }

    @Test fun audioSpeaksOnlyCurrentQuestionAndPreservesLanguageAndPolicy() {
        for (language in ContentLanguage.entries) for (mode in AudioMode.entries) {
            val engine = FakeSpeechEngine()
            val audio = QuantityMatchAudio(DefaultAudioController(engine, mode))
            audio.visible(true)
            assertTrue(engine.spoken.isEmpty())
            val state = SessionReducer.start(plan(RoundLength.FIVE, 4), language, SubitisingContent.repository).state
            var current = state
            repeat(5) { index ->
                audio.replay(current, language)
                if (mode == AudioMode.OFF) assertTrue(engine.spoken.isEmpty()) else {
                    assertEquals(current.task.question.instruction.speech[language], engine.spoken.last().text)
                    assertEquals(language, engine.spoken.last().context.language)
                    val (n, direction) = QuantityMatchContent.definition(current.task.question)
                    assertEquals(direction == QuantityMatchContent.Direction.NUMERAL_TO_QUANTITY, engine.spoken.last().text.contains(n.toString()))
                }
                current = SessionReducer.reduce(current, SessionAction.Answer(current.nextAttempt!!, current.task.question.correct)).state
                if (index < 4) current = SessionReducer.reduce(current, SessionAction.Next(current.task.id)).state
            }
            val before = engine.spoken.size
            audio.visible(false); audio.replay(state, language); audio.visible(true)
            assertEquals(before, engine.spoken.size)
            audio.close()
        }
    }
}
