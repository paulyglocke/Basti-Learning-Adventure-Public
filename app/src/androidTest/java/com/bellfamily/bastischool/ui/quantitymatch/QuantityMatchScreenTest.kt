package com.bellfamily.bastischool.ui.quantitymatch

import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.InputMode
import androidx.compose.ui.input.InputModeManager
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInputModeManager
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.quantitymatch.*
import com.bellfamily.bastischool.learning.session.*
import com.bellfamily.bastischool.learning.subitising.SubitisingContent
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class QuantityMatchScreenTest {
    @get:Rule val compose = createComposeRule()
    private lateinit var state: MutableState<SessionState>
    private lateinit var input: InputModeManager
    private fun show(seed: Long, language: ContentLanguage, short: Boolean = false) {
        val plan = (QuantityMatchContent.generate(SessionId("ui"), RoundLength.FIVE, seed) as GenerationResult.Generated).plan
        state = mutableStateOf(SessionReducer.start(plan, language, SubitisingContent.repository).state)
        compose.setContent {
            input = LocalInputModeManager.current
            CompositionLocalProvider(LocalDensity provides Density(LocalDensity.current.density, 1.5f)) {
                MaterialTheme { QuantityMatchScreen(state.value, language, false, false, false,
                    { state.value = SessionReducer.reduce(state.value, it).state }, {}, {}, {}, {},
                    Modifier.width(if (short) 640.dp else 320.dp).height(if (short) 300.dp else 700.dp)) }
            }
        }
    }
    private fun advanceTo(direction: QuantityMatchContent.Direction) = compose.runOnIdle {
        val index = state.value.plan.tasks.indexOfFirst { QuantityMatchContent.definition(it.question).second == direction }
        require(index >= 0)
        repeat(index) {
            val current = state.value
            state.value = SessionReducer.reduce(current, SessionAction.Answer(current.nextAttempt!!, current.task.question.correct)).state
            state.value = SessionReducer.reduce(state.value, SessionAction.Next(state.value.task.id)).state
        }
    }
    @OptIn(ExperimentalTestApi::class, androidx.compose.ui.ExperimentalComposeUiApi::class)
    @Test fun quantityToNumeralHasOneSemanticPromptGroupAndKeyboardAnswer() {
        show(0, ContentLanguage.ENGLISH)
        advanceTo(QuantityMatchContent.Direction.QUANTITY_TO_NUMERAL)
        val n = SubitisingContent.quantity(state.value.task.question.correct)
        compose.onNodeWithTag("quantity-match-prompt-dots").assertContentDescriptionEquals(SubitisingContent.description(n).display.en)
        compose.onAllNodesWithContentDescription(SubitisingContent.description(n).display.en).assertCountEquals(1)
        compose.runOnIdle { assertTrue(input.requestInputMode(InputMode.Keyboard)) }
        val answer = compose.onNodeWithTag("quantity-match-answer-$n").performScrollTo()
        answer.performSemanticsAction(SemanticsActions.RequestFocus) { assertTrue(it()) }
        answer.assertIsFocused().performKeyInput { pressKey(Key.Enter) }
        compose.runOnIdle { assertEquals(AnswerState.CORRECT, state.value.current.answer) }
    }
    @OptIn(ExperimentalTestApi::class, androidx.compose.ui.ExperimentalComposeUiApi::class)
    @Test fun numeralToQuantityUsesIndependentSemanticPatternChoicesInGerman() {
        show(1, ContentLanguage.GERMAN)
        advanceTo(QuantityMatchContent.Direction.NUMERAL_TO_QUANTITY)
        val n = SubitisingContent.quantity(state.value.task.question.correct)
        compose.onNodeWithTag("quantity-match-prompt").assertTextEquals("Welche Gruppe zeigt $n?")
        state.value.task.question.choices.forEach {
            val choice = SubitisingContent.quantity(it)
            compose.onNodeWithTag("quantity-match-answer-$choice").assertContentDescriptionEquals(SubitisingContent.description(choice).display.de).assertHasClickAction()
            compose.onAllNodesWithContentDescription(SubitisingContent.description(choice).display.de, useUnmergedTree = true).assertCountEquals(1)
        }
        compose.runOnIdle { assertTrue(input.requestInputMode(InputMode.Keyboard)) }
        val answer = compose.onNodeWithTag("quantity-match-answer-$n").performScrollTo()
        answer.performSemanticsAction(SemanticsActions.RequestFocus) { assertTrue(it()) }
        answer.assertIsFocused().performKeyInput { pressKey(Key.DirectionCenter) }
        compose.runOnIdle { assertEquals(AnswerState.CORRECT, state.value.current.answer) }
    }

    @Test fun shortLandscapeHelpRetryAndCompletionRemainReachable() {
        show(2, ContentLanguage.GERMAN, short = true)
        compose.onNodeWithTag("quantity-match-help").performScrollTo().performClick()
        val q = state.value.task.question
        val wrong = SubitisingContent.quantity(q.choices.first { it != q.correct })
        compose.onNodeWithTag("quantity-match-answer-$wrong").performScrollTo().performClick()
        compose.onNodeWithTag("quantity-match-retry").performScrollTo().performClick()
        compose.runOnIdle { assertEquals(1, state.value.current.attempts); assertTrue(state.value.current.support.hint) }
        repeat(5) {
            val n = SubitisingContent.quantity(state.value.task.question.correct)
            compose.onNodeWithTag("quantity-match-answer-$n").performScrollTo().performClick()
            compose.onNodeWithTag("quantity-match-next").performScrollTo().performClick()
        }
        compose.onNodeWithTag("quantity-match-complete").assertExists()
        compose.onNodeWithTag("quantity-match-again").assertIsDisplayed()
        compose.onNodeWithTag("quantity-match-home").assertIsDisplayed()
    }
}
