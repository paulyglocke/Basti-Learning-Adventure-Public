package com.bellfamily.bastischool.ui.length

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.view.Surface
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
import com.bellfamily.bastischool.learning.length.*
import com.bellfamily.bastischool.learning.models.ContentLanguage
import com.bellfamily.bastischool.learning.session.*
import com.bellfamily.bastischool.ui.testing.LayoutTestActivity
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
@OptIn(ExperimentalComposeUiApi::class,ExperimentalTestApi::class)
class LengthComparisonLayoutTest(private val german:Boolean,private val fontScale:Float,private val landscape:Boolean) {
    companion object {
        @JvmStatic @Parameterized.Parameters(name="de={0}-font={1}-landscape={2}")
        fun cases()=listOf(arrayOf(false,1f,false),arrayOf(true,1.5f,false),arrayOf(true,2f,true),arrayOf(false,1f,true))
    }
    @get:Rule val compose=createAndroidComposeRule<LayoutTestActivity>()
    @Test fun fixedStripsShareScaleAndRemainVisibleDuringKeyboardMovesAndChecks() {
        compose.runOnUiThread{compose.activity.requestedOrientation=if(landscape)ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE else ActivityInfo.SCREEN_ORIENTATION_PORTRAIT}
        compose.waitUntil(10_000){compose.activity.resources.configuration.orientation==if(landscape)Configuration.ORIENTATION_LANDSCAPE else Configuration.ORIENTATION_PORTRAIT}
        compose.waitUntil(10_000) {
            var settled=false
            compose.runOnUiThread {
                val decor=compose.activity.window.decorView
                settled=decor.width>0&&decor.height>0&&(if(landscape)decor.width>decor.height else decor.height>decor.width)&&
                    decor.display?.rotation==(if(landscape)Surface.ROTATION_90 else Surface.ROTATION_0)
            };settled
        }
        val language=if(german)ContentLanguage.GERMAN else ContentLanguage.ENGLISH
        val samples=listOf(6 to 2,2 to 6,2 to 2,6 to 6).map { (a,b) ->
            (0L..3000).asSequence().map{LengthComparison.start(SessionId("layout-$a-$b"),RoundLength.FIVE,it,language)}.first{it.task.a==a&&it.task.b==b}
        }
        var board by mutableStateOf(samples.first());lateinit var input:InputModeManager
        val form=if(compose.activity.resources.configuration.smallestScreenWidthDp>=600)"tablet"else"phone"
        val suffix="$form-${if(german)"de"else"en"}-${(fontScale*10).toInt()}-${if(landscape)"landscape"else"portrait"}"
        compose.setContent {
            val density=LocalDensity.current;input=LocalInputModeManager.current
            CompositionLocalProvider(LocalDensity provides Density(density.density,fontScale)) {
                MaterialTheme {
                    LengthComparisonContent(board,language,false,false,false,
                        {board=LengthComparison.reduce(board,LengthComparison.Action.Select(it))},{board=LengthComparison.reduce(board,LengthComparison.Action.Move(it))},
                        {board=LengthComparison.reduce(board,LengthComparison.Action.Choose(it))},{board=LengthComparison.reduce(board,LengthComparison.Action.Undo)},
                        {board=LengthComparison.reduce(board,LengthComparison.Action.Check)},{board=LengthComparison.reduce(board,LengthComparison.Action.Next)},
                        {board=LengthComparison.reduce(board,LengthComparison.Action.Replay)},{board=LengthComparison.reduce(board,LengthComparison.Action.Help)},{},{},{})
                }
            }
        }
        compose.waitUntil(10_000){val size=compose.onAllNodesWithTag("length-game").fetchSemanticsNodes().firstOrNull()?.size?:return@waitUntil false;if(landscape)size.width>size.height else size.height>size.width}
        fun click(tag:String)=compose.onNodeWithTag(tag).performScrollTo().performClick()
        fun keyboard(tag:String,scroll:Boolean) {
            val node=compose.onNodeWithTag(tag);if(scroll)node.performScrollTo()
            node.assertHeightIsAtLeast(48.dp).assertWidthIsAtLeast(48.dp)
            compose.runOnUiThread{input.requestInputMode(InputMode.Keyboard)}
            node.performSemanticsAction(SemanticsActions.RequestFocus){it()};node.performKeyInput{pressKey(Key.Enter)}
        }
        for(sample in samples) {
            compose.runOnIdle{board=sample}
            compose.waitForIdle()
            val name="length-layout-a${sample.task.a}-b${sample.task.b}-$suffix"
            compose.onNodeWithTag("length-strip-a").assertIsDisplayed();compose.onNodeWithTag("length-strip-b").assertIsDisplayed()
            val trackA=compose.onNodeWithTag("length-track-a",useUnmergedTree=true).fetchSemanticsNode().boundsInRoot
            val trackB=compose.onNodeWithTag("length-track-b",useUnmergedTree=true).fetchSemanticsNode().boundsInRoot
            assertEquals(trackA.left,trackB.left,.1f);assertEquals(trackA.width,trackB.width,.1f)
            captureLengthReview("$name-start",compose,board,compose.activity.window.decorView)
            click("length-answer-${board.task.relation.name.lowercase()}");compose.onNodeWithTag("length-check").performScrollTo().assertIsNotEnabled()
            keyboard("length-strip-a",false);compose.runOnIdle{assertEquals(Strip.A,board.selected)}
            keyboard("length-align",true);compose.runOnIdle{assertEquals(0,board.positions.a);assertEquals(sample.task,board.task);assertEquals(0,board.current.attempts)}
            click("length-undo");compose.runOnIdle{assertEquals(sample.positions,board.positions);assertNull(board.selected)}
            for(strip in Strip.entries) {
                keyboard("length-strip-${strip.name.lowercase()}",false);keyboard("length-align",true)
            }
            compose.runOnIdle{assertTrue(board.positions.aligned)}
            click("length-right");compose.runOnIdle{assertEquals(1,board.positions.b);assertEquals(0,board.current.attempts)}
            click("length-left");compose.runOnIdle{assertTrue(board.positions.aligned)}
            click("length-help");compose.runOnIdle{assertTrue(board.current.support.hint)}
            for(relation in LengthRelation.entries)compose.onNodeWithTag("length-answer-${relation.name.lowercase()}").performScrollTo().assertHeightIsAtLeast(48.dp)
            click("length-check");compose.runOnIdle{assertTrue(board.current.solved);assertEquals(1,board.current.attempts)}
            compose.onNodeWithTag("length-next").performScrollTo().assertIsDisplayed().assertHeightIsAtLeast(48.dp)
            compose.onNodeWithTag("length-strip-a").assertIsDisplayed().assertIsNotEnabled();compose.onNodeWithTag("length-strip-b").assertIsDisplayed().assertIsNotEnabled()
            assertEquals(trackA.size,compose.onNodeWithTag("length-track-a",useUnmergedTree=true).fetchSemanticsNode().boundsInRoot.size)
            captureLengthReview("$name-solved",compose,board,compose.activity.window.decorView)
        }
    }
}
