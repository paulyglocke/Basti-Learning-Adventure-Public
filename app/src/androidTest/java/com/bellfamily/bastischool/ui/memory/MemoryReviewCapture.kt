package com.bellfamily.bastischool.ui.memory

import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.test.platform.app.InstrumentationRegistry
import com.bellfamily.bastischool.saveNativeReviewBitmap

/** Actual display capture avoids Compose PixelCopy's short callback timeout on a busy emulator. */
internal fun captureMemoryReview(name:String,compose:ComposeTestRule) {
    compose.waitForIdle()
    val instrumentation=InstrumentationRegistry.getInstrumentation()
    instrumentation.waitForIdleSync()
    val bitmap=checkNotNull(instrumentation.uiAutomation.takeScreenshot()) { "Missing emulator screenshot" }
    try { saveNativeReviewBitmap(name,bitmap) } finally { bitmap.recycle() }
}
