package com.bellfamily.bastischool.ui.wilma

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Bitmap
import java.io.File
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.input.InputMode
import androidx.compose.ui.input.InputModeManager
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInputModeManager
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.wilma.*
import com.bellfamily.bastischool.learning.session.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class WilmaDayColoursScreenTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
    private lateinit var focus: FocusManager
    private fun colour(tag:String, expected:Color) {
        val node=compose.onNodeWithTag(tag).performScrollTo().assertIsDisplayed()
        // Compare the resting fill, not Material focus/ripple overlays from earlier tests or taps.
        compose.runOnIdle { focus.clearFocus(force = true) }
        compose.mainClock.advanceTimeBy(1000)
        compose.waitForIdle()
        // Android-backed indications/rendering can outlive the Compose test clock.
        // Await the observable resting fill without loosening its colour or coverage threshold.
        compose.waitUntil(timeoutMillis = 5_000) {
            val pixels=node.captureToImage().toPixelMap()
            var matching=0; var sampled=0
            for(y in 0 until pixels.height step 3) for(x in 0 until pixels.width step 3) {
                val actual=pixels[x,y]; sampled++
                if(kotlin.math.abs(expected.red-actual.red)<.015f &&
                    kotlin.math.abs(expected.green-actual.green)<.015f &&
                    kotlin.math.abs(expected.blue-actual.blue)<.015f) matching++
            }
            matching > sampled/2
        }

    }
    @OptIn(ExperimentalTestApi::class, androidx.compose.ui.ExperimentalComposeUiApi::class)
    @Test fun stripUsesAllSevenCuesAndPreservesLabelsKeyboardAndDisabledSelection() {
        var enabled by mutableStateOf(true)
        var selected:ContentId? by mutableStateOf(null)
        var calls=0
        lateinit var input:InputModeManager
        var surface=Color.White
        compose.setContent {
            focus=LocalFocusManager.current
            input=LocalInputModeManager.current
            val d=LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(d.density,1.5f)) { MaterialTheme {
                surface=MaterialTheme.colorScheme.surface
                Box(Modifier.width(320.dp)) { WilmaStrip(emptyMap(),ContentLanguage.GERMAN,WilmaContent.days,selected,
                    if(enabled) WilmaContent.days.toSet() else emptySet(), {selected=it;calls++}) }
            }}
        }
        WilmaContent.days.forEach {id ->
            colour("day-${id.value}",WilmaDayColours.day(id))
            compose.onNodeWithTag("day-${id.value}").assertTextEquals(WilmaContent.day(id).text.display.de)
                .assertIsEnabled().performClick()
            compose.runOnIdle {assertEquals(id,selected)}
        }
        compose.runOnIdle {assertTrue(input.requestInputMode(InputMode.Keyboard))}
        compose.onNodeWithTag("day-day.sunday").performSemanticsAction(SemanticsActions.RequestFocus) {it()}
        compose.onNodeWithTag("day-day.sunday").assertIsFocused().performKeyInput {pressKey(Key.Enter)}
        compose.runOnIdle {assertEquals(8,calls);selected=null;enabled=false}
        WilmaContent.days.forEach {id ->
            colour("day-${id.value}",WilmaDayColours.background(id,false,surface))
            compose.onNodeWithTag("day-${id.value}").assertIsNotEnabled().performTouchInput {click()}
        }
        compose.runOnIdle {assertEquals(8,calls)}
    }
    private fun ordering(landscape:Boolean) {
        compose.activity.requestedOrientation=if(landscape) ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE else ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        compose.waitUntil(10_000){compose.activity.resources.configuration.orientation==
            if(landscape) Configuration.ORIENTATION_LANDSCAPE else Configuration.ORIENTATION_PORTRAIT}
        var state by mutableStateOf(WilmaOrder.start(SessionId("colours"),42,ContentLanguage.GERMAN))
        var surface=Color.White
        compose.setContent {
            focus=LocalFocusManager.current
            val d=LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(d.density,1.5f)) {MaterialTheme {
                surface=MaterialTheme.colorScheme.surface
                WilmaScreen(WilmaSelection(phase=WilmaPhase.ORDER),null,state,ContentLanguage.GERMAN,emptyMap(),false,false,false,false,
                    {},{},{},{},{},{state=WilmaOrder.reduce(state,it).state},{},{},{},
                    Modifier.size(if(landscape)700.dp else 320.dp,if(landscape)240.dp else 480.dp).testTag("colour-review"))
            }}
        }
        WilmaContent.days.forEach {id ->
            colour("order-${id.value}",WilmaDayColours.day(id))
            val node=compose.onNodeWithTag("order-${id.value}").assertTextEquals(WilmaContent.day(id).text.display.de).assertIsEnabled()
            val layouts=mutableListOf<androidx.compose.ui.text.TextLayoutResult>()
            node.performSemanticsAction(SemanticsActions.GetTextLayoutResult){it(layouts)}
            assertTrue(layouts.isNotEmpty());assertTrue(layouts.none {it.didOverflowHeight})
        }
        val capture=compose.onNodeWithTag("colour-review").captureToImage().asAndroidBitmap()
        File(compose.activity.cacheDir,"wilma-colours-${if(landscape) "landscape" else "portrait"}.png").outputStream().use {
            capture.compress(Bitmap.CompressFormat.PNG,100,it)
        }
        compose.onNodeWithTag("order-day.sunday").performScrollTo().performClick()
        compose.runOnIdle {assertEquals(AnswerState.RETRY_AVAILABLE,state.current.answer)}
        WilmaContent.days.forEach {id ->
            colour("order-${id.value}",WilmaDayColours.background(id,false,surface))
            compose.onNodeWithTag("order-${id.value}").assertIsNotEnabled()
        }
        compose.onNodeWithTag("wilma-retry").performScrollTo().performClick()
        compose.onNodeWithTag("order-day.monday").performScrollTo().performClick()
        compose.onNodeWithTag("order-day.monday").assertDoesNotExist()
        compose.onNodeWithTag("placed-day.monday").assertExists()
        compose.runOnIdle {assertEquals(listOf(WilmaContent.days.first()),state.placed);assertEquals(2,state.steps.first().attempts)}
    }
    @Test fun orderingColoursLabelsRetryAndPlacementInGermanPortrait()=ordering(false)
    @Test fun orderingColoursLabelsRetryAndPlacementInShortLandscape()=ordering(true)
}
