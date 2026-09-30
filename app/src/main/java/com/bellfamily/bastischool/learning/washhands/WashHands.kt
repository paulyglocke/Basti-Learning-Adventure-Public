package com.bellfamily.bastischool.learning.washhands

import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.session.*
import com.bellfamily.bastischool.learning.sequencing.*
import com.bellfamily.bastischool.learning.progress.*
import com.bellfamily.bastischool.audio.SpeechTrigger

data class WashStep(val id: ContentId, val text: ContentText)
data class WashTransition(val state: SequenceState, val events: List<ProgressEvent> = emptyList(), val speech: SessionEffect.Narrate? = null)
object WashHands {
    const val REVISION=1
    val version=ContentVersion(1,1)
    val activity=ActivityId("activity.practical.wash_hands")
    val steps=listOf(
        WashStep(ContentId("step.wash_hands.water_on"),ContentText.plain("Turn on the water","Wasser anmachen")),
        WashStep(ContentId("step.wash_hands.wet"),ContentText.plain("Wet your hands","Hände nass machen")),
        WashStep(ContentId("step.wash_hands.soap"),ContentText.plain("Wash with soap","Mit Seife waschen")),
        WashStep(ContentId("step.wash_hands.rinse"),ContentText.plain("Rinse your hands","Hände abspülen")))
    val order=steps.map {it.id}
    fun step(id:ContentId)=steps.single {it.id==id}
    val title=ContentText.plain("Wash Hands","Hände waschen")
    val instruction=ContentText.plain("Listen to the steps. Tap them in order, then check. You can remove a step and try again.",
        "Hör dir die Schritte an. Tippe sie der Reihe nach an und prüfe dann. Du kannst einen Schritt zurücklegen und es noch einmal versuchen.")
    val wrong=ContentText.plain("Have another look at your order. You can change it.","Schau dir deine Reihenfolge noch einmal an. Du kannst sie ändern.")
    val completion=ContentText.plain("You put the steps in order!","Du hast die Schritte geordnet!")
    fun hint(s:SequenceState)=s.hintPosition?.let {ContentText.plain("Check step ${it+1}.","Schau dir Schritt ${it+1} an.")} ?: instruction
    fun start(id:SessionId,seed:Long,language:ContentLanguage)=SequenceAssembly.start(id,order,seed,language)
    fun reduce(s:SequenceState,a:SequenceAction):WashTransition {
        val n=SequenceAssembly.reduce(s,a)
        if(n===s) return if(a.session==s.id && a.revision==s.revision && a.operation==SequenceOperation.Replay && s.completed)
            WashTransition(s,speech=SessionEffect.Narrate(completion,NarrationKind.COMPLETION,SpeechTrigger.REPLAY)) else WashTransition(s)
        val events=if(a.operation==SequenceOperation.Check) attempts(n)+if(n.completed)listOf(complete(n)) else emptyList() else emptyList()
        val speech=when(a.operation){
            SequenceOperation.Check->SessionEffect.Narrate(if(n.completed)completion else wrong,if(n.completed)NarrationKind.COMPLETION else NarrationKind.FEEDBACK)
            SequenceOperation.Hint->SessionEffect.Narrate(hint(n),NarrationKind.INSTRUCTION,SpeechTrigger.MANUAL)
            SequenceOperation.Replay->SessionEffect.Narrate(instruction,NarrationKind.INSTRUCTION,SpeechTrigger.REPLAY)
            else->null
        }
        return WashTransition(n,events,speech)
    }
    fun listen(id:ContentId)=SessionEffect.Narrate(step(id).text,NarrationKind.INSTRUCTION,SpeechTrigger.MANUAL)
    private fun origin(s:SequenceState)=ProgressOrigin(s.id,activity,REVISION,version)
    private fun evidence(s:SequenceState,i:Int)=TaskEvidence(TaskInstanceId(s.id,i+1),TaskDefinitionId("task.wash_hands.position_${i+1}"),
        SkillId("skill.practical.sequence"),LearningContextId("context.practical.wash_hands"),1)
    // A Check evaluates four explicit positions. Editing/listening itself creates no evidence.
    fun attempts(s:SequenceState):List<AttemptEvent> {require(s.attempts>0);return order.indices.map {i->
        AttemptEvent(origin(s),evidence(s,i),AttemptId(TaskInstanceId(s.id,i+1),s.attempts),s.language,s.lastChecked[i],
            if(s.lastChecked[i]==order[i])AttemptOutcome.CORRECT else AttemptOutcome.INCORRECT,s.support)
    }}
    fun complete(s:SequenceState):CompletionEvent {require(s.completed);return CompletionEvent(origin(s),order.indices.map {i->
        CompletedTask(evidence(s,i),s.lastChecked[i],AttemptOutcome.CORRECT,s.attempts,s.attempts-1,s.support)
    })}
}
