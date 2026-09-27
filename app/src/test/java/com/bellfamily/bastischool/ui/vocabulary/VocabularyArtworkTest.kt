package com.bellfamily.bastischool.ui.vocabulary

import com.bellfamily.bastischool.learning.models.ContentId
import com.bellfamily.bastischool.learning.vocabulary.VocabularyContent
import org.junit.Assert.*
import org.junit.Test
import java.io.File
import java.nio.ByteBuffer

class VocabularyArtworkTest {
    @Test fun allSixSemanticItemsResolveToDistinctCanonicalPngs() {
        assertEquals(VocabularyContent.items.map { it.id }.toSet(), VocabularyArtwork.paths.keys)
        assertEquals(6, VocabularyArtwork.paths.values.toSet().size)
        VocabularyArtwork.paths.forEach { (id, path) ->
            assertEquals("Animals/canonical/${id.value.substringAfter('.')}.png", path)
            val bytes = File("src/main/assets", path).readBytes()
            assertArrayEquals(byteArrayOf(-119,80,78,71,13,10,26,10), bytes.copyOfRange(0,8))
            assertEquals("IHDR", String(bytes.copyOfRange(12,16), Charsets.US_ASCII))
            assertTrue(ByteBuffer.wrap(bytes,16,4).int > 0 && ByteBuffer.wrap(bytes,20,4).int > 0)
            assertEquals(6, bytes[25].toInt()) // RGBA; Android decode is tested on the emulator.
        }
        assertNull(VocabularyArtwork.paths[ContentId("animal.unknown")])
    }

    @Test fun existingCelebrationConsumersAgreeOnTheSameCanonicalAssets() {
        com.bellfamily.bastischool.ui.common.CelebrationState.artwork.forEach { (id, path) ->
            assertEquals(path, VocabularyArtwork.paths[id])
        }
    }
}
