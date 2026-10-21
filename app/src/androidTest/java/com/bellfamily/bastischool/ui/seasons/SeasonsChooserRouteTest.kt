package com.bellfamily.bastischool.ui.seasons

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.lifecycle.ViewModelProvider
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import com.bellfamily.bastischool.MainActivity
import com.bellfamily.bastischool.learning.content.SeasonIds
import com.bellfamily.bastischool.learning.models.ContentLanguage
import com.bellfamily.bastischool.learning.seasons.SeasonsContent
import com.bellfamily.bastischool.learning.seasons.SeasonsOrder
import com.bellfamily.bastischool.learning.seasons.SeasonsPhase
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class SeasonsChooserRouteTest {
    @get:Rule val compose = createEmptyComposeRule()

    @Test fun illustratedHomeChooserEveryModeOptionsBackAndSharedMonthsRoutesRemainAvailable() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        context.getSharedPreferences("basti_shell", 0).edit()
            .putString("lang", "en").putString("audioMode", "off").putInt("round", 5).commit()
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            fun vm(): SeasonsViewModel {
                lateinit var result: SeasonsViewModel
                scenario.onActivity { result = ViewModelProvider(it)[SeasonsViewModel::class.java] }
                return result
            }
            fun ready() {
                compose.waitUntil(10_000) { val value = vm(); value.selection != null && !value.busy && !value.saveFailed && !value.imageFailed }
            }
            fun enterChooser() {
                compose.onNodeWithTag("home-category-TIME").performScrollTo().performClick()
                compose.onNodeWithTag("home-activity-time").performScrollTo().performClick()
                compose.onNodeWithTag("open-seasons").performScrollTo().performClick()
                compose.onNodeWithTag("seasons-open-explore").assertExists()
                ready()
            }
            fun backToChooser() {
                scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
                compose.onNodeWithTag("seasons-open-explore").assertExists()
                ready()
            }

            enterChooser()
            compose.onNodeWithText("⚙ Options").performClick()
            compose.onNodeWithText("🇩🇪 Deutsch").performClick()
            scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
            compose.onNodeWithTag("seasons-open-explore").assertExists()
            assertEquals("de", context.getSharedPreferences("basti_shell", 0).getString("lang", null))

            val destinations = listOf(
                SeasonsPhase.EXPLORE to "season-artwork",
                SeasonsPhase.PRACTICE to "seasons-prompt",
                SeasonsPhase.NEXT to "seasons-prompt",
                SeasonsPhase.BEFORE to "seasons-prompt",
                SeasonsPhase.MISSING to "seasons-missing-sequence",
                SeasonsPhase.CLUES to "seasons-prompt",
                SeasonsPhase.ORDER to "seasons-order-prompt",
                SeasonsPhase.MATCH to "seasons-match-prompt",
                SeasonsPhase.COMBINED to "seasons-combined-prompt",
            )
            destinations.forEach { (phase, destinationTag) ->
                val chooserTag = if (phase == SeasonsPhase.EXPLORE) "seasons-open-explore"
                    else "seasons-choose-${phase.name.lowercase()}"
                compose.onNodeWithTag(chooserTag).performScrollTo().performClick()
                ready()
                assertEquals(phase, vm().selection?.phase)
                if (phase == SeasonsPhase.ORDER && vm().ordering?.completed == true) {
                    compose.onNodeWithTag("seasons-complete").assertTextEquals(
                        SeasonsOrder.completion.display[ContentLanguage.GERMAN]
                    )
                    compose.onNodeWithTag("seasons-placed-count").assertTextEquals("4 von 4 Jahreszeiten eingeordnet")
                    SeasonIds.canonicalOrder.forEachIndexed { index, id ->
                        compose.onNodeWithText("${index + 1}. ${SeasonsContent.season(id).text.display[ContentLanguage.GERMAN]}").assertExists()
                    }
                } else {
                    compose.onNodeWithTag(destinationTag).performScrollTo().assertExists()
                }
                backToChooser()
            }

            compose.onNodeWithTag("seasons-chooser-home").performScrollTo().performClick()
            compose.onNodeWithTag("home-all").assertExists()
        }
    }
}
