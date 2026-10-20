package com.bellfamily.bastischool.ui.home

import android.graphics.BitmapFactory
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import com.bellfamily.bastischool.R
import com.bellfamily.bastischool.ui.design.BastiPalette

internal const val DRAGON_ART = "Animals/canonical/dragon.png"
internal val homeAssetPaths = mapOf(
    HomeCategory.MATHS to "Animals/canonical/dinosaur.png",
    HomeCategory.WORDS to DRAGON_ART,
    HomeCategory.ANIMALS to "Animals/canonical/whale.png",
    HomeCategory.COLOURS to "SceneDescriptions/classroom_school/canonical/classroom_006_art_sorting.png",
    HomeCategory.EVERYDAY to "SceneDescriptions/classroom_school/canonical/classroom_007_school_routines.png",
)

@Composable
internal fun homeAsset(path: String): Painter {
    val context = LocalContext.current
    val density = context.resources.displayMetrics.density
    return remember(context, path, density) {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.assets.open(path).use { BitmapFactory.decodeStream(it, null, bounds) }
        var sample = 1
        // Preserve enough pixels for a 240dp detail image; never enlarge a downsampled small original.
        while (maxOf(bounds.outWidth, bounds.outHeight) / (sample * 2) >= 240 * density) sample *= 2
        context.assets.open(path).use {
            BitmapPainter(requireNotNull(BitmapFactory.decodeStream(it, null,
                BitmapFactory.Options().apply { inSampleSize = sample })).asImageBitmap())
        }
    }
}

@Composable
internal fun homeCategoryArt(category: HomeCategory): Painter {
    if (category == HomeCategory.TIME) return painterResource(R.drawable.seasons_center)
    val original = homeAsset(homeAssetPaths.getValue(category))
    if (category !in listOf(HomeCategory.MATHS, HomeCategory.WORDS)) return original
    // Shared decorative composition: unchanged canonical character plus native visual cues.
    // No generated/modified illustration, bitmap export or production asset.
    return remember(original, category) {
        object : Painter() {
            override val intrinsicSize = Size(210f, 120f)
            override fun DrawScope.onDraw() {
                val factor = minOf(size.width * .61f / original.intrinsicSize.width,
                    size.height / original.intrinsicSize.height)
                val artSize = original.intrinsicSize * factor
                translate(top = (size.height - artSize.height) / 2) { with(original) { draw(artSize) } }
                if (category == HomeCategory.MATHS) {
                    // One, two, three blocks: quantity is distinguishable without colour or reading.
                    val block = size.height * .14f
                    repeat(3) { row ->
                        repeat(row + 1) { column ->
                            val position = Offset(size.width * .60f + column * block * 1.2f,
                                size.height * (.18f + row * .27f))
                            drawRoundRect(BastiPalette.surface, position, Size(block, block), CornerRadius(block * .16f))
                            drawRoundRect(BastiPalette.primary, position, Size(block, block), CornerRadius(block * .16f),
                                style = Stroke(block * .12f))
                        }
                    }
                } else {
                    val origin = Offset(size.width * .63f, size.height * .10f)
                    val bubble = Size(size.width * .34f, size.height * .37f)
                    drawRoundRect(BastiPalette.surface, origin, bubble, CornerRadius(size.height * .09f))
                    drawRoundRect(BastiPalette.primary, origin, bubble, CornerRadius(size.height * .09f),
                        style = Stroke(size.height * .018f))
                    val tail = Path().apply {
                        moveTo(size.width * .69f, size.height * .45f)
                        lineTo(size.width * .62f, size.height * .58f)
                        lineTo(size.width * .80f, size.height * .45f)
                    }
                    drawPath(tail, BastiPalette.surface)
                    drawPath(tail, BastiPalette.primary, style = Stroke(size.height * .018f))
                    repeat(3) { dot -> drawCircle(BastiPalette.primary, size.height * .025f,
                        Offset(size.width * (.71f + dot * .09f), size.height * .28f)) }
                }
            }
        }
    }
}
