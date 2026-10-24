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
import com.bellfamily.bastischool.learning.coloursort.FourColourSort
import com.bellfamily.bastischool.learning.models.ContentLanguage
import com.bellfamily.bastischool.learning.session.*
import com.bellfamily.bastischool.ui.coloursort.ColourSortViewModel
import com.bellfamily.bastischool.ui.colours.MatchColoursViewModel
import com.bellfamily.bastischool.ui.coloursort.FourColourSortViewModel
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
            fun match(): MatchColoursViewModel { var vm: MatchColoursViewModel? = null; scenario.onActivity { vm = ViewModelProvider(it)[MatchColoursViewModel::class.java] }; return vm!! }
            fun sortFour(): FourColourSortViewModel { var vm: FourColourSortViewModel? = null; scenario.onActivity { vm = ViewModelProvider(it)[FourColourSortViewModel::class.java] }; return vm!! }
            fun waitColours() = compose.waitUntil(10_000) { !colours().busy && !colours().saveFailed }
            fun waitMatch() = compose.waitUntil(10_000) { !match().busy && !match().saveFailed && match().state != null }
            fun waitFour() = compose.waitUntil(10_000) { !sortFour().busy && !sortFour().saveFailed && sortFour().state != null }
            fun click(tag: String) { compose.onNodeWithTag(tag).performScrollTo().performClick(); waitColours() }

            compose.openHomeActivity("Colour Sort")
            waitColours()
            compose.onNodeWithTag("colours-open-explore").assertIsDisplayed()
            compose.waitForIdle()
            capture(context, "chooser-en")
            click("colours-open-explore")
            compose.onNodeWithTag("colours-explore-choice-colour.green").performScrollTo().performClick()
            waitColours()
            compose.onNodeWithTag("colours-explore-name").assertTextEquals("Green")
            capture(context, "explore-green-en")
            scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
            compose.onNodeWithTag("colours-open-find").performScrollTo().performClick()
            waitColours()
            compose.waitUntil(10_000) { colours().quiz != null }
            capture(context, "find-en-active")
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
            capture(context, "find-de-active")
            scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
            compose.onNodeWithTag("colours-open-explore").assertIsDisplayed()
            capture(context, "chooser-de")
            setTestDisplay("wm size 2400x1080"); scenario.recreate(); compose.waitForIdle(); capture(context, "chooser-phone-landscape")
            setTestDisplay("wm size 1200x1920"); scenario.recreate(); compose.waitForIdle(); capture(context, "chooser-tablet-portrait")
            setTestDisplay("wm size 1920x1200"); scenario.recreate(); compose.waitForIdle(); capture(context, "chooser-tablet-landscape")
            setTestDisplay("wm size reset"); scenario.recreate(); compose.waitForIdle()
            setTestDisplay("settings put system font_scale 2.0"); scenario.recreate(); compose.waitForIdle(); capture(context, "chooser-de-2x")
            setTestDisplay("settings put system font_scale 1.0"); scenario.recreate(); compose.waitForIdle()
            compose.onNodeWithTag("colours-open-match").performScrollTo().performClick()
            compose.waitUntil(10_000) { !match().busy && match().state != null }
            compose.onNodeWithTag("colour-match-target").assertIsDisplayed()
            capture(context, "match-de-active")
            setTestDisplay("wm size 2400x1080"); scenario.recreate(); waitMatch(); capture(context, "match-phone-landscape")
            setTestDisplay("wm size 1200x1920"); scenario.recreate(); waitMatch(); capture(context, "match-tablet-portrait")
            setTestDisplay("wm size reset"); scenario.recreate(); waitMatch()
            val matchingState = match().state!!
            val wrongMatch = matchingState.task.question.choices.first { it != matchingState.task.question.correct }
            compose.onNodeWithTag("colour-match-choice-${wrongMatch.value}").performScrollTo().performClick()
            waitMatch()
            compose.onNodeWithTag("colour-match-feedback-wrong").performScrollTo().assertIsDisplayed()
            capture(context, "match-incorrect")
            compose.onNodeWithTag("colour-match-retry").performScrollTo().performClick()
            waitMatch()
            val correctMatch = match().state!!.task.question.correct
            compose.onNodeWithTag("colour-match-choice-${correctMatch.value}").performScrollTo().performClick()
            waitMatch()
            compose.onNodeWithTag("colour-match-feedback-correct").performScrollTo().assertIsDisplayed()
            capture(context, "match-correct")
            compose.onNodeWithTag("colour-match-next").performScrollTo().performClick()
            waitMatch()
            assertEquals("activity.colours.match", match().state!!.plan.activity.value)
            scenario.recreate()
            compose.waitUntil(10_000) { !match().busy && match().state != null }
            assertEquals("activity.colours.match", match().state!!.plan.activity.value)
            capture(context, "match-restored")
            scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
            compose.onNodeWithTag("colours-open-sort-four").performScrollTo().performClick()
            compose.waitUntil(10_000) { !sortFour().busy && sortFour().state != null }
            compose.onNodeWithTag("sort-four-title").assertIsDisplayed()
            capture(context, "sort-four-start")
            setTestDisplay("wm size 1200x600"); scenario.recreate(); waitFour(); capture(context, "sort-four-short-landscape")
            setTestDisplay("wm size 1920x1200"); scenario.recreate(); waitFour(); capture(context, "sort-four-tablet-landscape")
            setTestDisplay("wm size reset"); scenario.recreate(); waitFour()
            assertEquals(8, sortFour().state!!.placements.size)
            assertEquals(4, sortFour().state!!.rule.categories.size)
            val firstFour = sortFour().state!!.order.first()
            compose.onNodeWithTag("sort-four-item-${firstFour.value}").performScrollTo().performClick()
            waitFour()
            val firstCategory = FourColourSort.item(firstFour).category
            compose.onNodeWithTag("sort-four-category-${firstCategory.value}").performScrollTo().performClick()
            waitFour()
            assertTrue(sortFour().state!!.placement(firstFour).placed)
            capture(context, "sort-four-partial")
            scenario.recreate()
            compose.waitUntil(10_000) { !sortFour().busy && sortFour().state != null }
            assertEquals(8, sortFour().state!!.placements.size)
            capture(context, "sort-four-restored")
            FourColourSort.objects.filter { it.id != firstFour }.forEach { item ->
                compose.onNodeWithTag("sort-four-item-${item.id.value}").performScrollTo().performClick()
                waitFour()
                compose.onNodeWithTag("sort-four-category-${item.category.value}").performScrollTo().performClick()
                waitFour()
            }
            assertTrue(sortFour().state!!.completed)
            compose.onNodeWithTag("sort-four-complete").assertIsDisplayed()
            capture(context, "sort-four-complete")
            scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
            compose.onNodeWithTag("colours-open-sort").performScrollTo().performClick()
            compose.waitUntil(10_000) { !sort().busy && sort().state != null }
            val savedSort: SortState = sort().state!!
            capture(context, "sort-two-start")
            assertEquals(4, savedSort.placements.size)
            assertEquals(setOf("colour.red", "colour.blue"), savedSort.rule.categories.map { it.value }.toSet())
            scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
            compose.onNodeWithTag("colours-open-explore").assertIsDisplayed()
            scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
            compose.onNodeWithTag("home-all").assertIsDisplayed()
        }
        } finally {
            setTestDisplay("wm size reset")
            setTestDisplay("settings put system font_scale 1.0")
            isolation.restore()
        }
    }
}

private fun setTestDisplay(command: String) {
    InstrumentationRegistry.getInstrumentation().uiAutomation.executeShellCommand(command).close()
}

private fun capture(context: android.content.Context, name: String) {
    val folder = File(context.cacheDir, "colours-phase5a2")
    check(folder.mkdirs() || folder.isDirectory)
    val bitmap = InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot()
    val screenshot = File(folder, "$name.png")
    try { FileOutputStream(screenshot).use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) } }
    finally { bitmap.recycle() }
}

/** Keeps connected route tests from replacing a real install's language or learning journals. */
private class RouteTestDataIsolation(private val context: android.content.Context) {
    private val directories = listOf(
        "colour-sort-session", "colours-find-session", "colours-explore-selection", "colours-match-session",
        "four-colour-sort-session", "native-progress"
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
