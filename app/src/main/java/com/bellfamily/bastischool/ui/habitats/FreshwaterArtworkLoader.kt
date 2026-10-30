package com.bellfamily.bastischool.ui.habitats

import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.bellfamily.bastischool.learning.animals.HabitatExploreContent
import com.bellfamily.bastischool.learning.models.ContentId
import java.io.InputStream

internal data class FreshwaterArtwork(
    val scene: ImageBitmap?,
    val animals: Map<ContentId, ImageBitmap>
)

internal class FreshwaterArtworkLoader(private val open: (String) -> InputStream) {
    private var cached: FreshwaterArtwork? = null

    fun load(): FreshwaterArtwork? {
        cached?.let { return it }
        val scene = decode(HabitatExploreContent.freshwaterScene, 1448, 1086) ?: return null
        val animals = HabitatExploreContent.freshwaterAnimals.mapNotNull { animal ->
            decode(animal.artworkPath)?.let { animal.id to it }
        }.toMap()
        return FreshwaterArtwork(scene, animals).also { cached = it }
    }

    private fun decode(path: String, expectedWidth: Int? = null, expectedHeight: Int? = null): ImageBitmap? = try {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        open(path).use { BitmapFactory.decodeStream(it, null, bounds) }
        if (expectedWidth != null && (bounds.outWidth != expectedWidth || bounds.outHeight != expectedHeight)) null
        else open(path).use {
            BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = 2 })?.asImageBitmap()
        }
    } catch (_: Exception) { null }
}
