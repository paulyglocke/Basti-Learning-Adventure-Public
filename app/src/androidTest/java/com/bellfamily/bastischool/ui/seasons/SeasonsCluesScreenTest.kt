package com.bellfamily.bastischool.ui.seasons

import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.bellfamily.bastischool.learning.models.ContentLanguage
import com.bellfamily.bastischool.learning.seasons.*
import com.bellfamily.bastischool.learning.session.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class SeasonsCluesScreenTest {
    @get:Rule val compose = createComposeRule()

    @Test fun englishClueRetryHelpAndCompletion() = round(ContentLanguage.ENGLISH, false)
    @Test fun germanClueLargeTextRetryHelpAndCompletion() = round(ContentLanguage.GERMAN, true)

    private fun round(language: ContentLanguage, large: Boolean) {
        val plan = (SeasonsClues.generate(SessionId("ui-clues"), RoundLength.FIVE, 42) as GenerationResult.Generated).plan
        val state = mutableStateOf(SessionReducer.start(plan, language, SeasonsContent.repository).state)
        var listens = 0
        var again = 0
        var home = 0
        compose.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, if (large) 1.5f else 1f)) {
                MaterialTheme {
                    SeasonsScreen(SeasonsSelection(phase = SeasonsPhase.CLUES), state.value, language, null,
                        false, false, false, false, onSelect = {}, onPhase = {}, onReplay = {
                            listens++
                            state.value = SessionReducer.reduce(state.value, SessionAction.Replay(state.value.task.id)).state
                        }, onAction = { state.value = SessionReducer.reduce(state.value, it).state },
                        onOption = { listens++ }, onAgain = { again++ }, onRetry = {}, onHome = { home++ },
                        modifier = Modifier.width(320.dp))
                }
            }
        }
        compose.onNodeWithTag(SeasonsPhase.CLUES.tag).performScrollTo().assertIsSelected()
        compose.onNodeWithTag("season-artwork").assertDoesNotExist()
        compose.onNodeWithTag("season-anchor").assertDoesNotExist()
        compose.onNodeWithTag("seasons-missing-sequence").assertDoesNotExist()
        val first = state.value.task.question
        compose.onNodeWithTag("seasons-prompt").performScrollTo().assertIsDisplayed()
            .assertTextEquals(first.instruction.display[language])
        for (id in first.choices) {
            compose.onNodeWithTag("answer-${id.value}").performScrollTo().assertIsDisplayed()
                .assertTextEquals(SeasonsContent.season(id).text.display[language])
        }
        compose.onNodeWithTag("speaker-${first.correct.value}").performScrollTo().performClick()
        compose.onNodeWithTag("seasons-replay").performScrollTo().performClick()
        compose.runOnIdle { assertEquals(2, listens); assertEquals(0, state.value.current.attempts) }
        compose.onNodeWithTag("answer-${first.choices.first { it != first.correct }.value}").performScrollTo().performClick()
        compose.runOnIdle { assertEquals(AnswerState.RETRY_AVAILABLE, state.value.current.answer) }
        compose.onNodeWithTag("seasons-retry").performScrollTo().performClick()
        compose.onNodeWithTag("seasons-hint").performScrollTo().performClick()
        compose.onNodeWithText(first.hint!!.display[language]).performScrollTo().assertIsDisplayed()
        repeat(5) { index ->
            compose.onNodeWithTag("seasons-progress").performScrollTo().assertTextEquals(
                if (language == ContentLanguage.GERMAN) "Frage ${index + 1} von 5" else "Question ${index + 1} of 5")
            compose.onNodeWithTag("seasons-prompt").performScrollTo().assertIsDisplayed()
                .assertTextEquals(state.value.task.question.instruction.display[language])
            val answer = compose.onNodeWithTag("answer-${state.value.task.question.correct.value}")
            answer.performScrollTo().performClick().assertIsNotEnabled()
            compose.onNodeWithTag("seasons-next").performScrollTo().performClick()
        }
        compose.onNodeWithTag("completion-celebration").assertExists()
        compose.onNodeWithTag("seasons-again").assertIsDisplayed().performClick()
        compose.onNodeWithTag("seasons-home").assertIsDisplayed().performClick()
        compose.runOnIdle {
            assertEquals(SessionPhase.COMPLETED, state.value.phase)
            assertEquals(1, again); assertEquals(1, home)
        }
    }
}
