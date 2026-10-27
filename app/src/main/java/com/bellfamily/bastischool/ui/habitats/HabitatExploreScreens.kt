package com.bellfamily.bastischool.ui.habitats

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.bellfamily.bastischool.learning.animals.HabitatExploreContent
import com.bellfamily.bastischool.learning.models.ContentLanguage
import com.bellfamily.bastischool.ui.common.NativeActionButton
import com.bellfamily.bastischool.ui.common.NativeActionRole
import com.bellfamily.bastischool.ui.common.NativeActivityTitle
import com.bellfamily.bastischool.ui.common.NativeSupportMessage
import com.bellfamily.bastischool.ui.design.BastiPalette
import com.bellfamily.bastischool.ui.design.BastiShapes
import com.bellfamily.bastischool.ui.design.BastiSpacing
import com.bellfamily.bastischool.ui.design.BastiTypography

@Composable
fun HabitatExploreChooserScreen(
    language: ContentLanguage,
    image: ImageBitmap?,
    artworkFailed: Boolean,
    onOcean: () -> Unit,
    onAnimals: () -> Unit,
    onHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    val german = language == ContentLanguage.GERMAN
    fun t(en: String, de: String) = if (german) de else en
    Column(modifier.fillMaxSize().background(BastiPalette.background).verticalScroll(rememberScrollState())
        .padding(BastiSpacing.medium), verticalArrangement = Arrangement.spacedBy(BastiSpacing.medium)) {
        NativeActivityTitle(t("Explore Habitats", "Lebensräume entdecken"), Modifier.semantics { heading() })
        Text(HabitatExploreContent.choosePrompt.display[language], Modifier.fillMaxWidth(),
            style = BastiTypography.instruction, color = BastiPalette.ink)
        Surface(onClick = onOcean, enabled = image != null,
            modifier = Modifier.fillMaxWidth().testTag("habitat-ocean-choice")
                .semantics { role = Role.Button },
            shape = BastiShapes.card, color = BastiPalette.secondaryContainer,
            contentColor = BastiPalette.ink, border = BorderStroke(1.dp, BastiPalette.outline)) {
            Column(Modifier.padding(BastiSpacing.medium), verticalArrangement = Arrangement.spacedBy(BastiSpacing.small)) {
                image?.let {
                    Image(it, contentDescription = null, modifier = Modifier.fillMaxWidth()
                        .heightIn(min = 120.dp, max = 260.dp).clip(RoundedCornerShape(18.dp))
                        .testTag("habitat-ocean-preview"), contentScale = ContentScale.Fit)
                }
                Text(HabitatExploreContent.oceanTitle.display[language],
                    Modifier.fillMaxWidth().semantics { heading() }, style = BastiTypography.answer)
                Text(t("Explore a sea scene and meet a dolphin.", "Entdecke eine Meereslandschaft und lerne einen Delfin kennen."),
                    Modifier.fillMaxWidth(), style = BastiTypography.parentInformation)
                if (image == null && !artworkFailed)
                    Text(t("Opening the ocean…", "Das Meer wird geöffnet …"), Modifier.testTag("habitat-ocean-loading"))
            }
        }
        if (artworkFailed) NativeSupportMessage(t(
            "The ocean picture could not be opened. Try again or return to Animals & Nature.",
            "Das Meeresbild konnte nicht geöffnet werden. Versuche es erneut oder gehe zu Tiere & Natur zurück."))
        NativeActionButton(t("Animals & Nature", "Tiere & Natur"), NativeActionRole.SECONDARY,
            onAnimals, Modifier.fillMaxWidth().testTag("habitats-animals-back"))
        NativeActionButton(t("Home", "Startseite"), NativeActionRole.NAVIGATION,
            onHome, Modifier.fillMaxWidth().testTag("habitats-home"))
    }
}

