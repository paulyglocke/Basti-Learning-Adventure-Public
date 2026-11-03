package com.bellfamily.bastischool.ui.addition

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.dp
import com.bellfamily.bastischool.learning.addition.MakeFiveContent
import com.bellfamily.bastischool.learning.models.ContentLanguage
import com.bellfamily.bastischool.learning.session.*
import com.bellfamily.bastischool.ui.common.*

@Composable
fun MakeFiveScreen(state: SessionState?, language: ContentLanguage, busy: Boolean, saveFailed: Boolean, audioFailed: Boolean,
    onAction: (SessionAction) -> Unit, onReplay: () -> Unit, onAgain: () -> Unit, onRetry: () -> Unit, onHome: () -> Unit,
    modifier: Modifier = Modifier, onPop: (String) -> Unit = {}) {
    fun t(en: String, de: String) = if (language == ContentLanguage.GERMAN) de else en
    val ready = state != null && state.language == language && !busy && !saveFailed
    if (state?.phase == SessionPhase.COMPLETED) {
        NativeCompletionScreen(state.plan.id.value, language, state.plan.completionText.display[language], ready,
            "make-five-", onReplay, onAgain, onHome, onPop, modifier, saveFailed, onRetry, audioFailed)
        return
    }
    Column(modifier.fillMaxSize().background(Color(0xFFEAF7FC)).verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        NativeActivityTitle(MakeFiveContent.title.display[language])
        if (state == null) Text(t("Opening…", "Wird geöffnet…")) else {
            NativeQuestionProgress(state.index + 1, state.plan.tasks.size, language)
            val fact = MakeFiveContent.fact(state.task.question)
            FiveFrame(fact.shown, language)
            Text(state.task.question.instruction.display[language], style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.testTag("make-five-prompt"))
            NativeActionButton(t("Listen again", "Noch einmal hören"), NativeActionRole.SECONDARY, onReplay,
                Modifier.fillMaxWidth().testTag("make-five-replay"), ready)
            state.task.question.choices.forEach { id ->
                val answer = MakeFiveContent.numberValue(id)
                NativeTextChoice(answer.toString(), { state.nextAttempt?.let { onAction(SessionAction.Answer(it, id)) } },
                    Modifier.fillMaxWidth().testTag("make-five-answer-$answer"),
                    ready && state.current.answer == AnswerState.UNANSWERED && state.nextAttempt != null)
            }
            if (state.current.support.hint) NativeSupportMessage(MakeFiveContent.help.display[language])
            if (!state.current.locked) NativeActionButton(t("Help", "Hilfe"), NativeActionRole.SECONDARY,
                { onAction(SessionAction.Hint(state.task.id)) }, Modifier.testTag("make-five-help"), ready)
            when (state.current.answer) {
                AnswerState.RETRY_AVAILABLE -> {
                    NativeSupportMessage(state.task.question.wrongFeedback.display[language])
                    NativeActionButton(t("Try again", "Nochmal versuchen"), NativeActionRole.SECONDARY,
                        { onAction(SessionAction.Retry(AttemptId(state.task.id, state.current.attempts))) }, Modifier.testTag("make-five-retry"), ready)
                }
                AnswerState.CORRECT -> {
                    NativeSupportMessage(state.task.question.correctFeedback.display[language])
                    NativeActionButton(t("Next", "Weiter"), NativeActionRole.PRIMARY,
                        { onAction(SessionAction.Next(state.task.id)) }, Modifier.fillMaxWidth().testTag("make-five-next"), ready)
                }
                else -> Unit
            }
        }
        if (saveFailed) {
            NativeSupportMessage(t("Your saved work is kept. Please try again.", "Deine gespeicherte Arbeit bleibt erhalten. Versuche es erneut."))
            NativeActionButton(t("Try again", "Erneut versuchen"), NativeActionRole.SECONDARY, onRetry,
                Modifier.testTag("make-five-save-retry"), !busy)
        }
        if (audioFailed) Text(t("Speech is unavailable. Check installed offline voices.", "Die Sprachausgabe ist nicht verfügbar. Prüfe installierte Offline-Stimmen."))
        NativeActionButton(t("Back home", "Zurück zum Start"), NativeActionRole.NAVIGATION, onHome,
            Modifier.fillMaxWidth().testTag("make-five-home"))
    }
}

@Composable
private fun FiveFrame(shown: Int, language: ContentLanguage) {
    val cells = List(5) { index -> index < shown }
    val label = if (language == ContentLanguage.GERMAN) "Fünferfeld: " + cells.joinToString(", ") { if (it) "Punkt" else "leer" }
        else "Five-frame: " + cells.joinToString(", ") { if (it) "dot" else "empty" }
    Row(Modifier.fillMaxWidth().testTag("make-five-frame").clearAndSetSemantics { contentDescription = label },
        horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        repeat(5) { index ->
            val occupied = index < shown
            Box(Modifier.weight(1f).aspectRatio(0.9f)
                .background(Color.White, RoundedCornerShape(12.dp))
                .border(2.dp, if (occupied) Color(0xFF3A7795) else Color(0xFF88AFC0), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center) {
                if (occupied) Box(Modifier.size(30.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary))
                else Box(Modifier.size(30.dp).clip(CircleShape).border(2.dp, Color(0xFF88AFC0), CircleShape))
            }
        }
    }
}
