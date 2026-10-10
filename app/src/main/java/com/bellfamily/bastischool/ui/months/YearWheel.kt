package com.bellfamily.bastischool.ui.months

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bellfamily.bastischool.learning.content.SeasonIds
import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.months.*
import kotlin.math.*

internal fun seasonColour(id: ContentId): Color = when (id) {
    SeasonIds.WINTER -> Color(0xFFC9E6FA)
    SeasonIds.SPRING -> Color(0xFFD7EDBF)
    SeasonIds.SUMMER -> Color(0xFFFFE79B)
    SeasonIds.AUTUMN -> Color(0xFFF5C4A6)
    else -> error("Unknown season")
}

/** Decorative ring semantics are merged into one description; full-size buttons own focus. */
@Composable
fun YearWheel(selected: ContentId, language: ContentLanguage, enabled: Boolean, onSelect: (ContentId) -> Unit,
    modifier: Modifier = Modifier) {
    val select by rememberUpdatedState(onSelect)
    val label = MonthContent.month(selected).text.display[language]
    val description = if (language == ContentLanguage.GERMAN) "Jahresrad. Ausgewählt: $label. Wähle unten einen Monat."
        else "Year wheel. Selected: $label. Choose a month below."
    Canvas(modifier.widthIn(max = 480.dp).fillMaxWidth().aspectRatio(1f).testTag("months-wheel")
        .semantics { contentDescription = description }
        .pointerInput(enabled) {
            if (enabled) detectTapGestures { point ->
                YearWheelGeometry.hit(point.x - size.width / 2f, point.y - size.height / 2f,
                    min(size.width, size.height) / 2f - 6.dp.toPx())?.let(select)
            }
        }) {
        val radius = size.minDimension / 2f - 6.dp.toPx()
        val inner = radius * .48f
        val outerRect = Rect(center - Offset(radius, radius), center + Offset(radius, radius))
        val innerRect = Rect(center - Offset(inner, inner), center + Offset(inner, inner))
        val ink = Color(0xFF23354A)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = ink.toArgb(); textAlign = Paint.Align.CENTER; textSize = 11.sp.toPx()
        }
        MonthContent.definitions.forEach { month ->
            val angle = YearWheelGeometry.angle(month.id)
            val path = Path().apply {
                arcTo(outerRect, angle - 15, 30f, true)
                arcTo(innerRect, angle + 15, -30f, false)
                close()
            }
            drawPath(path, seasonColour(month.season))
            drawPath(path, ink.copy(alpha = .5f), style = Stroke(1.dp.toPx()))
            if (month.id == selected) drawPath(path, ink, style = Stroke(4.dp.toPx()))
            val radians = Math.toRadians(angle.toDouble())
            val position = center + Offset(cos(radians).toFloat(), sin(radians).toFloat()) * (radius * .75f)
            paint.isFakeBoldText = month.id == selected
            drawContext.canvas.nativeCanvas.drawText(month.shortLabel[language], position.x,
                position.y - (paint.ascent() + paint.descent()) / 2, paint)
        }
        paint.textSize = 18.sp.toPx(); paint.isFakeBoldText = true
        drawContext.canvas.nativeCanvas.drawText(if (language == ContentLanguage.GERMAN) "Jahr" else "Year",
            center.x, center.y - (paint.ascent() + paint.descent()) / 2, paint)
    }
}
