package com.bellfamily.bastischool.learning.prepositions

import com.bellfamily.bastischool.learning.content.*
import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.session.*
import java.util.Random

enum class ReferenceObject { ROCK, TABLE, BOX, CLOUD, CAVE }
enum class PositionAnimal(val key: String, val subject: LocalizedText) {
    SNAKE("snake", LocalizedText("the snake", "die Schlange")),
    DINOSAUR("dinosaur", LocalizedText("the dinosaur", "der Dinosaurier")),
    DRAGON("dragon", LocalizedText("the dragon", "der Drache")),
    CROCODILE("crocodile", LocalizedText("the crocodile", "das Krokodil")),
    BIRD("bird", LocalizedText("the bird", "der Vogel")),
    BEE("bee", LocalizedText("the bee", "die Biene")),
    BUTTERFLY("butterfly", LocalizedText("the butterfly", "der Schmetterling")),
    FISH("fish", LocalizedText("the fish", "der Fisch")),
    TURTLE("turtle", LocalizedText("the turtle", "die Schildkröte")),
    OCTOPUS("octopus", LocalizedText("the octopus", "der Oktopus")),
    SEAHORSE("seahorse", LocalizedText("the seahorse", "das Seepferdchen"))
}
enum class PositionRelation(val key: String, val label: LocalizedText, val reference: ReferenceObject,
                            val count: Int, val phrase: LocalizedText) {
    ON("on", LocalizedText("on", "auf"), ReferenceObject.ROCK, 1, LocalizedText("on the rock", "auf dem Stein")),
    UNDER("under", LocalizedText("under", "unter"), ReferenceObject.TABLE, 1, LocalizedText("under the table", "unter dem Tisch")),
    BEHIND("behind", LocalizedText("behind", "hinter"), ReferenceObject.ROCK, 1, LocalizedText("behind the rock", "hinter dem Stein")),
    NEXT_TO("next_to", LocalizedText("next to", "neben"), ReferenceObject.ROCK, 1, LocalizedText("next to the rock", "neben dem Stein")),
    IN("in", LocalizedText("in", "in"), ReferenceObject.BOX, 1, LocalizedText("in the box", "in der Kiste")),
    BETWEEN("between", LocalizedText("between", "zwischen"), ReferenceObject.ROCK, 2, LocalizedText("between the two rocks", "zwischen den beiden Steinen")),
    ABOVE("above", LocalizedText("above", "über"), ReferenceObject.CLOUD, 1, LocalizedText("above the cloud", "über der Wolke")),
    BELOW("below", LocalizedText("below", "unterhalb"), ReferenceObject.CLOUD, 1, LocalizedText("below the cloud", "unterhalb der Wolke")),
    INSIDE("inside", LocalizedText("inside", "drinnen"), ReferenceObject.CAVE, 1, LocalizedText("inside the cave", "in der Höhle")),
    OUTSIDE("outside", LocalizedText("outside", "draußen"), ReferenceObject.CAVE, 1, LocalizedText("outside the cave", "außerhalb der Höhle")),
    IN_FRONT_OF("in_front_of", LocalizedText("in front of", "vor"), ReferenceObject.ROCK, 1, LocalizedText("in front of the rock", "vor dem Stein")),
    NEAR("near", LocalizedText("near", "in der Nähe"), ReferenceObject.ROCK, 1, LocalizedText("near the rock", "in der Nähe des Steins")),
    FAR_FROM("far_from", LocalizedText("far from", "weit weg"), ReferenceObject.ROCK, 1, LocalizedText("far from the rock", "weit weg vom Stein"));
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
    const val REVISION = 2
    val version = ContentVersion(1, 2)
    val scenes: List<PositionScene> = java.util.Collections.unmodifiableList(PrepositionsArtwork.scenes)
    // Frozen v1 membership; never expand an accepted old round using the v2 catalogue.
    internal val legacyRelations = listOf(PositionRelation.ON, PositionRelation.UNDER, PositionRelation.BEHIND,
        PositionRelation.NEXT_TO, PositionRelation.IN, PositionRelation.BETWEEN)
    private val legacyAnimals = listOf(PositionAnimal.SNAKE, PositionAnimal.DINOSAUR, PositionAnimal.DRAGON, PositionAnimal.CROCODILE)
    internal val legacyRepository: ContentRepository = BundledContentRepository(ContentVersion(1, 1),
        legacyRelations.map { PrepositionDefinition(it.id, ContentText(it.label, it.label)) }, emptyList())
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
            val choices = choices(scene.relation, random)
            ChoiceTask(TaskInstanceId(id, index + 1), question(scene, choices, content))
        }
        val plan = SessionPlan(id, activity, REVISION, content.version, SessionPolicy(round), tasks, completion)
        plan.validate(content)
        GenerationResult.Generated(plan)
    } catch (error: IllegalArgumentException) { GenerationResult.Rejected(error.message ?: "Invalid Prepositions content") }

    /** Avoid competing vocabulary variants/overlapping cues, including among distractors. */
    internal fun compatible(a: PositionRelation, b: PositionRelation): Boolean = a != b &&
        setOf(a, b) !in listOf(setOf(PositionRelation.IN, PositionRelation.INSIDE),
            setOf(PositionRelation.UNDER, PositionRelation.BELOW), setOf(PositionRelation.ON, PositionRelation.ABOVE),
            setOf(PositionRelation.NEXT_TO, PositionRelation.NEAR))

    private fun choices(correct: PositionRelation, random: Random): List<ContentId> {
        val chosen = mutableListOf(correct)
        // Prefer a concrete spatial contrast before the other seeded distractors.
        val opposite = when(correct) {
            PositionRelation.ON -> PositionRelation.UNDER
            PositionRelation.UNDER -> PositionRelation.ON
            PositionRelation.ABOVE -> PositionRelation.BELOW
            PositionRelation.BELOW -> PositionRelation.ABOVE
            PositionRelation.BEHIND -> PositionRelation.IN_FRONT_OF
            PositionRelation.IN_FRONT_OF -> PositionRelation.BEHIND
            PositionRelation.IN, PositionRelation.INSIDE -> PositionRelation.OUTSIDE
            PositionRelation.OUTSIDE -> PositionRelation.INSIDE
            PositionRelation.NEAR, PositionRelation.NEXT_TO -> PositionRelation.FAR_FROM
            PositionRelation.FAR_FROM -> PositionRelation.NEAR
            PositionRelation.BETWEEN -> PositionRelation.NEXT_TO
        }
        chosen += opposite
        for (candidate in PositionRelation.entries.shuffled(random)) {
            if (chosen.size == 4) break
            if (chosen.all { compatible(it, candidate) }) chosen += candidate
        }
        require(chosen.size == 4)
        return chosen.shuffled(random).map { it.id }
    }

    fun question(scene: PositionScene, choices: List<ContentId>, content: ContentRepository = repository): ChoiceQuestion {
        require(scene in scenes)
        require(choices.size == 4 && choices.toSet().size == 4 && scene.relation.id in choices)
        choices.forEach { require(content.find(it) is PrepositionDefinition) }
        val names = choices.map { content.find(it)!!.text }
        require(names.map { it.display.en }.toSet().size == 4 && names.map { it.display.de }.toSet().size == 4)
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
        val legacy = state.plan.activityRevision == 1 && state.plan.contentVersion == legacyRepository.version
        require(state.plan.activity == activity && (legacy ||
            state.plan.activityRevision == REVISION && state.plan.contentVersion == version))
        val content = if (legacy) legacyRepository else repository
        require(state.plan.completionText == completion && state.plan.policy.wrongAnswer == WrongAnswerPolicy.RETRY)
        state.plan.tasks.forEach { task ->
            val scene = scene(task)
            if (legacy) require(scene.animal in legacyAnimals && scene.relation in legacyRelations)
            val expected = question(scene, task.question.choices, content)
            val actual = task.question
            require(actual.difficulty == expected.difficulty && actual.correct == expected.correct && actual.skill == expected.skill && actual.context == expected.context &&
                actual.instruction == expected.instruction && actual.correctFeedback == expected.correctFeedback &&
                actual.wrongFeedback == expected.wrongFeedback && actual.hint == expected.hint)
        }
    }
    /** Exact snapshots only; never regenerate or rewrite a saved round's version. */
    fun restore(bytes: ByteArray): SessionRestoreResult {
        val current = SessionCheckpoint.restore(bytes, activity, REVISION, repository)
        val result = if (current == SessionRestoreResult.Rejected(CheckpointRejection.INCOMPATIBLE))
            SessionCheckpoint.restore(bytes, activity, 1, legacyRepository) else current
        if (result is SessionRestoreResult.Restored) {
            try { validate(result.state) }
            catch (_: IllegalArgumentException) { return SessionRestoreResult.Rejected(CheckpointRejection.MALFORMED) }
            catch (_: NoSuchElementException) { return SessionRestoreResult.Rejected(CheckpointRejection.MALFORMED) }
        }
        return result
    }
    private fun <T> List<T>.shuffled(random: Random): List<T> = toMutableList().apply { java.util.Collections.shuffle(this, random) }
}
