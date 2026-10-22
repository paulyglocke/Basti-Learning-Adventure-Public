package com.bellfamily.bastischool.ui.colours

import androidx.compose.ui.graphics.Color
import com.bellfamily.bastischool.learning.colours.ColoursContent
import com.bellfamily.bastischool.learning.models.ContentId

/** Stable, solid introductory patches; labels remain separate from the sampled area. */
object ColourSwatches {
    val red = Color(0xFFC62828)
    val blue = Color(0xFF1565C0)
    val yellow = Color(0xFFF9A825)
    val green = Color(0xFF2E7D32)
    fun color(id: ContentId): Color = when (id) {
        ColoursContent.red -> red
        ColoursContent.blue -> blue
        ColoursContent.yellow -> yellow
        ColoursContent.green -> green
        else -> error("Unknown introductory colour ${id.value}")
    }
}
