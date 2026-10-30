package com.bellfamily.bastischool.ui.habitats

import android.graphics.Bitmap
import java.io.File
import java.io.FileOutputStream
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.lifecycle.ViewModelProvider
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import com.bellfamily.bastischool.MainActivity
import com.bellfamily.bastischool.learning.animals.HabitatExploreContent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test

class FreshwaterExploreRouteTest {
    @get:Rule val compose = createEmptyComposeRule()

    @Test fun productionFreshwaterRouteSelectionRestorationAndResponsiveCapture() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        val prefs = context.getSharedPreferences("basti_shell", 0)
        val oldLanguage = prefs.getString("lang", null)
        val oldAudioMode = prefs.getString("audioMode", null)
        val oldSound = prefs.getBoolean("sound", true)
        val oldFontScale = context.resources.configuration.fontScale
        val root = requireNotNull(context.getExternalFilesDir(null))
        val captureDir = File(root, "animals-phase5c5")
        val sharedDir = "/sdcard/Download/animals-phase5c5"
        check(captureDir.mkdirs() || captureDir.isDirectory)
        fun shell(command: String) { instrumentation.uiAutomation.executeShellCommand(command).close() }
        shell("mkdir -p $sharedDir")
        fun capture(name: String, width: Int = 1080, height: Int = 2400) {
            compose.waitForIdle()
            Thread.sleep(400)
            val bitmap = instrumentation.uiAutomation.takeScreenshot()
            assertEquals("$name width", width, bitmap.width)
            assertEquals("$name height", height, bitmap.height)
            val file = File(captureDir, "$name.png")
            try { FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) } }
            finally { bitmap.recycle() }
            shell("cp ${file.absolutePath} $sharedDir/$name.png")
        }
        fun optionsLanguage(scenario: ActivityScenario<MainActivity>, german: Boolean) {
            compose.onNodeWithTag("shell-options").performScrollTo().performClick()
            compose.onNodeWithText(if (german) "🇩🇪 Deutsch" else "🇬🇧 English")
                .performScrollTo().performClick()
            scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
            compose.waitForIdle()
        }
        fun tapAnimal(tag: String, x: Float, y: Float) {
            compose.onNodeWithTag("freshwater-scene").performTouchInput {
                click(Offset(width * x, height * y))
            }
            compose.onNodeWithTag("freshwater-hotspot-$tag").assertIsSelected()
            val content = HabitatExploreContent.freshwaterAnimals.first { it.id.value.endsWith(".$tag") }
            val scene = compose.onNodeWithTag("freshwater-scene").fetchSemanticsNode().boundsInRoot
            val hotspot = compose.onNodeWithTag("freshwater-hotspot-$tag").fetchSemanticsNode().boundsInRoot
            assertEquals(content.target.left, (hotspot.left - scene.left) / scene.width, .025f)
            assertEquals(content.target.top, (hotspot.top - scene.top) / scene.height, .025f)
            assertEquals(content.target.right, (hotspot.right - scene.left) / scene.width, .025f)
            assertEquals(content.target.bottom, (hotspot.bottom - scene.top) / scene.height, .025f)
        }
        fun viewport(scenario: ActivityScenario<MainActivity>, size: String, width: Int, height: Int, tag: String) {
            shell("wm size $size")
            scenario.recreate()
            compose.waitUntil(15_000) {
                compose.onAllNodesWithTag("freshwater-scene").fetchSemanticsNodes().isNotEmpty()
            }
            compose.onNodeWithTag("freshwater-title").assertExists()
            compose.onNodeWithTag("freshwater-scene").assertExists()
            compose.onNodeWithTag("freshwater-choice-frog").assertExists()
            compose.onNodeWithTag("freshwater-choice-duck").assertExists()
            capture(tag, width, height)
        }

        try {
            prefs.edit().putString("lang", "en").putString("audioMode", "off")
                .putBoolean("sound", false).commit()
            shell("wm size 1080x2400")
            shell("settings put system font_scale 1.0")
            ActivityScenario.launch(MainActivity::class.java).use { scenario ->
                var owner: FreshwaterExploreViewModel? = null
                scenario.onActivity { owner = ViewModelProvider(it)[FreshwaterExploreViewModel::class.java] }
                compose.onNodeWithTag("home-category-ANIMALS").performScrollTo().performClick()
                compose.onNodeWithTag("home-activity-exploreHabitats").performScrollTo().performClick()
                compose.waitUntil(15_000) { var ready = false; scenario.onActivity { ready = owner?.scene != null }; ready }
                assertNotNull("Freshwater background decodes", owner?.scene)
                assertEquals(724, owner?.scene?.width)
                assertEquals(543, owner?.scene?.height)
                compose.onNodeWithTag("habitat-ocean-choice").assertIsDisplayed()
                compose.onNodeWithTag("habitat-woodland-choice").assertIsDisplayed()
                compose.onNodeWithTag("habitat-underground-choice").assertIsDisplayed()
                compose.onNodeWithTag("habitat-freshwater-choice").assertExists()
                capture("01-chooser-en")

                compose.onNodeWithTag("habitat-freshwater-choice").performScrollTo().performClick()
                compose.waitUntil(15_000) {
                    var ready = false
                    scenario.onActivity { ready = owner?.animalImages?.size == 2 || owner?.artworkFailed == true }
                    ready
                }
                assertEquals("Both animal cutouts decode", 2, owner?.animalImages?.size)
                assertEquals(null, owner?.selection?.selectedAnimal)
                compose.onNodeWithTag("freshwater-fact-text").assertDoesNotExist()
                compose.onNodeWithTag("freshwater-scene-art").assertIsDisplayed()
                capture("02-freshwater-initial-en")

                optionsLanguage(scenario, german = true)
                compose.onNodeWithTag("freshwater-title").assertTextContains("Süßwasser")
                capture("03-freshwater-initial-de")
                optionsLanguage(scenario, german = false)

                // Empty water must not map to an unrelated animal. The initial state is still null.
                compose.onNodeWithTag("freshwater-scene").performTouchInput {
                    click(Offset(width * .48f, height * .82f))
                }
                scenario.onActivity { assertEquals(null, owner?.selection?.selectedAnimal) }

                tapAnimal("frog", .20f, .59f)
                compose.onNodeWithText("Many frogs lay eggs in water and also use damp places on land.")
                    .performScrollTo().assertIsDisplayed()
                assertEquals(HabitatExploreContent.frog, owner?.selection?.selectedAnimal)
                capture("04-frog-selected-en")

                optionsLanguage(scenario, german = true)
                compose.onNodeWithText("Viele Frösche legen ihre Eier im Wasser ab und nutzen auch feuchte Plätze an Land.")
                    .performScrollTo().assertIsDisplayed()
                compose.onNodeWithTag("freshwater-choice-frog").assertIsSelected()
                capture("05-frog-selected-de")
                optionsLanguage(scenario, german = false)

                tapAnimal("duck", .76f, .54f)
                compose.onNodeWithText("Many ducks swim on ponds and lakes.").performScrollTo().assertIsDisplayed()
                compose.onNodeWithText("Many frogs lay eggs in water and also use damp places on land.")
                    .assertDoesNotExist()
                compose.onNodeWithTag("freshwater-hotspot-frog").assertIsNotSelected()
                compose.onNodeWithTag("freshwater-replay").performScrollTo().assertIsDisplayed().performClick()
                scenario.onActivity { assertEquals(HabitatExploreContent.duck, owner?.selection?.selectedAnimal) }
                capture("06-duck-selected-en")

                optionsLanguage(scenario, german = true)
                compose.onNodeWithText("Viele Enten schwimmen auf Teichen und Seen.").performScrollTo().assertIsDisplayed()
                capture("07-duck-selected-de")
                optionsLanguage(scenario, german = false)

                tapAnimal("frog", .20f, .59f)
                tapAnimal("duck", .76f, .54f)
                compose.onNodeWithText("Many ducks swim on ponds and lakes.").performScrollTo().assertIsDisplayed()
                capture("08-frog-to-duck-switch")
                scenario.recreate()
                compose.waitForIdle()
                compose.onNodeWithTag("freshwater-hotspot-duck").assertIsSelected()
                compose.onNodeWithText("Many ducks swim on ponds and lakes.").performScrollTo().assertIsDisplayed()
                capture("09-selection-restored-recreation")

                optionsLanguage(scenario, german = true)
                compose.onNodeWithTag("freshwater-choice-duck").assertIsSelected()
                compose.onNodeWithText("Viele Enten schwimmen auf Teichen und Seen.").performScrollTo().assertIsDisplayed()
                capture("10-options-language-return-de")

                shell("settings put system font_scale 1.5")
                scenario.recreate()
                compose.waitForIdle()
                compose.onNodeWithTag("freshwater-prompt").assertExists()
                compose.onNodeWithTag("freshwater-replay").performScrollTo().assertIsDisplayed()
                capture("16-german-1_5x")
                shell("settings put system font_scale 2.0")
                scenario.recreate()
                compose.waitForIdle()
                compose.onNodeWithText("Viele Enten schwimmen auf Teichen und Seen.").performScrollTo().assertIsDisplayed()
                compose.onNodeWithTag("freshwater-replay").performScrollTo().assertIsDisplayed()
                capture("17-german-2x")

                shell("settings put system font_scale 1.0")
                scenario.recreate()
                viewport(scenario, "2400x1080", 2400, 1080, "12-phone-landscape")
                compose.onNodeWithTag("freshwater-replay").performScrollTo().assertIsDisplayed()
                viewport(scenario, "1280x720", 1280, 720, "13-short-landscape")
                compose.onNodeWithTag("freshwater-prompt").assertExists()
                compose.onNodeWithTag("freshwater-replay").performScrollTo().assertIsDisplayed()
                viewport(scenario, "1600x2560", 1600, 2560, "14-tablet-portrait-emulator")
                viewport(scenario, "2560x1600", 2560, 1600, "15-tablet-landscape-emulator")

                shell("wm size 1080x2400")
                scenario.recreate()
                compose.waitForIdle()
                compose.onNodeWithTag("freshwater-habitats-back").performScrollTo().performClick()
                compose.onNodeWithTag("habitat-freshwater-choice").assertExists()
                compose.onNodeWithTag("habitat-freshwater-choice").performScrollTo().assertIsDisplayed()
                capture("18-back-to-chooser")
                compose.onNodeWithTag("habitats-animals-back").performScrollTo().performClick()
                compose.onNodeWithTag("home-activity-exploreHabitats").performScrollTo().assertIsDisplayed()
            }
        } finally {
            shell("wm size 1080x2400")
            shell("settings put system font_scale $oldFontScale")
            val edit = prefs.edit().putBoolean("sound", oldSound)
            if (oldLanguage == null) edit.remove("lang") else edit.putString("lang", oldLanguage)
            if (oldAudioMode == null) edit.remove("audioMode") else edit.putString("audioMode", oldAudioMode)
            edit.commit()
        }
    }
}
