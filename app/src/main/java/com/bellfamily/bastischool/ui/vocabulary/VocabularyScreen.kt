package com.bellfamily.bastischool.ui.vocabulary

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import com.bellfamily.bastischool.ui.common.NativeActionButton
import com.bellfamily.bastischool.ui.common.NativeActionRole
import com.bellfamily.bastischool.ui.common.NativeCompletionScreen
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.session.*
import com.bellfamily.bastischool.learning.vocabulary.*

@Composable
fun VocabularyScreen(selection:VocabularySelection?,state:SessionState?,language:ContentLanguage,
    busy:Boolean,saveFailed:Boolean,audioFailed:Boolean,onSelect:(ContentId)->Unit,onPhase:(VocabularyPhase)->Unit,
    onReplay:()->Unit,onExample:()->Unit,onAction:(SessionAction)->Unit,onOption:(ContentId)->Unit,
    onAgain:()->Unit,onRetry:()->Unit,onHome:()->Unit,modifier:Modifier=Modifier,onPop:(String)->Unit={}) {
    fun t(en:String,de:String)=if(language==ContentLanguage.GERMAN)de else en
    val ready=!busy && !saveFailed && selection!=null
    if(selection != null && selection.phase != VocabularyPhase.EXPLORE && state?.phase == SessionPhase.COMPLETED) {
        NativeCompletionScreen(state.plan.id.value, language, state.plan.completionText.display[language],
            ready && state.language == language, "vocabulary-", onReplay, onAgain, onHome, onPop,
            modifier, saveFailed, onRetry, audioFailed) {
            VocabularyPhase.entries.forEach {phase ->
                OutlinedButton({onPhase(phase)}, enabled = ready, modifier = Modifier.heightIn(min = 56.dp).testTag("vocabulary-${phase.name.lowercase()}")) {
                    Text(when(phase) {VocabularyPhase.EXPLORE -> t("Learn words","Wörter kennenlernen"); VocabularyPhase.FIND -> t("Find the word","Finde das Wort"); VocabularyPhase.NAME -> t("What is it?","Was ist das?")})
                }
            }
        }
        return
    }
    Column(modifier.fillMaxSize().background(Color(0xFFEAF7FC)).verticalScroll(rememberScrollState()).padding(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
        Text(t("Vocabulary Booster","Wortschatz"),style=MaterialTheme.typography.headlineMedium)
        VocabularyPhase.entries.forEach { phase ->
            FilterChip(selection?.phase==phase,{onPhase(phase)},enabled=ready,
                label={Text(when(phase){VocabularyPhase.EXPLORE->t("Learn words","Wörter kennenlernen");VocabularyPhase.FIND->t("Find the word","Finde das Wort");VocabularyPhase.NAME->t("What is it?","Was ist das?")})},
                modifier=Modifier.fillMaxWidth().heightIn(min=56.dp).testTag("vocabulary-${phase.name.lowercase()}"))
        }
        if(saveFailed) {
            Text(t("Progress could not be saved or restored. Saved records are kept.","Der Fortschritt konnte nicht gespeichert oder wiederhergestellt werden. Gespeicherte Einträge bleiben erhalten."))
            NativeActionButton(t("Try again", "Erneut versuchen"), NativeActionRole.SECONDARY, onClick=onRetry,enabled=!busy,modifier=Modifier.heightIn(min=56.dp).testTag("vocabulary-load"))
        }
        if(audioFailed) Text(t("Speech is unavailable. Please check installed offline English and German voices in device settings.","Die Sprachausgabe ist nicht verfügbar. Bitte prüfe installierte Offline-Stimmen für Englisch und Deutsch in den Geräte-Einstellungen."))
        if(selection==null) Text(t("Opening…","Wird geöffnet…"))
        else if(selection.phase==VocabularyPhase.EXPLORE) {
            VocabularyContent.items.chunked(2).forEach { row ->
                Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {row.forEach { item ->
                    OutlinedButton({onSelect(item.id)},enabled=ready,modifier=Modifier.weight(1f).heightIn(min=56.dp).testTag("word-${item.id.value}")) {Text(item.text.display[language])}
                }}
            }
            Text(selection.item.text.display[language],style=MaterialTheme.typography.headlineMedium,modifier=Modifier.testTag("vocabulary-word"))
            AnimalPicture(selection.item,language,Modifier.fillMaxWidth())
            NativeActionButton(t("Listen again", "Noch einmal hören"), NativeActionRole.SECONDARY, onClick=onReplay,enabled=ready,modifier=Modifier.fillMaxWidth().heightIn(min=56.dp).testTag("vocabulary-replay"))
            Text(selection.item.example.display[language],style=MaterialTheme.typography.titleLarge)
            NativeActionButton(t("Listen to the sentence", "Satz anhören"), NativeActionRole.SECONDARY, onClick = onExample,enabled=ready,modifier=Modifier.heightIn(min=56.dp).testTag("vocabulary-example"))
            Text(t("Can you say the word in your own sentence? You can ask someone to help.","Kannst du mit dem Wort einen eigenen Satz sagen? Du kannst dir helfen lassen."))
        } else if(state!=null) {
            val canAct=ready && state.language==language
            Text(if(state.phase==SessionPhase.COMPLETED)t("Adventure complete!","Abenteuer geschafft!") else
                t("Question ${state.index+1} of ${state.plan.tasks.size}","Frage ${state.index+1} von ${state.plan.tasks.size}"),style=MaterialTheme.typography.headlineSmall)
            NativeActionButton(t("Listen again", "Noch einmal hören"), NativeActionRole.SECONDARY, onClick = onReplay,enabled=canAct,modifier=Modifier.fillMaxWidth().heightIn(min=56.dp).testTag("vocabulary-replay"))
            if(state.phase==SessionPhase.ACTIVE) {
                Text(state.task.question.instruction.display[language],style=MaterialTheme.typography.titleLarge)
                if(selection.phase==VocabularyPhase.NAME) AnimalPicture(VocabularyContent.item(state.task.question.correct),language,Modifier.fillMaxWidth())
                state.task.question.choices.chunked(2).forEach { row ->
                    Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {row.forEach { id ->
                        val item=VocabularyContent.item(id)
                        Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(4.dp)) {
                            Button({state.nextAttempt?.let {onAction(SessionAction.Answer(it,id))}},
                                enabled=canAct && state.current.answer==AnswerState.UNANSWERED,
                                modifier=Modifier.fillMaxWidth().heightIn(min=72.dp).testTag("answer-${id.value}")) {
                                if(selection.phase==VocabularyPhase.FIND) Column(horizontalAlignment=Alignment.CenterHorizontally) {
                                    AnimalPicture(item,language,Modifier.fillMaxWidth(),small=true)
                                    if(state.current.support.hint)Text(item.text.display[language])
                                }
                                else Text(item.text.display[language])
                            }
                            OutlinedButton({onOption(id)},enabled=canAct,modifier=Modifier.fillMaxWidth().heightIn(min=56.dp).testTag("speaker-${id.value}").semantics {
                                contentDescription=t("Listen: ","Anhören: ")+item.text.display[language]
                            }){Text(t("Listen","Hören"))}
                        }
                    }}
                }
                if(state.current.answer==AnswerState.CORRECT)Text(state.task.question.correctFeedback.display[language])
                if(state.current.answer==AnswerState.RETRY_AVAILABLE) {
                    Text(state.task.question.wrongFeedback.display[language])
                    NativeActionButton(t("Try again", "Nochmal versuchen"), NativeActionRole.SECONDARY, onClick = {onAction(SessionAction.Retry(AttemptId(state.task.id,state.current.attempts)))},enabled=canAct,modifier=Modifier.heightIn(min=56.dp).testTag("vocabulary-retry"))
                }
                if(state.current.support.hint)Text(state.task.question.hint!!.display[language])
                if(!state.current.locked)NativeActionButton(t("Help", "Hilfe"), NativeActionRole.SECONDARY, onClick = {onAction(SessionAction.Hint(state.task.id))},enabled=canAct,modifier=Modifier.heightIn(min=56.dp).testTag("vocabulary-help"))
                if(state.current.locked)NativeActionButton(t("Next", "Weiter"), NativeActionRole.PRIMARY, onClick = {onAction(SessionAction.Next(state.task.id))},enabled=canAct,modifier=Modifier.fillMaxWidth().heightIn(min=56.dp).testTag("vocabulary-next"))
            }
        }
        NativeActionButton(t("Back home", "Zurück zum Start"), NativeActionRole.NAVIGATION, onClick = onHome,modifier=Modifier.fillMaxWidth().heightIn(min=56.dp).testTag("vocabulary-home"))
    }
}

/** Temporary legacy glyph, deliberately isolated from authored speech and replaceable by reviewed art. */
@Composable
private fun AnimalPicture(item:VocabularyDefinition,language:ContentLanguage,modifier:Modifier,small:Boolean=false) {
    Box(modifier.heightIn(min=if(small)100.dp else 160.dp).semantics {contentDescription=item.text.display[language]},contentAlignment=Alignment.Center) {
        Text(item.visual.glyph,fontSize=if(small)48.sp else 88.sp,modifier=Modifier.clearAndSetSemantics {})
    }
}
