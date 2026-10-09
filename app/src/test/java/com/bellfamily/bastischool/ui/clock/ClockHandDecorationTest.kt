package com.bellfamily.bastischool.ui.clock

import com.bellfamily.bastischool.learning.clock.ClockTime
import org.junit.Assert.*
import org.junit.Test
import kotlin.math.*

class ClockHandDecorationTest {
    @Test fun charactersFollowSemanticAnglesAndLineEndpoints() {
        val cases = listOf(ClockTime(12), ClockTime(3), ClockTime(3,30), ClockTime(6), ClockTime(9), ClockTime(11,30))
        cases.forEach { time ->
            listOf(ClockHandDecoration.hour(time) to time.hourAngle,
                ClockHandDecoration.minute(time) to time.minuteAngle).forEach { (hand, angle) ->
                assertEquals(angle - 90f, hand.rotation, .001f)
                assertEquals(sin(Math.toRadians(angle.toDouble())).toFloat() * hand.length, hand.x, .001f)
                assertEquals(-cos(Math.toRadians(angle.toDouble())).toFloat() * hand.length, hand.y, .001f)
                assertEquals(hand.viewportWidth / hand.viewportHeight, hand.width / hand.height, .001f)
                assertTrue(hand.tailX > 0f && hand.tailX < hand.viewportWidth * .15f)
            }
        }
    }
    @Test fun cardinalOrientationAndContinuousHourMovement() {
        listOf(0 to -90f, 15 to 0f, 30 to 90f, 45 to 180f).forEach { (minute, rotation) ->
            assertEquals(rotation, ClockHandDecoration.minute(ClockTime(3,minute)).rotation, .001f)
        }
        assertEquals(15f, ClockHandDecoration.hour(ClockTime(3,30)).rotation, .001f)
        assertEquals(.48f, ClockHandDecoration.hour(ClockTime(12)).length, .001f)
        assertEquals(.83f, ClockHandDecoration.minute(ClockTime(12)).length, .001f)
    }
}
