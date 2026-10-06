package com.bellfamily.bastischool.ui.numberorder

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
import com.bellfamily.bastischool.learning.numberorder.NumberOrderContent
import com.bellfamily.bastischool.learning.models.ContentLanguage
import com.bellfamily.bastischool.learning.session.*
import com.bellfamily.bastischool.ui.common.*
import com.bellfamily.bastischool.learning.subitising.SubitisingContent
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape

/** The entire static sequence is one reading target; tiles never accept input. */
@Composable
private fun NumberSequence(item: NumberOrderContent.Missing, language: ContentLanguage) {
    Row(Modifier.fillMaxWidth().testTag("number-order-sequence").clearAndSetSemantics {
        contentDescription = NumberOrderContent.sequenceDescription(item, language)
    }, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        item.tiles.forEach { number ->
            Box(Modifier.weight(1f).heightIn(min = 72.dp)
                .background(Color.White, RoundedCornerShape(8.dp))
                .border(1.dp, Color(0xFF526575), RoundedCornerShape(8.dp))
                .padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
                Text(number?.toString() ?: "_", style = MaterialTheme.typography.headlineMedium)
            }
        }
    }
}

@Composable
fun NumberOrderScreen(state: SessionState?, language: ContentLanguage, busy: Boolean, saveFailed: Boolean, audioFailed: Boolean,
    onAction: (SessionAction) -> Unit, onReplay: () -> Unit, onAgain: () -> Unit, onRetry: () -> Unit, onHome: () -> Unit,
    modifier: Modifier = Modifier, onPop: (String) -> Unit = {}) {
    fun t(en: String, de: String) = if (language == ContentLanguage.GERMAN) de else en
    val ready = state != null && state.language == language && !busy && !saveFailed
    if (state?.phase == SessionPhase.COMPLETED) {
        NativeCompletionScreen(state.plan.id.value, language, state.plan.completionText.display[language], ready,
            "number-order-", onReplay, onAgain, onHome, onPop, modifier, saveFailed, onRetry, audioFailed)
        return
    }
    Column(modifier.fillMaxSize().background(Color(0xFFEAF7FC)).verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        NativeActivityTitle(NumberOrderContent.title.display[language])
        if (state == null) Text(t("Opening…", "Wird geöffnet…")) else {
            NativeQuestionProgress(state.index + 1, state.plan.tasks.size, language)
            Text(state.task.question.instruction.display[language], style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.testTag("number-order-prompt"))
            when (val item = NumberOrderContent.definition(state.task.question)) {
                is NumberOrderContent.Neighbour -> Text(item.anchor.toString(),
                    style = MaterialTheme.typography.displayLarge, modifier = Modifier.testTag("number-order-anchor"))
                is NumberOrderContent.Missing -> NumberSequence(item, language)
            }
            NativeActionButton(t("Listen again", "Noch einmal hören"), NativeActionRole.SECONDARY, onReplay,
                Modifier.fillMaxWidth().testTag("number-order-replay"), ready)
            state.task.question.choices.forEach { id ->
                NativeTextChoice(SubitisingContent.quantity(id).toString(),
                    { state.nextAttempt?.let { onAction(SessionAction.Answer(it, id)) } },
                    Modifier.fillMaxWidth().testTag("number-order-answer-${id.value.substringAfter('.') }"),
                    ready && state.current.answer == AnswerState.UNANSWERED && state.nextAttempt != null)
            }
            if (state.current.support.hint) NativeSupportMessage(state.task.question.hint!!.display[language])
            if (!state.current.locked) NativeActionButton(t("Help", "Hilfe"), NativeActionRole.SECONDARY,
                { onAction(SessionAction.Hint(state.task.id)) }, Modifier.testTag("number-order-help"), ready)
            when (state.current.answer) {
                AnswerState.RETRY_AVAILABLE -> {
                    NativeSupportMessage(state.task.question.wrongFeedback.display[language])
                    NativeActionButton(t("Try again", "Nochmal versuchen"), NativeActionRole.SECONDARY,
                        { onAction(SessionAction.Retry(AttemptId(state.task.id, state.current.attempts))) }, Modifier.testTag("number-order-retry"), ready)
                }
                AnswerState.CORRECT -> {
                    NativeSupportMessage(state.task.question.correctFeedback.display[language])
                    NativeActionButton(t("Next", "Weiter"), NativeActionRole.PRIMARY,
                        { onAction(SessionAction.Next(state.task.id)) }, Modifier.fillMaxWidth().testTag("number-order-next"), ready)
                }
                else -> Unit
            }
        }
        if (saveFailed) {
            NativeSupportMessage(t("Your saved work is kept. Please try again.", "Deine gespeicherte Arbeit bleibt erhalten. Versuche es erneut."))
            NativeActionButton(t("Try again", "Erneut versuchen"), NativeActionRole.SECONDARY, onRetry,
                Modifier.testTag("number-order-save-retry"), !busy)
        }
        if (audioFailed) Text(t("Speech is unavailable. Check installed offline voices.", "Die Sprachausgabe ist nicht verfügbar. Prüfe installierte Offline-Stimmen."))
        NativeActionButton(t("Back home", "Zurück zum Start"), NativeActionRole.NAVIGATION, onHome, Modifier.fillMaxWidth().testTag("number-order-home"))
    }
}
