package com.bellfamily.bastischool.learning.patterns

import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.session.*
import com.bellfamily.bastischool.learning.progress.*
import com.bellfamily.bastischool.learning.vocabulary.VocabularyContent
import java.util.Collections
import java.util.Random

enum class PatternLevel(val key:String,val rule:List<Int>,val title:ContentText) {
    AB("ab",listOf(0,1),ContentText.plain("Alternating","Immer abwechselnd")),
    AAB("aab",listOf(0,0,1),ContentText.plain("Two, then one","Zwei, dann eins")),
    ABC("abc",listOf(0,1,2),ContentText.plain("Three in turn","Drei im Wechsel"));
    val distinctSymbols get()=rule.distinct().size
    val difficulty get()=when(this){AB->1;AAB->2;ABC->3}
}
enum class PatternContext(val key:String) { ANIMALS("animals"),SHAPES("shapes") }
enum class PatternShape(val id:ContentId,val label:ContentText) {
    CIRCLE(ContentId("shape.circle"),ContentText.plain("Circle","Kreis")),
    SQUARE(ContentId("shape.square"),ContentText.plain("Square","Quadrat")),
    TRIANGLE(ContentId("shape.triangle"),ContentText.plain("Triangle","Dreieck"))
}
/** Frozen semantic rule; repeated symbols are intentional and never matched by label/colour. */
data class PatternTask(val level:PatternLevel,val context:PatternContext,val symbols:List<ContentId>,val choices:List<ContentId>) {
    val unit get()=level.rule.map{symbols[it]}
    val example get()=unit+unit
    val key get()="${level.key}.${context.key}."+symbols.joinToString("."){it.value.substringAfter('.')}
    init {
        require(symbols.size==level.distinctSymbols&&symbols.distinct().size==symbols.size)
        require(symbols.all{it in PatternBuilder.pool(context)})
        require(choices.size==symbols.size&&choices.toSet()==symbols.toSet())
    }
}
object PatternBuilder {
    const val REVISION=1
    val version=ContentVersion(1,1)
    val title=ContentText.plain("Pattern Builder","Muster bauen")
    val instruction=ContentText.plain("Continue the pattern. Tap pictures to fill the empty spaces, then press Check.",
        "Setze das Muster fort. Tippe auf die Bilder, um die leeren Felder zu füllen. Tippe dann auf Prüfen.")
    val complete=ContentText.plain("You completed every pattern!","Du hast alle Muster ergänzt!")
    val correct=ContentText.plain("The same group repeats. You continued the pattern!","Die gleiche Gruppe wiederholt sich. Du hast das Muster fortgesetzt!")
    val retry=ContentText.plain("Look at the repeating group. You can change the pictures and try again.",
        "Schau dir die sich wiederholende Gruppe an. Du kannst die Bilder ändern und es noch einmal versuchen.")
    fun pool(context:PatternContext)=when(context) {
        PatternContext.ANIMALS->VocabularyContent.items.map{it.id}
        PatternContext.SHAPES->PatternShape.entries.map{it.id}
    }
    fun label(id:ContentId):ContentText=PatternShape.entries.firstOrNull{it.id==id}?.label?:VocabularyContent.item(id).text
    fun prompt(task:PatternTask)=ContentText.plain(
        "Look: ${task.example.joinToString(", "){label(it).speech.en}}. Build the next group. Then press Check.",
        "Schau: ${task.example.joinToString(", "){label(it).speech.de}}. Lege die nächste Gruppe. Tippe dann auf Prüfen.")
    fun hint(task:PatternTask)=ContentText.plain(
        "This group repeats: ${task.unit.joinToString(", "){label(it).speech.en}}. Fill the next group in the same order.",
        "Diese Gruppe wiederholt sich: ${task.unit.joinToString(", "){label(it).speech.de}}. Lege die nächste Gruppe in der gleichen Reihenfolge.")
    fun activity(level:PatternLevel)=ActivityId("activity.patterns.${level.key}")
    fun skill(level:PatternLevel)=SkillId("skill.reasoning.pattern.${level.key}")
    fun context(task:PatternTask)=LearningContextId("context.patterns.${task.context.key}")
    private fun candidates(level:PatternLevel,context:PatternContext):List<List<ContentId>> {
        fun choose(prefix:List<ContentId>):List<List<ContentId>> = if(prefix.size==level.distinctSymbols)listOf(prefix)
            else pool(context).filter{it !in prefix}.flatMap{choose(prefix+it)}
        return choose(emptyList())
    }
    fun generate(level:PatternLevel,round:RoundLength,seed:Long,previous:List<PatternTask> = emptyList()):List<PatternTask> {
        val random=Random(seed)
        val pools=PatternContext.entries.associateWith{candidates(level,it).toMutableList().also{Collections.shuffle(it,random)}}
        val used=mutableSetOf<String>();val animalSets=mutableSetOf<Set<ContentId>>()
        val first=if(random.nextBoolean())PatternContext.ANIMALS else PatternContext.SHAPES
        val plan=(0 until round.count).map{index->
            val context=if(index%2==0)first else PatternContext.entries.single{it!=first}
            val symbols=pools.getValue(context).first { candidate->
                val key="${level.key}.${context.key}."+candidate.joinToString("."){it.value.substringAfter('.')}
                key !in used && (index!=0||key!=previous.lastOrNull()?.key) && (context!=PatternContext.ANIMALS||candidate.toSet() !in animalSets)
            }
            val choices=symbols.toMutableList().also{Collections.shuffle(it,random)}
            PatternTask(level,context,symbols.toList(),choices.toList()).also{used+=it.key;if(context==PatternContext.ANIMALS)animalSets+=symbols.toSet()}
        }.toMutableList()
        if(plan==previous)Collections.swap(plan,0,2)
        return Collections.unmodifiableList(plan)
    }
    data class Result(val attempts:Int=0,val wrong:Int=0,val solved:Boolean=false,val checked:List<ContentId> = emptyList(),val support:SupportUse=SupportUse()) {
        init {require(attempts in 0..10000&&wrong in 0..attempts&&attempts==wrong+if(solved)1 else 0);require((attempts==0)==checked.isEmpty())}
    }
    data class State(val id:SessionId,val level:PatternLevel,val tasks:List<PatternTask>,val round:RoundLength,val language:ContentLanguage,
        val index:Int=0,val placed:List<ContentId?> = List(level.rule.size){null},val results:List<Result> = List(tasks.size){Result()},
        val completed:Boolean=false,val acknowledged:Boolean=false) {
        val task get()=tasks[index]
        val current get()=results[index]
        val full get()=placed.none{it==null}
        val needsCorrection get()=current.attempts>0&&!current.solved&&placed==current.checked
        val firstMismatch get()=placed.indices.firstOrNull{placed[it]!=task.unit[it]}
        init {
            require(tasks.size==round.count&&tasks.all{it.level==level}&&tasks.map{it.key}.distinct().size==tasks.size)
            require(tasks.zipWithNext().all{(a,b)->a.context!=b.context})
            require(index in tasks.indices&&placed.size==level.rule.size&&results.size==tasks.size)
            require(placed.all{it==null||it in task.symbols})
            require(results.take(index).all{it.solved}&&results.drop(index+1).all{it==Result()})
            results.forEachIndexed{i,r->if(r.attempts>0){require(r.checked.size==level.rule.size&&r.checked.all{it in tasks[i].symbols});require(r.solved==(r.checked==tasks[i].unit))}}
            require(!current.solved||placed==task.unit)
            require(!completed||index==tasks.lastIndex&&current.solved);require(!acknowledged||completed)
        }
    }
    fun start(id:SessionId,level:PatternLevel,round:RoundLength,seed:Long,language:ContentLanguage,previous:List<PatternTask> = emptyList()) =
        State(id,level,generate(level,round,seed,previous),round,language)
    sealed interface Action {
        data class Add(val symbol:ContentId):Action
        data class Remove(val slot:Int):Action
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
            is Action.Add -> if(s.current.solved||a.symbol !in s.task.choices||s.full)s else {
                val slot=s.placed.indexOfFirst{it==null};s.copy(placed=s.placed.mapIndexed{i,old->if(i==slot)a.symbol else old})
            }
            is Action.Remove -> if(s.current.solved||a.slot !in s.placed.indices||s.placed[a.slot]==null)s else s.copy(placed=s.placed.mapIndexed{i,old->if(i==a.slot)null else old})
            Action.Undo -> s.placed.indexOfLast{it!=null}.let{if(it<0)s else reduce(s,Action.Remove(it))}
            Action.Check -> if(s.current.solved||!s.full||s.needsCorrection||s.current.attempts>=10000)s else {
                val solved=s.placed==s.task.unit;result(s.current.copy(attempts=s.current.attempts+1,wrong=s.current.wrong+if(solved)0 else 1,solved=solved,checked=s.placed.filterNotNull()))
            }
            Action.Next -> if(!s.current.solved)s else if(s.index==s.tasks.lastIndex)s.copy(completed=true)else s.copy(index=s.index+1,placed=List(s.level.rule.size){null})
            Action.Replay -> if(s.current.solved)s else result(s.current.copy(support=s.current.support.copy(replays=(s.current.support.replays+1).coerceAtMost(10000))))
            Action.Help -> if(s.current.solved||s.current.support.hint)s else result(s.current.copy(support=s.current.support.copy(hint=true)))
            is Action.Language -> s
        }
    }
    fun answerId(symbols:List<ContentId>):ContentId {
        require(symbols.size in 2..3)
        return ContentId("pattern.answer."+symbols.joinToString("_"){it.value.substringAfter('.')})
    }
    private fun origin(s:State)=ProgressOrigin(s.id,activity(s.level),REVISION,version)
    private fun evidence(s:State,i:Int)=TaskEvidence(TaskInstanceId(s.id,i+1),TaskDefinitionId("task.patterns.${s.tasks[i].key}"),skill(s.level),context(s.tasks[i]),s.level.difficulty)
    fun attempt(s:State):AttemptEvent {val r=s.current;require(r.attempts>0);return AttemptEvent(origin(s),evidence(s,s.index),AttemptId(TaskInstanceId(s.id,s.index+1),r.attempts),s.language,answerId(r.checked),if(r.solved)AttemptOutcome.CORRECT else AttemptOutcome.INCORRECT,r.support)}
    fun completion(s:State):CompletionEvent {require(s.completed);return CompletionEvent(origin(s),s.results.mapIndexed{i,r->CompletedTask(evidence(s,i),answerId(r.checked),AttemptOutcome.CORRECT,r.attempts,r.wrong,r.support)})}
}
