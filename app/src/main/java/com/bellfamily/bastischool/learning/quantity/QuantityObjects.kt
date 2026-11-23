package com.bellfamily.bastischool.learning.quantity

import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.vocabulary.VocabularyContent
import java.util.Locale

/** Reviewed count nouns shared by concrete quantity activities; singulars stay canonical. */
object QuantityObjects {
    fun plural(id:ContentId)=when(id.value) {
        "animal.dinosaur"->ContentText.plain("dinosaurs","Dinosaurier")
        "food.apple"->ContentText.plain("apples","Äpfel")
        else->error("No reviewed quantity plural for $id")
    }
    fun noun(id:ContentId,count:Int,language:ContentLanguage):String = if(count!=1)plural(id).display[language]
        else VocabularyContent.item(id).text.display[language].let{if(language==ContentLanguage.ENGLISH)it.lowercase(Locale.ROOT)else it}
}
