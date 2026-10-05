package com.bellfamily.bastischool.ui.wilma

import android.graphics.BitmapFactory
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.InputMode
import androidx.compose.ui.input.InputModeManager
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInputModeManager
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.test.platform.app.InstrumentationRegistry
import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.wilma.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class WilmaBilingualScreenTest {
    @get:Rule val compose=createComposeRule()
    private val taps=mutableListOf<Pair<ContentId,ContentLanguage>>()
    private lateinit var input:InputModeManager

    private fun show(language:ContentLanguage,landscape:Boolean=false) {
        val assets=InstrumentationRegistry.getInstrumentation().targetContext.assets
        val paths=WilmaContent.days.map(WilmaContent::image)+listOf(WilmaContent.HEAD,WilmaContent.TAIL)
        val images=paths.associateWith { path -> assets.open(path).use {
            requireNotNull(BitmapFactory.decodeStream(it,null,BitmapFactory.Options().apply {inSampleSize=2})).asImageBitmap()
        } }
        compose.setContent {
            input=LocalInputModeManager.current
            val density=LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density,1.5f)) {
                MaterialTheme {
                    WilmaScreen(WilmaSelection(phase=WilmaPhase.BILINGUAL),null,null,language,images,
                        false,false,false,false,
                        onPhase={},onDay={error("Unexpected Explore selection")},onReplay={error("Unexpected replay")},
                        onOption={error("Unexpected single-language option")},onAction={error("Unexpected quiz")},
                        onOrder={error("Unexpected ordering")},onAgain={error("Unexpected completion")},onRetry={},onHome={},
                        modifier=Modifier.size(if(landscape)640.dp else 320.dp,if(landscape)240.dp else 480.dp),
                        onBilingualDay={day,spokenLanguage -> taps+=day to spokenLanguage})
                }
            }
        }
    }

    private fun verifyPairs(language:ContentLanguage) {
        compose.onNodeWithTag("wilma-language-Deutsch").performScrollTo().assertTextEquals("Deutsch")
        compose.onNodeWithTag("wilma-language-English").assertTextEquals("English")
        listOf("de","en").forEach { side ->
            compose.onNodeWithTag("wilma-bilingual-head-$side").assert(hasNoClickAction())
            compose.onNodeWithTag("wilma-bilingual-tail-$side").assert(hasNoClickAction())
        }
        WilmaContent.days.forEach {day ->
            val left=compose.onNodeWithTag("wilma-bilingual-de-${day.value}").performScrollTo().assertIsDisplayed()
            val right=compose.onNodeWithTag("wilma-bilingual-en-${day.value}").assertIsDisplayed()
            left.assertContentDescriptionEquals("${WilmaContent.day(day).text.display.de}, ${if(language==ContentLanguage.GERMAN) "Deutsch" else "German"}")
            right.assertContentDescriptionEquals("${WilmaContent.day(day).text.display.en}, ${if(language==ContentLanguage.GERMAN) "Englisch" else "English"}")
            val l=left.fetchSemanticsNode().boundsInRoot
            val r=right.fetchSemanticsNode().boundsInRoot
            assertTrue(l.right<r.left)
            assertEquals(l.top,r.top,1f)
            assertEquals(l.height,r.height,1f)
            left.performClick();right.performClick()
        }
        compose.runOnIdle {
            assertEquals(WilmaContent.days.flatMap {listOf(it to ContentLanguage.GERMAN,it to ContentLanguage.ENGLISH)},taps)
        }
        for(tag in listOf("wilma-replay","wilma-prompt","wilma-next","wilma-help","wilma-retry","wilma-placed-count","wilma-complete","completion-celebration"))
            compose.onNodeWithTag(tag).assertDoesNotExist()
        compose.onAllNodes(hasText("Question",substring=true)).assertCountEquals(0)
        compose.onAllNodes(hasText("Frage",substring=true)).assertCountEquals(0)
        compose.onNodeWithTag("wilma-home").performScrollTo().assertIsDisplayed()
    }

    @Test fun englishShellShowsAllPairsAlignedAtNarrowWidthWithLargeText() {
        show(ContentLanguage.ENGLISH);verifyPairs(ContentLanguage.ENGLISH)
    }
    @Test fun germanShellKeepsBothLanguagesReachableInShortLandscapeWithLargeText() {
        show(ContentLanguage.GERMAN,true);verifyPairs(ContentLanguage.GERMAN)
    }
    @OptIn(ExperimentalTestApi::class,androidx.compose.ui.ExperimentalComposeUiApi::class)
    @Test fun keyboardActivationSpeaksExactlyTheFocusedLanguage() {
        show(ContentLanguage.ENGLISH)
        val day=WilmaContent.days.first()
        val node=compose.onNodeWithTag("wilma-bilingual-en-${day.value}").performScrollTo()
        compose.runOnIdle {assertTrue(input.requestInputMode(InputMode.Keyboard))}
        node.performSemanticsAction(SemanticsActions.RequestFocus) {assertTrue(it())}
        node.assertIsFocused().performKeyInput {pressKey(Key.Enter)}
        compose.runOnIdle {assertEquals(listOf(day to ContentLanguage.ENGLISH),taps)}
    }
}
