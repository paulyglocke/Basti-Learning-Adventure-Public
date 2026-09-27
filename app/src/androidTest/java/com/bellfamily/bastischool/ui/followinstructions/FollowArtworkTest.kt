package com.bellfamily.bastischool.ui.followinstructions

import androidx.test.platform.app.InstrumentationRegistry
import com.bellfamily.bastischool.learning.follow.FollowContent
import org.junit.Assert.*
import org.junit.Test

class FollowArtworkTest {
    @Test fun canonicalPoolIncludingRepairedHorseDecodesAndCaches() {
        val assets = InstrumentationRegistry.getInstrumentation().targetContext.assets
        var reads = 0
        val loader = FollowArtworkLoader { reads++; assets.open(it) }
        val images = loader.load()
        assertEquals(FollowContent.objects.map { it.id }.toSet(), images.keys)
        images.values.forEach { assertTrue(it.width in 1..384 && it.height in 1..384) }
        assertSame(images, loader.load())
        assertEquals(FollowContent.objects.size * 2, reads)
    }
}
