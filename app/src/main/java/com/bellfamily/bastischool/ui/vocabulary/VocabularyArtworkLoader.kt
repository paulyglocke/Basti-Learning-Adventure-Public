package com.bellfamily.bastischool.ui.vocabulary

import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.bellfamily.bastischool.learning.models.ContentId
import java.io.IOException
import java.io.InputStream

/** Worker-confined, bounded six-image cache. Failed decodes are cached too. */
internal class VocabularyArtworkLoader(private val open: (String) -> InputStream) {
    private var cached: Map<ContentId, ImageBitmap>? = null

    fun load(): Map<ContentId, ImageBitmap> {
        cached?.let { return it }
        val images = VocabularyArtwork.paths.mapNotNull { (id, path) ->
            val image = try {
                val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                open(path).use { BitmapFactory.decodeStream(it, null, bounds) }
                require(bounds.outWidth > 0 && bounds.outHeight > 0)
                var sample = 1
                while ((maxOf(bounds.outWidth, bounds.outHeight).toLong() + sample - 1) / sample > 384) sample *= 2
                open(path).use {
                    BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = sample })?.asImageBitmap()
                }
            } catch (_: IOException) { null } catch (_: IllegalArgumentException) { null }
            image?.let { id to it }
        }.toMap()
        return images.also { cached = it }
    }
}
