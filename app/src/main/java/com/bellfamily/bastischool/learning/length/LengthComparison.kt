package com.bellfamily.bastischool.learning.length

import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.progress.*
import com.bellfamily.bastischool.learning.session.*
import java.util.Collections
import java.util.Random

enum class LengthRelation(val id: ContentId, val text: ContentText) {
    SHORTER(ContentId("length.shorter"), ContentText.plain("Shorter", "Kürzer")),
    SAME(ContentId("length.same"), ContentText.plain("The same length", "Gleich lang")),
    LONGER(ContentId("length.longer"), ContentText.plain("Longer", "Länger"))
}
enum class Strip { A, B }
data class StripPositions(val a: Int, val b: Int) {
    init { require(a in 0..3 && b in 0..3) }
    val aligned get() = a == 0 && b == 0
    fun offset(strip: Strip) = if (strip == Strip.A) a else b
    fun move(strip: Strip, offset: Int) = if (strip == Strip.A) copy(a = offset) else copy(b = offset)
}
data class LengthTask(val a: Int, val b: Int, val initial: StripPositions, val palette: Int) {
    init {
        require(a in 2..6 && b in 2..6 && (a == b || kotlin.math.abs(a - b) >= 2))
        require(initial.a in 1..3 && initial.b in 1..3 && initial.a != initial.b)
        require(palette in 0..3)
    }
    val relation get() = when { b < a -> LengthRelation.SHORTER; b > a -> LengthRelation.LONGER; else -> LengthRelation.SAME }
}

