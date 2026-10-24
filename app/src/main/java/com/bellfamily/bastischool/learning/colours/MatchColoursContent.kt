package com.bellfamily.bastischool.learning.colours

import com.bellfamily.bastischool.learning.content.BundledContentRepository
import com.bellfamily.bastischool.learning.content.ContentRepository
import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.session.*
import java.util.Collections
import java.util.Random

enum class ColourShape { CIRCLE, SQUARE, TRIANGLE, RECTANGLE }
data class ColourSample(val id: ContentId, val colour: ContentId, val shape: ColourShape)

/** Visual correspondence: the target hue is shown, then matched across a different shape. */
object MatchColoursContent {
    const val REVISION = 1
    val activity = ActivityId("activity.colours.match")
    val skill = SkillId("skill.colours.matching")
    val context = LearningContextId("context.colours.different_shapes")
    val shapes = ColourShape.entries
    val samples: List<ColourSample> = ColoursContent.order.flatMapIndexed { colorIndex, color ->
        shapes.mapIndexed { shapeIndex, shape ->
            ColourSample(ContentId("sample.colours.${color.value.substringAfterLast('.')}.${shape.name.lowercase()}"), color, shape)
        }
    }
    private val shared = ColoursContent.repository.all()
    val repository: ContentRepository = BundledContentRepository(
        ContentVersion(1, 1),
        shared + samples.mapIndexed { index, sample ->
            SemanticObjectDefinition(sample.id, ContentText.plain("Choice ${index + 1}", "Auswahl ${index + 1}"))
        }, emptyList()
    )
    val version get() = repository.version
    val completion = ContentText.plain("You matched the colours! Well done!", "Du hast die Farben zugeordnet! Sehr gut!")
    fun sample(id: ContentId) = samples.single { it.id == id }

    fun question(targetColour: ContentId, targetShape: ColourShape, choices: List<ContentId>): ChoiceQuestion {
        require(targetColour in ColoursContent.order && choices.size == 4 && choices.toSet().size == 4)
        val sampleChoices = choices.map(::sample)
        require(sampleChoices.map { it.colour }.toSet() == ColoursContent.order.toSet())
        require(sampleChoices.single { it.colour == targetColour }.shape != targetShape)
        return ChoiceQuestion(
            TaskDefinitionId("task.colours.match.${targetColour.value.substringAfterLast('.')}.${targetShape.name.lowercase()}"),
            skill, context, 1,
            ContentText.plain("Find the same colour in a different shape.", "Finde dieselbe Farbe in einer anderen Form."),
            choices, sampleChoices.single { it.colour == targetColour }.id,
            ContentText.plain("The colours match!", "Die Farben passen zusammen!"),
            ContentText.plain("Have another look. Compare the colours.", "Schau noch einmal. Vergleiche die Farben."),
            ContentText.plain("Look at the colour, not the shape.", "Achte auf die Farbe, nicht auf die Form.")
        )
    }

    fun generate(id: SessionId, round: RoundLength, seed: Long): GenerationResult = try {
        val random = Random(seed)
        val targets = mutableListOf<ContentId>()
        while (targets.size < round.count) {
            val cycle = ColoursContent.order.shuffled(random)
            targets += cycle
        }
        val tasks = targets.take(round.count).mapIndexed { i, color ->
            val targetShape = shapes[(i + (seed.toInt().ushr(2) % shapes.size)) % shapes.size]
            val availableShapes = shapes.filter { it != targetShape }.shuffled(random)
            val samplesByColour = ColoursContent.order.mapIndexed { colorIndex, candidate ->
                val shape = availableShapes[(colorIndex + i) % availableShapes.size]
                samples.single { it.colour == candidate && it.shape == shape }
            }
            val choices = samplesByColour.map { it.id }.shuffled(random)
            ChoiceTask(TaskInstanceId(id, i + 1), question(color, targetShape, choices))
        }
        val plan = SessionPlan(id, activity, REVISION, version, SessionPolicy(round), tasks, completion)
        plan.validate(repository)
        GenerationResult.Generated(plan)
    } catch (error: IllegalArgumentException) {
        GenerationResult.Rejected(error.message ?: "Invalid matching content")
    }

    fun targetColour(question: ChoiceQuestion): ContentId = samples.single { it.id == question.correct }.colour
    fun targetShape(question: ChoiceQuestion): ColourShape = ColourShape.valueOf(question.definition.value.substringAfterLast('.').uppercase())

    fun validate(state: SessionState) {
        require(state.plan.activity == activity && state.plan.activityRevision == REVISION && state.plan.contentVersion == version)
        require(state.plan.policy.wrongAnswer == WrongAnswerPolicy.RETRY)
        state.plan.tasks.forEach { task ->
            val target = targetColour(task.question)
            val shape = targetShape(task.question)
            require(task.question.choices.size == 4)
            require(task.question.choices.map(::sample).map { it.colour }.toSet() == ColoursContent.order.toSet())
            require(sample(task.question.correct).colour == target)
            require(sample(task.question.correct).shape != shape)
            require(task.question.skill == skill && task.question.context == context)
        }
        require(state.plan.tasks.map { targetColour(it.question) }.toSet() == ColoursContent.order.toSet())
    }
}
