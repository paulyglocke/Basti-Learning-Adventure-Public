package com.bellfamily.bastischool.learning.clock

import com.bellfamily.bastischool.learning.models.ContentLanguage
import kotlin.math.roundToInt

/** Teaching time, not a device date. Angles and digital text are derived, never stored. */
data class ClockTime(val hour: Int = 12, val minute: Int = 0) {
    init { require(hour in 1..12 && minute in 0..59) }
    val minutes: Int get() = hour % 12 * 60 + minute
    val minuteAngle: Float get() = minute * 6f
    val hourAngle: Float get() = (hour % 12 + minute / 60f) * 30f
    val digital: String get() = "$hour:${minute.toString().padStart(2, '0')}"
    fun advance(delta: Int) = fromMinutes(minutes + delta)
    fun snap(interval: Int = EXPLORE_STEP): ClockTime {
        require(interval in listOf(5, 30))
        return fromMinutes((minutes.toDouble() / interval).roundToInt() * interval)
    }
    companion object {
        const val EXPLORE_STEP = 5
        fun fromMinutes(value: Int): ClockTime {
            val wrapped = Math.floorMod(value, 720)
            return ClockTime((wrapped / 60).let { if (it == 0) 12 else it }, wrapped % 60)
        }
    }
}

/** Transient pointer accumulator. Unwrap successive angles; never jump hours on touch-down.
 * Events must be less than half a revolution apart. Ignore the unstable central pivot in UI. */
class ClockDrag(time: ClockTime, angle: Float) {
    private var previous = angle
    private var minutes = time.minutes.toDouble()
    fun move(angle: Float): ClockTime {
        val delta = ((angle - previous + 540f) % 360f) - 180f
        previous = angle
        minutes += delta / 6.0
        return ClockTime.fromMinutes(minutes.roundToInt())
    }
}

object ClockWording {
    const val ACTIVITY = "activity.time.clock"
    private val en = listOf("One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine", "Ten", "Eleven", "Twelve")
    private val de = listOf("Ein", "Zwei", "Drei", "Vier", "Fünf", "Sechs", "Sieben", "Acht", "Neun", "Zehn", "Elf", "Zwölf")
    fun phrase(time: ClockTime, language: ContentLanguage): String {
        val german = language == ContentLanguage.GERMAN
        return when (time.minute) {
            0 -> if (german) "${de[time.hour - 1]} Uhr" else "${en[time.hour - 1]} o'clock"
            30 -> if (german) "Halb ${if (time.hour == 12) "eins" else de[time.hour].lowercase()}"
                else "Half past ${en[time.hour - 1].lowercase()}"
            // No new quarter/five-minute curriculum: neutral numeric reading between teaching points.
            else -> if (german) "${de[time.hour - 1]} Uhr ${time.minute}" else "${en[time.hour - 1]} ${if (time.minute < 10) "oh " else ""}${time.minute}"
        }
    }
}
