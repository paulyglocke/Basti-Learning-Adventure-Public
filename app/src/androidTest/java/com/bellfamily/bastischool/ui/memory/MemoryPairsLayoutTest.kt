package com.bellfamily.bastischool.ui.memory

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
import androidx.compose.ui.unit.dp
import com.bellfamily.bastischool.learning.memory.*
import com.bellfamily.bastischool.learning.models.ContentLanguage
import com.bellfamily.bastischool.learning.session.SessionId
import com.bellfamily.bastischool.ui.testing.LayoutTestActivity
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
@OptIn(ExperimentalComposeUiApi::class,ExperimentalTestApi::class)
class MemoryPairsLayoutTest(private val german:Boolean,private val fontScale:Float,private val landscape:Boolean) {
    companion object {
        @JvmStatic @Parameterized.Parameters(name="de={0}-font={1}-landscape={2}")
        fun cases()=listOf(arrayOf(false,1f,false),arrayOf(true,1.5f,false),arrayOf(true,2f,true),arrayOf(false,1f,true))
    }
    @get:Rule val compose=createAndroidComposeRule<LayoutTestActivity>()
    @Test fun bothEightCardBoardsKeepTargetsHelpFeedbackAndKeyboardReachable() {
        compose.runOnUiThread{compose.activity.requestedOrientation=if(landscape)ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE else ActivityInfo.SCREEN_ORIENTATION_PORTRAIT}
        compose.waitUntil(10_000){compose.activity.resources.configuration.orientation==if(landscape)Configuration.ORIENTATION_LANDSCAPE else Configuration.ORIENTATION_PORTRAIT}
        val language=if(german)ContentLanguage.GERMAN else ContentLanguage.ENGLISH
        var selected by mutableStateOf(MemoryMode.ANIMALS)
        lateinit var input:InputModeManager
        val form=if(compose.activity.resources.configuration.smallestScreenWidthDp>=600)"tablet"else "phone"
        val suffix="$form-${if(german)"de"else"en"}-${(fontScale*10).toInt()}-${if(landscape)"landscape"else"portrait"}"
        compose.setContent {
            val density=LocalDensity.current;input=LocalInputModeManager.current
            CompositionLocalProvider(LocalDensity provides Density(density.density,fontScale)) {
                MaterialTheme {
                    val config=MemoryConfig(selected,4)
                    var board by remember(config){mutableStateOf(MemoryPairs.start(SessionId("layout"),config,4,language))}
                    MemoryPairsContent(board,config,language,false,false,false,
                        {board=MemoryPairs.reduce(board,MemoryPairs.Action.Reveal(it))},
                        {board=MemoryPairs.reduce(board,MemoryPairs.Action.Listen(it))},
                        {board=MemoryPairs.reduce(board,MemoryPairs.Action.Continue)},
                        {board=MemoryPairs.reduce(board,MemoryPairs.Action.Replay)},
                        {board=MemoryPairs.reduce(board,MemoryPairs.Action.Help)},{},{},{},{})
                }
            }
        }
        for(mode in MemoryMode.entries) {
            compose.runOnIdle{selected=mode}
            for(slot in 0..7)compose.onNodeWithTag("memory-card-$slot").performScrollTo().assertIsDisplayed().assertHeightIsAtLeast(48.dp).assertWidthIsAtLeast(48.dp)
            compose.onNodeWithTag("memory-card-0").performScrollTo().assertContentDescriptionEquals(if(german)"Karte 1, verdeckt"else "Card 1, face down")
            val beforeSize=compose.onNodeWithTag("memory-card-0").fetchSemanticsNode().size
            compose.runOnUiThread{input.requestInputMode(InputMode.Keyboard)}
            compose.onNodeWithTag("memory-card-0").performSemanticsAction(SemanticsActions.RequestFocus){it()}
            compose.onNodeWithTag("memory-card-0").performKeyInput{pressKey(Key.Enter)}
            assertEquals(beforeSize,compose.onNodeWithTag("memory-card-0").fetchSemanticsNode().size)
            compose.onNodeWithTag("memory-listen-0").performScrollTo().assertIsDisplayed().assertHeightIsAtLeast(48.dp)
            compose.onNodeWithTag("memory-help").performScrollTo().performClick()
            compose.onNodeWithTag("memory-next").performScrollTo().assertIsDisplayed().assertHeightIsAtLeast(48.dp)
            captureMemoryReview("memory-layout-${mode.key}-$suffix",compose)
            compose.onNodeWithTag("memory-next").performClick()
            compose.onNodeWithTag("memory-help").assertExists()
        }
    }
}
