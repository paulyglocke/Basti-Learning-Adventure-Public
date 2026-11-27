package com.bellfamily.bastischool.ui.common

import androidx.compose.ui.graphics.ImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Effects can inherit an unconfined dispatcher in tests; only decoding may leave the UI thread. */
internal suspend fun loadArtworkForUi(load: () -> ImageBitmap?, publish: (ImageBitmap?) -> Unit) {
    val loaded = withContext(Dispatchers.IO) { load() }
    withContext(Dispatchers.Main.immediate) { publish(loaded) }
}
