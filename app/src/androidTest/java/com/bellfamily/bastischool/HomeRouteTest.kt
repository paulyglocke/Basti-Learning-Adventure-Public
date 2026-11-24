package com.bellfamily.bastischool

import android.os.Bundle
import android.util.Log
import android.app.Activity
import android.app.Application
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import java.util.concurrent.atomic.AtomicReference
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class HomeRouteTest(private val language: String) {
    @get:Rule val compose = createEmptyComposeRule()
    companion object {
        @JvmStatic @Parameterized.Parameters(name="{0}") fun cases() = listOf("en", "de")
    }

    @Test fun everyHomeEntryOpensItsExistingDestinationAndReturnsHome() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val prefs = instrumentation.targetContext.getSharedPreferences("basti_shell", 0)
        val oldLanguage = prefs.getString("lang", null)
        val oldAudio = prefs.getString("audioMode", null)
        prefs.edit().putString("lang", language).putString("audioMode", "off").commit()
        val expectedScreens = listOf("TELL_ME", "VOCABULARY_CHOOSER", "VERB_LIBRARY", "LETTERS", "WEB", "DAYS_SEASONS",
            "VERB_QUIZ", "SUBITISING", "QUANTITY_MATCH", "COMPARE_QUANTITY", "NUMBER_ORDER", "CLOCK",
            "ADD_TOGETHER", "ADD_CHALLENGE", "SUBTRACTION", "MIXED_ADDITION", "BUILD_FIVE", "BUILD_TEN", "MAKE_FIVE", "COUNTING_TRAY", "WEB", "PREPOSITIONS", "WEB", "MEMORY_CHOOSER", "WASH_HANDS", "HABITATS_CHOOSER", "HABITAT_MATCH", "ANIMAL_GROUPS", "COLOURS_CHOOSER", "FOLLOW_LEVELS", "PATTERN_CHOOSER", "QUANTITY_CHANGE_CHOOSER", "NUMBER_STORIES_CHOOSER", "CONSERVATION_CHOOSER", "LENGTH_COMPARE")
        try {
            ActivityScenario.launch(MainActivity::class.java).use { scenario ->
                homeCardGroups().flatten().forEachIndexed { index, card ->
                    val title = if (language == "de") card.titleDe else card.titleEn
                    Log.i("HomeRouteTest", "Opening $language ${index + 1}/${expectedScreens.size}: $title (${card.mode})")
                    try {
                        compose.openHomeActivity(title)
                        compose.waitForIdle()
                        val legacyMode = if (card.verbExplorer) "verbExplorer" else card.mode
                        val before = if (expectedScreens[index] == "WEB") {
                            try {
                                compose.renderedLegacy(scenario, legacyMode!!)
                            } catch (failure: Throwable) {
                                Log.e("HomeRouteTest", "Home $language entry ${index + 1}: $title ($legacyMode) did not render", failure)
                                throw failure
                            }
                        } else null
                        if (expectedScreens[index] == "LETTERS") {
                            compose.waitUntil(10_000) {
                                compose.onAllNodesWithTag("letters-prompt").fetchSemanticsNodes().isNotEmpty()
                            }
                            compose.onNodeWithTag("letters-prompt").assertExists()
                            compose.onNodeWithTag("letters-letter-card").assertExists()
                            compose.onNodeWithTag("letters-replay").assertExists()
                        }
                        // Capture a real framework save and verify the restored destination.
                        // Directly calling onSaveInstanceState bypasses that lifecycle contract.
                        val saved = recreateAndCaptureState(scenario)
                        assertEquals(title, expectedScreens[index], saved.getString("screen"))
                        if (expectedScreens[index] == "WEB") {
                            assertEquals(title, legacyMode, saved.getString("webMode"))
                        }
                        compose.waitForIdle()
                        if (before != null) {
                            val after = compose.renderedLegacy(scenario, legacyMode!!)
                            assertEquals(title, before.getString("text"), after.getString("text"))
                            assertEquals(title,
                                JSONObject(before.getString("checkpoint")).getJSONObject("state").toString(),
                                JSONObject(after.getString("checkpoint")).getJSONObject("state").toString())
                        }
                        Log.i("HomeRouteTest", "Restored $language ${index + 1}: $title")
                        // No answers, scores or completed sessions are needed to check entry dispatch.
                        scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
                        if (expectedScreens[index] in setOf("HABITATS_CHOOSER", "HABITAT_MATCH")) {
                            // Explore Habitats is nested; Back restores the browse page that launched it.
                            compose.onNodeWithText(if (language == "de") "Alle Aktivitäten" else "All Activities")
                                .performScrollTo().assertIsDisplayed()
                            scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
                        } else {
                            val greeting = if (language == "de") "Was wollen wir entdecken?" else "What shall we explore?"
                            compose.waitUntil(10_000) { compose.onAllNodesWithText(greeting).fetchSemanticsNodes().size == 1 }
                        }
                    } catch (failure: Throwable) {
                        Log.e("HomeRouteTest", "Failed $language entry ${index + 1}: $title", failure)
                        throw failure
                    }
                }
            }
        } finally {
            prefs.edit().putString("lang", oldLanguage).putString("audioMode", oldAudio).commit()
        }
    }

    private fun recreateAndCaptureState(scenario: ActivityScenario<MainActivity>): Bundle {
        val application = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as Application
        lateinit var original: MainActivity
        scenario.onActivity { original = it }
        val saved = AtomicReference<Bundle?>(null)
        val callbacks = object : Application.ActivityLifecycleCallbacks {
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
                if (activity === original) saved.set(Bundle(outState))
            }
            override fun onActivityCreated(activity: Activity, state: Bundle?) {}
            override fun onActivityStarted(activity: Activity) {}
            override fun onActivityResumed(activity: Activity) {}
            override fun onActivityPaused(activity: Activity) {}
            override fun onActivityStopped(activity: Activity) {}
            override fun onActivityDestroyed(activity: Activity) {}
        }
        application.registerActivityLifecycleCallbacks(callbacks)
        return try {
            scenario.recreate()
            checkNotNull(saved.get()) { "Framework did not save the Home destination during recreation" }
        } finally {
            application.unregisterActivityLifecycleCallbacks(callbacks)
        }
    }
}
