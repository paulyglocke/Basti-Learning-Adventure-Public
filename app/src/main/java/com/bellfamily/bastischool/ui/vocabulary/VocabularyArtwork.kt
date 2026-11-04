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
        ContentId("school.book") to "school_actions/board/object_book.png",
        ContentId("school.pencil") to "school_actions/board/object_pencil.png",
        ContentId("school.paper") to "school_actions/board/object_paper.png",
        ContentId("school.bag") to "school_actions/board/object_bag.png",
        ContentId("school.desk") to "school_actions/board/place_desk.png",
        ContentId("school.shelf") to "school_actions/board/place_shelf.png",
        ContentId("school.teacher") to "school_actions/board/place_teacher.png",
        ContentId("school.cup") to "school_actions/board/place_cup.png",
    ))
}
