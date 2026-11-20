package com.bellfamily.bastischool.learning.numberstories

import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.session.*
import com.bellfamily.bastischool.learning.progress.*
import com.bellfamily.bastischool.learning.bonds.NumberBonds
import com.bellfamily.bastischool.learning.vocabulary.VocabularyContent
import com.bellfamily.bastischool.learning.quantity.QuantityWork
import java.util.Collections
import java.util.Random
import java.util.Locale

enum class StoryMode(val key:String,val delta:Int,val title:ContentText) {
    ADD("add",1,ContentText.plain("More arrive","Es kommen welche dazu")),
    TAKE_AWAY("take_away",-1,ContentText.plain("Some leave","Es gehen welche weg"))
}
enum class StoryRange(val key:String,val limit:Int) { FIVE("five",5),TEN("ten",10) }
data class StoryConfig(val mode:StoryMode,val range:StoryRange) {
    val key get()="${mode.key}-${range.key}"
    companion object { val all=StoryMode.entries.flatMap{m->StoryRange.entries.map{StoryConfig(m,it)}} }
}
enum class StoryScene(val key:String,val objectId:ContentId,val title:ContentText,val plural:ContentText) {
    POND("pond",ContentId("animal.dinosaur"),ContentText.plain("By the pond","Am Teich"),ContentText.plain("dinosaurs","Dinosaurier")),
    PICNIC("picnic",ContentId("food.apple"),ContentText.plain("At the picnic","Beim Picknick"),ContentText.plain("apples","Äpfel"));
    fun noun(count:Int,language:ContentLanguage):String = if(count!=1)plural.display[language] else VocabularyContent.item(objectId).text.display[language].let{if(language==ContentLanguage.ENGLISH)it.lowercase(Locale.ROOT)else it}
}
data class StoryTask(val start:Int,val amount:Int,val scene:StoryScene)

