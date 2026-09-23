package com.bellfamily.bastischool.ui.vocabulary

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.width
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.vocabulary.*
import com.bellfamily.bastischool.learning.session.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class VocabularyScreenTest {
    @get:Rule val compose=createComposeRule()
    private lateinit var current:MutableState<SessionState>
    private lateinit var selected:MutableState<VocabularySelection>
    private var listens=0
    private var home=0
    private var again=0
    private fun show(lang:ContentLanguage,phase:VocabularyPhase=VocabularyPhase.FIND,round:RoundLength=RoundLength.FIVE,large:Boolean=false) {
        val plan=(VocabularyContent.generate(phase,SessionId("ui-vocabulary"),round,42) as GenerationResult.Generated).plan
        current=mutableStateOf(SessionReducer.start(plan,lang,VocabularyContent.repository).state)
        selected=mutableStateOf(VocabularySelection())
        compose.setContent {
            val density=LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density,if(large)1.5f else 1f)) {
                MaterialTheme {
                    VocabularyScreen(selected.value,current.value,lang,false,false,false,
                        onSelect={selected.value=selected.value.copy(selected=it)},onPhase={selected.value=selected.value.copy(phase=it)},
                        onReplay={listens++},onExample={listens++},onAction={current.value=SessionReducer.reduce(current.value,it).state},
                        onOption={listens++},onAgain={again++},onRetry={},onHome={home++},modifier=Modifier.width(if(large)320.dp else 700.dp))
                }
            }
        }
    }
    @Test fun allExploreWordsAndSentencesAreReachableWithoutAttempts() {
        show(ContentLanguage.ENGLISH)
        VocabularyContent.items.forEach {item ->
            compose.onNodeWithTag("word-${item.id.value}").performScrollTo().performClick()
            compose.onNodeWithTag("vocabulary-word").assertTextEquals(item.text.display.en)
            compose.onNodeWithText(item.example.display.en).performScrollTo().assertIsDisplayed()
            compose.onNodeWithTag("vocabulary-replay").performScrollTo().performClick()
            compose.onNodeWithTag("vocabulary-example").performScrollTo().performClick()
        }
        compose.runOnIdle {assertEquals(12,listens);assertEquals(0,current.value.current.attempts)}
    }
    @Test fun englishFindFiveSeparatesPictureAnswerListenRetryHelpAndCompletion() {
        show(ContentLanguage.ENGLISH)
        compose.onNodeWithTag("vocabulary-find").performScrollTo().performClick()
        val q=current.value.task.question
        compose.onNodeWithTag("speaker-${q.correct.value}").performScrollTo().performClick()
        compose.onNodeWithTag("vocabulary-replay").performScrollTo().performClick()
        compose.runOnIdle {assertEquals(2,listens);assertEquals(0,current.value.current.attempts)}
        compose.onNodeWithTag("vocabulary-help").performScrollTo().performClick()
        compose.onNodeWithTag("answer-${q.correct.value}").assertTextContains(VocabularyContent.item(q.correct).text.display.en)
        compose.onNodeWithTag("answer-${q.choices.first {it!=q.correct}.value}").performScrollTo().performClick()
        compose.onNodeWithTag("vocabulary-retry").performScrollTo().performClick()
        repeat(5) {
            val correct=current.value.task.question.correct.value
            compose.onNodeWithTag("answer-$correct").performScrollTo().performClick()
            compose.onNodeWithTag("answer-$correct").assertIsNotEnabled()
            compose.onNodeWithTag("vocabulary-next").performScrollTo().performClick()
        }
        compose.onNodeWithTag("vocabulary-again").performScrollTo().assertIsDisplayed().performClick()
        compose.onNodeWithTag("vocabulary-home").performScrollTo().assertIsDisplayed().performClick()
        compose.runOnIdle {assertEquals(5,current.value.score);assertEquals(1,again);assertEquals(1,home);assertTrue(current.value.progress.first().support.hint)}
    }
    @Test fun germanPictureToWordTenLargeFontCompletionIsReachable() {
        show(ContentLanguage.GERMAN,VocabularyPhase.NAME,RoundLength.TEN,true)
        compose.onNodeWithTag("word-animal.horse").performScrollTo().performClick()
        compose.onNodeWithTag("vocabulary-word").assertTextEquals("Pferd")
        compose.onNodeWithTag("vocabulary-name").performScrollTo().performClick()
        repeat(10) {
            val id=current.value.task.question.correct
            compose.onNodeWithTag("answer-${id.value}").performScrollTo().assertTextEquals(VocabularyContent.item(id).text.display.de).performClick()
            compose.onNodeWithTag("vocabulary-next").performScrollTo().performClick()
        }
        compose.onNodeWithTag("vocabulary-again").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("vocabulary-home").performScrollTo().assertIsDisplayed()
        compose.runOnIdle {assertEquals(10,current.value.score);assertEquals(SessionPhase.COMPLETED,current.value.phase)}
    }
}
