package com.bellfamily.bastischool.learning.colours

import com.bellfamily.bastischool.learning.models.ContentLanguage
import org.junit.Assert.assertEquals
import org.junit.Test

class RainbowContentTest {
    @Test fun sevenStableColoursUseConventionalOrderAndLocalizedNames() {
        assertEquals(listOf("colour.red", "colour.orange", "colour.yellow", "colour.green", "colour.blue", "colour.indigo", "colour.violet"),
            RainbowContent.order.map { it.value })
        assertEquals(listOf("red", "orange", "yellow", "green", "blue", "indigo", "violet"),
            RainbowContent.order.map { RainbowContent.definition(it).text.display[ContentLanguage.ENGLISH] })
        assertEquals(listOf("rot", "orange", "gelb", "grün", "blau", "indigo", "violett"),
            RainbowContent.order.map { RainbowContent.definition(it).text.display[ContentLanguage.GERMAN] })
    }

    @Test fun reviewedYellowRemainsTheExistingCanonicalYellow() {
        assertEquals("colour.yellow", RainbowContent.order[2].value)
        assertEquals(0xFFFFEB3B.toInt(), RainbowContent.swatches[2])
    }
}