/** Structured situations, editable groups and explicit checked evidence. */
object NumberStories {
    const val REVISION=1
    val version=ContentVersion(1,1)
    val title=ContentText.plain("Number Stories","Rechengeschichten")
    val complete=ContentText.plain("You solved every story!","Du hast alle Geschichten gelöst!")
    fun initialMask(task:StoryTask)=QuantityWork.filled(task.start)
    fun target(task:StoryTask,config:StoryConfig)=task.start+config.mode.delta*task.amount
    fun valid(t:StoryTask,c:StoryConfig)=t.start in 1..c.range.limit&&t.amount in 1..5&&target(t,c) in 0..c.range.limit
    fun story(t:StoryTask,c:StoryConfig):ContentText {
        val enStart=if(t.scene==StoryScene.POND)"${t.start} ${t.scene.noun(t.start,ContentLanguage.ENGLISH)} ${if(t.start==1)"is"else"are"} by the pond."
            else "There ${if(t.start==1)"is"else"are"} ${t.start} ${t.scene.noun(t.start,ContentLanguage.ENGLISH)} on the picnic blanket."
        val deStart=if(t.scene==StoryScene.POND)"Am Teich ${if(t.start==1)"ist"else"sind"} ${t.start} ${t.scene.noun(t.start,ContentLanguage.GERMAN)}."
            else "Auf der Picknickdecke ${if(t.start==1)"liegt"else"liegen"} ${t.start} ${t.scene.noun(t.start,ContentLanguage.GERMAN)}."
        val enChange=if(c.mode==StoryMode.ADD) {
            if(t.scene==StoryScene.POND)"${t.amount} more ${t.scene.noun(t.amount,ContentLanguage.ENGLISH)} ${if(t.amount==1)"comes"else"come"} to the pond."
            else "You put ${t.amount} more ${t.scene.noun(t.amount,ContentLanguage.ENGLISH)} on the blanket."
        }else if(t.scene==StoryScene.POND)"${t.amount} ${t.scene.noun(t.amount,ContentLanguage.ENGLISH)} ${if(t.amount==1)"walks"else"walk"} away."
            else "You take ${t.amount} ${t.scene.noun(t.amount,ContentLanguage.ENGLISH)} off the blanket."
        val deChange=if(c.mode==StoryMode.ADD) {
            if(t.scene==StoryScene.POND)"${t.amount} ${t.scene.noun(t.amount,ContentLanguage.GERMAN)} ${if(t.amount==1)"kommt"else"kommen"} dazu."
            else "Du legst ${t.amount} ${t.scene.noun(t.amount,ContentLanguage.GERMAN)} dazu."
        }else if(t.scene==StoryScene.POND)"${t.amount} ${t.scene.noun(t.amount,ContentLanguage.GERMAN)} ${if(t.amount==1)"geht"else"gehen"} weg."
            else "Du nimmst ${t.amount} ${t.scene.noun(t.amount,ContentLanguage.GERMAN)} von der Decke."
        return ContentText.plain("$enStart $enChange","$deStart $deChange")
    }
    fun prompt(t:StoryTask,c:StoryConfig)=story(t,c).let{ContentText.plain("${it.speech.en} How many are there now? Change your group, then press Check.","${it.speech.de} Wie viele sind es jetzt? Verändere deine Gruppe. Tippe dann auf Prüfen.")}
    fun change(t:StoryTask,c:StoryConfig)=ContentText.plain(
        if(c.mode==StoryMode.ADD)"${t.amount} more"else"${t.amount} away",
        if(c.mode==StoryMode.ADD)"${t.amount} dazu"else"${t.amount} weg")
    fun hint(t:StoryTask,c:StoryConfig)=ContentText.plain(
        "Start with ${t.start}. ${if(c.mode==StoryMode.ADD)"Tap ${t.amount} empty spaces to add more."else"Tap ${t.amount} objects to take them away."} Count your new group. None left means zero. You can undo each move.",
        "Beginne mit ${t.start}. ${if(c.mode==StoryMode.ADD)"Tippe auf ${t.amount} leere Felder und lege welche dazu."else"Tippe auf ${t.amount} Bilder und nimm sie weg."} Zähle deine neue Gruppe. Keins übrig bedeutet null. Du kannst jeden Schritt rückgängig machen.")
    fun feedback(s:State)=if(!s.current.solved)ContentText.plain(
        "Look at the start and listen to the story again. ${if(s.config.mode==StoryMode.ADD)"Add"else"Take away"} ${s.task.amount}. You can change your group and check again.",
        "Schau auf den Anfang und höre die Geschichte noch einmal. ${if(s.config.mode==StoryMode.ADD)"Lege ${s.task.amount} dazu."else"Nimm ${s.task.amount} weg."} Du kannst deine Gruppe ändern und erneut prüfen.")
        else ContentText.plain("${s.task.start} ${if(s.config.mode==StoryMode.ADD)"plus"else"take away"} ${s.task.amount} ${if(s.answer==0)"leaves none: zero!"else"makes ${s.answer}!"}",
            "${s.task.start} ${if(s.config.mode==StoryMode.ADD)"plus"else"minus"} ${s.task.amount} sind ${s.answer}${if(s.answer==0)". Keins übrig: null!"else"!"}")
    fun activity(c:StoryConfig)=ActivityId("activity.math.number_stories.${c.mode.key}.${c.range.key}")
    fun skill(c:StoryConfig)=SkillId("skill.math.number_stories.${c.mode.key}.within_${c.range.limit}")
    fun context(t:StoryTask)=LearningContextId("context.math.number_stories.${t.scene.key}.${t.scene.objectId.value.substringAfter('.')}")

