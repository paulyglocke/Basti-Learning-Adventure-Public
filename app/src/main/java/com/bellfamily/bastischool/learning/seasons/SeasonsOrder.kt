package com.bellfamily.bastischool.learning.seasons

import com.bellfamily.bastischool.learning.content.SeasonIds

import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.session.*
import com.bellfamily.bastischool.learning.progress.*
import com.bellfamily.bastischool.audio.SpeechTrigger
import java.util.Collections
import com.bellfamily.bastischool.learning.sequencing.OrderedPlacement

/** Four placement steps, not a quiz round. Immutable snapshots contain the actual scramble. */
class SeasonsOrderState internal constructor(val id: SessionId, val language: ContentLanguage,
    choices: List<ContentId>, steps: List<TaskProgress>, val acknowledged: Boolean = false) {
    val choices: List<ContentId> = Collections.unmodifiableList(choices.toList())
    val steps: List<TaskProgress> = Collections.unmodifiableList(steps.toList())
    private val placement = OrderedPlacement(SeasonIds.canonicalOrder, this.choices, this.steps)
    val index get() = placement.index
    val completed get() = index==4
    val placed get() = SeasonIds.canonicalOrder.take(index)
    val task get() = TaskInstanceId(id,(index+1).coerceAtMost(4))
    val current get() = steps[index.coerceAtMost(3)]
    val nextAttempt get() = if(!completed && current.answer==AnswerState.UNANSWERED) AttemptId(task,current.attempts+1) else null
    init {
        require(!acknowledged || completed)
    }
    internal fun changed(language: ContentLanguage=this.language, steps:List<TaskProgress> = this.steps, acknowledged:Boolean=this.acknowledged) =
        SeasonsOrderState(id,language,choices,steps,acknowledged)
}
sealed interface SeasonsOrderAction {
    data class Place(val attempt:AttemptId,val season:ContentId):SeasonsOrderAction
    data class Retry(val attempt:AttemptId):SeasonsOrderAction
    data class Replay(val task:TaskInstanceId):SeasonsOrderAction
    data class Help(val task:TaskInstanceId):SeasonsOrderAction
    data class Language(val language:ContentLanguage):SeasonsOrderAction
}
data class SeasonsOrderTransition(val state:SeasonsOrderState,val events:List<ProgressEvent> = emptyList(),val speech:SessionEffect.Narrate?=null)

