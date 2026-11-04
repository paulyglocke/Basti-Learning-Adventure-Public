package com.bellfamily.bastischool.learning.counting

import com.bellfamily.bastischool.learning.models.ContentLanguage
import com.bellfamily.bastischool.learning.models.ContentId
import com.bellfamily.bastischool.learning.models.ContentText
import com.bellfamily.bastischool.learning.models.ContentVersion
import com.bellfamily.bastischool.learning.progress.*
import com.bellfamily.bastischool.learning.session.*
import com.bellfamily.bastischool.learning.subitising.SubitisingContent
import java.util.Collections
import java.util.Random

/** Tap-to-build quantities in a five-space tray. Checks, not object movement, create evidence. */
object CountingTray {
    const val REVISION = 1
    val version = ContentVersion(1, 1)
    val activity = ActivityId("activity.math.build_quantity")
    val skill = SkillId("skill.math.quantity_construction")
    val context = LearningContextId("context.math.dinosaur_counting_tray")
    val title = ContentText.plain("Dinosaur Counting Tray", "Dinosaurier-Zählfeld")
    val complete = ContentText.plain("You made every group!", "Du hast alle Gruppen gelegt!")
    val checkAgain = ContentText.plain(
        "Count the dinosaurs in your tray. You can add one or take one away.",
        "Zähle die Dinosaurier im Feld. Du kannst einen dazulegen oder wegnehmen."
    )
    val help = ContentText.plain(
        "Tap an empty space to add a dinosaur. Tap a dinosaur to take it away.",
        "Tippe auf ein leeres Feld, um einen Dinosaurier dazuzulegen. Tippe auf einen Dinosaurier, um ihn wegzunehmen."
    )

    private val numberWords = listOf("zero", "one", "two", "three", "four", "five")
    fun countId(count: Int): ContentId {
        require(count in 0..5)
        return if (count == 0) ContentId("number.zero") else SubitisingContent.numbers[count - 1]
    }
    fun count(id: ContentId) = if (id.value == "number.zero") 0 else SubitisingContent.quantity(id)
    fun numberWord(value: Int, language: ContentLanguage) = when (language) {
        ContentLanguage.ENGLISH -> numberWords[value]
        ContentLanguage.GERMAN -> listOf("null", "eins", "zwei", "drei", "vier", "fünf")[value]
    }
    fun prompt(target: Int) = ContentText.plain(
        "Put ${numberWords[target]} ${if (target == 1) "dinosaur" else "dinosaurs"} in the tray.",
        if (target == 1) "Lege einen Dinosaurier ins Feld."
        else "Lege ${numberWord(target, ContentLanguage.GERMAN)} Dinosaurier ins Feld."
    )
    fun success(target: Int) = ContentText.plain(
        "You made a group of ${numberWords[target]} ${if (target == 1) "dinosaur" else "dinosaurs"}!",
        if (target == 1) "Du hast eine Gruppe mit einem Dinosaurier gelegt!"
        else "Du hast eine Gruppe mit ${numberWord(target, ContentLanguage.GERMAN)} Dinosauriern gelegt!"
    )

    data class Result(
        val attempts: Int = 0,
        val wrong: Int = 0,
        val solved: Boolean = false,
        val checkedCount: Int? = null,
        val support: SupportUse = SupportUse()
    ) {
        init {
            require(attempts in 0..10000 && wrong in 0..attempts)
            require(attempts == wrong + if (solved) 1 else 0)
            require((attempts == 0) == (checkedCount == null))
            require(checkedCount == null || checkedCount in 0..5)
        }
    }

    data class State(
        val id: SessionId,
        val targets: List<Int>,
        val round: RoundLength,
        val language: ContentLanguage,
        val index: Int = 0,
        val placedMask: Int = 0,
        val results: List<Result> = List(targets.size) { Result() },
        val completed: Boolean = false,
        val acknowledged: Boolean = false
    ) {
        val target get() = targets[index]
        val current get() = results[index]
        val placedCount get() = Integer.bitCount(placedMask)

        init {
            require(targets.size == round.count && targets.all { it in 1..5 })
            require(targets.count { it == 1 } == round.count / 5)
            require((1..5).all { value -> targets.count { it == value } == round.count / 5 })
            require(targets.zipWithNext().none { (a, b) -> a == b })
            require(index in targets.indices && placedMask in 0..31 && results.size == targets.size)
            require(results.take(index).all { it.solved } && results.drop(index + 1).all { it == Result() })
            results.forEachIndexed { i, result ->
                if (result.solved) require(result.checkedCount == targets[i])
                if (result.checkedCount != null) require(result.solved == (result.checkedCount == targets[i]))
            }
            if (current.solved) require(placedCount == target)
            require(!completed || index == targets.lastIndex && current.solved)
            require(!acknowledged || completed)
        }
    }

