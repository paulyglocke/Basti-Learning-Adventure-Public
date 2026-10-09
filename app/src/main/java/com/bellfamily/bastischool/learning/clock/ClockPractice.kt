package com.bellfamily.bastischool.learning.clock

import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.progress.*
import com.bellfamily.bastischool.learning.session.*
import java.util.Collections
import java.util.Random

/** A manipulation task, not a multiple-choice question. Only explicit Check creates evidence. */
object ClockPractice {
    const val REVISION = 1
    const val STEP = 30
    val version = ContentVersion(1, 1)
    val activity = ActivityId("activity.time.clock.make")
    val targets = (1..12).flatMap { listOf(ClockTime(it), ClockTime(it, 30)) }
    val complete = ContentText.plain("You made all eight times!", "Du hast alle acht Uhrzeiten eingestellt!")
    val retry = ContentText.plain("Not quite. Try again.", "Noch nicht ganz. Versuch es noch einmal.")
    fun phrase(t: ClockTime, language: ContentLanguage) = ClockWording.phrase(t, language).replaceFirstChar { it.lowercase() }
    fun prompt(t: ClockTime) = ContentText.plain("Make ${phrase(t, ContentLanguage.ENGLISH)}.", "Stelle ${phrase(t, ContentLanguage.GERMAN)} ein.")
    fun correct(t: ClockTime) = ContentText.plain("Yes! That's ${phrase(t, ContentLanguage.ENGLISH)}.", "Ja! Das ist ${phrase(t, ContentLanguage.GERMAN)}.")
    fun hint(t: ClockTime): ContentText {
        val mark = if (t.minute == 0) 12 else 6
        return ContentText.plain("Put the long hand on $mark.", "Stelle den langen Zeiger auf die $mark.")
    }
    fun startTime(target: ClockTime) = target.advance(-90)
    fun generate(seed: Long): List<ClockTime> {
        val random = Random(seed)
        fun pick(minute: Int) = targets.filter { it.minute == minute }.toMutableList().also { Collections.shuffle(it, random) }.take(4)
        val whole = pick(0); val half = pick(30)
        // One of each type in every pair, with seeded pair order: mixed, bounded and balanced.
        return (0..3).flatMap { if (random.nextBoolean()) listOf(whole[it], half[it]) else listOf(half[it], whole[it]) }
    }
    data class Result(val attempts: Int = 0, val wrong: Int = 0, val solved: Boolean = false,
                      val support: SupportUse = SupportUse(), val checked: ClockTime? = null) {
        init {
            require(attempts in 0..10000 && wrong in 0..attempts)
            require(attempts == wrong + if (solved) 1 else 0)
            require((attempts == 0) == (checked == null))
            require(support.hint == (wrong >= 2))
        }
    }
    data class State(val id: SessionId, val targets: List<ClockTime>, val language: ContentLanguage,
                     val index: Int = 0, val position: ClockTime = startTime(targets[0]),
                     val results: List<Result> = List(8) { Result() }, val completed: Boolean = false,
                     val acknowledged: Boolean = false) {
        val target get() = targets[index]
        val current get() = results[index]
        val firstTry get() = results.count { it.solved && it.attempts == 1 }
        init {
            require(targets.size == 8 && targets.toSet().size == 8 && targets.all { it in ClockPractice.targets })
            require(targets.count { it.minute == 0 } == 4 && index in 0..7 && results.size == 8)
            require(position.minute in listOf(0, 30))
            require(results.take(index).all { it.solved } && results.drop(index + 1).all { it == Result() })
            results.forEachIndexed { i, r ->
                require(r.checked == null || r.checked in ClockPractice.targets)
                if (r.checked != null) require(r.solved == (r.checked == targets[i]))
            }
            if (current.solved) require(position == target)
            require(!completed || index == 7 && current.solved)
            require(!acknowledged || completed)
        }
    }
    sealed interface Action {
        data class Move(val time: ClockTime): Action
        data object Check: Action
        data object Next: Action
        data object Replay: Action
        data class Language(val language: ContentLanguage): Action
    }
    fun reduce(s: State, a: Action): State {
        if (a is Action.Language) return if (s.language == a.language) s else s.copy(language = a.language)
        if (s.completed) return s
        fun result(r: Result) = s.copy(results = s.results.mapIndexed { i, old -> if (i == s.index) r else old })
        return when (a) {
            is Action.Move -> if (s.current.solved) s else s.copy(position = a.time.snap(STEP))
            Action.Replay -> if (s.current.solved) s else result(s.current.copy(support = s.current.support.copy(replays = s.current.support.replays + 1)))
            Action.Check -> {
                if (s.current.solved || s.current.attempts == 10000) s else {
                    val success = s.position == s.target
                    val wrong = s.current.wrong + if (success) 0 else 1
                    result(s.current.copy(attempts = s.current.attempts + 1, wrong = wrong, solved = success,
                        checked = s.position, support = s.current.support.copy(hint = wrong >= 2)))
                }
            }
            Action.Next -> if (!s.current.solved) s else if (s.index == 7) s.copy(completed = true)
                else s.copy(index = s.index + 1, position = startTime(s.targets[s.index + 1]))
            is Action.Language -> s
        }
    }
    private fun name(t: ClockTime) = "h${t.hour}.${if (t.minute == 0) "whole" else "half"}"
    fun timeId(t: ClockTime) = ContentId("time.clock.${name(t)}")
    private fun origin(s: State) = ProgressOrigin(s.id, activity, REVISION, version)
    private fun evidence(s: State, index: Int) = TaskEvidence(TaskInstanceId(s.id, index + 1),
        TaskDefinitionId("task.time.clock.make.${name(s.targets[index])}"),
        SkillId("skill.time.clock.make.${if (s.targets[index].minute == 0) "whole" else "half"}"),
        LearningContextId("context.time.analogue_clock"), 1)
    fun attempt(s: State): AttemptEvent {
        val r = s.current
        // The hint triggered by this wrong check is support for the NEXT check, not retroactive support.
        val priorHint = r.wrong - (if (r.solved) 0 else 1) >= 2
        return AttemptEvent(origin(s), evidence(s, s.index), AttemptId(TaskInstanceId(s.id, s.index + 1), r.attempts),
            s.language, timeId(requireNotNull(r.checked)), if (r.solved) AttemptOutcome.CORRECT else AttemptOutcome.INCORRECT,
            r.support.copy(hint = priorHint))
    }
    fun completion(s: State) = CompletionEvent(origin(s), s.results.mapIndexed { i, r ->
        require(s.completed && r.solved)
        CompletedTask(evidence(s, i), timeId(s.targets[i]), AttemptOutcome.CORRECT, r.attempts, r.attempts - 1, r.support)
    })
}
