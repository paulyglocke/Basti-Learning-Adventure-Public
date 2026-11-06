package com.bellfamily.bastischool

import android.graphics.BitmapFactory
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

/** Real Home → WebView route and the same offline assets shipped in release. */
class VerbMotionProductionTest {
    @get:Rule val compose=createEmptyComposeRule()
    @Test fun everyProductionAtlasDecodesWithTransparencyAndVisiblePoses() {
        val assets=InstrumentationRegistry.getInstrumentation().targetContext.assets
        val source=assets.open("verb-motion-content.js").bufferedReader().use {it.readText()}
        val catalogue=JSONObject(source.substringAfter("const assets=").substringBefore(";\nconst lessons="))
        assertEquals(39,catalogue.length())
        catalogue.keys().forEach {id->
            val definition=catalogue.getJSONObject(id)
            val columns=definition.getInt("columns");val rows=definition.getInt("rows")
            val bitmap=assets.open(definition.getString("path")).use {BitmapFactory.decodeStream(it)}
            assertNotNull("$id decodes offline",bitmap)
            assertTrue(bitmap.hasAlpha());assertTrue(bitmap.width<=2048&&bitmap.height<=2048)
            assertEquals(0,bitmap.width%columns);assertEquals(0,bitmap.height%rows)
            val width=bitmap.width/columns;val height=bitmap.height/rows
            for(frame in 0 until columns*rows){
                val pixels=IntArray(width*height)
                bitmap.getPixels(pixels,0,width,frame%columns*width,frame/columns*height,width,height)
                val coverage=pixels.count {it ushr 24>128}.toFloat()/pixels.size
                assertTrue("$id pose $frame has visible art and empty space: $coverage",coverage in .015f.. .9f)
            }
            bitmap.recycle()
        }
    }

    @Test fun all53LessonsWorkInBothLanguagesAndBackgroundReturnStaysPaused() {
        val instrumentation=InstrumentationRegistry.getInstrumentation()
        val prefs=instrumentation.targetContext.getSharedPreferences("basti_shell",0)
        val oldLanguage=prefs.getString("lang",null);val oldAudio=prefs.getString("audioMode",null)
        prefs.edit().putString("lang","en").putString("audioMode","off").commit()
        try {
            ActivityScenario.launch(MainActivity::class.java).use {scenario->
                compose.openHomeActivity("Learn Verbs")
                // A cold system WebView can initialize after the Compose route is
                // visible. Wait for the real page/bridge boot before DOM checks.
                compose.waitUntil(60_000){eval(scenario,"typeof bootLegacy === 'function' && typeof activeScreen !== 'undefined' && window.__shellMode === 'verbExplorer'")=="true"}
                compose.renderedLegacy(scenario,"verbExplorer")
                assertEquals(53,eval(scenario,"verbQs.length").toInt())
                for(language in listOf("en","de"))for(index in 0 until 53){
                    eval(scenario,"setLang('$language');openVerbLesson($index,false);true")
                    compose.waitUntil(10_000){eval(scenario,"Boolean(VerbMotion.state()?.ready)")=="true"}
                    val result=JSONObject(decode(eval(scenario,"JSON.stringify({title:document.querySelector('.mainWord').textContent,expected:verbQs[$index].v['$language'],frame:VerbMotion.review(.56).frame})")))
                    assertEquals(result.getString("expected"),result.getString("title"))
                    assertTrue(result.getInt("frame")>=0)
                    eval(scenario,"document.querySelector('[data-motion=pictures]').click();true")
                    assertEquals(4,eval(scenario,"document.querySelectorAll('.verb-picture-steps figure').length").toInt())
                    if(index%10==0)android.util.Log.i("VerbMotionReview","Checked $language lesson ${index+1}/53")
                    if(language=="en"&&index in listOf(4,25,26,30,31,33,46)){
                        // Save genuine device rendering of water, results, hanging and fetch.
                        eval(scenario,"document.querySelector('[data-motion=pictures]').click();VerbMotion.review(.56);window.scrollTo(0,0);true")
                        val folder=File(instrumentation.targetContext.getExternalFilesDir(null),"verb-production-review").apply {mkdirs()}
                        instrumentation.uiAutomation.takeScreenshot()?.let {bitmap->
                            File(folder,"lesson-${index+1}.png").outputStream().use {bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG,100,it)}
                            bitmap.recycle()
                        }
                    }
                }
                eval(scenario,"setLang('en');openVerbLesson(0,false);true")
                compose.waitUntil(10_000){eval(scenario,"Boolean(VerbMotion.state()?.ready)")=="true"}
                eval(scenario,"document.querySelector('[data-motion=play]').click();true")
                scenario.moveToState(Lifecycle.State.CREATED);scenario.moveToState(Lifecycle.State.RESUMED)
                assertEquals("false",eval(scenario,"VerbMotion.state().playing"))
                eval(scenario,"backFromLesson();true")
                assertEquals(53,eval(scenario,"document.querySelectorAll('.verbLibraryCard').length").toInt())
                scenario.onActivity {it.onBackPressedDispatcher.onBackPressed()}
                compose.waitUntil(10_000){compose.onAllNodesWithText("What shall we explore?").fetchSemanticsNodes().isNotEmpty()}
                compose.onNodeWithText("What shall we explore?").assertExists()
            }
        }finally{prefs.edit().putString("lang",oldLanguage).putString("audioMode",oldAudio).commit()}
    }
    private fun decode(value:String)=JSONArray("[$value]").getString(0)
    private fun eval(scenario:ActivityScenario<MainActivity>,expression:String):String {
        val result=AtomicReference<String>();val done=CountDownLatch(1)
        scenario.onActivity {activity->
            val web=legacyView(activity.window.decorView)
            if(web==null){result.set("null");done.countDown()}
            else web.evaluateJavascript(expression) {result.set(it);done.countDown()}
        }
        assertTrue("WebView must answer",done.await(10,TimeUnit.SECONDS))
        return result.get()
    }
}
