package com.bellfamily.bastischool.ui.common

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/** Presentation only: callers retain labels, enabled state and action ownership. */
enum class NativeActionRole { PRIMARY, SECONDARY, NAVIGATION }

/** Material semantics/ripple with a growing, never fixed-height, child-sized target. */
@Composable
fun NativeActionButton(
    label: String,
    role: NativeActionRole,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val target = modifier.heightIn(min = 56.dp)
    val shape = RoundedCornerShape(16.dp)
    val padding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
    val content: @Composable () -> Unit = {
        Text(label, modifier = Modifier.fillMaxWidth(), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center)
    }
    when (role) {
        NativeActionRole.PRIMARY -> Button(onClick, target, enabled, shape = shape,
            contentPadding = padding) { content() }
        NativeActionRole.SECONDARY -> FilledTonalButton(onClick, target, enabled, shape = shape,
            contentPadding = padding) { content() }
        NativeActionRole.NAVIGATION -> OutlinedButton(onClick, target, enabled, shape = shape,
            contentPadding = padding) { content() }
    }
}
