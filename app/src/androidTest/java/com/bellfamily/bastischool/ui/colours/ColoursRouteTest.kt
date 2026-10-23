package com.bellfamily.bastischool.ui.colours

import android.graphics.Bitmap
import java.io.File
import java.io.FileOutputStream
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
        val isolation = RouteTestDataIsolation(context)
        try {
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
            // Persist an actual device capture of the restored active question for the review gallery.
            val restoredCapture = File(context.getExternalFilesDir("colours-phase5a1b"), "find-session-restored.png")
            restoredCapture.parentFile?.mkdirs()
            val bitmap = InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot()
            try { FileOutputStream(restoredCapture).use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) } }
            finally { bitmap.recycle() }
            val copied = InstrumentationRegistry.getInstrumentation().uiAutomation
                .executeShellCommand("cp ${restoredCapture.absolutePath} /sdcard/Download/find-session-restored-5a1b.png")
            copied.use { java.io.FileInputStream(it.fileDescriptor).readBytes() }

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
        } finally {
            isolation.restore()
        }
    }
}

/** Keeps connected route tests from replacing a real install's language or learning journals. */
private class RouteTestDataIsolation(private val context: android.content.Context) {
    private val directories = listOf(
        "colour-sort-session", "colours-find-session", "colours-explore-selection", "native-progress"
    )
    private val backup = File(context.cacheDir, "colours-route-test-${System.nanoTime()}")
    private val had = directories.associateWith { name -> File(context.noBackupFilesDir, name).exists() }
    private val prefs = context.getSharedPreferences("basti_shell", 0)
    private val oldLanguage = prefs.getString("lang", null)
    private val oldAudioMode = prefs.getString("audioMode", null)
    private val oldRound = prefs.getInt("round", -1)

    init {
        backup.mkdirs()
        directories.forEach { name ->
            val source = File(context.noBackupFilesDir, name)
            if (had.getValue(name)) check(source.renameTo(File(backup, name))) { "Could not isolate $name" }
        }
    }

    fun restore() {
        directories.forEach { name ->
            val target = File(context.noBackupFilesDir, name)
            target.deleteRecursively()
            if (had.getValue(name)) check(File(backup, name).renameTo(target)) { "Could not restore $name" }
        }
        prefs.edit().apply {
            if (oldLanguage == null) remove("lang") else putString("lang", oldLanguage)
            if (oldAudioMode == null) remove("audioMode") else putString("audioMode", oldAudioMode)
            if (oldRound < 0) remove("round") else putInt("round", oldRound)
        }.commit()
        backup.deleteRecursively()
    }
}
