package com.bellfamily.bastischool.ui.homeprototype

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
                        PrototypeHeaderAction(t("Back", "Zurück"), back,
                            Modifier.weight(1f, fill = false).testTag("prototype-back"))
                        PrototypeHeaderAction(t("Home", "Startseite"), { open("home") },
                            Modifier.weight(1f, fill = false).testTag("prototype-home"))
                    }
                }
                pageStates.SaveableStateProvider(navigation.current) {
                    val scroll = rememberScrollState()
                    Column(Modifier.weight(1f).widthIn(max = 1088.dp).fillMaxWidth()
                        .testTag("prototype-scroll").verticalScroll(scroll)
                        .padding(if (compactHeight) BastiSpacing.medium else BastiSpacing.page),
                        verticalArrangement = Arrangement.spacedBy(BastiSpacing.medium)) {
                        when {
                            navigation.current == "home" -> PrototypeHome(german, open, compactHeight)
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
                                ActivityGrid(prototypeActivities(), german, open)
                            }
                            navigation.current.startsWith("category:") -> {
                                val category = PrototypeCategory.valueOf(navigation.current.substringAfter(':'))
                                CategoryIntro(category, german)
                                Text(t("Choose an activity to preview.", "Wähle eine Aktivität für die Vorschau."),
                                    Modifier.fillMaxWidth(), style = BastiTypography.parentInformation)
                                ActivityGrid(category.activityIds.map(::prototypeActivity), german, open)
                                if (category == PrototypeCategory.MATHS) MathsArtNotice(german)
                                if (category == PrototypeCategory.COLOURS) NativeSupportMessage(t(
                                    "One activity here. In a future Home, this category can open Colour Sort directly.",
                                    "Hier gibt es eine Aktivität. Auf der künftigen Startseite kann Farben direkt zum Farbensortieren führen."))
                            }
                            navigation.current.startsWith("activity:") -> {
                                val id = navigation.current.substringAfter(':')
                                val card = prototypeActivity(id)
                                BastiSectionHeading(if (german) card.titleDe else card.titleEn)
                                if (id == "washHands") NativeSupportMessage(t(
                                    "Picture placeholder: a handwashing illustration is still needed.",
                                    "Bildplatzhalter: Eine Illustration zum Händewaschen fehlt noch."))
                                else Image(prototypeCategoryArt(prototypeCategory(id) ?: PrototypeCategory.WORDS), null,
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
private fun PrototypeHome(german: Boolean, open: (String) -> Unit, compactHeight: Boolean) {
    fun t(en: String, de: String) = if (german) de else en
    if (compactHeight) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(BastiSpacing.small)) {
            Image(prototypeAsset(DRAGON_ART), null, Modifier.size(48.dp), contentScale = ContentScale.Fit)
            Text(t("Basti’s Learning Adventure", "Bastis Lernabenteuer"),
                Modifier.weight(1f).semantics { heading() }, style = BastiTypography.answer)
            PrototypeHeaderAction(t("Grown-ups", "Erwachsene"),
                { open("settings") }, Modifier.widthIn(max = 200.dp).testTag("prototype-settings"))
        }
    } else {
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val inlineTitle = maxWidth >= 300.dp * LocalDensity.current.fontScale
        val settingsMaxWidth = maxWidth - 80.dp
        Column(verticalArrangement = Arrangement.spacedBy(BastiSpacing.small)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(BastiSpacing.small)) {
                if (inlineTitle) Text(t("Basti’s Learning Adventure", "Bastis Lernabenteuer"),
                    Modifier.weight(1f).semantics { heading() }, style = BastiTypography.answer)
                else {
                    Image(prototypeAsset(DRAGON_ART), null, Modifier.size(64.dp), contentScale = ContentScale.Fit)
                    Spacer(Modifier.weight(1f))
                }
                PrototypeHeaderAction(t("Grown-ups", "Erwachsene"), { open("settings") },
                    Modifier.widthIn(max = settingsMaxWidth).testTag("prototype-settings"))
            }
            if (inlineTitle) Welcome(german, Modifier.fillMaxWidth())
            else {
                Text(t("Basti’s Learning Adventure", "Bastis Lernabenteuer"),
                    Modifier.fillMaxWidth().semantics { heading() }, style = BastiTypography.answer)
                Text(t("What shall we explore?", "Was wollen wir entdecken?"),
                    Modifier.fillMaxWidth().semantics { heading() }, style = BastiTypography.homeHeading)
            }
        }
    }
    }
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val columns = prototypeColumns(maxWidth.value, LocalDensity.current.fontScale)
            .coerceAtMost(if (compactHeight) 2 else 3)
        Column(verticalArrangement = Arrangement.spacedBy(BastiSpacing.medium)) {
            PrototypeCategory.entries.chunked(columns).forEach { row ->
                Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(BastiSpacing.medium)) {
                    row.forEach { category ->
                        val cardModifier = Modifier.weight(1f).fillMaxHeight().testTag("prototype-category-${category.name}")
                        val choose = { open("category:${category.name}") }
                        if (compactHeight) CompactCategoryCard(category, german, choose, cardModifier)
                        else BastiCategoryCard(category.label(german), prototypeCategoryArt(category), category.treatment,
                            choose, cardModifier)
                    }
                }
            }
        }
    }
    if (compactHeight) Text(t("What shall we explore?", "Was wollen wir entdecken?"),
        Modifier.fillMaxWidth().semantics { heading() }, style = BastiTypography.homeHeading)
    NativeActionButton(t("All Activities", "Alle Aktivitäten"), NativeActionRole.SECONDARY,
        { open("all") }, Modifier.fillMaxWidth().testTag("prototype-all"))
    PreviewNotice(german)
    MathsArtNotice(german)
}

