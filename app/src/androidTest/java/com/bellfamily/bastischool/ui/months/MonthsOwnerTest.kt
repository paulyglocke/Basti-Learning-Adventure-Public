package com.bellfamily.bastischool.ui.months

import android.app.Application
import android.content.Context
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.lifecycle.ViewModelStore
import androidx.test.core.app.ApplicationProvider
import com.bellfamily.bastischool.audio.*
import com.bellfamily.bastischool.learning.models.ContentLanguage
import com.bellfamily.bastischool.learning.months.*
import com.bellfamily.bastischool.learning.progress.AtomicProgressStorage
import com.bellfamily.bastischool.learning.progress.android.AndroidAtomicCommit
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class MonthsOwnerTest {
    @get:Rule val compose=createEmptyComposeRule()
    @get:Rule val temp=TemporaryFolder()
    private class App(context:Context,private val directory:File):Application() {
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
    @Test fun exactSilentRestoreLanguageRapidSelectionsAndNoProgress() {
        val directory=temp.newFolder()
        val app=App(ApplicationProvider.getApplicationContext(),directory)
        val owners=ViewModelStore();val engine=Engine();var factories=0
        lateinit var vm:MonthsViewModel
        val disk=MonthsSelectionStore(AtomicProgressStorage(File(directory,"months-selection"),AndroidAtomicCommit))
        fun open() {
            compose.runOnUiThread {vm=MonthsViewModel(app){factories++;engine};owners.put("months",vm);vm.configure("de","all");vm.setVisible(true)}
            compose.waitUntil(10_000){var ready=false;compose.runOnUiThread{ready=vm.selection!=null};ready}
        }
        try {
            open()
            compose.runOnUiThread {
                assertEquals(0,factories);assertTrue(engine.spoken.isEmpty())
                vm.select(MonthIds.MARCH)
                assertEquals("März",engine.spoken.last().text)
                assertEquals(ContentLanguage.GERMAN,engine.spoken.last().context.language)
                val stops=engine.stops
                vm.configure("en","questions")
                assertTrue(engine.stops>stops)
                assertEquals(MonthIds.MARCH,vm.selection!!.selected)
                vm.listen();assertEquals("March",engine.spoken.last().text)
                vm.configure("de","off")
                MonthIds.canonicalOrder.forEach(vm::select)
                assertEquals(2,engine.spoken.size)
                vm.setVisible(false)
            }
            compose.waitUntil(10_000){runCatching{disk.read()==MonthsSelection(MonthIds.DECEMBER)}.getOrDefault(false)}
            compose.runOnUiThread{owners.clear()}
            open()
            compose.runOnUiThread {
                assertEquals(MonthIds.DECEMBER,vm.selection!!.selected)
                assertEquals(1,factories);assertEquals(2,engine.spoken.size)
                vm.setVisible(false);vm.select(MonthIds.JANUARY);vm.listen();vm.setVisible(true)
                assertEquals(MonthIds.DECEMBER,vm.selection!!.selected)
                assertEquals(2,engine.spoken.size)
                vm.select(MonthIds.DECEMBER);assertEquals("Dezember",engine.spoken.last().text)
            }
            assertEquals(setOf("months-selection"),directory.listFiles()!!.map{it.name}.toSet())
        } finally {compose.runOnUiThread{owners.clear()}}
    }
    @Test fun unreadableCheckpointIsPreservedUntilItCanBeRead() {
        val directory=temp.newFolder();val journal=File(directory,"months-selection").apply{mkdirs()}
        val bytes=byteArrayOf(0,0,0,99)
        File(journal,"events.bin").writeBytes(bytes)
        val app=App(ApplicationProvider.getApplicationContext(),directory)
        val owners=ViewModelStore();var engines=0
        lateinit var vm:MonthsViewModel
        try {
            compose.runOnUiThread{vm=MonthsViewModel(app){engines++;Engine()};owners.put("months",vm);vm.setVisible(true)}
            compose.waitUntil(10_000){var failed=false;compose.runOnUiThread{failed=vm.saveFailed};failed}
            compose.runOnUiThread{vm.select(MonthIds.MARCH);assertNull(vm.selection);assertEquals(0,engines)}
            assertArrayEquals(bytes,File(journal,"events.bin").readBytes())
            MonthsSelectionStore(AtomicProgressStorage(journal,AndroidAtomicCommit)).write(MonthsSelection(MonthIds.MAY))
            compose.runOnUiThread{vm.retry()}
            compose.waitUntil(10_000){var ready=false;compose.runOnUiThread{ready=vm.selection!=null};ready}
            compose.runOnUiThread{assertEquals(MonthIds.MAY,vm.selection!!.selected);assertFalse(vm.saveFailed);assertEquals(0,engines)}
        } finally {compose.runOnUiThread{owners.clear()}}
    }
}
