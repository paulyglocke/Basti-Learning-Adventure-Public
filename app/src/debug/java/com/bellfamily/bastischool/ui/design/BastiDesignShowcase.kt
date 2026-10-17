package com.bellfamily.bastischool.ui.design

import android.graphics.BitmapFactory
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bellfamily.bastischool.ui.common.*

/** Authored preview copy only, not a curriculum catalogue or new navigation. */
internal fun demoCategoryLabel(treatment: BastiCategoryTreatment, german: Boolean): String = when (treatment) {
    BastiCategoryTreatment.MATHS -> if (german) "Zahlen & Mathematik" else "Numbers & Maths"
    BastiCategoryTreatment.WORDS -> if (german) "Wörter & Erzählen" else "Words & Talking"
    BastiCategoryTreatment.TIME -> if (german) "Zeit & Jahreszeiten" else "Time & Seasons"
    BastiCategoryTreatment.ANIMALS -> if (german) "Tiere & Natur" else "Animals & Nature"
    BastiCategoryTreatment.COLOURS -> if (german) "Farben & Formen" else "Colours & Shapes"
    BastiCategoryTreatment.EVERYDAY -> if (german) "Alltag & Selbstständigkeit" else "Everyday Skills"
}

/** Only this debug showcase opts into the proposed theme. Actions change demo state, not learning. */
@Composable
internal fun BastiDesignShowcase(german: Boolean, modifier: Modifier = Modifier, page: String = "all") {
    fun t(en: String, de: String) = if (german) de else en
    var selected by rememberSaveable { mutableStateOf(BastiCategoryTreatment.MATHS.name) }
    var activations by rememberSaveable { mutableIntStateOf(0) }
    val context = LocalContext.current
    // Small sampled copies of existing canonical artwork; originals remain untouched.
    val illustrations = remember(context) {
        listOf("trex", "dragon", "turtle", "whale", "cat", "crocodile").map { animal ->
            val path = "Animals/canonical/$animal.png"
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.assets.open(path).use { BitmapFactory.decodeStream(it, null, bounds) }
            val target = 96 * context.resources.displayMetrics.density
            var sample = 1
            while (maxOf(bounds.outWidth, bounds.outHeight) / (sample * 2) >= target) sample *= 2
            context.assets.open(path).use { stream ->
                BitmapPainter(requireNotNull(BitmapFactory.decodeStream(stream, null,
                    BitmapFactory.Options().apply { inSampleSize = sample })).asImageBitmap())
            }
        }
    }
    Column(modifier.verticalScroll(rememberScrollState()).padding(BastiSpacing.page),
        verticalArrangement = Arrangement.spacedBy(BastiSpacing.large)) {
        Text(t("Design preview — not Home", "Designvorschau — nicht die Startseite"),
            style = BastiTypography.parentInformation, modifier = Modifier.fillMaxWidth().testTag("demo-notice"))
        if (page == "all" || page == "categories") {
            BastiSectionHeading(t("Colour & illustration samples", "Farb- und Bildbeispiele"))
            Text(t("Sample mascots, not final category artwork. Tap to select a sample.",
                "Beispielmaskottchen, keine endgültigen Bereichsbilder. Tippe auf ein Beispiel."),
                style = BastiTypography.parentInformation, modifier = Modifier.fillMaxWidth())
            BoxWithConstraints(Modifier.fillMaxWidth()) {
                val columns = if (maxWidth >= 480.dp * LocalDensity.current.fontScale) 2 else 1
                Column(verticalArrangement = Arrangement.spacedBy(BastiSpacing.medium)) {
                    BastiCategoryTreatment.entries.chunked(columns).forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(BastiSpacing.medium)) {
                            row.forEach { treatment ->
                                BastiCategoryCard(demoCategoryLabel(treatment, german), illustrations[treatment.ordinal],
                                    treatment, { selected = treatment.name },
                                    Modifier.weight(1f).testTag("category-${treatment.name}"), selected == treatment.name)
                            }
                        }
                    }
                }
            }
        }
        if (page == "all" || page == "type") {
            BastiSectionHeading(t("Typography samples", "Schriftbeispiele"))
            val samples = listOf(
                t("Ready for an adventure?", "Bereit für ein Abenteuer?") to BastiTypography.homeHeading,
                t("Explore together", "Gemeinsam entdecken") to BastiTypography.screenHeading,
                t("What would you like to discover?", "Was möchtest du entdecken?") to BastiTypography.question,
                t("A familiar animal", "Ein vertrautes Tier") to BastiTypography.answer,
                t("Take your time. You can listen again.", "Lass dir Zeit. Du kannst noch einmal zuhören.") to BastiTypography.instruction,
            )
            samples.forEachIndexed { index, (label, style) ->
                Text(label, style = style, modifier = Modifier.fillMaxWidth().testTag("type-$index"))
            }
            NativeSupportMessage(t("You can ask for help.", "Du kannst um Hilfe bitten."), Modifier.testTag("type-support"))
            Text(t("For grown-ups: these are presentation examples, not learning tasks.",
                "Für Erwachsene: Dies sind Gestaltungsbeispiele, keine Lernaufgaben."),
                style = BastiTypography.parentInformation, modifier = Modifier.fillMaxWidth().testTag("type-parent"))
        }
        if (page == "all" || page == "actions") {
            BastiSectionHeading(t("Action samples", "Beispielaktionen"))
            Text(t("Demo only: no navigation or audio.", "Nur eine Vorschau: keine Navigation oder Sprachausgabe."),
                style = BastiTypography.parentInformation, modifier = Modifier.fillMaxWidth())
            NativeActionButton(t("Continue exploring together", "Gemeinsam weiterentdecken"), NativeActionRole.PRIMARY,
                { activations++ }, Modifier.testTag("demo-primary"))
            NativeActionButton(t("Listen again", "Noch einmal hören"), NativeActionRole.SECONDARY,
                { activations++ }, Modifier.testTag("demo-secondary"))
            NativeActionButton(t("Back to the overview", "Zurück zur Übersicht"), NativeActionRole.NAVIGATION,
                { activations++ }, Modifier.testTag("demo-navigation"))
            NativeActionButton(t("Unavailable example", "Nicht verfügbares Beispiel"), NativeActionRole.PRIMARY,
                { activations++ }, Modifier.testTag("demo-disabled"), enabled = false)
            Text(t("Demo activations: $activations", "Beispielbetätigungen: $activations"),
                style = BastiTypography.parentInformation, modifier = Modifier.fillMaxWidth().testTag("demo-count"))
        }
    }
}

@Preview(name = "English", widthDp = 360, heightDp = 800)
@Preview(name = "English large", widthDp = 360, heightDp = 800, fontScale = 1.5f)
@Composable
private fun EnglishPreview() = BastiDesignTheme {
    Surface(color = BastiPalette.background) { BastiDesignShowcase(false) }
}

@Preview(name = "Deutsch groß", widthDp = 360, heightDp = 800, fontScale = 2f)
@Preview(name = "Deutsch Querformat", widthDp = 640, heightDp = 320, fontScale = 2f)
@Composable
private fun GermanPreview() = BastiDesignTheme {
    Surface(color = BastiPalette.background) { BastiDesignShowcase(true) }
}
