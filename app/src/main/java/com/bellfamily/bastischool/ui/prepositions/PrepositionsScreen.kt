package com.bellfamily.bastischool.ui.prepositions

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import com.bellfamily.bastischool.ui.common.NativeActionButton
import com.bellfamily.bastischool.ui.common.NativeActionRole
import com.bellfamily.bastischool.ui.common.NativeCompletionScreen
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.prepositions.*
import com.bellfamily.bastischool.learning.session.*

@Composable
fun PrepositionsScreen(state: SessionState?, language: ContentLanguage, busy: Boolean, saveFailed: Boolean,
                       audioFailed: Boolean, onAction: (SessionAction) -> Unit, onOption: (ContentId) -> Unit,
                       onRetrySave: () -> Unit, onAgain: () -> Unit, onIntroduction: () -> Unit,
                       onHome: () -> Unit, onLegacy: () -> Unit, modifier: Modifier = Modifier, onPop: (String) -> Unit = {}) {
    val de = language == ContentLanguage.GERMAN
    fun t(en: String, german: String) = if (de) german else en
    if(state?.phase == SessionPhase.COMPLETED) {
        NativeCompletionScreen(state.plan.id.value, language, state.plan.completionText.display[language],
            !busy && !saveFailed && state.language == language, "", {onAction(SessionAction.Replay(state.task.id))},
            onAgain, onHome, onPop, modifier, saveFailed, onRetrySave, audioFailed) {
            NativeActionButton(t("Use previous version", "Bisherige Version öffnen"), NativeActionRole.NAVIGATION, onClick = onLegacy, modifier = Modifier.heightIn(min = 48.dp).testTag("legacy"))
        }
        return
    }
    Column(modifier.fillMaxSize().background(Color(0xFFEAF7FC)).verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (saveFailed) {
            Text(t("Progress could not be saved. Please try again. Your saved progress is kept.",
                "Der Fortschritt konnte nicht gespeichert werden. Bitte versuche es erneut. Gespeicherter Fortschritt bleibt erhalten."))
            NativeActionButton(t("Try saving again", "Speichern erneut versuchen"), NativeActionRole.SECONDARY, onClick = onRetrySave, enabled = !busy, modifier = Modifier.testTag("retry-save"))
        }
        if (audioFailed) Text(t("Speech is unavailable. Check installed offline voices in Options. You can still use the pictures.",
            "Die Sprachausgabe ist nicht verfügbar. Prüfe die installierten Offline-Stimmen in den Optionen. Du kannst die Bilder weiter nutzen."))
        if (state == null) Text(if (busy) t("Opening…", "Wird geöffnet…") else t("This round could not be opened. You can return Home or use the previous version.", "Diese Runde konnte nicht geöffnet werden. Du kannst zum Start oder zur bisherigen Version zurückkehren."))
        else {
            val current = state.current
            val task = state.task
            val ready = !busy && !saveFailed && state.language == language
            Text(if (state.phase == SessionPhase.COMPLETED) t("Adventure complete!", "Abenteuer geschafft!")
                else t("Question ${state.index + 1} of ${state.plan.tasks.size}", "Frage ${state.index + 1} von ${state.plan.tasks.size}"),
                style = MaterialTheme.typography.headlineSmall, modifier = Modifier.testTag("progress"))
            NativeActionButton(t("Listen again", "Noch einmal hören"), NativeActionRole.SECONDARY, onClick = { onAction(SessionAction.Replay(task.id)) }, enabled = ready,
                modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp).testTag("replay"))
            if (state.phase == SessionPhase.ACTIVE) {
                Text(task.question.instruction.display[language], style = MaterialTheme.typography.titleLarge)
                BoxWithConstraints {
                    if (maxWidth >= 640.dp) Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        PositionSceneImage(PrepositionsContent.scene(task), language, Modifier.weight(1f))
                        Answers(state, language, ready, onAction, onOption, Modifier.weight(1f))
                    } else Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        PositionSceneImage(PrepositionsContent.scene(task), language, Modifier.fillMaxWidth())
                        Answers(state, language, ready, onAction, onOption, Modifier.fillMaxWidth())
                    }
                }
                if (current.answer == AnswerState.CORRECT) Text(task.question.correctFeedback.display[language], modifier = Modifier.testTag("feedback"))
                else if (current.answer == AnswerState.RETRY_AVAILABLE) Text(task.question.wrongFeedback.display[language], modifier = Modifier.testTag("feedback"))
                if (current.support.hint) Text(task.question.hint!!.display[language], modifier = Modifier.testTag("hint-text"))
                if (!current.locked) NativeActionButton(t("Help", "Hilfe"), NativeActionRole.SECONDARY, onClick = { onAction(SessionAction.Hint(task.id)) }, enabled = ready,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp).testTag("hint"))
                if (current.answer == AnswerState.RETRY_AVAILABLE) NativeActionButton(t("Try again", "Nochmal versuchen"), NativeActionRole.SECONDARY,
                    onClick = { onAction(SessionAction.Retry(AttemptId(task.id, current.attempts))) }, enabled = ready,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp).testTag("retry"))
                if (current.locked) NativeActionButton(t("Next", "Weiter"), NativeActionRole.PRIMARY, onClick = { onAction(SessionAction.Next(task.id)) }, enabled = ready,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp).testTag("next"))
                NativeActionButton(t("How to play", "So geht’s"), NativeActionRole.SECONDARY, onClick = onIntroduction, enabled = ready)
            }
        }
        NativeActionButton(t("Back home", "Zurück zum Start"), NativeActionRole.NAVIGATION, onClick = onHome, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp).testTag("home"))
        NativeActionButton(t("Use previous version", "Bisherige Version öffnen"), NativeActionRole.NAVIGATION, onClick = onLegacy, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("legacy"))
    }
}

