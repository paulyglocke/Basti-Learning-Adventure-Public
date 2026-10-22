package com.bellfamily.bastischool.learning.colours

import com.bellfamily.bastischool.learning.models.ContentId
import com.bellfamily.bastischool.learning.progress.ProgressStorage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream

/** Minimal browsing state; intentionally separate from assessed Find and existing Colour Sort records. */
class ColoursSelectionStore(private val storage: ProgressStorage) {
    fun read(): ContentId = storage.access { transaction ->
        val bytes = transaction.read() ?: return@access ColoursContent.red
        require(bytes.size in 8..128)
        DataInputStream(ByteArrayInputStream(bytes)).use { input ->
            require(input.readInt() == 1)
            val id = ContentId(input.readUTF())
            require(input.available() == 0)
            ColoursContent.definition(id).id
        }
    }

    fun write(id: ContentId) {
        ColoursContent.definition(id)
        val bytes = ByteArrayOutputStream().also { buffer ->
            DataOutputStream(buffer).use { output -> output.writeInt(1); output.writeUTF(id.value) }
        }.toByteArray()
        storage.access { it.replace(bytes) }
    }
}
