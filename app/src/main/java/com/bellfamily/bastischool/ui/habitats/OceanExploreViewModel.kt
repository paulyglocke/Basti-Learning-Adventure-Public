package com.bellfamily.bastischool.ui.habitats

import android.app.Application
import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.AndroidViewModel
import com.bellfamily.bastischool.audio.*
import com.bellfamily.bastischool.audio.android.AndroidSystemSpeechEngine
import com.bellfamily.bastischool.learning.animals.HabitatExploreAction
import com.bellfamily.bastischool.learning.animals.HabitatExploreContent
import com.bellfamily.bastischool.learning.animals.HabitatExploreReducer
import com.bellfamily.bastischool.learning.animals.HabitatExploreState
import com.bellfamily.bastischool.learning.models.ContentLanguage
import java.util.concurrent.Executors

/** Ephemeral, unscored Explore owner. It has no journal or Progress Repository. */
class OceanExploreViewModel @JvmOverloads constructor(
    application: Application,
    engineFactory: () -> SpeechEngine = { AndroidSystemSpeechEngine(application) }
) : AndroidViewModel(application) {
    var image by mutableStateOf<ImageBitmap?>(null); private set
    var artworkFailed by mutableStateOf(false); private set
    var selection by mutableStateOf(HabitatExploreState()); private set
    val selectedDolphin get() = selection.selectedAnimal == HabitatExploreContent.dolphin
    var audioFailed by mutableStateOf(false); private set

    private val loader = OceanArtworkLoader { application.assets.open(it) }
    private val worker = Executors.newSingleThreadExecutor()
    private val main = Handler(Looper.getMainLooper())
    private val owner = SpeechOwner("native-ocean-explore")
    private val audio = DefaultAudioController(engineFactory, AudioMode.OFF)
    private var visible = false
    private var closed = false
    private var requested = false
    private var language = ContentLanguage.ENGLISH
    private var mode = AudioMode.OFF
    private var speechRevision = 0L

    fun configure(languageCode: String, audioMode: String) {
        val nextLanguage = if (languageCode == "de") ContentLanguage.GERMAN else ContentLanguage.ENGLISH
        val nextMode = when (audioMode) {
            "all" -> AudioMode.ALL
            "questions" -> AudioMode.QUESTIONS
            else -> AudioMode.OFF
        }
        if (language != nextLanguage || mode != nextMode) cancelSpeech()
        language = nextLanguage
        mode = nextMode
        audio.setMode(nextMode)
    }

    fun setVisible(value: Boolean) {
        if (visible == value) return
        visible = value
        audio.cancelOwner(owner)
        speechRevision++
        audioFailed = false
        if (value && !closed && !requested) {
            requested = true
            worker.execute {
                val loaded = loader.load()
                main.post {
                    if (!closed) {
                        image = loaded
                        artworkFailed = loaded == null
                    }
                }
            }
        }
    }

    fun selectDolphin() {
        if (!visible || closed) return
        selection = HabitatExploreReducer.reduce(selection, HabitatExploreAction.Select(HabitatExploreContent.dolphin))
        speak(replay = false)
    }

    fun replay() {
        if (!visible || closed || !selectedDolphin) return
        speak(replay = true)
    }

    private fun cancelSpeech() {
        speechRevision++
        audio.cancelOwner(owner)
        audioFailed = false
    }

    private fun speak(replay: Boolean) {
        cancelSpeech()
        val token = speechRevision
        val context = audio.openContext(owner, SpeechSessionId("ocean-explore-dolphin"), language)
        val trigger = if (replay) SpeechTrigger.REPLAY else SpeechTrigger.MANUAL
        audio.speak(SpeechRequest.fromContent(context, HabitatExploreContent.dolphinUtterance,
            SpeechKind.DISCOVERY_FACT, trigger)) { outcome ->
            if (!closed && visible && token == speechRevision)
                audioFailed = outcome.result is SpeechResult.Failed
        }
    }

    override fun onCleared() {
        closed = true
        cancelSpeech()
        audio.close()
        worker.shutdown()
        super.onCleared()
    }
}
