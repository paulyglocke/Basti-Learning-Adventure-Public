package com.bellfamily.bastischool.ui.months

import android.graphics.Paint
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import com.bellfamily.bastischool.R
import com.bellfamily.bastischool.learning.content.SeasonIds
import com.bellfamily.bastischool.learning.models.ContentId
import com.bellfamily.bastischool.learning.models.ContentLanguage
import com.bellfamily.bastischool.learning.months.*
import com.bellfamily.bastischool.ui.common.*
import kotlinx.coroutines.launch
import kotlin.math.*

@Composable
fun MonthsSeasonWheelScreen(state: MonthsSeasonWheelState?, language: ContentLanguage, saveFailed: Boolean,
    audioFailed: Boolean, onRotate: (Int) -> Unit, onMode: (MonthsWheelMode) -> Unit, onMonth: (ContentId) -> Unit,
    onCheck: () -> Unit, onReplay: () -> Unit, onHint: () -> Unit, onShowAlignment: () -> Unit,
    onAgain: () -> Unit, onRetry: () -> Unit,
    onBack: () -> Unit, modifier: Modifier = Modifier,
    onChangeWeather: () -> Unit = {}, onWeatherListen: () -> Unit = {}, onToggleWeather: () -> Unit = {}) {
    fun t(en: String, de: String) = if (language == ContentLanguage.GERMAN) de else en
    val largeText = LocalDensity.current.fontScale >= 1.5f
    val weatherMotion = if (state?.showsWeather == true) weatherMotionAllowed() else false
    Column(modifier.fillMaxSize().background(Color(0xFFF8F5FC)).verticalScroll(rememberScrollState())
        .padding(16.dp).testTag("months-season-wheel-content"),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        NativeActivityTitle(MonthsMode.SEASONS_WHEEL.title.display[language])
        if (state == null) {
            Text(t("Opening the wheel…", "Das Rad wird geöffnet…"))
        } else {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                NativeActionButton(t("Match", "Zuordnen"), NativeActionRole.SECONDARY,
                    { onMode(MonthsWheelMode.MATCH) }, Modifier.weight(1f).testTag("wheel-match-mode")
                        .semantics { selected = state.mode == MonthsWheelMode.MATCH }
                        .then(if (state.mode == MonthsWheelMode.MATCH) Modifier.border(2.dp, Color(0xFF23354A), RoundedCornerShape(16.dp)) else Modifier))
                NativeActionButton(t("Explore", "Entdecken"), NativeActionRole.SECONDARY,
                    { onMode(MonthsWheelMode.EXPLORE) }, Modifier.weight(1f).testTag("wheel-explore-mode")
                        .semantics { selected = state.mode == MonthsWheelMode.EXPLORE }
                        .then(if (state.mode == MonthsWheelMode.EXPLORE) Modifier.border(2.dp, Color(0xFF23354A), RoundedCornerShape(16.dp)) else Modifier))
            }
            NativeSupportMessage(if (state.mode == MonthsWheelMode.EXPLORE)
                t("Turn the wheel. Touch a month to hear its name.", "Drehe das Rad. Tippe auf einen Monat, um seinen Namen zu hören.")
                else t("Turn the months so each group of three matches its season.",
                    "Drehe die Monate, bis jede Dreiergruppe zu ihrer Jahreszeit passt."))
            MonthsSeasonWheelArt(state, language, !saveFailed, !largeText, weatherMotion, onRotate, onMonth)
            if (largeText) {
                listOf(listOf(SeasonIds.WINTER to t("Top left", "Oben links"), SeasonIds.SPRING to t("Top right", "Oben rechts")),
                    listOf(SeasonIds.AUTUMN to t("Bottom left", "Unten links"), SeasonIds.SUMMER to t("Bottom right", "Unten rechts")))
                    .forEach { row ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            row.forEach { (season, position) ->
                                Text("${MonthContent.season(season).text.display[language]} · $position",
                                    modifier = Modifier.weight(1f).background(Color.White, RoundedCornerShape(12.dp)).padding(8.dp),
                                    textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
            }
            val arrowName = MonthContent.month(MonthsSeasonWheelGeometry.arrowMonth(state.rotation)).text.display[language]
            Text(t("At the arrow: $arrowName", "Am Pfeil: $arrowName"),
                style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold,
                modifier = Modifier.testTag("wheel-selected-month"))
            if (state.mode == MonthsWheelMode.EXPLORE) {
                if (state.showsWeather) {
                    val season = MonthContent.season(MonthContent.month(state.selectedMonth).season)
                    Text("$arrowName — ${season.text.display[language]}",
                        style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().testTag("wheel-weather-month"))
                    NativeSupportMessage(state.weather.phrase.display[language], Modifier.testTag("wheel-weather-label"))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        NativeActionButton(t("Change Weather", "Wetter ändern"), NativeActionRole.SECONDARY,
                            onChangeWeather, Modifier.weight(1f).testTag("wheel-weather-change"), enabled = !saveFailed)
                        NativeActionButton(t("Listen to weather", "Wetter anhören"), NativeActionRole.SECONDARY,
                            onWeatherListen, Modifier.weight(1f).testTag("wheel-weather-listen"))
                    }
                    Text(MonthsWeatherContent.variability.display[language],
                        style = MaterialTheme.typography.bodyMedium, modifier = Modifier.fillMaxWidth())
                }
                NativeActionButton(if (state.weatherEnabled) t("Hide weather", "Wetter ausblenden")
                    else t("Show weather", "Wetter anzeigen"), NativeActionRole.SECONDARY,
                    onToggleWeather, Modifier.fillMaxWidth().testTag("wheel-weather-toggle"), enabled = !saveFailed)
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                NativeActionButton(t("↶ Previous month", "↶ Voriger Monat"), NativeActionRole.SECONDARY,
                    { onRotate(-1) }, Modifier.weight(1f).testTag("wheel-previous"), enabled = !saveFailed && state.feedback != MonthsWheelFeedback.COMPLETE)
                NativeActionButton(t("Next month ↷", "Nächster Monat ↷"), NativeActionRole.SECONDARY,
                    { onRotate(1) }, Modifier.weight(1f).testTag("wheel-next"), enabled = !saveFailed && state.feedback != MonthsWheelFeedback.COMPLETE)
            }
            NativeActionButton(t("Listen", "Anhören"), NativeActionRole.SECONDARY, onReplay,
                Modifier.fillMaxWidth().testTag("wheel-listen"))
            if (state.mode == MonthsWheelMode.EXPLORE)
                NativeActionButton(t("Show the seasons", "Jahreszeiten zeigen"), NativeActionRole.SECONDARY, onShowAlignment,
                    Modifier.fillMaxWidth().testTag("wheel-show-alignment"))
            if (state.mode == MonthsWheelMode.MATCH && state.feedback != MonthsWheelFeedback.COMPLETE) {
                NativeActionButton(t("Help", "Hilfe"), NativeActionRole.SECONDARY, onHint,
                    Modifier.fillMaxWidth().testTag("wheel-hint"))
                if (state.hintUsed) NativeSupportMessage(t("December, January and February stay together in winter.",
                    "Dezember, Januar und Februar bleiben im Winter zusammen."))
                NativeActionButton(t("Check", "Prüfen"), NativeActionRole.PRIMARY, onCheck,
                    Modifier.fillMaxWidth().testTag("wheel-check"), enabled = !saveFailed)
            }
            when (state.feedback) {
                MonthsWheelFeedback.RETRY -> if (state.mode == MonthsWheelMode.MATCH)
                    NativeSupportMessage(t("Take another look. Turn the wheel and try again.",
                        "Schau noch einmal hin. Drehe das Rad und versuche es erneut."))
                MonthsWheelFeedback.COMPLETE -> {
                    NativeSupportMessage(t("Yes! Every month is with its season. Well done!",
                        "Ja! Jeder Monat gehört zu seiner Jahreszeit. Sehr gut!"))
                    NativeActionButton(t("Play again", "Noch einmal"), NativeActionRole.PRIMARY, onAgain,
                        Modifier.fillMaxWidth().testTag("wheel-again"))
                }
                else -> Unit
            }
            if (saveFailed) {
                NativeSupportMessage(t("Saving is unavailable. Please try again.", "Speichern ist nicht möglich. Versuche es erneut."))
                NativeActionButton(t("Try again", "Erneut versuchen"), NativeActionRole.SECONDARY, onRetry,
                    Modifier.fillMaxWidth().testTag("wheel-retry-save"))
            }
            if (audioFailed) NativeSupportMessage(t("Speech is unavailable. Check installed offline voices.",
                "Die Sprachausgabe ist nicht verfügbar. Prüfe installierte Offline-Stimmen."))
            if (largeText) {
                Text(t("Month names", "Monatsnamen"), style = MaterialTheme.typography.titleMedium)
                MonthContent.definitions.chunked(2).forEach { row ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        row.forEach { month ->
                            NativeActionButton(month.text.display[language], NativeActionRole.SECONDARY,
                                { onMonth(month.id) }, Modifier.weight(1f).testTag("wheel-name-${month.id.value}"))
                        }
                    }
                }
            }
            Text(MonthContent.convention.display[language], style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.fillMaxWidth())
        }
        NativeActionButton(t("Back to Months", "Zurück zu den Monaten"), NativeActionRole.NAVIGATION, onBack,
            Modifier.fillMaxWidth().testTag("wheel-back"))
    }
}

