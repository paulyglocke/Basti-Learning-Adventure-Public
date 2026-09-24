package com.bellfamily.bastischool.ui.seasons

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.InputMode
import androidx.compose.ui.input.InputModeManager
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInputModeManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.seasons.*
import com.bellfamily.bastischool.learning.session.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import java.io.File

class SeasonsActionButtonTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
    private var busy by mutableStateOf(false)
    private var saveFailed by mutableStateOf(false)
    private var imageFailed by mutableStateOf(false)
    private var language by mutableStateOf(ContentLanguage.GERMAN)
    private var phase by mutableStateOf(SeasonsPhase.PRACTICE)
    private lateinit var quiz: MutableState<SessionState>
    private lateinit var order: MutableState<SeasonsOrderState>
    private lateinit var input: InputModeManager
    private var replay = 0
    private var retryLoad = 0
    private var home = 0

    private fun show(orientation: Int = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
        val landscape = orientation != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        compose.activity.requestedOrientation = orientation
        compose.waitUntil(10_000) { compose.activity.resources.configuration.orientation ==
            if(landscape) Configuration.ORIENTATION_LANDSCAPE else Configuration.ORIENTATION_PORTRAIT }
        val plan = (SeasonsContent.generate(SessionId("action-screen"), RoundLength.FIVE, 42) as GenerationResult.Generated).plan
        quiz = mutableStateOf(SessionReducer.start(plan, language, SeasonsContent.repository).state)
        order = mutableStateOf(SeasonsOrder.start(SessionId("action-order"), 42, language))
        val picture = compose.activity.assets.open(SeasonsContent.image(quiz.value.task.question.correct).path).use {
            requireNotNull(BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = 4 })).asImageBitmap()
        }
        compose.setContent {
            input = LocalInputModeManager.current
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, 1.5f)) {
                MaterialTheme(colorScheme = lightColorScheme(primary = Color(0xFF2E7D5B), secondary = Color(0xFFE59D28))) {
                    SeasonsScreen(SeasonsSelection(phase = phase), quiz.value, language, picture, busy, saveFailed, imageFailed, false,
                        onSelect = {}, onPhase = { phase = it }, onReplay = { replay++ },
                        onAction = { quiz.value = SessionReducer.reduce(quiz.value, it).state }, onOption = {}, onAgain = {},
                        onRetry = { retryLoad++ }, onHome = { home++ },
                        modifier = Modifier.size(if(landscape) 700.dp else 320.dp, if(landscape) 240.dp else 480.dp).testTag("action-review"),
                        ordering = order.value, onOrder = { order.value = SeasonsOrder.reduce(order.value, it).state })
                }
            }
        }
    }

    private fun action(tag: String, label: String): SemanticsNodeInteraction {
        val node = compose.onNodeWithTag(tag).performScrollTo().assertIsDisplayed().assertTextEquals(label)
            .assertHeightIsAtLeast(56.dp).assertWidthIsAtLeast(48.dp)
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button))
        val layouts = mutableListOf<androidx.compose.ui.text.TextLayoutResult>()
        node.performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(layouts) }
        assertTrue(layouts.isNotEmpty())
        assertTrue("Clipped label: $label", layouts.none { it.hasVisualOverflow })
        return node
    }

    @OptIn(ExperimentalTestApi::class, androidx.compose.ui.ExperimentalComposeUiApi::class)
    @Test fun portraitActionsKeepCallbacksTagsSupportAndKeyboardActivation() {
        show()
        action("seasons-replay", "Noch einmal hören").performClick()
        compose.runOnIdle { assertTrue(input.requestInputMode(InputMode.Keyboard)) }
        compose.onNodeWithTag("seasons-replay").performSemanticsAction(SemanticsActions.RequestFocus) { it() }
        compose.onNodeWithTag("seasons-replay").assertIsFocused().performKeyInput { pressKey(Key.Enter) }
        compose.runOnIdle { assertEquals(2, replay); assertEquals(0, quiz.value.current.attempts) }
        action("seasons-hint", "Hilfe").performClick()
        compose.runOnIdle { assertTrue(quiz.value.current.support.hint); assertEquals(0, quiz.value.current.attempts) }
        val question = quiz.value.task.question
        compose.onNodeWithTag("answer-${question.choices.first { it != question.correct }.value}").performScrollTo().performClick()
        action("seasons-retry", "Nochmal versuchen").performClick()
        compose.runOnIdle { assertEquals(AnswerState.UNANSWERED, quiz.value.current.answer); assertEquals(1, quiz.value.current.attempts) }
        compose.onNodeWithTag("answer-${question.correct.value}").performScrollTo().performClick()
        action("seasons-next", "Weiter").performClick()
        compose.runOnIdle { assertEquals(1, quiz.value.index); assertEquals(1, quiz.value.score) }
        action("seasons-home", "Zurück zum Start").performClick()
        compose.runOnIdle { assertEquals(1, home) }
    }

    @Test fun disabledAndFailureRulesStayDistinctAcrossExploreQuizAndOrder() {
        show()
        compose.runOnIdle { saveFailed = true }
        action("retry-load", "Erneut versuchen").assertIsEnabled().performClick()
        action("seasons-replay", "Noch einmal hören").assertIsNotEnabled().performTouchInput { click() }
        action("seasons-hint", "Hilfe").assertIsNotEnabled()
        action("seasons-home", "Zurück zum Start").assertIsEnabled()
        compose.runOnIdle { busy = true }
        action("retry-load", "Erneut versuchen").assertIsNotEnabled().performTouchInput { click() }
        compose.runOnIdle { busy = false; saveFailed = false; imageFailed = true }
        action("retry-load", "Erneut versuchen").assertIsEnabled().performClick()
        action("seasons-replay", "Noch einmal hören").assertIsEnabled()
        compose.onNodeWithTag("answer-${quiz.value.task.question.correct.value}").performScrollTo().assertIsNotEnabled()
        compose.runOnIdle { imageFailed = false; language = ContentLanguage.ENGLISH }
        compose.onNodeWithTag("seasons-hint").performScrollTo().assertIsNotEnabled()
        compose.onNodeWithTag("seasons-replay").performScrollTo().assertIsNotEnabled()
        compose.runOnIdle { phase = SeasonsPhase.EXPLORE }
        compose.onNodeWithTag("seasons-replay").performScrollTo().assertIsEnabled()
        compose.runOnIdle { phase = SeasonsPhase.ORDER }
        compose.onNodeWithTag("seasons-replay").performScrollTo().assertIsNotEnabled()
        compose.runOnIdle { language = ContentLanguage.GERMAN }
        compose.onNodeWithTag("season-order-season.winter").performScrollTo().performClick()
        action("seasons-retry", "Nochmal versuchen").assertIsEnabled()
        compose.runOnIdle { busy = true }
        action("seasons-retry", "Nochmal versuchen").assertIsNotEnabled()
        action("seasons-hint", "Hilfe").assertIsNotEnabled()
        compose.runOnIdle { busy = false }
        action("seasons-hint", "Hilfe").performClick()
        action("seasons-retry", "Nochmal versuchen").performClick()
        compose.runOnIdle { assertEquals(AnswerState.UNANSWERED, order.value.current.answer); assertTrue(order.value.current.support.hint)
            phase = SeasonsPhase.PRACTICE
            quiz.value = SessionReducer.reduce(quiz.value, SessionAction.Answer(quiz.value.nextAttempt!!, quiz.value.task.question.correct)).state
            busy = true
        }
        action("seasons-next", "Weiter").assertIsNotEnabled().performTouchInput { click() }
        compose.runOnIdle { assertEquals(0, quiz.value.index); assertEquals(0, replay); assertEquals(2, retryLoad) }
    }

    private fun landscape(orientation: Int, suffix: String) {
        show(orientation)
        action("seasons-replay", "Noch einmal hören").performClick()
        action("seasons-hint", "Hilfe").performClick()
        val question = quiz.value.task.question
        compose.onNodeWithTag("answer-${question.choices.first { it != question.correct }.value}").performScrollTo().performClick()
        action("seasons-retry", "Nochmal versuchen").performClick()
        compose.onNodeWithTag("answer-${question.correct.value}").performScrollTo().performClick()
        action("seasons-next", "Weiter").assertIsEnabled()
        action("seasons-home", "Zurück zum Start").assertIsEnabled()
        val image = compose.onNodeWithTag("action-review").captureToImage().asAndroidBitmap()
        File(compose.activity.cacheDir, "seasons-actions-$suffix.png").outputStream().use { image.compress(Bitmap.CompressFormat.PNG, 100, it) }
        action("seasons-next", "Weiter").performClick()
        compose.runOnIdle { assertEquals(1, quiz.value.index); assertEquals(1, replay) }
    }
    @Test fun shortLandscapeLargeGermanActionsRemainReachable() = landscape(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE, "landscape")
    @Test fun reverseLandscapeLargeGermanActionsRemainReachable() = landscape(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE, "reverse")
}
