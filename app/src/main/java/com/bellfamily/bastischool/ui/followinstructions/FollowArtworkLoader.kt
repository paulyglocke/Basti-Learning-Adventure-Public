package com.bellfamily.bastischool.ui.followinstructions

import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.bellfamily.bastischool.learning.follow.FollowContent
import com.bellfamily.bastischool.learning.models.ContentId
import com.bellfamily.bastischool.learning.schoolskills.SchoolSkillsContent
import com.bellfamily.bastischool.learning.schoolskills.SchoolActionsContent
import java.io.InputStream

internal class FollowArtworkLoader(private val open: (String) -> InputStream) {
    private var cache: Map<ContentId, ImageBitmap>? = null
    private var schoolScene: ImageBitmap? = null
    fun load(): Map<ContentId, ImageBitmap> {
        cache?.let { return it }
        val loaded = FollowContent.objects.mapNotNull { objectDef ->
            val image = try {
                val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                open(objectDef.assetPath).use { BitmapFactory.decodeStream(it, null, bounds) }
                require(bounds.outWidth > 0 && bounds.outHeight > 0)
                var sample = 1
                while ((maxOf(bounds.outWidth, bounds.outHeight).toLong() + sample - 1) / sample > 384) sample *= 2
                open(objectDef.assetPath).use { BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = sample })?.asImageBitmap() }
            } catch (_: Exception) { null }
            image?.let { objectDef.id to it }
        }.toMap()
        return loaded.also { cache = it }
    }

    fun loadSchoolScene(): ImageBitmap? {
        schoolScene?.let { return it }
        val assetPath = SchoolSkillsContent.sceneAsset
        val image = try {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            open(assetPath).use { BitmapFactory.decodeStream(it, null, bounds) }
            require(bounds.outWidth > 0 && bounds.outHeight > 0)
            var sample = 1
            while ((maxOf(bounds.outWidth, bounds.outHeight).toLong() + sample - 1) / sample > 512) sample *= 2
            open(assetPath).use { BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = sample })?.asImageBitmap() }
        } catch (_: Exception) { null }
        return image.also { schoolScene = it }
    }

    fun loadSchoolPhrases(): Map<ContentId, ImageBitmap> = SchoolSkillsContent.phrases.mapNotNull { phrase ->
        val image = try {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            open(phrase.illustrationAsset).use { BitmapFactory.decodeStream(it, null, bounds) }
            require(bounds.outWidth > 0 && bounds.outHeight > 0)
            var sample = 1
            while ((maxOf(bounds.outWidth, bounds.outHeight).toLong() + sample - 1) / sample > 512) sample *= 2
            open(phrase.illustrationAsset).use { BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = sample })?.asImageBitmap() }
        } catch (_: Exception) { null }
        image?.let { phrase.id to it }
    }.toMap()

    fun loadSchoolActions(): Map<ContentId, ImageBitmap> = SchoolActionsContent.actions.mapNotNull { action ->
        val image = try {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            open(action.illustrationAsset).use { BitmapFactory.decodeStream(it, null, bounds) }
            require(bounds.outWidth > 0 && bounds.outHeight > 0)
            var sample = 1
            while ((maxOf(bounds.outWidth, bounds.outHeight).toLong() + sample - 1) / sample > 512) sample *= 2
            open(action.illustrationAsset).use { BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = sample })?.asImageBitmap() }
        } catch (_: Exception) { null }
        image?.let { action.id to it }
    }.toMap()
}
