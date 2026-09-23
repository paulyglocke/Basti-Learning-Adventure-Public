package com.bellfamily.bastischool.learning.wilma

import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.session.*
import com.bellfamily.bastischool.learning.progress.*
import com.bellfamily.bastischool.audio.SpeechTrigger
import java.util.Collections
import com.bellfamily.bastischool.learning.sequencing.OrderedPlacement

/** Seven placement steps, not a quiz round. Immutable snapshots contain the actual scramble. */
class WilmaOrderState internal constructor(val id: SessionId, val language: ContentLanguage,
    choices: List<ContentId>, steps: List<TaskProgress>, val acknowledged: Boolean = false) {
    val choices: List<ContentId> = Collections.unmodifiableList(choices.toList())
    val steps: List<TaskProgress> = Collections.unmodifiableList(steps.toList())
    private val placement = OrderedPlacement(WilmaContent.days, this.choices, this.steps)
    val index get() = placement.index
    val completed get() = index==7
    val placed get() = WilmaContent.days.take(index)
    val task get() = TaskInstanceId(id,(index+1).coerceAtMost(7))
    val current get() = steps[index.coerceAtMost(6)]
    val nextAttempt get() = if(!completed && current.answer==AnswerState.UNANSWERED) AttemptId(task,current.attempts+1) else null
    init {
        require(!acknowledged || completed)
    }
    internal fun changed(language: ContentLanguage=this.language, steps:List<TaskProgress> = this.steps, acknowledged:Boolean=this.acknowledged) =
        WilmaOrderState(id,language,choices,steps,acknowledged)
}
sealed interface WilmaOrderAction {
    data class Place(val attempt:AttemptId,val day:ContentId):WilmaOrderAction
    data class Retry(val attempt:AttemptId):WilmaOrderAction
    data class Replay(val task:TaskInstanceId):WilmaOrderAction
    data class Help(val task:TaskInstanceId):WilmaOrderAction
    data class Language(val language:ContentLanguage):WilmaOrderAction
}
data class WilmaOrderTransition(val state:WilmaOrderState,val events:List<ProgressEvent> = emptyList(),val speech:SessionEffect.Narrate?=null)

object WilmaOrder {
    val activity=ActivityId("activity.wilma.order")
    fun start(id:SessionId,seed:Long,language:ContentLanguage):WilmaOrderState {
        val choices=OrderedPlacement.scramble(WilmaContent.days,seed)
        return WilmaOrderState(id,language,choices,List(7){TaskProgress()})
    }
    fun prompt(state:WilmaOrderState):ContentText = when {
        state.completed -> WilmaContent.completion
        state.index==0 -> ContentText.plain("Put the days in order. Start with ${WilmaContent.day(WilmaContent.days.first()).text.speech.en}. Tap a day below.",
            "Ordne die Wochentage. Beginne mit ${WilmaContent.day(WilmaContent.days.first()).text.speech.de}. Tippe unten auf einen Tag.")
        else -> WilmaContent.day(state.placed.last()).text.speech.let {
            ContentText.plain("${it.en} is in place. Which day comes next?", "${it.de} ist an seinem Platz. Welcher Tag kommt als Nächstes?")
        }
    }
    fun help(state:WilmaOrderState):ContentText = if(state.completed) WilmaContent.completion else
        WilmaContent.day(WilmaContent.days[state.index]).text.speech.let {ContentText.plain("Next is ${it.en}.","Als Nächstes kommt ${it.de}.")}
    fun reduce(s:WilmaOrderState,a:WilmaOrderAction):WilmaOrderTransition {
        fun unchanged()=WilmaOrderTransition(s)
        fun update(p:TaskProgress):WilmaOrderState=s.changed(steps=s.steps.toMutableList().also {it[s.index]=p})
        return when(a) {
            is WilmaOrderAction.Language -> WilmaOrderTransition(s.changed(language=a.language))
            is WilmaOrderAction.Place -> {
                if(s.nextAttempt!=a.attempt || a.day !in s.choices || a.day in s.placed || s.current.attempts>=10000) return unchanged()
                val correct=a.day==WilmaContent.days[s.index]
                val steps=OrderedPlacement(WilmaContent.days,s.choices,s.steps).place(a.day) ?: return unchanged()
                val n=s.changed(steps=steps)
                val events=listOf(attempt(n,s.index)) + if(n.completed)listOf(completion(n)) else emptyList()
                val text=if(correct)prompt(n) else ContentText.plain("Try again. The days already placed stay here.","Versuche es noch einmal. Die eingeordneten Tage bleiben hier.")
                WilmaOrderTransition(n,events,SessionEffect.Narrate(text,
                    if(n.completed)NarrationKind.COMPLETION else if(correct)NarrationKind.INSTRUCTION else NarrationKind.FEEDBACK,SpeechTrigger.AUTOMATIC))
            }
            is WilmaOrderAction.Retry -> {
                if(s.completed || s.current.answer!=AnswerState.RETRY_AVAILABLE || a.attempt!=AttemptId(s.task,s.current.attempts)) unchanged()
                else WilmaOrderTransition(update(s.current.copy(answer=AnswerState.UNANSWERED,retries=s.current.retries+1)))
            }
            is WilmaOrderAction.Replay -> {
                if(a.task!=s.task) unchanged() else {
                    val n=if(s.completed) s else update(s.current.copy(support=s.current.support.copy(replays=(s.current.support.replays+1).coerceAtMost(10000))))
                    WilmaOrderTransition(n,speech=SessionEffect.Narrate(prompt(n),if(n.completed)NarrationKind.COMPLETION else NarrationKind.INSTRUCTION,SpeechTrigger.REPLAY))
                }
            }
            is WilmaOrderAction.Help -> {
                if(s.completed || a.task!=s.task) unchanged() else {
                    val n=update(s.current.copy(support=s.current.support.copy(hint=true)))
                    WilmaOrderTransition(n,speech=SessionEffect.Narrate(help(n),NarrationKind.INSTRUCTION,SpeechTrigger.MANUAL))
                }
            }
        }
    }
    private fun origin(s:WilmaOrderState)=ProgressOrigin(s.id,activity,WilmaContent.REVISION,WilmaContent.repository.version)
    private fun evidence(s:WilmaOrderState,i:Int)=TaskEvidence(TaskInstanceId(s.id,i+1),
        TaskDefinitionId("task.wilma.order.${WilmaContent.days[i].value.substringAfter('.')}"),SkillId("skill.weekdays.sequence"),LearningContextId("context.weekdays.wilma_order"),1)
    fun attempt(s:WilmaOrderState,i:Int):AttemptEvent {
        val p=s.steps[i]
        return AttemptEvent(origin(s),evidence(s,i),AttemptId(TaskInstanceId(s.id,i+1),p.attempts),s.language,p.lastChoice!!,
            if(p.answer==AnswerState.CORRECT)AttemptOutcome.CORRECT else AttemptOutcome.INCORRECT,p.support)
    }
    fun completion(s:WilmaOrderState):CompletionEvent {
        require(s.completed)
        return CompletionEvent(origin(s),s.steps.mapIndexed {i,p -> CompletedTask(evidence(s,i),p.lastChoice!!,AttemptOutcome.CORRECT,p.attempts,p.retries,p.support)})
    }
}
