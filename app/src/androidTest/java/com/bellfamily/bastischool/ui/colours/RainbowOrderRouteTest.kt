package com.bellfamily.bastischool.ui.colours

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.lifecycle.ViewModelProvider
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import com.bellfamily.bastischool.MainActivity
import com.bellfamily.bastischool.openHomeActivity
import com.bellfamily.bastischool.learning.colours.RainbowOrderLevel
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import java.io.File

class RainbowOrderRouteTest {
    @get:Rule val compose = createEmptyComposeRule()

    @Test fun chooserOptionsLanguageAndBackKeepTheSameRainbowSession() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val names = listOf("rainbow-order-starter-session", "rainbow-order-full-session", "native-progress")
        val backup = File(context.cacheDir, "rainbow-route-${System.nanoTime()}").apply { mkdirs() }
        val had = names.associateWith { File(context.noBackupFilesDir, it).exists() }
        names.forEach { name -> if (had.getValue(name)) check(File(context.noBackupFilesDir, name).renameTo(File(backup, name))) }
        val prefs = context.getSharedPreferences("basti_shell", 0)
        val oldLanguage = prefs.getString("lang", null); val oldAudio = prefs.getString("audioMode", null)
        val oldRound = prefs.getInt("round", -1)
        try {
            prefs.edit().putString("lang", "en").putString("audioMode", "off").putInt("round", 5).commit()
            ActivityScenario.launch(MainActivity::class.java).use { scenario ->
                fun owner(): RainbowOrderViewModel {
                    var result: RainbowOrderViewModel? = null
                    scenario.onActivity { result = ViewModelProvider(it)[RainbowOrderViewModel::class.java] }
                    return result!!
                }
                fun settled() = compose.waitUntil(10_000) { !owner().busy && owner().state != null }
                compose.openHomeActivity("Colour Sort")
                compose.onNodeWithTag("colours-open-rainbow-order").performScrollTo().performClick()
                settled()
                assertEquals(RainbowOrderLevel.STARTER, owner().state!!.level)
                compose.onNodeWithTag("rainbow-order-prompt").assertIsDisplayed()
                compose.onNodeWithTag("rainbow-order-model").assertIsDisplayed()
                val before = owner().state!!
                compose.onNodeWithTag("shell-options").performClick()
                compose.onNodeWithText("🇩🇪 Deutsch").performClick()
                compose.onNodeWithTag("shell-back").performClick()
                settled()
                assertEquals(before.id, owner().state!!.id)
                assertEquals(before.choices, owner().state!!.choices)
                assertEquals(before.steps, owner().state!!.steps)
                assertEquals("GERMAN", owner().state!!.language.name)
                compose.onNodeWithTag("shell-back").performClick()
                compose.onNodeWithTag("colours-open-rainbow-order").performScrollTo().assertIsDisplayed()
                compose.onNodeWithTag("shell-back").performClick()
                compose.onNodeWithTag("home-all").assertIsDisplayed()
            }
        } finally {
            names.forEach { name ->
                File(context.noBackupFilesDir, name).deleteRecursively()
                if (had.getValue(name)) check(File(backup, name).renameTo(File(context.noBackupFilesDir, name)))
            }
            prefs.edit().apply {
                if (oldLanguage == null) remove("lang") else putString("lang", oldLanguage)
                if (oldAudio == null) remove("audioMode") else putString("audioMode", oldAudio)
                if (oldRound < 0) remove("round") else putInt("round", oldRound)
            }.commit()
            backup.deleteRecursively()
        }
    }
}