@Composable
private fun Answers(state: SessionState, language: ContentLanguage, ready: Boolean,
                    action: (SessionAction) -> Unit, option: (ContentId) -> Unit, modifier: Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        state.task.question.choices.forEach { choice ->
            val label = PrepositionsContent.repository.find(choice)!!.text.display[language]
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { state.nextAttempt?.let { action(SessionAction.Answer(it, choice)) } },
                    enabled = ready && state.current.answer == AnswerState.UNANSWERED,
                    modifier = Modifier.weight(1f).heightIn(min = 64.dp).testTag("answer-${choice.value}")) { Text(label) }
                OutlinedButton(onClick = { option(choice) }, enabled = ready,
                    modifier = Modifier.heightIn(min = 64.dp).widthIn(min = 64.dp).testTag("speaker-${choice.value}")
                        .semantics { contentDescription = if (language == ContentLanguage.GERMAN) "Anhören: $label" else "Listen: $label" }) {
                    Text(if (language == ContentLanguage.GERMAN) "Hören" else "Listen")
                }
            }
        }
    }
}

/** Temporary animal glyphs preserve legacy imagery; relation objects/layers are native drawing. */
@Composable
private fun PositionSceneImage(scene: PositionScene, language: ContentLanguage, modifier: Modifier) {
    val geometry = PositionGeometry.forRelation(scene.relation)
    val paint = remember { Paint(Paint.ANTI_ALIAS_FLAG).apply { textAlign = Paint.Align.CENTER } }
    Canvas(modifier.aspectRatio(1.6f).testTag("position-scene").semantics { contentDescription = scene.description[language] }) {
        scale(size.width / 320f, size.height / 200f, pivot = Offset.Zero) {
            drawRect(Color(0xFFD6F2FF), size = Size(320f, 200f))
            drawRect(Color(0xFFB7DC8A), Offset(0f, 170f), Size(320f, 30f))
            fun animal() {
                val r = geometry.animal
                paint.textSize = r.height
                val glyph = when(scene.animal) { PositionAnimal.SNAKE -> "🐍"; PositionAnimal.DINOSAUR -> "🦖"; PositionAnimal.DRAGON -> "🐉"; PositionAnimal.CROCODILE -> "🐊" }
                drawContext.canvas.nativeCanvas.drawText(glyph, r.x + r.width / 2, r.bottom - paint.fontMetrics.descent, paint)
            }
            if (geometry.animalBehind) animal()
            geometry.objects.forEach { r ->
                val colour = if (scene.relation.reference == ReferenceObject.ROCK) Color(0xFF788998) else Color(0xFFAD743F)
                drawRoundRect(colour, Offset(r.x, r.y), Size(r.width, r.height), androidx.compose.ui.geometry.CornerRadius(8f))
                if (scene.relation.reference == ReferenceObject.TABLE) {
                    drawRect(colour, Offset(r.x + 4, r.bottom), Size(10f, 72f))
                    drawRect(colour, Offset(r.right - 14, r.bottom), Size(10f, 72f))
                }
                if (scene.relation.reference == ReferenceObject.BOX) drawRect(Color(0xFF604126), Offset(r.x + 6, r.y + 6), Size(r.width - 12, 40f))
            }
            if (!geometry.animalBehind) animal()
            geometry.boxFront?.let { r -> drawRect(Color(0xFFBD8551), Offset(r.x, r.y), Size(r.width, r.height)) }
        }
    }
}
