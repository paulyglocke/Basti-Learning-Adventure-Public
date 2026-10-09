package com.bellfamily.bastischool.ui.clock

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bellfamily.bastischool.learning.clock.*
import com.bellfamily.bastischool.learning.models.ContentLanguage
import com.bellfamily.bastischool.ui.common.*
import kotlin.math.*

@Composable
fun ClockScreen(state: ClockExploreState?, language: ContentLanguage, saveFailed: Boolean, audioFailed: Boolean,
    onMove: (ClockTime) -> Unit, onSettle: () -> Unit, onInterrupt: () -> Unit, onAdjust: (Int) -> Unit,
    onListen: () -> Unit, onRetry: () -> Unit, onHome: () -> Unit, modifier: Modifier = Modifier, onPractice: (() -> Unit)? = null) {
    fun t(en: String, de: String) = if (language == ContentLanguage.GERMAN) de else en
    Column(modifier.fillMaxSize().background(Color(0xFFEAF7FC)).verticalScroll(rememberScrollState()).padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        NativeActivityTitle(t("Clock & Time", "Uhr & Zeit"))
        if (onPractice != null) NativeActionButton(t("Make This Time", "Stelle diese Uhrzeit ein"), NativeActionRole.SECONDARY,
            onPractice, Modifier.fillMaxWidth().testTag("clock-open-make"))
        NativeSupportMessage(t("Move the long hand.", "Bewege den langen Zeiger."))
        if (state == null) Text(t("Opening…", "Wird geöffnet…")) else {
            ClockFace(state.time, language, !saveFailed, onMove, onSettle, onInterrupt, onAdjust)
            Text(state.time.digital, fontSize = 40.sp, modifier = Modifier.testTag("clock-digital").semantics {
                contentDescription = t("Digital time: ${state.time.digital}", "Digitale Zeit: ${state.time.digital}")
            })
            Text(ClockWording.phrase(state.time, language), fontSize = 24.sp, modifier = Modifier.testTag("clock-phrase"))
            Text(t("Short hand: hours. Long hand: minutes.", "Kurzer Zeiger: Stunden. Langer Zeiger: Minuten."))
            Text(t("Long hand at 12: whole hour. At 6: half past.", "Langer Zeiger auf 12: volle Stunde. Auf 6: halbe Stunde."))
            NativeActionButton(t("5 minutes back", "5 Minuten zurück"), NativeActionRole.SECONDARY,
                { onAdjust(-ClockTime.EXPLORE_STEP) }, Modifier.fillMaxWidth().testTag("clock-back"), !saveFailed)
            NativeActionButton(t("5 minutes forward", "5 Minuten weiter"), NativeActionRole.SECONDARY,
                { onAdjust(ClockTime.EXPLORE_STEP) }, Modifier.fillMaxWidth().testTag("clock-forward"), !saveFailed)
            NativeActionButton(t("Listen", "Anhören"), NativeActionRole.SECONDARY, onListen,
                Modifier.fillMaxWidth().testTag("clock-listen"))
        }
        if (saveFailed) {
            NativeSupportMessage(t("Saving is unavailable. Please try again.", "Speichern ist nicht möglich. Versuche es erneut."))
            NativeActionButton(t("Try again", "Erneut versuchen"), NativeActionRole.SECONDARY, onRetry)
        }
        if (audioFailed) Text(t("Speech is unavailable. Check installed offline voices.", "Die Sprachausgabe ist nicht verfügbar. Prüfe installierte Offline-Stimmen."))
        NativeActionButton(t("Back home", "Zurück zum Start"), NativeActionRole.NAVIGATION, onHome, Modifier.fillMaxWidth().testTag("clock-home"))
    }
}

