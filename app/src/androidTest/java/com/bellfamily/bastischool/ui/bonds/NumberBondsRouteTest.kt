package com.bellfamily.bastischool.ui.bonds

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.lifecycle.ViewModelProvider
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import com.bellfamily.bastischool.*
import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.bonds.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class NumberBondsRouteTest {
    @get:Rule val compose=createEmptyComposeRule()
    @Test fun bothLevelsKeepFixedEggsRestoreAddedEggsAndCompleteWithSharedRewards() {
        val prefs=InstrumentationRegistry.getInstrumentation().targetContext.getSharedPreferences("basti_shell",0)
        val lang=prefs.getString("lang",null);val audio=prefs.getString("audioMode",null);val round=prefs.getInt("round",5)
        try {
            for(level in BondLevel.entries) {
                prefs.edit().putString("lang","en").putString("audioMode","off").putInt("round",5).commit()
                ActivityScenario.launch(MainActivity::class.java).use {scenario ->
                    fun vm(activity:MainActivity):NumberBondsViewModel=if(level==BondLevel.FIVE)ViewModelProvider(activity)[BuildFiveViewModel::class.java]else ViewModelProvider(activity)[BuildTenViewModel::class.java]
                    fun state():NumberBonds.State {var s:NumberBonds.State?=null;scenario.onActivity{s=vm(it).state};return s!!}
                    fun settled(){compose.waitUntil(20_000){var ready=false;scenario.onActivity{ready=vm(it).state!=null&&!vm(it).busy&&!vm(it).saveFailed};ready}}
                    fun click(tag:String){compose.onNodeWithTag(tag).performScrollTo().performClick();settled()}
                    compose.openHomeActivity("Build ${level.total}");settled()
                    if(state().completed)click("number-bonds-${level.key}-again")
                    compose.onNodeWithTag("number-bonds-slot-0").assertIsNotEnabled()
                    val shown=state().shown
                    click("number-bonds-slot-$shown");assertEquals(1,state().addedCount);assertEquals(0,state().current.attempts)
                    val mask=state().placedMask
                    scenario.recreate();settled();assertEquals(mask,state().placedMask);assertEquals(shown,state().shown)
                    captureNativeReview("build-${level.total}-partial",compose)
                    compose.onNodeWithTag("shell-options").performClick()
                    compose.waitUntil(10_000){compose.onAllNodesWithTag("options-screen").fetchSemanticsNodes().size==1}
                    compose.onNodeWithText("🇩🇪 Deutsch").performClick();scenario.onActivity{it.onBackPressedDispatcher.onBackPressed()};settled()
                    assertEquals(mask,state().placedMask);assertEquals(ContentLanguage.GERMAN,state().language)
                    while(!state().completed) {
                        val current=state()
                        if(!current.current.solved){
                            for(slot in current.shown until level.total)if(state().placedMask and (1 shl slot)==0)click("number-bonds-slot-$slot")
                            assertEquals(current.current.attempts,state().current.attempts)
                            click("number-bonds-check")
                            assertTrue(state().current.solved)
                            if(current.index==0){compose.onNodeWithTag("number-bonds-equation").assertTextEquals("${current.shown} + ${level.total-current.shown} = ${level.total}");captureNativeReview("build-${level.total}-solved",compose)}
                        }
                        click("number-bonds-next")
                    }
                    assertTrue(state().acknowledged);compose.onNodeWithTag("completion-celebration").assertExists()
                }
            }
        }finally{prefs.edit().putString("lang",lang).putString("audioMode",audio).putInt("round",round).commit()}
    }
}
