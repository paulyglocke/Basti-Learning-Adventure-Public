package com.bellfamily.bastischool.learning.colours

import com.bellfamily.bastischool.learning.content.CoreContent
import com.bellfamily.bastischool.learning.content.ContentRepository
import com.bellfamily.bastischool.learning.content.BundledContentRepository
import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.session.*
import java.util.Random

/** Four reviewed colour concepts. The definitions are shared with the existing colour and weekday cues. */
object ColoursContent {
    const val REVISION = 1
    val red = ContentId("colour.red")
    val blue = ContentId("colour.blue")
    val yellow = ContentId("colour.yellow")
    val green = ContentId("colour.green")
    val order = listOf(red, blue, yellow, green)
    private val sharedDefinitions = CoreContent.repository()
    /** Frozen activity pack version; later unrelated CoreContent additions do not invalidate its journal. */
    val repository: ContentRepository = BundledContentRepository(
        ContentVersion(schema = 1, revision = 1),
        order.map { id -> sharedDefinitions.find(id) as? ColourDefinition ?: error("Missing shared colour ${id.value}") },
        emptyList()
    )
    val version get() = repository.version
    val activity = ActivityId("activity.colours.find_colour")
    val completion = ContentText.plain("You found the colours! Well done!", "Du hast die Farben gefunden! Sehr gut!")
    val exploreTitle = ContentText.plain("Explore Colours", "Farben entdecken")
    val findTitle = ContentText.plain("Find the Colour", "Finde die Farbe")

    fun definition(id: ContentId): ColourDefinition {
        require(id in order) { "Not in the introductory colour set: ${id.value}" }
        return repository.find(id) as? ColourDefinition ?: error("Missing shared colour ${id.value}")
    }

    private fun capitalized(value: String) = value.replaceFirstChar { it.uppercase() }

    fun question(target: ContentId, choices: List<ContentId>): ChoiceQuestion {
        val colour = definition(target)
        require(choices.size == order.size && choices.toSet() == order.toSet())
        val en = colour.text.display.en
        val de = capitalized(colour.text.display.de)
        return ChoiceQuestion(
            definition = TaskDefinitionId("task.colours.find.${target.value.substringAfterLast('.')}") ,
            skill = SkillId("skill.colours.recognition"),
            context = LearningContextId("context.colours.named_swatches"),
            difficulty = 1,
            instruction = ContentText.plain("Find $en.", "Finde $de."),
            choices = choices,
            correct = target,
            correctFeedback = ContentText.plain("That's $en!", "Das ist $de!"),
            wrongFeedback = ContentText.plain("Have another look. Try again.", "Schau noch einmal. Versuch es erneut."),
            hint = ContentText.plain("Listen to the colour name once more and look carefully.",
                "Hör dir den Farbnamen noch einmal an und schau genau hin.")
        )
    }

    /** One shuffled visit to every colour before the round repeats; adjacent repeats are prevented. */
    fun generate(id: SessionId, round: RoundLength, seed: Long): GenerationResult {
        return try {
            val random = Random(seed)
            var cycle = emptyList<ContentId>()
            var previous: ContentId? = null
            val tasks = (0 until round.count).map { index ->
                if (index % order.size == 0) {
                    cycle = order.toMutableList().also { java.util.Collections.shuffle(it, random) }
                    if (cycle.first() == previous) cycle = cycle.toMutableList().also {
                        val swap = it[0]; it[0] = it[1]; it[1] = swap
                    }
                }
                val target = cycle[index % order.size]
                previous = target
                val choices = order.toMutableList().also { java.util.Collections.shuffle(it, random) }
                ChoiceTask(TaskInstanceId(id, index + 1), question(target, choices))
            }
            val plan = SessionPlan(id, activity, REVISION, version, SessionPolicy(round), tasks, completion)
            plan.validate(repository)
            GenerationResult.Generated(plan)
        } catch (error: IllegalArgumentException) {
            GenerationResult.Rejected(error.message ?: "Invalid colour content")
        }
    }

    fun validate(state: SessionState) {
        require(state.plan.activity == activity && state.plan.activityRevision == REVISION)
        require(state.plan.contentVersion == version && state.plan.completionText == completion)
        require(state.plan.policy.wrongAnswer == WrongAnswerPolicy.RETRY)
        val targets = state.plan.tasks.map { task ->
            val q = task.question
            require(q.choices.toSet() == order.toSet() && q.correct in order)
            val expected = question(q.correct, q.choices)
            require(q.definition == expected.definition && q.skill == expected.skill && q.context == expected.context &&
                q.difficulty == expected.difficulty && q.instruction == expected.instruction &&
                q.correctFeedback == expected.correctFeedback && q.wrongFeedback == expected.wrongFeedback && q.hint == expected.hint)
            q.correct
        }
        require(targets.toSet() == order.toSet())
        require(targets.zipWithNext().none { (a, b) -> a == b })
        val counts = targets.groupingBy { it }.eachCount().values
        require(counts.maxOrNull()!! - counts.minOrNull()!! <= 1)
    }
}
