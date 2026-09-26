package com.bellfamily.bastischool.ui.vocabulary

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.view.Surface
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.session.*
import com.bellfamily.bastischool.learning.vocabulary.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class VocabularyTextChoiceTest(private val german: Boolean, private val orientation: Int) {
    companion object {
        @JvmStatic @Parameterized.Parameters(name = "German={0},orientation={1}")
        fun cases() = listOf(
            arrayOf<Any>(false, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT),
            arrayOf<Any>(true, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT),
            arrayOf<Any>(true, ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE),
            arrayOf<Any>(true, ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE),
        )
    }
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

    @OptIn(ExperimentalTestApi::class)
    @Test fun nameChoicesKeepAuthoredLabelsIdentityLockingAndIndependentListen() {
        val landscape = orientation != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        compose.activity.requestedOrientation = orientation
        compose.waitUntil(10_000) { compose.activity.resources.configuration.orientation ==
            if (landscape) Configuration.ORIENTATION_LANDSCAPE else Configuration.ORIENTATION_PORTRAIT }
        val rotation = when (orientation) {
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE -> Surface.ROTATION_90
            ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE -> Surface.ROTATION_270
            else -> Surface.ROTATION_0
        }
        compose.waitUntil(10_000) { compose.activity.window.decorView.display?.rotation == rotation }
        val language = if (german) ContentLanguage.GERMAN else ContentLanguage.ENGLISH
        val plan = (VocabularyContent.generate(VocabularyPhase.NAME, SessionId("name-choice-ui"),
            RoundLength.FIVE, 42) as GenerationResult.Generated).plan
        var state by mutableStateOf(SessionReducer.start(plan, language, VocabularyContent.repository).state)
        var busy by mutableStateOf(false)
        var saveFailed by mutableStateOf(false)
        val answers = mutableListOf<SessionAction.Answer>()
        val heard = mutableListOf<ContentId>()
        compose.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, 1.5f)) {
                MaterialTheme {
                    VocabularyScreen(VocabularySelection(phase = VocabularyPhase.NAME), state, language,
                        busy, saveFailed, false, {}, {}, {}, {},
                        onAction = { action ->
                            if (action is SessionAction.Answer) answers += action
                            state = SessionReducer.reduce(state, action).state
                        }, onOption = { heard += it }, onAgain = {}, onRetry = {}, onHome = {},
                        modifier = Modifier.size(if (landscape) 700.dp else 280.dp, if (landscape) 240.dp else 480.dp))
                }
            }
        }
        val question = state.task.question
        question.choices.forEach { id ->
            val node = compose.onNodeWithTag("answer-${id.value}").performScrollTo()
                .assertIsDisplayed().assertIsEnabled().assertTextEquals(VocabularyContent.item(id).text.display[language])
                .assertHeightIsAtLeast(72.dp).assertWidthIsAtLeast(48.dp)
                .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button))
                .assert(SemanticsMatcher.keyNotDefined(SemanticsProperties.ContentDescription))
            val layouts = mutableListOf<TextLayoutResult>()
            node.performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(layouts) }
            assertFalse(layouts.single().hasVisualOverflow)
            // Shared text-choice typography, rather than the former filled Button's label style.
            assertEquals(FontWeight.SemiBold, layouts.single().layoutInput.style.fontWeight)
        }
        val correct = question.correct
        val answer = compose.onNodeWithTag("answer-${correct.value}")
        compose.runOnIdle { busy = true }
        answer.assertIsNotEnabled()
        compose.runOnIdle { busy = false; saveFailed = true }
        answer.assertIsNotEnabled()
        compose.runOnIdle { saveFailed = false }
        compose.onNodeWithTag("speaker-${correct.value}").performScrollTo().performClick()
        compose.runOnIdle { assertEquals(listOf(correct), heard); assertTrue(answers.isEmpty()); assertEquals(0, state.current.attempts) }
        val wrong = question.choices.first { it != correct }
        val attempt = state.nextAttempt!!
        compose.onNodeWithTag("answer-${wrong.value}").performScrollTo().performClick()
        compose.runOnIdle { assertEquals(SessionAction.Answer(attempt, wrong), answers.single()); assertEquals(AnswerState.RETRY_AVAILABLE, state.current.answer) }
        question.choices.forEach { compose.onNodeWithTag("answer-${it.value}").assertIsNotEnabled() }
        compose.onNodeWithTag("vocabulary-retry").performScrollTo().performClick()
        compose.onNodeWithTag("vocabulary-help").performScrollTo().performClick()
        compose.runOnIdle { assertTrue(state.current.support.hint); assertEquals(question.choices, state.task.question.choices) }
        answer.performScrollTo().performSemanticsAction(SemanticsActions.RequestFocus) { it() }
        answer.assertIsFocused().performKeyInput { pressKey(Key.Enter) }
        compose.runOnIdle { assertEquals(correct, answers.last().choice); assertEquals(1, state.score); assertEquals(2, state.current.attempts) }
        question.choices.forEach { compose.onNodeWithTag("answer-${it.value}").assertIsNotEnabled() }
        answer.performScrollTo().performTouchInput { click() }
        compose.onNodeWithTag("speaker-${correct.value}").performScrollTo().assertIsEnabled().performClick()
        compose.onNodeWithTag("vocabulary-next").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("vocabulary-home").performScrollTo().assertIsDisplayed()
        compose.runOnIdle { assertEquals(2, answers.size); assertEquals(listOf(correct, correct), heard); assertEquals(1, state.score) }
    }
}
