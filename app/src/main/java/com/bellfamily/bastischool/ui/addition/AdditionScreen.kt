package com.bellfamily.bastischool.ui.addition

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.dp
import com.bellfamily.bastischool.learning.addition.AdditionContent
import com.bellfamily.bastischool.learning.models.ContentLanguage
import com.bellfamily.bastischool.learning.session.*
import com.bellfamily.bastischool.ui.common.*
import com.bellfamily.bastischool.ui.subitising.DotPattern
import com.bellfamily.bastischool.learning.subitising.SubitisingContent

@Composable
private fun AddendGroup(n: Int, left: Boolean, language: ContentLanguage, modifier: Modifier) {
    val side = if (left) "left" else "right"
    Column(modifier.testTag("addition-$side").clearAndSetSemantics {
        contentDescription = AdditionContent.groupDescription(n, left, language)
    }, horizontalAlignment = Alignment.CenterHorizontally) {
        BoxWithConstraints(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            // Equal weighted columns produce identical square fields and dot size on both sides.
            DotPattern(n, language, Modifier.size(maxWidth.coerceAtMost(224.dp)))
        }
    }
}

@Composable
fun AdditionScreen(state: SessionState?, language: ContentLanguage, busy: Boolean, saveFailed: Boolean, audioFailed: Boolean,
    onAction: (SessionAction) -> Unit, onReplay: () -> Unit, onAgain: () -> Unit, onRetry: () -> Unit, onHome: () -> Unit,
    modifier: Modifier = Modifier, onPop: (String) -> Unit = {}) {
    fun t(en: String, de: String) = if (language == ContentLanguage.GERMAN) de else en
    val ready = state != null && state.language == language && !busy && !saveFailed
    if (state?.phase == SessionPhase.COMPLETED) {
        NativeCompletionScreen(state.plan.id.value, language, state.plan.completionText.display[language], ready,
            "addition-", onReplay, onAgain, onHome, onPop, modifier, saveFailed, onRetry, audioFailed)
        return
    }
    Column(modifier.fillMaxSize().background(Color(0xFFEAF7FC)).verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        NativeActivityTitle(AdditionContent.title.display[language])
        if (state == null) Text(t("Opening…", "Wird geöffnet…")) else {
            NativeQuestionProgress(state.index + 1, state.plan.tasks.size, language)
            val pair = AdditionContent.definition(state.task.question)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                AddendGroup(pair.left, true, language, Modifier.weight(1f))
                Text("+", style = MaterialTheme.typography.headlineLarge,
                    modifier = Modifier.testTag("addition-plus").clearAndSetSemantics {})
                AddendGroup(pair.right, false, language, Modifier.weight(1f))
            }
            Text(state.task.question.instruction.display[language], style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.testTag("addition-prompt"))
            NativeActionButton(t("Listen again", "Noch einmal hören"), NativeActionRole.SECONDARY, onReplay,
                Modifier.fillMaxWidth().testTag("addition-replay"), ready)
            state.task.question.choices.forEach { id ->
                NativeTextChoice(SubitisingContent.quantity(id).toString(),
                    { state.nextAttempt?.let { onAction(SessionAction.Answer(it, id)) } },
                    Modifier.fillMaxWidth().testTag("addition-answer-${id.value.substringAfter('.') }"),
                    ready && state.current.answer == AnswerState.UNANSWERED && state.nextAttempt != null)
            }
            if (state.current.support.hint) NativeSupportMessage(AdditionContent.help.display[language])
            if (!state.current.locked) NativeActionButton(t("Help", "Hilfe"), NativeActionRole.SECONDARY,
                { onAction(SessionAction.Hint(state.task.id)) }, Modifier.testTag("addition-help"), ready)
            when (state.current.answer) {
                AnswerState.RETRY_AVAILABLE -> {
                    NativeSupportMessage(state.task.question.wrongFeedback.display[language])
                    NativeActionButton(t("Try again", "Nochmal versuchen"), NativeActionRole.SECONDARY,
                        { onAction(SessionAction.Retry(AttemptId(state.task.id, state.current.attempts))) }, Modifier.testTag("addition-retry"), ready)
                }
                AnswerState.CORRECT -> {
                    NativeSupportMessage(state.task.question.correctFeedback.display[language])
                    NativeActionButton(t("Next", "Weiter"), NativeActionRole.PRIMARY,
                        { onAction(SessionAction.Next(state.task.id)) }, Modifier.fillMaxWidth().testTag("addition-next"), ready)
                }
                else -> Unit
            }
        }
        if (saveFailed) {
            NativeSupportMessage(t("Your saved work is kept. Please try again.", "Deine gespeicherte Arbeit bleibt erhalten. Versuche es erneut."))
            NativeActionButton(t("Try again", "Erneut versuchen"), NativeActionRole.SECONDARY, onRetry,
                Modifier.testTag("addition-save-retry"), !busy)
        }
        if (audioFailed) Text(t("Speech is unavailable. Check installed offline voices.", "Die Sprachausgabe ist nicht verfügbar. Prüfe installierte Offline-Stimmen."))
        NativeActionButton(t("Back home", "Zurück zum Start"), NativeActionRole.NAVIGATION, onHome, Modifier.fillMaxWidth().testTag("addition-home"))
    }
}
