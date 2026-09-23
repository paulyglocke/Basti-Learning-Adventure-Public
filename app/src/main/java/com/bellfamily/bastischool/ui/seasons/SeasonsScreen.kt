package com.bellfamily.bastischool.ui.seasons

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import com.bellfamily.bastischool.ui.common.NativeCompletionScreen
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.bellfamily.bastischool.learning.content.SeasonIds
import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.seasons.*
import com.bellfamily.bastischool.learning.session.*

@Composable
fun DaysSeasonsHub(language: ContentLanguage, onSeasons: () -> Unit, onWilma: () -> Unit, onLegacy: () -> Unit, modifier: Modifier = Modifier) {
    val de = language == ContentLanguage.GERMAN
    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(if(de) "Tage & Jahreszeiten" else "Days & Seasons", style = MaterialTheme.typography.headlineMedium)
        Button(onClick=onWilma, modifier=Modifier.fillMaxWidth().heightIn(min=72.dp).testTag("open-wilma")) {
            Text(if(de) "Wilmas Woche" else "Wilma’s Week")
        }
        Button(onClick=onSeasons, modifier=Modifier.fillMaxWidth().heightIn(min=72.dp).testTag("open-seasons")) {
            Text(if(de) "Jahreszeiten – Lernen und Üben" else "Seasons – Learn and practise")
        }
        OutlinedButton(onClick=onLegacy, modifier=Modifier.fillMaxWidth().heightIn(min=72.dp).testTag("legacy-calendar")) {
            Text(if(de) "Tage & Jahreszeiten – bisherige Version" else "Days & Seasons – previous version")
        }
    }
}