object SeasonsOrder {
    const val REVISION = 1
    val activity=ActivityId("activity.seasons.order")
    val introduction=ContentText.plain(
        "The seasons form a cycle. For this picture of the year, start with Spring, then Summer, Autumn and Winter. After Winter comes Spring again. Tap the seasons in order.",
        "Die Jahreszeiten bilden einen Kreis. Für dieses Bild vom Jahr beginnen wir mit dem Frühling, dann kommen Sommer, Herbst und Winter. Nach dem Winter kommt wieder der Frühling. Tippe die Jahreszeiten der Reihe nach an.")
    val completion=ContentText.plain("You built the year! After Winter comes Spring again.",
        "Du hast die Jahreszeiten geordnet! Nach dem Winter kommt wieder der Frühling.")
    fun start(id:SessionId,seed:Long,language:ContentLanguage):SeasonsOrderState {
        val choices=OrderedPlacement.scramble(SeasonIds.canonicalOrder,seed)
        return SeasonsOrderState(id,language,choices,List(4){TaskProgress()})
    }
    fun prompt(state:SeasonsOrderState):ContentText = when {
        state.completed -> completion
        state.index==0 -> introduction
        else -> SeasonsContent.season(state.placed.last()).text.speech.let {
            ContentText.plain("${it.en} is in place. Which season comes next?", "${it.de} ist an seinem Platz. Welche Jahreszeit kommt als Nächstes?")
        }
    }
    fun help(state:SeasonsOrderState):ContentText = if(state.completed) completion else
        SeasonsContent.season(SeasonIds.canonicalOrder[state.index]).text.speech.let {ContentText.plain("Next is ${it.en}.","Als Nächstes kommt ${it.de}.")}
    fun reduce(s:SeasonsOrderState,a:SeasonsOrderAction):SeasonsOrderTransition {
        fun unchanged()=SeasonsOrderTransition(s)
        fun update(p:TaskProgress):SeasonsOrderState=s.changed(steps=s.steps.toMutableList().also {it[s.index]=p})
        return when(a) {
            is SeasonsOrderAction.Language -> SeasonsOrderTransition(s.changed(language=a.language))
            is SeasonsOrderAction.Place -> {
                if(s.nextAttempt!=a.attempt || a.season !in s.choices || a.season in s.placed || s.current.attempts>=10000) return unchanged()
                val correct=a.season==SeasonIds.canonicalOrder[s.index]
                val steps=OrderedPlacement(SeasonIds.canonicalOrder,s.choices,s.steps).place(a.season) ?: return unchanged()
                val n=s.changed(steps=steps)
                val events=listOf(attempt(n,s.index)) + if(n.completed)listOf(completion(n)) else emptyList()
                val text=if(correct)prompt(n) else ContentText.plain("Try again. The seasons already placed stay here.","Versuche es noch einmal. Die eingeordneten Jahreszeiten bleiben hier.")
                SeasonsOrderTransition(n,events,SessionEffect.Narrate(text,
                    if(n.completed)NarrationKind.COMPLETION else if(correct)NarrationKind.INSTRUCTION else NarrationKind.FEEDBACK,SpeechTrigger.AUTOMATIC))
            }
            is SeasonsOrderAction.Retry -> {
                if(s.completed || s.current.answer!=AnswerState.RETRY_AVAILABLE || a.attempt!=AttemptId(s.task,s.current.attempts)) unchanged()
                else SeasonsOrderTransition(update(s.current.copy(answer=AnswerState.UNANSWERED,retries=s.current.retries+1)))
            }
            is SeasonsOrderAction.Replay -> {
                if(a.task!=s.task) unchanged() else {
                    val n=if(s.completed) s else update(s.current.copy(support=s.current.support.copy(replays=(s.current.support.replays+1).coerceAtMost(10000))))
                    SeasonsOrderTransition(n,speech=SessionEffect.Narrate(prompt(n),if(n.completed)NarrationKind.COMPLETION else NarrationKind.INSTRUCTION,SpeechTrigger.REPLAY))
                }
            }
            is SeasonsOrderAction.Help -> {
                if(s.completed || a.task!=s.task) unchanged() else {
                    val n=update(s.current.copy(support=s.current.support.copy(hint=true)))
                    SeasonsOrderTransition(n,speech=SessionEffect.Narrate(help(n),NarrationKind.INSTRUCTION,SpeechTrigger.MANUAL))
                }
            }
        }
    }
    private fun origin(s:SeasonsOrderState)=ProgressOrigin(s.id,activity,REVISION,SeasonsContent.repository.version)
    private fun evidence(s:SeasonsOrderState,i:Int)=TaskEvidence(TaskInstanceId(s.id,i+1),
        TaskDefinitionId("task.seasons.order.${SeasonIds.canonicalOrder[i].value.substringAfter('.')}"),SkillId("skill.seasons.sequence"),LearningContextId("context.seasons.year_order"),1)
    fun attempt(s:SeasonsOrderState,i:Int):AttemptEvent {
        val p=s.steps[i]
        return AttemptEvent(origin(s),evidence(s,i),AttemptId(TaskInstanceId(s.id,i+1),p.attempts),s.language,p.lastChoice!!,
            if(p.answer==AnswerState.CORRECT)AttemptOutcome.CORRECT else AttemptOutcome.INCORRECT,p.support)
    }
    fun completion(s:SeasonsOrderState):CompletionEvent {
        require(s.completed)
        return CompletionEvent(origin(s),s.steps.mapIndexed {i,p -> CompletedTask(evidence(s,i),p.lastChoice!!,AttemptOutcome.CORRECT,p.attempts,p.retries,p.support)})
    }
}
