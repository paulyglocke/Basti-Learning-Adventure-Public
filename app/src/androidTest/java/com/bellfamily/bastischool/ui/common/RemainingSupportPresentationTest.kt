package com.bellfamily.bastischool.ui.common

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Bitmap
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.vocabulary.*
import com.bellfamily.bastischool.learning.wilma.*
import com.bellfamily.bastischool.learning.session.*
import com.bellfamily.bastischool.ui.vocabulary.VocabularyScreen
import com.bellfamily.bastischool.ui.wilma.WilmaScreen
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.io.File

@RunWith(Parameterized::class)
class RemainingSupportPresentationTest(private val surface: String, private val orientation: Int, private val language: ContentLanguage) {
    companion object {
        @JvmStatic @Parameterized.Parameters(name = "{0}-{1}-{2}")
        fun cases(): List<Array<Any>> = listOf("vocabulary", "wilma", "order").flatMap { surface ->
            listOf(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT, ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE,
                ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE).map { arrayOf<Any>(surface, it, ContentLanguage.GERMAN) } +
                listOf(arrayOf<Any>(surface, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT, ContentLanguage.ENGLISH))
        }
    }
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
    private lateinit var state: MutableState<SessionState>
    private lateinit var order: MutableState<WilmaOrderState>
    private var home = 0
    private val vocabulary get() = surface == "vocabulary"
    private val ordering get() = surface == "order"
    private val prefix get() = if(vocabulary) "vocabulary-" else "wilma-"
    private val hintTag get() = prefix + "help"
    private fun show() {
        val landscape = orientation != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        compose.activity.requestedOrientation = orientation
        compose.waitUntil(10_000) { compose.activity.resources.configuration.orientation ==
            if(landscape) Configuration.ORIENTATION_LANDSCAPE else Configuration.ORIENTATION_PORTRAIT }
        val id = SessionId("support-presentation")
        val result = (if(vocabulary) VocabularyContent.generate(VocabularyPhase.FIND, id, RoundLength.FIVE, 42)
            else WilmaContent.generate(WilmaPhase.FIND, id, RoundLength.FIVE, 42)) as GenerationResult.Generated
        state = mutableStateOf(SessionReducer.start(result.plan, language,
            if(vocabulary) VocabularyContent.repository else WilmaContent.repository).state)
        order = mutableStateOf(WilmaOrder.start(id, 42, language))
        compose.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, 1.5f)) {
                MaterialTheme {
                    val modifier = Modifier.size(if(landscape) 700.dp else 320.dp, if(landscape) 240.dp else 480.dp).testTag("support-review")
                    val action: (SessionAction) -> Unit = { state.value = SessionReducer.reduce(state.value, it).state }
                    val replay = {
                        if(ordering) order.value = WilmaOrder.reduce(order.value, WilmaOrderAction.Replay(order.value.task)).state
                        else action(SessionAction.Replay(state.value.task.id))
                    }
                    if(vocabulary) VocabularyScreen(VocabularySelection(phase = VocabularyPhase.FIND), state.value,
                        language, false, false, false, {}, {}, replay, {}, action, {}, {}, {}, {home++}, modifier)
                    else WilmaScreen(WilmaSelection(phase = if(ordering) WilmaPhase.ORDER else WilmaPhase.FIND),
                        state.value, order.value, language, emptyMap(), false, false, false, false, {}, {}, replay, {}, action,
                        {order.value = WilmaOrder.reduce(order.value, it).state}, {}, {}, {home++}, modifier)

                }
            }
        }
    }
    private fun action(tag: String) = compose.onNodeWithTag(tag).performScrollTo().assertIsDisplayed().performClick()
    private fun choose(id: ContentId) {
        val tag = (if(ordering) "order-" else if(vocabulary) "answer-" else "day-") + id.value
        val node = compose.onNodeWithTag(tag).performScrollTo().assertIsEnabled()
        // Existing tall Wilma strip may exceed the short viewport; use its accessible action.
        // Original Wilma touch/colour/follow tests continue to exercise physical tap behavior.
        if(!vocabulary && !ordering) node.performSemanticsAction(SemanticsActions.OnClick) { it() }
        else node.performClick()
    }
    private fun message(text: String) {
        val node = compose.onNode(hasText(text) and hasNoClickAction()).performScrollTo().assertIsDisplayed().assert(hasNoClickAction())
        node.assert(SemanticsMatcher.keyNotDefined(SemanticsProperties.ContentDescription))
            .assert(SemanticsMatcher.keyNotDefined(SemanticsProperties.Focused))
        val layouts = mutableListOf<androidx.compose.ui.text.TextLayoutResult>()
        node.performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(layouts) }
        assertEquals(1, layouts.size)
        assertFalse(layouts.single().hasVisualOverflow)
    }
    @Test fun authoredGuidanceTimingSupportAccountingAndActionsSurvivePresentation() {
        show()
        val q = state.value.task.question
        val wrongText = if(ordering) {
            if(language == ContentLanguage.GERMAN) "Versuche es noch einmal. Deine eingeordneten Tage bleiben hier."
            else "Try again. Your placed days stay here."
        } else q.wrongFeedback.display[language]
        val hintText = if(ordering) WilmaOrder.help(order.value).display[language] else q.hint!!.display[language]
        compose.onNodeWithText(wrongText).assertDoesNotExist()
        compose.onNodeWithText(hintText).assertDoesNotExist()
        action(prefix + "replay")
        compose.runOnIdle {
            val progress = if(ordering) order.value.current else state.value.current
            assertEquals(0, progress.attempts); assertEquals(1, progress.support.replays); assertFalse(progress.support.hint)
        }
        action(hintTag)
        message(hintText)
        if(vocabulary) compose.onNodeWithTag("answer-${q.correct.value}")
            .assertTextContains(VocabularyContent.item(q.correct).text.display[language])
        compose.onNodeWithText(wrongText).assertDoesNotExist()
        val wrong = if(ordering) order.value.choices.first { it != WilmaContent.days[order.value.index] } else q.choices.first { it != q.correct }
        choose(wrong)
        message(wrongText)
        message(hintText)
        val image = compose.onNodeWithTag("support-review").captureToImage().asAndroidBitmap()
        File(compose.activity.cacheDir, "remaining-support-$surface-$orientation-$language.png").outputStream().use { image.compress(Bitmap.CompressFormat.PNG, 100, it) }
        compose.runOnIdle {
            val progress = if(ordering) order.value.current else state.value.current
            assertEquals(1, progress.attempts); assertTrue(progress.support.hint); assertEquals(0, state.value.score)
        }
        action(prefix + "retry")
        compose.onNodeWithText(wrongText).assertDoesNotExist()
        message(hintText)
        compose.runOnIdle {
            val progress = if(ordering) order.value.current else state.value.current
            assertEquals(1, progress.attempts); assertEquals(1, progress.retries); assertTrue(progress.support.hint)
        }
        val correct = if(ordering) WilmaContent.days[order.value.index] else q.correct
        choose(correct)
        if(ordering) {
            compose.runOnIdle { assertEquals(listOf(correct), order.value.placed) }
            val nextWrong = order.value.choices.first { it !in order.value.placed && it != WilmaContent.days[order.value.index] }
            choose(nextWrong)
            message(wrongText)
            action(prefix + "retry")
            compose.runOnIdle { assertEquals(listOf(correct), order.value.placed) }
        } else {
            message(hintText) // Existing hint remains visible after supported success.
            compose.runOnIdle { assertEquals(1, state.value.score); assertTrue(state.value.current.locked) }
            action(prefix + "next")
            compose.runOnIdle { assertEquals(1, state.value.index); assertEquals(1, state.value.score) }
        }
        action(prefix + "home")
        compose.runOnIdle { assertEquals(1, home) }
    }
}
