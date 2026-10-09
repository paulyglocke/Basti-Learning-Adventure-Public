package com.bellfamily.bastischool.ui.clock

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.test.platform.app.InstrumentationRegistry
import android.graphics.Bitmap
import java.io.File
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.bellfamily.bastischool.learning.clock.*
import com.bellfamily.bastischool.learning.models.ContentLanguage
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class ClockScreenTest {
    @get:Rule val compose=createComposeRule()
    private lateinit var state:MutableState<ClockExploreState>
    private var listens=0
    private fun show(german:Boolean=false,short:Boolean=false) {
        state=mutableStateOf(ClockExploreState(ClockTime(3),if(german)ContentLanguage.GERMAN else ContentLanguage.ENGLISH))
        compose.setContent {
            CompositionLocalProvider(LocalDensity provides Density(LocalDensity.current.density,1.5f)) {
                MaterialTheme {
                    ClockScreen(state.value,state.value.language,false,false,
                        {state.value=state.value.copy(time=it)}, {state.value=state.value.copy(time=state.value.time.snap())},{},
                        {state.value=state.value.copy(time=state.value.time.advance(it))},{listens++},{},{},
                        Modifier.width(if(short)600.dp else 320.dp).height(if(short)280.dp else 700.dp))
                }
            }
        }
    }
    @OptIn(ExperimentalTestApi::class)
    @Test fun narrowLargeTextControlsKeyboardAndSemantics() {
        show()
        val face=compose.onNodeWithTag("clock-face").performScrollTo()
        face.assertContentDescriptionEquals("Clock showing Three o'clock. Long hand: minutes. Short hand: hours.")
        face.performSemanticsAction(SemanticsActions.RequestFocus){it()}
        face.performKeyInput {pressKey(Key.DirectionRight)}
        compose.runOnIdle {assertEquals(ClockTime(3,5),state.value.time)}
        repeat(5) {compose.onNodeWithTag("clock-forward").performScrollTo().performClick()}
        compose.onNodeWithTag("clock-digital").performScrollTo().assertTextEquals("3:30")
        compose.onNodeWithTag("clock-phrase").assertTextEquals("Half past three")
        compose.onNodeWithTag("clock-listen").performScrollTo().performClick()
        compose.runOnIdle {assertEquals(1,listens)}
        compose.onNodeWithTag("clock-home").performScrollTo().assertIsDisplayed()
    }
    @Test fun shortGermanReachableAndBackwardsWrap() {
        show(true,true)
        compose.runOnIdle {state.value=state.value.copy(time=ClockTime(12))}
        compose.onNodeWithTag("clock-back").performScrollTo().performClick()
        compose.onNodeWithTag("clock-digital").performScrollTo().assertTextEquals("11:55")
        compose.runOnIdle {state.value=state.value.copy(time=ClockTime(3,30))}
        compose.onNodeWithTag("clock-phrase").performScrollTo().assertTextEquals("Halb vier")
        compose.onNodeWithTag("clock-home").performScrollTo().assertIsDisplayed()
    }
    @Test fun circularDragMovesBothRepresentations() {
        show()
        compose.onNodeWithTag("clock-face").performScrollTo().performTouchInput {
            down(center.copy(y=height*.15f))
            // Small initial movement crosses touch slop before the quarter-turn samples.
            moveTo(center.copy(x=width*.58f,y=height*.15f),100)
            moveTo(center.copy(x=width*.85f),100)
            moveTo(center.copy(y=height*.85f),100)
            up()
        }
        compose.runOnIdle {assertTrue(state.value.time.minute in 25..35);assertTrue(state.value.time.hourAngle>100f)}
        compose.onNodeWithTag("clock-digital").performScrollTo().assertTextEquals(state.value.time.digital)
    }
    @Test fun decorativeVectorsKeepOneClockSemanticTargetAndRenderAtRepresentativeTimes() {
        show()
        listOf(ClockTime(12), ClockTime(3), ClockTime(3,30), ClockTime(6), ClockTime(9), ClockTime(11,30)).forEach { time ->
            compose.runOnIdle { state.value = state.value.copy(time = time) }
            val face = compose.onNodeWithTag("clock-face").performScrollTo()
            face.onChildren().assertCountEquals(0)
            face.assertContentDescriptionEquals("Clock showing ${ClockWording.phrase(time, ContentLanguage.ENGLISH)}. Long hand: minutes. Short hand: hours.")
            val bitmap = face.captureToImage().asAndroidBitmap()
            val cache = InstrumentationRegistry.getInstrumentation().targetContext.cacheDir
            File(cache, "clock-hands-${time.hour}-${time.minute}.png").outputStream().use {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
            }
        }
    }

}
