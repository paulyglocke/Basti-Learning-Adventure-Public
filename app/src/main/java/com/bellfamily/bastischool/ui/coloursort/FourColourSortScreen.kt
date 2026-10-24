package com.bellfamily.bastischool.ui.coloursort

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import com.bellfamily.bastischool.learning.colours.ColourShape
import com.bellfamily.bastischool.learning.coloursort.*
import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.sorting.*
import com.bellfamily.bastischool.ui.colours.ColourShapeGraphic
import com.bellfamily.bastischool.ui.colours.ColourSwatches
import com.bellfamily.bastischool.ui.common.*

@Composable
fun FourColourSortScreen(state: SortState?, language: ContentLanguage, busy: Boolean, saveFailed: Boolean, audioFailed: Boolean,
    onAction: (SortAction) -> Unit, onAgain: () -> Unit, onRetry: () -> Unit, onHome: () -> Unit,
    modifier: Modifier = Modifier, onPop: (String) -> Unit = {}) {
    fun t(en: String, de: String) = if (language == ContentLanguage.GERMAN) de else en
    val ready = state != null && !busy && !saveFailed && state.language == language
    if (state?.completed == true) {
        NativeCompletionScreen(state.id.value, language, FourColourSort.completion.display[language], ready, "sort-four-",
            { onAction(SortAction.Replay) }, onAgain, onHome, onPop, modifier, saveFailed, onRetry, audioFailed); return
    }
    Column(modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).verticalScroll(rememberScrollState())
        .padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(FourColourSort.title.display[language], modifier = Modifier.testTag("sort-four-title"), style = MaterialTheme.typography.headlineMedium)
        if (state == null) Text(t("Opening…", "Wird geöffnet …"), Modifier.testTag("sort-four-loading")) else {
            Text("${state.placements.count { it.placed }} ${t("of", "von")} 8 ${t("sorted", "einsortiert")}", Modifier.testTag("sort-four-progress"))
            Text(FourColourSort.instruction.display[language], style = MaterialTheme.typography.titleMedium)
            NativeActionButton(t("Listen again", "Noch einmal hören"), NativeActionRole.SECONDARY,
                { onAction(SortAction.Replay) }, Modifier.fillMaxWidth().testTag("sort-four-replay"), ready)
            Text(if (state.selected == null) t("Choose an object", "Wähle ein Ding") else t("Now choose its colour group", "Wähle jetzt die passende Farbgruppe"), Modifier.testTag("sort-four-step"))
            state.order.filter { !state.placement(it).placed }.chunked(2).forEachIndexed { rowIndex, row ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    row.forEach { id ->
                        val item = FourColourSort.item(id); val selected = state.selected == id
                        Column(Modifier.weight(1f).heightIn(min = 96.dp).border(if (selected) 4.dp else 1.dp,
                            if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
                            .clickable(ready, role = Role.Button) { onAction(SortAction.Select(id)) }
                            .semantics { this.selected = selected; stateDescription = if (selected) t("Selected", "Ausgewählt") else t("Available to sort", "Zum Einsortieren") }
                            .testTag("sort-four-item-${id.value}").padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            ColourShapeGraphic(if (item.shape == ColourSortShape.BALL) ColourShape.CIRCLE else ColourShape.SQUARE,
                                ColourSwatches.color(item.category), Modifier.size(46.dp))
                            Text(item.text.display[language], style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    if (row.size == 1) Spacer(Modifier.weight(1f))
                }
            }
            state.selectedPlacement?.let { placement ->
                if (placement.attempts > 0 && !placement.placed) NativeSupportMessage(
                    if (language == ContentLanguage.GERMAN) "Schau dir die Farbe an. Probiere eine andere Gruppe." else "Look at its colour. Try another group.",
                    Modifier.testTag("sort-four-guidance"))
                if (placement.support.hint) NativeSupportMessage(
                    state.selected?.let(FourColourSort::item)?.category?.let { FourColourSort.category(it).display[language] } ?: "",
                    Modifier.testTag("sort-four-hint"))
            }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                FourColourSort.categories.chunked(2).forEach { categories ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        categories.forEach { category ->
                            val placed = FourColourSort.objects.filter { it.category == category && state.placement(it.id).placed }
                            val name = FourColourSort.category(category).display[language]
                            Column(Modifier.weight(1f).heightIn(min = 116.dp)
                                .background(ColourSwatches.color(category).copy(alpha = .12f), RoundedCornerShape(16.dp))
                                .border(3.dp, ColourSwatches.color(category), RoundedCornerShape(16.dp))
                                .clickable(ready && state.selected != null, role = Role.Button) {
                                    state.selected?.let { onAction(SortAction.Place(state.id, it, category, state.placement(it).attempts + 1)) }
                                }.semantics { contentDescription = if (language == ContentLanguage.GERMAN) "Farbgruppe $name. ${placed.size} Dinge." else "$name group. ${placed.size} objects." }
                                .testTag("sort-four-category-${category.value}").padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(name, style = MaterialTheme.typography.titleMedium, modifier = Modifier.clearAndSetSemantics {})
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) { placed.forEach { item ->
                                    val sorted = FourColourSort.item(item.id)
                                    ColourShapeGraphic(if (sorted.shape == ColourSortShape.BALL) ColourShape.CIRCLE else ColourShape.SQUARE,
                                        ColourSwatches.color(sorted.category), Modifier.size(28.dp).testTag("sort-four-placed-${item.id.value}"))
                                } }
                            }
                        }
                    }
                }
            }
            if (state.selected == null && state.placements.any { it.placed }) Text(
                if (language == ContentLanguage.GERMAN) "Die Farben passen zusammen!" else "The colours match!", Modifier.testTag("sort-four-correct"))
            if (state.selected != null) NativeActionButton(t("Help", "Hilfe"), NativeActionRole.SECONDARY,
                { onAction(SortAction.Hint) }, Modifier.testTag("sort-four-help"), ready)
        }
        if (saveFailed) {
            Text(t("Your saved work is kept. Please try again.", "Deine gespeicherte Arbeit bleibt erhalten. Versuche es noch einmal."))
            NativeActionButton(t("Try again", "Erneut versuchen"), NativeActionRole.SECONDARY, onRetry, Modifier.testTag("sort-four-save-retry"), !busy)
        }
        if (audioFailed) Text(t("Speech is unavailable. Check installed offline voices in Options.", "Die Sprachausgabe ist nicht verfügbar. Prüfe die Offline-Stimmen in den Optionen."))
        NativeActionButton(t("Back home", "Zurück zum Start"), NativeActionRole.NAVIGATION, onHome, Modifier.fillMaxWidth().testTag("sort-four-home"))
    }
}
