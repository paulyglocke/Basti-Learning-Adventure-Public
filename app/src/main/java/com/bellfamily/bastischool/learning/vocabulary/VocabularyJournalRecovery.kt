package com.bellfamily.bastischool.learning.vocabulary

import com.bellfamily.bastischool.learning.progress.ProgressStorage
import com.bellfamily.bastischool.learning.progress.ProgressTransaction

/** Current journals win. Legacy bytes are only read when current is absent.
 * The session host validates before writing its recovered state to current storage;
 * invalid or uncertain records are never replaced with an unrelated new round.
 * Both original stores are retained, and legacy is never written through this adapter.
 */
class VocabularyJournalRecovery(private val current: ProgressStorage, private val legacy: ProgressStorage) : ProgressStorage {
    override fun <T> access(block: (ProgressTransaction) -> T): T = current.access { transaction ->
        block(object : ProgressTransaction {
            override fun read(): ByteArray? = transaction.read() ?: legacy.access { it.read() }
            override fun replace(bytes: ByteArray) = transaction.replace(bytes)
        })
    }
}
