package com.bellfamily.bastischool.ui.wilma

import android.graphics.BitmapFactory
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.test.platform.app.InstrumentationRegistry
import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.session.*
import com.bellfamily.bastischool.learning.wilma.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class WilmaScreenTest {
    @get:Rule val compose=createComposeRule()
    private lateinit var selection:MutableState<WilmaSelection>
    private lateinit var quiz:MutableState<SessionState>
    private lateinit var ordering:MutableState<WilmaOrderState>
    private lateinit var language:MutableState<ContentLanguage>
    private var listens=0
    private var home=0
    private var again=0
    private fun show(phase:WilmaPhase,lang:ContentLanguage=ContentLanguage.ENGLISH,round:RoundLength=RoundLength.FIVE,large:Boolean=false) {
        val assets=InstrumentationRegistry.getInstrumentation().targetContext.assets
        val paths=WilmaContent.days.map(WilmaContent::image)+listOf(WilmaContent.HEAD,WilmaContent.TAIL,WilmaContent.REFERENCE)
        val pictures=paths.associateWith {path->assets.open(path).use {requireNotNull(BitmapFactory.decodeStream(it,null,BitmapFactory.Options().apply {inSampleSize=2})).asImageBitmap()} }
        selection=mutableStateOf(WilmaSelection(phase=phase));language=mutableStateOf(lang)
        val quizPhase=if(phase==WilmaPhase.RELATIONS)phase else WilmaPhase.FIND
        val plan=(WilmaContent.generate(quizPhase,SessionId("ui-quiz"),round,42) as GenerationResult.Generated).plan
        quiz=mutableStateOf(SessionReducer.start(plan,lang,WilmaContent.repository).state)
        ordering=mutableStateOf(WilmaOrder.start(SessionId("ui-order"),42,lang))
        compose.setContent {
            val density=LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density,if(large)1.5f else 1f)) {
                MaterialTheme {WilmaScreen(selection.value,quiz.value,ordering.value,language.value,pictures,false,false,false,false,
                    onPhase={selection.value=selection.value.copy(phase=it)},onDay={selection.value=selection.value.copy(selected=it)},onReplay={listens++},onOption={listens++},
                    onAction={quiz.value=SessionReducer.reduce(quiz.value,it).state},onOrder={ordering.value=WilmaOrder.reduce(ordering.value,it).state},
                    onAgain={again++},onRetry={},onHome={home++},modifier=Modifier.width(if(large)320.dp else 700.dp))}
            }
        }
    }
    @Test fun sevenDayTargetsSelectBilingualNamesAndHeadIsNotADay() {
        show(WilmaPhase.EXPLORE)
        compose.onNodeWithTag("wilma-head-day").assert(hasNoClickAction())
        compose.onNodeWithTag("completion-celebration").assertDoesNotExist()
        WilmaContent.days.forEach {id ->
            compose.onNodeWithTag("day-${id.value}").performScrollTo().performClick()
            compose.onNodeWithTag("wilma-selected").assertTextEquals(WilmaContent.day(id).text.display.en)
        }
        compose.runOnIdle {language.value=ContentLanguage.GERMAN}
        compose.onNodeWithTag("wilma-selected").assertTextEquals(WilmaContent.day(WilmaContent.days.last()).text.display.de)
        compose.onNodeWithTag("wilma-replay").performScrollTo().performClick()
        compose.runOnIdle {assertEquals(1,listens);assertEquals(WilmaContent.days.last(),selection.value.selected)}
    }
    @Test fun findDayFiveRoundSeparatesListenHelpRetryAndCompletion() {
        show(WilmaPhase.FIND)
        val q=quiz.value.task.question
        compose.onNodeWithTag("wilma-speaker-${q.correct.value}").performScrollTo().performClick()
        compose.onNodeWithTag("wilma-replay").performScrollTo().performClick()
        compose.runOnIdle {assertEquals(2,listens);assertEquals(0,quiz.value.current.attempts)}
        compose.onNodeWithTag("wilma-help").performScrollTo().performClick()
        compose.onNodeWithTag("day-${q.choices.first {it!=q.correct}.value}").performScrollTo().performClick()
        compose.onNodeWithTag("wilma-retry").performScrollTo().performClick()
        repeat(5) {
            compose.onNodeWithTag("day-${quiz.value.task.question.correct.value}").performScrollTo().performClick()
            compose.onNodeWithTag("day-${quiz.value.task.question.correct.value}").assertIsNotEnabled()
            compose.onNodeWithTag("wilma-next").performScrollTo().performClick()
        }
        compose.onNodeWithTag("completion-celebration").assertExists()
        compose.onNodeWithTag("wilma-again").assertIsDisplayed().performClick()
        compose.onNodeWithTag("wilma-home").assertIsDisplayed().performClick()
        compose.runOnIdle {assertEquals(5,quiz.value.score);assertEquals(1,again);assertEquals(1,home)}
    }
    @Test fun germanBeforeAfterTenRoundLargeTextCompletes() {
        show(WilmaPhase.RELATIONS,ContentLanguage.GERMAN,RoundLength.TEN,true)
        repeat(10) {
            compose.onNodeWithTag("day-${quiz.value.task.question.correct.value}").performScrollTo().performClick()
            compose.onNodeWithTag("wilma-next").performScrollTo().performClick()
        }
        compose.onNodeWithTag("wilma-complete").assertExists()
        compose.onNodeWithTag("completion-celebration").assertExists()
        compose.onNodeWithTag("wilma-again").assertIsDisplayed()
        compose.onNodeWithTag("wilma-home").assertIsDisplayed()
    }
    @Test fun germanOrderingLargeTextPreservesPlacedDaysAfterMistakeAndCompletes() {
        show(WilmaPhase.ORDER,ContentLanguage.GERMAN,large=true)
        compose.onNodeWithTag("order-${WilmaContent.days[2].value}").performScrollTo().performClick()
        compose.onNodeWithTag("wilma-retry").performScrollTo().performClick()
        compose.onNodeWithTag("wilma-help").performScrollTo().performClick()
        WilmaContent.days.forEach {id ->
            compose.onNodeWithTag("order-${id.value}").performScrollTo().performClick()
            compose.onNodeWithTag("order-${id.value}").assertDoesNotExist()
            compose.onNodeWithTag("placed-${id.value}").assertExists()
        }
        compose.onNodeWithTag("wilma-placed-count").assertTextEquals("7 von 7 Tagen eingeordnet")
        compose.onNodeWithTag("completion-celebration").assertExists()
        compose.onNodeWithTag("wilma-again").assertIsDisplayed()
        compose.onNodeWithTag("wilma-home").assertIsDisplayed()
        compose.runOnIdle {assertTrue(ordering.value.completed);assertEquals(2,ordering.value.steps.first().attempts)}
    }
}
