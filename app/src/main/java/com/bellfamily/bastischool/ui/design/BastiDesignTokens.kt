package com.bellfamily.bastischool.ui.design

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Typography
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Opt-in discovery chrome, never a replacement for semantic learning colours. */
object BastiPalette {
    val background = Color(0xFFFBF8F0)
    val surface = Color(0xFFFFFFFF)
    val ink = Color(0xFF203747)
    val primary = Color(0xFF2E6B50)
    val onPrimary = surface
    val secondaryContainer = Color(0xFFDDEDE2)
    val onSecondaryContainer = ink
    val outline = Color(0xFF65756E)
}

/** Colour treatments only: these are not routes, curriculum groups or progress IDs. */
enum class BastiCategoryTreatment(val container: Color) {
    MATHS(Color(0xFFE3F0FC)), WORDS(Color(0xFFEEE9F8)), TIME(Color(0xFFE7F2DC)),
    ANIMALS(Color(0xFFDFF1E9)), COLOURS(Color(0xFFFFF0D0)), EVERYDAY(Color(0xFFF9E6DC));
    val foreground: Color get() = BastiPalette.ink
}

object BastiSpacing {
    val small = 8.dp
    val medium = 16.dp
    val large = 24.dp
    val page = 24.dp
    val minimumTarget = 56.dp
}

object BastiShapes {
    val control = RoundedCornerShape(16.dp)
    val card = RoundedCornerShape(24.dp)
}

/** Explicit line heights grow with system font scale. No bundled font or line cap. */
object BastiTypography {
    private fun style(size: Int, line: Int, weight: FontWeight = FontWeight.Normal) = TextStyle(
        fontFamily = FontFamily.Default, fontSize = size.sp, lineHeight = line.sp, fontWeight = weight,
    )
    val homeHeading = style(32, 40, FontWeight.Bold)
    val screenHeading = style(28, 36, FontWeight.Bold)
    val question = style(24, 32, FontWeight.SemiBold)
    val answer = style(20, 28, FontWeight.SemiBold)
    val instruction = style(20, 28)
    val support = instruction
    val parentInformation = style(16, 24)
    val action = style(18, 26, FontWeight.SemiBold)

    // Existing NativeActionButton / NativeSupportMessage consume these Material roles.
    val material = Typography(
        displaySmall = homeHeading, headlineMedium = screenHeading, headlineSmall = question,
        titleLarge = answer, bodyLarge = instruction, bodyMedium = parentInformation,
        bodySmall = parentInformation, labelLarge = action,
    )
}
