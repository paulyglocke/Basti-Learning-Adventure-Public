package com.bellfamily.bastischool.learning.seasons

import com.bellfamily.bastischool.learning.content.SeasonIds
import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.progress.*
import com.bellfamily.bastischool.learning.session.*
import java.util.Collections

/** Short authored observations of the canonical lakeside/tree scenes, not universal weather rules.
 * Keep definition IDs stable; authored changes need a deliberate activity revision/recovery review. */
object SeasonsClues {
    val activity = ActivityId("activity.seasons.clues")
    const val REVISION = 1

    // Two clues per season, checked against SeasonContent narration and the existing illustrations.
    // Every clue has a non-colour cue. Wording variants remain one learning context.
    val catalogue: List<ChoiceQuestion> = Collections.unmodifiableList(listOf(
        authored("spring.new_leaves", SeasonIds.SPRING,
            "New green leaves are growing on the tree, and flowers are starting to bloom.",
            "Am Baum wachsen neue grüne Blätter, und die Blumen fangen an zu blühen."),
        authored("spring.blossoms", SeasonIds.SPRING,
            "Blossoms are opening on the tree, and new green leaves are growing.",
            "Am Baum öffnen sich Blüten, und neue grüne Blätter wachsen."),
        authored("summer.full_tree", SeasonIds.SUMMER,
            "The tree is full of thick green leaves, and grass and plants grow all around it.",
            "Der Baum hat ein dichtes grünes Blätterdach. Rundherum wachsen Gras und Pflanzen."),
        authored("summer.sunshine", SeasonIds.SUMMER,
            "The sun is shining, and the tree is full of thick green leaves.",
            "Die Sonne scheint, und der Baum hat ein dichtes grünes Blätterdach."),
        authored("autumn.falling_leaves", SeasonIds.AUTUMN,
            "Orange, red and yellow leaves are falling from the tree.",
            "Orange, rote und gelbe Blätter fallen vom Baum."),
        authored("autumn.ground_leaves", SeasonIds.AUTUMN,
            "Leaves cover the ground under the tree, and more leaves are falling.",
            "Unter dem Baum liegen viele Blätter, und weitere fallen herunter."),
        authored("winter.snow", SeasonIds.WINTER,
            "Snow covers the ground, and the tree has no leaves.",
            "Schnee bedeckt den Boden, und der Baum hat keine Blätter."),
        authored("winter.ice", SeasonIds.WINTER,
            "There is ice on the lake and snow on the tree's bare branches.",
            "Auf dem See ist Eis, und auf den kahlen Ästen des Baumes liegt Schnee.")
    ))

    private fun authored(key: String, season: ContentId, en: String, de: String): ChoiceQuestion {
        val name = SeasonsContent.season(season).text.speech
        val help = when(season) {
            SeasonIds.SPRING -> ContentText.plain("Think about blossoms and new leaves.",
                "Denk an Blüten und neue Blätter.")
            SeasonIds.SUMMER -> ContentText.plain("Think about a tree full of leaves and lots of growing plants.",
                "Denk an einen Baum voller Blätter und viele wachsende Pflanzen.")
            SeasonIds.AUTUMN -> ContentText.plain("Think about leaves falling to the ground.",
                "Denk an Blätter, die auf den Boden fallen.")
            SeasonIds.WINTER -> ContentText.plain("Think about snow, ice and branches without leaves.",
                "Denk an Schnee, Eis und Äste ohne Blätter.")
            else -> error("Unknown season")
        }
        return ChoiceQuestion(TaskDefinitionId("task.seasons.clues.$key"),
            SkillId("skill.seasons.clues"), LearningContextId("context.seasons.lakeside_tree"), 1,
            ContentText.plain("$en Which season is it?", "$de Welche Jahreszeit ist das?"),
            SeasonIds.canonicalOrder, season,
            ContentText.plain("It is ${name.en}.", "Es ist ${name.de}."),
            ContentText.plain("Try again. You can listen or ask for help.",
                "Versuche es noch einmal. Du kannst noch einmal hören oder dir helfen lassen."), help)
    }

    fun generate(id: SessionId, round: RoundLength, seed: Long): GenerationResult {
        // Reuse the shared seeded eight-clue cycle and choice orders. Ten-question plans
        // remain unchanged; five-question plans take one clue per season and one extra.
        val generated = CandidateTaskGenerator(catalogue).generate(
            GenerationRequest(id, activity, REVISION, SessionPolicy(RoundLength.TEN), seed, SeasonsContent.completion),
            SeasonsContent.repository)
        if (generated !is GenerationResult.Generated || round == RoundLength.TEN) return generated
        val full = generated.plan
        val cycle = full.tasks.take(catalogue.size)
        val selected = cycle.distinctBy { it.question.correct }.toMutableList()
        selected += cycle.first { it !in selected }
        // Keep seeded relative order and frozen choices; ordinals belong to the final round.
        val tasks = cycle.filter { it in selected }.mapIndexed { index, task ->
            ChoiceTask(TaskInstanceId(id, index + 1), task.question)
        }
        return GenerationResult.Generated(SessionPlan(id, activity, REVISION, full.contentVersion,
            SessionPolicy(round), tasks, full.completionText))
    }

    fun validate(state: SessionState) {
        require(state.plan.activity == activity && state.plan.activityRevision == REVISION &&
            state.plan.contentVersion == SeasonsContent.repository.version)
        require(state.plan.policy == SessionPolicy(state.plan.policy.round) && state.plan.completionText == SeasonsContent.completion)
        state.plan.tasks.forEach { task ->
            val q = task.question
            val expected = requireNotNull(catalogue.singleOrNull { it.definition == q.definition }) { "Unknown season clue" }
            require(q.choices.size == 4 && q.choices.toSet() == SeasonIds.canonicalOrder.toSet())
            require(q.correct == expected.correct && q.skill == expected.skill && q.context == expected.context &&
                q.difficulty == expected.difficulty && q.instruction == expected.instruction && q.hint == expected.hint &&
                q.correctFeedback == expected.correctFeedback && q.wrongFeedback == expected.wrongFeedback)
        }
    }

    fun host(journal: ProgressStorage, progress: ProgressRepository) = DurableSessionHost(journal, progress,
        activity, REVISION, SeasonsContent.repository, ::generate, ::validate)
}
