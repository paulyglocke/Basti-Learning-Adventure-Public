package com.bellfamily.bastischool.learning.prepositions

import com.bellfamily.bastischool.learning.content.*
import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.session.*
import org.junit.Assert.*
import org.junit.Test

class PrepositionsContentTest {
    @Test fun canonicalSixRelationsAndTwentyFourScenes() {
        assertEquals(listOf("on", "under", "behind", "next_to", "in", "between"), PositionRelation.entries.map { it.key })
        assertEquals(24, PrepositionsContent.scenes.map { it.id }.toSet().size)
        assertEquals(listOf("auf", "unter", "hinter", "neben", "in", "zwischen"), PositionRelation.entries.map { it.label.de })
        for (scene in PrepositionsContent.scenes) {
            val choices = (listOf(scene.relation) + PositionRelation.entries.filter { it != scene.relation }.take(3)).map { it.id }
            val question = PrepositionsContent.question(scene, choices)
            assertEquals(scene.relation.id, question.correct)
            assertEquals("skill.spatial.${scene.relation.key}", question.skill.value)
            assertTrue(question.correctFeedback.speech.en.endsWith(scene.description.en))
            assertTrue(question.correctFeedback.speech.de.endsWith(scene.description.de))
            assertEquals(scene.relation.phrase.de, question.hint!!.speech.de)
            assertTrue(question.instruction.speech.en.endsWith(choices.joinToString(", ") { PrepositionsContent.repository.find(it)!!.text.speech.en } + "."))
            assertTrue(question.instruction.speech.de.endsWith(choices.joinToString(", ") { PrepositionsContent.repository.find(it)!!.text.speech.de } + "."))
        }
    }
    @Test fun fiveAndTenAreDeterministicWithoutRepeatingScenes() {
        for (round in RoundLength.entries) for (seed in 0L..50L) {
            val first = plan(round, seed); val second = plan(round, seed)
            assertEquals(round.count, first.tasks.size)
            assertEquals(round.count, first.tasks.map { it.question.definition }.toSet().size)
            first.tasks.zip(second.tasks).forEach { (a,b) ->
                assertEquals(a.id,b.id); assertEquals(a.question.choices,b.question.choices)
                assertEquals(a.question.instruction,b.question.instruction)
                assertEquals(4,a.question.choices.toSet().size)
                assertTrue(a.question.correct in a.question.choices)
            }
        }
    }
    @Test fun incompleteVocabularyAndInvalidChoicesAreRejected() {
        val missing = BundledContentRepository(PrepositionsContent.version, emptyList(), emptyList())
        assertTrue(PrepositionsContent.generate(SessionId("bad"), RoundLength.FIVE, 1, missing) is GenerationResult.Rejected)
        assertThrows(IllegalArgumentException::class.java) { PrepositionsContent.question(PrepositionsContent.scenes.first(), listOf(ContentId("position.on"))) }
        assertThrows(IllegalArgumentException::class.java) { LocalizedText("on", " ") }
    }
    @Test fun nativeGeometryPreservesAllRelationsAndReferenceCounts() {
        for (relation in PositionRelation.entries) {
            val g = PositionGeometry.forRelation(relation); val a = g.animal; val o = g.objects.first()
            assertEquals(relation.count,g.objects.size)
            for (r in g.objects + a) { assertTrue(r.x >= 0 && r.y >= 0 && r.right <= 320 && r.bottom <= 200) }
            when(relation) {
                PositionRelation.ON -> assertEquals(o.y,a.bottom)
                PositionRelation.UNDER -> { assertTrue(a.y > o.bottom); assertTrue(a.x > o.x + 14 && a.right < o.right - 14) }
                PositionRelation.BEHIND -> { assertTrue(g.animalBehind); assertTrue(a.y < o.y && a.bottom > o.y) }
                PositionRelation.NEXT_TO -> assertTrue(a.right < o.x)
                PositionRelation.IN -> { assertNotNull(g.boxFront); assertTrue(a.x > o.x && a.right < o.right) }
                PositionRelation.BETWEEN -> { assertTrue(a.x > o.right); assertTrue(a.right < g.objects[1].x) }
            }
        }
    }
    companion object {
        fun plan(round: RoundLength = RoundLength.FIVE, seed: Long = 42) =
            (PrepositionsContent.generate(SessionId("positions-test"), round, seed) as GenerationResult.Generated).plan
    }
}
