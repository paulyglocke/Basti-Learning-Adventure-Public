package com.bellfamily.bastischool.ui.tellme

import android.graphics.Bitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.lifecycle.ViewModelProvider
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import com.bellfamily.bastischool.MainActivity
import com.bellfamily.bastischool.learning.models.ContentLanguage
import com.bellfamily.bastischool.learning.tellme.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class TellMeRouteTest {
    @get:Rule val compose = createEmptyComposeRule()
    @Test fun nativeConversationRetainsStateAcrossOptionsLanguageAndRecreationButHomeResets() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        context.getSharedPreferences("basti_shell", 0).edit().putString("lang", "en").putString("audioMode", "off").commit()
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            fun vm(): TellMeViewModel { var owner: TellMeViewModel? = null; scenario.onActivity { owner = ViewModelProvider(it)[TellMeViewModel::class.java] }; return owner!! }
            fun settled() = compose.waitUntil(10_000) { !vm().loading }
            fun click(tag: String) = compose.onNodeWithTag(tag).performScrollTo().performClick()
            compose.onNodeWithText("Tell Me!").performScrollTo().performClick()
            val category = vm().flow.categories.first().id
            click("tellme-category-${category.value}"); settled()
            assertNotNull(vm().artwork)
            compose.onNodeWithTag("tellme-artwork").performScrollTo()
            java.io.File(context.cacheDir, "tellme-en.png").outputStream().use {
                compose.onRoot().captureToImage().asAndroidBitmap().compress(Bitmap.CompressFormat.PNG, 100, it)
            }
            click("tellme-continue"); click("tellme-continue"); settled()
            click("tellme-help"); click("tellme-grownups"); click("tellme-continue")
            val saved = vm().state
            val owner = vm()
            scenario.recreate(); settled()
            assertSame(owner, vm()); assertEquals(saved, vm().state)
            compose.onNodeWithText("⚙ Options").performClick()
            compose.onNodeWithText("🇩🇪 Deutsch").performClick()
            scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }; settled()
            assertEquals(saved, vm().state)
            compose.onNodeWithTag("tellme-model").performScrollTo().assertTextEquals(vm().flow.support(vm().flow.scene(saved)!!, ContentLanguage.GERMAN).model!!)
            scenario.moveToState(androidx.lifecycle.Lifecycle.State.CREATED)
            scenario.moveToState(androidx.lifecycle.Lifecycle.State.RESUMED)
            assertEquals(saved, vm().state)
            scenario.onActivity { activity ->
                fun hasWeb(view: View): Boolean = view is WebView || view is ViewGroup && (0 until view.childCount).any { hasWeb(view.getChildAt(it)) }
                assertFalse(hasWeb(activity.window.decorView))
            }
            click("tellme-home")
            assertEquals(TellMeState(), vm().state)
            compose.onNodeWithText("Erzähl mal!").performScrollTo().performClick()
            compose.onNodeWithTag("tellme-landing").assertIsDisplayed()
            val later = vm().flow.categories.last().id
            click("tellme-category-${later.value}"); settled()
            assertNotNull(vm().artwork)
            compose.onNodeWithTag("tellme-artwork").performScrollTo().assertContentDescriptionEquals(vm().flow.scene(vm().state)!!.title[ContentLanguage.GERMAN]!!)
            java.io.File(context.cacheDir, "tellme-de.png").outputStream().use {
                compose.onRoot().captureToImage().asAndroidBitmap().compress(Bitmap.CompressFormat.PNG, 100, it)
            }
            // A newly-created owner has no durable conversation checkpoint to restore.
            val fresh = TellMeViewModel(context.applicationContext as android.app.Application)
            assertEquals(TellMeState(), fresh.state)
            val store = androidx.lifecycle.ViewModelStore(); store.put("fresh", fresh); store.clear()
        }
    }
}
