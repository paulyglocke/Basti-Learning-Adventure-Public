package com.bellfamily.bastischool.ui.vocabulary

import com.bellfamily.bastischool.learning.models.ContentId
import java.util.Collections

/** Presentation mapping only; saved task/content identities remain unchanged. */
object VocabularyArtwork {
    val paths: Map<ContentId, String> = Collections.unmodifiableMap(mapOf(
        ContentId("animal.dinosaur") to "Animals/canonical/dinosaur.png",
        ContentId("animal.snake") to "Animals/canonical/snake.png",
        ContentId("animal.whale") to "Animals/canonical/whale.png",
        ContentId("animal.horse") to "Animals/canonical/horse.png",
        ContentId("animal.crocodile") to "Animals/canonical/crocodile.png",
        ContentId("animal.fish") to "Animals/canonical/fish.png",
    ))
}
