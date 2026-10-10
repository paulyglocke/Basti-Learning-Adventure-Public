package com.bellfamily.bastischool.learning.tellme

import com.bellfamily.bastischool.learning.models.ContentLanguage
import com.bellfamily.bastischool.learning.scenedescription.*
import org.junit.Assert.*
import org.junit.Test

class TellMeQuestionRotationTest {
    private val flow = TellMeFlow()
    private val woodland = SceneCategoryId("woodland_forest")
    private val rotation = TellMeQuestionRotation()
    private var state = TellMeState()
    private fun enter(next: TellMeState) { state = rotation.select(state, next, flow) }
    private fun advance() { enter(flow.advance(state, flow.scene(state)!!.id, state.stage)) }
    private fun visit(index: Int) {
        enter(flow.home())
        enter(flow.start(woodland))
        repeat(index) { advance() }
    }
    private fun support(language: ContentLanguage) = flow.support(flow.scene(state)!!, language, state.questionIndex)

    @Test fun repeatedVisitsAlternateOnlyExplicitPairedBundlesAndReturnToFirst() {
        repeat(3) { visitNumber ->
            visit(3)
            assertEquals(visitNumber % 2, state.questionIndex)
            assertEquals(if (visitNumber % 2 == 0) "What is on the hedgehog's back?" else "What is the fox doing?", support(ContentLanguage.ENGLISH).prompt)
            assertEquals(if (visitNumber % 2 == 0) "Was ist auf dem Rücken des Igels?" else "Was macht der Fuchs?", support(ContentLanguage.GERMAN).prompt)
        }
    }

    @Test fun helpModelAdultDisclosureAndLanguageRenderingKeepTheSelectedBundle() {
        visit(6); visit(6)
        assertEquals(1, state.questionIndex)
        val selected = state.questionIndex
        val english = support(ContentLanguage.ENGLISH)
        val german = support(ContentLanguage.GERMAN)
        repeat(3) {
            enter(flow.help(state)); enter(flow.grownUps(state))
            assertEquals(selected, state.questionIndex)
            assertEquals(english, support(ContentLanguage.ENGLISH))
            assertEquals(german, support(ContentLanguage.GERMAN))
        }
        assertEquals(TellMeStage.TALK, state.stage)
        assertEquals(selected, state.questionIndex)
        assertEquals("I can see the sun and dark clouds.", support(ContentLanguage.ENGLISH).model)
        assertEquals("Ich sehe die Sonne und dunkle Wolken.", support(ContentLanguage.GERMAN).model)
    }

    @Test fun staleCallbacksAndRepeatedSelectionDoNotConsumeAnotherVisit() {
        visit(3); visit(3)
        val talk = state
        enter(state)
        assertEquals(talk, state)
        val id = flow.scene(state)!!.id
        advance()
        val next = state
        enter(flow.advance(state, id, TellMeStage.TALK))
        assertEquals(next, state)
        enter(flow.advance(state, flow.scene(state)!!.id, TellMeStage.COMPLETE))
        assertEquals(next, state)
        visit(3)
        assertEquals(0, state.questionIndex)
    }

    @Test fun earlyExitDoesNotConsumeUnseenScenesAndAgainRotatesEnteredScenes() {
        visit(3)
        visit(6)
        assertEquals(0, state.questionIndex) // Rain scene was not visited in the first run.
        repeat(3) { advance() }
        assertEquals(TellMeStage.COMPLETE, state.stage)
        enter(flow.again(state))
        repeat(6) { advance() }
        assertEquals(1, state.questionIndex)
    }

    @Test fun aNewActivityLifetimeStartsAtCanonicalPromptWithoutPersistentHistory() {
        visit(3); visit(3)
        val fresh = TellMeQuestionRotation().select(TellMeState(woodland, 2), TellMeState(woodland, 3), flow)
        assertEquals(0, fresh.questionIndex)
    }

    @Test fun allAuthoredSlotsHaveMatchingSupportAndUncuratedScenesKeepTheirLegacySelection() {
        flow.repository.all().forEach { scene ->
            ContentLanguage.entries.forEach { language ->
                if (scene.tellMePrompts.isEmpty()) {
                    assertEquals(0, rotation.select(TellMeState(), TellMeState(scene.categoryId, flow.scenes(scene.categoryId).indexOf(scene)), flow).questionIndex)
                } else scene.tellMePrompts.forEachIndexed { index, prompt ->
                    val actual = flow.support(scene, language, index)
                    assertEquals(prompt.prompt[language], actual.prompt)
                    assertEquals(prompt.starter[language], actual.starter)
                    assertEquals(prompt.words[language], actual.words)
                    assertEquals(prompt.model[language], actual.model)
                    assertEquals(prompt.childExample[language]!! to prompt.model[language]!!, actual.expansion)
                    assertEquals(prompt.guidance[language], actual.principle)
                    assertNull(actual.focus)
                }
            }
        }
        val squirrel = flow.repository.find(SceneId("scene.woodland.actions.02"))!!
        assertEquals("What is the squirrel carrying?", flow.support(squirrel, ContentLanguage.ENGLISH).prompt)
        val uncurated = SceneDescription(squirrel.id, squirrel.categoryId, squirrel.image, squirrel.metadataPath,
            squirrel.wave, squirrel.title, squirrel.purpose, squirrel.primaryFocus, squirrel.secondaryFocus,
            squirrel.targets, squirrel.examples, squirrel.adultSupport)
        assertEquals("What is the squirrel carrying?", flow.support(uncurated, ContentLanguage.ENGLISH).prompt)
        assertThrows(IllegalArgumentException::class.java) { flow.support(uncurated, ContentLanguage.ENGLISH, 1) }
        assertThrows(IllegalArgumentException::class.java) { TellMeState(questionIndex = 1) }
    }

    @Test fun allNineAdventuresRotateEveryEnteredSceneAcrossCompleteRepeatVisits() {
        flow.categories.forEach { category ->
            val cursor = TellMeQuestionRotation()
            var current = TellMeState()
            repeat(3) { visit ->
                current = cursor.select(current, flow.start(category.id), flow)
                repeat(9) {
                    assertEquals(visit % 2, current.questionIndex)
                    val scene = flow.scene(current)!!
                    val slot = current.questionIndex
                    ContentLanguage.entries.forEach { language ->
                        val selected = flow.support(scene, language, slot)
                        assertEquals(scene.tellMePrompts[slot].prompt[language], selected.prompt)
                        assertTrue(selected.hasHelp); assertNotNull(selected.model)
                    }
                    current = cursor.select(current, flow.help(current), flow)
                    assertEquals(slot, current.questionIndex)
                    current = cursor.select(current, flow.advance(current, scene.id, TellMeStage.TALK), flow)
                }
                assertEquals(TellMeStage.COMPLETE, current.stage)
                current = cursor.select(current, flow.home(), flow)
            }
        }
    }

    @Test fun duplicateCategoryActivationCannotResetOrConsumeTheCurrentSlot() {
        visit(0); visit(0)
        assertEquals(1, state.questionIndex)
        val selected = support(ContentLanguage.ENGLISH)
        enter(flow.start(woodland))
        assertEquals(1, state.questionIndex)
        assertEquals(selected, support(ContentLanguage.ENGLISH))
        visit(0)
        assertEquals(0, state.questionIndex)
    }
}
