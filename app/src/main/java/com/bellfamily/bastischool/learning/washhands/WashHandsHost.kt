package com.bellfamily.bastischool.learning.washhands

import com.bellfamily.bastischool.learning.sequencing.*


import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.session.*
import com.bellfamily.bastischool.learning.progress.*
import java.io.*
import java.security.MessageDigest

/** Worker-confined sequence journal: exact state + four checked positions and optional completion. */
class WashHandsHost(private val disk:ProgressStorage,private val progress:ProgressRepository) {
    var state:SequenceState?=null; private set
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
    private fun start(id:SessionId,seed:Long,language:ContentLanguage):SessionEffect.Narrate? {
        val n=WashHands.start(id,seed,language);save(n,emptyList());state=n;pending=emptyList();failure=false
        return null
    }
    fun dispatch(action:SequenceAction):SessionEffect.Narrate? {
        val before=state ?: return null
        if(failure || hasPending) return null
        val next=WashHands.reduce(before,action)
        if(next.state===before && next.speech==null) return null
        save(next.state,next.events);state=next.state;pending=next.events;flush()
        return next.speech
    }
    fun retryWrites() {val bytes=disk.access {it.read()} ?: throw IOException("Missing sequence checkpoint");load(bytes);last=bytes;flush()}
    private fun flush() {
        failure=false
        for(e in pending) if(progress.append(e) !is ProgressWriteResult.Saved) {failure=true;return}
        val s=state ?: return
        val n=if(s.completed)s.changed(acknowledged=true) else s
        try {save(n,emptyList());state=n;pending=emptyList()} catch(_:IOException){failure=true} catch(_:SecurityException){failure=true}
    }
    private fun save(s:SequenceState,events:List<ProgressEvent>) {
        val bytes=encode(s,events)
        try {
            disk.access {tx ->
                val actual=tx.read()
                if(!(actual?.contentEquals(last ?: byteArrayOf()) ?: (last==null))) throw IOException("Sequence checkpoint changed")
                tx.replace(bytes)
            }
            last=bytes
        } catch(e:IOException){failure=true;throw e} catch(e:SecurityException){failure=true;throw e}
    }
    private fun load(bytes:ByteArray) {val decoded=decode(bytes);state=decoded.first;pending=decoded.second}
    fun listen(id:ContentId):SessionEffect.Narrate? = if(!failure && !hasPending && state!=null && id in WashHands.order) WashHands.listen(id) else null
    companion object {
        internal fun encode(s:SequenceState,events:List<ProgressEvent>):ByteArray {
            val buffer=ByteArrayOutputStream()
            DataOutputStream(buffer).use {o ->
                o.writeInt(0x57485331);o.writeInt(WashHands.REVISION);o.writeInt(WashHands.version.schema);o.writeInt(WashHands.version.revision)
                o.writeUTF(s.id.value);o.writeUTF(s.language.name);o.writeBoolean(s.acknowledged)
                o.writeInt(s.revision);o.writeInt(s.attempts)
                o.writeInt(s.support.replays);o.writeBoolean(s.support.hint);o.writeBoolean(s.support.parentHelp)
                o.writeInt(s.hintPosition ?: -1)
                for(list in listOf(s.presentation,s.constructed,s.lastChecked)) {o.writeInt(list.size);list.forEach {o.writeUTF(it.value)}}
                val stored=ProgressCodec.encode(events.mapIndexed {i,e->StoredProgressEvent(i+1L,0,e)})
                o.writeInt(stored.size);o.write(stored)
            }
            val payload=buffer.toByteArray();return payload+MessageDigest.getInstance("SHA-256").digest(payload)
        }
        internal fun decode(bytes:ByteArray):Pair<SequenceState,List<ProgressEvent>> {
            require(bytes.size in 100..32_000)
            val payload=bytes.copyOfRange(0,bytes.size-32)
            require(MessageDigest.isEqual(bytes.takeLast(32).toByteArray(),MessageDigest.getInstance("SHA-256").digest(payload)))
            return DataInputStream(ByteArrayInputStream(payload)).use {i ->
                require(i.readInt()==0x57485331 && i.readInt()==WashHands.REVISION && i.readInt()==WashHands.version.schema && i.readInt()==WashHands.version.revision)
                val id=SessionId(i.readUTF());val language=ContentLanguage.valueOf(i.readUTF());val acknowledged=i.bool()
                val revision=i.readInt();val attempts=i.readInt();val support=SupportUse(i.readInt(),i.bool(),i.bool())
                val hint=i.readInt().also {require(it in -1..3)}.takeIf {it>=0}
                fun ids()=List(i.readInt().also {require(it in 0..4)}){ContentId(i.readUTF())}
                val s=SequenceState(id,language,WashHands.order,ids(),ids(),revision,attempts,ids(),support,hint,acknowledged)
                val length=i.readInt().also {require(it in 1..20_000)}
                val events=ProgressCodec.decode(ByteArray(length).also {i.readFully(it)}).map {it.event}
                require(i.available()==0)
                if(events.isNotEmpty()) {
                    require(!acknowledged && s.constructed==s.lastChecked)
                    require(events==WashHands.attempts(s)+if(s.completed)listOf(WashHands.complete(s)) else emptyList())
                }
                require((s.completed && !s.acknowledged)==events.any {it is CompletionEvent})
                s to events
            }
        }
        private fun DataInputStream.bool()=readUnsignedByte().also {require(it in 0..1)}==1
    }
}
