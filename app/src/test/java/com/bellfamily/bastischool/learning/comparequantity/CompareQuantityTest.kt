package com.bellfamily.bastischool.learning.comparequantity

import com.bellfamily.bastischool.audio.*
import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.session.*
import org.junit.Assert.*
import org.junit.Test

class CompareQuantityTest {
    private fun plan(round: RoundLength, seed: Long) =
        (CompareQuantityContent.generate(SessionId("compare"), round, seed) as GenerationResult.Generated).plan

    @Test fun allTwentyFivePairsHaveCorrectSemanticAnswersAndExactBilingualWords() {
        assertEquals(listOf("Left", "Right", "Same"), CompareQuantityContent.answers.map { CompareQuantityContent.label(it).display.en })
        assertEquals(listOf("Links", "Rechts", "Gleich"), CompareQuantityContent.answers.map { CompareQuantityContent.label(it).display.de })
        for (left in 1..5) for (right in 1..5) {
            val pair = CompareQuantityContent.PairOfGroups(left, right)
            val q = CompareQuantityContent.question(pair)
            assertEquals(ContentId("comparison.${if (left > right) "left" else if (right > left) "right" else "same"}"), q.correct)
            assertEquals(pair, CompareQuantityContent.definition(q))
            assertEquals(3, q.choices.toSet().size)
            assertEquals(1, q.choices.count { it == q.correct })
            assertEquals("Which side has more?", q.instruction.display.en)
            assertEquals("Welche Seite hat mehr?", q.instruction.display.de)
            assertEquals("Yes!", q.correctFeedback.display.en)
            assertEquals("Ja!", q.correctFeedback.display.de)
            assertEquals("Try again.", q.wrongFeedback.display.en)
            assertEquals("Versuch es noch einmal.", q.wrongFeedback.display.de)
            assertEquals("Look at both groups. Compare how many dots each one has.", q.hint!!.display.en)
            assertEquals("Schau dir beide Gruppen an. Vergleiche, wie viele Punkte jede Gruppe hat.", q.hint!!.display.de)
        }
        assertThrows(IllegalArgumentException::class.java) { CompareQuantityContent.PairOfGroups(0, 3) }
        assertThrows(IllegalArgumentException::class.java) { CompareQuantityContent.PairOfGroups(3, 6) }
    }

    @Test fun seededRoundsAreBalancedVariedUniqueAndExactlyRestorable() {
        val firstRelations = mutableSetOf<CompareQuantityContent.Relation>()
        for (seed in 0L..99L) for (round in RoundLength.entries) {
            val p = plan(round, seed)
            val state = SessionReducer.start(p, ContentLanguage.ENGLISH, CompareQuantityContent.repository).state
            val again = SessionReducer.start(plan(round, seed), ContentLanguage.ENGLISH, CompareQuantityContent.repository).state
            assertArrayEquals(SessionCheckpoint.encode(state), SessionCheckpoint.encode(again))
            CompareQuantityContent.validate(state)
            val pairs = p.tasks.map { CompareQuantityContent.definition(it.question) }
            firstRelations += pairs.first().relation
            assertEquals(round.count, pairs.toSet().size)
            assertEquals(round.count, p.tasks.map { it.question.definition }.toSet().size)
            assertTrue(pairs.all { it.left in 1..5 && it.right in 1..5 })
            assertTrue(pairs.flatMap { listOf(it.left, it.right) }.toSet().size >= 3)
            val counts = pairs.groupingBy { it.relation }.eachCount()
            val f = round.count / 5
            assertEquals(2 * f, counts[CompareQuantityContent.Relation.LEFT_MORE])
            assertEquals(2 * f, counts[CompareQuantityContent.Relation.RIGHT_MORE])
            assertEquals(f, counts[CompareQuantityContent.Relation.SAME])
            assertTrue(pairs.zipWithNext().all { it.first != it.second })
            val restored = SessionCheckpoint.restore(SessionCheckpoint.encode(state), CompareQuantityContent.activity,
                CompareQuantityContent.REVISION, CompareQuantityContent.repository) as SessionRestoreResult.Restored
            assertArrayEquals(SessionCheckpoint.encode(state), SessionCheckpoint.encode(restored.state))
        }
        assertEquals(CompareQuantityContent.Relation.entries.toSet(), firstRelations)
    }

    @Test fun replayContainsOnlyPromptAcrossBothLanguagesAllPoliciesAndNavigation() {
        for (language in ContentLanguage.entries) for (mode in AudioMode.entries) {
            val engine = FakeSpeechEngine()
            val audio = CompareQuantityAudio(DefaultAudioController(engine, mode))
            audio.visible(true)
            assertTrue(engine.spoken.isEmpty())
            var state = SessionReducer.start(plan(RoundLength.FIVE, 4), language, CompareQuantityContent.repository).state
            repeat(5) {
                val before = engine.spoken.size
                val replay = SessionReducer.reduce(state, SessionAction.Replay(state.task.id))
                audio.effects(state.plan.id, language, replay.effects)
                if (mode == AudioMode.OFF) assertTrue(engine.spoken.isEmpty()) else {
                    assertEquals(CompareQuantityContent.prompt.speech[language], engine.spoken.last().text)
                    assertEquals(language, engine.spoken.last().context.language)
                    assertEquals(before + 1, engine.spoken.size)
                }
                val answer = SessionReducer.reduce(replay.state, SessionAction.Answer(replay.state.nextAttempt!!, replay.state.task.question.correct))
                val afterReplay = engine.spoken.size
                audio.effects(state.plan.id, language, answer.effects)
                assertEquals(afterReplay + if (mode == AudioMode.ALL) 1 else 0, engine.spoken.size)
                state = SessionReducer.reduce(answer.state, SessionAction.Next(answer.state.task.id)).state
            }
            val before = engine.spoken.size
            audio.visible(false); audio.replay(state, language); audio.visible(true)
            assertEquals(before, engine.spoken.size)
            audio.replay(state, language)
            if (mode != AudioMode.OFF) assertEquals(CompareQuantityContent.prompt.speech[language], engine.spoken.last().text)
            audio.close()
        }
    }
}