@Composable
private fun MonthsSeasonWheelArt(state: MonthsSeasonWheelState, language: ContentLanguage, enabled: Boolean,
    showOverlayNames: Boolean, weatherMotion: Boolean,
    onRotate: (Int) -> Unit, onMonth: (ContentId) -> Unit) {
    val configuration = LocalConfiguration.current
    val diameter = minOf(configuration.screenWidthDp.dp - 32.dp,
        (configuration.screenHeightDp * .82f).dp, 460.dp).coerceAtLeast(240.dp)
    val scope = rememberCoroutineScope()
    val celebration by animateFloatAsState(if (state.feedback == MonthsWheelFeedback.COMPLETE) 1f else 0f,
        tween(durationMillis = 700), label = "wheel-season-colours")
    val angle = remember { Animatable(state.rotation * 30f) }
    var drag by remember { mutableFloatStateOf(0f) }
    var snapRequest by remember { mutableIntStateOf(0) }
    LaunchedEffect(state.rotation, snapRequest) {
        angle.animateTo(MonthsSeasonWheelGeometry.animationTarget(angle.value, state.rotation), tween(durationMillis = 260))
    }
    val monthAtArrow = MonthsSeasonWheelGeometry.arrowMonth(state.rotation)
    val selectedName = MonthContent.month(monthAtArrow).text.display[language]
    val desc = if (language == ContentLanguage.GERMAN)
        "Monatsrad. $selectedName am Pfeil. Mit den Tasten nach links oder rechts drehen."
        else "Months wheel. ${MonthContent.month(monthAtArrow).text.display[language]} at the arrow. Use previous or next to rotate."
    Box(Modifier.size(diameter).testTag("months-seasons-wheel").semantics { contentDescription = desc },
        contentAlignment = Alignment.Center) {
        val centerSize = diameter * .54f
        Image(painterResource(R.drawable.seasons_center), contentDescription = null,
            modifier = Modifier.size(centerSize).testTag("wheel-centre-art"))
        if (state.showsWeather) MonthsWeatherOverlay(state.selectedMonth, state.weather, weatherMotion,
            Modifier.size(centerSize))
        if (showOverlayNames) Box(Modifier.size(centerSize), contentAlignment = Alignment.Center) {
            val labels = listOf(Triple(SeasonIds.WINTER, Alignment.TopStart, Pair(.12f, .16f)),
                Triple(SeasonIds.SPRING, Alignment.TopEnd, Pair(-.12f, .16f)),
                Triple(SeasonIds.AUTUMN, Alignment.BottomStart, Pair(.12f, -.16f)),
                Triple(SeasonIds.SUMMER, Alignment.BottomEnd, Pair(-.12f, -.16f)))
            labels.forEach { (id, alignment, shift) ->
                Text(MonthContent.season(id).text.display[language], fontSize = 11.sp,
                    fontWeight = FontWeight.Bold, color = Color(0xFF172F42),
                    textAlign = TextAlign.Center, maxLines = 2,
                    modifier = Modifier.align(alignment).offset(x = centerSize * shift.first, y = centerSize * shift.second)
                        .width(centerSize * .32f).background(Color.White.copy(alpha = .82f), RoundedCornerShape(6.dp))
                        .padding(2.dp))
            }
        }
        Canvas(Modifier.fillMaxSize().testTag("wheel-ring")
            .pointerInput(enabled, state.feedback, state.rotation) {
                if (enabled && state.feedback != MonthsWheelFeedback.COMPLETE) detectDragGestures(
                    onDragEnd = {
                        val visual = angle.value + drag
                        val target = MonthsSeasonWheelGeometry.snappedSteps(visual)
                        scope.launch {
                            angle.snapTo(visual)
                            drag = 0f
                            onRotate(target - state.rotation)
                            snapRequest++ // Also settle a short drag that stays on the same month.
                        }
                    },
                    onDrag = { change, _ ->
                        val c = Offset(size.width / 2f, size.height / 2f)
                        val previous = change.previousPosition - c
                        val current = change.position - c
                        val radius = hypot(current.x, current.y)
                        if (radius > min(size.width, size.height) * .28f) {
                            var delta = Math.toDegrees((atan2(current.y, current.x) - atan2(previous.y, previous.x)).toDouble()).toFloat()
                            if (delta > 180f) delta -= 360f
                            if (delta < -180f) delta += 360f
                            drag += delta
                            change.consume()
                        }
                    })
            }
            .pointerInput(language, state.rotation) {
                detectTapGestures { tap ->
                    val x = tap.x - size.width / 2f; val y = tap.y - size.height / 2f
                    val radius = hypot(x, y)
                    if (radius in min(size.width, size.height) * .28f..min(size.width, size.height) * .49f) {
                        val degrees = Math.toDegrees(atan2(y.toDouble(), x.toDouble())).toFloat()
                        val index = MonthContent.definitions.indices.minByOrNull { i ->
                            val diff = Math.floorMod((degrees - MonthsSeasonWheelGeometry.centre(i, state.rotation) + 540f).roundToInt(), 360) - 180
                            abs(diff)
                        }
                        if (index != null) onMonth(MonthIds.canonicalOrder[index])
                    }
                }
            }) {
            val radius = size.minDimension / 2f - 2.dp.toPx()
            val inner = radius * .55f
            val outerRect = Rect(center - Offset(radius, radius), center + Offset(radius, radius))
            val innerRect = Rect(center - Offset(inner, inner), center + Offset(inner, inner))
            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color(0xFF1B344A).toArgb(); textAlign = Paint.Align.CENTER
                textSize = 12.sp.toPx()
            }
            val available = 2f * (radius * .79f) * sin(Math.PI / 12).toFloat() - 5.dp.toPx()
            MonthContent.definitions.forEachIndexed { index, month ->
                val start = MonthsSeasonWheelGeometry.start(index) + angle.value + drag
                val centreDegrees = start + 15f
                val path = Path().apply {
                    arcTo(outerRect, start, 30f, true)
                    arcTo(innerRect, start + 30f, -30f, false)
                    close()
                }
                val neutral = if (month.id == monthAtArrow) Color(0xFFFFE8CA) else Color(0xFFFDFBF7)
                val fill = androidx.compose.ui.graphics.lerp(neutral, seasonColour(month.season), celebration)
                drawPath(path, fill)
                drawPath(path, Color(0xFF45566B), style = Stroke(1.2.dp.toPx()))
                val name = month.text.display[language]
                val visibleName = if (paint.measureText(name) <= available) name else month.shortLabel[language]
                val radians = Math.toRadians(centreDegrees.toDouble())
                val point = center + Offset(cos(radians).toFloat(), sin(radians).toFloat()) * (radius * .79f)
                drawContext.canvas.nativeCanvas.drawText(visibleName, point.x,
                    point.y - (paint.ascent() + paint.descent()) / 2f, paint)
            }
            // One fixed inward arrow, radially aligned with the centre of a month segment.
            // Its angle comes from the same 30° geometry used for segment placement.
            val pointerRadians = Math.toRadians(MonthsSeasonWheelGeometry.POINTER_ANGLE_DEGREES.toDouble())
            val radial = Offset(cos(pointerRadians).toFloat(), sin(pointerRadians).toFloat())
            val tangent = Offset(-radial.y, radial.x)
            val tip = center + radial * (radius * .92f)
            val base = center + radial * (radius + .5.dp.toPx())
            val halfWidth = 8.dp.toPx()
            val arrow = Path().apply {
                moveTo(tip.x, tip.y)
                lineTo(base.x - tangent.x * halfWidth, base.y - tangent.y * halfWidth)
                lineTo(base.x + tangent.x * halfWidth, base.y + tangent.y * halfWidth)
                close()
            }
            drawPath(arrow, Color.White, style = Stroke(4.dp.toPx()))
            drawPath(arrow, Color(0xFFE95650))
        }
    }
}
