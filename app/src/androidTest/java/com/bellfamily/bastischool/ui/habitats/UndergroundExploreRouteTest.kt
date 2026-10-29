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
import com.bellfamily.bastischool.learning.animals.UndergroundAnimalContent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test

class UndergroundExploreRouteTest {
    @get:Rule val compose = createEmptyComposeRule()

    @Test fun productionUndergroundRouteInteractionsRestorationAndResponsiveVisualCapture() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        val prefs = context.getSharedPreferences("basti_shell", 0)
        val oldLanguage = prefs.getString("lang", null)
        val oldAudioMode = prefs.getString("audioMode", null)
        val oldSound = prefs.getBoolean("sound", true)
        val oldFontScale = context.resources.configuration.fontScale
        val root = requireNotNull(context.getExternalFilesDir(null))
        val captureDir = File(root, "animals-phase5c4")
        val sharedDir = "/sdcard/Download/animals-phase5c4"
        check(captureDir.mkdirs() || captureDir.isDirectory)
        fun shell(command: String) { instrumentation.uiAutomation.executeShellCommand(command).close() }
        shell("mkdir -p $sharedDir")
        fun capture(name: String, width: Int = 1080, height: Int = 2400) {
            compose.waitForIdle()
            Thread.sleep(800)
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
        fun point(x: Float, y: Float) {
            compose.onNodeWithTag("underground-scene").performTouchInput {
                click(Offset(width * x, height * y))
            }
        }
        fun assertBounds(tag: String, animal: UndergroundAnimalContent) {
            val scene = compose.onNodeWithTag("underground-scene").fetchSemanticsNode().boundsInRoot
            val hotspot = compose.onNodeWithTag(tag).fetchSemanticsNode().boundsInRoot
            assertEquals("$tag left source fraction scene=$scene hotspot=$hotspot", animal.target.left,
                (hotspot.left - scene.left) / scene.width, .025f)
            assertEquals("$tag top source fraction scene=$scene hotspot=$hotspot", animal.target.top,
                (hotspot.top - scene.top) / scene.height, .025f)
            assertEquals("$tag right source fraction scene=$scene hotspot=$hotspot", animal.target.right,
                (hotspot.right - scene.left) / scene.width, .025f)
            assertEquals("$tag bottom source fraction scene=$scene hotspot=$hotspot", animal.target.bottom,
                (hotspot.bottom - scene.top) / scene.height, .025f)
        }
        fun viewport(scenario: ActivityScenario<MainActivity>, size: String, width: Int, height: Int, suffix: String) {
            shell("wm size $size")
            scenario.recreate()
            compose.waitForIdle()
            compose.waitUntil(10_000) {
                compose.onAllNodesWithTag("underground-scene").fetchSemanticsNodes().isNotEmpty()
            }
            compose.onNodeWithTag("underground-title").assertExists()
            compose.onNodeWithTag("underground-scene").assertExists()
            compose.onNodeWithTag("underground-choice-mole").assertExists()
            compose.onNodeWithTag("underground-replay").assertExists()
            capture(suffix, width, height)
        }

        try {
            prefs.edit().putString("lang", "en").putString("audioMode", "off")
                .putBoolean("sound", false).commit()
            ActivityScenario.launch(MainActivity::class.java).use { scenario ->
                var owner: UndergroundExploreViewModel? = null
                scenario.onActivity { owner = ViewModelProvider(it)[UndergroundExploreViewModel::class.java] }
                compose.onNodeWithTag("home-category-ANIMALS").performScrollTo().performClick()
                compose.onNodeWithTag("home-activity-exploreHabitats").performScrollTo().performClick()
                compose.waitUntil(15_000) { var ready = false; scenario.onActivity { ready = owner?.scene != null }; ready }
                assertNotNull("Cutaway background should decode", owner?.scene)
                assertEquals(724, owner?.scene?.width)
                compose.onNodeWithTag("habitat-ocean-choice").assertIsDisplayed()
                compose.onNodeWithTag("habitat-woodland-choice").assertIsDisplayed()
                compose.onNodeWithTag("habitat-underground-choice").assertIsDisplayed()
                capture("01-habitat-chooser-en")

                compose.onNodeWithTag("habitat-underground-choice").performScrollTo().performClick()
                compose.waitUntil(15_000) {
                    var ready = false
                    scenario.onActivity { ready = owner?.animalImages?.size == 3 || owner?.artworkFailed == true }
                    ready
                }
                assertEquals("All three transparent animal layers decode", 3, owner?.animalImages?.size)
                compose.onNodeWithTag("underground-scene-art").assertIsDisplayed()
                compose.onNodeWithTag("underground-fact-text").assertDoesNotExist()
                capture("02-underground-initial-en")

                point(.24f, .77f)
                compose.onNodeWithTag("underground-hotspot-mole").assertIsSelected()
                compose.onNodeWithText("A mole lives mostly underground and digs tunnels.").assertIsDisplayed()
                compose.onNodeWithTag("underground-choice-mole").assertIsSelected()
                assertBounds("underground-hotspot-mole", HabitatExploreContent.undergroundAnimals[0])
                scenario.onActivity { assertEquals(HabitatExploreContent.mole, owner?.selection?.selectedAnimal) }
                capture("03-mole-selected-en")

                point(.545f, .47f)
                compose.onNodeWithTag("underground-hotspot-earthworm").assertIsSelected()
                compose.onNodeWithTag("underground-hotspot-mole").assertIsNotSelected()
                compose.onNodeWithText("Earthworms move through soil.").assertIsDisplayed()
                assertBounds("underground-hotspot-earthworm", HabitatExploreContent.undergroundAnimals[2])
                capture("04-earthworm-selected-en")

                point(.815f, .69f)
                compose.onNodeWithTag("underground-hotspot-rabbit").assertIsSelected()
                compose.onNodeWithText("Wild rabbits can use underground warrens for shelter.").assertIsDisplayed()
                compose.onNodeWithText("Earthworms move through soil.").assertDoesNotExist()
                assertBounds("underground-hotspot-rabbit", HabitatExploreContent.undergroundAnimals[1])
                capture("05-rabbit-selected-en")
                compose.onNodeWithTag("underground-replay").performScrollTo().performClick()

                optionsLanguage(scenario, german = true)
                compose.onNodeWithText("Wildkaninchen können unterirdische Baue als Schutz nutzen.")
                    .performScrollTo().assertIsDisplayed()
                compose.onNodeWithTag("underground-choice-rabbit").assertIsSelected()
                capture("06-rabbit-selected-de-options-return")
                scenario.recreate()
                compose.onNodeWithText("Wildkaninchen können unterirdische Baue als Schutz nutzen.")
                    .performScrollTo().assertIsDisplayed()
                compose.onNodeWithTag("underground-hotspot-rabbit").assertIsSelected()
                capture("07-rabbit-restored-after-recreation-de")

                viewport(scenario, "2400x1080", 2400, 1080, "08-phone-landscape-de")
                viewport(scenario, "1280x720", 1280, 720, "09-short-landscape-de")
                viewport(scenario, "1600x2560", 1600, 2560, "10-tablet-portrait-emulator")
                viewport(scenario, "2560x1600", 2560, 1600, "11-tablet-landscape-emulator")
                shell("wm size reset")

                shell("settings put system font_scale 1.5")
                Thread.sleep(500)
                scenario.recreate()
                scenario.onActivity { assertEquals(1.5f, it.resources.configuration.fontScale) }
                compose.onNodeWithTag("underground-title").performScrollTo().assertIsDisplayed()
                compose.onNodeWithTag("underground-choice-rabbit").performScrollTo().assertIsDisplayed()
                capture("12-german-1-5x")
                shell("settings put system font_scale 2.0")
                Thread.sleep(500)
                scenario.recreate()
                scenario.onActivity { assertEquals(2.0f, it.resources.configuration.fontScale) }
                compose.onNodeWithText("Wildkaninchen können unterirdische Baue als Schutz nutzen.")
                    .performScrollTo().assertIsDisplayed()
                compose.onNodeWithTag("underground-replay").performScrollTo().assertIsDisplayed()
                capture("13-german-2x")
                shell("settings put system font_scale 1.0")
                Thread.sleep(500)
                scenario.recreate()

                compose.onNodeWithTag("underground-habitats-back").performScrollTo().performClick()
                compose.onNodeWithTag("habitat-underground-choice").assertIsDisplayed()
                capture("14-back-to-habitat-chooser-de")
                compose.onNodeWithTag("shell-back").performClick()
                compose.onNodeWithTag("home-activity-exploreHabitats").assertIsDisplayed()
                compose.onNodeWithTag("home-home").performClick()
                compose.onNodeWithTag("home-category-ANIMALS").assertIsDisplayed()
            }
        } finally {
            shell("wm size reset")
            shell("settings put system font_scale $oldFontScale")
            prefs.edit().putString("lang", oldLanguage).putString("audioMode", oldAudioMode)
                .putBoolean("sound", oldSound).commit()
        }
    }
}
