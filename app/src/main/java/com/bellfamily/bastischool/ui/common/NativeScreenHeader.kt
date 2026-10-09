package com.bellfamily.bastischool.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp

/** Growing shell title; extreme text sizes can scroll without consuming the entire screen. */
@Composable
fun NativeScreenHeader(title: @Composable () -> Unit, navigationIcon: @Composable () -> Unit,
    actions: @Composable () -> Unit, background: Color, windowInsets: WindowInsets) {
    Row(Modifier.fillMaxWidth().background(background).windowInsetsPadding(windowInsets)
        .heightIn(max = LocalConfiguration.current.screenHeightDp.dp * .4f)
        .verticalScroll(rememberScrollState()).padding(horizontal = 4.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        navigationIcon()
        Box(Modifier.weight(1f)) {
            ProvideTextStyle(MaterialTheme.typography.titleLarge) { title() }
        }
        actions()
    }
}