/** Opt-in prototype chrome only. Material keeps focus/ripple/keyboard semantics. */
@Composable
private fun PrototypeHeaderAction(label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    TextButton(onClick = onClick, modifier = modifier.width(IntrinsicSize.Max).sizeIn(minWidth = BastiSpacing.minimumTarget,
        minHeight = BastiSpacing.minimumTarget), shape = BastiShapes.control,
        contentPadding = PaddingValues(BastiSpacing.small)) {
        Text(label, Modifier.fillMaxWidth(), style = BastiTypography.action, textAlign = TextAlign.Center)
    }
}

/** Short windows need a horizontal illustration/label composition, not smaller text. */
@Composable
private fun CompactCategoryCard(category: PrototypeCategory, german: Boolean, onClick: () -> Unit, modifier: Modifier) {
    Surface(onClick = onClick, modifier = modifier.heightIn(min = BastiSpacing.minimumTarget)
        .semantics { role = Role.Button }, shape = BastiShapes.card,
        color = category.treatment.container, contentColor = category.treatment.foreground,
        border = BorderStroke(1.dp, BastiPalette.outline)) {
        Row(Modifier.padding(BastiSpacing.medium), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(BastiSpacing.small)) {
            Image(prototypeCategoryArt(category), null, Modifier.size(64.dp), contentScale = ContentScale.Fit)
            Text(category.label(german), Modifier.weight(1f), style = BastiTypography.answer)
        }
    }
}

@Composable
private fun Welcome(german: Boolean, modifier: Modifier = Modifier) {
    BoxWithConstraints(modifier) {
        val spacious = maxWidth >= 340.dp * LocalDensity.current.fontScale
        // At large text the dragon no longer steals the invitation's reading width.
        if (spacious) Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(BastiSpacing.medium)) {
            Image(prototypeAsset(DRAGON_ART), null, Modifier.size(96.dp), contentScale = ContentScale.Fit)
            Text(if (german) "Was wollen wir entdecken?" else "What shall we explore?",
                Modifier.weight(1f).semantics { heading() }, style = BastiTypography.homeHeading)
        } else Column(verticalArrangement = Arrangement.spacedBy(BastiSpacing.small)) {
            Image(prototypeAsset(DRAGON_ART), null, Modifier.size(64.dp), contentScale = ContentScale.Fit)
            Text(if (german) "Was wollen wir entdecken?" else "What shall we explore?",
                Modifier.fillMaxWidth().semantics { heading() }, style = BastiTypography.homeHeading)
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

@Composable
private fun CategoryIntro(category: PrototypeCategory, german: Boolean) {
    Surface(color = category.treatment.container, shape = BastiShapes.card, modifier = Modifier.fillMaxWidth()) {
        BoxWithConstraints(Modifier.padding(BastiSpacing.medium)) {
            if (maxWidth >= 280.dp * LocalDensity.current.fontScale) {
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(BastiSpacing.medium)) {
                    Image(prototypeCategoryArt(category), null, Modifier.size(80.dp), contentScale = ContentScale.Fit)
                    BastiSectionHeading(category.label(german), Modifier.weight(1f))
                }
            } else Column(verticalArrangement = Arrangement.spacedBy(BastiSpacing.small)) {
                Image(prototypeCategoryArt(category), null, Modifier.size(64.dp), contentScale = ContentScale.Fit)
                BastiSectionHeading(category.label(german))
            }
        }
    }
}

@Composable
private fun ActivityGrid(cards: List<HomeCard>, german: Boolean, open: (String) -> Unit) {
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val columns = if (maxWidth >= 640.dp * LocalDensity.current.fontScale) 2 else 1
        Column(verticalArrangement = Arrangement.spacedBy(BastiSpacing.medium)) {
            cards.chunked(columns).forEach { row ->
                Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(BastiSpacing.medium)) {
                    row.forEach { card ->
                        val id = card.prototypeId()
                        Surface(onClick = { open("activity:$id") }, modifier = Modifier.weight(1f).fillMaxHeight()
                            .heightIn(min = BastiSpacing.minimumTarget).testTag("prototype-activity-$id")
                            .semantics { role = Role.Button }, shape = BastiShapes.card,
                            color = prototypeCategory(id)?.treatment?.container ?: BastiPalette.secondaryContainer,
                            contentColor = BastiPalette.ink, border = BorderStroke(1.dp, BastiPalette.outline)) {
                            Column(Modifier.padding(BastiSpacing.medium), verticalArrangement = Arrangement.spacedBy(BastiSpacing.small)) {
                                Text(if (german) card.titleDe else card.titleEn, Modifier.fillMaxWidth(), style = BastiTypography.answer)
                                Text(if (german) card.descriptionDe else card.descriptionEn,
                                    Modifier.fillMaxWidth(), style = BastiTypography.parentInformation)
                            }
                        }
                    }
                    if (row.size < columns) Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}
