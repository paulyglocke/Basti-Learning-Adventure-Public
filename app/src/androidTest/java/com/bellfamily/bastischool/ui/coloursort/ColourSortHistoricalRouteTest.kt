package com.bellfamily.bastischool.ui.coloursort

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.lifecycle.ViewModelProvider
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import com.bellfamily.bastischool.MainActivity
import com.bellfamily.bastischool.openHomeActivity
import com.bellfamily.bastischool.learning.coloursort.*
import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.progress.*
import com.bellfamily.bastischool.learning.progress.android.*
import com.bellfamily.bastischool.learning.session.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import java.io.File
import java.util.Base64

/** Runs only on the fresh, isolated API 35 Phase5A1B AVD. */
class ColourSortHistoricalRouteTest {
    @get:Rule val compose = createEmptyComposeRule()

    @Test fun historicalPartialJournalSurvivesNewChooserRouteAndCompletesExactlyOnce() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val app = instrumentation.targetContext.applicationContext
        val sessionId = SessionId("legacy-sort-session-5a1b")
        val sortDirectory = File(app.noBackupFilesDir, "colour-sort-session")
        val progressDirectory = File(app.noBackupFilesDir, "native-progress")
        // Other instrumentation tests legitimately create app data. This test runs only on
        // the disposable Phase 5A.1B AVD: move that data aside, use empty test-owned paths,
        // then restore the original paths even when an assertion fails.
        val backup = File(app.cacheDir, "colour-sort-legacy-compat-${System.nanoTime()}")
        val sortBackup = File(backup, "colour-sort-session")
        val progressBackup = File(backup, "native-progress")
        backup.mkdirs()
        val hadSort = sortDirectory.exists()
        val hadProgress = progressDirectory.exists()
        if (hadSort) assertTrue("Could not isolate existing Sort journal", sortDirectory.renameTo(sortBackup))
        if (hadProgress) assertTrue("Could not isolate existing progress journal", progressDirectory.renameTo(progressBackup))

        val prefs = app.getSharedPreferences("basti_shell", 0)
        val oldLanguage = prefs.getString("lang", null)
        val oldAudioMode = prefs.getString("audioMode", null)
        val oldRound = prefs.getInt("round", -1)

        try {
        val fixture = Base64.getMimeDecoder().decode(
            instrumentation.context.assets.open("coloursort/legacy-v1-partial-session.b64")
                .bufferedReader().use { it.readText() }
        )
        val (historicalState, pending) = ColourSortHost.decode(fixture)
        assertTrue(pending.isEmpty())
        assertEquals(sessionId, historicalState.id)
        assertEquals(listOf(true, false, false, false), historicalState.placements.map { it.placed })

            val progress = AndroidProgressRepository.create(app)
            val historicalAttempt = ColourSort.attempt(historicalState, 0)
            assertTrue(progress.append(historicalAttempt) is ProgressWriteResult.Saved)
            assertTrue(sortDirectory.mkdirs() || sortDirectory.isDirectory)
            File(sortDirectory, "events.bin").writeBytes(fixture)

            prefs.edit().putString("lang", "en").putString("audioMode", "off").putInt("round", 5).commit()
            ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            fun owner(): ColourSortViewModel {
                var result: ColourSortViewModel? = null
                scenario.onActivity { result = ViewModelProvider(it)[ColourSortViewModel::class.java] }
                return result!!
            }
            fun settle() = compose.waitUntil(15_000) { !owner().busy && !owner().saveFailed && owner().state != null }
            fun tap(tag: String) { compose.onNodeWithTag(tag).performScrollTo().performClick(); settle() }

            compose.openHomeActivity("Colour Sort")
            tap("colours-open-sort")
            settle()
            val restored = owner().state!!
            assertEquals(sessionId, restored.id)
            assertEquals(listOf(true, false, false, false), restored.placements.map { it.placed })
            assertEquals(1, restored.placements[0].attempts)
            assertEquals(3, restored.placements.count { !it.placed })
            compose.onNodeWithTag("sort-progress").assertTextEquals("1 of 4 sorted")
            compose.onNodeWithTag("sort-placed-object.colours.red_ball", useUnmergedTree = true).assertIsDisplayed()
            ColourSort.objects.drop(1).forEach { item ->
                tap("sort-item-${item.id.value}")
                tap("sort-category-${item.category.value}")
            }
            val completed = owner().state!!
            assertEquals(sessionId, completed.id)
            assertTrue(completed.completed && completed.acknowledged)

            fun records() = (progress.read(ProgressQuery(session = sessionId)) as ProgressReadResult.Events)
                .records.map { it.event }
            assertEquals(4, records().filterIsInstance<AttemptEvent>().size)
            assertEquals(1, records().filterIsInstance<CompletionEvent>().size)

            val recovered = ColourSortHost(AtomicProgressStorage(sortDirectory, AndroidAtomicCommit), progress)
            recovered.open(SessionId("should-not-replace-restored-session"), 5L, ContentLanguage.GERMAN)
            assertEquals(sessionId, recovered.state!!.id)
            assertTrue(recovered.state!!.completed && recovered.state!!.acknowledged)
            assertEquals(4, records().filterIsInstance<AttemptEvent>().size)
            assertEquals(1, records().filterIsInstance<CompletionEvent>().size)
            }
        } finally {
            sortDirectory.deleteRecursively()
            progressDirectory.deleteRecursively()
            if (hadSort) assertTrue("Could not restore prior Sort journal", sortBackup.renameTo(sortDirectory))
            if (hadProgress) assertTrue("Could not restore prior progress journal", progressBackup.renameTo(progressDirectory))
            prefs.edit().apply {
                if (oldLanguage == null) remove("lang") else putString("lang", oldLanguage)
                if (oldAudioMode == null) remove("audioMode") else putString("audioMode", oldAudioMode)
                if (oldRound < 0) remove("round") else putInt("round", oldRound)
            }.commit()
            backup.deleteRecursively()
        }
    }
}
