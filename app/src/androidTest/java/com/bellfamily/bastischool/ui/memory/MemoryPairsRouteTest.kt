package com.bellfamily.bastischool.ui.memory

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.lifecycle.ViewModelProvider
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import com.bellfamily.bastischool.*
import com.bellfamily.bastischool.learning.memory.*
import com.bellfamily.bastischool.learning.models.ContentLanguage
import com.bellfamily.bastischool.learning.progress.*
import com.bellfamily.bastischool.learning.progress.android.AndroidProgressRepository
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import java.io.File

/** Test stores are restored afterwards; never clear app data or another device. */
internal fun isolatedMemoryStores(block:()->Unit) {
    val app=InstrumentationRegistry.getInstrumentation().targetContext
    val paths=(MemoryMode.entries.flatMap { mode->(2..4).map { "memory-pairs-${mode.key}-$it-session" } }+"native-progress")
        .map { File(app.noBackupFilesDir,it) }
    val backup=File(app.cacheDir,"memory-test-backup-${System.nanoTime()}").apply{check(mkdirs())}
    try {
        paths.forEach { if(it.exists())check(it.renameTo(File(backup,it.name))) }
        block()
    }finally {
        paths.forEach { check(!it.exists()||it.deleteRecursively());val saved=File(backup,it.name);if(saved.exists())check(saved.renameTo(it)) }
        check(backup.deleteRecursively())
    }
}
class MemoryPairsRouteTest {
    @get:Rule val compose=createEmptyComposeRule()
    @Test fun allSixBoardsRestoreTurnsTranslateUseSupportAndCompleteWithSharedBalloons()=isolatedMemoryStores {
        val context=InstrumentationRegistry.getInstrumentation().targetContext
        val prefs=context.getSharedPreferences("basti_shell",0)
        val oldLang=prefs.getString("lang",null);val oldAudio=prefs.getString("audioMode",null)
        try {
            prefs.edit().putString("lang","en").putString("audioMode","off").commit()
            ActivityScenario.launch(MainActivity::class.java).use { scenario ->
                fun model():MemoryPairsViewModel {var vm:MemoryPairsViewModel?=null;scenario.onActivity{vm=ViewModelProvider(it)[MemoryPairsViewModel::class.java]};return vm!!}
                fun settled(){compose.waitUntil(20_000){var ready=false;scenario.onActivity{val vm=ViewModelProvider(it)[MemoryPairsViewModel::class.java];ready=vm.state!=null&&!vm.busy&&!vm.saveFailed};ready}}
                fun state()=model().state!!
                fun click(tag:String){val node=compose.onNodeWithTag(tag);if(state().completed)node.assertIsDisplayed().performClick()else node.performScrollTo().performClick();settled()}
                fun partner(s:MemoryPairs.State,slot:Int)=s.deck.indices.single{it!=slot&&s.deck[it].pair==s.deck[slot].pair}
                compose.openHomeActivity("Memory Pairs")
                compose.onNodeWithTag("memory-chooser").assertExists()
                captureMemoryReview("memory-chooser-en",compose)
                for(mode in MemoryMode.entries)for(pairs in 2..4) {
                    compose.onNodeWithTag("memory-start-${mode.key}-$pairs").performScrollTo().performClick();settled()
                    assertEquals(MemoryConfig(mode,pairs),state().config)
                    assertTrue(state().results.all{it.attempts==0})
                    compose.onNodeWithTag("memory-card-0").performScrollTo()
                    captureMemoryReview("memory-${mode.key}-${pairs*2}-board",compose)
                    val deck=state().deck
                    compose.onNodeWithTag("memory-card-0").assertContentDescriptionEquals(if(state().language==ContentLanguage.GERMAN)"Karte 1, verdeckt"else "Card 1, face down")
                    click("memory-card-0");assertEquals(1,state().exposures.sum());assertEquals(0,state().results.sumOf{it.attempts})
                    scenario.recreate();settled();assertEquals(deck,state().deck);assertEquals(listOf(0),state().revealed)
                    if(mode==MemoryMode.ANIMALS&&pairs==2) {
                        compose.onNodeWithTag("shell-options").performClick()
                        compose.waitUntil(10_000){compose.onAllNodesWithTag("options-screen").fetchSemanticsNodes().size==1}
                        compose.onNodeWithText("🇩🇪 Deutsch").performClick();scenario.recreate()
                        compose.waitUntil(10_000){compose.onAllNodesWithTag("options-screen").fetchSemanticsNodes().size==1}
                        scenario.onActivity{it.onBackPressedDispatcher.onBackPressed()};settled()
                        assertEquals(ContentLanguage.GERMAN,state().language);assertEquals(deck,state().deck);assertEquals(listOf(0),state().revealed)
                    }
                    val other=state().deck.indices.first{state().deck[it].pair!=state().deck[0].pair}
                    click("memory-card-$other");assertEquals(MemoryPairs.Phase.DIFFERENT,state().phase)
                    val mismatch=state();click("memory-listen-0");assertEquals(mismatch.revealed,state().revealed);assertEquals(1,state().results.sumOf{it.attempts})
                    scenario.recreate();settled();assertEquals(mismatch.revealed,state().revealed);assertEquals(MemoryPairs.Phase.DIFFERENT,state().phase)
                    captureMemoryReview("memory-${mode.key}-${pairs*2}-different-de",compose)
                    click("memory-next");click("memory-help");assertEquals(MemoryPairs.Phase.MODEL,state().phase)
                    assertEquals(1,state().results.sumOf{it.attempts});val model=state()
                    scenario.recreate();settled();assertEquals(model,state())
                    click("memory-next")
                    while(!state().completed) {
                        val s=state();val slot=s.deck.indices.first{!s.visible(it)}
                        click("memory-card-$slot");click("memory-card-${partner(s,slot)}")
                        assertEquals(MemoryPairs.Phase.MATCH,state().phase)
                        if(state().solvedPairs==1){compose.onNodeWithTag("memory-card-$slot").performScrollTo();captureMemoryReview("memory-${mode.key}-${pairs*2}-match-de",compose)}
                        click("memory-next")
                    }
                    assertTrue(state().acknowledged);compose.onNodeWithTag("completion-celebration").assertExists()
                    compose.onNodeWithTag("balloon-0").performClick()
                    compose.onNodeWithTag("balloon-0").assert(SemanticsMatcher.expectValue(androidx.compose.ui.semantics.SemanticsProperties.StateDescription,"Tier entdeckt"))
                    captureMemoryReview("memory-${mode.key}-${pairs*2}-complete-de",compose)
                    click("memory-replay");assertTrue(state().completed)
                    click("memory-boards");compose.onNodeWithTag("memory-chooser").assertExists()
                }
                val records=(AndroidProgressRepository.create(context).read()as ProgressReadResult.Events).records.map{it.event}
                assertEquals(6,records.filterIsInstance<CompletionEvent>().size)
                assertEquals(6,records.filterIsInstance<CompletionEvent>().map{it.id}.distinct().size)
                assertEquals(24,records.filterIsInstance<AttemptEvent>().size) // 18 successful pairs + six mismatches.
            }
        }finally {prefs.edit().putString("lang",oldLang).putString("audioMode",oldAudio).commit()}
    }
}
