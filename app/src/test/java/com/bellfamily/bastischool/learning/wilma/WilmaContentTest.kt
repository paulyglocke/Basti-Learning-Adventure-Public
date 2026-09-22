package com.bellfamily.bastischool.learning.wilma

import com.bellfamily.bastischool.learning.content.*
import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.session.*
import com.bellfamily.bastischool.learning.progress.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import java.io.File

class WilmaContentTest {
    @get:Rule val temp=TemporaryFolder()
    @Test fun canonicalDaysColoursAndArtworkAreExactAndDecorationIsNotADay() {
        val ids=listOf("monday","tuesday","wednesday","thursday","friday","saturday","sunday")
        val en=listOf("Monday","Tuesday","Wednesday","Thursday","Friday","Saturday","Sunday")
        val de=listOf("Montag","Dienstag","Mittwoch","Donnerstag","Freitag","Samstag","Sonntag")
        val colours=listOf("green","red","yellow","blue","purple","orange","pink")
        assertEquals(ids.map {"day.$it"},WilmaContent.days.map {it.value})
        WilmaContent.days.forEachIndexed {i,id ->
            val day=WilmaContent.day(id)
            assertEquals(en[i],day.text.display.en);assertEquals(de[i],day.text.display.de)
            assertEquals("colour.${colours[i]}",day.colourCue.value)
            assertEquals("Wilma/day_segment_${ids[i]}_${colours[i]}.png",WilmaContent.image(id))
        }
        val paths=WilmaContent.days.map(WilmaContent::image)+listOf(WilmaContent.HEAD,WilmaContent.TAIL,WilmaContent.REFERENCE)
        assertEquals(10,paths.toSet().size)
        paths.forEach {path ->
            val file=File("src/main/assets/$path")
            assertTrue(file.exists());assertArrayEquals(byteArrayOf(-119,80,78,71,13,10,26,10),file.inputStream().use {it.readNBytes(8)})
        }
        assertThrows(IllegalArgumentException::class.java){WilmaContent.day(ContentId("day.head"))}
        assertThrows(IllegalArgumentException::class.java){WilmaSelection(ContentId("day.tail"))}
    }
    @Test fun allBeforeAfterAnswersIncludingWeekBoundaryDeriveFromCycle() {
        WilmaContent.days.forEachIndexed {i,id ->
            val before=WilmaContent.days[(i+6)%7];val after=WilmaContent.days[(i+1)%7]
            assertEquals(before,WilmaContent.question(id,DayRelation.BEFORE).correct)
            assertEquals(after,WilmaContent.question(id,DayRelation.AFTER).correct)
            assertTrue(WilmaContent.question(id,DayRelation.BEFORE).instruction.speech.de.contains("vor ${WilmaContent.day(id).text.speech.de}"))
            assertTrue(WilmaContent.question(id,DayRelation.AFTER).instruction.speech.en.contains("after ${WilmaContent.day(id).text.speech.en}"))
        }
    }
    @Test fun deterministicFiveTenRoundsKeepWilmaInCanonicalChoiceOrder() {
        for(phase in listOf(WilmaPhase.FIND,WilmaPhase.RELATIONS)) for(round in RoundLength.entries) for(seed in 0L..20L) {
            fun start()=SessionReducer.start((WilmaContent.generate(phase,SessionId("test"),round,seed) as GenerationResult.Generated).plan,ContentLanguage.ENGLISH,WilmaContent.repository).state
            val s=start();assertArrayEquals(SessionCheckpoint.encode(s),SessionCheckpoint.encode(start()))
            assertEquals(round.count,s.plan.tasks.size);assertEquals(round.count,s.plan.tasks.map {it.id}.toSet().size)
            assertTrue(s.plan.tasks.all {it.question.choices==WilmaContent.days})
            WilmaContent.validate(phase,s)
            val de=SessionReducer.reduce(s,SessionAction.Language(ContentLanguage.GERMAN)).state
            assertEquals(s.task.id,de.task.id);assertEquals(s.task.question.choices,de.task.question.choices)
        }
    }
    @Test fun browseCheckpointRestoresEveryDayAndPhaseWithoutProgress() {
        val disk=AtomicProgressStorage(temp.newFolder(),JvmAtomicCommit)
        assertEquals(WilmaSelection(),WilmaSelectionStore(disk).read())
        for(day in WilmaContent.days) for(phase in WilmaPhase.entries) {
            val s=WilmaSelection(day,phase);WilmaSelectionStore(disk).write(s);assertEquals(s,WilmaSelectionStore(disk).read())
        }
        disk.access {it.replace(byteArrayOf(1,2,3))}
        assertThrows(IllegalArgumentException::class.java){WilmaSelectionStore(disk).read()}
        assertArrayEquals(byteArrayOf(1,2,3),disk.access {it.read()})
    }
}
