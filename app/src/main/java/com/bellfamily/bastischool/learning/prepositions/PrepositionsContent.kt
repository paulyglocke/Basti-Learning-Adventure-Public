package com.bellfamily.bastischool.learning.prepositions

import com.bellfamily.bastischool.learning.content.*
import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.session.*
import java.util.Random

enum class ReferenceObject { ROCK, TABLE, BOX }
enum class PositionAnimal(val key: String, val subject: LocalizedText) {
    SNAKE("snake", LocalizedText("the snake", "die Schlange")),
    DINOSAUR("dinosaur", LocalizedText("the dinosaur", "der Dinosaurier")),
    DRAGON("dragon", LocalizedText("the dragon", "der Drache")),
    CROCODILE("crocodile", LocalizedText("the crocodile", "das Krokodil"))
}
enum class PositionRelation(val key: String, val label: LocalizedText, val reference: ReferenceObject,
                            val count: Int, val phrase: LocalizedText) {
    ON("on", LocalizedText("on", "auf"), ReferenceObject.ROCK, 1, LocalizedText("on the rock", "auf dem Stein")),
    UNDER("under", LocalizedText("under", "unter"), ReferenceObject.TABLE, 1, LocalizedText("under the table", "unter dem Tisch")),
    BEHIND("behind", LocalizedText("behind", "hinter"), ReferenceObject.ROCK, 1, LocalizedText("behind the rock", "hinter dem Stein")),
    NEXT_TO("next_to", LocalizedText("next to", "neben"), ReferenceObject.ROCK, 1, LocalizedText("next to the rock", "neben dem Stein")),
    IN("in", LocalizedText("in", "in"), ReferenceObject.BOX, 1, LocalizedText("in the box", "in der Kiste")),
    BETWEEN("between", LocalizedText("between", "zwischen"), ReferenceObject.ROCK, 2, LocalizedText("between the two rocks", "zwischen den beiden Steinen"));
    val id get() = ContentId("position.$key")
}
data class PositionScene(val animal: PositionAnimal, val relation: PositionRelation) {
    val id get() = ContentId("scene.prepositions.${animal.key}.${relation.key}")
    val taskId get() = TaskDefinitionId("task.prepositions.${animal.key}.${relation.key}")
    val context get() = LearningContextId("context.prepositions.${animal.key}.${relation.reference.name.lowercase()}")
    val description get() = LocalizedText(
        "${animal.subject.en.replaceFirstChar { it.uppercase() }} is ${relation.phrase.en}.",
        "${animal.subject.de.replaceFirstChar { it.uppercase() }} ist ${relation.phrase.de}.")
}

object PrepositionsContent {
    val activity = ActivityId("activity.prepositions")
    const val REVISION = 1
    val version = ContentVersion(1, 1)
    val scenes: List<PositionScene> = java.util.Collections.unmodifiableList(PositionAnimal.entries.flatMap { animal -> PositionRelation.entries.map { PositionScene(animal, it) } })
    val completion = ContentText.plain("Adventure complete! Well done!", "Abenteuer geschafft! Sehr gut!")
    val tutorial = ContentText.plain(
        "I will show you where an animal is. Listen to the choices and tap the right place.",
        "Ich zeige dir, wo ein Tier ist. Höre die Möglichkeiten und tippe auf den richtigen Ort.")
    val repository: ContentRepository = BundledContentRepository(version,
        PositionRelation.entries.map { PrepositionDefinition(it.id, ContentText(it.label, it.label)) }, emptyList())

    fun scene(task: ChoiceTask): PositionScene = scenes.single { it.taskId == task.question.definition }

    fun generate(id: SessionId, round: RoundLength, seed: Long, content: ContentRepository = repository): GenerationResult = try {
        val random = Random(seed)
        val pool = scenes.shuffled(random)
        val tasks = pool.take(round.count).mapIndexed { index, scene ->
            val choices = (listOf(scene.relation) + PositionRelation.entries.filter { it != scene.relation }
                .shuffled(random).take(3)).shuffled(random).map { it.id }
            ChoiceTask(TaskInstanceId(id, index + 1), question(scene, choices, content))
        }
        val plan = SessionPlan(id, activity, REVISION, content.version, SessionPolicy(round), tasks, completion)
        plan.validate(content)
        GenerationResult.Generated(plan)
    } catch (error: IllegalArgumentException) { GenerationResult.Rejected(error.message ?: "Invalid Prepositions content") }

    fun question(scene: PositionScene, choices: List<ContentId>, content: ContentRepository = repository): ChoiceQuestion {
        require(choices.size == 4 && choices.toSet().size == 4 && scene.relation.id in choices)
        choices.forEach { require(content.find(it) is PrepositionDefinition) }
        val names = choices.map { content.find(it)!!.text }
        val instruction = ContentText.plain(
            "Where is ${scene.animal.subject.en}? Look at the picture. Choose: ${names.joinToString(", ") { it.speech.en }}.",
            "Wo ist ${scene.animal.subject.de}? Schau dir das Bild an. Wähle: ${names.joinToString(", ") { it.speech.de }}.")
        return ChoiceQuestion(scene.taskId, SkillId("skill.spatial.${scene.relation.key}"), scene.context, 1,
            instruction, choices, scene.relation.id,
            ContentText.plain("Great! ${scene.description.en}", "Super! ${scene.description.de}"),
            ContentText.plain("Try again!", "Nochmal versuchen!"), ContentText(scene.relation.phrase, scene.relation.phrase))
    }

    /** A checkpoint must still describe the authored scene, not just valid-looking IDs. */
    fun validate(state: SessionState) {
        require(state.plan.activity == activity && state.plan.activityRevision == REVISION && state.plan.contentVersion == version)
        require(state.plan.completionText == completion && state.plan.policy.wrongAnswer == WrongAnswerPolicy.RETRY)
        state.plan.tasks.forEach { task ->
            val expected = question(scene(task), task.question.choices)
            val actual = task.question
            require(actual.difficulty == expected.difficulty && actual.correct == expected.correct && actual.skill == expected.skill && actual.context == expected.context &&
                actual.instruction == expected.instruction && actual.correctFeedback == expected.correctFeedback &&
                actual.wrongFeedback == expected.wrongFeedback && actual.hint == expected.hint)
        }
    }
    private fun <T> List<T>.shuffled(random: Random): List<T> = toMutableList().apply { java.util.Collections.shuffle(this, random) }
}
