package com.bellfamily.bastischool.ui.verbmotion

import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.provider.Settings
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.test.platform.app.InstrumentationRegistry
import com.bellfamily.bastischool.audio.AudioMode
import com.bellfamily.bastischool.ui.testing.LayoutTestActivity
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import java.io.File

class VerbMotionScreenTest {
    @get:Rule val compose=createAndroidComposeRule<LayoutTestActivity>()
    private class Owner:LifecycleOwner {override val lifecycle=LifecycleRegistry(this)}
    private fun scale(value:String?) {
        val command=if(value==null)"settings delete global animator_duration_scale" else "settings put global animator_duration_scale $value"
        InstrumentationRegistry.getInstrumentation().uiAutomation.executeShellCommand(command).use {
            android.os.ParcelFileDescriptor.AutoCloseInputStream(it).readBytes()
        }
    }
    @Test fun actualFramesMoveThenFreezeForPauseBackgroundAndReducedMotion() {
        val resolver=compose.activity.contentResolver
        val original=Settings.Global.getString(resolver,Settings.Global.ANIMATOR_DURATION_SCALE)
        lateinit var owner:Owner
        try {
            scale("1");compose.mainClock.autoAdvance=false
            compose.runOnUiThread {
                compose.activity.requestedOrientation=ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                owner=Owner();owner.lifecycle.currentState=Lifecycle.State.RESUMED
            }
            compose.setContent {CompositionLocalProvider(LocalLifecycleOwner provides owner) {
                MaterialTheme {VerbMotionScreen(false,AudioMode.OFF,false,{_,_->},{},{})}
            }}
            fun ready() {
                compose.waitUntil(10_000) {
                    compose.mainClock.advanceTimeByFrame()
                    compose.onAllNodesWithTag("verb-stage").fetchSemanticsNodes().isNotEmpty()
                }
                compose.mainClock.advanceTimeByFrame()
            }
            ready()
            VerbMotionSample.entries.forEach {sample->
                compose.onNodeWithTag("verb-select-${sample.name.lowercase()}").performScrollTo().performClick()
                compose.mainClock.advanceTimeBy(32);ready()
                compose.onNodeWithTag("verb-watch").performScrollTo().performClick()
                compose.mainClock.advanceTimeBy(400)
                val before=frame()
                compose.mainClock.advanceTimeBy(600)
                val after=frame()
                assertFalse("${sample.name} must actually change pixels",before.sameAs(after))
                if(sample==VerbMotionSample.JUMP) {
                    // The airborne rabbit's ears previously crossed this top border.
                    (after.width/5 until after.width*4/5).forEach {x->
                        assertEquals("Airborne silhouette must clear the top edge",0xFFFFF5DF.toInt(),after.getPixel(x,4))
                    }
                }
                save("${sample.name.lowercase()}-motion",after)
                compose.onNodeWithTag("verb-pause").performScrollTo().performClick()
                compose.mainClock.advanceTimeBy(64)
                val paused=frame();compose.mainClock.advanceTimeBy(1000)
                assertTrue("Pause holds the current pose",paused.sameAs(frame()))
            }
            compose.onNodeWithTag("verb-watch").performScrollTo().performClick();compose.mainClock.advanceTimeBy(400)
            compose.runOnUiThread {owner.lifecycle.currentState=Lifecycle.State.CREATED}
            compose.mainClock.advanceTimeBy(100);val paused=frame();compose.mainClock.advanceTimeBy(1000)
            assertTrue(paused.sameAs(frame()))
            compose.runOnUiThread {owner.lifecycle.currentState=Lifecycle.State.RESUMED}
            compose.mainClock.advanceTimeBy(64);val returned=frame();compose.mainClock.advanceTimeBy(1000)
            assertTrue("Return stays paused",returned.sameAs(frame()))
            compose.onNodeWithTag("verb-watch").performScrollTo().performClick();compose.mainClock.advanceTimeBy(400)
            compose.runOnUiThread {
                owner.lifecycle.currentState=Lifecycle.State.CREATED
                owner.lifecycle.currentState=Lifecycle.State.RESUMED
            }
            compose.mainClock.advanceTimeBy(64);val fastReturn=frame();compose.mainClock.advanceTimeBy(1000)
            assertTrue("Pause/resume between frames must also stop playback",fastReturn.sameAs(frame()))
            compose.onNodeWithTag("verb-watch").performScrollTo().performClick();compose.mainClock.advanceTimeBy(100)
            scale("0");compose.mainClock.advanceTimeBy(100)
            compose.waitUntil(5000) {
                compose.mainClock.advanceTimeByFrame()
                compose.onAllNodesWithTag("verb-pose-0").fetchSemanticsNodes().isNotEmpty()
            }
            compose.onNodeWithTag("verb-pose-0").performScrollTo().assertIsDisplayed()
            val still=compose.onNodeWithTag("verb-pose-0").captureToImage().asAndroidBitmap()
            compose.mainClock.advanceTimeBy(1000)
            assertTrue(still.sameAs(compose.onNodeWithTag("verb-pose-0").captureToImage().asAndroidBitmap()))
            compose.onNodeWithTag("verb-watch").assertDoesNotExist()
        } finally {compose.mainClock.autoAdvance=true;scale(original)}
    }
    private fun frame():Bitmap {
        compose.onNodeWithTag("verb-stage").performScrollTo()
        return compose.onNodeWithTag("verb-stage").captureToImage().asAndroidBitmap()
    }
    private fun save(name:String,bitmap:Bitmap) {
        val folder=File(compose.activity.getExternalFilesDir(null),"verb-motion-review").apply {mkdirs()}
        File(folder,"$name.png").outputStream().use {bitmap.compress(Bitmap.CompressFormat.PNG,100,it)}
    }
}
