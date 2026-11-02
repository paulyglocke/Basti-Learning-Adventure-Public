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
        assertEquals("Where did we see rabbits?", q.instruction.display.en)
        assertTrue(q.rationale.contains("both in-app scenes"))
    }

    @Test fun polishedCopyIsBilingualConciseAndKeepsTheReviewedAnswerSets() {
        val byId = HabitatMatchingContent.questions.associateBy { it.id }
        assertEquals("Where did we see rabbits?", byId.getValue("rabbit_scenes").instruction.display.en)
        assertEquals("Wo haben wir Kaninchen gesehen?", byId.getValue("rabbit_scenes").instruction.display.de)
        assertEquals("Where did we see the earthworm?", byId.getValue("earthworm_scene").instruction.display.en)
        assertEquals("Wo haben wir den Regenwurm gesehen?", byId.getValue("earthworm_scene").instruction.display.de)
        assertEquals("Ja! Den Regenwurm haben wir unter der Erde entdeckt.",
            byId.getValue("earthworm_scene").correctFeedback.display.de)
        assertEquals("Where can wild rabbits find shelter underground?", byId.getValue("rabbit_shelter").instruction.display.en)
        assertEquals("Wo finden Wildkaninchen unter der Erde Schutz?", byId.getValue("rabbit_shelter").instruction.display.de)
        assertEquals("Look at the animal and the pictures. What do you remember?",
            byId.getValue("mole_tunnels").hint.display.en)
        assertEquals("Schau dir das Tier und die Bilder an. Woran erinnerst du dich?",
            byId.getValue("mole_tunnels").hint.display.de)
        assertTrue(HabitatMatchingContent.questions.all { it.wrongFeedback.display.en == "Not that place. Try another one." })

        assertEquals(setOf(HabitatMatchingContent.ocean), byId.getValue("dolphin_scene").accepted)
        assertEquals(setOf(HabitatMatchingContent.woodland, HabitatMatchingContent.underground), byId.getValue("rabbit_scenes").accepted)
        assertEquals(setOf(HabitatMatchingContent.underground), byId.getValue("mole_tunnels").accepted)
        assertEquals(setOf(HabitatMatchingContent.underground), byId.getValue("earthworm_scene").accepted)
        assertEquals(setOf(HabitatMatchingContent.freshwater), byId.getValue("frog_scene").accepted)
        assertEquals(setOf(HabitatMatchingContent.freshwater), byId.getValue("duck_scene").accepted)
        assertEquals(setOf(HabitatMatchingContent.underground), byId.getValue("rabbit_shelter").accepted)
    }

    @Test fun copyPolishPreservesFrozenPriorWordingOnSavedSessions() {
        val generated = (HabitatMatchingContent.generate(SessionId("legacy-copy"), RoundLength.FIVE, 82L)
            as GenerationResult.Generated).plan
        val oldHint = ContentText.plain("Think about the place we explored.", "Denk an den Ort, den wir entdeckt haben.")
        val tasks = generated.tasks.map { task ->
            val q = task.question
            val item = HabitatMatchingContent.reviewed(q.definition)
            val oldInstruction = when (item.id) {
                "rabbit_scenes" -> ContentText.plain("Choose a place where we met rabbits.",
                    "Wähle einen Ort, an dem wir Kaninchen getroffen haben.")
                "earthworm_scene" -> ContentText.plain("Where did we explore the earthworm moving through soil?",
                    "Wo haben wir den Regenwurm entdeckt, der sich durch den Boden bewegt?")
                "rabbit_shelter" -> ContentText.plain("Where can wild rabbits use a warren for shelter?",
                    "Wo können Wildkaninchen einen Bau als Schutz nutzen?")
                else -> q.instruction
            }
            val oldCorrect = when (item.id) {
                "earthworm_scene" -> ContentText.plain("Yes! We explored the earthworm in the underground scene.",
                    "Ja! Wir haben den Regenwurm in der Untergrund-Szene entdeckt.")
                "rabbit_shelter" -> ContentText.plain("Yes! Wild rabbits can shelter in underground warrens.",
                    "Ja! Wildkaninchen können in unterirdischen Bauen Schutz finden.")
                else -> q.correctFeedback
            }
            ChoiceTask(task.id, ChoiceQuestion(q.definition, q.skill, q.context, q.difficulty, oldInstruction,
                q.choices, q.correct, oldCorrect, q.wrongFeedback, oldHint, q.acceptedAnswers))
        }
        val oldPlan = SessionPlan(generated.id, generated.activity, generated.activityRevision,
            generated.contentVersion, generated.policy, tasks, generated.completionText, generated.taskCount)
        val oldState = SessionReducer.start(oldPlan, ContentLanguage.ENGLISH, HabitatMatchingContent.repository).state
        val encoded = SessionCheckpoint.encode(oldState)
        val restored = SessionCheckpoint.restore(encoded, HabitatMatchingContent.activity, HabitatMatchingContent.REVISION,
            HabitatMatchingContent.repository) as SessionRestoreResult.Restored
        HabitatMatchingContent.validate(restored.state)
        assertEquals("Think about the place we explored.", restored.state.task.question.hint!!.display.en)
        assertEquals(oldState.plan.tasks.map { it.question.instruction }, restored.state.plan.tasks.map { it.question.instruction })
        assertEquals(oldState.plan.tasks.map { it.question.acceptedAnswers }, restored.state.plan.tasks.map { it.question.acceptedAnswers })
        assertEquals(HabitatMatchingContent.repository.version, restored.state.plan.contentVersion)
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