@Composable
fun ClockFace(time: ClockTime, language: ContentLanguage, enabled: Boolean,
    onMove: (ClockTime) -> Unit, onSettle: () -> Unit, onInterrupt: () -> Unit, onAdjust: (Int) -> Unit,
    step: Int = ClockTime.EXPLORE_STEP, practice: Boolean = false) {
    val latestTime by rememberUpdatedState(time)
    val move by rememberUpdatedState(onMove)
    val settle by rememberUpdatedState(onSettle)
    val interrupt by rememberUpdatedState(onInterrupt)
    val german = language == ContentLanguage.GERMAN
    val label = if (practice) {
        val hour = time.hour
        val minuteMark = if (time.minute == 0) 12 else 6
        if (german) "Aktuelle Uhr: Langer Zeiger auf $minuteMark. Kurzer Zeiger ${if (time.minute == 0) "auf $hour" else "zwischen $hour und ${hour % 12 + 1}"}."
        else "Current clock: long hand on $minuteMark. Short hand ${if (time.minute == 0) "on $hour" else "between $hour and ${hour % 12 + 1}"}."
    } else if (german) "Uhr zeigt ${ClockWording.phrase(time, language)}. Langer Zeiger: Minuten. Kurzer Zeiger: Stunden."
        else "Clock showing ${ClockWording.phrase(time, language)}. Long hand: minutes. Short hand: hours."
    Canvas(Modifier.widthIn(max = 380.dp).fillMaxWidth().aspectRatio(1f).testTag("clock-face")
        .semantics {
            contentDescription = label
            if (!practice) stateDescription = time.digital
            if (enabled) customActions = listOf(
                CustomAccessibilityAction(if (german) "$step Minuten zurück" else "$step minutes back") { onAdjust(-step); true },
                CustomAccessibilityAction(if (german) "$step Minuten weiter" else "$step minutes forward") { onAdjust(step); true })
        }
        .onKeyEvent {
            if (!enabled || it.type != KeyEventType.KeyDown) false
            else when (it.key) {
                Key.DirectionLeft -> { onAdjust(-step); true }
                Key.DirectionRight -> { onAdjust(step); true }
                else -> false
            }
        }.focusable(enabled)
        .pointerInput(enabled, language, step) {
            if (!enabled) return@pointerInput
            var drag: ClockDrag? = null
            fun angle(p: Offset): Float = ((atan2(p.x - size.width / 2f, -(p.y - size.height / 2f)) * 180f / PI.toFloat()) + 360f) % 360f
            fun away(p: Offset) = (p - Offset(size.width / 2f, size.height / 2f)).getDistance() > size.width * .12f
            detectDragGestures(
                onDragStart = { p -> interrupt(); drag = if (away(p)) ClockDrag(latestTime, angle(p)) else null },
                onDragEnd = { if (drag != null) settle(); drag = null },
                onDragCancel = { if (drag != null) settle(); drag = null },
                onDrag = { change, _ ->
                    change.consume()
                    if (away(change.position)) {
                        if (drag == null) drag = ClockDrag(latestTime, angle(change.position))
                        else {
                            val next = drag!!.move(angle(change.position))
                            move(if (practice) next.snap(step) else next)
                        }
                    } else drag = null
                })
        }) {
        val radius = size.minDimension * .47f
        val ink = Color(0xFF172B3A)
        drawCircle(Color.White, radius)
        drawCircle(ink, radius, style = Stroke(3.dp.toPx()))
        fun point(angle: Float, length: Float): Offset {
            val radians = Math.toRadians(angle.toDouble())
            return center + Offset(sin(radians).toFloat(), -cos(radians).toFloat()) * length
        }
        for (n in 0 until 60) drawLine(ink, point(n * 6f, radius * .93f), point(n * 6f, radius * if (n % 5 == 0) .86f else .90f), if (n % 5 == 0) 3.dp.toPx() else 1.dp.toPx())
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = android.graphics.Color.rgb(23, 43, 58); textAlign = Paint.Align.CENTER; textSize = radius * .16f }
        for (n in 1..12) {
            val p = point(n * 30f, radius * .72f)
            paint.isFakeBoldText = n == 12 || n == 6
            drawContext.canvas.nativeCanvas.drawText(n.toString(), p.x, p.y - (paint.ascent() + paint.descent()) / 2, paint)
        }
        drawLine(ink, center, point(time.hourAngle, radius * .48f), 10.dp.toPx(), StrokeCap.Round)
        drawLine(Color(0xFF176B88), center, point(time.minuteAngle, radius * .83f), 5.dp.toPx(), StrokeCap.Round)
        drawCircle(ink, 8.dp.toPx())
    }
}
