package com.bellfamily.bastischool.ui.prepositions

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.width
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.prepositions.*
import com.bellfamily.bastischool.learning.session.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PrepositionsScreenTest {
    @get:Rule val compose = createComposeRule()
    private lateinit var current: MutableState<SessionState>
    private var listens=0
    private var home=0
    private var again=0
    private fun show(lang: ContentLanguage, round: RoundLength = RoundLength.FIVE, large: Boolean = false) {
        val plan=(PrepositionsContent.generate(SessionId("ui-test"),round,42) as GenerationResult.Generated).plan
        current=mutableStateOf(SessionReducer.start(plan,lang,PrepositionsContent.repository).state)
        compose.setContent {
            val density=LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density,if(large)1.5f else 1f)) {
                MaterialTheme {
                    PrepositionsScreen(current.value,lang,false,false,false,
                        onAction={ current.value=SessionReducer.reduce(current.value,it).state },
                        onOption={listens++}, onRetrySave={}, onAgain={again++}, onIntroduction={},
                        onHome={home++},onLegacy={},modifier=Modifier.width(if(large)320.dp else 700.dp))
                }
            }
        }
    }
    @Test fun englishSpeakersReplayHintWrongRetryAndCompletionAreSeparateActions() {
        show(ContentLanguage.ENGLISH)
        val question=current.value.task.question
        compose.onNodeWithTag("speaker-${question.correct.value}").performScrollTo().performClick()
        compose.runOnIdle {assertEquals(1,listens);assertEquals(0,current.value.current.attempts)}
        compose.onNodeWithTag("replay").performScrollTo().performClick()
        compose.runOnIdle {assertEquals(0,current.value.current.attempts);assertEquals(1,current.value.current.support.replays)}
        compose.onNodeWithTag("hint").performScrollTo().performClick()
        compose.onNodeWithTag("hint-text").assertExists()
        val wrong=question.choices.first {it!=question.correct}
        compose.onNodeWithTag("answer-${wrong.value}").performScrollTo().performClick()
        compose.onNodeWithTag("retry").performScrollTo().performClick()
        repeat(5) {
            val correct=current.value.task.question.correct
            compose.onNodeWithTag("answer-${correct.value}").performScrollTo().performClick()
            compose.onNodeWithTag("answer-${correct.value}").assertIsNotEnabled()
            compose.onNodeWithTag("speaker-${correct.value}").assertIsEnabled()
            compose.onNodeWithTag("next").performScrollTo().performClick()
        }
        compose.onNodeWithTag("again").performScrollTo().assertIsDisplayed().performClick()
        compose.onNodeWithTag("home").performScrollTo().assertIsDisplayed().performClick()
        compose.runOnIdle {assertEquals(5,current.value.score);assertEquals(1,home);assertEquals(1,again)}
    }
    @Test fun germanLargeFontTenQuestionRoundKeepsCompletionReachable() {
        show(ContentLanguage.GERMAN,RoundLength.TEN,true)
        repeat(10) {
            compose.onNodeWithTag("answer-${current.value.task.question.correct.value}").performScrollTo().performClick()
            compose.onNodeWithTag("next").performScrollTo().performClick()
        }
        compose.onNodeWithTag("replay").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("again").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("home").performScrollTo().assertIsDisplayed()
        compose.runOnIdle {assertEquals(SessionPhase.COMPLETED,current.value.phase);assertEquals(10,current.value.score)}
    }
}
