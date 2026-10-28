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

class WoodlandExploreOwnerTest {
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

    @Test fun soundOffSelectionIsSilentLanguageSwitchKeepsAnimalAndReplayUsesCurrentAnimal() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val store = ViewModelStore()
        val engine = Engine()
        var creations = 0
        lateinit var owner: WoodlandExploreViewModel
        compose.runOnUiThread {
            owner = WoodlandExploreViewModel(app) { creations++; engine }
            store.put("woodland", owner)
            owner.configure("en", "off")
            owner.setVisible(true)
        }
        try {
            compose.waitUntil(10_000) { var loaded = false; compose.runOnUiThread { loaded = owner.image != null }; loaded }
            assertEquals(724, owner.image?.width)
            compose.runOnUiThread {
                owner.select(HabitatExploreContent.rabbit)
                owner.replay()
                owner.configure("de", "all")
            }
            assertEquals("Sound Off must not create an engine", 0, creations)
            compose.runOnUiThread { engine.ready(); owner.replay() }
            assertEquals(1, engine.spoken.size)
            assertEquals("Kaninchen. Wildkaninchen können unterirdische Baue als Schutz nutzen.",
                engine.spoken.last().text)
            compose.runOnUiThread { owner.select(HabitatExploreContent.fox) }
            assertEquals(ContentLanguage.GERMAN, engine.spoken.last().context.language)
            assertEquals("Fuchs. Füchse leben in vielen Lebensräumen. Ein Fuchs kann einen unterirdischen Bau nutzen, um seine Jungen aufzuziehen.",
                engine.spoken.last().text)
            val priorStops = engine.stops
            compose.runOnUiThread { owner.select(HabitatExploreContent.rabbit) }
            assertTrue("Changing animal interrupts/replaces the previous utterance", engine.stops > priorStops)
            assertEquals("Kaninchen. Wildkaninchen können unterirdische Baue als Schutz nutzen.",
                engine.spoken.last().text)
            compose.runOnUiThread { owner.replay() }
            assertEquals(SpeechTrigger.REPLAY, engine.spoken.last().trigger)
            assertEquals("Kaninchen. Wildkaninchen können unterirdische Baue als Schutz nutzen.",
                engine.spoken.last().text)
            val beforeReturn = engine.spoken.size
            compose.runOnUiThread { owner.setVisible(false); owner.setVisible(true) }
            assertEquals("Returning to the scene is silent", beforeReturn, engine.spoken.size)
        } finally {
            compose.runOnUiThread { store.clear() }
        }
        assertEquals(1, engine.closed)
    }
}
