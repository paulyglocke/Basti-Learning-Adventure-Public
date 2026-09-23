package com.bellfamily.bastischool.ui.common

import android.app.Application
import android.graphics.BitmapFactory
import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.AndroidViewModel
import com.bellfamily.bastischool.learning.models.ContentId
import java.util.concurrent.Executors

val LocalCelebrationArt = staticCompositionLocalOf<Map<ContentId, ImageBitmap>> { emptyMap() }

/** Five bundled images, decoded once off the UI thread; no Activity reference or durable state. */
class CelebrationArtViewModel(application: Application) : AndroidViewModel(application) {
    var images by mutableStateOf<Map<ContentId, ImageBitmap>>(emptyMap()); private set
    private val worker = Executors.newSingleThreadExecutor()
    private val main = Handler(Looper.getMainLooper())
    private var closed = false
    init {
        worker.execute {
            val loaded = CelebrationState.artwork.mapNotNull { (id, path) ->
                try {
                    application.assets.open(path).use { stream ->
                        BitmapFactory.decodeStream(stream, null, BitmapFactory.Options().apply { inSampleSize = 2 })
                            ?.asImageBitmap()?.let { id to it }
                    }
                } catch (_: java.io.IOException) { null }
            }.toMap()
            main.post { if(!closed) images = loaded }
        }
        worker.shutdown()
    }
    override fun onCleared() { closed = true; main.removeCallbacksAndMessages(null); worker.shutdownNow() }
}
