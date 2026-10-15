package com.bellfamily.bastischool.learning.months

import com.bellfamily.bastischool.learning.models.ContentLanguage
import com.bellfamily.bastischool.learning.progress.AtomicProgressStorage
import com.bellfamily.bastischool.learning.progress.JvmAtomicCommit
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream

class MonthsWeatherTest {
    @get:Rule val temp = TemporaryFolder()

    @Test fun allTwelveDefaultsAndPlausibleAlternativesAreComplete() {
        assertEquals(listOf(MonthsWeather.SNOW, MonthsWeather.FROST, MonthsWeather.WIND, MonthsWeather.RAIN,
            MonthsWeather.SUNSHINE, MonthsWeather.SUNSHINE, MonthsWeather.SUNSHINE, MonthsWeather.SUNSHINE,
            MonthsWeather.SUN_AND_CLOUDS, MonthsWeather.WIND, MonthsWeather.RAIN, MonthsWeather.SNOW),
            MonthIds.canonicalOrder.map(MonthsWeatherContent::default))
        MonthIds.canonicalOrder.forEach { month ->
            val options = MonthsWeatherContent.choices(month)
            assertTrue(options.size >= 3)
            assertEquals(options.size, options.toSet().size)
            var current = options.first()
            val visited = mutableSetOf(current)
            repeat(options.size - 1) { current = MonthsWeatherContent.next(month, current); visited.add(current) }
            assertEquals(options.toSet(), visited)
            assertEquals(options.first(), MonthsWeatherContent.next(month, current))
        }
        assertEquals(MonthsWeather.entries.toSet(), MonthIds.canonicalOrder.flatMap(MonthsWeatherContent::choices).toSet())
    }

    @Test fun bilingualPhrasesAndNarrationUseCanonicalMonthAndSeason() {
        assertEquals(listOf("Es schneit!", "Es ist frostig!", "Es regnet leicht!", "Es regnet!",
            "Es ist sonnig!", "Es ist windig!", "Es ist bewölkt!", "Sonne und Wolken!"),
            MonthsWeather.entries.map { it.phrase.display.de })
        assertEquals(listOf("It's snowing!", "It's frosty!", "It's raining gently!", "It's raining!",
            "It's sunny!", "It's windy!", "It's cloudy!", "Sunshine and clouds!"),
            MonthsWeather.entries.map { it.phrase.display.en })
        MonthContent.definitions.forEach { month ->
            MonthsWeatherContent.choices(month.id).forEach { weather ->
                val text = MonthsWeatherContent.narration(month.id, weather)
                ContentLanguage.entries.forEach { language ->
                    assertTrue(text.speech[language].contains(month.text.speech[language]))
                    assertTrue(text.speech[language].contains(MonthContent.season(month.season).text.speech[language]))
                    assertTrue(text.speech[language].contains(weather.phrase.speech[language]))
                    assertTrue(text.speech[language].contains(MonthsWeatherContent.variability.speech[language]))
                }
            }
        }
    }

    @Test fun arrowSelectionDrivesDefaultsAndRapidRotationDropsObsoleteAlternatives() {
        var state = MonthsSeasonWheelState(0, MonthsWheelMode.EXPLORE)
        assertEquals(MonthIds.MARCH, state.selectedMonth)
        repeat(120) {
            val before = state.changeWeather()
            assertNotEquals(state.weather, before.weather)
            state = before.rotate(1)
            assertNull(state.weatherOverride)
            assertEquals(MonthsSeasonWheelGeometry.arrowMonth(state.rotation), state.selectedMonth)
            assertEquals(MonthsWeatherContent.default(state.selectedMonth), state.weather)
        }
        assertEquals(MonthIds.MARCH, state.selectedMonth)
        val altered = state.changeWeather()
        assertEquals(altered, altered.rotate(12))
    }

    @Test fun interruptedSnapsAndShortDragsAlwaysEndOnExactGeometry() {
        for (current in listOf(-735.4f, -13f, 0f, 13f, 58.2f, 359.9f, 734.2f)) {
            for (rotation in 0..11) {
                val target = MonthsSeasonWheelGeometry.animationTarget(current, rotation)
                assertEquals(0f, target % MonthsSeasonWheelGeometry.SEGMENT_DEGREES, 0f)
                assertEquals(rotation, Math.floorMod(MonthsSeasonWheelGeometry.snappedSteps(target), 12))
                assertTrue(kotlin.math.abs(target - current) <= 195f)
            }
        }
        assertEquals(0f, MonthsSeasonWheelGeometry.animationTarget(13f, 0), 0f)
        assertEquals(30f, MonthsSeasonWheelGeometry.animationTarget(13f, 1), 0f)
    }

    @Test fun matchAndHiddenWeatherDoNotCycleOrChangeChallengeRules() {
        val match = MonthsSeasonWheelState(2)
        assertFalse(match.showsWeather)
        assertEquals(match, match.changeWeather())
        assertEquals(MonthsWheelFeedback.RETRY, match.check().feedback)
        assertEquals(MonthsWheelFeedback.COMPLETE, match.rotate(-2).check().feedback)
        val hidden = match.copy(mode = MonthsWheelMode.EXPLORE, weatherEnabled = false)
        assertFalse(hidden.showsWeather)
        assertEquals(hidden, hidden.changeWeather())
        assertEquals(hidden, hidden.check())
    }

    @Test fun newCheckpointRestoresWeatherAndOldCheckpointRestoresExactLearningState() {
        val file = temp.root.resolve("wheel")
        val store = MonthsSeasonWheelStore(AtomicProgressStorage(file, JvmAtomicCommit))
        val state = MonthsSeasonWheelState(2, MonthsWheelMode.EXPLORE, attempts = 3,
            replayCount = 2, hintUsed = true, round = 4).changeWeather().copy(weatherEnabled = false)
        store.write(state)
        assertEquals(state, store.read())
        val old = MonthsSeasonWheelState(0, feedback = MonthsWheelFeedback.COMPLETE,
            attempts = 2, replayCount = 3, hintUsed = true, round = 5)
        val bytes = ByteArrayOutputStream().apply { DataOutputStream(this).use {
            it.writeInt(0x4D535731); it.writeInt(old.rotation); it.writeUTF(old.mode.name)
            it.writeUTF(old.feedback.name); it.writeInt(old.attempts); it.writeInt(old.replayCount)
            it.writeBoolean(old.hintUsed); it.writeInt(old.round)
        } }.toByteArray()
        AtomicProgressStorage(file, JvmAtomicCommit).access { it.replace(bytes) }
        assertEquals(old, store.read())
        assertArrayEquals(bytes, AtomicProgressStorage(file, JvmAtomicCommit).access { it.read() })
        store.write(store.read()!!)
        assertEquals(old, store.read())
    }

    @Test fun showAlignmentResetsWeatherForTheNewSelectedMonth() {
        val january = MonthsSeasonWheelState(2, MonthsWheelMode.EXPLORE).changeWeather()
        assertEquals(MonthsWeather.FROST, january.weather)
        val aligned = january.showAlignment()
        assertEquals(MonthIds.MARCH, aligned.selectedMonth)
        assertEquals(MonthsWeather.WIND, aligned.weather)
        assertTrue(MonthsSeasonWheelGeometry.matches(aligned.rotation))
    }

    @Test(expected = IllegalArgumentException::class)
    fun invalidSavedWeatherCannotBeReinterpretedForAnotherMonth() {
        MonthsSeasonWheelState(8, MonthsWheelMode.EXPLORE, weatherOverride = MonthsWeather.SNOW) // July
    }
}
