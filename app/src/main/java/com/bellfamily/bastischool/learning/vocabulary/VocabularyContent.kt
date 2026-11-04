package com.bellfamily.bastischool.learning.vocabulary

import com.bellfamily.bastischool.learning.content.*
import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.session.*
import com.bellfamily.bastischool.learning.progress.*

/** Small reviewed animal slice. No implied reading mastery or cross-context generalisation. */
class VocabularyPack(items: List<VocabularyDefinition>, categories: List<VocabularyCategory>) {
    val items = java.util.Collections.unmodifiableList(items.sortedBy { it.id.value })
    val repository: ContentRepository
    init {
        require(items.size >= 4 && items.map { it.id }.distinct().size == items.size)
        require(categories.map { it.id }.distinct().size == categories.size)
        require(items.all { item -> categories.any { it.id == item.category } })
        repository = BundledContentRepository(ContentVersion(1,1), categories + this.items, emptyList())
    }
    fun item(id: ContentId) = repository.find(id) as? VocabularyDefinition
        ?: throw IllegalArgumentException("Unknown vocabulary item")
}

enum class VocabularyPhase { EXPLORE, FIND, NAME }

object VocabularyContent {
    const val REVISION = 1
    private val animals = ContentId("category.animals")
    val classroom = ContentId("category.classroom")
    private fun word(visual: TemporaryAnimalVisual, en: String, de: String, exampleEn: String, exampleDe: String,
                     findEn: String, findDe: String) = VocabularyDefinition(visual.animal, ContentText.plain(en,de),
        animals, visual, ContentText.plain(exampleEn,exampleDe), ContentText.plain(findEn,findDe))
    val pack = VocabularyPack(listOf(
        word(TemporaryAnimalVisual.DINOSAUR,"Dinosaur","Dinosaurier","The dinosaur stomps.","Der Dinosaurier stampft.","Find the dinosaur.","Finde den Dinosaurier."),
        word(TemporaryAnimalVisual.SNAKE,"Snake","Schlange","The snake slithers.","Die Schlange schlängelt sich.","Find the snake.","Finde die Schlange."),
        word(TemporaryAnimalVisual.WHALE,"Whale","Wal","The whale swims.","Der Wal schwimmt.","Find the whale.","Finde den Wal."),
        word(TemporaryAnimalVisual.HORSE,"Horse","Pferd","The horse gallops.","Das Pferd galoppiert.","Find the horse.","Finde das Pferd."),
        word(TemporaryAnimalVisual.CROCODILE,"Crocodile","Krokodil","The crocodile swims.","Das Krokodil schwimmt.","Find the crocodile.","Finde das Krokodil."),
        word(TemporaryAnimalVisual.FISH,"Fish","Fisch","The fish swims.","Der Fisch schwimmt.","Find the fish.","Finde den Fisch.")
    ), listOf(VocabularyCategory(animals,ContentText.plain("Animals","Tiere"))))
    val classroomPack = VocabularyPack(listOf(
        classroomWord("book","Book","Buch","The book is on the desk.","Das Buch liegt auf dem Tisch.","Find the book.","Finde das Buch."),
        classroomWord("pencil","Pencil","Bleistift","I write with a pencil.","Ich schreibe mit einem Bleistift.","Find the pencil.","Finde den Bleistift."),
        classroomWord("paper","Paper","Papier","The paper is white.","Das Papier ist weiß.","Find the paper.","Finde das Papier."),
        classroomWord("bag","Backpack","Rucksack","My backpack is blue.","Mein Rucksack ist blau.","Find the backpack.","Finde den Rucksack."),
        classroomWord("desk","Desk","Schreibtisch","The desk is in the classroom.","Der Schreibtisch steht im Klassenraum.","Find the desk.","Finde den Schreibtisch."),
        classroomWord("shelf","Bookshelf","Bücherregal","The books are on the shelf.","Die Bücher stehen im Regal.","Find the bookshelf.","Finde das Bücherregal."),
        classroomWord("teacher","Teacher","Lehrkraft","The teacher helps the class.","Die Lehrkraft hilft der Klasse.","Find the teacher.","Finde die Lehrkraft."),
        classroomWord("cup","Pencil cup","Stiftebecher","The pencils are in the cup.","Die Stifte sind im Becher.","Find the pencil cup.","Finde den Stiftebecher.")
    ), listOf(VocabularyCategory(classroom,ContentText.plain("Classroom things","Klassenzimmer"))))
    private fun classroomWord(key:String,en:String,de:String,exampleEn:String,exampleDe:String,findEn:String,findDe:String) =
        VocabularyDefinition(ContentId("school.$key"),ContentText.plain(en,de),classroom,null,
            ContentText.plain(exampleEn,exampleDe),ContentText.plain(findEn,findDe),
            "school_actions/board/${if(key in setOf("book","pencil","paper","bag","cup")) "object_$key" else "place_$key"}.png")
    val repository get() = pack.repository
    val items get() = pack.items
    fun items(category:ContentId) = when(category) {animals -> items; classroom -> classroomPack.items; else -> throw IllegalArgumentException("Unknown vocabulary category")}
    fun repository(category:ContentId) = when(category) {animals -> repository; classroom -> classroomPack.repository; else -> throw IllegalArgumentException("Unknown vocabulary category")}
    fun item(id: ContentId) = (items + classroomPack.items).firstOrNull {it.id==id} ?: throw IllegalArgumentException("Unknown vocabulary item")
    val completion = ContentText.plain("Adventure complete! Well done!","Abenteuer geschafft! Sehr gut!")
    fun activity(phase: VocabularyPhase): ActivityId {
        require(phase != VocabularyPhase.EXPLORE)
        return ActivityId("activity.vocabulary.${phase.name.lowercase()}")
    }
    fun activity(phase:VocabularyPhase,category:ContentId):ActivityId {
        require(phase != VocabularyPhase.EXPLORE)
        return if(category==animals) activity(phase) else ActivityId("activity.vocabulary.classroom.${phase.name.lowercase()}").also { require(category==classroom) }
    }
    fun question(phase: VocabularyPhase, correct: ContentId, choices: List<ContentId>): ChoiceQuestion {
        activity(phase)
        val target = item(correct)
        require(choices.size == 4 && choices.distinct().size == 4 && correct in choices)
        choices.forEach(::item)
        val name = correct.value.substringAfter('.')
        val mode = if(phase == VocabularyPhase.FIND) "word_to_picture" else "picture_to_word"
        return ChoiceQuestion(TaskDefinitionId("task.vocabulary.$mode.$name"), SkillId("skill.vocabulary.$mode.$name"),
            LearningContextId("context.vocabulary.temporary_animal_visual"), 1,
            if(phase == VocabularyPhase.FIND) target.findPrompt else ContentText.plain("What is this animal called?","Wie heißt dieses Tier?"),
            choices, correct, ContentText.plain("Yes! ${target.text.speech.en}.","Ja! ${target.text.speech.de}."),
            ContentText.plain("Have another look. You can listen or ask for help.","Schau noch einmal. Du kannst zuhören oder Hilfe holen."), target.text)
    }
    fun question(phase:VocabularyPhase,category:ContentId,correct:ContentId,choices:List<ContentId>,variant:Int=0):ChoiceQuestion {
        if(category==animals)return question(phase,correct,choices)
        activity(phase,category)
        val target=classroomPack.item(correct)
        require(choices.size==4 && choices.distinct().size==4 && correct in choices)
        choices.forEach(classroomPack::item)
        val name=correct.value.substringAfter('.')
        val mode=if(phase==VocabularyPhase.FIND) "word_to_picture" else "picture_to_word"
        return ChoiceQuestion(TaskDefinitionId("task.vocabulary.classroom.$mode.$name${if(variant==0) "" else ".v$variant"}"),SkillId("skill.vocabulary.classroom.$mode.$name"),
            LearningContextId("context.vocabulary.classroom_visual"),1,
            if(phase==VocabularyPhase.FIND) target.findPrompt else ContentText.plain("What is this called?","Wie heißt das?"),choices,correct,
            ContentText.plain("Yes! ${target.text.speech.en}.","Ja! ${target.text.speech.de}."),
            ContentText.plain("Have another look. You can listen or ask for help.","Schau noch einmal. Du kannst zuhören oder Hilfe holen."),target.text)
    }
    fun generate(phase: VocabularyPhase, id: SessionId, round: RoundLength, seed: Long): GenerationResult {
        val random = java.util.Random(seed)
        val candidates = items.map { target ->
            val distractors = items.map { it.id }.filter { it != target.id }.toMutableList()
            java.util.Collections.shuffle(distractors,random)
            question(phase,target.id,listOf(target.id) + distractors.take(3))
        }
        return CandidateTaskGenerator(candidates).generate(GenerationRequest(id,activity(phase),REVISION,
            SessionPolicy(round),seed,completion),repository)
    }
    fun generate(phase:VocabularyPhase,category:ContentId,id:SessionId,round:RoundLength,seed:Long):GenerationResult {
        if(category==animals)return generate(phase,id,round,seed)
        val source=java.util.Random(seed)
        val candidates=classroomPack.items.flatMap { target ->
            val distractors=classroomPack.items.map {it.id}.filter {it!=target.id}.toMutableList()
            java.util.Collections.shuffle(distractors,source)
            (0..2).map { variant ->
                val options=(0..2).map { distractors[(variant*2+it)%distractors.size] }
                question(phase,category,target.id,listOf(target.id)+options,variant)
            }
        }
        return CandidateTaskGenerator(candidates).generate(GenerationRequest(id,activity(phase,category),REVISION,
            SessionPolicy(round),seed,completion),repository(category))
    }
    fun validate(phase: VocabularyPhase, state: SessionState) {
        require(state.plan.activity == activity(phase) && state.plan.activityRevision == REVISION && state.plan.contentVersion == repository.version)
        require(state.plan.completionText == completion && state.plan.policy.wrongAnswer == WrongAnswerPolicy.RETRY)
        state.plan.tasks.forEach { task ->
            val actual=task.question; val expected=question(phase,actual.correct,actual.choices)
            require(actual.definition==expected.definition && actual.skill==expected.skill && actual.context==expected.context &&
                actual.difficulty==expected.difficulty && actual.instruction==expected.instruction && actual.correctFeedback==expected.correctFeedback &&
                actual.wrongFeedback==expected.wrongFeedback && actual.hint==expected.hint)
        }
    }
    fun host(phase: VocabularyPhase,journal: ProgressStorage,progress: ProgressRepository) = DurableSessionHost(journal,progress,
        activity(phase),REVISION,repository,{id,round,seed -> generate(phase,id,round,seed)},{validate(phase,it)})
    fun host(phase:VocabularyPhase,category:ContentId,journal:ProgressStorage,progress:ProgressRepository) = DurableSessionHost(journal,progress,
        activity(phase,category),REVISION,repository(category),{id,round,seed -> generate(phase,category,id,round,seed)},{state -> validate(phase,category,state)})
    fun validate(phase:VocabularyPhase,category:ContentId,state:SessionState) {
        require(state.plan.activity==activity(phase,category) && state.plan.activityRevision==REVISION && state.plan.contentVersion==repository(category).version)
        require(state.plan.completionText==completion && state.plan.policy.wrongAnswer==WrongAnswerPolicy.RETRY)
        state.plan.tasks.forEach { task ->
            val actual=task.question
            val variant=actual.definition.value.substringAfterLast(".v").toIntOrNull() ?: 0
            val expected=question(phase,category,actual.correct,actual.choices,variant)
            require(actual.definition==expected.definition && actual.skill==expected.skill && actual.context==expected.context &&
                actual.difficulty==expected.difficulty && actual.instruction==expected.instruction && actual.correctFeedback==expected.correctFeedback &&
                actual.wrongFeedback==expected.wrongFeedback && actual.hint==expected.hint)
        }
    }
}

data class VocabularySelection(val selected: ContentId = ContentId("animal.dinosaur"), val phase: VocabularyPhase = VocabularyPhase.EXPLORE,
    val category:ContentId=ContentId("category.animals")) {
    init { require(VocabularyContent.items(category).any { it.id == selected }) }
    val item get() = VocabularyContent.item(selected)
}
