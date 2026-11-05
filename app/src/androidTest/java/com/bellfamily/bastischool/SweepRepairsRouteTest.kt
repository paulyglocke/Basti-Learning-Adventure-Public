package com.bellfamily.bastischool

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.unit.dp
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import com.bellfamily.bastischool.ui.common.BundledArtworkCache
import com.bellfamily.bastischool.ui.home.HomeActivityArtwork
import com.bellfamily.bastischool.ui.washhands.WashHandsArtwork
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class SweepRepairsRouteTest {
    @get:Rule val compose=createEmptyComposeRule()
    @Test fun matchingOptionsRecreationReturnsToBothOriginalBrowsePages() {
        val prefs=InstrumentationRegistry.getInstrumentation().targetContext.getSharedPreferences("basti_shell",0)
        val lang=prefs.getString("lang",null);val audio=prefs.getString("audioMode",null)
        try {
            prefs.edit().putString("lang","en").putString("audioMode","off").commit()
            ActivityScenario.launch(MainActivity::class.java).use { scenario ->
                fun back(){scenario.onActivity {it.onBackPressedDispatcher.onBackPressed()};compose.waitForIdle()}
                fun optionsRoundTrip(){
                    compose.onNodeWithText("⚙ Options").performClick();scenario.recreate();compose.waitForIdle();back();back()
                }
                compose.openHomeActivity("Match Animals to Habitats");optionsRoundTrip()
                compose.onNodeWithText("All Activities").assertExists()
                compose.onNodeWithTag("home-activity-matchHabitats").performScrollTo().assertIsDisplayed()
                compose.onNodeWithTag("home-home").performClick()
                compose.onNodeWithTag("home-category-ANIMALS").performScrollTo().performClick()
                compose.onNodeWithTag("home-activity-matchHabitats").performScrollTo().performClick();optionsRoundTrip()
                compose.onNodeWithText("Animals & Nature").assertExists()
                compose.onNodeWithText("All Activities").assertDoesNotExist()
                compose.onNodeWithTag("home-activity-matchHabitats").performScrollTo().assertIsDisplayed()
            }
        } finally {prefs.edit().putString("lang",lang).putString("audioMode",audio).commit()}
    }
    @Test fun thumbnailAndStepPicturesDecodeAtBoundedSizeAndMissingArtIsSafe() {
        val assets=InstrumentationRegistry.getInstrumentation().targetContext.assets
        val paths=HomeActivityArtwork.entries.values.mapNotNull {it.asset}+WashHandsArtwork.paths.values+
            listOf("Animals/canonical/dinosaur.png","Animals/canonical/whale.png","Animals/canonical/rabbit.png",
                "Animals/canonical/dolphin.png","school_actions/board/object_book.png","vocabulary/food/apple.jpg")
        paths.distinct().forEach {path ->
            val image=BundledArtworkCache.load(assets,path,256)
            assertNotNull(path,image);assertTrue(image!!.width<=256 && image.height<=256)
            assertSame(image,BundledArtworkCache.load(assets,path,256))
        }
        assertNull(BundledArtworkCache.load(assets,"missing-audit-picture.png",256))
    }
    @Test fun activityThumbnailsAreVisibleThroughRealBrowsePages() {
        val prefs=InstrumentationRegistry.getInstrumentation().targetContext.getSharedPreferences("basti_shell",0)
        val lang=prefs.getString("lang",null);val audio=prefs.getString("audioMode",null)
        try {
            prefs.edit().putString("lang","en").putString("audioMode","off").commit()
            ActivityScenario.launch(MainActivity::class.java).use {
                compose.onNodeWithTag("home-category-MATHS").performScrollTo().performClick()
                compose.onNodeWithTag("home-activity-count").performScrollTo().assertIsDisplayed()
                captureSweepScreen("home-maths-count",compose)
                compose.onNodeWithTag("home-activity-makeFive").performScrollTo().assertIsDisplayed()
                captureSweepScreen("home-maths-five",compose)
                compose.onNodeWithTag("home-home").performClick()
                compose.onNodeWithTag("home-category-WORDS").performScrollTo().performClick()
                compose.onNodeWithTag("home-activity-vocabulary").performScrollTo().assertIsDisplayed()
                val tag="bundled-art-school_actions/board/object_book.png"
                compose.waitUntil(10_000){compose.onAllNodesWithTag(tag,useUnmergedTree=true).fetchSemanticsNodes().isNotEmpty()}
                compose.onNodeWithTag(tag,useUnmergedTree=true).assertIsDisplayed().assertWidthIsAtLeast(20.dp)
                captureSweepScreen("home-words",compose)
            }
        } finally {prefs.edit().putString("lang",lang).putString("audioMode",audio).commit()}
    }
}

internal fun captureSweepScreen(name:String,compose:ComposeTestRule) {
    val instrumentation=InstrumentationRegistry.getInstrumentation()
    val directory=java.io.File(instrumentation.targetContext.getExternalFilesDir(null),"sweep-review")
    check(directory.isDirectory || directory.mkdirs())
    compose.waitForIdle()
    val bitmap=compose.onRoot().captureToImage().asAndroidBitmap()
    java.io.FileOutputStream(java.io.File(directory,"$name.png")).use {
        check(bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG,100,it))
    }
    bitmap.recycle()
}