/** Direct length comparison. Translation never changes length; aligned starts make ends comparable. */
object LengthComparison {
    const val REVISION = 1
    val version = ContentVersion(1, 1)
    val activity = ActivityId("activity.math.length_comparison")
    val title = ContentText.plain("Longer / Shorter", "Länger / kürzer")
    val complete = ContentText.plain("You compared every pair of strips!", "Du hast alle Streifenpaare verglichen!")
    val question = ContentText.plain("Is the bottom strip B shorter, the same length, or longer than the top strip A?",
        "Ist der untere Streifen B kürzer, gleich lang oder länger als der obere Streifen A?")
    val prompt = ContentText.plain(
        "Compare the bottom strip B with the top strip A. Select a strip, then move it to the starting line. Line up both starts. ${question.speech.en} Choose, then press Check.",
        "Vergleiche den unteren Streifen B mit dem oberen Streifen A. Wähle einen Streifen und schiebe ihn zur Startlinie. Lege beide Anfänge an die Linie. ${question.speech.de} Wähle und tippe auf Prüfen.")
    val hint = ContentText.plain(
        "Put both starts on the line. Look at the other ends. B ends before A: shorter. Both end together: the same length. B ends after A: longer. Moving a strip does not change its length.",
        "Lege beide Anfänge an die Linie. Schau auf die anderen Enden. B endet vor A: kürzer. Beide enden zusammen: gleich lang. B endet hinter A: länger. Beim Verschieben bleibt die Länge gleich.")
    fun feedback(s: State): ContentText = if (!s.current.solved) ContentText.plain(
        "Line up both starts and compare the other ends, then choose another answer.",
        "Lege beide Anfänge an die Linie. Vergleiche die anderen Enden und wähle eine andere Antwort.") else when (s.task.relation) {
        LengthRelation.SHORTER -> ContentText.plain("B is shorter than A. Its end does not reach A's end.", "B ist kürzer als A. Sein Ende reicht nicht so weit wie das Ende von A.")
        LengthRelation.SAME -> ContentText.plain("B and A are the same length. Both starts and both ends line up.", "B und A sind gleich lang. Beide Anfänge und beide Enden liegen auf einer Höhe.")
        LengthRelation.LONGER -> ContentText.plain("B is longer than A. Its end reaches past A's end.", "B ist länger als A. Sein Ende reicht weiter als das Ende von A.")
    }
    fun generate(round: RoundLength, seed: Long, previous: List<LengthTask> = emptyList()): List<LengthTask> {
        val random = Random(seed)
        val relations = LengthRelation.entries.toMutableList().also { Collections.shuffle(it, random) }
        val order = List(round.count) { relations[it % 3] }.toMutableList().also { Collections.shuffle(it, random) }
        val tasks = mutableListOf<LengthTask>()
        for ((index, relation) in order.withIndex()) {
            val pairs = (2..6).flatMap { a -> (2..6).mapNotNull { b ->
                val matches = when (relation) { LengthRelation.SHORTER -> a - b >= 2; LengthRelation.SAME -> a == b; LengthRelation.LONGER -> b - a >= 2 }
                (a to b).takeIf { matches && tasks.none { it.a == a && it.b == b } }
            } }.toMutableList().also { Collections.shuffle(it, random) }
            val pair = pairs.first { candidate -> index != 0 || listOfNotNull(previous.firstOrNull(),previous.lastOrNull()).none { old -> old.a == candidate.first && old.b == candidate.second } }
            val aOffset = 1 + random.nextInt(3)
            val bOffset = (1..3).filter { it != aOffset }.let { it[random.nextInt(it.size)] }
            tasks += LengthTask(pair.first, pair.second, StripPositions(aOffset, bOffset), random.nextInt(4))
        }
        return Collections.unmodifiableList(tasks)
    }
    data class Result(val attempts: Int = 0, val wrong: Int = 0, val solved: Boolean = false,
        val checked: LengthRelation? = null, val support: SupportUse = SupportUse()) {
        init { require(attempts in 0..10000 && wrong in 0..attempts && attempts == wrong + if (solved) 1 else 0); require((attempts == 0) == (checked == null)) }
    }
    data class State(val id: SessionId, val tasks: List<LengthTask>, val round: RoundLength, val language: ContentLanguage,
        val index: Int = 0, val positions: StripPositions = tasks.first().initial, val undo: List<StripPositions> = emptyList(),
        val selected: Strip? = null, val answer: LengthRelation? = null, val results: List<Result> = List(tasks.size) { Result() },
        val completed: Boolean = false, val acknowledged: Boolean = false) {
        val task get() = tasks[index]
        val current get() = results[index]
        val needsCorrection get() = current.attempts > 0 && !current.solved && answer == current.checked
        init {
            require(tasks.size == round.count && index in tasks.indices && results.size == tasks.size)
            require(tasks.map { it.a to it.b }.distinct().size == tasks.size)
            val counts = LengthRelation.entries.map { relation -> tasks.count { it.relation == relation } }
            require(counts.min() >= 1 && counts.max() - counts.min() <= 1)
            require(undo.size <= 100 && (undo + positions).zipWithNext().all { (a,b) -> (a.a != b.a) xor (a.b != b.b) })
            require(results.take(index).all { it.solved } && results.drop(index + 1).all { it == Result() })
            results.forEachIndexed { i,r -> r.checked?.let { require(r.solved == (it == tasks[i].relation)) } }
            require(!current.solved || positions.aligned && answer == task.relation && selected == null)
            require(!completed || index == tasks.lastIndex && current.solved); require(!acknowledged || completed)
        }
    }
    fun start(id: SessionId, round: RoundLength, seed: Long, language: ContentLanguage, previous: List<LengthTask> = emptyList()) =
        State(id, generate(round, seed, previous), round, language)
    sealed interface Action {
        data class Select(val strip: Strip): Action
        data class Move(val offset: Int): Action
        data class Choose(val relation: LengthRelation): Action
        data object Undo: Action
        data object Check: Action
        data object Next: Action
        data object Replay: Action
        data object Help: Action
        data class Language(val language: ContentLanguage): Action
    }
    fun reduce(s: State, a: Action): State {
        if (a is Action.Language) return if (s.language == a.language) s else s.copy(language = a.language)
        if (s.completed) return s
        fun result(r: Result) = s.copy(results = s.results.mapIndexed { i,old -> if (i == s.index) r else old }, selected = if (r.solved) null else s.selected)
        return when (a) {
            is Action.Select -> if (s.current.solved) s else s.copy(selected = if (s.selected == a.strip) null else a.strip)
            is Action.Move -> if (s.current.solved || s.selected == null || a.offset !in 0..3 || a.offset == s.positions.offset(s.selected)) s else
                s.copy(positions = s.positions.move(s.selected, a.offset), undo = (s.undo + s.positions).takeLast(100))
            is Action.Choose -> if (s.current.solved || s.answer == a.relation) s else s.copy(answer = a.relation)
            Action.Undo -> if (s.current.solved || s.undo.isEmpty()) s else s.copy(positions = s.undo.last(), undo = s.undo.dropLast(1), selected = null)
            Action.Check -> if (s.current.solved || !s.positions.aligned || s.answer == null || s.needsCorrection || s.current.attempts >= 10000) s else {
                val solved = s.answer == s.task.relation
                result(s.current.copy(attempts = s.current.attempts + 1, wrong = s.current.wrong + if (solved) 0 else 1, solved = solved, checked = s.answer))
            }
            Action.Next -> if (!s.current.solved) s else if (s.index == s.tasks.lastIndex) s.copy(completed = true) else
                s.copy(index = s.index + 1, positions = s.tasks[s.index + 1].initial, undo = emptyList(), selected = null, answer = null)
            Action.Help -> if (s.current.solved || s.current.support.hint) s else result(s.current.copy(support = s.current.support.copy(hint = true)))
            Action.Replay -> if (s.current.solved) s else result(s.current.copy(support = s.current.support.copy(replays = (s.current.support.replays + 1).coerceAtMost(10000))))
            is Action.Language -> s
        }
    }
    private fun origin(s: State) = ProgressOrigin(s.id, activity, REVISION, version)
    private fun evidence(s: State, i: Int) = TaskEvidence(TaskInstanceId(s.id, i + 1),
        TaskDefinitionId("task.math.length_comparison.a_${s.tasks[i].a}.b_${s.tasks[i].b}"), SkillId("skill.math.length.direct_comparison"),
        LearningContextId("context.math.length.strips"), 1)
    fun attempt(s: State): AttemptEvent { val r = s.current; return AttemptEvent(origin(s), evidence(s,s.index), AttemptId(TaskInstanceId(s.id,s.index+1),r.attempts),
        s.language, requireNotNull(r.checked).id, if(r.solved) AttemptOutcome.CORRECT else AttemptOutcome.INCORRECT, r.support) }
    fun completion(s: State): CompletionEvent { require(s.completed); return CompletionEvent(origin(s), s.results.mapIndexed { i,r ->
        CompletedTask(evidence(s,i),s.tasks[i].relation.id,AttemptOutcome.CORRECT,r.attempts,r.wrong,r.support) }) }
}
