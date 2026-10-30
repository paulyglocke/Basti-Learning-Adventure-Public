package com.bellfamily.bastischool.ui.habitats

import android.graphics.Bitmap
import java.io.File
import java.io.FileOutputStream
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.lifecycle.ViewModelProvider
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import com.bellfamily.bastischool.MainActivity
import com.bellfamily.bastischool.learning.animals.HabitatMatchingContent
import com.bellfamily.bastischool.learning.models.ContentId
import com.bellfamily.bastischool.learning.progress.*
import com.bellfamily.bastischool.learning.progress.android.AndroidProgressRepository
import com.bellfamily.bastischool.learning.session.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class HabitatMatchingRouteTest {
    @get:Rule val compose = createEmptyComposeRule()

    @Test fun matchingActivityRunsAndCapturesRealBilingualResponsiveStates() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        val prefs = context.getSharedPreferences("basti_shell", 0)
        val oldLanguage = prefs.getString("lang", null)
        val oldAudio = prefs.getString("audioMode", null)
        val oldSound = prefs.getBoolean("sound", true)
        val oldRound = prefs.getInt("round", 5)
        val oldFont = context.resources.configuration.fontScale
        val artifactRoot = File(requireNotNull(context.getExternalFilesDir(null)), "animals-phase5c6")
        check(artifactRoot.mkdirs() || artifactRoot.isDirectory)
        val deviceGallery = "/sdcard/Download/animals-phase5c6"
        fun shell(command: String) { instrumentation.uiAutomation.executeShellCommand(command).close() }
        shell("mkdir -p $deviceGallery")
        fun capture(name: String, width: Int, height: Int) {
            compose.waitForIdle(); Thread.sleep(250)
            val bitmap = instrumentation.uiAutomation.takeScreenshot()
            assertEquals("$name width", width, bitmap.width)
            assertEquals("$name height", height, bitmap.height)
            val file = File(artifactRoot, "$name.png")
            try { FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) } }
            finally { bitmap.recycle() }
            shell("cp ${file.absolutePath} $deviceGallery/$name.png")
        }
        fun chooseLanguage(scenario: ActivityScenario<MainActivity>, owner: HabitatMatchingViewModel, german: Boolean) {
            compose.onNodeWithTag("shell-options").performScrollTo().performClick()
            compose.onNodeWithText(if (german) "🇩🇪 Deutsch" else "🇬🇧 English")
                .performScrollTo().performClick()
            scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
            compose.waitUntil(15_000) {
                !owner.busy && owner.state?.language == if (german)
                    com.bellfamily.bastischool.learning.models.ContentLanguage.GERMAN else
                    com.bellfamily.bastischool.learning.models.ContentLanguage.ENGLISH
            }
        }
        fun clickChoice(owner: HabitatMatchingViewModel, id: ContentId) {
            compose.onNodeWithTag("habitat-match-choice-${id.value}").performScrollTo().assertIsDisplayed().performClick()
            compose.waitUntil(15_000) { !owner.busy && owner.state?.current?.lastChoice == id }
        }
        shell("wm size 1080x2400")
        shell("settings put system font_scale 1.0")
        prefs.edit().putString("lang", "en").putString("audioMode", "off").putBoolean("sound", false).putInt("round", 5).commit()
        try {
            ActivityScenario.launch(MainActivity::class.java).use { scenario ->
                var owner: HabitatMatchingViewModel? = null
                scenario.onActivity { owner = ViewModelProvider(it)[HabitatMatchingViewModel::class.java] }
                val vm = requireNotNull(owner)
                compose.onNodeWithTag("home-category-ANIMALS").performScrollTo().performClick()
                compose.onNodeWithTag("home-activity-matchHabitats").performScrollTo().assertIsDisplayed()
                capture("01-animals-category-en", 1080, 2400)
                compose.onNodeWithTag("home-activity-matchHabitats").performClick()
                compose.waitUntil(20_000) { vm.state != null && !vm.busy && vm.habitatImages.size == 4 && vm.animalImages.size == 6 }
                assertEquals(7, vm.state!!.plan.taskCount)
                assertEquals(7, vm.state!!.plan.tasks.size)
                compose.onNodeWithTag("habitat-match-title").assertIsDisplayed()
                compose.onNodeWithTag("habitat-match-instruction").assertIsDisplayed()
                compose.onNodeWithTag("habitat-match-progress").assertTextContains("1 of 7")
                compose.onNodeWithTag("habitat-match-replay").assertIsDisplayed()
                compose.onNodeWithTag("habitat-match-help").assertIsDisplayed()
                val first = vm.state!!
                val firstItem = HabitatMatchingContent.reviewed(first.task.question.definition)
                assertNotNull(vm.animalImages[firstItem.animal.id])
                first.task.question.choices.forEach { id ->
                    compose.onNodeWithTag("habitat-match-choice-${id.value}")
                        .assertExists().assert(SemanticsMatcher.expectValue(androidx.compose.ui.semantics.SemanticsProperties.Role,
                            androidx.compose.ui.semantics.Role.Button))
                    assertNotNull("${id.value} artwork", vm.habitatImages[id])
                }
                capture("02-first-question-en", 1080, 2400)
                val beforeReplayQuestion = vm.state!!.task.question.definition
                compose.onNodeWithTag("habitat-match-replay").performScrollTo().performClick()
                compose.waitUntil(15_000) { !vm.busy }
                assertEquals(beforeReplayQuestion, vm.state!!.task.question.definition)
                assertEquals(0, vm.state!!.index)

                chooseLanguage(scenario, vm, german = true)
                compose.onNodeWithTag("habitat-match-instruction").assertIsDisplayed()
                assertEquals(first.task.question.definition, vm.state!!.task.question.definition)
                assertEquals(first.task.question.acceptedAnswers, vm.state!!.task.question.acceptedAnswers)
                capture("03-first-question-de", 1080, 2400)

                val current = vm.state!!
                val wrong = current.task.question.choices.firstOrNull { it !in current.task.question.acceptedAnswers }
                    ?: error("Each starter question needs at least one distractor")
                clickChoice(vm, wrong)
                compose.onNodeWithTag("habitat-match-feedback-wrong").performScrollTo().assertIsDisplayed()
                compose.onNodeWithTag("habitat-match-retry").performScrollTo().assertIsDisplayed()
                capture("04-wrong-retry-feedback-de", 1080, 2400)
                compose.onNodeWithTag("habitat-match-retry").performScrollTo().performClick()
                compose.waitUntil(15_000) { !vm.busy && vm.state?.current?.answer == AnswerState.UNANSWERED }
                clickChoice(vm, vm.state!!.task.question.acceptedAnswers.first())
                compose.onNodeWithTag("habitat-match-feedback-correct").performScrollTo().assertIsDisplayed()
                capture("05-correct-feedback-de", 1080, 2400)
                val advancedFromFirst = vm.state!!.task.id
                compose.onNodeWithTag("habitat-match-continue").performScrollTo().performClick()
                compose.waitUntil(15_000) { !vm.busy && (vm.state!!.task.id != advancedFromFirst || vm.state!!.phase == SessionPhase.COMPLETED) }
                assertEquals(1, vm.state!!.index)
                compose.onNodeWithTag("habitat-match-progress").assertTextContains("2 von 7")
                capture("06-continue-2-of-7-de", 1080, 2400)
                val idBeforeRecreate = vm.state!!.task.question.definition
                val indexBeforeRecreate = vm.state!!.index
                scenario.recreate()
                compose.waitUntil(20_000) { vm.state != null && !vm.busy }
                assertEquals(indexBeforeRecreate, vm.state!!.index)
                assertEquals(idBeforeRecreate, vm.state!!.task.question.definition)
                capture("07-activity-recreated-session-resumed", 1080, 2400)

                shell("settings put system font_scale 1.5")
                scenario.recreate(); compose.waitForIdle()
                capture("08-german-1_5x", 1080, 2400)
                shell("settings put system font_scale 2.0")
                scenario.recreate(); compose.waitForIdle()
                capture("09-german-2x", 1080, 2400)
                compose.onNodeWithTag("habitat-match-replay").performScrollTo().assertIsDisplayed()
                shell("settings put system font_scale 1.0")
                scenario.recreate(); compose.waitForIdle()

                // Complete all remaining prompts through the production answer and Continue controls.
                while (vm.state!!.phase == SessionPhase.ACTIVE) {
                    val q = vm.state!!.task.question
                    clickChoice(vm, q.acceptedAnswers.first())
                    val task = vm.state!!.task.id
                    compose.onNodeWithTag("habitat-match-continue").performScrollTo().performClick()
                    compose.waitUntil(15_000) { !vm.busy && (vm.state!!.task.id != task || vm.state!!.phase == SessionPhase.COMPLETED) }
                }
                compose.waitUntil(20_000) { vm.state?.phase == SessionPhase.COMPLETED && !vm.busy }
                compose.onNodeWithTag("completion-title").assertIsDisplayed()
                compose.onNodeWithTag("habitat-match-complete").assertIsDisplayed()
                capture("10-completion-and-progress-result", 1080, 2400)
                val records = (AndroidProgressRepository.create(context).read(ProgressQuery(activity = HabitatMatchingContent.activity,
                    session = vm.state!!.plan.id))
                    as ProgressReadResult.Events).records.map { it.event }
                assertEquals(8, records.filterIsInstance<AttemptEvent>().size)
                assertEquals(1, records.filterIsInstance<CompletionEvent>().size)
                assertEquals(7, records.filterIsInstance<CompletionEvent>().single().tasks.size)
                assertEquals(HabitatMatchingContent.activity, records.first().origin.activity)

                val completedId = vm.state!!.plan.id
                compose.onNodeWithTag("habitat-match-again").assertIsDisplayed().performClick()
                compose.waitUntil(20_000) { vm.state?.phase == SessionPhase.ACTIVE && vm.state?.plan?.id != completedId && !vm.busy }
                assertEquals(0, vm.state!!.index)
                chooseLanguage(scenario, vm, german = false)
                capture("11-options-language-return-en", 1080, 2400)

                shell("wm size 2400x1080")
                scenario.recreate(); compose.waitForIdle()
                compose.onNodeWithTag("habitat-match-instruction").assertIsDisplayed()
                capture("12-phone-landscape", 2400, 1080)
                compose.onNodeWithTag("habitat-match-replay").performScrollTo().assertIsDisplayed()
                shell("wm size 1280x720")
                scenario.recreate(); compose.waitForIdle()
                compose.onNodeWithTag("habitat-match-progress").assertIsDisplayed()
                compose.onNodeWithTag("habitat-match-instruction").assertIsDisplayed()
                capture("13-short-landscape", 1280, 720)
                compose.onNodeWithTag("habitat-match-replay").performScrollTo().assertIsDisplayed()
                shell("wm size 1600x2560")
                scenario.recreate(); compose.waitForIdle()
                capture("14-tablet-portrait-emulator-viewport", 1600, 2560)
                shell("wm size 2560x1600")
                scenario.recreate(); compose.waitForIdle()
                capture("15-tablet-landscape-emulator-viewport", 2560, 1600)
                shell("wm size 1080x2400")
                scenario.recreate(); compose.waitForIdle()
                scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
                compose.onNodeWithTag("home-activity-matchHabitats").assertIsDisplayed()
            }
        } finally {
            shell("wm size 1080x2400")
            shell("settings put system font_scale $oldFont")
            val edit = prefs.edit().putBoolean("sound", oldSound).putInt("round", oldRound)
            if (oldLanguage == null) edit.remove("lang") else edit.putString("lang", oldLanguage)
            if (oldAudio == null) edit.remove("audioMode") else edit.putString("audioMode", oldAudio)
            edit.commit()
        }
    }
}
