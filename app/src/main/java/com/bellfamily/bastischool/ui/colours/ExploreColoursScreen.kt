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
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import com.bellfamily.bastischool.learning.colours.ColoursContent
import com.bellfamily.bastischool.learning.models.ContentId
import com.bellfamily.bastischool.learning.models.ContentLanguage
import com.bellfamily.bastischool.ui.common.*
import com.bellfamily.bastischool.ui.design.*

@Composable
fun ExploreColoursScreen(
    selected: ContentId,
    language: ContentLanguage,
    busy: Boolean,
    saveFailed: Boolean,
    audioFailed: Boolean,
    onSelect: (ContentId) -> Unit,
    onReplay: () -> Unit,
    onRetry: () -> Unit,
    onHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    val german = language == ContentLanguage.GERMAN
    fun t(en: String, de: String) = if (german) de else en
    val definition = ColoursContent.definition(selected)
    val label = definition.text.display[language].replaceFirstChar { it.uppercase() }
    Column(modifier.fillMaxSize().background(BastiPalette.background).verticalScroll(rememberScrollState())
        .padding(horizontal = BastiSpacing.medium, vertical = BastiSpacing.medium),
        verticalArrangement = Arrangement.spacedBy(BastiSpacing.small)) {
        Text(t("Explore Colours", "Farben entdecken"), Modifier.fillMaxWidth().testTag("colours-explore-title"),
            style = BastiTypography.screenHeading, color = BastiPalette.ink)
        Text(t("Tap a colour to see and hear its name.", "Tippe auf eine Farbe und höre ihren Namen."),
            style = BastiTypography.instruction, color = BastiPalette.ink)
        Box(Modifier.fillMaxWidth().heightIn(min = 112.dp, max = 192.dp).background(ColourSwatches.color(selected), RoundedCornerShape(22.dp))
            .border(3.dp, BastiPalette.ink, RoundedCornerShape(22.dp)).testTag("colours-explore-sample")
            .semantics { contentDescription = label }, contentAlignment = Alignment.Center) {
            Text(label, color = if (selected == ColoursContent.yellow) BastiPalette.ink else MaterialTheme.colorScheme.surface,
                style = BastiTypography.question,
                modifier = Modifier.testTag("colours-explore-name"))
        }
        ColourGrid(selected, language, enabled = !busy && !saveFailed, tagPrefix = "colours-explore") { onSelect(it) }
        NativeActionButton(t("Listen again", "Noch einmal hören"), NativeActionRole.SECONDARY,
            onReplay, Modifier.fillMaxWidth().testTag("colours-explore-replay"), enabled = !busy && !saveFailed)
        if (busy && selected == ColoursContent.red) Text(t("Opening…", "Wird geöffnet…"), Modifier.testTag("colours-explore-loading"))
        if (saveFailed) {
            NativeSupportMessage(t("Your saved choice is safe. Try again.", "Deine gespeicherte Auswahl ist sicher. Versuche es noch einmal."))
            NativeActionButton(t("Try again", "Erneut versuchen"), NativeActionRole.SECONDARY, onRetry,
                Modifier.testTag("colours-explore-retry"), enabled = !busy)
        }
        if (audioFailed) Text(t("Speech is unavailable. Check installed offline voices in Options.",
            "Die Sprachausgabe ist nicht verfügbar. Prüfe die Offline-Stimmen in den Optionen."), Modifier.testTag("colours-explore-audio-error"))
        NativeActionButton(t("Home", "Startseite"), NativeActionRole.NAVIGATION, onHome,
            Modifier.fillMaxWidth().testTag("colours-explore-home"))
    }
}

@Composable
internal fun ColourGrid(selected: ContentId?, language: ContentLanguage, enabled: Boolean, tagPrefix: String,
    onSelect: (ContentId) -> Unit) {
    val german = language == ContentLanguage.GERMAN
    fun t(en: String, de: String) = if (german) de else en
    ColoursContent.order.chunked(2).forEach { row ->
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(BastiSpacing.small)) {
            row.forEach { id ->
                val definition = ColoursContent.definition(id)
                val label = definition.text.display[language].replaceFirstChar { it.uppercase() }
                val chosen = selected == id
                Surface(onClick = { onSelect(id) }, enabled = enabled,
                    modifier = Modifier.weight(1f).heightIn(min = 112.dp).testTag("$tagPrefix-choice-${id.value}")
                        .semantics {
                            role = Role.Button
                            contentDescription = label
                            this.selected = chosen
                            stateDescription = if (chosen) t("Selected", "Ausgewählt") else t("Available", "Verfügbar")
                        }, shape = BastiShapes.control, color = BastiPalette.surface,
                    border = BorderStroke(if (chosen) 3.dp else 1.dp,
                        if (chosen) BastiPalette.ink else BastiPalette.outline)) {
                    Column(Modifier.padding(BastiSpacing.small), horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(BastiSpacing.small)) {
                        Box(Modifier.fillMaxWidth().heightIn(min = 52.dp, max = 68.dp)
                            .background(ColourSwatches.color(id), RoundedCornerShape(12.dp))
                            .border(2.dp, BastiPalette.ink, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                            if (chosen) Text("✓", color = if (id == ColoursContent.yellow) BastiPalette.ink else MaterialTheme.colorScheme.surface,
                                style = BastiTypography.answer, modifier = Modifier.testTag("$tagPrefix-selected-${id.value}"))
                        }
                        Text(label, style = BastiTypography.answer, color = BastiPalette.ink)
                    }
                }
            }
            if (row.size == 1) Spacer(Modifier.weight(1f))
        }
    }
}
