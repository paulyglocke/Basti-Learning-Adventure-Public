package com.bellfamily.bastischool.ui.addition

import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import com.bellfamily.bastischool.learning.addition.*
import com.bellfamily.bastischool.learning.models.ContentLanguage
import com.bellfamily.bastischool.learning.session.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class ArithmeticFollowupScreenTest {
    @get:Rule val compose = createComposeRule()

    @Test fun takeAwayGermanShowsGroupsZeroChoiceAndCompletesFiveQuestionRound() {
        val plan = (SubtractionContent.generate(SessionId("subtract-screen"), RoundLength.FIVE, 7) as GenerationResult.Generated).plan
        val state = mutableStateOf(SessionReducer.start(plan, ContentLanguage.GERMAN, SubtractionContent.repository).state)
        compose.setContent {
            MaterialTheme {
                SubtractionScreen(state.value, ContentLanguage.GERMAN, false, false, false,
                    { state.value = SessionReducer.reduce(state.value, it).state }, {}, {}, {}, {}, Modifier.width(640.dp).height(360.dp))
            }
        }
        val answers = mutableSetOf<Int>()
        repeat(5) {
            val fact = SubtractionContent.definition(state.value.task.question)
            answers += fact.answer
            compose.onNodeWithTag("subtraction-start").performScrollTo().assertExists()
            compose.onNodeWithTag("subtraction-take-away").performScrollTo().assertExists()
            compose.onNodeWithTag("subtraction-minus").assertIsDisplayed()
            compose.onNodeWithTag("subtraction-prompt").performScrollTo().assertTextEquals("Wie viele sind noch da?")
            compose.onNodeWithTag("subtraction-answer-${fact.answer}").performScrollTo().performClick()
            compose.onNodeWithTag("subtraction-next").performScrollTo().performClick()
        }
        assertEquals((0..4).toSet(), answers)
        compose.onNodeWithTag("subtraction-complete").assertExists()
    }

    @Test fun mixedAdditionScreenContainsBothRangesAndRequestedSums() {
        val plan = (MixedAdditionContent.generate(SessionId("mixed-screen"), RoundLength.FIVE, 11) as GenerationResult.Generated).plan
        val state = mutableStateOf(SessionReducer.start(plan, ContentLanguage.ENGLISH, MixedAdditionContent.repository).state)
        compose.setContent {
            MaterialTheme {
                AdditionScreen(state.value, ContentLanguage.ENGLISH, false, false, false,
                    { state.value = SessionReducer.reduce(state.value, it).state }, {}, {}, {}, {},
                    Modifier.width(640.dp).height(360.dp), curriculum = MixedAdditionContent)
            }
        }
        val facts = mutableSetOf<AdditionFact>()
        repeat(5) {
            val fact = MixedAdditionContent.definition(state.value.task.question)
            facts += fact
            compose.onNodeWithTag("addition-prompt").performScrollTo().assertTextEquals("How many altogether?")
            compose.onNodeWithTag("addition-left-number").performScrollTo().assertTextEquals(fact.left.toString())
            compose.onNodeWithTag("addition-right-number").performScrollTo().assertTextEquals(fact.right.toString())
            val correct = MixedAdditionContent.numberId(fact.total).value.substringAfter('.')
            compose.onNodeWithTag("addition-answer-$correct").performScrollTo().performClick()
            compose.onNodeWithTag("addition-next").performScrollTo().performClick()
        }
        assertEquals(2, facts.count { it in AdditionContent.catalogue() })
        assertTrue(AdditionFact(4, 7) in facts)
        assertTrue(AdditionFact(5, 9) in facts)
        compose.onNodeWithTag("addition-complete").assertExists()
    }

    @Test fun makeFiveShowsFilledAndEmptySpacesAndPractisesEveryBond() {
        val plan = (MakeFiveContent.generate(SessionId("make-five-screen"), RoundLength.FIVE, 19) as GenerationResult.Generated).plan
        val state = mutableStateOf(SessionReducer.start(plan, ContentLanguage.ENGLISH, MakeFiveContent.repository).state)
        compose.setContent {
            MaterialTheme {
                MakeFiveScreen(state.value, ContentLanguage.ENGLISH, false, false, false,
                    { state.value = SessionReducer.reduce(state.value, it).state }, {}, {}, {}, {}, Modifier.width(640.dp).height(360.dp))
            }
        }
        val missing = mutableSetOf<Int>()
        repeat(5) {
            val fact = MakeFiveContent.fact(state.value.task.question)
            missing += fact.missing
            compose.onNodeWithTag("make-five-prompt").performScrollTo()
                .assertTextEquals("You have ${fact.shown}. How many more make five?")
            val cells = List(5) { index -> if (index < fact.shown) "dot" else "empty" }.joinToString(", ")
            compose.onNodeWithTag("make-five-frame").performScrollTo()
                .assertContentDescriptionEquals("Five-frame: $cells")
            compose.onNodeWithTag("make-five-answer-${fact.missing}").performScrollTo().performClick()
            compose.onNodeWithTag("make-five-next").performScrollTo().performClick()
        }
        assertEquals((1..4).toSet(), missing)
        compose.onNodeWithTag("make-five-complete").assertExists()
    }
}
