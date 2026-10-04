package com.bellfamily.bastischool.ui.common

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bellfamily.bastischool.learning.models.ContentLanguage

/** Active quiz position, supplied one-based by the caller; no session or completion ownership. */
@Composable
fun NativeQuestionProgress(current: Int, total: Int, language: ContentLanguage, modifier: Modifier = Modifier) {
    Text(
        text = if (language == ContentLanguage.GERMAN) "Frage $current von $total" else "Question $current of $total",
        modifier = modifier,
        style = MaterialTheme.typography.headlineSmall,
    )
}