@Composable
fun OceanExploreScreen(
    language: ContentLanguage,
    image: ImageBitmap?,
    artworkFailed: Boolean,
    selectedDolphin: Boolean,
    audioFailed: Boolean,
    onDolphin: () -> Unit,
    onReplay: () -> Unit,
    onHabitats: () -> Unit,
    onHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    val german = language == ContentLanguage.GERMAN
    fun t(en: String, de: String) = if (german) de else en
    val dolphinName = HabitatExploreContent.dolphinName.display[language]
    Column(modifier.fillMaxSize().background(BastiPalette.background).verticalScroll(rememberScrollState())
        .padding(horizontal = BastiSpacing.medium, vertical = BastiSpacing.small),
        verticalArrangement = Arrangement.spacedBy(BastiSpacing.small)) {
        NativeActivityTitle(HabitatExploreContent.oceanTitle.display[language],
            Modifier.semantics { heading() }.testTag("ocean-title"))
        Text(HabitatExploreContent.scenePrompt.display[language], Modifier.fillMaxWidth().testTag("ocean-prompt"),
            style = BastiTypography.instruction, color = BastiPalette.ink)
        if (image != null) OceanScene(image, selectedDolphin, language, dolphinName, onDolphin)
        else if (artworkFailed) NativeSupportMessage(t(
            "The ocean picture could not be opened. You can still choose the dolphin by name.",
            "Das Meeresbild konnte nicht geöffnet werden. Du kannst den Delfin trotzdem über seinen Namen auswählen."))
        else Text(t("Opening the ocean…", "Das Meer wird geöffnet …"), Modifier.testTag("ocean-loading"))

        NativeActionButton(dolphinName, NativeActionRole.SECONDARY, onDolphin,
            Modifier.fillMaxWidth().testTag("ocean-dolphin-choice"))
        if (selectedDolphin) {
            Surface(modifier = Modifier.fillMaxWidth().testTag("ocean-dolphin-fact"),
                shape = BastiShapes.card, color = BastiPalette.secondaryContainer,
                border = BorderStroke(2.dp, BastiPalette.primary)) {
                Column(Modifier.padding(BastiSpacing.medium), verticalArrangement = Arrangement.spacedBy(BastiSpacing.small)) {
                    Text(dolphinName, Modifier.fillMaxWidth().semantics { heading() }, style = BastiTypography.answer,
                        color = BastiPalette.ink)
                    Text(HabitatExploreContent.dolphinFact.display[language], Modifier.fillMaxWidth(),
                        style = BastiTypography.instruction, color = BastiPalette.ink)
                    NativeActionButton(t("Listen again", "Noch einmal hören"), NativeActionRole.SECONDARY,
                        onReplay, Modifier.fillMaxWidth().testTag("ocean-replay"))
                }
            }
        }
        if (audioFailed) Text(t("Speech is unavailable. Check installed offline voices in Options.",
            "Die Sprachausgabe ist nicht verfügbar. Prüfe die Offline-Stimmen in den Optionen."),
            Modifier.fillMaxWidth().testTag("ocean-audio-error"), color = BastiPalette.ink)
        NativeActionButton(t("Back to habitats", "Zurück zu den Lebensräumen"), NativeActionRole.SECONDARY,
            onHabitats, Modifier.fillMaxWidth().testTag("ocean-habitats-back"))
        NativeActionButton(t("Home", "Startseite"), NativeActionRole.NAVIGATION,
            onHome, Modifier.fillMaxWidth().testTag("ocean-home"))
    }
}

/** Full 4:3 scene fit; the focus region is normalized to the visible dolphin in the approved composite. */
@Composable
private fun OceanScene(image: ImageBitmap, selected: Boolean, language: ContentLanguage,
    dolphinName: String, onDolphin: () -> Unit) {
    BoxWithConstraints(Modifier.fillMaxWidth().heightIn(min = 130.dp, max = 430.dp)
        .aspectRatio(4f / 3f).clip(RoundedCornerShape(20.dp)).background(BastiPalette.surface)
        .border(2.dp, BastiPalette.outline, RoundedCornerShape(20.dp)).testTag("ocean-scene")) {
        Image(image, contentDescription = null, modifier = Modifier.fillMaxSize().testTag("ocean-scene-art"),
            contentScale = ContentScale.Fit)
        val selectedDescription = when (language) {
            ContentLanguage.ENGLISH -> if (selected) "$dolphinName, selected. Tap to hear the name and a fact."
                else "$dolphinName, not selected. Tap to hear the name and a fact."
            ContentLanguage.GERMAN -> if (selected) "$dolphinName, ausgewählt. Tippe, um den Namen und eine Information zu hören."
                else "$dolphinName, nicht ausgewählt. Tippe, um den Namen und eine Information zu hören."
        }
        Box(Modifier.align(Alignment.TopStart)
            .offset(x = maxWidth * 0.085f, y = maxHeight * 0.008f)
            .fillMaxWidth(0.34f).fillMaxHeight(0.33f)
            .then(if (selected) Modifier.border(4.dp, BastiPalette.ink, RoundedCornerShape(28.dp)) else Modifier)
            .clickable(onClick = onDolphin)
            .testTag("ocean-dolphin-hotspot")
            .semantics(mergeDescendants = true) {
                role = Role.Button
                contentDescription = selectedDescription
                this.selected = selected
                stateDescription = when (language) {
                    ContentLanguage.ENGLISH -> if (selected) "Selected" else "Not selected"
                    ContentLanguage.GERMAN -> if (selected) "Ausgewählt" else "Nicht ausgewählt"
                }
            })
    }
}
