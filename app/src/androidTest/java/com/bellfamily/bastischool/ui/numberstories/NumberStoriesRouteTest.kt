package com.bellfamily.bastischool.ui.numberstories

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.lifecycle.ViewModelProvider
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import com.bellfamily.bastischool.*
import com.bellfamily.bastischool.learning.numberstories.*
import com.bellfamily.bastischool.learning.models.ContentLanguage
import com.bellfamily.bastischool.learning.progress.*
import com.bellfamily.bastischool.learning.progress.android.AndroidProgressRepository
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import java.io.File

internal fun isolatedNumberStoriesStores(block:()->Unit) {
    val app=InstrumentationRegistry.getInstrumentation().targetContext
    val paths=(StoryConfig.all.map{"number-stories-${it.key}-session"}+"native-progress").map{File(app.noBackupFilesDir,it)}
    val backup=File(app.cacheDir,"number-stories-test-backup-${System.nanoTime()}").apply{check(mkdirs())}
    try{paths.forEach{if(it.exists())check(it.renameTo(File(backup,it.name)))};block()}
    finally{paths.forEach{check(!it.exists()||it.deleteRecursively());val old=File(backup,it.name);if(old.exists())check(old.renameTo(it))};check(backup.deleteRecursively())}
}
class NumberStoriesRouteTest {
    @get:Rule val compose=createEmptyComposeRule()
    @Test fun allFourChoicesRestoreEditsUndoWrongAndSolvedWorkAndCompleteFiveAndTenRounds()=isolatedNumberStoriesStores {
        val context=InstrumentationRegistry.getInstrumentation().targetContext
        val prefs=context.getSharedPreferences("basti_shell",0)
        val oldLang=prefs.getString("lang",null);val oldAudio=prefs.getString("audioMode",null);val oldRound=prefs.getInt("round",5)
        try {
            prefs.edit().putString("lang","en").putString("audioMode","off").putInt("round",5).commit()
            ActivityScenario.launch(MainActivity::class.java).use { scenario->
                fun model():NumberStoriesViewModel {var vm:NumberStoriesViewModel?=null;scenario.onActivity{vm=ViewModelProvider(it)[NumberStoriesViewModel::class.java]};return vm!!}
                fun settled(){compose.waitUntil(20_000){var ready=false;scenario.onActivity{val vm=ViewModelProvider(it)[NumberStoriesViewModel::class.java];ready=vm.state!=null&&!vm.busy&&!vm.saveFailed};ready}}
                fun state()=model().state!!
                fun click(tag:String){compose.onNodeWithTag(tag).performScrollTo().performClick();settled()}
                fun options(){compose.onNodeWithTag("shell-options").performClick();compose.waitUntil(10_000){compose.onAllNodesWithTag("options-screen").fetchSemanticsNodes().size==1}}
                fun back(){scenario.onActivity{it.onBackPressedDispatcher.onBackPressed()}}
                compose.openHomeActivity("Number Stories");compose.onNodeWithTag("number-stories-chooser").assertExists()
                captureNativeDisplayReview("number-stories-chooser-en",compose)
                for(c in StoryConfig.all) {
                    options();compose.onNodeWithTag("options-round-${c.range.limit}").performScrollTo().performClick();back()
                    compose.onNodeWithTag("number-stories-start-${c.key}").performScrollTo().performClick();settled()
                    assertEquals(c,state().config);assertEquals(c.range.limit,state().tasks.size)
                    compose.onNodeWithTag("number-stories-given-0",useUnmergedTree=true).assert(SemanticsMatcher.keyNotDefined(SemanticsActions.OnClick))
                    val initial=state();click("number-stories-slot-0");val edited=state()
                    assertEquals(0,edited.current.attempts);scenario.recreate();settled();assertEquals(edited,state())
                    if(c==StoryConfig.all.first()) {
                        options();compose.onNodeWithText("🇩🇪 Deutsch").performClick();scenario.recreate()
                        compose.waitUntil(10_000){compose.onAllNodesWithTag("options-screen").fetchSemanticsNodes().size==1};back();settled()
                        assertEquals(ContentLanguage.GERMAN,state().language);assertEquals(edited.tasks,state().tasks);assertEquals(edited.mask,state().mask);assertEquals(edited.undoMasks,state().undoMasks)
                    }
                    compose.onNodeWithTag("number-stories-board").performScrollTo();captureNativeDisplayReview("number-stories-${c.key.replace('_','-')}-partial-de",compose)
                    click("number-stories-undo");assertEquals(initial.mask,state().mask);assertEquals(0,state().current.attempts)
                    click("number-stories-check");assertTrue(state().needsCorrection);assertEquals(1,state().current.attempts)
                    compose.onNodeWithTag("number-stories-check").assertIsNotEnabled()
                    val wrong=state();scenario.recreate();settled();assertEquals(wrong,state())
                    captureNativeDisplayReview("number-stories-${c.key.replace('_','-')}-retry-de",compose)
                    click("number-stories-help");assertTrue(state().current.support.hint);assertEquals(wrong.mask,state().mask)
                    var first=true;var capturedZero=false
                    while(!state().completed) {
                        val current=state()
                        val desired=(1 shl current.answer)-1
                        for(slot in 0 until c.range.limit)if((state().mask xor desired)and(1 shl slot)!=0)click("number-stories-slot-$slot")
                        assertEquals(current.current.attempts,state().current.attempts)
                        click("number-stories-check");assertTrue(state().current.solved)
                        compose.onNodeWithTag("number-stories-slot-0").assertIsNotEnabled()
                        compose.onNodeWithTag("number-stories-equation").performScrollTo().assertExists()
                        if(first) {
                            captureNativeDisplayReview("number-stories-${c.key.replace('_','-')}-solved-de",compose)
                            val locked=state();scenario.recreate();settled();assertEquals(locked,state());first=false
                        }
                        if(state().answer==0) {
                            compose.onNodeWithTag("number-stories-zero").performScrollTo().assertIsDisplayed()
                            captureNativeDisplayReview("number-stories-${c.key.replace('_','-')}-zero-de",compose);capturedZero=true
                        }
                        click("number-stories-next")
                    }
                    if(c.mode==StoryMode.TAKE_AWAY)assertTrue(capturedZero)
                    assertTrue(state().acknowledged);compose.onNodeWithTag("completion-celebration").assertExists()
                    compose.onNodeWithTag("balloon-0").performClick()
                    compose.onNodeWithTag("balloon-0").assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription,"Tier entdeckt"))
                    captureNativeDisplayReview("number-stories-${c.key.replace('_','-')}-complete-de",compose)
                    compose.onNodeWithTag("number-stories-replay").performClick();settled();assertTrue(state().completed)
                    click("number-stories-levels");compose.onNodeWithTag("number-stories-chooser").assertExists()
                }
                captureNativeDisplayReview("number-stories-chooser-de",compose)
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
