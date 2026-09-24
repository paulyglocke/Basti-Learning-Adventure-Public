package com.bellfamily.bastischool.ui.wilma

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import com.bellfamily.bastischool.learning.wilma.WilmaContent
import org.junit.Assert.*
import org.junit.Test

class WilmaDayColoursTest {
    @Test fun sevenCanonicalCuesStayDistinctAndEveryStateHasReadableText() {
        assertEquals(listOf("green","red","yellow","blue","purple","orange","pink"),
            WilmaContent.days.map { WilmaContent.day(it).colourCue.value.substringAfter('.') })
        assertEquals(7, WilmaContent.days.map(WilmaDayColours::day).distinct().size)
        for (surface in listOf(Color.White, Color(0xFF121212))) {
            WilmaContent.days.forEach { day ->
                val normal = WilmaDayColours.background(day, true, surface)
                val disabled = WilmaDayColours.background(day, false, surface)
                assertEquals(WilmaDayColours.day(day), normal)
                assertNotEquals(normal, disabled)
                for (background in listOf(normal, disabled)) {
                    val a = background.luminance(); val b = WilmaDayColours.foreground(background).luminance()
                    assertTrue("$day on $background", (maxOf(a,b)+.05f)/(minOf(a,b)+.05f) >= 4.5f)
                }
            }
        }
    }
}
