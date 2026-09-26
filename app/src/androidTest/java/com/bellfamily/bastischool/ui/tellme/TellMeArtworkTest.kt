package com.bellfamily.bastischool.ui.tellme

import androidx.test.platform.app.InstrumentationRegistry
import com.bellfamily.bastischool.learning.scenedescription.BundledSceneDescriptions
import org.junit.Assert.*
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.IOException

class TellMeArtworkTest {
    @Test fun firstAndLaterCategoryDecodeSampledWithOneSceneCache() {
        val repo = BundledSceneDescriptions.repository()
        val assets = InstrumentationRegistry.getInstrumentation().targetContext.assets
        var reads = 0
        val loader = TellMeArtworkLoader { reads++; assets.open(it) }
        val first = repo.all().first()
        val later = repo.all().last()
        listOf(first, later).forEachIndexed { index, scene ->
            val bitmap = loader.load(scene)!!
            assertTrue(bitmap.width in 1..1024); assertTrue(bitmap.height in 1..1024)
            assertSame(bitmap, loader.load(scene)); assertEquals((index + 1) * 2, reads)
        }
        assertNotNull(loader.load(first)); assertEquals(6, reads)
    }
    @Test fun missingAndCorruptAssetsFailCalmlyAndFailedLoadIsCached() {
        val scene = BundledSceneDescriptions.repository().all().first()
        var reads = 0
        val missing = TellMeArtworkLoader { reads++; throw IOException("Missing") }
        assertNull(missing.load(scene)); assertNull(missing.load(scene)); assertEquals(1, reads)
        assertNull(TellMeArtworkLoader { ByteArrayInputStream(byteArrayOf(1, 2, 3)) }.load(scene))
    }
}
