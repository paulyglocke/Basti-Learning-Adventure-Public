package com.bellfamily.bastischool.learning.wilma

import com.bellfamily.bastischool.learning.content.*
import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.session.*
import com.bellfamily.bastischool.learning.progress.*

enum class WilmaPhase(val title: ContentText) {
    EXPLORE(ContentText.plain("Learn", "Lernen")),
    FIND(ContentText.plain("Find the day", "Finde den Tag")),
    RELATIONS(ContentText.plain("Before and after", "Davor und danach")),
    ORDER(ContentText.plain("Put the week in order", "Ordne die Woche"))
}
enum class DayRelation { FIND, BEFORE, AFTER }

/** Presentation over canonical weekdays. Head/tail deliberately have no ContentId. */
object WilmaContent {
    val repository = CoreContent.repository()
    val days = WeekdayIds.canonicalOrder
    const val REVISION = 1
    const val HEAD = "Wilma/wilma_head.png"
    const val TAIL = "Wilma/wilma_tail.png"
    const val REFERENCE = "Wilma/wilma_full_reference.png"
    val completion = ContentText.plain("The week is complete. Well done!", "Die Woche ist fertig. Gut gemacht!")
    val roundCompletion = ContentText.plain("Adventure complete! Well done!", "Abenteuer geschafft! Gut gemacht!")
    fun day(id: ContentId) = repository.find(id) as? WeekdayDefinition ?: throw IllegalArgumentException("Unknown weekday")
    fun image(id: ContentId): String {
        val item = day(id)
        return "Wilma/day_segment_${id.value.substringAfter('.')}_${item.colourCue.value.substringAfter('.')}.png"
    }
    fun answer(anchor: ContentId, relation: DayRelation): ContentId {
        day(anchor)
        return when(relation) { DayRelation.FIND -> anchor; DayRelation.BEFORE -> repository.previousDay(anchor).id; DayRelation.AFTER -> repository.nextDay(anchor).id }
    }
    fun question(anchor: ContentId, relation: DayRelation): ChoiceQuestion {
        val name = day(anchor).text.speech
        val correct = answer(anchor,relation); val target = day(correct).text.speech
        val instruction = when(relation) {
            DayRelation.FIND -> ContentText.plain("Find ${name.en}. Tap its part of Wilma.", "Finde ${name.de}. Tippe auf den passenden Teil von Wilma.")
            DayRelation.BEFORE -> ContentText.plain("Which day comes before ${name.en}?", "Welcher Tag kommt vor ${name.de}?")
            DayRelation.AFTER -> ContentText.plain("Which day comes after ${name.en}?", "Welcher Tag kommt nach ${name.de}?")
        }
        val hint = when(relation) {
            DayRelation.FIND -> ContentText.plain("Look along the week. Find ${target.en}.", "Schau dir die Woche an. Finde ${target.de}.")
            DayRelation.BEFORE -> ContentText.plain("Before ${name.en} comes ${target.en}.", "Vor ${name.de} kommt ${target.de}.")
            DayRelation.AFTER -> ContentText.plain("After ${name.en} comes ${target.en}.", "Nach ${name.de} kommt ${target.de}.")
        }
        val kind = relation.name.lowercase()
        return ChoiceQuestion(TaskDefinitionId("task.wilma.$kind.${anchor.value.substringAfter('.') }"),
            SkillId(if(relation==DayRelation.FIND) "skill.weekdays.recognise.${anchor.value.substringAfter('.')}" else "skill.weekdays.$kind"),
            LearningContextId("context.weekdays.wilma"),1,instruction,days,correct,
            ContentText.plain("Yes, ${target.en}.","Ja, ${target.de}."),
            ContentText.plain("Try again. You can ask for help.","Versuche es noch einmal. Du kannst dir helfen lassen."),hint)
    }
    fun activity(phase: WilmaPhase): ActivityId {
        require(phase==WilmaPhase.FIND || phase==WilmaPhase.RELATIONS)
        return ActivityId("activity.wilma.${phase.name.lowercase()}")
    }
    fun generate(phase: WilmaPhase, id: SessionId, round: RoundLength, seed: Long): GenerationResult {
        val relations = if(phase==WilmaPhase.FIND) listOf(DayRelation.FIND) else listOf(DayRelation.BEFORE,DayRelation.AFTER)
        val generated = CandidateTaskGenerator(days.flatMap { d -> relations.map {question(d,it)} }).generate(
            GenerationRequest(id,activity(phase),REVISION,SessionPolicy(round),seed,roundCompletion),repository)
        if(generated !is GenerationResult.Generated) return generated
        val p=generated.plan
        // Wilma's physical sequence stays Monday–Sunday. Only task selection is shuffled.
        val tasks=p.tasks.map { t -> val (anchor,relation)=definition(t.question.definition); ChoiceTask(t.id,question(anchor,relation)) }
        return GenerationResult.Generated(SessionPlan(p.id,p.activity,p.activityRevision,p.contentVersion,p.policy,tasks,p.completionText))
    }
    private fun definition(id: TaskDefinitionId): Pair<ContentId,DayRelation> {
        val pieces=id.value.split('.');require(pieces.size==4 && pieces.take(2)==listOf("task","wilma"))
        return ContentId("day.${pieces[3]}") to DayRelation.valueOf(pieces[2].uppercase())
    }
    fun validate(phase: WilmaPhase, state: SessionState) {
        require(state.plan.activity==activity(phase) && state.plan.activityRevision==REVISION && state.plan.contentVersion==repository.version)
        require(state.plan.policy.wrongAnswer==WrongAnswerPolicy.RETRY && state.plan.completionText==roundCompletion)
        state.plan.tasks.forEach { task ->
            val q=task.question;val (anchor,relation)=definition(q.definition)
            require((phase==WilmaPhase.FIND)==(relation==DayRelation.FIND))
            val expected=question(anchor,relation)
            require(q.choices==days && q.correct==expected.correct && q.skill==expected.skill && q.context==expected.context &&
                q.difficulty==expected.difficulty && q.instruction==expected.instruction && q.hint==expected.hint &&
                q.correctFeedback==expected.correctFeedback && q.wrongFeedback==expected.wrongFeedback)
        }
    }
    fun host(phase: WilmaPhase, journal: ProgressStorage, progress: ProgressRepository) = DurableSessionHost(
        journal,progress,activity(phase),REVISION,repository,{id,round,seed -> generate(phase,id,round,seed)},{validate(phase,it)})
}
