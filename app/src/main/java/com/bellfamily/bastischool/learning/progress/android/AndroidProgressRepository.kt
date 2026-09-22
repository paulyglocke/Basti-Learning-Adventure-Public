package com.bellfamily.bastischool.learning.progress.android

import android.content.Context
import android.system.ErrnoException
import android.system.Os
import android.system.OsConstants
import com.bellfamily.bastischool.learning.progress.*
import java.io.File
import java.io.IOException

object AndroidProgressRepository {
    /** Invoke repository operations off main. No Activity retained, network, backup or legacy import. */
    fun create(context: Context, clock: ProgressClock = ProgressClock { System.currentTimeMillis() }): ProgressRepository =
        FileProgressRepository(AtomicProgressStorage(
            File(context.applicationContext.noBackupFilesDir, "native-progress"), AndroidAtomicCommit), clock)
}

private object AndroidAtomicCommit : AtomicProgressCommit {
    override fun replace(source: File, target: File) {
        try { Os.rename(source.absolutePath, target.absolutePath) }
        catch (error: ErrnoException) { throw IOException("Progress commit failed", error) }
    }
    override fun syncDirectory(directory: File) {
        try {
            val fd = Os.open(directory.absolutePath, OsConstants.O_RDONLY, 0)
            try { Os.fsync(fd) } finally { Os.close(fd) }
        } catch (error: ErrnoException) { throw IOException("Progress directory sync failed", error) }
    }
}
