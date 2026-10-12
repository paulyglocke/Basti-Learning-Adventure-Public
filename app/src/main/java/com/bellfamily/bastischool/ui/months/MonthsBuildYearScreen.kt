package com.bellfamily.bastischool.ui.months

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.months.*
import com.bellfamily.bastischool.learning.session.*
import com.bellfamily.bastischool.ui.common.*

@Composable
fun MonthsBuildYearScreen(state: MonthsBuildYearState?, language: ContentLanguage, busy: Boolean,
    saveFailed: Boolean, audioFailed: Boolean, onAction: (MonthsBuildYearAction) -> Unit,
    onListen: (ContentId) -> Unit, onReplay: () -> Unit, onAgain: () -> Unit,
    onRetrySave: () -> Unit, onHome: () -> Unit, modifier: Modifier = Modifier, onPop: (String) -> Unit = {}) {
    fun t(en: String, de: String) = if (language == ContentLanguage.GERMAN) de else en
    val ready = state != null && state.language == language && !busy && !saveFailed
    var selectedValue by rememberSaveable(state?.id?.value, state?.index) { mutableStateOf("") }
    val selected = selectedValue.takeIf { it.isNotEmpty() }?.let(::ContentId)
    if (state?.completed == true) {
        NativeCompletionScreen(state.id.value, language, MonthsBuildYear.completion.display[language], ready,
            "months-build-year-", onReplay, onAgain, onHome, onPop, modifier, saveFailed, onRetrySave, audioFailed)
        return
    }
    Column(modifier.fillMaxSize().background(Color(0xFFF8F5FC)).verticalScroll(rememberScrollState()).padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        NativeActivityTitle(MonthsMode.BUILD_YEAR.title.display[language])
        if (state == null) {
            if (!saveFailed) Text(t("Opening…", "Wird geöffnet…"))
        } else {
            NativeSupportMessage(MonthsBuildYear.prompt(state).display[language], Modifier.testTag("months-build-year-prompt"))
            Text(t("Example", "Beispiel"), style = androidx.compose.material3.MaterialTheme.typography.titleMedium)
            Row(Modifier.fillMaxWidth().testTag("months-build-year-example"),
                horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                listOf(MonthIds.DECEMBER, MonthIds.JANUARY, MonthIds.FEBRUARY).forEachIndexed { index, id ->
                    Text(MonthContent.month(id).text.display[language], Modifier.weight(1f)
                        .background(Color.White, RoundedCornerShape(12.dp)).padding(horizontal = 6.dp, vertical = 10.dp)
                        .semantics { contentDescription = MonthContent.month(id).text.display[language] },
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    if (index < 2) Text("→", color = Color(0xFF23354A))
                }
            }
            NativeActionButton(t("Listen again", "Noch einmal hören"), NativeActionRole.SECONDARY, onReplay,
                Modifier.fillMaxWidth().testTag("months-build-year-replay"), ready)

            state.order.indices.forEach { position ->
                val filled = state.placed.getOrNull(position)
                val active = position == state.index && state.current.answer == AnswerState.UNANSWERED
                val label = filled?.let { MonthContent.month(it).text.display[language] }
                    ?: t("Space ${position + 1}", "Feld ${position + 1}")
                val accessible = when {
                    filled != null -> t("Position ${position + 1}: $label", "Position ${position + 1}: $label")
                    active -> t("Position ${position + 1}, empty. Place the selected month here.", "Position ${position + 1}, frei. Lege den ausgewählten Monat hier ab.")
                    else -> t("Position ${position + 1}, waiting", "Position ${position + 1}, wartet")
                }
                NativeActionButton(label, if (active) NativeActionRole.PRIMARY else NativeActionRole.SECONDARY,
                    onClick = { if (active && selected != null && ready) {
                        onAction(MonthsBuildYearAction.Place(state.nextAttempt!!, selected)); selectedValue = ""
                    } },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 72.dp).testTag("months-build-year-slot-${position + 1}")
                        .semantics { contentDescription = accessible }, enabled = active && selected != null && ready)
            }

            NativeSupportMessage(t("Choose a month, then tap the next space.", "Wähle einen Monat und tippe dann auf das nächste Feld."))
            state.choices.filterNot { it in state.placed }.forEach { id ->
                val name = MonthContent.month(id).text.display[language]
                val isSelected = selected == id
                NativeTextChoice(name, { if (ready && state.current.answer == AnswerState.UNANSWERED) selectedValue = id.value },
                    Modifier.fillMaxWidth().testTag("months-build-year-choice-${id.value}")
                        .semantics { this.selected = isSelected },
                    enabled = ready && state.current.answer == AnswerState.UNANSWERED)
                NativeActionButton(t("Listen: $name", "Anhören: $name"), NativeActionRole.SECONDARY, { onListen(id) },
                    Modifier.fillMaxWidth().testTag("months-build-year-listen-${id.value}"), ready)
            }
            if (selected != null) NativeSupportMessage(t("Selected: ${MonthContent.month(selected).text.display[language]}",
                "Ausgewählt: ${MonthContent.month(selected).text.display[language]}"), Modifier.testTag("months-build-year-selected"))
            if (state.current.support.hint) NativeSupportMessage(MonthsBuildYear.hint(state).display[language], Modifier.testTag("months-build-year-hint"))
            if (state.current.answer == AnswerState.RETRY_AVAILABLE) {
                NativeSupportMessage(t("Not quite. The months already placed stay here. Try again.",
                    "Noch nicht. Die eingeordneten Monate bleiben hier. Versuche es noch einmal."))
                NativeActionButton(t("Try again", "Nochmal versuchen"), NativeActionRole.SECONDARY,
                    { onAction(MonthsBuildYearAction.Retry(AttemptId(state.task, state.current.attempts))) },
                    Modifier.testTag("months-build-year-retry"), ready)
            }
            if (state.current.answer == AnswerState.UNANSWERED) NativeActionButton(t("Hint", "Tipp"), NativeActionRole.SECONDARY,
                { onAction(MonthsBuildYearAction.Help(state.task)) }, Modifier.testTag("months-build-year-help"), ready)
        }
        if (saveFailed) {
            NativeSupportMessage(t("Your saved work is kept. Please try saving again.", "Deine gespeicherte Arbeit bleibt erhalten. Versuche erneut zu speichern."))
            NativeActionButton(t("Try again", "Erneut versuchen"), NativeActionRole.SECONDARY, onRetrySave,
                Modifier.testTag("months-build-year-save-retry"), !busy)
        }
        if (audioFailed) NativeSupportMessage(t("Speech is unavailable. Check installed offline voices in Options.",
            "Die Sprachausgabe ist nicht verfügbar. Prüfe die Offline-Stimmen in den Optionen."))
        NativeActionButton(t("Home", "Startseite"), NativeActionRole.NAVIGATION, onHome,
            Modifier.fillMaxWidth().testTag("months-build-year-home"))
    }
}
