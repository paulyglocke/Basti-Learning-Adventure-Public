package com.bellfamily.bastischool.ui.common

import com.bellfamily.bastischool.learning.models.ContentId
import com.bellfamily.bastischool.learning.vocabulary.VocabularyContent
import java.util.Collections

enum class BalloonPhase { FLOATING, REVEALING, SETTLED }
data class BalloonSlot(val id: Int, val animal: ContentId, val phase: BalloonPhase = BalloonPhase.FLOATING)

/** Ephemeral presentation only: no session actions, progress effects, clock or persistence. */
class CelebrationState private constructor(slots: List<BalloonSlot>) {
    val slots: List<BalloonSlot> = Collections.unmodifiableList(slots.toList())
    fun pop(id: Int): CelebrationState = transition(id, BalloonPhase.FLOATING, BalloonPhase.REVEALING)
    fun settle(id: Int): CelebrationState = transition(id, BalloonPhase.REVEALING, BalloonPhase.SETTLED)
    private fun transition(id: Int, from: BalloonPhase, to: BalloonPhase): CelebrationState {
        if(slots.none {it.id == id && it.phase == from}) return this
        return CelebrationState(slots.map {if(it.id == id) it.copy(phase = to) else it})
    }
    companion object {
        const val COUNT = 5
        // Canonical semantic labels are shared; artwork paths never define animal identity.
        val animals: List<ContentId> = Collections.unmodifiableList(listOf("dinosaur", "snake", "crocodile", "whale", "fish").map {ContentId("animal.$it")})
        val artwork = Collections.unmodifiableMap(mapOf(
            ContentId("animal.dinosaur") to "Animals/canonical/dinosaur.png",
            ContentId("animal.snake") to "Animals/canonical/snake.png",
            ContentId("animal.crocodile") to "Animals/canonical/crocodile.png",
            ContentId("animal.whale") to "Animals/canonical/whale.png",
            ContentId("animal.fish") to "Animals/canonical/fish.png"
        ))
        fun create(completion: String): CelebrationState {
            require(completion.isNotBlank())
            animals.forEach {VocabularyContent.item(it)}
            val offset = Math.floorMod(completion.hashCode(), COUNT)
            return CelebrationState(List(COUNT) {BalloonSlot(it, animals[(it + offset) % COUNT])})
        }
    }
}