    sealed interface Action {
        data class Toggle(val slot: Int) : Action
        data object Check : Action
        data object Next : Action
        data object Replay : Action
        data object Help : Action
        data class Language(val language: ContentLanguage) : Action
    }

    fun generateTargets(round: RoundLength, seed: Long): List<Int> {
        val random = Random(seed)
        val remaining = (1..5).flatMap { value -> List(round.count / 5) { value } }
        fun arrange(left: List<Int>, previous: Int?): List<Int>? {
            if (left.isEmpty()) return emptyList()
            val choices = left.distinct().filter { it != previous }.toMutableList()
            Collections.shuffle(choices, random)
            for (choice in choices) {
                val next = left.toMutableList().also { it.remove(choice) }
                val suffix = arrange(next, choice) ?: continue
                return listOf(choice) + suffix
            }
            return null
        }
        return Collections.unmodifiableList(requireNotNull(arrange(remaining, null)))
    }

    fun start(id: SessionId, round: RoundLength, seed: Long, language: ContentLanguage) =
        State(id, generateTargets(round, seed), round, language)

    fun reduce(state: State, action: Action): State {
        if (action is Action.Language) return if (state.language == action.language) state else state.copy(language = action.language)
        if (state.completed) return state
        fun updateResult(result: Result) = state.copy(results = state.results.mapIndexed { i, old -> if (i == state.index) result else old })
        return when (action) {
            is Action.Toggle -> {
                if (state.current.solved || action.slot !in 0..4) state
                else state.copy(placedMask = state.placedMask xor (1 shl action.slot))
            }
            Action.Check -> {
                val current = state.current
                if (current.solved || current.attempts >= 10000) state else {
                    val success = state.placedCount == state.target
                    updateResult(current.copy(attempts = current.attempts + 1,
                        wrong = current.wrong + if (success) 0 else 1, solved = success,
                        checkedCount = state.placedCount))
                }
            }
            Action.Replay -> if (state.current.solved || state.current.support.replays >= 10000) state
                else updateResult(state.current.copy(support = state.current.support.copy(replays = state.current.support.replays + 1)))
            Action.Help -> if (state.current.solved || state.current.support.hint) state
                else updateResult(state.current.copy(support = state.current.support.copy(hint = true)))
            Action.Next -> if (!state.current.solved) state
                else if (state.index == state.targets.lastIndex) state.copy(completed = true)
                else state.copy(index = state.index + 1, placedMask = 0)
            is Action.Language -> state
        }
    }

    private fun origin(state: State) = ProgressOrigin(state.id, activity, REVISION, version)
    private fun evidence(state: State, index: Int) = TaskEvidence(
        TaskInstanceId(state.id, index + 1),
        TaskDefinitionId("task.math.build_quantity.${numberWords[state.targets[index]]}"), skill, context, 1
    )

    fun attempt(state: State): AttemptEvent {
        val result = state.current
        require(result.attempts > 0 && result.checkedCount != null)
        return AttemptEvent(origin(state), evidence(state, state.index),
            AttemptId(TaskInstanceId(state.id, state.index + 1), result.attempts), state.language,
            countId(result.checkedCount), if (result.solved) AttemptOutcome.CORRECT else AttemptOutcome.INCORRECT,
            result.support)
    }

    fun completion(state: State) = CompletionEvent(origin(state), state.results.mapIndexed { index, result ->
        require(state.completed && result.solved)
        CompletedTask(evidence(state, index), countId(state.targets[index]), AttemptOutcome.CORRECT,
            result.attempts, result.attempts - 1, result.support)
    })
}
