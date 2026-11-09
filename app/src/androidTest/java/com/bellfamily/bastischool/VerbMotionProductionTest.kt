package com.bellfamily.bastischool

import android.graphics.BitmapFactory
import android.provider.Settings
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import com.bellfamily.bastischool.learning.models.ContentLanguage
import com.bellfamily.bastischool.learning.verbs.VerbContent
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

/** Actual Home → native Compose lessons and the offline artwork shipped in release. */
class VerbMotionProductionTest {
    @get:Rule val compose=createEmptyComposeRule()
    private fun motion(value:String?) {
        val command=if(value==null)"settings delete global animator_duration_scale"else "settings put global animator_duration_scale $value"
        InstrumentationRegistry.getInstrumentation().uiAutomation.executeShellCommand(command).use{android.os.ParcelFileDescriptor.AutoCloseInputStream(it).readBytes()}
    }
    @Test fun everyProductionAtlasDecodesWithTransparencyAndVisiblePoses() {
        val assets=InstrumentationRegistry.getInstrumentation().targetContext.assets
        val catalogue=VerbContent.lessons.map{it.motion}.distinctBy{it.asset}
        assertEquals(39,catalogue.size)
        catalogue.forEach {m->
            val bitmap=assets.open(m.asset).use{BitmapFactory.decodeStream(it)}
            assertNotNull(m.asset,bitmap);assertTrue(bitmap.hasAlpha());assertTrue(maxOf(bitmap.width,bitmap.height)<=2048)
            assertEquals(0,bitmap.width%m.columns);assertEquals(0,bitmap.height%m.rows)
            val w=bitmap.width/m.columns;val h=bitmap.height/m.rows
            for(frame in 0 until m.columns*m.rows){
                val pixels=IntArray(w*h);bitmap.getPixels(pixels,0,w,frame%m.columns*w,frame/m.columns*h,w,h)
                val coverage=pixels.count{it ushr 24>128}.toFloat()/pixels.size
                assertTrue("${m.asset} frame $frame coverage $coverage",coverage in .015f.. .9f)
            }
            bitmap.recycle()
        }
    }
    @Test fun all53NativeLessonsWorkInBothLanguagesWithPictureStepsAndPausedBackgroundReturn() {
        val instrumentation=InstrumentationRegistry.getInstrumentation();val context=instrumentation.targetContext
        val prefs=context.getSharedPreferences("basti_shell",0)
        val oldLanguage=prefs.getString("lang",null);val oldAudio=prefs.getString("audioMode",null)
        val oldMotion=Settings.Global.getString(context.contentResolver,Settings.Global.ANIMATOR_DURATION_SCALE)
        try {
            motion("0")
            prefs.edit().putString("lang","en").putString("audioMode","off").commit()
            ActivityScenario.launch(MainActivity::class.java).use {scenario->
                compose.openHomeActivity("Learn Verbs")
                for(language in ContentLanguage.entries){
                    if(language==ContentLanguage.GERMAN){
                        compose.onNodeWithTag("shell-options").performClick()
                        compose.waitUntil(10_000){compose.onAllNodesWithTag("options-screen").fetchSemanticsNodes().size==1}
                        compose.onNodeWithText("🇩🇪 Deutsch").performClick()
                        scenario.onActivity{it.onBackPressedDispatcher.onBackPressed()}
                    }
                    compose.waitUntil(20_000){compose.onAllNodesWithTag("native-verb-library").fetchSemanticsNodes().size==1}
                    compose.onNodeWithText(if(language==ContentLanguage.GERMAN)"Verben entdecken"else "Verb Explorer").assertExists()
                    for((index,lesson) in VerbContent.lessons.withIndex()){
                        val card="native-verb-card-${lesson.id.value}"
                        compose.onNodeWithTag("native-verb-library").performScrollToIndex(index+2)
                        compose.onNodeWithTag(card).assertIsDisplayed().performClick()
                        compose.waitUntil(20_000){compose.onAllNodesWithTag("native-verb-picture-0").fetchSemanticsNodes().size==1}
                        repeat(4){compose.onNodeWithTag("native-verb-picture-$it").performScrollTo().assertIsDisplayed()}
                        compose.onNodeWithText(lesson.explanation.display[language]).assertExists()
                        if(language==ContentLanguage.ENGLISH&&index in listOf(4,25,26,30,31,33,46)){
                            compose.onNodeWithTag("native-verb-picture-0").performScrollTo()
                            captureNativeReview("native-lesson-${index+1}",compose)
                        }
                        compose.onNodeWithTag("native-verb-return").performScrollTo().performClick()
                        compose.waitUntil(10_000){compose.onAllNodesWithTag("native-verb-library").fetchSemanticsNodes().size==1}
                        if(index%10==0)android.util.Log.i("NativeVerbReview","Checked $language lesson ${index+1}/53")
                    }
                    if(language==ContentLanguage.ENGLISH){
                        motion("1")
                        val card="native-verb-card-verb.fly"
                        compose.onNodeWithTag("native-verb-library").performScrollToIndex(2);compose.onNodeWithTag(card).assertIsDisplayed().performClick()
                        compose.waitUntil(20_000){compose.onAllNodesWithTag("native-verb-stage").fetchSemanticsNodes().size==1}
                        compose.onNodeWithTag("native-verb-play").performScrollTo().performClick()
                        scenario.moveToState(Lifecycle.State.CREATED);scenario.moveToState(Lifecycle.State.RESUMED)
                        compose.onNodeWithTag("native-verb-stage").assert(SemanticsMatcher.expectValue(androidx.compose.ui.semantics.SemanticsProperties.StateDescription,"Paused"))
                        scenario.recreate()
                        compose.waitUntil(20_000){compose.onAllNodesWithTag("native-verb-stage").fetchSemanticsNodes().size==1}
                        compose.onNodeWithTag("native-verb-stage").assert(SemanticsMatcher.expectValue(androidx.compose.ui.semantics.SemanticsProperties.StateDescription,"Paused"))
                        compose.onNodeWithTag("native-verb-return").performScrollTo().performClick()
                        compose.waitUntil(10_000){compose.onAllNodesWithTag("native-verb-library").fetchSemanticsNodes().size==1}
                    }
                    motion("0")
                }
            }
        }finally{prefs.edit().putString("lang",oldLanguage).putString("audioMode",oldAudio).commit();motion(oldMotion)}
    }
}
