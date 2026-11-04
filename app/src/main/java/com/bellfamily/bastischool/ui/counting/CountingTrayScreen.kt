package com.bellfamily.bastischool.ui.counting

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bellfamily.bastischool.learning.counting.CountingTray
import com.bellfamily.bastischool.learning.models.ContentLanguage
import com.bellfamily.bastischool.ui.common.*
import com.bellfamily.bastischool.ui.design.BastiPalette
import com.bellfamily.bastischool.ui.home.homeAsset

@Composable
fun CountingTrayScreen(
    state: CountingTray.State?, language: ContentLanguage, busy: Boolean, saveFailed: Boolean, audioFailed: Boolean,
    onToggle: (Int) -> Unit, onCheck: () -> Unit, onNext: () -> Unit, onReplay: () -> Unit,
    onHelp: () -> Unit, onAgain: () -> Unit, onRetry: () -> Unit, onHome: () -> Unit,
    modifier: Modifier = Modifier, onPop: (String) -> Unit = {}
) {
    fun t(en: String, de: String) = if (language == ContentLanguage.GERMAN) de else en
    val ready = state != null && state.language == language && !busy && !saveFailed
    if (state?.completed == true) {
        NativeCompletionScreen(state.id.value, language, CountingTray.complete.display[language], ready,
            "counting-tray-", onReplay, onAgain, onHome, onPop, modifier, saveFailed, onRetry, audioFailed)
        return
    }
    Column(modifier.fillMaxSize().background(Color(0xFFEAF7FC)).verticalScroll(rememberScrollState()).padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        NativeActivityTitle(CountingTray.title.display[language])
        if (state == null) Text(t("Opening…", "Wird geöffnet…")) else {
            NativeQuestionProgress(state.index + 1, state.targets.size, language)
            Text(state.target.toString(), Modifier.testTag("counting-tray-target"),
                style = MaterialTheme.typography.displayLarge, color = BastiPalette.ink)
            Text(CountingTray.prompt(state.target).display[language], Modifier.fillMaxWidth()
                .testTag("counting-tray-prompt"), style = MaterialTheme.typography.headlineSmall)
            NativeActionButton(t("Listen again", "Noch einmal hören"), NativeActionRole.SECONDARY, onReplay,
                Modifier.fillMaxWidth().testTag("counting-tray-replay"), ready)

            Row(Modifier.fillMaxWidth().widthIn(max = 680.dp).testTag("counting-tray-frame"),
                horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                repeat(5) { slot ->
                    val occupied = state.placedMask and (1 shl slot) != 0
                    val description = if (language == ContentLanguage.GERMAN) {
                        if (occupied) "Dinosaurier auf Feld ${slot + 1}. Tippe, um ihn wegzunehmen."
                        else "Leeres Feld ${slot + 1}. Tippe, um einen Dinosaurier dazuzulegen."
                    } else {
                        if (occupied) "Dinosaur in space ${slot + 1}. Tap to take it away."
                        else "Empty space ${slot + 1}. Tap to add a dinosaur."
                    }
                    Surface(onClick = { onToggle(slot) }, enabled = ready && !state.current.solved,
                        modifier = Modifier.weight(1f).aspectRatio(1f).testTag("counting-tray-slot-${slot + 1}")
                            .semantics { contentDescription = description },
                        shape = RoundedCornerShape(14.dp),
                        color = if (occupied) Color(0xFFE8F3D5) else Color.White,
                        border = androidx.compose.foundation.BorderStroke(2.dp,
                            if (occupied) Color(0xFF628C42) else Color(0xFF88AFC0))) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            if (occupied) Image(homeAsset("Animals/canonical/dinosaur.png"), null,
                                Modifier.fillMaxWidth(.82f).fillMaxHeight(.82f), alignment = Alignment.Center)
                            else Box(Modifier.size(22.dp).clip(RoundedCornerShape(50))
                                .background(Color(0xFFEAF7FC)).border(2.dp, Color(0xFF88AFC0), RoundedCornerShape(50)))
                        }
                    }
                }
            }
            Text(CountingTray.help.display[language], Modifier.fillMaxWidth().testTag("counting-tray-how-to"),
                style = MaterialTheme.typography.bodyLarge)

            if (state.current.support.hint) NativeSupportMessage(CountingTray.help.display[language],
                modifier = Modifier.testTag("counting-tray-help"))

            if (state.current.solved) {
                NativeSupportMessage(CountingTray.success(state.target).display[language],
                    modifier = Modifier.testTag("counting-tray-feedback"))
                NativeActionButton(t("Next", "Weiter"), NativeActionRole.PRIMARY, onNext,
                    Modifier.fillMaxWidth().testTag("counting-tray-next"), ready)
            } else {
                if (state.current.attempts > 0) NativeSupportMessage(CountingTray.checkAgain.display[language],
                    modifier = Modifier.testTag("counting-tray-feedback"))
                NativeActionButton(t("Check", "Prüfen"), NativeActionRole.PRIMARY, onCheck,
                    Modifier.fillMaxWidth().testTag("counting-tray-check"), ready)
                if (!state.current.support.hint) NativeActionButton(t("Help", "Hilfe"), NativeActionRole.SECONDARY,
                    onHelp, Modifier.fillMaxWidth().testTag("counting-tray-help-button"), ready)
            }
        }
        if (saveFailed) {
            NativeSupportMessage(t("Your saved work is kept. Please try again.",
                "Deine gespeicherte Arbeit bleibt erhalten. Versuche es erneut."))
            NativeActionButton(t("Try again", "Erneut versuchen"), NativeActionRole.SECONDARY, onRetry,
                Modifier.testTag("counting-tray-save-retry"), !busy)
        }
        if (audioFailed) Text(t("Speech is unavailable. Check installed offline voices.",
            "Die Sprachausgabe ist nicht verfügbar. Prüfe installierte Offline-Stimmen."), fontSize = 16.sp)
        NativeActionButton(t("Back home", "Zurück zum Start"), NativeActionRole.NAVIGATION, onHome,
            Modifier.fillMaxWidth().testTag("counting-tray-home"))
    }
}
