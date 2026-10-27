package com.bellfamily.bastischool.ui.habitats

import android.app.Application
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.lifecycle.ViewModelStore
import androidx.test.core.app.ApplicationProvider
import com.bellfamily.bastischool.audio.*
import com.bellfamily.bastischool.learning.models.ContentLanguage
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class OceanExploreOwnerTest {
    @get:Rule val compose = createEmptyComposeRule()
    private class Engine : SpeechEngine {
        override var readiness = EngineReadiness.INITIALISING
        override var onReadinessChanged: ((EngineReadiness) -> Unit)? = null
        val spoken = mutableListOf<EngineSpeechRequest>()
        var closed = 0
        fun ready() { readiness = EngineReadiness.READY; onReadinessChanged?.invoke(readiness) }
        override fun speak(request: EngineSpeechRequest, result: (EngineResult) -> Unit) { spoken += request }
        override fun stop() = Unit
        override fun close() { closed++; readiness = EngineReadiness.CLOSED }
    }

    @Test fun soundOffDoesNotCreateSpeechEngineAndExplicitReplayUsesCurrentLanguage() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val store = ViewModelStore()
        val engine = Engine()
        var creations = 0
        lateinit var owner: OceanExploreViewModel
        compose.runOnUiThread {
            owner = OceanExploreViewModel(app) { creations++; engine }
            store.put("ocean", owner)
            owner.configure("en", "off")
            owner.setVisible(true)
        }
        try {
            compose.waitUntil(10_000) { var loaded = false; compose.runOnUiThread { loaded = owner.image != null }; loaded }
            compose.runOnUiThread { owner.selectDolphin(); owner.replay() }
            assertEquals(0, creations)
            compose.runOnUiThread { owner.configure("de", "all"); owner.selectDolphin() }
            assertEquals(1, creations)
            compose.runOnUiThread { engine.ready() }
            assertEquals(1, engine.spoken.size)
            assertEquals(ContentLanguage.GERMAN, engine.spoken.single().context.language)
            assertEquals(SpeechKind.DISCOVERY_FACT, engine.spoken.single().kind)
            assertEquals("Delfin. Delfine leben im Meer.", engine.spoken.single().text)
            compose.runOnUiThread { owner.setVisible(false); owner.setVisible(true) }
            assertEquals("Returning to Explore must be silent", 1, engine.spoken.size)
            compose.runOnUiThread { owner.replay() }
            assertEquals(2, engine.spoken.size)
        } finally {
            compose.runOnUiThread { store.clear() }
        }
        assertEquals(1, engine.closed)
    }
}
