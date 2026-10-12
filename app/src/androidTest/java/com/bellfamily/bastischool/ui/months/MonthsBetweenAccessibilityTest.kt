package com.bellfamily.bastischool.ui.months

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.months.*
import com.bellfamily.bastischool.learning.session.*
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class MonthsBetweenAccessibilityTest {
    @get:Rule val compose = createAndroidComposeRule<androidx.activity.ComponentActivity>()

    @Test fun relationshipEndpointsAndAnswerActionsHaveBilingualSemanticsAtLargeText() {
        var language by mutableStateOf(ContentLanguage.ENGLISH)
        val plan = (MonthsBetween.generate(SessionId("between-a11y"), RoundLength.FIVE, 9) as GenerationResult.Generated).plan
        var state by mutableStateOf(SessionReducer.start(plan, language, MonthContent.repository).state)
        fun action(value: SessionAction) { state = SessionReducer.reduce(state, value).state }
        compose.setContent {
            CompositionLocalProvider(LocalDensity provides Density(LocalDensity.current.density, 2f)) {
                MaterialTheme {
                    MonthsQuizScreen(MonthsMode.BETWEEN, state, language, false, false, false, ::action,
                        { action(SessionAction.Replay(state.task.id)) }, {}, {}, {}, {}, {}, Modifier)
                }
            }
        }
        val definition = MonthsBetween.definition(state.task.question)
        val beforeEn = MonthContent.month(definition.before).text.display.en
        val afterEn = MonthContent.month(definition.after).text.display.en
        compose.onNodeWithTag("months-between-sequence").assertContentDescriptionEquals(
            "$beforeEn, then the month between, then $afterEn")
        compose.onNodeWithTag("months-between-before").performScrollTo()
            .assertContentDescriptionEquals("Before: $beforeEn")
        compose.onNodeWithTag("months-between-after").performScrollTo()
            .assertContentDescriptionEquals("After: $afterEn")
        val answer = state.task.question.correct
        val enChoice = compose.onNodeWithTag("months-answer-${answer.value}").performScrollTo()
        enChoice.assertContentDescriptionEquals("Answer choice: ${MonthContent.month(answer).text.display.en}")
            .assertHeightIsAtLeast(64.dp)

        compose.runOnIdle {
            language = ContentLanguage.GERMAN
            state = SessionReducer.reduce(state, SessionAction.Language(language)).state
        }
        val beforeDe = MonthContent.month(definition.before).text.display.de
        val afterDe = MonthContent.month(definition.after).text.display.de
        compose.onNodeWithTag("months-between-sequence").assertContentDescriptionEquals(
            "$beforeDe, dann der gesuchte Monat dazwischen, dann $afterDe")
        compose.onNodeWithTag("months-answer-${answer.value}").performScrollTo()
            .assertContentDescriptionEquals("Antwort: ${MonthContent.month(answer).text.display.de}")
            .performClick()
        compose.runOnIdle { assertEquals(AnswerState.CORRECT, state.current.answer) }
    }
}
