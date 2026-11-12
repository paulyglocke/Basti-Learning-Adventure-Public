package com.bellfamily.bastischool.ui.quantitychange

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
import com.bellfamily.bastischool.captureNativeDisplayReview
import com.bellfamily.bastischool.learning.quantitychange.*
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
class QuantityChangeLayoutTest(private val german:Boolean,private val fontScale:Float,private val landscape:Boolean) {
    companion object {
        @JvmStatic @Parameterized.Parameters(name="de={0}-font={1}-landscape={2}")
        fun cases()=listOf(arrayOf(false,1f,false),arrayOf(true,1.5f,false),arrayOf(true,2f,true),arrayOf(false,1f,true))
    }
    @get:Rule val compose=createAndroidComposeRule<LayoutTestActivity>()
    @Test fun allConfigurationsKeepTheStartingReferenceAndSupportKeyboardUndoAndZero() {
        compose.runOnUiThread{compose.activity.requestedOrientation=if(landscape)ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE else ActivityInfo.SCREEN_ORIENTATION_PORTRAIT}
        compose.waitUntil(10_000){compose.activity.resources.configuration.orientation==if(landscape)Configuration.ORIENTATION_LANDSCAPE else Configuration.ORIENTATION_PORTRAIT}
        // Configuration can change before the actual window finishes rotating.
        // Measure stable tiles only after the decor has the requested geometry.
        compose.waitUntil(10_000) {
            var settled=false
            compose.runOnUiThread {
                val decor=compose.activity.window.decorView
                settled=decor.width>0&&decor.height>0&&
                    (if(landscape)decor.width>decor.height else decor.height>decor.width)&&
                    decor.display?.rotation==(if(landscape)Surface.ROTATION_90 else Surface.ROTATION_0)
            }
            settled
        }
        val language=if(german)ContentLanguage.GERMAN else ContentLanguage.ENGLISH
        fun sample(c:ChangeConfig,start:Int)=(0L..100).asSequence().map{QuantityChange.start(SessionId("layout-${c.key}-$start"),c,RoundLength.FIVE,it,language)}.first{it.task.start==start}
        var board by mutableStateOf(sample(ChangeConfig.all.first(),0))
        lateinit var input:InputModeManager
        val form=if(compose.activity.resources.configuration.smallestScreenWidthDp>=600)"tablet"else"phone"
        val suffix="$form-${if(german)"de"else"en"}-${(fontScale*10).toInt()}-${if(landscape)"landscape"else"portrait"}"
        compose.setContent {
            val density=LocalDensity.current;input=LocalInputModeManager.current
            CompositionLocalProvider(LocalDensity provides Density(density.density,fontScale)) {
                MaterialTheme {
                    QuantityChangeContent(board,board.config,language,false,false,false,
                        {board=QuantityChange.reduce(board,QuantityChange.Action.Toggle(it))},
                        {board=QuantityChange.reduce(board,QuantityChange.Action.Undo)},
                        {board=QuantityChange.reduce(board,QuantityChange.Action.Check)},
                        {board=QuantityChange.reduce(board,QuantityChange.Action.Next)},
                        {board=QuantityChange.reduce(board,QuantityChange.Action.Replay)},
                        {board=QuantityChange.reduce(board,QuantityChange.Action.Help)},{},{},{},{})
                }
            }
        }
        compose.waitUntil(10_000) {
            val size=compose.onNodeWithTag("quantity-change-game").fetchSemanticsNode().size
            if(landscape)size.width>size.height else size.height>size.width
        }
        for(c in ChangeConfig.all)for(anchor in listOf(c.starts.first(),c.starts.last())) {
            compose.runOnIdle{board=sample(c,anchor)}
            val initial=board.mask;val task=board.task
            compose.onNodeWithTag("quantity-change-given-0",useUnmergedTree=true).assertIsDisplayed().assert(SemanticsMatcher.keyNotDefined(SemanticsActions.OnClick))
            val slot=if(c.mode==ChangeMode.MORE)anchor else anchor-1
            val tile=compose.onNodeWithTag("quantity-change-slot-$slot").performScrollTo().assertWidthIsAtLeast(48.dp).assertHeightIsAtLeast(48.dp)
            val size=tile.fetchSemanticsNode().size
            compose.runOnUiThread{input.requestInputMode(InputMode.Keyboard)}
            tile.performSemanticsAction(SemanticsActions.RequestFocus){it()};tile.performKeyInput{pressKey(Key.Enter)}
            compose.runOnIdle{assertEquals(board.answer,board.count);assertEquals(0,board.current.attempts)}
            assertEquals(size,tile.fetchSemanticsNode().size)
            compose.onNodeWithTag("quantity-change-undo").performScrollTo().assertHeightIsAtLeast(48.dp).performClick()
            compose.runOnIdle{assertEquals(initial,board.mask)}
            compose.onNodeWithTag("quantity-change-help").performScrollTo().performClick()
            compose.runOnIdle{assertTrue(board.current.support.hint);assertEquals(initial,board.mask);assertEquals(task,board.task)}
            tile.performScrollTo().performClick()
            if(board.answer==0)compose.onNodeWithTag("quantity-change-zero").performScrollTo().assertExists()
            compose.onNodeWithTag("quantity-change-check").performScrollTo().assertHeightIsAtLeast(48.dp).performClick()
            compose.runOnIdle{assertTrue(board.current.solved);assertEquals(1,board.current.attempts)}
            compose.onNodeWithTag("quantity-change-next").performScrollTo().assertIsDisplayed().assertHeightIsAtLeast(48.dp)
            for(i in 0 until c.range.limit)compose.onNodeWithTag("quantity-change-given-$i",useUnmergedTree=true).assertIsDisplayed()
            if(anchor==c.starts.first())captureNativeDisplayReview("quantity-change-layout-${c.key}-$suffix",compose)
        }
    }
}
