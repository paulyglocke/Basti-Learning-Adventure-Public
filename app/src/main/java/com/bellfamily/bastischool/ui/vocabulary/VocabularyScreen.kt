package com.bellfamily.bastischool.ui.vocabulary

import com.bellfamily.bastischool.ui.common.NativeChoiceRows
import com.bellfamily.bastischool.ui.common.NativeActivityTitle
import com.bellfamily.bastischool.ui.common.NativeQuestionProgress

import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import com.bellfamily.bastischool.ui.common.NativeTextChoice
import com.bellfamily.bastischool.ui.common.NativeActionButton
import com.bellfamily.bastischool.ui.common.NativeSupportMessage
import com.bellfamily.bastischool.ui.common.NativeActionRole
import com.bellfamily.bastischool.ui.common.NativeCompletionScreen
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.focusable
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.session.*
import com.bellfamily.bastischool.learning.vocabulary.*
import com.bellfamily.bastischool.ui.design.BastiPalette

@Composable
fun VocabularyScreen(selection:VocabularySelection?,state:SessionState?,language:ContentLanguage,
    busy:Boolean,saveFailed:Boolean,audioFailed:Boolean,onSelect:(ContentId)->Unit,onPhase:(VocabularyPhase)->Unit,
    onReplay:()->Unit,onExample:()->Unit,onAction:(SessionAction)->Unit,onOption:(ContentId)->Unit,
    onAgain:()->Unit,onRetry:()->Unit,onHome:()->Unit,modifier:Modifier=Modifier,onPop:(String)->Unit={},
    images:Map<ContentId,ImageBitmap> = emptyMap(),onChooseActivity:()->Unit={}) {
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
    Column(modifier.fillMaxSize().background(BastiPalette.background).verticalScroll(rememberScrollState()).padding(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
        NativeActivityTitle(t("Vocabulary Booster","Wortschatz"))
        if(saveFailed) {
            Text(t("Progress could not be saved or restored. Saved records are kept.","Der Fortschritt konnte nicht gespeichert oder wiederhergestellt werden. Gespeicherte Einträge bleiben erhalten."))
            NativeActionButton(t("Try again", "Erneut versuchen"), NativeActionRole.SECONDARY, onClick=onRetry,enabled=!busy,modifier=Modifier.heightIn(min=56.dp).testTag("vocabulary-load"))
        }
        if(audioFailed) Text(t("Speech is unavailable. Please check installed offline English and German voices in device settings.","Die Sprachausgabe ist nicht verfügbar. Bitte prüfe installierte Offline-Stimmen für Englisch und Deutsch in den Geräte-Einstellungen."))
        if(selection==null) Text(t("Opening…","Wird geöffnet…"))
        else if(selection.phase==VocabularyPhase.EXPLORE) {
            BoxWithConstraints(Modifier.fillMaxWidth()) {
                val wideTablet = maxWidth >= 700.dp * LocalDensity.current.fontScale &&
                    LocalConfiguration.current.screenHeightDp >= 600
                if (wideTablet) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement=Arrangement.spacedBy(20.dp),
                        verticalAlignment=Alignment.Top) {
                        AnimalPicture(selection.item,language,images[selection.selected],Modifier.weight(1f),imageHeightOverride=360.dp)
                        ExploreWordControls(selection,language,ready,onReplay,onExample,onSelect,Modifier.weight(1f))
                    }
                } else {
                    Column(Modifier.fillMaxWidth(),verticalArrangement=Arrangement.spacedBy(12.dp)) {
                        AnimalPicture(selection.item,language,images[selection.selected],Modifier.fillMaxWidth())
                        ExploreWordControls(selection,language,ready,onReplay,onExample,onSelect)
                    }
                }
            }
        } else if(state!=null) {
            val canAct=ready && state.language==language
            if(state.phase==SessionPhase.COMPLETED) Text(t("Adventure complete!","Abenteuer geschafft!"),style=MaterialTheme.typography.headlineSmall)
            else NativeQuestionProgress(state.index+1,state.plan.tasks.size,language)
            NativeActionButton(t("Listen again", "Noch einmal hören"), NativeActionRole.SECONDARY, onClick = onReplay,enabled=canAct,modifier=Modifier.fillMaxWidth().heightIn(min=56.dp).testTag("vocabulary-replay"))
            if(state.phase==SessionPhase.ACTIVE) {
                Text(state.task.question.instruction.display[language],style=MaterialTheme.typography.titleLarge)
                if(selection.phase==VocabularyPhase.NAME) AnimalPicture(VocabularyContent.item(state.task.question.correct),language,images[state.task.question.correct],Modifier.fillMaxWidth(),imageHeightOverride=160.dp)
                state.task.question.choices.chunked(2).forEach { row ->
                    Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {row.forEach { id ->
                        val item=VocabularyContent.item(id)
                        Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(4.dp)) {
                            val canAnswer=canAct && state.current.answer==AnswerState.UNANSWERED
                            val answer:()->Unit={state.nextAttempt?.let {onAction(SessionAction.Answer(it,id))};Unit}
                            if(selection.phase==VocabularyPhase.NAME) NativeTextChoice(
                                label=item.text.display[language],
                                onClick=answer,
                                enabled=canAnswer,
                                modifier=Modifier.fillMaxWidth().heightIn(min=72.dp).focusable()
                                    .onKeyEvent { event ->
                                        if (event.type == KeyEventType.KeyUp && (event.key == Key.Enter || event.key == Key.Spacebar) && canAnswer) {
                                            answer(); true
                                        } else false
                                    }.testTag("answer-${id.value}"),
                            ) else Button(answer,
                                enabled=canAct && state.current.answer==AnswerState.UNANSWERED,
                                modifier=Modifier.fillMaxWidth().heightIn(min=72.dp).testTag("answer-${id.value}")) {
                                if(selection.phase==VocabularyPhase.FIND) Column(horizontalAlignment=Alignment.CenterHorizontally) {
                                    AnimalPicture(item,language,images[id],Modifier.fillMaxWidth(),small=true)
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
                    NativeSupportMessage(state.task.question.wrongFeedback.display[language])
                    NativeActionButton(t("Try again", "Nochmal versuchen"), NativeActionRole.SECONDARY, onClick = {onAction(SessionAction.Retry(AttemptId(state.task.id,state.current.attempts)))},enabled=canAct,modifier=Modifier.heightIn(min=56.dp).testTag("vocabulary-retry"))
                }
                if(state.current.support.hint)NativeSupportMessage(state.task.question.hint!!.display[language])
                if(!state.current.locked)NativeActionButton(t("Help", "Hilfe"), NativeActionRole.SECONDARY, onClick = {onAction(SessionAction.Hint(state.task.id))},enabled=canAct,modifier=Modifier.heightIn(min=56.dp).testTag("vocabulary-help"))
                if(state.current.locked)NativeActionButton(t("Next", "Weiter"), NativeActionRole.PRIMARY, onClick = {onAction(SessionAction.Next(state.task.id))},enabled=canAct,modifier=Modifier.fillMaxWidth().heightIn(min=56.dp).testTag("vocabulary-next"))
            }
        }
        NativeActionButton(t("Choose an activity", "Aktivität wählen"), NativeActionRole.SECONDARY,
            onClick=onChooseActivity, enabled=ready, modifier=Modifier.fillMaxWidth().heightIn(min=56.dp).testTag("vocabulary-choose-activity"))
        NativeActionButton(t("Back home", "Zurück zum Start"), NativeActionRole.NAVIGATION, onClick = onHome,modifier=Modifier.fillMaxWidth().heightIn(min=56.dp).testTag("vocabulary-home"))
    }
}

/** Passive fitted artwork; the existing single animal label is independent of bitmap availability. */
@Composable
private fun AnimalPicture(item:VocabularyDefinition,language:ContentLanguage,image:ImageBitmap?,modifier:Modifier,small:Boolean=false,imageHeightOverride:androidx.compose.ui.unit.Dp?=null) {
    val screenWidth=LocalConfiguration.current.screenWidthDp.dp
    val screenHeight=LocalConfiguration.current.screenHeightDp.dp
    val imageHeight=if(small)100.dp else imageHeightOverride ?: when {
        screenHeight<600.dp -> 200.dp
        screenWidth>=700.dp -> 360.dp
        screenWidth>=400.dp -> 300.dp
        else -> 240.dp
    }
    Box(modifier.heightIn(min=if(small)100.dp else imageHeight).semantics {contentDescription=item.text.display[language]},contentAlignment=Alignment.Center) {
        if(image != null) Image(image,contentDescription=null,contentScale=ContentScale.Fit,
            modifier=Modifier.fillMaxWidth().height(if(small)100.dp else imageHeight).testTag("vocabulary-art-${item.id.value}"))
        else Text(if(language==ContentLanguage.GERMAN) "Bild gerade nicht verfügbar" else "Picture unavailable right now",
            modifier=Modifier.testTag("vocabulary-art-unavailable-${item.id.value}").clearAndSetSemantics {})
    }
}

@Composable
private fun ExploreWordControls(
    selection:VocabularySelection,
    language:ContentLanguage,
    ready:Boolean,
    onReplay:()->Unit,
    onExample:()->Unit,
    onSelect:(ContentId)->Unit,
    modifier:Modifier=Modifier,
) {
    fun t(en:String,de:String)=if(language==ContentLanguage.GERMAN)de else en
    Column(modifier,verticalArrangement=Arrangement.spacedBy(12.dp)) {
        Text(t("Explore an animal","Ein Tier entdecken"),style=MaterialTheme.typography.titleLarge)
        Text(selection.item.text.display[language],style=MaterialTheme.typography.headlineMedium,
            modifier=Modifier.testTag("vocabulary-word"))
        NativeActionButton(t("Listen again", "Noch einmal hören"), NativeActionRole.SECONDARY,
            onClick=onReplay,enabled=ready,modifier=Modifier.fillMaxWidth().heightIn(min=56.dp).testTag("vocabulary-replay"))
        Text(selection.item.example.display[language],style=MaterialTheme.typography.titleLarge)
        NativeActionButton(t("Listen to the sentence", "Satz anhören"), NativeActionRole.SECONDARY,
            onClick=onExample,enabled=ready,modifier=Modifier.heightIn(min=56.dp).testTag("vocabulary-example"))
        Text(t("Can you say the word in your own sentence? You can ask someone to help.",
            "Kannst du mit dem Wort einen eigenen Satz sagen? Du kannst dir helfen lassen."))
        Text(t("Choose another animal","Wähle ein anderes Tier"),style=MaterialTheme.typography.titleMedium)
        NativeChoiceRows(VocabularyContent.items) { item ->
            OutlinedButton({onSelect(item.id)},enabled=ready,modifier=Modifier.weight(1f).heightIn(min=56.dp).testTag("word-${item.id.value}")) {
                Text(item.text.display[language])
            }
        }
    }
}
