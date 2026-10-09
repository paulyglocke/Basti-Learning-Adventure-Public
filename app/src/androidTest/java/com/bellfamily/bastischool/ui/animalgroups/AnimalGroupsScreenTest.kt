package com.bellfamily.bastischool.ui.animalgroups

import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.semantics.SemanticsProperties
import com.bellfamily.bastischool.R
import java.io.File
import android.content.res.Configuration
import android.view.Surface
import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.input.InputMode
import androidx.compose.ui.input.InputModeManager
import androidx.compose.ui.platform.LocalInputModeManager
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.bellfamily.bastischool.learning.animalgroups.*
import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.session.*
import com.bellfamily.bastischool.learning.sorting.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@OptIn(ExperimentalTestApi::class, androidx.compose.ui.ExperimentalComposeUiApi::class)
@RunWith(Parameterized::class)
class AnimalGroupsScreenTest(private val language:ContentLanguage,private val orientation:Int) {
    companion object {
        @JvmStatic @Parameterized.Parameters(name="{0}-{1}")fun cases()=listOf(
            arrayOf<Any>(ContentLanguage.ENGLISH,ActivityInfo.SCREEN_ORIENTATION_PORTRAIT),
            arrayOf<Any>(ContentLanguage.GERMAN,ActivityInfo.SCREEN_ORIENTATION_PORTRAIT),
            arrayOf<Any>(ContentLanguage.ENGLISH,ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE),
            arrayOf<Any>(ContentLanguage.ENGLISH,ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE),
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
        var state by mutableStateOf(AnimalGroups.start(SessionId("groups-ui"),4,language))
        val app=androidx.test.core.app.ApplicationProvider.getApplicationContext<android.app.Application>()
        val images=AnimalGroupsArtworkLoader {app.assets.open(it)}.load()
        assertEquals(4,images.size)
        for (resource in listOf(R.drawable.habitat_water, R.drawable.habitat_land)) {
            val bitmap=BitmapFactory.decodeResource(app.resources,resource)
            assertEquals(1536,bitmap.width);assertEquals(1024,bitmap.height)
            assertTrue(bitmap.hasAlpha())
            assertEquals(0,android.graphics.Color.alpha(bitmap.getPixel(0,0)))
            assertEquals(0,android.graphics.Color.alpha(bitmap.getPixel(bitmap.width-1,0)))
            bitmap.recycle()
        }
        var home=0;var replay=0
        val selections=mutableListOf<ContentId>()
        lateinit var input: InputModeManager
        compose.setContent {
            input = LocalInputModeManager.current
            val density=LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density,1.5f)) {
                MaterialTheme {AnimalGroupsScreen(state,language,false,false,false,images,
                    {if(it==SortAction.Replay)replay++;if(it is SortAction.Select)selections+=it.item;state=AnimalGroups.reduce(state,it).state},
                    {state=AnimalGroups.start(SessionId("again-ui"),5,language)},{},{home++})}
            }
        }
        fun item(id:ContentId)=compose.onNodeWithTag("groups-item-${id.value}")
        fun category(id:ContentId)=compose.onNodeWithTag("groups-category-${id.value}")
        AnimalGroups.categories.forEach {category(it).performScrollTo().assertIsDisplayed().assertHeightIsAtLeast(56.dp).assertWidthIsAtLeast(56.dp)}
        fun checkHabitatLayout() {
            val bounds=AnimalGroups.categories.map { id ->
                val button=category(id).performScrollTo().fetchSemanticsNode().boundsInRoot
                val picture=compose.onNodeWithTag("groups-habitat-${id.value}",useUnmergedTree=true)
                picture.assertIsDisplayed().assert(hasNoClickAction())
                val node=picture.fetchSemanticsNode()
                assertFalse(node.config.contains(SemanticsProperties.ContentDescription))
                assertTrue(node.boundsInRoot.left>button.left && node.boundsInRoot.right<button.right)
                assertTrue(node.boundsInRoot.top>button.top && node.boundsInRoot.bottom<button.bottom)
                assertEquals(button.center.x,node.boundsInRoot.center.x,1f)
                button
            }
            assertEquals(bounds[0].width,bounds[1].width,1f)
            assertEquals(bounds[0].height,bounds[1].height,1f)
        }
        checkHabitatLayout()
        compose.onRoot().captureToImage().asAndroidBitmap().let { bitmap ->
            File(app.cacheDir,"habitats-${language.name}-$orientation.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG,100,it) }
        }
        AnimalGroups.objects.forEach {o->
            item(o.id).performScrollTo().assertIsDisplayed().assertHasClickAction().assertHeightIsAtLeast(56.dp)
            compose.onAllNodesWithText(o.text.display[language]).assertCountEquals(1)
        }
        compose.onNodeWithTag("groups-replay").performScrollTo().performClick();assertEquals(1,replay)
        assertTrue(state.placements.all {it.attempts==0})
        val first=AnimalGroups.objects.first()
        item(first.id).performScrollTo()
        // Match native keyboard input: RequestFocus alone does not leave Android touch mode.
        // performKeyInput sends keys to the focused target, not automatically to this tag.
        compose.runOnIdle { assertTrue(input.requestInputMode(InputMode.Keyboard)) }
        item(first.id).performSemanticsAction(SemanticsActions.RequestFocus){assertTrue(it())}
        item(first.id).assertIsFocused().performKeyInput {pressKey(Key.Enter)}
        item(first.id).assertIsSelected()
        compose.runOnIdle {
            assertEquals(first.id,state.selected)
            assertEquals(listOf(first.id),selections)
        }
        category(AnimalGroups.land).performScrollTo().performClick()
        assertFalse(state.placement(first.id).placed);assertEquals(first.id,state.selected)
        compose.onNodeWithTag("groups-guidance").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("groups-help").performScrollTo().performClick()
        compose.onNodeWithTag("groups-hint").performScrollTo().assertIsDisplayed()
        category(first.category).performScrollTo().performClick()
        item(first.id).assertDoesNotExist()
        compose.onNodeWithTag("groups-placed-${first.id.value}",useUnmergedTree=true).assertExists()
        assertFalse(state.completed)
        checkHabitatLayout() // Uneven placed-animal counts must not change button equality.
        AnimalGroups.objects.drop(1).forEach {o->item(o.id).performScrollTo().performClick();category(o.category).performScrollTo().performClick()}
        assertTrue(state.completed)
        compose.onNodeWithTag("completion-celebration").assertExists()
        compose.onNodeWithTag("groups-home").assertIsDisplayed().performClick();assertEquals(1,home)
        compose.onNodeWithTag("groups-again").performClick()
        assertFalse(state.completed);assertTrue(state.placements.all {!it.placed})
    }
}
