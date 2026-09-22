package com.bellfamily.bastischool.learning.progress

import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.session.*
import java.io.File
import java.nio.channels.FileChannel
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.nio.file.StandardOpenOption

internal object JvmAtomicCommit : AtomicProgressCommit {
    override fun replace(source: File, target: File) {
        Files.move(source.toPath(), target.toPath(), StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING)
    }
    override fun syncDirectory(directory: File) {
        FileChannel.open(directory.toPath(), StandardOpenOption.READ).use { it.force(true) }
    }
}
internal object ProgressFixtures {
    fun event(session: String = "round-one", number: Int = 1, skill: String = "skill.test.naming",
              activity: String = "activity.test_choice", language: ContentLanguage = ContentLanguage.GERMAN): AttemptEvent {
        val id = SessionId(session)
        val task = TaskInstanceId(id, 1)
        return AttemptEvent(ProgressOrigin(id, ActivityId(activity), 1, ContentVersion(1, 1)),
            TaskEvidence(task, TaskDefinitionId("task.find_monday"), SkillId(skill), LearningContextId("context.test.calendar"), 1),
            AttemptId(task, number), language, ContentId("day.monday"), AttemptOutcome.CORRECT,
            SupportUse(2, hint = true, parentHelp = true))
    }
    fun repository(directory: File, clock: ProgressClock = ProgressClock { 1234 }, commit: AtomicProgressCommit = JvmAtomicCommit) =
        FileProgressRepository(AtomicProgressStorage(directory, commit), clock)
    fun records(repository: ProgressRepository, query: ProgressQuery = ProgressQuery()) =
        (repository.read(query) as ProgressReadResult.Events).records
}
