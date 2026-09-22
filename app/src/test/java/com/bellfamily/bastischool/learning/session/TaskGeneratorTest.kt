package com.bellfamily.bastischool.learning.session

import com.bellfamily.bastischool.learning.content.ContentRepository
import com.bellfamily.bastischool.learning.models.*
import org.junit.Assert.*
import org.junit.Test
import com.bellfamily.bastischool.learning.session.SessionFixtures as F

class TaskGeneratorTest {
    @Test fun fixedSeedProducesSameSessionRegardlessOfCandidateInsertionOrder() {
        val candidates = F.candidates()
        fun snapshot(values: List<ChoiceQuestion>) = SessionCheckpoint.encode(SessionReducer.start(
            (CandidateTaskGenerator(values).generate(F.request(), F.content) as GenerationResult.Generated).plan,
            ContentLanguage.ENGLISH, F.content).state)
        assertArrayEquals(snapshot(candidates), snapshot(candidates.reversed()))
        assertArrayEquals(snapshot(candidates), snapshot(candidates))
    }
    @Test fun boundedCyclesHaveUniqueInstanceIdsAndKeepEveryCorrectAnswerAcrossManySeeds() {
        for (round in RoundLength.entries) for (seed in 0L..49L) {
            val plan = F.plan(round, seed = seed)
            assertEquals(round.count, plan.tasks.map { it.id }.toSet().size)
            assertEquals(3, plan.tasks.take(3).map { it.question.definition }.toSet().size)
            plan.tasks.forEach {
                assertEquals(F.content.weekdays().map { it.id }.toSet(), it.question.choices.toSet())
                assertTrue(it.question.correct in it.question.choices)
            }
        }
    }
    @Test fun emptyDuplicateOversizedAndMissingContentCandidatesFailExplicitly() {
        val candidates = F.candidates()
        for (bad in listOf(emptyList(), listOf(candidates[0], candidates[0]), List(257) { candidates[0] })) {
            assertTrue(CandidateTaskGenerator(bad).generate(F.request(), F.content) is GenerationResult.Rejected)
        }
        val missing = object : ContentRepository by F.content { override fun find(id: ContentId): ContentDefinition? = null }
        assertTrue(CandidateTaskGenerator(candidates).generate(F.request(), missing) is GenerationResult.Rejected)
    }
    @Test fun taskPlanAndStateCollectionsAreDefensiveImmutableSnapshots() {
        val candidates = F.candidates().toMutableList()
        val generator = CandidateTaskGenerator(candidates)
        candidates.clear()
        val plan = (generator.generate(F.request(), F.content) as GenerationResult.Generated).plan
        val state = SessionReducer.start(plan, ContentLanguage.ENGLISH, F.content).state
        assertThrows(UnsupportedOperationException::class.java) { (plan.tasks as MutableList).clear() }
        assertThrows(UnsupportedOperationException::class.java) { (state.progress as MutableList).clear() }
        assertThrows(UnsupportedOperationException::class.java) { (state.task.question.choices as MutableList).clear() }
    }
    @Test fun invalidAnswerIdentityDuplicateChoicesAndUnboundedAuthoredContentFail() {
        val q = F.candidates().first()
        assertThrows(IllegalArgumentException::class.java) { q.reordered(listOf(q.correct, q.correct)) }
        assertThrows(IllegalArgumentException::class.java) { q.reordered(q.choices.filter { it != q.correct }) }
        assertThrows(IllegalArgumentException::class.java) { SessionId("display text is not identity") }
        assertThrows(IllegalArgumentException::class.java) { SkillId("colour.red") }
        assertThrows(IllegalArgumentException::class.java) { ChoiceQuestion(q.definition, q.skill, q.context, 1,
            ContentText.plain("a".repeat(1001), "b"), q.choices, q.correct, q.correctFeedback, q.wrongFeedback) }
    }
    @Test fun optionalHintIsAbsentWithoutChangingAnswerPolicy() {
        val q = F.candidates().first()
        val noHint = ChoiceQuestion(q.definition, q.skill, q.context, 1, q.instruction, q.choices,
            q.correct, q.correctFeedback, q.wrongFeedback)
        val plan = (CandidateTaskGenerator(listOf(noHint)).generate(F.request(), F.content) as GenerationResult.Generated).plan
        val state = SessionReducer.start(plan, ContentLanguage.ENGLISH, F.content).state
        assertFalse(state.hintAvailable)
        assertSame(state, SessionReducer.reduce(state, SessionAction.Hint(state.task.id)).state)
        assertEquals(AnswerState.RETRY_AVAILABLE, F.answer(state, false).state.current.answer)
    }
}
