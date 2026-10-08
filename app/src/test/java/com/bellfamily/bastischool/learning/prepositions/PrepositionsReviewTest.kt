package com.bellfamily.bastischool.learning.prepositions

import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.session.*
import java.io.File
import org.junit.Assert.*
import org.junit.Test

class PrepositionsReviewTest {
    @Test fun everyReferenceUsesTheCaseRequiredByTheReviewedPreposition() {
        // Singular dative, singular genitive, plural dative, plural genitive, English plural.
        val nouns = mapOf(
            ReferenceObject.ROCK to listOf("dem Stein", "des Steins", "Steinen", "Steine", "rocks"),
            ReferenceObject.TABLE to listOf("dem Tisch", "des Tisches", "Tischen", "Tische", "tables"),
            ReferenceObject.BOX to listOf("der Kiste", "der Kiste", "Kisten", "Kisten", "boxes"),
            ReferenceObject.CLOUD to listOf("der Wolke", "der Wolke", "Wolken", "Wolken", "clouds"),
            ReferenceObject.CAVE to listOf("der Höhle", "der Höhle", "Höhlen", "Höhlen", "caves"))
        val prefixes = listOf("auf", "unter", "hinter", "neben", "in", "zwischen", "über", "unter",
            "in", "außerhalb", "vor", "in der Nähe", "weit weg von")
        val english = listOf("on", "under", "behind", "next to", "in", "between", "above", "below",
            "inside", "outside", "in front of", "near", "far from")
        val enSingular = mapOf(ReferenceObject.ROCK to "rock", ReferenceObject.TABLE to "table",
            ReferenceObject.BOX to "box", ReferenceObject.CLOUD to "cloud", ReferenceObject.CAVE to "cave")
        // Stronger than allowed-choice coverage: check all 13 phrases on all 52 scenes.
        PrepositionsContent.scenes.forEach { scene ->
            val noun = nouns.getValue(scene.relation.reference)
            PositionRelation.entries.forEachIndexed { index, relation ->
                val plural = scene.relation == PositionRelation.BETWEEN || relation == PositionRelation.BETWEEN
                val genitive = relation in setOf(PositionRelation.NEAR, PositionRelation.OUTSIDE)
                val obj = if (plural) "${if (genitive) "der" else "den"} beiden ${noun[if (genitive) 3 else 2]}"
                    else noun[if (genitive) 1 else 0]
                val expected = "${prefixes[index]} $obj".replace("von dem ", "vom ")
                val de = answerPhrase(scene, relation.id, ContentLanguage.GERMAN)
                val en = answerPhrase(scene, relation.id, ContentLanguage.ENGLISH)
                assertEquals("${scene.id}: $relation", expected, de)
                assertEquals("${english[index]} the ${if (plural) "two ${noun[4]}" else enSingular.getValue(scene.relation.reference)}", en)
                assertFalse(de.contains("unterhalb")); assertFalse(de.contains("drinnen"))
                assertFalse(de.contains("von dem")); assertFalse(de.contains("in in"))
                assertEquals(scene.animal.subject.de.replaceFirstChar { it.uppercase() } + " ist $expected.",
                    positionAnswerStem(scene).display.de.removeSuffix("…") + " $de.")
            }
        }
        assertEquals("unter der Wolke", PositionRelation.BELOW.phrase.de)
        assertEquals("über der Wolke", PositionRelation.ABOVE.phrase.de)
        assertEquals("unter dem Tisch", PositionRelation.UNDER.phrase.de)
        assertEquals("in der Höhle", PositionRelation.INSIDE.phrase.de)
        assertEquals("außerhalb der Höhle", PositionRelation.OUTSIDE.phrase.de)
    }

    @Test fun ambiguousPairsAreExcludedButUsefulOppositesRemainAvailable() {
        val excluded = listOf(PositionRelation.IN to PositionRelation.INSIDE,
            PositionRelation.UNDER to PositionRelation.BELOW, PositionRelation.ON to PositionRelation.ABOVE,
            PositionRelation.NEXT_TO to PositionRelation.NEAR)
        ReferenceObject.entries.forEach { reference -> excluded.forEach { (a, b) ->
            assertFalse(PrepositionsContent.compatible(a, b, reference))
            assertFalse(PrepositionsContent.compatible(b, a, reference))
        } }
        assertFalse(PrepositionsContent.compatible(PositionRelation.OUTSIDE, PositionRelation.IN_FRONT_OF, ReferenceObject.CAVE))
        assertFalse(PrepositionsContent.compatible(PositionRelation.IN_FRONT_OF, PositionRelation.OUTSIDE, ReferenceObject.CAVE))
        assertTrue(PrepositionsContent.compatible(PositionRelation.OUTSIDE, PositionRelation.IN_FRONT_OF, ReferenceObject.ROCK))
        val useful = listOf(PositionRelation.NEAR to PositionRelation.FAR_FROM,
            PositionRelation.BEHIND to PositionRelation.IN_FRONT_OF, PositionRelation.ABOVE to PositionRelation.BELOW,
            PositionRelation.ON to PositionRelation.UNDER, PositionRelation.INSIDE to PositionRelation.OUTSIDE,
            PositionRelation.BETWEEN to PositionRelation.NEXT_TO)
        ReferenceObject.entries.forEach { reference -> useful.forEach { (a, b) ->
            assertTrue(PrepositionsContent.compatible(a, b, reference))
            assertTrue(PrepositionsContent.compatible(b, a, reference))
        } }
        val seen = mutableSetOf<Set<PositionRelation>>()
        for (seed in 0L..199L) {
            val plan = PrepositionsContentTest.plan(RoundLength.TEN, seed)
            val state = SessionReducer.start(plan, ContentLanguage.GERMAN, PrepositionsContent.repository).state
            PrepositionsContent.validate(state)
            plan.tasks.forEach { task ->
                val scene = PrepositionsContent.scene(task)
                val choices = task.question.choices.map { id -> PositionRelation.entries.single { it.id == id } }
                choices.forEachIndexed { i, a -> choices.drop(i + 1).forEach { b ->
                    assertTrue(PrepositionsContent.compatible(a, b, scene.relation.reference))
                    seen += setOf(a, b)
                } }
                for (text in listOf(task.question.instruction, task.question.correctFeedback, task.question.hint!!)) {
                    assertFalse(text.display.de.contains("unterhalb")); assertFalse(text.speech.de.contains("unterhalb"))
                }
            }
        }
        useful.forEach { (a, b) -> assertTrue("Useful contrast $a/$b must still occur", setOf(a, b) in seen) }
    }

