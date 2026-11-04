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
            val version=it.readInt();require(version in 1..2)
            val selected=ContentId(it.readUTF());val phase=VocabularyPhase.valueOf(it.readUTF())
            val state = VocabularySelection(selected,phase,if(version==1) ContentId("category.animals") else ContentId(it.readUTF()))
            require(it.available() == 0)
            state
        }
    }
    fun write(state: VocabularySelection) {
        val bytes = ByteArrayOutputStream()
        DataOutputStream(bytes).use { it.writeInt(2); it.writeUTF(state.selected.value); it.writeUTF(state.phase.name);it.writeUTF(state.category.value) }
        storage.access { it.replace(bytes.toByteArray()) }
    }
}
