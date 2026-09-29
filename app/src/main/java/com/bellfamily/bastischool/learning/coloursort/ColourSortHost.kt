package com.bellfamily.bastischool.learning.coloursort

import com.bellfamily.bastischool.learning.sorting.*

import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.session.*
import com.bellfamily.bastischool.learning.progress.*
import java.io.*
import java.security.MessageDigest

/** Worker-confined sorting journal: exact state + at most final placement and completion effects. */
class ColourSortHost(private val disk:ProgressStorage,private val progress:ProgressRepository) {
    var state:SortState?=null; private set
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
        val n=ColourSort.start(id,seed,language);save(n,emptyList());state=n;pending=emptyList();failure=false
        return SessionEffect.Narrate(ColourSort.prompt(n),NarrationKind.INSTRUCTION)
    }
    fun dispatch(action:SortAction):SessionEffect.Narrate? {
        val before=state ?: return null
        if(failure || hasPending) return null
        val next=ColourSort.reduce(before,action)
        if(next.state===before && next.speech==null) return null
        save(next.state,next.events);state=next.state;pending=next.events;flush()
        return next.speech
    }
    fun retryWrites() {val bytes=disk.access {it.read()} ?: throw IOException("Missing sorting checkpoint");load(bytes);last=bytes;flush()}
    private fun flush() {
        failure=false
        for(e in pending) if(progress.append(e) !is ProgressWriteResult.Saved) {failure=true;return}
        val s=state ?: return
        val n=if(s.completed)s.changed(acknowledged=true) else s
        try {save(n,emptyList());state=n;pending=emptyList()} catch(_:IOException){failure=true} catch(_:SecurityException){failure=true}
    }
    private fun save(s:SortState,events:List<ProgressEvent>) {
        val bytes=encode(s,events)
        try {
            disk.access {tx ->
                val actual=tx.read()
                if(!(actual?.contentEquals(last ?: byteArrayOf()) ?: (last==null))) throw IOException("Sorting checkpoint changed")
                tx.replace(bytes)
            }
            last=bytes
        } catch(e:IOException){failure=true;throw e} catch(e:SecurityException){failure=true;throw e}
    }
    private fun load(bytes:ByteArray) {val decoded=decode(bytes);state=decoded.first;pending=decoded.second}
    companion object {
        internal fun encode(s:SortState,events:List<ProgressEvent>):ByteArray {
            val buffer=ByteArrayOutputStream()
            DataOutputStream(buffer).use {o ->
                o.writeInt(0x43535231);o.writeInt(ColourSort.REVISION);o.writeInt(ColourSort.version.schema);o.writeInt(ColourSort.version.revision)
                o.writeUTF(s.id.value);o.writeUTF(s.language.name);o.writeBoolean(s.acknowledged)
                o.writeUTF(s.selected?.value ?: "")
                s.order.forEach {o.writeUTF(it.value)}
                s.placements.forEach {p ->
                    o.writeBoolean(p.placed);o.writeInt(p.attempts)
                    o.writeInt(p.support.replays);o.writeBoolean(p.support.hint);o.writeBoolean(p.support.parentHelp)
                    o.writeUTF(p.lastCategory?.value ?: "")
                }
                val stored=ProgressCodec.encode(events.mapIndexed {i,e->StoredProgressEvent(i+1L,0,e)})
                o.writeInt(stored.size);o.write(stored)
            }
            val payload=buffer.toByteArray();return payload+MessageDigest.getInstance("SHA-256").digest(payload)
        }
        internal fun decode(bytes:ByteArray):Pair<SortState,List<ProgressEvent>> {
            require(bytes.size in 100..32_000)
            val payload=bytes.copyOfRange(0,bytes.size-32)
            require(MessageDigest.isEqual(bytes.takeLast(32).toByteArray(),MessageDigest.getInstance("SHA-256").digest(payload)))
            return DataInputStream(ByteArrayInputStream(payload)).use {i ->
                require(i.readInt()==0x43535231 && i.readInt()==ColourSort.REVISION && i.readInt()==ColourSort.version.schema && i.readInt()==ColourSort.version.revision)
                val id=SessionId(i.readUTF());val language=ContentLanguage.valueOf(i.readUTF());val acknowledged=i.bool()
                val selected=i.readUTF().takeIf {it.isNotEmpty()}?.let(::ContentId)
                val order=List(4){ContentId(i.readUTF())}
                val placements=List(4){SortPlacement(i.bool(),i.readInt(),
                    support=SupportUse(i.readInt(),i.bool(),i.bool()),lastCategory=i.readUTF().takeIf {it.isNotEmpty()}?.let(::ContentId))}
                val s=SortState(id,ColourSort.rule,language,order,placements,selected,acknowledged)
                val length=i.readInt().also {require(it in 1..20_000)}
                val events=ProgressCodec.decode(ByteArray(length).also {i.readFully(it)}).map {it.event}
                require(i.available()==0 && events.size<=2)
                events.forEach { e -> when(e) {
                    is AttemptEvent -> {
                        val index=e.evidence.task.ordinal-1
                        require(index in 0..3 && s.placements[index].attempts>0)
                        require(e==ColourSort.attempt(s,index))
                    }
                    is CompletionEvent -> require(s.completed && !s.acknowledged && e==ColourSort.completionEvent(s))
                } }
                require(events.filterIsInstance<AttemptEvent>().size<=1)
                require((s.completed && !s.acknowledged)==events.any {it is CompletionEvent})
                s to events
            }
        }
        private fun DataInputStream.bool()=readUnsignedByte().also {require(it in 0..1)}==1
    }
}
