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
class LengthOrderingLayoutTest(private val german:Boolean,private val fontScale:Float,private val landscape:Boolean) {
    companion object {
        @JvmStatic @Parameterized.Parameters(name="de={0}-font={1}-landscape={2}")
        fun cases()=listOf(arrayOf(false,1f,false),arrayOf(true,1.5f,false),arrayOf(true,2f,true),arrayOf(false,1f,true))
    }
    @get:Rule val compose=createAndroidComposeRule<LayoutTestActivity>()
    @Test fun sharedScaleFixedBoardKeyboardPlacementUndoAndChangedCheckRemainReachable() {
        compose.runOnUiThread {compose.activity.requestedOrientation=if(landscape)ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE else ActivityInfo.SCREEN_ORIENTATION_PORTRAIT}
        compose.waitUntil(10_000) {compose.activity.resources.configuration.orientation==if(landscape)Configuration.ORIENTATION_LANDSCAPE else Configuration.ORIENTATION_PORTRAIT}
        compose.waitUntil(10_000) {var settled=false;compose.runOnUiThread {
            val decor=compose.activity.window.decorView
            settled=decor.width>0&&decor.height>0&&(if(landscape)decor.width>decor.height else decor.height>decor.width)&&
                decor.display?.rotation==(if(landscape)Surface.ROTATION_90 else Surface.ROTATION_0)
        };settled}
        val language=if(german)ContentLanguage.GERMAN else ContentLanguage.ENGLISH
        val samples=listOf(listOf(2,4,8),listOf(2,6,8)).map { lengths ->
            (0L..3000).asSequence().map {LengthOrdering.start(SessionId("layout-${lengths.joinToString("-")}"),RoundLength.FIVE,it,language)}.first {it.task.lengths.sorted()==lengths}
        }
        var board by mutableStateOf(samples.first());lateinit var input:InputModeManager
        val form=if(compose.activity.resources.configuration.smallestScreenWidthDp>=600)"tablet"else"phone"
        val suffix="$form-${if(german)"de"else"en"}-${(fontScale*10).toInt()}-${if(landscape)"landscape"else"portrait"}"
        compose.setContent {
            val density=LocalDensity.current;input=LocalInputModeManager.current
            CompositionLocalProvider(LocalDensity provides Density(density.density,fontScale)) {
                MaterialTheme {LengthOrderingContent(board,language,false,false,false,
                    {board=LengthOrdering.reduce(board,LengthOrdering.Action.Place(it))},{board=LengthOrdering.reduce(board,LengthOrdering.Action.Remove(it))},
                    {board=LengthOrdering.reduce(board,LengthOrdering.Action.Undo)},{board=LengthOrdering.reduce(board,LengthOrdering.Action.Check)},
                    {board=LengthOrdering.reduce(board,LengthOrdering.Action.Next)},{board=LengthOrdering.reduce(board,LengthOrdering.Action.Replay)},
                    {board=LengthOrdering.reduce(board,LengthOrdering.Action.Help)},{},{},{})}
            }
        }
        fun click(tag:String)=compose.onNodeWithTag(tag).performScrollTo().performClick()
        fun keyboard(tag:String) {
            val node=compose.onNodeWithTag(tag)
            node.assertIsDisplayed().assertHeightIsAtLeast(48.dp).assertWidthIsAtLeast(48.dp)
            compose.runOnUiThread {input.requestInputMode(InputMode.Keyboard)}
            node.performSemanticsAction(SemanticsActions.RequestFocus) {it()};node.performKeyInput {pressKey(Key.Enter)}
        }
        fun source(id:com.bellfamily.bastischool.learning.models.ContentId)="length-order-source-${id.value.takeLast(1)}"
        for(sample in samples) {
            compose.runOnIdle {board=sample};compose.waitForIdle()
            val name="length-order-layout-${sample.task.lengths.sorted().joinToString("-")}-$suffix"
            val widths=LengthOrdering.strips.map {compose.onNodeWithTag("length-order-track-source-${it.value.takeLast(1)}",useUnmergedTree=true).fetchSemanticsNode().boundsInRoot.width}+
                (1..3).map {compose.onNodeWithTag("length-order-track-place-$it",useUnmergedTree=true).fetchSemanticsNode().boundsInRoot.width}
            assertTrue(widths.all {kotlin.math.abs(it-widths.first())<=1.1f})
            LengthOrdering.strips.forEach {compose.onNodeWithTag(source(it)).assertIsDisplayed()}
            (1..3).forEach {compose.onNodeWithTag("length-order-place-$it").assertIsDisplayed()}
            captureLengthOrderingReview("$name-start",compose,board,compose.activity.window.decorView)
            compose.onNodeWithTag("length-order-check").performScrollTo().assertIsNotEnabled()
            keyboard(source(board.task.presentation.first()));compose.onNodeWithTag(source(board.task.presentation.first())).assertIsNotEnabled()
            compose.runOnIdle {assertEquals(0,board.current.attempts)}
            click("length-order-undo");compose.runOnIdle {assertTrue(board.current.order.isEmpty())}
            sample.task.presentation.forEach {keyboard(source(it))}
            click("length-order-check");compose.runOnIdle {assertTrue(board.needsCorrection);assertEquals(1,board.current.attempts)}
            compose.onNodeWithTag("length-order-check").assertIsNotEnabled()
            click("length-order-help")
            keyboard("length-order-place-1");click("length-order-undo")
            compose.onNodeWithTag("length-order-check").assertIsNotEnabled()
            repeat(3) {keyboard("length-order-place-1")}
            sample.task.target.forEach {keyboard(source(it))}
            click("length-order-check");compose.runOnIdle {assertTrue(board.solved);assertEquals(2,board.current.attempts);assertEquals(sample.task,board.task)}
            compose.onNodeWithTag("length-order-next").performScrollTo().assertIsDisplayed().assertIsEnabled().assertHeightIsAtLeast(48.dp)
            (1..3).forEach {compose.onNodeWithTag("length-order-place-$it").assertIsDisplayed().assertIsNotEnabled()}
            captureLengthOrderingReview("$name-solved",compose,board,compose.activity.window.decorView)
        }
    }
}
