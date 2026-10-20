package com.bellfamily.bastischool

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.ComposeTestRule
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import androidx.test.core.app.ActivityScenario
import java.util.concurrent.atomic.AtomicReference
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.*

/** Use real presentation navigation; do not bypass dispatch or mutate a route for a test. */
internal fun ComposeTestRule.openHomeActivity(title: String) {
    onNodeWithTag("home-all").performScrollTo().performClick()
    onNodeWithText(title).performScrollTo().performClick()
}

internal fun legacyView(view: View): WebView? = if (view is WebView) view else
    if (view is ViewGroup) (0 until view.childCount).firstNotNullOfOrNull { legacyView(view.getChildAt(it)) } else null

/** Inspect the live DOM and its layout, not just the Kotlin destination or WebView existence. */
internal fun ComposeTestRule.renderedLegacy(scenario: ActivityScenario<MainActivity>, mode: String): JSONObject {
    val value = AtomicReference<String?>(null)
    waitUntil(10_000) {
        scenario.onActivity { activity -> legacyView(activity.window.decorView)?.evaluateJavascript("""
            (() => {
              if (typeof activeScreen === 'undefined' || window.__shellMode !== '$mode') return null;
              const root = document.getElementById(activeScreen);
              const body = document.getElementById(activeScreen === 'verbExplorer' ? 'verbExplorerContent' : 'gameContent');
              if (!root || !body || root.classList.contains('hidden') || !body.innerText.trim()) return null;
              const bounds = body.getBoundingClientRect();
              return JSON.stringify({mode: window.__shellMode, screen: activeScreen, text: body.innerText,
                width: bounds.width, height: bounds.height, checkpoint: serializeLegacySession()});
            })()
        """.trimIndent()) { value.set(it) } }
        value.get()?.let { it != "null" && it != "undefined" } == true
    }
    val result = JSONObject(JSONArray("[${value.get()}]").getString(0))
    assertEquals(mode, result.getString("mode"))
    assertEquals(if(mode=="verbExplorer") "verbExplorer" else "game", result.getString("screen"))
    assertTrue(result.getString("text").isNotBlank())
    assertTrue(result.getDouble("width") > 0)
    assertTrue(result.getDouble("height") > 0)
    return result
}
