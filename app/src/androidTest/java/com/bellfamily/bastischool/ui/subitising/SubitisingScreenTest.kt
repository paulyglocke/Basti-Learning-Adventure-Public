package com.bellfamily.bastischool.ui.subitising

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.InputMode
import androidx.compose.ui.input.InputModeManager
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInputModeManager
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.bellfamily.bastischool.learning.models.ContentLanguage
import com.bellfamily.bastischool.learning.session.*
import com.bellfamily.bastischool.learning.subitising.SubitisingContent
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class SubitisingScreenTest {
    @get:Rule val compose=createComposeRule()
    private lateinit var state:MutableState<SessionState>
    private lateinit var input:InputModeManager
    private var replays=0
    private var again=0
    private fun show(language:ContentLanguage,short:Boolean=false) {
        val plan=(SubitisingContent.generate(SessionId("ui-dots"),RoundLength.FIVE,4) as GenerationResult.Generated).plan
        state=mutableStateOf(SessionReducer.start(plan,language,SubitisingContent.repository).state)
        compose.setContent {
            input=LocalInputModeManager.current
            CompositionLocalProvider(LocalDensity provides Density(LocalDensity.current.density,1.5f)) {
                MaterialTheme {
                    SubitisingScreen(state.value,language,false,false,false,
                        {state.value=SessionReducer.reduce(state.value,it).state},{replays++},{again++},{},{},
                        Modifier.width(if(short)600.dp else 320.dp).height(if(short)280.dp else 700.dp))
                }
            }
        }
    }
    @OptIn(ExperimentalTestApi::class,androidx.compose.ui.ExperimentalComposeUiApi::class)
    @Test fun narrowEnglishSingleDotGroupListenHelpRetryAndKeyboardAnswer() {
        show(ContentLanguage.ENGLISH)
        val q=state.value.task.question
        val n=SubitisingContent.quantity(q.correct)
        compose.onNodeWithTag("subitising-dots").performScrollTo().assertContentDescriptionEquals(SubitisingContent.description(n).display.en)
        compose.onAllNodesWithContentDescription(SubitisingContent.description(n).display.en).assertCountEquals(1)
        compose.onNodeWithTag("subitising-dots").assert(hasNoClickAction())
        compose.onNodeWithTag("subitising-replay").performScrollTo().performClick()
        compose.runOnIdle {assertEquals(1,replays);assertEquals(0,state.value.current.attempts)}
        compose.onNodeWithTag("subitising-help").performScrollTo().performClick()
        val wrong=SubitisingContent.quantity(q.choices.first {it!=q.correct})
        compose.onNodeWithTag("subitising-answer-$wrong").performScrollTo().performClick()
        compose.onNodeWithTag("subitising-retry").performScrollTo().performClick()
        compose.runOnIdle {assertEquals(1,state.value.current.attempts);assertTrue(state.value.current.support.hint);assertTrue(input.requestInputMode(InputMode.Keyboard))}
        val answer=compose.onNodeWithTag("subitising-answer-$n").performScrollTo().assertTextEquals("$n")
        answer.performSemanticsAction(SemanticsActions.RequestFocus){assertTrue(it())}
        answer.assertIsFocused().performKeyInput {pressKey(Key.Enter)}
        compose.runOnIdle {assertEquals(AnswerState.CORRECT,state.value.current.answer)}
    }
    @Test fun shortGermanLargeTextRoundCompletesWithReachableActions() {
        show(ContentLanguage.GERMAN,true)
        repeat(5) {
            val n=SubitisingContent.quantity(state.value.task.question.correct)
            compose.onNodeWithTag("subitising-dots").performScrollTo().assertContentDescriptionEquals(SubitisingContent.description(n).display.de)
            compose.onNodeWithTag("subitising-answer-$n").performScrollTo().performClick()
            compose.onNodeWithTag("subitising-next").performScrollTo().performClick()
        }
        compose.onNodeWithTag("subitising-complete").assertExists()
        compose.onNodeWithTag("subitising-again").assertIsDisplayed().performClick()
        compose.runOnIdle {assertEquals(1,again)}
    }
}
