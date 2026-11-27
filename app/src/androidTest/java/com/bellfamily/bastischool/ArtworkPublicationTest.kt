package com.bellfamily.bastischool

import android.os.Looper
import androidx.compose.ui.graphics.ImageBitmap
import com.bellfamily.bastischool.ui.common.loadArtworkForUi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.junit.Assert.*
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference

class ArtworkPublicationTest {
    @Test fun workerCompletionPublishesOnMainEvenForUnconfinedCaller() {
        val loading = CountDownLatch(1)
        val release = CountDownLatch(1)
        val finished = CountDownLatch(1)
        val publishLooper = AtomicReference<Looper?>()
        val loadLooper = AtomicReference<Looper?>()
        val image = ImageBitmap(8, 8)
        val received = AtomicReference<ImageBitmap?>()
        val job = CoroutineScope(Dispatchers.Unconfined).launch {
            try {
                loadArtworkForUi({
                    loadLooper.set(Looper.myLooper()); loading.countDown()
                    check(release.await(5, TimeUnit.SECONDS))
                    image
                }, {
                    publishLooper.set(Looper.myLooper()); received.set(it)
                })
            } finally { finished.countDown() }
        }
        try {
            assertTrue(loading.await(5, TimeUnit.SECONDS))
            release.countDown()
            assertTrue(finished.await(5, TimeUnit.SECONDS))
            assertNotSame(Looper.getMainLooper(), loadLooper.get())
            assertSame(image, received.get())
            assertSame("An IO completion must not publish UI state on its worker", Looper.getMainLooper(), publishLooper.get())
        } finally { release.countDown(); job.cancel() }
    }

    @Test fun cancellationWhileDecodingRejectsTheLatePicture() {
        val loading = CountDownLatch(1)
        val release = CountDownLatch(1)
        val finished = CountDownLatch(1)
        val publications = AtomicInteger()
        val job = CoroutineScope(Dispatchers.Unconfined).launch {
            try {
                loadArtworkForUi({
                    loading.countDown(); check(release.await(5, TimeUnit.SECONDS)); ImageBitmap(8, 8)
                }, { publications.incrementAndGet() })
            } finally { finished.countDown() }
        }
        try {
            assertTrue(loading.await(5, TimeUnit.SECONDS))
            job.cancel(); release.countDown()
            assertTrue(finished.await(5, TimeUnit.SECONDS))
            assertEquals(0, publications.get())
        } finally { release.countDown(); job.cancel() }
    }
}
