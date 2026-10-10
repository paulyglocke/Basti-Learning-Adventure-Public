package com.bellfamily.bastischool.ui.tellme

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.view.Surface
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.input.InputMode
import androidx.compose.ui.input.InputModeManager
import androidx.compose.ui.platform.LocalInputModeManager
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.bellfamily.bastischool.learning.models.ContentLanguage
import com.bellfamily.bastischool.learning.tellme.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class TellMeScreenTest(private val german: Boolean, private val orientation: Int) {
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
    @Test fun curatedPromptKeepsItsMatchingHelpAndModelAcrossLanguageChanges() {
        val flow = TellMeFlow()
        var state by mutableStateOf(TellMeState(com.bellfamily.bastischool.learning.scenedescription.SceneCategoryId("woodland_forest"), 3, questionIndex = 1))
        var language by mutableStateOf(if (german) ContentLanguage.GERMAN else ContentLanguage.ENGLISH)
        compose.setContent {
            MaterialTheme {
                TellMeScreen(flow, state, language, ImageBitmap(40, 30), false,
                    {}, { id, stage -> state = flow.advance(state, id, stage) },
                    { state = flow.help(state) }, {}, {}, {}, {})
            }
        }
        fun verifyPrompt() {
            val de = language == ContentLanguage.GERMAN
            compose.onNodeWithTag("tellme-prompt").performScrollTo().assertTextEquals(if (de) "Was macht der Fuchs?" else "What is the fox doing?")
        }
        verifyPrompt()
        compose.onNodeWithTag("tellme-help").performScrollTo().performClick()
        compose.runOnIdle { language = if (language == ContentLanguage.GERMAN) ContentLanguage.ENGLISH else ContentLanguage.GERMAN }
        verifyPrompt()
        val de = language == ContentLanguage.GERMAN
        compose.onNodeWithTag("tellme-starter").performScrollTo().assertTextEquals(if (de) "Der Fuchs …" else "The fox is…")
        compose.onNodeWithTag("tellme-model").performScrollTo().assertTextEquals(if (de) "Der Fuchs schnuppert beim Baumstamm." else "The fox is sniffing near the log.")
        compose.runOnIdle { assertEquals(1, state.questionIndex); assertEquals(3, state.index) }
        compose.onNodeWithTag("tellme-continue").performScrollTo().assertTextEquals(if (de) "Nächstes Bild" else "Next picture").performClick()
        compose.runOnIdle { assertEquals(4, state.index); assertFalse(state.help) }
        compose.onNodeWithTag("tellme-model").assertDoesNotExist()
    }
    @OptIn(ExperimentalTestApi::class, androidx.compose.ui.ExperimentalComposeUiApi::class)
    @Test fun conversationRemainsOpenEndedScrollableAndChildControlled() {
        val landscape = orientation != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        compose.activity.requestedOrientation = orientation
        compose.waitUntil(10_000) { compose.activity.resources.configuration.orientation ==
            if (landscape) Configuration.ORIENTATION_LANDSCAPE else Configuration.ORIENTATION_PORTRAIT }
        val rotation = if (!landscape) Surface.ROTATION_0 else if (orientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) Surface.ROTATION_90 else Surface.ROTATION_270
        compose.waitUntil(10_000) { compose.activity.window.decorView.display?.rotation == rotation }
        val flow = TellMeFlow()
        var state by mutableStateOf(TellMeState())
        var artwork by mutableStateOf<ImageBitmap?>(ImageBitmap(40, 30))
        var home = 0
        val pops = mutableListOf<String>()
        val language = if (german) ContentLanguage.GERMAN else ContentLanguage.ENGLISH
        lateinit var input: InputModeManager
        compose.setContent {
            input = LocalInputModeManager.current
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, 1.5f)) {
                MaterialTheme {
                    TellMeScreen(flow, state, language, artwork, false,
                        { state = flow.start(it) }, { id, stage -> state = flow.advance(state, id, stage) },
                        { state = flow.help(state) }, { state = flow.grownUps(state) },
                        { state = flow.again(state) }, { state = flow.home() }, { home++; state = flow.home() },
                        Modifier.size(if (landscape) 700.dp else 280.dp, if (landscape) 240.dp else 480.dp),
                        onPop = { pops += it })
                }
            }
        }
        fun click(tag: String) = compose.onNodeWithTag(tag).performScrollTo().assertIsDisplayed().performClick()
        compose.onNodeWithTag("tellme-landing").assertTextEquals(if (german) "Wähle ein Abenteuer und erzähle, was du sehen kannst." else "Choose an adventure and tell me what you can see.")
        flow.categories.forEach { category -> compose.onNodeWithTag("tellme-category-${category.id.value}")
            .performScrollTo().assertTextEquals(category.display[language]).assertHeightIsAtLeast(56.dp) }
        click("tellme-category-${flow.categories.first().id.value}")
        compose.onNodeWithTag("completion-celebration").assertDoesNotExist()
        val first = flow.scene(state)!!
        val support = flow.support(first, language)
        compose.onNodeWithTag("tellme-artwork").performScrollTo().assertContentDescriptionEquals(first.title[language]!!)
        compose.onNodeWithTag("tellme-prompt").performScrollTo().assertTextEquals(support.prompt!!)
        val imageBounds = compose.onNodeWithTag("tellme-artwork").getUnclippedBoundsInRoot()
        val promptBounds = compose.onNodeWithTag("tellme-prompt").getUnclippedBoundsInRoot()
        // The dp cap rounds to a physical pixel at non-integral emulator densities.
        assertTrue(imageBounds.bottom - imageBounds.top <= (if (landscape) 208.dp else 420.dp) + (1f / compose.activity.resources.displayMetrics.density).dp)
        assertTrue(imageBounds.bottom <= promptBounds.top)
        compose.onNodeWithTag("tellme-starter").assertDoesNotExist()
        compose.onNodeWithTag("tellme-model").assertDoesNotExist()
        click("tellme-help")
        val starter = compose.onNodeWithTag("tellme-starter").performScrollTo().assertTextEquals(support.starter!!)
            .assert(SemanticsMatcher.keyNotDefined(SemanticsActions.OnClick))
            .assert(SemanticsMatcher.keyNotDefined(SemanticsProperties.ContentDescription))
        val layouts = mutableListOf<TextLayoutResult>()
        starter.performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(layouts) }
        assertFalse(layouts.single().hasVisualOverflow)
        compose.onNodeWithTag("tellme-words").performScrollTo().assertTextEquals(support.words.joinToString("\n"))
        click("tellme-grownups")
        compose.onNodeWithTag("tellme-adult").performScrollTo().assertExists()
        compose.runOnIdle { assertEquals(0, state.index); assertEquals(TellMeStage.TALK, state.stage) }
        compose.onNodeWithTag("tellme-model").performScrollTo().assertTextEquals(support.model!!)
        compose.onNodeWithText(if (german) "Du könntest sagen:" else "You could say:").assertExists()
        compose.onNodeWithTag("completion-celebration").assertDoesNotExist()
        compose.runOnIdle { assertEquals(first.id, flow.scene(state)!!.id); assertTrue(state.help); assertTrue(state.grownUps) }
        val continueButton = compose.onNodeWithTag("tellme-continue").performScrollTo().assertTextEquals(if (german) "Nächstes Bild" else "Next picture").assertHeightIsAtLeast(56.dp)
        compose.runOnIdle { assertTrue(input.requestInputMode(InputMode.Keyboard)) }
        continueButton.performSemanticsAction(SemanticsActions.RequestFocus) { it() }
        continueButton.assertIsFocused().performKeyInput { pressKey(Key.Enter) }
        compose.runOnIdle { assertEquals(1, state.index); assertFalse(state.help); assertFalse(state.grownUps) }
        compose.onNodeWithTag("tellme-model").assertDoesNotExist()
        compose.onNodeWithTag("tellme-progress").assertTextEquals(if (german) "2 von 9" else "2 of 9")
        compose.onNodeWithTag("tellme-starter").assertDoesNotExist()
        compose.onNodeWithTag("tellme-adult").assertDoesNotExist()
        // A failed image is passive: it cannot advance a conversation or suppress the explicit actions.
        compose.runOnIdle { artwork = null }
        compose.onNodeWithTag("tellme-image-unavailable").performScrollTo().assertIsDisplayed()
        compose.runOnIdle { assertEquals(1, state.index); assertEquals(TellMeStage.TALK, state.stage) }
        repeat(8) {
            compose.onNodeWithTag("completion-celebration").assertDoesNotExist()
            compose.onNodeWithTag("tellme-model").assertDoesNotExist()
            click("tellme-help")
            val currentSupport = flow.support(flow.scene(state)!!, language, state.questionIndex)
            compose.onNodeWithTag("tellme-model").performScrollTo().assertTextEquals(currentSupport.model!!)
            compose.runOnIdle { assertEquals(TellMeStage.TALK, state.stage) }
            click("tellme-continue")
        }
        compose.onNodeWithTag("tellme-completion").assertTextEquals(if (german) "Toll erzählt!" else "Great talking!")
        compose.onNodeWithTag("tellme-home").performScrollTo().assertIsDisplayed()
        click("balloon-0")
        compose.onNodeWithTag("balloon-0").assertHasNoClickAction()
        compose.onNodeWithTag("balloon-1").assertHasClickAction()
        compose.runOnIdle {
            assertEquals(listOf(state.celebrationId), pops)
            assertEquals(TellMeStage.COMPLETE, state.stage)
        }
        click("tellme-again")
        compose.onNodeWithTag("completion-celebration").assertDoesNotExist()
        compose.runOnIdle { assertEquals(TellMeState(first.categoryId), state) }
        // Reach completion again without manufacturing a success/failure event.
        compose.runOnIdle { repeat(9) { state = flow.advance(state, flow.scene(state)!!.id, state.stage) } }
        click("tellme-categories")
        compose.onNodeWithTag("tellme-landing").assertIsDisplayed()
        click("tellme-category-${flow.categories.last().id.value}")
        click("tellme-home")
        compose.runOnIdle { assertEquals(1, home); assertEquals(TellMeState(), state) }
    }
}
