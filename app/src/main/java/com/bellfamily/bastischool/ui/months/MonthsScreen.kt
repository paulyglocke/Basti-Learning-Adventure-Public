package com.bellfamily.bastischool.ui.months

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.bellfamily.bastischool.learning.content.SeasonIds
import com.bellfamily.bastischool.learning.models.ContentLanguage
import com.bellfamily.bastischool.learning.models.ContentId
import com.bellfamily.bastischool.learning.months.MonthContent
import com.bellfamily.bastischool.learning.months.MonthsSelection
import com.bellfamily.bastischool.ui.common.*
import com.bellfamily.bastischool.ui.design.BastiPalette

/** Content-first Year Wheel exploration. Activity selection lives on MonthsActivityChooserScreen. */
@Composable
fun MonthsScreen(
    selection: MonthsSelection?, language: ContentLanguage, saveFailed: Boolean, audioFailed: Boolean,
    onSelect: (ContentId) -> Unit, onListen: () -> Unit, onRetry: () -> Unit, onHome: () -> Unit,
    modifier: Modifier = Modifier, onReturnToQuestion: (() -> Unit)? = null,
) {
    fun t(en: String, de: String) = if (language == ContentLanguage.GERMAN) de else en
    BoxWithConstraints(modifier.fillMaxSize().background(BastiPalette.background)) {
        val wideTablet = maxWidth >= 700.dp && maxHeight >= 600.dp
        val shortLandscape = maxWidth >= 700.dp && maxHeight < 600.dp
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp).testTag("months-content"),
            horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (selection == null) {
                NativeActivityTitle(t("Explore the Year Wheel", "Das Jahresrad entdecken"), Modifier.fillMaxWidth())
                NativeSupportMessage(t("Choose a month to hear its name and explore its season.",
                    "Wähle einen Monat, um seinen Namen und seine Jahreszeit zu entdecken."))
                if (!saveFailed) Text(t("Opening…", "Wird geöffnet…"))
            } else if (wideTablet) {
                if (onReturnToQuestion != null) NativeActionButton(t("Back to the question", "Zurück zur Frage"),
                    NativeActionRole.NAVIGATION, onReturnToQuestion,
                    Modifier.fillMaxWidth().testTag("months-between-return"))
                NativeActivityTitle(t("Explore the Year Wheel", "Das Jahresrad entdecken"), Modifier.fillMaxWidth())
                NativeSupportMessage(t("Choose a month to hear its name and explore its season.",
                    "Wähle einen Monat, um seinen Namen und seine Jahreszeit zu entdecken."))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(24.dp),
                    verticalAlignment = Alignment.Top) {
                    Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        YearWheel(selection.selected, language, !saveFailed, onSelect,
                            Modifier.widthIn(max = 520.dp).fillMaxWidth())
                        SelectedMonth(selection.selected, language)
                        ListenButton(language, onListen)
                    }
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        MonthChoices(selection, language, !saveFailed, onSelect, 2)
                        SeasonOverview(language)
                    }
                }
            } else if (shortLandscape) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.Top) {
                    Column(Modifier.weight(1f).heightIn(min = 240.dp), horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center) {
                        YearWheel(selection.selected, language, !saveFailed, onSelect,
                            Modifier.widthIn(max = 240.dp).fillMaxWidth())
                    }
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (onReturnToQuestion != null) NativeActionButton(t("Back to the question", "Zurück zur Frage"),
                            NativeActionRole.NAVIGATION, onReturnToQuestion,
                            Modifier.fillMaxWidth().testTag("months-between-return"))
                        NativeActivityTitle(t("Explore the Year Wheel", "Das Jahresrad entdecken"), Modifier.fillMaxWidth())
                        NativeSupportMessage(t("Choose a month to explore.", "Wähle einen Monat zum Entdecken."))
                        SelectedMonth(selection.selected, language)
                        ListenButton(language, onListen)
                        MonthChoices(selection, language, !saveFailed, onSelect, 2)
                        SeasonOverview(language)
                    }
                }
            } else {
                if (onReturnToQuestion != null) NativeActionButton(t("Back to the question", "Zurück zur Frage"),
                    NativeActionRole.NAVIGATION, onReturnToQuestion,
                    Modifier.fillMaxWidth().testTag("months-between-return"))
                NativeActivityTitle(t("Explore the Year Wheel", "Das Jahresrad entdecken"), Modifier.fillMaxWidth())
                NativeSupportMessage(t("Choose a month to hear its name and explore its season.",
                    "Wähle einen Monat, um seinen Namen und seine Jahreszeit zu entdecken."))
                YearWheel(selection.selected, language, !saveFailed, onSelect,
                    Modifier.widthIn(max = 480.dp).fillMaxWidth())
                SelectedMonth(selection.selected, language)
                ListenButton(language, onListen)
                BoxWithConstraints(Modifier.fillMaxWidth()) {
                    val columns = (maxWidth / (180.dp * LocalDensity.current.fontScale)).toInt().coerceIn(1, 3)
                    MonthChoices(selection, language, !saveFailed, onSelect, columns)
                }
                SeasonOverview(language)
            }
            Text(MonthContent.convention.display[language], Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.bodyMedium)
            if (saveFailed) {
                NativeSupportMessage(t("Saving is unavailable. Please try again.", "Speichern ist nicht möglich. Versuche es erneut."))
                NativeActionButton(t("Try again", "Erneut versuchen"), NativeActionRole.SECONDARY, onRetry,
                    Modifier.testTag("months-retry"))
            }
            if (audioFailed) NativeSupportMessage(t("Speech is unavailable. Check installed offline voices.",
                "Die Sprachausgabe ist nicht verfügbar. Prüfe installierte Offline-Stimmen."))
            NativeActionButton(t("Home", "Startseite"), NativeActionRole.NAVIGATION, onHome,
                Modifier.fillMaxWidth().testTag("months-home"))
        }
    }
}

