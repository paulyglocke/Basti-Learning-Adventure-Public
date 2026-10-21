package com.bellfamily.bastischool.ui.wilma

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.bellfamily.bastischool.learning.models.ContentLanguage
import com.bellfamily.bastischool.learning.wilma.WilmaContent
import com.bellfamily.bastischool.learning.wilma.WilmaPhase
import com.bellfamily.bastischool.ui.common.NativeActionButton
import com.bellfamily.bastischool.ui.common.NativeActionRole
import com.bellfamily.bastischool.ui.common.NativeActivityTitle
import com.bellfamily.bastischool.ui.design.BastiPalette
import com.bellfamily.bastischool.ui.design.BastiShapes
import com.bellfamily.bastischool.ui.design.BastiSpacing

private data class WilmaChoice(val phase: WilmaPhase, val description: String)

@Composable
fun WilmaActivityChooserScreen(
    language: ContentLanguage,
    images: Map<String, androidx.compose.ui.graphics.ImageBitmap>,
    ready: Boolean,
    busy: Boolean,
    saveFailed: Boolean,
    imageFailed: Boolean,
    onChoose: (WilmaPhase) -> Unit,
    onRetry: () -> Unit,
    onHome: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val german = language == ContentLanguage.GERMAN
    fun t(en: String, de: String) = if (german) de else en
    val practice = listOf(
        WilmaChoice(WilmaPhase.FIND,
            t("Listen for a day and find its place on Wilma.", "Höre einen Tag und finde seinen Platz auf Wilma.")),
        WilmaChoice(WilmaPhase.RELATIONS,
            t("Find the day before or after another day.", "Finde den Tag vor oder nach einem anderen Tag.")),
        WilmaChoice(WilmaPhase.ORDER,
            t("Put all seven days in their week order.", "Ordne alle sieben Tage in der richtigen Reihenfolge.")),
        WilmaChoice(WilmaPhase.TODAY,
            t("Explore today, yesterday and tomorrow.", "Entdecke heute, gestern und morgen.")),
    )
    val bilingual = WilmaChoice(WilmaPhase.BILINGUAL,
        t("See and hear every day in German and English.", "Sieh und höre jeden Tag auf Deutsch und Englisch."))

    Column(
        modifier.fillMaxSize().background(BastiPalette.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = BastiSpacing.medium, vertical = BastiSpacing.large),
        verticalArrangement = Arrangement.spacedBy(BastiSpacing.medium),
    ) {
        NativeActivityTitle(t("Wilma’s Week", "Wilmas Woche"), Modifier.semantics { heading() })
        Text(t("Choose what to explore or practise", "Wähle, was du entdecken oder üben möchtest"),
            Modifier.fillMaxWidth(), style = com.bellfamily.bastischool.ui.design.BastiTypography.instruction,
            color = BastiPalette.ink)

        Surface(
            onClick = { onChoose(WilmaPhase.EXPLORE) },
            enabled = ready && !busy && !saveFailed && !imageFailed,
            modifier = Modifier.fillMaxWidth().heightIn(min = 164.dp)
                .testTag("wilma-open-EXPLORE").focusable().activateOnKeyboard { onChoose(WilmaPhase.EXPLORE) }
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
                        WilmaArt(images, language, Modifier.weight(1f).heightIn(max = 180.dp))
                        ExploreCopy(language, Modifier.weight(1.2f))
                    }
                } else {
                    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(BastiSpacing.small)) {
                        WilmaArt(images, language, Modifier.fillMaxWidth().heightIn(max = 132.dp))
                        ExploreCopy(language, Modifier.fillMaxWidth())
                    }
                }
            }
        }

        if (busy && !ready) Text(t("Opening Wilma’s Week…", "Wilmas Woche wird geöffnet …"),
            Modifier.testTag("wilma-chooser-loading"), color = BastiPalette.ink)
        if (saveFailed || imageFailed) {
            Text(t("Your saved work is safe. Try opening Wilma’s Week again.",
                "Deine gespeicherte Arbeit ist sicher. Öffne Wilmas Woche noch einmal."),
                Modifier.testTag("wilma-chooser-error"), color = BastiPalette.ink)
            NativeActionButton(t("Try again", "Erneut versuchen"), NativeActionRole.SECONDARY,
                onRetry, enabled = !busy, modifier = Modifier.fillMaxWidth().testTag("wilma-chooser-retry"))
        }

        ChoiceGroup(t("Practise the days", "Wochentage üben"), practice, language,
            enabled = ready && !busy && !saveFailed && !imageFailed, onChoose)
        ChoiceGroup(t("Explore in two languages", "Zwei Sprachen entdecken"), listOf(bilingual), language,
            enabled = ready && !busy && !saveFailed && !imageFailed, onChoose)
        NativeActionButton(t("Home", "Startseite"), NativeActionRole.NAVIGATION, onHome,
            Modifier.fillMaxWidth().testTag("wilma-chooser-home"))
    }
}

