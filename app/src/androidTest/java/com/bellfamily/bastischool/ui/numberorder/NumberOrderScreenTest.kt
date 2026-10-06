package com.bellfamily.bastischool.ui.numberorder

import androidx.compose.foundation.layout.*
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
import com.bellfamily.bastischool.learning.models.ContentLanguage
import com.bellfamily.bastischool.learning.numberorder.NumberOrderContent
import com.bellfamily.bastischool.learning.session.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class NumberOrderScreenTest {
    @get:Rule val compose = createComposeRule()
    private lateinit var state: MutableState<SessionState>
    private lateinit var input: InputModeManager
    private var listens = 0
    private var again = 0
    private fun show(language: ContentLanguage, short: Boolean = false) {
        fun fresh(id: String): SessionState {
            val p = (NumberOrderContent.generate(SessionId(id), RoundLength.FIVE, 11) as GenerationResult.Generated).plan
            return SessionReducer.start(p, language, NumberOrderContent.repository).state
        }
        state = mutableStateOf(fresh("ui-order"))
        compose.setContent {
            input = LocalInputModeManager.current
            CompositionLocalProvider(LocalDensity provides Density(LocalDensity.current.density, 1.5f)) {
                MaterialTheme {
                    NumberOrderScreen(state.value, language, false, false, false,
                        { state.value = SessionReducer.reduce(state.value, it).state }, { listens++ },
                        { again++; state.value = fresh("fresh-$again") }, {}, {},
                        Modifier.width(if (short) 640.dp else 320.dp).height(if (short) 300.dp else 700.dp))
                }
            }
        }
    }
    private fun checkPresentation(language: ContentLanguage) {
        val q = state.value.task.question
        compose.onNodeWithTag("number-order-prompt").performScrollTo().assertTextEquals(q.instruction.display[language])
        when (val item = NumberOrderContent.definition(q)) {
            is NumberOrderContent.Neighbour -> {
                compose.onNodeWithTag("number-order-anchor").performScrollTo().assertTextEquals(item.anchor.toString()).assert(hasNoClickAction())
                compose.onNodeWithTag("number-order-sequence").assertDoesNotExist()
            }
            is NumberOrderContent.Missing -> {
                val label = NumberOrderContent.sequenceDescription(item, language)
                compose.onNodeWithTag("number-order-sequence").performScrollTo().assertContentDescriptionEquals(label).assert(hasNoClickAction())
                compose.onAllNodesWithContentDescription(label, useUnmergedTree = true).assertCountEquals(1)
                compose.onNodeWithTag("number-order-sequence").onChildren().assertCountEquals(0)
                compose.onNodeWithTag("number-order-anchor").assertDoesNotExist()
            }
        }
    }
    @OptIn(ExperimentalTestApi::class, androidx.compose.ui.ExperimentalComposeUiApi::class)
    @Test fun narrowEnglishAllTypesAndKeyboardAnswersComplete() {
        show(ContentLanguage.ENGLISH)
        val types = mutableSetOf<NumberOrderContent.Type>()
        repeat(5) {
            checkPresentation(ContentLanguage.ENGLISH)
            val q = state.value.task.question
            val item = NumberOrderContent.definition(q)
            types += item.type
            compose.runOnIdle { assertTrue(input.requestInputMode(InputMode.Keyboard)) }
            val answer = compose.onNodeWithTag("number-order-answer-${q.correct.value.substringAfter('.')}").performScrollTo().assertTextEquals(item.answer.toString())
            answer.performSemanticsAction(SemanticsActions.RequestFocus) { assertTrue(it()) }
            answer.assertIsFocused().performKeyInput { pressKey(if (item.type == NumberOrderContent.Type.MISSING) Key.DirectionCenter else Key.Enter) }
            compose.runOnIdle { assertEquals(AnswerState.CORRECT, state.value.current.answer) }
            compose.onNodeWithTag("number-order-next").performScrollTo().performClick()
        }
        assertEquals(NumberOrderContent.Type.entries.toSet(), types)
        compose.onNodeWithTag("number-order-complete").assertExists()
    }
    @Test fun shortGermanAllTypesHelpRetryListenAndAgain() {
        show(ContentLanguage.GERMAN, short = true)
        compose.onNodeWithTag("number-order-replay").performScrollTo().performClick()
        compose.runOnIdle { assertEquals(1, listens); assertEquals(0, state.value.current.attempts) }
        compose.onNodeWithTag("number-order-help").performScrollTo().performClick()
        val q = state.value.task.question
        val wrong = q.choices.first { it != q.correct }
        compose.onNodeWithTag("number-order-answer-${wrong.value.substringAfter('.')}").performScrollTo().performClick()
        compose.onNodeWithTag("number-order-retry").performScrollTo().performClick()
        compose.runOnIdle { assertTrue(state.value.current.support.hint); assertEquals(1, state.value.current.attempts) }
        repeat(5) {
            checkPresentation(ContentLanguage.GERMAN)
            val correct = state.value.task.question.correct
            compose.onNodeWithTag("number-order-answer-${correct.value.substringAfter('.')}").performScrollTo().performClick()
            compose.onNodeWithTag("number-order-next").performScrollTo().performClick()
        }
        compose.onNodeWithTag("number-order-complete").assertExists()
        compose.onNodeWithTag("number-order-again").assertIsDisplayed().performClick()
        compose.runOnIdle { assertEquals(1, again); assertEquals(0, state.value.current.attempts); assertEquals(SessionPhase.ACTIVE, state.value.phase) }
    }
}
