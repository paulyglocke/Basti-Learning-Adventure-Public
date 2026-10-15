package com.bellfamily.bastischool.ui.months

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Bitmap
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
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.text.TextLayoutResult
import com.bellfamily.bastischool.learning.models.ContentId
import com.bellfamily.bastischool.learning.models.ContentLanguage
import com.bellfamily.bastischool.learning.months.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.io.File
import kotlin.math.cos
import kotlin.math.sin

@RunWith(Parameterized::class)
class MonthsWeatherScreenTest(private val german: Boolean, private val font: Float, private val landscape: Boolean) {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
    companion object {
        @JvmStatic @Parameterized.Parameters(name = "de={0},font={1},landscape={2}")
        fun cases() = listOf(false, true).flatMap { de -> listOf(1f, 1.5f, 2f).flatMap { font ->
            listOf(false, true).map { arrayOf<Any>(de, font, it) }
        } }
    }

    @OptIn(ExperimentalTestApi::class, androidx.compose.ui.ExperimentalComposeUiApi::class)
    @Test fun defaultsCyclingSpeechControlsAndChallengeSeparation() {
        compose.activity.requestedOrientation = if (landscape) ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE else ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        compose.waitUntil(10_000) { compose.activity.resources.configuration.orientation ==
            if (landscape) Configuration.ORIENTATION_LANDSCAPE else Configuration.ORIENTATION_PORTRAIT }
        val language = if (german) ContentLanguage.GERMAN else ContentLanguage.ENGLISH
        var state by mutableStateOf(MonthsSeasonWheelState(2, MonthsWheelMode.EXPLORE))
        var speech = 0
        val spokenMonths = mutableListOf<ContentId>()
        lateinit var keyboard: () -> Boolean
        compose.setContent {
            val density = LocalDensity.current.density
            val input = LocalInputModeManager.current
            keyboard = { input.requestInputMode(InputMode.Keyboard) }
            CompositionLocalProvider(LocalDensity provides Density(density, font)) {
                MaterialTheme {
                    MonthsSeasonWheelScreen(state, language, false, false,
                        { state = state.rotate(it) }, { state = state.copy(mode = it, feedback = MonthsWheelFeedback.READY) },
                        { spokenMonths.add(it) }, { state = state.check() }, {}, {}, { state = state.showAlignment() }, {}, {}, {}, Modifier,
                        onChangeWeather = { state = state.changeWeather() }, onWeatherListen = { speech++ },
                        onToggleWeather = { state = state.copy(weatherEnabled = !state.weatherEnabled) })
                }
            }
        }
        for (month in listOf(MonthIds.JANUARY, MonthIds.FEBRUARY, MonthIds.APRIL, MonthIds.JULY, MonthIds.OCTOBER)) {
            compose.runOnIdle {
                val rotation = (0..11).single { MonthsSeasonWheelGeometry.arrowMonth(it) == month }
                state = state.rotate(rotation - state.rotation)
            }
            val definition = MonthContent.month(month)
            compose.onNodeWithTag("wheel-weather-label").performScrollTo()
                .assertTextEquals(MonthsWeatherContent.default(month).phrase.display[language]).assertIsDisplayed()
            compose.onNodeWithTag("wheel-weather-month").performScrollTo().assertTextEquals(
                "${definition.text.display[language]} — ${MonthContent.season(definition.season).text.display[language]}")
            listOf("wheel-weather-month", "wheel-weather-label").forEach(::assertUnclipped)
            compose.onNodeWithTag("wheel-weather-overlay", useUnmergedTree = true).assertExists()
            if ((!german && font == 1f && !landscape) || (german && font == 2f && landscape && month == MonthIds.OCTOBER)) {
                compose.onNodeWithTag("wheel-ring").performScrollTo()
                save("weather-${month.value}-${if (german) "de" else "en"}-$font-$landscape.png")
                compose.onNodeWithTag("wheel-weather-label").performScrollTo()
                save("weather-label-${month.value}-${if (german) "de" else "en"}-$font-$landscape.png")
            }
        }
        // Tapping the selected segment must speak the current month after repeated rotations,
        // not the month captured when the gesture detector was first composed.
        compose.onNodeWithTag("wheel-ring").performScrollTo().performTouchInput {
            val angle = Math.toRadians(MonthsSeasonWheelGeometry.POINTER_ANGLE_DEGREES.toDouble())
            click(center + Offset(cos(angle).toFloat(), sin(angle).toFloat()) * (width * .4f))
        }
        compose.runOnIdle { assertEquals(listOf(state.selectedMonth), spokenMonths) }
        listOf("wheel-weather-change", "wheel-weather-listen", "wheel-weather-toggle").forEach {
            compose.onNodeWithTag(it).performScrollTo().assertHasClickAction().assertHeightIsAtLeast(48.dp)
            assertUnclipped(it)
        }
        compose.onNodeWithTag("wheel-weather-change").performScrollTo().assertHeightIsAtLeast(48.dp)
        val control = compose.onNodeWithTag("wheel-weather-change").fetchSemanticsNode().boundsInRoot
        val root = compose.onRoot().fetchSemanticsNode().boundsInRoot
        assertTrue(control.left >= root.left && control.right <= root.right)
        compose.runOnIdle { assertTrue(keyboard()) }
        compose.onNodeWithTag("wheel-weather-change").performSemanticsAction(androidx.compose.ui.semantics.SemanticsActions.RequestFocus) { it() }
        compose.onNodeWithTag("wheel-weather-change").performKeyInput { pressKey(Key.Enter) }
        compose.onNodeWithTag("wheel-weather-label").performScrollTo().assertTextEquals(MonthsWeather.RAIN.phrase.display[language])
        compose.onNodeWithTag("wheel-weather-listen").performScrollTo().assertHasClickAction().performClick()
        compose.runOnIdle { assertEquals(1, speech) }
        compose.onNodeWithTag("wheel-previous").performScrollTo().performClick() // November resets to its default.
        compose.runOnIdle { assertEquals(MonthIds.NOVEMBER, state.selectedMonth); assertNull(state.weatherOverride) }
        compose.onNodeWithTag("wheel-weather-toggle").performScrollTo().performClick()
        compose.onNodeWithTag("wheel-weather-overlay", useUnmergedTree = true).assertDoesNotExist()
        compose.onNodeWithTag("wheel-weather-label").assertDoesNotExist()
        compose.onNodeWithTag("wheel-weather-toggle").performClick()
        compose.onNodeWithTag("wheel-weather-overlay", useUnmergedTree = true).assertExists()
        compose.onNodeWithTag("wheel-match-mode").performScrollTo().performClick()
        compose.onNodeWithTag("wheel-weather-overlay", useUnmergedTree = true).assertDoesNotExist()
        compose.onNodeWithTag("wheel-weather-month").assertDoesNotExist()
        compose.onNodeWithTag("wheel-weather-change").assertDoesNotExist()
        compose.onNodeWithTag("wheel-check").performScrollTo().performClick()
        compose.runOnIdle { assertEquals(MonthsWheelFeedback.RETRY, state.feedback) }
    }

    private fun assertUnclipped(tag: String) {
        val results = mutableListOf<TextLayoutResult>()
        compose.onNodeWithTag(tag).performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(results) }
        assertTrue("Text layout exists: $tag", results.isNotEmpty())
        assertFalse("No clipped text: $tag", results.any { it.hasVisualOverflow })
    }

    private fun save(name: String) {
        val bitmap = compose.onRoot().captureToImage().asAndroidBitmap()
        File(compose.activity.getExternalFilesDir(null), name).outputStream().use {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
        }
    }
}
