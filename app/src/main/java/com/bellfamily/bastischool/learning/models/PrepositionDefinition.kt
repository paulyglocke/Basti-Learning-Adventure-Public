package com.bellfamily.bastischool.learning.models

/** Canonical relation vocabulary; scene geometry never determines answer identity. */
data class PrepositionDefinition(override val id: ContentId, override val text: ContentText) : ContentDefinition {
    init { require(id.value.startsWith("position.")) }
}
