package com.bellfamily.bastischool

import android.os.Bundle
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.unit.dp
import androidx.compose.ui.semantics.SemanticsActions
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import com.bellfamily.bastischool.ui.coloursort.ColourSortViewModel
import com.bellfamily.bastischool.ui.animalgroups.AnimalGroupsViewModel
import com.bellfamily.bastischool.ui.washhands.WashHandsViewModel
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class HomeProductionNavigationTest {
    @get:Rule val compose = createEmptyComposeRule()
    private fun withApp(test: (ActivityScenario<MainActivity>) -> Unit) {
        val prefs=InstrumentationRegistry.getInstrumentation().targetContext.getSharedPreferences("basti_shell",0)
        val lang=prefs.getString("lang",null); val audio=prefs.getString("audioMode",null)
        try {
            prefs.edit().putString("lang","en").putString("audioMode","off").commit()
            ActivityScenario.launch(MainActivity::class.java).use(test)
        } finally { prefs.edit().putString("lang",lang).putString("audioMode",audio).commit() }
    }
    private fun click(tag: String) {
        val node = compose.onNodeWithTag(tag)
        if (compose.onAllNodes(hasTestTag(tag) and hasAnyAncestor(hasScrollAction()))
                .fetchSemanticsNodes().isNotEmpty()) node.performScrollTo()
        node.assertIsDisplayed().performClick()
    }
    private fun back(scenario: ActivityScenario<MainActivity>) {
        scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
        compose.waitForIdle()
    }
    private fun saved(scenario: ActivityScenario<MainActivity>): Bundle {
        val bundle=Bundle()
        scenario.moveToState(Lifecycle.State.CREATED)
        scenario.onActivity { InstrumentationRegistry.getInstrumentation().callActivityOnSaveInstanceState(it,bundle) }
        scenario.moveToState(Lifecycle.State.RESUMED)
        compose.waitForIdle()
        return bundle
    }

    @OptIn(ExperimentalTestApi::class)
    @Test fun keyboardCategoryAndHeaderOrderRetainVisibleNamedActions() = withApp { _ ->
        val category = compose.onNodeWithTag("home-category-WORDS").performScrollTo()
        InstrumentationRegistry.getInstrumentation().sendKeyDownUpSync(android.view.KeyEvent.KEYCODE_TAB)
        category.performSemanticsAction(SemanticsActions.RequestFocus) { assertTrue(it()) }
        category.assertIsFocused().performKeyInput { pressKey(Key.Enter) }
        val back = compose.onNodeWithTag("home-back")
        back.performSemanticsAction(SemanticsActions.RequestFocus) { assertTrue(it()) }
        back.assertIsFocused().performKeyInput { pressKey(Key.Tab) }
        compose.onNodeWithTag("home-settings").assertIsFocused().performKeyInput { pressKey(Key.Tab) }
        compose.onNodeWithTag("home-home").assertIsFocused().performKeyInput { pressKey(Key.Enter) }
        compose.onNodeWithTag("home-category-WORDS").assertExists()
        compose.onNodeWithTag("home-back").assertDoesNotExist()
    }

    @Test fun categoryOptionsLanguageRecreationAllActivitiesAndDirectColoursUseRealShell() = withApp { scenario ->
        compose.onAllNodesWithTag("home-settings").assertCountEquals(1)
        compose.onNodeWithText("⚙ Options").assertDoesNotExist()
        click("home-category-WORDS")
        click("home-settings")
        compose.onNodeWithText("🇩🇪 Deutsch").performClick()
        scenario.recreate(); back(scenario)
        compose.onNodeWithText("Wörter & Erzählen").assertIsDisplayed()
        assertEquals("category:WORDS",saved(scenario).getString("homePage"))
        scenario.recreate()
        compose.onNodeWithTag("home-activity-positions").performScrollTo().assertIsDisplayed()
        click("home-back")
        click("home-all")
        compose.onNodeWithTag("home-activity-mixed").performScrollTo().assertIsDisplayed()
        scenario.recreate()
        assertEquals("all",saved(scenario).getString("homePage"))
        compose.onNodeWithTag("home-activity-mixed").assertIsDisplayed()
        click("home-settings"); back(scenario)
        compose.onNodeWithTag("home-activity-mixed").assertIsDisplayed()
        back(scenario)
        click("home-category-COLOURS")
        assertEquals("COLOUR_SORT",saved(scenario).getString("screen"))
        compose.onNodeWithTag("home-activity-colourSort").assertDoesNotExist()
        back(scenario)
        compose.onNodeWithTag("home-category-COLOURS").assertExists()
    }

    @Test fun categoryLaunchAndExplicitHomeKeepExistingNativeDestination() = withApp { scenario ->
        click("home-category-MATHS")
        click("home-activity-addTogether")
        // Durable session loading runs on the journal worker, outside Compose idling.
        compose.waitUntil(10_000) { compose.onAllNodesWithTag("addition-prompt").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithTag("addition-prompt").performScrollTo().assertIsDisplayed()
        assertEquals("ADD_TOGETHER",saved(scenario).getString("screen"))
        click("addition-home")
        assertEquals("home",saved(scenario).getString("homePage"))
        compose.onNodeWithTag("home-category-MATHS").assertExists()
        click("home-category-TIME")
        click("home-activity-time")
        click("open-months")
        assertEquals("MONTHS_CHOOSER",saved(scenario).getString("screen"))
        back(scenario)
        assertEquals("DAYS_SEASONS",saved(scenario).getString("screen"))
        back(scenario)
        assertEquals("home",saved(scenario).getString("homePage"))
    }

    @OptIn(ExperimentalTestApi::class)
    @Test fun sharedLearningHeaderHasNamedTargetsAndNoDuplicateClockHeading() = withApp { scenario ->
        compose.openHomeActivity("Clock & Time")
        compose.onAllNodesWithText("Clock & Time").assertCountEquals(1)
        compose.onNodeWithText("Clock & Time")
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.Heading, Unit))

        compose.onNodeWithTag("shell-back").assertContentDescriptionEquals("Back")
            .assertHasClickAction().assertHeightIsAtLeast(56.dp).assertWidthIsAtLeast(56.dp)
        compose.onNodeWithTag("shell-options").assertHasClickAction()
            .assertHeightIsAtLeast(56.dp).assertWidthIsAtLeast(56.dp).performClick()
        compose.onNodeWithText("🇩🇪 Deutsch").performClick()
        back(scenario)

        compose.onAllNodesWithText("Uhr & Zeit").assertCountEquals(1)
        val backButton = compose.onNodeWithTag("shell-back").assertContentDescriptionEquals("Zurück")
            .assertHeightIsAtLeast(56.dp).assertWidthIsAtLeast(56.dp)
        backButton.performSemanticsAction(SemanticsActions.RequestFocus) { assertTrue(it()) }
        backButton.assertIsFocused().performKeyInput { pressKey(Key.Enter) }
        compose.onNodeWithTag("home-all").assertIsDisplayed()
    }

    @Test fun exactShellAndBodyTitlesAppearOnceAcrossNativeActivityFamilies() = withApp { scenario ->
        listOf("Add Together", "More or Fewer", "Number Order", "Numbers & Groups", "How many?", "Follow the Instructions")
            .forEach { title ->
                compose.openHomeActivity(title)
                compose.onAllNodesWithText(title).assertCountEquals(1)
                if (title == "Follow the Instructions") {
                    compose.onNodeWithText("Level 1 — One instruction").performClick()
                    compose.waitUntil(10_000) {
                        compose.onAllNodesWithTag("follow-instruction").fetchSemanticsNodes().isNotEmpty()
                    }
                    compose.onAllNodesWithText(title).assertCountEquals(1)
                    back(scenario)
                    compose.onAllNodesWithText(title).assertCountEquals(1)
                }
                back(scenario)
                compose.onNodeWithTag("home-all").assertIsDisplayed()
            }
    }

    @Test fun numbersWebContentAndExactQuestionSurviveOptionsAndRecreation() = withApp { scenario ->
        click("home-category-MATHS"); click("home-activity-count")
        val before=compose.renderedLegacy(scenario,"count")
        val state=JSONObject(before.getString("checkpoint")).getJSONObject("state").toString()
        compose.onNodeWithText("⚙ Options").performClick()
        scenario.recreate(); back(scenario)
        val after=compose.renderedLegacy(scenario,"count")
        assertEquals(state,JSONObject(after.getString("checkpoint")).getJSONObject("state").toString())
        assertEquals(before.getString("text"),after.getString("text"))
        scenario.recreate()
        assertEquals(state,JSONObject(compose.renderedLegacy(scenario,"count").getString("checkpoint")).getJSONObject("state").toString())
        back(scenario)
        compose.waitUntil(10_000){compose.onAllNodesWithTag("home-all").fetchSemanticsNodes().isNotEmpty()}
    }

    @Test fun existingSortingAndRoutineSessionsSurviveNewHomeWithoutJournalReset() = withApp { scenario ->
        // These compatibility checks deliberately keep the emulator's existing journals.
        // Older sorting route fixtures delete their journal before constructing an attempt.
        for (title in listOf("Colour Sort", "Animal Groups", "Wash Hands")) {
            fun state(): Any? {
                var snapshot: Any? = null
                scenario.onActivity { activity ->
                    val owners = ViewModelProvider(activity)
                    snapshot = when (title) {
                        "Colour Sort" -> owners[ColourSortViewModel::class.java].let { if (it.busy) null else it.state }
                        "Animal Groups" -> owners[AnimalGroupsViewModel::class.java].let { if (it.busy) null else it.state }
                        else -> owners[WashHandsViewModel::class.java].let { if (it.busy) null else it.state }
                    }
                }
                return snapshot
            }
            fun ready() = compose.waitUntil(10_000) { state() != null }
            compose.openHomeActivity(title); ready()
            val before = state()
            compose.onNodeWithText("⚙ Options").performClick()
            scenario.recreate(); back(scenario); ready()
            assertEquals("$title Options/recreation", before, state())
            back(scenario)
            compose.openHomeActivity(title); ready()
            assertEquals("$title Home/re-entry", before, state())
            back(scenario)
        }
    }
}
