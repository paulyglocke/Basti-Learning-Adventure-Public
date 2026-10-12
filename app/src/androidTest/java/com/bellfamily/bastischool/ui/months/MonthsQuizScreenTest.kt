package com.bellfamily.bastischool.ui.months

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.InputMode
import androidx.compose.ui.input.InputModeManager
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInputModeManager
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.*
import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.months.*
import com.bellfamily.bastischool.learning.session.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class MonthsQuizScreenTest(private val mode:MonthsMode,private val de:Boolean,private val font:Float,private val orientation:Int) {
    @get:Rule val compose=createAndroidComposeRule<ComponentActivity>()
    companion object {
        @JvmStatic @Parameterized.Parameters(name="{0},de={1},font={2},orientation={3}") fun cases()=
            listOf(MonthsMode.FIND,MonthsMode.RELATIONS,MonthsMode.MISSING,MonthsMode.BETWEEN).flatMap { mode -> listOf(false,true).flatMap {de ->
                listOf(1f,1.5f,2f).flatMap {font -> listOf(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT,
                    ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE,ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE).map {arrayOf<Any>(mode,de,font,it)} }
            } }
    }
    @OptIn(ExperimentalTestApi::class, androidx.compose.ui.ExperimentalComposeUiApi::class)
    @Test fun readableChoicesSeparateListeningRetryHintAndKeyboard() {
        val landscape=orientation!=ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        compose.activity.requestedOrientation=orientation
        compose.waitUntil(10_000){compose.activity.resources.configuration.orientation==if(landscape)Configuration.ORIENTATION_LANDSCAPE else Configuration.ORIENTATION_PORTRAIT}
        compose.waitForIdle()
        val language=if(de)ContentLanguage.GERMAN else ContentLanguage.ENGLISH
        val plan=(MonthsQuestions.generate(mode,SessionId("months-ui"),RoundLength.FIVE,10) as GenerationResult.Generated).plan
        var state by mutableStateOf(SessionReducer.start(plan,language,MonthContent.repository).state)
        var listens=0;var explores=0;var home=0
        lateinit var input:InputModeManager
        fun action(a:SessionAction){state=SessionReducer.reduce(state,a).state}
        compose.setContent {
            input=LocalInputModeManager.current
            CompositionLocalProvider(LocalDensity provides Density(LocalDensity.current.density,font)) {
                MaterialTheme {MonthsQuizScreen(mode,state,language,false,false,false,::action,
                    {action(SessionAction.Replay(state.task.id))},{listens++},{},{},{explores++},{home++},
                    Modifier.size(if(landscape)700.dp else 280.dp,if(landscape)240.dp else 650.dp))}
            }
        }
        fun click(tag:String)=compose.onNodeWithTag(tag).performScrollTo().assertIsDisplayed().performClick()
        fun readable(node:SemanticsNodeInteraction) {
            val result=mutableListOf<TextLayoutResult>()
            node.performSemanticsAction(SemanticsActions.GetTextLayoutResult){it(result)}
            result.single().let {t ->
                assertFalse(t.multiParagraph.didExceedMaxLines);assertTrue(t.multiParagraph.height<=t.size.height+1)
                repeat(t.lineCount){i ->assertFalse(t.isLineEllipsized(i));assertTrue(t.getLineRight(i)<=t.size.width+1);assertTrue(t.getLineLeft(i)>=-1)}
            }
        }
        compose.onNodeWithTag("months-wheel").assertDoesNotExist()
        readable(compose.onNodeWithTag("months-quiz-prompt").performScrollTo().assertTextEquals(state.task.question.instruction.display[language]))
        compose.onNodeWithTag("months-quiz-progress").performScrollTo().assertTextEquals(if(de)"Frage 1 von 5" else "Question 1 of 5")
        val q=state.task.question
        if (mode == MonthsMode.MISSING) {
            val sequence = MonthsMissing.definition(q)
            val node = compose.onNodeWithTag("months-quiz-sequence").performScrollTo().assertIsDisplayed()
            node.assertContentDescriptionEquals(sequence.description(language)).assertHasNoClickAction()
            assertEquals(0, node.fetchSemanticsNode().children.size)
            readable(compose.onNodeWithText(sequence.sequence(language), useUnmergedTree=true))
        }
        if (mode == MonthsMode.BETWEEN) {
            val definition = MonthsBetween.definition(q)
            val before = MonthContent.month(definition.before).text.display[language]
            val after = MonthContent.month(definition.after).text.display[language]
            val relation = compose.onNodeWithTag("months-between-sequence").performScrollTo().assertIsDisplayed()
            relation.assertContentDescriptionEquals(if(de) "$before, dann der gesuchte Monat dazwischen, dann $after"
                else "$before, then the month between, then $after")
            for ((tag, name) in listOf("months-between-before" to before, "months-between-after" to after)) {
                val endpoint = compose.onNodeWithTag(tag).performScrollTo().assertTextEquals(name)
                readable(endpoint)
                assertTrue(endpoint.fetchSemanticsNode().boundsInRoot.width > 0f)
            }
            compose.onNodeWithTag("months-between-gap").assertContentDescriptionEquals(
                if(de) "Gesuchter Monat dazwischen" else "Month to find between")
        }
        q.choices.forEach {id ->
            val tag="months-answer-${id.value}";val button=compose.onNodeWithTag(tag).performScrollTo()
            button.assertHasClickAction().assertHeightIsAtLeast(64.dp)
            if (mode == MonthsMode.BETWEEN) button.assertContentDescriptionEquals(
                if(de) "Antwort: ${MonthContent.month(id).text.display[language]}"
                else "Answer choice: ${MonthContent.month(id).text.display[language]}")
            val text=compose.onNode(hasText(MonthContent.month(id).text.display[language]) and hasAnyAncestor(hasTestTag(tag)),useUnmergedTree=true)
            readable(text)
            val b=button.fetchSemanticsNode().boundsInRoot;val t=text.fetchSemanticsNode().boundsInRoot
            assertTrue(t.top>=b.top && t.bottom<=b.bottom && t.left>=b.left && t.right<=b.right)
            click("months-listen-${id.value}")
        }
        compose.runOnIdle {assertEquals(4,listens);assertEquals(0,state.current.attempts)}
        click("months-quiz-replay");click("months-quiz-help")
        readable(compose.onNodeWithTag("months-quiz-hint").performScrollTo())
        compose.runOnIdle {assertEquals(0,state.current.attempts);assertEquals(1,state.current.support.replays);assertTrue(state.current.support.hint)}
        val wrong=q.choices.first{it!=q.correct}
        click("months-answer-${wrong.value}")
        compose.onNodeWithTag("months-answer-${wrong.value}").assertIsNotEnabled().assertIsSelected()
        compose.runOnIdle {assertEquals(1,state.current.attempts)}
        click("months-quiz-retry")
        compose.runOnIdle {assertEquals(1,state.current.attempts)}
        val correct=compose.onNodeWithTag("months-answer-${q.correct.value}").performScrollTo()
        compose.runOnIdle {assertTrue(input.requestInputMode(InputMode.Keyboard))}
        correct.performSemanticsAction(SemanticsActions.RequestFocus){it()};correct.performKeyInput{pressKey(Key.Enter)}
        compose.runOnIdle {assertEquals(AnswerState.CORRECT,state.current.answer);assertEquals(2,state.current.attempts)}
        click("months-quiz-next")
        compose.onNodeWithTag("months-quiz-progress").performScrollTo().assertTextEquals(if(de)"Frage 2 von 5" else "Question 2 of 5")
        click("months-quiz-explore");click("months-quiz-home")
        compose.runOnIdle {assertEquals(1,explores);assertEquals(1,home)}
    }
}
