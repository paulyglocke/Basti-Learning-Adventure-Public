package com.bellfamily.bastischool.ui.colours

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.bellfamily.bastischool.learning.colours.ColoursContent
import com.bellfamily.bastischool.learning.models.ContentLanguage
import com.bellfamily.bastischool.learning.session.*
import com.bellfamily.bastischool.ui.common.*
import com.bellfamily.bastischool.ui.design.*

@Composable
fun FindColourScreen(
    state: SessionState?,
    language: ContentLanguage,
    busy: Boolean,
    saveFailed: Boolean,
    audioFailed: Boolean,
    onReplay: () -> Unit,
    onHelp: () -> Unit,
    onAction: (SessionAction) -> Unit,
    onAgain: () -> Unit,
    onRetry: () -> Unit,
    onHome: () -> Unit,
    modifier: Modifier = Modifier,
    onPop: (String) -> Unit = {}
) {
    val german = language == ContentLanguage.GERMAN
    fun t(en: String, de: String) = if (german) de else en
    val ready = !busy && !saveFailed && state?.language == language
    if (state?.phase == SessionPhase.COMPLETED) {
        NativeCompletionScreen(state.plan.id.value, language, state.plan.completionText.display[language], ready,
            "colour-find-", onReplay, onAgain, onHome, onPop, modifier, saveFailed, onRetry, audioFailed)
        return
    }
    Column(modifier.fillMaxSize().background(BastiPalette.background).verticalScroll(rememberScrollState())
        .padding(horizontal = BastiSpacing.medium, vertical = BastiSpacing.medium),
        verticalArrangement = Arrangement.spacedBy(BastiSpacing.small)) {
        Text(t("Find the Colour", "Finde die Farbe"), Modifier.fillMaxWidth().testTag("colour-find-title"),
            style = BastiTypography.screenHeading, color = BastiPalette.ink)
        if (state == null) {
            Text(t("Opening…", "Wird geöffnet …"), Modifier.testTag("colour-find-loading"), color = BastiPalette.ink)
        } else {
            val task = state.task
            Text("${state.index + 1} ${t("of", "von")} ${state.plan.tasks.size}", Modifier.testTag("colour-find-progress"),
                style = BastiTypography.parentInformation, color = BastiPalette.ink)
            Text(task.question.instruction.display[language], Modifier.fillMaxWidth().testTag("colour-find-prompt"),
                style = BastiTypography.question, color = BastiPalette.ink)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(BastiSpacing.small)) {
                NativeActionButton(t("Listen again", "Noch einmal hören"), NativeActionRole.SECONDARY, onReplay,
                    Modifier.weight(1f).testTag("colour-find-replay"), ready)
                NativeActionButton(t("Help", "Hilfe"), NativeActionRole.SECONDARY, onHelp,
                    Modifier.weight(1f).testTag("colour-find-help"), ready && state.hintAvailable)
            }
            ColoursContent.order.chunked(2).forEach { row ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(BastiSpacing.small)) {
                    row.forEach { id ->
                        val colour = ColoursContent.definition(id)
                        val label = colour.text.display[language].replaceFirstChar { it.uppercase() }
                        val selected = state.current.lastChoice == id
                        Surface(onClick = {
                            state.nextAttempt?.let { attempt -> onAction(SessionAction.Answer(attempt, id)) }
                        }, enabled = ready && state.current.answer == AnswerState.UNANSWERED,
                            modifier = Modifier.weight(1f).heightIn(min = 124.dp).testTag("colour-find-choice-${id.value}")
                                .semantics { role = Role.Button; contentDescription = label },
                            shape = BastiShapes.control, color = BastiPalette.surface,
                            border = BorderStroke(if (selected) 4.dp else 1.dp,
                                if (selected) BastiPalette.ink else BastiPalette.outline)) {
                            Box(Modifier.fillMaxWidth().padding(BastiSpacing.small), contentAlignment = Alignment.Center) {
                                Box(Modifier.fillMaxWidth().heightIn(min = 88.dp, max = 112.dp)
                                    .background(ColourSwatches.color(id), RoundedCornerShape(14.dp))
                                    .border(2.dp, BastiPalette.ink, RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) {
                                    if (selected) Text("✓", style = BastiTypography.answer,
                                        color = if (id == ColoursContent.yellow) BastiPalette.ink else MaterialTheme.colorScheme.surface)
                                }
                            }
                        }
                    }
                }
            }
            val current = state.current
            if (current.support.hint) {
                task.question.hint?.let { hint ->
                    NativeSupportMessage(hint.display[language], Modifier.testTag("colour-find-hint-message"))
                }
            }
            if (current.answer == AnswerState.RETRY_AVAILABLE) {
                NativeSupportMessage(task.question.wrongFeedback.display[language], Modifier.testTag("colour-find-feedback-wrong"))
                NativeActionButton(t("Try again", "Versuch es erneut"), NativeActionRole.PRIMARY, {
                    onAction(SessionAction.Retry(AttemptId(task.id, current.attempts)))
                }, Modifier.fillMaxWidth().testTag("colour-find-retry"), ready)
            } else if (current.answer == AnswerState.CORRECT) {
                NativeSupportMessage(task.question.correctFeedback.display[language], Modifier.testTag("colour-find-feedback-correct"))
                NativeActionButton(t("Next", "Weiter"), NativeActionRole.PRIMARY,
                    { onAction(SessionAction.Next(task.id)) }, Modifier.fillMaxWidth().testTag("colour-find-next"), ready)
            }
        }
        if (saveFailed) {
            NativeSupportMessage(t("Your saved round is safe. Try again.", "Deine gespeicherte Runde ist sicher. Versuche es noch einmal."),
                Modifier.testTag("colour-find-error"))
            NativeActionButton(t("Try again", "Erneut versuchen"), NativeActionRole.SECONDARY, onRetry,
                Modifier.fillMaxWidth().testTag("colour-find-retry-save"), enabled = !busy)
        }
        if (audioFailed) Text(t("Speech is unavailable. Check installed offline voices in Options.",
            "Die Sprachausgabe ist nicht verfügbar. Prüfe die Offline-Stimmen in den Optionen."), Modifier.testTag("colour-find-audio-error"))
        NativeActionButton(t("Home", "Startseite"), NativeActionRole.NAVIGATION, onHome,
            Modifier.fillMaxWidth().testTag("colour-find-home"))
    }
}
