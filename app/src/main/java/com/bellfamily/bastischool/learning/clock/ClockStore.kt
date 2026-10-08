package com.bellfamily.bastischool.learning.clock

import com.bellfamily.bastischool.learning.models.ContentLanguage
import com.bellfamily.bastischool.learning.progress.ProgressStorage
import java.io.*

data class ClockExploreState(val time: ClockTime = ClockTime(), val language: ContentLanguage = ContentLanguage.ENGLISH)

/** Independent, bounded browsing checkpoint. No invented attempts or mastery evidence. */
class ClockStore(private val disk: ProgressStorage) {
    fun read(): ClockExploreState = disk.access { tx ->
        val bytes = tx.read() ?: return@access ClockExploreState()
        require(bytes.size in 14..64)
        DataInputStream(ByteArrayInputStream(bytes)).use {
            require(it.readInt() == 1)
            val state = ClockExploreState(ClockTime(it.readInt(), it.readInt()), ContentLanguage.valueOf(it.readUTF()))
            require(it.available() == 0)
            state
        }
    }
    fun write(state: ClockExploreState) {
        val bytes = ByteArrayOutputStream()
        DataOutputStream(bytes).use {
            it.writeInt(1); it.writeInt(state.time.hour); it.writeInt(state.time.minute); it.writeUTF(state.language.name)
        }
        disk.access { it.replace(bytes.toByteArray()) }
    }
}