@Composable
private fun ExploreCopy(language: ContentLanguage, modifier: Modifier = Modifier) {
    val german = language == ContentLanguage.GERMAN
    Column(modifier, verticalArrangement = Arrangement.spacedBy(BastiSpacing.small)) {
        Text(if (german) "Die Woche entdecken" else "Explore the week",
            Modifier.fillMaxWidth().semantics { heading() },
            style = com.bellfamily.bastischool.ui.design.BastiTypography.answer, color = BastiPalette.ink)
        Text(if (german) "Tippe auf einen Tag, um seinen Namen zu hören und Wilmas ganze Woche zu sehen."
            else "Choose a day to hear its name and explore Wilma’s whole week.",
            Modifier.fillMaxWidth(), style = com.bellfamily.bastischool.ui.design.BastiTypography.parentInformation,
            color = BastiPalette.ink)
    }
}

@Composable
private fun WilmaArt(
    images: Map<String, androidx.compose.ui.graphics.ImageBitmap>,
    language: ContentLanguage,
    modifier: Modifier = Modifier,
) {
    images[WilmaContent.REFERENCE]?.let {
        Image(it, if (language == ContentLanguage.GERMAN) "Wilma mit den sieben Wochentagen" else "Wilma with the seven days of the week",
            modifier.fillMaxWidth().testTag("wilma-chooser-artwork"), contentScale = ContentScale.Fit)
    }
}

@Composable
private fun ChoiceGroup(
    title: String,
    choices: List<WilmaChoice>,
    language: ContentLanguage,
    enabled: Boolean,
    onChoose: (WilmaPhase) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(BastiSpacing.small)) {
        Text(title, Modifier.fillMaxWidth().semantics { heading() },
            style = com.bellfamily.bastischool.ui.design.BastiTypography.answer, color = BastiPalette.ink)
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            val columns = if (maxWidth >= 620.dp * LocalDensity.current.fontScale) 2 else 1
            Column(verticalArrangement = Arrangement.spacedBy(BastiSpacing.small)) {
                choices.chunked(columns).forEach { row ->
                    Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(BastiSpacing.small)) {
                        row.forEach { choice ->
                            Surface(
                                onClick = { onChoose(choice.phase) }, enabled = enabled,
                                modifier = Modifier.weight(1f).fillMaxHeight().heightIn(min = 76.dp)
                                    .testTag("wilma-open-${choice.phase.name}").focusable()
                                    .activateOnKeyboard { onChoose(choice.phase) }.semantics { role = Role.Button },
                                shape = BastiShapes.control, color = BastiPalette.surface,
                                contentColor = BastiPalette.ink,
                                border = BorderStroke(1.dp, BastiPalette.outline),
                            ) {
                                Column(Modifier.padding(BastiSpacing.medium), verticalArrangement = Arrangement.spacedBy(BastiSpacing.small)) {
                                    Text(choice.phase.title.display[language], Modifier.fillMaxWidth(),
                                        style = com.bellfamily.bastischool.ui.design.BastiTypography.answer)
                                    Text(choice.description, Modifier.fillMaxWidth(),
                                        style = com.bellfamily.bastischool.ui.design.BastiTypography.parentInformation)
                                }
                            }
                        }
                        repeat(columns - row.size) { Spacer(Modifier.weight(1f)) }
                    }
                }
            }
        }
    }
}

private fun Modifier.activateOnKeyboard(onActivate: () -> Unit) = onKeyEvent { event ->
    if (event.type == KeyEventType.KeyUp && event.key in setOf(Key.Enter, Key.Spacebar, Key.NumPadEnter, Key.DirectionCenter)) {
        onActivate(); true
    } else false
}
