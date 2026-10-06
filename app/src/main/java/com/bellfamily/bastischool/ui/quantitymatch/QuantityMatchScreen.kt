package com.bellfamily.bastischool.ui.quantitymatch

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.bellfamily.bastischool.learning.models.ContentLanguage
import com.bellfamily.bastischool.learning.quantitymatch.QuantityMatchContent
import com.bellfamily.bastischool.learning.session.*
import com.bellfamily.bastischool.learning.subitising.SubitisingContent
import com.bellfamily.bastischool.ui.common.*

@Composable
private fun MatchDots(quantity: Int, language: ContentLanguage, tag: String, decorative: Boolean = false) {
    val semantics = if (decorative) Modifier.clearAndSetSemantics {} else Modifier.semantics {
        contentDescription = SubitisingContent.description(quantity).display[language]
    }
    Canvas(Modifier.size(180.dp).background(Color.White).testTag(tag).then(semantics)) {
        SubitisingContent.pattern(quantity).forEach {
            drawCircle(Color(0xFF172B3A), size.minDimension * .07f, Offset(size.width * it.x, size.height * it.y))
        }
    }
}

@Composable
fun QuantityMatchScreen(state: SessionState?, language: ContentLanguage, busy: Boolean, saveFailed: Boolean, audioFailed: Boolean,
    onAction: (SessionAction) -> Unit, onReplay: () -> Unit, onAgain: () -> Unit, onRetry: () -> Unit, onHome: () -> Unit,
    modifier: Modifier = Modifier, onPop: (String) -> Unit = {}) {
    fun t(en: String, de: String) = if (language == ContentLanguage.GERMAN) de else en
    val ready = state != null && state.language == language && !busy && !saveFailed
    if (state?.phase == SessionPhase.COMPLETED) {
        NativeCompletionScreen(state.plan.id.value, language, state.plan.completionText.display[language], ready,
            "quantity-match-", onReplay, onAgain, onHome, onPop, modifier, saveFailed, onRetry, audioFailed)
        return
    }
    Column(modifier.fillMaxSize().background(Color(0xFFEAF7FC)).verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        NativeActivityTitle(t("Numbers & Groups", "Zahlen & Mengen"))
        if (state == null) Text(t("Opening…", "Wird geöffnet…")) else {
            NativeQuestionProgress(state.index + 1, state.plan.tasks.size, language)
            val (target, direction) = QuantityMatchContent.definition(state.task.question)
            val numeralToQuantity = direction == QuantityMatchContent.Direction.NUMERAL_TO_QUANTITY
            if (numeralToQuantity) {
                Text(target.toString(), style = MaterialTheme.typography.displayLarge, modifier = Modifier.testTag("quantity-match-numeral"))
            } else {
                MatchDots(target, language, "quantity-match-prompt-dots")
            }
            Text(state.task.question.instruction.display[language], style = MaterialTheme.typography.headlineSmall, modifier = Modifier.testTag("quantity-match-prompt"))
            NativeActionButton(t("Listen again", "Noch einmal hören"), NativeActionRole.SECONDARY, onReplay,
                Modifier.fillMaxWidth().testTag("quantity-match-replay"), ready)
            state.task.question.choices.forEach { id ->
                val choice = SubitisingContent.quantity(id)
                if (numeralToQuantity) {
                    OutlinedButton(
                        onClick = { state.nextAttempt?.let { onAction(SessionAction.Answer(it, id)) } },
                        enabled = ready && state.current.answer == AnswerState.UNANSWERED && state.nextAttempt != null,
                        modifier = Modifier.fillMaxWidth().testTag("quantity-match-answer-$choice").semantics {
                            contentDescription = SubitisingContent.description(choice).display[language]
                        }
                    ) {
                        MatchDots(choice, language, "quantity-match-choice-dots-$choice", decorative = true)
                    }
                } else {
                    NativeTextChoice(choice.toString(), { state.nextAttempt?.let { onAction(SessionAction.Answer(it, id)) } },
                        Modifier.fillMaxWidth().testTag("quantity-match-answer-$choice"), ready && state.current.answer == AnswerState.UNANSWERED && state.nextAttempt != null)
                }
            }
            if (state.current.support.hint) NativeSupportMessage(state.task.question.hint!!.display[language])
            if (!state.current.locked) NativeActionButton(t("Help", "Hilfe"), NativeActionRole.SECONDARY,
                { onAction(SessionAction.Hint(state.task.id)) }, Modifier.testTag("quantity-match-help"), ready)
            when (state.current.answer) {
                AnswerState.RETRY_AVAILABLE -> {
                    NativeSupportMessage(state.task.question.wrongFeedback.display[language])
                    NativeActionButton(t("Try again", "Nochmal versuchen"), NativeActionRole.SECONDARY,
                        { onAction(SessionAction.Retry(AttemptId(state.task.id, state.current.attempts))) }, Modifier.testTag("quantity-match-retry"), ready)
                }
                AnswerState.CORRECT -> {
                    NativeSupportMessage(state.task.question.correctFeedback.display[language])
                    NativeActionButton(t("Next", "Weiter"), NativeActionRole.PRIMARY,
                        { onAction(SessionAction.Next(state.task.id)) }, Modifier.fillMaxWidth().testTag("quantity-match-next"), ready)
                }
                else -> Unit
            }
        }
        if (saveFailed) {
            NativeSupportMessage(t("Your saved work is kept. Please try again.", "Deine gespeicherte Arbeit bleibt erhalten. Versuche es erneut."))
            NativeActionButton(t("Try again", "Erneut versuchen"), NativeActionRole.SECONDARY, onRetry, Modifier.testTag("quantity-match-save-retry"), !busy)
        }
        if (audioFailed) Text(t("Speech is unavailable. Check installed offline voices.", "Die Sprachausgabe ist nicht verfügbar. Prüfe installierte Offline-Stimmen."))
        NativeActionButton(t("Back home", "Zurück zum Start"), NativeActionRole.NAVIGATION, onHome, Modifier.fillMaxWidth().testTag("quantity-match-home"))
    }
}
