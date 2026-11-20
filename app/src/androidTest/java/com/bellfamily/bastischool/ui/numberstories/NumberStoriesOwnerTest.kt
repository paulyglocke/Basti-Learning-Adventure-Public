package com.bellfamily.bastischool.ui.numberstories

import android.app.Application
import androidx.lifecycle.ViewModelStore
import androidx.test.platform.app.InstrumentationRegistry
import com.bellfamily.bastischool.audio.*
import com.bellfamily.bastischool.learning.numberstories.*
import com.bellfamily.bastischool.learning.models.ContentLanguage
import org.junit.Assert.*
import org.junit.Test

class NumberStoriesOwnerTest {
    private class Engine:SpeechEngine {
        override val readiness=EngineReadiness.READY
        override var onReadinessChanged:((EngineReadiness)->Unit)?=null
        val spoken=mutableListOf<EngineSpeechRequest>();var closed=false
        override fun speak(request:EngineSpeechRequest,result:(EngineResult)->Unit){spoken+=request;result(EngineResult.Completed)}
        override fun stop(){}
        override fun close(){closed=true}
    }
    @Test fun offIsLazyLateLevelResultsAreRejectedAndFreshOwnersRestoreSilently()=isolatedNumberStoriesStores {
        val instrumentation=InstrumentationRegistry.getInstrumentation()
        val app=instrumentation.targetContext.applicationContext as Application
        val engine=Engine();var created=0;val first=ViewModelStore();val second=ViewModelStore()
        lateinit var vm:NumberStoriesViewModel
        fun main(block:()->Unit)=instrumentation.runOnMainSync(block)
        fun settled(){val deadline=System.nanoTime()+20_000_000_000L;do{var ready=false;main{ready=vm.state!=null&&!vm.busy&&!vm.saveFailed};if(ready)return;Thread.sleep(20)}while(System.nanoTime()<deadline);fail("Number Stories owner did not settle")}
        try {
            main {
                vm=NumberStoriesViewModel(app){created++;engine};first.put("number-stories",vm)
                vm.configure("en","off",10);vm.setVisible(true,StoryConfig.all.first())
                vm.setVisible(false);vm.setVisible(true,StoryConfig.all.last())
            };settled()
            main {assertEquals(StoryConfig.all.last(),vm.state!!.config);assertEquals(10,vm.state!!.tasks.size);assertEquals(0,created);vm.toggle(0)};settled()
            main {vm.configure("en","questions",5);vm.replay()};settled()
            main {
                assertEquals(1,created);assertEquals(1,engine.spoken.size)
                assertEquals(NumberStories.prompt(vm.state!!.task,vm.state!!.config).speech.en,engine.spoken.single().text)
                vm.replay();vm.setVisible(false)
            };settled()
            lateinit var saved:NumberStories.State
            main {
                assertEquals(1,engine.spoken.size);saved=vm.state!!;assertEquals(2,saved.current.support.replays)
                assertTrue(saved.undoMasks.isNotEmpty());assertEquals(0,saved.current.attempts)
                vm.toggle(1);assertEquals(saved,vm.state);first.clear();assertTrue(engine.closed)
                vm=NumberStoriesViewModel(app){created++;Engine()};second.put("number-stories",vm)
                vm.configure("en","questions",5);vm.setVisible(true,StoryConfig.all.last())
            };settled()
            main {assertEquals(saved,vm.state);assertEquals(1,created);vm.configure("de","questions",5)};settled()
            main {
                assertEquals(saved.tasks,vm.state!!.tasks);assertEquals(saved.mask,vm.state!!.mask)
                assertEquals(ContentLanguage.GERMAN,vm.state!!.language);assertEquals(1,created)
                vm.setVisible(false);second.clear()
            }
        }finally{main{first.clear();second.clear()}}
    }
}
