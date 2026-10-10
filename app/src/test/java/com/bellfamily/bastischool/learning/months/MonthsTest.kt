package com.bellfamily.bastischool.learning.months

import com.bellfamily.bastischool.audio.*
import com.bellfamily.bastischool.learning.content.SeasonIds
import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.progress.*
import org.junit.Assert.*
import org.junit.Test
import kotlin.math.*

class MonthsTest {
    @Test fun canonicalNamesOrderAndSpeech() {
        val en = listOf("January","February","March","April","May","June","July","August","September","October","November","December")
        val de = listOf("Januar","Februar","März","April","Mai","Juni","Juli","August","September","Oktober","November","Dezember")
        val definitions = MonthContent.definitions
        assertEquals(12, definitions.map { it.id }.distinct().size)
        assertEquals(MonthIds.canonicalOrder, definitions.map { it.id })
        assertEquals(en, definitions.map { it.text.display.en })
        assertEquals(de, definitions.map { it.text.display.de })
        assertEquals(en, definitions.map { it.text.speech.en })
        assertEquals(de, definitions.map { it.text.speech.de })
        definitions.forEach { assertEquals(it, MonthContent.repository.find(it.id)) }
    }
    @Test fun everyNeighbourAndYearBoundary() {
        MonthIds.canonicalOrder.forEachIndexed { i, id ->
            assertEquals(MonthIds.canonicalOrder[(i+1)%12], MonthIds.next(id))
            assertEquals(MonthIds.canonicalOrder[(i+11)%12], MonthIds.previous(id))
        }
        assertEquals(MonthIds.JANUARY, MonthIds.next(MonthIds.DECEMBER))
        assertEquals(MonthIds.DECEMBER, MonthIds.previous(MonthIds.JANUARY))
        assertTrue(runCatching { MonthIds.next(ContentId("month.invalid")) }.isFailure)
    }
    @Test fun meteorologicalSeasonsAreAnExactPartition() {
        val seasons = listOf(SeasonIds.WINTER,SeasonIds.WINTER,SeasonIds.SPRING,SeasonIds.SPRING,SeasonIds.SPRING,
            SeasonIds.SUMMER,SeasonIds.SUMMER,SeasonIds.SUMMER,SeasonIds.AUTUMN,SeasonIds.AUTUMN,SeasonIds.AUTUMN,SeasonIds.WINTER)
        assertEquals(seasons, MonthContent.definitions.map { it.season })
        assertEquals(setOf(3), MonthContent.definitions.groupingBy { it.season }.eachCount().values.toSet())
    }
    @Test fun hitTestingJanuaryAtTopClockwiseAndDecemberAdjacent() {
        MonthIds.canonicalOrder.forEachIndexed { i, id ->
            assertEquals(-90f + 30*i, YearWheelGeometry.angle(id), .001f)
            for (offset in listOf(-14f,0f,14f)) {
                val radians = Math.toRadians((YearWheelGeometry.angle(id)+offset).toDouble())
                assertEquals(id,YearWheelGeometry.hit((cos(radians)*75).toFloat(),(sin(radians)*75).toFloat(),100f))
            }
        }
        assertNull(YearWheelGeometry.hit(0f,0f,100f))
        assertNull(YearWheelGeometry.hit(101f,0f,100f))
        assertNull(YearWheelGeometry.hit(Float.NaN,0f,100f))
        assertNull(YearWheelGeometry.hit(0f,0f,0f))
    }
    private class Disk : ProgressStorage {
        var bytes: ByteArray? = null
        override fun <T> access(block: (ProgressTransaction) -> T): T = block(object : ProgressTransaction {
            override fun read() = bytes?.clone()
            override fun replace(bytes: ByteArray) { this@Disk.bytes = bytes.clone() }
        })
    }
    @Test fun exactBrowsingRestoreRejectsCorruptionWithoutRewriting() {
        val disk = Disk()
        assertEquals(MonthsSelection(), MonthsSelectionStore(disk).read())
        MonthIds.canonicalOrder.forEach {
            MonthsSelectionStore(disk).write(MonthsSelection(it))
            assertEquals(MonthsSelection(it), MonthsSelectionStore(disk).read())
        }
        val valid = disk.bytes!!.clone()
        for (bad in listOf(valid + byteArrayOf(1), valid.take(5).toByteArray(), ByteArray(129), byteArrayOf(0,0,0,99))) {
            disk.bytes = bad
            assertTrue(runCatching { MonthsSelectionStore(disk).read() }.isFailure)
            assertArrayEquals(bad,disk.bytes)
        }
        assertTrue(runCatching { MonthsSelection(ContentId("day.monday")) }.isFailure)
    }
    @Test fun audioUsesFullMonthNamesPolicyAndSilentReturn() {
        AudioMode.entries.forEach { mode ->
            val engine = FakeSpeechEngine()
            val audio = MonthsAudio(DefaultAudioController(engine,mode))
            audio.visible(true)
            assertTrue(engine.spoken.isEmpty())
            ContentLanguage.entries.forEach { lang ->
                MonthContent.definitions.forEach {
                    audio.month(it.id,lang)
                    if(mode != AudioMode.OFF) {
                        assertEquals(it.text.speech[lang],engine.spoken.last().text)
                        assertEquals(lang,engine.spoken.last().context.language)
                        assertEquals(SpeechTrigger.MANUAL,engine.spoken.last().trigger)
                    }
                }
            }
            if(mode == AudioMode.OFF) assertTrue(engine.spoken.isEmpty())
            val count=engine.spoken.size
            audio.visible(false); audio.month(MonthIds.MARCH,ContentLanguage.GERMAN); audio.visible(true)
            assertEquals(count,engine.spoken.size)
            audio.mode(AudioMode.OFF); audio.month(MonthIds.MARCH,ContentLanguage.GERMAN)
            assertEquals(count,engine.spoken.size)
            audio.close()
        }
    }
    @Test fun replacementAndStaleCallbacksCannotReviveSpeechFailure() {
        val engine=FakeSpeechEngine(); var failed=false
        val audio=MonthsAudio(DefaultAudioController(engine,AudioMode.ALL)) {failed=it}
        audio.visible(true)
        audio.month(MonthIds.MARCH,ContentLanguage.GERMAN)
        val old=engine.spoken.last().id
        audio.month(MonthIds.JANUARY,ContentLanguage.ENGLISH)
        assertTrue(old in engine.cancelled)
        engine.emit(old,EngineResult.Failed(SpeechFailure.PLAYBACK))
        assertFalse(failed)
        audio.cancel()
        assertTrue(engine.spoken.last().id in engine.cancelled)
        audio.close()
    }
}
