package com.bellfamily.bastischool.ui.months

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.months.*
import com.bellfamily.bastischool.learning.session.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class MonthsQuizCompletionTest {
    @get:Rule val compose=createComposeRule()
    @Test fun bothModesAndLanguagesCompleteAndAgain() {
        var mode by mutableStateOf(MonthsMode.FIND)
        var language by mutableStateOf(ContentLanguage.ENGLISH)
        fun start(id:String)=SessionReducer.start((MonthsQuestions.generate(mode,SessionId(id),RoundLength.FIVE,5) as GenerationResult.Generated).plan,language,MonthContent.repository).state
        var state by mutableStateOf(start("first"))
        fun action(a:SessionAction){state=SessionReducer.reduce(state,a).state}
        compose.setContent {MaterialTheme {MonthsQuizScreen(mode,state,language,false,false,false,::action,{}, {},
            {state=start("again")},{},{},{})}}
        for(m in listOf(MonthsMode.FIND,MonthsMode.RELATIONS))for(lang in ContentLanguage.entries) {
            compose.runOnIdle {mode=m;language=lang;state=start("round-${m.name}-${lang.name}")}
            repeat(5) {
                val correct=state.task.question.correct
                compose.onNodeWithTag("months-answer-${correct.value}").performScrollTo().performClick()
                compose.onNodeWithTag("months-quiz-next").performScrollTo().performClick()
            }
            compose.onNodeWithTag("completion-title").assertExists()
            compose.onNodeWithTag("months-quiz-complete").performScrollTo().assertTextEquals(MonthsQuestions.completion.display[lang])
            compose.onNodeWithTag("months-quiz-again").performClick()
            compose.runOnIdle {assertEquals(SessionPhase.ACTIVE,state.phase);assertEquals(0,state.current.attempts);assertEquals(SessionId("again"),state.plan.id)}
        }
    }
}
