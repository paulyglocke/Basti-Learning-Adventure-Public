package com.bellfamily.bastischool.ui.wilma

import android.app.Application
import android.content.Context
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.lifecycle.ViewModelStore
import androidx.test.core.app.ApplicationProvider
import com.bellfamily.bastischool.audio.*
import com.bellfamily.bastischool.learning.models.ContentLanguage
import com.bellfamily.bastischool.learning.wilma.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class WilmaBilingualOwnerTest {
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
        override fun speak(request:EngineSpeechRequest,result:(EngineResult)->Unit) {spoken+=request;result(EngineResult.Completed)}
        override fun stop()=Unit
        override fun close()=Unit
    }

    @Test fun bilingualOwnerRoutesTapsByLanguageAndRestoresSilentlyWithoutQuizState() {
        val directory=temp.newFolder()
        val app=IsolatedApplication(ApplicationProvider.getApplicationContext(),directory)
        val store=ViewModelStore()
        val engine=Engine()
        lateinit var vm:WilmaViewModel
        fun open()=compose.runOnUiThread {
            vm=WilmaViewModel(app){engine};store.put("wilma",vm)
            vm.configure("en","all",5);vm.setVisible(true)
        }
        fun settled() {
            compose.waitUntil(10_000) {var done=false;compose.runOnUiThread {done=!vm.busy && vm.selection!=null};done}
            compose.runOnUiThread {assertFalse(vm.saveFailed)}
        }
        try {
            open();settled()
            compose.runOnUiThread {vm.phase(WilmaPhase.BILINGUAL)};settled()
            compose.runOnUiThread {
                assertTrue(engine.spoken.isEmpty())
                assertNull(vm.quiz);assertNull(vm.ordering)
                val selection=vm.selection
                for(mode in listOf("all","questions","off")) {
                    vm.configure("en",mode,5)
                    for(language in listOf(ContentLanguage.GERMAN,ContentLanguage.ENGLISH)) {
                        val before=engine.spoken.size
                        vm.bilingualDay(WilmaContent.days.first(),language)
                        assertEquals(before+if(mode=="off")0 else 1,engine.spoken.size)
                        if(mode!="off")assertEquals(language,engine.spoken.last().context.language)
                    }
                    assertEquals(selection,vm.selection)
                }
            }
            // Only browsing was persisted: no attempt/completion journals or progress directory.
            assertFalse(File(directory,"native-progress").exists())
            assertFalse(File(directory,"wilma-find").exists())
            assertFalse(File(directory,"wilma-order").exists())
            val beforeRestore=engine.spoken.size
            compose.runOnUiThread {store.clear()}
            open();settled()
            compose.runOnUiThread {
                assertEquals(WilmaPhase.BILINGUAL,vm.selection!!.phase)
                assertNull(vm.quiz);assertNull(vm.ordering)
                assertEquals(beforeRestore,engine.spoken.size)
                vm.bilingualDay(WilmaContent.days.last(),ContentLanguage.ENGLISH)
                assertEquals("Sunday",engine.spoken.last().text)
                assertEquals(ContentLanguage.ENGLISH,engine.spoken.last().context.language)
            }
        } finally {compose.runOnUiThread {store.clear()}}
    }
}
