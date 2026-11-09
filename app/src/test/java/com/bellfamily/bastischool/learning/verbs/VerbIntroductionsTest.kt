package com.bellfamily.bastischool.learning.verbs

import com.bellfamily.bastischool.audio.*
import com.bellfamily.bastischool.learning.models.*
import org.junit.Assert.*
import org.junit.Test

class VerbIntroductionsTest {
    @Test fun onlySuccessfulCompletePlaybackConsumesTheLanguageAndResetSpecificIntroduction() {
        val heard=mutableMapOf<String,Long>();var epoch=0L
        val guide=VerbIntroductions({heard[it]?:-1},{key,value->heard[key]=value},{epoch})
        val body=ContentText.plain("Which animal can jump?","Welches Tier kann springen?")
        val engine=FakeSpeechEngine();val controller=DefaultAudioController(engine,AudioMode.OFF)
        val owner=SpeechOwner("intro-test");var context=controller.openContext(owner,SpeechSessionId("tutorial"),ContentLanguage.ENGLISH)
        fun play(mode:AudioMode,language:ContentLanguage):VerbIntroductions.Prepared {
            controller.setMode(mode);context=controller.openContext(owner,SpeechSessionId("tutorial"),language)
            val prepared=guide.prepare("quiz",language,body)
            controller.speak(SpeechRequest.fromContent(context,prepared.text,SpeechKind.INSTRUCTION)){prepared.result(it.result)}
            return prepared
        }
        play(AudioMode.OFF,ContentLanguage.ENGLISH);assertTrue(heard.isEmpty())
        val first=play(AudioMode.QUESTIONS,ContentLanguage.ENGLISH);assertTrue(first.text.speech.en.startsWith(VerbContent.quizIntroduction.speech.en))
        controller.cancelOwner(owner);assertTrue(heard.isEmpty())
        play(AudioMode.ALL,ContentLanguage.ENGLISH);engine.emit(engine.spoken.last().id)
        assertEquals(body,guide.prepare("quiz",ContentLanguage.ENGLISH,body).text)
        assertNotEquals(body,guide.prepare("quiz",ContentLanguage.GERMAN,body).text)
        val old=guide.prepare("explorer",ContentLanguage.ENGLISH,body);epoch++
        old.result(SpeechResult.Completed)
        assertNotEquals(body,guide.prepare("explorer",ContentLanguage.ENGLISH,body).text)
        assertNotEquals(body,guide.prepare("quiz",ContentLanguage.ENGLISH,body).text)
        controller.close()
    }
}
