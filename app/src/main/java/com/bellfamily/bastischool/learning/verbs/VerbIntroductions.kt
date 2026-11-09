package com.bellfamily.bastischool.learning.verbs

import com.bellfamily.bastischool.audio.SpeechResult
import com.bellfamily.bastischool.learning.models.*

/** Tutorial completion follows successful current playback, separately per language and reset epoch. */
class VerbIntroductions(private val read:(String)->Long,private val write:(String,Long)->Unit,private val epoch:()->Long) {
    data class Prepared(val text:ContentText,val result:(SpeechResult)->Unit)
    fun prepare(area:String,language:ContentLanguage,body:ContentText):Prepared {
        require(area in setOf("quiz","explorer"))
        val key="nativeVerbIntro_v1_${area}_${language.name}";val expected=epoch()
        val needed=read(key)!=expected
        val intro=if(area=="quiz")VerbContent.quizIntroduction else VerbContent.explorerIntroduction
        val text=if(needed)ContentText.plain("${intro.speech.en} ${body.speech.en}","${intro.speech.de} ${body.speech.de}")else body
        return Prepared(text){result->if(needed&&result==SpeechResult.Completed&&epoch()==expected)write(key,expected)}
    }
}
