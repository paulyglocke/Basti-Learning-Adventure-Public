package com.bellfamily.bastischool.ui.months

import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.input.InputMode
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInputModeManager
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.*
import androidx.activity.ComponentActivity
import com.bellfamily.bastischool.learning.content.SeasonIds
import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.months.*
import com.bellfamily.bastischool.learning.session.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class MonthsSeasonsMatchScreenTest(private val german: Boolean, private val fontScale: Float, private val landscape: Boolean) {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
    companion object {
        @JvmStatic @Parameterized.Parameters(name = "de={0},font={1},landscape={2}")
        fun cases() = listOf(false, true).flatMap { de -> listOf(1f, 1.5f, 2f).flatMap { font ->
            listOf(false, true).map { land -> arrayOf<Any>(de, font, land) }
        } }
    }

    @OptIn(ExperimentalTestApi::class, androidx.compose.ui.ExperimentalComposeUiApi::class)
    @Test fun illustratedGroupsSupportKeyboardRetryGuideAndWrapping() {
        val language = if (german) ContentLanguage.GERMAN else ContentLanguage.ENGLISH
        var state by mutableStateOf(SessionReducer.start(
            (MonthsSeasonsMatch.generate(SessionId("season-ui"), RoundLength.FIVE, 81) as GenerationResult.Generated).plan,
            language, com.bellfamily.bastischool.learning.content.CoreContent.repository()).state)
        var replays = 0; var again = 0; var homes = 0
        var input: androidx.compose.ui.input.InputModeManager? = null
        val art = SeasonIds.canonicalOrder.associateWith { ImageBitmap(4, 4) }
        compose.setContent {
            input = LocalInputModeManager.current
            CompositionLocalProvider(LocalDensity provides Density(LocalDensity.current.density, fontScale)) {
                MaterialTheme {
                    MonthsSeasonsMatchScreen(state, art, language, false, false, false, false,
                        { action -> state = SessionReducer.reduce(state, action).state }, { replays++ }, { again++ }, {}, { homes++ },
                        Modifier.size(if (landscape) 700.dp else 320.dp, if (landscape) 240.dp else 640.dp))
                }
            }
        }
        val month = MonthsSeasonsMatch.month(state.task.question)
        val target = state.task.question.correct
        val wrong = state.task.question.choices.first { it != target }
        val displayedMonth = MonthContent.month(month).text.display[language]
        val monthNode = compose.onNodeWithTag("months-seasons-month-tile").performScrollTo()
            .assertTextEquals(displayedMonth)
        val layoutResults = mutableListOf<TextLayoutResult>()
        monthNode.performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(layoutResults) }
        val monthLayout = layoutResults.single()
        assertFalse(monthLayout.multiParagraph.didExceedMaxLines)
        repeat(monthLayout.lineCount) { line ->
            assertFalse(monthLayout.isLineEllipsized(line))
            assertTrue(monthLayout.getLineLeft(line) >= -1f)
            assertTrue(monthLayout.getLineRight(line) <= monthLayout.size.width + 1f)
        }
        compose.onNodeWithTag("months-season-target-${target.value}").performScrollTo()
            .assertHasClickAction().assertHeightIsAtLeast(150.dp)
            .assertContentDescriptionEquals(if (german) {
                "In ${MonthContent.season(target).text.display[language]} einordnen. Noch keine Monate zugeordnet"
            } else {
                "Place in ${MonthContent.season(target).text.display[language]}. No months grouped yet"
            })
        SeasonIds.canonicalOrder.forEach { season ->
            compose.onNodeWithTag("months-season-target-${season.value}").performScrollTo().assertIsDisplayed()
        }
        compose.onNodeWithTag("months-seasons-guide").performScrollTo().performClick()
        compose.onNodeWithTag("months-seasons-guide-content").performScrollTo().assertIsDisplayed()
        compose.runOnIdle { assertTrue(state.current.support.hint) }
        compose.onNodeWithTag("months-seasons-replay").performScrollTo().performClick()
        assertEquals(1, replays)

        assertTrue(input!!.requestInputMode(InputMode.Keyboard))
        val wrongTarget = compose.onNodeWithTag("months-season-target-${wrong.value}").performScrollTo()
        wrongTarget.performSemanticsAction(SemanticsActions.RequestFocus) { it() }
        wrongTarget.performKeyInput { pressKey(androidx.compose.ui.input.key.Key.Enter) }
        compose.onNodeWithTag("months-seasons-retry-message").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("months-seasons-retry-answer").performScrollTo().performClick()
        val correctTarget = compose.onNodeWithTag("months-season-target-${target.value}").performScrollTo()
        correctTarget.performSemanticsAction(SemanticsActions.RequestFocus) { it() }
        correctTarget.performKeyInput { pressKey(androidx.compose.ui.input.key.Key.Enter) }
        compose.onNodeWithTag("months-seasons-correct").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("months-season-target-${target.value}").assertTextContains(displayedMonth)
        compose.onNodeWithTag("months-seasons-next").performScrollTo().performClick()
        compose.onNodeWithTag("months-seasons-progress").performScrollTo().assertTextEquals(
            if (german) "Frage 2 von 5" else "Question 2 of 5")
        compose.onNodeWithTag("months-seasons-home").performScrollTo().performClick()
        compose.runOnIdle { assertEquals(1, homes); assertEquals(0, again) }
    }
}
