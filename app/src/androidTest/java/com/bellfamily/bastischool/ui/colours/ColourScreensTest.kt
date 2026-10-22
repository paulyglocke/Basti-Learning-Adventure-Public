package com.bellfamily.bastischool.ui.colours

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.bellfamily.bastischool.learning.colours.ColoursContent
import com.bellfamily.bastischool.learning.models.ContentLanguage
import com.bellfamily.bastischool.learning.session.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class ColourScreensTest {
    @get:Rule val compose = createComposeRule()

    @Test fun exploreSelectsAllFourSwatchesShowsSelectionAndExposesAccessibleNames() {
        var selected by mutableStateOf(ColoursContent.red)
        var replay = 0
        compose.setContent {
            MaterialTheme {
                ExploreColoursScreen(selected, ContentLanguage.ENGLISH, false, false, false,
                    { selected = it }, { replay++ }, {}, {}, Modifier)
            }
        }
        ColoursContent.order.forEach { id ->
            compose.onNodeWithTag("colours-explore-choice-${id.value}").assertIsDisplayed().assertHasClickAction()
                .assertHeightIsAtLeast(56.dp).assertWidthIsAtLeast(56.dp)
                .assertContentDescriptionEquals(ColoursContent.definition(id).text.display.en.replaceFirstChar { it.uppercase() })
            compose.onNodeWithTag("colours-explore-choice-${id.value}").performScrollTo().performClick()
            compose.onNodeWithTag("colours-explore-name").assertTextEquals(ColoursContent.definition(id).text.display.en.replaceFirstChar { it.uppercase() })
        }
        compose.onNodeWithTag("colours-explore-replay").performScrollTo().performClick()
        assertEquals(1, replay)
    }

    @Test fun findSupportsRetryHelpReplayAndCorrectAdvanceInGermanAtDoubleText() {
        val generated = ColoursContent.generate(SessionId("ui-find-colours"), RoundLength.FIVE, 19L) as GenerationResult.Generated
        var state by mutableStateOf(SessionReducer.start(generated.plan, ContentLanguage.GERMAN, ColoursContent.repository).state)
        var replay = 0
        var help = 0
        compose.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, 2f)) {
                MaterialTheme {
                    FindColourScreen(state, ContentLanguage.GERMAN, false, false, false,
                        { replay++ }, { help++; state = SessionReducer.reduce(state,
                            SessionAction.Hint(state.task.id)).state },
                        { state = SessionReducer.reduce(state, it).state }, {}, {}, {}, Modifier)
                }
            }
        }
        compose.onNodeWithText(state.task.question.instruction.display.de).assertIsDisplayed()
        compose.onNodeWithTag("colour-find-replay").performScrollTo().performClick()
        compose.onNodeWithTag("colour-find-help").performScrollTo().performClick()
        assertEquals(1, replay); assertEquals(1, help)
        val wrong = state.task.question.choices.first { it != state.task.question.correct }
        compose.onNodeWithTag("colour-find-choice-${wrong.value}").performScrollTo().performClick()
        compose.onNodeWithTag("colour-find-feedback-wrong").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("colour-find-retry").performScrollTo().performClick()
        compose.onNodeWithTag("colour-find-choice-${state.task.question.correct.value}").performScrollTo().performClick()
        compose.onNodeWithTag("colour-find-feedback-correct").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("colour-find-next").performScrollTo().assertIsDisplayed().performClick()
        assertEquals(1, state.index)
    }
}
