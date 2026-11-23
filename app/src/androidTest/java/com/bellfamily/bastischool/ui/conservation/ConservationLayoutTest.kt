package com.bellfamily.bastischool.ui.conservation

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
import com.bellfamily.bastischool.learning.conservation.*
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
class ConservationLayoutTest(private val german:Boolean,private val fontScale:Float,private val landscape:Boolean) {
    companion object {
        @JvmStatic @Parameterized.Parameters(name="de={0}-font={1}-landscape={2}")
        fun cases()=listOf(arrayOf(false,1f,false),arrayOf(true,1.5f,false),arrayOf(true,2f,true),arrayOf(false,1f,true))
    }
    @get:Rule val compose=createAndroidComposeRule<LayoutTestActivity>()
    @Test fun bothRangesKeepBeforeVisibleAndSupportKeyboardMovementUndoAndRelationChecks() {
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
        val samples=ConservationRange.entries.flatMap { c->
            listOf(QuantityRelation.SAME to QuantityObject.DINOSAUR,QuantityRelation.SAME to QuantityObject.APPLE,
                QuantityRelation.FEWER to QuantityObject.DINOSAUR,QuantityRelation.MORE to QuantityObject.APPLE).map { (relation,item)->
                (0L..3000).asSequence().map{Conservation.start(SessionId("layout-${c.key}-${relation.name}-${item.name}"),c,RoundLength.FIVE,it,language)}
                    .first{it.task.relation==relation&&it.task.item==item&&when(relation){QuantityRelation.FEWER->it.count==0;QuantityRelation.MORE->it.count==c.limit;else->it.count>1}}
            }
        }
        var board by mutableStateOf(samples.first());lateinit var input:InputModeManager
        val form=if(compose.activity.resources.configuration.smallestScreenWidthDp>=600)"tablet"else"phone"
        val suffix="$form-${if(german)"de"else"en"}-${(fontScale*10).toInt()}-${if(landscape)"landscape"else"portrait"}"
        compose.setContent {
            val density=LocalDensity.current;input=LocalInputModeManager.current
            CompositionLocalProvider(LocalDensity provides Density(density.density,fontScale)) {
                MaterialTheme {
                    ConservationContent(board,board.config,language,false,false,false,
                        {board=Conservation.reduce(board,Conservation.Action.Tap(it))},{board=Conservation.reduce(board,Conservation.Action.Choose(it))},
                        {board=Conservation.reduce(board,Conservation.Action.Undo)},{board=Conservation.reduce(board,Conservation.Action.Check)},
                        {board=Conservation.reduce(board,Conservation.Action.Next)},{board=Conservation.reduce(board,Conservation.Action.Replay)},
                        {board=Conservation.reduce(board,Conservation.Action.Help)},{},{},{},{})
                }
            }
        }
        compose.waitUntil(10_000){val size=compose.onAllNodesWithTag("conservation-game").fetchSemanticsNodes().firstOrNull()?.size?:return@waitUntil false;if(landscape)size.width>size.height else size.height>size.width}
        for(sample in samples) {
            compose.runOnIdle{board=sample}
            compose.waitUntil(10_000){compose.onAllNodesWithTag("conservation-art-ready").fetchSemanticsNodes().size==1}
            val c=board.config;val initial=board.mask
            compose.onNodeWithTag("conservation-given-0",useUnmergedTree=true).assertIsDisplayed().assert(SemanticsMatcher.keyNotDefined(SemanticsActions.OnClick))
            if(board.count>0) {
                val from=(0 until c.slots).first{initial and(1 shl it)!=0};val to=(0 until c.slots).first{initial and(1 shl it)==0}
                val tile=compose.onNodeWithTag("conservation-slot-$from").performScrollTo().assertWidthIsAtLeast(48.dp).assertHeightIsAtLeast(48.dp)
                val size=tile.fetchSemanticsNode().size
                compose.runOnUiThread{input.requestInputMode(InputMode.Keyboard)}
                tile.performSemanticsAction(SemanticsActions.RequestFocus){it()};tile.performKeyInput{pressKey(Key.Enter)}
                compose.runOnIdle{assertEquals(from,board.selected);assertEquals(initial,board.mask)}
                val target=compose.onNodeWithTag("conservation-slot-$to").performScrollTo().assertWidthIsAtLeast(48.dp).assertHeightIsAtLeast(48.dp)
                target.performSemanticsAction(SemanticsActions.RequestFocus){it()};target.performKeyInput{pressKey(Key.Enter)}
                compose.runOnIdle{assertNull(board.selected);assertEquals(sample.count,board.count);assertEquals(0,board.current.attempts)}
                assertEquals(size,tile.fetchSemanticsNode().size)
                compose.onNodeWithTag("conservation-undo").performScrollTo().assertHeightIsAtLeast(48.dp).performClick()
                compose.runOnIdle{assertEquals(initial,board.mask)}
            }else compose.onNodeWithTag("conservation-zero").performScrollTo().assertExists()
            compose.onNodeWithTag("conservation-help").performScrollTo().performClick()
            compose.runOnIdle{assertTrue(board.current.support.hint);assertEquals(initial,board.mask)}
            for(relation in QuantityRelation.entries)compose.onNodeWithTag("conservation-answer-${relation.name.lowercase()}").performScrollTo().assertHeightIsAtLeast(48.dp)
            compose.onNodeWithTag("conservation-answer-${board.task.relation.name.lowercase()}").performScrollTo().performClick()
            compose.onNodeWithTag("conservation-check").performScrollTo().assertHeightIsAtLeast(48.dp).performClick()
            compose.runOnIdle{assertTrue(board.current.solved);assertEquals(1,board.current.attempts);assertEquals(sample.count,board.count)}
            compose.onNodeWithTag("conservation-next").performScrollTo().assertIsDisplayed().assertHeightIsAtLeast(48.dp)
            for(i in 0 until c.limit)compose.onNodeWithTag("conservation-given-$i",useUnmergedTree=true).assertIsDisplayed()
            compose.onNodeWithTag("conservation-board").performScrollTo()
            captureConservationReview("conservation-layout-${c.key}-${board.task.relation.name.lowercase()}-${board.task.item.name.lowercase()}-$suffix",compose,board)
        }
    }
}
