package com.bellfamily.bastischool.ui.memory

import androidx.compose.ui.test.junit4.ComposeTestRule
import com.bellfamily.bastischool.captureNativeDisplayReview

internal fun captureMemoryReview(name:String,compose:ComposeTestRule) = captureNativeDisplayReview(name,compose)
