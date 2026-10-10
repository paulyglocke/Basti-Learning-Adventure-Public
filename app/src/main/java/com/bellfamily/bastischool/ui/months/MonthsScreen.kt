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
import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.months.*
import com.bellfamily.bastischool.ui.common.*

@Composable
fun MonthsScreen(selection: MonthsSelection?, language: ContentLanguage, saveFailed: Boolean, audioFailed: Boolean,
    onSelect: (ContentId) -> Unit, onListen: () -> Unit, onRetry: () -> Unit, onHome: () -> Unit,
    modifier: Modifier = Modifier) {
    fun t(en: String, de: String) = if (language == ContentLanguage.GERMAN) de else en
    Column(modifier.testTag("months-content").fillMaxSize().background(Color(0xFFF8F5FC)).verticalScroll(rememberScrollState()).padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        NativeActivityTitle(MonthContent.title.display[language])
        NativeSupportMessage(MonthContent.instruction.display[language])
        if (selection == null) {
            if (!saveFailed) Text(t("Opening…", "Wird geöffnet…"))
        } else {
            val month = MonthContent.month(selection.selected)
            YearWheel(month.id, language, !saveFailed, onSelect)
            Text(month.text.display[language], style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.testTag("months-selected"))
            Text(MonthContent.season(month.season).text.display[language])
            NativeActionButton(t("Listen", "Anhören"), NativeActionRole.SECONDARY, onListen,
                Modifier.fillMaxWidth().testTag("months-listen"))
            BoxWithConstraints(Modifier.fillMaxWidth()) {
                val columns = (maxWidth / (180.dp * LocalDensity.current.fontScale)).toInt().coerceIn(1, 3)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    MonthContent.definitions.chunked(columns).forEach { row ->
                        Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            row.forEach { item ->
                                val chosen = item.id == selection.selected
                                NativeTextChoice(item.text.display[language], { onSelect(item.id) },
                                    Modifier.weight(1f).fillMaxHeight().testTag(item.id.value)
                                        .semantics { selected = chosen }
                                        .then(if (chosen) Modifier.border(3.dp, Color(0xFF23354A), RoundedCornerShape(16.dp)) else Modifier),
                                    enabled = !saveFailed)
                            }
                        }
                    }
                }
            }
            // Named seasons plus explicit month names make the regions useful without colour vision.
            SeasonIds.canonicalOrder.forEach { season ->
                val names = MonthContent.definitions.filter { it.season == season }.let {
                    if (season == SeasonIds.WINTER) listOf(it.last()) + it.dropLast(1) else it
                }.joinToString(", ") { it.text.display[language] }
                Text("${MonthContent.season(season).text.display[language]}: $names",
                    modifier = Modifier.fillMaxWidth().background(seasonColour(season), RoundedCornerShape(12.dp)).padding(12.dp),
                    color = Color(0xFF23354A))
            }
            Text(MonthContent.convention.display[language], style = MaterialTheme.typography.bodyMedium)
        }
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
