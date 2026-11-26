package com.bellfamily.bastischool.ui.length

import android.view.View
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.lifecycle.ViewModelProvider
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import com.bellfamily.bastischool.MainActivity
import com.bellfamily.bastischool.openHomeActivity
import com.bellfamily.bastischool.learning.length.*
import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.progress.*
import com.bellfamily.bastischool.learning.progress.android.AndroidProgressRepository
import java.io.File
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

internal fun isolatedLengthOrderingStores(block: () -> Unit) {
    val app=InstrumentationRegistry.getInstrumentation().targetContext
    val stores=listOf("length-order-session","native-progress").map { File(app.noBackupFilesDir,it) }
    val backup=File(app.cacheDir,"length-order-backup-${System.nanoTime()}").apply { check(mkdirs()) }
    val moved=mutableSetOf<File>();var isolated=false
    try {
        stores.forEach { if(it.exists()) { check(it.renameTo(File(backup,it.name)));moved+=it } };isolated=true;block()
    } finally {
        stores.forEach { if(isolated||it in moved) {
            check(!it.exists()||it.deleteRecursively());val old=File(backup,it.name);if(old.exists())check(old.renameTo(it))
        } };check(backup.deleteRecursively())
    }
}

class LengthOrderingRouteTest {
    @get:Rule val compose=createEmptyComposeRule()
    @Test fun orderUndoRemoveRetryRestoreFiveAndTenAndKeepComparisonJournal()=isolatedLengthOrderingStores {
        val context=InstrumentationRegistry.getInstrumentation().targetContext;val prefs=context.getSharedPreferences("basti_shell",0)
        val oldLang=prefs.getString("lang",null);val oldAudio=prefs.getString("audioMode",null);val oldRound=prefs.getInt("round",5)
        val comparison=File(context.noBackupFilesDir,"length-comparison-session/events.bin")
        val oldComparison=if(comparison.exists())comparison.readBytes() else null
        try {
            prefs.edit().putString("lang","en").putString("audioMode","off").putInt("round",5).commit()
            ActivityScenario.launch(MainActivity::class.java).use { scenario ->
                fun model():LengthOrderingViewModel {var vm:LengthOrderingViewModel?=null;scenario.onActivity {vm=ViewModelProvider(it)[LengthOrderingViewModel::class.java]};return vm!!}
                fun settled()=compose.waitUntil(20_000) {var ready=false;scenario.onActivity {val vm=ViewModelProvider(it)[LengthOrderingViewModel::class.java];ready=vm.state!=null&&!vm.busy&&!vm.saveFailed};ready}
                fun state()=model().state!!
                fun click(tag:String) {val node=compose.onNodeWithTag(tag);if(!tag.startsWith("length-order-source-")&&!tag.startsWith("length-order-place-")&&!state().completed)node.performScrollTo();node.performClick();settled()}
                fun place(id:ContentId)=click("length-order-source-${id.value.takeLast(1)}")
                fun arrange(ids:List<ContentId>) {while(state().current.order.isNotEmpty())click("length-order-place-1");ids.forEach(::place)}
                fun back()=scenario.onActivity {it.onBackPressedDispatcher.onBackPressed()}
                fun options() {compose.onNodeWithTag("shell-options").performClick();compose.waitUntil(10_000) {compose.onAllNodesWithTag("options-screen").fetchSemanticsNodes().size==1}}
                fun capture(name:String) {lateinit var decor:View;scenario.onActivity {decor=it.window.decorView};captureLengthOrderingReview(name,compose,state(),decor)}
                compose.openHomeActivity("Order by Length");settled()
                for(count in listOf(5,10)) {
                    assertEquals(count,state().tasks.size);var first=true
                    while(!state().completed) {
                        if(first) {
                            capture("length-order-$count-start-${if(state().language==ContentLanguage.GERMAN)"de"else"en"}")
                            compose.onNodeWithTag("length-order-check").performScrollTo().assertIsNotEnabled()
                            place(state().task.presentation.first());val partial=state();scenario.recreate();settled();assertEquals(partial,state())
                            assertEquals(0,state().current.attempts)
                            capture("length-order-$count-partial-${if(state().language==ContentLanguage.GERMAN)"de"else"en"}")
                            click("length-order-undo");assertTrue(state().current.order.isEmpty())
                            state().task.presentation.take(2).forEach(::place);val two=state()
                            click("length-order-place-1");assertEquals(two.current.order.drop(1),state().current.order)
                            click("length-order-undo");assertEquals(two.current.order,state().current.order)
                            arrange(state().task.target.reversed());click("length-order-check");assertTrue(state().needsCorrection)
                            compose.onNodeWithTag("length-order-check").assertIsNotEnabled()
                            val wrong=state();scenario.recreate();settled();assertEquals(wrong,state())
                            compose.onNodeWithTag("length-order-feedback").performScrollTo().assertIsDisplayed();capture("length-order-$count-retry")
                            click("length-order-help");val supported=state();assertTrue(supported.current.support.hint)
                            options();compose.onNodeWithText("🇩🇪 Deutsch").performClick();scenario.recreate()
                            compose.waitUntil(10_000) {compose.onAllNodesWithTag("options-screen").fetchSemanticsNodes().size==1};back();settled()
                            assertEquals(ContentLanguage.GERMAN,state().language);assertEquals(supported.tasks,state().tasks);assertEquals(supported.work,state().work);assertEquals(supported.undo,state().undo)
                        }
                        arrange(state().task.target);click("length-order-check");assertTrue(state().solved)
                        if(first) {
                            capture("length-order-$count-solved-de");val solved=state();scenario.recreate();settled();assertEquals(solved,state());first=false
                        }
                        click("length-order-next")
                    }
                    assertTrue(state().acknowledged);compose.onNodeWithTag("completion-celebration").assertExists()
                    compose.onNodeWithTag("balloon-0").performClick();compose.onNodeWithTag("balloon-0").assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription,"Tier entdeckt"))
                    capture("length-order-$count-complete-de")
                    if(count==5) {
                        val completed=state();options();compose.onNodeWithTag("options-round-10").performScrollTo().performClick();back();settled()
                        assertEquals(completed,state());click("length-order-again");assertNotEquals(completed.tasks,state().tasks)
                    }
                }
                click("length-order-home");compose.onNodeWithTag("home-all").assertExists()
                val events=(AndroidProgressRepository.create(context).read() as ProgressReadResult.Events).records.map {it.event}
                assertEquals(17,events.filterIsInstance<AttemptEvent>().size)
                val completions=events.filterIsInstance<CompletionEvent>();assertEquals(listOf(5,10),completions.map {it.tasks.size})
                assertTrue(completions.all {it.tasks.first().support.hint})
                assertEquals(setOf("skill.math.length.order_three"),completions.flatMap {it.tasks}.map {it.evidence.skill.value}.toSet())
                if(oldComparison==null)assertFalse(comparison.exists())else assertArrayEquals(oldComparison,comparison.readBytes())
            }
        } finally {prefs.edit().putString("lang",oldLang).putString("audioMode",oldAudio).putInt("round",oldRound).commit()}
    }
}
