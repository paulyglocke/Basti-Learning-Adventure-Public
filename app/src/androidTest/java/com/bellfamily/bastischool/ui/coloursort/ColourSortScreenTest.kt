package com.bellfamily.bastischool.ui.coloursort

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.view.Surface
import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.bellfamily.bastischool.learning.coloursort.*
import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.session.*
import com.bellfamily.bastischool.learning.sorting.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@OptIn(ExperimentalTestApi::class)
@RunWith(Parameterized::class)
class ColourSortScreenTest(private val language:ContentLanguage,private val orientation:Int) {
    companion object {
        @JvmStatic @Parameterized.Parameters(name="{0}-{1}")fun cases()=listOf(
            arrayOf<Any>(ContentLanguage.ENGLISH,ActivityInfo.SCREEN_ORIENTATION_PORTRAIT),
            arrayOf<Any>(ContentLanguage.GERMAN,ActivityInfo.SCREEN_ORIENTATION_PORTRAIT),
            arrayOf<Any>(ContentLanguage.GERMAN,ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE),
            arrayOf<Any>(ContentLanguage.GERMAN,ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE))
    }
    @get:Rule val compose=createAndroidComposeRule<ComponentActivity>()
    @Test fun accessibleTapAndKeyboardPathRecoversWrongPlacementAndCompletesAtLargeFont() {
        compose.activity.requestedOrientation=orientation
        val landscape=orientation!=ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        compose.waitUntil(10_000){compose.activity.resources.configuration.orientation==if(landscape)Configuration.ORIENTATION_LANDSCAPE else Configuration.ORIENTATION_PORTRAIT}
        val rotation=when(orientation){ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE->Surface.ROTATION_90;ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE->Surface.ROTATION_270;else->Surface.ROTATION_0}
        compose.waitUntil(10_000){compose.activity.window.decorView.display?.rotation==rotation}
        var state by mutableStateOf(ColourSort.start(SessionId("sort-ui"),4,language))
        var home=0;var replay=0
        compose.setContent {
            val density=LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density,1.5f)) {
                MaterialTheme {ColourSortScreen(state,language,false,false,false,
                    {if(it==SortAction.Replay)replay++;state=ColourSort.reduce(state,it).state},
                    {state=ColourSort.start(SessionId("again-ui"),5,language)},{},{home++})}
            }
        }
        fun item(id:ContentId)=compose.onNodeWithTag("sort-item-${id.value}")
        fun category(id:ContentId)=compose.onNodeWithTag("sort-category-${id.value}")
        ColourSort.categories.forEach {category(it).performScrollTo().assertIsDisplayed().assertHeightIsAtLeast(56.dp).assertWidthIsAtLeast(56.dp)}
        ColourSort.objects.forEach {o->
            item(o.id).performScrollTo().assertIsDisplayed().assertHasClickAction().assertHeightIsAtLeast(56.dp)
            compose.onAllNodesWithText(o.text.display[language]).assertCountEquals(1)
        }
        compose.onNodeWithTag("sort-replay").performScrollTo().performClick();assertEquals(1,replay)
        assertTrue(state.placements.all {it.attempts==0})
        val first=ColourSort.objects.first()
        item(first.id).performScrollTo().performSemanticsAction(SemanticsActions.RequestFocus){it()}
        item(first.id).performKeyInput {pressKey(Key.Enter)}
        item(first.id).assertIsSelected()
        category(ColourSort.blue).performScrollTo().performClick()
        assertFalse(state.placement(first.id).placed);assertEquals(first.id,state.selected)
        compose.onNodeWithTag("sort-guidance").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("sort-help").performScrollTo().performClick()
        compose.onNodeWithTag("sort-hint").performScrollTo().assertIsDisplayed()
        category(first.category).performScrollTo().performClick()
        item(first.id).assertDoesNotExist()
        compose.onNodeWithTag("sort-placed-${first.id.value}",useUnmergedTree=true).assertExists()
        assertFalse(state.completed)
        ColourSort.objects.drop(1).forEach {o->item(o.id).performScrollTo().performClick();category(o.category).performScrollTo().performClick()}
        assertTrue(state.completed)
        compose.onNodeWithTag("completion-celebration").assertExists()
        compose.onNodeWithTag("sort-home").assertIsDisplayed().performClick();assertEquals(1,home)
        compose.onNodeWithTag("sort-again").performClick()
        assertFalse(state.completed);assertTrue(state.placements.all {!it.placed})
    }
}
