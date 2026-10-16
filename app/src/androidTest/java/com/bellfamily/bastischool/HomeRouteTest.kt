package com.bellfamily.bastischool

import android.os.Bundle
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import androidx.lifecycle.Lifecycle
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
        val expectedScreens = listOf("TELL_ME", "VOCABULARY", "WEB", "WEB", "WEB", "DAYS_SEASONS",
            "WEB", "SUBITISING", "QUANTITY_MATCH", "COMPARE_QUANTITY", "NUMBER_ORDER", "CLOCK",
            "ADD_TOGETHER", "WEB", "PREPOSITIONS", "WEB", "WASH_HANDS", "ANIMAL_GROUPS", "COLOUR_SORT", "FOLLOW_LEVELS")
        try {
            ActivityScenario.launch(MainActivity::class.java).use { scenario ->
                homeCardGroups().flatten().forEachIndexed { index, card ->
                    val title = if (language == "de") card.titleDe else card.titleEn
                    compose.onNodeWithText(title).performScrollTo().performClick()
                    compose.waitForIdle()
                    // Saving demotes ComponentActivity's LifecycleRegistry to CREATED.
                    // Inspect while stopped, then resume normally before exercising Back.
                    scenario.moveToState(Lifecycle.State.CREATED)
                    scenario.onActivity { activity ->
                        val saved = Bundle()
                        instrumentation.callActivityOnSaveInstanceState(activity, saved)
                        assertEquals(title, expectedScreens[index], saved.getString("screen"))
                        if (expectedScreens[index] == "WEB") {
                            assertEquals(title, if (card.verbExplorer) "verbExplorer" else card.mode, saved.getString("webMode"))
                        }
                    }
                    scenario.moveToState(Lifecycle.State.RESUMED)
                    compose.waitForIdle()
                    // No answers, scores or completed sessions are needed to check entry dispatch.
                    scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
                    val greeting = if (language == "de") "Bereit für ein Abenteuer?" else "Ready for an adventure?"
                    compose.waitUntil(10_000) { compose.onAllNodesWithText(greeting).fetchSemanticsNodes().size == 1 }
                }
            }
        } finally {
            prefs.edit().putString("lang", oldLanguage).putString("audioMode", oldAudio).commit()
        }
    }
}
