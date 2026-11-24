package com.bellfamily.bastischool.ui.washhands

import android.util.Log
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import com.bellfamily.bastischool.MainActivity
import com.bellfamily.bastischool.openHomeActivity
import com.bellfamily.bastischool.learning.progress.*
import com.bellfamily.bastischool.learning.progress.android.AndroidProgressRepository
import com.bellfamily.bastischool.learning.washhands.WashHands
import java.io.File
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

/** Exercise the actual shell and asynchronous owner, including disposal of the available-step grid. */
@RunWith(Parameterized::class)
class WashHandsLifecycleTest(private val language: String) {
    companion object {
        @JvmStatic @Parameterized.Parameters(name = "{0}") fun languages() = listOf("en", "de")
    }
    @get:Rule val compose = createEmptyComposeRule()

    @Test fun editsWrongWorkCompletionAndNewRoundSurviveLifecycleAndRepeatedDisposal() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val prefs = context.getSharedPreferences("basti_shell", 0)
        val oldLanguage = prefs.getString("lang", null)
        val oldAudio = prefs.getString("audioMode", null)
        val stores = listOf("wash-hands-session", "native-progress").map { File(context.noBackupFilesDir, it) }
        val backup = File(context.cacheDir, "wash-lifecycle-backup-${System.nanoTime()}").apply { check(mkdirs()) }
        val moved = mutableSetOf<File>()
        var isolated = false
        try {
            stores.forEach {
                if (it.exists()) { check(it.renameTo(File(backup, it.name))); moved += it }
            }
            isolated = true
            prefs.edit().putString("lang", language).putString("audioMode", "off").commit()
            ActivityScenario.launch(MainActivity::class.java).use { scenario ->
                fun model(): WashHandsViewModel {
                    lateinit var result: WashHandsViewModel
                    scenario.onActivity { result = ViewModelProvider(it)[WashHandsViewModel::class.java] }
                    return result
                }
                fun settled() = compose.waitUntil(20_000) {
                    var ready = false
                    scenario.onActivity {
                        val vm = ViewModelProvider(it)[WashHandsViewModel::class.java]
                        ready = vm.state != null && !vm.busy && !vm.saveFailed
                    }
                    ready
                }
                fun state() = checkNotNull(model().state)
                fun click(tag: String) {
                    val node = compose.onNodeWithTag(tag)
                    if (!state().completed) node.performScrollTo()
                    node.performClick()
                    settled()
                }
                fun back() = scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
                fun homeReady() = compose.waitUntil(10_000) {
                    compose.onAllNodesWithTag("home-all").fetchSemanticsNodes().size == 1
                }
                val title = if (language == "de") "Hände waschen" else "Wash Hands"
                fun open() { compose.openHomeActivity(title); settled() }
                val journal = File(context.noBackupFilesDir, "wash-hands-session/events.bin")
                fun preserves(label: String, action: () -> Unit) {
                    val bytes = journal.readBytes()
                    val session = state().id
                    Log.i("WashLifecycle", "$language begin $label: available=${state().available.size}, completed=${state().completed}")
                    action()
                    settled()
                    assertEquals(label, session, state().id)
                    assertArrayEquals(label, bytes, journal.readBytes())
                    Log.i("WashLifecycle", "$language preserved $label")
                }
                fun background() {
                    scenario.moveToState(Lifecycle.State.CREATED)
                    scenario.moveToState(Lifecycle.State.RESUMED)
                }
                fun optionsRecreation() {
                    compose.onNodeWithTag("shell-options").performClick()
                    compose.waitUntil(10_000) { compose.onAllNodesWithTag("options-screen").fetchSemanticsNodes().size == 1 }
                    scenario.recreate()
                    compose.waitUntil(10_000) { compose.onAllNodesWithTag("options-screen").fetchSemanticsNodes().size == 1 }
                    back()
                }
                fun homeRoundTrip() {
                    click("wash-home"); homeReady(); open()
                }

                open()
                assertEquals(4, state().available.size)
                preserves("empty background") { background() }
                click("wash-item-${WashHands.order.first().value}")
                assertEquals(WashHands.order.take(1), state().constructed)
                assertEquals(0, state().attempts)
                preserves("partial recreation") { scenario.recreate() }
                preserves("partial Options recreation") { optionsRecreation() }
                preserves("partial Home disposal") { homeRoundTrip() }
                click("wash-remove-${WashHands.order.first().value}")
                val wrong = WashHands.order.reversed()
                wrong.forEach { click("wash-item-${it.value}") }
                assertTrue(state().available.isEmpty())
                click("wash-check")
                assertTrue(state().needsCorrection)
                preserves("wrong background") { background() }
                preserves("wrong recreation") { scenario.recreate() }
                click("wash-help")
                wrong.forEach { click("wash-remove-${it.value}") }
                WashHands.order.forEach { click("wash-item-${it.value}") }
                click("wash-check")
                assertTrue(state().completed && state().acknowledged)
                compose.onNodeWithTag("completion-celebration").assertExists()
                preserves("completion recreation") { scenario.recreate() }
                preserves("completion Options recreation") { optionsRecreation() }
                val completed = state().id
                click("wash-again")
                assertNotEquals(completed, state().id)
                assertEquals(4, state().available.size)
                repeat(4) { cycle ->
                    preserves("new round Home disposal $cycle") { homeRoundTrip() }
                    preserves("new round background $cycle") { background() }
                }
                preserves("new round recreation") { scenario.recreate() }
                assertEquals(0, state().attempts)
                // Close with the grid still mounted, exercising Activity teardown as well as Home disposal.
                Log.i("WashLifecycle", "$language closing Activity with available-step grid mounted")
            }
            val events = (AndroidProgressRepository.create(context).read() as ProgressReadResult.Events).records.map { it.event }
            assertEquals(8, events.filterIsInstance<AttemptEvent>().size)
            assertEquals(1, events.filterIsInstance<CompletionEvent>().size)
            Log.i("WashLifecycle", "$language Activity disposed; exactly eight checked-position attempts and one completion")
        } finally {
            prefs.edit().putString("lang", oldLanguage).putString("audioMode", oldAudio).commit()
            stores.forEach {
                if (isolated || it in moved) {
                    check(!it.exists() || it.deleteRecursively())
                    val original = File(backup, it.name)
                    if (original.exists()) check(original.renameTo(it))
                }
            }
            check(backup.deleteRecursively())
        }
    }
}
