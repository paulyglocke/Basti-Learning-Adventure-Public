package com.bellfamily.bastischool.ui.habitats

import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.bellfamily.bastischool.learning.animals.HabitatExploreContent
import java.io.InputStream

internal class WoodlandArtworkLoader(private val open: (String) -> InputStream) {
    private var cached: ImageBitmap? = null

    fun load(): ImageBitmap? {
        cached?.let { return it }
        val image = try {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            open(HabitatExploreContent.woodlandScene).use { BitmapFactory.decodeStream(it, null, bounds) }
            require(bounds.outWidth == 1448 && bounds.outHeight == 1086)
            open(HabitatExploreContent.woodlandScene).use {
                BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = 2 })?.asImageBitmap()
            }
        } catch (_: Exception) { null }
        cached = image
        return image
    }
}
