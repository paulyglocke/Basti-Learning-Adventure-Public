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
import com.bellfamily.bastischool.learning.content.SeasonIds
import com.bellfamily.bastischool.learning.prepositions.*
import com.bellfamily.bastischool.learning.seasons.*
import com.bellfamily.bastischool.learning.session.*
import com.bellfamily.bastischool.ui.prepositions.PrepositionsScreen
import com.bellfamily.bastischool.ui.seasons.SeasonsScreen
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.io.File

@RunWith(Parameterized::class)
class SupportPresentationTest(private val surface: String, private val orientation: Int, private val language: ContentLanguage) {
    companion object {
        @JvmStatic @Parameterized.Parameters(name = "{0}-{1}-{2}")
        fun cases(): List<Array<Any>> = listOf("prepositions", "seasons", "order").flatMap { surface ->
            listOf(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT, ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE,
                ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE).map { arrayOf<Any>(surface, it, ContentLanguage.GERMAN) } +
                listOf(arrayOf<Any>(surface, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT, ContentLanguage.ENGLISH))
        }
    }
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
    private lateinit var state: MutableState<SessionState>
    private lateinit var order: MutableState<SeasonsOrderState>
    private var home = 0
    private val pre get() = surface == "prepositions"
    private val ordering get() = surface == "order"
    private val prefix get() = if(pre) "" else "seasons-"
    private val hintTag get() = if(pre) "hint" else "seasons-hint"
    private fun show() {
        val landscape = orientation != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        compose.activity.requestedOrientation = orientation
        compose.waitUntil(10_000) { compose.activity.resources.configuration.orientation ==
            if(landscape) Configuration.ORIENTATION_LANDSCAPE else Configuration.ORIENTATION_PORTRAIT }
        val id = SessionId("support-presentation")
        val result = (if(pre) PrepositionsContent.generate(id, RoundLength.FIVE, 42)
            else SeasonsContent.generate(id, RoundLength.FIVE, 42)) as GenerationResult.Generated
        state = mutableStateOf(SessionReducer.start(result.plan, language,
            if(pre) PrepositionsContent.repository else SeasonsContent.repository).state)
        order = mutableStateOf(SeasonsOrder.start(id, 42, language))
        compose.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, 1.5f)) {
                MaterialTheme {
                    val modifier = Modifier.size(if(landscape) 700.dp else 320.dp, if(landscape) 240.dp else 480.dp).testTag("support-review")
                    val action: (SessionAction) -> Unit = { state.value = SessionReducer.reduce(state.value, it).state }
                    if(pre) PrepositionsScreen(state.value, language, false, false, false, action, {}, {}, {}, {}, {home++}, {}, modifier)
                    else SeasonsScreen(SeasonsSelection(phase = if(ordering) SeasonsPhase.ORDER else SeasonsPhase.PRACTICE),
                        state.value, language, null, false, false, false, false, {}, {},
                        { if(ordering) order.value = SeasonsOrder.reduce(order.value, SeasonsOrderAction.Replay(order.value.task)).state
                          else action(SessionAction.Replay(state.value.task.id)) }, action, {}, {}, {}, {home++}, modifier,
                        ordering = order.value, onOrder = {order.value = SeasonsOrder.reduce(order.value, it).state})
                }
            }
        }
    }
    private fun action(tag: String) = compose.onNodeWithTag(tag).performScrollTo().assertIsDisplayed().performClick()
    private fun message(text: String) {
        val node = compose.onNodeWithText(text).performScrollTo().assertIsDisplayed().assert(hasNoClickAction())
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
            if(language == ContentLanguage.GERMAN) "Versuche es noch einmal. Die eingeordneten Jahreszeiten bleiben hier."
            else "Try again. The seasons already placed stay here."
        } else q.wrongFeedback.display[language]
        val hintText = if(ordering) SeasonsOrder.help(order.value).display[language] else q.hint!!.display[language]
        compose.onNodeWithText(wrongText).assertDoesNotExist()
        compose.onNodeWithText(hintText).assertDoesNotExist()
        action(prefix + "replay")
        compose.runOnIdle {
            val progress = if(ordering) order.value.current else state.value.current
            assertEquals(0, progress.attempts); assertEquals(1, progress.support.replays); assertFalse(progress.support.hint)
        }
        action(hintTag)
        message(hintText)
        compose.onNodeWithText(wrongText).assertDoesNotExist()
        if(pre) compose.onNodeWithTag("hint-text").assertTextEquals(hintText)
        val wrong = if(ordering) order.value.choices.first { it != SeasonIds.canonicalOrder[order.value.index] } else q.choices.first { it != q.correct }
        action((if(ordering) "season-order-" else "answer-") + wrong.value)
        message(wrongText)
        if(pre) compose.onNodeWithTag("feedback").assertTextEquals(wrongText)
        message(hintText)
        val image = compose.onNodeWithTag("support-review").captureToImage().asAndroidBitmap()
        File(compose.activity.cacheDir, "support-$surface-$orientation-$language.png").outputStream().use { image.compress(Bitmap.CompressFormat.PNG, 100, it) }
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
        val correct = if(ordering) SeasonIds.canonicalOrder[order.value.index] else q.correct
        action((if(ordering) "season-order-" else "answer-") + correct.value)
        if(ordering) {
            compose.runOnIdle { assertEquals(listOf(correct), order.value.placed) }
            val nextWrong = order.value.choices.first { it !in order.value.placed && it != SeasonIds.canonicalOrder[order.value.index] }
            action("season-order-" + nextWrong.value)
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
