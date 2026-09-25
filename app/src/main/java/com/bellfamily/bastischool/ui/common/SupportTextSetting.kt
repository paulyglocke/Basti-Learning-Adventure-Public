package com.bellfamily.bastischool.ui.common

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp

/** One shell-owned preference; the whole row is one labelled switch target. */
@Composable
fun SupportTextSetting(language: String, enabled: Boolean, onChange: (Boolean) -> Unit) {
    val german = language == "de"
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            Modifier.fillMaxWidth().heightIn(min = 56.dp).testTag("larger-support-text")
                .toggleable(value = enabled, role = Role.Switch, onValueChange = onChange)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(if (german) "Größere Hinweise und Hilfetexte" else "Larger hints and retry guidance",
                modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
            Switch(checked = enabled, onCheckedChange = null)
        }
        Text(if (german) "Vergrößert Hinweise und Hilfetexte in Präpositionen, Jahreszeiten, Wilmas Woche und Wortschatz. Die Schriftgröße deines Geräts gilt zusätzlich."
            else "Enlarges hints and retry guidance in Prepositions, Seasons, Wilma’s Week and Vocabulary Booster. Your device’s text size still applies.",
            style = MaterialTheme.typography.bodyMedium)
    }
}
