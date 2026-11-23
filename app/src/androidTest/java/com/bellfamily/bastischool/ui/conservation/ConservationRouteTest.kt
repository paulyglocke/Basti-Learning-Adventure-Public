package com.bellfamily.bastischool.ui.conservation

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.lifecycle.ViewModelProvider
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import com.bellfamily.bastischool.*
import com.bellfamily.bastischool.learning.conservation.*
import com.bellfamily.bastischool.learning.models.ContentLanguage
import com.bellfamily.bastischool.learning.progress.*
import com.bellfamily.bastischool.learning.progress.android.AndroidProgressRepository
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import java.io.File

internal fun isolatedConservationStores(block:()->Unit) {
    val app=InstrumentationRegistry.getInstrumentation().targetContext
    val paths=(ConservationRange.entries.map{"conservation-${it.key}-session"}+"native-progress").map{File(app.noBackupFilesDir,it)}
    val backup=File(app.cacheDir,"conservation-test-backup-${System.nanoTime()}").apply{check(mkdirs())}
    try{paths.forEach{if(it.exists())check(it.renameTo(File(backup,it.name)))};block()}
    finally{paths.forEach{check(!it.exists()||it.deleteRecursively());val old=File(backup,it.name);if(old.exists())check(old.renameTo(it))};check(backup.deleteRecursively())}
}
class ConservationRouteTest {
    @get:Rule val compose=createEmptyComposeRule()
    @Test fun bothRangesMoveUndoRestoreRetryAndCompleteWithSharedProgress()=isolatedConservationStores {
        val context=InstrumentationRegistry.getInstrumentation().targetContext;val prefs=context.getSharedPreferences("basti_shell",0)
        val oldLang=prefs.getString("lang",null);val oldAudio=prefs.getString("audioMode",null);val oldRound=prefs.getInt("round",5)
        try {
            prefs.edit().putString("lang","en").putString("audioMode","off").putInt("round",5).commit()
            ActivityScenario.launch(MainActivity::class.java).use { scenario->
                fun model():ConservationViewModel {var vm:ConservationViewModel?=null;scenario.onActivity{vm=ViewModelProvider(it)[ConservationViewModel::class.java]};return vm!!}
                fun settled(){compose.waitUntil(20_000){var ready=false;scenario.onActivity{val vm=ViewModelProvider(it)[ConservationViewModel::class.java];ready=vm.state!=null&&!vm.busy&&!vm.saveFailed};ready&&(model().state?.completed==true||compose.onAllNodesWithTag("conservation-art-ready").fetchSemanticsNodes().size==1)}}
                fun state()=model().state!!
                fun capture(name:String)=captureConservationReview(name,compose,model().state)
                fun click(tag:String){compose.onNodeWithTag(tag).performScrollTo().performClick();settled()}
                fun options(){compose.onNodeWithTag("shell-options").performClick();compose.waitUntil(10_000){compose.onAllNodesWithTag("options-screen").fetchSemanticsNodes().size==1}}
                fun back(){scenario.onActivity{it.onBackPressedDispatcher.onBackPressed()}}
                compose.openHomeActivity("Same Amount");compose.onNodeWithTag("conservation-chooser").assertExists()
                capture("conservation-chooser-en")
                for(c in ConservationRange.entries) {
                    options();compose.onNodeWithTag("options-round-${c.limit}").performScrollTo().performClick();back()
                    compose.onNodeWithTag("conservation-start-${c.key}").performScrollTo().performClick();settled()
                    assertEquals(c,state().config);assertEquals(c.limit,state().tasks.size)
                    var first=true
                    while(!state().completed) {
                        if(first) {
                            val initial=state()
                            if(initial.count>0) {
                                val from=(0 until c.slots).first{initial.mask and(1 shl it)!=0};val to=(0 until c.slots).first{initial.mask and(1 shl it)==0}
                                click("conservation-slot-$from");val selected=state();assertEquals(from,selected.selected)
                                scenario.recreate();settled();assertEquals(selected,state())
                                click("conservation-slot-$to");assertEquals(initial.count,state().count);assertEquals(0,state().current.attempts)
                                val moved=state();scenario.recreate();settled();assertEquals(moved,state())
                                capture("conservation-${c.key}-moved-${if(state().language==ContentLanguage.GERMAN)"de"else"en"}")
                                click("conservation-undo");assertEquals(initial.mask,state().mask)
                            }
                            click("conservation-answer-${QuantityRelation.entries.first{it!=state().task.relation}.name.lowercase()}")
                            val chosen=state();assertEquals(0,chosen.current.attempts);scenario.recreate();settled();assertEquals(chosen,state())
                            click("conservation-check");assertTrue(state().needsCorrection);compose.onNodeWithTag("conservation-check").assertIsNotEnabled()
                            val wrong=state();scenario.recreate();settled();assertEquals(wrong,state())
                            capture("conservation-${c.key}-retry")
                            click("conservation-help");assertTrue(state().current.support.hint);assertEquals(wrong.mask,state().mask)
                            options();compose.onNodeWithText("🇩🇪 Deutsch").performClick();scenario.recreate()
                            compose.waitUntil(10_000){compose.onAllNodesWithTag("options-screen").fetchSemanticsNodes().size==1};back();settled()
                            assertEquals(ContentLanguage.GERMAN,state().language);assertEquals(wrong.tasks,state().tasks);assertEquals(wrong.mask,state().mask)
                        }
                        val before=state();click("conservation-answer-${before.task.relation.name.lowercase()}");click("conservation-check")
                        assertTrue(state().current.solved);assertEquals(before.count,state().count)
                        if(first) {
                            capture("conservation-${c.key}-solved-de")
                            val locked=state();scenario.recreate();settled();assertEquals(locked,state());first=false
                        }
                        click("conservation-next")
                    }
                    assertTrue(state().acknowledged);compose.onNodeWithTag("completion-celebration").assertExists()
                    compose.onNodeWithTag("balloon-0").performClick();compose.onNodeWithTag("balloon-0").assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription,"Tier entdeckt"))
                    capture("conservation-${c.key}-complete-de")
                    click("conservation-levels");compose.onNodeWithTag("conservation-chooser").assertExists()
                }
                capture("conservation-chooser-de");back();compose.onNodeWithTag("home-all").assertExists()
                val events=(AndroidProgressRepository.create(context).read()as ProgressReadResult.Events).records.map{it.event}
                assertEquals(17,events.filterIsInstance<AttemptEvent>().size);val completions=events.filterIsInstance<CompletionEvent>()
                assertEquals(listOf(5,10),completions.map{it.tasks.size});assertTrue(completions.all{it.tasks.first().support.hint})
                assertEquals(9,completions.flatMap{it.tasks}.count{it.evidence.skill.value.contains("quantity_conservation")})
            }
        }finally{prefs.edit().putString("lang",oldLang).putString("audioMode",oldAudio).putInt("round",oldRound).commit()}
    }
}
