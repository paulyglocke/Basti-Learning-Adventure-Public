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
class MonthsBuildYearFourScreenTest(private val german: Boolean, private val fontScale: Float, private val orientation: Int) {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
    companion object {
        @JvmStatic @Parameterized.Parameters(name = "four-de={0},font={1},orientation={2}")
        fun cases() = listOf(false, true).flatMap { de -> listOf(1f, 1.5f, 2f).flatMap { font ->
            listOf(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT, ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE).map { arrayOf<Any>(de, font, it) }
        } }
    }

    @OptIn(ExperimentalTestApi::class, androidx.compose.ui.ExperimentalComposeUiApi::class)
    @Test fun fourMonthTapPlacementRetryKeyboardSemanticsAndResponsiveText() {
        val landscape = orientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        compose.activity.requestedOrientation = orientation
        compose.waitUntil(10_000) { compose.activity.resources.configuration.orientation ==
            if (landscape) Configuration.ORIENTATION_LANDSCAPE else Configuration.ORIENTATION_PORTRAIT }
        compose.waitForIdle()
        val language = if (german) ContentLanguage.GERMAN else ContentLanguage.ENGLISH
        var state by mutableStateOf(MonthsBuildYearFour.generate(SessionId("build-four-ui"), RoundLength.FIVE, 19, language))
        var listens=0;var replays=0;var homes=0;var lastEvents=emptyList<com.bellfamily.bastischool.learning.progress.ProgressEvent>()
        lateinit var input: androidx.compose.ui.input.InputModeManager
        compose.setContent {
            input = LocalInputModeManager.current
            CompositionLocalProvider(LocalDensity provides Density(LocalDensity.current.density, fontScale)) {
                MaterialTheme {
                    MonthsBuildYearFourScreen(state, language, false, false, false,
                        { action -> MonthsBuildYearFour.reduce(state, action).also { lastEvents=it.events;state=it.state } },
                        { listens++ }, { replays++ }, {}, {}, { homes++ },
                        Modifier.size(if (landscape) 700.dp else 280.dp, if (landscape) 240.dp else 600.dp))
                }
            }
        }
        fun click(tag:String) = compose.onNodeWithTag(tag).performScrollTo().assertIsDisplayed().performClick()
        fun readable(node: SemanticsNodeInteraction) {
            val results = mutableListOf<TextLayoutResult>()
            node.performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(results) }
            val layout=results.single()
            assertFalse(layout.multiParagraph.didExceedMaxLines)
            repeat(layout.lineCount) { line ->
                assertFalse(layout.isLineEllipsized(line))
                assertTrue(layout.getLineLeft(line) >= -1f)
                assertTrue(layout.getLineRight(line) <= layout.size.width + 1f)
            }
        }
        val expectedPrompt=MonthsBuildYearFour.introduction.display[language]
        val prompt=compose.onNodeWithTag("months-build-year-four-prompt").performScrollTo().assertTextEquals(expectedPrompt)
        readable(prompt)
        compose.onNodeWithTag("months-build-year-four-example").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("months-build-year-four-progress").performScrollTo()
            .assertTextEquals(if(german) "Folge 1 von 5" else "Sequence 1 of 5")
        compose.onNodeWithTag("months-build-year-four-slot-1").performScrollTo()
            .assertContentDescriptionEquals(if(german) "Position 1, frei. Lege den ausgewählten Monat hier ab." else "Position 1, empty. Place the selected month here.")
        state.choices.first().forEach { id ->
            val name=MonthContent.month(id).text.display[language]
            val choice=compose.onNodeWithTag("months-build-year-four-choice-${id.value}").performScrollTo().assertHeightIsAtLeast(64.dp)
            choice.assertContentDescriptionContains(name)
            readable(compose.onNode(hasText(name) and hasAnyAncestor(hasTestTag("months-build-year-four-choice-${id.value}")), useUnmergedTree=true))
        }

        val wrong=state.choices.first().first { it != state.order.first() }
        click("months-build-year-four-choice-${wrong.value}")
        var keyboardModeRequested = false
        compose.runOnUiThread { keyboardModeRequested = input.requestInputMode(InputMode.Keyboard) }
        assertTrue(keyboardModeRequested)
        val slot=compose.onNodeWithTag("months-build-year-four-slot-1")
        slot.performSemanticsAction(SemanticsActions.RequestFocus) { it() }
        slot.performKeyInput { pressKey(androidx.compose.ui.input.key.Key.Enter) }
        compose.runOnIdle {
            assertEquals(AnswerState.RETRY_AVAILABLE,state.current.answer)
            assertTrue(state.placed.isEmpty())
            assertEquals(AttemptOutcome.INCORRECT,lastEvents.filterIsInstance<com.bellfamily.bastischool.learning.progress.AttemptEvent>().single().outcome)
        }
        click("months-build-year-four-retry")
        click("months-build-year-four-help")
        compose.onNodeWithTag("months-build-year-four-hint").performScrollTo().assertIsDisplayed()
        click("months-build-year-four-replay")
        val spoken=state.choices.first().first()
        click("months-build-year-four-listen-${spoken.value}")
        state.order.forEachIndexed { index, month ->
            click("months-build-year-four-choice-${month.value}")
            click("months-build-year-four-slot-${index+1}")
            compose.runOnIdle { assertEquals(state.order.take(index+1),state.placed) }
        }
        assertEquals(1,listens);assertEquals(1,replays)
        click("months-build-year-four-continue")
        compose.runOnIdle { assertEquals(1,state.questionIndex);assertFalse(state.completed) }
        click("months-build-year-four-home")
        compose.runOnIdle { assertEquals(1,homes) }
    }
}
