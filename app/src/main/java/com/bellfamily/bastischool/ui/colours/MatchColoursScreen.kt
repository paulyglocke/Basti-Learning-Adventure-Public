package com.bellfamily.bastischool.ui.colours

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import com.bellfamily.bastischool.learning.colours.*
import com.bellfamily.bastischool.learning.models.ContentLanguage
import com.bellfamily.bastischool.learning.session.*
import com.bellfamily.bastischool.ui.common.*
import com.bellfamily.bastischool.ui.design.*

@Composable
fun MatchColoursScreen(state: SessionState?, language: ContentLanguage, busy: Boolean, saveFailed: Boolean, audioFailed: Boolean,
    onReplay: () -> Unit, onHelp: () -> Unit, onAction: (SessionAction) -> Unit, onAgain: () -> Unit, onRetry: () -> Unit, onHome: () -> Unit,
    modifier: Modifier = Modifier, onPop: (String) -> Unit = {}) {
    fun t(en: String, de: String) = if (language == ContentLanguage.GERMAN) de else en
    val ready = state != null && !busy && !saveFailed && state.language == language
    if (state?.phase == SessionPhase.COMPLETED) {
        NativeCompletionScreen(state.plan.id.value, language, state.plan.completionText.display[language], ready, "colour-match-",
            onReplay, onAgain, onHome, onPop, modifier, saveFailed, onRetry, audioFailed); return
    }
    Column(modifier.fillMaxSize().background(BastiPalette.background).verticalScroll(rememberScrollState())
        .padding(horizontal = BastiSpacing.medium, vertical = BastiSpacing.medium), verticalArrangement = Arrangement.spacedBy(BastiSpacing.small)) {
        Text(t("Match Colours", "Farben zuordnen"), Modifier.fillMaxWidth().testTag("colour-match-title"), style = BastiTypography.screenHeading, color = BastiPalette.ink)
        if (state == null) Text(t("Opening…", "Wird geöffnet …"), Modifier.testTag("colour-match-loading"), color = BastiPalette.ink) else {
            val question = state.task.question
            Text("${state.index + 1} ${t("of", "von")} ${state.plan.tasks.size}", Modifier.testTag("colour-match-progress"), style = BastiTypography.parentInformation, color = BastiPalette.ink)
            Text(question.instruction.display[language], Modifier.fillMaxWidth().testTag("colour-match-instruction"), style = BastiTypography.question, color = BastiPalette.ink)
            val targetColour = MatchColoursContent.targetColour(question)
            val targetShape = MatchColoursContent.targetShape(question)
            Surface(Modifier.fillMaxWidth().heightIn(min = 126.dp).testTag("colour-match-target"), shape = BastiShapes.card,
                color = BastiPalette.surface, border = BorderStroke(3.dp, BastiPalette.ink)) {
                Row(Modifier.padding(BastiSpacing.medium), horizontalArrangement = Arrangement.spacedBy(BastiSpacing.medium), verticalAlignment = Alignment.CenterVertically) {
                    ColourShapeGraphic(targetShape, ColourSwatches.color(targetColour), Modifier.size(84.dp).semantics { contentDescription = t("Target colour sample", "Farbmuster") })
                    Text(t("Find the same colour", "Finde dieselbe Farbe"), style = BastiTypography.answer, color = BastiPalette.ink)
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(BastiSpacing.small)) {
                NativeActionButton(t("Listen again", "Noch einmal hören"), NativeActionRole.SECONDARY, onReplay, Modifier.weight(1f).testTag("colour-match-replay"), ready)
                NativeActionButton(t("Help", "Hilfe"), NativeActionRole.SECONDARY, onHelp, Modifier.weight(1f).testTag("colour-match-help"), ready && state.hintAvailable)
            }
            question.choices.chunked(2).forEachIndexed { rowIndex, row ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(BastiSpacing.small)) {
                    row.forEachIndexed { colIndex, id ->
                        val sample = MatchColoursContent.sample(id)
                        val chosen = state.current.lastChoice == id
                        val selectedLabel = t("Choice ${rowIndex * 2 + colIndex + 1}", "Auswahl ${rowIndex * 2 + colIndex + 1}")
                        Surface(onClick = { state.nextAttempt?.let { onAction(SessionAction.Answer(it, id)) } },
                            enabled = ready && state.current.answer == AnswerState.UNANSWERED,
                            modifier = Modifier.weight(1f).heightIn(min = 112.dp).testTag("colour-match-choice-${id.value}")
                                .semantics { role = Role.Button; contentDescription = selectedLabel; if (chosen) stateDescription = t("Selected", "Ausgewählt") },
                            shape = RoundedCornerShape(18.dp), color = BastiPalette.surface,
                            border = BorderStroke(if (chosen) 4.dp else 1.dp, if (chosen) BastiPalette.ink else BastiPalette.outline)) {
                            Box(Modifier.fillMaxWidth().padding(BastiSpacing.small), contentAlignment = Alignment.Center) {
                                ColourShapeGraphic(sample.shape, ColourSwatches.color(sample.colour), Modifier.size(72.dp))
                            }
                        }
                    }
                    if (row.size == 1) Spacer(Modifier.weight(1f))
                }
            }
            if (state.current.support.hint) question.hint?.let { NativeSupportMessage(it.display[language], Modifier.testTag("colour-match-hint-message")) }
            when (state.current.answer) {
                AnswerState.RETRY_AVAILABLE -> {
                    NativeSupportMessage(question.wrongFeedback.display[language], Modifier.testTag("colour-match-feedback-wrong"))
                    NativeActionButton(t("Try again", "Versuch es erneut"), NativeActionRole.PRIMARY,
                        { onAction(SessionAction.Retry(AttemptId(state.task.id, state.current.attempts))) }, Modifier.fillMaxWidth().testTag("colour-match-retry"), ready)
                }
                AnswerState.CORRECT -> {
                    NativeSupportMessage(question.correctFeedback.display[language], Modifier.testTag("colour-match-feedback-correct"))
                    NativeActionButton(t("Next", "Weiter"), NativeActionRole.PRIMARY,
                        { onAction(SessionAction.Next(state.task.id)) }, Modifier.fillMaxWidth().testTag("colour-match-next"), ready)
                }
                else -> Unit
            }
        }
        if (saveFailed) {
            NativeSupportMessage(t("Your saved round is safe. Try again.", "Deine gespeicherte Runde ist sicher. Versuche es noch einmal."))
            NativeActionButton(t("Try again", "Erneut versuchen"), NativeActionRole.SECONDARY, onRetry, Modifier.testTag("colour-match-save-retry"), !busy)
        }
        if (audioFailed) Text(t("Speech is unavailable. Check installed offline voices in Options.", "Die Sprachausgabe ist nicht verfügbar. Prüfe die Offline-Stimmen in den Optionen."))
        NativeActionButton(t("Home", "Startseite"), NativeActionRole.NAVIGATION, onHome, Modifier.fillMaxWidth().testTag("colour-match-home"))
    }
}
