package com.bellfamily.bastischool.ui.common

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Bitmap
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.Color
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
import com.bellfamily.bastischool.learning.prepositions.*
import com.bellfamily.bastischool.learning.session.*
import com.bellfamily.bastischool.learning.vocabulary.*
import com.bellfamily.bastischool.learning.wilma.*
import com.bellfamily.bastischool.ui.prepositions.PrepositionsScreen
import com.bellfamily.bastischool.ui.vocabulary.VocabularyScreen
import com.bellfamily.bastischool.ui.wilma.WilmaScreen
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.io.File

/** Real screen/reducer contracts; role appearance is covered by NativeActionButtonTest. */
@RunWith(Parameterized::class)
class ActivityActionAdoptionTest(private val surface: String, private val orientation: Int) {
    companion object {
        @JvmStatic @Parameterized.Parameters(name = "{0}-orientation-{1}")
        fun cases(): List<Array<Any>> = listOf("prepositions", "vocabulary", "wilma", "wilma-order").flatMap { surface ->
            listOf(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT, ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE,
                ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE).map { arrayOf<Any>(surface, it) }
        }
    }
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
    private var busy by mutableStateOf(false)
    private var failed by mutableStateOf(false)
    private var explore by mutableStateOf(false)
    private lateinit var state: MutableState<SessionState>
    private lateinit var order: MutableState<WilmaOrderState>
    private lateinit var input: InputModeManager
    private var replay = 0
    private var reload = 0
    private var home = 0
    private var extra = 0
    private val prepositions get() = surface == "prepositions"
    private val vocabulary get() = surface == "vocabulary"
    private val ordering get() = surface == "wilma-order"
    private val prefix get() = if(prepositions) "" else if(vocabulary) "vocabulary-" else "wilma-"
    private val helpTag get() = if(prepositions) "hint" else prefix + "help"
    private val loadTag get() = if(prepositions) "retry-save" else if(vocabulary) "vocabulary-load" else "wilma-retry-save"

