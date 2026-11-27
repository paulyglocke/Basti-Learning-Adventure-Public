package com.bellfamily.bastischool

import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import androidx.test.runner.lifecycle.Stage
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/** Diagnostics are supplementary: the original test failure always wins. */
internal fun <T> withNativeFailureDiagnostics(record: (Throwable) -> Unit, block: () -> T): T =
    try { block() } catch (first: Throwable) {
        try { record(first) } catch (secondary: Throwable) {
            if (secondary !== first) first.addSuppressed(secondary)
            Log.e("ArtworkReadiness", "Could not collect supplementary diagnostics", secondary)
        }
        throw first
    }

/** Same bounded predicate as the caller; cache a tree while polling, without a post-failure Espresso wait. */
internal fun waitForNativeArtwork(compose: ComposeTestRule, phase: String, tag: String,
    expectedCount: Int = 1, exactCount: Boolean = false, useUnmergedTree: Boolean = true,
    timeoutMillis: Long = 10_000) {
    require(expectedCount > 0 && timeoutMillis > 0)
    var actual = -1
    var tree = "No semantics snapshot was available before the failure."
    var lastSnapshot = 0L
    withNativeFailureDiagnostics({ failure ->
        recordArtworkFailure(phase, tag, expectedCount, exactCount, actual, timeoutMillis, tree, failure)
    }) {
        compose.waitUntil(timeoutMillis) {
            actual = compose.onAllNodesWithTag(tag, useUnmergedTree).fetchSemanticsNodes().size
            val ready = if (exactCount) actual == expectedCount else actual >= expectedCount
            if (!ready && SystemClock.uptimeMillis() - lastSnapshot >= 1_000) {
                tree = runCatching { compose.onRoot(useUnmergedTree = true).printToString() }
                    .getOrElse { "Semantics snapshot failed: $it" }
                lastSnapshot = SystemClock.uptimeMillis()
            }
            ready
        }
    }
}

internal fun nativeDiagnosticsDirectory(): File = File(
    checkNotNull(InstrumentationRegistry.getInstrumentation().targetContext.getExternalFilesDir(null)),
    "ci-diagnostics")

private fun recordArtworkFailure(phase: String, tag: String, expected: Int, exact: Boolean,
    actual: Int, timeout: Long, tree: String, failure: Throwable) {
    val instrumentation = InstrumentationRegistry.getInstrumentation()
    val directory = nativeDiagnosticsDirectory().apply { check(isDirectory || mkdirs()) }
    val prefix = phase.lowercase().replace(Regex("[^a-z0-9]+"), "-").trim('-').take(90)
    val name = "$prefix-${SystemClock.uptimeMillis()}"
    val report = JSONObject().put("phase", phase).put("tag", tag).put("expectedCount", expected)
        .put("predicate", if (exact) "exactly" else "at-least").put("lastActualCount", actual)
        .put("timeoutMillis", timeout).put("failureType", failure.javaClass.name)
        .put("failure", failure.stackTraceToString()).put("semantics", "$name-tree.txt")
        .put("semanticsTiming", "Last snapshot during unsuccessful polling; not a new post-failure idle wait")
    File(directory, "$name-tree.txt").writeText(tree)
    Log.e("ArtworkReadiness", "phase=$phase tag=$tag expected=${if (exact) "exactly" else "at-least"} $expected actual=$actual")
    tree.lineSequence().forEach { Log.e("ArtworkReadiness", it) }

    // A starved main thread must not indefinitely hold up the original exception.
    val activities = JSONArray()
    val observed = CountDownLatch(1)
    var activityError: String? = null
    Handler(Looper.getMainLooper()).post {
        try {
            val monitor = ActivityLifecycleMonitorRegistry.getInstance()
            for (stage in listOf(Stage.RESUMED, Stage.STARTED, Stage.PAUSED, Stage.STOPPED)) {
                monitor.getActivitiesInStage(stage).forEach { activity ->
                    val decor = activity.window.decorView
                    val config = activity.resources.configuration
                    activities.put(JSONObject().put("class", activity.javaClass.name).put("stage", stage.name)
                        .put("width", decor.width).put("height", decor.height).put("attached", decor.isAttachedToWindow)
                        .put("windowFocus", decor.hasWindowFocus()).put("rotation", decor.display?.rotation)
                        .put("orientation", config.orientation).put("fontScale", config.fontScale.toDouble()))
                }
            }
        } catch (secondary: Throwable) {
            activityError = secondary.toString()
        } finally { observed.countDown() }
    }
    if (observed.await(1_500, TimeUnit.MILLISECONDS)) {
        report.put("activities", activities)
        activityError?.let { report.put("activityError", it) }
        runCatching { instrumentation.uiAutomation.rootInActiveWindow?.packageName?.toString() }
            .onSuccess {
                report.put("foregroundPackage", it ?: JSONObject.NULL)
                if (it == null) report.put("foregroundObservation", "No accessibility root was available")
            }
            .onFailure { report.put("foregroundError", it.toString()) }
        runCatching {
            val bitmap = checkNotNull(instrumentation.uiAutomation.takeScreenshot()) { "No display screenshot" }
            try {
                File(directory, "$name.png").outputStream().use {
                    check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, it))
                }
                report.put("screenshot", "$name.png")
            } finally { bitmap.recycle() }
        }.onFailure { report.put("screenshotError", it.toString()) }
    } else report.put("activityObservation", "Main thread did not respond within 1500ms; platform screenshot skipped")
    File(directory, "$name.json").writeText(report.toString(2) + "\n")
    Log.e("ArtworkReadiness", "First-failure report: ${File(directory, "$name.json")}")
}
