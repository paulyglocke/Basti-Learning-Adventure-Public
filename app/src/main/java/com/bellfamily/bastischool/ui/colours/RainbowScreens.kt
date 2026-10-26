package com.bellfamily.bastischool.ui.colours

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.bellfamily.bastischool.learning.colours.RainbowContent
import com.bellfamily.bastischool.learning.models.ContentLanguage
import com.bellfamily.bastischool.ui.common.*
import com.bellfamily.bastischool.ui.design.*

@Composable
fun RainbowExploreScreen(language: ContentLanguage, onSpeak: (String) -> Unit, onHome: () -> Unit,
                        modifier: Modifier = Modifier) {
    var selected by remember { mutableIntStateOf(0) }
    val german = language == ContentLanguage.GERMAN
    val names = RainbowContent.order.map { RainbowContent.definition(it).text.display[language].replaceFirstChar { ch -> ch.uppercase() } }
    val colors = RainbowContent.swatches.map { Color(it) }
    val screenHeight = LocalConfiguration.current.screenHeightDp
    val fontScale = LocalDensity.current.fontScale
    val graphicHeight = when {
        screenHeight < 350 -> 128.dp
        fontScale >= 1.8f -> 180.dp
        screenHeight < 470 -> 168.dp
        screenHeight < 700 -> 220.dp
        else -> 260.dp
    }
    Column(modifier.fillMaxSize().background(BastiPalette.background).verticalScroll(rememberScrollState())
        .padding(BastiSpacing.medium), verticalArrangement = Arrangement.spacedBy(BastiSpacing.medium),
        horizontalAlignment = Alignment.CenterHorizontally) {
        NativeActivityTitle(if (german) "Der Regenbogen" else "The Rainbow")
        Text(names[selected], style = BastiTypography.question, color = BastiPalette.ink,
            modifier = Modifier.testTag("rainbow-selected-name"))
        RainbowGraphic(colors, selected, names, Modifier.fillMaxWidth().height(graphicHeight)
            .testTag("rainbow-graphic")) { index -> selected = index; onSpeak(index.toString()) }
        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            names.chunked(2).forEachIndexed { row, rowNames ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    rowNames.forEachIndexed { column, name ->
                        val index = row * 2 + column
                        Surface(onClick = { selected = index; onSpeak(index.toString()) }, modifier = Modifier.weight(1f)
                            .heightIn(min = BastiSpacing.minimumTarget).testTag("rainbow-band-$index")
                            .semantics { contentDescription = name }, shape = BastiShapes.control,
                            color = if (selected == index) BastiPalette.secondaryContainer else BastiPalette.surface,
                            border = androidx.compose.foundation.BorderStroke(if (selected == index) 3.dp else 1.dp,
                                if (selected == index) BastiPalette.ink else BastiPalette.outline)) {
                            Box(Modifier.fillMaxSize().padding(4.dp), contentAlignment = Alignment.Center) {
                                Text(name, style = BastiTypography.parentInformation, color = BastiPalette.ink)
                            }
                        }
                    }
                    if (rowNames.size == 1) Spacer(Modifier.weight(1f))
                }
            }
        }
        NativeActionButton(if (german) "Noch einmal hören" else "Listen again", NativeActionRole.SECONDARY,
            { onSpeak(selected.toString()) }, Modifier.fillMaxWidth().testTag("rainbow-replay"))
        Text(RainbowContent.conventionalNote.display[language], style = BastiTypography.parentInformation,
            color = BastiPalette.ink)
        NativeActionButton(if (german) "Startseite" else "Home", NativeActionRole.NAVIGATION, onHome,
            Modifier.fillMaxWidth().testTag("rainbow-home"))
    }
}

@Composable
private fun RainbowGraphic(colors: List<Color>, selected: Int, names: List<String>, modifier: Modifier,
                           onSelect: (Int) -> Unit) {
    Canvas(modifier.semantics { contentDescription = "Seven-band rainbow, ${names[selected]} selected" }
        .pointerInput(Unit) { detectTapGestures { position ->
            val width = this.size.width.toFloat(); val height = this.size.height.toFloat()
            val cx = width / 2f; val cy = height * .92f
            val outerRadius = minOf(width * .48f, height * .8f); val bandWidth = outerRadius / 7f
            val distance = kotlin.math.hypot(position.x - cx, position.y - cy)
            val band = kotlin.math.floor((outerRadius - distance) / bandWidth + .5f).toInt()
            if (position.y <= cy && band in 0..6) onSelect(band)
        } }) {
        val outer = minOf(size.width * .48f, size.height * .8f)
        val thickness = outer / 7f
        val center = Offset(size.width / 2f, size.height * .92f)
        colors.indices.reversed().forEach { i ->
            val radius = outer - i * thickness
            // Positive sweep crosses the upper half in Compose's screen coordinate system.
            drawArc(colors[i], 180f, 180f, false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
                style = Stroke(if (i == selected) thickness * .94f else thickness * .78f))
        }
        // Selection marker is a short white notch on the selected band, not a hue change.
        val radius = outer - selected * thickness
        val point = Offset(center.x, center.y - radius)
        drawCircle(Color.White, thickness * .17f, point)
        drawCircle(Color.Black, thickness * .08f, point)
    }
}

