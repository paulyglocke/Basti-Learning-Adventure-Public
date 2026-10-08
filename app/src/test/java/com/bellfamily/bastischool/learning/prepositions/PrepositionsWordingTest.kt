package com.bellfamily.bastischool.learning.prepositions

import com.bellfamily.bastischool.learning.models.ContentLanguage
import org.junit.Assert.*
import org.junit.Test

class PrepositionsWordingTest {
    @Test fun questionsAndStemsUseCanonicalSubjectsAndNominativeArticles() {
        val subjects=listOf(
            "the snake" to "die Schlange", "the dinosaur" to "der Dinosaurier", "the dragon" to "der Drache",
            "the crocodile" to "das Krokodil", "the bird" to "der Vogel", "the bee" to "die Biene",
            "the butterfly" to "der Schmetterling", "the fish" to "der Fisch", "the turtle" to "die Schildkröte",
            "the octopus" to "der Oktopus", "the seahorse" to "das Seepferdchen")
        PositionAnimal.entries.zip(subjects).forEach { (animal,subject) ->
            val scene=PrepositionsContent.scenes.first {it.animal==animal}
            assertEquals("Where is ${subject.first}?",positionQuestion(scene).display.en)
            assertEquals("Wo ist ${subject.second}?",positionQuestion(scene).display.de)
            assertEquals("${subject.first.replaceFirstChar {it.uppercase()}} is…",positionAnswerStem(scene).display.en)
            assertEquals("${subject.second.replaceFirstChar {it.uppercase()}} ist…",positionAnswerStem(scene).display.de)
        }
    }

    @Test fun reviewedCorrectPhrasesRemainExactAcrossAll52Scenes() {
        assertEquals(52,PrepositionsContent.scenes.size)
        PrepositionsContent.scenes.forEach {scene -> ContentLanguage.entries.forEach { language ->
            assertEquals(scene.relation.phrase[language],answerPhrase(scene,scene.relation.id,language))
        } }
    }

    @Test fun sceneSpecificDistractorsPreserveCasePluralAndContractions() {
        val rock=PositionScene(PositionAnimal.SNAKE,PositionRelation.ON)
        assertEquals("next to the rock",answerPhrase(rock,PositionRelation.NEXT_TO.id,ContentLanguage.ENGLISH))
        assertEquals("über dem Stein",answerPhrase(rock,PositionRelation.ABOVE.id,ContentLanguage.GERMAN))
        assertEquals("unter dem Stein",answerPhrase(rock,PositionRelation.BELOW.id,ContentLanguage.GERMAN))
        assertEquals("weit weg vom Stein",answerPhrase(rock,PositionRelation.FAR_FROM.id,ContentLanguage.GERMAN))
        assertEquals("zwischen den beiden Steinen",answerPhrase(rock,PositionRelation.BETWEEN.id,ContentLanguage.GERMAN))
        val plural=PositionScene(PositionAnimal.SNAKE,PositionRelation.BETWEEN)
        assertEquals("in der Nähe der beiden Steine",answerPhrase(plural,PositionRelation.NEAR.id,ContentLanguage.GERMAN))
        val cloud=PositionScene(PositionAnimal.BIRD,PositionRelation.ABOVE)
        assertEquals("behind the cloud",answerPhrase(cloud,PositionRelation.BEHIND.id,ContentLanguage.ENGLISH))
        assertEquals("hinter der Wolke",answerPhrase(cloud,PositionRelation.BEHIND.id,ContentLanguage.GERMAN))
    }
}
