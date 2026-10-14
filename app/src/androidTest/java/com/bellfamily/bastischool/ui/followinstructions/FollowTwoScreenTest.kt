package com.bellfamily.bastischool.ui.followinstructions

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.input.InputMode
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInputModeManager
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.*
import com.bellfamily.bastischool.learning.follow.*
import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.session.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class FollowTwoScreenTest(private val german: Boolean, private val fontScale: Float, private val orientation: Int) {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
    companion object {
        @JvmStatic @Parameterized.Parameters(name = "follow-two-de={0},font={1},orientation={2}")
        fun cases() = listOf(false, true).flatMap { de -> listOf(1f, 1.5f, 2f).flatMap { font ->
            listOf(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT, ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE).map { arrayOf<Any>(de, font, it) }
        } }
    }

    @OptIn(ExperimentalTestApi::class, androidx.compose.ui.ExperimentalComposeUiApi::class)
    @Test fun orderedTapRetryReplayHintKeyboardSemanticsAndWrapping() {
        val landscape = orientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        compose.activity.requestedOrientation = orientation
        compose.waitUntil(10_000) { compose.activity.resources.configuration.orientation ==
            if (landscape) Configuration.ORIENTATION_LANDSCAPE else Configuration.ORIENTATION_PORTRAIT }
        val language = if (german) ContentLanguage.GERMAN else ContentLanguage.ENGLISH
        var state by mutableStateOf(FollowTwo.generate(SessionId("two-screen"), RoundLength.FIVE, 7, language))
        val images = FollowContent.objects.associate { it.id to ImageBitmap(32, 32) }
        var replays = 0; var examples = 0
        lateinit var input: androidx.compose.ui.input.InputModeManager
        compose.setContent {
            input = LocalInputModeManager.current
            CompositionLocalProvider(LocalDensity provides Density(LocalDensity.current.density, fontScale)) {
                MaterialTheme {
                    FollowTwoScreen(state, language, images, false, false, false,
                        { state = FollowTwo.reduce(state, it).state }, { replays++ }, { examples++ }, {}, {}, {},
                        Modifier.size(if (landscape) 700.dp else 280.dp, if (landscape) 240.dp else 600.dp))
                }
            }
        }
        fun click(tag: String) = compose.onNodeWithTag(tag).performScrollTo().assertIsDisplayed().performClick()
        val instruction = compose.onNodeWithTag("follow-two-instruction").performScrollTo()
            .assertTextEquals(FollowTwo.instruction(state.task).display[language])
        val layouts = mutableListOf<TextLayoutResult>()
        instruction.performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(layouts) }
        val text = layouts.single()
        assertFalse(text.multiParagraph.didExceedMaxLines)
        repeat(text.lineCount) { line ->
            assertFalse(text.isLineEllipsized(line))
            assertTrue(text.getLineLeft(line) >= -1f)
            assertTrue(text.getLineRight(line) <= text.size.width + 1f)
        }
        compose.onNodeWithTag("follow-two-example").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("follow-two-demo-object.follow.dinosaur").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("follow-two-demo-object.follow.horse").performScrollTo().assertIsDisplayed()
        click("follow-two-demo-listen"); click("follow-two-replay"); click("follow-two-hint")
        assertEquals(1, examples); assertEquals(1, replays)
        compose.onNodeWithTag("follow-two-hint-message").performScrollTo().assertIsDisplayed()
        state.task.choices.forEach { id ->
            compose.onNodeWithTag("follow-two-tap-${id.value}").performScrollTo().assertHeightIsAtLeast(150.dp)
                .assertContentDescriptionContains(FollowContent.objectFor(id).text.display[language])
        }
        val first = state.task.first; val second = state.task.second; val taskId = state.taskId
        click("follow-two-tap-${second.value}")
        compose.runOnIdle { assertEquals(AnswerState.RETRY_AVAILABLE, state.current.answer); assertEquals(0, state.step) }
        click("follow-two-retry")
        assertTrue(input.requestInputMode(InputMode.Keyboard))
        val firstNode = compose.onNodeWithTag("follow-two-tap-${first.value}").performScrollTo()
        firstNode.performSemanticsAction(SemanticsActions.RequestFocus) { it() }
        firstNode.performKeyInput { pressKey(androidx.compose.ui.input.key.Key.Enter) }
        compose.runOnIdle { assertEquals(1, state.step); assertFalse(state.readyToContinue); assertEquals(taskId, state.taskId) }
        compose.onNodeWithTag("follow-two-tap-${first.value}").performScrollTo()
            .assertContentDescriptionEquals(FollowContent.objectFor(first).text.display[language] +
                if (german) ", erster Schritt geschafft" else ", first step done")
        compose.onNodeWithTag("follow-two-next").assertDoesNotExist()
        click("follow-two-replay")
        assertEquals(2, replays)
        compose.onNodeWithTag("follow-two-instruction").performScrollTo()
            .assertTextEquals(FollowTwo.instruction(state.task).display[language])
        click("follow-two-tap-${second.value}")
        compose.runOnIdle { assertTrue(state.readyToContinue) }
        click("follow-two-next")
        compose.runOnIdle { assertEquals(1, state.questionIndex) }
        compose.onNodeWithTag("follow-two-progress").performScrollTo()
            .assertTextEquals(if (german) "Frage 2 von 5" else "Question 2 of 5")
    }
}
