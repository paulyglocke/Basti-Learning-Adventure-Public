package com.bellfamily.bastischool.ui.addition

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
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.bellfamily.bastischool.learning.addition.AdditionContent
import com.bellfamily.bastischool.learning.models.ContentLanguage
import com.bellfamily.bastischool.learning.session.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class AdditionScreenTest {
    @get:Rule val compose = createComposeRule()
    private lateinit var state: MutableState<SessionState>
    private lateinit var input: InputModeManager
    private var replays = 0
    private var again = 0
    private fun show(language: ContentLanguage, short: Boolean = false) {
        fun fresh(id: String): SessionState {
            val plan = (AdditionContent.generate(SessionId(id), RoundLength.FIVE, 4) as GenerationResult.Generated).plan
            return SessionReducer.start(plan, language, AdditionContent.repository).state
        }
        state = mutableStateOf(fresh("ui-addition"))
        compose.setContent {
            input = LocalInputModeManager.current
            CompositionLocalProvider(LocalDensity provides Density(LocalDensity.current.density, 1.5f)) {
                MaterialTheme {
                    AdditionScreen(state.value, language, false, false, false,
                        { state.value = SessionReducer.reduce(state.value, it).state }, { replays++ },
                        { again++; state.value = fresh("again-$again") }, {}, {},
                        Modifier.width(if (short) 640.dp else 320.dp).height(if (short) 300.dp else 700.dp))
                }
            }
        }
    }
    private fun groups(language: ContentLanguage) {
        val fact = AdditionContent.definition(state.value.task.question)
        for ((first, n) in listOf(true to fact.left, false to fact.right)) {
            val tag = if (first) "addition-left" else "addition-right"
            val label = AdditionContent.groupDescription(n, first, language)
            compose.onNodeWithTag(tag).performScrollTo().assertContentDescriptionEquals(label).assert(hasNoClickAction())
            compose.onAllNodesWithContentDescription(label, useUnmergedTree = true).assertCountEquals(1)
            compose.onNodeWithTag(tag).onChildren().assertCountEquals(0)
        }
        compose.onNodeWithTag("addition-plus").assertIsDisplayed().assert(hasNoClickAction())
            .assert(SemanticsMatcher.keyNotDefined(SemanticsProperties.Text))
    }
    @OptIn(ExperimentalTestApi::class, androidx.compose.ui.ExperimentalComposeUiApi::class)
    @Test fun narrowEnglishSeparateGroupsAndNumericKeyboardAnswers() {
        show(ContentLanguage.ENGLISH)
        val totals = mutableSetOf<Int>()
        repeat(5) { index ->
            groups(ContentLanguage.ENGLISH)
            val q = state.value.task.question
            val total = AdditionContent.definition(q).total
            totals += total
            compose.onNodeWithTag("addition-prompt").performScrollTo().assertTextEquals("How many altogether?")
            compose.runOnIdle { assertTrue(input.requestInputMode(InputMode.Keyboard)) }
            val answer = compose.onNodeWithTag("addition-answer-${q.correct.value.substringAfter('.')}").performScrollTo().assertTextEquals(total.toString())
            answer.performSemanticsAction(SemanticsActions.RequestFocus) { assertTrue(it()) }
            answer.assertIsFocused().performKeyInput { pressKey(if (index % 2 == 0) Key.Enter else Key.DirectionCenter) }
            compose.runOnIdle { assertEquals(AnswerState.CORRECT, state.value.current.answer) }
            // The separate groups remain unchanged even after answering.
            groups(ContentLanguage.ENGLISH)
            compose.onNodeWithTag("addition-next").performScrollTo().performClick()
        }
        assertEquals(setOf(2, 3, 4, 5), totals)
        compose.onNodeWithTag("addition-complete").assertExists()
    }
    @Test fun shortGermanLargeTextHelpRetryListenCompletionAndAgain() {
        show(ContentLanguage.GERMAN, short = true)
        groups(ContentLanguage.GERMAN)
        compose.onNodeWithTag("addition-prompt").performScrollTo().assertTextEquals("Wie viele sind es zusammen?")
        compose.onNodeWithTag("addition-replay").performScrollTo().performClick()
        compose.runOnIdle { assertEquals(1, replays); assertEquals(0, state.value.current.attempts) }
        compose.onNodeWithTag("addition-help").performScrollTo().performClick()
        val q = state.value.task.question
        val wrong = q.choices.first { it != q.correct }
        compose.onNodeWithTag("addition-answer-${wrong.value.substringAfter('.')}").performScrollTo().performClick()
        compose.onNodeWithTag("addition-retry").performScrollTo().performClick()
        compose.runOnIdle { assertEquals(1, state.value.current.attempts); assertTrue(state.value.current.support.hint) }
        repeat(5) {
            groups(ContentLanguage.GERMAN)
            val correct = state.value.task.question.correct
            compose.onNodeWithTag("addition-answer-${correct.value.substringAfter('.')}").performScrollTo().performClick()
            compose.onNodeWithTag("addition-next").performScrollTo().performClick()
        }
        compose.onNodeWithTag("addition-complete").assertExists()
        compose.onNodeWithTag("addition-again").assertIsDisplayed().performClick()
        compose.runOnIdle { assertEquals(1, again); assertEquals(0, state.value.current.attempts); assertEquals(SessionPhase.ACTIVE, state.value.phase) }
    }
}
