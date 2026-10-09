package com.bellfamily.bastischool.ui.tellme

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.input.InputMode
import androidx.compose.ui.input.InputModeManager
import androidx.compose.ui.platform.LocalInputModeManager
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.*
import com.bellfamily.bastischool.learning.models.ContentLanguage
import com.bellfamily.bastischool.learning.tellme.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.io.File

@RunWith(Parameterized::class)
class TellMeBannerTest(private val de: Boolean, private val font: Float, private val layout: String) {
    @get:Rule val compose=createAndroidComposeRule<ComponentActivity>()
    companion object {
        @JvmStatic @Parameterized.Parameters(name="de={0},font={1},{2}") fun cases() = listOf(false,true).flatMap { de ->
            listOf(1f,1.5f,2f).flatMap { scale -> listOf("narrow","landscape").map {arrayOf<Any>(de,scale,it)} } +
                listOf(arrayOf<Any>(de,2f,"tablet"),arrayOf<Any>(de,1f,"portrait"))
        }
    }
    @OptIn(ExperimentalTestApi::class, androidx.compose.ui.ExperimentalComposeUiApi::class)
    @Test fun allBannersPreserveOrderTextSemanticsSelectionAndFit() {
        if(layout=="landscape") {
            compose.activity.requestedOrientation=ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            compose.waitUntil(10_000) {compose.activity.resources.configuration.orientation==Configuration.ORIENTATION_LANDSCAPE}
        }
        val language=if(de)ContentLanguage.GERMAN else ContentLanguage.ENGLISH
        val flow=TellMeFlow()
        val selected=mutableListOf<String>()
        val dimensions=when(layout) {"narrow"->280.dp to 600.dp;"landscape"->700.dp to 240.dp;"tablet"->800.dp to 600.dp;else->400.dp to 700.dp}
        flow.categories.forEach { category ->
            val bitmap=BitmapFactory.decodeResource(compose.activity.resources,requireNotNull(tellMeBanner(category.id.value)))
            assertEquals(1536,bitmap.width);assertEquals(384,bitmap.height);bitmap.recycle()
        }
        assertNull(tellMeBanner("savannah"))
        assertNull(tellMeBanner("future_adventure"))
        lateinit var input: InputModeManager
        compose.setContent {
            input=LocalInputModeManager.current
            val density=if(layout=="tablet")1f else LocalDensity.current.density
            CompositionLocalProvider(LocalDensity provides Density(density,font)) {
                MaterialTheme {TellMeScreen(flow,TellMeState(),language,null,false,
                    {selected+=it.value},{_,_->},{},{},{},{},{},Modifier.size(dimensions.first,dimensions.second))}
            }
        }
        compose.onNodeWithText(if(de)"Erzähl mal!" else "Tell Me!").assertDoesNotExist()
        flow.categories.forEachIndexed { index,category ->
            val card=compose.onNodeWithTag("tellme-category-${category.id.value}").performScrollTo()
            card.assertHasClickAction().assertTextEquals(category.display[language]).assertHeightIsAtLeast(110.dp)
            assertEquals(Role.Button,card.fetchSemanticsNode().config[SemanticsProperties.Role])
            val title=compose.onNodeWithTag("tellme-banner-title-${category.id.value}",useUnmergedTree=true)
            val results=mutableListOf<TextLayoutResult>()
            title.performSemanticsAction(SemanticsActions.GetTextLayoutResult){it(results)}
            val text=results.single()
            assertFalse(text.multiParagraph.didExceedMaxLines)
            assertTrue(text.multiParagraph.height<=text.size.height+1)
            repeat(text.lineCount) {line->
                assertFalse(text.isLineEllipsized(line))
                assertTrue(text.getLineRight(line)<=text.size.width+1)
                assertTrue(text.getLineLeft(line)>=-1)
            }
            val cardBounds=card.fetchSemanticsNode().boundsInRoot
            val titleBounds=title.fetchSemanticsNode().boundsInRoot
            assertTrue(titleBounds.top>=cardBounds.top && titleBounds.bottom<=cardBounds.bottom)
            assertTrue(titleBounds.left>=cardBounds.left && titleBounds.right<=cardBounds.right)
            val image=compose.onNodeWithTag("tellme-banner-${category.id.value}",useUnmergedTree=true)
            image.assertHasNoClickAction().assert(SemanticsMatcher.keyNotDefined(SemanticsProperties.ContentDescription))
            if(index==0 || index==2 || index==8) {
                val bitmap=card.captureToImage().asAndroidBitmap()
                File(compose.activity.cacheDir,"banner-$de-$font-$layout-$index.png").outputStream().use {bitmap.compress(Bitmap.CompressFormat.PNG,100,it)}
            }
            card.performClick()
        }
        compose.runOnIdle {assertEquals(flow.categories.map {it.id.value},selected)}
        val first=flow.categories.first()
        val card=compose.onNodeWithTag("tellme-category-${first.id.value}").performScrollTo()
        compose.runOnIdle {assertTrue(input.requestInputMode(InputMode.Keyboard))}
        card.performSemanticsAction(SemanticsActions.RequestFocus){it()}
        card.performKeyInput {pressKey(Key.Enter)}
        compose.runOnIdle {assertEquals(first.id.value,selected.last());assertEquals(10,selected.size)}
    }
}
