package com.bellfamily.bastischool.ui.numberstories

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
import com.bellfamily.bastischool.learning.numberstories.*
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
class NumberStoriesLayoutTest(private val german:Boolean,private val fontScale:Float,private val landscape:Boolean) {
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
        fun sample(c:StoryConfig,scene:StoryScene)=(0L..100).asSequence().map{NumberStories.start(SessionId("layout-${c.key}-${scene.key}"),c,RoundLength.FIVE,it,language)}.first{it.task.scene==scene&&it.task.amount>1&&(c.mode==StoryMode.ADD||it.answer==0)}
        var board by mutableStateOf(sample(StoryConfig.all.first(),StoryScene.POND))
        lateinit var input:InputModeManager
        val form=if(compose.activity.resources.configuration.smallestScreenWidthDp>=600)"tablet"else"phone"
        val suffix="$form-${if(german)"de"else"en"}-${(fontScale*10).toInt()}-${if(landscape)"landscape"else"portrait"}"
        compose.setContent {
            val density=LocalDensity.current;input=LocalInputModeManager.current
            CompositionLocalProvider(LocalDensity provides Density(density.density,fontScale)) {
                MaterialTheme {
                    NumberStoriesContent(board,board.config,language,false,false,false,
                        {board=NumberStories.reduce(board,NumberStories.Action.Toggle(it))},
                        {board=NumberStories.reduce(board,NumberStories.Action.Undo)},
                        {board=NumberStories.reduce(board,NumberStories.Action.Check)},
                        {board=NumberStories.reduce(board,NumberStories.Action.Next)},
                        {board=NumberStories.reduce(board,NumberStories.Action.Replay)},
                        {board=NumberStories.reduce(board,NumberStories.Action.Help)},{},{},{},{})
                }
            }
        }
        compose.waitUntil(10_000) {
            val size=compose.onNodeWithTag("number-stories-game").fetchSemanticsNode().size
            if(landscape)size.width>size.height else size.height>size.width
        }
        for(c in StoryConfig.all)for(scene in StoryScene.entries) {
            compose.runOnIdle{board=sample(c,scene)}
            val initial=board.mask;val task=board.task
            compose.onNodeWithTag("number-stories-given-0",useUnmergedTree=true).assertIsDisplayed().assert(SemanticsMatcher.keyNotDefined(SemanticsActions.OnClick))
            val slot=if(c.mode==StoryMode.ADD)board.task.start else board.task.start-1
            val tile=compose.onNodeWithTag("number-stories-slot-$slot").performScrollTo().assertWidthIsAtLeast(48.dp).assertHeightIsAtLeast(48.dp)
            val size=tile.fetchSemanticsNode().size
            compose.runOnUiThread{input.requestInputMode(InputMode.Keyboard)}
            tile.performSemanticsAction(SemanticsActions.RequestFocus){it()};tile.performKeyInput{pressKey(Key.Enter)}
            compose.runOnIdle{assertEquals(Integer.bitCount(initial)+c.mode.delta,board.count);assertEquals(0,board.current.attempts)}
            assertEquals(size,tile.fetchSemanticsNode().size)
            compose.onNodeWithTag("number-stories-undo").performScrollTo().assertHeightIsAtLeast(48.dp).performClick()
            compose.runOnIdle{assertEquals(initial,board.mask)}
            compose.onNodeWithTag("number-stories-help").performScrollTo().performClick()
            compose.runOnIdle{assertTrue(board.current.support.hint);assertEquals(initial,board.mask);assertEquals(task,board.task)}
            val desired=(1 shl board.answer)-1
            for(i in 0 until c.range.limit)if((board.mask xor desired)and(1 shl i)!=0)compose.onNodeWithTag("number-stories-slot-$i").performScrollTo().performClick()
            if(board.answer==0)compose.onNodeWithTag("number-stories-zero").performScrollTo().assertExists()
            compose.onNodeWithTag("number-stories-check").performScrollTo().assertHeightIsAtLeast(48.dp).performClick()
            compose.runOnIdle{assertTrue(board.current.solved);assertEquals(1,board.current.attempts)}
            compose.onNodeWithTag("number-stories-next").performScrollTo().assertIsDisplayed().assertHeightIsAtLeast(48.dp)
            for(i in 0 until c.range.limit)compose.onNodeWithTag("number-stories-given-$i",useUnmergedTree=true).assertIsDisplayed()
            captureNativeDisplayReview("number-stories-layout-${c.key.replace('_','-')}-${scene.key}-$suffix",compose)
        }
    }
}
