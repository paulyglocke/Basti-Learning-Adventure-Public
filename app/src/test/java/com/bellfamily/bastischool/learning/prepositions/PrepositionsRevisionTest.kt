package com.bellfamily.bastischool.learning.prepositions

import com.bellfamily.bastischool.learning.content.BundledContentRepository
import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.progress.*
import com.bellfamily.bastischool.learning.session.*
import java.io.IOException
import org.junit.Assert.*
import org.junit.Test

/** Frozen v1 authoring fixture: no calls to the new question builder or generator. */
class PrepositionsRevisionTest {
    private val words=listOf("on|auf|on the rock|auf dem Stein|rock", "under|unter|under the table|unter dem Tisch|table",
        "behind|hinter|behind the rock|hinter dem Stein|rock", "next_to|neben|next to the rock|neben dem Stein|rock",
        "in|in|in the box|in der Kiste|box", "between|zwischen|between the two rocks|zwischen den beiden Steinen|rock").map {it.split('|')}
    private val content=BundledContentRepository(ContentVersion(1,1),words.map {
        PrepositionDefinition(ContentId("position.${it[0]}"),ContentText.plain(it[0].replace('_',' '),it[1]))
    },emptyList())
    private fun plan(id:SessionId,round:RoundLength,seed:Long):SessionPlan {
        val random=java.util.Random(seed)
        val animals=listOf("snake|the snake|die Schlange","dinosaur|the dinosaur|der Dinosaurier","dragon|the dragon|der Drache","crocodile|the crocodile|das Krokodil").map {it.split('|')}
        fun <T> shuffle(values:List<T>)=values.toMutableList().apply {java.util.Collections.shuffle(this,random)}
        val tasks=shuffle(animals.flatMap {a->words.map {a to it}}).take(round.count).mapIndexed {i,(a,r)->
            val choices=shuffle(listOf(r)+shuffle(words.filter {it!=r}).take(3)).map {ContentId("position.${it[0]}")}
            val names=choices.map {content.find(it)!!.text}
            ChoiceTask(TaskInstanceId(id,i+1),ChoiceQuestion(TaskDefinitionId("task.prepositions.${a[0]}.${r[0]}"),
                SkillId("skill.spatial.${r[0]}"),LearningContextId("context.prepositions.${a[0]}.${r[4]}"),1,
                ContentText.plain("Where is ${a[1]}? Look at the picture. Choose: ${names.joinToString(", "){it.speech.en}}.",
                    "Wo ist ${a[2]}? Schau dir das Bild an. Wähle: ${names.joinToString(", "){it.speech.de}}."),choices,ContentId("position.${r[0]}"),
                ContentText.plain("Great! ${a[1].replaceFirstChar {it.uppercase()}} is ${r[2]}.","Super! ${a[2].replaceFirstChar {it.uppercase()}} ist ${r[3]}."),
                ContentText.plain("Try again!","Nochmal versuchen!"),ContentText.plain(r[2],r[3])))
        }
        return SessionPlan(id,PrepositionsContent.activity,1,content.version,SessionPolicy(round),tasks,ContentText.plain("Adventure complete! Well done!","Abenteuer geschafft! Sehr gut!"))
    }
    private class Memory:ProgressStorage {
        var bytes:ByteArray?=null
        override fun <T> access(block:(ProgressTransaction)->T):T=block(object:ProgressTransaction {
            override fun read()=bytes?.clone()
            override fun replace(bytes:ByteArray) {this@Memory.bytes=bytes.clone()}
        })
    }
    private val records=mutableListOf<ProgressEvent>()
    private var fail=false
    private val progress=object:ProgressRepository {
        override fun append(event:ProgressEvent):ProgressWriteResult {
            if(fail)return ProgressWriteResult.Failed(ProgressFailure.IO)
            if(event !in records)records+=event
            return ProgressWriteResult.Saved(StoredProgressEvent(records.indexOf(event)+1L,0,event),true)
        }
        override fun read(query:ProgressQuery)=ProgressReadResult.Events(emptyList())
    }
    private fun old(disk:Memory)=DurableSessionHost(disk,progress,PrepositionsContent.activity,1,content,
        {id,round,seed->GenerationResult.Generated(plan(id,round,seed))},{})
    private fun restore(disk:Memory)=PrepositionsHost(disk,progress).also {
        assertTrue(it.open(SessionId("ignored"),RoundLength.TEN,999,ContentLanguage.GERMAN).isEmpty())
    }
    private fun answer(host:DurableSessionHost,correct:Boolean=true) {
        val s=host.state!!;host.dispatch(SessionAction.Answer(s.nextAttempt!!,if(correct)s.task.question.correct else s.task.question.choices.first {it!=s.task.question.correct}))
    }
    @Test fun allOriginalSceneDefinitionsRestoreAgainstFrozenV1Authoring() {
        val seen=mutableSetOf<TaskDefinitionId>()
        for(seed in 0L..50L) {
            val state=SessionReducer.start(plan(SessionId("old-$seed"),RoundLength.TEN,seed),ContentLanguage.ENGLISH,content).state
            seen+=state.plan.tasks.map {it.question.definition}
            val bytes=SessionCheckpoint.encode(state)
            val restored=PrepositionsContent.restore(bytes) as SessionRestoreResult.Restored
            assertArrayEquals(bytes,SessionCheckpoint.encode(restored.state))
        }
        assertEquals(24,seen.size)
    }
    @Test fun validChecksummedButIncompatibleJournalIsNeverOverwritten() {
        val disk=Memory()
        val incompatible=DurableSessionHost(disk,progress,PrepositionsContent.activity,99,content,{id,round,seed ->
            val old=plan(id,round,seed)
            GenerationResult.Generated(SessionPlan(old.id,old.activity,99,old.contentVersion,old.policy,old.tasks,old.completionText))
        },{})
        incompatible.open(SessionId("future"),RoundLength.FIVE,1,ContentLanguage.ENGLISH)
        val saved=disk.bytes!!.clone()
        assertThrows(IOException::class.java) {restore(disk)}
        assertArrayEquals(saved,disk.bytes);assertTrue(records.isEmpty())
    }
    @Test fun originalV1UnansweredAndRetrySnapshotsRestoreByteExactly() {
        val disk=Memory();val old=old(disk);old.open(SessionId("legacy"),RoundLength.TEN,42,ContentLanguage.ENGLISH)
        val before=disk.bytes!!.clone()
        assertArrayEquals(SessionCheckpoint.encode(old.state!!),SessionCheckpoint.encode(restore(disk).state!!))
        assertArrayEquals(before,disk.bytes)
        old.dispatch(SessionAction.Hint(old.state!!.task.id));answer(old,false)
        old.dispatch(SessionAction.Retry(AttemptId(old.state!!.task.id,1)))
        old.dispatch(SessionAction.Replay(old.state!!.task.id));old.dispatch(SessionAction.Language(ContentLanguage.GERMAN))
        assertArrayEquals(SessionCheckpoint.encode(old.state!!),SessionCheckpoint.encode(restore(disk).state!!))
        assertEquals(1,restore(disk).state!!.plan.activityRevision)
    }
    @Test fun oldAnsweredStateAndPendingAttemptRetainVersionIdentityAndDeliverOnce() {
        val disk=Memory();val old=old(disk);old.open(SessionId("legacy-attempt"),RoundLength.FIVE,1,ContentLanguage.ENGLISH)
        fail=true;answer(old);val snapshot=SessionCheckpoint.encode(old.state!!)
        fail=false;val resumed=restore(disk)
        assertArrayEquals(snapshot,SessionCheckpoint.encode(resumed.state!!))
        assertEquals(1,resumed.state!!.score);restore(disk);assertEquals(1,records.size)
        resumed.dispatch(SessionAction.Next(resumed.state!!.task.id))
        assertEquals(old.state!!.plan.tasks[1].id,resumed.state!!.task.id)
        assertEquals(old.state!!.plan.tasks[1].question.choices,resumed.state!!.task.question.choices)
    }
    @Test fun oldPendingCompletionDeduplicatesThenPlayAgainStartsV2() {
        val disk=Memory();val old=old(disk);old.open(SessionId("legacy-complete"),RoundLength.FIVE,3,ContentLanguage.GERMAN)
        repeat(5) {answer(old);if(it==4)fail=true;old.dispatch(SessionAction.Next(old.state!!.task.id))}
        fail=false;val resumed=restore(disk)
        assertEquals(CompletionState.ACKNOWLEDGED,resumed.state!!.completion)
        assertEquals(1,resumed.state!!.plan.activityRevision);val completed=SessionCheckpoint.encode(resumed.state!!)
        assertArrayEquals(completed,SessionCheckpoint.encode(restore(disk).state!!))
        assertEquals(1,records.filterIsInstance<CompletionEvent>().size)
        resumed.newRound(SessionId("new-v2"),RoundLength.TEN,7,ContentLanguage.GERMAN)
        assertEquals(2,resumed.state!!.plan.activityRevision);assertEquals(ContentVersion(1,2),resumed.state!!.plan.contentVersion)
        assertArrayEquals(SessionCheckpoint.encode(resumed.state!!),SessionCheckpoint.encode(restore(disk).state!!))
    }
    @Test fun v1DoesNotAcceptNewRelationsOrModifiedAuthoredTextAndBadJournalIsUntouched() {
        val original=plan(SessionId("invalid"),RoundLength.FIVE,1)
        fun encode(q:ChoiceQuestion,version:ContentVersion=content.version):ByteArray {
            val p=SessionPlan(original.id,original.activity,1,version,original.policy,
                original.tasks.mapIndexed {i,t->if(i==0)ChoiceTask(t.id,q)else t},original.completionText)
            return SessionCheckpoint.encode(SessionReducer.start(p,ContentLanguage.ENGLISH,
                if(version==content.version) content else PrepositionsContent.repository).state)
        }
        val q=original.tasks.first().question
        val changed=ChoiceQuestion(q.definition,q.skill,q.context,q.difficulty,ContentText.plain("Changed","Geändert"),q.choices,q.correct,q.correctFeedback,q.wrongFeedback,q.hint)
        assertTrue(PrepositionsContent.restore(encode(changed)) is SessionRestoreResult.Rejected)
        val v2=PrepositionsContentTest.plan().tasks.first().question
        assertTrue(PrepositionsContent.restore(encode(v2,ContentVersion(1,2))) is SessionRestoreResult.Rejected)
        // A forged v1 revision with an old content version and a new relation cannot pass reference validation.
        val newScene=PrepositionsContent.scenes.first {it.relation==PositionRelation.ABOVE}
        val newQuestion=PrepositionsContent.question(newScene,listOf("above","below","behind","in_front_of").map {ContentId("position.$it")})
        val forged=SessionPlan(original.id,original.activity,1,content.version,original.policy,
            original.tasks.mapIndexed {i,t->if(i==0)ChoiceTask(t.id,newQuestion)else t},original.completionText)
        val permissive=object:com.bellfamily.bastischool.learning.content.ContentRepository by PrepositionsContent.repository {override val version=content.version}
        val bytes=SessionCheckpoint.encode(SessionReducer.start(forged,ContentLanguage.ENGLISH,permissive).state)
        assertTrue(PrepositionsContent.restore(bytes) is SessionRestoreResult.Rejected)
        val disk=Memory();disk.bytes=byteArrayOf(1,2,3);val saved=disk.bytes!!.clone()
        assertThrows(IllegalArgumentException::class.java) {restore(disk)};assertArrayEquals(saved,disk.bytes)
    }
}
