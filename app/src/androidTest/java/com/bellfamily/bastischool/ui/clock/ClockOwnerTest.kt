package com.bellfamily.bastischool.ui.clock

import android.app.Application
import android.content.Context
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.lifecycle.ViewModelStore
import androidx.test.core.app.ApplicationProvider
import com.bellfamily.bastischool.audio.*
import com.bellfamily.bastischool.learning.clock.*
import com.bellfamily.bastischool.learning.models.ContentLanguage
import com.bellfamily.bastischool.learning.progress.AtomicProgressStorage
import com.bellfamily.bastischool.learning.progress.android.AndroidAtomicCommit
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class ClockOwnerTest {
    @get:Rule val compose=createEmptyComposeRule()
    @get:Rule val temp=TemporaryFolder()
    private class IsolatedApplication(context:Context,private val directory:File):Application() {
        init {attachBaseContext(context)}
        override fun getApplicationContext():Context=this
        override fun getNoBackupFilesDir():File=directory
    }
    private class Engine:SpeechEngine {
        override val readiness=EngineReadiness.READY
        override var onReadinessChanged:((EngineReadiness)->Unit)?=null
        val spoken=mutableListOf<EngineSpeechRequest>()
        var stops=0
        override fun speak(request:EngineSpeechRequest,result:(EngineResult)->Unit) {spoken+=request}
        override fun stop(){stops++}
        override fun close()=Unit
    }
    @Test fun silentDurableRestoreSettingsAndNoFabricatedProgress() {
        val directory=temp.newFolder()
        val app=IsolatedApplication(ApplicationProvider.getApplicationContext(),directory)
        val owners=ViewModelStore()
        val engine=Engine()
        lateinit var vm:ClockViewModel
        val disk=ClockStore(AtomicProgressStorage(File(directory,"clock-explore"),AndroidAtomicCommit))
        fun open() {
            compose.runOnUiThread {vm=ClockViewModel(app){engine};owners.put("clock",vm);vm.configure("de","all");vm.setVisible(true)}
            compose.waitUntil(10_000) {var ready=false;compose.runOnUiThread {ready=vm.state!=null};ready}
        }
        try {
            open()
            compose.runOnUiThread {
                assertTrue(engine.spoken.isEmpty())
                vm.move(ClockTime(3,30));vm.listen()
                assertEquals("Halb vier",engine.spoken.last().text)
                val stops=engine.stops
                vm.adjust(5);assertTrue(engine.stops>stops)
                vm.move(ClockTime(11,27));vm.setVisible(false)
            }
            val expected=ClockExploreState(ClockTime(11,27),ContentLanguage.GERMAN)
            compose.waitUntil(10_000) {runCatching {disk.read()==expected}.getOrDefault(false)}
            val count=engine.spoken.size
            compose.runOnUiThread {owners.clear()}
            open()
            compose.runOnUiThread {
                assertEquals(expected,vm.state);assertEquals(count,engine.spoken.size)
                vm.configure("en","questions");vm.move(ClockTime(3,30));vm.listen()
                assertEquals("Half past three",engine.spoken.last().text)
                vm.configure("de","off");vm.listen()
                assertEquals(count+1,engine.spoken.size)
                vm.setVisible(false);vm.setVisible(true)
                assertEquals(count+1,engine.spoken.size)
            }
            assertFalse(File(directory,"native-progress").exists())
        } finally {compose.runOnUiThread {owners.clear()}}
    }
}
