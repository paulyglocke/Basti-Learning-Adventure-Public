package com.bellfamily.bastischool.ui.habitats

import android.graphics.Bitmap
import java.io.File
import java.io.FileOutputStream
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.*
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import androidx.lifecycle.ViewModelProvider
import com.bellfamily.bastischool.MainActivity
import com.bellfamily.bastischool.learning.animals.HabitatExploreContent
import com.bellfamily.bastischool.ui.habitats.OceanExploreViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test

class OceanExploreRouteTest {
    @get:Rule val compose = createEmptyComposeRule()

    @Test fun productionRouteSelectionBilingualOptionsRestorationAndResponsiveLayouts() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        val prefs = context.getSharedPreferences("basti_shell", 0)
        val oldLanguage = prefs.getString("lang", null)
        val oldAudioMode = prefs.getString("audioMode", null)
        val oldSound = prefs.getBoolean("sound", true)
        val externalFiles = requireNotNull(context.getExternalFilesDir(null))
        val captureDir = File(externalFiles, "animals-phase5c2")
        val sharedCaptureDir = "/sdcard/Download/animals-phase5c2"
        check(captureDir.mkdirs() || captureDir.isDirectory)
        fun display(command: String) { instrumentation.uiAutomation.executeShellCommand(command).close() }
        display("mkdir -p $sharedCaptureDir")
        fun capture(name: String) {
            compose.waitForIdle()
            // wm size/font changes and Activity recreation can leave a short system/Compose
            // transition after the semantics tree is idle. Capture the settled rendered frame.
            Thread.sleep(1_200)
            val bitmap = instrumentation.uiAutomation.takeScreenshot()
            val file = File(captureDir, "$name.png")
            try { FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) } }
            finally { bitmap.recycle() }
            display("cp ${file.absolutePath} $sharedCaptureDir/$name.png")
        }
        fun reopenAtViewport(scenario: ActivityScenario<MainActivity>, size: String) {
            display("wm size $size")
            scenario.recreate()
            compose.waitForIdle()
            compose.onNodeWithTag("ocean-habitats-back").performScrollTo().performClick()
            compose.onNodeWithTag("habitat-ocean-choice").performScrollTo().performClick()
            compose.onNodeWithTag("ocean-title").performScrollTo()
            compose.onNodeWithTag("ocean-title").assertIsDisplayed()
            compose.onNodeWithTag("ocean-prompt").assertIsDisplayed()
            compose.onNodeWithTag("ocean-scene").assertIsDisplayed()
        }
        try {
            prefs.edit().putString("lang", "en").putString("audioMode", "off").putBoolean("sound", false).commit()
            ActivityScenario.launch(MainActivity::class.java).use { scenario ->
                var oceanOwner: OceanExploreViewModel? = null
                scenario.onActivity { oceanOwner = ViewModelProvider(it)[OceanExploreViewModel::class.java] }
                compose.onNodeWithTag("home-category-ANIMALS").performScrollTo().performClick()
                capture("01-animals-category-en")
                compose.onNodeWithTag("home-activity-exploreHabitats").performScrollTo().performClick()
                compose.waitUntil(10_000) {
                    var loaded = false
                    scenario.onActivity { loaded = oceanOwner?.image != null || oceanOwner?.artworkFailed == true }
                    loaded
                }
                assertNotNull("Approved Ocean scene should decode", oceanOwner?.image)
                compose.onNodeWithTag("habitat-ocean-choice").assertIsDisplayed()
                capture("02-habitat-chooser-en")
                compose.onNodeWithTag("habitat-ocean-choice").performScrollTo().performClick()
                compose.onNodeWithTag("ocean-scene-art").assertIsDisplayed()
                compose.onNodeWithTag("ocean-dolphin-fact").assertDoesNotExist()
                compose.onNodeWithTag("ocean-dolphin-hotspot").assertIsNotSelected()
                capture("03-ocean-initial-en")
                compose.onNodeWithTag("ocean-dolphin-hotspot").performClick()
                compose.onNodeWithText("Dolphins live in the sea.").assertIsDisplayed()
                compose.onNodeWithTag("ocean-dolphin-hotspot").assertIsSelected()
                compose.onNodeWithTag("ocean-replay").assertIsDisplayed()
                capture("04-dolphin-selected-en")

                scenario.recreate()
                compose.onNodeWithText("Dolphins live in the sea.").assertIsDisplayed()
                compose.onNodeWithTag("ocean-dolphin-hotspot").assertIsSelected()
                capture("05-selection-restored-after-activity-recreation")

                compose.onNodeWithTag("shell-options").performClick()
                compose.onNodeWithText("🇩🇪 Deutsch").performClick()
                compose.onNodeWithText("Ton aus").performClick()
                scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
                compose.onNodeWithText("Delfine leben im Meer.").assertIsDisplayed()
                compose.onNodeWithTag("ocean-dolphin-hotspot").assertIsSelected()
                compose.onNodeWithTag("ocean-replay").assertIsDisplayed()
                capture("06-options-return-selected-de")
                assertEquals(HabitatExploreContent.dolphin.value, "animal.dolphin")

                display("settings put system font_scale 2.0")
                Thread.sleep(500)
                scenario.recreate()
                compose.waitUntil(5_000) {
                    var currentScale = 1.0f
                    scenario.onActivity { currentScale = it.resources.configuration.fontScale }
                    currentScale == 2.0f
                }
                scenario.onActivity { assertEquals(2.0f, it.resources.configuration.fontScale) }
                compose.onNodeWithText("Delfine leben im Meer.").performScrollTo().assertIsDisplayed()
                capture("07-dolphin-selected-german-2x")
                display("settings put system font_scale 1.5")
                Thread.sleep(500)
                scenario.recreate()
                scenario.onActivity { assertEquals(1.5f, it.resources.configuration.fontScale) }
                compose.onNodeWithText("Delfine leben im Meer.").performScrollTo().assertIsDisplayed()
                capture("13-dolphin-selected-german-1-5x")
                display("settings put system font_scale 1.0")
                Thread.sleep(500)

                scenario.recreate()
                scenario.onActivity { assertEquals(1.0f, it.resources.configuration.fontScale) }
                reopenAtViewport(scenario, "2400x1080")
                capture("08-phone-landscape")
                compose.onNodeWithTag("ocean-dolphin-choice").performScrollTo().assertIsDisplayed()

                reopenAtViewport(scenario, "1280x720")
                capture("09-short-landscape")
                compose.onNodeWithTag("ocean-replay").performScrollTo().assertIsDisplayed()

                reopenAtViewport(scenario, "1600x2560")
                capture("10-tablet-portrait-emulator-viewport")

                reopenAtViewport(scenario, "2560x1600")
                compose.onNodeWithTag("ocean-dolphin-hotspot").assertIsSelected()
                capture("11-tablet-landscape-emulator-viewport")
                display("wm size reset")

                compose.onNodeWithTag("ocean-habitats-back").performScrollTo().performClick()
                compose.onNodeWithTag("habitat-ocean-choice").assertIsDisplayed()
                compose.onNodeWithTag("shell-back").performClick()
                compose.onNodeWithTag("home-activity-exploreHabitats").assertIsDisplayed()
                capture("12-back-to-animals-category-de")
                compose.onNodeWithTag("home-home").performClick()
                compose.onNodeWithText("Was wollen wir entdecken?").assertIsDisplayed()
            }
        } finally {
            display("wm size reset")
            display("settings put system font_scale 1.0")
            prefs.edit().putString("lang", oldLanguage).putString("audioMode", oldAudioMode)
                .putBoolean("sound", oldSound).commit()
        }
    }
}
