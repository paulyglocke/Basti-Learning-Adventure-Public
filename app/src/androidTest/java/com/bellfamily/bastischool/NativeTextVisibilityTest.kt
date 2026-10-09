package com.bellfamily.bastischool

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.*
import com.bellfamily.bastischool.learning.models.ContentLanguage
import com.bellfamily.bastischool.learning.prepositions.PrepositionsContent
import com.bellfamily.bastischool.ui.prepositions.PositionSceneImage
import com.bellfamily.bastischool.ui.common.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class NativeTextVisibilityTest(private val de: Boolean, private val scale: Float, private val landscape: Boolean) {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
    companion object {
        @JvmStatic @Parameterized.Parameters(name="de={0},font={1},landscape={2}")
        fun cases() = listOf(false,true).flatMap { de -> listOf(1f,1.5f).flatMap { scale ->
            listOf(false,true).map { arrayOf<Any>(de,scale,it) }
        } }
    }
    private fun readable(node: SemanticsNodeInteraction) {
        if (generateSequence(node.fetchSemanticsNode().parent) { it.parent }
                .any { it.config.contains(SemanticsActions.ScrollBy) }) node.performScrollTo()
        node.assertIsDisplayed()
        val results = mutableListOf<TextLayoutResult>()
        node.performSemanticsAction(SemanticsActions.GetTextLayoutResult) { assertTrue(it(results)) }
        assertTrue(results.isNotEmpty())
        results.forEach { layout ->
            // Check painted lines, not unused paragraph width in Compose's semantic layout copy.
            assertFalse("Truncated: ${layout.layoutInput.text}", layout.multiParagraph.didExceedMaxLines)
            assertTrue(layout.multiParagraph.height <= layout.size.height + 1)
            repeat(layout.lineCount) { line ->
                assertFalse(layout.isLineEllipsized(line))
                assertTrue("Right edge: ${layout.layoutInput.text}", layout.getLineRight(line) <= layout.size.width + 1)
                assertTrue(layout.getLineLeft(line) >= -1)
            }
        }
        val bounds = node.fetchSemanticsNode().boundsInRoot
        assertTrue("Text clipped vertically", bounds.height + 1 >= results.first().size.height)
        assertTrue("Text clipped horizontally", bounds.width + 1 >= results.first().size.width)
    }
    @Test fun homeSettingsCompletionAndFallbackTextRemainReadable() {
        if (landscape) {
            compose.activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            compose.waitUntil(10_000) { compose.activity.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE }
        }
        var page by mutableIntStateOf(0)
        var clicks = 0
        val lang = if (de) "de" else "en"
        val language = if (de) ContentLanguage.GERMAN else ContentLanguage.ENGLISH
        compose.setContent {
            CompositionLocalProvider(LocalDensity provides Density(LocalDensity.current.density,scale)) {
                MaterialTheme { Box(Modifier.size(if (landscape) 640.dp else 320.dp,if (landscape) 240.dp else 640.dp)) {
                    when(page) {
                        0 -> NativeHome(lang,PaddingValues(0.dp),false,{clicks++})
                        1 -> NativeOptions(lang,"off",5,10,PaddingValues(0.dp),"ready",false,{}, {}, {}, {}, {}, {})
                        2 -> NativeCompletionScreen("text-layout",language,if(de) "Du hast alle Aufgaben geschafft!" else "You completed every task!",true,"visibility-",{},{clicks++},{},{})
                        3 -> Column(Modifier.verticalScroll(rememberScrollState())) {
                            PositionSceneImage(PrepositionsContent.scenes.first(),language,null,Modifier.fillMaxWidth())
                        }
                        else -> Column(Modifier.verticalScroll(rememberScrollState())) {
                            NativeScreenHeader({Text(if(de) "Anweisungen folgen" else "Follow the Instructions")},
                                {OutlinedButton({}){Text("←")}}, {OutlinedButton({}){Text(if(de) "⚙ Optionen" else "⚙ Options")}},Color.Yellow,WindowInsets(0))
                        }
                    }
                } }
            }
        }
        if(landscape && scale == 1f) {
            val first=compose.onNodeWithText(if(de) "Erzähl mal!" else "Tell Me!").performScrollTo().fetchSemanticsNode().boundsInRoot
            val second=compose.onNodeWithText(if(de) "Wortschatz" else "Vocabulary Booster").fetchSemanticsNode().boundsInRoot
            assertEquals(first.top,second.top,1f)
            assertEquals("Equal card heights in a two-column row",first.height,second.height,1f)
        }
        val title = if(de) "Zusammenzählen" else "Add Together"
        readable(compose.onNodeWithText(title,useUnmergedTree=true))
        val results=mutableListOf<TextLayoutResult>()
        compose.onNodeWithText(title,useUnmergedTree=true).performSemanticsAction(SemanticsActions.GetTextLayoutResult){it(results)}
        if(de) assertEquals("Avoid fragmented Home title",1,results.single().lineCount)
        readable(compose.onNodeWithText(if(de) "Bewege den langen Zeiger und entdecke die Uhrzeit." else "Move the long hand and explore time.",useUnmergedTree=true))
        compose.onNodeWithText(if(de) "Uhr & Zeit" else "Clock & Time").performScrollTo().performClick()
        compose.runOnIdle { assertEquals(1,clicks); page=1 }
        listOf("🇬🇧 English","🇩🇪 Deutsch","100").forEach { readable(compose.onNodeWithText(it,useUnmergedTree=true)) }
        compose.runOnIdle { page=2 }
        readable(compose.onNodeWithText(if(de) "Abenteuer geschafft!" else "Adventure complete!",useUnmergedTree=true))
        val again=compose.onNodeWithTag("visibility-again")
        if(landscape) again.performScrollTo()
        again.assertIsDisplayed().assertHeightIsAtLeast(56.dp).performClick()
        readable(compose.onNodeWithTag("visibility-complete"))
        compose.runOnIdle { assertEquals(2,clicks); page=3 }
        readable(compose.onNodeWithTag("position-image-unavailable"))
        compose.runOnIdle { page=4 }
        readable(compose.onNodeWithText(if(de) "Anweisungen folgen" else "Follow the Instructions",useUnmergedTree=true))
    }
}
