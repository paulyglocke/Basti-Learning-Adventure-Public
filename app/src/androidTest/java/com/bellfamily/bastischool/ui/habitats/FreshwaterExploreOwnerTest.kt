package com.bellfamily.bastischool.ui.habitats

import android.app.Application
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.lifecycle.ViewModelStore
import androidx.test.core.app.ApplicationProvider
import com.bellfamily.bastischool.audio.*
import com.bellfamily.bastischool.learning.animals.HabitatExploreContent
import com.bellfamily.bastischool.learning.models.ContentLanguage
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class FreshwaterExploreOwnerTest {
    @get:Rule val compose = createEmptyComposeRule()

    private class Engine : SpeechEngine {
        override var readiness = EngineReadiness.INITIALISING
        override var onReadinessChanged: ((EngineReadiness) -> Unit)? = null
        val spoken = mutableListOf<EngineSpeechRequest>()
        var stops = 0
        var closed = 0
        fun ready() { readiness = EngineReadiness.READY; onReadinessChanged?.invoke(readiness) }
        override fun speak(request: EngineSpeechRequest, result: (EngineResult) -> Unit) { spoken += request }
        override fun stop() { stops++ }
        override fun close() { closed++; readiness = EngineReadiness.CLOSED }
    }

    @Test fun offlineSceneSoundOffLanguageReplayAndReplacementKeepSemanticSelection() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val store = ViewModelStore()
        val engine = Engine()
        var creations = 0
        lateinit var owner: FreshwaterExploreViewModel
        compose.runOnUiThread {
            owner = FreshwaterExploreViewModel(app) { creations++; engine }
            store.put("freshwater", owner)
            owner.configure("en", "off")
            owner.setVisible(true)
        }
        try {
            compose.waitUntil(15_000) {
                var loaded = false
                compose.runOnUiThread { loaded = owner.scene != null && owner.animalImages.size == 2 }
                loaded
            }
            assertEquals(724, owner.scene?.width)
            assertEquals(543, owner.scene?.height)
            assertFalse(owner.artworkFailed)
            compose.runOnUiThread {
                owner.select(HabitatExploreContent.frog)
                owner.replay()
                owner.select(HabitatExploreContent.duck)
            }
            assertEquals("Sound Off creates no speech engine", 0, creations)
            assertEquals(HabitatExploreContent.duck, owner.selection.selectedAnimal)
            compose.runOnUiThread { owner.configure("de", "all"); engine.ready(); owner.replay() }
            assertEquals(ContentLanguage.GERMAN, engine.spoken.last().context.language)
            assertEquals("Ente. Viele Enten schwimmen auf Teichen und Seen.", engine.spoken.last().text)
            assertEquals(SpeechTrigger.REPLAY, engine.spoken.last().trigger)
            val priorStops = engine.stops
            compose.runOnUiThread { owner.select(HabitatExploreContent.frog) }
            assertTrue("Selecting a different animal replaces current speech", engine.stops > priorStops)
            assertEquals("Frosch. Viele Frösche legen ihre Eier im Wasser ab und nutzen auch feuchte Plätze an Land.",
                engine.spoken.last().text)
            val utterances = engine.spoken.size
            compose.runOnUiThread { owner.setVisible(false); owner.setVisible(true) }
            assertEquals("Returning does not narrate automatically", utterances, engine.spoken.size)
            assertEquals(HabitatExploreContent.frog, owner.selection.selectedAnimal)
        } finally {
            compose.runOnUiThread { store.clear() }
        }
        assertEquals(1, engine.closed)
    }
}
