package com.bellfamily.bastischool.ui.design

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/** Explicit opt-in. MainActivity deliberately keeps its established theme in Phase 1. */
val BastiDiscoveryColours = lightColorScheme(
    primary = BastiPalette.primary, onPrimary = BastiPalette.onPrimary,
    primaryContainer = BastiPalette.secondaryContainer, onPrimaryContainer = BastiPalette.ink,
    secondary = BastiPalette.primary, onSecondary = BastiPalette.onPrimary,
    secondaryContainer = BastiPalette.secondaryContainer, onSecondaryContainer = BastiPalette.onSecondaryContainer,
    tertiary = BastiPalette.primary, onTertiary = BastiPalette.onPrimary,
    tertiaryContainer = BastiCategoryTreatment.WORDS.container, onTertiaryContainer = BastiPalette.ink,
    background = BastiPalette.background, onBackground = BastiPalette.ink,
    surface = BastiPalette.surface, onSurface = BastiPalette.ink,
    surfaceVariant = BastiPalette.secondaryContainer, onSurfaceVariant = BastiPalette.ink,
    surfaceTint = BastiPalette.primary,
    surfaceBright = BastiPalette.surface, surfaceDim = BastiPalette.secondaryContainer,
    surfaceContainerLowest = BastiPalette.surface, surfaceContainerLow = BastiPalette.background,
    surfaceContainer = BastiPalette.background, surfaceContainerHigh = BastiPalette.secondaryContainer,
    surfaceContainerHighest = BastiPalette.secondaryContainer,
    outline = BastiPalette.outline, outlineVariant = BastiPalette.outline,
    inverseSurface = BastiPalette.ink, inverseOnSurface = BastiPalette.surface,
    inversePrimary = BastiPalette.secondaryContainer,
    error = Color(0xFF8C3030), onError = BastiPalette.surface,
    errorContainer = Color(0xFFF9E6DC), onErrorContainer = BastiPalette.ink,
    scrim = Color.Black,
)

@Composable
fun BastiDesignTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = BastiDiscoveryColours,
        typography = BastiTypography.material,
        shapes = Shapes(medium = BastiShapes.control, large = BastiShapes.card),
        content = content,
    )
}