    /** Bounded seeded search: unique arithmetic, alternating settings and no adjacent repeated result. */
    fun generate(config:StoryConfig,round:RoundLength,seed:Long,previous:List<StoryTask> = emptyList()):List<StoryTask> {
        val random=Random(seed)
        val facts=(1..config.range.limit).flatMap{start->(1..5).map{start to it}}.filter{(n,a)->valid(StoryTask(n,a,StoryScene.POND),config)}
            .toMutableList().also{Collections.shuffle(it,random)}
        val selected=mutableListOf<Pair<Int,Int>>()
        var budget=100_000
        fun answer(f:Pair<Int,Int>)=f.first+config.mode.delta*f.second
        fun search():Boolean {
            if(--budget<0)return false
            if(selected.size==round.count)return selected.count{it.second>1}>=round.count/2&&selected.any{answer(it)==if(config.mode==StoryMode.ADD)config.range.limit else 0}
            for(f in facts) {
                if(f in selected||answer(f)==selected.lastOrNull()?.let(::answer))continue
                if(selected.isEmpty()&&previous.isNotEmpty()&&(f==previous.first().let{it.start to it.amount}||f==previous.last().let{it.start to it.amount}||answer(f)==target(previous.last(),config)))continue
                selected+=f
                if(search())return true
                selected.removeAt(selected.lastIndex)
            }
            return false
        }
        check(search()){ "No varied story round within search budget" }
        val firstScene=previous.lastOrNull()?.scene?.let{if(it==StoryScene.POND)StoryScene.PICNIC else StoryScene.POND}?:StoryScene.entries[random.nextInt(2)]
        return Collections.unmodifiableList(selected.mapIndexed{i,(n,a)->StoryTask(n,a,if(i%2==0)firstScene else if(firstScene==StoryScene.POND)StoryScene.PICNIC else StoryScene.POND)})
    }
    data class Result(val attempts:Int=0,val wrong:Int=0,val solved:Boolean=false,val checkedCount:Int?=null,val support:SupportUse=SupportUse()) {
        init {
            require(attempts in 0..10000&&wrong in 0..attempts&&attempts==wrong+if(solved)1 else 0)
            require((attempts==0)==(checkedCount==null));require(checkedCount==null||checkedCount in 0..10)
        }
    }
    data class State(val id:SessionId,val config:StoryConfig,val tasks:List<StoryTask>,val round:RoundLength,val language:ContentLanguage,
        val index:Int=0,val mask:Int=initialMask(tasks.first()),val undoMasks:List<Int> = emptyList(),
        val results:List<Result> = List(tasks.size){Result()},val completed:Boolean=false,val acknowledged:Boolean=false) {
        val task get()=tasks[index]
        val current get()=results[index]
        val count get()=Integer.bitCount(mask)
        val answer get()=target(task,config)
        val needsCorrection get()=current.attempts>0&&!current.solved&&count==current.checkedCount
        init {
            require(tasks.size==round.count&&tasks.all{valid(it,config)})
            require(tasks.map{it.start to it.amount}.distinct().size==tasks.size)
            require(tasks.zipWithNext().all{(a,b)->target(a,config)!=target(b,config)&&a.scene!=b.scene})
            require(tasks.count{it.amount>1}>=round.count/2)
            require(tasks.any{target(it,config)==if(config.mode==StoryMode.ADD)config.range.limit else 0})
            require(index in tasks.indices&&results.size==tasks.size)
            QuantityWork(mask,undoMasks).validate(config.range.limit)
            require(results.take(index).all{it.solved}&&results.drop(index+1).all{it==Result()})
            results.forEachIndexed{i,r->r.checkedCount?.let{require(it in 0..config.range.limit);require(r.solved==(it==target(tasks[i],config)))}}
            require(!current.solved||count==answer)
            require(!completed||index==tasks.lastIndex&&current.solved);require(!acknowledged||completed)
        }
    }
    fun start(id:SessionId,config:StoryConfig,round:RoundLength,seed:Long,language:ContentLanguage,previous:List<StoryTask> = emptyList())=
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
            is Action.Toggle->if(s.current.solved||a.slot !in 0 until s.config.range.limit)s else QuantityWork(s.mask,s.undoMasks).toggle(a.slot,s.config.range.limit).let{s.copy(mask=it.mask,undoMasks=it.undo)}
            Action.Undo->if(s.current.solved||s.undoMasks.isEmpty())s else QuantityWork(s.mask,s.undoMasks).undo().let{s.copy(mask=it.mask,undoMasks=it.undo)}
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
    private fun evidence(s:State,i:Int):TaskEvidence {
        val t=s.tasks[i]
        return TaskEvidence(TaskInstanceId(s.id,i+1),TaskDefinitionId("task.math.number_stories.${s.config.mode.key}.${s.config.range.key}.${t.scene.key}.start_${t.start}.change_${t.amount}"),skill(s.config),context(t),if(s.config.range==StoryRange.FIVE)1 else 2)
    }
    fun attempt(s:State):AttemptEvent {val r=s.current;return AttemptEvent(origin(s),evidence(s,s.index),AttemptId(TaskInstanceId(s.id,s.index+1),r.attempts),s.language,NumberBonds.countId(requireNotNull(r.checkedCount)),if(r.solved)AttemptOutcome.CORRECT else AttemptOutcome.INCORRECT,r.support)}
    fun completion(s:State):CompletionEvent {require(s.completed);return CompletionEvent(origin(s),s.results.mapIndexed{i,r->CompletedTask(evidence(s,i),NumberBonds.countId(target(s.tasks[i],s.config)),AttemptOutcome.CORRECT,r.attempts,r.wrong,r.support)})}
}
