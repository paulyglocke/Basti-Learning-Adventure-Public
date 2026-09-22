package com.bellfamily.bastischool.learning.session

import com.bellfamily.bastischool.learning.content.ContentRepository
import com.bellfamily.bastischool.learning.models.ContentText
import java.util.Random

data class GenerationRequest(val session: SessionId, val activity: ActivityId, val activityRevision: Int,
                             val policy: SessionPolicy, val seed: Long, val completionText: ContentText)
sealed interface GenerationResult {
    data class Generated(val plan: SessionPlan) : GenerationResult
    data class Rejected(val reason: String) : GenerationResult
}
fun interface TaskGenerator {
    fun generate(request: GenerationRequest, content: ContentRepository): GenerationResult
}

/** Finite reviewed candidates; visit a shuffled cycle before repeating. No rejection-sampling loop. */
class CandidateTaskGenerator(candidates: List<ChoiceQuestion>) : TaskGenerator {
    private val candidates = frozen(candidates)
    override fun generate(request: GenerationRequest, content: ContentRepository): GenerationResult = try {
        require(candidates.size in 1..256) { "Expected 1–256 candidates" }
        require(candidates.map { it.definition }.toSet().size == candidates.size) { "Duplicate task definition" }
        candidates.forEach { question -> question.choices.forEach {
            require(content.find(it) != null) { "Missing candidate content: ${it.value}" }
        } }
        val source = Random(request.seed)
        val canonical = candidates.sortedBy { it.definition.value }
        var cycle = emptyList<ChoiceQuestion>()
        val tasks = (0 until request.policy.round.count).map { index ->
            if (index % canonical.size == 0) cycle = shuffled(canonical, source)
            val question = cycle[index % canonical.size]
            ChoiceTask(TaskInstanceId(request.session, index + 1), question.reordered(shuffled(question.choices, source)))
        }
        val plan = SessionPlan(request.session, request.activity, request.activityRevision, content.version,
            request.policy, tasks, request.completionText)
        plan.validate(content)
        GenerationResult.Generated(plan)
    } catch (error: IllegalArgumentException) {
        GenerationResult.Rejected(error.message ?: "Invalid candidate content")
    }

    private fun <T> shuffled(input: List<T>, source: Random): List<T> = input.toMutableList().apply {
        for (index in lastIndex downTo 1) {
            val selected = source.nextInt(index + 1)
            val previous = this[index]
            this[index] = this[selected]
            this[selected] = previous
        }
    }
}
