package com.bellfamily.bastischool.ui.colours

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.unit.dp
import com.bellfamily.bastischool.learning.colours.*
import com.bellfamily.bastischool.learning.models.ContentLanguage
import com.bellfamily.bastischool.learning.session.AnswerState
import com.bellfamily.bastischool.learning.session.AttemptId
import com.bellfamily.bastischool.ui.common.*
import com.bellfamily.bastischool.ui.design.*

@Composable
fun RainbowOrderSessionScreen(state: RainbowOrderState?, level: RainbowOrderLevel, language: ContentLanguage,
    busy: Boolean, saveFailed: Boolean, onLevel: (RainbowOrderLevel) -> Unit,
    onAction: (RainbowOrderAction) -> Unit, onAgain: () -> Unit, onRetry: () -> Unit,
    onReplay: () -> Unit, onHelp: () -> Unit, onHome: () -> Unit, modifier: Modifier = Modifier) {
    val de = language == ContentLanguage.GERMAN
    val enabled = !busy && !saveFailed
    Column(modifier.fillMaxSize().background(BastiPalette.background).verticalScroll(rememberScrollState())
        .padding(BastiSpacing.medium), verticalArrangement = Arrangement.spacedBy(BastiSpacing.medium)) {
        NativeActivityTitle(if (de) "Regenbogen ordnen" else "Rainbow Order")
        Row(horizontalArrangement = Arrangement.spacedBy(BastiSpacing.small)) {
            FilterChip(level == RainbowOrderLevel.STARTER, { onLevel(RainbowOrderLevel.STARTER) }, enabled = enabled,
                label = { Text(if (de) "Mit Beispiel" else "With example") })
            FilterChip(level == RainbowOrderLevel.FULL, { onLevel(RainbowOrderLevel.FULL) }, enabled = enabled,
                label = { Text(if (de) "Alle sieben" else "All seven") })
        }
        if (state != null) {
            Text(RainbowOrderContent.prompt(state).display[language], style = BastiTypography.answer,
                color = BastiPalette.ink, modifier = Modifier.testTag("rainbow-order-prompt"))
            if (state.level == RainbowOrderLevel.STARTER || state.current.support.hint) {
                Text(if (de) "Beispiel" else "Example", style = BastiTypography.parentInformation,
                    modifier = Modifier.semantics { heading() })
                ColourBandRow(RainbowContent.order, language, Modifier.fillMaxWidth().testTag("rainbow-order-model"))
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                state.target.forEachIndexed { i, id ->
                    val done = i < state.index
                    val color = Color(RainbowContent.swatches[RainbowContent.order.indexOf(id)])
                    Box(Modifier.weight(1f).heightIn(min = 56.dp).background(if (done) color else BastiPalette.surface,
                        BastiShapes.control).testTag("rainbow-order-slot-${i}"), contentAlignment = Alignment.Center) {
                        Text(if (done) "✓" else "${i + 1}", color = if (done) Color.White else BastiPalette.ink)
                    }
                }
            }
            if (!state.completed && state.current.answer != AnswerState.RETRY_AVAILABLE) {
                ColourBandRow(state.choices.filterNot { it in state.placed }, language,
                    Modifier.fillMaxWidth().testTag("rainbow-order-choices")) { id ->
                    state.nextAttempt?.let { onAction(RainbowOrderAction.Place(it, id)) }
                }
            }
            if (state.current.answer == AnswerState.RETRY_AVAILABLE && !state.completed) {
                NativeSupportMessage(if (de) "Schau noch einmal auf die Reihenfolge." else "Have another look at the order.",
                    Modifier.testTag("rainbow-order-wrong"))
                NativeActionButton(if (de) "Nochmal versuchen" else "Try again", NativeActionRole.SECONDARY,
                    { onAction(RainbowOrderAction.Retry(AttemptId(state.task, state.current.attempts))) },
                    Modifier.fillMaxWidth().testTag("rainbow-order-retry"), enabled = enabled)
            }
            if (state.completed) {
                NativeSupportMessage(RainbowOrderContent.completion.display[language], Modifier.testTag("rainbow-order-complete"))
                NativeActionButton(if (de) "Noch einmal" else "Play again", NativeActionRole.PRIMARY, onAgain,
                    Modifier.fillMaxWidth().testTag("rainbow-order-again"), enabled = enabled)
            }
        } else Text(if (de) "Wird geladen …" else "Loading…", Modifier.testTag("rainbow-order-loading"))
        if (saveFailed) {
            NativeSupportMessage(if (de) "Deine gespeicherte Reihenfolge ist sicher. Versuche es erneut." else "Your saved order is safe. Try again.",
                Modifier.testTag("rainbow-order-save-error"))
            NativeActionButton(if (de) "Erneut versuchen" else "Retry", NativeActionRole.SECONDARY, onRetry,
                Modifier.fillMaxWidth().testTag("rainbow-order-save-retry"), enabled = !busy)
        }
        NativeActionButton(if (de) "Noch einmal hören" else "Listen again", NativeActionRole.SECONDARY, onReplay,
            Modifier.fillMaxWidth().testTag("rainbow-order-replay"), enabled = enabled && state != null)
        NativeActionButton(if (de) "Hilfe" else "Help", NativeActionRole.SECONDARY, onHelp,
            Modifier.fillMaxWidth().testTag("rainbow-order-help"), enabled = enabled && state?.completed == false)
        NativeActionButton(if (de) "Startseite" else "Home", NativeActionRole.NAVIGATION, onHome,
            Modifier.fillMaxWidth().testTag("rainbow-order-home"))
    }
}

@Composable
private fun ColourBandRow(colours: List<com.bellfamily.bastischool.learning.models.ContentId>, language: ContentLanguage,
    modifier: Modifier, onClick: ((com.bellfamily.bastischool.learning.models.ContentId) -> Unit)? = null) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
        colours.forEach { id ->
            val index = RainbowContent.order.indexOf(id)
            val label = RainbowContent.definition(id).text.display[language].replaceFirstChar { it.uppercase() }
            Surface(onClick = { onClick?.invoke(id) }, enabled = onClick != null,
                modifier = Modifier.weight(1f).heightIn(min = BastiSpacing.minimumTarget)
                    .testTag("rainbow-order-colour-${id.value.substringAfterLast('.')}").semantics { contentDescription = label },
                shape = RoundedCornerShape(16.dp), color = Color(RainbowContent.swatches[index]),
                border = androidx.compose.foundation.BorderStroke(1.dp, BastiPalette.outline)) {
                Box(Modifier.fillMaxWidth().padding(4.dp), contentAlignment = Alignment.Center) {
                    Text(label, color = if (index == 2) Color.Black else Color.White, style = BastiTypography.parentInformation)
                }
            }
        }
    }
}
