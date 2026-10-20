package com.bellfamily.bastischool

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import com.bellfamily.bastischool.ui.testing.LayoutTestActivity
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.*
import com.bellfamily.bastischool.ui.home.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class HomeBrowseLayoutTest(private val de: Boolean, private val scale: Float, private val window: String) {
    @get:Rule val compose = createAndroidComposeRule<LayoutTestActivity>()
    companion object {
        @JvmStatic @Parameterized.Parameters(name="de={0}-scale={1}-{2}")
        fun cases() = listOf(false,true).flatMap { de -> listOf(1f,1.5f,2f).flatMap { scale ->
            listOf("phone","short","tabletPortrait","tabletLandscape").map { arrayOf<Any>(de,scale,it) }
        } }
    }
    private fun readable(text: String, scroll: Boolean = true) {
        val node=compose.onNodeWithText(text,useUnmergedTree=true)
        if(scroll)node.performScrollTo()
        node.assertIsDisplayed()
        val layouts=mutableListOf<TextLayoutResult>()
        node.performSemanticsAction(SemanticsActions.GetTextLayoutResult){assertTrue(it(layouts))}
        val result=layouts.single()
        assertFalse("$text at $window/$scale",result.hasVisualOverflow)
        repeat(result.lineCount){line->
            assertFalse(result.isLineEllipsized(line))
            assertTrue(result.getLineLeft(line)>=-1)
            assertTrue(result.getLineRight(line)<=result.size.width+1)
        }
        val bounds=node.fetchSemanticsNode().boundsInRoot
        assertTrue(bounds.width+1>=result.size.width)
        assertTrue(bounds.height+1>=result.size.height)
    }
    @Test fun productionBrowsePagesWrapAndRetainAllControlsWithoutPreviewCopy() {
        val landscape=window in listOf("short","tabletLandscape")
        compose.activity.requestedOrientation=if(landscape) ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE else ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        compose.waitUntil(10_000){compose.activity.resources.configuration.orientation==if(landscape) Configuration.ORIENTATION_LANDSCAPE else Configuration.ORIENTATION_PORTRAIT}
        val (w,h)=when(window){"phone"->360 to 800;"short"->640 to 320;"tabletPortrait"->800 to 1100;else->1100 to 800}
        var page by mutableStateOf("home")
        var settings=0
        val selected=mutableListOf<String>()
        compose.setContent { CompositionLocalProvider(LocalDensity provides Density(1f,scale)) {
            Box(Modifier.size(w.dp,h.dp)) { HomeBrowseScreen(page,de,
                { if(it=="settings")settings++ else page=it },{selected+=it.homeId()}) }
        } }
        compose.onAllNodes(hasTestTag("home-settings")).assertCountEquals(1)
        compose.onAllNodes(hasText("Preview",substring=true) or hasText("Vorschau",substring=true) or hasText("temporary",substring=true)).assertCountEquals(0)
        HomeCategory.entries.forEach { category ->
            readable(category.label(de))
            compose.onNodeWithTag("home-category-${category.name}").performScrollTo()
                .assertHeightIsAtLeast(56.dp).assertWidthIsAtLeast(56.dp)
                .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role,Role.Button))
                .performTouchInput { click() }
            readable(category.label(de))
            listOf(if(de)"Zurück" else "Back",if(de)"Erwachsene" else "Grown-ups",if(de)"Startseite" else "Home").forEach { readable(it,false) }
            compose.onNodeWithTag("home-settings").assertHeightIsAtLeast(56.dp).performClick()
            category.activityIds.forEach { id ->
                val card=homeActivity(id)
                readable(if(de)card.titleDe else card.titleEn)
                readable(if(de)card.descriptionDe else card.descriptionEn)
                compose.onNodeWithTag("home-activity-$id").performScrollTo().performClick()
            }
            compose.onNodeWithTag("home-back").performClick()
        }
        assertEquals(6,settings)
        assertEquals(HomeCategory.entries.flatMap { it.activityIds },selected)
        compose.onNodeWithTag("home-all").performScrollTo().performClick()
        homeActivities().forEach { readable(if(de)it.titleDe else it.titleEn) }
        compose.onNodeWithTag("home-activity-mixed").performScrollTo().performClick()
        assertEquals("mixed",selected.last())
        compose.onNodeWithTag("home-home").performClick()
        readable(if(de)"Was wollen wir entdecken?" else "What shall we explore?")
    }
}
