package com.bellfamily.bastischool.learning.seasons

import com.bellfamily.bastischool.learning.models.ContentId
import com.bellfamily.bastischool.learning.progress.ProgressStorage
import java.io.*

/** Small local browsing checkpoint, separate from quiz evidence; never emits progress. */
class SeasonsSelectionStore(private val storage: ProgressStorage) {
    fun read(): SeasonsSelection = storage.access { tx ->
        val bytes = tx.read() ?: return@access SeasonsSelection()
        require(bytes.size in 12..256)
        DataInputStream(ByteArrayInputStream(bytes)).use {
            require(it.readInt() == 1)
            val state = SeasonsSelection(ContentId(it.readUTF()), SeasonsPhase.valueOf(it.readUTF()))
            require(it.available() == 0)
            state
        }
    }
    fun write(state: SeasonsSelection) {
        val bytes = ByteArrayOutputStream()
        DataOutputStream(bytes).use { it.writeInt(1); it.writeUTF(state.selected.value); it.writeUTF(state.phase.name) }
        storage.access { it.replace(bytes.toByteArray()) }
    }
}
