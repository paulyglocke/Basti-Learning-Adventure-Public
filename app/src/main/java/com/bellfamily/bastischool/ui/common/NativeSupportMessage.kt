package com.bellfamily.bastischool.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** Presentation preference only; Android font scaling still applies. */
val LocalLargerSupportText = staticCompositionLocalOf { false }

/**
 * Calm presentation for existing authored hints and retry guidance.
 * Callers own wording and visibility; this adds no support level, action, announcement or state.
 * A single Text node retains reading order/tags without redundant accessibility descriptions.
 */
@Composable
fun NativeSupportMessage(text: String, modifier: Modifier = Modifier) {
    val base = MaterialTheme.typography.bodyLarge
    val style = if (LocalLargerSupportText.current) base.copy(
        fontSize = base.fontSize * 1.25f, lineHeight = base.lineHeight * 1.25f,
    ) else base
    Text(
        text = text,
        modifier = modifier.fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
            .padding(12.dp),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        style = style,
    )
}
