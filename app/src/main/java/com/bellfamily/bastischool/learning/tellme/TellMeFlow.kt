package com.bellfamily.bastischool.learning.tellme

import com.bellfamily.bastischool.learning.models.ContentLanguage
import com.bellfamily.bastischool.learning.scenedescription.*

/** Participation only: there is deliberately no answer, attempt, score or progress result. */
enum class TellMeStage { TALK, COMPLETE }
data class TellMeState(
    val category: SceneCategoryId? = null,
    val index: Int = 0,
    val stage: TellMeStage = TellMeStage.TALK,
    val help: Boolean = false,
    val grownUps: Boolean = false,
    val questionIndex: Int = 0,
) {
    /** Optional celebration ownership only; never an assessment or progress result. */
    val celebrationId: String? get() = if (stage == TellMeStage.COMPLETE) "tellme.${category!!.value}" else null
    init {
        require(index in 0..8)
        require(questionIndex in 0..2)
        require(stage != TellMeStage.COMPLETE || index == 8)
        require(category != null || index == 0 && stage == TellMeStage.TALK && !help && !grownUps && questionIndex == 0)
    }
}
data class TellMeSupport(val prompt: String?, val starter: String?, val words: List<String>, val model: String?,
                         val principle: String?, val focus: String?, val expansion: Pair<String,String>?) {
    val hasHelp get() = starter != null || words.isNotEmpty() || model != null
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
            TellMeStage.TALK -> if(state.index == 8) state.copy(stage = TellMeStage.COMPLETE)
                else state.copy(index = state.index + 1, stage = TellMeStage.TALK, help = false, grownUps = false, questionIndex = 0)
            TellMeStage.COMPLETE -> state
        }
    }
    fun help(state: TellMeState) = if(state.category != null && state.stage == TellMeStage.TALK) state.copy(help = true) else state
    fun grownUps(state: TellMeState) = if(state.category != null && state.stage != TellMeStage.COMPLETE) state.copy(grownUps = !state.grownUps) else state

    /** Language-local fallback by authored support kind only. Never use targets or English fallback. */
    fun support(scene: SceneDescription, language: ContentLanguage, questionIndex: Int = 0): TellMeSupport {
        require(if (scene.tellMePrompts.isEmpty()) questionIndex == 0 else questionIndex in scene.tellMePrompts.indices)
        val selected = scene.tellMePrompts.getOrNull(questionIndex)
        fun lines(kind: SceneSupportKind) = scene.adultSupport.lines(kind, language).orEmpty()
        val prompt = listOf(SceneSupportKind.QUESTIONS, SceneSupportKind.STARTER_PROMPTS, SceneSupportKind.EXPANSION_PROMPTS)
            .firstNotNullOfOrNull { lines(it).firstOrNull() }
        val expansion = if (selected != null) {
            selected.childExample[language]!! to selected.model[language]!!
        } else scene.adultSupport.expansions.firstNotNullOfOrNull { pair ->
            val child = pair.child[language]; val adult = pair.adult[language]
            if(child != null && adult != null) child to adult else null
        }
        return TellMeSupport(selected?.prompt?.get(language) ?: prompt,
            selected?.starter?.get(language) ?: lines(SceneSupportKind.SENTENCE_STARTERS).firstOrNull(),
            selected?.words?.get(language) ?: lines(SceneSupportKind.WORDS_TO_MODEL).take(3),
            selected?.model?.get(language) ?: scene.examples?.get(language)?.firstOrNull() ?: lines(SceneSupportKind.MODELLING_EXAMPLES).firstOrNull(),
            selected?.guidance?.get(language) ?: scene.adultSupport.principle?.get(language),
            if (selected == null) scene.adultSupport.focus?.get(language) else null, expansion)
    }
}

/** Activity-lifetime cursors only. Entered scenes rotate; rendering/support never consumes a slot. */
class TellMeQuestionRotation {
    private val nextIndices = mutableMapOf<SceneId, Int>()

    fun select(previous: TellMeState, next: TellMeState, flow: TellMeFlow): TellMeState {
        if (next == previous || next.stage != TellMeStage.TALK) return next
        val scene = flow.scene(next) ?: return next
        // Repeated category activation while already on this picture is not another visit.
        if (flow.scene(previous)?.id == scene.id && previous.stage != TellMeStage.COMPLETE) return next.copy(questionIndex = previous.questionIndex)
        if (scene.tellMePrompts.isEmpty()) return next
        val index = nextIndices[scene.id] ?: 0
        nextIndices[scene.id] = (index + 1) % scene.tellMePrompts.size
        return next.copy(questionIndex = index)
    }
}
