package com.bellfamily.bastischool.ui.months

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.lifecycle.ViewModelProvider
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import com.bellfamily.bastischool.MainActivity
import com.bellfamily.bastischool.learning.months.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class MonthsRouteTest {
    @get:Rule val compose=createEmptyComposeRule()
    @Test fun hubOptionsBackAndRecreationKeepMonth() {
        val prefs=InstrumentationRegistry.getInstrumentation().targetContext.getSharedPreferences("basti_shell",0)
        val oldLang=prefs.getString("lang",null);val oldAudio=prefs.getString("audioMode",null)
        prefs.edit().putString("lang","en").putString("audioMode","off").commit()
        try {
            ActivityScenario.launch(MainActivity::class.java).use {scenario ->
                fun settled(){compose.waitUntil(10_000){var ready=false;scenario.onActivity{ready=ViewModelProvider(it)[MonthsViewModel::class.java].selection!=null};ready}}
                compose.onNodeWithText("Days & Seasons").performScrollTo().performClick()
                compose.onNodeWithTag("open-months").performScrollTo().performClick();settled()
                compose.onNodeWithTag(MonthIds.SEPTEMBER.value).performScrollTo().performClick()
                scenario.recreate();settled()
                compose.onNodeWithTag("months-selected").performScrollTo().assertTextEquals("September")
                compose.onNodeWithText("⚙ Options").performClick()
                compose.onNodeWithText("🇩🇪 Deutsch").performClick()
                scenario.onActivity{it.onBackPressedDispatcher.onBackPressed()};settled()
                compose.onNodeWithTag("months-selected").performScrollTo().assertTextEquals("September")
                compose.onNodeWithTag(MonthIds.MARCH.value).performScrollTo().assertTextEquals("März")
                scenario.onActivity{it.onBackPressedDispatcher.onBackPressed()}
                compose.onNodeWithTag("open-months").performScrollTo().performClick();settled()
                compose.onNodeWithTag("months-selected").performScrollTo().assertTextEquals("September")
                compose.onNodeWithTag("months-home").performScrollTo().performClick()
                compose.onNodeWithText("Tage & Jahreszeiten").assertExists()
            }
        } finally {prefs.edit().putString("lang",oldLang).putString("audioMode",oldAudio).commit()}
    }
}
