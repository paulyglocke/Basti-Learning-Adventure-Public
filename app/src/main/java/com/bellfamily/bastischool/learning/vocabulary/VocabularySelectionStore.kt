package com.bellfamily.bastischool.learning.vocabulary

import com.bellfamily.bastischool.learning.models.ContentId
import com.bellfamily.bastischool.learning.progress.ProgressStorage
import java.io.*

/** Small local browsing checkpoint, separate from quiz evidence; never emits progress. */
class VocabularySelectionStore(private val storage: ProgressStorage) {
    fun read(): VocabularySelection = storage.access { tx ->
        val bytes = tx.read() ?: return@access VocabularySelection()
        require(bytes.size in 12..256)
        DataInputStream(ByteArrayInputStream(bytes)).use {
            require(it.readInt() == 1)
            val state = VocabularySelection(ContentId(it.readUTF()), VocabularyPhase.valueOf(it.readUTF()))
            require(it.available() == 0)
            state
        }
    }
    fun write(state: VocabularySelection) {
        val bytes = ByteArrayOutputStream()
        DataOutputStream(bytes).use { it.writeInt(1); it.writeUTF(state.selected.value); it.writeUTF(state.phase.name) }
        storage.access { it.replace(bytes.toByteArray()) }
    }
}
