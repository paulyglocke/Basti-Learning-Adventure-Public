package com.bellfamily.bastischool.learning.conservation

import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.progress.*
import com.bellfamily.bastischool.learning.session.*
import com.bellfamily.bastischool.learning.quantity.QuantityObjects
import java.util.Collections
import java.util.Random

enum class ConservationRange(val key:String,val limit:Int) {
    FIVE("five",5),TEN("ten",10);
    val slots get()=limit*2
}
enum class QuantityRelation(val delta:Int,val id:ContentId,val text:ContentText) {
    FEWER(-1,ContentId("quantity.fewer"),ContentText.plain("Fewer","Weniger")),
    SAME(0,ContentId("quantity.same"),ContentText.plain("Same amount","Gleich viele")),
    MORE(1,ContentId("quantity.more"),ContentText.plain("More","Mehr"))
}
enum class QuantityObject(val id:ContentId) {
    DINOSAUR(ContentId("animal.dinosaur")),APPLE(ContentId("food.apple"));
    fun noun(count:Int,language:ContentLanguage)=QuantityObjects.noun(id,count,language)
}
data class ConservationTask(val before:Int,val relation:QuantityRelation,val item:QuantityObject,val initialMask:Int) {
    val count get()=before+relation.delta
}

/** Moving occupied spaces can change position, never quantity. Only explicit relation checks assess. */
object Conservation {
    const val REVISION=1
    val version=ContentVersion(1,1)
    val title=ContentText.plain("Same Amount","Gleich viele")
    val complete=ContentText.plain("You compared every group!","Du hast alle Gruppen verglichen!")
    fun activity(c:ConservationRange)=ActivityId("activity.math.conservation.${c.key}")
    fun change(t:ConservationTask)=when(t.relation) {
        QuantityRelation.SAME->ContentText.plain("Only the places changed. Nothing joined or left.","Nur die Plätze wurden verändert. Es kam nichts dazu und nichts ging weg.")
        QuantityRelation.FEWER->ContentText.plain("One was taken away.","Eins wurde weggenommen.")
        QuantityRelation.MORE->ContentText.plain("One was added.","Eins wurde dazugelegt.")
    }
    fun prompt(t:ConservationTask,c:ConservationRange):ContentText {
        val en=t.item.noun(t.before,ContentLanguage.ENGLISH);val de=t.item.noun(t.before,ContentLanguage.GERMAN)
        return ContentText.plain("Before: ${t.before} $en. ${change(t).speech.en} You can move the pictures. Are there fewer, the same amount, or more now than before? Choose, then press Check.",
            "Vorher: ${t.before} $de. ${change(t).speech.de} Du kannst die Bilder verschieben. Sind es jetzt weniger, gleich viele oder mehr als vorher? Wähle und tippe auf Prüfen.")
    }
    val hint=ContentText.plain("Tap a picture, then an empty place to move it. Count both groups, or match each picture to one in the Before group. Moving pictures does not add or take any away.",
        "Tippe auf ein Bild und dann auf einen freien Platz. Zähle beide Gruppen oder ordne jedem Bild eins aus der Vorher-Gruppe zu. Beim Verschieben kommt nichts dazu und nichts geht weg.")
    fun feedback(s:State):ContentText {
        if(!s.current.solved)return ContentText.plain("Compare with Before. You can move the pictures and count again, then choose another answer.",
            "Vergleiche mit Vorher. Du kannst die Bilder verschieben und noch einmal zählen. Wähle dann eine andere Antwort.")
        return when(s.task.relation) {
            QuantityRelation.SAME->ContentText.plain("Still ${s.task.before}! Moving the pictures changes their places, not how many there are.","Immer noch ${s.task.before}! Beim Verschieben ändern sich die Plätze. Die Anzahl bleibt gleich.")
            QuantityRelation.FEWER->ContentText.plain("Fewer: one was taken away. There ${if(s.count==1)"is"else"are"} ${s.count} now${if(s.count==0)": zero"else""}.","Weniger: Eins wurde weggenommen. Jetzt sind es ${s.count}${if(s.count==0)": null"else""}.")
            QuantityRelation.MORE->ContentText.plain("More: one was added. There are ${s.count} now.","Mehr: Eins wurde dazugelegt. Jetzt sind es ${s.count}.")
        }
    }
    fun validMask(mask:Int,c:ConservationRange,count:Int)=mask>=0&&mask<(1 shl c.slots)&&Integer.bitCount(mask)==count
    fun valid(t:ConservationTask,c:ConservationRange)=t.before in 1..c.limit&&t.count in 0..c.limit&&validMask(t.initialMask,c,t.count)
    private fun shuffledMask(count:Int,c:ConservationRange,random:Random):Int {
        val positions=(0 until c.slots).toMutableList().also{Collections.shuffle(it,random)}
        var mask=positions.take(count).fold(0){m,i->m or(1 shl i)}
        // Do not begin with the same compact arrangement as the fixed reference.
        if(count>0&&mask==(1 shl count)-1)mask=(mask xor 1) or(1 shl(c.slots-1))
        return mask
    }
    fun generate(c:ConservationRange,round:RoundLength,seed:Long,previous:List<ConservationTask> = emptyList()):List<ConservationTask> {
        val random=Random(seed)
        val relations=List(round.count){if(it%5==1)QuantityRelation.FEWER else if(it%5==3)QuantityRelation.MORE else QuantityRelation.SAME}.toMutableList()
        Collections.shuffle(relations,random)
        val chosen=mutableListOf<ConservationTask>()
        val first=previous.lastOrNull()?.item?.let{if(it==QuantityObject.APPLE)QuantityObject.DINOSAUR else QuantityObject.APPLE}?:QuantityObject.entries[random.nextInt(2)]
        for((i,relation)in relations.withIndex()) {
            val item=if(i%2==0)first else QuantityObject.entries.first{it!=first}
            val candidates=(1..c.limit).filter { n->n+relation.delta in 0..c.limit&&chosen.none{it.before==n&&it.relation==relation&&it.item==item} }
                .toMutableList().also{Collections.shuffle(it,random)}
            val n=candidates.firstOrNull{n->i!=0||previous.lastOrNull()?.let{it.before!=n||it.relation!=relation}!=false}?:candidates.first()
            chosen+=ConservationTask(n,relation,item,shuffledMask(n+relation.delta,c,random))
        }
        return Collections.unmodifiableList(chosen)
    }
    data class Result(val attempts:Int=0,val wrong:Int=0,val solved:Boolean=false,val checked:QuantityRelation?=null,val support:SupportUse=SupportUse()) {
        init {require(attempts in 0..10000&&wrong in 0..attempts&&attempts==wrong+if(solved)1 else 0);require((attempts==0)==(checked==null))}
    }
    data class State(val id:SessionId,val config:ConservationRange,val tasks:List<ConservationTask>,val round:RoundLength,val language:ContentLanguage,
        val index:Int=0,val mask:Int=tasks.first().initialMask,val undoMasks:List<Int> = emptyList(),val selected:Int?=null,val answer:QuantityRelation?=null,
        val results:List<Result> = List(tasks.size){Result()},val completed:Boolean=false,val acknowledged:Boolean=false) {
        val task get()=tasks[index]
        val current get()=results[index]
        val count get()=Integer.bitCount(mask)
        val needsCorrection get()=current.attempts>0&&!current.solved&&answer==current.checked
        init {
            require(tasks.size==round.count&&tasks.all{valid(it,config)})
            require(tasks.map{Triple(it.before,it.relation,it.item)}.distinct().size==tasks.size)
            require(tasks.count{it.relation==QuantityRelation.SAME}==round.count*3/5)
            require(tasks.count{it.relation==QuantityRelation.FEWER}==round.count/5&&tasks.count{it.relation==QuantityRelation.MORE}==round.count/5)
            require(tasks.zipWithNext().all{(a,b)->a.item!=b.item})
            require(index in tasks.indices&&results.size==tasks.size)
            require(validMask(mask,config,task.count)&&undoMasks.size<=100&&undoMasks.all{validMask(it,config,task.count)})
            require((undoMasks+mask).zipWithNext().all{(a,b)->Integer.bitCount(a xor b)==2})
            require(selected==null||selected in 0 until config.slots&&mask and(1 shl selected)!=0)
            require(results.take(index).all{it.solved}&&results.drop(index+1).all{it==Result()})
            results.forEachIndexed{i,r->r.checked?.let{require(r.solved==(it==tasks[i].relation))}}
            require(!current.solved||answer==task.relation&&selected==null)
            require(!completed||index==tasks.lastIndex&&current.solved);require(!acknowledged||completed)
        }
    }
    fun start(id:SessionId,config:ConservationRange,round:RoundLength,seed:Long,language:ContentLanguage,previous:List<ConservationTask> = emptyList())=
        State(id,config,generate(config,round,seed,previous),round,language)
    sealed interface Action {
        data class Tap(val slot:Int):Action
        data class Choose(val relation:QuantityRelation):Action
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
        fun result(r:Result)=s.copy(results=s.results.mapIndexed{i,old->if(i==s.index)r else old},selected=if(r.solved)null else s.selected)
        return when(a) {
            is Action.Tap->if(s.current.solved||a.slot !in 0 until s.config.slots)s else when {
                s.mask and(1 shl a.slot)!=0->s.copy(selected=if(s.selected==a.slot)null else a.slot)
                s.selected==null->s
                else->s.copy(mask=(s.mask xor(1 shl s.selected)) or(1 shl a.slot),selected=null,undoMasks=(s.undoMasks+s.mask).takeLast(100))
            }
            is Action.Choose->if(s.current.solved||s.answer==a.relation)s else s.copy(answer=a.relation)
            Action.Undo->if(s.current.solved||s.undoMasks.isEmpty())s else s.copy(mask=s.undoMasks.last(),undoMasks=s.undoMasks.dropLast(1),selected=null)
            Action.Check->if(s.current.solved||s.answer==null||s.needsCorrection||s.current.attempts>=10000)s else {
                val solved=s.answer==s.task.relation
                result(s.current.copy(attempts=s.current.attempts+1,wrong=s.current.wrong+if(solved)0 else 1,solved=solved,checked=s.answer))
            }
            Action.Next->if(!s.current.solved)s else if(s.index==s.tasks.lastIndex)s.copy(completed=true)else s.copy(index=s.index+1,mask=s.tasks[s.index+1].initialMask,undoMasks=emptyList(),selected=null,answer=null)
            Action.Help->if(s.current.solved||s.current.support.hint)s else result(s.current.copy(support=s.current.support.copy(hint=true)))
            Action.Replay->if(s.current.solved)s else result(s.current.copy(support=s.current.support.copy(replays=(s.current.support.replays+1).coerceAtMost(10000))))
            is Action.Language->s
        }
    }
    private fun origin(s:State)=ProgressOrigin(s.id,activity(s.config),REVISION,version)
    private fun evidence(s:State,i:Int):TaskEvidence {
        val t=s.tasks[i]
        val skill=if(t.relation==QuantityRelation.SAME)"quantity_conservation"else"quantity_change_comparison"
        return TaskEvidence(TaskInstanceId(s.id,i+1),TaskDefinitionId("task.math.conservation.${s.config.key}.${t.item.name.lowercase()}.start_${t.before}.${t.relation.name.lowercase()}"),
            SkillId("skill.math.$skill.within_${s.config.limit}"),LearningContextId("context.math.conservation.${t.item.name.lowercase()}"),if(s.config==ConservationRange.FIVE)1 else 2)
    }
    fun attempt(s:State):AttemptEvent {val r=s.current;return AttemptEvent(origin(s),evidence(s,s.index),AttemptId(TaskInstanceId(s.id,s.index+1),r.attempts),s.language,requireNotNull(r.checked).id,if(r.solved)AttemptOutcome.CORRECT else AttemptOutcome.INCORRECT,r.support)}
    fun completion(s:State):CompletionEvent {require(s.completed);return CompletionEvent(origin(s),s.results.mapIndexed{i,r->CompletedTask(evidence(s,i),s.tasks[i].relation.id,AttemptOutcome.CORRECT,r.attempts,r.wrong,r.support)})}
}
