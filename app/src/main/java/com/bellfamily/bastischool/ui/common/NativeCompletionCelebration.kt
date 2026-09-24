package com.bellfamily.bastischool.ui.common

import android.provider.Settings
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.bellfamily.bastischool.learning.models.ContentLanguage
import com.bellfamily.bastischool.learning.vocabulary.VocabularyContent
import kotlin.math.*

/** Actions live outside the optional scrollable reward area, including in short landscape. */
@Composable
fun NativeCompletionScreen(completion: String, language: ContentLanguage, summary: String, ready: Boolean,
    prefix: String, onReplay: () -> Unit, onAgain: () -> Unit, onHome: () -> Unit, onPop: (String) -> Unit,
    modifier: Modifier = Modifier, saveFailed: Boolean = false, onRetry: () -> Unit = {},
    audioFailed: Boolean = false, moreActions: @Composable () -> Unit = {}) {
    fun t(en: String, de: String) = if(language == ContentLanguage.GERMAN) de else en
    BoxWithConstraints(modifier.fillMaxSize().background(Color(0xFFEAF7FC)).padding(12.dp)) {
        val wide = maxWidth >= 600.dp
        Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(t("Adventure complete!", "Abenteuer geschafft!"), style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.testTag("completion-title"))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                NativeActionButton(t("Play again", "Nochmal spielen"), NativeActionRole.PRIMARY, onAgain, Modifier.weight(1f).testTag("${prefix}again"), enabled = ready)
                NativeActionButton(t("Back home", "Zurück zum Start"), NativeActionRole.NAVIGATION, onHome, Modifier.weight(1f).testTag("${prefix}home"))
                if(wide) NativeActionButton(t("Listen again", "Noch einmal hören"), NativeActionRole.SECONDARY, onReplay, Modifier.weight(1f).testTag("${prefix}replay"), enabled = ready)
            }
            if(!wide) NativeActionButton(t("Listen again", "Noch einmal hören"), NativeActionRole.SECONDARY, onReplay, Modifier.fillMaxWidth().testTag("${prefix}replay"), enabled = ready)
            BoxWithConstraints(Modifier.weight(1f)) {
                val rewardHeight = maxHeight.coerceIn(88.dp, 220.dp)
                Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if(saveFailed) {
                        Text(t("Your saved work is kept. Please try saving again.", "Deine gespeicherte Arbeit bleibt erhalten. Versuche erneut zu speichern."))
                        NativeActionButton(t("Try again", "Erneut versuchen"), NativeActionRole.SECONDARY, onRetry, Modifier.testTag("completion-retry-save"))
                    }
                    if(audioFailed) Text(t("Speech is unavailable. Check installed offline voices in Options.", "Die Sprachausgabe ist nicht verfügbar. Prüfe die Offline-Stimmen in den Optionen."))
                    NativeCompletionCelebration(completion, language, onPop, Modifier.fillMaxWidth().height(rewardHeight), enabled = ready)
                    Text(summary, modifier = Modifier.testTag("${prefix}complete"))
                    moreActions()
                }
            }
        }
    }
}

/** System animation-scale zero and backgrounding simplify reveals and stop the float clock. */
@Composable
private fun celebrationMotion(): Boolean {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    fun enabled() = Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) > 0f
    var active by remember(lifecycle) {mutableStateOf(lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED))}
    var motion by remember {mutableStateOf(enabled())}
    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            if(event == Lifecycle.Event.ON_RESUME) {motion = enabled(); active = true}
            if(event == Lifecycle.Event.ON_PAUSE || event == Lifecycle.Event.ON_STOP) active = false
        }
        lifecycle.addObserver(observer)
        onDispose {lifecycle.removeObserver(observer)}
    }
    return active && motion
}

@Composable
fun NativeCompletionCelebration(completion: String, language: ContentLanguage, onPop: (String) -> Unit,
    modifier: Modifier = Modifier, enabled: Boolean = true, reducedMotion: Boolean = false) {
    var state by remember(completion) {mutableStateOf(CelebrationState.create(completion))}
    val motion = celebrationMotion() && !reducedMotion
    val bob: State<Float> = if(motion && state.slots.any {it.phase == BalloonPhase.FLOATING}) {
        rememberInfiniteTransition(label = "gentle balloons").animateFloat(0f, 1f,
            infiniteRepeatable(tween(6000, easing = LinearEasing)), label = "float")
    } else rememberUpdatedState(0f)
    Row(modifier.clipToBounds().testTag("completion-celebration").semantics {
        contentDescription = if(language == ContentLanguage.GERMAN) "Freiwillige Luftballon-Feier" else "Optional balloon celebration"
    }, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        state.slots.forEach { slot -> key(completion, slot.id) {
            RewardBalloon(slot, language, bob, motion, enabled, Modifier.weight(1f).fillMaxHeight(),
                pop = {val next = state.pop(slot.id); if(next !== state) {state = next; onPop(completion)}},
                settle = {state = state.settle(slot.id)})
        } }
    }
}

