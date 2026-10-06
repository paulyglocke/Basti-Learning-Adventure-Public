package com.bellfamily.bastischool.ui.subitising

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.bellfamily.bastischool.learning.models.ContentLanguage
import com.bellfamily.bastischool.learning.session.*
import com.bellfamily.bastischool.learning.subitising.SubitisingContent
import com.bellfamily.bastischool.ui.common.*

@Composable
fun DotPattern(quantity: Int, language: ContentLanguage, modifier: Modifier = Modifier) {
    val dots=SubitisingContent.pattern(quantity)
    Canvas(modifier.size(224.dp).background(Color.White).testTag("subitising-dots").semantics {
        contentDescription=SubitisingContent.description(quantity).display[language]
    }) {
        dots.forEach { drawCircle(Color(0xFF172B3A),size.minDimension*.065f,Offset(size.width*it.x,size.height*it.y)) }
    }
}

@Composable
fun SubitisingScreen(state: SessionState?, language: ContentLanguage, busy: Boolean, saveFailed: Boolean, audioFailed: Boolean,
    onAction: (SessionAction)->Unit, onReplay: ()->Unit, onAgain: ()->Unit, onRetry: ()->Unit, onHome: ()->Unit,
    modifier: Modifier=Modifier, onPop: (String)->Unit={}) {
    fun t(en:String,de:String)=if(language==ContentLanguage.GERMAN)de else en
    val ready=state!=null && state.language==language && !busy && !saveFailed
    if(state?.phase==SessionPhase.COMPLETED) {
        NativeCompletionScreen(state.plan.id.value,language,state.plan.completionText.display[language],ready,
            "subitising-",onReplay,onAgain,onHome,onPop,modifier,saveFailed,onRetry,audioFailed)
        return
    }
    Column(modifier.fillMaxSize().background(Color(0xFFEAF7FC)).verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement=Arrangement.spacedBy(12.dp),horizontalAlignment=Alignment.CenterHorizontally) {
        NativeActivityTitle(t("How many?","Wie viele?"))
        if(state==null) Text(t("Opening…","Wird geöffnet…")) else {
            NativeQuestionProgress(state.index+1,state.plan.tasks.size,language)
            DotPattern(SubitisingContent.quantity(state.task.question.correct),language)
            NativeActionButton(t("Listen again","Noch einmal hören"),NativeActionRole.SECONDARY,onReplay,
                Modifier.fillMaxWidth().testTag("subitising-replay"),ready)
            state.task.question.choices.forEach {id ->
                NativeTextChoice(SubitisingContent.quantity(id).toString(),
                    {state.nextAttempt?.let {onAction(SessionAction.Answer(it,id))}},
                    Modifier.fillMaxWidth().testTag("subitising-answer-${SubitisingContent.quantity(id)}"),
                    ready && state.current.answer==AnswerState.UNANSWERED && state.nextAttempt!=null)
            }
            if(state.current.support.hint) NativeSupportMessage(SubitisingContent.help.display[language])
            if(!state.current.locked) NativeActionButton(t("Help","Hilfe"),NativeActionRole.SECONDARY,
                {onAction(SessionAction.Hint(state.task.id))},Modifier.testTag("subitising-help"),ready)
            when(state.current.answer) {
                AnswerState.RETRY_AVAILABLE -> {
                    NativeSupportMessage(state.task.question.wrongFeedback.display[language])
                    NativeActionButton(t("Try again","Nochmal versuchen"),NativeActionRole.SECONDARY,
                        {onAction(SessionAction.Retry(AttemptId(state.task.id,state.current.attempts)))},Modifier.testTag("subitising-retry"),ready)
                }
                AnswerState.CORRECT -> {
                    NativeSupportMessage(state.task.question.correctFeedback.display[language])
                    NativeActionButton(t("Next","Weiter"),NativeActionRole.PRIMARY,
                        {onAction(SessionAction.Next(state.task.id))},Modifier.fillMaxWidth().testTag("subitising-next"),ready)
                }
                else -> Unit
            }
        }
        if(saveFailed) {
            NativeSupportMessage(t("Your saved work is kept. Please try again.","Deine gespeicherte Arbeit bleibt erhalten. Versuche es erneut."))
            NativeActionButton(t("Try again","Erneut versuchen"),NativeActionRole.SECONDARY,onRetry,Modifier.testTag("subitising-save-retry"),!busy)
        }
        if(audioFailed) Text(t("Speech is unavailable. Check installed offline voices.","Die Sprachausgabe ist nicht verfügbar. Prüfe installierte Offline-Stimmen."))
        NativeActionButton(t("Back home","Zurück zum Start"),NativeActionRole.NAVIGATION,onHome,Modifier.fillMaxWidth().testTag("subitising-home"))
    }
}
