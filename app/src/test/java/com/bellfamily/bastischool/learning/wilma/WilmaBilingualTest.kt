package com.bellfamily.bastischool.learning.wilma

import com.bellfamily.bastischool.audio.*
import com.bellfamily.bastischool.learning.models.ContentLanguage
import com.bellfamily.bastischool.learning.progress.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class WilmaBilingualTest {
    @get:Rule val temp=TemporaryFolder()

    @Test fun canonicalPairsAndColoursMatchAcrossLanguages() {
        val days=WilmaContent.days.map(WilmaContent::day)
        assertEquals(listOf("Montag","Dienstag","Mittwoch","Donnerstag","Freitag","Samstag","Sonntag"),days.map {it.text.display.de})
        assertEquals(listOf("Monday","Tuesday","Wednesday","Thursday","Friday","Saturday","Sunday"),days.map {it.text.display.en})
        assertEquals(listOf("green","red","yellow","blue","purple","orange","pink"),days.map {it.colourCue.value.substringAfter('.')})
    }

    @Test fun alternatingLanguageTapsUseOneOwnerAndExistingPolicyWithoutOpeningSpeech() {
        for(mode in AudioMode.entries) {
            val engine=FakeSpeechEngine()
            val audio=WilmaAudio(DefaultAudioController(engine,mode))
            audio.visible(true)
            assertTrue(engine.spoken.isEmpty())
            WilmaContent.days.forEach { day ->
                for(language in listOf(ContentLanguage.GERMAN,ContentLanguage.ENGLISH)) {
                    val before=engine.spoken.size
                    audio.day(day,language)
                    assertEquals(before+if(mode==AudioMode.OFF)0 else 1,engine.spoken.size)
                    if(mode!=AudioMode.OFF) {
                        assertEquals(WilmaContent.day(day).text.speech[language],engine.spoken.last().text)
                        assertEquals(language,engine.spoken.last().context.language)
                        assertEquals(SpeechKind.OPTION,engine.spoken.last().kind)
                        assertEquals(SpeechTrigger.MANUAL,engine.spoken.last().trigger)
                    }
                }
            }
            val spoken=engine.spoken.size
            audio.visible(false);audio.day(WilmaContent.days.first(),ContentLanguage.GERMAN)
            audio.visible(true)
            assertEquals(spoken,engine.spoken.size)
            audio.close()
        }
    }

    @Test fun bilingualPhaseUsesExistingBrowseCheckpointAndRetainsExploreDay() {
        val disk=AtomicProgressStorage(temp.newFolder(),JvmAtomicCommit)
        val saved=WilmaSelection(WilmaContent.days.last(),WilmaPhase.BILINGUAL)
        WilmaSelectionStore(disk).write(saved)
        assertEquals(saved,WilmaSelectionStore(disk).read())
    }
}
