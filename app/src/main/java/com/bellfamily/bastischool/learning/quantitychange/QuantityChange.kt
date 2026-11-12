package com.bellfamily.bastischool.learning.quantitychange

import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.session.*
import com.bellfamily.bastischool.learning.progress.*
import com.bellfamily.bastischool.learning.bonds.NumberBonds
import com.bellfamily.bastischool.learning.vocabulary.VocabularyContent
import java.util.Collections
import java.util.Random

enum class ChangeMode(val key:String,val delta:Int,val title:ContentText) {
    MORE("more",1,ContentText.plain("Add one","Eins dazulegen")),
    LESS("less",-1,ContentText.plain("Take one away","Eins wegnehmen"))
}
enum class ChangeRange(val key:String,val limit:Int) { FIVE("five",5),TEN("ten",10) }
data class ChangeConfig(val mode:ChangeMode,val range:ChangeRange) {
    val key get()="${mode.key}-${range.key}"
    val starts get()=if(mode==ChangeMode.MORE)0 until range.limit else 1..range.limit
    companion object { val all=ChangeMode.entries.flatMap{mode->ChangeRange.entries.map{ChangeConfig(mode,it)}} }
}
data class ChangeTask(val start:Int,val animal:ContentId)

/** Relative quantity construction. Edits are exploratory; a changed Check is the only submission. */
object QuantityChange {
    const val REVISION=1
    val version=ContentVersion(1,1)
    val title=ContentText.plain("One More / One Less","Eins mehr / eins weniger")
    val complete=ContentText.plain("You changed every group!","Du hast alle Gruppen verändert!")
    val animals get()=VocabularyContent.items.map{it.id}
    fun label(id:ContentId)=VocabularyContent.item(id).text
    fun initialMask(task:ChangeTask)=(1 shl task.start)-1
    fun target(task:ChangeTask,config:ChangeConfig)=task.start+config.mode.delta
    fun prompt(task:ChangeTask,config:ChangeConfig):ContentText {
        val en=when(task.start){0->"There are no animals yet.";1->"There is one animal.";else->"There are ${task.start} animals."}
        val de=when(task.start){0->"Hier sind noch keine Tiere.";1->"Hier ist ein Tier.";else->"Hier sind ${task.start} Tiere."}
        return ContentText.plain("$en ${if(config.mode==ChangeMode.MORE)"Add one more animal."else"Take one animal away."} How many now? Change the group, then press Check.",
            "$de ${if(config.mode==ChangeMode.MORE)"Lege ein Tier dazu."else"Nimm ein Tier weg."} Wie viele sind es jetzt? Verändere die Gruppe. Tippe dann auf Prüfen.")
    }
    fun hint(config:ChangeConfig)=ContentText.plain(
        if(config.mode==ChangeMode.MORE)"Look at the starting group. Tap one empty space to add an animal. Count the new group. Undo lets you try again."
        else "Look at the starting group. Tap one animal to take it away. Count what is left. If none are left, that is zero. Undo lets you try again.",
        if(config.mode==ChangeMode.MORE)"Schau dir die Anfangsgruppe an. Tippe auf ein leeres Feld und lege ein Tier dazu. Zähle die neue Gruppe. Du kannst den letzten Schritt rückgängig machen."
        else "Schau dir die Anfangsgruppe an. Tippe auf ein Tier und nimm es weg. Zähle, wie viele übrig sind. Wenn keins übrig ist, sind es null. Du kannst den letzten Schritt rückgängig machen.")
    fun feedback(s:State)=if(!s.current.solved)ContentText.plain(
        "Compare with the starting group. ${if(s.config.mode==ChangeMode.MORE)"We need exactly one more."else"We need exactly one fewer."} You can change the group and try again.",
        "Vergleiche mit der Anfangsgruppe. ${if(s.config.mode==ChangeMode.MORE)"Es soll genau ein Tier mehr sein."else"Es soll genau ein Tier weniger sein."} Du kannst die Gruppe ändern und es erneut versuchen.")
        else if(s.answer==0)ContentText.plain("One animal, take one away. None left: zero!","Ein Tier, eins weg. Keins übrig: null!")
        else ContentText.plain("${s.task.start} ${if(s.config.mode==ChangeMode.MORE)"and one more make"else"take away one leaves"} ${s.answer}!",
            "${s.task.start} ${if(s.config.mode==ChangeMode.MORE)"und eins dazu sind"else"minus eins sind"} ${s.answer}!")
    fun activity(c:ChangeConfig)=ActivityId("activity.math.one_${c.mode.key}.${c.range.key}")
    fun skill(c:ChangeConfig)=SkillId("skill.math.one_${c.mode.key}.within_${c.range.limit}")
    fun context(t:ChangeTask)=LearningContextId("context.math.quantity_change.${t.animal.value.substringAfter('.')}")
    fun generate(config:ChangeConfig,round:RoundLength,seed:Long,previous:List<ChangeTask> = emptyList()):List<ChangeTask> {
        val random=Random(seed);val starts=mutableListOf<Int>()
        while(starts.size<round.count) {
            val cycle=config.starts.toMutableList().also{Collections.shuffle(it,random)}
            if(cycle.first()==(starts.lastOrNull()?:previous.lastOrNull()?.start))Collections.swap(cycle,0,1)
            starts+=cycle.take(round.count-starts.size)
        }
        val tasks=mutableListOf<ChangeTask>()
        starts.forEach { start->
            val eligible=animals.shuffled(random).filter{animal->animal!=(tasks.lastOrNull()?:previous.lastOrNull())?.animal&&ChangeTask(start,animal) !in tasks}
            val animal=eligible.minBy{a->tasks.count{it.animal==a}}
            tasks+=ChangeTask(start,animal)
        }
        if(tasks==previous) {
            val first=tasks.first()
            tasks[0]=first.copy(animal=animals.first{it!=first.animal&&it!=tasks[1].animal&&it!=previous.last().animal&&first.copy(animal=it) !in tasks})
        }
        return Collections.unmodifiableList(tasks)
    }
    data class Result(val attempts:Int=0,val wrong:Int=0,val solved:Boolean=false,val checkedCount:Int?=null,val support:SupportUse=SupportUse()) {
        init {
            require(attempts in 0..10000&&wrong in 0..attempts&&attempts==wrong+if(solved)1 else 0)
            require((attempts==0)==(checkedCount==null));require(checkedCount==null||checkedCount in 0..10)
        }
    }
    data class State(val id:SessionId,val config:ChangeConfig,val tasks:List<ChangeTask>,val round:RoundLength,val language:ContentLanguage,
        val index:Int=0,val mask:Int=initialMask(tasks.first()),val undoMasks:List<Int> = emptyList(),
        val results:List<Result> = List(tasks.size){Result()},val completed:Boolean=false,val acknowledged:Boolean=false) {
        val task get()=tasks[index]
        val current get()=results[index]
        val count get()=Integer.bitCount(mask)
        val answer get()=target(task,config)
        val needsCorrection get()=current.attempts>0&&!current.solved&&count==current.checkedCount
        init {
            require(tasks.size==round.count&&tasks.all{it.start in config.starts&&it.animal in animals}&&tasks.distinct().size==tasks.size)
            require(tasks.zipWithNext().all{(a,b)->a.start!=b.start&&a.animal!=b.animal})
            val counts=config.starts.map{n->tasks.count{it.start==n}};require(counts.max()-counts.min()<=1)
            require(index in tasks.indices&&results.size==tasks.size)
            fun valid(m:Int)=m>=0&&m<(1 shl config.range.limit)
            require(valid(mask)&&undoMasks.size<=100&&undoMasks.all(::valid))
            require((undoMasks+mask).zipWithNext().all{(a,b)->Integer.bitCount(a xor b)==1})
            require(results.take(index).all{it.solved}&&results.drop(index+1).all{it==Result()})
            results.forEachIndexed{i,r->r.checkedCount?.let{require(it in 0..config.range.limit);require(r.solved==(it==target(tasks[i],config)))}}
            require(!current.solved||count==answer)
            require(!completed||index==tasks.lastIndex&&current.solved);require(!acknowledged||completed)
        }
    }
    fun start(id:SessionId,config:ChangeConfig,round:RoundLength,seed:Long,language:ContentLanguage,previous:List<ChangeTask> = emptyList())=
        State(id,config,generate(config,round,seed,previous),round,language)
    sealed interface Action {
        data class Toggle(val slot:Int):Action
        data object Undo:Action
        data object Check:Action
        data object Next:Action
        data object Replay:Action
        data object Help:Action
        data class Language(val language:ContentLanguage):Action
    }
    fun reduce(s:State,a:Action):State {
        if(a is Action.Language)return if(s.language==a.language)s else s.copy(language=a.language)
        if(s.completed)return s
        fun result(r:Result)=s.copy(results=s.results.mapIndexed{i,old->if(i==s.index)r else old})
        return when(a) {
            is Action.Toggle->if(s.current.solved||a.slot !in 0 until s.config.range.limit)s else s.copy(mask=s.mask xor(1 shl a.slot),undoMasks=(s.undoMasks+s.mask).takeLast(100))
            Action.Undo->if(s.current.solved||s.undoMasks.isEmpty())s else s.copy(mask=s.undoMasks.last(),undoMasks=s.undoMasks.dropLast(1))
            Action.Check->if(s.current.solved||s.needsCorrection||s.current.attempts>=10000)s else {
                val solved=s.count==s.answer;result(s.current.copy(attempts=s.current.attempts+1,wrong=s.current.wrong+if(solved)0 else 1,solved=solved,checkedCount=s.count))
            }
            Action.Next->if(!s.current.solved)s else if(s.index==s.tasks.lastIndex)s.copy(completed=true)else s.copy(index=s.index+1,mask=initialMask(s.tasks[s.index+1]),undoMasks=emptyList())
            Action.Help->if(s.current.solved||s.current.support.hint)s else result(s.current.copy(support=s.current.support.copy(hint=true)))
            Action.Replay->if(s.current.solved)s else result(s.current.copy(support=s.current.support.copy(replays=(s.current.support.replays+1).coerceAtMost(10000))))
            is Action.Language->s
        }
    }
    private fun origin(s:State)=ProgressOrigin(s.id,activity(s.config),REVISION,version)
    private fun evidence(s:State,i:Int)=TaskEvidence(TaskInstanceId(s.id,i+1),TaskDefinitionId("task.math.one_${s.config.mode.key}.${s.config.range.key}.${NumberBonds.countId(s.tasks[i].start).value.substringAfter('.')}.${s.tasks[i].animal.value.substringAfter('.')}"),skill(s.config),context(s.tasks[i]),if(s.config.range==ChangeRange.FIVE)1 else 2)
    fun attempt(s:State):AttemptEvent {val r=s.current;return AttemptEvent(origin(s),evidence(s,s.index),AttemptId(TaskInstanceId(s.id,s.index+1),r.attempts),s.language,NumberBonds.countId(requireNotNull(r.checkedCount)),if(r.solved)AttemptOutcome.CORRECT else AttemptOutcome.INCORRECT,r.support)}
    fun completion(s:State):CompletionEvent {require(s.completed);return CompletionEvent(origin(s),s.results.mapIndexed{i,r->CompletedTask(evidence(s,i),NumberBonds.countId(target(s.tasks[i],s.config)),AttemptOutcome.CORRECT,r.attempts,r.wrong,r.support)})}
}
