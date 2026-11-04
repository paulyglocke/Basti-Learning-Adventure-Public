package com.bellfamily.bastischool.ui.followinstructions

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.bellfamily.bastischool.learning.models.ContentId
import com.bellfamily.bastischool.learning.models.ContentLanguage
import com.bellfamily.bastischool.learning.session.*
import com.bellfamily.bastischool.learning.schoolskills.SchoolSkillsContent
import com.bellfamily.bastischool.ui.common.*

@Composable
fun SchoolSkillsScreen(
    state: SessionState?, language: ContentLanguage, scene: ImageBitmap?, illustrations: Map<ContentId, ImageBitmap>, busy: Boolean,
    saveFailed: Boolean, audioFailed: Boolean, onAction: (SessionAction) -> Unit,
    onReplay: () -> Unit, onAgain: () -> Unit, onRetrySave: () -> Unit,
    onHome: () -> Unit, onLevels: () -> Unit, modifier: Modifier = Modifier,
    onPop: (String) -> Unit = {}
) {
    fun t(en: String, de: String) = if (language == ContentLanguage.GERMAN) de else en
    val ready = !busy && !saveFailed && state != null && state.language == language
    if (state?.phase == SessionPhase.COMPLETED) {
        NativeCompletionScreen(state.plan.id.value, language, state.plan.completionText.display[language], ready,
            "school-skills-", onReplay, onAgain, onHome, onPop, modifier, saveFailed, onRetrySave, audioFailed)
        return
    }
    Column(modifier.fillMaxSize().background(Color(0xFFEAF7FC)).verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        NativeActivityTitle(t("Useful Words at School", "Hilfreiche Worte in der Schule"))
        NativeSupportMessage(t("There are many good ways to ask for what you need. Practise one phrase at a time.",
            "Es gibt viele gute Möglichkeiten, um etwas zu bitten. Übe einen Satz nach dem anderen."))
        val illustration = state?.task?.question?.correct?.let { illustrations[it] } ?: scene
        if (illustration != null) Image(illustration, t("A classroom illustration showing this situation", "Eine Illustration aus dem Klassenzimmer zu dieser Situation"),
            Modifier.fillMaxWidth().heightIn(max = 190.dp).clip(RoundedCornerShape(18.dp)).testTag("school-skills-scene"),
            contentScale = ContentScale.Crop)
        if (state == null) {
            if (!saveFailed) Text(t("Opening…", "Wird geöffnet…"))
        } else {
            Text(t("Question ${state.index + 1} of ${state.plan.tasks.size}", "Frage ${state.index + 1} von ${state.plan.tasks.size}"),
                style = MaterialTheme.typography.titleMedium, modifier = Modifier.testTag("school-skills-progress"))
            Text(state.task.question.instruction.display[language], Modifier.fillMaxWidth().testTag("school-skills-prompt"),
                style = MaterialTheme.typography.titleLarge)
            NativeActionButton(t("Listen again", "Noch einmal hören"), NativeActionRole.SECONDARY, onReplay,
                Modifier.fillMaxWidth().testTag("school-skills-replay"), ready)
            if (state.hintAvailable) NativeActionButton(t("Hint", "Tipp"), NativeActionRole.SECONDARY,
                { onAction(SessionAction.Hint(state.task.id)) }, Modifier.fillMaxWidth().testTag("school-skills-hint"), ready)
            if (state.current.support.hint)
                NativeSupportMessage(state.task.question.hint!!.display[language], Modifier.testTag("school-skills-hint-message"))
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                state.task.question.choices.forEach { id ->
                    NativeTextChoice(SchoolSkillsContent.phrase(id).text.display[language], {
                        state.nextAttempt?.let { onAction(SessionAction.Answer(it, id)) }
                    }, Modifier.fillMaxWidth().testTag("school-skills-choice-${id.value}"),
                        ready && state.current.answer == AnswerState.UNANSWERED)
                }
            }
            when (state.current.answer) {
                AnswerState.RETRY_AVAILABLE -> {
                    NativeSupportMessage(state.task.question.wrongFeedback.display[language], Modifier.testTag("school-skills-feedback"))
                    NativeActionButton(t("Try again", "Nochmal versuchen"), NativeActionRole.SECONDARY,
                        { onAction(SessionAction.Retry(AttemptId(state.task.id, state.current.attempts))) },
                        Modifier.fillMaxWidth().testTag("school-skills-retry"), ready)
                }
                AnswerState.CORRECT -> {
                    NativeSupportMessage(state.task.question.correctFeedback.display[language], Modifier.testTag("school-skills-feedback"))
                    NativeActionButton(t(if (state.index == state.plan.tasks.lastIndex) "Finish" else "Next",
                        if (state.index == state.plan.tasks.lastIndex) "Fertig" else "Weiter"), NativeActionRole.PRIMARY,
                        { onAction(SessionAction.Next(state.task.id)) }, Modifier.fillMaxWidth().testTag("school-skills-next"), ready)
                }
                else -> Unit
            }
        }
        if (saveFailed) {
            NativeSupportMessage(t("Your saved work is kept. Please try again.", "Deine gespeicherte Arbeit bleibt erhalten. Versuch es erneut."))
            NativeActionButton(t("Try again", "Erneut versuchen"), NativeActionRole.SECONDARY, onRetrySave,
                Modifier.testTag("school-skills-save-retry"), !busy)
        }
        if (audioFailed) NativeSupportMessage(t("Speech is unavailable. Check installed offline voices in Options.",
            "Die Sprachausgabe ist nicht verfügbar. Prüfe die Offline-Stimmen in den Optionen."))
        NativeActionButton(t("Home", "Startseite"), NativeActionRole.NAVIGATION, onHome,
            Modifier.fillMaxWidth().testTag("school-skills-home"))
        NativeActionButton(t("Choose a level", "Stufe wählen"), NativeActionRole.NAVIGATION, onLevels,
            Modifier.fillMaxWidth().testTag("school-skills-levels"))
    }
}
