package com.bellfamily.bastischool.ui.followinstructions

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.bellfamily.bastischool.learning.follow.FollowContent
import com.bellfamily.bastischool.learning.models.ContentLanguage
import com.bellfamily.bastischool.learning.session.*
import com.bellfamily.bastischool.ui.common.*

@Composable
fun FollowScreen(state: SessionState?, language: ContentLanguage, images: Map<com.bellfamily.bastischool.learning.models.ContentId, ImageBitmap>, busy: Boolean, saveFailed: Boolean, audioFailed: Boolean,
    onAction: (SessionAction) -> Unit, onReplay: () -> Unit, onAgain: () -> Unit, onRetry: () -> Unit, onHome: () -> Unit,
    modifier: Modifier = Modifier, onPop: (String) -> Unit = {}) {
    fun t(en: String, de: String) = if (language == ContentLanguage.GERMAN) de else en
    val ready = !busy && !saveFailed && state != null && state.language == language
    if (state?.phase == SessionPhase.COMPLETED) {
        NativeCompletionScreen(state.plan.id.value, language, state.plan.completionText.display[language], ready, "follow-", onReplay, onAgain, onHome, onPop, modifier, saveFailed, onRetry, audioFailed)
        return
    }
    Column(modifier.fillMaxSize().background(Color(0xFFEAF7FC)).verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(t("Follow the Instructions", "Anweisungen folgen"), style = MaterialTheme.typography.headlineMedium)
        if (state == null) Text(t("Opening…", "Wird geöffnet…")) else {
            Text(t("Question ${state.index + 1} of ${state.plan.tasks.size}", "Frage ${state.index + 1} von ${state.plan.tasks.size}"), Modifier.testTag("follow-progress"))
            Text(state.task.question.instruction.display[language], modifier = Modifier.testTag("follow-instruction"), style = MaterialTheme.typography.titleLarge)
            NativeActionButton(t("Listen again", "Noch einmal hören"), NativeActionRole.SECONDARY, onReplay, Modifier.fillMaxWidth().testTag("follow-replay"), ready)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                state.task.question.choices.forEach { id ->
                    val objectDef = FollowContent.objectFor(id); val image = images[id]
                    Box(Modifier.weight(1f).height(150.dp).testTag("follow-object-${id.value}")) {
                        // The image itself is the tap target; never paint an opaque button over it.
                        Box(Modifier.fillMaxSize().testTag("follow-tap-${id.value}")
                            .clickable(enabled = image != null && ready && state.current.answer == AnswerState.UNANSWERED,
                                role = Role.Button) {
                                state.nextAttempt?.let { onAction(SessionAction.Answer(it, id)) }
                            }.semantics { contentDescription = objectDef.text.display[language] }) {
                            if (image != null) Image(image, contentDescription = null, modifier = Modifier.fillMaxSize().testTag("follow-art-${id.value}"), contentScale = ContentScale.Fit)
                            else Text(t("Picture unavailable", "Bild nicht verfügbar"), Modifier.align(Alignment.Center).testTag("follow-unavailable-${id.value}"))
                        }
                    }
                }
            }
            when (state.current.answer) {
                AnswerState.RETRY_AVAILABLE -> {
                    NativeSupportMessage(state.task.question.wrongFeedback.display[language])
                    NativeActionButton(t("Try again", "Nochmal versuchen"), NativeActionRole.SECONDARY, { onAction(SessionAction.Retry(AttemptId(state.task.id, state.current.attempts))) }, Modifier.testTag("follow-retry"), ready)
                }
                AnswerState.CORRECT -> {
                    NativeSupportMessage(state.task.question.correctFeedback.display[language])
                    NativeActionButton(t("Next", "Weiter"), NativeActionRole.PRIMARY, { onAction(SessionAction.Next(state.task.id)) }, Modifier.fillMaxWidth().testTag("follow-next"), ready)
                }
                else -> Unit
            }
        }
        if (saveFailed) NativeActionButton(t("Try again", "Erneut versuchen"), NativeActionRole.SECONDARY, onRetry, Modifier.testTag("follow-save-retry"), !busy)
        if (audioFailed) Text(t("Speech is unavailable. Check installed offline voices.", "Die Sprachausgabe ist nicht verfügbar. Prüfe installierte Offline-Stimmen."))
        NativeActionButton(t("Back home", "Zurück zum Start"), NativeActionRole.NAVIGATION, onHome, Modifier.fillMaxWidth().testTag("follow-home"))
    }
}
