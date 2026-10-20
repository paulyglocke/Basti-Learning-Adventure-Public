package com.bellfamily.bastischool.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
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


/** Presentation only. The shell owns pages, Options and real activity dispatch. Insets are host-owned. */
@Composable
internal fun HomeBrowseScreen(page: String, german: Boolean, onOpen: (String) -> Unit,
    onActivity: (HomeCard) -> Unit, modifier: Modifier = Modifier, recoveryFailed: Boolean = false) {
    val pages = rememberSaveableStateHolder()
    fun t(en: String, de: String) = if (german) de else en
    BastiDesignTheme {
        Surface(modifier.fillMaxSize(), color = BastiPalette.background) {
            BoxWithConstraints(Modifier.fillMaxSize()) {
                val compactHeight = maxHeight < 480.dp
                Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
                    if (page != "home") BoxWithConstraints(Modifier.widthIn(max = 1040.dp).fillMaxWidth()
                        .padding(horizontal = BastiSpacing.medium, vertical = BastiSpacing.small)) {
                        val back = @Composable { HomeHeaderAction(t("Back", "Zurück"), { onOpen("home") },
                            Modifier.testTag("home-back")) }
                        val grownUps = @Composable { HomeHeaderAction(t("Grown-ups", "Erwachsene"), { onOpen("settings") },
                            Modifier.testTag("home-settings")) }
                        val home = @Composable { HomeHeaderAction(t("Home", "Startseite"), { onOpen("home") },
                            Modifier.testTag("home-home")) }
                        if (maxWidth < 420.dp * LocalDensity.current.fontScale) {
                            Column(Modifier.fillMaxWidth()) {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    back(); grownUps()
                                }
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) { home() }
                            }
                        } else Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            back(); grownUps(); home()
                        }
                    }
                    pages.SaveableStateProvider(page) {
                        Column(Modifier.weight(1f).widthIn(max = 1088.dp).fillMaxWidth()
                            .testTag("home-scroll").verticalScroll(rememberScrollState())
                            .padding(if (compactHeight) BastiSpacing.medium else BastiSpacing.page),
                            verticalArrangement = Arrangement.spacedBy(BastiSpacing.medium)) {
                            if (recoveryFailed) NativeSupportMessage(t(
                                "The last activity could not be restored. Choose an activity to start again.",
                                "Das letzte Spiel konnte nicht wiederhergestellt werden. Wähle ein Spiel, um neu zu starten."))
                            when {
                                page == "home" -> HomeWelcomeCards(german, onOpen, compactHeight)
                                page == "all" -> {
                                    BastiSectionHeading(t("All Activities", "Alle Aktivitäten"))
                                    Text(t("20 activities to explore", "20 Aktivitäten zum Entdecken"),
                                        Modifier.fillMaxWidth(), style = BastiTypography.instruction)
                                    HomeActivityGrid(homeActivities(), german) { onActivity(homeActivity(it.substringAfter(':'))) }
                                }
                                else -> {
                                    val category = HomeCategory.valueOf(page.substringAfter(':'))
                                    HomeCategoryIntro(category, german)
                                    Text(t("Choose an activity.", "Wähle eine Aktivität."),
                                        Modifier.fillMaxWidth(), style = BastiTypography.parentInformation)
                                    HomeActivityGrid(category.activityIds.map(::homeActivity), german) {
                                        onActivity(homeActivity(it.substringAfter(':')))
                                    }
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
internal fun HomeWelcomeCards(german: Boolean, open: (String) -> Unit, compactHeight: Boolean) {
    fun t(en: String, de: String) = if (german) de else en
    if (compactHeight) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(BastiSpacing.small)) {
            Image(homeAsset(DRAGON_ART), null, Modifier.size(48.dp), contentScale = ContentScale.Fit)
            Text(t("Basti’s Learning Adventure", "Bastis Lernabenteuer"),
                Modifier.weight(1f).semantics { heading() }, style = BastiTypography.answer)
            HomeHeaderAction(t("Grown-ups", "Erwachsene"),
                { open("settings") }, Modifier.widthIn(max = 200.dp).testTag("home-settings"))
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
                    Image(homeAsset(DRAGON_ART), null, Modifier.size(64.dp), contentScale = ContentScale.Fit)
                    Spacer(Modifier.weight(1f))
                }
                HomeHeaderAction(t("Grown-ups", "Erwachsene"), { open("settings") },
                    Modifier.widthIn(max = settingsMaxWidth).testTag("home-settings"))
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
        val columns = homeColumns(maxWidth.value, LocalDensity.current.fontScale)
            .coerceAtMost(if (compactHeight) 2 else 3)
        Column(verticalArrangement = Arrangement.spacedBy(BastiSpacing.medium)) {
            HomeCategory.entries.chunked(columns).forEach { row ->
                Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(BastiSpacing.medium)) {
                    row.forEach { category ->
                        val cardModifier = Modifier.weight(1f).fillMaxHeight().testTag("home-category-${category.name}")
                        val choose = { open("category:${category.name}") }
                        if (compactHeight) CompactCategoryCard(category, german, choose, cardModifier)
                        else BastiCategoryCard(category.label(german), homeCategoryArt(category), category.treatment,
                            choose, cardModifier)
                    }
                }
            }
        }
    }
    if (compactHeight) Text(t("What shall we explore?", "Was wollen wir entdecken?"),
        Modifier.fillMaxWidth().semantics { heading() }, style = BastiTypography.homeHeading)
    NativeActionButton(t("All Activities", "Alle Aktivitäten"), NativeActionRole.SECONDARY,
        { open("all") }, Modifier.fillMaxWidth().testTag("home-all"))
}

/** Home chrome only. Material keeps focus/ripple/keyboard semantics. */
@Composable
internal fun HomeHeaderAction(label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    TextButton(onClick = onClick, modifier = modifier.width(IntrinsicSize.Max).sizeIn(minWidth = BastiSpacing.minimumTarget,
        minHeight = BastiSpacing.minimumTarget), shape = BastiShapes.control,
        contentPadding = PaddingValues(BastiSpacing.small)) {
        Text(label, Modifier.fillMaxWidth(), style = BastiTypography.action, textAlign = TextAlign.Center)
    }
}

/** Short windows need a horizontal illustration/label composition, not smaller text. */
@Composable
private fun CompactCategoryCard(category: HomeCategory, german: Boolean, onClick: () -> Unit, modifier: Modifier) {
    Surface(onClick = onClick, modifier = modifier.heightIn(min = BastiSpacing.minimumTarget)
        .semantics { role = Role.Button }, shape = BastiShapes.card,
        color = category.treatment.container, contentColor = category.treatment.foreground,
        border = BorderStroke(1.dp, BastiPalette.outline)) {
        Row(Modifier.padding(BastiSpacing.medium), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(BastiSpacing.small)) {
            Image(homeCategoryArt(category), null, Modifier.size(64.dp), contentScale = ContentScale.Fit)
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
            Image(homeAsset(DRAGON_ART), null, Modifier.size(96.dp), contentScale = ContentScale.Fit)
            Text(if (german) "Was wollen wir entdecken?" else "What shall we explore?",
                Modifier.weight(1f).semantics { heading() }, style = BastiTypography.homeHeading)
        } else Column(verticalArrangement = Arrangement.spacedBy(BastiSpacing.small)) {
            Image(homeAsset(DRAGON_ART), null, Modifier.size(64.dp), contentScale = ContentScale.Fit)
            Text(if (german) "Was wollen wir entdecken?" else "What shall we explore?",
                Modifier.fillMaxWidth().semantics { heading() }, style = BastiTypography.homeHeading)
        }
    }
}

@Composable
internal fun HomeCategoryIntro(category: HomeCategory, german: Boolean) {
    Surface(color = category.treatment.container, shape = BastiShapes.card, modifier = Modifier.fillMaxWidth()) {
        BoxWithConstraints(Modifier.padding(BastiSpacing.medium)) {
            if (maxWidth >= 280.dp * LocalDensity.current.fontScale) {
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(BastiSpacing.medium)) {
                    Image(homeCategoryArt(category), null, Modifier.size(80.dp), contentScale = ContentScale.Fit)
                    BastiSectionHeading(category.label(german), Modifier.weight(1f))
                }
            } else Column(verticalArrangement = Arrangement.spacedBy(BastiSpacing.small)) {
                Image(homeCategoryArt(category), null, Modifier.size(64.dp), contentScale = ContentScale.Fit)
                BastiSectionHeading(category.label(german))
            }
        }
    }
}

@Composable
internal fun HomeActivityGrid(cards: List<HomeCard>, german: Boolean, open: (String) -> Unit) {
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val columns = if (maxWidth >= 640.dp * LocalDensity.current.fontScale) 2 else 1
        Column(verticalArrangement = Arrangement.spacedBy(BastiSpacing.medium)) {
            cards.chunked(columns).forEach { row ->
                Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(BastiSpacing.medium)) {
                    row.forEach { card ->
                        val id = card.homeId()
                        Surface(onClick = { open("activity:$id") }, modifier = Modifier.weight(1f).fillMaxHeight()
                            .heightIn(min = BastiSpacing.minimumTarget).testTag("home-activity-$id")
                            .semantics { role = Role.Button }, shape = BastiShapes.card,
                            color = homeCategory(id)?.treatment?.container ?: BastiPalette.secondaryContainer,
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
