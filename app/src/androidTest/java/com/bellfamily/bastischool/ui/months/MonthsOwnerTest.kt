package com.bellfamily.bastischool.ui.months

import android.app.Application
import android.content.Context
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.lifecycle.ViewModelStore
import androidx.test.core.app.ApplicationProvider
import com.bellfamily.bastischool.audio.*
import com.bellfamily.bastischool.learning.models.ContentLanguage
import com.bellfamily.bastischool.learning.session.*
import com.bellfamily.bastischool.learning.months.*
import com.bellfamily.bastischool.learning.progress.AtomicProgressStorage
import com.bellfamily.bastischool.learning.progress.android.AndroidAtomicCommit
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class MonthsOwnerTest {
    @get:Rule val compose=createEmptyComposeRule()
    @get:Rule val temp=TemporaryFolder()
    private class App(context:Context,private val directory:File):Application() {
        init {attachBaseContext(context)}
        override fun getApplicationContext():Context=this
        override fun getNoBackupFilesDir():File=directory
    }
    private class Engine:SpeechEngine {
        override val readiness=EngineReadiness.READY
        override var onReadinessChanged:((EngineReadiness)->Unit)?=null
        val spoken=mutableListOf<EngineSpeechRequest>()
        var stops=0
        override fun speak(request:EngineSpeechRequest,result:(EngineResult)->Unit) {spoken+=request}
        override fun stop(){stops++}
        override fun close()=Unit
    }
    @Test fun exactSilentRestoreLanguageRapidSelectionsAndNoProgress() {
        val directory=temp.newFolder()
        val app=App(ApplicationProvider.getApplicationContext(),directory)
        val owners=ViewModelStore();val engine=Engine();var factories=0
        lateinit var vm:MonthsViewModel
        val disk=MonthsSelectionStore(AtomicProgressStorage(File(directory,"months-selection"),AndroidAtomicCommit))
        fun open() {
            compose.runOnUiThread {vm=MonthsViewModel(app){factories++;engine};owners.put("months",vm);vm.configure("de","all");vm.setVisible(true)}
            compose.waitUntil(10_000){var ready=false;compose.runOnUiThread{ready=vm.selection!=null};ready}
        }
        try {
            open()
            compose.runOnUiThread {
                assertEquals(0,factories);assertTrue(engine.spoken.isEmpty())
                vm.select(MonthIds.MARCH)
                assertEquals("März",engine.spoken.last().text)
                assertEquals(ContentLanguage.GERMAN,engine.spoken.last().context.language)
                val stops=engine.stops
                vm.configure("en","questions")
                assertTrue(engine.stops>stops)
                assertEquals(MonthIds.MARCH,vm.selection!!.selected)
                vm.listen();assertEquals("March",engine.spoken.last().text)
                vm.configure("de","off")
                MonthIds.canonicalOrder.forEach(vm::select)
                assertEquals(2,engine.spoken.size)
                vm.setVisible(false)
            }
            compose.waitUntil(10_000){runCatching{disk.read()==MonthsSelection(MonthIds.DECEMBER)}.getOrDefault(false)}
            compose.runOnUiThread{owners.clear()}
            open()
            compose.runOnUiThread {
                assertEquals(MonthIds.DECEMBER,vm.selection!!.selected)
                assertEquals(1,factories);assertEquals(2,engine.spoken.size)
                vm.setVisible(false);vm.select(MonthIds.JANUARY);vm.listen();vm.setVisible(true)
                assertEquals(MonthIds.DECEMBER,vm.selection!!.selected)
                assertEquals(2,engine.spoken.size)
                vm.select(MonthIds.DECEMBER);assertEquals("Dezember",engine.spoken.last().text)
            }
            assertEquals(setOf("months-selection"),directory.listFiles()!!.map{it.name}.toSet())
        } finally {compose.runOnUiThread{owners.clear()}}
    }
    @Test fun unreadableCheckpointIsPreservedUntilItCanBeRead() {
        val directory=temp.newFolder();val journal=File(directory,"months-selection").apply{mkdirs()}
        val bytes=byteArrayOf(0,0,0,99)
        File(journal,"events.bin").writeBytes(bytes)
        val app=App(ApplicationProvider.getApplicationContext(),directory)
        val owners=ViewModelStore();var engines=0
        lateinit var vm:MonthsViewModel
        try {
            compose.runOnUiThread{vm=MonthsViewModel(app){engines++;Engine()};owners.put("months",vm);vm.setVisible(true)}
            compose.waitUntil(10_000){var failed=false;compose.runOnUiThread{failed=vm.saveFailed};failed}
            compose.runOnUiThread{vm.select(MonthIds.MARCH);assertNull(vm.selection);assertEquals(0,engines)}
            assertArrayEquals(bytes,File(journal,"events.bin").readBytes())
            MonthsSelectionStore(AtomicProgressStorage(journal,AndroidAtomicCommit)).write(MonthsSelection(MonthIds.MAY))
            compose.runOnUiThread{vm.retry()}
            compose.waitUntil(10_000){var ready=false;compose.runOnUiThread{ready=vm.selection!=null};ready}
            compose.runOnUiThread{assertEquals(MonthIds.MAY,vm.selection!!.selected);assertFalse(vm.saveFailed);assertEquals(0,engines)}
        } finally {compose.runOnUiThread{owners.clear()}}
    }
    @Test fun quizModeIsolationAssistanceSilentRestoreAndLanguageDuringWork() = quizLifecycle(MonthsMode.FIND)
    @Test fun missingModeSilentRestoreLanguageAudioAndIndependentJournal() = quizLifecycle(MonthsMode.MISSING)

    @Test fun weatherSpeechPolicyRapidSelectionAndDurableSilentRestoration() {
        val directory = temp.newFolder()
        val app = App(ApplicationProvider.getApplicationContext(), directory)
        val disk = MonthsSeasonWheelStore(AtomicProgressStorage(File(directory, "months-seasons-wheel-v1"), AndroidAtomicCommit))
        disk.write(MonthsSeasonWheelState(2, MonthsWheelMode.EXPLORE)) // January
        val owners = ViewModelStore()
        val engine = Engine()
        lateinit var vm: MonthsViewModel
        fun open() {
            compose.runOnUiThread {
                vm = MonthsViewModel(app) { engine }; owners.put("months", vm)
                vm.configure("en", "questions"); vm.setVisible(true, MonthsMode.SEASONS_WHEEL)
            }
            compose.waitUntil(10_000) { var ready = false; compose.runOnUiThread { ready = vm.wheel != null }; ready }
        }
        try {
            open()
            lateinit var snapshot: MonthsSeasonWheelState
            compose.runOnUiThread {
                assertTrue(engine.spoken.isEmpty())
                vm.wheelWeatherListen()
                assertTrue(engine.spoken.last().text.startsWith("January. Winter. It's snowing!"))
                val stops = engine.stops
                repeat(25) { vm.wheelRotate(1) }
                assertTrue(engine.stops > stops)
                assertEquals(1, engine.spoken.size) // Never narrate intermediate or snapped selections automatically.
                vm.configure("de", "questions")
                vm.wheelWeatherListen()
                assertEquals(MonthsWeatherContent.narration(vm.wheel!!.selectedMonth, vm.wheel!!.weather).speech.de,
                    engine.spoken.last().text)
                vm.wheelChangeWeather(); snapshot = vm.wheel!!
                vm.configure("de", "off"); vm.wheelWeatherListen()
                assertEquals(2, engine.spoken.size)
                vm.setVisible(false); vm.wheelWeatherListen()
                assertEquals(2, engine.spoken.size)
            }
            compose.waitUntil(10_000) { disk.read() == snapshot }
            compose.runOnUiThread { owners.clear() }
            open()
            compose.runOnUiThread {
                assertEquals(snapshot, vm.wheel)
                assertEquals(2, engine.spoken.size)
                vm.wheelMode(MonthsWheelMode.MATCH)
                vm.wheelWeatherListen(); vm.wheelChangeWeather()
                assertEquals(snapshot.weather, vm.wheel!!.weather)
                assertEquals(2, engine.spoken.size)
            }
            assertEquals(setOf("months-seasons-wheel-v1"), directory.listFiles()!!.map { it.name }.toSet())
        } finally { compose.runOnUiThread { owners.clear() } }
    }

    private fun quizLifecycle(primary: MonthsMode) {
        val directory=temp.newFolder();val app=App(ApplicationProvider.getApplicationContext(),directory)
        val owners=ViewModelStore();val engine=Engine();lateinit var vm:MonthsViewModel
        fun settled() {
            compose.waitUntil(10_000){var ready=false;compose.runOnUiThread{ready=vm.quiz!=null && !vm.quizBusy};ready}
            compose.runOnUiThread{assertFalse(vm.quizFailed)}
        }
        fun open() {
            compose.runOnUiThread{vm=MonthsViewModel(app){engine};owners.put("months",vm);vm.configure("en","all",5);vm.setVisible(true,primary)}
            settled()
        }
        try {
            open()
            compose.runOnUiThread{
                assertEquals(vm.quiz!!.task.question.instruction.speech.en,engine.spoken.last().text)
                val before=vm.quiz!!.current
                vm.option(vm.quiz!!.task.question.choices.last())
                assertEquals(before,vm.quiz!!.current)
                assertEquals(MonthContent.month(vm.quiz!!.task.question.choices.last()).text.speech.en,engine.spoken.last().text)
                vm.replay()
            };settled()
            val before=engine.spoken.size
            lateinit var saved:ByteArray
            compose.runOnUiThread{
                assertEquals(1,vm.quiz!!.current.support.replays);assertEquals(0,vm.quiz!!.current.attempts)
                vm.learn {vm.setVisible(true,MonthsMode.EXPLORE)}
            }
            compose.waitUntil(10_000){var ready=false;compose.runOnUiThread{ready=vm.phase==MonthsMode.EXPLORE && vm.selection!=null};ready}
            compose.runOnUiThread{assertEquals(before,engine.spoken.size);vm.setVisible(true,primary)};settled()
            compose.runOnUiThread{
                assertTrue(vm.quiz!!.current.support.hint)
                saved=SessionCheckpoint.encode(vm.quiz!!)
                vm.setVisible(true,MonthsMode.RELATIONS)
            };settled()
            compose.runOnUiThread{
                assertEquals(MonthsQuestions.activity(MonthsMode.RELATIONS),vm.quiz!!.plan.activity)
                assertEquals(0,vm.quiz!!.current.support.replays)
                vm.setVisible(true,primary)
                assertArrayEquals(saved,SessionCheckpoint.encode(vm.quiz!!))
                val q=vm.quiz!!
                vm.action(SessionAction.Answer(q.nextAttempt!!,q.task.question.correct))
                vm.configure("de","questions",10) // New settings cannot change the frozen five-task plan.
            };settled()
            compose.runOnUiThread{
                assertEquals(ContentLanguage.GERMAN,vm.quiz!!.language);assertEquals(5,vm.quiz!!.plan.tasks.size)
                assertEquals(AnswerState.CORRECT,vm.quiz!!.current.answer)
                saved=SessionCheckpoint.encode(vm.quiz!!)
                vm.setVisible(false);owners.clear()
            }
            val count=engine.spoken.size
            // Restore in the same language to verify exact bytes, without opening narration.
            compose.runOnUiThread{vm=MonthsViewModel(app){engine};owners.put("months",vm);vm.configure("de","all",10);vm.setVisible(true,primary)};settled()
            compose.runOnUiThread{
                assertArrayEquals(saved,SessionCheckpoint.encode(vm.quiz!!));assertEquals(count,engine.spoken.size)
                vm.configure("de","off",10);vm.replay()
            };settled()
            compose.runOnUiThread{assertEquals(count,engine.spoken.size)}
            assertTrue(File(directory,"months-${primary.name.lowercase()}-session/events.bin").isFile)
            assertTrue(File(directory,"months-relations-session/events.bin").isFile)
        } finally {compose.runOnUiThread{owners.clear()}}
    }
}
