package com.bellfamily.bastischool.ui.quantitychange

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.lifecycle.ViewModelProvider
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import com.bellfamily.bastischool.*
import com.bellfamily.bastischool.learning.quantitychange.*
import com.bellfamily.bastischool.learning.models.ContentLanguage
import com.bellfamily.bastischool.learning.progress.*
import com.bellfamily.bastischool.learning.progress.android.AndroidProgressRepository
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import java.io.File

internal fun isolatedQuantityChangeStores(block:()->Unit) {
    val app=InstrumentationRegistry.getInstrumentation().targetContext
    val paths=(ChangeConfig.all.map{"quantity-change-${it.key}-session"}+"native-progress").map{File(app.noBackupFilesDir,it)}
    val backup=File(app.cacheDir,"quantity-change-test-backup-${System.nanoTime()}").apply{check(mkdirs())}
    try{paths.forEach{if(it.exists())check(it.renameTo(File(backup,it.name)))};block()}
    finally{paths.forEach{check(!it.exists()||it.deleteRecursively());val old=File(backup,it.name);if(old.exists())check(old.renameTo(it))};check(backup.deleteRecursively())}
}
class QuantityChangeRouteTest {
    @get:Rule val compose=createEmptyComposeRule()
    @Test fun allFourChoicesRestoreEditsUndoWrongAndSolvedWorkAndCompleteFiveAndTenRounds()=isolatedQuantityChangeStores {
        val context=InstrumentationRegistry.getInstrumentation().targetContext
        val prefs=context.getSharedPreferences("basti_shell",0)
        val oldLang=prefs.getString("lang",null);val oldAudio=prefs.getString("audioMode",null);val oldRound=prefs.getInt("round",5)
        try {
            prefs.edit().putString("lang","en").putString("audioMode","off").putInt("round",5).commit()
            ActivityScenario.launch(MainActivity::class.java).use { scenario->
                fun model():QuantityChangeViewModel {var vm:QuantityChangeViewModel?=null;scenario.onActivity{vm=ViewModelProvider(it)[QuantityChangeViewModel::class.java]};return vm!!}
                fun settled(){compose.waitUntil(20_000){var ready=false;scenario.onActivity{val vm=ViewModelProvider(it)[QuantityChangeViewModel::class.java];ready=vm.state!=null&&!vm.busy&&!vm.saveFailed};ready}}
                fun state()=model().state!!
                fun click(tag:String){compose.onNodeWithTag(tag).performScrollTo().performClick();settled()}
                fun options(){compose.onNodeWithTag("shell-options").performClick();compose.waitUntil(10_000){compose.onAllNodesWithTag("options-screen").fetchSemanticsNodes().size==1}}
                fun back(){scenario.onActivity{it.onBackPressedDispatcher.onBackPressed()}}
                compose.openHomeActivity("One More / One Less");compose.onNodeWithTag("quantity-change-chooser").assertExists()
                captureNativeDisplayReview("quantity-change-chooser-en",compose)
                for(c in ChangeConfig.all) {
                    options();compose.onNodeWithTag("options-round-${c.range.limit}").performScrollTo().performClick();back()
                    compose.onNodeWithTag("quantity-change-start-${c.key}").performScrollTo().performClick();settled()
                    assertEquals(c,state().config);assertEquals(c.range.limit,state().tasks.size)
                    compose.onNodeWithTag("quantity-change-given-0",useUnmergedTree=true).assert(SemanticsMatcher.keyNotDefined(SemanticsActions.OnClick))
                    val initial=state();click("quantity-change-slot-0");val edited=state()
                    assertEquals(0,edited.current.attempts);scenario.recreate();settled();assertEquals(edited,state())
                    if(c==ChangeConfig.all.first()) {
                        options();compose.onNodeWithText("🇩🇪 Deutsch").performClick();scenario.recreate()
                        compose.waitUntil(10_000){compose.onAllNodesWithTag("options-screen").fetchSemanticsNodes().size==1};back();settled()
                        assertEquals(ContentLanguage.GERMAN,state().language);assertEquals(edited.tasks,state().tasks);assertEquals(edited.mask,state().mask);assertEquals(edited.undoMasks,state().undoMasks)
                    }
                    compose.onNodeWithTag("quantity-change-board").performScrollTo();captureNativeDisplayReview("quantity-change-${c.key}-partial-de",compose)
                    click("quantity-change-undo");assertEquals(initial.mask,state().mask);assertEquals(0,state().current.attempts)
                    click("quantity-change-check");assertTrue(state().needsCorrection);assertEquals(1,state().current.attempts)
                    compose.onNodeWithTag("quantity-change-check").assertIsNotEnabled()
                    val wrong=state();scenario.recreate();settled();assertEquals(wrong,state())
                    captureNativeDisplayReview("quantity-change-${c.key}-retry-de",compose)
                    click("quantity-change-help");assertTrue(state().current.support.hint);assertEquals(wrong.mask,state().mask)
                    var first=true;var capturedZero=false
                    while(!state().completed) {
                        val current=state()
                        if(c.mode==ChangeMode.MORE&&current.task.start==0) {
                            compose.onNodeWithTag("quantity-change-zero").performScrollTo().assertExists()
                            captureNativeDisplayReview("quantity-change-${c.key}-from-zero-de",compose)
                        }
                        val slot=if(c.mode==ChangeMode.MORE)current.task.start else current.task.start-1
                        click("quantity-change-slot-$slot");assertEquals(current.current.attempts,state().current.attempts)
                        click("quantity-change-check");assertTrue(state().current.solved)
                        compose.onNodeWithTag("quantity-change-slot-0").assertIsNotEnabled()
                        compose.onNodeWithTag("quantity-change-equation").performScrollTo().assertExists()
                        if(first) {
                            captureNativeDisplayReview("quantity-change-${c.key}-solved-de",compose)
                            val locked=state();scenario.recreate();settled();assertEquals(locked,state());first=false
                        }
                        if(state().answer==0) {
                            compose.onNodeWithTag("quantity-change-zero").performScrollTo().assertIsDisplayed()
                            captureNativeDisplayReview("quantity-change-${c.key}-zero-de",compose);capturedZero=true
                        }
                        click("quantity-change-next")
                    }
                    if(c.mode==ChangeMode.LESS)assertTrue(capturedZero)
                    assertTrue(state().acknowledged);compose.onNodeWithTag("completion-celebration").assertExists()
                    compose.onNodeWithTag("balloon-0").performClick()
                    compose.onNodeWithTag("balloon-0").assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription,"Tier entdeckt"))
                    captureNativeDisplayReview("quantity-change-${c.key}-complete-de",compose)
                    compose.onNodeWithTag("quantity-change-replay").performClick();settled();assertTrue(state().completed)
                    click("quantity-change-levels");compose.onNodeWithTag("quantity-change-chooser").assertExists()
                }
                captureNativeDisplayReview("quantity-change-chooser-de",compose)
                back();compose.onNodeWithTag("home-all").assertExists()
                val events=(AndroidProgressRepository.create(context).read()as ProgressReadResult.Events).records.map{it.event}
                assertEquals(34,events.filterIsInstance<AttemptEvent>().size)
                val completions=events.filterIsInstance<CompletionEvent>();assertEquals(4,completions.size)
                assertEquals(listOf(5,10,5,10),completions.map{it.tasks.size});assertTrue(completions.all{it.tasks.first().support.hint})
                assertTrue(completions.flatMap{it.tasks}.any{it.lastChoice.value=="number.zero"})
            }
        }finally{prefs.edit().putString("lang",oldLang).putString("audioMode",oldAudio).putInt("round",oldRound).commit()}
    }
}
