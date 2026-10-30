package com.bellfamily.bastischool.ui.habitats

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import com.bellfamily.bastischool.learning.animals.HabitatMatchingContent
import com.bellfamily.bastischool.learning.models.ContentId
import com.bellfamily.bastischool.learning.models.ContentLanguage
import com.bellfamily.bastischool.learning.session.*
import com.bellfamily.bastischool.ui.common.*
import com.bellfamily.bastischool.ui.design.*

@Composable
fun HabitatMatchingScreen(
    state: SessionState?,
    habitatImages: Map<ContentId, ImageBitmap>,
    animalImages: Map<ContentId, ImageBitmap>,
    language: ContentLanguage,
    busy: Boolean,
    saveFailed: Boolean,
    audioFailed: Boolean,
    onReplay: () -> Unit,
    onHelp: () -> Unit,
    onAction: (SessionAction) -> Unit,
    onAgain: () -> Unit,
    onRetry: () -> Unit,
    onHome: () -> Unit,
    modifier: Modifier = Modifier,
    onPop: (String) -> Unit = {}
) {
    fun t(en: String, de: String) = if (language == ContentLanguage.GERMAN) de else en
    val ready = state != null && !busy && !saveFailed && state.language == language
    if (state?.phase == SessionPhase.COMPLETED) {
        NativeCompletionScreen(state.plan.id.value, language, state.plan.completionText.display[language], ready,
            "habitat-match-", onReplay, onAgain, onHome, onPop, modifier, saveFailed, onRetry, audioFailed)
        return
    }
    val scrollState = remember { ScrollState(0) }
    val configuration = LocalConfiguration.current
    LaunchedEffect(state?.task?.id, language, configuration.orientation, configuration.screenWidthDp,
        configuration.screenHeightDp, configuration.fontScale) {
        scrollState.scrollTo(0)
    }
    Column(modifier.fillMaxSize().background(BastiPalette.background).verticalScroll(scrollState)
        .padding(horizontal = BastiSpacing.medium, vertical = BastiSpacing.medium),
        verticalArrangement = Arrangement.spacedBy(BastiSpacing.small)) {
        NativeActivityTitle(t("Match Animals to Habitats", "Tiere und Lebensräume zuordnen"),
            Modifier.fillMaxWidth().testTag("habitat-match-title"))
        if (state == null) {
            Text(t("Opening…", "Wird geöffnet …"), Modifier.testTag("habitat-match-loading"), color = BastiPalette.ink)
        } else {
            val question = state.task.question
            val item = HabitatMatchingContent.reviewed(question.definition)
            Text("${state.index + 1} ${t("of", "von")} ${state.plan.tasks.size}",
                Modifier.fillMaxWidth().testTag("habitat-match-progress"), style = BastiTypography.parentInformation, color = BastiPalette.ink)
            Text(question.instruction.display[language], Modifier.fillMaxWidth().testTag("habitat-match-instruction"),
                style = BastiTypography.question, color = BastiPalette.ink)
            Row(Modifier.fillMaxWidth().testTag("habitat-match-animal"),
                horizontalArrangement = Arrangement.spacedBy(BastiSpacing.medium), verticalAlignment = Alignment.CenterVertically) {
                animalImages[item.animal.id]?.let { image ->
                    Image(image, contentDescription = item.animal.name.display[language],
                        modifier = Modifier.sizeIn(minWidth = 88.dp, minHeight = 88.dp, maxWidth = 120.dp, maxHeight = 120.dp),
                        contentScale = ContentScale.Fit)
                }
                Text(item.animal.name.display[language], Modifier.weight(1f).testTag("habitat-match-animal-name"),
                    style = BastiTypography.answer, color = BastiPalette.ink)
            }
            question.choices.chunked(if (configuration.fontScale >= 1.8f) 1 else 2).forEach { row ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(BastiSpacing.small)) {
                    row.forEachIndexed { columnIndex, id ->
                        val habitat = HabitatMatchingContent.habitat(id) ?: return@forEachIndexed
                        val chosen = state.current.lastChoice == id
                        val selectedState = when {
                            !chosen -> ""
                            state.current.answer == AnswerState.CORRECT -> t("Selected, correct", "Ausgewählt, richtig")
                            state.current.answer == AnswerState.RETRY_AVAILABLE -> t("Selected, try again", "Ausgewählt, versuche es erneut")
                            else -> t("Selected", "Ausgewählt")
                        }
                        val description = habitat.label.display[language] + if (selectedState.isNotEmpty()) ", $selectedState" else ""
                        Surface(onClick = { state.nextAttempt?.let { onAction(SessionAction.Answer(it, id)) } },
                            enabled = ready && state.current.answer == AnswerState.UNANSWERED,
                            modifier = Modifier.weight(1f).heightIn(min = 126.dp)
                                .testTag("habitat-match-choice-${id.value}")
                                .semantics {
                                    role = Role.Button
                                    contentDescription = description
                                    if (selectedState.isNotEmpty()) stateDescription = selectedState
                                },
                            shape = RoundedCornerShape(18.dp), color = BastiPalette.surface,
                            border = BorderStroke(if (chosen) 4.dp else 1.dp,
                                if (chosen) BastiPalette.ink else BastiPalette.outline)) {
                            Column(Modifier.fillMaxWidth().padding(BastiSpacing.small),
                                horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                habitatImages[id]?.let { image ->
                                    Image(image, contentDescription = null, modifier = Modifier.fillMaxWidth().heightIn(min = 80.dp, max = 112.dp)
                                        .testTag("habitat-match-preview-${id.value}"), contentScale = ContentScale.Crop)
                                }
                                Text(habitat.label.display[language], Modifier.testTag("habitat-match-label-${id.value}"),
                                    style = BastiTypography.answer, color = BastiPalette.ink)
                            }
                        }
                    }
                    if (row.size == 1 && configuration.fontScale < 1.8f) Spacer(Modifier.weight(1f))
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(BastiSpacing.small)) {
                NativeActionButton(t("Listen again", "Noch einmal hören"), NativeActionRole.SECONDARY,
                    onReplay, Modifier.weight(1f).testTag("habitat-match-replay"), ready)
                NativeActionButton(t("Help", "Hilfe"), NativeActionRole.SECONDARY,
                    onHelp, Modifier.weight(1f).testTag("habitat-match-help"), ready && state.hintAvailable)
            }
            if (state.current.support.hint) question.hint?.let {
                NativeSupportMessage(it.display[language], Modifier.testTag("habitat-match-hint"))
            }
            when (state.current.answer) {
                AnswerState.RETRY_AVAILABLE -> {
                    NativeSupportMessage(question.wrongFeedback.display[language], Modifier.testTag("habitat-match-feedback-wrong"))
                    NativeActionButton(t("Try again", "Versuch es erneut"), NativeActionRole.PRIMARY,
                        { onAction(SessionAction.Retry(AttemptId(state.task.id, state.current.attempts))) },
                        Modifier.fillMaxWidth().testTag("habitat-match-retry"), ready)
                }
                AnswerState.CORRECT -> {
                    NativeSupportMessage(question.correctFeedback.display[language], Modifier.testTag("habitat-match-feedback-correct"))
                    NativeActionButton(t("Continue", "Weiter"), NativeActionRole.PRIMARY,
                        { onAction(SessionAction.Next(state.task.id)) }, Modifier.fillMaxWidth().testTag("habitat-match-continue"), ready)
                }
                else -> Unit
            }
        }
        if (saveFailed) {
            NativeSupportMessage(t("Your saved round is safe. Try again.", "Deine gespeicherte Runde ist sicher. Versuche es noch einmal."))
            NativeActionButton(t("Try again", "Erneut versuchen"), NativeActionRole.SECONDARY,
                onRetry, Modifier.testTag("habitat-match-save-retry"), !busy)
        }
        if (audioFailed) Text(t("Speech is unavailable. Check installed offline voices in Options.",
            "Die Sprachausgabe ist nicht verfügbar. Prüfe die Offline-Stimmen in den Optionen."))
        NativeActionButton(t("Home", "Startseite"), NativeActionRole.NAVIGATION, onHome,
            Modifier.fillMaxWidth().testTag("habitat-match-home"))
    }
}
