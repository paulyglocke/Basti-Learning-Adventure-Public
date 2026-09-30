package com.bellfamily.bastischool.ui.washhands

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.view.Surface
import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.input.*
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.*
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.*
import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.session.*
import com.bellfamily.bastischool.learning.sequencing.*
import com.bellfamily.bastischool.learning.washhands.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@OptIn(ExperimentalTestApi::class,ExperimentalComposeUiApi::class)
@RunWith(Parameterized::class)
class WashHandsScreenTest(private val language:ContentLanguage,private val orientation:Int) {
    companion object {@JvmStatic @Parameterized.Parameters(name="{0}-{1}") fun cases()=listOf(
        arrayOf<Any>(ContentLanguage.ENGLISH,ActivityInfo.SCREEN_ORIENTATION_PORTRAIT),
        arrayOf<Any>(ContentLanguage.GERMAN,ActivityInfo.SCREEN_ORIENTATION_PORTRAIT),
        arrayOf<Any>(ContentLanguage.GERMAN,ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE),
        arrayOf<Any>(ContentLanguage.GERMAN,ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE))}
    @get:Rule val compose=createAndroidComposeRule<ComponentActivity>()
    @Test fun listenKeyboardBuildCheckUndoHintAndCompletionRemainReachable() {
        compose.activity.requestedOrientation=orientation
        val landscape=orientation!=ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        compose.waitUntil(10_000){compose.activity.resources.configuration.orientation==if(landscape)Configuration.ORIENTATION_LANDSCAPE else Configuration.ORIENTATION_PORTRAIT}
        val rotation=when(orientation){ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE->Surface.ROTATION_90;ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE->Surface.ROTATION_270;else->Surface.ROTATION_0}
        compose.waitUntil(10_000){compose.activity.window.decorView.display?.rotation==rotation}
        var state by mutableStateOf(WashHands.start(SessionId("wash-ui"),4,language))
        val operations=mutableListOf<SequenceOperation>();val heard=mutableListOf<ContentId>();var home=0
        lateinit var input:InputModeManager
        compose.setContent {
            input=LocalInputModeManager.current
            val density=LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density,1.5f)) {
                MaterialTheme {WashHandsScreen(state,language,false,false,false,
                    {operations+=it.operation;state=WashHands.reduce(state,it).state},{heard+=it},
                    {state=WashHands.start(SessionId("again"),5,language)},{},{home++})}
            }
        }
        fun node(tag:String)=compose.onNodeWithTag(tag)
        fun click(tag:String){node(tag).performScrollTo().assertIsDisplayed().performClick()}
        WashHands.steps.forEach {s->
            node("wash-item-${s.id.value}").performScrollTo().assertIsDisplayed().assertHasClickAction().assertHeightIsAtLeast(56.dp)
            click("wash-listen-${s.id.value}")
        }
        assertEquals(WashHands.order,heard);assertTrue(state.constructed.isEmpty());assertTrue(operations.isEmpty())
        for(i in 1..4)node("wash-position-$i").performScrollTo().assertIsDisplayed()
        node("wash-check").performScrollTo().assertIsNotEnabled()
        val reverse=WashHands.order.reversed();val first=reverse[0]
        val target=node("wash-item-${first.value}").performScrollTo()
        compose.runOnIdle{assertTrue(input.requestInputMode(InputMode.Keyboard))}
        target.performSemanticsAction(SemanticsActions.RequestFocus){assertTrue(it())}
        target.assertIsFocused().performKeyInput {pressKey(Key.Enter)}
        compose.runOnIdle{assertEquals(listOf(first),state.constructed);assertEquals(listOf(SequenceOperation.Append(first)),operations)}
        reverse.drop(1).forEach {click("wash-item-${it.value}")}
        assertFalse(state.completed);assertEquals(0,state.attempts)
        click("wash-check");assertEquals(reverse,state.constructed);assertFalse(state.completed)
        node("wash-guidance").performScrollTo().assertIsDisplayed();node("wash-hint").assertDoesNotExist()
        click("wash-help");node("wash-hint").performScrollTo().assertIsDisplayed();assertEquals(0,state.hintPosition)
        reverse.forEach {click("wash-remove-${it.value}")};assertTrue(state.constructed.isEmpty())
        WashHands.order.forEach {click("wash-item-${it.value}")}
        assertFalse(state.completed);click("wash-check");assertTrue(state.completed)
        node("completion-celebration").assertExists()
        node("wash-home").assertIsDisplayed().performClick();assertEquals(1,home)
        node("wash-again").performClick();assertFalse(state.completed)
    }
}
