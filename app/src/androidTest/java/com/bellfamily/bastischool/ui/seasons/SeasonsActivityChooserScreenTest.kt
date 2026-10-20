package com.bellfamily.bastischool.ui.seasons

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.bellfamily.bastischool.learning.models.ContentLanguage
import com.bellfamily.bastischool.learning.seasons.SeasonsPhase
import com.bellfamily.bastischool.ui.testing.LayoutTestActivity
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.assertTrue
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class SeasonsActivityChooserScreenTest(
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
    @Test fun everyModeAndSeasonArtworkStayReachableAtLargeText() {
        val (width, height) = when (window) {
            "phone-portrait" -> 360.dp to 800.dp
            "short-landscape" -> 640.dp to 320.dp
            "tablet-portrait" -> 800.dp to 1100.dp
            else -> 1100.dp to 800.dp
        }
        var chosen: SeasonsPhase? = null
        var home = 0
        compose.setContent {
            CompositionLocalProvider(LocalDensity provides Density(LocalDensity.current.density, fontScale)) {
                MaterialTheme {
                    Box(Modifier.size(width, height)) {
                        SeasonsActivityChooserScreen(
                            language = language,
                            ready = true,
                            busy = false,
                            saveFailed = false,
                            onExplore = { chosen = SeasonsPhase.EXPLORE },
                            onChoosePhase = { chosen = it },
                            onRetry = {},
                            onHome = { home++ },
                        )
                    }
                }
            }
        }

        val exploreLabel = if (language == ContentLanguage.GERMAN) "Jahreszeiten entdecken" else "Explore the seasons"
        compose.onNodeWithText(exploreLabel).assertExists()
        val art = compose.onNodeWithContentDescription(
            if (language == ContentLanguage.GERMAN) "Vier Jahreszeiten" else "The four seasons",
            useUnmergedTree = true)
        art.assertIsDisplayed()
        val artBounds = art.fetchSemanticsNode().boundsInRoot
        val titleBounds = compose.onNodeWithText(exploreLabel, useUnmergedTree = true)
            .fetchSemanticsNode().boundsInRoot
        assertTrue("Explore art and title must not overlap at $window / ${fontScale}×",
            artBounds.bottom <= titleBounds.top || titleBounds.bottom <= artBounds.top ||
                artBounds.right <= titleBounds.left || titleBounds.right <= artBounds.left)
        val explore = compose.onNodeWithTag("seasons-open-explore").performScrollTo().assertIsDisplayed()
            .assertHasClickAction().assertHeightIsAtLeast(56.dp)
        explore.performSemanticsAction(SemanticsActions.RequestFocus) { assertTrue(it()) }
            .assertIsFocused().performKeyInput { pressKey(Key.Enter) }
        compose.runOnIdle { assertEquals(SeasonsPhase.EXPLORE, chosen) }
        compose.runOnIdle { chosen = null }
        val practice = compose.onNodeWithTag("seasons-choose-practice").performScrollTo()
        practice.performSemanticsAction(SemanticsActions.RequestFocus) { assertTrue(it()) }
            .assertIsFocused().performKeyInput { pressKey(Key.Enter) }
        compose.runOnIdle { assertEquals(SeasonsPhase.PRACTICE, chosen) }

        SeasonsPhase.entries.filter { it != SeasonsPhase.EXPLORE }.forEach { phase ->
            val label = phase.title.display[language]
            compose.onNodeWithTag("seasons-choose-${phase.name.lowercase()}").performScrollTo()
                .assertIsDisplayed().assertHasClickAction().assertHeightIsAtLeast(56.dp)
                .assertTextEquals(label,
                    when (phase) {
                        SeasonsPhase.PRACTICE -> if (language == ContentLanguage.GERMAN) "Sieh dir ein Jahreszeitenbild an und wähle den Namen." else "Look at a season picture and choose its name."
                        SeasonsPhase.NEXT -> if (language == ContentLanguage.GERMAN) "Finde die Jahreszeit, die als Nächstes kommt." else "Find the season that comes next."
                        SeasonsPhase.BEFORE -> if (language == ContentLanguage.GERMAN) "Finde die Jahreszeit, die davor kommt." else "Find the season that comes before."
                        SeasonsPhase.MISSING -> if (language == ContentLanguage.GERMAN) "Finde die fehlende Jahreszeit im Jahr." else "Find the season missing from the year."
                        SeasonsPhase.CLUES -> if (language == ContentLanguage.GERMAN) "Höre einen Hinweis und wähle die Jahreszeit." else "Listen to a clue and choose the season."
                        SeasonsPhase.ORDER -> if (language == ContentLanguage.GERMAN) "Ordne die Jahreszeiten im Jahreslauf." else "Place the seasons in their year order."
                        SeasonsPhase.MATCH -> if (language == ContentLanguage.GERMAN) "Ordne jede Jahreszeit dem passenden Hinweis zu." else "Match each season with its clue."
                        SeasonsPhase.COMBINED -> if (language == ContentLanguage.GERMAN) "Finde die Jahreszeit davor und danach." else "Choose the season before and after."
                        SeasonsPhase.EXPLORE -> error("Explore uses the featured choice")
                    })
            compose.onNodeWithTag("seasons-choose-${phase.name.lowercase()}").performClick()
            compose.runOnIdle { assertEquals(phase, chosen) }
        }
        compose.onNodeWithTag("seasons-chooser-home").performScrollTo().assertIsDisplayed().performClick()
        compose.runOnIdle { assertEquals(1, home) }
    }
}
