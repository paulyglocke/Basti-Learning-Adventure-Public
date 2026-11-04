package com.bellfamily.bastischool.ui.followinstructions

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.bellfamily.bastischool.learning.models.ContentId
import com.bellfamily.bastischool.learning.models.ContentLanguage
import com.bellfamily.bastischool.learning.schoolskills.SchoolActionSequenceContent
import com.bellfamily.bastischool.learning.session.*
import com.bellfamily.bastischool.ui.common.*

@Composable
fun SchoolActionSequenceRoute(
    viewModel: FollowViewModel, languageCode: String, modifier: Modifier,
    onAgain: () -> Unit, onHome: () -> Unit, onLevels: () -> Unit, onPop: (String) -> Unit
) {
    SchoolActionSequenceScreen(viewModel.actionSequenceState,
        if (languageCode == "de") ContentLanguage.GERMAN else ContentLanguage.ENGLISH,
        viewModel.schoolActionImages, viewModel.busy, viewModel.saveFailed, viewModel.audioFailed,
        viewModel::actionSequenceAction, viewModel::actionSequenceReplay, onAgain, viewModel::retrySave,
        onHome, onLevels, modifier, onPop)
}

@Composable
fun SchoolActionSequenceScreen(
    state: SessionState?, language: ContentLanguage, images: Map<ContentId, ImageBitmap>, busy: Boolean,
    saveFailed: Boolean, audioFailed: Boolean, onAction: (SessionAction) -> Unit,
    onReplay: () -> Unit, onAgain: () -> Unit, onRetrySave: () -> Unit,
    onHome: () -> Unit, onLevels: () -> Unit, modifier: Modifier = Modifier,
    onPop: (String) -> Unit = {}
) {
    fun t(en: String, de: String) = if (language == ContentLanguage.GERMAN) de else en
    val ready = !busy && !saveFailed && state != null && state.language == language
    if (state?.phase == SessionPhase.COMPLETED) {
        NativeCompletionScreen(state.plan.id.value, language, state.plan.completionText.display[language], ready,
            "school-action-order-", onReplay, onAgain, onHome, onPop, modifier, saveFailed, onRetrySave, audioFailed)
        return
    }
    Column(modifier.fillMaxSize().background(Color(0xFFEAF7FC)).verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        NativeActivityTitle(t("Two Actions in Order", "Zwei Aktionen der Reihe nach"))
        NativeSupportMessage(t("Listen for both actions and choose the pictures in the same order.",
            "Hör dir beide Aktionen an und wähle die Bilder in derselben Reihenfolge."))
        if (state == null) {
            if (!saveFailed) Text(t("Opening…", "Wird geöffnet…"))
        } else {
            Text(t("Question ${state.index + 1} of ${state.plan.tasks.size}", "Frage ${state.index + 1} von ${state.plan.tasks.size}"),
                style = MaterialTheme.typography.titleMedium, modifier = Modifier.testTag("school-action-order-progress"))
            Text(state.task.question.instruction.display[language], Modifier.fillMaxWidth().testTag("school-action-order-prompt"),
                style = MaterialTheme.typography.titleLarge)
            NativeActionButton(t("Listen again", "Noch einmal hören"), NativeActionRole.SECONDARY, onReplay,
                Modifier.fillMaxWidth().testTag("school-action-order-replay"), ready)
            if (state.hintAvailable) NativeActionButton(t("Hint", "Tipp"), NativeActionRole.SECONDARY,
                { onAction(SessionAction.Hint(state.task.id)) }, Modifier.fillMaxWidth().testTag("school-action-order-hint"), ready)
            if (state.current.support.hint)
                NativeSupportMessage(state.task.question.hint!!.display[language], Modifier.testTag("school-action-order-hint-message"))
            state.task.question.choices.chunked(2).forEach { row ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    row.forEach { id ->
                        val sequence = SchoolActionSequenceContent.sequence(id)
                        val shape = RoundedCornerShape(16.dp)
                        Column(Modifier.weight(1f).clip(shape).background(Color.White)
                            .border(2.dp, Color(0xFFB6D9E5), shape).clickable(
                                enabled = ready && state.current.answer == AnswerState.UNANSWERED,
                                role = Role.Button
                            ) { state.nextAttempt?.let { onAction(SessionAction.Answer(it, id)) } }
                            .semantics(mergeDescendants = true) { contentDescription = sequence.label.display[language] }
                            .padding(8.dp).testTag("school-action-order-choice-${id.value}"),
                            horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(5.dp)) {
                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                listOf(sequence.first, sequence.second).forEachIndexed { index, action ->
                                    if (index > 0) Text("→", style = MaterialTheme.typography.titleMedium)
                                    images[action.id]?.let { image ->
                                        Image(image, null, Modifier.weight(1f).heightIn(min = 62.dp, max = 84.dp)
                                            .clip(RoundedCornerShape(8.dp)), contentScale = ContentScale.Crop)
                                    }
                                }
                            }
                            Text(sequence.label.display[language], style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
            when (state.current.answer) {
                AnswerState.RETRY_AVAILABLE -> {
                    NativeSupportMessage(state.task.question.wrongFeedback.display[language], Modifier.testTag("school-action-order-feedback"))
                    NativeActionButton(t("Try again", "Nochmal versuchen"), NativeActionRole.SECONDARY,
                        { onAction(SessionAction.Retry(AttemptId(state.task.id, state.current.attempts))) },
                        Modifier.fillMaxWidth().testTag("school-action-order-retry"), ready)
                }
                AnswerState.CORRECT -> {
                    NativeSupportMessage(state.task.question.correctFeedback.display[language], Modifier.testTag("school-action-order-feedback"))
                    NativeActionButton(t(if (state.index == state.plan.tasks.lastIndex) "Finish" else "Next",
                        if (state.index == state.plan.tasks.lastIndex) "Fertig" else "Weiter"), NativeActionRole.PRIMARY,
                        { onAction(SessionAction.Next(state.task.id)) }, Modifier.fillMaxWidth().testTag("school-action-order-next"), ready)
                }
                else -> Unit
            }
        }
        if (saveFailed) {
            NativeSupportMessage(t("Your saved work is kept. Please try again.", "Deine gespeicherte Arbeit bleibt erhalten. Versuch es erneut."))
            NativeActionButton(t("Try again", "Erneut versuchen"), NativeActionRole.SECONDARY, onRetrySave,
                Modifier.testTag("school-action-order-save-retry"), !busy)
        }
        if (audioFailed) NativeSupportMessage(t("Speech is unavailable. Check installed offline voices in Options.",
            "Die Sprachausgabe ist nicht verfügbar. Prüfe die Offline-Stimmen in den Optionen."))
        NativeActionButton(t("Home", "Startseite"), NativeActionRole.NAVIGATION, onHome,
            Modifier.fillMaxWidth().testTag("school-action-order-home"))
        NativeActionButton(t("Choose an activity", "Übung wählen"), NativeActionRole.NAVIGATION, onLevels,
            Modifier.fillMaxWidth().testTag("school-action-order-levels"))
    }
}
