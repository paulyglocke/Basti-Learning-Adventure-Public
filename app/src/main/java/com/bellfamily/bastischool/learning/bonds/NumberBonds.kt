package com.bellfamily.bastischool.learning.bonds

import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.session.*
import com.bellfamily.bastischool.learning.progress.*
import java.util.Collections
import java.util.Random

enum class BondLevel(val total:Int,val key:String) { FIVE(5,"five"),TEN(10,"ten") }
/** Concrete part-whole construction. Fixed starting objects and editable additions stay distinct. */
object NumberBonds {
    const val REVISION=1
    val version=ContentVersion(1,1)
    private val en=listOf("zero","one","two","three","four","five","six","seven","eight","nine","ten")
    private val de=listOf("null","eins","zwei","drei","vier","fünf","sechs","sieben","acht","neun","zehn")
    fun activity(level:BondLevel)=ActivityId("activity.math.build_${level.total}")
    fun skill(level:BondLevel)=SkillId("skill.math.number_bonds.build_${level.total}")
    fun context(level:BondLevel)=LearningContextId("context.math.eggs.make_${level.total}")
    fun title(level:BondLevel)=ContentText.plain("Build ${level.total}","${if(level==BondLevel.FIVE)"Fünf" else "Zehn"} legen")
    val complete=ContentText.plain("You completed every group!","Du hast alle Gruppen ergänzt!")
    val help=ContentText.plain("The eggs already here stay in place. Tap an empty nest to add an egg. Tap an added egg to take it away. Then press Check.",
        "Die vorhandenen Eier bleiben liegen. Tippe auf ein leeres Nest, um ein Ei dazuzulegen. Tippe auf ein dazugelegtes Ei, um es wegzunehmen. Tippe dann auf Prüfen.")
    fun prompt(shown:Int,level:BondLevel)=ContentText.plain("There ${if(shown==1)"is one egg" else "are ${en[shown]} eggs"}. Add enough to make ${en[level.total]}.",
        "Hier ${if(shown==1)"liegt ein Ei" else "liegen ${de[shown]} Eier"}. Lege so viele dazu, dass es ${de[level.total]} sind.")
    fun feedback(state:State)=if(state.current.solved)ContentText.plain("${state.shown} and ${state.addedCount} make ${state.level.total}!","${state.shown} und ${state.addedCount} sind ${state.level.total}!")
        else ContentText.plain("Count the eggs. You can add more or take an added egg away.","Zähle die Eier. Du kannst weitere dazulegen oder ein dazugelegtes Ei wegnehmen.")
    fun countId(value:Int):ContentId {require(value in 0..10);return ContentId("number.${en[value]}")}
    data class Result(val attempts:Int=0,val wrong:Int=0,val solved:Boolean=false,val checkedCount:Int?=null,val support:SupportUse=SupportUse()) {
        init {require(attempts in 0..10000 && wrong in 0..attempts && attempts==wrong+if(solved)1 else 0)
            require((attempts==0)==(checkedCount==null));require(checkedCount==null||checkedCount in 0..10)}
    }
    data class State(val id:SessionId,val level:BondLevel,val targets:List<Int>,val round:RoundLength,val language:ContentLanguage,
        val index:Int=0,val placedMask:Int=0,val results:List<Result> = List(targets.size){Result()},val completed:Boolean=false,val acknowledged:Boolean=false) {
        val shown get()=targets[index]
        val fixedMask get()=(1 shl shown)-1
        val addedCount get()=Integer.bitCount(placedMask)
        val totalCount get()=shown+addedCount
        val current get()=results[index]
        init {
            require(targets.size==round.count && targets.all{it in 1 until level.total} && index in targets.indices && results.size==targets.size)
            require(targets.zipWithNext().none{(a,b)->a==b})
            val counts=(1 until level.total).map{n->targets.count{it==n}};require(counts.max()-counts.min()<=1)
            require(placedMask>=0 && placedMask<(1 shl level.total) && placedMask and fixedMask==0)
            require(results.take(index).all{it.solved}&&results.drop(index+1).all{it==Result()})
            results.forEachIndexed { i,r->if(r.checkedCount!=null){require(r.checkedCount in targets[i]..level.total);require(r.solved==(r.checkedCount==level.total))} }
            if(current.solved)require(totalCount==level.total)
            require(!completed||index==targets.lastIndex&&current.solved);require(!acknowledged||completed)
        }
    }
    sealed interface Action {
        data class Toggle(val slot:Int):Action
        data object Check:Action
        data object Next:Action
        data object Replay:Action
        data object Help:Action
        data class Language(val language:ContentLanguage):Action
    }
    fun targets(level:BondLevel,round:RoundLength,seed:Long):List<Int> {
        val random=Random(seed);val result=mutableListOf<Int>()
        while(result.size<round.count) {
            val cycle=(1 until level.total).toMutableList().also{Collections.shuffle(it,random)}
            if(cycle.first()==result.lastOrNull())Collections.swap(cycle,0,1)
            result+=cycle.take(round.count-result.size)
        }
        return Collections.unmodifiableList(result)
    }
    fun start(id:SessionId,level:BondLevel,round:RoundLength,seed:Long,language:ContentLanguage)=State(id,level,targets(level,round,seed),round,language)
    fun reduce(state:State,action:Action):State {
        if(action is Action.Language)return state.copy(language=action.language)
        if(state.completed)return state
        fun update(result:Result)=state.copy(results=state.results.mapIndexed{i,old->if(i==state.index)result else old})
        return when(action) {
            is Action.Toggle->if(state.current.solved||action.slot !in state.shown until state.level.total)state else state.copy(placedMask=state.placedMask xor (1 shl action.slot))
            Action.Check->{val r=state.current;if(r.solved||r.attempts>=10000)state else {val solved=state.totalCount==state.level.total;update(r.copy(attempts=r.attempts+1,wrong=r.wrong+if(solved)0 else 1,solved=solved,checkedCount=state.totalCount))}}
            Action.Next->if(!state.current.solved)state else if(state.index==state.targets.lastIndex)state.copy(completed=true)else state.copy(index=state.index+1,placedMask=0)
            Action.Help->if(state.current.solved||state.current.support.hint)state else update(state.current.copy(support=state.current.support.copy(hint=true)))
            Action.Replay->if(state.current.solved||state.current.support.replays>=10000)state else update(state.current.copy(support=state.current.support.copy(replays=state.current.support.replays+1)))
            is Action.Language->state
        }
    }
    private fun origin(s:State)=ProgressOrigin(s.id,activity(s.level),REVISION,version)
    private fun evidence(s:State,i:Int)=TaskEvidence(TaskInstanceId(s.id,i+1),TaskDefinitionId("task.math.build_${s.level.total}.${en[s.targets[i]]}"),skill(s.level),context(s.level),if(s.level==BondLevel.FIVE)1 else 2)
    fun attempt(s:State):AttemptEvent {val r=s.current;return AttemptEvent(origin(s),evidence(s,s.index),AttemptId(TaskInstanceId(s.id,s.index+1),r.attempts),s.language,countId(requireNotNull(r.checkedCount)),if(r.solved)AttemptOutcome.CORRECT else AttemptOutcome.INCORRECT,r.support)}
    fun completion(s:State)=CompletionEvent(origin(s),s.results.mapIndexed{i,r->require(s.completed&&r.solved);CompletedTask(evidence(s,i),countId(s.level.total),AttemptOutcome.CORRECT,r.attempts,r.attempts-1,r.support)})
}