    private fun show() {
        val landscape = orientation != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        compose.activity.requestedOrientation = orientation
        compose.waitUntil(10_000) { compose.activity.resources.configuration.orientation ==
            if(landscape) Configuration.ORIENTATION_LANDSCAPE else Configuration.ORIENTATION_PORTRAIT }
        val id = SessionId("action-adoption")
        val generated = when {
            prepositions -> PrepositionsContent.generate(id, RoundLength.FIVE, 42)
            vocabulary -> VocabularyContent.generate(VocabularyPhase.FIND, id, RoundLength.FIVE, 42)
            else -> WilmaContent.generate(WilmaPhase.FIND, id, RoundLength.FIVE, 42)
        } as GenerationResult.Generated
        val repository = when { prepositions -> PrepositionsContent.repository; vocabulary -> VocabularyContent.repository; else -> WilmaContent.repository }
        state = mutableStateOf(SessionReducer.start(generated.plan, ContentLanguage.GERMAN, repository).state)
        order = mutableStateOf(WilmaOrder.start(id, 42, ContentLanguage.GERMAN))
        compose.setContent {
            input = LocalInputModeManager.current
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, 1.5f)) {
                MaterialTheme(colorScheme = lightColorScheme(primary = Color(0xFF2E7D5B), secondary = Color(0xFFE59D28))) {
                    val modifier = Modifier.size(if(landscape) 700.dp else 320.dp, if(landscape) 240.dp else 480.dp).testTag("action-review")
                    val action: (SessionAction) -> Unit = {
                        if(it is SessionAction.Replay) replay++
                        state.value = SessionReducer.reduce(state.value, it).state
                    }
                    when {
                        prepositions -> PrepositionsScreen(state.value, ContentLanguage.GERMAN, busy, failed, false,
                            action, {}, {reload++}, {}, {extra++}, {home++}, {extra++}, modifier)
                        vocabulary -> VocabularyScreen(VocabularySelection(phase = if(explore) VocabularyPhase.EXPLORE else VocabularyPhase.FIND),
                            state.value, ContentLanguage.GERMAN, busy, failed, false, {}, {}, {replay++}, {extra++}, action, {}, {}, {reload++}, {home++}, modifier)
                        else -> WilmaScreen(WilmaSelection(phase = if(ordering) WilmaPhase.ORDER else WilmaPhase.FIND), state.value, order.value,
                            ContentLanguage.GERMAN, emptyMap(), busy, failed, false, false, {}, {}, {replay++}, {}, action,
                            {order.value = WilmaOrder.reduce(order.value, it).state}, {}, {reload++}, {home++}, modifier)
                    }
                }
            }
        }
    }

    private fun button(tag: String, label: String): SemanticsNodeInteraction = checkButton(compose.onNodeWithTag(tag), label)
    private fun checkButton(node: SemanticsNodeInteraction, label: String): SemanticsNodeInteraction {
        node.performScrollTo().assertIsDisplayed().assertTextEquals(label)
            .assertHeightIsAtLeast(56.dp).assertWidthIsAtLeast(56.dp)
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button))
        val layouts = mutableListOf<androidx.compose.ui.text.TextLayoutResult>()
        node.performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(layouts) }
        assertTrue(layouts.isNotEmpty())
        assertFalse("Label clipped: $label", layouts.any { it.hasVisualOverflow })
        return node
    }

    private fun choose(tag: String) {
        val node = compose.onNodeWithTag(tag).performScrollTo().assertIsEnabled()
        // The unmodified Wilma segment is taller than this deliberately short viewport.
        // Activate its accessible action, not a center coordinate outside the visible clip.
        // Existing WilmaScreen/colour tests retain their touch-activation coverage.
        if(surface == "wilma") node.performSemanticsAction(SemanticsActions.OnClick) { it() }
        else node.performClick()
    }

    @OptIn(ExperimentalTestApi::class, androidx.compose.ui.ExperimentalComposeUiApi::class)
    @Test fun germanLargeTextActionsKeepSemanticsCallbacksAndDisabledRules() {
        show()
        button(prefix + "replay", "Noch einmal hören").performClick()
        compose.runOnIdle { assertTrue(input.requestInputMode(InputMode.Keyboard)) }
        compose.onNodeWithTag(prefix + "replay").performSemanticsAction(SemanticsActions.RequestFocus) { it() }
        compose.onNodeWithTag(prefix + "replay").assertIsFocused().performKeyInput { pressKey(Key.Enter) }
        compose.runOnIdle { assertEquals(2, replay); assertEquals(0, state.value.current.attempts) }
        button(helpTag, "Hilfe").performClick()
        compose.runOnIdle { assertTrue(if(ordering) order.value.current.support.hint else state.value.current.support.hint) }
        val question = state.value.task.question
        val wrong = if(ordering) WilmaContent.days.last() else question.choices.first { it != question.correct }
        val answerPrefix = if(ordering) "order-" else if(prepositions || vocabulary) "answer-" else "day-"
        choose(answerPrefix + wrong.value)
        compose.runOnIdle { busy = true }
        button(prefix + "retry", "Nochmal versuchen").assertIsNotEnabled().performTouchInput { click() }
        compose.runOnIdle { assertEquals(AnswerState.RETRY_AVAILABLE, if(ordering) order.value.current.answer else state.value.current.answer); busy = false }
        button(prefix + "retry", "Nochmal versuchen").performClick()
        compose.runOnIdle { assertEquals(AnswerState.UNANSWERED, if(ordering) order.value.current.answer else state.value.current.answer) }
        val correct = if(ordering) WilmaContent.days.first() else question.correct
        choose(answerPrefix + correct.value)
        compose.runOnIdle { if(!ordering) assertEquals("Correct day/item activation: ${state.value.current}", AnswerState.CORRECT, state.value.current.answer) }
        if(!ordering) {
            compose.runOnIdle { busy = true }
            button(prefix + "next", "Weiter").assertIsNotEnabled().performTouchInput { click() }
            compose.runOnIdle { assertEquals(0, state.value.index); busy = false }
            button(prefix + "next", "Weiter").assertIsEnabled()
        } else compose.runOnIdle { assertEquals(listOf(correct), order.value.placed) }
        button(prefix + "home", "Zurück zum Start").performClick()
        val image = compose.onNodeWithTag("action-review").captureToImage().asAndroidBitmap()
        File(compose.activity.cacheDir, "actions-$surface-$orientation.png").outputStream().use { image.compress(Bitmap.CompressFormat.PNG, 100, it) }
        if(!ordering) {
            button(prefix + "next", "Weiter").performClick()
            compose.runOnIdle { assertEquals(1, state.value.index); assertEquals(1, state.value.score) }
        }
        compose.runOnIdle { failed = true }
        val retryLabel = if(prepositions) "Speichern erneut versuchen" else "Erneut versuchen"
        button(loadTag, retryLabel).assertIsEnabled().performClick()
        button(prefix + "replay", "Noch einmal hören").assertIsNotEnabled().performTouchInput { click() }
        button(helpTag, "Hilfe").assertIsNotEnabled()
        button(prefix + "home", "Zurück zum Start").assertIsEnabled()
        compose.runOnIdle { busy = true }
        button(loadTag, retryLabel).assertIsNotEnabled().performTouchInput { click() }
        compose.runOnIdle { assertEquals(1, reload); assertEquals(1, home); assertEquals(2, replay); busy = false; failed = false }
        if(prepositions) {
            checkButton(compose.onNodeWithText("So geht’s"), "So geht’s").performClick()
            button("legacy", "Bisherige Version öffnen").performClick()
            compose.runOnIdle { assertEquals(2, extra) }
        } else if(vocabulary) {
            compose.runOnIdle { explore = true }
            button("vocabulary-example", "Satz anhören").performClick()
            button("vocabulary-replay", "Noch einmal hören").performClick()
            compose.runOnIdle { assertEquals(1, extra); assertEquals(3, replay); busy = true }
            button("vocabulary-example", "Satz anhören").assertIsNotEnabled()
            button("vocabulary-replay", "Noch einmal hören").assertIsNotEnabled()
        }
    }
}
