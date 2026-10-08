package com.bellfamily.bastischool.learning.clock

import com.bellfamily.bastischool.audio.*
import com.bellfamily.bastischool.learning.models.ContentLanguage
import com.bellfamily.bastischool.learning.progress.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class ClockTest {
    @get:Rule val temp = TemporaryFolder()
    @Test fun rangesWrapAndDigitalAreOneState() {
        for (h in 1..12) for (m in 0..59) {
            val t = ClockTime(h,m)
            assertEquals(t, ClockTime.fromMinutes(t.minutes))
            assertEquals(t, t.advance(720))
            assertEquals(t, t.advance(-720))
            assertEquals("$h:${m.toString().padStart(2,'0')}", t.digital)
        }
        listOf(0 to 0, 13 to 0, 1 to -1, 1 to 60).forEach { (h,m) -> assertTrue(runCatching {ClockTime(h,m)}.isFailure) }
        assertEquals(ClockTime(4),ClockTime(3,55).advance(5))
        assertEquals(ClockTime(12),ClockTime(11,55).advance(5))
        assertEquals(ClockTime(1),ClockTime(12,55).advance(5))
        assertEquals(ClockTime(3,55),ClockTime(4).advance(-5))
        assertEquals(ClockTime(11,55),ClockTime(12).advance(-5))
        assertEquals(ClockTime(12,55),ClockTime(1).advance(-5))
    }
    @Test fun continuousGeometry() {
        val examples = listOf(Triple(ClockTime(12),0f,0f), Triple(ClockTime(3),90f,0f),
            Triple(ClockTime(3,15),97.5f,90f), Triple(ClockTime(3,30),105f,180f),
            Triple(ClockTime(3,45),112.5f,270f), Triple(ClockTime(4),120f,0f),
            Triple(ClockTime(6,30),195f,180f), Triple(ClockTime(11,30),345f,180f))
        examples.forEach { (t,h,m) -> assertEquals(h,t.hourAngle,.0001f);assertEquals(m,t.minuteAngle,.0001f) }
    }
    @Test fun dragFullRevolutionAndBackPreservesHourAndSnaps() {
        val drag = ClockDrag(ClockTime(3),0f)
        listOf(90f to ClockTime(3,15),180f to ClockTime(3,30),270f to ClockTime(3,45),0f to ClockTime(4),30f to ClockTime(4,5),0f to ClockTime(4),330f to ClockTime(3,55)).forEach { (angle,t) -> assertEquals(t,drag.move(angle)) }
        assertEquals(ClockTime(3,30),ClockTime(3,28).snap())
        assertEquals(ClockTime(4),ClockTime(3,58).snap())
        assertEquals(ClockTime(12),ClockDrag(ClockTime(11,55),330f).move(0f))
        assertEquals(ClockTime(1),ClockDrag(ClockTime(12,55),330f).move(0f))
        assertEquals(ClockTime(12,55),ClockDrag(ClockTime(1),0f).move(330f))
    }
    @Test fun allAuthoredWholeAndHalfHours() {
        val en = listOf("One","Two","Three","Four","Five","Six","Seven","Eight","Nine","Ten","Eleven","Twelve")
        val de = listOf("Ein","Zwei","Drei","Vier","Fünf","Sechs","Sieben","Acht","Neun","Zehn","Elf","Zwölf")
        val halves = listOf("Halb zwei","Halb drei","Halb vier","Halb fünf","Halb sechs","Halb sieben","Halb acht","Halb neun","Halb zehn","Halb elf","Halb zwölf","Halb eins")
        for(h in 1..12) {
            assertEquals("${en[h-1]} o'clock",ClockWording.phrase(ClockTime(h),ContentLanguage.ENGLISH))
            assertEquals("${de[h-1]} Uhr",ClockWording.phrase(ClockTime(h),ContentLanguage.GERMAN))
            assertEquals("Half past ${en[h-1].lowercase()}",ClockWording.phrase(ClockTime(h,30),ContentLanguage.ENGLISH))
            assertEquals(halves[h-1],ClockWording.phrase(ClockTime(h,30),ContentLanguage.GERMAN))
        }
    }
    @Test fun exactRestoreAndMalformedCheckpointRejected() {
        val disk=AtomicProgressStorage(temp.newFolder(),JvmAtomicCommit)
        assertEquals(ClockExploreState(),ClockStore(disk).read())
        for(language in ContentLanguage.entries) {
            val state=ClockExploreState(ClockTime(11,27),language)
            ClockStore(disk).write(state)
            assertEquals(state,ClockStore(disk).read())
        }
        disk.access {it.replace(byteArrayOf(0,0,0,99))}
        assertTrue(runCatching {ClockStore(disk).read()}.isFailure)
    }
    @Test fun manualAudioPolicySilentOpeningAndReplacement() {
        for(mode in AudioMode.entries) {
            val engine=FakeSpeechEngine()
            val audio=ClockAudio(DefaultAudioController(engine,mode))
            audio.visible(true)
            assertTrue(engine.spoken.isEmpty())
            for(language in ContentLanguage.entries) {
                val state=ClockExploreState(ClockTime(3,30),language)
                audio.listen(state)
                if(mode!=AudioMode.OFF) {
                    assertEquals(ClockWording.phrase(state.time,language),engine.spoken.last().text)
                    assertEquals(language,engine.spoken.last().context.language)
                    audio.cancel()
                    assertTrue(engine.cancelled.contains(engine.spoken.last().id))
                }
            }
            if(mode==AudioMode.OFF) assertTrue(engine.spoken.isEmpty())
            val count=engine.spoken.size
            audio.visible(false);audio.listen(ClockExploreState());audio.visible(true)
            assertEquals(count,engine.spoken.size)
            audio.mode(AudioMode.OFF);audio.listen(ClockExploreState())
            assertEquals(count,engine.spoken.size)
            audio.close()
        }
    }
}
