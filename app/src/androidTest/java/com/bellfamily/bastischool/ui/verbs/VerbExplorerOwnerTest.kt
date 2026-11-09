package com.bellfamily.bastischool.ui.verbs

import android.app.Application
import androidx.lifecycle.ViewModelStore
import androidx.test.platform.app.InstrumentationRegistry
import com.bellfamily.bastischool.audio.*
import com.bellfamily.bastischool.learning.models.ContentLanguage
import com.bellfamily.bastischool.learning.verbs.VerbContent
import org.junit.Assert.*
import org.junit.Test

class VerbExplorerOwnerTest {
    private class Engine:SpeechEngine {
        override val readiness=EngineReadiness.READY
        override var onReadinessChanged:((EngineReadiness)->Unit)?=null
        val spoken=mutableListOf<EngineSpeechRequest>()
        val callbacks=mutableMapOf<SpeechRequestId,(EngineResult)->Unit>()
        var closed=false
        override fun speak(request:EngineSpeechRequest,result:(EngineResult)->Unit){spoken+=request;callbacks[request.id]=result}
        override fun stop(){}
        override fun close(){closed=true}
    }
    @Test fun soundOffIsLazyBackgroundDropsPendingEntranceAndOnlyCurrentCompletedIntroductionIsConsumed() {
        val instrumentation=InstrumentationRegistry.getInstrumentation()
        val app=instrumentation.targetContext.applicationContext as Application
        val prefs=app.getSharedPreferences("basti_shell",0)
        val keys=listOf("nativeVerbIntro_v1_explorer_ENGLISH","nativeVerbIntro_v1_explorer_GERMAN")
        val before=keys.associateWith{key->prefs.all[key] as? Long}
        val engine=Engine();var created=0
        val store=ViewModelStore()
        try {
            prefs.edit().putLong(keys[0],-1).putLong(keys[1],-1).commit()
            instrumentation.runOnMainSync {
                val model=VerbExplorerViewModel(app){created++;engine};store.put("explorer-test",model)
                val lesson=VerbContent.byId.getValue("verb.jump")
                model.configure("en","off");model.prepareLesson(lesson.id.value);model.setVisible(true);model.entered(lesson)
                assertEquals(0,created)
                model.configure("en","questions");model.prepareLesson(lesson.id.value)
                model.setVisible(false);model.setVisible(true);model.entered(lesson)
                assertEquals(0,created) // A late load/entrance after suspension cannot restart narration.
                model.prepareLesson(lesson.id.value);model.entered(lesson)
                assertEquals(1,created);assertEquals(1,engine.spoken.size)
                assertTrue(engine.spoken.last().text.startsWith(VerbContent.explorerIntroduction.speech.en))
                val stale=engine.spoken.last().id
                model.setVisible(false);engine.callbacks.getValue(stale)(EngineResult.Completed)
                assertEquals(-1,prefs.getLong(keys[0],-1))
                model.configure("de","questions");model.prepareLesson(lesson.id.value);model.setVisible(true);model.entered(lesson)
                assertEquals(ContentLanguage.GERMAN,engine.spoken.last().context.language)
                engine.callbacks.getValue(engine.spoken.last().id)(EngineResult.Completed)
                assertEquals(prefs.getLong("tutorialResetEpoch",0),prefs.getLong(keys[1],-1))
                val count=engine.spoken.size
                model.setVisible(false);model.setVisible(true);model.entered(lesson)
                assertEquals(count,engine.spoken.size)
                store.clear();assertTrue(engine.closed)
            }
        }finally {
            instrumentation.runOnMainSync{store.clear()}
            val edit=prefs.edit();keys.forEach{key->before[key]?.let{edit.putLong(key,it)}?:edit.remove(key)};edit.commit()
        }
    }
}
