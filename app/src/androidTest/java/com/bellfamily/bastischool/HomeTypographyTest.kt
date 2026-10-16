package com.bellfamily.bastischool

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Bitmap
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.bellfamily.bastischool.ui.common.NativeScreenHeader
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.io.File

@RunWith(Parameterized::class)
class HomeTypographyTest(private val language: String, private val scale: Float, private val landscape: Boolean) {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
    companion object {
        @JvmStatic @Parameterized.Parameters(name="{0}-{1}-landscape={2}")
        fun cases() = listOf("en", "de").flatMap { lang -> listOf(1f, 1.5f, 2f).flatMap { scale ->
            listOf(false, true).map { arrayOf<Any>(lang, scale, it) }
        } }
    }

    private fun readable(text: String): TextLayoutResult {
        val node = compose.onNodeWithText(text, useUnmergedTree = true).performScrollTo().assertIsDisplayed()
        val results = mutableListOf<TextLayoutResult>()
        node.performSemanticsAction(SemanticsActions.GetTextLayoutResult) { assertTrue(it(results)) }
        val layout = results.single()
        assertFalse("Truncated: $text", layout.multiParagraph.didExceedMaxLines)
        val bounds = node.fetchSemanticsNode().boundsInRoot
        assertTrue("Clipped height: $text", bounds.height + 1 >= layout.size.height)
        assertTrue("Clipped width: $text", bounds.width + 1 >= layout.size.width)
        val fontPx = with(layout.layoutInput.density) { layout.layoutInput.style.fontSize.toPx() }
        repeat(layout.lineCount) { line ->
            assertFalse(layout.isLineEllipsized(line))
            assertTrue("Right edge: $text", layout.getLineRight(line) <= layout.size.width + 1)
            assertTrue("Left edge: $text", layout.getLineLeft(line) >= -1)
            if (line > 0) assertTrue("Overlapping line spacing: $text",
                layout.getLineBaseline(line) - layout.getLineBaseline(line - 1) + 1 >= fontPx)
        }
        return layout
    }

    @Test fun homeAndHeaderWrapWithoutOverlapOrHiddenActions() {
        val orientation = if (landscape) Configuration.ORIENTATION_LANDSCAPE else Configuration.ORIENTATION_PORTRAIT
        compose.activity.requestedOrientation = if (landscape) ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            else ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        compose.waitUntil(10_000) { compose.activity.resources.configuration.orientation == orientation }
        val de = language == "de"
        val title = if (de) "Bastis Lernabenteuer" else "Basti's Learning Adventure"
        val greeting = if (de) "Bereit für ein Abenteuer?" else "Ready for an adventure?"
        val options = if (de) "⚙ Optionen" else "⚙ Options"
        var openedOptions = 0
        compose.setContent {
            CompositionLocalProvider(LocalDensity provides Density(LocalDensity.current.density, scale)) {
                MaterialTheme {
                    Box(Modifier.size(if (landscape) 640.dp else 320.dp, if (landscape) 320.dp else 640.dp)) {
                        Scaffold(topBar = {
                            NativeScreenHeader({ Text(title, fontWeight = FontWeight.Black) }, {},
                                { OutlinedButton({ openedOptions++ }) { Text(options) } },
                                Color(0xFFFFD76A), WindowInsets(0), reflowTitle = true)
                        }) { padding -> NativeHome(language, padding, false, {}) }
                    }
                }
            }
        }
        val titleLayout = readable(title)
        // A long word may wrap, but never leave a one/two-letter orphan like the audited “er”.
        val text = titleLayout.layoutInput.text.text
        for (line in 1 until titleLayout.lineCount) {
            val start = titleLayout.getLineStart(line)
            val end = titleLayout.getLineEnd(line, visibleEnd = true)
            if (start > 0 && start < text.length && text[start - 1].isLetter() && text[start].isLetter()) {
                assertTrue("Orphaned title fragment", text.substring(start, end).trim().length > 2)
            }
        }
        readable(options)
        compose.onNodeWithText(options).performScrollTo().performTouchInput { click() }
        compose.runOnIdle { assertEquals(1, openedOptions) }
        val greetingLayout = readable(greeting)
        assertEquals(32f, greetingLayout.layoutInput.style.fontSize.value)
        assertEquals(40f, greetingLayout.layoutInput.style.lineHeight.value)
        save("top")
        readable(if (de) "Wähle ein Spiel. Jede Runde ist kurz und einfach." else "Choose a game. Each round is short and simple.")
        homeCardGroups().flatten().forEach { card ->
            readable(if (de) card.titleDe else card.titleEn)
            readable(if (de) card.descriptionDe else card.descriptionEn)
        }
        save("bottom")
    }

    private fun save(position: String) {
        val image = compose.onRoot().captureToImage().asAndroidBitmap()
        File(compose.activity.getExternalFilesDir(null), "home-$language-$scale-$landscape-$position.png")
            .outputStream().use { image.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }
}
