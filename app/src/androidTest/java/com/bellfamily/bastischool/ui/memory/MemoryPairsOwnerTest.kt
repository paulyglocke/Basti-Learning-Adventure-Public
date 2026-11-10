package com.bellfamily.bastischool.ui.memory

import android.app.Application
import androidx.lifecycle.ViewModelStore
import androidx.test.platform.app.InstrumentationRegistry
import com.bellfamily.bastischool.audio.*
import com.bellfamily.bastischool.learning.memory.*
import com.bellfamily.bastischool.learning.models.ContentLanguage
import org.junit.Assert.*
import org.junit.Test

class MemoryPairsOwnerTest {
    private class Engine:SpeechEngine {
        override val readiness=EngineReadiness.READY
        override var onReadinessChanged:((EngineReadiness)->Unit)?=null
        val spoken=mutableListOf<EngineSpeechRequest>()
        var stops=0;var closed=false
        override fun speak(request:EngineSpeechRequest,result:(EngineResult)->Unit){spoken+=request;result(EngineResult.Completed)}
        override fun stop(){stops++}
        override fun close(){closed=true}
    }
    @Test fun offIsLazyFreshOwnerRestoresSilentlyAndSwitchingWhileLoadingCannotPublishAnotherBoard()=isolatedMemoryStores {
        val instrumentation=InstrumentationRegistry.getInstrumentation()
        val app=instrumentation.targetContext.applicationContext as Application
        val engine=Engine();var created=0
        val firstStore=ViewModelStore();val secondStore=ViewModelStore()
        lateinit var vm:MemoryPairsViewModel
        fun main(block:()->Unit)=instrumentation.runOnMainSync(block)
        fun settled() {
            val deadline=System.nanoTime()+20_000_000_000L
            do {var ready=false;main{ready=vm.state!=null&&!vm.busy&&!vm.saveFailed};if(ready)return;Thread.sleep(20)}while(System.nanoTime()<deadline)
            fail("Memory owner failed to settle")
        }
        try {
            main {
                vm=MemoryPairsViewModel(app){created++;engine};firstStore.put("memory",vm)
                vm.configure("en","off");vm.setVisible(true,MemoryConfig(MemoryMode.ANIMALS,2))
                vm.setVisible(false);vm.setVisible(true,MemoryConfig(MemoryMode.NUMBERS,4))
            }
            settled();main {
                assertEquals(MemoryConfig(MemoryMode.NUMBERS,4),vm.state!!.config);assertEquals(0,created)
                vm.reveal(0)
            };settled()
            lateinit var saved:MemoryPairs.State
            main {saved=vm.state!!;vm.configure("en","questions");vm.replay()};settled()
            main {
                assertEquals(1,created);assertEquals(1,engine.spoken.size)
                val count=engine.spoken.size;vm.setVisible(false);vm.reveal(1);vm.replay();assertEquals(count,engine.spoken.size)
                saved=vm.state!!;assertEquals(listOf(0),saved.revealed);firstStore.clear();assertTrue(engine.closed)
                vm=MemoryPairsViewModel(app){created++;Engine()};secondStore.put("memory",vm)
                vm.configure("en","questions");vm.setVisible(true,saved.config)
            };settled()
            main {assertEquals(saved,vm.state);assertEquals(1,created);vm.configure("de","questions")};settled()
            main {
                assertEquals(saved.deck,vm.state!!.deck);assertEquals(saved.revealed,vm.state!!.revealed)
                assertEquals(ContentLanguage.GERMAN,vm.state!!.language);assertEquals(1,created)
                vm.setVisible(false);secondStore.clear()
            }
        }finally {main{firstStore.clear();secondStore.clear()}}
    }
}
