package com.bellfamily.bastischool.ui.followinstructions

import android.app.Application
import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.AndroidViewModel
import com.bellfamily.bastischool.audio.*
import com.bellfamily.bastischool.audio.android.AndroidSystemSpeechEngine
import com.bellfamily.bastischool.learning.follow.*
import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.schoolskills.SchoolSkillsContent
import com.bellfamily.bastischool.learning.schoolskills.SchoolActionsContent
import com.bellfamily.bastischool.learning.schoolskills.SchoolActionSequenceContent
import com.bellfamily.bastischool.learning.schoolskills.SchoolActionTripleContent
import com.bellfamily.bastischool.learning.schoolskills.ClassroomHelperContent
import com.bellfamily.bastischool.learning.progress.AtomicProgressStorage
import com.bellfamily.bastischool.learning.progress.android.*
import com.bellfamily.bastischool.learning.session.*
import java.io.File
import java.util.UUID
import java.util.concurrent.Executors

enum class FollowLevel { ONE, TWO, SCHOOL_SKILLS, SCHOOL_ACTIONS, SCHOOL_ACTION_SEQUENCE, SCHOOL_ACTION_TRIPLE, SCHOOL_CLASSROOM_HELPER }

class FollowViewModel @JvmOverloads constructor(
    application: Application,
    engineFactory: () -> SpeechEngine = { AndroidSystemSpeechEngine(application) }
) : AndroidViewModel(application) {
    var state by mutableStateOf<SessionState?>(null); private set
    var twoState by mutableStateOf<FollowTwoState?>(null); private set
    var schoolState by mutableStateOf<SessionState?>(null); private set
    var schoolActionsState by mutableStateOf<SessionState?>(null); private set
    var actionSequenceState by mutableStateOf<SessionState?>(null); private set
    var actionTripleState by mutableStateOf<SessionState?>(null); private set
    var classroomHelperState by mutableStateOf<SessionState?>(null); private set
    var images by mutableStateOf<Map<ContentId, ImageBitmap>>(emptyMap()); private set
    var schoolScene by mutableStateOf<ImageBitmap?>(null); private set
    var schoolIllustrations by mutableStateOf<Map<ContentId, ImageBitmap>>(emptyMap()); private set
    var schoolActionImages by mutableStateOf<Map<ContentId, ImageBitmap>>(emptyMap()); private set
    var classroomBoardImages by mutableStateOf<Map<ContentId, ImageBitmap>>(emptyMap()); private set
    var busy by mutableStateOf(false); private set
    var saveFailed by mutableStateOf(false); private set
    var audioFailed by mutableStateOf(false); private set
    private val worker = Executors.newSingleThreadExecutor()
    private val main = Handler(Looper.getMainLooper())
    private val progress = AndroidProgressRepository.create(application)
    private val host = FollowContent.host(AtomicProgressStorage(File(application.noBackupFilesDir, "follow-instructions-session"), AndroidAtomicCommit), progress)
    private val twoHost = FollowTwoHost(AtomicProgressStorage(File(application.noBackupFilesDir, "follow-instructions-two-session"), AndroidAtomicCommit), progress)
    private val schoolHost = SchoolSkillsContent.host(AtomicProgressStorage(File(application.noBackupFilesDir, "school-skills-session"), AndroidAtomicCommit), progress)
    private val schoolActionsHost = SchoolActionsContent.host(AtomicProgressStorage(File(application.noBackupFilesDir, "school-actions-session"), AndroidAtomicCommit), progress)
    private val actionSequenceHost = SchoolActionSequenceContent.host(AtomicProgressStorage(File(application.noBackupFilesDir, "school-action-sequence-session"), AndroidAtomicCommit), progress)
    private val actionTripleHost = SchoolActionTripleContent.host(AtomicProgressStorage(File(application.noBackupFilesDir, "school-action-triple-session"), AndroidAtomicCommit), progress)
    private val classroomHelperHost = ClassroomHelperContent.host(AtomicProgressStorage(File(application.noBackupFilesDir, "classroom-helper-session"), AndroidAtomicCommit), progress)
    private val loader = FollowArtworkLoader { application.assets.open(it) }
    private val audio = FollowAudio(DefaultAudioController(engineFactory, AudioMode.OFF)) { audioFailed = it is SpeechResult.Failed }
    private var visible = false
    private var level: FollowLevel? = null
    private var loaded = false
    private var closed = false
    private var language = ContentLanguage.ENGLISH
    private var mode = AudioMode.OFF
    private var round = RoundLength.FIVE
    private var epoch = 0L
    fun configure(lang: String, audioMode: String, count: Int) {
        val nextLanguage = if (lang == "de") ContentLanguage.GERMAN else ContentLanguage.ENGLISH
        val nextMode = when (audioMode) { "all" -> AudioMode.ALL; "questions" -> AudioMode.QUESTIONS; else -> AudioMode.OFF }
        round = if (count == 10) RoundLength.TEN else RoundLength.FIVE
        if (nextLanguage != language || nextMode != mode) { language = nextLanguage; mode = nextMode; epoch++; audio.mode(nextMode); syncLanguage() }
    }
    fun setVisible(value: Boolean, nextLevel: FollowLevel? = FollowLevel.ONE) {
        if (visible == value && level == nextLevel) return
        visible = value; level = nextLevel; epoch++; audio.visible(value && nextLevel != null)
        if (value && !loaded) { loaded = true; worker.execute {
            val result = loader.load(); val classroom = loader.loadSchoolScene(); val illustrations = loader.loadSchoolPhrases(); val actions = loader.loadSchoolActions(); val board = loader.loadClassroomBoard()
            main.post { if (!closed) { images = result; schoolScene = classroom; schoolIllustrations = illustrations; schoolActionImages = actions; classroomBoardImages = board } }
        } }
        if (value && nextLevel == FollowLevel.ONE && !busy) ensureOne()
        if (value && nextLevel == FollowLevel.TWO && !busy) ensureTwo()
        if (value && nextLevel == FollowLevel.SCHOOL_SKILLS && !busy) ensureSchool()
        if (value && nextLevel == FollowLevel.SCHOOL_ACTIONS && !busy) ensureSchoolActions()
        if (value && nextLevel == FollowLevel.SCHOOL_ACTION_SEQUENCE && !busy) ensureActionSequence()
        if (value && nextLevel == FollowLevel.SCHOOL_ACTION_TRIPLE && !busy) ensureActionTriple()
        if (value && nextLevel == FollowLevel.SCHOOL_CLASSROOM_HELPER && !busy) ensureClassroomHelper()
    }
    private fun ensureOne() {
        if (state == null) work { host.open(SessionId(UUID.randomUUID().toString()), round, System.nanoTime(), language) }
        else syncLanguage()
    }
    private fun ensureTwo() {
        if (twoState == null) twoWork { twoHost.open(SessionId(UUID.randomUUID().toString()), round, System.nanoTime(), language) }
        else syncLanguage()
    }
    private fun ensureSchool() {
        if (schoolState == null) schoolWork { schoolHost.open(SessionId(UUID.randomUUID().toString()), round, System.nanoTime(), language) }
        else syncLanguage()
    }
    private fun ensureSchoolActions() {
        if (schoolActionsState == null) schoolActionsWork { schoolActionsHost.open(SessionId(UUID.randomUUID().toString()), round, System.nanoTime(), language) }
        else syncLanguage()
    }
    private fun ensureActionSequence() {
        if (actionSequenceState == null) actionSequenceWork { actionSequenceHost.open(SessionId(UUID.randomUUID().toString()), round, System.nanoTime(), language) }
        else syncLanguage()
    }
    private fun ensureActionTriple() {
        if (actionTripleState == null) actionTripleWork { actionTripleHost.open(SessionId(UUID.randomUUID().toString()), round, System.nanoTime(), language) }
        else syncLanguage()
    }
    private fun ensureClassroomHelper() {
        if (classroomHelperState == null) classroomHelperWork { classroomHelperHost.open(SessionId(UUID.randomUUID().toString()), round, System.nanoTime(), language) }
        else syncLanguage()
    }
    private fun syncLanguage() {
        if (!busy && level == FollowLevel.ONE && host.state?.language != language && host.state != null)
            work { host.dispatch(SessionAction.Language(language)) }
        if (!busy && level == FollowLevel.TWO && twoHost.state?.language != language && twoHost.state != null)
            twoWork { twoHost.dispatch(FollowTwoAction.Language(language)) }
        if (!busy && level == FollowLevel.SCHOOL_SKILLS && schoolHost.state?.language != language && schoolHost.state != null)
            schoolWork { schoolHost.dispatch(SessionAction.Language(language)) }
        if (!busy && level == FollowLevel.SCHOOL_ACTIONS && schoolActionsHost.state?.language != language && schoolActionsHost.state != null)
            schoolActionsWork { schoolActionsHost.dispatch(SessionAction.Language(language)) }
        if (!busy && level == FollowLevel.SCHOOL_ACTION_SEQUENCE && actionSequenceHost.state?.language != language && actionSequenceHost.state != null)
            actionSequenceWork { actionSequenceHost.dispatch(SessionAction.Language(language)) }
        if (!busy && level == FollowLevel.SCHOOL_ACTION_TRIPLE && actionTripleHost.state?.language != language && actionTripleHost.state != null)
            actionTripleWork { actionTripleHost.dispatch(SessionAction.Language(language)) }
        if (!busy && level == FollowLevel.SCHOOL_CLASSROOM_HELPER && classroomHelperHost.state?.language != language && classroomHelperHost.state != null)
            classroomHelperWork { classroomHelperHost.dispatch(SessionAction.Language(language)) }
    }
    fun replay() { state?.let { action(SessionAction.Replay(it.task.id)) } }
    fun action(action: SessionAction) { if (!ready(FollowLevel.ONE)) return; epoch++; audio.cancel(); work { host.dispatch(action) } }
    fun twoAction(action: FollowTwoAction) { if (!ready(FollowLevel.TWO)) return; epoch++; audio.cancel(); twoWork { twoHost.dispatch(action) } }
    fun schoolAction(action: SessionAction) { if (!ready(FollowLevel.SCHOOL_SKILLS)) return; epoch++; audio.cancel(); schoolWork { schoolHost.dispatch(action) } }
    fun twoReplay() { twoState?.let { twoAction(FollowTwoAction.Replay(it.taskId)) } }
    fun schoolReplay() { schoolState?.let { schoolAction(SessionAction.Replay(it.task.id)) } }
    fun schoolActionsAction(action: SessionAction) { if (!ready(FollowLevel.SCHOOL_ACTIONS)) return; epoch++; audio.cancel(); schoolActionsWork { schoolActionsHost.dispatch(action) } }
    fun schoolActionsReplay() { schoolActionsState?.let { schoolActionsAction(SessionAction.Replay(it.task.id)) } }
    fun actionSequenceAction(action: SessionAction) { if (!ready(FollowLevel.SCHOOL_ACTION_SEQUENCE)) return; epoch++; audio.cancel(); actionSequenceWork { actionSequenceHost.dispatch(action) } }
    fun actionSequenceReplay() { actionSequenceState?.let { actionSequenceAction(SessionAction.Replay(it.task.id)) } }
    fun actionTripleAction(action: SessionAction) { if (!ready(FollowLevel.SCHOOL_ACTION_TRIPLE)) return; epoch++; audio.cancel(); actionTripleWork { actionTripleHost.dispatch(action) } }
    fun actionTripleReplay() { actionTripleState?.let { actionTripleAction(SessionAction.Replay(it.task.id)) } }
    fun classroomHelperAction(action: SessionAction) { if (!ready(FollowLevel.SCHOOL_CLASSROOM_HELPER)) return; epoch++; audio.cancel(); classroomHelperWork { classroomHelperHost.dispatch(action) } }
    fun classroomHelperReplay() { classroomHelperState?.let { classroomHelperAction(SessionAction.Replay(it.task.id)) } }
    fun twoExample() { val current = twoState ?: return; if (ready(FollowLevel.TWO)) {
        epoch++; audio.effects(current.id, language, listOf(SessionEffect.Narrate(FollowTwo.demonstration,
            NarrationKind.INSTRUCTION, SpeechTrigger.MANUAL)))
    } }
    fun retrySave() { if (!busy && level == FollowLevel.ONE) work { host.retryWrites(); emptyList() }
        else if (!busy && level == FollowLevel.TWO) twoWork { twoHost.retryWrites(); null }
        else if (!busy && level == FollowLevel.SCHOOL_SKILLS) schoolWork { schoolHost.retryWrites(); emptyList() }
        else if (!busy && level == FollowLevel.SCHOOL_ACTIONS) schoolActionsWork { schoolActionsHost.retryWrites(); emptyList() }
        else if (!busy && level == FollowLevel.SCHOOL_ACTION_SEQUENCE) actionSequenceWork { actionSequenceHost.retryWrites(); emptyList() }
        else if (!busy && level == FollowLevel.SCHOOL_ACTION_TRIPLE) actionTripleWork { actionTripleHost.retryWrites(); emptyList() }
        else if (!busy && level == FollowLevel.SCHOOL_CLASSROOM_HELPER) classroomHelperWork { classroomHelperHost.retryWrites(); emptyList() } }
    fun again() { if (!ready(FollowLevel.ONE) || state?.phase != SessionPhase.COMPLETED) return; epoch++; audio.cancel(); work { host.newRound(SessionId(UUID.randomUUID().toString()), round, System.nanoTime(), language) } }
    fun twoAgain() { if (!ready(FollowLevel.TWO) || twoState?.completed != true) return; epoch++; audio.cancel()
        twoWork { twoHost.again(SessionId(UUID.randomUUID().toString()), round, System.nanoTime(), language) } }
    fun schoolAgain() { if (!ready(FollowLevel.SCHOOL_SKILLS) || schoolState?.phase != SessionPhase.COMPLETED) return; epoch++; audio.cancel()
        schoolWork { schoolHost.newRound(SessionId(UUID.randomUUID().toString()), round, System.nanoTime(), language) } }
    fun schoolActionsAgain() { if (!ready(FollowLevel.SCHOOL_ACTIONS) || schoolActionsState?.phase != SessionPhase.COMPLETED) return; epoch++; audio.cancel()
        schoolActionsWork { schoolActionsHost.newRound(SessionId(UUID.randomUUID().toString()), round, System.nanoTime(), language) } }
    fun actionSequenceAgain() { if (!ready(FollowLevel.SCHOOL_ACTION_SEQUENCE) || actionSequenceState?.phase != SessionPhase.COMPLETED) return; epoch++; audio.cancel()
        actionSequenceWork { actionSequenceHost.newRound(SessionId(UUID.randomUUID().toString()), round, System.nanoTime(), language) } }
    fun actionTripleAgain() { if (!ready(FollowLevel.SCHOOL_ACTION_TRIPLE) || actionTripleState?.phase != SessionPhase.COMPLETED) return; epoch++; audio.cancel()
        actionTripleWork { actionTripleHost.newRound(SessionId(UUID.randomUUID().toString()), round, System.nanoTime(), language) } }
    fun classroomHelperAgain() { if (!ready(FollowLevel.SCHOOL_CLASSROOM_HELPER) || classroomHelperState?.phase != SessionPhase.COMPLETED) return; epoch++; audio.cancel()
        classroomHelperWork { classroomHelperHost.newRound(SessionId(UUID.randomUUID().toString()), round, System.nanoTime(), language) } }
    private fun ready(expected: FollowLevel) = visible && level == expected && !busy && !saveFailed &&
        when (expected) {
            FollowLevel.ONE -> state?.language == language
            FollowLevel.TWO -> twoState?.language == language
            FollowLevel.SCHOOL_SKILLS -> schoolState?.language == language
            FollowLevel.SCHOOL_ACTIONS -> schoolActionsState?.language == language
            FollowLevel.SCHOOL_ACTION_SEQUENCE -> actionSequenceState?.language == language
            FollowLevel.SCHOOL_ACTION_TRIPLE -> actionTripleState?.language == language
            FollowLevel.SCHOOL_CLASSROOM_HELPER -> classroomHelperState?.language == language
        }
    private fun work(operation: () -> List<SessionEffect>) {
        if (busy || closed) return
        busy = true; val token = ++epoch
        worker.execute {
            var failed = false; var effects = emptyList<SessionEffect>()
            try { effects = operation() } catch (_: Exception) { failed = true }
            val next = host.state; val pending = host.failure
            main.post {
                if (!closed) {
                    busy = false
                    saveFailed = failed || pending
                    if (!failed) state = next
                    if (!failed && !saveFailed && token == epoch && next != null && visible && level == FollowLevel.ONE && next.language == language) {
                        audio.effects(next.plan.id, language, effects)
                    }
                    if (!saveFailed) ensureCurrent()
                }
            }
        }
    }
    private fun twoWork(operation: () -> SessionEffect.Narrate?) {
        if (busy || closed) return
        busy = true; val token = ++epoch
        worker.execute {
            val result = runCatching(operation)
            val next = twoHost.state; val pending = twoHost.failure
            main.post {
                if (!closed) {
                    busy = false; saveFailed = result.isFailure || pending
                    if (next != null) twoState = next
                    if (!saveFailed && token == epoch && next != null && visible && level == FollowLevel.TWO && next.language == language)
                        audio.effects(next.id, language, listOfNotNull(result.getOrNull()))
                    if (!saveFailed) ensureCurrent()
                }
            }
        }
    }
    private fun schoolWork(operation: () -> List<SessionEffect>) {
        if (busy || closed) return
        busy = true; val token = ++epoch
        worker.execute {
            val result = runCatching(operation)
            val next = schoolHost.state; val pending = schoolHost.failure
            main.post {
                if (!closed) {
                    busy = false; saveFailed = result.isFailure || pending
                    if (next != null) schoolState = next
                    if (!saveFailed && token == epoch && next != null && visible && level == FollowLevel.SCHOOL_SKILLS && next.language == language)
                        audio.effects(next.plan.id, language, result.getOrDefault(emptyList()))
                    if (!saveFailed) ensureCurrent()
                }
            }
        }
    }
    private fun schoolActionsWork(operation: () -> List<SessionEffect>) {
        if (busy || closed) return
        busy = true; val token = ++epoch
        worker.execute {
            val result = runCatching(operation)
            val next = schoolActionsHost.state; val pending = schoolActionsHost.failure
            main.post {
                if (!closed) {
                    busy = false; saveFailed = result.isFailure || pending
                    if (next != null) schoolActionsState = next
                    if (!saveFailed && token == epoch && next != null && visible && level == FollowLevel.SCHOOL_ACTIONS && next.language == language)
                        audio.effects(next.plan.id, language, result.getOrDefault(emptyList()))
                    if (!saveFailed) ensureCurrent()
                }
            }
        }
    }
    private fun actionSequenceWork(operation: () -> List<SessionEffect>) {
        if (busy || closed) return
        busy = true; val token = ++epoch
        worker.execute {
            val result = runCatching(operation)
            val next = actionSequenceHost.state; val pending = actionSequenceHost.failure
            main.post {
                if (!closed) {
                    busy = false; saveFailed = result.isFailure || pending
                    if (next != null) actionSequenceState = next
                    if (!saveFailed && token == epoch && next != null && visible && level == FollowLevel.SCHOOL_ACTION_SEQUENCE && next.language == language)
                        audio.effects(next.plan.id, language, result.getOrDefault(emptyList()))
                    if (!saveFailed) ensureCurrent()
                }
            }
        }
    }
    private fun actionTripleWork(operation: () -> List<SessionEffect>) {
        if (busy || closed) return
        busy = true; val token = ++epoch
        worker.execute {
            val result = runCatching(operation)
            val next = actionTripleHost.state; val pending = actionTripleHost.failure
            main.post {
                if (!closed) {
                    busy = false; saveFailed = result.isFailure || pending
                    if (next != null) actionTripleState = next
                    if (!saveFailed && token == epoch && next != null && visible && level == FollowLevel.SCHOOL_ACTION_TRIPLE && next.language == language)
                        audio.effects(next.plan.id, language, result.getOrDefault(emptyList()))
                    if (!saveFailed) ensureCurrent()
                }
            }
        }
    }
    private fun classroomHelperWork(operation: () -> List<SessionEffect>) {
        if (busy || closed) return
        busy = true; val token = ++epoch
        worker.execute {
            val result = runCatching(operation)
            val next = classroomHelperHost.state; val pending = classroomHelperHost.failure
            main.post {
                if (!closed) {
                    busy = false; saveFailed = result.isFailure || pending
                    if (next != null) classroomHelperState = next
                    if (!saveFailed && token == epoch && next != null && visible && level == FollowLevel.SCHOOL_CLASSROOM_HELPER && next.language == language)
                        audio.effects(next.plan.id, language, result.getOrDefault(emptyList()))
                    if (!saveFailed) ensureCurrent()
                }
            }
        }
    }
    private fun ensureCurrent() {
        if (!visible || busy) return
        when (level) {
            FollowLevel.ONE -> ensureOne()
            FollowLevel.TWO -> ensureTwo()
            FollowLevel.SCHOOL_SKILLS -> ensureSchool()
            FollowLevel.SCHOOL_ACTIONS -> ensureSchoolActions()
            FollowLevel.SCHOOL_ACTION_SEQUENCE -> ensureActionSequence()
            FollowLevel.SCHOOL_ACTION_TRIPLE -> ensureActionTriple()
            FollowLevel.SCHOOL_CLASSROOM_HELPER -> ensureClassroomHelper()
            null -> Unit
        }
    }
    override fun onCleared() { closed = true; audio.close(); worker.shutdown(); super.onCleared() }
}
