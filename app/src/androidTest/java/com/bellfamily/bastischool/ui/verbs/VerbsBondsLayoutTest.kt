package com.bellfamily.bastischool.ui.verbs

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
import com.bellfamily.bastischool.captureNativeReview
import com.bellfamily.bastischool.learning.bonds.*
import com.bellfamily.bastischool.learning.models.ContentLanguage
import com.bellfamily.bastischool.learning.session.*
import com.bellfamily.bastischool.learning.verbs.VerbContent
import com.bellfamily.bastischool.ui.bonds.NumberBondsContent
import com.bellfamily.bastischool.ui.testing.LayoutTestActivity
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
@OptIn(ExperimentalComposeUiApi::class,ExperimentalTestApi::class)
class VerbsBondsLayoutTest(private val german:Boolean,private val fontScale:Float,private val landscape:Boolean) {
    companion object {
        @JvmStatic @Parameterized.Parameters(name="de={0}-font={1}-landscape={2}")
        fun cases()=listOf(arrayOf(false,1f,false),arrayOf(true,1.5f,false),arrayOf(true,2f,true),arrayOf(false,1f,true))
    }
    @get:Rule val compose=createAndroidComposeRule<LayoutTestActivity>()
    @Test fun longLessonAndBothBoardsKeepPicturesLargeTouchTargetsAndKeyboardActionsReachable() {
        compose.runOnUiThread{compose.activity.requestedOrientation=if(landscape)ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE else ActivityInfo.SCREEN_ORIENTATION_PORTRAIT}
        compose.waitUntil(10_000){compose.activity.resources.configuration.orientation==if(landscape)Configuration.ORIENTATION_LANDSCAPE else Configuration.ORIENTATION_PORTRAIT}
        val language=if(german)ContentLanguage.GERMAN else ContentLanguage.ENGLISH
        var selected by mutableIntStateOf(0)
        var returned=false;var replayed=0
        lateinit var input:InputModeManager
        val form=if(compose.activity.resources.configuration.smallestScreenWidthDp>=600)"tablet"else "phone"
        val suffix="$form-${if(german)"de"else "en"}-${(fontScale*10).toInt()}-${if(landscape)"landscape"else "portrait"}"
        compose.setContent {
            val density=LocalDensity.current;input=LocalInputModeManager.current
            CompositionLocalProvider(LocalDensity provides Density(density.density,fontScale)) {
                MaterialTheme {
                    if(selected==0)VerbLessonScreen(VerbContent.byId.getValue("verb.copy_sounds"),language,false,false,{replayed++},{},{returned=true})
                    else {
                        val level=if(selected==1)BondLevel.FIVE else BondLevel.TEN
                        var board by remember(level){mutableStateOf(NumberBonds.start(SessionId("layout"),level,RoundLength.FIVE,42,language))}
                        NumberBondsContent(board,level,language,false,false,false,
                            {board=NumberBonds.reduce(board,NumberBonds.Action.Toggle(it))},
                            {board=NumberBonds.reduce(board,NumberBonds.Action.Check)},
                            {board=NumberBonds.reduce(board,NumberBonds.Action.Next)},
                            {replayed++},{board=NumberBonds.reduce(board,NumberBonds.Action.Help)},{},{},{returned=true})
                    }
                }
            }
        }
        compose.waitUntil(20_000){compose.onAllNodesWithTag("native-verb-pictures").fetchSemanticsNodes().isNotEmpty()}
        val picture=compose.onAllNodesWithTag("native-verb-picture-0").fetchSemanticsNodes().isNotEmpty()
        if(!picture)compose.onNodeWithTag("native-verb-pictures").performScrollTo().performClick()
        repeat(4){compose.onNodeWithTag("native-verb-picture-$it").performScrollTo().assertIsDisplayed()}
        compose.onNodeWithTag("native-verb-listen").performScrollTo().assertIsDisplayed().assertHeightIsAtLeast(48.dp)
        compose.runOnUiThread{input.requestInputMode(InputMode.Keyboard)}
        compose.onNodeWithTag("native-verb-listen").performSemanticsAction(SemanticsActions.RequestFocus){it()}
        compose.onNodeWithTag("native-verb-listen").performKeyInput{pressKey(Key.Enter)}
        compose.runOnIdle{assertEquals(1,replayed)}
        compose.onNodeWithTag("native-verb-return").performScrollTo().performClick()
        compose.runOnIdle{assertTrue(returned)}
        captureNativeReview("layout-lesson-$suffix",compose)
        for(mode in 1..2){
            compose.runOnIdle{selected=mode;returned=false}
            compose.onNodeWithTag("number-bonds-slot-0").performScrollTo().assertIsDisplayed().assertIsNotEnabled().assertWidthIsAtLeast(48.dp).assertHeightIsAtLeast(48.dp)
            compose.onNodeWithTag("number-bonds-check").performScrollTo().assertIsDisplayed().assertHeightIsAtLeast(48.dp).performClick()
            compose.onNodeWithTag("number-bonds-help").performScrollTo().performClick()
            compose.onNodeWithTag("number-bonds-replay").performScrollTo().performClick()
            compose.onNodeWithTag("number-bonds-slot-0").performScrollTo()
            captureNativeReview("layout-build-${if(mode==1)5 else 10}-$suffix",compose)
        }
    }
}