private val balloonColours = listOf(Color(0xFFE97886), Color(0xFF70B5E8), Color(0xFFF0BF56), Color(0xFF82C79A), Color(0xFFAD91D9))

@Composable
private fun RewardBalloon(slot: BalloonSlot, language: ContentLanguage, bob: State<Float>, motion: Boolean, enabled: Boolean,
    modifier: Modifier, pop: () -> Unit, settle: () -> Unit) {
    val progress = remember {Animatable(if(slot.phase == BalloonPhase.SETTLED) 1f else 0f)}
    LaunchedEffect(slot.phase, motion) {
        if(slot.phase == BalloonPhase.REVEALING) {
            if(motion) progress.animateTo(1f, tween(720, easing = LinearEasing)) else progress.snapTo(1f)
            settle()
        }
    }
    val animal = VocabularyContent.item(slot.animal)
    val floating = slot.phase == BalloonPhase.FLOATING
    val label = if(language == ContentLanguage.GERMAN) "Luftballon ${slot.id + 1} platzen lassen" else "Pop balloon ${slot.id + 1}"
    val interaction = if(floating) Modifier.clickable(enabled = enabled, role = Role.Button, onClickLabel = label, onClick = pop) else Modifier
    Box(modifier.then(interaction).clipToBounds().testTag("balloon-${slot.id}").semantics {
        contentDescription = if(floating) label else animal.text.display[language]
        stateDescription = if(floating) {
            if(language == ContentLanguage.GERMAN) "Luftballon" else "Balloon"
        } else if(language == ContentLanguage.GERMAN) "Tier entdeckt" else "Animal revealed"
    }, contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val p = progress.value
            val cx = size.width / 2
            val cy = size.height * .58f
            val radius = min(size.width * .37f, 28.dp.toPx())
            val offset = if(motion && floating) sin((bob.value * 2 * PI + slot.id * .9)).toFloat() * 5.dp.toPx() else 0f
            if(floating || p < .14f) {
                val anticipation = if(floating) 1f else 1f + .07f * sin(p / .14f * PI).toFloat()
                val r = radius * anticipation
                drawLine(Color(0xFF708090), Offset(cx, cy + r + offset), Offset(cx - 3, min(size.height - 6, cy + r + 20.dp.toPx()) + offset), 1.dp.toPx())
                drawOval(balloonColours[slot.id], Offset(cx-r, cy-r*1.25f+offset), Size(r*2,r*2.5f))
                drawOval(Color.White.copy(alpha=.28f),Offset(cx-r*.55f,cy-r*.8f+offset),Size(r*.3f,r*.55f))
            }
            if(motion && !floating && p in .14f.. .78f) {
                val burst = (p - .14f) / .64f
                repeat(8) { piece ->
                    val angle = piece * PI / 4 + slot.id
                    val distance = radius * (0.25f + burst * 1.3f)
                    val x = cx + cos(angle).toFloat() * distance
                    val y = cy - radius*.5f + sin(angle).toFloat() * distance + burst * 10.dp.toPx()
                    drawRect(balloonColours[(piece+slot.id)%5].copy(alpha=1f-burst),Offset(x,y),Size(3.dp.toPx(),5.dp.toPx()))
                }
            }
        }
        if(!floating) {
            val revealModifier = Modifier.fillMaxWidth().heightIn(max = 88.dp).padding(2.dp).graphicsLayer {
            val p = progress.value
            alpha = if(p < .14f && motion) 0f else 1f
            val jump = ((p - .14f)/.86f).coerceIn(0f,1f)
            translationY = if(motion) -sin(jump * PI).toFloat() * 22.dp.toPx() else 0f
            scaleX = if(motion) .85f + .15f * jump else 1f; scaleY = scaleX
            }
            val picture = LocalCelebrationArt.current[slot.animal]
            if(picture != null) Image(picture, null, revealModifier.testTag("animal-art-${slot.id}"), contentScale = ContentScale.Fit)
            else Text(animal.text.display[language], modifier = revealModifier.clearAndSetSemantics {}, style = MaterialTheme.typography.labelSmall)
        }
    }
}
