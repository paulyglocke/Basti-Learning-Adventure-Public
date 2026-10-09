package com.bellfamily.bastischool.ui.clock

import com.bellfamily.bastischool.learning.clock.ClockTime
import kotlin.math.cos
import kotlin.math.sin

/** Stateless geometry in clock-radius units. SVGs point right, with their tail at the left. */
internal data class ClockHandDecoration(val angle: Float, val length: Float, val width: Float,
    val viewportWidth: Float, val viewportHeight: Float, val tailX: Float, val tailY: Float) {
    val rotation get() = angle - 90f
    val x get() = sin(Math.toRadians(angle.toDouble())).toFloat() * length
    val y get() = -cos(Math.toRadians(angle.toDouble())).toFloat() * length
    val height get() = width * viewportHeight / viewportWidth

    companion object {
        fun hour(time: ClockTime) = ClockHandDecoration(time.hourAngle, .48f, .19f, 300f, 170f, 28f, 104f)
        fun minute(time: ClockTime) = ClockHandDecoration(time.minuteAngle, .83f, .20f, 320f, 180f, 32f, 102f)
    }
}
