package com.bellfamily.bastischool.ui.common

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/** Activity title presentation only; wording and placement remain caller-owned. */
@Composable
fun NativeActivityTitle(title: String, modifier: Modifier = Modifier) {
    Text(title, modifier = modifier, style = MaterialTheme.typography.headlineMedium)
}
