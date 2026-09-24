package com.bellfamily.bastischool.ui.wilma

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.luminance
import com.bellfamily.bastischool.learning.models.ContentId
import com.bellfamily.bastischool.learning.wilma.WilmaContent

/** Wilma-only UI swatches sampled from the existing segment PNGs; semantic mapping stays in CoreContent. */
internal object WilmaDayColours {
    private val swatches = mapOf(
        "colour.green" to Color(0xFF8ECA3F),
        "colour.red" to Color(0xFFFB2032),
        "colour.yellow" to Color(0xFFFDDA0B),
        "colour.blue" to Color(0xFF06B5FA),
        "colour.purple" to Color(0xFF9E40D6),
        "colour.orange" to Color(0xFFFC7507),
        "colour.pink" to Color(0xFFFC559E),
    )
    fun day(id: ContentId): Color = swatches.getValue(WilmaContent.day(id).colourCue.value)
    fun background(id: ContentId, enabled: Boolean, surface: Color): Color =
        if (enabled) day(id) else day(id).copy(alpha = .25f).compositeOver(surface)

    /** Choose the higher-contrast neutral foreground, including on faded disabled backgrounds. */
    fun foreground(background: Color): Color = if (background.luminance() > .179f) Color.Black else Color.White
}
