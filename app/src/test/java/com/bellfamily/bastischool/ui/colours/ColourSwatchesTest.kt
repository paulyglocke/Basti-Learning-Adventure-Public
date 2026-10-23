package com.bellfamily.bastischool.ui.colours

import org.junit.Assert.assertTrue
import org.junit.Test

class ColourSwatchesTest {
    @Test fun yellowSampleUsesYellowRatherThanAmberOrOrangeHue() {
        val red = ColourSwatches.yellow.red
        val green = ColourSwatches.yellow.green
        val blue = ColourSwatches.yellow.blue
        val max = maxOf(red, green, blue)
        val min = minOf(red, green, blue)
        val hue = when (max) {
            red -> 60f * ((green - blue) / (max - min))
            green -> 60f * (2f + (blue - red) / (max - min))
            else -> 60f * (4f + (red - green) / (max - min))
        }.let { if (it < 0f) it + 360f else it }

        assertTrue("Expected a clear yellow hue, got $hue°", hue in 45f..70f)
    }
}
