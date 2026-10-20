package com.bellfamily.bastischool.ui.months

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.bellfamily.bastischool.learning.models.ContentLanguage
import com.bellfamily.bastischool.learning.content.SeasonIds
import com.bellfamily.bastischool.learning.months.MonthsMode
import com.bellfamily.bastischool.ui.common.NativeActionButton
import com.bellfamily.bastischool.ui.common.NativeActionRole
import com.bellfamily.bastischool.ui.common.NativeActivityTitle
import com.bellfamily.bastischool.ui.design.BastiPalette
import com.bellfamily.bastischool.ui.design.BastiShapes
import com.bellfamily.bastischool.ui.design.BastiSpacing
import com.bellfamily.bastischool.ui.design.BastiTypography

private data class MonthsChoice(
    val tag: String,
    val title: String,
    val description: String,
    val onChoose: () -> Unit,
)

/** Months-only navigation. It owns no learning state or persistent activity identity. */
@Composable
fun MonthsActivityChooserScreen(
    language: ContentLanguage,
    onExplore: () -> Unit,
    onFind: () -> Unit,
    onRelations: () -> Unit,
    onMissing: () -> Unit,
    onBetween: () -> Unit,
    onBuildYear: () -> Unit,
    onSeasonsMatch: () -> Unit,
    onSeasonsWheel: () -> Unit,
    onHome: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val german = language == ContentLanguage.GERMAN
    fun t(en: String, de: String) = if (german) de else en
    val practice = listOf(
        MonthsChoice("months-open-find", MonthsMode.FIND.title.display[language],
            t("Hear a month and find its name.", "Höre einen Monat und finde seinen Namen."), onFind),
        MonthsChoice("months-open-relations", MonthsMode.RELATIONS.title.display[language],
            t("Explore which month comes before or next.", "Entdecke, welcher Monat davor oder danach kommt."), onRelations),
        MonthsChoice("months-open-missing", MonthsMode.MISSING.title.display[language],
            t("Find the month missing from a short sequence.", "Finde den Monat, der in einer kurzen Reihe fehlt."), onMissing),
        MonthsChoice("months-open-between", MonthsMode.BETWEEN.title.display[language],
            t("Find a month between two neighbours.", "Finde einen Monat zwischen zwei Nachbarmonaten."), onBetween),
    )
    val connect = listOf(
        MonthsChoice("months-open-build-year", MonthsMode.BUILD_YEAR.title.display[language],
            t("Choose a three- or four-month sequence to build.", "Wähle eine Reihenfolge aus drei oder vier Monaten."), onBuildYear),
        MonthsChoice("months-open-seasons-match", t("Sort Months into Seasons", "Monate den Jahreszeiten zuordnen"),
            t("Place each month in its season group.", "Ordne jeden Monat seiner Jahreszeitengruppe zu."), onSeasonsMatch),
        MonthsChoice("months-open-seasons-wheel", t("Seasons & Months Wheel", "Jahreszeiten- und Monatsrad"),
            t("Explore the year wheel or match months to seasons.", "Entdecke das Jahresrad oder ordne Monate den Jahreszeiten zu."), onSeasonsWheel),
    )

    Column(modifier.fillMaxSize().background(BastiPalette.background)
        .verticalScroll(rememberScrollState()).padding(horizontal = BastiSpacing.medium, vertical = BastiSpacing.large),
        verticalArrangement = Arrangement.spacedBy(BastiSpacing.medium)) {
        NativeActivityTitle(t("Months", "Monate"), Modifier.semantics { heading() })
        Text(t("Choose what to do", "Wähle, was du machen möchtest"),
            style = BastiTypography.instruction, color = BastiPalette.ink)

        Surface(onClick = onExplore, modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp)
            .testTag("months-open-explore").semantics { role = Role.Button },
            shape = BastiShapes.card, color = BastiPalette.secondaryContainer,
            contentColor = BastiPalette.ink, border = androidx.compose.foundation.BorderStroke(1.dp, BastiPalette.outline)) {
            Column(Modifier.padding(BastiSpacing.medium), verticalArrangement = Arrangement.spacedBy(BastiSpacing.small)) {
                Text(t("Explore the Year Wheel", "Das Jahresrad entdecken"),
                    Modifier.fillMaxWidth().semantics { heading() }, style = BastiTypography.answer)
                Text(t("Choose a month on the wheel or from the month names below. Hear its name and explore its season.",
                    "Wähle einen Monat auf dem Rad oder aus der Monatsliste. Höre seinen Namen und entdecke seine Jahreszeit."),
                    Modifier.fillMaxWidth(), style = BastiTypography.parentInformation)
                Row(Modifier.fillMaxWidth().height(8.dp), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    listOf(SeasonIds.WINTER, SeasonIds.SPRING, SeasonIds.SUMMER, SeasonIds.AUTUMN).forEach { season ->
                        Box(Modifier.weight(1f).fillMaxHeight().background(seasonColour(season), RoundedCornerShape(8.dp)))
                    }
                }
            }
        }

        ChoiceGroup(t("Practise Months", "Monate üben"), practice)
        ChoiceGroup(t("Build and connect", "Ordnen und verbinden"), connect)
        NativeActionButton(t("Home", "Startseite"), NativeActionRole.NAVIGATION, onHome,
            Modifier.fillMaxWidth().testTag("months-chooser-home"))
    }
}

@Composable
private fun ChoiceGroup(title: String, choices: List<MonthsChoice>) {
    Column(verticalArrangement = Arrangement.spacedBy(BastiSpacing.small)) {
        Text(title, Modifier.fillMaxWidth().semantics { heading() },
            style = BastiTypography.answer, color = BastiPalette.ink)
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            val columns = if (maxWidth >= 620.dp * LocalDensity.current.fontScale) 2 else 1
            Column(verticalArrangement = Arrangement.spacedBy(BastiSpacing.small)) {
                choices.chunked(columns).forEach { row ->
                    Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(BastiSpacing.small)) {
                        row.forEach { choice ->
                            Surface(onClick = choice.onChoose,
                                modifier = Modifier.weight(1f).fillMaxHeight().heightIn(min = 76.dp)
                                    .testTag(choice.tag).semantics { role = Role.Button },
                                shape = BastiShapes.control, color = BastiPalette.surface,
                                contentColor = BastiPalette.ink,
                                border = androidx.compose.foundation.BorderStroke(1.dp, BastiPalette.outline)) {
                                Column(Modifier.padding(BastiSpacing.medium), verticalArrangement = Arrangement.spacedBy(BastiSpacing.small),
                                    horizontalAlignment = Alignment.Start) {
                                    Text(choice.title, Modifier.fillMaxWidth(), style = BastiTypography.answer)
                                    Text(choice.description, Modifier.fillMaxWidth(), style = BastiTypography.parentInformation)
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
