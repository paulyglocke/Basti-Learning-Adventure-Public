package com.bellfamily.bastischool.learning.letters

import com.bellfamily.bastischool.learning.content.BundledContentRepository
import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.session.*

data class InitialLetterAnimal(
    val id: ContentId,
    val name: ContentText,
    val initialEnglish: String,
    val initialGerman: String,
    val image: String
) {
    init {
        require(id.value.startsWith("animal."))
        require(initialEnglish.matches(Regex("[A-Z]")) && initialGerman.matches(Regex("[A-Z]")))
        require(image.startsWith("Animals/canonical/") && image.endsWith(".png"))
    }
    fun initial(language: ContentLanguage) = if (language == ContentLanguage.GERMAN) initialGerman else initialEnglish
}

/** Authored written-letter pairs. This is grapheme matching, not a phonics lesson. */
object LettersContent : ChoiceSessionCurriculum {
    const val REVISION = 2
    override val activity = ActivityId("activity.letters.find_animal")
    override val revision = REVISION
    private val skill = SkillId("skill.letters.find_written_initial")
    private val context = LearningContextId("context.letters.canonical_animals")
    private val definitions = listOf(
        InitialLetterAnimal(ContentId("animal.dinosaur"), ContentText.plain("Dinosaur", "Dinosaurier"), "D", "D", "Animals/canonical/dinosaur.png"),
        InitialLetterAnimal(ContentId("animal.snake"), ContentText.plain("Snake", "Schlange"), "S", "S", "Animals/canonical/snake.png"),
        InitialLetterAnimal(ContentId("animal.whale"), ContentText.plain("Whale", "Wal"), "W", "W", "Animals/canonical/whale.png"),
        InitialLetterAnimal(ContentId("animal.horse"), ContentText.plain("Horse", "Pferd"), "H", "P", "Animals/canonical/horse.png"),
        InitialLetterAnimal(ContentId("animal.crocodile"), ContentText.plain("Crocodile", "Krokodil"), "C", "K", "Animals/canonical/crocodile.png"),
        InitialLetterAnimal(ContentId("animal.fish"), ContentText.plain("Fish", "Fisch"), "F", "F", "Animals/canonical/fish.png"),
        InitialLetterAnimal(ContentId("animal.dog"), ContentText.plain("Dog", "Hund"), "D", "H", "Animals/canonical/dog.png"),
        InitialLetterAnimal(ContentId("animal.cat"), ContentText.plain("Cat", "Katze"), "C", "K", "Animals/canonical/cat.png"),
        InitialLetterAnimal(ContentId("animal.rabbit"), ContentText.plain("Rabbit", "Kaninchen"), "R", "K", "Animals/canonical/rabbit.png"),
        InitialLetterAnimal(ContentId("animal.mouse"), ContentText.plain("Mouse", "Maus"), "M", "M", "Animals/canonical/mouse.png"),
        InitialLetterAnimal(ContentId("animal.frog"), ContentText.plain("Frog", "Frosch"), "F", "F", "Animals/canonical/frog.png"),
        InitialLetterAnimal(ContentId("animal.turtle"), ContentText.plain("Turtle", "Schildkröte"), "T", "S", "Animals/canonical/turtle.png"),
        InitialLetterAnimal(ContentId("animal.bear"), ContentText.plain("Bear", "Bär"), "B", "B", "Animals/canonical/bear.png"),
        InitialLetterAnimal(ContentId("animal.duck"), ContentText.plain("Duck", "Ente"), "D", "E", "Animals/canonical/duck.png"),
        InitialLetterAnimal(ContentId("animal.dolphin"), ContentText.plain("Dolphin", "Delfin"), "D", "D", "Animals/canonical/dolphin.png"),
        InitialLetterAnimal(ContentId("animal.eagle"), ContentText.plain("Eagle", "Adler"), "E", "A", "Animals/canonical/eagle.png"),
        InitialLetterAnimal(ContentId("animal.alligator"), ContentText.plain("Alligator", "Alligator"), "A", "A", "Animals/canonical/alligator.png"),
        InitialLetterAnimal(ContentId("animal.mammoth"), ContentText.plain("Mammoth", "Mammut"), "M", "M", "Animals/canonical/mammoth.png"),
        InitialLetterAnimal(ContentId("animal.mole"), ContentText.plain("Mole", "Maulwurf"), "M", "M", "Animals/canonical/mole.png")
    )
    val animals: List<InitialLetterAnimal> = definitions.toList()
    private val byId = animals.associateBy { it.id }
    override val repository = BundledContentRepository(ContentVersion(1, 2), animals.map { SemanticObjectDefinition(it.id, it.name) }, emptyList())
    override val completion = ContentText.plain("Adventure complete! You found the first letters.", "Abenteuer geschafft! Du hast die Anfangsbuchstaben gefunden.")

