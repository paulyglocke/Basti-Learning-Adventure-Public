package com.bellfamily.bastischool.learning.wilma

import com.bellfamily.bastischool.learning.content.WeekdayIds
import com.bellfamily.bastischool.learning.models.ContentId
import com.bellfamily.bastischool.learning.progress.ProgressStorage
import java.io.*

data class WilmaSelection(val selected:ContentId=WeekdayIds.MONDAY,val phase:WilmaPhase=WilmaPhase.EXPLORE) {
    init {WilmaContent.day(selected)}
}
class WilmaSelectionStore(private val disk:ProgressStorage) {
    fun read():WilmaSelection=disk.access {tx ->
        val bytes=tx.read() ?: return@access WilmaSelection()
        require(bytes.size in 12..256)
        DataInputStream(ByteArrayInputStream(bytes)).use {i ->
            require(i.readInt()==1)
            val s=WilmaSelection(ContentId(i.readUTF()),WilmaPhase.valueOf(i.readUTF()));require(i.available()==0);s
        }
    }
    fun write(s:WilmaSelection) {
        val buffer=ByteArrayOutputStream()
        DataOutputStream(buffer).use {it.writeInt(1);it.writeUTF(s.selected.value);it.writeUTF(s.phase.name)}
        disk.access {it.replace(buffer.toByteArray())}
    }
}
