package com.bellfamily.bastischool.ui.letters

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.bellfamily.bastischool.learning.letters.LettersContent
import com.bellfamily.bastischool.learning.models.ContentId
import com.bellfamily.bastischool.learning.models.ContentLanguage
import com.bellfamily.bastischool.learning.session.*
import com.bellfamily.bastischool.ui.common.*

@Composable
fun LettersScreen(
    state: SessionState?, language: ContentLanguage, images: Map<ContentId, ImageBitmap>,
    busy: Boolean, saveFailed: Boolean, audioFailed: Boolean,
    onAction: (SessionAction) -> Unit, onOption: (ContentId) -> Unit,
    onReplay: () -> Unit, onAgain: () -> Unit,
    onRetry: () -> Unit, onHome: () -> Unit, modifier: Modifier = Modifier, onPop: (String) -> Unit = {}
) {
    fun t(en: String, de: String) = if (language == ContentLanguage.GERMAN) de else en
    val ready = state != null && state.language == language && !busy && !saveFailed
    if (state?.phase == SessionPhase.COMPLETED) {
        NativeCompletionScreen(state.plan.id.value, language, state.plan.completionText.display[language], ready,
            "letters-", onReplay, onAgain, onHome, onPop, modifier, saveFailed, onRetry, audioFailed)
        return
    }
    Column(modifier.fillMaxSize().background(Color(0xFFEAF7FC)).verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        NativeActivityTitle(t("Animal First Letters", "Tier-Anfangsbuchstaben"))
        if (state == null) {
            Text(t("Opening…", "Wird geöffnet…"))
        } else {
            val question = state.task.question
            val targetLetter = LettersContent.letter(question.correct, language)
            NativeQuestionProgress(state.index + 1, state.plan.tasks.size, language)
            Text(question.instruction.display[language], style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.testTag("letters-prompt"))
            Surface(color = Color(0xFFFFF0C2), shape = RoundedCornerShape(28.dp), tonalElevation = 2.dp,
                modifier = Modifier.size(104.dp).testTag("letters-letter-card"), contentColor = Color(0xFF4A3820)) {
                Box(contentAlignment = Alignment.Center) {
                    Text(targetLetter, style = MaterialTheme.typography.displayMedium)
                }
            }
            NativeActionButton(t("Listen again", "Noch einmal hören"), NativeActionRole.SECONDARY, onReplay,
                Modifier.fillMaxWidth().testTag("letters-replay"), ready)
            val choices = question.choices.chunked(2)
            choices.forEach { row ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    row.forEach { id ->
                        val animal = LettersContent.animal(id)
                        val label = animal.name.display[language]
                        val image = images[id]
                        Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                            Card(onClick = {
                                state.nextAttempt?.let { onAction(SessionAction.Answer(it, id)) }
                            }, enabled = ready && state.current.answer == AnswerState.UNANSWERED && state.nextAttempt != null,
                                modifier = Modifier.fillMaxWidth().testTag("letters-answer-${id.value.substringAfter('.')}")
                                    .semantics { contentDescription = label },
                                colors = CardDefaults.cardColors(containerColor = Color.White)) {
                                Column(Modifier.fillMaxWidth().padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    if (image != null) {
                                        androidx.compose.foundation.Image(image, contentDescription = label,
                                            contentScale = ContentScale.Fit, modifier = Modifier.fillMaxWidth().height(108.dp)
                                                .clip(RoundedCornerShape(16.dp)))
                                    } else {
                                        Spacer(Modifier.fillMaxWidth().height(108.dp))
                                    }
                                    Text(label, style = MaterialTheme.typography.titleMedium)
                                }
                            }
                            TextButton(onClick = { onOption(id) }, enabled = ready,
                                modifier = Modifier.testTag("letters-listen-${id.value.substringAfter('.')}") ) {
                                Text(t("Listen", "Anhören"))
                            }
                        }
                    }
                    if (row.size == 1) Spacer(Modifier.weight(1f))
                }
            }
            if (state.current.support.hint && question.hint != null)
                NativeSupportMessage(question.hint.display[language])
            if (!state.current.locked) NativeActionButton(t("Help", "Hilfe"), NativeActionRole.SECONDARY,
                { onAction(SessionAction.Hint(state.task.id)) }, Modifier.testTag("letters-help"), ready)
            when (state.current.answer) {
                AnswerState.RETRY_AVAILABLE -> {
                    NativeSupportMessage(question.wrongFeedback.display[language])
                    NativeActionButton(t("Try again", "Nochmal versuchen"), NativeActionRole.SECONDARY,
                        { onAction(SessionAction.Retry(AttemptId(state.task.id, state.current.attempts))) }, Modifier.testTag("letters-retry"), ready)
                }
                AnswerState.CORRECT -> {
                    NativeSupportMessage(question.correctFeedback.display[language])
                    NativeActionButton(t("Next", "Weiter"), NativeActionRole.PRIMARY,
                        { onAction(SessionAction.Next(state.task.id)) }, Modifier.fillMaxWidth().testTag("letters-next"), ready)
                }
                else -> Unit
            }
        }
        if (saveFailed) {
            NativeSupportMessage(t("Your saved work is kept. Please try again.", "Deine gespeicherte Arbeit bleibt erhalten. Versuche es erneut."))
            NativeActionButton(t("Try again", "Erneut versuchen"), NativeActionRole.SECONDARY, onRetry,
                Modifier.testTag("letters-save-retry"), !busy)
        }
        if (audioFailed) Text(t("Speech is unavailable. Check installed offline voices.", "Die Sprachausgabe ist nicht verfügbar. Prüfe installierte Offline-Stimmen."))
        NativeActionButton(t("Back home", "Zurück zum Start"), NativeActionRole.NAVIGATION, onHome,
            Modifier.fillMaxWidth().testTag("letters-home"))
    }
}
