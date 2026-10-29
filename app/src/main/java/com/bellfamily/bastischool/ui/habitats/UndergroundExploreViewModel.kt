package com.bellfamily.bastischool.ui.habitats

import android.app.Application
import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.compose.ui.graphics.ImageBitmap
import com.bellfamily.bastischool.audio.*
import com.bellfamily.bastischool.audio.android.AndroidSystemSpeechEngine
import com.bellfamily.bastischool.learning.animals.HabitatExploreContent
import com.bellfamily.bastischool.learning.animals.UndergroundExploreAction
import com.bellfamily.bastischool.learning.animals.UndergroundExploreReducer
import com.bellfamily.bastischool.learning.animals.UndergroundExploreState
import com.bellfamily.bastischool.learning.models.ContentId
import com.bellfamily.bastischool.learning.models.ContentLanguage
import java.util.concurrent.Executors

/** Ephemeral, unscored Underground Explore owner; it has no journal or Progress Repository. */
class UndergroundExploreViewModel @JvmOverloads constructor(
    application: Application,
    engineFactory: () -> SpeechEngine = { AndroidSystemSpeechEngine(application) }
) : AndroidViewModel(application) {
    var scene by mutableStateOf<ImageBitmap?>(null); private set
    var animalImages by mutableStateOf<Map<ContentId, ImageBitmap>>(emptyMap()); private set
    var artworkFailed by mutableStateOf(false); private set
    var selection by mutableStateOf(UndergroundExploreState()); private set
    var audioFailed by mutableStateOf(false); private set

    private val loader = UndergroundArtworkLoader { path -> application.assets.open(path) }
    private val worker = Executors.newSingleThreadExecutor()
    private val main = Handler(Looper.getMainLooper())
    private val owner = SpeechOwner("native-underground-explore")
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
                        scene = loaded?.scene
                        animalImages = loaded?.animals.orEmpty()
                        artworkFailed = loaded?.scene == null ||
                            HabitatExploreContent.undergroundAnimals.any { it.id !in animalImages }
                    }
                }
            }
        }
    }

    fun select(animal: ContentId) {
        if (!visible || closed) return
        selection = UndergroundExploreReducer.reduce(selection, UndergroundExploreAction.Select(animal))
        HabitatExploreContent.undergroundAnimal(animal)?.let { speak(it.utterance, replay = false) }
    }

    fun replay() {
        if (!visible || closed) return
        selection.selectedAnimal?.let(HabitatExploreContent::undergroundAnimal)?.let { speak(it.utterance, replay = true) }
    }

    private fun cancelSpeech() {
        speechRevision++
        audio.cancelOwner(owner)
        audioFailed = false
    }

    private fun speak(content: com.bellfamily.bastischool.learning.models.ContentText, replay: Boolean) {
        cancelSpeech()
        val token = speechRevision
        val context = audio.openContext(owner, SpeechSessionId("underground-explore"), language)
        val trigger = if (replay) SpeechTrigger.REPLAY else SpeechTrigger.MANUAL
        audio.speak(SpeechRequest.fromContent(context, content, SpeechKind.DISCOVERY_FACT, trigger)) { outcome ->
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
