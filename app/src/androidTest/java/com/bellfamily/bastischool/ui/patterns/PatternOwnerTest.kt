package com.bellfamily.bastischool.ui.patterns

import android.app.Application
import androidx.lifecycle.ViewModelStore
import androidx.test.platform.app.InstrumentationRegistry
import com.bellfamily.bastischool.audio.*
import com.bellfamily.bastischool.learning.patterns.*
import com.bellfamily.bastischool.learning.models.ContentLanguage
import org.junit.Assert.*
import org.junit.Test

class PatternOwnerTest {
    private class Engine:SpeechEngine {
        override val readiness=EngineReadiness.READY
        override var onReadinessChanged:((EngineReadiness)->Unit)?=null
        val spoken=mutableListOf<EngineSpeechRequest>();var closed=false
        override fun speak(request:EngineSpeechRequest,result:(EngineResult)->Unit){spoken+=request;result(EngineResult.Completed)}
        override fun stop(){}
        override fun close(){closed=true}
    }
    @Test fun offIsLazyLateLevelResultsAreRejectedAndFreshOwnersRestoreSilently()=isolatedPatternStores {
        val instrumentation=InstrumentationRegistry.getInstrumentation()
        val app=instrumentation.targetContext.applicationContext as Application
        val engine=Engine();var created=0;val first=ViewModelStore();val second=ViewModelStore()
        lateinit var vm:PatternBuilderViewModel
        fun main(block:()->Unit)=instrumentation.runOnMainSync(block)
        fun settled(){val deadline=System.nanoTime()+20_000_000_000L;do{var ready=false;main{ready=vm.state!=null&&!vm.busy&&!vm.saveFailed};if(ready)return;Thread.sleep(20)}while(System.nanoTime()<deadline);fail("Pattern owner did not settle")}
        try {
            main {
                vm=PatternBuilderViewModel(app){created++;engine};first.put("patterns",vm)
                vm.configure("en","off",10);vm.setVisible(true,PatternLevel.AB)
                vm.setVisible(false);vm.setVisible(true,PatternLevel.ABC)
            };settled()
            main {assertEquals(PatternLevel.ABC,vm.state!!.level);assertEquals(10,vm.state!!.tasks.size);assertEquals(0,created);vm.add(vm.state!!.task.unit[0])};settled()
            main {vm.configure("en","questions",5);vm.replay()};settled()
            main {
                assertEquals(1,created);assertEquals(1,engine.spoken.size)
                assertEquals(PatternBuilder.prompt(vm.state!!.task).speech.en,engine.spoken.single().text)
                vm.replay();vm.setVisible(false)
            };settled()
            lateinit var saved:PatternBuilder.State
            main {
                assertEquals(1,engine.spoken.size);saved=vm.state!!;assertEquals(2,saved.current.support.replays)
                assertNotNull(saved.placed[0]);assertEquals(0,saved.current.attempts)
                vm.add(saved.task.unit[1]);assertEquals(saved,vm.state);first.clear();assertTrue(engine.closed)
                vm=PatternBuilderViewModel(app){created++;Engine()};second.put("patterns",vm)
                vm.configure("en","questions",5);vm.setVisible(true,PatternLevel.ABC)
            };settled()
            main {assertEquals(saved,vm.state);assertEquals(1,created);vm.configure("de","questions",5)};settled()
            main {
                assertEquals(saved.tasks,vm.state!!.tasks);assertEquals(saved.placed,vm.state!!.placed)
                assertEquals(ContentLanguage.GERMAN,vm.state!!.language);assertEquals(1,created)
                vm.setVisible(false);second.clear()
            }
        }finally{main{first.clear();second.clear()}}
    }
}
