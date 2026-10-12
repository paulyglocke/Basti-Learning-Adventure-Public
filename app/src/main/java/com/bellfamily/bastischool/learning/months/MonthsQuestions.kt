package com.bellfamily.bastischool.learning.months

import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.progress.*
import com.bellfamily.bastischool.learning.session.*
import java.util.Collections
import java.util.Random

/** Explore's browsing checkpoint stays unchanged; quiz destinations own separate shared hosts. */
enum class MonthsMode(val title: ContentText) {
    EXPLORE(ContentText.plain("Explore", "Entdecken")),
    FIND(ContentText.plain("Find the Month", "Finde den Monat")),
    RELATIONS(ContentText.plain("Before and Next", "Davor und danach")),
    MISSING(ContentText.plain("Missing Month", "Fehlender Monat")),
    BUILD_YEAR(ContentText.plain("Build the Year", "Baue das Jahr"))
}

object MonthsQuestions {
    const val REVISION = 1
    enum class Kind { FIND, BEFORE, AFTER }
    data class Definition(val anchor: ContentId, val kind: Kind) {
        init { MonthContent.month(anchor) }
        val id get() = TaskDefinitionId("task.months.${kind.name.lowercase()}.${anchor.value.substringAfter('.')}")
        val correct get() = when (kind) { Kind.FIND -> anchor; Kind.BEFORE -> MonthIds.previous(anchor); Kind.AFTER -> MonthIds.next(anchor) }
        val choices: List<ContentId> get() {
            val index = MonthIds.canonicalOrder.indexOf(anchor)
            val offsets = if (kind == Kind.BEFORE) listOf(-2,-1,0,1) else listOf(-1,0,1,2)
            return offsets.map { MonthIds.canonicalOrder[Math.floorMod(index + it, 12)] }
        }
    }
    val completion = ContentText.plain("You practised the months. Well done!", "Du hast die Monate geübt. Sehr gut!")
    fun activity(mode: MonthsMode): ActivityId {
        require(mode != MonthsMode.EXPLORE && mode != MonthsMode.BUILD_YEAR)
        return ActivityId("activity.months.${mode.name.lowercase()}")
    }
    fun catalogue(mode: MonthsMode): List<Definition> {
        require(mode != MonthsMode.MISSING) { "Missing sequences use MonthsMissing.catalogue" }
        activity(mode)
        val kinds = if (mode == MonthsMode.FIND) listOf(Kind.FIND) else listOf(Kind.BEFORE, Kind.AFTER)
        return MonthIds.canonicalOrder.flatMap { month -> kinds.map { Definition(month, it) } }
    }
    fun definition(mode: MonthsMode, question: ChoiceQuestion) = catalogue(mode).single { it.id == question.definition }
    fun question(task: Definition, choices: List<ContentId> = task.choices): ChoiceQuestion {
        require(choices.size == 4 && choices.toSet() == task.choices.toSet())
        val name = MonthContent.month(task.anchor).text.speech
        val answer = MonthContent.month(task.correct).text.speech
        val prompt = when(task.kind) {
            Kind.FIND -> ContentText.plain("Find ${name.en}.", "Finde ${name.de}.")
            Kind.BEFORE -> ContentText.plain("Which month comes immediately before ${name.en}?", "Welcher Monat kommt direkt vor ${name.de}?")
            Kind.AFTER -> ContentText.plain("Which month comes immediately after ${name.en}?", "Welcher Monat kommt direkt nach ${name.de}?")
        }
        val hint = when(task.kind) {
            Kind.FIND -> ContentText.plain("Listen to the month, then listen to the choices.", "Höre dir den Monat an. Höre dir dann die Auswahl an.")
            Kind.BEFORE -> ContentText.plain("Think of the months in order. Go back one month from ${name.en}.",
                "Denk an die Reihenfolge der Monate. Gehe von ${name.de} einen Monat zurück.")
            Kind.AFTER -> ContentText.plain("Think of the months in order. Go forward one month from ${name.en}.",
                "Denk an die Reihenfolge der Monate. Gehe von ${name.de} einen Monat weiter.")
        }
        val skill = if(task.kind == Kind.FIND) "recognise" else task.kind.name.lowercase()
        return ChoiceQuestion(task.id, SkillId("skill.months.$skill"),
            LearningContextId(if(task.kind == Kind.FIND) "context.months.names" else "context.months.year_order"),
            1, prompt, choices, task.correct, ContentText.plain("Yes, ${answer.en}!", "Ja, ${answer.de}!"),
            ContentText.plain("Try again. You can listen or ask for help.", "Versuche es noch einmal. Du kannst noch einmal hören oder dir helfen lassen."), hint)
    }
    fun generate(mode: MonthsMode, id: SessionId, round: RoundLength, seed: Long): GenerationResult {
        if (mode == MonthsMode.MISSING) return MonthsMissing.generate(id, round, seed)
        val random = Random(seed)
        fun <T> shuffled(items: List<T>) = items.toMutableList().also { Collections.shuffle(it,random) }
        val anchors = shuffled(MonthIds.canonicalOrder).take(round.count)
        val beforeCount = round.count / 2 + if(round == RoundLength.FIVE && random.nextBoolean()) 1 else 0
        val kinds = if(mode == MonthsMode.FIND) List(round.count) { Kind.FIND } else
            shuffled(List(beforeCount) { Kind.BEFORE } + List(round.count-beforeCount) { Kind.AFTER })
        val tasks = anchors.mapIndexed { index, anchor ->
            val task = Definition(anchor,kinds[index])
            ChoiceTask(TaskInstanceId(id,index+1), question(task,shuffled(task.choices)))
        }
        val plan = SessionPlan(id,activity(mode),REVISION,MonthContent.repository.version,SessionPolicy(round),tasks,completion)
        plan.validate(MonthContent.repository)
        return GenerationResult.Generated(plan)
    }
    fun validate(mode: MonthsMode, state: SessionState) {
        if (mode == MonthsMode.MISSING) return MonthsMissing.validate(state)
        val plan = state.plan
        require(plan.activity == activity(mode) && plan.activityRevision == REVISION && plan.contentVersion == MonthContent.repository.version)
        require(plan.policy == SessionPolicy(plan.policy.round) && plan.completionText == completion)
        val definitions = plan.tasks.map { definition(mode,it.question) }
        require(definitions.map { it.anchor }.distinct().size == plan.tasks.size)
        if(mode == MonthsMode.RELATIONS) require(kotlin.math.abs(definitions.count { it.kind == Kind.BEFORE } - definitions.count { it.kind == Kind.AFTER }) <= 1)
        plan.tasks.forEach { task ->
            val q = task.question
            val expected = question(definition(mode,q),q.choices)
            require(q.correct == expected.correct && q.skill == expected.skill && q.context == expected.context &&
                q.difficulty == expected.difficulty && q.instruction == expected.instruction && q.hint == expected.hint &&
                q.correctFeedback == expected.correctFeedback && q.wrongFeedback == expected.wrongFeedback)
        }
    }
    fun host(mode: MonthsMode, journal: ProgressStorage, progress: ProgressRepository) = DurableSessionHost(
        journal,progress,activity(mode),REVISION,MonthContent.repository,
        {id,round,seed -> generate(mode,id,round,seed)}, {validate(mode,it)})
}
