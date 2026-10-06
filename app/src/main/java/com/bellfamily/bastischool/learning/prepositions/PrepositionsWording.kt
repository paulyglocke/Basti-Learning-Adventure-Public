package com.bellfamily.bastischool.learning.prepositions

import com.bellfamily.bastischool.learning.models.*

/** Scene-derived presentation and speech; stored question payloads remain compatible. */
fun positionQuestion(scene: PositionScene) = ContentText.plain(
    "Where is ${scene.animal.subject.en}?", "Wo ist ${scene.animal.subject.de}?")

fun positionAnswerStem(scene: PositionScene) = ContentText.plain(
    "${scene.animal.subject.en.replaceFirstChar { it.uppercase() }} is…",
    "${scene.animal.subject.de.replaceFirstChar { it.uppercase() }} ist…")

/** Reviewed scene-specific phrase/case rules shared verbatim by display and speech. */
fun answerPhrase(scene: PositionScene, choice: ContentId, language: ContentLanguage): String {
    val relation = PositionRelation.entries.single { it.id == choice }
    if (relation == scene.relation) return relation.phrase[language]
    val plural = scene.relation.count == 2 || relation == PositionRelation.BETWEEN
    val en = when (scene.relation.reference) {
        ReferenceObject.ROCK -> if (plural) "the two rocks" else "the rock"
        ReferenceObject.TABLE -> if (plural) "the two tables" else "the table"
        ReferenceObject.BOX -> if (plural) "the two boxes" else "the box"
        ReferenceObject.CLOUD -> if (plural) "the two clouds" else "the cloud"
        ReferenceObject.CAVE -> if (plural) "the two caves" else "the cave"
    }
    if (language == ContentLanguage.ENGLISH) return "${relation.label.en} $en"
    val genitive = relation in setOf(PositionRelation.BELOW, PositionRelation.OUTSIDE, PositionRelation.NEAR)
    val noun = when (scene.relation.reference) {
        ReferenceObject.ROCK -> if (plural) "Steinen" else if (genitive) "des Steins" else "dem Stein"
        ReferenceObject.TABLE -> if (plural) "Tischen" else if (genitive) "des Tisches" else "dem Tisch"
        ReferenceObject.BOX -> if (plural) "Kisten" else "der Kiste"
        ReferenceObject.CLOUD -> if (plural) "Wolken" else "der Wolke"
        ReferenceObject.CAVE -> if (plural) "Höhlen" else "der Höhle"
    }
    val objectPhrase = if (plural) {
        val pluralNoun = if (genitive) when (scene.relation.reference) {
            ReferenceObject.ROCK -> "Steine"
            ReferenceObject.TABLE -> "Tische"
            else -> noun
        } else noun
        "${if (genitive) "der" else "den"} beiden $pluralNoun"
    } else noun
    val preposition = when (relation) {
        PositionRelation.INSIDE -> "in"
        PositionRelation.OUTSIDE -> "außerhalb"
        PositionRelation.NEAR -> "in der Nähe"
        PositionRelation.FAR_FROM -> "weit weg von"
        else -> relation.label.de
    }
    return "$preposition $objectPhrase".replace("von dem ", "vom ")
}
