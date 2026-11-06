package com.bellfamily.bastischool.ui.verbmotion

import android.graphics.BitmapFactory
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class VerbMotionActivityTest {
    @get:Rule(order=0) val motionScale=MotionScaleRule()
    @get:Rule(order=1) val compose=createEmptyComposeRule()
    @Test fun allAtlasCellsDecodeWithActualAlphaAndVisiblePixels() {
        val assets=InstrumentationRegistry.getInstrumentation().targetContext.assets
        VerbMotionSample.entries.forEach {sample->
            val bitmap=assets.open(sample.asset).use {BitmapFactory.decodeStream(it)}
            assertNotNull(bitmap);assertTrue(bitmap.hasAlpha());assertTrue(bitmap.width<=2048 && bitmap.height<=2048)
            val width=bitmap.width/sample.atlasColumns;val height=bitmap.height/sample.atlasRows
            (0 until sample.frameCount).forEach {frame->
                val pixels=IntArray(width*height)
                bitmap.getPixels(pixels,0,width,(frame%sample.atlasColumns)*width,(frame/sample.atlasColumns)*height,width,height)
                val coverage=pixels.count {it ushr 24>128}.toFloat()/pixels.size
                assertTrue("${sample.name} frame $frame",coverage in .04f.. .9f)
            }
            bitmap.recycle()
        }
    }
    @Test fun recreationKeepsSelectionAndLanguageButNeverResumesSpeechOrMotion() {
        ActivityScenario.launch(VerbMotionActivity::class.java).use {scenario->
            compose.onNodeWithTag("verb-language-de").performClick()
            compose.onNodeWithTag("verb-select-snap").performScrollTo().performClick()
            compose.waitUntil(10_000) {compose.onAllNodesWithTag("verb-stage").fetchSemanticsNodes().isNotEmpty()}
            scenario.recreate();compose.waitForIdle()
            compose.onNodeWithTag("verb-language-de").assertIsSelected()
            compose.onNodeWithTag("verb-select-snap").assertIsSelected()
            compose.waitUntil(10_000) {compose.onAllNodesWithTag("verb-stage").fetchSemanticsNodes().isNotEmpty()}
            compose.onNodeWithTag("verb-stage").assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription,"Pausiert"))
            scenario.moveToState(Lifecycle.State.CREATED);scenario.moveToState(Lifecycle.State.RESUMED)
            compose.onNodeWithTag("verb-stage").assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription,"Pausiert"))
            compose.onNodeWithTag("verb-close").performScrollTo().performClick()
            compose.waitUntil(5000) {scenario.state==Lifecycle.State.DESTROYED}
        }
    }
}
