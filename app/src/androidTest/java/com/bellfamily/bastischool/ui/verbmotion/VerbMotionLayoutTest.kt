package com.bellfamily.bastischool.ui.verbmotion

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.input.InputMode
import androidx.compose.ui.input.InputModeManager
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInputModeManager
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.Density
import com.bellfamily.bastischool.audio.AudioMode
import com.bellfamily.bastischool.learning.models.ContentLanguage
import com.bellfamily.bastischool.ui.testing.LayoutTestActivity
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
@OptIn(ExperimentalComposeUiApi::class,ExperimentalTestApi::class)
class VerbMotionLayoutTest(private val german:Boolean,private val fontScale:Float,private val landscape:Boolean) {
    companion object {
        @JvmStatic @Parameterized.Parameters(name="de={0}-font={1}-landscape={2}")
        fun cases()=listOf(arrayOf(false,1f,false),arrayOf(true,1.5f,false),arrayOf(true,2f,true),arrayOf(false,1f,true))
    }
    @get:Rule(order=0) val motionScale=MotionScaleRule()
    @get:Rule(order=1) val compose=createAndroidComposeRule<LayoutTestActivity>()
    @Test fun allPictureStepsAndKeyboardListenRemainReachable() {
        compose.runOnUiThread {compose.activity.requestedOrientation=if(landscape)
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE else ActivityInfo.SCREEN_ORIENTATION_PORTRAIT}
        compose.waitUntil(10_000) {compose.activity.resources.configuration.orientation==
            if(landscape)Configuration.ORIENTATION_LANDSCAPE else Configuration.ORIENTATION_PORTRAIT}
        var heard:Pair<VerbMotionSample,ContentLanguage>?=null
        var closed=false
        lateinit var input:InputModeManager
        compose.setContent {
            val density=LocalDensity.current
            input=LocalInputModeManager.current
            CompositionLocalProvider(LocalDensity provides Density(density.density,fontScale)) {
                MaterialTheme {VerbMotionScreen(german,AudioMode.QUESTIONS,false,{v,l->heard=v to l},{},{closed=true})}
            }
        }
        compose.waitUntil(10_000) {compose.onAllNodesWithTag("verb-stage").fetchSemanticsNodes().isNotEmpty()}
        compose.onNodeWithTag("verb-picture-toggle").performScrollTo().performClick()
        val language=if(german)ContentLanguage.GERMAN else ContentLanguage.ENGLISH
        VerbMotionSample.entries.forEach {sample->
            compose.onNodeWithTag("verb-select-${sample.name.lowercase()}").performScrollTo().performClick()
            compose.waitUntil(10_000) {compose.onAllNodesWithTag("verb-pose-0").fetchSemanticsNodes().isNotEmpty()}
            (0..3).forEach {frame->
                compose.onNodeWithTag("verb-pose-$frame").performScrollTo().assertIsDisplayed()
                    .assertContentDescriptionEquals(sample.stages[frame][language])
            }
            compose.onNodeWithTag("verb-listen").performScrollTo().assertIsDisplayed().performClick()
            compose.runOnIdle {assertEquals(sample to language,heard)}
        }
        compose.runOnUiThread {input.requestInputMode(InputMode.Keyboard);heard=null}
        compose.onNodeWithTag("verb-listen").performScrollTo()
            .performSemanticsAction(SemanticsActions.RequestFocus){it()}
        compose.onNodeWithTag("verb-listen").performKeyInput {pressKey(Key.Enter)}
        compose.runOnIdle {assertEquals(VerbMotionSample.SNAP to language,heard)}
        compose.onNodeWithTag("verb-close").performScrollTo().assertIsDisplayed().performClick()
        compose.runOnIdle {assertTrue(closed)}
    }
}
