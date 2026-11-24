package com.bellfamily.bastischool.ui.length

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.lifecycle.ViewModelProvider
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import com.bellfamily.bastischool.*
import com.bellfamily.bastischool.learning.length.*
import com.bellfamily.bastischool.learning.models.ContentLanguage
import com.bellfamily.bastischool.learning.progress.*
import com.bellfamily.bastischool.learning.progress.android.AndroidProgressRepository
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import java.io.File

internal fun isolatedLengthStores(block:()->Unit) {
    val app=InstrumentationRegistry.getInstrumentation().targetContext
    val paths=listOf("length-comparison-session","native-progress").map{File(app.noBackupFilesDir,it)}
    val backup=File(app.cacheDir,"length-test-backup-${System.nanoTime()}").apply{check(mkdirs())}
    try{paths.forEach{if(it.exists())check(it.renameTo(File(backup,it.name)))};block()}
    finally{paths.forEach{check(!it.exists()||it.deleteRecursively());val old=File(backup,it.name);if(old.exists())check(old.renameTo(it))};check(backup.deleteRecursively())}
}
class LengthComparisonRouteTest {
    @get:Rule val compose=createEmptyComposeRule()
    @Test fun alignRestoreUndoRetryAndCompleteFiveAndTenWithSharedProgress()=isolatedLengthStores {
        val context=InstrumentationRegistry.getInstrumentation().targetContext;val prefs=context.getSharedPreferences("basti_shell",0)
        val oldLang=prefs.getString("lang",null);val oldAudio=prefs.getString("audioMode",null);val oldRound=prefs.getInt("round",5)
        try {
            prefs.edit().putString("lang","en").putString("audioMode","off").putInt("round",5).commit()
            ActivityScenario.launch(MainActivity::class.java).use { scenario ->
                fun model():LengthComparisonViewModel {var vm:LengthComparisonViewModel?=null;scenario.onActivity{vm=ViewModelProvider(it)[LengthComparisonViewModel::class.java]};return vm!!}
                fun settled(){compose.waitUntil(20_000){var ready=false;scenario.onActivity{val vm=ViewModelProvider(it)[LengthComparisonViewModel::class.java];ready=vm.state!=null&&!vm.busy&&!vm.saveFailed};ready}}
                fun state()=model().state!!
                fun click(tag:String){val node=compose.onNodeWithTag(tag);if(!tag.startsWith("length-strip-")&&!state().completed)node.performScrollTo();node.performClick();settled()}
                fun capture(name:String) {
                    lateinit var decor:android.view.View
                    scenario.onActivity{decor=it.window.decorView}
                    captureLengthReview(name,compose,state(),decor)
                }
                fun options(){compose.onNodeWithTag("shell-options").performClick();compose.waitUntil(10_000){compose.onAllNodesWithTag("options-screen").fetchSemanticsNodes().size==1}}
                fun back(){scenario.onActivity{it.onBackPressedDispatcher.onBackPressed()}}
                compose.openHomeActivity("Longer / Shorter");settled()
                for(count in listOf(5,10)) {
                    assertEquals(count,state().tasks.size);var first=true
                    while(!state().completed) {
                        if(first) {
                            capture("length-$count-start-${if(state().language==ContentLanguage.GERMAN)"de"else"en"}")
                            val initial=state();click("length-answer-${initial.task.relation.name.lowercase()}")
                            compose.onNodeWithTag("length-check").assertIsNotEnabled();assertEquals(0,state().current.attempts)
                            click("length-strip-a");val selected=state();assertEquals(Strip.A,selected.selected)
                            scenario.recreate();settled();assertEquals(selected,state())
                            click("length-align");val moved=state();assertEquals(0,moved.positions.a);assertEquals(initial.task,moved.task)
                            assertEquals(0,moved.current.attempts);scenario.recreate();settled();assertEquals(moved,state())
                            capture("length-$count-moved-${if(state().language==ContentLanguage.GERMAN)"de"else"en"}")
                            click("length-undo");assertEquals(initial.positions,state().positions)
                        }
                        for(strip in Strip.entries) {
                            if(state().selected!=strip)click("length-strip-${strip.name.lowercase()}")
                            click("length-align")
                        }
                        assertTrue(state().positions.aligned)
                        if(first) {
                            val wrong=LengthRelation.entries.first{it!=state().task.relation}
                            click("length-answer-${wrong.name.lowercase()}");val chosen=state();scenario.recreate();settled();assertEquals(chosen,state())
                            click("length-check");assertTrue(state().needsCorrection)
                            val saved=state();scenario.recreate();settled();assertEquals(saved,state());capture("length-$count-retry")
                            click("length-help");assertTrue(state().current.support.hint)
                            options();compose.onNodeWithText("🇩🇪 Deutsch").performClick();scenario.recreate()
                            compose.waitUntil(10_000){compose.onAllNodesWithTag("options-screen").fetchSemanticsNodes().size==1};back();settled()
                            assertEquals(ContentLanguage.GERMAN,state().language);assertEquals(saved.tasks,state().tasks);assertEquals(saved.positions,state().positions)
                        }
                        click("length-answer-${state().task.relation.name.lowercase()}");click("length-check");assertTrue(state().current.solved)
                        if(first) {
                            capture("length-$count-solved-de");val solved=state();scenario.recreate();settled();assertEquals(solved,state());first=false
                        }
                        click("length-next")
                    }
                    assertTrue(state().acknowledged);compose.onNodeWithTag("completion-celebration").assertExists()
                    compose.onNodeWithTag("balloon-0").performClick();compose.onNodeWithTag("balloon-0").assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription,"Tier entdeckt"))
                    capture("length-$count-complete-de")
                    if(count==5) {
                        val completed=state();options();compose.onNodeWithTag("options-round-10").performScrollTo().performClick();back();settled()
                        assertEquals(completed,state());click("length-again");assertNotEquals(completed.tasks,state().tasks)
                    }
                }
                click("length-home");compose.onNodeWithTag("home-all").assertExists()
                val events=(AndroidProgressRepository.create(context).read()as ProgressReadResult.Events).records.map{it.event}
                assertEquals(17,events.filterIsInstance<AttemptEvent>().size)
                val completions=events.filterIsInstance<CompletionEvent>();assertEquals(listOf(5,10),completions.map{it.tasks.size})
                assertTrue(completions.all{it.tasks.first().support.hint});assertEquals(setOf("skill.math.length.direct_comparison"),completions.flatMap{it.tasks}.map{it.evidence.skill.value}.toSet())
            }
        }finally{prefs.edit().putString("lang",oldLang).putString("audioMode",oldAudio).putInt("round",oldRound).commit()}
    }
}
