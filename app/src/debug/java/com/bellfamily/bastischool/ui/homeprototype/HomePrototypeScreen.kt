package com.bellfamily.bastischool.ui.homeprototype

import com.bellfamily.bastischool.ui.home.*
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.bellfamily.bastischool.HomeCard
import com.bellfamily.bastischool.ui.common.NativeActionButton
import com.bellfamily.bastischool.ui.common.NativeActionRole
import com.bellfamily.bastischool.ui.common.NativeSupportMessage
import com.bellfamily.bastischool.ui.design.*

/** Debug-only interactive review. Never calls a production activity callback. */
@Composable
internal fun HomePrototype(modifier: Modifier = Modifier, initialGerman: Boolean = false) {
    val navSaver = remember { Saver<PrototypeNavigation, ArrayList<String>>(
        save = { ArrayList(it.stack) }, restore = { PrototypeNavigation.restore(it) }) }
    var navigation by rememberSaveable(stateSaver = navSaver) { mutableStateOf(PrototypeNavigation()) }
    var german by rememberSaveable { mutableStateOf(initialGerman) }
    val pageStates = rememberSaveableStateHolder()
    val back = { navigation = navigation.back() }
    val open: (String) -> Unit = { navigation = navigation.open(it) }
    fun t(en: String, de: String) = if (german) de else en
    BackHandler(navigation.stack.size > 1, back)

    BastiDesignTheme {
        Surface(modifier.fillMaxSize(), color = BastiPalette.background) {
            BoxWithConstraints(Modifier.fillMaxSize()) {
            val compactHeight = maxHeight < 480.dp
            Column(Modifier.safeDrawingPadding().fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
                if (navigation.current != "home") {
                    Row(Modifier.widthIn(max = 1040.dp).fillMaxWidth().padding(horizontal = BastiSpacing.medium,
                        vertical = BastiSpacing.small), horizontalArrangement = Arrangement.SpaceBetween) {
                        HomeHeaderAction(t("Back", "Zurück"), back,
                            Modifier.weight(1f, fill = false).testTag("home-back"))
                        HomeHeaderAction(t("Home", "Startseite"), { open("home") },
                            Modifier.weight(1f, fill = false).testTag("home-home"))
                    }
                }
                pageStates.SaveableStateProvider(navigation.current) {
                    val scroll = rememberScrollState()
                    Column(Modifier.weight(1f).widthIn(max = 1088.dp).fillMaxWidth()
                        .testTag("prototype-scroll").verticalScroll(scroll)
                        .padding(if (compactHeight) BastiSpacing.medium else BastiSpacing.page),
                        verticalArrangement = Arrangement.spacedBy(BastiSpacing.medium)) {
                        when {
                            navigation.current == "home" -> { HomeWelcomeCards(german, open, compactHeight); PreviewNotice(german); MathsArtNotice(german) }
                            navigation.current == "settings" -> {
                                BastiSectionHeading(t("For grown-ups", "Für Erwachsene"))
                                NativeSupportMessage(t("Preview settings only. Your app settings and learning sessions stay unchanged.",
                                    "Nur Vorschau-Einstellungen. Deine App-Einstellungen und Lernrunden bleiben unverändert."))
                                BastiSectionHeading(t("Preview language", "Sprache der Vorschau"))
                                listOf(false to "English", true to "Deutsch").forEach { (de, label) ->
                                    FilterChip(selected = german == de, onClick = { german = de },
                                        label = { Text(label, Modifier.fillMaxWidth(), style = BastiTypography.answer) },
                                        modifier = Modifier.fillMaxWidth().heightIn(min = BastiSpacing.minimumTarget)
                                            .testTag("prototype-language-${if (de) "de" else "en"}"))
                                }
                                Text(t("No sound, progress or session settings are changed in this prototype.",
                                    "Dieser Prototyp ändert weder Ton noch Fortschritt oder Lernrunden."),
                                    Modifier.fillMaxWidth(), style = BastiTypography.parentInformation)
                            }
                            navigation.current == "all" -> {
                                BastiSectionHeading(t("All Activities", "Alle Aktivitäten"))
                                Text(t("20 activities to explore", "20 Aktivitäten zum Entdecken"),
                                    Modifier.fillMaxWidth(), style = BastiTypography.instruction)
                                PreviewNotice(german)
                                HomeActivityGrid(homeActivities(), german, open)
                            }
                            navigation.current.startsWith("category:") -> {
                                val category = HomeCategory.valueOf(navigation.current.substringAfter(':'))
                                HomeCategoryIntro(category, german)
                                Text(t("Choose an activity to preview.", "Wähle eine Aktivität für die Vorschau."),
                                    Modifier.fillMaxWidth(), style = BastiTypography.parentInformation)
                                HomeActivityGrid(category.activityIds.map(::homeActivity), german, open)
                                if (category == HomeCategory.MATHS) MathsArtNotice(german)
                                if (category == HomeCategory.COLOURS) NativeSupportMessage(t(
                                    "One activity here. In a future Home, this category can open Colour Sort directly.",
                                    "Hier gibt es eine Aktivität. Auf der künftigen Startseite kann Farben direkt zum Farbensortieren führen."))
                            }
                            navigation.current.startsWith("activity:") -> {
                                val id = navigation.current.substringAfter(':')
                                val card = homeActivity(id)
                                BastiSectionHeading(if (german) card.titleDe else card.titleEn)
                                if (id == "washHands") NativeSupportMessage(t(
                                    "Picture placeholder: a handwashing illustration is still needed.",
                                    "Bildplatzhalter: Eine Illustration zum Händewaschen fehlt noch."))
                                else Image(homeCategoryArt(homeCategory(id) ?: HomeCategory.WORDS), null,
                                    Modifier.fillMaxWidth().height(160.dp), contentScale = ContentScale.Fit)
                                Text(if (german) card.descriptionDe else card.descriptionEn,
                                    Modifier.fillMaxWidth(), style = BastiTypography.question)
                                NativeSupportMessage(t("Activity preview — no game has started. Your saved work is untouched.",
                                    "Aktivitätsvorschau — kein Spiel wurde gestartet. Dein gespeicherter Stand bleibt unverändert."),
                                    Modifier.testTag("prototype-detail-notice"))
                                if (id == "time") {
                                    Text(t("When integrated, Days & Seasons keeps its existing destinations:",
                                        "Bei der späteren Einbindung bleiben die bisherigen Ziele erhalten:"),
                                        Modifier.fillMaxWidth(), style = BastiTypography.instruction)
                                    Text(t("Wilma’s Week · Seasons · Months · Previous-version activities",
                                        "Wilmas Woche · Jahreszeiten · Monate · Aktivitäten der bisherigen Version"),
                                        Modifier.fillMaxWidth(), style = BastiTypography.answer)
                                }
                                if (id == "mixed") Text(t("Mixed Adventure stays a separate cross-subject activity, not Today’s Adventure.",
                                    "Das Gemischte Abenteuer bleibt eine eigene fächerübergreifende Aktivität, nicht das geplante Tagesabenteuer."),
                                    Modifier.fillMaxWidth(), style = BastiTypography.instruction)
                                NativeActionButton(t("Back to activities", "Zurück zu den Aktivitäten"), NativeActionRole.SECONDARY,
                                    back, Modifier.fillMaxWidth().testTag("prototype-detail-back"))
                            }
                        }
                    }
                }
            }
            }
        }
    }
}

@Composable
private fun PreviewNotice(german: Boolean) {
    Text(if (german) "Vorschau · Hier starten keine Spiele." else "Preview · No games start here.",
        Modifier.fillMaxWidth().testTag("prototype-notice"), style = BastiTypography.parentInformation)
}

@Composable
private fun MathsArtNotice(german: Boolean) {
    Text(if (german) "Mathebild: vorläufige Zählskizze." else "Maths picture: temporary counting sketch.",
        Modifier.fillMaxWidth(), style = BastiTypography.parentInformation)
}
