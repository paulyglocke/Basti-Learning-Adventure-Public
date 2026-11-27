package com.bellfamily.bastischool.ui.conservation

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.ComposeTestRule
import com.bellfamily.bastischool.captureNativeDisplayReview
import com.bellfamily.bastischool.waitForNativeArtwork
import com.bellfamily.bastischool.learning.conservation.Conservation

/** Board readiness guarantees that every occupied cell uses the same decoded bitmap. */
internal fun captureConservationReview(name:String,compose:ComposeTestRule,state:Conservation.State?) {
    if(state!=null&&!state.completed) {
        waitForNativeArtwork(compose, "conservation-board-$name", "conservation-art-ready",
            exactCount=true, useUnmergedTree=false)
    }else if(name.contains("chooser")) {
        waitForNativeArtwork(compose, "conservation-chooser-$name",
            "bundled-art-Animals/canonical/dinosaur.png", expectedCount=3, exactCount=true)
    }
    captureNativeDisplayReview(name,compose)
}
