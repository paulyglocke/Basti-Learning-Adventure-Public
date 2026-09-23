package com.bellfamily.bastischool.ui.common

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.LocalInputModeManager
import androidx.compose.ui.input.InputMode
import androidx.compose.ui.input.InputModeManager
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.bellfamily.bastischool.learning.models.ContentLanguage
import com.bellfamily.bastischool.learning.vocabulary.VocabularyContent
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class CompletionCelebrationTest {
    @get:Rule val compose=createAndroidComposeRule<ComponentActivity>()
    @OptIn(ExperimentalTestApi::class, androidx.compose.ui.ExperimentalComposeUiApi::class)
    @Test fun accessibleSlotsPopOnceSettleAndKeepIndependentTargets() {
        var pops=0
        compose.mainClock.autoAdvance=false
        lateinit var inputMode: InputModeManager
        compose.setContent {inputMode = LocalInputModeManager.current; MaterialTheme {NativeCompletionCelebration("round",ContentLanguage.ENGLISH,{pops++},Modifier.size(320.dp,200.dp))}}
        compose.mainClock.advanceTimeByFrame()
        repeat(5){compose.onNodeWithTag("balloon-$it").assertHasClickAction().assertContentDescriptionEquals("Pop balloon ${it+1}")}
        val click=compose.onNodeWithTag("balloon-0").fetchSemanticsNode().config[SemanticsActions.OnClick].action!!
        compose.runOnIdle {click();click();assertEquals(1,pops)}
        compose.mainClock.advanceTimeBy(1000)
        compose.onNodeWithTag("balloon-0").assertHasNoClickAction().assertContentDescriptionEquals(VocabularyContent.item(CelebrationState.create("round").slots[0].animal).text.display.en)
        compose.runOnIdle { assertTrue(inputMode.requestInputMode(InputMode.Keyboard)) }
        compose.onNodeWithTag("balloon-1").assertHasClickAction().performSemanticsAction(SemanticsActions.RequestFocus){it()}
        compose.mainClock.advanceTimeByFrame()
        compose.onNodeWithTag("balloon-1").assertIsFocused()
        compose.onNodeWithTag("balloon-1").performKeyInput {pressKey(Key.Enter)}
        compose.mainClock.advanceTimeBy(1000)
        compose.onNodeWithTag("balloon-1").assertHasNoClickAction()
        compose.onNodeWithTag("balloon-2").assertHasClickAction()
        compose.runOnIdle {assertEquals(2,pops)}
    }
    @Test fun reducedMotionAndRecompositionKeepNamesButNewCompletionResets() {
        var language by mutableStateOf(ContentLanguage.ENGLISH)
        var completion by mutableStateOf("one")
        var pops=0
        compose.setContent {MaterialTheme {NativeCompletionCelebration(completion,language,{pops++},Modifier.size(320.dp,180.dp),reducedMotion=true)}}
        compose.onNodeWithTag("balloon-3").performClick()
        compose.runOnIdle {language=ContentLanguage.GERMAN}
        val id=CelebrationState.create("one").slots[3].animal
        compose.onNodeWithTag("balloon-3").assertContentDescriptionEquals(VocabularyContent.item(id).text.display.de).assertHasNoClickAction()
        compose.onNodeWithTag("balloon-2").assertContentDescriptionEquals("Luftballon 3 platzen lassen")
        compose.runOnIdle {completion="two"}
        repeat(5){compose.onNodeWithTag("balloon-$it").assertHasClickAction()}
        compose.runOnIdle {assertEquals(1,pops)}
    }
    @Test fun canonicalArtworkLoadsAndEveryRevealedAnimalUsesItsImage() {
        lateinit var art: CelebrationArtViewModel
        compose.runOnUiThread {
            art = androidx.lifecycle.ViewModelProvider(compose.activity,
                androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.getInstance(compose.activity.application))[CelebrationArtViewModel::class.java]
        }
        compose.waitUntil(10_000) {art.images.size == CelebrationState.COUNT}
        compose.setContent {CompositionLocalProvider(LocalCelebrationArt provides art.images) {
            MaterialTheme {NativeCompletionCelebration("art",ContentLanguage.ENGLISH,{},Modifier.size(320.dp,200.dp),reducedMotion=true)}
        }}
        repeat(5) {
            compose.onNodeWithTag("balloon-$it").performClick().assertHasNoClickAction()
            compose.onNodeWithTag("animal-art-$it",useUnmergedTree=true).assertIsDisplayed()
        }
        assertEquals(CelebrationState.animals.toSet(),art.images.keys)
        assertTrue(art.images.values.all {it.width in 1..200 && it.height in 1..200})
    }
    private fun screen(landscape:Boolean) {
        var again=0;var home=0;var listens=0
        if(landscape) {
            compose.activity.requestedOrientation=ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            compose.waitUntil(10_000){compose.activity.resources.configuration.orientation==Configuration.ORIENTATION_LANDSCAPE}
        }
        compose.setContent {
            val density=LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density,1.5f)) {
                MaterialTheme {Box(Modifier.size(if(landscape)700.dp else 320.dp,if(landscape)240.dp else 480.dp)) {
                    NativeCompletionScreen("layout",ContentLanguage.GERMAN,"Du hast alle Aufgaben geschafft!",true,"test-",
                        {listens++},{again++},{home++},{})
                }}
            }
        }
        compose.onNodeWithTag("test-again").assertIsDisplayed().performClick()
        compose.onNodeWithTag("test-home").assertIsDisplayed().performClick()
        compose.onNodeWithTag("test-replay").assertIsDisplayed().performClick()
        val homeBounds=compose.onNodeWithTag("test-home").fetchSemanticsNode().boundsInRoot
        val rewardBounds=compose.onNodeWithTag("completion-celebration").fetchSemanticsNode().boundsInRoot
        assertTrue(homeBounds.bottom<=rewardBounds.top)
        repeat(5){compose.onNodeWithTag("balloon-$it").assertIsDisplayed()}
        compose.runOnIdle {assertEquals(1,again);assertEquals(1,home);assertEquals(1,listens)}
    }
    @Test fun portraitLargeTextActionsAreImmediatelyUsableBeforePopping()=screen(false)
    @Test fun shortLandscapeLargeTextActionsStayAboveReward()=screen(true)
}
