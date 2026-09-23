package com.bellfamily.bastischool.learning.seasons

import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.session.*
import com.bellfamily.bastischool.learning.progress.*
import java.io.*
import java.security.MessageDigest

/** Worker-confined ordering journal: exact state + at most final placement and completion effects. */
class SeasonsOrderHost(private val disk:ProgressStorage,private val progress:ProgressRepository) {
    var state:SeasonsOrderState?=null; private set
    var failure=false; private set
    private var pending=emptyList<ProgressEvent>()
    private var last:ByteArray?=null
    val hasPending get()=pending.isNotEmpty()
    fun open(id:SessionId,seed:Long,language:ContentLanguage):SessionEffect.Narrate? {
        val bytes=disk.access {it.read()};last=bytes
        if(bytes!=null) {load(bytes);flush();return null}
        return start(id,seed,language)
    }
    fun again(id:SessionId,seed:Long,language:ContentLanguage):SessionEffect.Narrate? {
        check(state?.completed==true && state?.acknowledged==true && !failure && !hasPending && id!=state?.id)
        return start(id,seed,language)
    }
    private fun start(id:SessionId,seed:Long,language:ContentLanguage):SessionEffect.Narrate {
        val n=SeasonsOrder.start(id,seed,language);save(n,emptyList());state=n;pending=emptyList();failure=false
        return SessionEffect.Narrate(SeasonsOrder.prompt(n),NarrationKind.INSTRUCTION)
    }
    fun dispatch(action:SeasonsOrderAction):SessionEffect.Narrate? {
        val before=state ?: return null
        if(failure || hasPending) return null
        val next=SeasonsOrder.reduce(before,action)
        if(next.state===before && next.speech==null) return null
        save(next.state,next.events);state=next.state;pending=next.events;flush()
        return next.speech
    }
    fun retryWrites() {val bytes=disk.access {it.read()} ?: throw IOException("Missing ordering checkpoint");load(bytes);last=bytes;flush()}
    private fun flush() {
        failure=false
        for(e in pending) if(progress.append(e) !is ProgressWriteResult.Saved) {failure=true;return}
        val s=state ?: return
        val n=if(s.completed)s.changed(acknowledged=true) else s
        try {save(n,emptyList());state=n;pending=emptyList()} catch(_:IOException){failure=true} catch(_:SecurityException){failure=true}
    }
    private fun save(s:SeasonsOrderState,events:List<ProgressEvent>) {
        val bytes=encode(s,events)
        try {
            disk.access {tx ->
                val actual=tx.read()
                if(!(actual?.contentEquals(last ?: byteArrayOf()) ?: (last==null))) throw IOException("Ordering checkpoint changed")
                tx.replace(bytes)
            }
            last=bytes
        } catch(e:IOException){failure=true;throw e} catch(e:SecurityException){failure=true;throw e}
    }
    private fun load(bytes:ByteArray) {val decoded=decode(bytes);state=decoded.first;pending=decoded.second}
    companion object {
        internal fun encode(s:SeasonsOrderState,events:List<ProgressEvent>):ByteArray {
            val buffer=ByteArrayOutputStream()
            DataOutputStream(buffer).use {o ->
                o.writeInt(0x534F5231);o.writeInt(SeasonsOrder.REVISION);o.writeInt(SeasonsContent.repository.version.schema);o.writeInt(SeasonsContent.repository.version.revision)
                o.writeUTF(s.id.value);o.writeUTF(s.language.name);o.writeBoolean(s.acknowledged)
                s.choices.forEach {o.writeUTF(it.value)}
                s.steps.forEach {p ->
                    o.writeUTF(p.answer.name);o.writeInt(p.attempts);o.writeInt(p.retries)
                    o.writeInt(p.support.replays);o.writeBoolean(p.support.hint);o.writeBoolean(p.support.parentHelp)
                    o.writeUTF(p.lastChoice?.value ?: "")
                }
                val stored=ProgressCodec.encode(events.mapIndexed {i,e->StoredProgressEvent(i+1L,0,e)})
                o.writeInt(stored.size);o.write(stored)
            }
            val payload=buffer.toByteArray();return payload+MessageDigest.getInstance("SHA-256").digest(payload)
        }
        internal fun decode(bytes:ByteArray):Pair<SeasonsOrderState,List<ProgressEvent>> {
            require(bytes.size in 100..32_000)
            val payload=bytes.copyOfRange(0,bytes.size-32)
            require(MessageDigest.isEqual(bytes.takeLast(32).toByteArray(),MessageDigest.getInstance("SHA-256").digest(payload)))
            return DataInputStream(ByteArrayInputStream(payload)).use {i ->
                require(i.readInt()==0x534F5231 && i.readInt()==SeasonsOrder.REVISION && i.readInt()==SeasonsContent.repository.version.schema && i.readInt()==SeasonsContent.repository.version.revision)
                val id=SessionId(i.readUTF());val language=ContentLanguage.valueOf(i.readUTF());val acknowledged=i.bool()
                val choices=List(4){ContentId(i.readUTF())}
                val steps=List(4){TaskProgress(AnswerState.valueOf(i.readUTF()),i.readInt(),i.readInt(),SupportUse(i.readInt(),i.bool(),i.bool()),i.readUTF().takeIf {it.isNotEmpty()}?.let(::ContentId))}
                val s=SeasonsOrderState(id,language,choices,steps,acknowledged)
                val length=i.readInt().also {require(it in 1..20_000)}
                val events=ProgressCodec.decode(ByteArray(length).also {i.readFully(it)}).map {it.event}
                require(i.available()==0 && events.size<=2)
                events.forEach { e -> when(e) {
                    is AttemptEvent -> {
                        val index=e.evidence.task.ordinal-1
                        require(index in 0..3 && (index==s.index || index==s.index-1) && s.steps[index].answer!=AnswerState.UNANSWERED)
                        require(e==SeasonsOrder.attempt(s,index))
                    }
                    is CompletionEvent -> require(s.completed && !s.acknowledged && e==SeasonsOrder.completion(s))
                } }
                require(events.filterIsInstance<AttemptEvent>().size<=1)
                require((s.completed && !s.acknowledged)==events.any {it is CompletionEvent})
                s to events
            }
        }
        private fun DataInputStream.bool()=readUnsignedByte().also {require(it in 0..1)}==1
    }
}
