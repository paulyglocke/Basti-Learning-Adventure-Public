package com.bellfamily.bastischool.ui.clock

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bellfamily.bastischool.learning.clock.*
import com.bellfamily.bastischool.learning.models.ContentLanguage
import com.bellfamily.bastischool.ui.common.*

@Composable
fun ClockPracticeScreen(state: ClockPractice.State?, language: ContentLanguage, busy: Boolean, failed: Boolean, audioFailed: Boolean,
    onMove: (ClockTime) -> Unit, onInterrupt: () -> Unit, onAdjust: (Int) -> Unit, onCheck: () -> Unit,
    onNext: () -> Unit, onReplay: () -> Unit, onAgain: () -> Unit, onRetry: () -> Unit, onExplore: () -> Unit,
    onHome: () -> Unit, modifier: Modifier = Modifier, onPop: (String) -> Unit = {}) {
    fun t(en: String, de: String) = if (language == ContentLanguage.GERMAN) de else en
    val ready = state != null && state.language == language && !busy && !failed
    if (state?.completed == true) {
        NativeCompletionScreen(state.id.value, language, ClockPractice.complete.display[language], ready,
            "clock-make-", onReplay, onAgain, onHome, onPop, modifier, failed, onRetry, audioFailed)
        return
    }
    Column(modifier.fillMaxSize().background(Color(0xFFEAF7FC)).verticalScroll(rememberScrollState()).padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        NativeActivityTitle(t("Make This Time", "Stelle diese Uhrzeit ein"))
        if (state == null) Text(t("Opening…", "Wird geöffnet…")) else {
            NativeQuestionProgress(state.index + 1, 8, language)
            Text(ClockPractice.prompt(state.target).display[language], fontSize = 24.sp,
                modifier = Modifier.testTag("clock-target").semantics {
                    contentDescription = t("Target: ", "Ziel: ") + ClockPractice.prompt(state.target).display[language]
                })
            ClockFace(state.position, language, ready && !state.current.solved, onMove, {}, onInterrupt, onAdjust,
                step = ClockPractice.STEP, practice = true)
            if (state.current.solved) {
                Text(state.position.digital, fontSize = 32.sp, modifier = Modifier.testTag("clock-make-digital"))
                NativeSupportMessage(ClockPractice.correct(state.target).display[language])
                NativeActionButton(t("Next", "Weiter"), NativeActionRole.PRIMARY, onNext,
                    Modifier.fillMaxWidth().testTag("clock-make-next"), ready)
            } else {
                if (state.current.wrong > 0) NativeSupportMessage(ClockPractice.retry.display[language])
                if (state.current.support.hint) NativeSupportMessage(ClockPractice.hint(state.target).display[language],
                    modifier = Modifier.testTag("clock-make-hint"))
                NativeActionButton(t("30 minutes back", "30 Minuten zurück"), NativeActionRole.SECONDARY,
                    { onAdjust(-30) }, Modifier.fillMaxWidth().testTag("clock-make-back"), ready)
                NativeActionButton(t("30 minutes forward", "30 Minuten weiter"), NativeActionRole.SECONDARY,
                    { onAdjust(30) }, Modifier.fillMaxWidth().testTag("clock-make-forward"), ready)
                NativeActionButton(t("Check", "Prüfen"), NativeActionRole.PRIMARY, onCheck,
                    Modifier.fillMaxWidth().testTag("clock-make-check"), ready)
            }
            NativeActionButton(t("Listen", "Anhören"), NativeActionRole.SECONDARY, onReplay,
                Modifier.fillMaxWidth().testTag("clock-make-listen"), ready)
        }
        if (failed) {
            NativeSupportMessage(t("Your saved work is kept. Please try again.", "Deine gespeicherte Arbeit bleibt erhalten. Versuche es erneut."))
            NativeActionButton(t("Try again", "Erneut versuchen"), NativeActionRole.SECONDARY, onRetry, enabled = !busy)
        }
        if (audioFailed) Text(t("Speech is unavailable. Check installed offline voices.", "Die Sprachausgabe ist nicht verfügbar. Prüfe installierte Offline-Stimmen."))
        NativeActionButton(t("Explore", "Entdecken"), NativeActionRole.NAVIGATION, onExplore,
            Modifier.fillMaxWidth().testTag("clock-make-explore"))
        NativeActionButton(t("Back home", "Zurück zum Start"), NativeActionRole.NAVIGATION, onHome,
            Modifier.fillMaxWidth().testTag("clock-make-home"))
    }
}
