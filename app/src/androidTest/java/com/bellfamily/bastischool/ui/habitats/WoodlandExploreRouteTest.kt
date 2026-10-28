package com.bellfamily.bastischool.ui.habitats

import android.graphics.Bitmap
import java.io.File
import java.io.FileOutputStream
import androidx.compose.ui.geometry.Offset
import kotlin.math.abs
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

class WoodlandExploreRouteTest {
    @get:Rule val compose = createEmptyComposeRule()

    @Test fun productionWoodlandRouteSelectionBilingualOptionsRestorationAndResponsiveLayouts() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        val prefs = context.getSharedPreferences("basti_shell", 0)
        val oldLanguage = prefs.getString("lang", null)
        val oldAudioMode = prefs.getString("audioMode", null)
        val oldSound = prefs.getBoolean("sound", true)
        val oldFontScale = context.resources.configuration.fontScale
        val externalFiles = requireNotNull(context.getExternalFilesDir(null))
        val captureDir = File(externalFiles, "animals-phase5c3")
        val sharedCaptureDir = "/sdcard/Download/animals-phase5c3"
        check(captureDir.mkdirs() || captureDir.isDirectory)
        fun shell(command: String) { instrumentation.uiAutomation.executeShellCommand(command).close() }
        shell("mkdir -p $sharedCaptureDir")
        fun capture(name: String, expectedWidth: Int = 1080, expectedHeight: Int = 2400) {
            compose.waitForIdle()
            Thread.sleep(1_000)
            val bitmap = instrumentation.uiAutomation.takeScreenshot()
            assertEquals("$name screenshot width", expectedWidth, bitmap.width)
            assertEquals("$name screenshot height", expectedHeight, bitmap.height)
            val file = File(captureDir, "$name.png")
            try { FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) } }
            finally { bitmap.recycle() }
            shell("cp ${file.absolutePath} $sharedCaptureDir/$name.png")
        }
        fun languageInOptions(scenario: ActivityScenario<MainActivity>, german: Boolean) {
            compose.onNodeWithTag("shell-options").performScrollTo().performClick()
            compose.onNodeWithText(if (german) "🇩🇪 Deutsch" else "🇬🇧 English").performScrollTo().performClick()
            scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
            compose.waitForIdle()
        }
        fun selectByScenePoint(fractionX: Float, fractionY: Float) {
            compose.onNodeWithTag("woodland-scene").performScrollTo().performTouchInput {
                click(Offset(width * fractionX, height * fractionY))
            }
        }
        fun assertHotspotMatchesScene(animalTag: String, target: com.bellfamily.bastischool.learning.animals.NormalizedSceneTarget) {
            val scene = compose.onNodeWithTag("woodland-scene").fetchSemanticsNode().boundsInRoot
            val hotspot = compose.onNodeWithTag(animalTag).fetchSemanticsNode().boundsInRoot
            assertEquals("$animalTag left image fraction", target.left, (hotspot.left - scene.left) / scene.width, .025f)
            assertEquals("$animalTag top image fraction", target.top, (hotspot.top - scene.top) / scene.height, .025f)
            assertEquals("$animalTag right image fraction", target.right, (hotspot.right - scene.left) / scene.width, .025f)
            assertEquals("$animalTag bottom image fraction", target.bottom, (hotspot.bottom - scene.top) / scene.height, .025f)
        }
        fun changeViewport(scenario: ActivityScenario<MainActivity>, size: String) {
            shell("wm size $size")
            scenario.recreate()
            compose.waitForIdle()
            compose.onNodeWithTag("woodland-title").performScrollTo().assertIsDisplayed()
            compose.onNodeWithTag("woodland-scene").assertIsDisplayed()
        }

        try {
            prefs.edit().putString("lang", "en").putString("audioMode", "off").putBoolean("sound", false).commit()
            ActivityScenario.launch(MainActivity::class.java).use { scenario ->
                var owner: WoodlandExploreViewModel? = null
                scenario.onActivity { owner = ViewModelProvider(it)[WoodlandExploreViewModel::class.java] }
                compose.onNodeWithTag("home-category-ANIMALS").performScrollTo().performClick()
                compose.onNodeWithTag("home-activity-exploreHabitats").performScrollTo().performClick()
                compose.waitUntil(15_000) {
                    var loaded = false
                    scenario.onActivity { loaded = owner?.image != null || owner?.artworkFailed == true }
                    loaded
                }
                assertNotNull("Approved Woodland scene should decode", owner?.image)
                assertEquals(724, owner?.image?.width)
                compose.onNodeWithTag("habitat-ocean-choice").assertIsDisplayed()
                compose.onNodeWithTag("habitat-woodland-choice").assertIsDisplayed()
                capture("01-habitat-chooser-en")

                compose.onNodeWithTag("habitat-woodland-choice").performScrollTo().performClick()
                compose.onNodeWithTag("woodland-scene-art").assertIsDisplayed()
                compose.onNodeWithTag("woodland-fact-text").assertDoesNotExist()
                selectByScenePoint(.15f, .4f)
                scenario.onActivity { assertEquals(null, owner?.selection?.selectedAnimal) }
                capture("02-woodland-initial-en")

                languageInOptions(scenario, german = true)
                compose.onNodeWithText("Wald", useUnmergedTree = true).assertIsDisplayed()
                compose.onNodeWithText("Tippe auf den Fuchs oder das Kaninchen oder wähle unten den Namen eines Tieres aus.")
                    .assertIsDisplayed()
                capture("03-woodland-initial-de-options-return")

                selectByScenePoint(.45f, .67f)
                compose.onNodeWithText("Wildkaninchen können unterirdische Baue als Schutz nutzen.").assertIsDisplayed()
                compose.onNodeWithTag("woodland-hotspot-rabbit").assertIsSelected()
                compose.onNodeWithTag("woodland-choice-rabbit").assertIsSelected()
                assertHotspotMatchesScene("woodland-hotspot-rabbit", HabitatExploreContent.woodlandAnimal(HabitatExploreContent.rabbit)!!.target)
                scenario.onActivity { assertEquals(HabitatExploreContent.rabbit, owner?.selection?.selectedAnimal) }
                capture("04-rabbit-selected-de")

                selectByScenePoint(.80f, .67f)
                compose.onNodeWithText("Füchse leben in vielen Lebensräumen. Ein Fuchs kann einen unterirdischen Bau nutzen, um seine Jungen aufzuziehen.")
                    .assertIsDisplayed()
                compose.onNodeWithText("Wildkaninchen können unterirdische Baue als Schutz nutzen.").assertDoesNotExist()
                compose.onNodeWithTag("woodland-hotspot-fox").assertIsSelected()
                compose.onNodeWithTag("woodland-hotspot-rabbit").assertIsNotSelected()
                assertHotspotMatchesScene("woodland-hotspot-fox", HabitatExploreContent.woodlandAnimal(HabitatExploreContent.fox)!!.target)
                scenario.onActivity { assertEquals(HabitatExploreContent.fox, owner?.selection?.selectedAnimal) }
                capture("05-fox-selected-de-switch-from-rabbit")
                compose.onNodeWithTag("woodland-replay").performClick()

                languageInOptions(scenario, german = false)
                compose.onNodeWithText("Foxes live in many habitats. A fox may use an underground den to raise its cubs.")
                    .assertIsDisplayed()
                capture("06-options-return-fox-selected-en")

                compose.onNodeWithTag("woodland-choice-rabbit").performScrollTo().performClick()
                compose.onNodeWithText("Wild rabbits can use underground warrens for shelter.").assertIsDisplayed()
                capture("07-rabbit-selected-en-switch-from-fox")
                scenario.recreate()
                compose.onNodeWithText("Wild rabbits can use underground warrens for shelter.").assertIsDisplayed()
                compose.onNodeWithTag("woodland-hotspot-rabbit").assertIsSelected()
                capture("08-rabbit-selection-restored-after-activity-recreation")

                languageInOptions(scenario, german = true)
                compose.onNodeWithText("Wildkaninchen können unterirdische Baue als Schutz nutzen.").assertIsDisplayed()
                capture("15-options-return-rabbit-selected-de")

                scenario.onActivity { assertEquals(1.0f, it.resources.configuration.fontScale) }
                changeViewport(scenario, "2400x1080")
                capture("09-phone-landscape-de", 2400, 1080)

                changeViewport(scenario, "1280x720")
                capture("10-short-landscape-de", 1280, 720)
                compose.onNodeWithTag("woodland-replay").performScrollTo().assertIsDisplayed()

                changeViewport(scenario, "1600x2560")
                capture("11-tablet-portrait-emulator-viewport-de", 1600, 2560)

                changeViewport(scenario, "2560x1600")
                capture("12-tablet-landscape-emulator-viewport-de", 2560, 1600)

                shell("wm size reset")
                shell("settings put system font_scale 1.5")
                Thread.sleep(500)
                scenario.recreate()
                scenario.onActivity { assertEquals(1.5f, it.resources.configuration.fontScale) }
                compose.onNodeWithText("Wildkaninchen können unterirdische Baue als Schutz nutzen.")
                    .performScrollTo().assertIsDisplayed()
                capture("13-woodland-german-1.5x")
                shell("settings put system font_scale 2.0")
                Thread.sleep(500)
                scenario.recreate()
                scenario.onActivity { assertEquals(2.0f, it.resources.configuration.fontScale) }
                compose.onNodeWithText("Wildkaninchen können unterirdische Baue als Schutz nutzen.")
                    .performScrollTo().assertIsDisplayed()
                compose.onNodeWithTag("woodland-title").performScrollTo().assertIsDisplayed()
                compose.onNodeWithTag("woodland-prompt").assertIsDisplayed()
                capture("14-woodland-german-2x")
                shell("settings put system font_scale 1.0")
                Thread.sleep(500)
                scenario.recreate()

                languageInOptions(scenario, german = true)
                compose.onNodeWithTag("woodland-habitats-back").performScrollTo().performClick()
                compose.onNodeWithTag("habitat-ocean-choice").assertIsDisplayed()
                compose.onNodeWithTag("habitat-woodland-choice").assertIsDisplayed()
                capture("16-back-to-habitat-chooser-de")
                compose.onNodeWithTag("shell-back").performClick()
                compose.onNodeWithTag("home-activity-exploreHabitats").assertIsDisplayed()
                compose.onNodeWithTag("home-home").performClick()
                compose.onNodeWithText("Was wollen wir entdecken?").assertIsDisplayed()
            }
        } finally {
            shell("wm size reset")
            shell("settings put system font_scale $oldFontScale")
            prefs.edit().putString("lang", oldLanguage).putString("audioMode", oldAudioMode)
                .putBoolean("sound", oldSound).commit()
        }
    }
}