@Composable
private fun SelectedMonth(selected: ContentId, language: ContentLanguage) {
    val month = MonthContent.month(selected)
    Text(month.text.display[language], style = MaterialTheme.typography.headlineMedium,
        modifier = Modifier.testTag("months-selected"))
    Text(MonthContent.season(month.season).text.display[language], style = MaterialTheme.typography.titleMedium)
}

@Composable
private fun ListenButton(language: ContentLanguage, onListen: () -> Unit) {
    NativeActionButton(if (language == ContentLanguage.GERMAN) "Anhören" else "Listen",
        NativeActionRole.SECONDARY, onListen, Modifier.fillMaxWidth().testTag("months-listen"))
}

@Composable
private fun MonthChoices(selection: MonthsSelection, language: ContentLanguage, enabled: Boolean,
    onSelect: (ContentId) -> Unit, columns: Int) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        MonthContent.definitions.chunked(columns).forEach { row ->
            Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { item ->
                    val chosen = item.id == selection.selected
                    NativeTextChoice(item.text.display[language], { onSelect(item.id) },
                        Modifier.weight(1f).fillMaxHeight().testTag(item.id.value)
                            .semantics { selected = chosen }
                            .then(if (chosen) Modifier.border(3.dp, Color(0xFF23354A), RoundedCornerShape(16.dp)) else Modifier),
                        enabled = enabled)
                }
                if (row.size < columns) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun SeasonOverview(language: ContentLanguage) {
    // Named seasons and months keep season meaning independent of colour vision.
    SeasonIds.canonicalOrder.forEach { season ->
        val names = MonthContent.definitions.filter { it.season == season }.let {
            if (season == SeasonIds.WINTER) listOf(it.last()) + it.dropLast(1) else it
        }.joinToString(", ") { it.text.display[language] }
        Text("${MonthContent.season(season).text.display[language]}: $names",
            modifier = Modifier.fillMaxWidth().background(seasonColour(season), RoundedCornerShape(12.dp)).padding(12.dp),
            color = Color(0xFF23354A))
    }
}
