package com.bellfamily.bastischool.ui.comparequantity

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
import com.bellfamily.bastischool.learning.comparequantity.CompareQuantityContent
import com.bellfamily.bastischool.learning.models.ContentLanguage
import com.bellfamily.bastischool.learning.session.*
import com.bellfamily.bastischool.ui.common.*
import com.bellfamily.bastischool.ui.subitising.DotPattern

@Composable
private fun ComparisonGroup(n: Int, left: Boolean, language: ContentLanguage, modifier: Modifier) {
    val side = if (left) "left" else "right"
    Column(modifier.testTag("compare-quantity-$side").clearAndSetSemantics {
        contentDescription = CompareQuantityContent.groupDescription(n, left, language)
    }, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(if (language == ContentLanguage.GERMAN) { if (left) "Links" else "Rechts" } else { if (left) "Left" else "Right" },
            style = MaterialTheme.typography.titleMedium)
        BoxWithConstraints(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            // Equal weighted columns produce identical square fields and dot size on both sides.
            DotPattern(n, language, Modifier.size(maxWidth.coerceAtMost(224.dp)))
        }
    }
}

@Composable
fun CompareQuantityScreen(state: SessionState?, language: ContentLanguage, busy: Boolean, saveFailed: Boolean, audioFailed: Boolean,
    onAction: (SessionAction) -> Unit, onReplay: () -> Unit, onAgain: () -> Unit, onRetry: () -> Unit, onHome: () -> Unit,
    modifier: Modifier = Modifier, onPop: (String) -> Unit = {}) {
    fun t(en: String, de: String) = if (language == ContentLanguage.GERMAN) de else en
    val ready = state != null && state.language == language && !busy && !saveFailed
    if (state?.phase == SessionPhase.COMPLETED) {
        NativeCompletionScreen(state.plan.id.value, language, state.plan.completionText.display[language], ready,
            "compare-quantity-", onReplay, onAgain, onHome, onPop, modifier, saveFailed, onRetry, audioFailed)
        return
    }
    Column(modifier.fillMaxSize().background(Color(0xFFEAF7FC)).verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        NativeActivityTitle(CompareQuantityContent.title.display[language])
        if (state == null) Text(t("Opening…", "Wird geöffnet…")) else {
            NativeQuestionProgress(state.index + 1, state.plan.tasks.size, language)
            Text(state.task.question.instruction.display[language], style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.testTag("compare-quantity-prompt"))
            val pair = CompareQuantityContent.definition(state.task.question)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                ComparisonGroup(pair.left, true, language, Modifier.weight(1f))
                ComparisonGroup(pair.right, false, language, Modifier.weight(1f))
            }
            NativeActionButton(t("Listen again", "Noch einmal hören"), NativeActionRole.SECONDARY, onReplay,
                Modifier.fillMaxWidth().testTag("compare-quantity-replay"), ready)
            state.task.question.choices.forEach { id ->
                NativeTextChoice(CompareQuantityContent.label(id).display[language],
                    { state.nextAttempt?.let { onAction(SessionAction.Answer(it, id)) } },
                    Modifier.fillMaxWidth().testTag("compare-quantity-answer-${id.value.substringAfter('.') }"),
                    ready && state.current.answer == AnswerState.UNANSWERED && state.nextAttempt != null)
            }
            if (state.current.support.hint) NativeSupportMessage(CompareQuantityContent.help.display[language])
            if (!state.current.locked) NativeActionButton(t("Help", "Hilfe"), NativeActionRole.SECONDARY,
                { onAction(SessionAction.Hint(state.task.id)) }, Modifier.testTag("compare-quantity-help"), ready)
            when (state.current.answer) {
                AnswerState.RETRY_AVAILABLE -> {
                    NativeSupportMessage(state.task.question.wrongFeedback.display[language])
                    NativeActionButton(t("Try again", "Nochmal versuchen"), NativeActionRole.SECONDARY,
                        { onAction(SessionAction.Retry(AttemptId(state.task.id, state.current.attempts))) }, Modifier.testTag("compare-quantity-retry"), ready)
                }
                AnswerState.CORRECT -> {
                    NativeSupportMessage(state.task.question.correctFeedback.display[language])
                    NativeActionButton(t("Next", "Weiter"), NativeActionRole.PRIMARY,
                        { onAction(SessionAction.Next(state.task.id)) }, Modifier.fillMaxWidth().testTag("compare-quantity-next"), ready)
                }
                else -> Unit
            }
        }
        if (saveFailed) {
            NativeSupportMessage(t("Your saved work is kept. Please try again.", "Deine gespeicherte Arbeit bleibt erhalten. Versuche es erneut."))
            NativeActionButton(t("Try again", "Erneut versuchen"), NativeActionRole.SECONDARY, onRetry,
                Modifier.testTag("compare-quantity-save-retry"), !busy)
        }
        if (audioFailed) Text(t("Speech is unavailable. Check installed offline voices.", "Die Sprachausgabe ist nicht verfügbar. Prüfe installierte Offline-Stimmen."))
        NativeActionButton(t("Back home", "Zurück zum Start"), NativeActionRole.NAVIGATION, onHome, Modifier.fillMaxWidth().testTag("compare-quantity-home"))
    }
}
