package com.bellfamily.bastischool.learning.months

import com.bellfamily.bastischool.learning.models.ContentId
import com.bellfamily.bastischool.learning.progress.ProgressStorage
import java.io.*

/** Independent browsing state. Language/audio remain shell settings; no learning events. */
class MonthsSelectionStore(private val storage: ProgressStorage) {
    fun read(): MonthsSelection = storage.access { tx ->
        val bytes = tx.read() ?: return@access MonthsSelection()
        require(bytes.size in 8..128)
        DataInputStream(ByteArrayInputStream(bytes)).use {
            require(it.readInt() == 1)
            val result = MonthsSelection(ContentId(it.readUTF()))
            require(it.available() == 0)
            result
        }
    }
    fun write(state: MonthsSelection) {
        val bytes = ByteArrayOutputStream()
        DataOutputStream(bytes).use { it.writeInt(1); it.writeUTF(state.selected.value) }
        storage.access { it.replace(bytes.toByteArray()) }
    }
}
