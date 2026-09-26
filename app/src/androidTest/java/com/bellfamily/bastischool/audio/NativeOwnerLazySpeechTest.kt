package com.bellfamily.bastischool.audio

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStore
import androidx.test.core.app.ApplicationProvider
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import com.bellfamily.bastischool.learning.models.ContentLanguage
import com.bellfamily.bastischool.learning.session.SessionAction
import com.bellfamily.bastischool.ui.prepositions.PrepositionsViewModel
import com.bellfamily.bastischool.ui.seasons.SeasonsViewModel
import com.bellfamily.bastischool.ui.vocabulary.VocabularyViewModel
import com.bellfamily.bastischool.ui.wilma.WilmaViewModel
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

/** Actual retained owners with a counting engine factory; no vendor TTS or physical device needed. */
@RunWith(Parameterized::class)
class NativeOwnerLazySpeechTest(private val owner: String) {
    companion object {
        @JvmStatic @Parameterized.Parameters(name = "{0}")
        fun owners() = listOf("prepositions", "seasons", "wilma", "vocabulary")
    }
    @get:Rule val compose = createEmptyComposeRule()
    private class Engine : SpeechEngine {
        override var readiness = EngineReadiness.INITIALISING
        override var onReadinessChanged: ((EngineReadiness) -> Unit)? = null
        val spoken = mutableListOf<EngineSpeechRequest>()
        var closes = 0
        fun ready() { readiness = EngineReadiness.READY; onReadinessChanged?.invoke(readiness) }
        override fun speak(request: EngineSpeechRequest, result: (EngineResult) -> Unit) { spoken += request }
        override fun stop() = Unit
        override fun close() { closes++; readiness = EngineReadiness.CLOSED }
    }
    private lateinit var configure: (String, String, Int) -> Unit
    private lateinit var visible: (Boolean) -> Unit
    private lateinit var replay: () -> Unit
    private lateinit var settled: () -> Boolean
    private lateinit var usable: () -> Boolean
    private val engine = Engine()
    private val store = ViewModelStore()
    private var creations = 0

    private fun create() = compose.runOnUiThread {
        val application = ApplicationProvider.getApplicationContext<Application>()
        val factory: () -> SpeechEngine = { creations++; engine }
        val vm: ViewModel = when(owner) {
            "prepositions" -> PrepositionsViewModel(application, factory).also {
                configure = it::configure; visible = it::setVisible
                replay = { it.state?.let { s -> it.action(SessionAction.Replay(s.task.id)) } }
                settled = { !it.busy }; usable = { it.state != null && !it.saveFailed && !it.recoveryFailed }
            }
            "seasons" -> SeasonsViewModel(application, factory).also {
                configure = it::configure; visible = it::setVisible; replay = it::replay
                settled = { !it.busy }; usable = { it.selection != null && !it.saveFailed }
            }
            "wilma" -> WilmaViewModel(application, factory).also {
                configure = it::configure; visible = it::setVisible; replay = it::replay
                settled = { !it.busy }; usable = { it.selection != null && !it.saveFailed }
            }
            else -> VocabularyViewModel(application, factory).also {
                configure = it::configure; visible = it::setVisible; replay = it::replay
                settled = { !it.busy }; usable = { it.selection != null && !it.saveFailed }
            }
        }
        // AndroidViewModelFactory's production one-Application construction path remains available.
        assertNotNull(vm.javaClass.getConstructor(Application::class.java))
        store.put(owner, vm)
    }
    private fun awaitSettled() {
        compose.waitUntil(15_000) { var done = false; compose.runOnUiThread { done = settled() }; done }
        compose.runOnUiThread { assertTrue(usable()) }
    }

    @Test fun constructingHiddenOwnerChangingSettingsAndClearingNeverCreatesEngine() {
        create()
        try {
            compose.runOnUiThread {
                configure("en", "off", 5); visible(false)
                configure("de", "all", 5); configure("en", "questions", 5)
                assertEquals(0, creations)
            }
        } finally { compose.runOnUiThread { store.clear() } }
        assertEquals(0, creations); assertEquals(0, engine.closes)
    }

    @Test fun visibleOffIsLazyFirstListenSurvivesInitAndOwnerReusesThenClosesEngine() {
        create()
        try {
            compose.runOnUiThread { configure("en", "off", 5); visible(true) }
            awaitSettled()
            compose.runOnUiThread { replay() }; awaitSettled()
            compose.runOnUiThread { assertEquals(0, creations); configure("en", "all", 5) }
            awaitSettled()
            compose.runOnUiThread { assertEquals(0, creations); replay() }; awaitSettled()
            compose.runOnUiThread {
                assertEquals(1, creations); assertTrue(engine.spoken.isEmpty())
                engine.ready()
                assertEquals(1, engine.spoken.size) // No second Listen needed.
                visible(false); visible(true)
                assertEquals(1, engine.spoken.size) // Return is silent.
                configure("de", "all", 5)
            }
            awaitSettled()
            compose.runOnUiThread { replay() }; awaitSettled()
            compose.runOnUiThread {
                assertEquals(1, creations)
                assertEquals(ContentLanguage.GERMAN, engine.spoken.last().context.language)
                assertEquals(2, engine.spoken.size)
            }
        } finally { compose.runOnUiThread { store.clear(); store.clear() } }
        assertEquals(1, engine.closes)
    }
}
