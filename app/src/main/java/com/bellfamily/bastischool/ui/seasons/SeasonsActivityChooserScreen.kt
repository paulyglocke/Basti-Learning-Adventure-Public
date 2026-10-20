package com.bellfamily.bastischool.ui.seasons

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.focusable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.bellfamily.bastischool.R
import com.bellfamily.bastischool.learning.models.ContentLanguage
import com.bellfamily.bastischool.learning.seasons.SeasonsPhase
import com.bellfamily.bastischool.ui.common.NativeActionButton
import com.bellfamily.bastischool.ui.common.NativeActionRole
import com.bellfamily.bastischool.ui.common.NativeActivityTitle
import com.bellfamily.bastischool.ui.common.NativeSupportMessage
import com.bellfamily.bastischool.ui.design.BastiPalette
import com.bellfamily.bastischool.ui.design.BastiShapes
import com.bellfamily.bastischool.ui.design.BastiSpacing
import com.bellfamily.bastischool.ui.design.BastiTypography

private data class SeasonsChoice(
    val phase: SeasonsPhase,
    val description: String,
)

@Composable
fun SeasonsActivityChooserScreen(
    language: ContentLanguage,
    ready: Boolean,
    busy: Boolean,
    saveFailed: Boolean,
    onExplore: () -> Unit,
    onChoosePhase: (SeasonsPhase) -> Unit,
    onRetry: () -> Unit,
    onHome: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val german = language == ContentLanguage.GERMAN
    fun t(en: String, de: String) = if (german) de else en
    val recognition = listOf(
        SeasonsChoice(SeasonsPhase.PRACTICE, t("Look at a season picture and choose its name.", "Sieh dir ein Jahreszeitenbild an und wähle den Namen.")),
        SeasonsChoice(SeasonsPhase.NEXT, t("Find the season that comes next.", "Finde die Jahreszeit, die als Nächstes kommt.")),
        SeasonsChoice(SeasonsPhase.BEFORE, t("Find the season that comes before.", "Finde die Jahreszeit, die davor kommt.")),
        SeasonsChoice(SeasonsPhase.MISSING, t("Find the season missing from the year.", "Finde die fehlende Jahreszeit im Jahr.")),
        SeasonsChoice(SeasonsPhase.CLUES, t("Listen to a clue and choose the season.", "Höre einen Hinweis und wähle die Jahreszeit.")),
    )
    val buildAndConnect = listOf(
        SeasonsChoice(SeasonsPhase.ORDER, t("Place the seasons in their year order.", "Ordne die Jahreszeiten im Jahreslauf.")),
        SeasonsChoice(SeasonsPhase.MATCH, t("Match each season with its clue.", "Ordne jede Jahreszeit dem passenden Hinweis zu.")),
        SeasonsChoice(SeasonsPhase.COMBINED, t("Choose the season before and after.", "Finde die Jahreszeit davor und danach.")),
    )

    Column(
        modifier.fillMaxSize().background(BastiPalette.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = BastiSpacing.medium, vertical = BastiSpacing.large),
        verticalArrangement = Arrangement.spacedBy(BastiSpacing.medium),
    ) {
        NativeActivityTitle(t("Seasons", "Jahreszeiten"), Modifier.semantics { heading() })
        Text(t("Choose what to explore or practise", "Wähle, was du entdecken oder üben möchtest"),
            Modifier.fillMaxWidth(), style = BastiTypography.instruction, color = BastiPalette.ink)

        Surface(
            onClick = onExplore,
            enabled = ready,
            modifier = Modifier.fillMaxWidth().heightIn(min = 176.dp)
                .testTag("seasons-open-explore").focusable().keyboardActivate(onExplore)
                .semantics { role = Role.Button },
            shape = BastiShapes.card,
            color = BastiPalette.secondaryContainer,
            contentColor = BastiPalette.ink,
            border = BorderStroke(1.dp, BastiPalette.outline),
        ) {
            BoxWithConstraints(Modifier.padding(BastiSpacing.medium)) {
                if (maxWidth >= 620.dp * LocalDensity.current.fontScale) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(BastiSpacing.medium),
                        verticalAlignment = Alignment.CenterVertically) {
                        SeasonsIllustration(language, Modifier.widthIn(max = 224.dp).weight(1f))
                        ExploreCopy(language, Modifier.weight(1.2f))
                    }
                } else {
                    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(BastiSpacing.medium)) {
                        val fontScale = LocalDensity.current.fontScale
                        val illustrationSize = when {
                            fontScale >= 1.75f -> 88.dp
                            fontScale >= 1.25f -> 112.dp
                            else -> 144.dp
                        }
                        SeasonsIllustration(language,
                            Modifier.size(illustrationSize).align(Alignment.CenterHorizontally))
                        ExploreCopy(language, Modifier.fillMaxWidth())
                    }
                }
            }
        }

        SeasonsChoiceGroup(t("Practise the seasons", "Jahreszeiten üben"), recognition,
            language, ready && !busy, onChoosePhase)
        SeasonsChoiceGroup(t("Build and connect", "Ordnen und verbinden"), buildAndConnect,
            language, ready && !busy, onChoosePhase)
        if (saveFailed) {
            NativeSupportMessage(t("Saved progress could not be restored. Your saved records are kept. Try again.",
                "Der gespeicherte Fortschritt konnte nicht geladen werden. Gespeicherte Einträge bleiben erhalten. Versuche es erneut."))
            NativeActionButton(t("Try again", "Erneut versuchen"), NativeActionRole.SECONDARY,
                onRetry, Modifier.fillMaxWidth().testTag("seasons-chooser-retry"), enabled = !busy)
        } else if (busy || !ready) {
            Text(t("Getting the seasons ready…", "Die Jahreszeiten werden vorbereitet …"),
                Modifier.fillMaxWidth(), style = MaterialTheme.typography.bodyLarge, color = BastiPalette.ink)
        }
        NativeActionButton(t("Home", "Startseite"), NativeActionRole.NAVIGATION, onHome,
            Modifier.fillMaxWidth().testTag("seasons-chooser-home"))
    }
}

