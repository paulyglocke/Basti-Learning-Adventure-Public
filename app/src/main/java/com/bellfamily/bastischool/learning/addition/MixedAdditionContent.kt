package com.bellfamily.bastischool.learning.addition

import com.bellfamily.bastischool.learning.content.BundledContentRepository
import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.session.*
import java.util.Collections
import java.util.Random

/** A balanced practice round drawing from both established addition ranges. */
object MixedAdditionContent : AdditionCurriculum {
    private val numberWords = listOf("zero", "one", "two", "three", "four", "five", "six", "seven", "eight", "nine",
        "ten", "eleven", "twelve", "thirteen", "fourteen", "fifteen", "sixteen", "seventeen", "eighteen", "nineteen", "twenty")
    override val activity = ActivityId("activity.math.add_mixed")
    override val skill = SkillId("skill.math.addition.mixed")
    override val context = LearningContextId("context.math.addition.mixed")
    override val revision = 1
    override val repository = BundledContentRepository(ContentVersion(1, 1), (1..20).map {
        SemanticObjectDefinition(numberId(it), ContentText.plain("$it", "$it"))
    }, emptyList())
    override val title = ContentText.plain("Mixed Addition", "Gemischte Plusaufgaben")
    override val prompt = ContentText.plain("How many altogether?", "Wie viele sind es zusammen?")
    override val help = ContentText.plain("Look at both groups. You can start with the larger group and count on.",
        "Schau dir beide Gruppen an. Du kannst mit der größeren Gruppe beginnen und weiterzählen.")
    override val completion = AdditionContent.completion
    override val useTenFrame = true

    private val easy = AdditionContent.catalogue()
    private val challenge = AdditionChallengeContent.catalogue()
    override fun catalogue() = easy + challenge
    override fun numberId(number: Int) = AdditionChallengeContent.numberId(number)
    override fun numberValue(id: ContentId) = AdditionChallengeContent.numberValue(id)
    override fun groupDescription(number: Int, first: Boolean, language: ContentLanguage) =
        AdditionChallengeContent.groupDescription(number, first, language)

    override fun question(fact: AdditionFact, choices: List<ContentId>): ChoiceQuestion {
        require(fact in catalogue() && choices.size == 4 && choices.toSet().size == 4)
        require(numberId(fact.total) in choices && choices.all { numberValue(it) in 2..20 })
        val key = "${numberWords[fact.left]}.${numberWords[fact.right]}.${numberWords[fact.total]}"
        return ChoiceQuestion(TaskDefinitionId("task.math.add_mixed.$key"), skill, context, if (fact in easy) 1 else 2,
            prompt, choices, numberId(fact.total), ContentText.plain("Yes, ${fact.total}!", "Ja, ${fact.total}!"),
            ContentText.plain("Try again.", "Versuch es noch einmal."), help)
    }

    override fun definition(question: ChoiceQuestion): AdditionFact {
        val parts = question.definition.value.split('.')
        require(parts.size == 6 && parts.take(3) == listOf("task", "math", "add_mixed"))
        val fact = AdditionFact(numberWords.indexOf(parts[3]), numberWords.indexOf(parts[4]))
        require(fact in catalogue() && numberWords[fact.total] == parts[5])
        return fact
    }

    override fun generate(id: SessionId, round: RoundLength, seed: Long): GenerationResult {
        val random = Random(seed)
        fun <T> shuffle(values: List<T>) = values.toMutableList().also { Collections.shuffle(it, random) }
        val challengeCount = (round.count + 1) / 2
        val easyCount = round.count - challengeCount
        val required = listOf(AdditionFact(4, 7), AdditionFact(5, 9))
        val otherChallenge = shuffle(challenge.filterNot { it in required }).take(challengeCount - required.size)
        val selected = shuffle(easy).take(easyCount) + required + otherChallenge
        val tasks = shuffle(selected).mapIndexed { index, fact ->
            val near = shuffle((2..20).toList()).sortedBy { kotlin.math.abs(it - fact.total) }.take(4)
            val choices = shuffle(near.map(::numberId))
            ChoiceTask(TaskInstanceId(id, index + 1), question(fact, choices))
        }
        val plan = SessionPlan(id, activity, revision, repository.version, SessionPolicy(round), tasks, completion)
        plan.validate(repository)
        return GenerationResult.Generated(plan)
    }

    override fun validate(state: SessionState) {
        val plan = state.plan
        require(plan.activity == activity && plan.activityRevision == revision && plan.contentVersion == repository.version)
        require(plan.policy.wrongAnswer == WrongAnswerPolicy.RETRY && plan.completionText == completion)
        val facts = plan.tasks.map { task ->
            val fact = definition(task.question)
            val expected = question(fact, task.question.choices)
            require(task.question.correct == expected.correct && task.question.skill == skill && task.question.context == context &&
                task.question.difficulty == expected.difficulty && task.question.instruction == prompt && task.question.hint == help)
            require(task.question.choices.size == 4 && task.question.choices.toSet().size == 4)
            fact
        }
        require(facts.size == plan.policy.round.count && facts.toSet().size == facts.size)
        require(facts.count { it in easy } == plan.policy.round.count / 2)
        require(facts.any { it == AdditionFact(4, 7) } && facts.any { it == AdditionFact(5, 9) })
    }
}
