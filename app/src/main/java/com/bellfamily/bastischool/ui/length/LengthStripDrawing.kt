package com.bellfamily.bastischool.ui.length

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

/** One shared drawing scale for direct comparison and ordering, independent of row/strip length. */
internal fun DrawScope.drawLengthStrip(length: Int?, offset: Int = 0, colour: Int = 0) {
    val colours=listOf(Color(0xFF367C87),Color(0xFFB66531),Color(0xFF7260A8),Color(0xFF4B8051))
    val margin=6.dp.toPx();val unit=(size.width-2*margin)/9f
    drawLine(Color(0xFF244951),Offset(margin,4.dp.toPx()),Offset(margin,size.height-4.dp.toPx()),3.dp.toPx())
    if(length==null)return
    val x=margin+offset*unit;val top=(size.height-26.dp.toPx())/2;val rect=Size(length*unit,26.dp.toPx())
    drawRoundRect(colours[colour%colours.size],Offset(x,top),rect,CornerRadius(3.dp.toPx()))
    drawRoundRect(Color(0xFF244951),Offset(x,top),rect,CornerRadius(3.dp.toPx()),style=Stroke(1.5.dp.toPx()))
}
