package com.bellfamily.bastischool.ui.seasons

import android.graphics.BitmapFactory
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.width
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import com.bellfamily.bastischool.learning.content.SeasonIds
import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.seasons.*
import com.bellfamily.bastischool.learning.session.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class SeasonsScreenTest {
    @get:Rule val compose = createComposeRule()
    private lateinit var current: MutableState<SessionState>
    private lateinit var selected: MutableState<SeasonsSelection>
    private var listens=0
    private var home=0
    private var again=0
    private fun show(lang:ContentLanguage,round:RoundLength=RoundLength.FIVE,large:Boolean=false) {
        val assets=InstrumentationRegistry.getInstrumentation().targetContext.assets
        val pictures=SeasonIds.canonicalOrder.associateWith {id -> assets.open(SeasonsContent.image(id).path).use {BitmapFactory.decodeStream(it).asImageBitmap()} }
        val plan=(SeasonsContent.generate(SessionId("ui-seasons"),round,42) as GenerationResult.Generated).plan
        current=mutableStateOf(SessionReducer.start(plan,lang,SeasonsContent.repository).state)
        selected=mutableStateOf(SeasonsSelection())
        compose.setContent {
            val density=LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density,if(large)1.5f else 1f)) {
                MaterialTheme {
                    val id=if(selected.value.phase==SeasonsPhase.EXPLORE)selected.value.selected else current.value.task.question.correct
                    SeasonsScreen(selected.value,current.value,lang,pictures[id],false,false,false,false,
                        onSelect={selected.value=selected.value.select(it)},onPhase={selected.value=selected.value.copy(phase=it)},
                        onReplay={listens++},onAction={current.value=SessionReducer.reduce(current.value,it).state},
                        onOption={listens++},onAgain={again++},onRetry={},onHome={home++},
                        modifier=Modifier.width(if(large)320.dp else 700.dp))
                }
            }
        }
    }
    @Test fun exploreUsesAllCanonicalNamesDescriptionsAndUncroppedArtwork() {
        show(ContentLanguage.ENGLISH)
        compose.onNodeWithTag("completion-celebration").assertDoesNotExist()
        SeasonIds.canonicalOrder.forEach {id ->
            compose.onNodeWithTag("season-${id.value}").performScrollTo().performClick()
            compose.onNodeWithTag("season-name").assertTextEquals(SeasonsContent.season(id).text.display.en)
            compose.onNodeWithTag("season-description").performScrollTo().assertTextEquals(SeasonsContent.season(id).spokenDescription.en)
            val bounds=compose.onNodeWithTag("season-artwork").performScrollTo().fetchSemanticsNode().boundsInRoot
            assertEquals(4f/3f,bounds.width/bounds.height,0.02f)
            compose.onNodeWithTag("seasons-replay").performScrollTo().performClick()
        }
        compose.runOnIdle {assertEquals(4,listens);assertEquals(0,current.value.current.attempts)}
    }
    @Test fun englishFiveQuestionRoundSeparatesSpeakersAnswersRetryAndCompletion() {
        show(ContentLanguage.ENGLISH)
        compose.onNodeWithTag("practice").performScrollTo().performClick()
        val q=current.value.task.question
        compose.onNodeWithTag("speaker-${q.correct.value}").performScrollTo().performClick()
        compose.onNodeWithTag("seasons-replay").performScrollTo().performClick()
        compose.runOnIdle {assertEquals(2,listens);assertEquals(0,current.value.current.attempts)}
        compose.onNodeWithTag("seasons-hint").performScrollTo().performClick()
        compose.onNodeWithTag("answer-${q.choices.first {it!=q.correct}.value}").performScrollTo().performClick()
        compose.onNodeWithTag("seasons-retry").performScrollTo().performClick()
        repeat(5) {
            val correct=current.value.task.question.correct.value
            compose.onNodeWithTag("answer-$correct").performScrollTo().performClick()
            compose.onNodeWithTag("answer-$correct").assertIsNotEnabled()
            compose.onNodeWithTag("seasons-next").performScrollTo().performClick()
        }
        compose.onNodeWithTag("completion-celebration").assertExists()
        compose.onNodeWithTag("seasons-again").assertIsDisplayed().performClick()
        compose.onNodeWithTag("seasons-home").assertIsDisplayed().performClick()
        compose.runOnIdle {assertEquals(5,current.value.score);assertEquals(1,again);assertEquals(1,home)}
    }
    @Test fun germanLargeFontTenQuestionCompletionIsReachable() {
        show(ContentLanguage.GERMAN,RoundLength.TEN,true)
        compose.onNodeWithTag("season-season.winter").performScrollTo().performClick()
        compose.onNodeWithTag("season-description").assertTextEquals(SeasonsContent.season(SeasonIds.WINTER).spokenDescription.de)
        compose.onNodeWithTag("practice").performScrollTo().performClick()
        repeat(10) {
            compose.onNodeWithTag("answer-${current.value.task.question.correct.value}").performScrollTo().performClick()
            compose.onNodeWithTag("seasons-next").performScrollTo().performClick()
        }
        compose.onNodeWithTag("completion-celebration").assertExists()
        compose.onNodeWithTag("seasons-again").assertIsDisplayed()
        compose.onNodeWithTag("seasons-home").assertIsDisplayed()
        compose.runOnIdle {assertEquals(10,current.value.score);assertEquals(SessionPhase.COMPLETED,current.value.phase)}
    }
}
