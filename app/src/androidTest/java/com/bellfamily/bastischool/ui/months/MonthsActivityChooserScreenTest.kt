package com.bellfamily.bastischool.ui.months

import com.bellfamily.bastischool.ui.testing.LayoutTestActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.bellfamily.bastischool.learning.models.ContentLanguage
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class MonthsActivityChooserScreenTest(
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

    @Test fun allChoicesRemainReachableAndChildSized() {
        val (width, height) = when (window) {
            "phone-portrait" -> 360.dp to 800.dp
            "short-landscape" -> 640.dp to 320.dp
            "tablet-portrait" -> 800.dp to 1100.dp
            else -> 1100.dp to 800.dp
        }
        var explored = 0
        compose.setContent {
            CompositionLocalProvider(LocalDensity provides Density(LocalDensity.current.density, fontScale)) {
                MaterialTheme {
                    Box(Modifier.size(width, height)) {
                        MonthsActivityChooserScreen(language,
                            onExplore = { explored++ }, onFind = {}, onRelations = {}, onMissing = {}, onBetween = {},
                            onBuildYear = {}, onSeasonsMatch = {}, onSeasonsWheel = {}, onHome = {})
                    }
                }
            }
        }
        compose.onNodeWithTag("months-open-explore").performScrollTo().assertIsDisplayed()
            .assertHasClickAction().assertHeightIsAtLeast(56.dp).performClick()
        compose.runOnIdle { assertEquals(1, explored) }
        listOf("months-open-find", "months-open-relations", "months-open-missing", "months-open-between",
            "months-open-build-year", "months-open-seasons-match", "months-open-seasons-wheel", "months-chooser-home")
            .forEach { tag ->
                compose.onNodeWithTag(tag).performScrollTo().assertIsDisplayed()
                    .assertHasClickAction().assertHeightIsAtLeast(56.dp)
            }
    }
}
