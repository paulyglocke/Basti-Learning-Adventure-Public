package com.bellfamily.bastischool.ui.followinstructions

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.bellfamily.bastischool.learning.follow.*
import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.session.*
import com.bellfamily.bastischool.ui.common.*

@Composable
fun FollowLevelsScreen(language: ContentLanguage, onOne: () -> Unit, onTwo: () -> Unit,
    onHome: () -> Unit, modifier: Modifier = Modifier) {
    fun t(en: String, de: String) = if (language == ContentLanguage.GERMAN) de else en
    Column(modifier.fillMaxSize().background(Color(0xFFEAF7FC)).verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        NativeActivityTitle(t("Follow the Instructions", "Anweisungen folgen"))
        NativeSupportMessage(t("Choose how many things to do.", "Wähle, wie viele Dinge du tun möchtest."))
        NativeActionButton(t("Level 1 — One instruction", "Stufe 1 — Eine Anweisung"), NativeActionRole.NAVIGATION,
            onOne, Modifier.fillMaxWidth().heightIn(min = 72.dp).testTag("follow-level-one"))
        NativeActionButton(t("Level 2 — Two in order", "Stufe 2 — Zwei nacheinander"), NativeActionRole.NAVIGATION,
            onTwo, Modifier.fillMaxWidth().heightIn(min = 72.dp).testTag("follow-level-two"))
        NativeActionButton(t("Home", "Startseite"), NativeActionRole.NAVIGATION, onHome,
            Modifier.fillMaxWidth().testTag("follow-level-home"))
    }
}

