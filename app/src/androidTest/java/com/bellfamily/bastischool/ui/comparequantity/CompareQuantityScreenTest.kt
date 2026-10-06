package com.bellfamily.bastischool.ui.comparequantity

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
import com.bellfamily.bastischool.learning.comparequantity.CompareQuantityContent
import com.bellfamily.bastischool.learning.models.ContentLanguage
import com.bellfamily.bastischool.learning.session.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class CompareQuantityScreenTest {
    @get:Rule val compose = createComposeRule()
    private lateinit var state: MutableState<SessionState>
    private lateinit var input: InputModeManager
    private var replayCount = 0
    private var againCount = 0
    private fun show(language: ContentLanguage, short: Boolean = false) {
        fun fresh(id: String): SessionState {
            val plan = (CompareQuantityContent.generate(SessionId(id), RoundLength.FIVE, 17) as GenerationResult.Generated).plan
            return SessionReducer.start(plan, language, CompareQuantityContent.repository).state
        }
        state = mutableStateOf(fresh("ui-compare"))
        compose.setContent {
            input = LocalInputModeManager.current
            CompositionLocalProvider(LocalDensity provides Density(LocalDensity.current.density, 1.5f)) {
                MaterialTheme {
                    CompareQuantityScreen(state.value, language, false, false, false,
                        { state.value = SessionReducer.reduce(state.value, it).state }, { replayCount++ },
                        { againCount++; state.value = fresh("again-$againCount") }, {}, {},
                        Modifier.width(if (short) 640.dp else 320.dp).height(if (short) 300.dp else 700.dp))
                }
            }
        }
    }
    private fun checkGroups(language: ContentLanguage) {
        val pair = CompareQuantityContent.definition(state.value.task.question)
        for ((left, n) in listOf(true to pair.left, false to pair.right)) {
            val side = if (left) "left" else "right"
            val description = CompareQuantityContent.groupDescription(n, left, language)
            compose.onNodeWithTag("compare-quantity-$side").performScrollTo().assertContentDescriptionEquals(description).assert(hasNoClickAction())
            compose.onAllNodesWithContentDescription(description, useUnmergedTree = true).assertCountEquals(1)
        }
        // Child labels/canvases (and hence dots) do not add semantic targets.
        compose.onNodeWithTag("compare-quantity-left").onChildren().assertCountEquals(0)
        compose.onNodeWithTag("compare-quantity-right").onChildren().assertCountEquals(0)
    }
    @OptIn(ExperimentalTestApi::class, androidx.compose.ui.ExperimentalComposeUiApi::class)
    @Test fun narrowEnglishGroupsAndAllThreeAnswersHaveKeyboardActivation() {
        show(ContentLanguage.ENGLISH)
        val seen = mutableSetOf<String>()
        repeat(5) {
            checkGroups(ContentLanguage.ENGLISH)
            for (answer in CompareQuantityContent.answers) {
                val side = answer.value.substringAfter('.')
                compose.onNodeWithTag("compare-quantity-answer-$side").assertTextEquals(CompareQuantityContent.label(answer).display.en).assertHasClickAction()
            }
            val side = state.value.task.question.correct.value.substringAfter('.')
            seen += side
            compose.runOnIdle { assertTrue(input.requestInputMode(InputMode.Keyboard)) }
            val node = compose.onNodeWithTag("compare-quantity-answer-$side").performScrollTo()
            node.performSemanticsAction(SemanticsActions.RequestFocus) { assertTrue(it()) }
            node.assertIsFocused().performKeyInput { pressKey(if (side == "same") Key.DirectionCenter else Key.Enter) }
            compose.runOnIdle { assertEquals(AnswerState.CORRECT, state.value.current.answer) }
            compose.onNodeWithTag("compare-quantity-next").performScrollTo().performClick()
        }
        assertEquals(setOf("left", "right", "same"), seen)
        compose.onNodeWithTag("compare-quantity-complete").assertExists()
    }
    @Test fun shortGermanHelpRetryReplayCompletionAndAgainAreReachable() {
        show(ContentLanguage.GERMAN, short = true)
        checkGroups(ContentLanguage.GERMAN)
        compose.onNodeWithTag("compare-quantity-prompt").performScrollTo().assertTextEquals("Welche Seite hat mehr?")
        compose.onNodeWithTag("compare-quantity-replay").performScrollTo().performClick()
        compose.runOnIdle { assertEquals(1, replayCount); assertEquals(0, state.value.current.attempts) }
        compose.onNodeWithTag("compare-quantity-help").performScrollTo().performClick()
        val wrong = state.value.task.question.choices.first { it != state.value.task.question.correct }.value.substringAfter('.')
        compose.onNodeWithTag("compare-quantity-answer-$wrong").performScrollTo().performClick()
        compose.onNodeWithTag("compare-quantity-retry").performScrollTo().performClick()
        compose.runOnIdle { assertEquals(1, state.value.current.attempts); assertTrue(state.value.current.support.hint) }
        repeat(5) {
            val correct = state.value.task.question.correct
            compose.onNodeWithTag("compare-quantity-answer-${correct.value.substringAfter('.')}").performScrollTo()
                .assertTextEquals(CompareQuantityContent.label(correct).display.de).performClick()
            compose.onNodeWithTag("compare-quantity-next").performScrollTo().performClick()
        }
        compose.onNodeWithTag("compare-quantity-complete").assertExists()
        compose.onNodeWithTag("compare-quantity-again").assertIsDisplayed().performClick()
        compose.runOnIdle { assertEquals(1, againCount); assertEquals(0, state.value.current.attempts); assertEquals(SessionPhase.ACTIVE, state.value.phase) }
        checkGroups(ContentLanguage.GERMAN)
    }
}
