package com.bellfamily.bastischool.ui.months

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Bitmap
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.input.InputMode
import androidx.compose.ui.input.InputModeManager
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInputModeManager
import androidx.compose.ui.semantics.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.*
import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.months.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.io.File

@RunWith(Parameterized::class)
class MonthsScreenTest(private val de: Boolean, private val font: Float, private val layout: String) {
    @get:Rule val compose=createAndroidComposeRule<ComponentActivity>()
    companion object {
        @JvmStatic @Parameterized.Parameters(name="de={0},font={1},{2}") fun cases()=listOf(false,true).flatMap { de ->
            listOf(1f,1.5f,2f).flatMap { font -> listOf("portrait","landscape").map { arrayOf<Any>(de,font,it) } } +
                listOf(arrayOf<Any>(de,2f,"tablet"))
        }
    }
    @OptIn(ExperimentalTestApi::class, androidx.compose.ui.ExperimentalComposeUiApi::class)
    @Test fun wheelButtonsTextBoundsKeyboardAndScrolling() {
        val landscape=layout=="landscape"
        compose.activity.requestedOrientation=if(landscape) ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE else ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        compose.waitUntil(10_000){compose.activity.resources.configuration.orientation==
            if(landscape) Configuration.ORIENTATION_LANDSCAPE else Configuration.ORIENTATION_PORTRAIT}
        compose.waitForIdle()
        val lang=if(de)ContentLanguage.GERMAN else ContentLanguage.ENGLISH
        var selected by mutableStateOf(MonthsSelection())
        var listened=0;var homes=0
        lateinit var input:InputModeManager
        compose.setContent {
            input=LocalInputModeManager.current
            val density=if(layout=="tablet")1f else LocalDensity.current.density
            CompositionLocalProvider(LocalDensity provides Density(density,font)) {
                MaterialTheme {
                    MonthsScreen(selected,lang,false,false,{selected=MonthsSelection(it)},{listened++},{},{homes++},
                        Modifier.size(if(layout=="portrait")280.dp else 700.dp,if(layout=="landscape")240.dp else 600.dp))
                }
            }
        }
        val wheel=compose.onNodeWithTag("months-wheel").performScrollTo()
        wheel.assertContentDescriptionEquals(if(de)"Jahresrad. Ausgewählt: Januar. Wähle unten einen Monat." else "Year wheel. Selected: January. Choose a month below.")
        wheel.onChildren().assertCountEquals(0)
        // A large wheel intentionally scrolls in short landscape. Centre the actual circle,
        // not the clipped visible rectangle, before injecting a tap at three o'clock.
        val content=compose.onNodeWithTag("months-content")
        val viewport=content.fetchSemanticsNode().boundsInRoot
        val before=wheel.fetchSemanticsNode()
        val delta=before.positionInRoot.y+before.size.height/2f-viewport.center.y
        content.performSemanticsAction(SemanticsActions.ScrollBy){it(0f,delta)}
        compose.waitForIdle()
        val node=wheel.fetchSemanticsNode()
        val point=node.positionInRoot+Offset(node.size.width*.85f,node.size.height*.5f)-node.boundsInRoot.topLeft
        wheel.performTouchInput {click(point)}
        compose.runOnIdle {assertEquals(MonthIds.APRIL,selected.selected)}
        wheel.captureToImage().asAndroidBitmap().let { bitmap ->
            File(compose.activity.cacheDir,"months-$de-$font-$layout.png").outputStream().use {bitmap.compress(Bitmap.CompressFormat.PNG,100,it)}
        }
        MonthContent.definitions.forEach { month ->
            val button=compose.onNodeWithTag(month.id.value).performScrollTo().assertIsDisplayed()
            button.assertHasClickAction().assertHeightIsAtLeast(64.dp).assertTextEquals(month.text.display[lang])
            val text=compose.onNode(hasText(month.text.display[lang]) and hasAnyAncestor(hasTestTag(month.id.value)),useUnmergedTree=true)
            val results=mutableListOf<TextLayoutResult>()
            text.performSemanticsAction(SemanticsActions.GetTextLayoutResult){it(results)}
            val result=results.single()
            assertFalse(result.multiParagraph.didExceedMaxLines)
            assertTrue(result.multiParagraph.height<=result.size.height+1)
            repeat(result.lineCount){line -> assertFalse(result.isLineEllipsized(line));assertTrue(result.getLineRight(line)<=result.size.width+1)}
            val b=button.fetchSemanticsNode().boundsInRoot;val t=text.fetchSemanticsNode().boundsInRoot
            assertTrue(t.top>=b.top && t.bottom<=b.bottom && t.left>=b.left && t.right<=b.right)
            button.performClick().assertIsSelected()
            compose.runOnIdle {assertEquals(month.id,selected.selected)}
        }
        compose.onNodeWithTag("months-selected").performScrollTo().assertTextEquals(if(de)"Dezember" else "December")
        val january=compose.onNodeWithTag(MonthIds.JANUARY.value).performScrollTo()
        compose.runOnIdle {assertTrue(input.requestInputMode(InputMode.Keyboard))}
        january.performSemanticsAction(SemanticsActions.RequestFocus){it()}
        january.performKeyInput {pressKey(Key.Enter)}
        january.assertIsSelected()
        compose.onNodeWithTag("months-listen").performScrollTo().performClick()
        compose.onNodeWithTag("months-home").performScrollTo().assertIsDisplayed().performClick()
        compose.runOnIdle {assertEquals(1,listened);assertEquals(1,homes)}
        compose.onNodeWithText("Question 1 of 5").assertDoesNotExist()
        compose.onNodeWithText("Again").assertDoesNotExist()
    }
}
