package com.bellfamily.bastischool.learning.tellme

import com.bellfamily.bastischool.learning.models.ContentLanguage
import com.bellfamily.bastischool.learning.scenedescription.*

/** Participation only: there is deliberately no answer, attempt, score or progress result. */
enum class TellMeStage { TALK, MODEL, COMPLETE }
data class TellMeState(
    val category: SceneCategoryId? = null,
    val index: Int = 0,
    val stage: TellMeStage = TellMeStage.TALK,
    val help: Boolean = false,
    val grownUps: Boolean = false,
) {
    init {
        require(index in 0..8)
        require(stage != TellMeStage.COMPLETE || index == 8)
        require(category != null || index == 0 && stage == TellMeStage.TALK && !help && !grownUps)
    }
}
data class TellMeSupport(val prompt: String?, val starter: String?, val words: List<String>, val model: String?,
                         val principle: String?, val focus: String?, val expansion: Pair<String,String>?) {
    val hasHelp get() = starter != null || words.isNotEmpty()
    val hasGrownUps get() = principle != null || focus != null || expansion != null
}

class TellMeFlow(val repository: SceneDescriptionRepository = BundledSceneDescriptions.repository()) {
    val categories get() = repository.categories()
    fun scenes(category: SceneCategoryId) = repository.scenes(category)
    fun scene(state: TellMeState): SceneDescription? = state.category?.let { scenes(it).getOrNull(state.index) }
    fun start(category: SceneCategoryId): TellMeState {
        require(categories.any {it.id == category} && scenes(category).size == 9)
        return TellMeState(category)
    }
    fun home() = TellMeState()
    fun again(state: TellMeState) = if(state.category != null && state.stage == TellMeStage.COMPLETE) start(state.category) else state
    /** Reject an obsolete/double activation from the previous picture or phase. */
    fun advance(state: TellMeState, expectedScene: SceneId, expectedStage: TellMeStage): TellMeState {
        if(scene(state)?.id != expectedScene || state.stage != expectedStage) return state
        return when(state.stage) {
            TellMeStage.TALK -> state.copy(stage = TellMeStage.MODEL)
            TellMeStage.MODEL -> if(state.index == 8) state.copy(stage = TellMeStage.COMPLETE)
                else state.copy(index = state.index + 1, stage = TellMeStage.TALK, help = false, grownUps = false)
            TellMeStage.COMPLETE -> state
        }
    }
    fun help(state: TellMeState) = if(state.category != null && state.stage == TellMeStage.TALK) state.copy(help = true) else state
    fun grownUps(state: TellMeState) = if(state.category != null && state.stage != TellMeStage.COMPLETE) state.copy(grownUps = !state.grownUps) else state

    /** Language-local fallback by authored support kind only. Never use targets or English fallback. */
    fun support(scene: SceneDescription, language: ContentLanguage): TellMeSupport {
        fun lines(kind: SceneSupportKind) = scene.adultSupport.lines(kind, language).orEmpty()
        val prompt = listOf(SceneSupportKind.QUESTIONS, SceneSupportKind.STARTER_PROMPTS, SceneSupportKind.EXPANSION_PROMPTS)
            .firstNotNullOfOrNull { lines(it).firstOrNull() }
        val expansion = scene.adultSupport.expansions.firstNotNullOfOrNull { pair ->
            val child = pair.child[language]; val adult = pair.adult[language]
            if(child != null && adult != null) child to adult else null
        }
        return TellMeSupport(prompt, lines(SceneSupportKind.SENTENCE_STARTERS).firstOrNull(),
            lines(SceneSupportKind.WORDS_TO_MODEL).take(3),
            scene.examples?.get(language)?.firstOrNull() ?: lines(SceneSupportKind.MODELLING_EXAMPLES).firstOrNull(),
            scene.adultSupport.principle?.get(language), scene.adultSupport.focus?.get(language), expansion)
    }
}
