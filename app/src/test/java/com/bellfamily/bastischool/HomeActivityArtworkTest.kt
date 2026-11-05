package com.bellfamily.bastischool

import com.bellfamily.bastischool.ui.home.*
import com.bellfamily.bastischool.ui.washhands.WashHandsArtwork
import com.bellfamily.bastischool.learning.washhands.WashHands
import java.io.File
import org.junit.Assert.*
import org.junit.Test

class HomeActivityArtworkTest {
    @Test fun everyActivityHasItsOwnValidArtworkDescriptor() {
        val entries=HomeActivityArtwork.entries
        assertEquals(homeActivities().map {it.homeId()}.toSet(),entries.keys)
        assertEquals(entries.size,entries.values.toSet().size)
        entries.values.forEach { art ->
            assertTrue((art.asset==null)!=(art.diagram==null))
            art.asset?.let {assertTrue("Missing $it",File("src/main/assets",it).isFile)}
        }
    }
    @Test fun allExistingWashStepsHaveIllustrationsWithoutChangingSequenceIdentity() {
        assertEquals(WashHands.order.toSet(),WashHandsArtwork.paths.keys)
        assertEquals(4,WashHandsArtwork.paths.values.toSet().size)
        WashHandsArtwork.paths.values.forEach {path ->
            val bytes=File("src/main/assets",path).readBytes()
            assertTrue(bytes.size>100);assertArrayEquals(byteArrayOf(-1,-40),bytes.take(2).toByteArray())
        }
        assertEquals(1,WashHands.REVISION)
    }
}
