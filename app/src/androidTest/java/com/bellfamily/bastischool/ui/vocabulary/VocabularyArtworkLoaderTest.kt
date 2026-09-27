package com.bellfamily.bastischool.ui.vocabulary

import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.IOException

class VocabularyArtworkLoaderTest {
    @Test fun allSixImagesDecodeOnceAtBoundedSizeAndReuseTheSameCache() {
        val assets = InstrumentationRegistry.getInstrumentation().targetContext.assets
        var reads = 0
        val loader = VocabularyArtworkLoader { reads++; assets.open(it) }
        val images = loader.load()
        assertEquals(VocabularyArtwork.paths.keys, images.keys)
        images.values.forEach { assertTrue(it.width in 1..384 && it.height in 1..384) }
        assertSame(images, loader.load())
        assertEquals(12, reads)
    }

    @Test fun missingAndCorruptImagesAreIsolatedAndCachedWithoutReplacingThemWithOtherAnimals() {
        val assets = InstrumentationRegistry.getInstrumentation().targetContext.assets
        var reads = 0
        val loader = VocabularyArtworkLoader { path ->
            reads++
            when(path) {
                VocabularyArtwork.paths.values.first() -> throw IOException("Missing")
                VocabularyArtwork.paths.values.last() -> ByteArrayInputStream(byteArrayOf(1, 2, 3))
                else -> assets.open(path)
            }
        }
        val images = loader.load()
        assertEquals(VocabularyArtwork.paths.keys.drop(1).dropLast(1).toSet(), images.keys)
        assertSame(images, loader.load())
        assertEquals(10, reads)
    }
}
