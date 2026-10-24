package com.bellfamily.bastischool.ui.colours

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.bellfamily.bastischool.learning.colours.*
import com.bellfamily.bastischool.learning.models.ContentLanguage
import com.bellfamily.bastischool.learning.session.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class MatchColoursScreenTest {
    @get:Rule val compose = createComposeRule()
    @Test fun visualTargetChoicesRetryAndCorrectAdvanceRemainVisibleAndActionable() {
        val plan = (MatchColoursContent.generate(SessionId("match-screen"), RoundLength.FIVE, 65L) as GenerationResult.Generated).plan
        var state by mutableStateOf(SessionReducer.start(plan, ContentLanguage.ENGLISH, MatchColoursContent.repository).state)
        compose.setContent {
            MaterialTheme { MatchColoursScreen(state, ContentLanguage.ENGLISH, false, false, false, {}, {},
                { state = SessionReducer.reduce(state, it).state }, {}, {}, {}, Modifier) }
        }
        compose.onNodeWithTag("colour-match-target").assertIsDisplayed()
        val question = state.task.question
        val correct = question.correct
        val incorrect = question.choices.first { it != correct }
        assertNotEquals(MatchColoursContent.targetShape(question), MatchColoursContent.sample(correct).shape)
        question.choices.forEach { id -> compose.onNodeWithTag("colour-match-choice-${id.value}").performScrollTo().assertIsDisplayed().assertHasClickAction() }
        compose.onNodeWithTag("colour-match-choice-${incorrect.value}").performScrollTo().performClick()
        compose.onNodeWithTag("colour-match-feedback-wrong").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("colour-match-retry").performScrollTo().performClick()
        compose.onNodeWithTag("colour-match-choice-${correct.value}").performScrollTo().performClick()
        compose.onNodeWithTag("colour-match-feedback-correct").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("colour-match-next").performScrollTo().performClick()
        compose.runOnIdle { assertEquals(1, state.index) }
    }
}
