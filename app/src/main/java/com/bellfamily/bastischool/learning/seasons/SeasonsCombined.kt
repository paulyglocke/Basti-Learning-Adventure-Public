package com.bellfamily.bastischool.learning.seasons

import com.bellfamily.bastischool.learning.content.SeasonIds
import com.bellfamily.bastischool.learning.models.ContentId
import com.bellfamily.bastischool.learning.models.ContentText
import com.bellfamily.bastischool.learning.models.ContentLanguage
import com.bellfamily.bastischool.learning.session.ActivityId
import com.bellfamily.bastischool.learning.session.RoundLength
import java.util.Random
import com.bellfamily.bastischool.learning.progress.ProgressStorage
import java.io.*

/** The bounded two-slot season-cycle quiz.  Slots are deliberately ordered. */
object SeasonsCombined {
    const val REVISION = 1
    val activity = ActivityId("activity.seasons.combined")
    val prompt = ContentText.plain("Which season comes before and after %s?", "Welche Jahreszeit kommt vor und nach dem %s?")
    val beforeLabel = ContentText.plain("Before", "Davor")
    val afterLabel = ContentText.plain("After", "Danach")
    val help = ContentText.plain("Think around the year: what comes just before it, and what comes just after it?", "Denke an den Jahreskreis: Was kommt direkt davor und was direkt danach?")
    data class Task(val anchor: ContentId, val choices: List<ContentId>) {
        val before get() = SeasonIds.previous(anchor)
        val after get() = SeasonIds.next(anchor)
    }
    data class State(val id: String, val tasks: List<Task>, val index: Int = 0,
                     val before: ContentId? = null, val after: ContentId? = null,
                     val attempts: Int = 0, val help: Boolean = false,
                     val language: ContentLanguage = ContentLanguage.ENGLISH,
                     val completed: Boolean = false) {
        val task get() = tasks[index.coerceAtMost(tasks.lastIndex)]
        val solved get() = before == task.before && after == task.after
    }
    sealed interface Action { data class Before(val id: ContentId): Action; data class After(val id: ContentId): Action; data object Check: Action; data object Retry: Action; data object Help: Action; data class Language(val value: ContentLanguage): Action; data object Next: Action }
    fun reduce(s: State, a: Action): State = when (a) {
        is Action.Before -> if (s.completed) s else s.copy(before = a.id)
        is Action.After -> if (s.completed) s else s.copy(after = a.id)
        Action.Help -> if (s.help) s else s.copy(help = true)
        Action.Retry -> s.copy(attempts = s.attempts + 1)
        Action.Check -> if (s.solved) s else s.copy(attempts = s.attempts + 1)
        is Action.Language -> s.copy(language = a.value)
        Action.Next -> if (!s.solved) s else if (s.index + 1 >= s.tasks.size) s.copy(completed = true) else s.copy(index = s.index + 1, before = null, after = null, attempts = 0, help = false)
    }
    class Host(private val storage: ProgressStorage) {
        var state: State? = null; private set
        fun open(id: String, round: RoundLength, seed: Long, language: ContentLanguage) {
            val bytes = storage.access { it.read() }
            state = if (bytes == null) State(id, generate(round, seed), language = language).also { save(it) } else decode(bytes)
        }
        fun dispatch(action: Action) { state = reduce(requireNotNull(state), action).also { save(it) } }
        private fun save(s: State) { val out=ByteArrayOutputStream(); DataOutputStream(out).use { o -> o.writeInt(1);o.writeUTF(s.id);o.writeUTF(s.language.name);o.writeInt(s.index);o.writeInt(s.attempts);o.writeBoolean(s.help);o.writeBoolean(s.completed);o.writeUTF(s.before?.value ?: "");o.writeUTF(s.after?.value ?: "");o.writeInt(s.tasks.size);s.tasks.forEach { t -> o.writeUTF(t.anchor.value);t.choices.forEach { o.writeUTF(it.value) } } }; storage.access { it.replace(out.toByteArray()) } }
        private fun decode(bytes: ByteArray): State = DataInputStream(ByteArrayInputStream(bytes)).use { i -> require(i.readInt()==1);val id=i.readUTF();val lang=ContentLanguage.valueOf(i.readUTF());val index=i.readInt();val attempts=i.readInt();val help=i.readBoolean();val complete=i.readBoolean();val before=i.readUTF().takeIf { it.isNotEmpty() }?.let(::ContentId);val after=i.readUTF().takeIf { it.isNotEmpty() }?.let(::ContentId);val tasks=List(i.readInt()) { task(ContentId(i.readUTF()),List(4){ContentId(i.readUTF())}) };State(id,tasks,index,before,after,attempts,help,lang,complete) }
    }
    fun task(anchor: ContentId, choices: List<ContentId> = SeasonIds.canonicalOrder) = Task(anchor, choices.toList()).also {
        require(it.choices.size == 4 && it.choices.toSet() == SeasonIds.canonicalOrder.toSet())
    }
    fun generate(round: RoundLength, seed: Long): List<Task> {
        val anchors = SeasonIds.canonicalOrder.toMutableList()
        val random = Random(seed)
        java.util.Collections.shuffle(anchors, random)
        val all = anchors + anchors
        return all.take(round.count).mapIndexed { index, anchor ->
            val choices = SeasonIds.canonicalOrder.toMutableList()
            java.util.Collections.shuffle(choices, Random(seed + index * 31L + 7L))
            task(anchor, choices)
        }
    }
    fun question(task: Task, language: ContentLanguage): String {
        val name = SeasonsContent.season(task.anchor).text.display[language]
        return prompt.display[language].format(name)
    }
}
