package com.bellfamily.bastischool.ui.letters

import android.app.Application
import android.graphics.BitmapFactory
import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.bellfamily.bastischool.audio.android.AndroidSystemSpeechEngine
import com.bellfamily.bastischool.learning.letters.LettersContent
import com.bellfamily.bastischool.learning.models.ContentId
import com.bellfamily.bastischool.ui.addition.AdditionViewModel
import java.util.concurrent.Executors

class LettersViewModel(application: Application) : AdditionViewModel(
    application, LettersContent, "letters-session", { AndroidSystemSpeechEngine(application) }
) {
    var images by mutableStateOf<Map<ContentId, ImageBitmap>>(emptyMap())
        private set
    private val worker = Executors.newSingleThreadExecutor()
    private val main = Handler(Looper.getMainLooper())
    private var closed = false
    private var artworkRequested = false

    override fun setVisible(value: Boolean) {
        super.setVisible(value)
        if (!value || artworkRequested || closed) return
        artworkRequested = true
        worker.execute {
            val loaded = LettersContent.animals.mapNotNull { animal ->
                try {
                    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                    getApplication<Application>().assets.open(animal.image).use { BitmapFactory.decodeStream(it, null, bounds) }
                    if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return@mapNotNull null
                    var sample = 1
                    while ((maxOf(bounds.outWidth, bounds.outHeight).toLong() + sample - 1) / sample > 384) sample *= 2
                    getApplication<Application>().assets.open(animal.image).use { stream ->
                        BitmapFactory.decodeStream(stream, null, BitmapFactory.Options().apply { inSampleSize = sample })
                            ?.asImageBitmap()?.let { animal.id to it }
                    }
                } catch (_: Exception) { null }
            }.toMap()
            main.post { if (!closed) images = loaded }
        }
    }

    override fun onCleared() {
        closed = true
        main.removeCallbacksAndMessages(null)
        worker.shutdownNow()
        super.onCleared()
    }
}
