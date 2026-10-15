package com.bellfamily.bastischool.ui.months

import android.graphics.Bitmap
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.test.platform.app.InstrumentationRegistry
import com.bellfamily.bastischool.learning.models.ContentLanguage
import com.bellfamily.bastischool.learning.months.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import java.io.File

class MonthsWeatherMotionTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
    private class Owner : LifecycleOwner {
        override val lifecycle = LifecycleRegistry(this)
    }

    @Test fun movingFramesFadeToLatestMonthAndStopForLifecycleAndSystemReducedMotion() {
        val resolver = compose.activity.contentResolver
        val setting = Settings.Global.ANIMATOR_DURATION_SCALE
        val previousScale = Settings.Global.getString(resolver, setting)
        fun scale(value: String?) {
            val command = if (value == null) "settings delete global $setting" else "settings put global $setting $value"
            InstrumentationRegistry.getInstrumentation().uiAutomation.executeShellCommand(command).use {
                android.os.ParcelFileDescriptor.AutoCloseInputStream(it).readBytes()
            }
        }
        lateinit var owner: Owner
        var state by mutableStateOf(MonthsSeasonWheelState(2, MonthsWheelMode.EXPLORE))
        try {
            scale("1")
            compose.runOnUiThread { owner = Owner(); owner.lifecycle.currentState = Lifecycle.State.RESUMED }
            // Infinite animations are cancelled by the test policy when autoAdvance is
            // enabled. Take clock ownership before composing, not after the first frame.
            compose.mainClock.autoAdvance = false
            compose.setContent {
                CompositionLocalProvider(LocalLifecycleOwner provides owner) {
                    MaterialTheme {
                        MonthsSeasonWheelScreen(state, ContentLanguage.ENGLISH, false, false,
                            { state = state.rotate(it) }, { state = state.copy(mode = it) }, {}, {}, {}, {}, {}, {}, {}, {}, Modifier)
                    }
                }
            }
            compose.mainClock.advanceTimeBy(1000)
            compose.onNodeWithTag("wheel-ring").performScrollTo()
            // Pixel changes establish real drawing movement for every supported weather type.
            MonthsWeather.entries.forEach { weather ->
                val month = MonthIds.canonicalOrder.first { weather in MonthsWeatherContent.choices(it) }
                val rotation = (0..11).single { MonthsSeasonWheelGeometry.arrowMonth(it) == month }
                compose.runOnUiThread { state = state.copy(rotation = rotation, weatherOverride = weather) }
                compose.mainClock.advanceTimeBy(1000)
                val before = frame()
                compose.mainClock.advanceTimeBy(800)
                assertFalse("$weather must visibly move", before.sameAs(frame()))
            }
            // Supersede a fading scene repeatedly; only the final target survives.
            repeat(8) { i ->
                compose.runOnUiThread { state = MonthsSeasonWheelState(i, MonthsWheelMode.EXPLORE) }
                compose.mainClock.advanceTimeBy(64)
            }
            compose.runOnUiThread { state = MonthsSeasonWheelState(1, MonthsWheelMode.EXPLORE) }
            compose.mainClock.advanceTimeBy(1000)
            compose.onAllNodesWithTag("wheel-weather-overlay", useUnmergedTree = true).assertCountEquals(1)
            compose.onNodeWithTag("wheel-weather-label").assertTextEquals("It's frosty!")
            compose.runOnUiThread { owner.lifecycle.currentState = Lifecycle.State.CREATED }
            compose.mainClock.advanceTimeBy(1000)
            val paused = frame()
            compose.mainClock.advanceTimeBy(2000)
            assertTrue("No frames should change in the background", paused.sameAs(frame()))
            compose.runOnUiThread { owner.lifecycle.currentState = Lifecycle.State.RESUMED }
            compose.mainClock.advanceTimeBy(1000)
            val resumed = frame()
            compose.mainClock.advanceTimeBy(1000)
            assertFalse("Animation resumes on return", resumed.sameAs(frame()))
            scale("0")
            compose.waitUntil(5000) { Settings.Global.getFloat(resolver, setting) == 0f }
            compose.mainClock.advanceTimeBy(1000)
            compose.waitForIdle()
            val still = frame()
            compose.mainClock.advanceTimeBy(2000)
            assertTrue("System reduced motion must be static", still.sameAs(frame()))
            compose.onRoot().captureToImage().asAndroidBitmap().save(File(compose.activity.getExternalFilesDir(null), "weather-static-february.png"))
            compose.runOnUiThread { state = state.copy(mode = MonthsWheelMode.MATCH) }
            compose.mainClock.advanceTimeBy(1000)
            compose.onNodeWithTag("wheel-weather-overlay", useUnmergedTree = true).assertDoesNotExist()
        } finally {
            compose.mainClock.autoAdvance = true
            scale(previousScale)
        }
    }

    private fun frame() = compose.onNodeWithTag("wheel-weather-overlay", useUnmergedTree = true)
        .captureToImage().asAndroidBitmap()
    private fun Bitmap.save(file: File) = file.outputStream().use { compress(Bitmap.CompressFormat.PNG, 100, it) }
}
