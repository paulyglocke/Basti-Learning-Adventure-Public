package com.bellfamily.bastischool.learning.progress

import java.io.*

/** Platform supplies same-filesystem atomic replacement and directory fsync; no non-atomic fallback. */
interface AtomicProgressCommit {
    fun replace(source: File, target: File)
    fun syncDirectory(directory: File)
}

/** Shared file policy, also exercised on actual JVM temporary files with an atomic commit adapter. */
class AtomicProgressStorage(private val directory: File, private val commit: AtomicProgressCommit) : ProgressStorage {
    override fun <T> access(block: (ProgressTransaction) -> T): T = synchronized(processLock) {
        if (!directory.isDirectory && !directory.mkdirs()) throw IOException("Cannot create progress directory")
        // Persist a newly created store directory before acknowledging any records in it.
        commit.syncDirectory(directory.parentFile ?: throw IOException("Missing progress parent"))
        RandomAccessFile(File(directory, "events.lock"), "rw").use { lockFile ->
            lockFile.channel.lock().use {
                block(object : ProgressTransaction {
                    private val target = File(directory, "events.bin")
                    override fun read(): ByteArray? {
                        if (!target.exists()) return null
                        FileInputStream(target).use { input ->
                            val result = ByteArrayOutputStream()
                            val buffer = ByteArray(8192)
                            while (true) {
                                val size = input.read(buffer)
                                if (size < 0) break
                                if (result.size() + size > ProgressCodec.MAX_BYTES) throw ProgressProblem(ProgressFailure.CAPACITY)
                                result.write(buffer, 0, size)
                            }
                            return result.toByteArray()
                        }
                    }
                    override fun replace(bytes: ByteArray) {
                        if (bytes.size > ProgressCodec.MAX_BYTES) throw ProgressProblem(ProgressFailure.CAPACITY)
                        val pending = File(directory, "events.pending")
                        FileOutputStream(pending).use { stream -> stream.write(bytes); stream.fd.sync() }
                        commit.replace(pending, target)
                        commit.syncDirectory(directory)
                    }
                })
            }
        }
    }
    companion object {
        // Avoid OverlappingFileLockException between repository instances in this process;
        // the separate OS lock also serializes cooperating processes across data-file replacement.
        private val processLock = Any()
    }
}
