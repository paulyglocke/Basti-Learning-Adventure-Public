package com.bellfamily.bastischool.ui.length

import android.app.Application
import androidx.lifecycle.ViewModelStore
import androidx.test.platform.app.InstrumentationRegistry
import com.bellfamily.bastischool.audio.*
import com.bellfamily.bastischool.learning.length.*
import com.bellfamily.bastischool.learning.models.ContentLanguage
import org.junit.Assert.*
import org.junit.Test

class LengthOrderingOwnerTest {
    private class Engine:SpeechEngine {
        override val readiness=EngineReadiness.READY
        override var onReadinessChanged:((EngineReadiness)->Unit)?=null
        val spoken=mutableListOf<EngineSpeechRequest>();var closed=false
        override fun speak(request:EngineSpeechRequest,result:(EngineResult)->Unit){spoken+=request;result(EngineResult.Completed)}
        override fun stop(){}
        override fun close(){closed=true}
    }
    @Test fun offStaysLazyHiddenResultsStaySilentAndFreshOwnersRestoreExactWork()=isolatedLengthOrderingStores {
        val instrumentation=InstrumentationRegistry.getInstrumentation()
        val app=instrumentation.targetContext.applicationContext as Application
        val engine=Engine();var created=0;val first=ViewModelStore();val second=ViewModelStore()
        lateinit var vm:LengthOrderingViewModel
        fun main(block:()->Unit)=instrumentation.runOnMainSync(block)
        fun settled(){val deadline=System.nanoTime()+20_000_000_000L;do{var ready=false;main{ready=vm.state!=null&&!vm.busy&&!vm.saveFailed};if(ready)return;Thread.sleep(20)}while(System.nanoTime()<deadline);fail("Length owner did not settle")}
        try {
            main {vm=LengthOrderingViewModel(app){created++;engine};first.put("length",vm);vm.configure("en","off",10);vm.setVisible(true);vm.setVisible(false)};settled()
            main {assertEquals(10,vm.state!!.tasks.size);assertEquals(0,created);vm.setVisible(true);vm.place(vm.state!!.task.presentation.first())};settled()
            main {vm.configure("en","questions",5);vm.replay()};settled()
            main {assertEquals(1,created);assertEquals(LengthOrdering.prompt.speech.en,engine.spoken.single().text);vm.replay();vm.setVisible(false)};settled()
            lateinit var saved:LengthOrdering.State
            main {
                assertEquals(1,engine.spoken.size);saved=vm.state!!;assertEquals(2,saved.current.support.replays);assertEquals(1,saved.current.order.size)
                vm.place(vm.state!!.task.target.first());assertEquals(saved,vm.state);first.clear();assertTrue(engine.closed)
                vm=LengthOrderingViewModel(app){created++;Engine()};second.put("length",vm);vm.configure("en","questions",5);vm.setVisible(true)
            };settled()
            main {assertEquals(saved,vm.state);assertEquals(1,created);vm.configure("de","questions",5)};settled()
            main {assertEquals(saved.tasks,vm.state!!.tasks);assertEquals(saved.work,vm.state!!.work);assertEquals(ContentLanguage.GERMAN,vm.state!!.language);assertEquals(1,created);vm.setVisible(false)}
        }finally{main{first.clear();second.clear()}}
    }
}
