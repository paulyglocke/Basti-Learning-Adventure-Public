package com.bellfamily.bastischool.ui.habitats

import android.app.Application
import android.graphics.BitmapFactory
import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.AndroidViewModel
import com.bellfamily.bastischool.audio.*
import com.bellfamily.bastischool.audio.android.AndroidSystemSpeechEngine
import com.bellfamily.bastischool.learning.animals.HabitatMatchingContent
import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.progress.AtomicProgressStorage
import com.bellfamily.bastischool.learning.progress.android.AndroidAtomicCommit
import com.bellfamily.bastischool.learning.progress.android.AndroidProgressRepository
import com.bellfamily.bastischool.learning.session.*
import java.io.File
import java.util.UUID
import java.util.concurrent.Executors

data class HabitatMatchArtwork(val habitats: Map<ContentId, ImageBitmap>, val animals: Map<ContentId, ImageBitmap>)

class HabitatMatchingViewModel @JvmOverloads constructor(
    application: Application,
    engineFactory: () -> SpeechEngine = { AndroidSystemSpeechEngine(application) }
) : AndroidViewModel(application) {
    var state by mutableStateOf<SessionState?>(null); private set
    var habitatImages by mutableStateOf<Map<ContentId, ImageBitmap>>(emptyMap()); private set
    var animalImages by mutableStateOf<Map<ContentId, ImageBitmap>>(emptyMap()); private set
    var busy by mutableStateOf(false); private set
    var saveFailed by mutableStateOf(false); private set
    var audioFailed by mutableStateOf(false); private set
    private val app = application
    private val main = Handler(Looper.getMainLooper())
    private val worker = Executors.newSingleThreadExecutor()
    private val host = HabitatMatchingContent.host(
        AtomicProgressStorage(File(application.noBackupFilesDir, "animal-habitat-match-session"), AndroidAtomicCommit),
        AndroidProgressRepository.create(application))
    private val audio = DefaultAudioController(engineFactory, AudioMode.OFF)
    private val owner = SpeechOwner("native-habitat-matching")
    private var language = ContentLanguage.ENGLISH
    private var mode = AudioMode.OFF
    private var round = RoundLength.FIVE
    private var visible = false
    private var closed = false
    private var epoch = 0L

    fun configure(lang: String, audioMode: String, count: Int) {
        val nextLanguage = if (lang == "de") ContentLanguage.GERMAN else ContentLanguage.ENGLISH
        val nextMode = when (audioMode) { "all" -> AudioMode.ALL; "questions" -> AudioMode.QUESTIONS; else -> AudioMode.OFF }
        val changed = language != nextLanguage || mode != nextMode
        language = nextLanguage; mode = nextMode; round = if (count == 10) RoundLength.TEN else RoundLength.FIVE
        if (changed) {
            epoch++; audio.cancelOwner(owner); audio.setMode(mode)
            if (visible && !busy && host.state?.language != language) work { host.dispatch(SessionAction.Language(language)) }
        }
    }

    fun setVisible(value: Boolean) {
        if (visible == value) return
        visible = value; epoch++
        if (!value) audio.cancelOwner(owner)
        if (value) {
            loadArtwork()
            if (state == null && !busy) work { host.open(SessionId(UUID.randomUUID().toString()), round, System.nanoTime(), language) }
            else if (!busy && state?.language != language) work { host.dispatch(SessionAction.Language(language)) }
        }
    }

    fun action(action: SessionAction) {
        if (visible && !busy && !saveFailed && state?.language == language) {
            epoch++; audio.cancelOwner(owner); work { host.dispatch(action) }
        }
    }
    fun replay() = state?.let { action(SessionAction.Replay(it.task.id)) }
    fun help() = state?.let { action(SessionAction.Hint(it.task.id)) }
    fun again() {
        if (visible && state?.phase == SessionPhase.COMPLETED && !busy) {
            epoch++; audio.cancelOwner(owner)
            work { host.newRound(SessionId(UUID.randomUUID().toString()), round, System.nanoTime(), language) }
        }
    }
    fun retry() { if (!busy) { epoch++; work { host.retryWrites(); emptyList() } } }

    private var artworkLoading = false
    private fun loadArtwork() {
        if (artworkLoading || (habitatImages.size == HabitatMatchingContent.habitats.size && animalImages.size == HabitatMatchingContent.animals.size)) return
        artworkLoading = true
        worker.execute {
            val loaded = runCatching {
                val scenes = HabitatMatchingContent.habitats.mapNotNull { item ->
                    decode(item.artwork, 360)?.let { item.id to it }
                }.toMap()
                val animals = HabitatMatchingContent.animals.mapNotNull { item ->
                    decode(item.artwork, 320)?.let { item.id to it }
                }.toMap()
                HabitatMatchArtwork(scenes, animals)
            }
            main.post {
                artworkLoading = false
                loaded.getOrNull()?.let { habitatImages = it.habitats; animalImages = it.animals }
            }
        }
    }
    private fun decode(path: String, bound: Int): ImageBitmap? = app.assets.open(path).use { input ->
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeStream(input, null, options)
        if (options.outWidth <= 0 || options.outHeight <= 0) return null
        var sample = 1
        while (options.outWidth / (sample * 2) >= bound && options.outHeight / (sample * 2) >= bound / 2) sample *= 2
        app.assets.open(path).use { bitmapInput ->
            BitmapFactory.decodeStream(bitmapInput, null, BitmapFactory.Options().apply { inSampleSize = sample })?.asImageBitmap()
        }
    }

    private fun work(operation: () -> List<SessionEffect>) {
        if (busy || closed) return
        busy = true; val ticket = epoch
        worker.execute {
            var failed = false
            val effects = try { operation() } catch (_: Exception) { failed = true; emptyList() }
            val current = host.state; val pending = host.failure
            main.post {
                if (!closed) {
                    busy = false; saveFailed = failed || pending
                    if (!failed) state = current
                    if (!failed && visible && ticket == epoch && current?.language == language) effects.forEach { effect ->
                        if (effect is SessionEffect.Narrate) {
                            val kind = when (effect.kind) {
                                NarrationKind.FEEDBACK -> SpeechKind.FEEDBACK
                                NarrationKind.COMPLETION -> SpeechKind.COMPLETION
                                else -> SpeechKind.INSTRUCTION
                            }
                            val context = audio.openContext(owner, SpeechSessionId(current.plan.id.value), language)
                            audio.speak(SpeechRequest.fromContent(context, effect.text, kind, effect.trigger)) {
                                if (!closed && visible && ticket == epoch) audioFailed = it.result is SpeechResult.Failed
                            }
                        }
                    }
                    if (!failed && visible && current == null) work {
                        host.open(SessionId(UUID.randomUUID().toString()), round, System.nanoTime(), language)
                    }
                }
            }
        }
    }

    override fun onCleared() {
        closed = true; audio.cancelOwner(owner); audio.close(); worker.shutdown(); super.onCleared()
    }
}
