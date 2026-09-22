package com.bellfamily.bastischool.learning.models

/** Definition identity, never a translated label, list index, asset or task-instance ID. */
data class ContentId(val value: String) {
    init {
        require(value.matches(Regex("[a-z][a-z0-9_]*(\\.[a-z][a-z0-9_]*)+"))) {
            "Invalid semantic content ID: $value"
        }
        require(!value.startsWith("skill.")) { "Skill IDs are not content IDs: $value" }
    }
}

enum class ContentLanguage { ENGLISH, GERMAN }

data class LocalizedText(val en: String, val de: String) {
    init {
        require(en.isNotBlank()) { "Required English text is blank" }
        require(de.isNotBlank()) { "Required German text is blank" }
    }
    operator fun get(language: ContentLanguage): String = when (language) {
        ContentLanguage.ENGLISH -> en
        ContentLanguage.GERMAN -> de
    }
}

/** Display decoration never implicitly becomes speech. Native playback still needs its safety boundary. */
data class ContentText(val display: LocalizedText, val speech: LocalizedText) {
    companion object {
        /** Explicit opt-in for undecorated labels whose display and speech are identical. */
        fun plain(en: String, de: String): ContentText {
            val text = LocalizedText(en, de)
            return ContentText(text, text)
        }
    }
}

/** Carry this alongside persisted content references; revisions are not inferred from labels. */
data class ContentVersion(val schema: Int, val revision: Int) {
    init {
        require(schema > 0) { "Content schema must be positive" }
        require(revision > 0) { "Content revision must be positive" }
    }
}

sealed interface ContentDefinition {
    val id: ContentId
    val text: ContentText
}

/** Semantic colour, not an Android colour value or an artwork filename. */
data class ColourDefinition(override val id: ContentId, override val text: ContentText) : ContentDefinition {
    init { require(id.value.startsWith("colour.")) { "Colour ID must use colour.: $id" } }
}

data class WeekdayDefinition(
    override val id: ContentId,
    override val text: ContentText,
    val colourCue: ContentId
) : ContentDefinition {
    init { require(id.value.startsWith("day.")) { "Weekday ID must use day.: $id" } }
}

/** Asset identity is distinct from both content identity and its replaceable local filename. */
data class AssetId(val value: String) {
    init {
        require(value.startsWith("asset.")) { "Asset ID must use asset.: $value" }
        ContentId(value) // Same semantic syntax, without accepting paths/URLs.
    }
}

data class LocalImageAsset(val id: AssetId, val path: String) {
    init {
        require(path.isNotBlank() && ':' !in path && '\\' !in path &&
            path.split('/').all { it.isNotBlank() && it != "." && it != ".." }) {
            "Image path must be relative to bundled assets: $path"
        }
    }
}

data class SeasonDefinition(
    override val id: ContentId,
    override val text: ContentText,
    /** Optional playback by a future screen, not a replacement for short question prompts. */
    val spokenDescription: LocalizedText,
    val illustration: AssetId
) : ContentDefinition {
    init { require(id.value.startsWith("season.")) { "Season ID must use season.: $id" } }
}
