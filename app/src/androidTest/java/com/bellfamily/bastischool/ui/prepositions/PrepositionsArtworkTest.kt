package com.bellfamily.bastischool.ui.prepositions

import androidx.test.platform.app.InstrumentationRegistry
import com.bellfamily.bastischool.learning.models.ContentId
import com.bellfamily.bastischool.learning.prepositions.PrepositionsContent
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
            assertTrue(bitmap.width >= 640 && bitmap.height >= 480)
            assertSame(bitmap,loader.load(id));assertEquals(index+1,reads)
        }
        assertNotNull(loader.load(ids.first()));assertEquals(4,reads) // Earlier scenes were evicted.
    }
    @Test fun all52ReviewedScenesIncludingCropsDecodeWithoutFixedDimensions() {
        val assets = InstrumentationRegistry.getInstrumentation().targetContext.assets
        val loader = PrepositionsArtworkLoader { assets.open(it) }
        assertEquals(52, PrepositionsContent.scenes.size)
        PrepositionsContent.scenes.forEach { scene ->
            val bitmap = loader.load(scene.id)
            assertNotNull(scene.id.value, bitmap)
            assertTrue(bitmap!!.width >= 640 && bitmap.height >= 480)
            // Odd crop sizes may round by one decoded pixel with inSampleSize=2.
            assertEquals(4f / 3f, bitmap.width.toFloat() / bitmap.height, 0.069f)
        }
        val nextTo = loader.load(ContentId("scene.prepositions.snake.next_to"))!!
        val original = loader.load(ContentId("scene.prepositions.snake.on"))!!
        assertTrue(nextTo.width < original.width)
        assertEquals(original.height, nextTo.height)
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
