package com.bellfamily.bastischool.learning.session

import com.bellfamily.bastischool.learning.content.ContentRepository
import com.bellfamily.bastischool.learning.models.ContentText
import com.bellfamily.bastischool.learning.progress.*

/** Minimum content boundary required by the shared durable choice-session owner. */
interface ChoiceSessionCurriculum {
    val activity: ActivityId
    val revision: Int
    val repository: ContentRepository
    val completion: ContentText
    fun generate(id: SessionId, round: RoundLength, seed: Long): GenerationResult
    fun validate(state: SessionState)

    fun host(journal: ProgressStorage, progress: ProgressRepository) = DurableSessionHost(
        journal, progress, activity, revision, repository, ::generate, ::validate)
}
