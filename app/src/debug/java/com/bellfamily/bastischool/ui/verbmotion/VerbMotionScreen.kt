package com.bellfamily.bastischool.ui.verbmotion

import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.bellfamily.bastischool.audio.AudioMode
import com.bellfamily.bastischool.learning.models.ContentLanguage
import com.bellfamily.bastischool.ui.common.NativeActionButton
import com.bellfamily.bastischool.ui.common.NativeActionRole

@Composable
internal fun VerbMotionScreen(initialGerman: Boolean, audioMode: AudioMode, audioFailed: Boolean,
    onListen: (VerbMotionSample, ContentLanguage) -> Unit, onInterrupt: () -> Unit, onClose: () -> Unit,
    modifier: Modifier = Modifier, initialVerb: String? = null) {
    var german by rememberSaveable { mutableStateOf(initialGerman) }
    var selected by rememberSaveable { mutableStateOf(VerbMotionSample.entries.firstOrNull {
        it.name.equals(initialVerb,true) }?.name ?: VerbMotionSample.JUMP.name) }
    var pictureSteps by rememberSaveable { mutableStateOf(false) }
    var playing by remember { mutableStateOf(false) }
    var replay by remember { mutableIntStateOf(0) }
    val sample = VerbMotionSample.valueOf(selected)
    val language = if(german) ContentLanguage.GERMAN else ContentLanguage.ENGLISH
    fun t(en: String, de: String) = if(german) de else en
    val currentInterrupt by rememberUpdatedState(onInterrupt)
    val permission = verbMotionPermission {playing=false; currentInterrupt()}
    val showSteps = pictureSteps || !permission.second
    LaunchedEffect(permission, selected, language, showSteps) {
        playing = false; currentInterrupt()
    }
    DisposableEffect(Unit) { onDispose { currentInterrupt() } }
    val atlasResult by rememberVerbAtlas(sample)
    val atlas = atlasResult?.getOrNull()
    val moving = playing && permission.first && !showSteps && atlas != null
    var elapsedNanos by remember(sample) { mutableLongStateOf(0L) }
    LaunchedEffect(moving, replay, sample) {
        if(moving) {
            var previous: Long? = null
            while(true) {
                val now = withInfiniteAnimationFrameNanos {it}
                previous?.let {elapsedNanos=(elapsedNanos+now-it)%(sample.cycleMillis*1_000_000L)}
                previous=now
            }
        }
    }

    Column(modifier.verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement=Arrangement.spacedBy(12.dp)) {
        Text(t("Verb movements", "Verben in Bewegung"),style=MaterialTheme.typography.headlineMedium)
        Text(t("Preview · three movements", "Vorschau · drei Bewegungen"),style=MaterialTheme.typography.labelLarge)
        Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
            FilterChip(!german,{german=false},label={Text("English")},modifier=Modifier.testTag("verb-language-en"))
            FilterChip(german,{german=true},label={Text("Deutsch")},modifier=Modifier.testTag("verb-language-de"))
        }
        Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
            VerbMotionSample.entries.forEach { verb ->
                val label=if(verb==VerbMotionSample.SNAP)t("Snap", "Zuschnappen") else verb.label.display[language]
                FilterChip(selected==verb.name,{selected=verb.name},label={Text(label)},
                    modifier=Modifier.weight(1f).heightIn(min=48.dp).testTag("verb-select-${verb.name.lowercase()}"))
            }
        }
        Text(sample.label.display[language],style=MaterialTheme.typography.headlineSmall)
        Text(sample.explanation.display[language])
        if(atlas != null) {
            if(showSteps) {
                Text(t("Picture steps", "Bildschritte"),style=MaterialTheme.typography.titleLarge)
                BoxWithConstraints(Modifier.fillMaxWidth()) {
                    val columns = if(maxWidth >= 320.dp*LocalDensity.current.fontScale) 2 else 1
                    Column(verticalArrangement=Arrangement.spacedBy(12.dp)) {
                        (0..3).toList().chunked(columns).forEach { row ->
                            Row(horizontalArrangement=Arrangement.spacedBy(12.dp)) {
                                row.forEach { frame ->
                                    OutlinedCard(Modifier.weight(1f)) {
                                        Column(Modifier.padding(8.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) {
                                            VerbMotionStage(sample,atlas,VerbPose(sample.pictureFrames[frame]),sample.stages[frame][language],
                                                Modifier.fillMaxWidth().height(144.dp),still=true,pictureIndex=frame)
                                            Text("${frame+1}. ${sample.stages[frame][language]}")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                VerbMotionStage(sample,atlas,VerbMotionTimeline.sample(sample,elapsedNanos/1_000_000L),
                    sample.example.display[language],
                    Modifier.fillMaxWidth().heightIn(min=180.dp,max=300.dp).aspectRatio(1.5f)
                        .clip(RoundedCornerShape(20.dp)).semantics {
                            stateDescription=if(moving)t("Playing", "Wird abgespielt") else t("Paused", "Pausiert")
                        })
            }
        } else Text(if(atlasResult==null)t("Opening pictures…", "Bilder werden geöffnet…") else
            t("The pictures could not open. Choose another movement or close the preview.",
                "Die Bilder konnten nicht geöffnet werden. Wähle eine andere Bewegung oder schließe die Vorschau."))
        Text(sample.example.display[language])
        if(!showSteps) {
            NativeActionButton(t("Watch again", "Noch einmal ansehen"),NativeActionRole.PRIMARY,{
                currentInterrupt(); elapsedNanos=0; replay++; playing=true
            },Modifier.fillMaxWidth().testTag("verb-watch"),enabled=atlas!=null && permission.first)
            NativeActionButton(t("Pause", "Pause"),NativeActionRole.SECONDARY,{
                playing=false; currentInterrupt()
            },Modifier.fillMaxWidth().testTag("verb-pause"),enabled=moving)
        }
        NativeActionButton(t("Listen", "Anhören"),NativeActionRole.SECONDARY,{onListen(sample,language)},
            Modifier.fillMaxWidth().testTag("verb-listen"),enabled=audioMode!=AudioMode.OFF && permission.first)
        if(audioMode==AudioMode.OFF)Text(t("Sound is off in Options.", "Der Ton ist in den Optionen ausgeschaltet."))
        if(audioFailed)Text(t("Speech is unavailable. Check installed offline voices in Options.",
            "Die Sprachausgabe ist nicht verfügbar. Prüfe die Offline-Stimmen in den Optionen."))
        if(permission.second) NativeActionButton(
            if(pictureSteps)t("Show movement", "Bewegung zeigen") else t("Show picture steps", "Bildschritte zeigen"),
            NativeActionRole.SECONDARY,{pictureSteps=!pictureSteps},Modifier.fillMaxWidth().testTag("verb-picture-toggle"))
        else Text(t("Movement is off on this device. You can explore the picture steps.",
            "Bewegung ist auf diesem Gerät ausgeschaltet. Du kannst dir die Bildschritte ansehen."))
        NativeActionButton(t("Close preview", "Vorschau schließen"),NativeActionRole.NAVIGATION,onClose,
            Modifier.fillMaxWidth().testTag("verb-close"))
    }
}

/** Background and system reduced-motion changes remove the animation clock immediately. */
@Composable
private fun verbMotionPermission(onSuspend: () -> Unit): Pair<Boolean,Boolean> {
    val resolver=LocalContext.current.contentResolver
    val lifecycle=LocalLifecycleOwner.current.lifecycle
    fun enabled()=Settings.Global.getFloat(resolver,Settings.Global.ANIMATOR_DURATION_SCALE,1f)>0f
    var systemMotion by remember {mutableStateOf(enabled())}
    var resumed by remember {mutableStateOf(lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED))}
    val suspendNow by rememberUpdatedState(onSuspend)
    DisposableEffect(resolver,lifecycle) {
        val observer=LifecycleEventObserver {_,_->
            resumed=lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED);systemMotion=enabled()
            // Clear playback synchronously: a rapid pause/resume may occur between Compose frames.
            if(!resumed)suspendNow()
        }
        val settings=object:ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange:Boolean){systemMotion=enabled();if(!systemMotion)suspendNow()}
        }
        lifecycle.addObserver(observer)
        resolver.registerContentObserver(Settings.Global.getUriFor(Settings.Global.ANIMATOR_DURATION_SCALE),false,settings)
        onDispose {lifecycle.removeObserver(observer);resolver.unregisterContentObserver(settings)}
    }
    return resumed to systemMotion
}
