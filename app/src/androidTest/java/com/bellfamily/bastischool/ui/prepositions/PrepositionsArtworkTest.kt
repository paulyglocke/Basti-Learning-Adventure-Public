package com.bellfamily.bastischool.ui.prepositions

import androidx.test.platform.app.InstrumentationRegistry
import com.bellfamily.bastischool.learning.models.ContentId
import org.junit.Assert.*
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.IOException

class PrepositionsArtworkTest {
    @Test fun originalFlyingAndUnderwaterPngsDecodeAndCacheOnlyCurrentScene() {
        val assets=InstrumentationRegistry.getInstrumentation().targetContext.assets
        var reads=0
        val loader=PrepositionsArtworkLoader {reads++;assets.open(it)}
        val ids=listOf("snake.on","bird.above","fish.inside").map {ContentId("scene.prepositions.$it")}
        for((index,id) in ids.withIndex()) {
            val bitmap=loader.load(id)!!
            assertEquals(724,bitmap.width);assertEquals(543,bitmap.height)
            assertSame(bitmap,loader.load(id));assertEquals(index+1,reads)
        }
        assertNotNull(loader.load(ids.first()));assertEquals(4,reads) // Earlier scenes were evicted.
    }
    @Test fun missingAndCorruptArtworkReturnCalmFailureWithoutRepeatedDecode() {
        val id=ContentId("scene.prepositions.snake.on")
        var reads=0
        val missing=PrepositionsArtworkLoader {reads++;throw IOException("Missing")}
        assertNull(missing.load(id));assertNull(missing.load(id));assertEquals(1,reads)
        val corrupt=PrepositionsArtworkLoader {ByteArrayInputStream(byteArrayOf(1,2,3))}
        assertNull(corrupt.load(id))
    }
}
