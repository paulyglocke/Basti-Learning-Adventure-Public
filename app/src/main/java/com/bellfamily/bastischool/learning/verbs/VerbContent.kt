package com.bellfamily.bastischool.learning.verbs

import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.content.BundledContentRepository
import com.bellfamily.bastischool.learning.session.*
import java.util.Collections
import java.util.Random

data class VerbAnimal(val id: ContentId, val key: String, val label: ContentText, val artwork: String)
data class VerbMotion(val mode: String, val duration: Int, val frames: List<Int>, val still: List<Int>,
    val asset: String, val columns: Int, val rows: Int, val subject: ContentId) {
    init {
        require(duration in 500..15000 && columns in 2..4 && rows in 2..4)
        require(frames.isNotEmpty() && still.size == 4 && (frames + still).all { it in 0 until columns*rows })
    }
}
data class VerbLesson(val id: ContentId, val label: ContentText, val explanation: ContentText, val example: ContentText,
    val animal: String, val distractors: List<String>, val motion: VerbMotion, val stages: List<LocalizedText>) {
    val sound get() = motion.mode in setOf("sound", "perchSound", "buzz", "copy", "hiss")
    fun speech(language: ContentLanguage) = "${label.speech[language]}. ${explanation.speech[language]}. ${example.speech[language]}"
}

/** One generated semantic catalogue serves Explore, the assessed quiz and retained web adapters. */
object VerbContent : ChoiceSessionCurriculum {
    val animals = VerbRecords.animals
    val lessons = VerbRecords.lessons
    val byId = lessons.associateBy { it.id.value }
    override val activity = ActivityId("activity.language.animal_actions")
    override val revision = 1
    override val completion = ContentText.plain("You explored every action in this round!", "Du hast alle Aktionen dieser Runde entdeckt!")
    val instructionsId=ContentId("instruction.animal_actions")
    val quizIntroduction=ContentText.plain("Listen to the action and choose the animal. You can hear each animal's name or open its lesson before answering.",
        "Hör dir die Aktion an und wähle das Tier. Du kannst jeden Tiernamen anhören oder dir vor der Antwort die Aktion anschauen.")
    val explorerIntroduction=ContentText.plain("Watch the action, listen to its meaning and example, then try the action or sound together.",
        "Schau dir die Aktion an, hör dir die Bedeutung und das Beispiel an und macht dann die Bewegung oder das Geräusch gemeinsam nach.")
    override val repository = BundledContentRepository(ContentVersion(1, 1), animals.values.map {
        SemanticObjectDefinition(it.id, it.label)
    } + SemanticObjectDefinition(instructionsId,quizIntroduction), emptyList())
    fun lesson(question: ChoiceQuestion) = byId.getValue(question.definition.value.replaceFirst("task.", "verb."))
    fun question(verb: VerbLesson, choices: List<ContentId>): ChoiceQuestion {
        val animal = animals.getValue(verb.animal)
        return ChoiceQuestion(TaskDefinitionId(verb.id.value.replaceFirst("verb.", "task.")),
            SkillId("skill.language.animal_actions"), LearningContextId("context.language.animal_actions"), 1,
            ContentText.plain("Which animal can ${verb.label.display.en}?", "Welches Tier kann ${verb.label.display.de}?"),
            choices, animal.id, ContentText.plain("Yes! ${verb.example.display.en}", "Ja! ${verb.example.display.de}"),
            ContentText.plain("Listen again, or explore the action before trying again.", "Hör noch einmal zu oder schau dir die Aktion vor dem nächsten Versuch an."),
            ContentText.plain("Explore the action, then come back to this question.", "Schau dir die Aktion an und komm dann zu dieser Frage zurück."))
    }
    override fun generate(id: SessionId, round: RoundLength, seed: Long): GenerationResult {
        val random = Random(seed)
        val candidates = lessons.toMutableList().also { Collections.shuffle(it, random) }.distinctBy { it.animal }.take(round.count)
        val tasks = candidates.mapIndexed { index, verb ->
            val choices = (listOf(verb.animal) + verb.distractors).map { animals.getValue(it).id }.toMutableList()
                .also { Collections.shuffle(it, random) }
            ChoiceTask(TaskInstanceId(id,index+1), question(verb, choices))
        }
        return GenerationResult.Generated(SessionPlan(id,activity,revision,repository.version,SessionPolicy(round),tasks,completion))
    }
    override fun validate(state: SessionState) {
        val plan=state.plan
        require(plan.activity==activity && plan.activityRevision==revision && plan.contentVersion==repository.version)
        require(plan.completionText==completion && plan.policy.wrongAnswer==WrongAnswerPolicy.RETRY)
        val verbs=plan.tasks.map { task ->
            val verb=lesson(task.question);val expected=question(verb, task.question.choices)
            require(task.question.choices.toSet()==(listOf(verb.animal)+verb.distractors).map { animals.getValue(it).id }.toSet())
            require(task.question.correct==expected.correct && task.question.instruction==expected.instruction &&
                task.question.skill==expected.skill && task.question.context==expected.context && task.question.difficulty==1 &&
                task.question.correctFeedback==expected.correctFeedback && task.question.wrongFeedback==expected.wrongFeedback &&
                task.question.hint==expected.hint && task.question.acceptedAnswers==setOf(expected.correct))
            verb
        }
        require(verbs.map { it.id }.distinct().size==plan.policy.round.count)
        require(verbs.map { it.animal }.distinct().size==verbs.size)
    }
}
