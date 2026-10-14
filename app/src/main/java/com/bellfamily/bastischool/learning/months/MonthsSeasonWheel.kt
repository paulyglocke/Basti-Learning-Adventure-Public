package com.bellfamily.bastischool.learning.months

import com.bellfamily.bastischool.learning.content.SeasonIds
import com.bellfamily.bastischool.learning.models.ContentId
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.util.Random
import kotlin.math.roundToInt
import com.bellfamily.bastischool.learning.progress.ProgressStorage

/** Screen angles are measured clockwise from three o'clock, as in Compose Canvas. */
object MonthsSeasonWheelGeometry {
    const val SEGMENT_DEGREES = 30f
    const val SOLVED_JANUARY_CENTRE = -135f
    /** Canvas uses -90° for twelve o'clock; the pointer is half a segment clockwise. */
    const val POINTER_ANGLE_DEGREES = -90f + SEGMENT_DEGREES / 2f

    fun centre(index: Int, rotation: Int = 0): Float {
        require(index in 0..11)
        return SOLVED_JANUARY_CENTRE + (index + Math.floorMod(rotation, 12)) * SEGMENT_DEGREES
    }

    fun start(index: Int, rotation: Int = 0) = centre(index, rotation) - SEGMENT_DEGREES / 2f

    fun snappedSteps(dragDegrees: Float): Int {
        require(dragDegrees.isFinite())
        return (dragDegrees / SEGMENT_DEGREES).roundToInt()
    }

    fun seasonAt(index: Int, rotation: Int = 0): ContentId {
        val centre = Math.floorMod(centre(index, rotation).toInt(), 360)
        return when (centre) {
            in 180 until 270 -> SeasonIds.WINTER
            in 270 until 360 -> SeasonIds.SPRING
            in 0 until 90 -> SeasonIds.SUMMER
            else -> SeasonIds.AUTUMN
        }
    }

    /** Select the segment whose rendered centre is nearest the fixed pointer. */
    fun arrowMonth(rotation: Int): ContentId {
        val index = MonthContent.definitions.indices.minBy { candidate ->
            val difference = Math.floorMod(
                (centre(candidate, rotation) - POINTER_ANGLE_DEGREES + 540f).roundToInt(), 360
            ) - 180
            kotlin.math.abs(difference)
        }
        return MonthIds.canonicalOrder[index]
    }

    fun matches(rotation: Int) = MonthContent.definitions.indices.all { index ->
        seasonAt(index, rotation) == MonthContent.definitions[index].season
    }
}

enum class MonthsWheelMode { MATCH, EXPLORE }
enum class MonthsWheelFeedback { READY, RETRY, COMPLETE }

data class MonthsSeasonWheelState(
    val rotation: Int,
    val mode: MonthsWheelMode = MonthsWheelMode.MATCH,
    val feedback: MonthsWheelFeedback = MonthsWheelFeedback.READY,
    val attempts: Int = 0,
    val replayCount: Int = 0,
    val hintUsed: Boolean = false,
    val round: Int = 1
) {
    init {
        require(rotation in 0..11 && attempts >= 0 && replayCount >= 0 && round > 0)
        require(feedback != MonthsWheelFeedback.COMPLETE || MonthsSeasonWheelGeometry.matches(rotation))
    }

    fun rotate(steps: Int): MonthsSeasonWheelState = if (feedback == MonthsWheelFeedback.COMPLETE) this else
        copy(rotation = Math.floorMod(rotation + steps, 12), feedback = MonthsWheelFeedback.READY)

    fun check(): MonthsSeasonWheelState = if (mode == MonthsWheelMode.EXPLORE || feedback == MonthsWheelFeedback.COMPLETE) this else
        copy(feedback = if (MonthsSeasonWheelGeometry.matches(rotation)) MonthsWheelFeedback.COMPLETE else MonthsWheelFeedback.RETRY,
            attempts = attempts + 1)

    companion object {
        fun new(seed: Long, round: Int = 1) = MonthsSeasonWheelState(Random(seed).nextInt(11) + 1, round = round)
    }
}

/** Separate browsing checkpoint. It does not alter existing Months quiz journals or progress IDs. */
class MonthsSeasonWheelStore(private val storage: ProgressStorage) {
    fun read(): MonthsSeasonWheelState? = storage.access { transaction ->
        val bytes = transaction.read() ?: return@access null
        require(bytes.size in 29..80)
        DataInputStream(ByteArrayInputStream(bytes)).use { input ->
            require(input.readInt() == 0x4D535731) // MSW1
            val state = MonthsSeasonWheelState(input.readInt(), MonthsWheelMode.valueOf(input.readUTF()),
                MonthsWheelFeedback.valueOf(input.readUTF()), input.readInt(), input.readInt(), input.readBoolean(), input.readInt())
            require(input.available() == 0)
            state
        }
    }

    fun write(state: MonthsSeasonWheelState) {
        val bytes = ByteArrayOutputStream()
        DataOutputStream(bytes).use { out ->
            out.writeInt(0x4D535731); out.writeInt(state.rotation); out.writeUTF(state.mode.name)
            out.writeUTF(state.feedback.name); out.writeInt(state.attempts); out.writeInt(state.replayCount)
            out.writeBoolean(state.hintUsed); out.writeInt(state.round)
        }
        storage.access { it.replace(bytes.toByteArray()) }
    }
}
