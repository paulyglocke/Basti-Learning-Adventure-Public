package com.bellfamily.bastischool.ui.months

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.input.InputMode
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInputModeManager
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.bellfamily.bastischool.R
import com.bellfamily.bastischool.learning.models.ContentLanguage
import com.bellfamily.bastischool.learning.months.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.io.File

@RunWith(Parameterized::class)
class MonthsSeasonWheelScreenTest(private val german: Boolean, private val font: Float, private val landscape: Boolean) {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
    companion object {
        @JvmStatic @Parameterized.Parameters(name="de={0},font={1},landscape={2}")
        fun cases() = listOf(false, true).flatMap { de ->
            listOf(1f, 1.5f, 2f).flatMap { font -> listOf(false, true).map { arrayOf<Any>(de, font, it) } }
        }
    }

    @OptIn(ExperimentalTestApi::class, androidx.compose.ui.ExperimentalComposeUiApi::class)
    @Test fun imageRingControlsFeedbackAndResponsiveSemantics() {
        val art = BitmapFactory.decodeResource(compose.activity.resources, R.drawable.seasons_center)
        assertEquals(1024, art.width)
        assertEquals(1024, art.height)
        assertEquals(0, Color.alpha(art.getPixel(0, 0)))
        assertTrue(Color.alpha(art.getPixel(512, 512)) > 0)
        compose.activity.requestedOrientation = if (landscape) ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            else ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        compose.waitUntil(10_000) { compose.activity.resources.configuration.orientation ==
            if (landscape) Configuration.ORIENTATION_LANDSCAPE else Configuration.ORIENTATION_PORTRAIT }
        val language = if (german) ContentLanguage.GERMAN else ContentLanguage.ENGLISH
        var state by mutableStateOf(MonthsSeasonWheelState(rotation = 1))
        var spoken = 0
        lateinit var requestKeyboard: () -> Boolean
        compose.setContent {
            val density = LocalDensity.current.density
            val input = LocalInputModeManager.current
            requestKeyboard = { input.requestInputMode(InputMode.Keyboard) }
            CompositionLocalProvider(LocalDensity provides Density(density, font)) {
                MaterialTheme {
                    MonthsSeasonWheelScreen(state, language, false, false,
                        { state = state.rotate(it) }, { state = state.copy(mode = it, feedback = MonthsWheelFeedback.READY) },
                        { spoken++ }, { state = state.check() },
                        { state = state.copy(replayCount = state.replayCount + 1) },
                        { state = state.copy(hintUsed = true) }, { state = state.copy(rotation = 0) },
                        { state = MonthsSeasonWheelState(rotation = 2) }, {}, {}, Modifier)
                }
            }
        }
        compose.onNodeWithTag("months-seasons-wheel").performScrollTo().assertExists()
            .assertContentDescriptionContains(if (german) "Monatsrad. Februar am Pfeil. Mit den Tasten nach links oder rechts drehen."
                else "Months wheel. February at the arrow. Use previous or next to rotate.")
        compose.onNodeWithTag("wheel-centre-art").assertExists()
        compose.onNodeWithTag("wheel-ring").performScrollTo()
        val wheelBefore = compose.onNodeWithTag("months-seasons-wheel").fetchSemanticsNode().boundsInRoot
        val centreBefore = compose.onNodeWithTag("wheel-centre-art").fetchSemanticsNode().boundsInRoot
        assertEquals(wheelBefore.center.x, centreBefore.center.x, 1.5f)
        assertEquals(wheelBefore.center.y, centreBefore.center.y, 1.5f)
        if (font >= 1.5f) compose.onNodeWithTag("wheel-name-month.september").performScrollTo()
            .assertTextEquals(if (german) "September" else "September")
        compose.onNodeWithTag("wheel-match-mode").assertIsSelected()
        compose.onNodeWithTag("wheel-ring").performScrollTo().performTouchInput { click(Offset(width * .88f, height * .5f)) }
        compose.runOnIdle { assertEquals(1, spoken) }
        compose.onNodeWithTag("wheel-check").performScrollTo().performClick()
        compose.runOnIdle { assertEquals(MonthsWheelFeedback.RETRY, state.feedback) }
        compose.onNodeWithTag("wheel-ring").performScrollTo()
        compose.onNodeWithTag("months-season-wheel-content").captureToImage().asAndroidBitmap().save(
            File(compose.activity.getExternalFilesDir(null), "months-wheel-wrong-${if (german) "de" else "en"}-$font-$landscape.png"))
        compose.onNodeWithTag("wheel-hint").performScrollTo().performClick()
        compose.onNodeWithTag("wheel-listen").performScrollTo().performClick()
        compose.onNodeWithTag("wheel-previous").performScrollTo().assertHeightIsAtLeast(48.dp).performClick()
        compose.runOnIdle { assertEquals(0, state.rotation) }
        compose.onNodeWithTag("wheel-check").performScrollTo().performClick()
        compose.runOnIdle { assertEquals(MonthsWheelFeedback.COMPLETE, state.feedback) }
        compose.onNodeWithTag("wheel-selected-month").performScrollTo()
            .assertTextEquals(if (german) "Am Pfeil: März" else "At the arrow: March")
        compose.onNodeWithTag("wheel-ring").performScrollTo()
        val wheelAfter = compose.onNodeWithTag("months-seasons-wheel").fetchSemanticsNode().boundsInRoot
        val centreAfter = compose.onNodeWithTag("wheel-centre-art").fetchSemanticsNode().boundsInRoot
        assertEquals("The centre must remain centred within the wheel", wheelAfter.center.x,
            centreAfter.center.x, 1.5f)
        assertEquals("The centre must remain centred within the wheel", wheelAfter.center.y,
            centreAfter.center.y, 1.5f)
        compose.onNodeWithTag("wheel-again").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("wheel-ring").performScrollTo()
        compose.onNodeWithTag("months-season-wheel-content").captureToImage().asAndroidBitmap().save(
            File(compose.activity.getExternalFilesDir(null), "months-wheel-correct-${if (german) "de" else "en"}-$font-$landscape.png"))
        compose.runOnIdle { assertTrue(requestKeyboard()) }
        compose.onNodeWithTag("wheel-again").performSemanticsAction(androidx.compose.ui.semantics.SemanticsActions.RequestFocus) { it() }
        compose.onNodeWithTag("wheel-again").performKeyInput { pressKey(Key.Enter) }
        compose.runOnIdle { assertEquals(2, state.rotation) }
        compose.onNodeWithTag("wheel-explore-mode").performScrollTo().performClick().assertIsSelected()
        compose.onNodeWithTag("wheel-show-alignment").performScrollTo().performClick()
        compose.runOnIdle { assertEquals(0, state.rotation); assertEquals(1, spoken.coerceAtMost(1)) }
    }

    private fun Bitmap.save(file: File) = file.outputStream().use { compress(Bitmap.CompressFormat.PNG, 100, it) }
}
