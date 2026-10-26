package com.bellfamily.bastischool.ui.colours

import android.content.res.Configuration
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import com.bellfamily.bastischool.learning.colours.ColoursContent
import com.bellfamily.bastischool.learning.colours.RainbowOrderContent
import com.bellfamily.bastischool.learning.colours.RainbowOrderAction
import com.bellfamily.bastischool.learning.colours.RainbowOrderLevel
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

    @Test fun rainbowExploreSelectsEveryBandAndReplayUsesSelectedBand() {
        var spoken = -1
        compose.setContent { MaterialTheme {
            RainbowExploreScreen(ContentLanguage.GERMAN, { spoken = it.toInt() }, {}, Modifier)
        } }
        for (index in 0..6) {
            compose.onNodeWithTag("rainbow-band-$index").performScrollTo().assertIsDisplayed().performClick()
            compose.onNodeWithTag("rainbow-selected-name").assertTextEquals(
                listOf("Rot", "Orange", "Gelb", "Grün", "Blau", "Indigo", "Violett")[index])
            compose.runOnIdle { assertEquals(index, spoken) }
        }
        compose.onNodeWithTag("rainbow-replay").performScrollTo().performClick()
        compose.runOnIdle { assertEquals(6, spoken) }
    }

    @Test fun rainbowGraphicRendersSevenBandsAndVisibleHitRegionsMatchThem() {
        var spoken = -1
        compose.setContent { MaterialTheme {
            RainbowExploreScreen(ContentLanguage.ENGLISH, { spoken = it.toInt() }, {}, Modifier)
        } }
        val graphic = compose.onNodeWithTag("rainbow-graphic").assertExists()
        val graphicSize = graphic.fetchSemanticsNode().size
        assertTrue("Rainbow canvas width must be positive; actual size=$graphicSize", graphicSize.width > 0)
        assertTrue("Rainbow canvas height must be positive; actual size=$graphicSize", graphicSize.height > 0)
        val image = graphic.captureToImage()
        val pixels = image.toPixelMap()
        val size = graphicSize
        val centerX = pixels.width / 2f
        val centerY = pixels.height * .92f
        val outerRadius = min(pixels.width * .48f, pixels.height * .8f)
        val bandWidth = outerRadius / 7f
        val palette = listOf(0xFFC62828, 0xFFEF6C00, 0xFFFFEB3B, 0xFF2E7D32,
            0xFF1565C0, 0xFF3949AB, 0xFF7B1FA2).map { Color(it) }

        palette.forEachIndexed { index, target ->
            val pixelCount = (0 until pixels.height).sumOf { y ->
                (0 until pixels.width).count { x ->
                    val pixel = pixels[x, y]
                    abs(pixel.red - target.red) < .05f && abs(pixel.green - target.green) < .05f &&
                        abs(pixel.blue - target.blue) < .05f
                }
            }
            assertTrue("Rainbow band $index must have visible pixels; count=$pixelCount image=${pixels.width}x${pixels.height} size=$size",
                pixelCount > 100)
        }

        // Test close to each band's outer edge, still inside its visible stroke.
        // This catches touch rings offset by one full band from their artwork.
        palette.indices.forEach { index ->
            val inward = index * bandWidth - .30f * bandWidth
            val radius = outerRadius - inward
            graphic.performTouchInput { click(Offset(centerX, centerY - radius)) }
            compose.runOnIdle { assertEquals("Visible band $index must select its own colour", index, spoken) }

            // Check both curved shoulders where each visible arc approaches its ends.
            for (angle in listOf(Math.PI / 6.0, 5.0 * Math.PI / 6.0)) {
                val point = Offset(centerX + (radius * cos(angle)).toFloat(),
                    centerY - (radius * sin(angle)).toFloat())
                graphic.performTouchInput { click(point) }
                compose.runOnIdle { assertEquals("Curved shoulder of visible band $index", index, spoken) }
            }
        }

        // The clear space just inside Violet and outside Red must not select a band.
        spoken = -1
        val outside = outerRadius + .55f * bandWidth
        graphic.performTouchInput { click(Offset(centerX, centerY - outside)) }
        compose.runOnIdle { assertEquals("Outside the visible rainbow", -1, spoken) }
        spoken = -1
        val insideHole = outerRadius - 6.55f * bandWidth
        graphic.performTouchInput { click(Offset(centerX, centerY - insideHole)) }
        compose.runOnIdle { assertEquals("Inside the rainbow opening", -1, spoken) }
    }

    @Test fun compactRainbowOrderShowsTheWholeInstructionBeforeScrolling() {
        val initial = RainbowOrderContent.start(SessionId("compact-order-ui"), RainbowOrderLevel.FULL,
            31L, ContentLanguage.ENGLISH)
        compose.setContent {
            val compact = Configuration(LocalConfiguration.current).apply {
                screenWidthDp = 411
                screenHeightDp = 274
            }
            CompositionLocalProvider(LocalConfiguration provides compact) {
                MaterialTheme {
                    Box(Modifier.fillMaxWidth().height(178.dp).testTag("compact-order-viewport")) {
                        RainbowOrderSessionScreen(initial, RainbowOrderLevel.FULL, ContentLanguage.ENGLISH,
                            false, false, {}, {}, {}, {}, {}, {}, {}, Modifier.fillMaxSize())
                    }
                }
            }
        }
        val viewport = compose.onNodeWithTag("compact-order-viewport").fetchSemanticsNode().boundsInRoot
        val prompt = compose.onNodeWithTag("rainbow-order-prompt").assertIsDisplayed().fetchSemanticsNode().boundsInRoot
        assertTrue("Task instruction must fit in the first short-landscape viewport: viewport=$viewport prompt=$prompt",
            prompt.top >= viewport.top && prompt.bottom <= viewport.bottom)
    }

    @Test fun rainbowOrderDestinationSemanticsDescribeEmptyAndPlacedColours() {
        val initial = RainbowOrderContent.start(SessionId("order-slot-a11y"), RainbowOrderLevel.STARTER,
            32L, ContentLanguage.GERMAN)
        var state by mutableStateOf(initial)
        compose.setContent {
            MaterialTheme {
                RainbowOrderSessionScreen(state, RainbowOrderLevel.STARTER, ContentLanguage.GERMAN,
                    false, false, {}, { action ->
                        if (action is RainbowOrderAction.Place) state = RainbowOrderContent.reduce(state, action).state
                    }, {}, {}, {}, {}, {}, Modifier)
            }
        }
        compose.onNodeWithTag("rainbow-order-slot-0").assertContentDescriptionEquals("Platz 1, leer")
        compose.runOnIdle {
            state = RainbowOrderContent.reduce(state,
                RainbowOrderAction.Place(state.nextAttempt!!, state.target.first())).state
        }
        compose.onNodeWithTag("rainbow-order-slot-0").assertContentDescriptionEquals("Platz 1: Rot, eingesetzt")
    }

    @Test fun rainbowOrderStarterAndFullChallengesBuildTheCanonicalOrder() {
        var replay = 0
        var help = 0
        compose.setContent { MaterialTheme {
            RainbowOrderScreen(ContentLanguage.ENGLISH, { replay++ }, { help++ }, {}, Modifier)
        } }
        compose.onNodeWithTag("rainbow-order-model").assertIsDisplayed()
        compose.onNodeWithTag("rainbow-order-choice-0").performClick()
        compose.onNodeWithTag("rainbow-order-choice-2").performClick()
        compose.onNodeWithTag("rainbow-order-choice-4").performClick()
        compose.onNodeWithTag("rainbow-order-choice-6").performClick()
        compose.onNodeWithTag("rainbow-order-feedback").assertTextEquals("That is the rainbow order!")
        compose.onNodeWithTag("rainbow-order-replay").performScrollTo().performClick()
        compose.onNodeWithTag("rainbow-order-help").performScrollTo().performClick()
        compose.onNodeWithText("All seven").performScrollTo().performClick()
        compose.onNodeWithTag("rainbow-order-model").assertDoesNotExist()
        compose.onNodeWithTag("rainbow-order-choice-0").performClick()
        compose.onNodeWithTag("rainbow-order-choice-1").performClick()
        compose.onNodeWithTag("rainbow-order-choice-2").performClick()
        compose.onNodeWithTag("rainbow-order-choice-3").performClick()
        compose.onNodeWithTag("rainbow-order-choice-4").performClick()
        compose.onNodeWithTag("rainbow-order-choice-5").performClick()
        compose.onNodeWithTag("rainbow-order-choice-6").performClick()
        compose.onNodeWithTag("rainbow-order-feedback").assertTextEquals("That is the rainbow order!")
        compose.runOnIdle { assertEquals(1, replay); assertEquals(1, help) }
    }
}
