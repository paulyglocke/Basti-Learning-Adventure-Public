package com.bellfamily.bastischool.ui.months

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.months.*
import com.bellfamily.bastischool.learning.session.*
import com.bellfamily.bastischool.ui.common.*

@Composable
fun MonthsBuildYearLevelsScreen(
    language: ContentLanguage,
    onThreeMonths: () -> Unit,
    onFourMonths: () -> Unit,
    onHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    val german = language == ContentLanguage.GERMAN
    Column(modifier.fillMaxSize().background(Color(0xFFF8F5FC)).verticalScroll(rememberScrollState()).padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        NativeActivityTitle(MonthsMode.BUILD_YEAR.title.display[language])
        NativeSupportMessage(if (german) "Wähle eine Reihenfolge zum Bauen." else "Choose a month sequence to build.")
        NativeActionButton(if (german) "Stufe 1 — Drei Monate" else "Level 1 — Three Months",
            NativeActionRole.NAVIGATION, onThreeMonths, Modifier.fillMaxWidth().heightIn(min = 64.dp).testTag("months-build-year-level-1"))
        NativeActionButton(if (german) "Stufe 2 — Vier Monate" else "Level 2 — Four Months",
            NativeActionRole.NAVIGATION, onFourMonths, Modifier.fillMaxWidth().heightIn(min = 64.dp).testTag("months-build-year-level-2"))
        NativeActionButton(if (german) "Home" else "Home", NativeActionRole.NAVIGATION, onHome,
            Modifier.fillMaxWidth().testTag("months-build-year-levels-home"))
    }
}

@Composable
fun MonthsBuildYearFourScreen(
    state: MonthsBuildYearFourState?, language: ContentLanguage, busy: Boolean,
    saveFailed: Boolean, audioFailed: Boolean,
    onAction: (MonthsBuildYearFourAction) -> Unit,
    onListen: (ContentId) -> Unit, onReplay: () -> Unit, onAgain: () -> Unit,
    onRetrySave: () -> Unit, onHome: () -> Unit,
    modifier: Modifier = Modifier, onPop: (String) -> Unit = {}
) {
    fun t(en: String, de: String) = if (language == ContentLanguage.GERMAN) de else en
    val ready = state != null && state.language == language && !busy && !saveFailed
    var selectedValue by rememberSaveable(state?.id?.value, state?.questionIndex, state?.index) { mutableStateOf("") }
    val selected = selectedValue.takeIf(String::isNotEmpty)?.let(::ContentId)
    if (state?.completed == true) {
        NativeCompletionScreen(state.id.value, language, MonthsBuildYearFour.completion.display[language], ready,
            "months-build-year-four-", onReplay, onAgain, onHome, onPop, modifier, saveFailed, onRetrySave, audioFailed)
        return
    }

    Column(modifier.fillMaxSize().background(Color(0xFFF8F5FC)).verticalScroll(rememberScrollState()).padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        NativeActivityTitle(if (language == ContentLanguage.GERMAN) "Baue das Jahr — Vier Monate" else "Build the Year — Four Months")
        if (state == null) {
            if (!saveFailed) Text(t("Opening…", "Wird geöffnet…"))
        } else {
            Text(t("Sequence ${state.questionIndex + 1} of ${state.round.count}", "Folge ${state.questionIndex + 1} von ${state.round.count}"),
                style = MaterialTheme.typography.titleMedium, modifier = Modifier.testTag("months-build-year-four-progress"))
            NativeSupportMessage(MonthsBuildYearFour.prompt(state).display[language], Modifier.testTag("months-build-year-four-prompt"))
            if (state.questionIndex == 0 && state.index == 0) {
                Text(t("Example", "Beispiel"), style = MaterialTheme.typography.titleMedium)
                MonthBuildYearFourDemonstration(MonthsBuildYearFour.demonstration, language)
            }
            NativeActionButton(t("Listen again", "Noch einmal hören"), NativeActionRole.SECONDARY, onReplay,
                Modifier.fillMaxWidth().testTag("months-build-year-four-replay"), ready)

            state.order.indices.forEach { position ->
                val filled = state.placed.getOrNull(position)
                val active = position == state.index && !state.sequenceReady
                val label = filled?.let { MonthContent.month(it).text.display[language] }
                    ?: t("Space ${position + 1}", "Feld ${position + 1}")
                val accessible = when {
                    filled != null -> t("Position ${position + 1}: $label", "Position ${position + 1}: $label")
                    active -> t("Position ${position + 1}, empty. Place the selected month here.", "Position ${position + 1}, frei. Lege den ausgewählten Monat hier ab.")
                    else -> t("Position ${position + 1}, waiting", "Position ${position + 1}, wartet")
                }
                NativeActionButton(label, if (active) NativeActionRole.PRIMARY else NativeActionRole.SECONDARY,
                    onClick = { if (active && selected != null && ready) {
                        state.nextAttempt?.let { onAction(MonthsBuildYearFourAction.Place(it, selected)) }
                        selectedValue = ""
                    } },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 72.dp).testTag("months-build-year-four-slot-${position + 1}")
                        .semantics { contentDescription = accessible }, enabled = active && selected != null && ready)
            }

            NativeSupportMessage(t("Choose a month, then tap the next space.", "Wähle einen Monat und tippe dann auf das nächste Feld."))
            if (!state.sequenceReady) {
                MonthBuildYearFourCards(state.choices[state.questionIndex].filterNot { it in state.placed }, language,
                    "months-build-year-four-choice", enabled = ready && state.current.answer == AnswerState.UNANSWERED,
                    selected = selected, onChoose = { id -> selectedValue = id.value })
                state.choices[state.questionIndex].forEach { id ->
                    val name = MonthContent.month(id).text.display[language]
                    NativeActionButton(t("Listen: $name", "Anhören: $name"), NativeActionRole.SECONDARY, { onListen(id) },
                        Modifier.fillMaxWidth().testTag("months-build-year-four-listen-${id.value}"),
                        ready && state.current.answer == AnswerState.UNANSWERED)
                }
            }
            if (selected != null) NativeSupportMessage(t("Selected: ${MonthContent.month(selected).text.display[language]}",
                "Ausgewählt: ${MonthContent.month(selected).text.display[language]}"), Modifier.testTag("months-build-year-four-selected"))
            if (state.current.support.hint && !state.sequenceReady)
                NativeSupportMessage(MonthsBuildYearFour.hint(state).display[language], Modifier.testTag("months-build-year-four-hint"))
            if (state.current.answer == AnswerState.RETRY_AVAILABLE) {
                NativeSupportMessage(t("Not quite. The months already placed stay here. Try again.",
                    "Noch nicht. Die eingeordneten Monate bleiben hier. Versuche es noch einmal."))
                NativeActionButton(t("Try again", "Nochmal versuchen"), NativeActionRole.SECONDARY,
                    { onAction(MonthsBuildYearFourAction.Retry(AttemptId(state.placementTask, state.currentAttemptNumber))) },
                    Modifier.testTag("months-build-year-four-retry"), ready)
            }
            if (state.sequenceReady) NativeActionButton(
                t(if (state.questionIndex == state.round.count - 1) "Finish" else "Continue", if (state.questionIndex == state.round.count - 1) "Fertig" else "Weiter"),
                NativeActionRole.PRIMARY, { onAction(MonthsBuildYearFourAction.Continue(state.task)) },
                Modifier.fillMaxWidth().testTag("months-build-year-four-continue"), ready)
            else if (state.current.answer == AnswerState.UNANSWERED) NativeActionButton(t("Hint", "Tipp"), NativeActionRole.SECONDARY,
                { onAction(MonthsBuildYearFourAction.Help(state.task)) }, Modifier.testTag("months-build-year-four-help"), ready)
        }
        if (saveFailed) {
            NativeSupportMessage(t("Your saved work is kept. Please try saving again.", "Deine gespeicherte Arbeit bleibt erhalten. Versuche erneut zu speichern."))
            NativeActionButton(t("Try again", "Erneut versuchen"), NativeActionRole.SECONDARY, onRetrySave,
                Modifier.testTag("months-build-year-four-save-retry"), !busy)
        }
        if (audioFailed) NativeSupportMessage(t("Speech is unavailable. Check installed offline voices in Options.",
            "Die Sprachausgabe ist nicht verfügbar. Prüfe die Offline-Stimmen in den Optionen."))
        NativeActionButton(t("Home", "Startseite"), NativeActionRole.NAVIGATION, onHome,
            Modifier.fillMaxWidth().testTag("months-build-year-four-home"))
    }
}

