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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
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
import com.bellfamily.bastischool.learning.schoolskills.ClassroomHelperContent
import com.bellfamily.bastischool.learning.session.*
import com.bellfamily.bastischool.ui.common.*

@Composable
fun ClassroomHelperRoute(
    viewModel: FollowViewModel, languageCode: String, modifier: Modifier,
    onAgain: () -> Unit, onHome: () -> Unit, onLevels: () -> Unit, onPop: (String) -> Unit
) {
    ClassroomHelperScreen(viewModel.classroomHelperState,
        if (languageCode == "de") ContentLanguage.GERMAN else ContentLanguage.ENGLISH,
        viewModel.classroomBoardImages, viewModel.busy, viewModel.saveFailed, viewModel.audioFailed,
        viewModel::classroomHelperAction, viewModel::classroomHelperReplay, onAgain, viewModel::retrySave,
        onHome, onLevels, modifier, onPop)
}

@Composable
fun ClassroomHelperScreen(
    state: SessionState?, language: ContentLanguage, images: Map<ContentId, ImageBitmap>, busy: Boolean,
    saveFailed: Boolean, audioFailed: Boolean, onAction: (SessionAction) -> Unit,
    onReplay: () -> Unit, onAgain: () -> Unit, onRetrySave: () -> Unit,
    onHome: () -> Unit, onLevels: () -> Unit, modifier: Modifier = Modifier,
    onPop: (String) -> Unit = {}
) {
    fun t(en: String, de: String) = if (language == ContentLanguage.GERMAN) de else en
    val taskKey = state?.task?.question?.definition?.value ?: "opening"
    var selectedItem by rememberSaveable(taskKey) { mutableStateOf<String?>(null) }
    var itemMismatch by rememberSaveable(taskKey) { mutableStateOf(false) }
    val ready = !busy && !saveFailed && state != null && state.language == language
    if (state?.phase == SessionPhase.COMPLETED) {
        NativeCompletionScreen(state.plan.id.value, language, state.plan.completionText.display[language], ready,
            "classroom-helper-", onReplay, onAgain, onHome, onPop, modifier, saveFailed, onRetrySave, audioFailed)
        return
    }
    Column(modifier.fillMaxSize().background(Color(0xFFEAF7FC)).verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        NativeActivityTitle(t("Classroom Helper", "Klassenzimmer-Hilfe"))
        NativeSupportMessage(t("Listen, pick up the named item, then tap where it belongs.",
            "Hör gut zu, wähle den genannten Gegenstand und tippe dann auf seinen Platz."))
        if (state == null) {
            if (!saveFailed) Text(t("Opening…", "Wird geöffnet…"))
        } else {
            val prompt = ClassroomHelperContent.promptFor(state.task.question.definition)
            Text(t("Question ${state.index + 1} of ${state.plan.tasks.size}", "Frage ${state.index + 1} von ${state.plan.tasks.size}"),
                style = MaterialTheme.typography.titleMedium, modifier = Modifier.testTag("classroom-helper-progress"))
            Text(state.task.question.instruction.display[language], Modifier.fillMaxWidth().testTag("classroom-helper-prompt"),
                style = MaterialTheme.typography.titleLarge)
            NativeActionButton(t("Listen again", "Noch einmal hören"), NativeActionRole.SECONDARY, onReplay,
                Modifier.fillMaxWidth().testTag("classroom-helper-replay"), ready)
            if (state.hintAvailable) NativeActionButton(t("Hint", "Tipp"), NativeActionRole.SECONDARY,
                { onAction(SessionAction.Hint(state.task.id)) }, Modifier.fillMaxWidth().testTag("classroom-helper-hint"), ready)
            if (state.current.support.hint)
                NativeSupportMessage(state.task.question.hint!!.display[language], Modifier.testTag("classroom-helper-hint-message"))

            when (state.current.answer) {
                AnswerState.UNANSWERED -> {
                    if (selectedItem == prompt.item.id.value) {
                        val item = prompt.item
                        Row(Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(14.dp)).padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            images[item.id]?.let { Image(it, null, Modifier.size(52.dp).clip(RoundedCornerShape(10.dp))) }
                            Text(t("Picked ${item.label.display.en}. Now choose where it goes.", "${item.label.display.de} ausgewählt. Wähle jetzt den richtigen Platz."),
                                style = MaterialTheme.typography.titleMedium)
                        }
                        Text(t("Choose a place", "Wähle einen Platz"), style = MaterialTheme.typography.titleMedium)
                        ClassroomBoardGrid(state.task.question.choices.map { id ->
                            val place = ClassroomHelperContent.places.first { it.id == id }
                            id to place.label.display[language]
                        }, images,
                            ready, "classroom-helper-place", Modifier.testTag("classroom-helper-places")) { placeId ->
                            state.nextAttempt?.let { onAction(SessionAction.Answer(it, placeId)) }
                        }
                    } else {
                        Text(t("Tap the item named in the instruction.", "Tippe auf den Gegenstand aus der Anweisung."),
                            style = MaterialTheme.typography.titleMedium)
                        if (itemMismatch) NativeSupportMessage(t("Try the item named in the instruction.", "Wähle den Gegenstand aus der Anweisung."),
                            Modifier.testTag("classroom-helper-item-feedback"))
                        ClassroomBoardGrid(ClassroomHelperContent.items.map { it.id to it.label.display[language] }, images,
                            ready, "classroom-helper-item", Modifier.testTag("classroom-helper-items")) { itemId ->
                            if (itemId == prompt.item.id) { selectedItem = itemId.value; itemMismatch = false }
                            else { selectedItem = null; itemMismatch = true }
                        }
                    }
                }
                AnswerState.RETRY_AVAILABLE -> {
                    NativeSupportMessage(state.task.question.wrongFeedback.display[language], Modifier.testTag("classroom-helper-feedback"))
                    NativeActionButton(t("Try again", "Nochmal versuchen"), NativeActionRole.SECONDARY, {
                        selectedItem = null
                        itemMismatch = false
                        onAction(SessionAction.Retry(AttemptId(state.task.id, state.current.attempts)))
                    }, Modifier.fillMaxWidth().testTag("classroom-helper-retry"), ready)
                }
                AnswerState.CORRECT -> {
                    NativeSupportMessage(state.task.question.correctFeedback.display[language], Modifier.testTag("classroom-helper-feedback"))
                    Row(Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(16.dp)).padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        images[prompt.item.id]?.let { Image(it, prompt.item.label.display[language], Modifier.size(72.dp).clip(RoundedCornerShape(12.dp))) }
                        Text("→", style = MaterialTheme.typography.headlineMedium)
                        images[prompt.place.id]?.let { Image(it, prompt.place.label.display[language], Modifier.size(96.dp).clip(RoundedCornerShape(12.dp))) }
                    }
                    NativeActionButton(t(if (state.index == state.plan.tasks.lastIndex) "Finish" else "Next",
                        if (state.index == state.plan.tasks.lastIndex) "Fertig" else "Weiter"), NativeActionRole.PRIMARY, {
                        selectedItem = null
                        itemMismatch = false
                        onAction(SessionAction.Next(state.task.id))
                    }, Modifier.fillMaxWidth().testTag("classroom-helper-next"), ready)
                }
                else -> Unit
            }
        }
        if (saveFailed) {
            NativeSupportMessage(t("Your saved work is kept. Please try again.", "Deine gespeicherte Arbeit bleibt erhalten. Versuch es erneut."))
            NativeActionButton(t("Try again", "Erneut versuchen"), NativeActionRole.SECONDARY, onRetrySave,
                Modifier.testTag("classroom-helper-save-retry"), !busy)
        }
        if (audioFailed) NativeSupportMessage(t("Speech is unavailable. Check installed offline voices in Options.",
            "Die Sprachausgabe ist nicht verfügbar. Prüfe die Offline-Stimmen in den Optionen."))
        NativeActionButton(t("Home", "Startseite"), NativeActionRole.NAVIGATION, onHome,
            Modifier.fillMaxWidth().testTag("classroom-helper-home"))
        NativeActionButton(t("Choose an activity", "Übung wählen"), NativeActionRole.NAVIGATION, onLevels,
            Modifier.fillMaxWidth().testTag("classroom-helper-levels"))
    }
}

@Composable
private fun ClassroomBoardGrid(
    entries: List<Pair<ContentId, String>>, images: Map<ContentId, ImageBitmap>, enabled: Boolean,
    tag: String, modifier: Modifier = Modifier, onSelect: (ContentId) -> Unit
) {
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        entries.chunked(2).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { (id, label) ->
                    val shape = RoundedCornerShape(16.dp)
                    Column(Modifier.weight(1f).clip(shape).background(Color.White)
                        .border(2.dp, Color(0xFFB6D9E5), shape).clickable(enabled, role = Role.Button) { onSelect(id) }
                        .semantics(mergeDescendants = true) { contentDescription = label }
                        .padding(8.dp).testTag("$tag-${id.value}"),
                        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        images[id]?.let { Image(it, null, Modifier.fillMaxWidth().height(96.dp).clip(RoundedCornerShape(10.dp)),
                            contentScale = ContentScale.Crop) }
                        Text(label, style = MaterialTheme.typography.titleSmall)
                    }
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}
