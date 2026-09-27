package com.bellfamily.bastischool.ui.vocabulary

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Bitmap
import android.view.Surface
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.input.InputMode
import androidx.compose.ui.input.InputModeManager
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInputModeManager
import androidx.compose.ui.semantics.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.session.*
import com.bellfamily.bastischool.learning.vocabulary.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class VocabularyArtworkScreenTest(private val german: Boolean, private val orientation: Int) {
    companion object {
        @JvmStatic @Parameterized.Parameters(name = "German={0},orientation={1}")
        fun cases() = listOf(
            arrayOf<Any>(false, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT),
            arrayOf<Any>(true, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT),
            arrayOf<Any>(true, ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE),
            arrayOf<Any>(true, ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE),
        )
    }
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

    @OptIn(ExperimentalTestApi::class, androidx.compose.ui.ExperimentalComposeUiApi::class)
    @Test fun artworkInAllModesPreservesIdentitySemanticsListenRetryAndFailureState() {
        val landscape = orientation != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        compose.activity.requestedOrientation = orientation
        compose.waitUntil(10_000) { compose.activity.resources.configuration.orientation ==
            if(landscape) Configuration.ORIENTATION_LANDSCAPE else Configuration.ORIENTATION_PORTRAIT }
        val rotation = if(!landscape) Surface.ROTATION_0 else if(orientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) Surface.ROTATION_90 else Surface.ROTATION_270
        compose.waitUntil(10_000) { compose.activity.window.decorView.display?.rotation == rotation }
        val lang = if(german) ContentLanguage.GERMAN else ContentLanguage.ENGLISH
        val loader = VocabularyArtworkLoader { compose.activity.assets.open(it) }
        var images by mutableStateOf(loader.load())
        var selection by mutableStateOf(VocabularySelection())
        fun session(phase: VocabularyPhase): SessionState {
            val plan = (VocabularyContent.generate(phase, SessionId("art-ui"), RoundLength.FIVE, 42) as GenerationResult.Generated).plan
            return SessionReducer.start(plan, lang, VocabularyContent.repository).state
        }
        var state by mutableStateOf(session(VocabularyPhase.FIND))
        val heard = mutableListOf<ContentId>()
        lateinit var input: InputModeManager
        compose.setContent {
            input = LocalInputModeManager.current
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, 1.5f)) {
                MaterialTheme {
                    VocabularyScreen(selection, state, lang, false, false, false,
                        { selection = selection.copy(selected = it) },
                        { selection = selection.copy(phase = it); if(it != VocabularyPhase.EXPLORE) state = session(it) },
                        {}, {}, { state = SessionReducer.reduce(state, it).state }, { heard += it }, {}, {}, {},
                        modifier = Modifier.size(if(landscape)700.dp else 280.dp, if(landscape)240.dp else 480.dp), images = images)
                }
            }
        }
        fun click(tag: String) = compose.onNodeWithTag(tag).performScrollTo().performClick()
        VocabularyContent.items.forEach { item ->
            click("word-${item.id.value}")
            compose.onNodeWithTag("vocabulary-art-${item.id.value}", useUnmergedTree = true).performScrollTo().assertIsDisplayed()
            compose.onNodeWithContentDescription(item.text.display[lang]).assertExists()
            compose.onNodeWithText(item.visual.glyph).assertDoesNotExist()
        }
        click("vocabulary-find")
        val question = state.task.question
        question.choices.forEach { id ->
            val answer = compose.onNodeWithTag("answer-${id.value}").performScrollTo()
            answer.assertContentDescriptionEquals(VocabularyContent.item(id).text.display[lang])
                .assertHasClickAction().assertIsEnabled().assertHeightIsAtLeast(72.dp)
            // Only the parent Button is interactive; the image adds no duplicate label/action.
            compose.onNodeWithTag("vocabulary-art-${id.value}", useUnmergedTree = true)
                .assert(SemanticsMatcher.keyNotDefined(SemanticsActions.OnClick))
                .assert(SemanticsMatcher.keyNotDefined(SemanticsProperties.ContentDescription))
        }
        click("speaker-${question.correct.value}")
        compose.runOnIdle { assertEquals(listOf(question.correct), heard); assertEquals(0, state.current.attempts) }
        click("answer-${question.choices.first { it != question.correct }.value}")
        compose.runOnIdle { assertEquals(AnswerState.RETRY_AVAILABLE, state.current.answer) }
        question.choices.forEach { compose.onNodeWithTag("answer-${it.value}").assertIsNotEnabled() }
        click("vocabulary-retry")
        click("vocabulary-help")
        compose.onNodeWithTag("answer-${question.correct.value}").assertTextContains(VocabularyContent.item(question.correct).text.display[lang])
        val answer = compose.onNodeWithTag("answer-${question.correct.value}").performScrollTo()
        compose.runOnIdle { assertTrue(input.requestInputMode(InputMode.Keyboard)) }
        answer.performSemanticsAction(SemanticsActions.RequestFocus) { it() }
        answer.assertIsFocused().performKeyInput { pressKey(Key.Enter) }
        compose.runOnIdle { assertEquals(1, state.score); assertEquals(2, state.current.attempts) }
        answer.assertIsNotEnabled()
        click("speaker-${question.correct.value}")
        click("vocabulary-name")
        val id = state.task.question.correct
        compose.onNodeWithTag("vocabulary-art-${id.value}", useUnmergedTree = true).performScrollTo().assertIsDisplayed()
        java.io.File(compose.activity.cacheDir, "vocabulary-art-$german-$orientation.png").outputStream().use {
            compose.onRoot().captureToImage().asAndroidBitmap().compress(Bitmap.CompressFormat.PNG, 100, it)
        }
        val saved = SessionCheckpoint.encode(state)
        compose.runOnIdle { images = emptyMap() }
        compose.onNodeWithTag("vocabulary-art-unavailable-${id.value}", useUnmergedTree = true).performScrollTo().assertIsDisplayed()
        compose.runOnIdle { assertArrayEquals(saved, SessionCheckpoint.encode(state)) }
        // Returning artwork never submits or changes the task, either.
        compose.runOnIdle { images = loader.load() }
        compose.runOnIdle { assertArrayEquals(saved, SessionCheckpoint.encode(state)) }
        state.task.question.choices.forEach {
            compose.onNodeWithTag("answer-${it.value}").performScrollTo().assertTextEquals(VocabularyContent.item(it).text.display[lang]).assertIsEnabled()
        }
        compose.onNodeWithTag("vocabulary-home").performScrollTo().assertIsDisplayed()
    }
}
