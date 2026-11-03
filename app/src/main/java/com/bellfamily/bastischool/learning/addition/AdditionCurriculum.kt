package com.bellfamily.bastischool.learning.addition

import com.bellfamily.bastischool.learning.content.ContentRepository
import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.progress.*
import com.bellfamily.bastischool.learning.session.*

data class AdditionFact(val left: Int, val right: Int) {
    init { require(left in 1..10 && right in 1..10 && left + right <= 20) }
    val total get() = left + right
    val unordered get() = minOf(left, right) to maxOf(left, right)
}

/** A separately versioned addition range with its own activity, skill and journal. */
interface AdditionCurriculum : ChoiceSessionCurriculum {
    val skill: SkillId
    val context: LearningContextId
    val title: ContentText
    val prompt: ContentText
    val help: ContentText
    val useTenFrame: Boolean

    fun catalogue(): List<AdditionFact>
    fun numberId(number: Int): ContentId
    fun numberValue(id: ContentId): Int
    fun groupDescription(number: Int, first: Boolean, language: ContentLanguage): String
    fun question(fact: AdditionFact, choices: List<ContentId>): ChoiceQuestion
    fun definition(question: ChoiceQuestion): AdditionFact
}
