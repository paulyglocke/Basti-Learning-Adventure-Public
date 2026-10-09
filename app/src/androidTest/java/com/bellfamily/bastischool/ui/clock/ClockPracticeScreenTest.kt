package com.bellfamily.bastischool.ui.clock

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.bellfamily.bastischool.learning.clock.*
import com.bellfamily.bastischool.learning.models.ContentLanguage
import com.bellfamily.bastischool.learning.session.SessionId
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class ClockPracticeScreenTest {
    @get:Rule val compose=createComposeRule()
    private lateinit var state:MutableState<ClockPractice.State>
    private var listens=0
    private fun action(a:ClockPractice.Action) {state.value=ClockPractice.reduce(state.value,a)}
    private fun show(german:Boolean=false,short:Boolean=false) {
        state=mutableStateOf(ClockPractice.State(SessionId("make-ui"),ClockPractice.generate(4),if(german)ContentLanguage.GERMAN else ContentLanguage.ENGLISH))
        compose.setContent {
            CompositionLocalProvider(LocalDensity provides Density(LocalDensity.current.density,1.5f)) {
                MaterialTheme {
                    ClockPracticeScreen(state.value,state.value.language,false,false,false,
                        {action(ClockPractice.Action.Move(it))},{},{action(ClockPractice.Action.Move(state.value.position.advance(it)))},
                        {action(ClockPractice.Action.Check)},{action(ClockPractice.Action.Next)},{listens++},
                        {state.value=ClockPractice.State(SessionId("again-ui"),ClockPractice.generate(8),state.value.language)},{},{},{},
                        Modifier.width(if(short)600.dp else 320.dp).height(if(short)280.dp else 700.dp))
                }
            }
        }
    }
    @OptIn(ExperimentalTestApi::class)
    @Test fun narrowEnglishTargetHiddenDigitalRetryHintKeyboardAndAccessibleAdjustment() {
        show()
        val target=state.value.target
        compose.onNodeWithTag("clock-target").performScrollTo().assertTextEquals(ClockPractice.prompt(target).display.en)
        compose.onNodeWithTag("clock-make-digital").assertDoesNotExist()
        val face=compose.onNodeWithTag("clock-face").performScrollTo()
        assertFalse(face.fetchSemanticsNode().config.contains(SemanticsProperties.StateDescription))
        assertTrue(face.fetchSemanticsNode().config[SemanticsProperties.ContentDescription].single().startsWith("Current clock: long hand on"))
        repeat(2) {compose.onNodeWithTag("clock-make-check").performScrollTo().performClick()}
        compose.onNodeWithTag("clock-make-hint").performScrollTo().assertTextEquals(ClockPractice.hint(target).display.en)
        compose.runOnIdle {assertEquals(2,state.value.current.attempts);assertEquals(target,state.value.target)}
        face.performScrollTo().performSemanticsAction(SemanticsActions.RequestFocus){it()}
        face.performKeyInput {pressKey(Key.DirectionRight)}
        val actions=face.fetchSemanticsNode().config[SemanticsActions.CustomActions]
        compose.runOnIdle {
            assertEquals(listOf("30 minutes back","30 minutes forward"),actions.map {it.label})
            assertTrue(actions[1].action())
        }
        compose.onNodeWithTag("clock-make-forward").performScrollTo().performClick()
        compose.runOnIdle {assertEquals(target,state.value.position);assertFalse(state.value.current.solved)}
        compose.onNodeWithTag("clock-make-check").performScrollTo().performClick()
        compose.onNodeWithTag("clock-make-digital").performScrollTo().assertTextEquals(target.digital)
        compose.onNodeWithTag("clock-make-next").performScrollTo().performClick()
        compose.runOnIdle {assertEquals(1,state.value.index);assertEquals(0,state.value.current.attempts)}
    }
    @Test fun shortGermanFullCompletionAndAgain() {
        show(true,true)
        repeat(8) {
            compose.onNodeWithTag("clock-target").performScrollTo().assertTextEquals(ClockPractice.prompt(state.value.target).display.de)
            compose.onNodeWithTag("clock-make-listen").performScrollTo().performClick()
            repeat(3) {compose.onNodeWithTag("clock-make-forward").performScrollTo().performClick()}
            compose.onNodeWithTag("clock-make-check").performScrollTo().performClick()
            compose.onNodeWithTag("clock-make-next").performScrollTo().performClick()
        }
        compose.runOnIdle {assertTrue(state.value.completed);assertEquals(8,state.value.firstTry);assertEquals(8,listens)}
        compose.onNodeWithTag("clock-make-again").assertIsDisplayed().performClick()
        compose.runOnIdle {assertFalse(state.value.completed);assertEquals(0,state.value.index)}
        compose.onNodeWithTag("clock-make-home").performScrollTo().assertIsDisplayed()
    }
    @Test fun circularManipulationUsesHalfHoursWithoutSubmitting() {
        show()
        val start=state.value.position
        compose.onNodeWithTag("clock-face").performScrollTo().performTouchInput {
            down(center.copy(y=height*.15f))
            moveTo(center.copy(x=width*.58f,y=height*.15f),100)
            moveTo(center.copy(x=width*.85f),100)
            moveTo(center.copy(y=height*.85f),100)
            up()
        }
        compose.runOnIdle {
            assertEquals(start.advance(30),state.value.position)
            assertEquals(0,state.value.current.attempts)
            assertTrue(state.value.position.minute in listOf(0,30))
        }
    }
}
