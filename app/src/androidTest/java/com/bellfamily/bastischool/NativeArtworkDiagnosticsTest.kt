package com.bellfamily.bastischool

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ComposeTimeoutException
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.bellfamily.bastischool.ui.testing.LayoutTestActivity
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class NativeArtworkDiagnosticsTest {
    @get:Rule val compose = createAndroidComposeRule<LayoutTestActivity>()

    @Test fun missingArtworkRecordsExpectedStateBeforeTeardownAndKeepsTimeout() {
        compose.setContent {
            Box(Modifier.fillMaxSize()) { Text("Diagnostic fixture", Modifier.testTag("diagnostic-present")) }
        }
        val directory = nativeDiagnosticsDirectory()
        val before = directory.listFiles().orEmpty().map { it.name }.toSet()
        try {
            waitForNativeArtwork(compose, "diagnostics-self-test", "deliberately-missing-artwork",
                timeoutMillis=200)
            fail("The absent artwork must still fail")
        } catch (first: ComposeTimeoutException) {
            val reportFile = directory.listFiles().orEmpty().single { it.name !in before && it.extension == "json" }
            val report = JSONObject(reportFile.readText())
            assertEquals("diagnostics-self-test", report.getString("phase"))
            assertEquals("deliberately-missing-artwork", report.getString("tag"))
            assertEquals(0, report.getInt("lastActualCount"))
            assertEquals(first.javaClass.name, report.getString("failureType"))
            assertTrue(directory.resolve(report.getString("semantics")).readText().contains("diagnostic-present"))
            val activities = report.getJSONArray("activities")
            assertTrue((0 until activities.length()).any { i ->
                val activity = activities.getJSONObject(i)
                activity.getString("class") == LayoutTestActivity::class.java.name &&
                    activity.getString("stage") == "RESUMED" && activity.getBoolean("windowFocus") &&
                    activity.getBoolean("attached") && activity.getInt("width") > 0 && activity.getInt("height") > 0
            })
            assertTrue(report.has("foregroundPackage"))
            if (report.isNull("foregroundPackage"))
                assertEquals("No accessibility root was available", report.getString("foregroundObservation"))
            else assertEquals("com.bellfamily.bastischool", report.getString("foregroundPackage"))
            assertTrue(directory.resolve(report.getString("screenshot")).length() > 0)
        }
        compose.onNodeWithTag("diagnostic-present").assertIsDisplayed()
    }

    @Test fun supplementaryFailureCannotReplaceOriginalFailure() {
        val original = AssertionError("original failure")
        val secondary = IllegalStateException("diagnostics unavailable")
        try {
            withNativeFailureDiagnostics({ throw secondary }) { throw original }
            fail("The original failure must escape")
        } catch (caught: AssertionError) {
            assertSame(original, caught)
            assertArrayEquals(arrayOf(secondary), caught.suppressed)
        }
    }
}
