package com.bellfamily.bastischool.learning.length

import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.progress.*
import com.bellfamily.bastischool.learning.sequencing.*
import com.bellfamily.bastischool.learning.session.*
import java.util.Collections
import java.util.Random

data class LengthOrderTask(val lengths: List<Int>, val presentation: List<ContentId>, val palette: Int) {
    init {
        require(lengths.size == 3 && lengths.all { it in 2..8 })
        require(lengths.sorted().zipWithNext().all { (a,b) -> b-a >= 2 })
        require(presentation.size == 3 && presentation.toSet() == LengthOrdering.strips.toSet())
        require(presentation != target && palette in 0..3)
    }
    val target get() = LengthOrdering.strips.sortedBy { length(it) }
    fun length(id: ContentId) = lengths[LengthOrdering.strips.indexOf(id).also { require(it >= 0) }]
}

/** Structural adapter around SequenceAssembly's free construction, removal and explicit Check. */
data class LengthOrderWork(val order: List<ContentId> = emptyList(), val revision: Int = 0,
    val attempts: Int = 0, val checked: List<ContentId> = emptyList(), val support: SupportUse = SupportUse(),
    val hintPosition: Int? = null) {
    fun sequence(id: SessionId, task: LengthOrderTask, language: ContentLanguage) =
        SequenceState(id,language,task.target,task.presentation,order,revision,attempts,checked,support,hintPosition)
    companion object {
        fun from(s: SequenceState) = LengthOrderWork(s.constructed,s.revision,s.attempts,s.lastChecked,s.support,s.hintPosition)
    }
}

object LengthOrdering {
    const val REVISION = 1
    val version = ContentVersion(1,1)
    val activity = ActivityId("activity.math.length_order")
    val strips = listOf("a","b","c").map { ContentId("length_order.strip_$it") }
    fun letter(id: ContentId) = id.value.takeLast(1).uppercase()
    val title = ContentText.plain("Order by Length", "Nach Länge ordnen")
    val question = ContentText.plain("Shortest at the top. Longest at the bottom.", "Der kürzeste nach oben. Der längste nach unten.")
    val prompt = ContentText.plain(
        "Put the strips on the right in order. Shortest at the top, longest at the bottom. Tap a strip on the left to add it. Tap a placed strip to put it back. You can use Undo. Then press Check.",
        "Ordne die Streifen rechts. Der kürzeste kommt nach oben, der längste nach unten. Tippe links auf einen Streifen, um ihn einzufügen. Tippe auf einen eingeordneten Streifen, um ihn zurückzulegen. Du kannst den letzten Schritt rückgängig machen. Dann tippe auf Prüfen.")
    val hint = ContentText.plain(
        "Compare the ends at the starting lines. Put the shortest strip first. Compare the two strips left, then put the shorter one next. The longest goes last. Moving a strip does not change its length.",
        "Vergleiche die Enden an den Startlinien. Lege den kürzesten Streifen zuerst hin. Vergleiche die beiden übrigen Streifen und lege den kürzeren als Nächstes hin. Der längste kommt zuletzt. Beim Verschieben bleibt die Länge gleich.")
    val complete = ContentText.plain("You ordered every set of strips!", "Du hast alle Streifengruppen geordnet!")
    fun feedback(s: State) = if (s.solved) ContentText.plain(
        "The strips go from shortest to longest.", "Die Streifen sind von kurz nach lang geordnet.") else ContentText.plain(
        "Compare the ends at the starting lines. You can put a strip back and try a different order.",
        "Vergleiche die Enden an den Startlinien. Du kannst einen Streifen zurücklegen und eine andere Reihenfolge versuchen.")

    fun generate(round: RoundLength, seed: Long, previous: List<LengthOrderTask> = emptyList()): List<LengthOrderTask> {
        val random = Random(seed)
        val sets = (2..8).flatMap { a -> (a+2..8).flatMap { b -> (b+2..8).map { c -> listOf(a,b,c) } } }
            .toMutableList().also { Collections.shuffle(it,random) }
        return Collections.unmodifiableList(sets.take(round.count).mapIndexed { index, values ->
            val permutations = values.flatMap { a -> values.filter { it != a }.map { b -> listOf(a,b,values.single { it != a && it != b }) } }
                .toMutableList().also { Collections.shuffle(it,random) }
            val lengths = permutations.first { candidate -> index != 0 || listOfNotNull(previous.firstOrNull(),previous.lastOrNull()).none { it.lengths == candidate } }
            val target = strips.sortedBy { lengths[strips.indexOf(it)] }
            val presentation = SequenceAssembly.start(SessionId("length-order-plan"),target,random.nextLong(),ContentLanguage.ENGLISH).presentation
            LengthOrderTask(lengths,presentation,random.nextInt(4))
        })
    }