/** A starter four-colour sequence uses the complete seven-band model as a reference. */
@Composable
fun RainbowOrderScreen(language: ContentLanguage, onReplay: () -> Unit, onHelp: () -> Unit,
                       onHome: () -> Unit, modifier: Modifier = Modifier) {
    val german = language == ContentLanguage.GERMAN
    var challenge by remember { mutableStateOf(false) }
    val count = if (challenge) 7 else 4
    val target = if (challenge) RainbowContent.order.indices.toList() else listOf(0, 2, 4, 6)
    var placed by rememberSaveable(challenge) { mutableStateOf(emptyList<Int>()) }
    var showModel by rememberSaveable(challenge) { mutableStateOf(!challenge) }
    Column(modifier.fillMaxSize().background(BastiPalette.background).verticalScroll(rememberScrollState())
        .padding(BastiSpacing.medium), verticalArrangement = Arrangement.spacedBy(BastiSpacing.medium)) {
        NativeActivityTitle(if (german) "Regenbogen ordnen" else "Rainbow Order")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = !challenge, onClick = { challenge = false; placed = emptyList() },
                label = { Text(if (german) "Mit Beispiel" else "With example") })
            FilterChip(selected = challenge, onClick = { challenge = true; placed = emptyList() },
                label = { Text(if (german) "Alle sieben" else "All seven") })
        }
        Text(if (challenge) {
            if (german) "Tippe die Farben in der Regenbogen-Reihenfolge an." else "Tap the colours in rainbow order."
        } else {
            if (german) "Schau auf das Beispiel. Tippe die Farben von außen nach innen an." else "Use the example. Tap the colours from the outside in."
        }, style = BastiTypography.answer, color = BastiPalette.ink)
        if (!challenge || showModel) Row(modifier = Modifier.fillMaxWidth().testTag("rainbow-order-model"),
            horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            RainbowOrderSwatches(RainbowContent.order.indices.toList(), Modifier.weight(1f), "rainbow-order-reference")
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            repeat(count) { i ->
                val index = placed.getOrNull(i)
                Box(Modifier.weight(1f).height(54.dp).background(index?.let { Color(RainbowContent.swatches[it]) }
                    ?: BastiPalette.surface, BastiShapes.control).testTag("rainbow-order-slot-$i"), contentAlignment = Alignment.Center) {
                    if (index == null) Text("${i + 1}", color = BastiPalette.ink) else Text("✓", color = Color.White)
                }
            }
        }
        RainbowOrderSwatches(target.reversed().filterNot { it in placed }, Modifier.fillMaxWidth(), "rainbow-order-choice") { choice ->
            if (placed.size < count && choice !in placed) placed = placed + choice
        }
        if (placed.size == count) {
            val correct = placed == target
            Text(if (correct) { if (german) "Das ist die Regenbogen-Reihenfolge!" else "That is the rainbow order!" }
            else { if (german) "Schau noch einmal auf das Beispiel." else "Have another look at the example." },
                color = BastiPalette.ink, modifier = Modifier.testTag("rainbow-order-feedback"))
            NativeActionButton(if (german) "Nochmal versuchen" else "Try again", NativeActionRole.SECONDARY,
                { placed = emptyList() }, Modifier.fillMaxWidth().testTag("rainbow-order-retry"))
        }
        NativeActionButton(if (german) "Noch einmal hören" else "Listen again", NativeActionRole.SECONDARY, onReplay,
            Modifier.fillMaxWidth().testTag("rainbow-order-replay"))
        NativeActionButton(if (german) "Hilfe" else "Help", NativeActionRole.SECONDARY,
            { showModel = true; onHelp() },
            Modifier.fillMaxWidth().testTag("rainbow-order-help"))
        NativeActionButton(if (german) "Startseite" else "Home", NativeActionRole.NAVIGATION, onHome,
            Modifier.fillMaxWidth().testTag("rainbow-order-home"))
    }
}

@Composable
private fun RainbowOrderSwatches(indices: List<Int>, modifier: Modifier, tag: String, onPick: ((Int) -> Unit)? = null) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
        indices.forEach { colorIndex ->
            Box(Modifier.weight(1f).heightIn(min = BastiSpacing.minimumTarget).background(Color(RainbowContent.swatches[colorIndex]), CircleShape)
                .then(if (onPick != null) Modifier.clickable { onPick(colorIndex) } else Modifier)
                .testTag("$tag-$colorIndex"), contentAlignment = Alignment.Center) {
            }
        }
    }
}
