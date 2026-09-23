package com.bellfamily.bastischool.learning.sequencing

import com.bellfamily.bastischool.learning.models.ContentId
import com.bellfamily.bastischool.learning.session.*
import java.util.Collections
import java.util.Random

/** Shared by the two real tap-to-place tasks. No content, audio, storage or navigation ownership. */
class OrderedPlacement(order: List<ContentId>, choices: List<ContentId>, steps: List<TaskProgress>) {
    private val order = order.toList()
    private val steps = steps.toList()
    val index = steps.takeWhile {it.answer == AnswerState.CORRECT}.size
    val completed get() = index == order.size
    val placed: List<ContentId> get() = order.take(index)
    init {
        require(order.size in 2..10 && order.distinct().size == order.size)
        require(choices.size == order.size && choices.toSet() == order.toSet())
        require(steps.size == order.size)
        steps.forEachIndexed { i, p ->
            require(p.attempts in 0..10000 && p.support.replays in 0..10000)
            require(p.answer != AnswerState.INCORRECT)
            if(i < index) require(p.answer == AnswerState.CORRECT && p.lastChoice == order[i])
            if(i > index) require(p == TaskProgress())
            if(p.attempts == 0) require(p.lastChoice == null && p.retries == 0 && p.answer == AnswerState.UNANSWERED)
            else {
                require(p.lastChoice in order.drop(i))
                require(p.retries == p.attempts - if(p.answer == AnswerState.UNANSWERED) 0 else 1)
                require((p.answer == AnswerState.CORRECT) == (p.lastChoice == order[i]))
            }
        }
    }
    fun place(choice: ContentId): List<TaskProgress>? {
        if(completed || choice !in order.drop(index)) return null
        val current = steps[index]
        if(current.answer != AnswerState.UNANSWERED || current.attempts >= 10000) return null
        val next = current.copy(answer = if(choice == order[index]) AnswerState.CORRECT else AnswerState.RETRY_AVAILABLE,
            attempts = current.attempts + 1, lastChoice = choice)
        return steps.toMutableList().also {it[index] = next}
    }
    companion object {
        fun scramble(order: List<ContentId>, seed: Long): List<ContentId> {
            require(order.size in 2..10 && order.distinct().size == order.size)
            val choices = order.toMutableList()
            Collections.shuffle(choices, Random(seed))
            if(choices == order) Collections.rotate(choices, 1)
            return choices.toList()
        }
    }
}
