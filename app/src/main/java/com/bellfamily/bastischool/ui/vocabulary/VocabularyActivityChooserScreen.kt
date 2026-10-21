package com.bellfamily.bastischool.ui.vocabulary

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.bellfamily.bastischool.learning.models.ContentLanguage
import com.bellfamily.bastischool.learning.models.ContentId
import com.bellfamily.bastischool.learning.vocabulary.VocabularyContent
import com.bellfamily.bastischool.learning.vocabulary.VocabularyPhase
import com.bellfamily.bastischool.ui.common.NativeActionButton
import com.bellfamily.bastischool.ui.common.NativeActionRole
import com.bellfamily.bastischool.ui.common.NativeActivityTitle
import com.bellfamily.bastischool.ui.design.BastiPalette
import com.bellfamily.bastischool.ui.design.BastiShapes
import com.bellfamily.bastischool.ui.design.BastiSpacing
import com.bellfamily.bastischool.ui.design.BastiTypography

@Composable
fun VocabularyActivityChooserScreen(
    language: ContentLanguage,
    images: Map<ContentId, ImageBitmap>,
    ready: Boolean,
    busy: Boolean,
    saveFailed: Boolean,
    onChoose: (VocabularyPhase) -> Unit,
    onRetry: () -> Unit,
    onHome: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val german = language == ContentLanguage.GERMAN
    fun t(en: String, de: String) = if (german) de else en
    val dinosaur = VocabularyContent.item(ContentId("animal.dinosaur"))

    Column(modifier.fillMaxSize().background(BastiPalette.background)
        .verticalScroll(rememberScrollState())
        .padding(horizontal = BastiSpacing.medium, vertical = BastiSpacing.large),
        verticalArrangement = Arrangement.spacedBy(BastiSpacing.medium)) {
        NativeActivityTitle(t("Vocabulary Booster", "Wortschatz"), Modifier.semantics { heading() })
        Text(t("Choose what to do", "Wähle, was du machen möchtest"),
            Modifier.fillMaxWidth(), style = BastiTypography.instruction, color = BastiPalette.ink)

        Surface(onClick = { onChoose(VocabularyPhase.EXPLORE) },
            enabled = ready && !busy && !saveFailed,
            modifier = Modifier.fillMaxWidth().heightIn(min = 180.dp)
                .testTag("vocabulary-open-EXPLORE").semantics { role = Role.Button },
            shape = BastiShapes.card, color = BastiPalette.secondaryContainer,
            contentColor = BastiPalette.ink, border = BorderStroke(1.dp, BastiPalette.outline)) {
            BoxWithConstraints(Modifier.fillMaxWidth().padding(BastiSpacing.medium)) {
                val sideBySide = maxWidth >= 620.dp * LocalDensity.current.fontScale
                if (sideBySide) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(BastiSpacing.medium)) {
                        images[dinosaur.id]?.let { image ->
                            Image(image, contentDescription = null,
                                Modifier.weight(1f).heightIn(min = 120.dp, max = 200.dp)
                                    .testTag("vocabulary-chooser-artwork"), contentScale = ContentScale.Fit)
                        }
                        ExploreCopy(language, Modifier.weight(1.2f))
                    }
                } else {
                    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(BastiSpacing.small)) {
                        images[dinosaur.id]?.let { image ->
                            Image(image, contentDescription = null,
                                Modifier.fillMaxWidth().heightIn(min = 120.dp, max = 160.dp)
                                    .testTag("vocabulary-chooser-artwork"), contentScale = ContentScale.Fit)
                        }
                        ExploreCopy(language, Modifier.fillMaxWidth())
                    }
                }
            }
        }

        Text(t("Practise", "Üben"), Modifier.fillMaxWidth().semantics { heading() },
            style = BastiTypography.answer, color = BastiPalette.ink)
        VocabularyChoice(VocabularyPhase.FIND, t("Find the word", "Finde das Wort"),
            t("Listen and choose the animal picture.", "Höre zu und wähle das Tierbild."),
            ready && !busy && !saveFailed, onChoose)
        VocabularyChoice(VocabularyPhase.NAME, t("What is it?", "Was ist das?"),
            t("Look at the animal and choose its name.", "Sieh dir das Tier an und wähle seinen Namen."),
            ready && !busy && !saveFailed, onChoose)

        if (busy && !ready) Text(t("Opening Vocabulary…", "Wortschatz wird geöffnet …"),
            Modifier.testTag("vocabulary-chooser-loading"), color = BastiPalette.ink)
        if (saveFailed) {
            Text(t("Your saved work is safe. Try opening Vocabulary again.",
                "Deine gespeicherte Arbeit ist sicher. Öffne den Wortschatz noch einmal."),
                Modifier.testTag("vocabulary-chooser-error"), color = BastiPalette.ink)
            NativeActionButton(t("Try again", "Erneut versuchen"), NativeActionRole.SECONDARY,
                onRetry, enabled = !busy, modifier = Modifier.fillMaxWidth().testTag("vocabulary-chooser-retry"))
        }
        NativeActionButton(t("Home", "Startseite"), NativeActionRole.NAVIGATION, onHome,
            Modifier.fillMaxWidth().testTag("vocabulary-chooser-home"))
    }
}

@Composable
private fun ExploreCopy(language: ContentLanguage, modifier: Modifier = Modifier) {
    val german = language == ContentLanguage.GERMAN
    Column(modifier, verticalArrangement = Arrangement.spacedBy(BastiSpacing.small)) {
        Text(if (german) "Wörter entdecken" else "Explore words",
            Modifier.fillMaxWidth().semantics { heading() },
            style = BastiTypography.answer, color = BastiPalette.ink)
        Text(if (german) "Sieh ein Tier, lerne seinen Namen und höre zu."
            else "See an animal, learn its name and listen.",
            Modifier.fillMaxWidth(), style = BastiTypography.parentInformation, color = BastiPalette.ink)
    }
}

@Composable
private fun VocabularyChoice(
    phase: VocabularyPhase,
    title: String,
    description: String,
    enabled: Boolean,
    onChoose: (VocabularyPhase) -> Unit,
) {
    Surface(onClick = { onChoose(phase) }, enabled = enabled,
        modifier = Modifier.fillMaxWidth().heightIn(min = BastiSpacing.minimumTarget)
            .testTag("vocabulary-open-${phase.name}").semantics { role = Role.Button },
        shape = BastiShapes.control, color = BastiPalette.surface,
        contentColor = BastiPalette.ink, border = BorderStroke(1.dp, BastiPalette.outline)) {
        Column(Modifier.padding(BastiSpacing.medium), verticalArrangement = Arrangement.spacedBy(BastiSpacing.small)) {
            Text(title, Modifier.fillMaxWidth(), style = BastiTypography.answer)
            Text(description, Modifier.fillMaxWidth(), style = BastiTypography.parentInformation)
        }
    }
}
