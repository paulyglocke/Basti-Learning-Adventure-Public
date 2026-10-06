package com.bellfamily.bastischool.learning.wilma

import com.bellfamily.bastischool.audio.*
import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.session.*
import org.junit.Assert.*
import org.junit.Test

class WilmaTodayTest {
    private fun plan(round: RoundLength, seed: Long) =
        (WilmaContent.generate(WilmaPhase.TODAY, SessionId("today"), round, seed) as GenerationResult.Generated).plan

    @Test fun allMappingsIncludeBothWeekBoundariesAndCanonicalBilingualWording() {
        val days = WilmaContent.days
        days.forEachIndexed { index, anchor ->
            val name = WilmaContent.day(anchor).text.display
            WilmaToday.Relation.entries.forEach { relation ->
                val yesterday = relation == WilmaToday.Relation.YESTERDAY
                val expected = days[(index + if(yesterday) 6 else 1) % 7]
                assertEquals(expected, WilmaToday.answer(anchor, relation))
                val choices = listOf(expected) + days.filter {it != expected}.take(3)
                val q = WilmaToday.question(anchor, relation, choices)
                assertEquals(expected, q.correct)
                assertEquals(1, q.choices.count {it == expected})
                assertEquals(if(yesterday) "Today is ${name.en}. What day was yesterday?" else "Today is ${name.en}. What day is tomorrow?", q.instruction.display.en)
                assertEquals(if(yesterday) "Heute ist ${name.de}. Welcher Tag war gestern?" else "Heute ist ${name.de}. Welcher Tag ist morgen?", q.instruction.display.de)
            }
        }
    }

    @Test fun deterministicBalancedRoundsHaveFourUniqueChoicesAndNoRepeatedPairs() {
        for(seed in 0L..99L) for(round in RoundLength.entries) {
            val p = plan(round, seed)
            val state = SessionReducer.start(p, ContentLanguage.ENGLISH, WilmaContent.repository).state
            val same = SessionReducer.start(plan(round, seed), ContentLanguage.ENGLISH, WilmaContent.repository).state
            assertArrayEquals(SessionCheckpoint.encode(state), SessionCheckpoint.encode(same))
            WilmaContent.validate(WilmaPhase.TODAY, state)
            val pairs = p.tasks.map {WilmaToday.definition(it.question)}
            assertEquals(round.count, pairs.toSet().size)
            assertEquals(minOf(7, round.count), pairs.map {it.first}.toSet().size)
            val counts = pairs.groupingBy {it.second}.eachCount().values.sorted()
            assertEquals(if(round == RoundLength.FIVE) listOf(2,3) else listOf(5,5), counts)
            p.tasks.forEach {assertEquals(4, it.question.choices.toSet().size)}
        }
    }

    @Test fun existingAudioOwnerSpeaksTodayQuestionInBothLanguagesAndObeysPolicy() {
        for(language in ContentLanguage.entries) for(mode in AudioMode.entries) {
            val engine = FakeSpeechEngine()
            val audio = WilmaAudio(DefaultAudioController(engine, mode))
            audio.visible(true)
            assertTrue(engine.spoken.isEmpty())
            val p = plan(RoundLength.FIVE, 2)
            val state = SessionReducer.start(p, language, WilmaContent.repository).state
            val replay = SessionReducer.reduce(state, SessionAction.Replay(state.task.id))
            audio.effects(p.id, language, replay.effects)
            if(mode == AudioMode.OFF) assertTrue(engine.spoken.isEmpty()) else {
                assertEquals(state.task.question.instruction.speech[language], engine.spoken.single().text)
                assertEquals(language, engine.spoken.single().context.language)
            }
            audio.visible(false)
            audio.effects(p.id, language, replay.effects)
            assertEquals(if(mode == AudioMode.OFF) 0 else 1, engine.spoken.size)
            audio.close()
        }
    }
}