@Composable
fun FollowTwoScreen(state: FollowTwoState?, language: ContentLanguage, images: Map<ContentId, ImageBitmap>,
    busy: Boolean, saveFailed: Boolean, audioFailed: Boolean, onAction: (FollowTwoAction) -> Unit,
    onReplay: () -> Unit, onExample: () -> Unit, onAgain: () -> Unit, onRetrySave: () -> Unit,
    onHome: () -> Unit, modifier: Modifier = Modifier, onPop: (String) -> Unit = {}, onLevels: (() -> Unit)? = null) {
    fun t(en: String, de: String) = if (language == ContentLanguage.GERMAN) de else en
    val ready = state != null && state.language == language && !busy && !saveFailed
    if (state?.completed == true) {
        NativeCompletionScreen(state.id.value, language, FollowTwo.completion.display[language], ready,
            "follow-two-", onReplay, onAgain, onHome, onPop, modifier, saveFailed, onRetrySave, audioFailed)
        return
    }
    Column(modifier.fillMaxSize().background(Color(0xFFEAF7FC)).verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        NativeActivityTitle(t("Two in order", "Zwei nacheinander"))
        if (state == null) {
            if (!saveFailed) Text(t("Opening…", "Wird geöffnet…"))
        } else {
            Text(t("Question ${state.questionIndex + 1} of ${state.round.count}",
                "Frage ${state.questionIndex + 1} von ${state.round.count}"),
                style = MaterialTheme.typography.titleMedium, modifier = Modifier.testTag("follow-two-progress"))
            if (state.questionIndex == 0 && state.step == 0) {
                NativeSupportMessage(FollowTwo.example.display[language], Modifier.testTag("follow-two-example"))
                NativeSupportMessage(FollowTwo.demonstration.display[language], Modifier.testTag("follow-two-demo-text"))
                Row(Modifier.fillMaxWidth().testTag("follow-two-demo"), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("dinosaur", "horse").forEachIndexed { index, name ->
                        val objectDef = FollowContent.objects.first { it.id.value.endsWith(name) }
                        Column(Modifier.weight(1f).background(Color.White, RoundedCornerShape(12.dp)).padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("${index + 1}.", style = MaterialTheme.typography.titleMedium)
                            images[objectDef.id]?.let { image ->
                                Image(image, null, Modifier.size(64.dp).testTag("follow-two-demo-${objectDef.id.value}"))
                            }
                            Text(objectDef.text.display[language], style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
                NativeActionButton(t("Hear the example", "Beispiel anhören"), NativeActionRole.SECONDARY,
                    onExample, Modifier.fillMaxWidth().testTag("follow-two-demo-listen"), ready)
            }
            Text(FollowTwo.instruction(state.task).display[language], Modifier.fillMaxWidth().testTag("follow-two-instruction"),
                style = MaterialTheme.typography.titleLarge)
            NativeActionButton(t("Listen again", "Noch einmal hören"), NativeActionRole.SECONDARY, onReplay,
                Modifier.fillMaxWidth().testTag("follow-two-replay"), ready)
            NativeSupportMessage(if (state.step == 0) t("First action", "Erste Handlung")
                else t("First action done. Now do the second.", "Erste Handlung geschafft. Jetzt kommt die zweite."),
                Modifier.testTag("follow-two-step"))
            FollowTwoChoices(state, language, images, ready && state.current.answer == AnswerState.UNANSWERED, onAction)
            if (state.current.support.hint && !state.readyToContinue)
                NativeSupportMessage(t(if (state.step == 0) "Listen for the animal named first." else "Listen again for the second animal.",
                    if (state.step == 0) "Hör auf das zuerst genannte Tier." else "Hör noch einmal auf das zweite Tier."),
                    Modifier.testTag("follow-two-hint-message"))
            if (state.current.answer == AnswerState.RETRY_AVAILABLE) {
                NativeSupportMessage(t("Not quite. Listen again and try this step.",
                    "Noch nicht ganz. Hör noch einmal zu und versuche diesen Schritt erneut."),
                    Modifier.testTag("follow-two-feedback"))
                NativeActionButton(t("Try again", "Nochmal versuchen"), NativeActionRole.SECONDARY,
                    { onAction(FollowTwoAction.Retry(AttemptId(state.taskId, state.steps[state.questionIndex].sumOf { it.retries } + 1))) },
                    Modifier.fillMaxWidth().testTag("follow-two-retry"), ready)
            }
            if (state.readyToContinue) {
                NativeSupportMessage(t("Both steps done. Continue when you are ready.",
                    "Beide Schritte geschafft. Mach weiter, wenn du bereit bist."))
                NativeActionButton(t(if (state.questionIndex == state.round.count - 1) "Finish" else "Next",
                    if (state.questionIndex == state.round.count - 1) "Fertig" else "Weiter"),
                    NativeActionRole.PRIMARY, { onAction(FollowTwoAction.Next(state.taskId)) },
                    Modifier.fillMaxWidth().testTag("follow-two-next"), ready)
            } else if (state.current.answer == AnswerState.UNANSWERED) {
                NativeActionButton(t("Hint", "Tipp"), NativeActionRole.SECONDARY,
                    { onAction(FollowTwoAction.Hint(state.taskId)) }, Modifier.fillMaxWidth().testTag("follow-two-hint"), ready)
            }
        }
        if (saveFailed) {
            NativeSupportMessage(t("Your saved work is kept. Please try again.", "Deine gespeicherte Arbeit bleibt erhalten. Versuch es erneut."))
            NativeActionButton(t("Try again", "Erneut versuchen"), NativeActionRole.SECONDARY, onRetrySave,
                Modifier.fillMaxWidth().testTag("follow-two-save-retry"), !busy)
        }
        if (audioFailed) NativeSupportMessage(t("Speech is unavailable. Check installed offline voices in Options.",
            "Die Sprachausgabe ist nicht verfügbar. Prüfe die Offline-Stimmen in den Optionen."))
        NativeActionButton(t("Home", "Startseite"), NativeActionRole.NAVIGATION, onHome,
            Modifier.fillMaxWidth().testTag("follow-two-home"))
        if (onLevels != null) NativeActionButton(t("Choose a level", "Stufe wählen"), NativeActionRole.NAVIGATION,
            onLevels, Modifier.fillMaxWidth().testTag("follow-two-levels"))
    }
}

@Composable
private fun FollowTwoChoices(state: FollowTwoState, language: ContentLanguage,
    images: Map<ContentId, ImageBitmap>, enabled: Boolean, onAction: (FollowTwoAction) -> Unit) {
    BoxWithConstraints(Modifier.fillMaxWidth().testTag("follow-two-choices")) {
        val columns = (maxWidth / (160.dp * LocalDensity.current.fontScale)).toInt().coerceIn(1, 2)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            state.task.choices.chunked(columns).forEach { group ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    group.forEach { id ->
                        val objectDef = FollowContent.objectFor(id)
                        val image = images[id]
                        val firstDone = state.step == 1 && id == state.task.first
                        val description = objectDef.text.display[language] + if (firstDone)
                            if (language == ContentLanguage.GERMAN) ", erster Schritt geschafft" else ", first step done" else ""
                        val shape = RoundedCornerShape(16.dp)
                        Column(Modifier.weight(1f).heightIn(min = 150.dp).background(Color.White, shape)
                            .then(if (firstDone) Modifier.border(3.dp, Color(0xFF275B8E), shape) else Modifier)
                            .testTag("follow-two-tap-${id.value}")
                            .clickable(enabled = enabled && image != null, role = Role.Button) {
                                state.nextAttempt?.let { onAction(FollowTwoAction.Choose(it, id)) }
                            }.semantics { contentDescription = description }.padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center) {
                            if (image != null) Image(image, null, Modifier.fillMaxWidth().height(96.dp).testTag("follow-two-art-${id.value}"),
                                contentScale = ContentScale.Fit)
                            else Text(if (language == ContentLanguage.GERMAN) "Bild nicht verfügbar" else "Picture unavailable")
                            Text(objectDef.text.display[language], style = MaterialTheme.typography.titleMedium)
                        }
                    }
                    repeat(columns - group.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }
    }
}
