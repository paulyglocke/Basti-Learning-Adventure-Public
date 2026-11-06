package com.bellfamily.bastischool.ui.verbmotion

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.bellfamily.bastischool.audio.*
import com.bellfamily.bastischool.audio.android.AndroidSystemSpeechEngine
import com.bellfamily.bastischool.ui.design.BastiDesignTheme
import com.bellfamily.bastischool.ui.design.BastiPalette

/** Explicit debug launch only. Does not create a production session or change preferences. */
class VerbMotionActivity : ComponentActivity() {
    private var audioFailed by mutableStateOf(false)
    private var mode by mutableStateOf(AudioMode.OFF)
    private lateinit var audio: VerbMotionAudio
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        audio = VerbMotionAudio(DefaultAudioController({ AndroidSystemSpeechEngine(application) }, AudioMode.OFF)) { audioFailed = it }
        setContent {
            BastiDesignTheme {
                Surface(color = BastiPalette.background) {
                    VerbMotionScreen(intent.getStringExtra("language") == "de", mode, audioFailed,
                        audio::listen, audio::cancel, { finish() },
                        Modifier.fillMaxSize().safeDrawingPadding(),
                        initialVerb = intent.getStringExtra("verb"))
                }
            }
        }
    }
    override fun onResume() {
        super.onResume()
        mode = when (getSharedPreferences("basti_shell", 0).getString("audioMode", "all")) {
            "all" -> AudioMode.ALL; "questions" -> AudioMode.QUESTIONS; else -> AudioMode.OFF
        }
        audio.mode(mode); audio.visible(true)
    }
    override fun onPause() { audio.visible(false); super.onPause() }
    override fun onDestroy() { audio.close(); super.onDestroy() }
}
