package com.bellfamily.bastischool.ui.patterns

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.lifecycle.ViewModelProvider
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import com.bellfamily.bastischool.*
import com.bellfamily.bastischool.learning.patterns.*
import com.bellfamily.bastischool.learning.models.ContentLanguage
import com.bellfamily.bastischool.learning.progress.*
import com.bellfamily.bastischool.learning.progress.android.AndroidProgressRepository
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import java.io.File

internal fun isolatedPatternStores(block:()->Unit) {
    val app=InstrumentationRegistry.getInstrumentation().targetContext
    val paths=(PatternLevel.entries.map { "pattern-builder-${it.key}-session" }+"native-progress").map{File(app.noBackupFilesDir,it)}
    val backup=File(app.cacheDir,"pattern-test-backup-${System.nanoTime()}").apply{check(mkdirs())}
    try {paths.forEach{if(it.exists())check(it.renameTo(File(backup,it.name)))};block()}
    finally {paths.forEach{check(!it.exists()||it.deleteRecursively());val saved=File(backup,it.name);if(saved.exists())check(saved.renameTo(it))};check(backup.deleteRecursively())}
}
class PatternRouteTest {
    @get:Rule val compose=createEmptyComposeRule()
    @Test fun allThreeRulesKeepTheirExamplesRestoreEditedAndCheckedWorkAndCompleteFiveAndTenRounds()=isolatedPatternStores {
        val context=InstrumentationRegistry.getInstrumentation().targetContext
        val prefs=context.getSharedPreferences("basti_shell",0)
        val oldLang=prefs.getString("lang",null);val oldAudio=prefs.getString("audioMode",null);val oldRound=prefs.getInt("round",5)
        try {
            prefs.edit().putString("lang","en").putString("audioMode","off").putInt("round",5).commit()
            ActivityScenario.launch(MainActivity::class.java).use { scenario ->
                fun model():PatternBuilderViewModel {var vm:PatternBuilderViewModel?=null;scenario.onActivity{vm=ViewModelProvider(it)[PatternBuilderViewModel::class.java]};return vm!!}
                fun settled(){compose.waitUntil(20_000){var ready=false;scenario.onActivity{val vm=ViewModelProvider(it)[PatternBuilderViewModel::class.java];ready=vm.state!=null&&!vm.busy&&!vm.saveFailed};ready}}
                fun state()=model().state!!
                fun click(tag:String){compose.onNodeWithTag(tag).performScrollTo().performClick();settled()}
                fun options(){compose.onNodeWithTag("shell-options").performClick();compose.waitUntil(10_000){compose.onAllNodesWithTag("options-screen").fetchSemanticsNodes().size==1}}
                fun back(){scenario.onActivity{it.onBackPressedDispatcher.onBackPressed()}}
                compose.openHomeActivity("Pattern Builder");compose.onNodeWithTag("pattern-chooser").assertExists()
                captureNativeDisplayReview("pattern-chooser-en",compose)
                for(level in PatternLevel.entries) {
                    if(level==PatternLevel.ABC){options();compose.onNodeWithTag("options-round-10").performScrollTo().performClick();back()}
                    compose.onNodeWithTag("pattern-start-${level.key}").performScrollTo().performClick();settled()
                    assertEquals(level,state().level);assertEquals(if(level==PatternLevel.ABC)10 else 5,state().tasks.size)
                    compose.onNodeWithTag("pattern-check").performScrollTo().assertIsNotEnabled()
                    compose.onNodeWithTag("pattern-given-0-0",useUnmergedTree=true).assert(SemanticsMatcher.keyNotDefined(SemanticsActions.OnClick))
                    val plan=state().tasks
                    val wrongSymbol=state().task.choices.first{it!=state().task.unit[0]}
                    click("pattern-choice-${wrongSymbol.value}");assertEquals(0,state().current.attempts)
                    val partial=state().placed
                    scenario.recreate();settled();assertEquals(plan,state().tasks);assertEquals(partial,state().placed)
                    click("pattern-listen-${wrongSymbol.value}");assertEquals(partial,state().placed);assertEquals(0,state().current.attempts)
                    if(level==PatternLevel.AB) {
                        options();compose.onNodeWithText("🇩🇪 Deutsch").performClick();scenario.recreate()
                        compose.waitUntil(10_000){compose.onAllNodesWithTag("options-screen").fetchSemanticsNodes().size==1};back();settled()
                        assertEquals(ContentLanguage.GERMAN,state().language);assertEquals(plan,state().tasks);assertEquals(partial,state().placed)
                    }
                    compose.onNodeWithTag("pattern-slot-0").performScrollTo();captureNativeDisplayReview("pattern-${level.key}-partial-de",compose)
                    click("pattern-undo");assertTrue(state().placed.all{it==null})
                    val task=state().task
                    task.unit.forEach {expected->click("pattern-choice-${task.choices.first{it!=expected}.value}")}
                    click("pattern-check");assertTrue(state().needsCorrection);assertEquals(1,state().current.attempts)
                    compose.onNodeWithTag("pattern-check").assertIsNotEnabled()
                    val wrong=state();scenario.recreate();settled();assertEquals(wrong,state())
                    captureNativeDisplayReview("pattern-${level.key}-retry-de",compose)
                    click("pattern-help");assertTrue(state().current.support.hint);assertEquals(wrong.placed,state().placed)
                    state().placed.indices.forEach{click("pattern-slot-$it")}
                    val captured=mutableSetOf<PatternContext>()
                    while(!state().completed) {
                        val current=state()
                        current.task.unit.forEach{id->click("pattern-choice-${id.value}")}
                        assertEquals(current.current.attempts,state().current.attempts)
                        click("pattern-check");assertTrue(state().current.solved)
                        compose.onNodeWithTag("pattern-slot-0").assertIsNotEnabled()
                        if(captured.add(current.task.context)) {
                            compose.onNodeWithTag("pattern-slot-0").performScrollTo()
                            captureNativeDisplayReview("pattern-${level.key}-${current.task.context.key}-solved-de",compose)
                        }
                        if(current.index==0){val locked=state();scenario.recreate();settled();assertEquals(locked,state())}
                        click("pattern-next")
                    }
                    assertEquals(PatternContext.entries.toSet(),captured)
                    assertTrue(state().acknowledged);compose.onNodeWithTag("completion-celebration").assertExists()
                    compose.onNodeWithTag("balloon-0").performClick()
                    compose.onNodeWithTag("balloon-0").assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription,"Tier entdeckt"))
                    captureNativeDisplayReview("pattern-${level.key}-complete-de",compose)
                    compose.onNodeWithTag("pattern-replay").performClick();settled();assertTrue(state().completed)
                    click("pattern-levels");compose.onNodeWithTag("pattern-chooser").assertExists()
                }
                captureNativeDisplayReview("pattern-chooser-de",compose)
                back();compose.onNodeWithTag("home-all").assertExists()
                val events=(AndroidProgressRepository.create(context).read()as ProgressReadResult.Events).records.map{it.event}
                assertEquals(23,events.filterIsInstance<AttemptEvent>().size)
                val completions=events.filterIsInstance<CompletionEvent>();assertEquals(3,completions.size)
                assertEquals(listOf(5,5,10),completions.map{it.tasks.size})
                assertTrue(completions.all{it.tasks.first().support.hint})
            }
        }finally{prefs.edit().putString("lang",oldLang).putString("audioMode",oldAudio).putInt("round",oldRound).commit()}
    }
}
