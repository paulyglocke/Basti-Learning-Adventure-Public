package com.bellfamily.bastischool.ui.tellme

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.bellfamily.bastischool.R
import com.bellfamily.bastischool.learning.models.ContentLanguage
import com.bellfamily.bastischool.learning.scenedescription.SceneCategory
import com.bellfamily.bastischool.ui.common.NativeActionButton
import com.bellfamily.bastischool.ui.common.NativeActionRole

/** Presentation mapping only. The repository continues to own membership, names and order. */
@DrawableRes
internal fun tellMeBanner(category: String): Int? = when (category) {
    "ocean_underwater" -> R.drawable.tellme_banner_ocean
    "jungle_rainforest" -> R.drawable.tellme_banner_jungle
    "woodland_forest" -> R.drawable.tellme_banner_woodland
    "park_playground" -> R.drawable.tellme_banner_park
    "sky_flying" -> R.drawable.tellme_banner_sky
    "mountains_alpine" -> R.drawable.tellme_banner_mountains
    "countryside_farm" -> R.drawable.tellme_banner_farm
    "classroom_school" -> R.drawable.tellme_banner_classroom
    "zoo_wildlife" -> R.drawable.tellme_banner_zoo
    else -> null
}

@Composable
internal fun TellMeAdventureBanner(category: SceneCategory, language: ContentLanguage, onClick: () -> Unit) {
    val label = category.display[language]
    val modifier = Modifier.fillMaxWidth().testTag("tellme-category-${category.id.value}")
    val resource = tellMeBanner(category.id.value)
    if (resource == null) {
        NativeActionButton(label, NativeActionRole.NAVIGATION, onClick, modifier)
        return
    }
    Card(onClick = onClick, modifier = modifier.semantics { role = Role.Button }, shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF173840))) {
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            // Preserve the complete supplied composition. Phones retain a 120dp minimum;
            // wide screens retain the 4:1 artwork ratio, and text can always grow the card.
            Box(Modifier.fillMaxWidth().heightIn(min = (maxWidth / 4).coerceAtLeast(120.dp)),
                contentAlignment = Alignment.Center) {
                Image(painterResource(resource), contentDescription = null,
                    modifier = Modifier.matchParentSize().testTag("tellme-banner-${category.id.value}"),
                    contentScale = ContentScale.Fit)
                Text(label, color = Color.White, style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold, textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 16.dp).fillMaxWidth()
                        .background(Color.Black.copy(alpha = .62f)).padding(horizontal = 12.dp, vertical = 8.dp)
                        .testTag("tellme-banner-title-${category.id.value}"))
            }
        }
    }
}
