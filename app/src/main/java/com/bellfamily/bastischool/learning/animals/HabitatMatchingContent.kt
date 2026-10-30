package com.bellfamily.bastischool.learning.animals

import com.bellfamily.bastischool.learning.content.BundledContentRepository
import com.bellfamily.bastischool.learning.content.ContentRepository
import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.session.*
import java.util.Random

/** A reviewed seven-question starter; scene questions describe the app's lessons, not exclusive biology. */
object HabitatMatchingContent {
    const val REVISION = 1
    const val TASK_COUNT = 7
    val activity = ActivityId("activity.discover.habitat_matching")
    val skill = SkillId("skill.discover.animal_habitat_matching")
    val completion = ContentText.plain(
        "You explored where these animals live and shelter. Well done!",
        "Du hast entdeckt, wo diese Tiere leben und Schutz finden. Sehr gut!")

    val ocean = ContentId("habitat.ocean")
    val woodland = ContentId("habitat.woodland")
    val underground = ContentId("habitat.underground")
    val freshwater = ContentId("habitat.freshwater")

    data class Habitat(val id: ContentId, val label: ContentText, val artwork: String)
    val habitats = listOf(
        Habitat(ocean, ContentText.plain("Ocean", "Meer"), "SceneDescriptions/ocean_underwater/canonical/ocean_001_reef_actions.png"),
        Habitat(woodland, ContentText.plain("Woodland", "Wald"), "SceneDescriptions/woodland_forest/canonical/woodland_008_homes_choices.png"),
        Habitat(underground, ContentText.plain("Underground", "Unter der Erde"), "SceneDescriptions/underground/canonical/underground_001_cutaway.png"),
        Habitat(freshwater, ContentText.plain("Freshwater", "Süßwasser"), "SceneDescriptions/freshwater_pond/canonical/freshwater_001_pond.png")
    )

    data class Animal(val id: ContentId, val name: ContentText, val artwork: String)
    val dolphin = Animal(ContentId("animal.dolphin"), ContentText.plain("Dolphin", "Delfin"), "Animals/canonical/dolphin.png")
    val rabbit = Animal(ContentId("animal.rabbit"), ContentText.plain("Rabbit", "Kaninchen"), "Animals/canonical/rabbit.png")
    val mole = Animal(ContentId("animal.mole"), ContentText.plain("Mole", "Maulwurf"), "Animals/canonical/mole.png")
    val earthworm = Animal(ContentId("animal.earthworm"), ContentText.plain("Earthworm", "Regenwurm"), "Animals/canonical/earthworm.png")
    val frog = Animal(ContentId("animal.frog"), ContentText.plain("Frog", "Frosch"), "Animals/canonical/frog.png")
    val duck = Animal(ContentId("animal.duck"), ContentText.plain("Duck", "Ente"), "Animals/canonical/duck.png")
    val animals = listOf(dolphin, rabbit, mole, earthworm, frog, duck)

    data class ReviewedQuestion(
        val id: String,
        val animal: Animal,
        val instruction: ContentText,
        val options: List<ContentId>,
        val accepted: Set<ContentId>,
        val correctFeedback: ContentText,
        val wrongFeedback: ContentText,
        val hint: ContentText,
        val rationale: String,
        val sourceFact: String
    )
    private val thinkAgain = ContentText.plain("Not that place. Try another one.", "Dieser Ort passt nicht. Probiere einen anderen.")
    private val hintText = ContentText.plain("Think about the place we explored.", "Denk an den Ort, den wir entdeckt haben.")
    val questions: List<ReviewedQuestion> = listOf(
        ReviewedQuestion("dolphin_scene", dolphin,
            ContentText.plain("Where did we explore the dolphin?", "Wo haben wir den Delfin entdeckt?"),
            listOf(ocean, woodland), setOf(ocean),
            ContentText.plain("Yes! We explored the dolphin in the ocean.", "Ja! Wir haben den Delfin im Meer entdeckt."), thinkAgain, hintText,
            "Asks about the app's pictured marine scene; does not claim dolphins only belong to one environment.",
            "Dolphins live in the sea. / Delfine leben im Meer."),
        ReviewedQuestion("rabbit_scenes", rabbit,
            ContentText.plain("Choose a place where we met rabbits.", "Wähle einen Ort, an dem wir Kaninchen getroffen haben."),
            listOf(woodland, underground, ocean), setOf(woodland, underground),
            ContentText.plain("Yes! We explored rabbits in both places.", "Ja! Wir haben Kaninchen an beiden Orten entdeckt."), thinkAgain, hintText,
            "Question-specific accepted answers reflect both in-app scenes. It makes no exhaustive biological classification.",
            "Wild rabbits can use underground warrens for shelter. / Wildkaninchen können unterirdische Baue als Schutz nutzen."),
        ReviewedQuestion("mole_tunnels", mole,
            ContentText.plain("Where does the mole dig tunnels?", "Wo gräbt der Maulwurf seine Gänge?"),
            listOf(underground, ocean), setOf(underground),
            ContentText.plain("Yes! The mole digs tunnels underground.", "Ja! Der Maulwurf gräbt Gänge unter der Erde."), thinkAgain, hintText,
            "Tests the reviewed tunnel fact, not whether a mole has any above-ground interaction.",
            "A mole lives mostly underground and digs tunnels. / Ein Maulwurf lebt überwiegend unter der Erde und gräbt Gänge."),
        ReviewedQuestion("earthworm_scene", earthworm,
            ContentText.plain("Where did we explore the earthworm moving through soil?", "Wo haben wir den Regenwurm entdeckt, der sich durch den Boden bewegt?"),
            listOf(underground, woodland), setOf(underground),
            ContentText.plain("Yes! We explored the earthworm in the underground scene.", "Ja! Wir haben den Regenwurm in der Untergrund-Szene entdeckt."), thinkAgain, hintText,
            "Refers to the app's soil cutaway and does not claim every earthworm lives at one soil depth.",
            "Earthworms move through soil. / Regenwürmer bewegen sich durch den Boden."),
        ReviewedQuestion("frog_scene", frog,
            ContentText.plain("Where did we explore the frog?", "Wo haben wir den Frosch entdeckt?"),
            listOf(freshwater, ocean), setOf(freshwater),
            ContentText.plain("Yes! We explored the frog at a freshwater pond.", "Ja! Wir haben den Frosch an einem Süßwasserteich entdeckt."), thinkAgain, hintText,
            "Asks about the freshwater pond lesson. The source fact also mentions damp land; it does not classify frogs as water-only.",
            "Many frogs lay eggs in water and also use damp places on land. / Viele Frösche legen ihre Eier im Wasser ab und nutzen auch feuchte Plätze an Land."),
        ReviewedQuestion("duck_scene", duck,
            ContentText.plain("Where did we explore the duck?", "Wo haben wir die Ente entdeckt?"),
            listOf(freshwater, woodland), setOf(freshwater),
            ContentText.plain("Yes! We explored the duck by a freshwater pond.", "Ja! Wir haben die Ente an einem Süßwasserteich entdeckt."), thinkAgain, hintText,
            "Asks about the pictured lesson; the word many in the source fact avoids an exclusive habitat claim.",
            "Many ducks swim on ponds and lakes. / Viele Enten schwimmen auf Teichen und Seen."),
        ReviewedQuestion("rabbit_shelter", rabbit,
            ContentText.plain("Where can wild rabbits use a warren for shelter?", "Wo können Wildkaninchen einen Bau als Schutz nutzen?"),
            listOf(underground, freshwater), setOf(underground),
            ContentText.plain("Yes! Wild rabbits can shelter in underground warrens.", "Ja! Wildkaninchen können in unterirdischen Bauen Schutz finden."), thinkAgain, hintText,
            "Explicitly asks about shelter, not the rabbit's only habitat.",
            "Wild rabbits can use underground warrens for shelter. / Wildkaninchen können unterirdische Baue als Schutz nutzen.")
    )

