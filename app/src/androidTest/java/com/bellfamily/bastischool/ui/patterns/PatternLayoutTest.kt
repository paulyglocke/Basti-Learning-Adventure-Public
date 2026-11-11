package com.bellfamily.bastischool.ui.patterns

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
import com.bellfamily.bastischool.captureNativeDisplayReview
import com.bellfamily.bastischool.learning.patterns.*
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
class PatternLayoutTest(private val german:Boolean,private val fontScale:Float,private val landscape:Boolean) {
    companion object {
        @JvmStatic @Parameterized.Parameters(name="de={0}-font={1}-landscape={2}")
        fun cases()=listOf(arrayOf(false,1f,false),arrayOf(true,1.5f,false),arrayOf(true,2f,true),arrayOf(false,1f,true))
    }
    @get:Rule val compose=createAndroidComposeRule<LayoutTestActivity>()
    @Test fun everyRuleAndContextKeepsTheExampleFixedAndProvidesReachableKeyboardConstruction() {
        compose.runOnUiThread{compose.activity.requestedOrientation=if(landscape)ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE else ActivityInfo.SCREEN_ORIENTATION_PORTRAIT}
        compose.waitUntil(10_000){compose.activity.resources.configuration.orientation==if(landscape)Configuration.ORIENTATION_LANDSCAPE else Configuration.ORIENTATION_PORTRAIT}
        val language=if(german)ContentLanguage.GERMAN else ContentLanguage.ENGLISH
        fun sample(level:PatternLevel,context:PatternContext)=(0L..100).asSequence().map{PatternBuilder.start(SessionId("layout"),level,RoundLength.FIVE,it,language)}.first{it.task.context==context}
        var board by mutableStateOf(sample(PatternLevel.AB,PatternContext.ANIMALS))
        lateinit var input:InputModeManager
        var listens=0
        val form=if(compose.activity.resources.configuration.smallestScreenWidthDp>=600)"tablet"else"phone"
        val suffix="$form-${if(german)"de"else"en"}-${(fontScale*10).toInt()}-${if(landscape)"landscape"else"portrait"}"
        compose.setContent {
            val density=LocalDensity.current;input=LocalInputModeManager.current
            CompositionLocalProvider(LocalDensity provides Density(density.density,fontScale)) {
                MaterialTheme {
                    PatternBuilderContent(board,board.level,language,false,false,false,
                        {board=PatternBuilder.reduce(board,PatternBuilder.Action.Add(it))},
                        {board=PatternBuilder.reduce(board,PatternBuilder.Action.Remove(it))},
                        {board=PatternBuilder.reduce(board,PatternBuilder.Action.Undo)},
                        {board=PatternBuilder.reduce(board,PatternBuilder.Action.Check)},
                        {board=PatternBuilder.reduce(board,PatternBuilder.Action.Next)},
                        {board=PatternBuilder.reduce(board,PatternBuilder.Action.Replay)},
                        {listens++},{board=PatternBuilder.reduce(board,PatternBuilder.Action.Help)},{},{},{},{})
                }
            }
        }
        for(level in PatternLevel.entries)for(context in PatternContext.entries) {
            compose.runOnIdle{board=sample(level,context)}
            val example=board.task.example;val first=board.task.unit[0]
            compose.onNodeWithTag("pattern-given-0-0",useUnmergedTree=true).assertIsDisplayed().assert(SemanticsMatcher.keyNotDefined(SemanticsActions.OnClick))
            compose.onNodeWithTag("pattern-slot-0").performScrollTo().assertWidthIsAtLeast(48.dp).assertHeightIsAtLeast(48.dp).assertIsNotEnabled()
            val size=compose.onNodeWithTag("pattern-slot-0").fetchSemanticsNode().size
            val choice=compose.onNodeWithTag("pattern-choice-${first.value}").performScrollTo().assertHeightIsAtLeast(48.dp).assertWidthIsAtLeast(48.dp)
            compose.runOnUiThread{input.requestInputMode(InputMode.Keyboard)}
            choice.performSemanticsAction(SemanticsActions.RequestFocus){it()};choice.performKeyInput{pressKey(Key.Enter)}
            compose.runOnIdle{assertEquals(first,board.placed[0]);assertEquals(0,board.current.attempts)}
            assertEquals(size,compose.onNodeWithTag("pattern-slot-0").fetchSemanticsNode().size)
            compose.onNodeWithTag("pattern-listen-${first.value}").performScrollTo().assertHeightIsAtLeast(48.dp).performClick()
            compose.onNodeWithTag("pattern-undo").performScrollTo().performClick();compose.runOnIdle{assertTrue(board.placed.all{it==null})}
            compose.onNodeWithTag("pattern-help").performScrollTo().performClick();compose.onNodeWithTag("pattern-hint").assertExists()
            example.let{compose.runOnIdle{assertEquals(it,board.task.example)}}
            board.task.unit.forEach{id->compose.onNodeWithTag("pattern-choice-${id.value}").performScrollTo().performClick()}
            compose.onNodeWithTag("pattern-check").performScrollTo().assertHeightIsAtLeast(48.dp).assertIsEnabled().performClick()
            compose.runOnIdle{assertTrue(board.current.solved);assertTrue(board.current.support.hint);assertEquals(1,board.current.attempts)}
            compose.onNodeWithTag("pattern-next").performScrollTo().assertIsDisplayed().assertHeightIsAtLeast(48.dp)
            for(group in 0..1)for(slot in level.rule.indices)compose.onNodeWithTag("pattern-given-$group-$slot",useUnmergedTree=true).assertIsDisplayed()
            if(level!=PatternLevel.AAB)captureNativeDisplayReview("pattern-layout-${level.key}-${context.key}-$suffix",compose)
        }
        compose.runOnIdle{assertEquals(6,listens)}
    }
}
