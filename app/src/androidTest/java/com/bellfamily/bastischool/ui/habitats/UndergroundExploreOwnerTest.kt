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

class UndergroundExploreOwnerTest {
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

    @Test fun loadsOfflineSceneAndAnimalsAndKeepsSelectionAcrossLanguageAndVisibilityChanges() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val store = ViewModelStore()
        val engine = Engine()
        var creations = 0
        lateinit var owner: UndergroundExploreViewModel
        compose.runOnUiThread {
            owner = UndergroundExploreViewModel(app) { creations++; engine }
            store.put("underground", owner)
            owner.configure("en", "off")
            owner.setVisible(true)
        }
        try {
            compose.waitUntil(15_000) {
                var loaded = false
                compose.runOnUiThread { loaded = owner.scene != null && owner.animalImages.size == 3 }
                loaded
            }
            assertEquals(724, owner.scene?.width)
            assertEquals(3, owner.animalImages.size)
            assertFalse(owner.artworkFailed)
            compose.runOnUiThread {
                owner.select(HabitatExploreContent.mole)
                owner.replay()
                owner.configure("de", "all")
            }
            assertEquals("Sound Off must not create a speech engine", 0, creations)
            compose.runOnUiThread { engine.ready(); owner.replay() }
            assertEquals(ContentLanguage.GERMAN, engine.spoken.last().context.language)
            assertEquals("Maulwurf. Ein Maulwurf lebt überwiegend unter der Erde und gräbt Gänge.",
                engine.spoken.last().text)
            val priorStops = engine.stops
            compose.runOnUiThread { owner.select(HabitatExploreContent.earthworm) }
            assertTrue("Changing animal replaces current speech", engine.stops > priorStops)
            assertEquals("Regenwurm. Regenwürmer bewegen sich durch den Boden.", engine.spoken.last().text)
            compose.runOnUiThread { owner.select(HabitatExploreContent.rabbit); owner.replay() }
            assertEquals("Wildkaninchen. Wildkaninchen können unterirdische Baue als Schutz nutzen.",
                engine.spoken.last().text)
            assertEquals(SpeechTrigger.REPLAY, engine.spoken.last().trigger)
            val utterances = engine.spoken.size
            compose.runOnUiThread { owner.setVisible(false); owner.setVisible(true) }
            assertEquals("Returning to the scene does not narrate automatically", utterances, engine.spoken.size)
            compose.runOnUiThread { assertEquals(HabitatExploreContent.rabbit, owner.selection.selectedAnimal) }
        } finally {
            compose.runOnUiThread { store.clear() }
        }
        assertEquals(1, engine.closed)
    }
}