    data class State(val id: SessionId, val tasks: List<LengthOrderTask>, val round: RoundLength, val language: ContentLanguage,
        val index: Int = 0, val work: List<LengthOrderWork> = List(tasks.size) { LengthOrderWork() },
        val undo: List<List<ContentId>> = emptyList(), val completed: Boolean = false, val acknowledged: Boolean = false) {
        val task get() = tasks[index]
        val current get() = work[index]
        val solved get() = current.attempts > 0 && current.checked == task.target
        val needsCorrection get() = current.attempts > 0 && !solved && current.order == current.checked
        val canCheck get() = !completed && !solved && current.order.size == 3 && !needsCorrection && current.attempts < 10000 && current.revision < 100000
        init {
            require(tasks.size == round.count && index in tasks.indices && work.size == tasks.size)
            require(tasks.map { it.lengths.sorted() }.distinct().size == tasks.size)
            work.forEachIndexed { i,w ->
                val sequence = w.sequence(id,tasks[i],language)
                if(i < index) require(sequence.completed)
                if(i > index) require(w == LengthOrderWork())
            }
            require(!completed || index == tasks.lastIndex && solved)
            require(!acknowledged || completed)
            require(undo.size <= 100)
            val history = undo + listOf(current.order)
            history.forEach { require(it.size <= 3 && it.distinct().size == it.size && it.all { id -> id in strips }) }
            require(history.zipWithNext().all { (a,b) ->
                val shorter = if(a.size < b.size)a else b; val longer = if(a.size < b.size)b else a
                longer.size == shorter.size+1 && longer.filter { it in shorter } == shorter
            })
        }
    }
    fun start(id: SessionId, round: RoundLength, seed: Long, language: ContentLanguage, previous: List<LengthOrderTask> = emptyList()) =
        State(id,generate(round,seed,previous),round,language)
    sealed interface Action {
        data class Place(val strip: ContentId): Action
        data class Remove(val strip: ContentId): Action
        data object Undo: Action
        data object Check: Action
        data object Next: Action
        data object Help: Action
        data object Replay: Action
        data class Language(val language: ContentLanguage): Action
    }
    fun reduce(s: State, a: Action): State {
        if(a is Action.Language) return if(s.language == a.language)s else s.copy(language=a.language)
        if(s.completed)return s
        if(a == Action.Next)return if(!s.solved)s else if(s.index == s.tasks.lastIndex)s.copy(completed=true)
            else s.copy(index=s.index+1,undo=emptyList())
        if(s.solved || s.current.revision >= 100000)return s
        fun update(w: LengthOrderWork, undo: List<List<ContentId>> = s.undo) =
            s.copy(work=s.work.mapIndexed { i,old -> if(i == s.index)w else old },undo=undo)
        if(a == Action.Undo) {
            if(s.undo.isEmpty())return s
            return update(s.current.copy(order=s.undo.last(),revision=s.current.revision+1,hintPosition=null),s.undo.dropLast(1))
        }
        if(a == Action.Check && !s.canCheck)return s
        val operation = when(a) {
            is Action.Place -> SequenceOperation.Append(a.strip)
            is Action.Remove -> SequenceOperation.Remove(a.strip)
            Action.Check -> SequenceOperation.Check
            Action.Help -> SequenceOperation.Hint
            Action.Replay -> SequenceOperation.Replay
            else -> return s
        }
        val sequence = s.current.sequence(s.id,s.task,s.language)
        val changed = SequenceAssembly.reduce(sequence,SequenceAction(sequence.id,sequence.revision,operation))
        var next = LengthOrderWork.from(changed)
        if(a == Action.Help && !next.support.hint) next = next.copy(support=next.support.copy(hint=true))
        if(next == s.current)return s
        val history = if(next.order != s.current.order)(s.undo + listOf(s.current.order)).takeLast(100) else s.undo
        return update(next,history)
    }
    private fun origin(s: State) = ProgressOrigin(s.id,activity,REVISION,version)
    private fun evidence(s: State, i: Int) = TaskEvidence(TaskInstanceId(s.id,i+1),
        TaskDefinitionId("task.math.length_order.a_${s.tasks[i].lengths[0]}.b_${s.tasks[i].lengths[1]}.c_${s.tasks[i].lengths[2]}"),
        SkillId("skill.math.length.order_three"),LearningContextId("context.math.length.strips"),1)
    private fun answer(ids: List<ContentId>) = ContentId("length_order.order_"+ids.joinToString("_") { it.value.takeLast(1) })
    fun attempt(s: State): AttemptEvent { val w=s.current; require(w.attempts > 0 && w.checked.size == 3)
        return AttemptEvent(origin(s),evidence(s,s.index),AttemptId(TaskInstanceId(s.id,s.index+1),w.attempts),s.language,
            answer(w.checked),if(s.solved)AttemptOutcome.CORRECT else AttemptOutcome.INCORRECT,w.support)
    }
    fun completion(s: State): CompletionEvent { require(s.completed)
        return CompletionEvent(origin(s),s.work.mapIndexed { i,w -> CompletedTask(evidence(s,i),answer(s.tasks[i].target),
            AttemptOutcome.CORRECT,w.attempts,w.attempts-1,w.support) })
    }
}
