package com.bellfamily.bastischool.ui.colours

import android.app.Application
import android.content.Context
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.lifecycle.ViewModelStore
import androidx.test.core.app.ApplicationProvider
import com.bellfamily.bastischool.learning.colours.*
import com.bellfamily.bastischool.learning.models.ContentLanguage
import com.bellfamily.bastischool.learning.progress.*
import com.bellfamily.bastischool.learning.progress.android.AndroidAtomicCommit
import com.bellfamily.bastischool.learning.progress.android.AndroidProgressRepository
import com.bellfamily.bastischool.learning.progress.ProgressReadResult
import com.bellfamily.bastischool.learning.session.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class RainbowOrderPersistenceTest {
    @get:Rule val compose = createEmptyComposeRule()
    @get:Rule val temp = TemporaryFolder()

    private class IsolatedApplication(context: Context, private val directory: File) : Application() {
        init { attachBaseContext(context) }
        override fun getApplicationContext(): Context = this
        override fun getNoBackupFilesDir(): File = directory
    }

    @Test fun activityOwnerRecreationRestoresPartialStarterAndFullAndDeduplicatesCompletion() {
        val directory = temp.newFolder()
        val app = IsolatedApplication(ApplicationProvider.getApplicationContext(), directory)
        val owners = ViewModelStore()
        lateinit var vm: RainbowOrderViewModel

        fun open(expected: RainbowOrderLevel = RainbowOrderLevel.STARTER) {
            compose.runOnUiThread {
                vm = RainbowOrderViewModel(app); owners.put("rainbow", vm)
                vm.configure("en"); vm.configureAudio("off"); vm.setVisible(true)
            }
            compose.waitUntil(10_000) {
                var ready = false
                compose.runOnUiThread { ready = vm.state?.level == expected && !vm.busy }
                ready
            }
            compose.runOnUiThread { assertFalse(vm.saveFailed) }
        }

        fun waitFor(predicate: (RainbowOrderState) -> Boolean) {
            compose.waitUntil(10_000) {
                var ready = false
                compose.runOnUiThread { ready = vm.state?.let(predicate) == true && !vm.busy }
                ready
            }
            compose.runOnUiThread { assertFalse(vm.saveFailed) }
        }

        fun placeNext() {
            lateinit var state: RainbowOrderState
            compose.runOnUiThread { state = vm.state!!; vm.dispatch(RainbowOrderAction.Place(state.nextAttempt!!, state.target[state.index])) }
            waitFor { it.index == state.index + 1 || it.completed }
        }

        try {
            open()
            compose.runOnUiThread {
                val s = vm.state!!
                vm.dispatch(RainbowOrderAction.Help(s.task))
            }
            waitFor { it.current.support.hint }
            placeNext()
            lateinit var partialStarter: RainbowOrderState
            compose.runOnUiThread { partialStarter = vm.state!!; vm.setVisible(false); owners.clear() }

            open()
            compose.runOnUiThread {
                assertEquals(partialStarter.id, vm.state!!.id)
                assertEquals(partialStarter.choices, vm.state!!.choices)
                assertEquals(partialStarter.steps, vm.state!!.steps)
                vm.configure("de")
            }
            waitFor { it.language == ContentLanguage.GERMAN }
            while (true) {
                var done = false; compose.runOnUiThread { done = vm.state!!.completed }
                if (done) break
                placeNext()
            }
            compose.waitUntil(10_000) { var ack = false; compose.runOnUiThread { ack = vm.state?.acknowledged == true && !vm.busy }; ack }
            val repository = AndroidProgressRepository.create(app)
            val starterEvents = (repository.read(ProgressQuery(activity = RainbowOrderContent.activity)) as ProgressReadResult.Events)
                .records.map { it.event }
            assertEquals(4, starterEvents.filterIsInstance<AttemptEvent>().count { it.origin.session == partialStarter.id })
            assertEquals(1, starterEvents.filterIsInstance<CompletionEvent>().count { it.origin.session == partialStarter.id })
            assertTrue(starterEvents.filterIsInstance<AttemptEvent>().all { it.support.hint })
            val completedId = partialStarter.id
            compose.runOnUiThread { vm.setVisible(false); owners.clear() }
            open()
            compose.runOnUiThread { assertEquals(completedId, vm.state!!.id); assertTrue(vm.state!!.completed); assertTrue(vm.state!!.acknowledged) }
            val afterRestore = (repository.read(ProgressQuery(activity = RainbowOrderContent.activity)) as ProgressReadResult.Events).records
            assertEquals(starterEvents.size, afterRestore.size)

            compose.runOnUiThread { vm.setVisible(false); owners.clear() }
            open()
            compose.runOnUiThread { vm.activate(RainbowOrderLevel.FULL) }
            compose.waitUntil(10_000) { var ready = false; compose.runOnUiThread { ready = vm.state?.level == RainbowOrderLevel.FULL && !vm.busy }; ready }
            compose.runOnUiThread {
                val s = vm.state!!
                vm.dispatch(RainbowOrderAction.Help(s.task))
            }
            waitFor { it.current.support.hint }
            placeNext(); placeNext()
            lateinit var partialFull: RainbowOrderState
            compose.runOnUiThread { partialFull = vm.state!!; vm.setVisible(false); owners.clear() }
            open(RainbowOrderLevel.FULL)
            compose.runOnUiThread {
                assertEquals(partialFull.id, vm.state!!.id)
                assertEquals(partialFull.choices, vm.state!!.choices)
                assertEquals(partialFull.steps, vm.state!!.steps)
                assertTrue(vm.state!!.steps.first().support.hint)
            }
            assertEquals(setOf("rainbow-order-starter-session", "rainbow-order-full-session", "rainbow-order-active-level", "native-progress"),
                directory.listFiles()!!.map { it.name }.toSet())
        } finally { compose.runOnUiThread { owners.clear() } }
    }
}
