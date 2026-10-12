package com.bellfamily.bastischool.ui.months

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.InputMode
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInputModeManager
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.*
import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.months.*
import com.bellfamily.bastischool.learning.progress.AttemptOutcome
import com.bellfamily.bastischool.learning.session.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class MonthsBuildYearScreenTest(private val german: Boolean, private val fontScale: Float, private val orientation: Int) {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
    companion object {
        @JvmStatic @Parameterized.Parameters(name = "de={0},font={1},orientation={2}")
        fun cases() = listOf(false, true).flatMap { de -> listOf(1f, 1.5f, 2f).flatMap { font ->
            listOf(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT, ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE).map { arrayOf<Any>(de, font, it) }
        } }
    }

    @OptIn(ExperimentalTestApi::class, androidx.compose.ui.ExperimentalComposeUiApi::class)
    @Test fun accessibleTapPlaceRetryCompletionAndResponsiveText() {
        val landscape = orientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        compose.activity.requestedOrientation = orientation
        compose.waitUntil(10_000) { compose.activity.resources.configuration.orientation ==
            if (landscape) Configuration.ORIENTATION_LANDSCAPE else Configuration.ORIENTATION_PORTRAIT }
        compose.waitForIdle()
        val language = if (german) ContentLanguage.GERMAN else ContentLanguage.ENGLISH
        val order = MonthsBuildYear.sequence(MonthIds.DECEMBER)
        var state by mutableStateOf(MonthsBuildYearState(SessionId("build-ui"), MonthIds.DECEMBER, language,
            com.bellfamily.bastischool.learning.sequencing.OrderedPlacement.scramble(order, 17),
            List(3) { com.bellfamily.bastischool.learning.session.TaskProgress() }))
        var listens = 0; var homes = 0; var replays = 0
        lateinit var input: androidx.compose.ui.input.InputModeManager
        compose.setContent {
            input = LocalInputModeManager.current
            CompositionLocalProvider(LocalDensity provides Density(LocalDensity.current.density, fontScale)) {
                MaterialTheme {
                    MonthsBuildYearScreen(state, language, false, false, false,
                        { action -> state = MonthsBuildYear.reduce(state, action).state }, { listens++ }, { replays++ }, {}, {}, { homes++ },
                        Modifier.size(if (landscape) 700.dp else 280.dp, if (landscape) 240.dp else 600.dp))
                }
            }
        }
        fun click(tag: String) = compose.onNodeWithTag(tag).performScrollTo().assertIsDisplayed().performClick()
        fun readable(node: SemanticsNodeInteraction) {
            val results = mutableListOf<TextLayoutResult>()
            node.performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(results) }
            val layout = results.single()
            assertFalse(layout.multiParagraph.didExceedMaxLines)
            repeat(layout.lineCount) { line ->
                assertFalse(layout.isLineEllipsized(line))
                assertTrue(layout.getLineLeft(line) >= -1f)
                assertTrue(layout.getLineRight(line) <= layout.size.width + 1f)
            }
        }
        val introduction = MonthsBuildYear.prompt(state).display[language]
        val prompt = compose.onNodeWithTag("months-build-year-prompt").performScrollTo().assertTextEquals(introduction)
        readable(prompt)
        compose.onNodeWithTag("months-build-year-example").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("months-build-year-slot-1").performScrollTo()
            .assertContentDescriptionEquals(if (german) "Position 1, frei. Lege den ausgewählten Monat hier ab." else "Position 1, empty. Place the selected month here.")
            .assertIsNotEnabled()

        val wrong = order[1]
        state.choices.forEach { id ->
            val tag = "months-build-year-choice-${id.value}"
            val tile = compose.onNodeWithTag(tag).performScrollTo().assertHeightIsAtLeast(64.dp)
            val text = compose.onNode(hasText(MonthContent.month(id).text.display[language]) and hasAnyAncestor(hasTestTag(tag)), useUnmergedTree = true)
            readable(text)
        }
        click("months-build-year-choice-${wrong.value}")
        compose.onNodeWithTag("months-build-year-slot-1").assertIsEnabled()
        // Keyboard/switch activation is available on the large semantic placement target.
        assertTrue(input.requestInputMode(InputMode.Keyboard))
        val slot = compose.onNodeWithTag("months-build-year-slot-1")
        slot.performSemanticsAction(SemanticsActions.RequestFocus) { it() }
        slot.performKeyInput { pressKey(androidx.compose.ui.input.key.Key.Enter) }
        compose.runOnIdle {
            assertEquals(AnswerState.RETRY_AVAILABLE, state.current.answer)
            assertTrue(state.placed.isEmpty())
            assertEquals(AttemptOutcome.INCORRECT, MonthsBuildYear.attempt(state, 0).outcome)
        }
        compose.onNodeWithTag("months-build-year-retry").performScrollTo().performClick()
        click("months-build-year-help")
        compose.onNodeWithTag("months-build-year-hint").performScrollTo().assertIsDisplayed()
        click("months-build-year-replay")
        click("months-build-year-listen-${MonthIds.DECEMBER.value}")
        order.forEachIndexed { index, month ->
            click("months-build-year-choice-${month.value}")
            click("months-build-year-slot-${index + 1}")
            compose.runOnIdle { assertEquals(order.take(index + 1), state.placed) }
        }
        compose.onNodeWithTag("months-build-year-home").assertIsDisplayed()
        compose.runOnIdle { assertEquals(1, listens); assertEquals(1, replays); assertTrue(state.completed) }
        compose.onNodeWithTag("months-build-year-home").performClick()
        compose.runOnIdle { assertEquals(1, homes) }
    }
}
