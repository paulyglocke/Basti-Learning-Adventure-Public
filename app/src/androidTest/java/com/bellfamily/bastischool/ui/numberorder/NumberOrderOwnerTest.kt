package com.bellfamily.bastischool.ui.numberorder

import android.app.Application
import android.content.Context
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.lifecycle.ViewModelStore
import androidx.test.core.app.ApplicationProvider
import com.bellfamily.bastischool.audio.*
import com.bellfamily.bastischool.learning.numberorder.NumberOrderContent
import com.bellfamily.bastischool.learning.models.ContentLanguage
import com.bellfamily.bastischool.learning.session.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class NumberOrderOwnerTest {
    @get:Rule val compose = createEmptyComposeRule()
    @get:Rule val temp = TemporaryFolder()
    private class IsolatedApplication(context: Context, private val directory: File) : Application() {
        init { attachBaseContext(context) }
        override fun getApplicationContext(): Context = this
        override fun getNoBackupFilesDir(): File = directory
    }
    private class Engine : SpeechEngine {
        override val readiness = EngineReadiness.READY
        override var onReadinessChanged: ((EngineReadiness) -> Unit)? = null
        val spoken = mutableListOf<EngineSpeechRequest>()
        override fun speak(request: EngineSpeechRequest, result: (EngineResult) -> Unit) { spoken += request; result(EngineResult.Completed) }
        override fun stop() = Unit
        override fun close() = Unit
    }
    @Test fun openingRestoreLanguageAndNavigationRemainSilentAndReplayNeverLeaksCounts() {
        val app = IsolatedApplication(ApplicationProvider.getApplicationContext(), temp.newFolder())
        val store = ViewModelStore()
        val engine = Engine()
        lateinit var vm: NumberOrderViewModel
        fun open() = compose.runOnUiThread {
            vm = NumberOrderViewModel(app) { engine }; store.put("compare", vm)
            vm.configure("en", "all", 5); vm.setVisible(true)
        }
        fun settled() {
            compose.waitUntil(10_000) {
                var done = false
                compose.runOnUiThread { done = !vm.busy && vm.state != null }
                done
            }
            compose.runOnUiThread { assertFalse(vm.saveFailed) }
        }
        try {
            open(); settled()
            compose.runOnUiThread { assertTrue(engine.spoken.isEmpty()); vm.replay() }
            settled()
            compose.runOnUiThread {
                assertEquals(vm.state!!.task.question.instruction.speech.en, engine.spoken.single().text)
                assertEquals(0, vm.state!!.current.attempts)
                vm.setVisible(false); vm.setVisible(true)
                assertEquals(1, engine.spoken.size)
            }
            compose.runOnUiThread { store.clear() }
            open(); settled()
            compose.runOnUiThread { assertEquals(1, engine.spoken.size); vm.configure("de", "questions", 5) }
            settled()
            compose.runOnUiThread { assertEquals(1, engine.spoken.size); vm.replay() }
            settled()
            compose.runOnUiThread {
                assertEquals(vm.state!!.task.question.instruction.speech.de, engine.spoken.last().text)
                assertEquals(ContentLanguage.GERMAN, engine.spoken.last().context.language)
                vm.configure("de", "off", 5); vm.replay()
            }
            settled()
            compose.runOnUiThread { assertEquals(2, engine.spoken.size) }
        } finally { compose.runOnUiThread { store.clear() } }
    }
}
