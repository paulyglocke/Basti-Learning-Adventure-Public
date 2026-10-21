package com.bellfamily.bastischool.ui.wilma

import android.graphics.BitmapFactory
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.test.platform.app.InstrumentationRegistry
import com.bellfamily.bastischool.learning.models.ContentLanguage
import com.bellfamily.bastischool.learning.wilma.WilmaContent
import com.bellfamily.bastischool.learning.wilma.WilmaPhase
import com.bellfamily.bastischool.ui.testing.LayoutTestActivity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class WilmaActivityChooserScreenTest(
    private val language: ContentLanguage,
    private val fontScale: Float,
    private val window: String,
) {
    @get:Rule val compose = createAndroidComposeRule<LayoutTestActivity>()

    companion object {
        @JvmStatic @Parameterized.Parameters(name = "lang={0},scale={1},window={2}")
        fun cases() = listOf(ContentLanguage.ENGLISH, ContentLanguage.GERMAN).flatMap { language ->
            listOf(1f, 1.5f, 2f).flatMap { scale ->
                listOf("phone-portrait", "short-landscape", "tablet-portrait", "tablet-landscape")
                    .map { window -> arrayOf<Any>(language, scale, window) }
            }
        }
    }

    @OptIn(ExperimentalTestApi::class)
    @Test fun allSixModesRemainReachableWithReadableLabelsAndWilmaArtwork() {
        val (width, height) = when (window) {
            "phone-portrait" -> 360.dp to 800.dp
            "short-landscape" -> 640.dp to 320.dp
            "tablet-portrait" -> 800.dp to 1100.dp
            else -> 1100.dp to 800.dp
        }
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val art = context.assets.open(WilmaContent.REFERENCE).use {
            requireNotNull(BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = 2 })).asImageBitmap()
        }
        var chosen: WilmaPhase? = null
        var home = 0
        compose.setContent {
            CompositionLocalProvider(LocalDensity provides Density(LocalDensity.current.density, fontScale)) {
                MaterialTheme {
                    Box(Modifier.size(width, height)) {
                        WilmaActivityChooserScreen(language, mapOf(WilmaContent.REFERENCE to art),
                            ready = true, busy = false, saveFailed = false, imageFailed = false,
                            onChoose = { chosen = it }, onRetry = {}, onHome = { home++ })
                    }
                }
            }
        }

        val exploreLabel = if (language == ContentLanguage.GERMAN) "Die Woche entdecken" else "Explore the week"
        compose.onNodeWithText(exploreLabel).performScrollTo().assertIsDisplayed()
        compose.onNodeWithContentDescription(
            if (language == ContentLanguage.GERMAN) "Wilma mit den sieben Wochentagen" else "Wilma with the seven days of the week",
            useUnmergedTree = true).performScrollTo().assertIsDisplayed().assertHeightIsAtLeast(40.dp)

        val explore = compose.onNodeWithTag("wilma-open-EXPLORE").performScrollTo().assertIsDisplayed()
            .assertHasClickAction().assertHeightIsAtLeast(56.dp)
        explore.performSemanticsAction(SemanticsActions.RequestFocus) { assertTrue(it()) }
            .assertIsFocused().performKeyInput { pressKey(Key.Enter) }
        compose.runOnIdle { assertEquals(WilmaPhase.EXPLORE, chosen) }

        WilmaPhase.entries.filter { it != WilmaPhase.EXPLORE }.forEach { phase ->
            val card = compose.onNodeWithTag("wilma-open-${phase.name}").performScrollTo()
                .assertIsDisplayed().assertHasClickAction().assertHeightIsAtLeast(56.dp)
            val label = phase.title.display[language]
            compose.onNodeWithText(label, useUnmergedTree = true).performScrollTo().assertIsDisplayed()
            card.performClick()
            compose.runOnIdle { assertEquals(phase, chosen) }
        }
        compose.onNodeWithTag("wilma-chooser-home").performScrollTo().assertIsDisplayed().performClick()
        compose.runOnIdle { assertEquals(1, home) }
    }
}
