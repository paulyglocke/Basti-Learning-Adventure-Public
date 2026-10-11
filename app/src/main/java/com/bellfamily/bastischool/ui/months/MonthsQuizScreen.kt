package com.bellfamily.bastischool.ui.months

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.months.*
import com.bellfamily.bastischool.learning.session.*
import com.bellfamily.bastischool.ui.common.*

@Composable
fun MonthsQuizScreen(mode: MonthsMode, state: SessionState?, language: ContentLanguage, busy: Boolean,
    saveFailed: Boolean, audioFailed: Boolean, onAction: (SessionAction) -> Unit, onReplay: () -> Unit,
    onOption: (ContentId) -> Unit, onAgain: () -> Unit, onRetrySave: () -> Unit, onExplore: () -> Unit,
    onHome: () -> Unit, modifier: Modifier = Modifier, onPop: (String) -> Unit = {}) {
    fun t(en: String, de: String) = if(language == ContentLanguage.GERMAN) de else en
    val ready = state != null && state.language == language && !busy && !saveFailed
    if(state?.phase == SessionPhase.COMPLETED) {
        NativeCompletionScreen(state.plan.id.value,language,state.plan.completionText.display[language],ready,
            "months-quiz-",onReplay,onAgain,onHome,onPop,modifier,saveFailed,onRetrySave,audioFailed,
            moreActions = { NativeActionButton(t("Explore months", "Monate entdecken"),NativeActionRole.NAVIGATION,onExplore,
                Modifier.fillMaxWidth().testTag("months-quiz-explore"),ready) })
        return
    }
    Column(modifier.fillMaxSize().background(Color(0xFFF8F5FC)).verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)) {
        NativeActivityTitle(mode.title.display[language])
        if(state == null) { if(!saveFailed) Text(t("Opening…", "Wird geöffnet…")) } else {
            val q = state.task.question
            NativeQuestionProgress(state.index+1,state.plan.tasks.size,language,Modifier.testTag("months-quiz-progress"))
            NativeSupportMessage(q.instruction.display[language],Modifier.testTag("months-quiz-prompt"))
            NativeActionButton(t("Listen again", "Noch einmal hören"),NativeActionRole.SECONDARY,onReplay,
                Modifier.fillMaxWidth().testTag("months-quiz-replay"),ready)
            q.choices.forEach { id ->
                val name = MonthContent.month(id).text.display[language]
                NativeTextChoice(name,{state.nextAttempt?.let {onAction(SessionAction.Answer(it,id))}},
                    Modifier.fillMaxWidth().testTag("months-answer-${id.value}").semantics { selected = state.current.lastChoice == id }
                        .then(if (state.current.lastChoice == id) Modifier.border(2.dp, Color(0xFF23354A), RoundedCornerShape(16.dp)) else Modifier),
                    ready && state.current.answer == AnswerState.UNANSWERED)
                NativeActionButton(t("Listen: $name", "Anhören: $name"),NativeActionRole.SECONDARY,{onOption(id)},
                    Modifier.fillMaxWidth().testTag("months-listen-${id.value}"),ready)
            }
            if(state.current.support.hint) NativeSupportMessage(q.hint!!.display[language],Modifier.testTag("months-quiz-hint"))
            if(!state.current.locked) NativeActionButton(t("Hint", "Tipp"),NativeActionRole.SECONDARY,
                {onAction(SessionAction.Hint(state.task.id))},Modifier.testTag("months-quiz-help"),ready)
            when(state.current.answer) {
                AnswerState.RETRY_AVAILABLE -> {
                    NativeSupportMessage(q.wrongFeedback.display[language])
                    NativeActionButton(t("Try again", "Nochmal versuchen"),NativeActionRole.SECONDARY,
                        {onAction(SessionAction.Retry(AttemptId(state.task.id,state.current.attempts)))},Modifier.testTag("months-quiz-retry"),ready)
                }
                AnswerState.CORRECT -> {
                    NativeSupportMessage(q.correctFeedback.display[language])
                    NativeActionButton(t("Next", "Weiter"),NativeActionRole.PRIMARY,{onAction(SessionAction.Next(state.task.id))},
                        Modifier.fillMaxWidth().testTag("months-quiz-next"),ready)
                }
                else -> Unit
            }
        }
        // Explicit model/help navigation marks support before the wheel can be exposed.
        NativeActionButton(t("Learn with the year wheel", "Mit dem Jahresrad lernen"),NativeActionRole.NAVIGATION,onExplore,
            Modifier.fillMaxWidth().testTag("months-quiz-explore"),ready)
        if(saveFailed) {
            NativeSupportMessage(t("Your saved work is kept. Please try saving again.", "Deine gespeicherte Arbeit bleibt erhalten. Versuche erneut zu speichern."))
            NativeActionButton(t("Try again", "Erneut versuchen"),NativeActionRole.SECONDARY,onRetrySave,
                Modifier.testTag("months-quiz-save-retry"),!busy)
        }
        if(audioFailed) NativeSupportMessage(t("Speech is unavailable. Check installed offline voices in Options.",
            "Die Sprachausgabe ist nicht verfügbar. Prüfe die Offline-Stimmen in den Optionen."))
        NativeActionButton(t("Back home", "Zurück zum Start"),NativeActionRole.NAVIGATION,onHome,
            Modifier.fillMaxWidth().testTag("months-quiz-home"))
    }
}
