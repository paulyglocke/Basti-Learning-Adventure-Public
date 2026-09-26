package com.bellfamily.bastischool.learning.tellme

import com.bellfamily.bastischool.learning.models.ContentLanguage
import com.bellfamily.bastischool.learning.scenedescription.*
import org.junit.Assert.*
import org.junit.Test

class TellMeFlowTest {
    private val flow = TellMeFlow()
    private val category = flow.categories.first().id
    private fun next(state: TellMeState) = flow.advance(state, flow.scene(state)!!.id, state.stage)

    @Test fun soleBundledCataloguePreservesAllManifestOrdering() {
        val bundled = BundledSceneDescriptions.repository()
        assertEquals(9, flow.categories.size)
        assertEquals(bundled.categories(), flow.categories)
        flow.categories.forEach { category ->
            assertEquals(9, flow.scenes(category.id).size)
            assertEquals(bundled.scenes(category.id).map { it.id }, flow.scenes(category.id).map { it.id })
        }
    }
    @Test fun categoryStartsAtFirstSceneWithoutAnswerOrScoreState() {
        val state = flow.start(category)
        assertEquals(TellMeState(category), state)
        assertSame(flow.scenes(category).first(), flow.scene(state))
        assertEquals(setOf("category", "index", "stage", "help", "grownUps"), TellMeState::class.java.declaredFields.filterNot { java.lang.reflect.Modifier.isStatic(it.modifiers) }.map { it.name }.toSet())
    }
    @Test fun talkToModelKeepsPictureThenAdvancesOnePicture() {
        val talk = flow.start(category)
        val model = next(talk)
        assertEquals(TellMeStage.MODEL, model.stage)
        assertSame(flow.scene(talk), flow.scene(model))
        val second = next(model)
        assertEquals(1, second.index); assertEquals(TellMeStage.TALK, second.stage)
    }
    @Test fun supportAndAdultExpansionDoNotAdvanceAndResetOnlyOnNextPicture() {
        val start = flow.start(category)
        val helped = flow.grownUps(flow.help(start))
        assertTrue(helped.help); assertTrue(helped.grownUps)
        assertSame(flow.scene(start), flow.scene(helped))
        val model = next(helped)
        assertTrue(model.help); assertTrue(model.grownUps)
        assertFalse(next(model).help); assertFalse(next(model).grownUps)
        assertEquals(helped.copy(grownUps = false), flow.grownUps(helped))
    }
    @Test fun ninthSceneCompletesOnlyAfterModelAndCompletionIsIdempotent() {
        var state = flow.start(category)
        repeat(8) { state = next(next(state)) }
        assertEquals(8, state.index); assertEquals(TellMeStage.TALK, state.stage)
        state = next(state); assertEquals(TellMeStage.MODEL, state.stage)
        state = next(state); assertEquals(TellMeStage.COMPLETE, state.stage)
        assertEquals(state, next(state))
    }
    @Test fun obsoletePhaseOrSceneCallbacksCannotSkipConversation() {
        val talk = flow.start(category); val id = flow.scene(talk)!!.id
        val model = next(talk)
        assertEquals(model, flow.advance(model, id, TellMeStage.TALK))
        val second = next(model)
        assertEquals(second, flow.advance(second, id, TellMeStage.MODEL))
    }
    @Test fun againRetainsCategoryWhileHomeAndChooseAnotherClearConversation() {
        var state = flow.start(flow.categories.last().id)
        repeat(18) { state = next(state) }
        assertEquals(TellMeState(state.category), flow.again(state))
        assertEquals(TellMeState(), flow.home()); assertNull(flow.scene(flow.home()))
    }
    @Test fun languageSelectionNeverMutatesSemanticStateAndAll81HaveAuthoredPrompts() {
        flow.categories.forEach { category ->
            var state = flow.start(category.id)
            flow.scenes(category.id).forEach { scene ->
                assertEquals(scene.id, flow.scene(state)!!.id)
                val before = state.copy()
                ContentLanguage.entries.forEach { lang ->
                    assertNotNull(scene.title[lang]); assertNotNull(flow.support(scene, lang).prompt)
                    assertEquals(before, state)
                }
                state = next(next(state))
            }
        }
    }
    @Test fun currentOptionalSupportAvailabilityIsHonestInBothLanguages() {
        ContentLanguage.entries.forEach { lang ->
            val support = flow.repository.all().map { flow.support(it, lang) }
            assertEquals(27, support.count { it.hasHelp })
            assertEquals(27, support.count { it.model != null })
            assertEquals(81, support.count { it.hasGrownUps })
            assertTrue(support.all { it.words.size <= 3 })
            flow.repository.all().forEach { scene ->
                val data = flow.support(scene, lang)
                assertEquals(scene.examples?.get(lang)?.firstOrNull() ?: scene.adultSupport.lines(SceneSupportKind.MODELLING_EXAMPLES, lang)?.firstOrNull(), data.model)
            }
        }
    }
    private fun fixture(groups: List<SceneSupportGroup>, examples: SceneLines? = null): SceneDescription {
        val s = flow.repository.all().first()
        return SceneDescription(s.id, s.categoryId, s.image, s.metadataPath, s.wave, s.title, s.purpose,
            s.primaryFocus, s.secondaryFocus, s.targets, examples, SceneAdultSupport(groups = groups))
    }
    @Test fun languageLocalPromptPriorityAndModelFallbackNeverBorrowEnglish() {
        val scene = fixture(listOf(
            SceneSupportGroup(SceneSupportKind.EXPANSION_PROMPTS, SceneLines(listOf("expansion"), listOf("Erweiterung"))),
            SceneSupportGroup(SceneSupportKind.STARTER_PROMPTS, SceneLines(listOf("starter"), listOf("Anfang"))),
            SceneSupportGroup(SceneSupportKind.QUESTIONS, SceneLines(listOf("question"))),
            SceneSupportGroup(SceneSupportKind.MODELLING_EXAMPLES, SceneLines(listOf("model"), listOf("Beispiel")))
        ), SceneLines(listOf("example")))
        assertEquals("question", flow.support(scene, ContentLanguage.ENGLISH).prompt)
        assertEquals("Anfang", flow.support(scene, ContentLanguage.GERMAN).prompt)
        assertEquals("example", flow.support(scene, ContentLanguage.ENGLISH).model)
        assertEquals("Beispiel", flow.support(scene, ContentLanguage.GERMAN).model)
    }
    @Test fun missingLanguageOmitsOptionalContentAndExpansionPromptIsLastFallback() {
        val englishOnly = fixture(listOf(SceneSupportGroup(SceneSupportKind.QUESTIONS, SceneLines(listOf("question")))))
        val missing = flow.support(englishOnly, ContentLanguage.GERMAN)
        assertNull(missing.prompt); assertNull(missing.model); assertFalse(missing.hasHelp); assertFalse(missing.hasGrownUps)
        val expansion = fixture(listOf(SceneSupportGroup(SceneSupportKind.EXPANSION_PROMPTS, SceneLines(listOf("expand"), listOf("Mehr")))))
        assertEquals("Mehr", flow.support(expansion, ContentLanguage.GERMAN).prompt)
    }
    @Test fun invalidCategoryAndImpossibleStateFailBeforeDisplay() {
        assertThrows(IllegalArgumentException::class.java) { flow.start(SceneCategoryId("unknown")) }
        assertThrows(IllegalArgumentException::class.java) { TellMeState(category, 9) }
        assertThrows(IllegalArgumentException::class.java) { TellMeState(category, 0, TellMeStage.COMPLETE) }
        assertThrows(IllegalArgumentException::class.java) { TellMeState(help = true) }
    }
}
