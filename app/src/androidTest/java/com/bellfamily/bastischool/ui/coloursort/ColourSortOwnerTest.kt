package com.bellfamily.bastischool.ui.coloursort

import android.app.Application
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.lifecycle.ViewModelStore
import androidx.test.core.app.ApplicationProvider
import com.bellfamily.bastischool.audio.*
import com.bellfamily.bastischool.learning.models.ContentLanguage
import com.bellfamily.bastischool.learning.sorting.SortAction
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import java.io.File

class ColourSortOwnerTest {
    @get:Rule val compose=createEmptyComposeRule()
    private class Engine:SpeechEngine {
        override var readiness=EngineReadiness.INITIALISING
        override var onReadinessChanged:((EngineReadiness)->Unit)?=null
        val spoken=mutableListOf<EngineSpeechRequest>();var closed=0
        fun ready(){readiness=EngineReadiness.READY;onReadinessChanged?.invoke(readiness)}
        override fun speak(request:EngineSpeechRequest,result:(EngineResult)->Unit){spoken+=request}
        override fun stop()=Unit
        override fun close(){closed++;readiness=EngineReadiness.CLOSED}
    }
    @Test fun lazyOffColdSpeechLanguageCancellationReturnAndClose() {
        val app=ApplicationProvider.getApplicationContext<Application>()
        File(app.noBackupFilesDir,"colour-sort-session/events.bin").delete()
        val store=ViewModelStore();val engine=Engine();var count=0;lateinit var vm:ColourSortViewModel
        fun settled() {
            compose.waitUntil(10_000){var done=false;compose.runOnUiThread {done = !vm.busy};done}
            compose.runOnUiThread {assertFalse("Save must succeed",vm.saveFailed);assertNotNull(vm.state)}
        }
        compose.runOnUiThread {vm=ColourSortViewModel(app){count++;engine};store.put("sort",vm);vm.configure("en","off")}
        assertEquals(0,count)
        try {
            compose.runOnUiThread {vm.setVisible(true)};settled();assertNotNull(vm.state)
            compose.runOnUiThread {vm.action(SortAction.Replay)};settled();assertEquals(0,count)
            compose.runOnUiThread {vm.configure("en","all");vm.action(SortAction.Replay)};settled()
            assertEquals(1,count);assertTrue(engine.spoken.isEmpty())
            compose.runOnUiThread {vm.configure("de","all")};settled()
            compose.runOnUiThread {engine.ready()};assertTrue(engine.spoken.isEmpty())
            compose.runOnUiThread {vm.action(SortAction.Replay)};settled()
            assertEquals(ContentLanguage.GERMAN,engine.spoken.single().context.language)
            compose.runOnUiThread {vm.setVisible(false);vm.setVisible(true)};settled();assertEquals(1,engine.spoken.size)
            compose.runOnUiThread {vm.action(SortAction.Replay)};settled();assertEquals(2,engine.spoken.size);assertEquals(1,count)
        }finally{compose.runOnUiThread{store.clear();store.clear()}}
        assertEquals(1,engine.closed)
    }
    @Test fun newRoundSpeaksOnceAndUnusedOwnerNeverAllocatesEngine() {
        val app=ApplicationProvider.getApplicationContext<Application>();File(app.noBackupFilesDir,"colour-sort-session/events.bin").delete()
        val store=ViewModelStore();val engine=Engine();var count=0;lateinit var vm:ColourSortViewModel
        compose.runOnUiThread {vm=ColourSortViewModel(app){count++;engine};store.put("sort",vm);vm.configure("en","all");vm.setVisible(true)}
        try {
            compose.waitUntil(10_000){var done=false;compose.runOnUiThread {done = !vm.busy};done};compose.runOnUiThread {assertFalse(vm.saveFailed);assertEquals(1,count);assertTrue(engine.spoken.isEmpty())}
            compose.runOnUiThread{engine.ready();vm.setVisible(true)};assertEquals(1,engine.spoken.size)
        }finally{compose.runOnUiThread{store.clear()}}
        assertEquals(1,engine.closed)
        compose.runOnUiThread {val unused=ColourSortViewModel(app){count++;engine};store.put("unused",unused);unused.configure("de","all");store.clear()}
        assertEquals(1,count)
    }
}
