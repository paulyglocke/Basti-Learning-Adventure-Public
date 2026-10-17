package com.bellfamily.bastischool.ui.design

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

/** The caller owns the translated heading; no navigation or activity state. */
@Composable
fun BastiSectionHeading(text: String, modifier: Modifier = Modifier) {
    Text(text, modifier.fillMaxWidth().semantics { heading() }, style = BastiTypography.screenHeading,
        color = MaterialTheme.colorScheme.onBackground)
}

/**
 * One labelled Material button target. Illustration is decorative beside the visible label.
 * Selection is caller-owned and communicated by semantics, outline thickness and a checkmark.
 * No destination, speech, learning state, correctness or persistence is owned here.
 */
@Composable
fun BastiCategoryCard(
    label: String,
    illustration: Painter,
    treatment: BastiCategoryTreatment,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    enabled: Boolean = true,
) {
    Surface(
        onClick = onClick, enabled = enabled,
        modifier = modifier.heightIn(min = BastiSpacing.minimumTarget).semantics {
            this.selected = selected
            role = Role.Button
        },
        shape = BastiShapes.card, color = treatment.container, contentColor = treatment.foreground,
        border = BorderStroke(if (selected) 3.dp else 1.dp, if (selected) BastiPalette.primary else BastiPalette.outline),
    ) {
        Column(Modifier.padding(BastiSpacing.medium), verticalArrangement = Arrangement.spacedBy(BastiSpacing.small)) {
            Box(Modifier.fillMaxWidth()) {
                Image(illustration, contentDescription = null,
                    modifier = Modifier.fillMaxWidth().height(96.dp), contentScale = ContentScale.Fit)
                if (selected) Canvas(Modifier.size(24.dp).align(Alignment.TopEnd)) {
                    val stroke = 3.dp.toPx()
                    drawLine(BastiPalette.primary, Offset(size.width * .15f, size.height * .5f),
                        Offset(size.width * .4f, size.height * .75f), stroke, StrokeCap.Round)
                    drawLine(BastiPalette.primary, Offset(size.width * .4f, size.height * .75f),
                        Offset(size.width * .85f, size.height * .2f), stroke, StrokeCap.Round)
                }
            }
            Text(label, modifier = Modifier.fillMaxWidth(), style = BastiTypography.answer)
        }
    }
}
