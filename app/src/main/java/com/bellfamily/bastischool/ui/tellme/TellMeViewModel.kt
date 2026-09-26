package com.bellfamily.bastischool.ui.tellme

import android.app.Application
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.AndroidViewModel
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.ImageBitmap
import com.bellfamily.bastischool.learning.scenedescription.*
import com.bellfamily.bastischool.learning.tellme.*
import java.util.concurrent.Executors

/** In-memory, Activity-scoped conversation. No quiz host, speech engine, storage or grading. */
class TellMeViewModel(application: Application): AndroidViewModel(application) {
    val flow by lazy {TellMeFlow()}
    var state by mutableStateOf(TellMeState()); private set
    var artwork by mutableStateOf<ImageBitmap?>(null); private set
    var loading by mutableStateOf(false); private set
    private val loader = TellMeArtworkLoader {application.assets.open(it)}
    private val worker = Executors.newSingleThreadExecutor()
    private val main = Handler(Looper.getMainLooper())
    private var epoch = 0L
    private var closed = false

    fun start(category: SceneCategoryId) = update(flow.start(category))
    fun advance(scene: SceneId, stage: TellMeStage) = update(flow.advance(state, scene, stage))
    fun help() = update(flow.help(state))
    fun grownUps() = update(flow.grownUps(state))
    fun again() = update(flow.again(state))
    fun home() = update(flow.home())
    private fun update(next: TellMeState) {
        if(closed || next == state) return
        val previous = flow.scene(state)?.id
        state = next
        val scene = flow.scene(next)?.takeIf {next.stage != TellMeStage.COMPLETE}
        if(scene?.id == previous && next.stage != TellMeStage.COMPLETE) return
        val token = ++epoch
        artwork = null; loading = scene != null
        if(scene != null) worker.execute {
            val image = loader.load(scene)
            main.post {if(!closed && token == epoch) {artwork = image; loading = false}}
        }
    }
    override fun onCleared() {closed = true; epoch++; worker.shutdown(); super.onCleared()}
}