@Composable
private fun MonthBuildYearFourCards(
    ids: List<ContentId>, language: ContentLanguage, tag: String,
    enabled: Boolean, selected: ContentId? = null, onChoose: (ContentId) -> Unit = {}
) {
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val fontScale = LocalDensity.current.fontScale
        val columns = (maxWidth / (160.dp * fontScale)).toInt().coerceIn(1, 2)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ids.chunked(columns).forEach { group ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    group.forEach { id ->
                        val label = MonthContent.month(id).text.display[language]
                        NativeTextChoice(label, { onChoose(id) }, Modifier.weight(1f).heightIn(min = 72.dp)
                            .testTag("$tag-${id.value}").semantics {
                                contentDescription = label
                                this.selected = selected == id
                            }, enabled = enabled)
                    }
                    repeat(columns - group.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }
    }
}

@Composable
private fun MonthBuildYearFourDemonstration(ids: List<ContentId>, language: ContentLanguage) {
    BoxWithConstraints(Modifier.fillMaxWidth().testTag("months-build-year-four-example")) {
        val columns = (maxWidth / (160.dp * LocalDensity.current.fontScale)).toInt().coerceIn(1, 2)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ids.chunked(columns).forEach { group ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    group.forEach { id ->
                        val month = MonthContent.month(id)
                        Text(month.text.display[language], Modifier.weight(1f).heightIn(min = 64.dp)
                            .background(Color.White, RoundedCornerShape(12.dp)).padding(12.dp)
                            .semantics { contentDescription = month.text.display[language] },
                            textAlign = TextAlign.Center, style = MaterialTheme.typography.titleMedium)
                    }
                    repeat(columns - group.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }
    }
}
