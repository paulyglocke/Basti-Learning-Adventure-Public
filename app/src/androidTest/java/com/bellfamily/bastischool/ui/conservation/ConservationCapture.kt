package com.bellfamily.bastischool.ui.conservation

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.ComposeTestRule
import com.bellfamily.bastischool.captureNativeDisplayReview
import com.bellfamily.bastischool.learning.conservation.Conservation

/** Board readiness guarantees that every occupied cell uses the same decoded bitmap. */
internal fun captureConservationReview(name:String,compose:ComposeTestRule,state:Conservation.State?) {
    if(state!=null&&!state.completed) {
        compose.waitUntil(10_000){compose.onAllNodesWithTag("conservation-art-ready").fetchSemanticsNodes().size==1}
    }else if(name.contains("chooser")) {
        compose.waitUntil(10_000){compose.onAllNodesWithTag("bundled-art-Animals/canonical/dinosaur.png",useUnmergedTree=true).fetchSemanticsNodes().size==3}
    }
    captureNativeDisplayReview(name,compose)
}