    init {
        require(animals.size >= 10 && byId.size == animals.size)
        for (language in ContentLanguage.entries) {
            require(animals.map { it.initial(language) }.toSet().size >= 4)
            animals.forEach { target ->
                require(animals.count { it.id != target.id && it.initial(language) != target.initial(language) } >= 3)
            }
        }
    }

    fun animal(id: ContentId) = byId[id] ?: throw IllegalArgumentException("Unknown letter animal: ${id.value}")
    fun letter(id: ContentId, language: ContentLanguage) = animal(id).initial(language)

    fun question(target: ContentId, choices: List<ContentId>): ChoiceQuestion {
        val answer = animal(target)
        require(choices.size == 4 && choices.distinct().size == 4 && target in choices)
        choices.forEach(::animal)
        for (language in ContentLanguage.entries) {
            require(choices.map { letter(it, language) }.count { it == letter(target, language) } == 1) {
                "Each letter question must have exactly one matching animal in ${language.name}"
            }
        }
        val en = answer.initialEnglish
        val de = answer.initialGerman
        return ChoiceQuestion(
            TaskDefinitionId("task.letters.find_animal.${target.value.substringAfter('.') }"), skill, context, 1,
            ContentText.plain("Which animal starts with the letter $en?", "Welches Tier beginnt mit dem Buchstaben $de?"),
            choices, target,
            ContentText.plain("Yes! ${answer.name.display.en} starts with $en.", "Ja! ${answer.name.display.de} beginnt mit $de."),
            ContentText.plain("Look at the first letter of each animal's name.", "Schau auf den ersten Buchstaben der Tiernamen."),
            ContentText.plain("$en is the first letter in ${answer.name.display.en}.", "$de ist der erste Buchstabe in ${answer.name.display.de}.")
        )
    }

    override fun generate(id: SessionId, round: RoundLength, seed: Long): GenerationResult {
        val random = java.util.Random(seed)
        val candidates = animals.map { target ->
            val distractors = animals.filter { it.id != target.id && it.initialEnglish != target.initialEnglish && it.initialGerman != target.initialGerman }
                .map { it.id }.toMutableList().also { java.util.Collections.shuffle(it, random) }
            question(target.id, listOf(target.id) + distractors.take(3).also { java.util.Collections.shuffle(it, random) })
        }
        return CandidateTaskGenerator(candidates).generate(GenerationRequest(id, activity, revision,
            SessionPolicy(round), seed, completion), repository)
    }

    override fun validate(state: SessionState) {
        require(state.plan.activity == activity && state.plan.activityRevision == revision && state.plan.contentVersion == repository.version)
        require(state.plan.completionText == completion && state.plan.policy.wrongAnswer == WrongAnswerPolicy.RETRY)
        val targets = state.plan.tasks.map { it.question.correct }
        require(targets.distinct().size == targets.size) { "A round must not repeat an animal" }
        state.plan.tasks.forEach { task ->
            val question = task.question
            val expected = question(question.correct, question.choices)
            require(question.definition == expected.definition && question.skill == expected.skill && question.context == expected.context &&
                question.difficulty == expected.difficulty && question.instruction == expected.instruction &&
                question.correctFeedback == expected.correctFeedback && question.wrongFeedback == expected.wrongFeedback && question.hint == expected.hint)
        }
    }
}
