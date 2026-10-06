package com.bellfamily.bastischool.ui.prepositions

import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.InputMode
import androidx.compose.ui.input.InputModeManager
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInputModeManager
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.bellfamily.bastischool.audio.*
import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.prepositions.*
import com.bellfamily.bastischool.learning.session.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class PrepositionsGuidedListenScreenTest {
    @get:Rule val compose=createComposeRule()
    private class Engine:SpeechEngine {
        override val readiness=EngineReadiness.READY
        override var onReadinessChanged:((EngineReadiness)->Unit)?=null
        val requests=mutableListOf<EngineSpeechRequest>()
        private var callback:((EngineResult)->Unit)?=null
        override fun speak(request:EngineSpeechRequest,result:(EngineResult)->Unit) {requests+=request;callback=result}
        fun finish() {val done=callback;callback=null;done?.invoke(EngineResult.Completed)}
        override fun stop() {val done=callback;callback=null;done?.invoke(EngineResult.Cancelled)}
        override fun close()=stop()
    }
    private val engine=Engine()
    private lateinit var audio:PrepositionsAudio
    private lateinit var state:MutableState<SessionState>
    private lateinit var spoken:MutableState<ContentId?>
    private lateinit var input:InputModeManager
    private fun show(language:ContentLanguage) {
        val plan=(PrepositionsContent.generate(SessionId("guided-ui"),RoundLength.FIVE,42) as GenerationResult.Generated).plan
        state=mutableStateOf(SessionReducer.start(plan,language,PrepositionsContent.repository).state)
        spoken=mutableStateOf(null)
        audio=PrepositionsAudio(DefaultAudioController(engine,AudioMode.ALL),spokenOption={spoken.value=it})
        audio.visible(true)
        compose.setContent {
            input=LocalInputModeManager.current
            DisposableEffect(Unit){onDispose {audio.close()}}
            val density=LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density,1.5f)) {
                MaterialTheme {
                    PrepositionsScreen(state.value,state.value.language,false,false,false,
                        onAction={action ->
                            audio.cancel()
                            val t=SessionReducer.reduce(state.value,action);state.value=t.state;audio.effects(t.state,t.effects)
                        },onOption={audio.option(state.value,it)},onRetrySave={},onAgain={},onIntroduction={},
                        onHome={audio.visible(false)},onLegacy={audio.visible(false)},
                        modifier=Modifier.width(320.dp),spokenOption=spoken.value)
                }
            }
        }
    }
    private fun highlighting(language:ContentLanguage)=SemanticsMatcher.expectValue(SemanticsProperties.StateDescription,
        if(language==ContentLanguage.GERMAN) "Wird vorgelesen" else "Being read aloud")
    private fun guided(language:ContentLanguage) {
        show(language)
        val scene=PrepositionsContent.scene(state.value.task)
        compose.onNodeWithTag("position-question").assertTextEquals(positionQuestion(scene).display[language])
        compose.onNodeWithTag("position-answer-stem").assertTextEquals(positionAnswerStem(scene).display[language])
        compose.onNodeWithTag("replay").performScrollTo().performClick()
        compose.onAllNodes(highlighting(language)).assertCountEquals(0)
        compose.runOnIdle {engine.finish();engine.finish()}
        state.value.task.question.choices.forEach { id ->
            val button=compose.onNodeWithTag("answer-${id.value}").performScrollTo().assertIsDisplayed()
            button.assert(highlighting(language)).assertTextEquals(answerPhrase(scene,id,language))
            button.assert(SemanticsMatcher.keyNotDefined(SemanticsProperties.Selected))
            compose.onAllNodes(highlighting(language)).assertCountEquals(1)
            compose.runOnIdle {engine.finish()}
        }
        compose.onAllNodes(highlighting(language)).assertCountEquals(0)
        compose.runOnIdle {
            assertEquals(6,engine.requests.size);assertTrue(engine.requests.all {it.context.language==language})
            assertEquals(0,state.value.current.attempts);assertEquals(1,state.value.current.support.replays)
        }
    }
    @Test fun englishGuidedOutlineAdvancesAndClearsAtLargeTextNarrowWidth()=guided(ContentLanguage.ENGLISH)
    @Test fun germanGuidedOutlineAdvancesAndClearsAtLargeTextNarrowWidth()=guided(ContentLanguage.GERMAN)

    @OptIn(ExperimentalTestApi::class,androidx.compose.ui.ExperimentalComposeUiApi::class)
    @Test fun individualListenIsSeparateFromImmediateKeyboardAnswerAndNavigationClearsOutline() {
        show(ContentLanguage.ENGLISH)
        val s=state.value;val third=s.task.question.choices[2]
        compose.onNodeWithTag("speaker-${third.value}").performScrollTo().performClick()
        compose.onNodeWithTag("answer-${third.value}").assert(highlighting(ContentLanguage.ENGLISH))
        compose.runOnIdle {
            assertEquals(1,engine.requests.size)
            assertEquals(answerPhrase(PrepositionsContent.scene(s.task),third,s.language)+".",engine.requests.single().text)
            assertEquals(0,state.value.current.attempts)
            assertTrue(input.requestInputMode(InputMode.Keyboard))
        }
        val answer=compose.onNodeWithTag("answer-${third.value}").performScrollTo()
        answer.performSemanticsAction(SemanticsActions.RequestFocus){assertTrue(it())}
        answer.assertIsFocused().performKeyInput {pressKey(Key.Enter)}
        compose.runOnIdle {assertEquals(1,state.value.current.attempts);assertEquals(third,state.value.current.lastChoice)}
        compose.onAllNodes(highlighting(ContentLanguage.ENGLISH)).assertCountEquals(0)
        compose.onNodeWithTag("speaker-${third.value}").performScrollTo().performClick()
        compose.onNodeWithTag("home").performScrollTo().performClick()
        compose.onAllNodes(highlighting(ContentLanguage.ENGLISH)).assertCountEquals(0)
    }
}
