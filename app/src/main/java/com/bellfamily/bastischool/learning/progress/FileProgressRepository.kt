package com.bellfamily.bastischool.learning.progress

import java.io.IOException

/** The exclusive transaction covers the entire read/deduplicate/replace operation. */
interface ProgressStorage {
    fun <T> access(block: (ProgressTransaction) -> T): T
}
interface ProgressTransaction {
    fun read(): ByteArray?
    fun replace(bytes: ByteArray)
}

class FileProgressRepository(private val storage: ProgressStorage, private val clock: ProgressClock) : ProgressRepository {
    override fun append(event: ProgressEvent): ProgressWriteResult = try {
        storage.access { transaction ->
            val records = transaction.read()?.let(ProgressCodec::decode).orEmpty()
            if (records.any { it.event.origin.session == event.origin.session && it.event.origin != event.origin })
                throw ProgressProblem(ProgressFailure.CONFLICT)
            val existing = records.firstOrNull { it.event.id == event.id }
            if (existing != null && existing.event != event) throw ProgressProblem(ProgressFailure.CONFLICT)
            val record = existing ?: run {
                if (records.size >= ProgressCodec.MAX_EVENTS) throw ProgressProblem(ProgressFailure.CAPACITY)
                val time = try { clock.nowMillis() } catch (_: RuntimeException) { throw ProgressProblem(ProgressFailure.CLOCK) }
                if (time < 0) throw ProgressProblem(ProgressFailure.CLOCK)
                StoredProgressEvent(records.size + 1L, time, event)
            }
            // Recommit identical bytes on duplicate retry too: confirm durability after an uncertain rename/fsync.
            transaction.replace(ProgressCodec.encode(if (existing == null) records + record else records))
            ProgressWriteResult.Saved(record, inserted = existing == null)
        }
    } catch (error: ProgressProblem) { ProgressWriteResult.Failed(error.reason) }
    catch (_: IOException) { ProgressWriteResult.Failed(ProgressFailure.IO) }
    catch (_: SecurityException) { ProgressWriteResult.Failed(ProgressFailure.IO) }

    override fun read(query: ProgressQuery): ProgressReadResult = try {
        storage.access { transaction ->
            val records = transaction.read()?.let(ProgressCodec::decode).orEmpty()
            val ordered = if (query.newestFirst) records.asReversed() else records
            ProgressReadResult.Events(ordered.asSequence().filter {
                (query.skill == null || it.event.involves(query.skill)) &&
                    (query.session == null || it.event.origin.session == query.session) &&
                    (query.activity == null || it.event.origin.activity == query.activity) &&
                    (query.kind == null || it.event.kind == query.kind)
            }.take(query.limit).toList())
        }
    } catch (error: ProgressProblem) { ProgressReadResult.Failed(error.reason) }
    catch (_: IOException) { ProgressReadResult.Failed(ProgressFailure.IO) }
    catch (_: SecurityException) { ProgressReadResult.Failed(ProgressFailure.IO) }
}
