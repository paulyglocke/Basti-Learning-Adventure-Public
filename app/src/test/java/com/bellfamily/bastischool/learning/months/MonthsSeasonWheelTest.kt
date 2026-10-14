package com.bellfamily.bastischool.learning.months

import com.bellfamily.bastischool.learning.content.SeasonIds
import com.bellfamily.bastischool.learning.models.ContentLanguage
import com.bellfamily.bastischool.learning.progress.AtomicProgressStorage
import com.bellfamily.bastischool.learning.progress.JvmAtomicCommit
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import kotlin.math.roundToInt

class MonthsSeasonWheelTest {
    @get:Rule val temp = TemporaryFolder()

    @Test fun twelveNamesAndExactSeasonQuadrants() {
        assertEquals(12, MonthContent.definitions.size)
        assertEquals(listOf("January", "February", "March", "April", "May", "June", "July", "August",
            "September", "October", "November", "December"), MonthContent.definitions.map { it.text.display.en })
        assertEquals(listOf("Januar", "Februar", "März", "April", "Mai", "Juni", "Juli", "August",
            "September", "Oktober", "November", "Dezember"), MonthContent.definitions.map { it.text.display.de })
        assertEquals(-150f, MonthsSeasonWheelGeometry.start(0))
        assertEquals(-90f, MonthsSeasonWheelGeometry.start(2)) // Spring begins at twelve.
        assertEquals(0f, MonthsSeasonWheelGeometry.start(5)) // Summer begins at three.
        assertEquals(90f, MonthsSeasonWheelGeometry.start(8)) // Autumn begins at six.
        assertEquals(180f, MonthsSeasonWheelGeometry.start(11)) // Winter begins at nine.
        for (index in 0..11) {
            assertEquals(MonthContent.definitions[index].season, MonthsSeasonWheelGeometry.seasonAt(index))
            if (index < 11) assertEquals(30f, MonthsSeasonWheelGeometry.start(index + 1) -
                MonthsSeasonWheelGeometry.start(index))
        }
        assertEquals(30f, MonthsSeasonWheelGeometry.start(0) + 360f - MonthsSeasonWheelGeometry.start(11))
        assertEquals(SeasonIds.WINTER, MonthsSeasonWheelGeometry.seasonAt(11))
        assertEquals(SeasonIds.WINTER, MonthsSeasonWheelGeometry.seasonAt(0))
        assertEquals(SeasonIds.WINTER, MonthsSeasonWheelGeometry.seasonAt(1))
        assertTrue(MonthsSeasonWheelGeometry.matches(0))
        (1..11).forEach { assertFalse(MonthsSeasonWheelGeometry.matches(it)) }
    }

    @Test fun generationRotationSnappingAndCalmRetry() {
        for (seed in 0L..100L) {
            val state = MonthsSeasonWheelState.new(seed)
            assertEquals(state, MonthsSeasonWheelState.new(seed))
            assertTrue(state.rotation in 1..11)
            assertEquals(MonthsWheelFeedback.RETRY, state.check().feedback)
            assertEquals(1, state.check().attempts)
        }
        assertEquals(0, MonthsSeasonWheelGeometry.snappedSteps(14.9f))
        assertEquals(1, MonthsSeasonWheelGeometry.snappedSteps(15f))
        assertEquals(-1, MonthsSeasonWheelGeometry.snappedSteps(-30f))
        val start = MonthsSeasonWheelState.new(11L)
        val retried = start.check().rotate(-start.rotation)
        assertEquals(MonthsWheelFeedback.READY, retried.feedback)
        val complete = retried.check()
        assertEquals(MonthsWheelFeedback.COMPLETE, complete.feedback)
        assertEquals(2, complete.attempts)
        assertEquals(complete, complete.rotate(1))
        assertEquals(complete, complete.check())
    }

    @Test fun restoreRetainsRotationModeSupportAndCompletionWithoutChangingOtherJournals() {
        val file = temp.root.resolve("wheel")
        val store = MonthsSeasonWheelStore(AtomicProgressStorage(file, JvmAtomicCommit))
        assertNull(store.read())
        val snapshot = MonthsSeasonWheelState.new(37).rotate(2).copy(mode = MonthsWheelMode.EXPLORE,
            replayCount = 2, hintUsed = true)
        store.write(snapshot)
        assertEquals(snapshot, MonthsSeasonWheelStore(AtomicProgressStorage(file, JvmAtomicCommit)).read())
        store.write(snapshot.copy(mode = MonthsWheelMode.MATCH).rotate(-snapshot.rotation).check())
        assertEquals(MonthsWheelFeedback.COMPLETE, store.read()!!.feedback)
        assertEquals(MonthsMode.EXPLORE.title.display[ContentLanguage.ENGLISH], "Explore")
        assertEquals("Months and Seasons", MonthsMode.SEASONS_MATCH.title.display.en)
    }

    @Test fun fixedPointerTargetsTheCentreOfOneMonthAtEverySnappedRotation() {
        assertEquals(-75f, MonthsSeasonWheelGeometry.POINTER_ANGLE_DEGREES)
        assertEquals(MonthIds.MARCH, MonthsSeasonWheelGeometry.arrowMonth(0))
        assertEquals(MonthIds.FEBRUARY, MonthsSeasonWheelGeometry.arrowMonth(1))
        assertEquals(MonthIds.APRIL, MonthsSeasonWheelGeometry.arrowMonth(11))
        for (rotation in 0..11) {
            val selectedIndex = MonthIds.canonicalOrder.indexOf(MonthsSeasonWheelGeometry.arrowMonth(rotation))
            val centre = MonthsSeasonWheelGeometry.centre(selectedIndex, rotation)
            val difference = Math.floorMod((centre - MonthsSeasonWheelGeometry.POINTER_ANGLE_DEGREES + 540f)
                .roundToInt(), 360) - 180
            assertEquals("Pointer must be centred on the selected month at rotation $rotation", 0, difference)
        }
    }
}
