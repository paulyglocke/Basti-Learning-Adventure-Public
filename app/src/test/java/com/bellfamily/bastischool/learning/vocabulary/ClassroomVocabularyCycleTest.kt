package com.bellfamily.bastischool.learning.vocabulary

import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.session.*
import org.junit.Assert.*
import org.junit.Test

class ClassroomVocabularyCycleTest {
    @Test fun classroomTargetsVisitEveryWordBeforeRepeating() {
        for(phase in listOf(VocabularyPhase.FIND,VocabularyPhase.NAME)) for(round in RoundLength.entries) for(seed in 0L..40L) {
            val plan=(VocabularyContent.generate(phase,VocabularyContent.classroom,SessionId("classroom"),round,seed) as GenerationResult.Generated).plan
            val targets=plan.tasks.map {it.question.correct}
            assertEquals(minOf(8,round.count),targets.take(8).distinct().size)
            assertTrue(targets.zipWithNext().none {(a,b)->a==b})
            assertEquals(round.count,plan.tasks.map {it.question.correct to it.question.choices.toSet()}.distinct().size)
            VocabularyContent.validate(phase,VocabularyContent.classroom,SessionReducer.start(plan,ContentLanguage.ENGLISH,VocabularyContent.classroomPack.repository).state)
        }
    }

    @Test fun olderClassroomPlanWithRepeatedTargetsStillRestoresExactly() {
        val phase=VocabularyPhase.FIND;val category=VocabularyContent.classroom;val id=SessionId("old-classroom")
        val words=listOf("book","pencil","teacher","desk","book").map {ContentId("school.$it")}
        val tasks=words.mapIndexed {index,target ->
            val choices=listOf(target)+VocabularyContent.items(category).map {it.id}.filter {it!=target}.take(3)
            ChoiceTask(TaskInstanceId(id,index+1),VocabularyContent.question(phase,category,target,choices,if(index==4)2 else 0))
        }
        val repository=VocabularyContent.repository(category)
        val plan=SessionPlan(id,VocabularyContent.activity(phase,category),VocabularyContent.REVISION,repository.version,SessionPolicy(RoundLength.FIVE),tasks,VocabularyContent.completion)
        val state=SessionReducer.start(plan,ContentLanguage.GERMAN,repository).state
        val bytes=SessionCheckpoint.encode(state)
        val restored=SessionCheckpoint.restore(bytes,plan.activity,plan.activityRevision,repository) as SessionRestoreResult.Restored
        VocabularyContent.validate(phase,category,restored.state)
        assertArrayEquals(bytes,SessionCheckpoint.encode(restored.state))
    }
}