@Composable
fun SeasonsScreen(selection: SeasonsSelection?, state: SessionState?, language: ContentLanguage, artwork: ImageBitmap?,
                  busy: Boolean, saveFailed: Boolean, imageFailed: Boolean, audioFailed: Boolean,
                  onSelect: (ContentId) -> Unit, onPhase: (SeasonsPhase) -> Unit, onReplay: () -> Unit,
                  onAction: (SessionAction) -> Unit, onOption: (ContentId) -> Unit, onAgain: () -> Unit,
                  onRetry: () -> Unit, onHome: () -> Unit, modifier: Modifier = Modifier, onPop: (String) -> Unit = {}) {
    val de=language==ContentLanguage.GERMAN
    fun t(en:String,german:String)=if(de)german else en
    val ready=!busy && !saveFailed && selection!=null
    if(selection?.phase == SeasonsPhase.PRACTICE && state?.phase == SessionPhase.COMPLETED) {
        NativeCompletionScreen(state.plan.id.value, language, state.plan.completionText.display[language],
            ready && state.language == language, "seasons-", onReplay, onAgain, onHome, onPop,
            modifier, saveFailed, onRetry, audioFailed) {
            OutlinedButton(onClick = {onPhase(SeasonsPhase.EXPLORE)}, enabled = ready, modifier = Modifier.heightIn(min = 56.dp).testTag("learn")) {Text(t("Learn", "Lernen"))}
        }
        return
    }
    Column(modifier.fillMaxSize().background(Color(0xFFEAF7FC)).verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement=Arrangement.spacedBy(12.dp)) {
        Text(t("Seasons", "Jahreszeiten"),style=MaterialTheme.typography.headlineMedium)
        Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
            FilterChip(selected=selection?.phase==SeasonsPhase.EXPLORE,onClick={onPhase(SeasonsPhase.EXPLORE)},enabled=ready,
                label={Text(t("Learn","Lernen"))},modifier=Modifier.testTag("learn"))
            FilterChip(selected=selection?.phase==SeasonsPhase.PRACTICE,onClick={onPhase(SeasonsPhase.PRACTICE)},enabled=ready,
                label={Text(t("Practise","Üben"))},modifier=Modifier.testTag("practice"))
        }
        if(saveFailed) Text(t("Progress could not be saved or restored. Please try again. Saved records are kept.",
            "Der Fortschritt konnte nicht gespeichert oder wiederhergestellt werden. Bitte versuche es erneut. Gespeicherte Einträge bleiben erhalten."))
        if(imageFailed) Text(t("The picture could not be opened. Please try again.","Das Bild konnte nicht geöffnet werden. Bitte versuche es erneut."))
        if(saveFailed || imageFailed) Button(onClick=onRetry,enabled=!busy,modifier=Modifier.testTag("retry-load")) {Text(t("Try again","Erneut versuchen"))}
        if(audioFailed) Text(t("Speech is unavailable. Please check installed offline English and German voices in device settings.",
            "Die Sprachausgabe ist nicht verfügbar. Bitte prüfe installierte Offline-Stimmen für Englisch und Deutsch in den Geräte-Einstellungen."))
        if(selection==null) Text(t("Opening…","Wird geöffnet…"))
        else if(selection.phase==SeasonsPhase.EXPLORE) {
            SeasonIds.canonicalOrder.chunked(2).forEach { row ->
                Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) { row.forEach { id ->
                    OutlinedButton(onClick={onSelect(id)},enabled=ready,
                        modifier=Modifier.weight(1f).heightIn(min=56.dp).testTag("season-${id.value}")) {
                        Text(SeasonsContent.season(id).text.display[language])
                    }
                } }
            }
            Text(selection.season.text.display[language],style=MaterialTheme.typography.headlineSmall,modifier=Modifier.testTag("season-name"))
            Button(onClick=onReplay,enabled=ready,modifier=Modifier.fillMaxWidth().heightIn(min=56.dp).testTag("seasons-replay")) {Text(t("Listen again","Noch einmal hören"))}
            BoxWithConstraints {
                if(maxWidth>=700.dp) Row(horizontalArrangement=Arrangement.spacedBy(16.dp)) {
                    SeasonPicture(artwork,selection.season.text.display[language],Modifier.weight(1.4f))
                    Text(selection.season.spokenDescription[language],modifier=Modifier.weight(1f).testTag("season-description"))
                } else Column(verticalArrangement=Arrangement.spacedBy(12.dp)) {
                    SeasonPicture(artwork,selection.season.text.display[language],Modifier.fillMaxWidth())
                    Text(selection.season.spokenDescription[language],modifier=Modifier.testTag("season-description"))
                }
            }
        } else if(state!=null) {
            val task=state.task
            val canAct=ready && state.language==language
            Text(if(state.phase==SessionPhase.COMPLETED)t("Adventure complete!","Abenteuer geschafft!")
                else t("Question ${state.index+1} of ${state.plan.tasks.size}","Frage ${state.index+1} von ${state.plan.tasks.size}"),
                style=MaterialTheme.typography.headlineSmall,modifier=Modifier.testTag("seasons-progress"))
            Button(onClick=onReplay,enabled=canAct,modifier=Modifier.fillMaxWidth().heightIn(min=56.dp).testTag("seasons-replay")) {Text(t("Listen again","Noch einmal hören"))}
            if(state.phase==SessionPhase.ACTIVE) {
                Text(task.question.instruction.display[language],style=MaterialTheme.typography.titleLarge)
                BoxWithConstraints {
                    if(maxWidth>=700.dp) Row(horizontalArrangement=Arrangement.spacedBy(16.dp)) {
                        SeasonPicture(artwork,SeasonsContent.season(task.question.correct).spokenDescription[language],Modifier.weight(1.4f))
                        SeasonAnswers(state,language,canAct,onAction,onOption,imageFailed,Modifier.weight(1f))
                    } else Column(verticalArrangement=Arrangement.spacedBy(12.dp)) {
                        SeasonPicture(artwork,SeasonsContent.season(task.question.correct).spokenDescription[language],Modifier.fillMaxWidth())
                        SeasonAnswers(state,language,canAct,onAction,onOption,imageFailed,Modifier.fillMaxWidth())
                    }
                }
                if(state.current.answer==AnswerState.CORRECT) Text(task.question.correctFeedback.display[language])
                if(state.current.answer==AnswerState.RETRY_AVAILABLE) {
                    Text(task.question.wrongFeedback.display[language])
                    Button(onClick={onAction(SessionAction.Retry(AttemptId(task.id,state.current.attempts)))},enabled=canAct,modifier=Modifier.testTag("seasons-retry")) {Text(t("Try again","Nochmal versuchen"))}
                }
                if(state.current.support.hint) Text(task.question.hint!!.display[language])
                if(!state.current.locked) OutlinedButton(onClick={onAction(SessionAction.Hint(task.id))},enabled=canAct,modifier=Modifier.heightIn(min=56.dp).testTag("seasons-hint")) {Text(t("Help","Hilfe"))}
                if(state.current.locked) Button(onClick={onAction(SessionAction.Next(task.id))},enabled=canAct,modifier=Modifier.fillMaxWidth().heightIn(min=56.dp).testTag("seasons-next")) {Text(t("Next","Weiter"))}
            }
        }
        Button(onClick=onHome,modifier=Modifier.fillMaxWidth().heightIn(min=56.dp).testTag("seasons-home")) {Text(t("Back home","Zurück zum Start"))}
    }
}

@Composable
private fun SeasonAnswers(state:SessionState,language:ContentLanguage,ready:Boolean,action:(SessionAction)->Unit,
                          option:(ContentId)->Unit,imageFailed:Boolean,modifier:Modifier) {
    Column(modifier,verticalArrangement=Arrangement.spacedBy(8.dp)) {
        state.task.question.choices.forEach { id ->
            val label=SeasonsContent.season(id).text.display[language]
            Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                Button(onClick={state.nextAttempt?.let {action(SessionAction.Answer(it,id))}},
                    enabled=ready && !imageFailed && state.current.answer==AnswerState.UNANSWERED,
                    modifier=Modifier.weight(1f).heightIn(min=64.dp).testTag("answer-${id.value}")) {Text(label)}
                OutlinedButton(onClick={option(id)},enabled=ready,
                    modifier=Modifier.heightIn(min=64.dp).testTag("speaker-${id.value}").semantics {
                        contentDescription=if(language==ContentLanguage.GERMAN)"Anhören: $label" else "Listen: $label"
                    }) {Text(if(language==ContentLanguage.GERMAN)"Hören" else "Listen")}
            }
        }
    }
}

@Composable
private fun SeasonPicture(bitmap:ImageBitmap?,description:String,modifier:Modifier) {
    if(bitmap!=null) Image(bitmap,description,modifier.aspectRatio(bitmap.width.toFloat()/bitmap.height).testTag("season-artwork"),contentScale=ContentScale.Fit)
    else Box(modifier.aspectRatio(4f/3f))
}
