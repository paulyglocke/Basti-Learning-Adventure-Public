package com.bellfamily.bastischool.ui.verbs

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.lifecycle.ViewModelProvider
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import com.bellfamily.bastischool.*
import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.session.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class VerbsRouteTest {
    @get:Rule val compose=createEmptyComposeRule()
    @Test fun quizLessonSettingsRecreationAndCompletionPreserveExactQuestionAndChoices() {
        val prefs=InstrumentationRegistry.getInstrumentation().targetContext.getSharedPreferences("basti_shell",0)
        val lang=prefs.getString("lang",null);val audio=prefs.getString("audioMode",null);val round=prefs.getInt("round",5)
        try {
            prefs.edit().putString("lang","en").putString("audioMode","off").putInt("round",5).commit()
            ActivityScenario.launch(MainActivity::class.java).use {scenario->
                fun state():SessionState {var s:SessionState?=null;scenario.onActivity{s=ViewModelProvider(it)[VerbsQuizViewModel::class.java].state};return s!!}
                fun settled(){compose.waitUntil(20_000){var ready=false;scenario.onActivity{val v=ViewModelProvider(it)[VerbsQuizViewModel::class.java];ready=v.state!=null&&!v.busy&&!v.saveFailed};ready}}
                fun click(tag:String){compose.onNodeWithTag(tag).performScrollTo().performClick();settled()}
                compose.openHomeActivity("Animal Actions");settled()
                if(state().phase==SessionPhase.COMPLETED)click("native-verbs-again")
                val task=state().task;val chosen=state().current
                // Speaker is a separate control and never answers.
                click("native-verbs-speaker-${task.question.choices.first().value}")
                assertEquals(chosen.answer,state().current.answer);assertEquals(chosen.attempts,state().current.attempts)
                captureNativeReview("animal-actions-en",compose)
                click("native-verbs-learn")
                compose.waitUntil(20_000){compose.onAllNodesWithTag("native-verb-lesson").fetchSemanticsNodes().size==1}
                compose.onNodeWithTag("shell-options").performClick()
                compose.waitUntil(10_000){compose.onAllNodesWithTag("options-screen").fetchSemanticsNodes().size==1}
                compose.onNodeWithText("🇩🇪 Deutsch").performClick();scenario.recreate()
                scenario.onActivity{it.onBackPressedDispatcher.onBackPressed()}
                compose.waitUntil(20_000){compose.onAllNodesWithTag("native-verb-return").fetchSemanticsNodes().size==1}
                compose.onNodeWithTag("native-verb-return").performScrollTo().performClick();settled()
                assertEquals(task.id,state().task.id);assertEquals(task.question.choices,state().task.question.choices)
                assertEquals(ContentLanguage.GERMAN,state().language);assertTrue(state().current.support.hint)
                captureNativeReview("animal-actions-de",compose)
                while(state().phase!=SessionPhase.COMPLETED){
                    val s=state()
                    if(s.current.answer==AnswerState.RETRY_AVAILABLE)click("native-verbs-retry")
                    if(!state().current.locked)click("native-verbs-answer-${state().task.question.correct.value}")
                    val saved=SessionCheckpoint.encode(state());scenario.recreate();settled()
                    assertArrayEquals(saved,SessionCheckpoint.encode(state()))
                    click("native-verbs-next")
                }
                compose.onNodeWithTag("completion-celebration").assertExists()
                assertEquals(CompletionState.ACKNOWLEDGED,state().completion)
                captureNativeReview("animal-actions-complete",compose)
            }
        }finally{prefs.edit().putString("lang",lang).putString("audioMode",audio).putInt("round",round).commit()}
    }
}
