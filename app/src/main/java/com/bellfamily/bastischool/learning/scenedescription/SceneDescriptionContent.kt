package com.bellfamily.bastischool.learning.scenedescription

import com.bellfamily.bastischool.learning.models.*
import java.util.Collections

/** Preserve the committed scene IDs, including numeric final segments unsupported by ContentId. */
data class SceneId(val value: String) {
    init { require(value.matches(Regex("scene\\.[a-z][a-z0-9_]*(\\.[a-z][a-z0-9_]*)*\\.[0-9]{2}"))) }
    val imageId: AssetId get() = AssetId("asset." + value.replace(Regex("\\.([0-9]+)$"), ".n$1"))
}

data class SceneCategoryId(val value: String) {
    init { require(value.matches(Regex("[a-z]+(?:_[a-z]+)*"))) }
}

/** Authored data, not speech-ready copy. Missing German is explicit and never falls back. */
data class SceneText(val en: String, val de: String? = null) {
    init { require(en.isNotBlank()); require(de == null || de.isNotBlank()) }
    operator fun get(language: ContentLanguage): String? = when (language) {
        ContentLanguage.ENGLISH -> en
        ContentLanguage.GERMAN -> de
    }
}

private fun <T> frozen(values: List<T>): List<T> = Collections.unmodifiableList(values.toList())

/** Locale lists are separately authored; their positions need not form translated pairs. */
class SceneLines(en: List<String>, de: List<String>? = null) {
    val en = frozen(en)
    val de = de?.let(::frozen)
    init { require(en.isNotEmpty() && en.all { it.isNotBlank() }); require(de == null || de.isNotEmpty() && de.all { it.isNotBlank() }) }
    operator fun get(language: ContentLanguage): List<String>? = when (language) {
        ContentLanguage.ENGLISH -> en
        ContentLanguage.GERMAN -> de
    }
}

enum class SceneTargetKind {
    NOUNS, VERBS, ADJECTIVES, OPTIONAL_ADJECTIVES, SPATIAL_LANGUAGE, OPTIONAL_VERBS,
    COMPARISON_LANGUAGE, COUNTING_LANGUAGE, FEATURE_LANGUAGE, SOCIAL_LANGUAGE, FUNCTIONAL_LANGUAGE, SENTENCE_MODELS
}
class SceneTargetGroup(val kind: SceneTargetKind, terms: List<SceneText>) {
    val terms = frozen(terms)
    init { require(terms.isNotEmpty()) }
}
enum class SceneSupportKind {
    WORDS_TO_MODEL, QUESTIONS, SENTENCE_STARTERS, MODELLING_EXAMPLES, FUNCTIONAL_FOLLOW_UPS,
    INSTRUCTION_EXAMPLES, STARTER_PROMPTS, EXPANSION_PROMPTS
}
data class SceneSupportGroup(val kind: SceneSupportKind, val lines: SceneLines)
data class SceneExpansion(val child: SceneText, val adult: SceneText)
class SceneAdultSupport(
    val principle: SceneText? = null,
    val focus: SceneText? = null,
    groups: List<SceneSupportGroup>,
    expansions: List<SceneExpansion> = emptyList(),
) {
    val groups = frozen(groups)
    val expansions = frozen(expansions)
    init { require(groups.isNotEmpty()); require(groups.map { it.kind }.distinct().size == groups.size) }
    fun lines(kind: SceneSupportKind, language: ContentLanguage): List<String>? = groups.find { it.kind == kind }?.lines?.get(language)
}

enum class SceneWave { ONE, TWO, THREE }
enum class SceneProductionStatus { APPROVED }
data class SceneCategory(val id: SceneCategoryId, val display: LocalizedText)

class SceneDescription(
    val id: SceneId,
    val categoryId: SceneCategoryId,
    val image: LocalImageAsset,
    val metadataPath: String,
    val wave: SceneWave,
    val title: SceneText,
    val purpose: SceneText?,
    primaryFocus: List<String>,
    secondaryFocus: List<String>,
    targets: List<SceneTargetGroup>,
    val examples: SceneLines?,
    val adultSupport: SceneAdultSupport,
    /** Source review limitation, not child-facing instructions or an automatic answer rule. */
    val reviewCaution: SceneText? = null,
) {
    val status = SceneProductionStatus.APPROVED
    val primaryFocus = frozen(primaryFocus)
    val secondaryFocus = frozen(secondaryFocus)
    val targets = frozen(targets)
    init {
        val prefix = "SceneDescriptions/${categoryId.value}/"
        require(image.id == id.imageId)
        require(image.path.matches(Regex(Regex.escape(prefix) + "canonical/[a-z0-9_]+\\.png")))
        require(metadataPath == prefix + "metadata/" + image.path.substringAfterLast('/').removeSuffix(".png") + ".json")
        require(primaryFocus.all { it.isNotBlank() } && secondaryFocus.all { it.isNotBlank() })
        require(targets.isNotEmpty() && targets.map { it.kind }.distinct().size == targets.size)
    }
    fun terms(kind: SceneTargetKind, language: ContentLanguage): List<String>? {
        val group = targets.find { it.kind == kind } ?: return null
        val values = group.terms.map { it[language] ?: return null }
        return frozen(values)
    }
}

/** Separate from quiz ContentRepository: open-ended scenes do not require invented bilingual speech. */
class SceneDescriptionRepository(categories: List<SceneCategory>, scenes: List<SceneDescription>) {
    val version = ContentVersion(1, 1)
    private val categoryRecords = frozen(categories)
    private val records = frozen(scenes)
    init {
        require(categories.isNotEmpty() && scenes.isNotEmpty())
        require(categories.map { it.id }.distinct().size == categories.size) { "Duplicate scene category ID" }
        require(scenes.map { it.id }.distinct().size == scenes.size) { "Duplicate scene ID" }
        require(scenes.map { it.image.path }.distinct().size == scenes.size) { "Duplicate canonical scene asset" }
        require(scenes.all { scene -> categories.any { it.id == scene.categoryId } }) { "Missing scene category reference" }
        require(categories.all { category -> scenes.any { it.categoryId == category.id } }) { "Empty scene category" }
    }
    /** Root manifest order, not localized/alphabetical sorting. */
    fun categories(): List<SceneCategory> = categoryRecords
    /** Category manifest order; unknown category returns an empty list. */
    fun scenes(category: SceneCategoryId): List<SceneDescription> = frozen(records.filter { it.categoryId == category })
    fun all(): List<SceneDescription> = records
    fun find(id: SceneId): SceneDescription? = records.find { it.id == id }
    fun image(id: SceneId): LocalImageAsset? = find(id)?.image
}
