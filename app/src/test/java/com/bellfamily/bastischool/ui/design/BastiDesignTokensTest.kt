package com.bellfamily.bastischool.ui.design

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontFamily
import org.junit.Assert.*
import org.junit.Test
import kotlin.math.pow

class BastiDesignTokensTest {
    private fun luminance(c: Color): Double {
        fun linear(v: Float) = if (v <= .04045f) v / 12.92 else ((v + .055) / 1.055).pow(2.4)
        return .2126 * linear(c.red) + .7152 * linear(c.green) + .0722 * linear(c.blue)
    }
    private fun contrast(a: Color, b: Color): Double {
        val values = listOf(luminance(a), luminance(b)).sorted()
        return (values.last() + .05) / (values.first() + .05)
    }

    @Test fun proposedPaletteIsExactAndAllTextPairsHaveNormalTextContrast() {
        assertEquals(0xFFFBF8F0.toInt(), BastiPalette.background.toArgb())
        assertEquals(0xFFFFFFFF.toInt(), BastiPalette.surface.toArgb())
        assertEquals(0xFF203747.toInt(), BastiPalette.ink.toArgb())
        assertEquals(0xFF2E6B50.toInt(), BastiPalette.primary.toArgb())
        assertEquals(0xFFDDEDE2.toInt(), BastiPalette.secondaryContainer.toArgb())
        assertEquals(listOf(0xFFE3F0FC, 0xFFEEE9F8, 0xFFE7F2DC, 0xFFDFF1E9, 0xFFFFF0D0, 0xFFF9E6DC)
            .map { it.toInt() }, BastiCategoryTreatment.entries.map { it.container.toArgb() })
        val pairs = listOf(BastiPalette.ink to BastiPalette.background, BastiPalette.ink to BastiPalette.surface,
            BastiPalette.onPrimary to BastiPalette.primary,
            BastiPalette.onSecondaryContainer to BastiPalette.secondaryContainer) +
            BastiCategoryTreatment.entries.map { it.foreground to it.container }
        pairs.forEach { (fg, bg) -> assertTrue("Contrast ${contrast(fg, bg)}", contrast(fg, bg) >= 4.5) }
    }

    @Test fun SelectionAndControlOutlinesContrastWithTheirSurfaces() {
        val surfaces = BastiCategoryTreatment.entries.map { it.container } +
            listOf(BastiPalette.background, BastiPalette.surface, BastiPalette.secondaryContainer)
        surfaces.forEach {
            assertTrue(contrast(BastiPalette.primary, it) >= 3.0)
            assertTrue(contrast(BastiPalette.outline, it) >= 3.0)
        }
    }

    @Test fun typographyUsesSystemFontAndExplicitComfortableLineHeights() {
        val styles = listOf(BastiTypography.homeHeading, BastiTypography.screenHeading,
            BastiTypography.question, BastiTypography.answer, BastiTypography.instruction,
            BastiTypography.support, BastiTypography.parentInformation, BastiTypography.action)
        styles.forEach {
            assertEquals(FontFamily.Default, it.fontFamily)
            assertTrue(it.lineHeight.value >= it.fontSize.value * 1.2f)
        }
        assertEquals(32f, BastiTypography.homeHeading.fontSize.value)
        assertEquals(40f, BastiTypography.homeHeading.lineHeight.value)
        assertEquals(BastiTypography.action, BastiTypography.material.labelLarge)
        assertEquals(BastiTypography.support, BastiTypography.material.bodyLarge)
    }

    @Test fun materialThemeUsesExplicitReadableForegrounds() {
        val c = BastiDiscoveryColours
        listOf(c.primary to c.onPrimary, c.primaryContainer to c.onPrimaryContainer,
            c.secondary to c.onSecondary, c.secondaryContainer to c.onSecondaryContainer,
            c.tertiary to c.onTertiary, c.tertiaryContainer to c.onTertiaryContainer,
            c.background to c.onBackground, c.surface to c.onSurface,
            c.surfaceVariant to c.onSurfaceVariant, c.inverseSurface to c.inverseOnSurface,
            c.error to c.onError, c.errorContainer to c.onErrorContainer).forEach { (bg, fg) ->
            assertTrue(contrast(fg, bg) >= 4.5)
        }
    }
}