    val repository: ContentRepository = BundledContentRepository(ContentVersion(1, 1),
        habitats.map { SemanticObjectDefinition(it.id, it.label) }, emptyList())

    fun question(item: ReviewedQuestion, optionOrder: List<ContentId> = item.options): ChoiceQuestion {
        require(optionOrder.size == item.options.size && optionOrder.toSet() == item.options.toSet())
        return ChoiceQuestion(TaskDefinitionId("task.animals.habitat.${item.id}"), skill,
            LearningContextId("context.animals.habitat.${item.id}"), 1, item.instruction, optionOrder,
            item.accepted.sortedBy { it.value }.first(), item.correctFeedback, item.wrongFeedback, item.hint, item.accepted)
    }

    fun generate(id: SessionId, round: RoundLength, seed: Long): GenerationResult = try {
        // This reviewed starter is fixed at seven items; app-wide 5/10 settings do not truncate its content.
        val random = Random(seed)
        val shuffled = questions.shuffled(random)
        val tasks = shuffled.mapIndexed { index, item ->
            ChoiceTask(TaskInstanceId(id, index + 1), question(item, item.options.shuffled(random)))
        }
        val plan = SessionPlan(id, activity, REVISION, repository.version, SessionPolicy(round), tasks, completion, TASK_COUNT)
        plan.validate(repository)
        GenerationResult.Generated(plan)
    } catch (error: IllegalArgumentException) {
        GenerationResult.Rejected(error.message ?: "Invalid habitat matching content")
    }

    fun reviewed(id: TaskDefinitionId): ReviewedQuestion = questions.single { question(it).definition == id }
    fun animal(id: ContentId): Animal? = animals.firstOrNull { it.id == id }
    fun habitat(id: ContentId): Habitat? = habitats.firstOrNull { it.id == id }

    fun validate(state: SessionState) {
        val plan = state.plan
        require(plan.activity == activity && plan.activityRevision == REVISION && plan.contentVersion == repository.version)
        require(plan.taskCount == TASK_COUNT && plan.tasks.size == TASK_COUNT && plan.policy.wrongAnswer == WrongAnswerPolicy.RETRY)
        val ids = plan.tasks.map { it.question.definition }
        require(ids.toSet() == questions.map { question(it).definition }.toSet() && ids.size == questions.size)
        plan.tasks.forEach { task ->
            val item = reviewed(task.question.definition)
            val expected = question(item)
            require(task.question.skill == skill && task.question.context == expected.context && task.question.difficulty == 1)
            require(task.question.instruction == expected.instruction && task.question.choices.toSet() == expected.choices.toSet())
            require(task.question.acceptedAnswers == expected.acceptedAnswers && task.question.correct == expected.correct)
            require(task.question.correctFeedback == expected.correctFeedback && task.question.wrongFeedback == expected.wrongFeedback)
            require(task.question.hint == expected.hint)
        }
    }

    fun host(journal: com.bellfamily.bastischool.learning.progress.ProgressStorage,
             progress: com.bellfamily.bastischool.learning.progress.ProgressRepository) = DurableSessionHost(
        journal, progress, activity, REVISION, repository,
        { id, round, seed -> generate(id, round, seed) }, ::validate)
}
