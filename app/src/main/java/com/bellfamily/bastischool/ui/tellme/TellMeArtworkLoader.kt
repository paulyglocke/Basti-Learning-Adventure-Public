package com.bellfamily.bastischool.ui.tellme

import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.bellfamily.bastischool.learning.scenedescription.SceneDescription
import com.bellfamily.bastischool.learning.scenedescription.SceneId
import java.io.InputStream
import java.io.IOException

/** Worker-confined single-scene cache, including a failed load; no runtime metadata parsing. */
internal class TellMeArtworkLoader(private val open: (String) -> InputStream) {
    private var id: SceneId? = null
    private var image: ImageBitmap? = null
    fun load(scene: SceneDescription): ImageBitmap? {
        if(id == scene.id) return image
        id = scene.id
        image = try {
            val bounds = BitmapFactory.Options().apply {inJustDecodeBounds = true}
            open(scene.image.path).use {BitmapFactory.decodeStream(it,null,bounds)}
            require(bounds.outWidth > 0 && bounds.outHeight > 0)
            var sample = 1
            while(bounds.outWidth / sample > 1024 || bounds.outHeight / sample > 1024) sample *= 2
            open(scene.image.path).use {
                BitmapFactory.decodeStream(it,null,BitmapFactory.Options().apply {inSampleSize = sample})?.asImageBitmap()
            }
        } catch(_: IOException) {null} catch(_: IllegalArgumentException) {null}
        return image
    }
}
