package com.bellfamily.bastischool.ui.washhands

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.lifecycle.ViewModelProvider
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import com.bellfamily.bastischool.MainActivity
import com.bellfamily.bastischool.learning.washhands.WashHands
import com.bellfamily.bastischool.learning.models.ContentLanguage
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import java.io.File

class WashHandsRouteTest {
    @get:Rule val compose=createEmptyComposeRule()
    @Test fun partialOrderHelpAndLanguageSurviveRecreationOptionsAndHome() {
        val context=InstrumentationRegistry.getInstrumentation().targetContext
        File(context.noBackupFilesDir,"wash-hands-session/events.bin").delete()
        context.getSharedPreferences("basti_shell",0).edit().putString("lang","en").putString("audioMode","off").commit()
        ActivityScenario.launch(MainActivity::class.java).use {scenario ->
            fun owner():WashHandsViewModel {var vm:WashHandsViewModel?=null;scenario.onActivity{vm=ViewModelProvider(it)[WashHandsViewModel::class.java]};return vm!!}
            fun settle()=compose.waitUntil(10_000){!owner().busy && owner().state!=null}
            fun click(tag:String){compose.onNodeWithTag(tag).performScrollTo().performClick();settle()}
            compose.onNodeWithText("Wash Hands").performScrollTo().performClick();settle()
            click("wash-item-${WashHands.order.last().value}");click("wash-help")
            val before=owner().state!!;val retained=owner()
            scenario.recreate();settle();assertSame(retained,owner())
            assertEquals(before.presentation,owner().state!!.presentation);assertEquals(before.constructed,owner().state!!.constructed);assertEquals(before.support,owner().state!!.support)
            compose.onNodeWithText("⚙ Options").performClick();compose.onNodeWithText("🇩🇪 Deutsch").performClick()
            scenario.onActivity{it.onBackPressedDispatcher.onBackPressed()};settle()
            assertEquals(ContentLanguage.GERMAN,owner().state!!.language);assertEquals(before.constructed,owner().state!!.constructed)
            click("wash-home");compose.onNodeWithText("Hände waschen").performScrollTo().performClick();settle()
            assertEquals(before.id,owner().state!!.id);assertEquals(before.hintPosition,owner().state!!.hintPosition)
        }
    }
}
