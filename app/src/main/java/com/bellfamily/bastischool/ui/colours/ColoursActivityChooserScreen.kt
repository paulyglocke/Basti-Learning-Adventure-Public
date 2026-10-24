package com.bellfamily.bastischool.ui.colours

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.bellfamily.bastischool.learning.models.ContentLanguage
import com.bellfamily.bastischool.ui.common.*
import com.bellfamily.bastischool.ui.design.*

@Composable
fun ColoursActivityChooserScreen(
    language: ContentLanguage,
    ready: Boolean,
    busy: Boolean,
    saveFailed: Boolean,
    onExplore: () -> Unit,
    onFind: () -> Unit,
    onSort: () -> Unit,
    onRetry: () -> Unit,
    onHome: () -> Unit,
    modifier: Modifier = Modifier,
    onMatch: () -> Unit = {},
    onFourSort: () -> Unit = {},
    onRainbowExplore: () -> Unit = {},
    onRainbowOrder: () -> Unit = {}
) {
    val german = language == ContentLanguage.GERMAN
    fun t(en: String, de: String) = if (german) de else en
    Column(modifier.fillMaxSize().background(BastiPalette.background).verticalScroll(rememberScrollState())
        .padding(horizontal = BastiSpacing.medium, vertical = BastiSpacing.large),
        verticalArrangement = Arrangement.spacedBy(BastiSpacing.medium)) {
        NativeActivityTitle(t("Colours", "Farben"), Modifier.semantics { heading() })
        Surface(onClick = onExplore, enabled = ready && !busy && !saveFailed,
            modifier = Modifier.fillMaxWidth().heightIn(min = 176.dp).testTag("colours-open-explore")
                .semantics { role = Role.Button }, shape = BastiShapes.card,
            color = BastiCategoryTreatment.COLOURS.container,
            border = BorderStroke(2.dp, BastiPalette.outline)) {
            Column(Modifier.padding(BastiSpacing.medium), verticalArrangement = Arrangement.spacedBy(BastiSpacing.medium)) {
                Text(t("Explore Colours", "Farben entdecken"), style = BastiTypography.question,
                    color = BastiPalette.ink, modifier = Modifier.semantics { heading() })
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    listOf(ColourSwatches.red, ColourSwatches.blue, ColourSwatches.yellow, ColourSwatches.green).forEach { color ->
                        Box(Modifier.weight(1f).sizeIn(minHeight = 48.dp).aspectRatio(1f)
                            .background(color, CircleShape), contentAlignment = Alignment.Center) {}
                    }
                }
                Text(t("Tap a colour to see and hear its name.", "Tippe auf eine Farbe und höre ihren Namen."),
                    style = BastiTypography.parentInformation, color = BastiPalette.ink)
            }
        }
        Text(t("Practise", "Üben"), Modifier.fillMaxWidth().semantics { heading() },
            style = BastiTypography.answer, color = BastiPalette.ink)
        ColourActivityChoice("colours-open-find", t("Find the Colour", "Finde die Farbe"),
            t("Listen to a colour and find its swatch.", "Höre eine Farbe und finde das passende Farbfeld."),
            ready && !busy && !saveFailed, onFind)
        ColourActivityChoice("colours-open-match", t("Match Colours", "Farben zuordnen"),
            t("Match the same colour in a different shape.", "Ordne dieselbe Farbe in einer anderen Form zu."),
            ready && !busy && !saveFailed, onMatch)
        ColourActivityChoice("colours-open-sort", t("Colour Sort — Two Colours", "Farben sortieren — zwei Farben"),
            t("Put the red and blue objects with their colour groups.", "Sortiere die roten und blauen Dinge in die Farbgruppen."),
            ready && !busy && !saveFailed, onSort)
        ColourActivityChoice("colours-open-sort-four", t("Four-Colour Sort", "Vier Farben sortieren"),
            t("Sort eight objects into four colour groups.", "Sortiere acht Dinge in vier Farbgruppen."),
            ready && !busy && !saveFailed, onFourSort)
        Text(t("Discover the rainbow", "Den Regenbogen entdecken"), Modifier.fillMaxWidth().semantics { heading() },
            style = BastiTypography.answer, color = BastiPalette.ink)
        ColourActivityChoice("colours-open-rainbow-explore", t("The Rainbow", "Der Regenbogen"),
            t("Explore seven colours in their traditional order.", "Entdecke sieben Farben in ihrer traditionellen Reihenfolge."),
            ready && !busy && !saveFailed, onRainbowExplore)
        ColourActivityChoice("colours-open-rainbow-order", t("Rainbow Order", "Regenbogen ordnen"),
            t("Use an example to place rainbow colours in order.", "Ordne die Regenbogenfarben mithilfe eines Beispiels."),
            ready && !busy && !saveFailed, onRainbowOrder)
        if (busy && !ready) Text(t("Opening Colours…", "Farben werden geöffnet …"), Modifier.testTag("colours-chooser-loading"))
        if (saveFailed) {
            NativeSupportMessage(t("Your saved work is safe. Try again.", "Deine gespeicherte Arbeit ist sicher. Versuche es noch einmal."),
                Modifier.testTag("colours-chooser-error"))
            NativeActionButton(t("Try again", "Erneut versuchen"), NativeActionRole.SECONDARY, onRetry,
                Modifier.fillMaxWidth().testTag("colours-chooser-retry"), enabled = !busy)
        }
        NativeActionButton(t("Home", "Startseite"), NativeActionRole.NAVIGATION, onHome,
            Modifier.fillMaxWidth().testTag("colours-chooser-home"))
    }
}

@Composable
private fun ColourActivityChoice(tag: String, title: String, description: String, enabled: Boolean, onClick: () -> Unit) {
    Surface(onClick = onClick, enabled = enabled,
        modifier = Modifier.fillMaxWidth().heightIn(min = BastiSpacing.minimumTarget).testTag(tag)
            .semantics { role = Role.Button }, shape = BastiShapes.control,
        color = BastiPalette.surface, border = BorderStroke(1.dp, BastiPalette.outline)) {
        Column(Modifier.padding(BastiSpacing.medium), verticalArrangement = Arrangement.spacedBy(BastiSpacing.small)) {
            Text(title, style = BastiTypography.answer, color = BastiPalette.ink)
            Text(description, style = BastiTypography.parentInformation, color = BastiPalette.ink)
        }
    }
}