    @Test fun visualContractsKeepSemanticIdsAndAuditEveryAuthoredSceneExactlyOnce() {
        // These assert authored reference contracts, not pixel recognition or device acceptance.
        assertEquals(ReferenceObject.ROCK, PositionRelation.ON.reference)
        assertEquals(ReferenceObject.CLOUD, PositionRelation.ABOVE.reference)
        assertEquals(ReferenceObject.TABLE, PositionRelation.UNDER.reference)
        assertEquals(ReferenceObject.CLOUD, PositionRelation.BELOW.reference)
        assertEquals(ReferenceObject.BOX, PositionRelation.IN.reference)
        assertEquals(ReferenceObject.CAVE, PositionRelation.INSIDE.reference)
        assertNotEquals(PositionRelation.IN.id, PositionRelation.INSIDE.id)
        assertNotEquals(PositionRelation.UNDER.id, PositionRelation.BELOW.id)
        assertEquals(2, PositionRelation.BETWEEN.count)
        val audit = File("../PREPOSITIONS_CONTENT_AUDIT.md").readText()
        val rows = Regex("(?m)^\\| `(scene\\.prepositions\\.[^`]+)` \\| (CLEAR|BORDERLINE|ARTWORK_FIX_REQUIRED) \\| (.+) \\|$")
            .findAll(audit).toList()
        assertEquals(52, rows.size)
        assertEquals(PrepositionsContent.scenes.map { it.id.value }.toSet(), rows.map { it.groupValues[1] }.toSet())
        assertTrue(rows.all { it.groupValues[3].isNotBlank() })
        PositionRelation.entries.forEach { assertTrue(audit.contains("| ${it.name} |")) }
        assertTrue(rows.none { it.groupValues[2] == "ARTWORK_FIX_REQUIRED" })
        assertEquals(52, rows.count { it.groupValues[2] == "CLEAR" })
        assertTrue(rows.none { it.groupValues[2] == "BORDERLINE" })
        val resolved = rows.filter { it.groupValues[3].contains("Previously ARTWORK_FIX_REQUIRED:") }
        assertTrue(resolved.all { it.groupValues[2] == "CLEAR" &&
            it.groupValues[3].contains("Replaced and visually re-reviewed 2026-10-08") })
        val fixes = resolved.map { it.groupValues[1] }.toSet()
        assertEquals(setOf("snake.next_to", "dinosaur.next_to", "dragon.next_to", "crocodile.next_to",
            "dinosaur.near", "crocodile.near", "fish.outside", "seahorse.outside",
            "dinosaur.far_from", "dragon.far_from").map { "scene.prepositions.$it" }.toSet(),
            rows.filter { it.groupValues[3].contains("Previously BORDERLINE:") }.map { it.groupValues[1] }.toSet())
        assertTrue(rows.filter { it.groupValues[3].contains("Previously BORDERLINE:") }.all {
            it.groupValues[3].contains("Replaced and visually re-reviewed 2026-10-08") })
        assertEquals(setOf("fish.inside", "turtle.inside", "octopus.inside", "seahorse.inside",
            "turtle.outside", "octopus.outside").map { "scene.prepositions.$it" }.toSet(), fixes)
    }

    @Test fun reviewedRestoreRejectsCaveOutsideAndFrontEvenWithOtherwiseExactText() {
        val original = PrepositionsContentTest.plan()
        val scene = PositionScene(PositionAnimal.FISH, PositionRelation.OUTSIDE)
        val q = PrepositionsContent.question(scene, listOf(PositionRelation.OUTSIDE, PositionRelation.IN_FRONT_OF,
            PositionRelation.ON, PositionRelation.UNDER).map { it.id })
        val forged = SessionPlan(original.id, original.activity, original.activityRevision, original.contentVersion,
            original.policy, original.tasks.mapIndexed { i, task -> if (i == 0) ChoiceTask(task.id, q) else task },
            original.completionText)
        val bytes = SessionCheckpoint.encode(SessionReducer.start(forged, ContentLanguage.GERMAN, PrepositionsContent.repository).state)
        assertEquals(SessionRestoreResult.Rejected(CheckpointRejection.MALFORMED), PrepositionsContent.restore(bytes))
    }
}
