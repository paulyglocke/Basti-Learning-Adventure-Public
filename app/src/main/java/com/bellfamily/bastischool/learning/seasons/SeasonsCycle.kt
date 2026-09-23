package com.bellfamily.bastischool.learning.seasons

import com.bellfamily.bastischool.learning.content.SeasonIds
import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.session.*
import com.bellfamily.bastischool.learning.progress.*

/** Two explicit practice modes; original recognition activity/version/journal remain unchanged. */
object SeasonsCycle {
    const val REVISION = 1
    fun activity(phase: SeasonsPhase): ActivityId {
        require(phase == SeasonsPhase.NEXT || phase == SeasonsPhase.BEFORE)
        return ActivityId("activity.seasons.${phase.name.lowercase()}")
    }
    fun question(phase: SeasonsPhase, anchor: ContentId, choices: List<ContentId>): ChoiceQuestion {
        activity(phase)
        require(choices.size == 4 && choices.toSet() == SeasonIds.canonicalOrder.toSet())
        val name = SeasonsContent.season(anchor).text.speech
        val correct = if(phase == SeasonsPhase.NEXT) SeasonIds.next(anchor) else SeasonIds.previous(anchor)
        val answer = SeasonsContent.season(correct).text.speech
        val instruction = if(phase == SeasonsPhase.NEXT)
            ContentText.plain("Which season comes after ${name.en}?", "Welche Jahreszeit kommt nach dem ${name.de}?")
        else ContentText.plain("Which season comes before ${name.en}?", "Welche Jahreszeit kommt vor dem ${name.de}?")
        val explanation = if(phase == SeasonsPhase.NEXT)
            ContentText.plain("After ${name.en} comes ${answer.en}. The seasons repeat in a cycle.",
                "Nach dem ${name.de} kommt der ${answer.de}. Die Jahreszeiten wiederholen sich im Kreis.")
        else ContentText.plain("Before ${name.en} comes ${answer.en}. The seasons repeat in a cycle.",
                "Vor dem ${name.de} kommt der ${answer.de}. Die Jahreszeiten wiederholen sich im Kreis.")
        return ChoiceQuestion(TaskDefinitionId("task.seasons.${phase.name.lowercase()}.${anchor.value.substringAfter('.')}"),
            SkillId("skill.seasons.${phase.name.lowercase()}"),LearningContextId("context.seasons.year_cycle"),1,
            instruction,choices,correct,explanation,ContentText.plain("Try again. You can listen or ask for help.",
                "Versuche es noch einmal. Du kannst noch einmal hören oder dir helfen lassen."),explanation)
    }
    fun anchor(phase: SeasonsPhase, question: ChoiceQuestion): ContentId = SeasonIds.canonicalOrder.single {
        question.definition == question(phase,it,SeasonIds.canonicalOrder).definition
    }
    fun generate(phase: SeasonsPhase,id: SessionId,round: RoundLength,seed: Long) = CandidateTaskGenerator(
        SeasonIds.canonicalOrder.map {question(phase,it,SeasonIds.canonicalOrder)}).generate(
        GenerationRequest(id,activity(phase),REVISION,SessionPolicy(round),seed,SeasonsContent.completion),SeasonsContent.repository)
    fun validate(phase: SeasonsPhase,state: SessionState) {
        require(state.plan.activity == activity(phase) && state.plan.activityRevision == REVISION && state.plan.contentVersion == SeasonsContent.repository.version)
        require(state.plan.policy == SessionPolicy(state.plan.policy.round) && state.plan.completionText == SeasonsContent.completion)
        state.plan.tasks.forEach {task ->
            val q=task.question;val expected=question(phase,anchor(phase,q),q.choices)
            require(q.correct == expected.correct && q.skill == expected.skill && q.context == expected.context &&
                q.difficulty == expected.difficulty && q.instruction == expected.instruction && q.hint == expected.hint &&
                q.correctFeedback == expected.correctFeedback && q.wrongFeedback == expected.wrongFeedback)
        }
    }
    fun host(phase: SeasonsPhase,journal: ProgressStorage,progress: ProgressRepository) = DurableSessionHost(journal,progress,
        activity(phase),REVISION,SeasonsContent.repository,{id,round,seed -> generate(phase,id,round,seed)},{validate(phase,it)})
}
