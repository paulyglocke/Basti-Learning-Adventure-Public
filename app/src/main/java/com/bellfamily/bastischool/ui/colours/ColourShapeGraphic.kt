package com.bellfamily.bastischool.ui.colours

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.bellfamily.bastischool.learning.colours.ColourShape

@Composable
fun ColourShapeGraphic(shape: ColourShape, colour: Color, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val inset = 3.dp.toPx(); val stroke = Stroke(2.dp.toPx())
        when (shape) {
            ColourShape.CIRCLE -> { drawCircle(colour, radius = size.minDimension / 2 - inset); drawCircle(Color(0xFF263238), radius = size.minDimension / 2 - inset, style = stroke) }
            ColourShape.SQUARE -> { drawRect(colour, topLeft = Offset(inset, inset), size = Size(size.width - inset * 2, size.height - inset * 2)); drawRect(Color(0xFF263238), topLeft = Offset(inset, inset), size = Size(size.width - inset * 2, size.height - inset * 2), style = stroke) }
            ColourShape.RECTANGLE -> { val w = size.width * .88f; val h = size.height * .62f; val p = Offset((size.width-w)/2, (size.height-h)/2); drawRect(colour, p, Size(w,h)); drawRect(Color(0xFF263238), p, Size(w,h), style=stroke) }
            ColourShape.TRIANGLE -> {
                val path = Path().apply { moveTo(size.width/2, inset); lineTo(size.width-inset, size.height-inset); lineTo(inset, size.height-inset); close() }
                drawPath(path, colour); drawPath(path, Color(0xFF263238), style=stroke)
            }
        }
    }
}
