package com.bellfamily.bastischool.ui.coloursort

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.lifecycle.ViewModelProvider
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import com.bellfamily.bastischool.MainActivity
import com.bellfamily.bastischool.learning.coloursort.ColourSort
import com.bellfamily.bastischool.learning.models.ContentLanguage
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import java.io.File

class ColourSortRouteTest {
    @get:Rule val compose=createEmptyComposeRule()
    @Test fun nativeRouteRetainsPlacementSelectionLanguageAndRecreationWithSoundOff() {
        val context=InstrumentationRegistry.getInstrumentation().targetContext
        File(context.noBackupFilesDir,"colour-sort-session/events.bin").delete()
        context.getSharedPreferences("basti_shell",0).edit().putString("lang","en").putString("audioMode","off").commit()
        ActivityScenario.launch(MainActivity::class.java).use {scenario->
            fun owner():ColourSortViewModel {var vm:ColourSortViewModel?=null;scenario.onActivity{vm=ViewModelProvider(it)[ColourSortViewModel::class.java]};return vm!!}
            fun settle()=compose.waitUntil(10_000){!owner().busy && owner().state!=null}
            fun click(tag:String){compose.onNodeWithTag(tag).performScrollTo().performClick();settle()}
            compose.onNodeWithText("Colour Sort").performScrollTo().performClick();settle()
            val first=ColourSort.objects.first();click("sort-item-${first.id.value}");click("sort-category-${first.category.value}")
            val second=ColourSort.objects[1];click("sort-item-${second.id.value}")
            val before=owner().state!!;val retained=owner()
            scenario.recreate();settle();assertSame(retained,owner());assertEquals(before.order,owner().state!!.order);assertEquals(before.placements,owner().state!!.placements);assertEquals(before.selected,owner().state!!.selected)
            compose.onNodeWithText("⚙ Options").performClick();compose.onNodeWithText("🇩🇪 Deutsch").performClick()
            scenario.onActivity{it.onBackPressedDispatcher.onBackPressed()};settle()
            assertEquals(ContentLanguage.GERMAN,owner().state!!.language);assertEquals(before.placements,owner().state!!.placements)
            click("sort-home");compose.onNodeWithText("Farben sortieren").performScrollTo().performClick();settle()
            assertEquals(before.id,owner().state!!.id);assertEquals(before.selected,owner().state!!.selected)
        }
    }
}