@Composable
private fun SeasonsIllustration(language: ContentLanguage, modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(R.drawable.seasons_center),
        contentDescription = if (language == ContentLanguage.GERMAN) "Vier Jahreszeiten" else "The four seasons",
        modifier = modifier.aspectRatio(1f).testTag("seasons-four-seasons-art"),
    )
}

@Composable
private fun ExploreCopy(language: ContentLanguage, modifier: Modifier = Modifier) {
    val german = language == ContentLanguage.GERMAN
    Column(modifier, verticalArrangement = Arrangement.spacedBy(BastiSpacing.small)) {
        Text(if (german) "Jahreszeiten entdecken" else "Explore the seasons",
            Modifier.fillMaxWidth().semantics { heading() }, style = BastiTypography.answer, color = BastiPalette.ink)
        Text(if (german) "Sieh dir die Bilder an, höre die Namen und entdecke, was jede Jahreszeit besonders macht."
             else "Look at the pictures, hear the names and discover what makes each season special.",
            Modifier.fillMaxWidth(), style = BastiTypography.parentInformation, color = BastiPalette.ink)
    }
}

@Composable
private fun SeasonsChoiceGroup(
    title: String,
    choices: List<SeasonsChoice>,
    language: ContentLanguage,
    ready: Boolean,
    onChoose: (SeasonsPhase) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(BastiSpacing.small)) {
        Text(title, Modifier.fillMaxWidth().semantics { heading() },
            style = BastiTypography.answer, color = BastiPalette.ink)
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            val columns = if (maxWidth >= 620.dp * LocalDensity.current.fontScale) 2 else 1
            Column(verticalArrangement = Arrangement.spacedBy(BastiSpacing.small)) {
                choices.chunked(columns).forEach { row ->
                    Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min),
                        horizontalArrangement = Arrangement.spacedBy(BastiSpacing.small)) {
                        row.forEach { choice ->
                            Surface(
                                onClick = { onChoose(choice.phase) },
                                enabled = ready,
                                modifier = Modifier.weight(1f).fillMaxHeight().heightIn(min = 76.dp)
                                    .testTag("seasons-choose-${choice.phase.name.lowercase()}")
                                    .focusable().keyboardActivate { onChoose(choice.phase) }
                                    .semantics { role = Role.Button },
                                shape = BastiShapes.control,
                                color = BastiPalette.surface,
                                contentColor = BastiPalette.ink,
                                border = BorderStroke(1.dp, BastiPalette.outline),
                            ) {
                                Column(Modifier.padding(BastiSpacing.medium),
                                    verticalArrangement = Arrangement.spacedBy(BastiSpacing.small)) {
                                    Text(choice.phase.title.display[language], Modifier.fillMaxWidth(),
                                        style = BastiTypography.answer)
                                    Text(choice.description, Modifier.fillMaxWidth(),
                                        style = BastiTypography.parentInformation)
                                }
                            }
                        }
                        if (row.size < columns) Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

private fun Modifier.keyboardActivate(onActivate: () -> Unit): Modifier = onKeyEvent { event ->
    if (event.type == KeyEventType.KeyUp &&
        event.key in setOf(Key.Enter, Key.Spacebar, Key.DirectionCenter)) {
        onActivate()
        true
    } else false
}
