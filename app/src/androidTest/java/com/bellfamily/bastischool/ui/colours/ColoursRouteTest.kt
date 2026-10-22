package com.bellfamily.bastischool.ui.colours

import com.bellfamily.bastischool.openHomeActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.lifecycle.ViewModelProvider
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import com.bellfamily.bastischool.MainActivity
import com.bellfamily.bastischool.learning.colours.ColoursContent
import com.bellfamily.bastischool.learning.models.ContentLanguage
import com.bellfamily.bastischool.learning.session.*
import com.bellfamily.bastischool.ui.coloursort.ColourSortViewModel
import com.bellfamily.bastischool.learning.sorting.SortState
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class ColoursRouteTest {
    @get:Rule val compose = createEmptyComposeRule()

    @Test fun productionChooserExploreFindOptionsAndOldSortRemainAvailable() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        context.getSharedPreferences("basti_shell", 0).edit()
            .putString("lang", "en").putString("audioMode", "off").putInt("round", 5).commit()
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            fun colours(): ColoursViewModel { var vm: ColoursViewModel? = null; scenario.onActivity { vm = ViewModelProvider(it)[ColoursViewModel::class.java] }; return vm!! }
            fun sort(): ColourSortViewModel { var vm: ColourSortViewModel? = null; scenario.onActivity { vm = ViewModelProvider(it)[ColourSortViewModel::class.java] }; return vm!! }
            fun waitColours() = compose.waitUntil(10_000) { !colours().busy && !colours().saveFailed }
            fun click(tag: String) { compose.onNodeWithTag(tag).performScrollTo().performClick(); waitColours() }

            compose.openHomeActivity("Colour Sort")
            waitColours()
            compose.onNodeWithTag("colours-open-explore").assertIsDisplayed()
            click("colours-open-explore")
            compose.onNodeWithTag("colours-explore-choice-colour.green").performScrollTo().performClick()
            waitColours()
            compose.onNodeWithTag("colours-explore-name").assertTextEquals("Green")
            scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
            compose.onNodeWithTag("colours-open-find").performScrollTo().performClick()
            waitColours()
            compose.waitUntil(10_000) { colours().quiz != null }
            val originalState = colours().quiz!!
            val original = SessionCheckpoint.encode(originalState)
            val originalTasks = originalState.plan.tasks.map { task ->
                task.id.ordinal to task.question.let { question ->
                    question.correct.value to question.choices.map { it.value }
                }
            }
            assertEquals(ColoursContent.activity, colours().quiz!!.plan.activity)
            scenario.recreate(); waitColours()
            assertArrayEquals(original, SessionCheckpoint.encode(colours().quiz!!))

            compose.onNodeWithText("⚙ Options").performClick()
            compose.onNodeWithText("🇩🇪 Deutsch").performClick()
            scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
            waitColours()
            assertEquals(ContentLanguage.GERMAN, colours().quiz!!.language)
            assertEquals(ColoursContent.activity, colours().quiz!!.plan.activity)
            assertEquals(originalTasks, colours().quiz!!.plan.tasks.map { task ->
                task.id.ordinal to task.question.let { question ->
                    question.correct.value to question.choices.map { it.value }
                }
            })
            compose.onNodeWithTag("colour-find-prompt").assertIsDisplayed()
            scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
            compose.onNodeWithTag("colours-open-sort").performScrollTo().performClick()
            compose.waitUntil(10_000) { !sort().busy && sort().state != null }
            val savedSort: SortState = sort().state!!
            assertEquals(4, savedSort.placements.size)
            assertEquals(setOf("colour.red", "colour.blue"), savedSort.rule.categories.map { it.value }.toSet())
            scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
            compose.onNodeWithTag("colours-open-explore").assertIsDisplayed()
            scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
            compose.onNodeWithTag("home-all").assertIsDisplayed()
        }
    }
}
