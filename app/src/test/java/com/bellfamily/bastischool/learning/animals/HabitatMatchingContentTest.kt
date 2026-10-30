package com.bellfamily.bastischool.learning.animals

import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.session.*
import org.junit.Assert.*
import org.junit.Test
import java.io.File

class HabitatMatchingContentTest {
    @Test fun stableActivityRevisionAndSevenUniqueReviewedQuestions() {
        assertEquals("activity.discover.habitat_matching", HabitatMatchingContent.activity.value)
        assertEquals(1, HabitatMatchingContent.REVISION)
        assertEquals(7, HabitatMatchingContent.questions.size)
        assertEquals(HabitatMatchingContent.questions.size,
            HabitatMatchingContent.questions.map { it.id }.toSet().size)
        assertEquals(setOf("dolphin_scene", "rabbit_scenes", "mole_tunnels", "earthworm_scene", "frog_scene", "duck_scene", "rabbit_shelter"),
            HabitatMatchingContent.questions.map { it.id }.toSet())
    }

    @Test fun multipleAcceptedRabbitScenesAreExplicitAndQuestionSpecific() {
        val q = HabitatMatchingContent.questions.single { it.id == "rabbit_scenes" }
        assertEquals(setOf(HabitatMatchingContent.woodland, HabitatMatchingContent.underground), q.accepted)
        assertEquals(3, q.options.size)
        assertTrue(q.options.containsAll(q.accepted))
        assertFalse(q.accepted.contains(HabitatMatchingContent.ocean))
        assertTrue(q.instruction.display.en.contains("Choose a place"))
        assertTrue(q.rationale.contains("both in-app scenes"))
    }

    @Test fun animalFactsAndAllSceneAndAnimalAssetsExist() {
        val root = listOf(File("src/main/assets"), File("app/src/main/assets")).first { it.isDirectory }
        HabitatMatchingContent.habitats.forEach { assertTrue(it.artwork, File(root, it.artwork).isFile) }
        HabitatMatchingContent.animals.forEach { assertTrue(it.artwork, File(root, it.artwork).isFile) }
        HabitatMatchingContent.questions.forEach {
            assertTrue(it.instruction.display.en.isNotBlank())
            assertTrue(it.instruction.display.de.isNotBlank())
            assertTrue(it.sourceFact.contains(" / "))
            assertTrue(it.accepted.isNotEmpty())
        }
        assertEquals("animal.rabbit", HabitatMatchingContent.rabbit.id.value)
        assertEquals("animal.frog", HabitatMatchingContent.frog.id.value)
        assertEquals("animal.duck", HabitatMatchingContent.duck.id.value)
    }

    @Test fun deterministicPlanHasSevenDistinctTasksAndValidatedCanonicalContent() {
        val id = SessionId("habitat-plan")
        val generated = HabitatMatchingContent.generate(id, RoundLength.FIVE, 82L) as GenerationResult.Generated
        HabitatMatchingContent.validate(SessionReducer.start(generated.plan, ContentLanguage.ENGLISH,
            HabitatMatchingContent.repository).state)
        val again = HabitatMatchingContent.generate(id, RoundLength.TEN, 82L) as GenerationResult.Generated
        assertEquals(7, generated.plan.tasks.size)
        assertEquals(generated.plan.tasks.map { it.question.definition }, again.plan.tasks.map { it.question.definition })
        assertEquals(generated.plan.tasks.map { it.question.choices }, again.plan.tasks.map { it.question.choices })
        assertEquals(HabitatMatchingContent.activity, generated.plan.activity)
        assertEquals("skill.discover.animal_habitat_matching", generated.plan.tasks.first().question.skill.value)
    }

    @Test fun correctHabitatDoesNotStayInOneScreenPositionAcrossSeeds() {
        val positions = (0L..80L).map { seed ->
            val plan = (HabitatMatchingContent.generate(SessionId("bias-$seed"), RoundLength.FIVE, seed)
                as GenerationResult.Generated).plan
            val question = plan.tasks.single { it.question.definition.value.endsWith("dolphin_scene") }.question
            question.choices.indexOf(HabitatMatchingContent.ocean)
        }.toSet()
        assertTrue("Correct choices must not be position-biased", positions.size > 1)
    }

    @Test fun replayAndLanguageKeepQuestionAndCanonicalAnswerIds() {
        val plan = (HabitatMatchingContent.generate(SessionId("language"), RoundLength.FIVE, 1L) as GenerationResult.Generated).plan
        var state = SessionReducer.start(plan, ContentLanguage.ENGLISH, HabitatMatchingContent.repository).state
        val definition = state.task.question.definition
        val options = state.task.question.choices
        state = SessionReducer.reduce(state, SessionAction.Language(ContentLanguage.GERMAN)).state
        assertEquals(definition, state.task.question.definition)
        assertEquals(options, state.task.question.choices)
        assertEquals(ContentLanguage.GERMAN, state.language)
        assertTrue(SessionReducer.reduce(state, SessionAction.Replay(state.task.id)).effects.any { it is SessionEffect.Narrate })
    }

    @Test fun checkpointV2PreservesSevenTaskPackAndMultiAnswerSet() {
        val plan = (HabitatMatchingContent.generate(SessionId("restore-set"), RoundLength.FIVE, 8L) as GenerationResult.Generated).plan
        val start = SessionReducer.start(plan, ContentLanguage.ENGLISH, HabitatMatchingContent.repository).state
        val rabbitTask = plan.tasks.single { it.question.definition.value.endsWith("rabbit_scenes") }
        val index = plan.tasks.indexOf(rabbitTask)
        var state = start
        repeat(index) {
            val answer = SessionAction.Answer(state.nextAttempt!!, state.task.question.correct)
            state = SessionReducer.reduce(state, answer).state
            state = SessionReducer.reduce(state, SessionAction.Next(state.task.id)).state
        }
        val restored = SessionCheckpoint.restore(SessionCheckpoint.encode(state), HabitatMatchingContent.activity,
            HabitatMatchingContent.REVISION, HabitatMatchingContent.repository) as SessionRestoreResult.Restored
        assertEquals(7, restored.state.plan.taskCount)
        assertEquals(setOf(HabitatMatchingContent.woodland, HabitatMatchingContent.underground),
            restored.state.task.question.acceptedAnswers)
        val alternative = restored.state.task.question.acceptedAnswers.first { it != restored.state.task.question.correct }
        val result = SessionReducer.reduce(restored.state, SessionAction.Answer(restored.state.nextAttempt!!, alternative))
        assertEquals(AnswerState.CORRECT, result.state.current.answer)
    }
}
