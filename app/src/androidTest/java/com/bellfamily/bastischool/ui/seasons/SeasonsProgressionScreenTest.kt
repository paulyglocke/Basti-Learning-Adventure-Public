package com.bellfamily.bastischool.ui.seasons

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
import com.bellfamily.bastischool.learning.content.SeasonIds
import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.seasons.*
import com.bellfamily.bastischool.learning.session.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class SeasonsProgressionScreenTest {
    @get:Rule val compose=createComposeRule()
    private lateinit var quiz:MutableState<SessionState>
    private lateinit var order:MutableState<SeasonsOrderState>
    private var again=0;private var home=0
    private fun show(phase:SeasonsPhase,lang:ContentLanguage,round:RoundLength) {
        val assets=InstrumentationRegistry.getInstrumentation().targetContext.assets
        val images=SeasonIds.canonicalOrder.associateWith {id->assets.open(SeasonsContent.image(id).path).use {
            requireNotNull(BitmapFactory.decodeStream(it,null,BitmapFactory.Options().apply {inSampleSize=2})).asImageBitmap()
        }}
        val mode=if(phase.isQuiz)phase else SeasonsPhase.NEXT
        val plan=(SeasonsCycle.generate(mode,SessionId("screen"),round,42) as GenerationResult.Generated).plan
        quiz=mutableStateOf(SessionReducer.start(plan,lang,SeasonsContent.repository).state)
        order=mutableStateOf(SeasonsOrder.start(SessionId("order-screen"),42,lang))
        compose.setContent {
            val d=LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(d.density,1.5f)) {MaterialTheme {
                SeasonsScreen(SeasonsSelection(phase=phase),quiz.value,lang,images[SeasonsCycle.anchor(mode,quiz.value.task.question)],false,false,false,false,
                    {},{},onReplay={if(phase==SeasonsPhase.ORDER)order.value=SeasonsOrder.reduce(order.value,SeasonsOrderAction.Replay(order.value.task)).state
                        else quiz.value=SessionReducer.reduce(quiz.value,SessionAction.Replay(quiz.value.task.id)).state},
                    onAction={quiz.value=SessionReducer.reduce(quiz.value,it).state},onOption={},onAgain={again++},onRetry={},onHome={home++},
                    modifier=Modifier.width(320.dp),ordering=order.value,orderArtwork=images,
                    onOrder={order.value=SeasonsOrder.reduce(order.value,it).state})
            }}
        }
    }
    private fun cycle(phase:SeasonsPhase,lang:ContentLanguage,round:RoundLength) {
        show(phase,lang,round)
        compose.onNodeWithTag("completion-celebration").assertDoesNotExist()
        repeat(round.count) {
            val q=quiz.value.task.question
            compose.onNodeWithTag("season-anchor").assertTextEquals(SeasonsContent.season(SeasonsCycle.anchor(phase,q)).text.display[lang])
            compose.onNodeWithTag("seasons-prompt").assertTextEquals(q.instruction.display[lang])
            compose.onNodeWithTag("seasons-replay").performScrollTo().performClick()
            compose.runOnIdle {assertEquals(0,quiz.value.current.attempts)}
            compose.onNodeWithTag("answer-${q.correct.value}").performScrollTo().performClick().assertIsNotEnabled()
            compose.onNodeWithTag("seasons-next").performScrollTo().performClick()
        }
        completion()
        compose.runOnIdle {assertEquals(round.count,quiz.value.score)}
    }
    private fun completion() {
        compose.onNodeWithTag("completion-celebration").assertExists()
        compose.onNodeWithTag("seasons-again").assertIsDisplayed().performClick()
        compose.onNodeWithTag("seasons-home").assertIsDisplayed().performClick()
        compose.onNodeWithTag("seasons-replay").assertIsDisplayed()
        compose.runOnIdle {assertEquals(1,again);assertEquals(1,home)}
    }
    @Test fun nextEnglishFiveRoundShowsAnchorAndCompletes()=cycle(SeasonsPhase.NEXT,ContentLanguage.ENGLISH,RoundLength.FIVE)
    @Test fun beforeGermanTenRoundShowsAnchorAndCompletes()=cycle(SeasonsPhase.BEFORE,ContentLanguage.GERMAN,RoundLength.TEN)
    @Test fun buildYearRetainsPlacedCardsOnMistakeAndCelebratesWithLargeText() {
        show(SeasonsPhase.ORDER,ContentLanguage.GERMAN,RoundLength.FIVE)
        compose.onNodeWithTag("seasons-order-prompt").assertTextEquals(SeasonsOrder.introduction.display.de)
        compose.onNodeWithTag("season-order-season.spring").performScrollTo().performClick()
        compose.onNodeWithTag("season-order-season.winter").performScrollTo().performClick()
        compose.onNodeWithTag("placed-season.spring").assertExists()
        compose.onNodeWithTag("seasons-hint").performScrollTo().performClick()
        compose.onNodeWithTag("seasons-replay").performScrollTo().performClick()
        compose.onNodeWithTag("seasons-retry").performScrollTo().performClick()
        SeasonIds.canonicalOrder.drop(1).forEach {id->
            compose.onNodeWithTag("season-order-${id.value}").performScrollTo().performClick()
            compose.onNodeWithTag("placed-${id.value}").assertExists()
        }
        compose.onNodeWithTag("seasons-placed-count").assertTextEquals("4 von 4 Jahreszeiten eingeordnet")
        completion()
        compose.runOnIdle {assertTrue(order.value.completed);assertEquals(2,order.value.steps[1].attempts);assertTrue(order.value.steps[1].support.hint)}
    }
}
