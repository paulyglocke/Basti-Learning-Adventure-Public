package com.bellfamily.bastischool.ui.clock

import android.app.Application
import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.*
import androidx.lifecycle.AndroidViewModel
import com.bellfamily.bastischool.audio.*
import com.bellfamily.bastischool.audio.android.AndroidSystemSpeechEngine
import com.bellfamily.bastischool.learning.clock.*
import com.bellfamily.bastischool.learning.models.ContentLanguage
import com.bellfamily.bastischool.learning.progress.AtomicProgressStorage
import com.bellfamily.bastischool.learning.progress.android.AndroidAtomicCommit
import com.bellfamily.bastischool.learning.progress.android.AndroidProgressRepository
import com.bellfamily.bastischool.learning.session.SessionId
import java.util.UUID
import java.io.File
import java.util.concurrent.Executors

class ClockViewModel @JvmOverloads constructor(
    application: Application,
    engineFactory: () -> SpeechEngine = { AndroidSystemSpeechEngine(application) }
) : AndroidViewModel(application) {
    var state by mutableStateOf<ClockExploreState?>(null); private set
    var saveFailed by mutableStateOf(false); private set
    var audioFailed by mutableStateOf(false); private set
    private val main = Handler(Looper.getMainLooper())
    private val worker = Executors.newSingleThreadExecutor()
    private val store = ClockStore(AtomicProgressStorage(File(application.noBackupFilesDir, "clock-explore"), AndroidAtomicCommit))
    private val practiceHost = ClockPracticeHost(AtomicProgressStorage(File(application.noBackupFilesDir, "clock-make"), AndroidAtomicCommit), AndroidProgressRepository.create(application))
    var practice by mutableStateOf<ClockPractice.State?>(null); private set
    var practiceBusy by mutableStateOf(false); private set
    var practiceFailed by mutableStateOf(false); private set
    private var practiceMode = false
    private var practiceSerial = 0L
    private var speechEpoch = 0L
    private val audio = ClockAudio(DefaultAudioController(engineFactory, AudioMode.OFF)) { audioFailed = it }
    private var language = ContentLanguage.ENGLISH
    private var mode = AudioMode.OFF
    private var visible = false
    private var loading = false
    private var closed = false
    private var revision = 0L
    private val pendingWrite = Runnable { persist() }

    fun configure(lang: String, sound: String) {
        val next = if (lang == "de") ContentLanguage.GERMAN else ContentLanguage.ENGLISH
        val nextMode = when (sound) { "all" -> AudioMode.ALL; "questions" -> AudioMode.QUESTIONS; else -> AudioMode.OFF }
        if (nextMode != mode) { speechEpoch++; mode = nextMode; audio.mode(mode) }
        if (next != language) {
            language = next; speechEpoch++; audio.cancel()
            state?.let { state = it.copy(language = next); scheduleSave() }
            if (practice != null) practiceWork { practiceHost.dispatch(ClockPractice.Action.Language(next)) }
        }
    }
    fun setVisible(value: Boolean, make: Boolean = false) {
        if (visible == value && practiceMode == make) return
        visible = value; practiceMode = make; speechEpoch++; audio.visible(value)
        if (value && make && practice == null && !practiceBusy) openPractice()
        if (value && !make && state == null) load()
        if (!value) { main.removeCallbacks(pendingWrite); persist() }
    }
    private fun load() {
        if (loading || closed) return
        loading = true
        worker.execute {
            val result = runCatching { store.read() }
            main.post {
                if (!closed) {
                    loading = false; saveFailed = result.isFailure
                    result.getOrNull()?.let {
                        // Global Options remains the language authority, as in other native owners.
                        state = it.copy(language = language)
                        if (it.language != language) scheduleSave()
                    }
                }
            }
        }
    }
    fun move(time: ClockTime) {
        if (!visible || saveFailed) return
        val current = state ?: return
        audio.cancel()
        state = current.copy(time = time)
        scheduleSave()
    }
    fun adjust(delta: Int) { state?.let { move(it.time.advance(delta)) } }
    fun settle() { state?.let { move(it.time.snap()) }; main.removeCallbacks(pendingWrite); persist() }
    fun interrupt() { speechEpoch++; audio.cancel() }
    fun listen() { if (visible) state?.let(audio::listen) }
    fun retrySave() { if (state == null) load() else persist() }
    private fun scheduleSave() {
        revision++
        main.removeCallbacks(pendingWrite)
        main.postDelayed(pendingWrite, 120)
    }
    private fun persist() {
        val snapshot = state ?: return
        if (closed) return
        val token = revision
        worker.execute {
            val result = runCatching { store.write(snapshot) }
            main.post { if (!closed && token == revision) saveFailed = result.isFailure }
        }
    }
    private fun openPractice() {
        val lang = language
        practiceWork { practiceHost.open(SessionId(UUID.randomUUID().toString()), System.nanoTime(), lang) }
    }
    fun practiceMove(time: ClockTime) {
        if (!practiceReady() || practice?.current?.solved == true) return
        val action = ClockPractice.Action.Move(time)
        val current = practice ?: return
        val next = ClockPractice.reduce(current, action)
        if (next == current) return
        interrupt(); practice = next
        // Moves remain responsive; the serial worker commits in order before any queued Check.
        practiceWork(blocking = false) { practiceHost.dispatch(action) }
    }
    fun practiceAdjust(delta: Int) { practice?.let { practiceMove(it.position.advance(delta)) } }
    fun practiceCheck() {
        if (!practiceReady()) return
        val before = practice ?: return
        if (before.current.solved || before.completed) return
        interrupt()
        practiceWork(speech = { after ->
            if (!before.current.support.hint && after.current.support.hint) SpeechKind.EXPLANATION else SpeechKind.FEEDBACK
        }) { practiceHost.dispatch(ClockPractice.Action.Check) }
    }
    fun practiceNext() {
        if (!practiceReady() || practice?.current?.solved != true) return
        interrupt()
        practiceWork(speech = { if (it.completed) SpeechKind.COMPLETION else SpeechKind.QUESTION }) {
            practiceHost.dispatch(ClockPractice.Action.Next)
        }
    }
    fun practiceReplay() {
        if (!practiceReady()) return
        interrupt()
        practiceWork(speech = { if (it.completed) SpeechKind.COMPLETION else SpeechKind.QUESTION }, manual = true) {
            practiceHost.dispatch(ClockPractice.Action.Replay)
        }
    }
    fun practiceAgain() {
        if (!practiceReady() || practice?.completed != true) return
        interrupt(); val lang = language
        practiceWork(speech = { SpeechKind.QUESTION }) {
            practiceHost.again(SessionId(UUID.randomUUID().toString()), System.nanoTime(), lang)
        }
    }
    fun practiceRetrySave() {
        if (practiceBusy) return
        interrupt()
        if (practiceHost.state == null) openPractice() else practiceWork { practiceHost.retryWrites() }
    }
    private fun practiceReady() = visible && practiceMode && !practiceBusy && !practiceFailed && practice?.language == language
    private fun practiceWork(blocking: Boolean = true, speech: ((ClockPractice.State) -> SpeechKind)? = null,
                             manual: Boolean = false, operation: () -> Unit) {
        if (closed) return
        if (blocking) practiceBusy = true
        val serial = ++practiceSerial; val audioToken = speechEpoch
        worker.execute {
            val result = runCatching(operation)
            val next = practiceHost.state; val failed = result.isFailure || practiceHost.failure
            main.post {
                if (!closed && serial == practiceSerial) {
                    practiceBusy = false; practiceFailed = failed
                    if (next != null) practice = next
                    if (!failed && next != null) {
                        if (next.language != language) {
                            val lang = language
                            practiceWork { practiceHost.dispatch(ClockPractice.Action.Language(lang)) }
                        } else if (visible && practiceMode && audioToken == speechEpoch && speech != null) {
                            audio.practice(next, speech(next), manual)
                        }
                    }
                }
            }
        }
    }
    override fun onCleared() {
        main.removeCallbacks(pendingWrite); persist()
        closed = true; audio.close(); worker.shutdown(); super.onCleared()
    }
}
