package com.bellfamily.bastischool.ui.prepositions

import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.bellfamily.bastischool.learning.models.ContentId
import com.bellfamily.bastischool.learning.prepositions.PrepositionsArtwork
import java.io.InputStream
import java.io.IOException

/** Worker-confined, one current scene only. Language/recomposition never decode another bitmap. */
internal class PrepositionsArtworkLoader(private val open: (String) -> InputStream) {
    private var cachedId: ContentId? = null
    private var cached: ImageBitmap? = null
    fun load(id: ContentId): ImageBitmap? {
        if (cachedId == id) return cached
        cachedId = id
        cached = try {
            open(PrepositionsArtwork.image(id).path).use {
                BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = 2 })?.asImageBitmap()
            }
        } catch (_: IOException) { null } catch (_: IllegalArgumentException) { null }
        return cached
    }
}
