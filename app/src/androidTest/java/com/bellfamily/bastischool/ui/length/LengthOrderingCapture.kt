package com.bellfamily.bastischool.ui.length

import android.view.View
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.test.platform.app.InstrumentationRegistry
import com.bellfamily.bastischool.captureNativeDisplayReview
import com.bellfamily.bastischool.learning.length.LengthOrdering
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/** Model commits can precede the displayed frame. Capture only the expected, presented UI phase. */
internal fun captureLengthOrderingReview(name: String, compose: ComposeTestRule, state: LengthOrdering.State, decor: View) {
    val tag = when { state.completed -> "length-order-again"; state.solved -> "length-order-next"; state.current.order.isNotEmpty() -> "length-order-place-1"; else -> "length-order-source-${state.task.presentation.first().value.takeLast(1)}" }
    compose.waitUntil(10_000) {
        compose.onAllNodesWithTag(tag).fetchSemanticsNodes().singleOrNull()?.config?.contains(SemanticsProperties.Disabled) == false
    }
    compose.onNodeWithTag(tag).assertIsEnabled()
    if(state.solved && !state.completed)compose.onNodeWithTag(tag).performScrollTo().assertIsDisplayed()
    compose.waitForIdle()
    val instrumentation = InstrumentationRegistry.getInstrumentation()
    val presented = CountDownLatch(1)
    instrumentation.runOnMainSync {
        check(decor.isAttachedToWindow)
        fun frame(remaining: Int) {
            decor.postInvalidateOnAnimation()
            decor.postOnAnimation { if(remaining == 1)presented.countDown() else frame(remaining - 1) }
        }
        frame(3)
    }
    check(presented.await(10, TimeUnit.SECONDS)) { "No display frames for $name" }
    compose.waitUntil(10_000) {
        instrumentation.uiAutomation.rootInActiveWindow?.packageName?.toString() == instrumentation.targetContext.packageName
    }
    captureNativeDisplayReview(name, compose)
}
